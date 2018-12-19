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

(deftest start-consumers-test
  (let [inchan (async/chan)
        outchan (async/chan)]
    (f/start-consumers 1 inchan outchan dumb_handler dumb_operator)
    (async/>!! inchan {:x "4" :foo "bar"})
    (is (= 5 (:response (async/<!! outchan))))))
