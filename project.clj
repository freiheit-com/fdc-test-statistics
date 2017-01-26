(defproject fdc-test-statistics "0.10.0"
  :description "lightweight test statistic data storage and aggregation"
  :url "https://github.com/freiheit-com/fdc-test-statistics"
  :license {:name "GPLv3"
            :url ""https://www.gnu.org/licenses/agpl-3.0.html""}
  :plugins [[lein-ring "0.9.7"]]
  :ring {:port 3001
         :handler fdc-ts.core/handler
         :init fdc-ts.migrations/migrate
         :ssl? true
         :nrepl {:start? true}
         :ssl-port 8443
         :keystore "test_keystore.jks" ;YOU NEED TO CHANGE THIS IN PRODUCTION
         :key-password "testpwd"} ;YOU NEED TO CHANGE THIS IN PRODUCTION
  :repl-options {:nrepl-middleware
                 [cider.nrepl.middleware.apropos/wrap-apropos
                  cider.nrepl.middleware.classpath/wrap-classpath
                  cider.nrepl.middleware.complete/wrap-complete
                  cider.nrepl.middleware.refresh/wrap-refresh
                  cider.nrepl.middleware.format/wrap-format
                  cider.nrepl.middleware.info/wrap-info
                  cider.nrepl.middleware.inspect/wrap-inspect
                  cider.nrepl.middleware.macroexpand/wrap-macroexpand
                  cider.nrepl.middleware.ns/wrap-ns
                  cider.nrepl.middleware.resource/wrap-resource
                  cider.nrepl.middleware.stacktrace/wrap-stacktrace
                  cider.nrepl.middleware.test/wrap-test
                  cider.nrepl.middleware.trace/wrap-trace
                  cider.nrepl.middleware.undef/wrap-undef
                  refactor-nrepl.middleware/wrap-refactor]}
  :jvm-opts ["-Dfdc.ts.config.file=test_config.clj"] ;YOU NEED TO CHANGE THIS IN PRODUCTION
  :main fdc-ts.core
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.12.0"]]
                   :dependencies [[midje "1.8.3"]]}}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [acyclic/squiggly-clojure "0.1.4"]
                 [cheshire "5.5.0"]
                 [cider/cider-nrepl "0.11.0-SNAPSHOT"]
                 [refactor-nrepl "2.0.0"]
                 [clj-time "0.8.0"]
                 [com.h2database/h2 "1.4.191"]
                 [compojure "1.3.4"]
                 [korma "0.4.0"]
                 [liberator "0.14.0"]
                 [mysql/mysql-connector-java "5.1.38"]
                 [migratus "0.8.13"]
                 ;[org.xerial/sqlite-jdbc "3.8.9.1"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [javax.servlet/javax.servlet-api "3.1.0"]
                 [com.taoensso/timbre "4.2.1"]
                 [com.fzakaria/slf4j-timbre "0.3.2"]
                 [prismatic/schema "1.0.5"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-servlet "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-mock "0.3.0"]
                 [ring-cors "0.1.7"]
                 [org.clojure/math.combinatorics "0.1.1"]])
