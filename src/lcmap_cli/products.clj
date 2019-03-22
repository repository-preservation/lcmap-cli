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
  [http_response]
  (let [r (try @http_response
               (catch Exception e {:error (str e)}))]
    
    (cond (:error r)
          {:error (:error r)}

          (contains? (set (range 200 300)) (:status r))
          (-> r http/decode :body)
          
          :else 
          {:error (-> r http/decode :body) :status (:status r)})))

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
               result (handler (post-request (assoc input :http-options http-options)))]
           (async/>!! out-chan result))))))
  ([number in-chan out-chan]
   (start-consumers number in-chan out-chan cfg/http-options)))

(defn date-range
  [{grid :grid years :years :as all}]
  (let [year_coll (string/split years #"/")
        start (-> year_coll first read-string)
        stop  (-> year_coll last read-string inc)
        year_range (range start stop)
        doy (cfg/product-doy grid)]
    (map (fn [i] (str i "-" doy)) year_range)))

(defn products
  [{grid :grid tile :tile product :product years :years :as all}]
  (let [chunk-size (cfg/request-instance-count grid)
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
                                    :resource "products"))))

    (map output_fn chip_xys)))

(defn maps
  [{grid :grid tile :tile product :product years :years :as all}]
  (let [chunk-size (cfg/request-instance-count grid)
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
                                    :resource "maps"))))

    (map output_fn date-coll)))

