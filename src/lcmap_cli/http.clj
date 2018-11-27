(ns lcmap-cli.http
  (:require [clojure.string :as string]
            [cheshire.core :as json]
            [org.httpkit.client :as http-kit]
            [lcmap-cli.config :as cfg]))

(comment
  (decode @(client :get :conus :ard :grid nil))
  (decode @(client :get :conus :ard :snap {:query-params {:x 1 :y 2}}))
  (grid :conus :ard)
  (snap :conus :ard 1 2)
  (near :conus :ard 1 2)
)

(defn url
  [grid src resource]
  (let [g (grid cfg/grids)]
    (str (src g) (resource g))))

(defn http-options
  [options]
    (merge options cfg/http-options))

(defmulti decode
  (fn [x]
    (first (string/split (get-in x [:headers :content-type]) #";"))))

(defmethod decode "application/json"
  [x]
  (assoc x :body (json/decode (:body x) true)))

(defmethod decode :default
  [x]
  x)

(defmulti client
  (fn [verb & etc]
    (keyword verb)))

(defmethod client :get
  [verb grid src resource options]
  (http-kit/get (url grid src resource) (http-options options)))

(defmethod client :post
  [verb grid src resource options]
  (http-kit/post (url grid src resource) (http-options options)))

(defmethod client :default
  [verb grid src resource options]
  (str verb "not supported"))
