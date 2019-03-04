(ns lcmap-cli.config-test
  (:require [clojure.test :refer :all]
            [environ.core :refer [env]]
            [lcmap-cli.config :refer :all]))


(deftest load-edn-test
  (testing "load edn file"
    (is (= (type (load-edn edn-file)) clojure.lang.PersistentArrayMap))
    (is (not (nil? (:http (load-edn edn-file)))))
    (is (not (nil? (:grids (load-edn edn-file)))))))
