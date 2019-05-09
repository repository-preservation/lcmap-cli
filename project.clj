(defproject lcmap-cli "0.3.5"
  :description "LCMAP Devops Interface"
  :url "https://github.com/usgs-eros/lcmap-cli"
  :license {:name "Unlicense"
            :url "http://unlicense.org/"}
  :dependencies [[cheshire "5.8.1"]
                 [environ "1.1.0"]
                 [http-kit "2.2.0"]
                 [net.mikera/core.matrix "0.62.0"]
                 [net.mikera/vectorz-clj "0.48.0"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/math.combinatorics "0.1.5"]
                 [org.clojure/tools.cli "0.4.1"]
                 [environ               "1.1.0"]]
  :main ^:skip-aot lcmap-cli.core
  :target-path "target/%s"
  :repl-options {:init-ns user}
  :plugins [[lein-environ "1.1.0"]]
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [http-kit.fake "0.2.1"]]
                   :plugins [[lein-binplus "0.6.4"]]}
             :test {:resource-paths ["test" "test/resources"]
                    :dependencies [[http-kit.fake "0.2.1"]]
                    :env {:edn-file "test/resources/lcmap-cli-test.edn"}}}
  
  :bin {:name "lcmap"
        :bin-path "~/bin"
        :bootclasspath false
        :jvm-opts ["-server" "$JVM_OPTS" ]})
