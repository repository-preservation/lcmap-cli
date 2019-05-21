(ns lcmap-cli.prediction-test
  (:require [clojure.test :refer :all]
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
                     :chips [[0,0], [1,1]]
                     :response (atom {:status 200
                                      :headers {:content-type "application/json"}
                                      :body "[\"some-value\"]"})})
           ["some-value"])))
  
  (testing "(handler) with HTTP 500"
    (is (= (handler {:tx 0
                     :ty 0
                     :grid "fake-http"
                     :acquired "1980/2019"
                     :month "07"
                     :day "01"
                     :response (atom {:status 500
                                      :headers {:content-type "application/json"}
                                      :body "[\"some-value\"]"})})
           {:tx 0
            :ty 0
            :acquired "1980/2019"
            :month "07"
            :day "01"
            :chips nil
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

(deftest tile-test
  (testing "(tile)"
    (with-fake-http ["http://fake/grid/snap"
                     {:status 200
                      :body "{\"tile\":{\"proj-pt\":[-15585.0,14805.0],\"grid-pt\":[17.0,22.0]},\"chip\":{\"proj-pt\":[-15585.0,14805.0],\"grid-pt\":[850.0,1100.0]}}"}
                     "http://fake/grid"
                     {:status 200
                      :body "[{\"name\": \"tile\", \"proj\":\"\", \"rx\": 1.0, \"ry\": -1.0, \"sx\": 150000.0, \"sy\": 150000.0, \"tx\": 2565585.0, \"ty\": 3314805.0}, 
                              {\"name\": \"chip\", \"proj\": \"\",\"rx\": 1.0, \"ry\": -1.0, \"sx\": 3000.0,   \"sy\": 3000.0,   \"tx\": 2565585.0, \"ty\": 3314805.0}]"}]

      (def result (tile {:tile "017022" :grid "fake-http" :dataset "ard"}))

      (is (= {:tx -15585.0, :ty 14805.0, :tile {:proj-pt [-15585.0 14805.0], :grid-pt [17.0 22.0]}, :grid "fake-http", :dataset "ard"}
             result)))))

(deftest chips-test
  (testing "(chips)"
    (with-fake-http ["http://fake/grid"
                     {:status 200
                      :body "[{\"name\": \"tile\", \"proj\": \"\",\"rx\": 1.0, \"ry\": -1.0, \"sx\": 2, \"sy\": 2, \"tx\": 0, \"ty\": 0}, {\"name\": \"chip\",\"proj\": \"\",\"rx\": 1.0, \"ry\": -1.0, \"sx\": 1, \"sy\": 1, \"tx\": 0, \"ty\": 0}]"}]
       
      (def result (chips {:grid "fake-http"
                          :dataset "ard"
                          :tile {:grid-pt [5 5] :proj-pt [5.0 5.0]}}))


      (def expected (into #{} '([10.0 -10.0]
                                [10.0 -11.0]
                                [11.0 -10.0]
                                [11.0 -11.0])))
      
    (is (= expected (into #{} (:chips result)))))))
