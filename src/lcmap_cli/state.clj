(ns lcmap-cli.state
  (:require [cheshire.core :as json]
            [clojure.core.async :as async]
            [clojure.string :as string]
            [clojure.walk :refer [stringify-keys keywordize-keys]]))

(def run-threads? (atom true))

(def detect-tile-in (async/chan))

(def detect-tile-out (async/chan))

(defn shutdown
  []
  (swap! run-threads? (fn [_] false)))
   
