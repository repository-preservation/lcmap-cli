(ns lcmap-cli.core
  (:require [cheshire.core :as json]
            [clojure.core.async :as async]
            [clojure.core.matrix :as matrix]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.http :as http]
            [lcmap.commons.numbers :refer [numberize]])
  (:gen-class :main true))

(matrix/set-current-implementation :vectorz)

(def run-threads? (atom true))
(def stdout (async/chan))
(def stderr (async/chan))
(def detect-tile-in (async/chan))
(def detect-tile-out (async/chan))
(def stdout-writer (async/thread (while (true? @run-threads?) (-> (async/<!! stdout) stringify-keys json/encode println))))

(def stderr-writer (binding [*out* *err*] (async/thread (while (true? @run-threads?) (-> (async/<!! stderr) stringify-keys json/encode println)))))

  ; -------------------------------------------------------------------------------------------------------
  ; The cli will automatically resolve functions at the command prompt to functions
  ; contained within the lcmap-cli.core namespace.  In order to be resolved, they (obviously) must be defined
  ; and must exist in cli-options.
  ;
  ; Validation works pretty well when a key is provided but a value is missing or incorrect.
  ;
  ; Validation currently does not work very well when a required key is missing.  In order
  ; to handle this, a series of clojure specs should be written for each function and
  ; then applied to the function prior to invoking it.  If validation fails then a good
  ; error message can be returned to the command prompt.  This spec mechanism is not in place.
  ;
  ; Current TODO:
  ; - Fill out xy-to-tile, tile-to-xy & chips (done)
  ; - Wire up detect, run detect concurrently based on configuration.
  ; - Push the configuration into an edn or json file using the default location of ~/.lcmap/lcmap-cli.edn
  ; - Clean up the error messages returned to the user when an exception occurs.
  ; - WRITE TESTS
  ; -------------------------------------------------------------------------------------------------------

(defn transform-matrix
  "Produce transform matrix from given grid-spec."
  [grid-spec]
  (let [rx (grid-spec :rx)
        ry (grid-spec :ry)
        sx (grid-spec :sx)
        sy (grid-spec :sy)
        tx (grid-spec :tx)
        ty (grid-spec :ty)]
    [[(/ rx sx)        0  (/ tx sx)]
     [       0  (/ ry sy) (/ ty sy)]
     [       0         0       1.0 ]]))

(defn point-matrix
  "Produce a homogeneous matrix from a map containing an :x and :y point."
  [params]
  (let [x (-> params :x numberize)
        y (-> params :y numberize)]
    [[x]
     [y]
     [1]]))

(defn tile->projection
  [{:keys [:h :v :grid]}]
  (let [pm (point-matrix {:x h :y v})
        tm (transform-matrix grid)
        m  (matrix/mmul (matrix/inverse tm) pm)]
    {:x (ffirst m)
     :y (first (second m))}))
    
(defn grids
  ([] (keys cfg/grids))
  ([_](grids)))

(defn grid
  [{:keys [grid dataset]}]
  (:body @(http/client :get (keyword grid) (keyword dataset) :grid nil)))

(defn snap
  [{:keys [grid dataset x y]}]
  (:body @(http/client :get
                       (keyword grid)
                       (keyword dataset)
                       :snap
                       {:query-params {:x x :y y}})))

(defn near
  [{:keys [grid dataset x y]}]
  (:body @(http/client :get
                       (keyword grid)
                       (keyword dataset)
                       :near
                       {:query-params {:x x :y y}})))

