(ns console)

(defn workflow-defs [] {:one nil :two nil :three nil})
(defn workflows [] (keys workflow-defs))
(defn status [workflow] "workflow status")

(defn jobs [] "jobs")
(defn top [] "top")
(defn ps [] "ps")

(defn services [] "list lcmap services")
(defn scale [service level] "scale lcmap services")

(defn grids [] "grids")
(defn grid [] "grid")
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
