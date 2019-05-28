(ns lcmap-cli.functions
  (:require [cheshire.core :as json]
            [clojure.core.matrix :as matrix]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.http :as http]
            [lcmap-cli.numbers :refer [numberize]]))

(matrix/set-current-implementation :vectorz)

(defn to-json
  [msg]
  (json/encode (stringify-keys msg)))

(defn stdout
  [msg]
  (println msg)
  msg)

(defn stderr
  [msg]
  (binding [*out* *err*]
    (println msg))
  msg)

(defn output
  [result]
  (if (:error result)
    (-> result to-json stderr)
    (-> result to-json stdout))
  result)

(defn trim
  [v]
  (if (string? v)
    (string/trim v)
    v))


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

(defn tile-to-projection
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
  [s]
  (loop [t s]
    (if (and (= "0" (str (first t))) (< 1 (count t)))
      (recur (rest t))
      (string/join t))))

(defn string-to-tile
  [tile-id]
  {:h (numberize (lstrip0 (subs tile-id 0 3)))
   :v (numberize (lstrip0 (subs tile-id 3)))})

(defn tile-to-string
  [h v]
  (format "%03d%03d" (int h) (int v)))

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
    (tile-to-string h v)))

(defn tile-to-xy
  [{g :grid d :dataset t :tile :as all}]
  (let [tg (tile-grid all)
        {:keys [:rx :ry :tx :ty :sx :sy]} tg
        {:keys [:h :v]} (string-to-tile t)]
    (tile-to-projection {:h h :v v :grid tg})))
    
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

(defn detect
  [{g :grid cx :cx cy :cy acquired :acquired}]
  (http/client :post
               (keyword g)
               :ccdc
               :segment
               {:body (json/encode {:cx cx :cy cy :acquired acquired})
                :headers {"Content-Type" "application/json"}}))

(defn train
  [{:keys [:grid :tx :ty :acquired :date :chips]}]
  (http/client :post
               (keyword grid)
               :ccdc
               :tile
               {:body (json/encode {:tx tx :ty ty :acquired acquired :date date :chips chips})
                :headers {"Content-Type" "application/json"}}))

(defn predict
  [{:keys [:grid :tx :ty :month :day :acquired :chips] :as all}]
  (http/client :post
               (keyword grid)
               :ccdc
               :gprediction
               {:body (json/encode {:tx tx :ty ty :month month :day day :acquired acquired :chips chips})
                :headers {"Content-Type" "application/json"}}))

