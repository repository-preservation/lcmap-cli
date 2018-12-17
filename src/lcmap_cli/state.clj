(ns lcmap-cli.state
  (:require [cheshire.core :as json]
            [clojure.core.async :as async]
            [clojure.string :as string]
            [clojure.walk :refer [stringify-keys keywordize-keys]]))

(def run-threads? (atom true))

(defn shutdown
  []
  (swap! run-threads? (fn [_] false)))
   
