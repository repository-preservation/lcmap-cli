(ns lcmap-cli.parallel-prediction
  (:require [clojure.core.async :as async]
            [clojure.stacktrace :as st]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.functions :as f]
            [lcmap-cli.http :as http]
            [lcmap-cli.state :as state]))

(defn handler
  [{:keys [:response :tx :ty :cx :cy :grid :month :day :acquired]}]

  (let [r (try {:response @response}
               (catch Exception e {:error (str e)}))]
    
    (cond (:error r)
          {:tx tx
           :ty ty
           :cx cx
           :cy cy
           :month month
           :day day
           :acquired acquired
           :error (:error r)}

          (contains? (set (range 200 300))(get-in r [:response :status]))
          (-> (:response r) http/decode :body)
          
          :else
          {:tx tx
           :ty ty
           :cx cx
           :cy cy
           :month month
           :day day
           :acquired acquired
           :error (str r)})))

(defn start-consumers
  [number in-chan out-chan]
  (dotimes [_ number]
    (async/thread
      (while (true? @state/run-threads?)
        (let [input  (async/<!! in-chan)
              result (handler (assoc input :response (f/predict input)))]
          (async/>!! out-chan result))))))

(defn tile
  [{g :grid t :tile m :month d :day a :acquired :as all}]
  (let [txy        (f/tile-to-xy (assoc all :dataset "ard"))
        xys        (f/chips (assoc all :dataset "ard"))
        chunk-size (get-in cfg/grids [(keyword g) :prediction-instance-count])
        in-chan    (async/chan)
        out-chan   (async/chan)
        consumers  (start-consumers chunk-size in-chan out-chan)
        sleep-for  (get-in cfg/grids [(keyword g) :prediction-sleep-for])]
    
    (async/go (doseq [xy xys]
                (Thread/sleep sleep-for)
                (async/>! in-chan {:tx (:x txy)
                                   :ty (:y txy)
                                   :cx (:cx xy)
                                   :cy (:cy xy)
                                   :month m
                                   :day d
                                   :acquired a
                                   :grid g})))
    (dotimes [i (count xys)]
      (f/output (async/<!! out-chan))))
  
    all)
      
  
(defn chip
  [{g :grid tx :tx ty :ty cx :cx cy :cy m :month d :day acquired :acquired :as all}]
  (handler (assoc all :response (f/predict all))))
