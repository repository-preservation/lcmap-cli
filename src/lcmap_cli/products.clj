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
        (http/decode r)
      
      :else
      (assoc _resp :error r))))

(defn product-request
  [{grid :grid :as all}]
  (http/client :post 
               (keyword grid) :ccdc :products
               {:body (json/encode all)
                :headers {"Content-Type" "application/json"}}))

(defn date-range
  [{grid :grid years :years :as all}]
  (let [year_coll (string/split years #"-")
        start (-> year_coll first read-string)
        stop  (-> year_coll last read-string inc)
        year_range (range start stop)
        doy (cfg/product-doy grid)]
    (map (fn [i] (str i "-" doy)) year_range)))

(defn tile
  [{grid :grid tile :tile product :product years :years :as all}]
  (let [chunk-size (cfg/segment-instance-count grid)
        in-chan    state/tile-in
        out-chan   state/tile-out
        chip_xys   (chips (assoc all :dataset "ard"))
        {tilex :x tiley :y} (tile-to-xy (assoc all :dataset "ard"))
        date-coll  (date-range all)]

    (f/start-consumers chunk-size in-chan out-chan response-handler product-request)
    (f/start-aggregator out-chan)
    (doseq [cxcy chip_xys]
      ;(Thread/sleep sleep-for)
      (async/>!! in-chan (assoc all :tilex tilex
                                    :tiley tiley
                                    :chipx (:cx cxcy)
                                    :chipy (:cy cxcy)
                                    :product product
                                    :dates date-coll)))))

(defn available
  [& args]
  true)


