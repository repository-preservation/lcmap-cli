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

(defn start-consumers
  [number in-chan out-chan]
  (dotimes [_ number]
    (async/thread
      (while (true? @state/run-threads?)
        (let [input  (async/<!! in-chan)
              result (handler (assoc input :response (f/detect input)))]
          (async/>!! out-chan result))))))

(defn start-aggregator
  [in-chan]
  (async/thread
    (while (true? @state/run-threads?)
      (let [result (async/<!! in-chan)]
        (if (:error result)
          (async/>!! state/stderr result)
          (async/>!! state/stdout (or result "no response")))))))

(defn tile
  [{g :grid t :tile a :acquired :as all}]
  (let [xys        (f/chips (assoc all :dataset "ard"))
        chunk-size (get-in cfg/grids [(keyword g) :segment-instance-count])
        in-chan    state/detect-tile-in
        out-chan   state/detect-tile-out
        consumers  (start-consumers chunk-size in-chan out-chan)
        aggregator (start-aggregator out-chan)
        sleep-for  5000]
             
    (doseq [xy xys]
      (Thread/sleep sleep-for)
      (async/>!! in-chan {:cx (:cx xy)
                          :cy (:cy xy)
                          :acquired a
                          :grid g})))
  all)
  
(defn chip
  [{g :grid cx :cx cy :cy acquired :acquired :as all}]
  (handler (assoc all :response (f/detect all))))