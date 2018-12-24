(ns lcmap-cli.changedetection
  (:require [clojure.core.async :as async]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.functions :as f]
            [lcmap-cli.http :as http]
            [lcmap-cli.state :as state]))

(defn handler
  [{:keys [:response :cx :cy :grid :acquired]}]

  (let [r (try {:response @response}
               (catch Exception e {:error e}))]
    
    (cond (:error r)
          {:cx cx :cy cy :acquired acquired :error (:error r)}

          (contains? (set (range 200 300))(get-in r [:response :status]))
          (-> (:response r) http/decode :body)
          
          :else {:cx cx :cy cy :acquired acquired :error r})))

(defn tile
  [{g :grid t :tile a :acquired :as all}]
  (let [xys        (f/chips (assoc all :dataset "ard"))
        chunk-size (get-in cfg/grids [(keyword g) :segment-instance-count])
        in-chan    state/tile-in
        out-chan   state/tile-out
        consumers  (f/start-consumers chunk-size in-chan out-chan handler f/detect)
        sleep-for  (get-in cfg/grids [(keyword g) :segment-sleep-for])]
    
    (async/go (doseq [xy xys]
                (Thread/sleep sleep-for)
                (async/>! in-chan {:cx (:cx xy)
                                   :cy (:cy xy)
                                   :acquired a
                                   :grid g})))
    (dotimes [i (count xys)]
      (let [result (async/<!! out-chan)]
        (if (:error result)
          (f/stderr (f/->json result))
          (f/stdout (f/->json (or result "no response")))))))
  all)
      
  
(defn chip
  [{g :grid cx :cx cy :cy acquired :acquired :as all}]
  (handler (assoc all :response (f/detect all))))
