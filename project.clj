(defproject lcmap-cli "0.1.3"
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
                 [org.clojure/tools.cli "0.4.1"]]
  :main ^:skip-aot lcmap-cli.core
  :target-path "target/%s"
  :repl-options {:init-ns user}
  :plugins [[lein-environ "1.1.0"]]
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["dev"]
                   :plugins [[lein-binplus "0.6.4"]]}
             :test {:resource-paths ["test" "test/resources"]
                    :dependencies [[http-kit.fake "0.2.1"]]
                    :env {:edn-file "lcmap-cli-test.edn"}}}
  
  :bin {:name "lcmap"
        :bin-path "~/bin"
        :bootclasspath false
        :jvm-opts ["-server" "$JVM_OPTS" ]})
