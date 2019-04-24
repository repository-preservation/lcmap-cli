(ns lcmap-cli.prediction
  (:require [cheshire.core :as json]
            [clojure.stacktrace :as st]
            [clojure.walk :refer [keywordize-keys]]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.functions :as f]
            [lcmap-cli.http :as http]))

(defn handler
  [{:keys [:response :tx :ty :grid :acquired :month :day :chips]}]

  (let [r (try {:response @response}
               (catch Exception e {:error (str e)}))]
    
    (cond (:error r)
          {:tx tx :ty ty :acquired acquired :month month :day day :chips chips :error (:error r)}

          (contains? (set (range 200 300))(get-in r [:response :status]))
          (-> (:response r) http/decode :body)
          
          :else {:tx tx :ty ty :acquired acquired :month month :day day :chips chips :error (str r)})))

(defn tile
  [{:keys [:grid :dataset :tile] :as all}]

  ;; f/snap returns this data
  ;;
  ;; {"chip": [{"grid-pt": [854.0, 1105.0], "proj-pt": [-3585.0, -195.0]}],
  ;;  "tile": [{"grid-pt": [16.0, 23.0],    "proj-pt": [-165585.0, -135195.0]}]}

  (let [xy   (f/tile-to-xy all)
        data (merge {:tx (:x xy) :ty (:y xy)} all)]
    
  (assoc data :tile (:tile (-> (merge xy all)
                               f/snap
                               json/decode
                               keywordize-keys)))))

(defn chips
  [{:keys [:grid :dataset :tile] :as all}]
  (let [grid-pt (:grid-pt tile)
        h       (first grid-pt)
        v       (second grid-pt)
        tid     {:grid grid
                 :dataset dataset
                 :tile (f/tile-to-string h v)}]
    (assoc all :chips (map vals (f/chips tid)))))

(defn predict
  [{:keys [:grid :acquired :month :day :tile] :as all}]
    
  (->>(assoc all :dataset "ard")
      tile
      chips
      f/predict
      (assoc all :response)
      handler))