(defn tile-grid
  [{g :grid d :dataset :as all}]
  (->> all
       grid
       json/decode
       keywordize-keys
       (filter #(= "tile" (:name %)))
       first))

(defn chip-grid
  [{g :grid d :dataset :as all}]
  (->> all
       grid
       json/decode
       keywordize-keys
       (filter #(= "chip" (:name %)))
       first))

(defn lstrip0
  "Remove leading zeros from a string."
  [t]
  (loop [t t]
    (if (and (= "0" (str (first t))) (> 1 (count t)))
      (recur (rest t))
      (string/join t))))

(defn string->tile
  [tile-id]
  {:h (numberize (lstrip0 (subs tile-id 0 3)))
   :v (numberize (lstrip0 (subs tile-id 3)))})

(defn tile->string
  [h v]
  (format "%03d%03d" h v))

(s/def ::x #(numberize %))
(s/def ::y #(numberize %))
(s/def ::tile (s/and string? #(re-matches #"\d{6}" %)))
(s/def ::grid (s/and string? (fn [x] (some #(= x %) (->> (grids) json/decode)))))
(s/def ::dataset string?)
(s/def :tile/dispatch (s/or :xy   (s/keys :req-un [::grid ::dataset ::x ::y])
                            :tile (s/keys :req-un [::grid ::dataset ::tile])))                        
(defn xy-to-tile
  [{g :grid d :dataset x :x y :y :as all}]
  (let [{:keys [:grid-pt]} (:tile (-> all snap json/decode keywordize-keys))
        h (int (first grid-pt))
        v (int (second grid-pt))]
    (tile->string h v)))

(defn tile-to-xy
  [{g :grid d :dataset t :tile :as all}]
  (let [tg (tile-grid all)
        {:keys [:rx :ry :tx :ty :sx :sy]} tg
        {:keys [:h :v]} (string->tile t)]
    (tile->projection {:h h :v v :grid tg})))
    
(defn chips
  [{g :grid d :dataset t :tile :as all}]
  (let [point (-> all tile-to-xy)
        cgrid (chip-grid all)
        tgrid (tile-grid all)
        x-start (:x point)
        y-start (:y point)
        x-stop  (+ x-start (* (:rx tgrid) (:sx tgrid)))
        y-stop  (+ y-start (* (:ry tgrid) (:sy tgrid)))
        x-interval (* (:rx cgrid) (:sx cgrid))
        y-interval (* (:ry cgrid) (:sy cgrid))]

    (for [x (range x-start x-stop x-interval)
          y (range y-start y-stop y-interval)]
      {:cx x :cy y})))

(defn ingest [] nil)
(defn ingest-list-available [] nil)
(defn ingest-list-completed [] nil)

(defn detect
  [{g :grid cx :cx cy :cy}]
  (http/client :post
               (keyword g)
               :ccdc
               :segment
               {:body (json/encode {:cx cx :cy cy})
                :headers {"Content-Type" "application/json"}}))

(defn detect-handler
  [{:keys [:response :cx :cy :grid]}]

  (let [r (try {:response @response}
               (catch Exception e {:error e}))]
    
    (cond (:error r)
          {:cx cx :cy cy :error (:error r)}

          (contains? (set (range 200 300))(get-in r [:response :status]))
          (-> (:response r) http/decode :body)
          
          :else {:cx cx :cy cy :error r})))

(defn start-detect-consumers
  [number in-chan out-chan]
  (dotimes [_ number]
    (async/thread
      (while (true? @run-threads?)
        (let [input  (async/<!! in-chan)
              result (detect-handler (assoc input :response (detect input)))]
          (async/>!! out-chan result))))))

(defn start-detect-aggregator
  [in-chan]
  (async/thread
    (while (true? @run-threads?)
      (let [result (async/<!! in-chan)]
        (if (:error result)
          (async/>!! stderr result)
          (async/>!! stdout (or result "no response")))))))

(defn detect-tile
  [{g :grid t :tile :as all}]
  (let [xys        (chips (assoc all :dataset "ard"))
        chunk-size (get-in cfg/grids [(keyword g) :segment-instance-count])
        in-chan    detect-tile-in
        out-chan   detect-tile-out
        consumers  (start-detect-consumers chunk-size in-chan out-chan)
        aggregator (start-detect-aggregator out-chan)
        sleep-for  5000]
             
    (doseq [xy xys]
      (Thread/sleep sleep-for)
      (async/>!! in-chan {:cx (:cx xy)
                          :cy (:cy xy)
                          :grid g})))
  all)
  
(defn detect-chip
  [{g :grid cx :cx cy :cy :as all}]
  (detect-handler (assoc all :response (detect all))))
  
(defn train [] nil)
(defn predict [] nil)
(defn product-maps [] nil)

;;(defn detect-tile-orig
;;  [{g :grid t :tile :as all}]
;;  (let [xys        (chips (assoc all :dataset "ard"))
;;        chunk-size (get-in cfg/grids [(keyword g) :segment-instance-count])
;;        chunks     (partition chunk-size chunk-size nil xys)]
;;    (for [chunk chunks]
      ;;(-> (vector (map detect (map #({:grid g :x (:x %) :y (:y %)}) chunk)))
      ;;    (map #(-> % deref http/decode :body))))))
;;      (->> (map (fn [v] {:grid g :cx (:cx v) :cy (:cy v)}) chunk)
;;           (map detect)
;;           (vec)
;;           (pmap (fn [r]
;;                   (-> r deref http/decode :body)))))))

(defn options
  [keys]
  (let [o {:help    ["-h" "--help"]
           :verbose [nil  "--verbose"]
           :grid    [nil  "--grid GRID" "grid id"]
           :dataset [nil  "--dataset DATASET" "dataset id"]
           :x       [nil  "--x X" "projection x coordinate"  :parse-fn numberize]
           :y       [nil  "--y Y" "projection y coordinate" :parse-fn numberize]
           :cx      [nil  "--cx CX" "chip x coordinate" :parse-fn numberize]
           :cy      [nil  "--cy CY" "chip y coordinate" :parse-fn numberize]
           :tile    [nil  "--tile TILE" "tile id"]
           :source  [nil  "--source"]
           :start   [nil  "--start"]
           :end     [nil  "--end"]}]
    (vals (select-keys o keys))))

(def cli-options
  {:grids                 (into [] (options [:help]))
   :grid                  (into [] (options [:help :grid :dataset]))
   :snap                  (into [] (options [:help :grid :dataset :x :y]))
   :near                  (into [] (options [:help :grid :dataset :x :y]))
   :xy-to-tile            (into [] (options [:help :grid :dataset :x :y]))
   :tile-to-xy            (into [] (options [:help :grid :dataset :tile]))
   :chips                 (into [] (options [:help :grid :dataset :tile]))
   :ingest                (into [] (options [:help :grid :source]))
   :ingest-list-available (into [] (options [:help :grid :start :end]))
   :ingest-list-completed (into [] (options [:help :grid :start :end]))
   :detect-chip           (into [] (options [:help :grid :cx :cy]))
   :detect-tile           (into [] (options [:help :grid :tile]))
   :train                 (into [] (options [:help :grid :tile]))
   :predict               (into [] (options [:help :grid :tile]))
   :product-maps          (into [] (options [:help :grid]))
   })

 (defn usage [action options-summary]
  (->> ["lcmap command line interface"
        ""
        (str "Usage: lcmap " action " [options]" )
        ""
        "Options:"
        options-summary
        ""
        "Please refer https://github.com/usgs-eros/lcmap-cli for more information."]
       (string/join \newline)))

(def actions
  (str "Available actions: " (into [] (map name (keys cli-options)))))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn function
  [args]
  (->> args str (symbol "lcmap-cli.core") resolve))

(defn ->trim
  [v]
  (if (string? v)
    (string/trim v)
    v))

(defn parameters
  [args]
  (let [p (parse-opts args (-> args first keyword cli-options))]
        (assoc p :options (reduce-kv (fn [m k v] (assoc m k (->trim v)))
                                     {}
                                     (:options p)))))

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args (-> args first keyword cli-options))
        cmds (into #{} (map name (keys cli-options)))
        cmd  (-> arguments first)]
       
    (cond
      (:help options)
      {:exit-message (usage (-> args first) summary) :ok? true}

      errors
      {:exit-message (error-msg errors)}

      (nil? (cmds cmd))
      {:exit-message actions}
            
      (and (= 1 (count arguments)) (cmds cmd))
      {:action cmd :options options}

      :else
      {:exit-message (usage (-> args first) summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn finalize
  [& args]
  (do
    (swap! run-threads? #(boolean false))
    (async/close! stdout)
    (async/close! stderr)
    (async/close! detect-tile-in)
    (async/close! detect-tile-out)))

(defn add-shutdown-hook
  []
  (.addShutdownHook (java.lang.Runtime/getRuntime)
                    (Thread. #(finalize) "shutdown-handler")))


(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]

    (add-shutdown-hook)

    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (try
        (->> ((function action) options) (async/>!! stdout))
        (catch Exception e
          (async/>!! stderr (.toString e)))))))
