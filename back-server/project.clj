(defproject color-namer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies
  [[org.clojure/clojure "1.10.3"]
   [ring-server "0.5.0"]
   [ring "1.8.1"]
   [ring/ring-defaults "0.3.2"]
   [hiccup "1.0.5"]
   [yogthos/config "1.1.7"]
   [metosin/reitit "0.5.12"]
   [com.datomic/client-cloud  "1.0.119"]
   [com.datomic/datomic-free "0.9.5697"
    :exclusions [joda-time
                 org.slf4j/slf4j-nop
                 com.google.guava/guava
                 commons-codec]]]
  :jvm-opts ["-Xmx1G"]
  :plugins [[lein-environ "1.1.0"]
            [lein-asset-minifier "0.4.6"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler color-namer.handler/app
         :uberwar-name "color-namer.war"}

  :min-lein-version "2.5.0"
  :uberjar-name "color-namer.jar"
  :main color-namer.server

  :source-paths ["src"]
  :resource-paths ["resources" "target/cljsbuild"]

  :profiles
  {:dev {:dependencies
         [[ring/ring-mock "0.4.0"]
          [ring/ring-devel "1.9.1"]
          [cljs-http "0.1.46"]]

         :source-paths ["src"]

         :env {:dev true}}

   :uberjar {:source-paths ["src"]
             :env {:production true}
             :aot :all
             :omit-source true}})
