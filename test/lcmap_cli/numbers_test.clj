(ns lcmap-cli.numbers-test
  (:require [clojure.test :refer :all]
            [lcmap-cli.numbers :refer :all]))

(deftest numberize-test
  (testing "(numberize)"
    (is (nil? (numberize true)))
    (is (= 0 (numberize 0)))
    (is (= 0 (numberize "0")))
    (is (= 0 (numberize "0.0")))
    (is (= 0.0 (numberize 0.0)))
    (is (= -10.25 (numberize "-10.25")))
    (is (= 1234 (numberize "1234asdf")))
    (is (nil? (numberize "asdf1234")))))
