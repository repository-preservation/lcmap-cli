(ns lcmap-cli.products-test
  (:use org.httpkit.fake)
  (:require [clojure.test :refer :all]
            [lcmap-cli.products :as products]
            [lcmap-cli.functions :as functions]
            [lcmap-cli.http :as http]
            [lcmap-cli.config :as cfg]
            [lcmap-cli.functions :as f]
            [lcmap-cli.state :as state]
            [environ.core :as environ]
            [clojure.core.async :as async]))

(deftest handler-test
  (testing "handler with 200 status"
    (is (= ["foo"]
           (products/handler (atom {:status 200
                                    :headers {:content-type "application/json"}
                                    :body "[\"foo\"]"})))))

  (testing "handler with non 200 status"
    (is (= {:error ["foo error"] :status 400}
           (products/handler (atom {:status 400
                                    :headers {:content-type "application/json"}
                                    :body "[\"foo error\"]"})))))

  (testing "handler with decode failure"
    (is (= {:error "java.lang.ClassCastException: clojure.lang.PersistentArrayMap cannot be cast to java.util.concurrent.Future"}
           (products/handler {:status 400
                              :headers {:content-type "application/json"}
                              :body "[\"foo error\"]"})))))

(deftest post-request-test
  (with-redefs [http/client (fn [a b c d e] (assoc e a b c d))]
    (is (= (products/post-request {:grid "foo" :resource "bar"})
           {:body "{\"grid\":\"foo\",\"resource\":\"bar\"}", :headers {"Content-Type" "application/json"}, :post :foo, :ccdc :bar}))))

(deftest start-consumers-test
  (with-redefs [products/handler      str
                products/post-request (fn [i] (+ (:val i) (get-in i [:http-options :timeout])))
                cfg/http-options      {:timeout 9}]

    (let [in-chan state/tile-in
          out-chan state/tile-out
          chunk-size 2
          consumers (products/start-consumers chunk-size in-chan out-chan)
          vals [{:val 4}]]

      (doseq [i vals]
        (async/>!! in-chan i))

      (is (= "13" (async/<!! out-chan))))))

(deftest date-range-test
  (with-redefs [cfg/product-doy (fn [i] "07-01")]
    (is (= ["2006-07-01" "2007-07-01"] (products/date-range {:grid "conus" :years "2006/2007"})))))


(deftest products-test
  (with-redefs [cfg/request-instance-count (fn [i] 1)
                functions/chips       (fn [i] [{:cx 1 :cy 2}])
                functions/tile-to-xy  (fn [i] {:x 3 :y 4})
                cfg/product-doy       (fn [i] "07-01")
                products/handler      str
                products/post-request keys
                cfg/http-options      {:timeout 9}]

    (is (= '("(:tile :dates :grid :product :cx :resource :cy :http-options)")
       (products/products {:grid "conus" :tile "027008" :product "tsc" :years "2006"})))))

(deftest maps-test
  (with-redefs [cfg/request-instance-count (fn [i] 1)
                functions/chips       (fn [i] [{:cx 1 :cy 2}])
                functions/tile-to-xy  (fn [i] {:x 3 :y 4})
                cfg/product-doy       (fn [i] "07-01")
                products/handler      str
                products/post-request keys
                cfg/http-options      {:timeout 9}]

    (is (= '("(:tile :date :chips :grid :tiley :tilex :product :resource :http-options)")
       (products/maps {:grid "conus" :tile "027008" :product "tsc" :years "2006"})))))


