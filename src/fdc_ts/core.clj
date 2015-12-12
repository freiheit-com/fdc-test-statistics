(ns fdc-ts.core
  (:use [korma db core] fdc-ts.db cheshire.core [clj-time [core :as t][coerce :as tc]])
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

(defn- project-coverage-staticists [coverage-data]
  ;TODO Write some tests
  ;scan coverage data: collect latest coverage for each language (reduce ... {:languages #{}, :collect } coverage-data)
  coverage-data)

(defresource put-coverage []
  :available-media-types ["application/json"]
  :allowed-methods [:put]
  :put! (fn [ctx] (insert-coverage (get-json-body ctx))))

(defresource get-project-coverage-statistic [project]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (generate-string (project-coverage-staticists (select-latest-coverage-data project)))))

;TODO
;GET /statistic/coverage/<project-name> -> aggregate coverage-data for project <project-name>


;UI -> Beliebig baubar gegen die API, web-ui mit reagent o.ä.

(defroutes app
  (PUT "/data/coverage" [] (put-coverage))
  (GET ["/statistics/coverage/:project" :project #"\w+"] [project] (get-project-coverage-statistic project)))

(def handler
  (-> app wrap-params))
