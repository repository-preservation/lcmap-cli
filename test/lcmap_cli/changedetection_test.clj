(ns lcmap-cli.changedetection-test
  (:require [clojure.test :refer :all]
            [lcmap-cli.changedetection :refer :all]))

(deftest handler-test
  (testing "(handler) with HTTP 200"
    (is (= (handler {:cx 0
                     :cy 0
                     :grid "fake-http"
                     :acquired "1980/2019"
                     :response (atom {:status 200
                                      :headers {:content-type "application/json"}
                                      :body "[\"some-value\"]"})})
           ["some-value"])))
  (testing "(handler) with HTTP 500"
    (is (= (handler {:cx 0
                     :cy 0
                     :grid "fake-http"
                     :acquired "1980/2019"
                     :response (atom {:status 500
                                      :headers {:content-type "application/json"}
                                      :body "[\"some-value\"]"})})
           {:cx 0 :cy 0 :acquired "1980/2019" :error {:response {:status 500
                                                                 :headers {:content-type "application/json"}
                                                                 :body "[\"some-value\"]"}}})))

  (testing "(handler) with HTTP 200 but decode failure"
    (every? #{:cx :cy :grid :acquired :error}
            (keys (handler {:cx 0
                            :cy 0
                            :grid "fake-http"
                            :acquired "1980/2019"
                            :response {:status 200
                                       :headers {:content-type "application/json"}
                                       :body "some-value\"]"}})))))
       

    


