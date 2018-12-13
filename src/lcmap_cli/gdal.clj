(ns lcmap-cli.gdal
  (:require [mount.core :as mount]
            [lcmap-cli.util :as util])
  (:import [org.gdal.gdal gdal]
           [org.gdal.gdal Driver]
           [org.gdal.gdal Dataset]
           [org.gdal.gdalconst gdalconst]))

;; init and state constructs blatantly ripped off from the
;; USGS-EROS/lcmap-chipmunk project on GitHub, created by
;; Jon Morton https://github.com/jmorton
;; 
;; ## Init
;;
;; This makes it easier to use Java GDAL libraries without
;; having to set environment variables. These are typical
;; install locations of GDAL libs on CentOS and Ubuntu.
;;
;; Before GDAL can open files, drivers must be registered.
;; Selective registration is more tedious and error prone,
;; so we just register all drivers.
;;
;; If anything goes wrong, a helpful string is printed to
;; stdout (not a log file).
;;

(defn init
  "Initialize GDAL drivers."
  []
  (try
    (util/amend-usr-path ["/usr/lib/java/gdal" "/usr/lib/jni"])
    (gdal/AllRegister)
    (catch RuntimeException e
      (binding [*out* *err*]
        (println (str "Could not update paths to native libraries. "
                      "You may need to set LD_LIBRARY_PATH to the "
                      "directory containing libgdaljni.so"))))
    (finally
      (import org.gdal.gdal.gdal))))

;; ## State
;;
;; A mount state is defined so that GDAL is initialized like
;; everything else (DB connections, HTTP listeners, etc...)
;;

(mount/defstate gdal-init
  :start (init))

(defn create_geotiff
  [name values ulx uly projection x_size y_size x_offset y_offset]
  (let [driver  (gdal/GetDriverByName "GTiff")
        dataset (.Create driver name x_size y_size)
        band    (.GetRasterBand dataset 1)
        transform (double-array [ulx 30 0 uly 0 -30])]
    (.SetGeoTransform dataset transform)
    (.SetProjection dataset projection)
    (.WriteRaster band x_offset y_offset x_size y_size (float-array values))
    (.delete band)
    (.delete dataset))
  name)

(defn update_geotiff
  ([tiff_name values x_offset y_offset x_size y_size]
   (let [dataset (gdal/Open tiff_name 1)
         band (.GetRasterBand dataset 1)]
     (.WriteRaster band x_offset y_offset x_size y_size (float-array values))
     (.delete band)
     (.delete dataset)))
  ([tiff_name values x_offset y_offset]
   (update_geotiff tiff_name values x_offset y_offset 100 100)))


