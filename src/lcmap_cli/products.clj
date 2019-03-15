(ns lcmap-cli.products
  (:require [clojure.math.combinatorics :refer [cartesian-product]] ; to support multiple products + dates
            [clojure.core.async :as async]
            [clojure.string :as string]
            [cheshire.core :as json]
            [lcmap-cli.state :as state]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.http :as http]
            [lcmap-cli.functions :as f]
            [lcmap-cli.functions :refer [chips tile-to-xy]]))

(defn response-handler
  [{:keys [:response :grid :date :product :tilex :tiley :tile]}]
  (let [r (try @response
               (catch Exception e {:error e}))
        _resp {:tilex tilex :tiley tiley :tile tile :date date :product product}]
   (cond 
      (:error r)
        (assoc _resp :error (:error r))

      (contains? (set (range 200 300)) (:status r))
        (:body r)
      
      :else
      (assoc _resp :error r))))

(defn post-request
  [{grid :grid resource :resource :as all}]
  (let [json_body (json/encode all)]
    (http/client :post 
                 (keyword grid) :ccdc (keyword resource)
                 {:body json_body
                  :headers {"Content-Type" "application/json"}})))

(defn start-consumers
  [number in-chan out-chan]
  (dotimes [_ number]
    (async/thread
      (while (true? @state/run-threads?)
        (let [input  (async/<!! in-chan)
              result (response-handler (hash-map :response (post-request input)))]
          (async/>!! out-chan result))))))

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
  (let [chunk-size (cfg/segment-instance-count grid)
        in-chan    state/tile-in
        out-chan   state/tile-out
        chip_xys   (chips (assoc all :dataset "ard"))
        {tilex :x tiley :y} (tile-to-xy (assoc all :dataset "ard"))
        date-coll  (date-range all)
        consumers  (start-consumers chunk-size in-chan out-chan)
        sleep-for  (get-in cfg/grids [(keyword grid) :segment-sleep-for])        ]

    (async/go
      (doseq [cxcy chip_xys]
        (async/>! in-chan (hash-map :grid grid
                                    :tile tile
                                    :cx (:cx cxcy)
                                    :cy (:cy cxcy)
                                    :dates date-coll
                                    :product product
                                    :resource "products"))))

    (let [results (map (fn [i] (async/<!! out-chan)) chip_xys)
          successes_errors (split-with (fn [i] (nil? (:error i))) results)]
      (hash-map :success (first successes_errors)
                :error (last successes_errors)))))

(defn maps
  [{grid :grid tile :tile product :product years :years :as all}]
  (let [chunk-size (cfg/segment-instance-count grid)
        in-chan    state/tile-in
        out-chan   state/tile-out
        chip_xys   (chips (assoc all :dataset "ard"))
        {tilex :x tiley :y} (tile-to-xy (assoc all :dataset "ard"))
        date-coll  (date-range all)
        consumers  (start-consumers chunk-size in-chan out-chan)
        sleep-for  (get-in cfg/grids [(keyword grid) :segment-sleep-for])]

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

    (let [results (map (fn [i] (async/<!! out-chan) ) date-coll)
          successes_errors (split-with (fn [i] (nil? (:error i))) results)]
      (hash-map :success (first successes_errors)
                :error (last successes_errors)))))


