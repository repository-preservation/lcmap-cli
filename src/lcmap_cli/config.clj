(ns lcmap-cli.config
  (:require [clojure.edn  :as edn]
            [environ.core :as environ]))

(defn read-cfg
  "Read config file from ~/.usgs/lcmap-cli.edn"
  []
  (let [cfg (or (:lcmap-cli-edn environ/env) "resources/lcmap-cli.edn")]
    (edn/read-string (slurp cfg))))

(def http-options
  (:http-options (read-cfg)))
  
(def grids
  (:grids (read-cfg)))

(defn segment-instance-count
  [grid]
  (:segment-instance-count ((keyword grid) grids)))

(defn product-doy
  [grid]
  (:product-doy ((keyword grid) grids)))
