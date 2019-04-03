(ns lcmap-cli.http-test
  (:require [clojure.test :refer :all]
            [lcmap-cli.http :refer :all]
            [lcmap-cli.config :as config]))

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
            :body [{:a-key 45}]}))
    
    (is (= (decode {:headers {:content-type "application/xhtml+xml"}
                    :body "<html><head/><body></body></html>"})
           {:headers {:content-type "application/xhtml+xml"}
            :body "<html><head/><body></body></html>"}))))
