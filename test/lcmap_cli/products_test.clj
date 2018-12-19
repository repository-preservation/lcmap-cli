(ns lcmap-cli.products-test
  (:use org.httpkit.fake)
  (:require [clojure.test :refer :all]
            [lcmap-cli.products :as products]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.functions :as f]
            [lcmap-cli.state :as state]
            [environ.core :as environ]))

(def test_config {:grids {:conus {:ard "http://localardhost:5656" :ccdc "http://localccdchost:5656" :products "/products"}}})

(deftest date-range-test
  (is (= ["2006-07-01" "2007-07-01"] (products/date-range {:grid "conus" :years "2006-2007"}))))

(deftest product-request-test
  (with-fake-http [{:url "http://localccdchost:5656/products" :method :post} {:status 200 :body "sweet_geotiff.tif"}]
    (let [response (products/product-request {:grid "conus" :penny "red"})]
      (is (= 200 (:status @response))))))

(deftest response-handler-test
  (let [resp (promise)
        delv (deliver resp {:status 200 :body "true" :headers {:content-type "application/json"}}) 
        input {:grid "conus" :date "2006-07-01" :product "primary-landcover" :tile "005007" :tilex -1815585 :tiley 2264805 :response resp }]
    
    (is (= true (:body (products/response-handler input))))))
