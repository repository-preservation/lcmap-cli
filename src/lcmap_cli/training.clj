(ns lcmap-cli.training
  (:require [cheshire.core :as json]
            [clojure.stacktrace :as st]
            [clojure.walk :refer [keywordize-keys]]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.functions :as f]
            [lcmap-cli.http :as http]))

(defn handler
  [{:keys [:response :tx :ty :grid :acquired :date :chips]}]

  (let [r (try {:response @response}
               (catch Exception e {:error (str e)}))]
    
    (cond (:error r)
          {:tx tx :ty ty :acquired acquired :date date :chips chips :error (:error r)}

          (contains? (set (range 200 300))(get-in r [:response :status]))
          (-> (:response r) http/decode :body)
          
          :else {:tx tx :ty ty :acquired acquired :date date :chips chips :error (str r)})))


(defn tiles
  [{:keys [:grid :dataset :tile] :as all}]

  ;; f/near returns this data
  ;;
  ;; {"chip": [{"grid-pt": [854.0, 1105.0], "proj-pt": [-3585.0, -195.0]},
  ;;           {"grid-pt": [854.0, 1104.0], "proj-pt": [-3585.0, 2805.0]},], 
  ;;  "tile": [{"grid-pt": [16.0, 23.0],    "proj-pt": [-165585.0, -135195.0]},
  ;;           {"grid-pt": [16.0, 22.0],    "proj-pt": [-165585.0, 14805.0]},]})

  (let [xy   (f/tile-to-xy all)
        data (merge {:tx (:x xy) :ty (:y xy)} (merge xy all))]
    
  (assoc data :tiles (:tile (-> data
                               f/near
                               json/decode
                               keywordize-keys)))))
   


(defn chips
  [{:keys [:grid :dataset :tiles] :as all}]
  
  (let [tids (map (fn [t] {:grid grid
                           :dataset dataset
                           :tile (f/tile-to-string (first  (:grid-pt t))
                                                   (second (:grid-pt t)))}) tiles)]
    
    (assoc all :chips (map vals (flatten (map f/chips tids))))))


(defn train
  [{:keys [:grid :acquired :date :tile] :as all}]
    
  (->>(assoc all :dataset "ard")
      tiles
      chips
      f/train
      (assoc all :response)
      handler))
