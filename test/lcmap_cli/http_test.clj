(ns lcmap-cli.http-test
  (:require [clojure.test :refer :all]
            [lcmap-cli.http :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]))


(deftest url-test
  (testing "(url)"
    (is (= (url :fake-http :ard :snap)
         "http://fake/grid/snap"))))

(deftest http-options-test
  (testing "(http-options)"
    (is (= (http-options {:keepalive 30000})
           {:keepalive 30000 :timeout 2400000}))))

(deftest decode-test
  (testing "(decode)"
    (is (= (decode {:headers {:content-type "application/json;what/ever"}
                    :body "[{\"a-key\": 45}]"})
           {:headers {:content-type "application/json;what/ever"}
            :body [{:a-key 45}]}))))
      
  (comment (testing "(url)"
       (with-fake-http ["http://fake/grid" "[{\"name\": \"tile\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 150000.0,
                                           \"sy\": 150000.0,
                                           \"tx\": 2565585.0,
                                           \"ty\": 3314805.0},
                                          {\"name\": \"chip\",
                                           \"proj\": \"\",
                                           \"rx\": 1.0,
                                           \"ry\": -1.0,
                                           \"sx\": 3000.0,
                                           \"sy\": 3000.0,
                                           \"tx\": 2565585.0,
                                           \"ty\": 3314805.0}]]"])))
