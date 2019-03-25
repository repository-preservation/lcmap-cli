(ns lcmap-cli.changedetection
  (:require [clojure.core.async :as async]
            [clojure.stacktrace :as st]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.functions :as f]
            [lcmap-cli.http :as http]
            [lcmap-cli.state :as state]))

(defn handler
  [{:keys [:response :cx :cy :grid :acquired]}]

  (let [r (try {:response @response}
               (catch Exception e {:error (str e)}))]
    
    (cond (:error r)
          {:cx cx :cy cy :acquired acquired :error (:error r)}

          (contains? (set (range 200 300))(get-in r [:response :status]))
          (-> (:response r) http/decode :body)
          
          :else {:cx cx :cy cy :acquired acquired :error (str r)})))

(defn start-consumers
  [number in-chan out-chan]
  (dotimes [_ number]
    (async/thread
      (while (true? @state/run-threads?)
        (let [input  (async/<!! in-chan)
              result (handler (assoc :response (f/detect input)))]
          (async/>!! out-chan result))))))

(defn tile
  [{g :grid t :tile a :acquired :as all}]
  (let [xys        (f/chips (assoc all :dataset "ard"))
        chunk-size (get-in cfg/grids [(keyword g) :segment-instance-count])
        in-chan    (async/chan)
        out-chan   (async/chan)
        consumers  (start-consumers chunk-size in-chan out-chan)
        sleep-for  (get-in cfg/grids [(keyword g) :segment-sleep-for])]
    
    (async/go (doseq [xy xys]
                (Thread/sleep sleep-for)
                (async/>! in-chan {:cx (:cx xy)
                                   :cy (:cy xy)
                                   :acquired a
                                   :grid g})))
    (dotimes [i (count xys)]
      (f/output (async/<!! out-chan))))
  
    all)
      
  
(defn chip
  [{g :grid cx :cx cy :cy acquired :acquired :as all}]
  (handler (assoc all :response (f/detect all))))
