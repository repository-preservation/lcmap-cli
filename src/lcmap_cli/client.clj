(ns lcmap-cli.client
  (:require [cheshire.core :as json]
            [lcmap-cli.config :as cfg]))

(defn -
  [action src resource params]
  (str action
       " "
       (src (cfg/environment))
       (resource cfg/resources)))

(comment
  (defn snap
  [x y]
   @(http/get (str host resource "?x=" x "&y=" y))))

                  
