(ns lcmap-cli.changedetection-test
  (:require [clojure.core.async :as async]
            [clojure.test :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [lcmap-cli.changedetection :refer :all]
            [lcmap-cli.state :refer [shutdown]]))

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
           {:cx 0
            :cy 0
            :acquired "1980/2019"
            :error "{:response {:status 500, :headers {:content-type \"application/json\"}, :body \"[\\\"some-value\\\"]\"}}"})))

  (testing "(handler) with HTTP 200 but decode failure"
    (every? #{:cx :cy :grid :acquired :error}
            (keys (handler {:cx 0
                            :cy 0
                            :grid "fake-http"
                            :acquired "1980/2019"
                            :response {:status 200
                                       :headers {:content-type "application/json"}
                                       :body "some-value\"]"}})))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; with-fake-http is not working across threads
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;(deftest consumers-test
;;  (testing "(start-consumers)"
;;    (with-fake-http ["http://fake/segment" "{\"cx\": 0, \"cy\": 0}"]
;;      (let [in  (async/chan)
;;            out (async/chan)]
;;
;;        (start-consumers 1 in out)
;;
;;        (async/>!! in {:cx 0 :cy 0 :acquired "1980/2019"})
;;        
;;        (is (= "{\"cx\": 0, \"cy\": 0}" (async/<!! out)))
;;
;;        (shutdown)))))
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
            
