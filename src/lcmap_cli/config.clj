(ns lcmap-cli.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [environ.core :refer [env]])
  
  (:import java.io.PushbackReader))

(def default-edn-file (str (System/getProperty "user.home") "/.usgs/lcmap-cli.edn"))

(def edn-file (or (-> env :edn-file) default-edn-file))

(defn load-edn
  [source]
  (try
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))
    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e))))) 

(def grids
  (if-let [c (load-edn edn-file)]
    (:grids c)
    nil))

(def http-options
  (if-let [c (load-edn edn-file)]
    (:http c)))

(defn product-instance-count
  [grid]
  (:product-instance-count ((keyword grid) grids)))

(defn raster-instance-count
  [grid]
  (:raster-instance-count ((keyword grid) grids)))

(defn product-mmdd
  [grid]
  (let [mm (:product-month ((keyword grid) grids))
        dd (:product-day   ((keyword grid) grids))]
    (str mm "-" dd)))
