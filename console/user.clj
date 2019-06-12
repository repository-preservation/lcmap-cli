(ns console
  (:use [clojure.repl]))

(defn workflow-defs [] {:one nil :two nil :three nil})
(defn workflows [] (keys workflow-defs))
(defn status [workflow] "workflow status")

(defn jobs
  "list all running jobs"
  []
  "jobs")

(defn services
  "list all lcmap services"
  []
  "list lcmap services")

(defn scale
  "scale lcmap services"
  [service level]
  "scale lcmap services")

(defn grids
  "list all lcmap grids"
  []
  "grids")

(defn grid
  "show the grid definition"
  [grid]
  "grid")

(defn snap [] "snap")
(defn near [] "near")
(defn registry [] "registry")

(defn ingest  [] "ingest")
(defn detect  [] "detect")
(defn train   [] "train")
(defn predict [] "predict")
(defn product [] "product")
(defn raster  [] "raster")

(defn cdetect  [] "detect chip")
(defn cpredict [] "predict chip")
(defn cproduct [] "product for chip")

(defn xy-to-tile [] "xy-to-tile")
(defn tile-to-xy [] "tile-to-xy")


(defn help [] "help")
(defn usage [] "usage")
(defn commands [] "commands")
