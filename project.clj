(defproject fdc-test-statistics "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GPLv3"
            :url ""https://www.gnu.org/licenses/agpl-3.0.html""}
  :plugins [[lein-ring "0.9.6"]]
  :ring {:handler fdc-ts.core/handler
         :init lobos.core/migrate
         :ssl? true
         :ssl-port 8443
         :keystore "/your/keystore.jks"
         :key-password "your-keystore-password"}
  :main fdc-ts.core
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [liberator "0.13"]
                 [compojure "1.3.4"]
                 [ring "1.4.0"]
                 [cheshire "5.5.0"]
                 [clj-time "0.8.0"]
                 [lobos "1.0.0-beta3"]
                 [org.xerial/sqlite-jdbc "3.8.9.1"]
                 [korma "0.4.0"]])
