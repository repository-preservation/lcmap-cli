(ns lcmap-cli.training-test
  (:require [clojure.test :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [lcmap-cli.training :refer :all]
            [lcmap-cli.state :refer [shutdown]]))

(deftest handler-test
  (testing "(handler) with HTTP 200"
    (is (= (handler {:tx 0
                     :ty 0
                     :grid "fake-http"
                     :acquired "1980/2019"
                     :date "2001-07-01"
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
                     :date "2001-07-01"
                     :response (atom {:status 500
                                      :headers {:content-type "application/json"}
                                      :body "[\"some-value\"]"})})
           {:tx 0
            :ty 0
            :acquired "1980/2019"
            :date "2001-07-01"
            :chips nil
            :error "{:response {:status 500, :headers {:content-type \"application/json\"}, :body \"[\\\"some-value\\\"]\"}}"})))

  (testing "(handler) with HTTP 200 but decode failure"
    (every? #{:tx :ty :grid :acquired :date :error}
            (keys (handler {:tx 0
                            :ty 0
                            :grid "fake-http"
                            :acquired "1980/2019"
                            :date "2001-07-01"
                            :response {:status 200
                                       :headers {:content-type "application/json"}
                                       :body "some-value\"]"}})))))            

(deftest tiles-test
  (testing "(tiles)"
    (with-fake-http ["http://fake/grid/near"
                     {:status 200
                      :body "{\"tile\":[{\"proj-pt\":[-165585.0,-135195.0],\"grid-pt\":[16.0,23.0]},{\"proj-pt\":[-165585.0,14805.0],\"grid-pt\":[16.0,22.0]},{\"proj-pt\":[-165585.0,164805.0],\"grid-pt\":[16.0,21.0]},{\"proj-pt\":[-15585.0,-135195.0],\"grid-pt\":[17.0,23.0]},{\"proj-pt\":[-15585.0,14805.0],\"grid-pt\":[17.0,22.0]},{\"proj-pt\":[-15585.0,164805.0],\"grid-pt\":[17.0,21.0]},{\"proj-pt\":[134415.0,-135195.0],\"grid-pt\":[18.0,23.0]},{\"proj-pt\":[134415.0,14805.0],\"grid-pt\":[18.0,22.0]},{\"proj-pt\":[134415.0,164805.0],\"grid-pt\":[18.0,21.0]}],\"chip\":[{\"proj-pt\":[-3585.0,-195.0],\"grid-pt\":[854.0,1105.0]},{\"proj-pt\":[-3585.0,2805.0],\"grid-pt\":[854.0,1104.0]},{\"proj-pt\":[-3585.0,5805.0],\"grid-pt\":[854.0,1103.0]},{\"proj-pt\":[-585.0,-195.0],\"grid-pt\":[855.0,1105.0]},{\"proj-pt\":[-585.0,2805.0],\"grid-pt\":[855.0,1104.0]},{\"proj-pt\":[-585.0,5805.0],\"grid-pt\":[855.0,1103.0]},{\"proj-pt\":[2415.0,-195.0],\"grid-pt\":[856.0,1105.0]},{\"proj-pt\":[2415.0,2805.0],\"grid-pt\":[856.0,1104.0]},{\"proj-pt\":[2415.0,5805.0],\"grid-pt\":[856.0,1103.0]}]}"}]

      (def result (tiles {:tx 0 :ty 0 :grid "fake-http" :dataset "ard"}))
      
      (is (not (nil? result)))
      
      (is (= (into #{}[[16.0,23.0][16.0,22.0][16.0,21.0]
                       [17.0,23.0][17.0,22.0][17.0,21.0]
                       [18.0,23.0][18.0,22.0][18.0,21.0]])
             (into #{} (map (fn [x] (:grid-pt x)) (:tiles result))))))))

(deftest chips-test
  (testing "(chips)"
    (with-fake-http ["http://fake/grid"
                     {:status 200
                      :body "[{\"name\": \"tile\", \"proj\": \"\",\"rx\": 1.0, \"ry\": -1.0, \"sx\": 2, \"sy\": 2, \"tx\": 0, \"ty\": 0}, {\"name\": \"chip\",\"proj\": \"\",\"rx\": 1.0, \"ry\": -1.0, \"sx\": 1, \"sy\": 1, \"tx\": 0, \"ty\": 0}]"}]
       
       (def result (chips {:grid "fake-http"
                           :dataset "ard"
                           :tiles [{:grid-pt [0 0] :proj-pt [0.0 0.0]}
                                   {:grid-pt [5 5] :proj-pt [5.0 5.0]}]}))


      (def expected (into #{} '({:cx 0.0, :cy 0.0}
                                {:cx 0.0, :cy -1.0}
                                {:cx 1.0, :cy 0.0}
                                {:cx 1.0, :cy -1.0}
                                {:cx 10.0, :cy -10.0}
                                {:cx 10.0, :cy -11.0}
                                {:cx 11.0, :cy -10.0}
                                {:cx 11.0, :cy -11.0})))
      
    (is (= expected (into #{} (:chips result)))))))
