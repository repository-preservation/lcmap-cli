(ns lcmap-cli.functions-test
  (:require [clojure.test :refer :all]
            [lcmap-cli.functions :refer :all]))

(deftest to-json-test
  (testing "to-json pass"
    (is (= "{\"key\":\"value\"}" (to-json {:key "value"})))))

