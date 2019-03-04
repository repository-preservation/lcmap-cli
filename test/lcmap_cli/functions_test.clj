(ns lcmap-cli.functions-test
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [lcmap-cli.functions :refer :all]
            [org.httpkit.client :as http-kit]
            [org.httpkit.fake :refer [with-fake-http]]))


(deftest to-json-test

  (testing "(to-json Hashmap)"
    (is (= "{\"key\":\"value\"}" (to-json {:key "value"}))))

  (testing "(to-json Integer)"
    (is (= "1" (to-json 1))))

  (testing "(to-json Float)"
    (is (= "1.0" (to-json 1.0))))

  (testing "(to-json Boolean)"
    (is (= "true" (to-json true))))

  (testing "(to-json Vector)"
    (is (= "[1,2,3]" (to-json [1 2 3]))))

  (testing "(to-json List)"
    (is (= "[1,2,3]" (to-json '(1 2 3)))))

  (testing "(to-json Set)"
    (is (= "[1,3,2]" (to-json #{1 2 3}))))
  
  (testing "(to-json String)"
    (is (= "\"a-value\"" (to-json "a-value"))))

  (testing "(to-json Keyword)"
    (is (= "\"a-keyword\"" (to-json :a-keyword))))

  (testing "(to-json Rational)"
    (is (= "0.3333333333333333" (to-json (/ 1 3)))))
  
  (testing "(to-json Exception)"
    (is (thrown? com.fasterxml.jackson.core.JsonGenerationException
                 (to-json (new java.lang.Object))))))


(deftest to-json-or-str-test

  (testing "(to-json-or-str Hashmap)"
    (is (= "{\"key\":\"value\"}" (to-json-or-str {:key "value"}))))

  (testing "(to-json-or-str Integer)"
    (is (= "1" (to-json-or-str 1))))

  (testing "(to-json-or-str Float)"
    (is (= "1.0" (to-json-or-str 1.0))))

  (testing "(to-json-or-str Boolean)"
    (is (= "true" (to-json-or-str true))))

  (testing "(to-json-or-str Vector)"
    (is (= "[1,2,3]" (to-json-or-str [1 2 3]))))

  (testing "(to-json-or-str List)"
    (is (= "[1,2,3]" (to-json-or-str '(1 2 3)))))

  (testing "(to-json-or-str Set)"
    (is (= "[1,3,2]" (to-json-or-str #{1 2 3}))))
  
  (testing "(to-json-or-str String)"
    (is (= "\"a-value\"" (to-json-or-str "a-value"))))

  (testing "(to-json-or-str Keyword)"
    (is (= "\"a-keyword\"" (to-json-or-str :a-keyword))))

  (testing "(to-json-or-str Rational)"
    (is (= "0.3333333333333333" (to-json-or-str (/ 1 3)))))
  
  (testing "(to-json-or-str Exception)"
    (is (not (nil? (to-json-or-str (new java.lang.Object)))))
    (is (= java.lang.String (type (to-json-or-str (new java.lang.Object)))))))


(deftest trim-test

  (testing "(trim java.lang.String)"
    (is (= "asdf" (trim "asdf     ")))
    (is (= "as   df" (trim "as   df")))
    (is (= "asdf" (trim "  asdf"))))

  (testing "(trim not-a-string)"
    (is (= 1 (trim 1)))))


(deftest transform-matrix-test

  (testing "(transform-matrix Hashmap)"
    (let [gs {:rx 3 :ry 3 :sx 1 :sy 1 :tx 2 :ty 2}]
      (is (= [[3 0 2][0 3 2][0 0 1.0]]
             (transform-matrix gs))))))


(deftest point-matrix-test

  (testing "(point-matrix Hashmap)"
    (let [p {:x "1" :y "3"}]
      (is (= [[1] [3] [1]] (point-matrix p))))))


(deftest tile->projection-test

  (testing "(tile->projection Hashmap)"
    (is (= 1 0))))


;; dummy, remove and replace with tests for grid, snap, etc.

(deftest fake-http
  (testing "fake http"

    (with-fake-http ["http://google.com/" "faked"
                     "http://flickr.com/" 500]
      (is (= (:body @(http-kit/get "http://google.com/")) "faked"))
      (is (= (:status @(http-kit/get "http://flickr.com/")) 500)))))


(deftest fake-config-test
  (testing "fake config"
    (print (:edn-file env))
    (is (= 1 0))))
    
