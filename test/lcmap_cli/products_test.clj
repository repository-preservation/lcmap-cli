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
                                    :body "[\"foo\"]"})
                             {:cx 111 :cy 222}))))

  (testing "handler with non 200 status"
    (is (= {:error ["foo error"], :status 400, :cx 111, :cy 222}
           (products/handler (atom {:status 400
                                    :headers {:content-type "application/json"}
                                    :body "[\"foo error\"]"})
                              {:cx 111 :cy 222}))))

  (testing "handler with decode failure"
    (is (= {:error "java.lang.ClassCastException: clojure.lang.PersistentArrayMap cannot be cast to java.util.concurrent.Future" :cx 111 :cy 222}
           (products/handler {:status 400
                              :headers {:content-type "application/json"}
                              :body "[\"foo error\"]"}
                              {:cx 111 :cy 222})))))

(deftest post-request-test
  (with-redefs [http/client (fn [a b c d e] (assoc e a b c d))]
    (is (= (products/post-request {:grid "foo" :resource "bar"})
           {:body "{\"grid\":\"foo\",\"resource\":\"bar\"}", :headers {"Content-Type" "application/json"}, :post :foo, :ccdc :bar}))))

(deftest start-consumers-test
  (with-redefs [products/handler      (fn [a b] (str a))
                products/post-request (fn [i] (+ (:val i) (get-in i [:http-options :timeout])))
                cfg/http-options      {:timeout 9}]

    (let [in-chan (async/chan)
          out-chan (async/chan)
          chunk-size 2
          consumers (products/start-consumers chunk-size in-chan out-chan)
          vals [{:val 4}]]

      (doseq [i vals]
        (async/>!! in-chan i))

      (is (= "13" (async/<!! out-chan))))))

(deftest date-range-test
    (is (= ["2006-07-01" "2007-07-01"] (products/date-range {:grid "fake-http" :years "2006/2007"}))))

(deftest chip-test
  (with-redefs [products/handler      (fn [a b] (str a))
                products/post-request keys
                cfg/http-options      {:timeout 9}
                functions/xy-to-tile  (fn [i] "027008")]

    (is (= "(:tile :dates :grid :products :cx :resource :cy :http-options)"
           (products/chip {:grid "conus" :cx 111111 :cy 222222 :names "tsc" :years "2006"})))))

(deftest product-test
  (with-redefs [cfg/product-instance-count (fn [i] 1)
                functions/chips       (fn [i] [{:cx 1 :cy 2}])
                functions/tile-to-xy  (fn [i] {:x 3 :y 4})
                products/handler      (fn [a b] (str a))
                products/post-request keys
                cfg/http-options      {:timeout 9}]

    (is (= '("(:tile :dates :grid :products :cx :resource :cy :http-options)")
       (products/product {:grid "conus" :tile "027008" :names "tsc" :years "2006"})))))

(deftest raster-test
  (with-redefs [cfg/raster-instance-count (fn [i] 1)
                functions/chips       (fn [i] [{:cx 1 :cy 2}])
                functions/tile-to-xy  (fn [i] {:x 3 :y 4})
                products/handler      (fn [a b] b)
                products/post-request keys
                cfg/http-options      {:timeout 9}]

    (is (= '({:tile "027008", :date "2006--", :grid "conus", :ty 4, :tx 3, :product "tsc", :resource "raster", :http-options {:timeout 7200000}})
       (products/raster {:grid "conus" :tile "027008" :names "tsc" :years "2006"})))))

(deftest bundle-test
  (with-redefs [cfg/bundle-instance-count (fn [i] 1)
                functions/tile-to-xy  (fn [i] {:x 3 :y 4})
                products/handler      (fn [a b] b)
                products/post-request keys
                cfg/http-options      {:timeout 9}]

    (is (= '({:tile "027008", :date "2006--", :grid "conus", :ty 4, :tx 3, :resource "bundle", :http-options {:timeout 7200000}})
       (products/bundle {:grid "conus" :tile "027008" :tar "foo.tar" :years "2006"})))))
