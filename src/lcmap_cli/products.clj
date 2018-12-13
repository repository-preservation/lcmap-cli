(ns lcmap-cli.products
  (:require [lcmap-cli.config :as cfg]
            [lcmap-cli.http :as http]
            [lcmap-cli.gdal :as gdal]
            [lcmap-cli.functions :refer [chips]]))

;; a tile is 50 chips x 50 chips
;; each chip is 100 pixels x 100 pixels
;; each pixel is 30m x 30m
(def meters_per_pixel     30)
(def pixels_per_chip_side 100)
(def chips_per_tile_side  50)

(defn calc_offset
  "Returns pixel offset for UL coordinates of chips"
  [tile_x tile_y chip_x chip_y]
  (let [x_diff (- chip_x tile_x)
        y_diff (- tile_y chip_y)
        x_offset (/ x_diff meters_per_pixel)
        y_offset (/ y_diff meters_per_pixel)]
    [x_offset y_offset]))

(defn create_blank_tile_tiff
  [name ulx uly projection]
  (let [values (repeat (* 5000 5000) 0)]
    (gdal/create_geotiff name values ulx uly projection 5000 5000 0 0)))

(defn add_chip_to_tile
  [name values tile_x tile_y chip_x chip_y]
  (let [[x_offset y_offset] (calc_offset tile_x tile_y chip_x chip_y)]
    (gdal/update_geotiff name values x_offset y_offset)))

(defn available
  [{grid :grid}]
  (http/client "get" grid :ccdc :available-products))

(defn product_name
  [product tile date]
  (str product tile date ".tif"))

(defn product
  [{grid :grid tile :tile product :product date :date}]
  (let [chip_coords (chips {:grid grid :dataset "ard" :tile tile})
        output (product_name product tile date)]
    ; create tile tif
    (doseq [i chip_coords]
      ; grab x and y coords
      ; make request for data
      (prn i)


      )

    )
  )
