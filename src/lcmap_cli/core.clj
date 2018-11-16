(ns lcmap-cli.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [lcmap-cli.config :as cfg])
  (:gen-class))


(comment 
(defn grid
  [grid src]
  (-> @(client :get grid src :grid nil)
      decode
      :body))

(defn snap
  [grid src x y]
  (-> @(client :get grid src :snap {:query-params {:x x :y y}})
      decode
      :body))

(defn near
  [grid src x y]
  (-> @(client :get grid src :near {:query-params {:x x :y y}})
      decode
      :body))

(defn tile
  ([grid src tile] nil)
  ([grid src x y] nil))

(defn chips
  [grid tile]
  nil)

(defn ingest
  [grid layer]
  nil)

(defn detect
  ([grid src x y]
   (-> @(client :post grid :ccdc :segment {:query-params {:cx x :cy y}})
       decode
       :body))
  ([grid tile]
   "Find all chips in tile, run them concurrently based on config"
   (chips grid tile)
   nil))
  
(defn train
  [grid tile]
  nil)

(defn predict
  ([grid x y] nil)
  ([grid tile] nil)))



(defn grid
  []
  (keys cfg/grids))

(defn grid-show [{:keys [grid]}] nil)

(defn grid-snap [{:keys [grid x y]}] nil)

(defn grid-near [] nil)

(defn tile-lookup [] nil)

(defn tile-chips [] nil)

(defn ingest [] nil)

(defn ingest-list-available [] nil)

(defn ingest-list-completed [] nil)

(defn detect [] nil)

(defn detect-list-available [] nil)

(defn detect-list-completed [] nil)

(defn train [] nil)

(defn train-list-available [] nil)

(defn train-list-completed [] nil)

(defn predict [] nil)

(defn predict-list-available [] nil)

(defn predict-list-completed [] nil)

(defn product-maps [] nil)

(defn validate-args
  [args]
  args)

(defn exit [status msg]
  (println msg)
  (System/exit status))

(comment https://github.com/clojure/tools.cli#example-usage)

(def cli-options
  [;; First three strings describe a short-option, long-option with optional
   ;; example argument description, and a description. All three are optional
   ;; and positional.
   ["-p" "--port PORT" "Port number"
    :default 80
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   
   ;; If no required argument description is given, the option is assumed to
   ;; be a boolean option defaulting to nil
   [nil "--detach" "Detach from controlling process"]
   ["-v" nil "Verbosity level; may be specified multiple times to increase value"
    ;; If no long-option is specified, an option :id must be given
    :id :verbosity
    :default 0
    ;; Use :update-fn to create non-idempotent options (:default is applied first)
    :update-fn inc]
   ;; A boolean option that can explicitly be set to false
   ["-d" "--[no-]daemon" "Daemonize the process" :default true]
   ["-h" "--help"]])

(defn usage
  []
  "command not found")

(defn action
  [arguments]
  (->> (string/split arguments #" ")
       (filter (complement string/blank?))
       (map (comp string/lower-case string/trim))
       (interpose "-")
       (string/join)
       not-empty))

(defn -main [& args]  
  (let [{:keys [arguments options summary errors]} (parse-opts args [])
        target (or (-> arguments first action) "_")
        func   (or (-> target symbol resolve) usage)]
            (apply func options)))

(comment
      (case (action arguments)
        "grid" (grid)
        "grid-show" (grid-show options)
        "grid-snap" (grid-snap options)
        "grid-near" (grid-near options)
        "tile-lookup" (tile-lookup options)
        "tile-chips" (tile-chips options)
        "ingest" (ingest options)
        "ingest-list-available" (ingest-list-available options)
        "ingest-list-completed" (ingest-list-completed options)
        "detect" (detect options)
        "detect-list-available" (detect-list-available options)
        "detect-list-completed" (detect-list-completed options)
        "train" (train options)
        "train-list-available" (train-list-available options)
        "train-list-completed" (train-list-completed options)
        "predict" (predict options)
        "predict-list-available" (predict-list-available options)
        "predict-list-completed" (predict-list-completed options)
        "product-maps" (product-maps options)))
