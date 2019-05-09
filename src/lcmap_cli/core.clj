(ns lcmap-cli.core
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [lcmap-cli.functions :as f]
            [lcmap-cli.changedetection]
            [lcmap-cli.products :as products]
            [lcmap-cli.state :as state]
            [lcmap-cli.training]
            [lcmap-cli.numbers :refer [numberize]])
  (:gen-class :main true))

(defn options
  [keys]
  (let [o {:help     ["-h" "--help"]
           :verbose  [nil  "--verbose"]
           :grid     [nil  "--grid GRID" "grid id" :missing "--grid is required"]
           :dataset  [nil  "--dataset DATASET" "dataset id" :missing "--dataset is required"]
           :x        [nil  "--x X" "projection x coordinate"  :parse-fn numberize :missing "--x is required"]
           :y        [nil  "--y Y" "projection y coordinate" :parse-fn numberize :missing "--y is required"]
           :cx       [nil  "--cx CX" "chip x coordinate" :parse-fn numberize :missing "--cx is required"]
           :cy       [nil  "--cy CY" "chip y coordinate" :parse-fn numberize :missing "--cy is required"]
           :tile     [nil  "--tile TILE" "tile id" :missing "--tile is required"]
           :source   [nil  "--source SOURCE" :missing "--source is required"]
           :date     [nil  "--date" :missing "--date is required"]
           :names    [nil  "--names NAMES" "product names" :missing "--names are required"]
           :years    [nil  "--years YEARS" "years to produce" :missing "--years are required"]
           :acquired [nil  "--acquired ACQUIRED" "iso8601 date range" :missing "--acquired is required"]}]
    (vals (select-keys o keys))))

(defn ->options
  [opts]
  (into [] (options opts)))

(def registry
  {:grids       {:func #'lcmap-cli.functions/grids
                 :args (->options [:help])}                 
   :grid        {:func #'lcmap-cli.functions/grid
                 :args (->options [:help :grid :dataset])}
   :snap        {:func #'lcmap-cli.functions/snap
                 :args (->options [:help :grid :dataset :x :y])}
   :near        {:func #'lcmap-cli.functions/near
                 :args (->options [:help :grid :dataset :x :y])}
   :xy-to-tile  {:func #'lcmap-cli.functions/xy-to-tile
                 :args (->options [:help :grid :dataset :x :y])}
   :tile-to-xy  {:func #'lcmap-cli.functions/tile-to-xy
                 :args (->options [:help :grid :dataset :tile])}
   :chips       {:func #'lcmap-cli.functions/chips
                 :args (->options [:help :grid :dataset :tile])}
   :ingest      {:func nil
                 :args (->options [:help :grid :source])}
   :detect-chip {:func #'lcmap-cli.changedetection/chip
                 :args (->options [:help :grid :cx :cy :acquired])}
   :detect      {:func #'lcmap-cli.changedetection/tile
                 :args (->options [:help :grid :tile :acquired])}
   :train       {:func #'lcmap-cli.training/train
                 :args (->options [:help :grid :tile :acquired :date])}
   :predict     {:func nil
                 :args (->options [:help :grid :tile])}
   :product      {:func #'lcmap-cli.products/product 
                  :args (->options [:help :grid :tile :names :years])}
   :product-chip {:func #'lcmap-cli.products/chip 
                  :args (->options [:help :grid :names :cx :cy :years])}
   :raster       {:func #'lcmap-cli.products/raster 
                  :args (->options [:help :grid :tile :names :years])}
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
  (str "Available actions: " (into [] (map name (keys registry)))))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn function
  [args]
  (get-in registry [(keyword args) :func]))
    
(defn parameters
  [args]
  (let [p (parse-opts args (-> args first keyword registry :args))]
        (assoc p :options (reduce-kv (fn [m k v] (assoc m k (f/trim v)))
                                     {}
                                     (:options p)))))

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args (get-in registry [(keyword (first args)) :args]))
        cmds (into #{} (map name (keys registry)))
        cmd  (first arguments)]

    (cond
      (:help options)
      {:exit-message (usage (first args) summary) :ok? true}

      errors
      {:exit-message (error-msg errors)}

      (nil? (cmds cmd))
      {:exit-message actions}
            
      (and (= 1 (count arguments)) (cmds cmd))
      {:action cmd :options options}

      :else
      {:exit-message (usage (-> args first) summary)})))

(defn exit [status msg]
  (f/stdout msg)
  (System/exit status))

(defn add-shutdown-hook
  []
  (.addShutdownHook (java.lang.Runtime/getRuntime)
                    (Thread. #(state/shutdown) "shutdown-handler")))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    
    (add-shutdown-hook)

    ;;
    ;; Functions must not raise Exceptions
    ;; Function results must be JSON encodable
    ;;
    ;; If an Exception is returned from a function it will halt the CLI/JVM.
    ;; Do not patch this at the -main level.  Catch & convert the stacktrace to a
    ;; string where it occurred.
    ;;
    ;; Simple way to do this is with clojure.stacktrace/print-stack-trace and with-out-str.
    ;; Alternatively, just make sure you do this: (str (Exception. "an exception"))
    ;;
    ;; Example:  (-> (Exception. "an exception") stacktrace/print-stack-trace with-out-str)
    ;;
    ;; Good example non-error response from function is:
    ;; {:cx 123 :cy 456 :acquired "1980/2019"}
    ;;
    ;; Or:
    ;; {:cx 123 :cy 456 :acquired "1980/2019" :other "information" :more "data" :what "ever you need to include in the output"}
    ;;
    ;; Good example error response from function is:
    ;; {:cx 123 :cy 456 :acquired "1980/2019" :error "a-string-and-not-a-stacktrace-object"}
    ;;
    ;;
    ;; (try (something-throws-an-exception)
    ;;      (catch Exception e {:cx 123
    ;;                          :cy 456
    ;;                          :error (-> e print-stack-trace with-out-str)}))
    ;;
    ;; (try (something-throws-an-exception)
    ;;      (catch Exception e {:cx 123
    ;;                          :cy 456
    ;;                          :error (str e)}))
    ;;
    
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (f/output ((function action) options)))))
