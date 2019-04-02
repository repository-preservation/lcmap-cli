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
  [{g :grid d :dataset tx :tx ty :ty :as all}]

  ;; f/near returns this data
  ;;
  ;; {"chip": [{"grid-pt": [854.0, 1105.0], "proj-pt": [-3585.0, -195.0]},
  ;;           {"grid-pt": [854.0, 1104.0], "proj-pt": [-3585.0, 2805.0]},], 
  ;;  "tile": [{"grid-pt": [16.0, 23.0],    "proj-pt": [-165585.0, -135195.0]},
  ;;           {"grid-pt": [16.0, 22.0],    "proj-pt": [-165585.0, 14805.0]},]})

  
  (assoc all :tiles (:tile (-> (merge {:x tx :y ty} all)
                                f/near
                                json/decode
                                keywordize-keys))))

(defn chips
  [{g :grid d :dataset t :tiles :as all}]

  (let [tids (map (fn [t] {:grid g
                           :dataset d
                           :tile (f/tile-to-string (first (get :grid-pt t))
                                                   (second (get :grid-pt t)))}) (:tiles all))]
    (assoc all :chips (map f/chips tids))))
                            
(defn train
  [{g :grid tx :tx ty :ty acquired :acquired date :date :as all}]

  (-> (assoc all :dataset "ard")
      tiles
      chips
      f/train
      (assoc all :response)
      handler))
