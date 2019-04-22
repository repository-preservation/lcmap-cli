(ns lcmap-cli.products
  (:require [clojure.core.async :as async]
            [clojure.string :as string]
            [cheshire.core :as json]
            [lcmap-cli.state :as state]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.http :as http]
            [lcmap-cli.functions :as f]
            [lcmap-cli.functions :refer [chips tile-to-xy]]))

(defn handler
  [http_response params]
  (let [r (try @http_response
               (catch Exception e {:error (str e)}))]
    
    (cond (:error r)
          (merge {:error (:error r)} params)

          (contains? (set (range 200 300)) (:status r))
          (-> r http/decode :body)
          
          :else 
          (merge {:error (-> r http/decode :body) :status (:status r)} params))))

(defn post-request
  [{grid :grid resource :resource http-options :http-options :as all}]
  (let [json_body (json/encode (dissoc all :http-options))
        headers {"Content-Type" "application/json"}]
    (http/client :post 
                 (keyword grid) :ccdc (keyword resource)
                 (merge {:body json_body :headers headers} http-options))))

(defn start-consumers
  ([number in-chan out-chan http-options]
   (dotimes [_ number]
     (async/thread
       (while (true? @state/run-threads?)
         (let [input  (async/<!! in-chan)
               params (assoc input :http-options http-options)
               result (handler (post-request params) params)]
           (async/>!! out-chan result))))))
  ([number in-chan out-chan]
   (start-consumers number in-chan out-chan cfg/http-options)))

(defn date-range
  [{grid :grid years :years :as all}]
  (let [year_coll (string/split years #"/")
        start (-> year_coll first read-string)
        stop  (-> year_coll last read-string inc)
        year_range (range start stop)
        mmdd (cfg/product-mmdd grid)]
    (map (fn [i] (str i "-" mmdd)) year_range)))

(defn chip
  [{grid :grid product :product years :years cx :cx cy :cy :as all}]
  (let [tile      (f/xy-to-tile {:grid grid :dataset "ard" :x cx :y cy}) 
        date-coll (date-range all)
        req-args  (-> (dissoc all :years) (assoc :tile tile :dates date-coll :resource "product" :http-options cfg/http-options)) 
        response  (post-request req-args)
        output    (handler response req-args)]
    (f/output output)
    output))

(defn product
  [{grid :grid tile :tile product :product years :years :as all}]
  (let [chunk-size (cfg/product-instance-count grid)
        in-chan    (async/chan)
        out-chan   (async/chan)
        chip_xys   (chips (assoc all :dataset "ard"))
        {tilex :x tiley :y} (tile-to-xy (assoc all :dataset "ard"))
        date-coll  (date-range all)
        consumers  (start-consumers chunk-size in-chan out-chan)
        output_fn  (fn [i] (let [result (async/<!! out-chan)] (f/output result) result))]

    (async/go
      (doseq [cxcy chip_xys]
        (async/>! in-chan (hash-map :grid grid
                                    :tile tile
                                    :cx (:cx cxcy)
                                    :cy (:cy cxcy)
                                    :dates date-coll
                                    :product product
                                    :resource "product"))))

    (doall (map output_fn chip_xys))))

(defn raster
  [{grid :grid tile :tile product :product years :years :as all}]
  (let [chunk-size (cfg/raster-instance-count grid)
        in-chan    (async/chan)
        out-chan   (async/chan)
        chip_xys   (chips (assoc all :dataset "ard"))
        {tilex :x tiley :y} (tile-to-xy (assoc all :dataset "ard"))
        date-coll  (date-range all)
        consumers  (start-consumers chunk-size in-chan out-chan {:timeout 7200000})
        output_fn  (fn [i] (let [result (async/<!! out-chan)] (f/output result) result))]

    (async/go
     (doseq [date date-coll]
       (async/>! in-chan (hash-map :grid grid
                                    :tile tile
                                    :tilex tilex
                                    :tiley tiley
                                    :chips chip_xys
                                    :date date
                                    :product product
                                    :resource "raster"))))

    (doall (map output_fn date-coll))))

