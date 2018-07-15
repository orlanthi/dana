 (defproject dana "0.1.0-SNAPSHOT"
   :description "Web app to explore a Paradise Papers dataset"
   :dependencies [[org.clojure/clojure "1.9.0"]

                  ;; Web Application
                  [metosin/compojure-api "2.0.0-alpha21"]
                  [metosin/spec-tools "0.7.1"] ;; so we can convert core.spec to swagger definitions
                  [org.clojure/tools.logging "0.4.1"] ;; keeps compojure-api happy

                  ;; MySQL
                  [org.clojure/java.jdbc "0.7.7"]
                  [mysql/mysql-connector-java "8.0.11"]
                  ]

   :ring {:handler dana.handler/app}
   :uberjar-name "server.jar"
   :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                  [cheshire "5.8.0"]
                                  [ring/ring-mock "0.3.2"]]
                   :plugins [[lein-ring "0.12.4"]
                             [com.jakemccrary/lein-test-refresh "0.23.0"]]}}
   :test-refresh {:watch-dirs ["src" "test"]
                  :refresh-dirs ["src" "test"]
                  :notify-command ["terminal-notifier" "-title" "Tests" "-group" "dana" "-message"]})
