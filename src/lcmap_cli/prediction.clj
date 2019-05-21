(ns lcmap-cli.prediction
  (:require [cheshire.core :as json]
            [clojure.stacktrace :as st]
            [clojure.walk :refer [keywordize-keys]]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.functions :as f]
            [lcmap-cli.http :as http]))


;; having trouble aligning the response from the data input due to transformation from tile to tx/ty
(defn handler
  [{:keys [:response :tx :ty :grid :acquired :month :day :tile :chips]}]

  (let [r (try {:response @response}
               (catch Exception e {:error (str e)}))]
    
    (cond (:error r)
          {:tile tile :acquired acquired :month month :day day :chips chips :error (:error r)}

          (contains? (set (range 200 300))(get-in r [:response :status]))
          (-> (:response r) http/decode :body (merge :tile tile))
          
          :else {:tile tile :acquired acquired :month month :day day :chips chips :error (str r)})))


(defn tile-snap
  [{:keys [:grid :dataset :tile] :as all}]

  ;; f/snap returns this data
  ;;
  ;; {"chip": [{"grid-pt": [854.0, 1105.0], "proj-pt": [-3585.0, -195.0]}],
  ;;  "tile": [{"grid-pt": [16.0, 23.0],    "proj-pt": [-165585.0, -135195.0]}]}

  (let [xy   (f/tile-to-xy all)
        data (merge {:tx (:x xy)
                     :ty (:y xy)}
                    all)]
    
    (:tile (-> (merge xy all) f/snap json/decode keywordize-keys))))


(defn chips
  [{:keys [:grid :dataset]} {:keys [:grid-pt]}]
  (let [h (first grid-pt)
        v (second grid-pt)
        t {:grid grid
           :dataset dataset
           :tile (f/tile-to-string h v)}]
    (map vals (f/chips t))))


(defn predict
  [{:keys [:grid :acquired :month :day :tile] :as all}]

  (let [a (assoc all :dataset "ard")
        t (tile-snap a)
        r (merge all {:tx    (-> t :proj-pt first)
                      :ty    (-> t :proj-pt second)
                      :chips (chips a t)})
        p (f/predict r)]
    (handler (assoc r :response p))))
