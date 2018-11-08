(ns lcmap-cli.core
  (:require [clojure.string :as string]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [lcmap-cli.config :as cfg])
  (:gen-class))

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
  (fn [a b c d e]
    (keyword a)))

(defmethod client :get
  [action grid src resource options]
  (http/get (url grid src resource) options))

(defmethod client :post
  [action grid src resource options]
  (http/post (url grid src resource) options))

(defmethod client :default
  [action grid src resource options]
  (str action "not supported"))


(defn grid
  [grid src]
  (-> @(client :get grid src :grid nil)
      decode
      :body))

(defn snap
  [grid src x y]
  (-> @(client :get grid src :snap {:query-params {:x x :y y}})
      decode
      :body))

(defn near
  [grid src x y]
  (-> @(client :get grid src :near {:query-params {:x x :y y}})
      decode
      :body))

(defn tile
  []
  nil)

(defn chips
  []
  nil)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
