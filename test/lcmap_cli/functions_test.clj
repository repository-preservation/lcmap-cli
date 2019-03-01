(ns lcmap-cli.functions-test
  (:require [clojure.test :refer :all]
            [lcmap-cli.functions :refer :all]))


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
    (is (not (nil? (to-json-or-str (new java.lang.Object))))))
        
  (testing "to-json-or-str-test-passes"
    (= 1 0))

  (testing "to-json-or-str-test-"
    (= 1 0)))


