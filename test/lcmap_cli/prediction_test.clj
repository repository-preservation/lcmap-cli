(ns lcmap-cli.prediction-test
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [lcmap-cli.prediction :refer :all]))

(deftest handler-test
  (testing "(handler) with HTTP 200"
    (is (= (handler {:tx 0
                     :ty 0
                     :cx 0
                     :cy 0
                     :grid "fake-http"
                     :acquired "1980/2019"
                     :month "7"
                     :day "1"
                     :response (atom {:status 200
                                      :headers {:content-type "application/json"}
                                      :body "[\"some-value\"]"})})
           ["some-value"])))
  
  (testing "(handler) with HTTP 500"
    (is (= (handler {:tx 0
                     :ty 0
                     :cx 0
                     :cy 0
                     :grid "fake-http"
                     :acquired "1980/2019"
                     :month "7"
                     :day "1"
                     :response (atom {:status 500
                                      :headers {:content-type "application/json"}
                                      :body "[\"some-value\"]"})})
           {:tx 0
            :ty 0
            :cx 0
            :cy 0
            :acquired "1980/2019"
            :month "7"
            :day "1"
            :error "{:response {:status 500, :headers {:content-type \"application/json\"}, :body \"[\\\"some-value\\\"]\"}}"})))

  (testing "(handler) with HTTP 200 but decode failure"
    (every? #{:cx :cy :grid :acquired :error}
            (keys (handler {:tx 0
                            :ty 0
                            :cx 0
                            :cy 0
                            :grid "fake-http"
                            :acquired "1980/2019"
                            :month "7"
                            :day "1"
                            :response {:status 200
                                       :headers {:content-type "application/json"}
                                       :body "some-value\"]"}})))))
