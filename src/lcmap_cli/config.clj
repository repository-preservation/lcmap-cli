(ns lcmap-cli.config)


;;  (:require[clojure.edn :require [read]]
;;           [clojure.io :as io)
;;  (:import java.io.PushbackReader)))

;;(defn load-edn
;;  "Load edn from an io/reader source (filename or io/resource)."
;;  [source]
;;  (try
;;    (with-open [r (io/reader source)]
;;      (edn/read (java.io.PushbackReader. r)))

;;    (catch java.io.IOException e
;;      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
;;    (catch RuntimeException e
;;      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

(def http-options
  {:timeout 2400000})
  
(def grids
  {:conus {:ard "http://lcmap-test.cr.usgs.gov/ard_cu_c01_v01"
           :aux "http://lcmap-test.cr.usgs.gov/aux_cu_c01_v01"
           :ccdc "http://lcmap-test.cr.usgs.gov/ard_cu_c01_v01_aux_cu_v01_ccdc_1_0"
           :grid "/grid"
           :snap "/grid/snap"
           :near "/grid/near"
           :inventory "/inventory"
           :sources "/sources"
           :tile "/tile"
           :chip "/chip"
           :pixel "/pixel"
           :segment "/segment"
           :annual-prediction "/annual_prediction"
           :segment-instance-count 25
           :segment-sleep-for 1000}
   :alaska {:ard "http://host:port/ard_ak_c01_v01"
            :aux "http://host:port/aux_ak_v01"
            :ccdc "http://host:port/ard_ak_c01_v01_aux_ak_v01_ccdc_1_0"
            :grid "/grid"
            :snap "/grid/snap"
            :near "/grid/near"
            :inventory "/inventory"
            :sources "/sources"
            :tile "/tile"
            :chip "/chip"
            :pixel "/pixel"
            :segment "/segment"
            :annual-prediction "/annual_prediction"
            :segment-instance-count 1
            :segment-sleep-for 1000}
   :hawaii {:ard "http://host:port/ard_hi_c01_v01"
            :aux "http://host:port/aux_hi_v01"
            :ccdc "http://host:port/ard_hi_c01_v01_aux_hi_v01_ccdc_1_0"
            :grid "/grid"
            :snap "/grid/snap"
            :near "/grid/near"
            :inventory "/inventory"
            :sources "/sources"
            :tile "/tile"
            :chip "/chip"
            :pixel "/pixel"
            :segment "/segment"
            :annual-prediction "/annual_prediction"
            :segment-instance-count 1
            :segment-sleep-for 1000}})
