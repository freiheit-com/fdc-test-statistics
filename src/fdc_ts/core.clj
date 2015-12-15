(ns fdc-ts.core
  (:gen-class)
  (:use [korma db core] fdc-ts.db cheshire.core [clj-time [core :as t][coerce :as tc][format :as tf]] [ring.adapter.jetty])
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY GET PUT POST]]
            [compojure.route :as route]))

;DESIGN-Prinzip: Alles extrem simpel und einfach halten!!

(defentity coverage_data)

(defn- get-json-body [ctx]
  (parse-string (slurp (get-in ctx [:request :body])) true))

(defn- add-today-timestamp [map]
  (assoc map :timestamp (t/today-at 00 00)))

(defn- coverage-today [data]
   (add-today-timestamp (select-keys data [:project :subproject :language])))

(defn- coverage-for-today-exist? [data]
  (> (count (select coverage_data (where (coverage-today data)))) 0))

;TODO Validate data!!!
(defn- insert-coverage [data]
  (if (coverage-for-today-exist? data)
    (update coverage_data (set-fields data) (where (coverage-today data)))
    (insert coverage_data (values (add-today-timestamp data)))))

(defn- select-latest-coverage-data [project]
  (let [end-today (t/today-at 23 59)]
    (select coverage_data (where {:project project
                                  :timestamp [between [(t/minus end-today (t/months 1)) end-today]]})
                                  ;we look back at most one month to, to ensure O(1) time complexity for statistic calculation
                          (order :timestamp :DESC))))

(defn- coverage-percentage [data]
  (let [lines (:lines data)]
    (if (<= lines 0)
      1.0 ;by definition
      (double (/ (:covered data) lines)))))

(defn- db-to-api-data [data]
  (assoc (select-keys data [:project :subproject :lines :covered :language])
    :day (tf/unparse (tf/formatter "yyyy-MM-dd") (tf/parse (:timestamp data)))
    :percentage (coverage-percentage data)))

(defn- latest-coverage-start-state []
  {:seen [] :collect []})

(defn- seen [data]
  [(:subproject data) (:language data)])

(defn- latest-coverage-data [state data]
  (if (some #(= % (seen data)) (:seen state))
    state
    {:seen (conj (:seen state) (seen data))
     :collect (conj (:collect state) (db-to-api-data data))}))

(defn- sum-coverage [[covered lines] coverage-data]
  [(+ (:covered coverage-data) covered) (+ (:lines coverage-data) lines)])

;TODO calc date (freshness) for overall-coverage -> oldest timestamp of language data?
;-> needed for build lamps!
(defn- coverage-overall [latest]
  (let [[covered lines] (reduce sum-coverage [0 0] latest)
        overall {:covered covered :lines lines}]
    (assoc overall :percentage (coverage-percentage overall))))

;TODO allow time series query: coverage-data from to
(defn project-coverage-statistics [coverage-data]
  (let [lastest-for-subproject (:collect (reduce latest-coverage-data latest-coverage-start-state coverage-data))]
    (if (empty? lastest-for-subproject)
      {:overall-coverage {}}
      {:overall-coverage (coverage-overall lastest-for-subproject)})))

  ;TODO Write more tests!

(defresource put-coverage []
  :available-media-types ["application/json"]
  :allowed-methods [:put]
  :put! (fn [ctx] (insert-coverage (get-json-body ctx))))

(defresource get-project-coverage-statistic [project]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (generate-string (project-coverage-statistics (select-latest-coverage-data project)))))

;TODO
;GET /statistic/coverage/<project-name> -> aggregate coverage-data for project <project-name>


;UI -> Beliebig baubar gegen die API, web-ui mit reagent o.ä.

(defroutes app
  (PUT "/data/coverage" [] (put-coverage))
  (GET ["/statistics/coverage/latest/:project" :project #"\w+"] [project] (get-project-coverage-statistic project)))

(def handler
  (-> app wrap-params))

(defn -main [& args]
  (run-jetty app {:port 10000 :join? false}))
