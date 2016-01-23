(defproject fdc-test-statistics "0.7.1"
  :description "lightweight test statistic data storage and aggregation"
  :url "https://github.com/freiheit-com/fdc-test-statistics"
  :license {:name "GPLv3"
            :url ""https://www.gnu.org/licenses/agpl-3.0.html""}
  :plugins [[lein-ring "0.9.6"]]
  :ring {:handler fdc-ts.core/handler
         :init lobos.core/migrate
         :ssl? true
         :ssl-port 8443
         :keystore "test_keystore.jks" ;YOU NEED TO CHANGE THIS IN PRODUCTION
         :key-password "testpwd"} ;YOU NEED TO CHANGE THIS IN PRODUCTION
  :jvm-opts ["-Dfdc.ts.config.file=test_config.clj"] ;YOU NEED TO CHANGE THIS IN PRODUCTION
  :main fdc-ts.core
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [liberator "0.14.0"]
                 [compojure "1.3.4"]
                 [ring "1.4.0"]
                 [ring/ring-mock "0.3.0"]
                 [cheshire "5.5.0"]
                 [clj-time "0.8.0"]
                 [lobos "1.0.0-beta3"]
                 [org.xerial/sqlite-jdbc "3.8.9.1"]
                 [korma "0.4.0"]])
