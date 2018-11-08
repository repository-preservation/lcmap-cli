(ns lcmap-cli.grid
  (:require [cheshire.core :as json]
            [lcmap-cli.config :as cfg]))


(defn <-
  [src resource params]
  (str (src (cfg/environment)) (resource (cfg/resources))))

(defn ->
  [src resource params]
  (str (src (cfg/environment)) (resources (cfg/resources))))

(client/<- [:ard :tile {:x 1 :y 2}])

(client/-> [:ard :inventory {:x 1 :y 2}])
                                   


