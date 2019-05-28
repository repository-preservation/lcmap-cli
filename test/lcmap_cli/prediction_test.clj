(ns lcmap-cli.prediction-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [lcmap-cli.prediction :refer :all]
            [lcmap-cli.state :refer [shutdown]]))

(deftest handler-test
  (testing "(handler) with HTTP 200"
    (is (= (handler {:tx 0
                     :ty 0
                     :grid "fake-http"
                     :acquired "1980/2019"
                     :month "07"
                     :day "01"
                     :tile "014027"
                     :chips [[0,0], [1,1]]
                     :response (atom {:status 200
                                      :headers {:content-type "application/json"}
                                      :body "\"some-value\""})})
           "some-value")))
  
  (testing "(handler) with HTTP 500"
    (is (= (handler {:tx 0
                     :ty 0
                     :grid "fake-http"
                     :acquired "1980/2019"
                     :month "07"
                     :day "01"
                     :tile "014027"
                     :chips []
                     :response (atom {:status 500
                                      :headers {:content-type "application/json"}
                                      :body "[\"some-value\"]"})})
           {:tx 0
            :ty 0
            :acquired "1980/2019"
            :month "07"
            :day "01"
            :chips 0
            :tile "014027"            
            :error "{:response {:status 500, :headers {:content-type \"application/json\"}, :body \"[\\\"some-value\\\"]\"}}"})))

  (testing "(handler) with HTTP 200 but decode failure"
    (every? #{:tx :ty :grid :acquired :month :day :error}
            (keys (handler {:tx 0
                            :ty 0
                            :grid "fake-http"
                            :acquired "1980/2019"
                            :month "07"
                            :day "01"
                            :response {:status 200
                                       :headers {:content-type "application/json"}
                                       :body "some-value\"]"}})))))            

(deftest tile-snap-test
  (testing "(tile-snap)"
    (with-fake-http ["http://fake/grid/snap"
                     {:status 200 :body "{\"tile\":{\"proj-pt\":[-15585.0,14805.0],\"grid-pt\":[17.0,22.0]},\"chip\":{\"proj-pt\":[-15585.0,14805.0],\"grid-pt\":[850.0,1100.0]}}"}

                     "http://fake/grid"
                     {:status 200 :body "[{\"name\": \"tile\", \"proj\":\"\", \"rx\": 1.0, \"ry\": -1.0, \"sx\": 150000.0, \"sy\": 150000.0, \"tx\": 2565585.0, \"ty\": 3314805.0}, 
                                          {\"name\": \"chip\", \"proj\": \"\",\"rx\": 1.0, \"ry\": -1.0, \"sx\": 3000.0,   \"sy\": 3000.0,   \"tx\": 2565585.0, \"ty\": 3314805.0}]"}]

      (def result (tile-snap {:tile "017022" :grid "fake-http" :dataset "ard"}))

      (is (= {:proj-pt [-15585.0 14805.0], :grid-pt [17.0 22.0]}
             result)))))

(deftest chips-test
  (testing "(chips)"
    (with-fake-http ["http://fake/grid"
                     {:status 200 :body "[{\"name\": \"tile\", \"proj\": \"\",\"rx\": 1.0, \"ry\": -1.0, \"sx\": 2, \"sy\": 2, \"tx\": 0, \"ty\": 0}, {\"name\": \"chip\",\"proj\": \"\",\"rx\": 1.0, \"ry\": -1.0, \"sx\": 1, \"sy\": 1, \"tx\": 0, \"ty\": 0}]"}]
       
      (def result (chips {:grid "fake-http" :dataset "ard"}
                         {:grid-pt [5 5] :proj-pt [5.0 5.0]}))

      (def expected (into #{} '([10.0 -10.0]
                                [10.0 -11.0]
                                [11.0 -10.0]
                                [11.0 -11.0])))
      
      (is (= expected (into #{} result))))))


(deftest predict-test
  (testing "(predict)"
    (with-fake-http ["http://fake/prediction"
                     {:status 200 :body "{\"tx\": 2565585, \"ty\": 3314805, \"month\":\"07\", \"day\": \"01\", \"grid\": \"fake-http\", \"acquired\": \"1980/2018\"}"}

                     "http://fake/grid/snap"
                     {:status 200 :body "{\"tile\":{\"proj-pt\":[-15585.0,14805.0],\"grid-pt\":[17.0,22.0]},\"chip\":{\"proj-pt\":[-15585.0,14805.0],\"grid-pt\":[850.0,1100.0]}}"}
                     
                     "http://fake/grid"
                     {:status 200 :body "[{\"name\": \"tile\", \"proj\":\"\", \"rx\": 1.0, \"ry\": -1.0, \"sx\": 150000.0, \"sy\": 150000.0, \"tx\": 2565585.0, \"ty\": 3314805.0}, 
                                          {\"name\": \"chip\", \"proj\": \"\",\"rx\": 1.0, \"ry\": -1.0, \"sx\": 3000.0,   \"sy\": 3000.0,   \"tx\": 2565585.0, \"ty\": 3314805.0}]"}]
      
      (def inputs {:grid "fake-http"
                   :acquired "1980/2018"
                   :month "07"
                   :day "01"
                   :tile "017022"})

      (def expected {:tx 2565585
                     :ty 3314805
                     :grid "fake-http"
                     :acquired "1980/2018"
                     :month "07"
                     :day "01"})

      (def results (json/decode (predict inputs) true))

      (is (= expected results)))))
