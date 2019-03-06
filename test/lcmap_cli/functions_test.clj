(ns lcmap-cli.functions-test
  (:require [clojure.test :refer :all]
            [lcmap-cli.functions :as f]
            [clojure.core.async :as async]))

(defn dumb_operator
  [{x :x foo :foo}]
  (-> x read-string inc))

(defn dumb_handler
  [i]
  i)

