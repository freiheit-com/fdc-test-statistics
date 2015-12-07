(defproject fdc-test-statistics "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GPLv3"
            :url ""https://www.gnu.org/licenses/agpl-3.0.html""}
  :plugins [[lein-ring "0.9.6"]]
  :ring {:handler fdc-ts.core/handler}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [liberator "0.13"]
                 [compojure "1.3.4"]
                 [ring/ring-core "1.2.1"]
                 [cheshire "5.5.0"]])
