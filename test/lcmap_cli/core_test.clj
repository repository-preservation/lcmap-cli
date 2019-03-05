(ns lcmap-cli.core-test
  (:require [clojure.test :refer :all]
            [lcmap-cli.core :refer :all]))


(deftest options-test
  (testing "(options)"
    (is (= '(["-h" "--help"]) (options [:help])))))


(deftest usage-test
  (testing "(usage)"
    (= java.lang.String
       (type (usage "some-action" "some-options")))))

(deftest actions-test
  (testing "actions"
    (= java.lang.String (type actions))))

(deftest error-msg-test
  (testing "(error-msg)"
    (= java.lang.String
       (type (error-msg ["one" "two" "three"])))))


(deftest function-test
  (testing "FIXME, I fail."
    (is (= 0 1))))


(deftest parameters-test
  (testing "FIXME, I fail."
    (is (= 0 1))))


(deftest validate-args-test
  (testing "FIXME, I fail."
    (is (= 0 1))))


