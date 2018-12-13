(ns lcmap-cli.state
  (:require [cheshire.core :as json]
            [clojure.core.async :as async]
            [clojure.string :as string]
            [clojure.walk :refer [stringify-keys keywordize-keys]]))

(def run-threads? (atom true))

(def stdout (async/chan))

(def stderr (async/chan))

(def detect-tile-in (async/chan))

(def detect-tile-out (async/chan))

(def stdout-writer (async/thread (while (true? @run-threads?) (-> (async/<!! stdout) stringify-keys json/encode println))))

(def stderr-writer (binding [*out* *err*] (async/thread (while (true? @run-threads?) (-> (async/<!! stderr) stringify-keys json/encode println)))))

(defn shutdown
  []
  (swap! run-threads? (fn [_] false)))
   
