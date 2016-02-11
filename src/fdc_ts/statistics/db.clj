(ns fdc-ts.statistics.db
  (:use fdc-ts.common
        fdc-ts.projects)
  (:require [korma.core :refer :all]
            [clj-time [core :as t][coerce :as tc][format :as tf][predicates :as tp]]))

(defentity coverage_data
  (belongs-to projects))

(defn add-today-timestamp [map]
  (assoc map :timestamp (tc/to-timestamp (t/today-at 00 00))))

(defn- coverage-query-today [{:keys [project subproject language]}]
  (add-today-timestamp {:projects.project project
                        :projects.subproject subproject
                        :projects.language language}))

(defn- coverage-for-today-exist? [data]
  (pos? (count (select coverage_data
                (with projects)
                (where (coverage-query-today data))))))

(defn- build-select-coverage []
  (-> (select* coverage_data)
      (with* projects #'identity)
      (order :timestamp :DESC)))

(defn- build-select-coverage-at
  "builds a query selecting coverage for PROJECT at TIME"
  [time project]
  (where (build-select-coverage) {:projects.project project
                                  ;we look back at most one month to, to ensure O(1) time complexity for statistic calculation
                                  :timestamp [between (map tc/to-timestamp [(t/minus time (t/months 1)) time])]}))

(defn- add-clause-if-not-nil
  ""
  [query test clause]
  (if test
    (where query clause)
    query))


(defn- build-select-coverage-data-at
  "selects coverage data of PROJECT since TIME"
  [time project subproject language]
  (->
   (build-select-coverage-at time project)
   (add-clause-if-not-nil subproject {:projects.subproject subproject})
   (add-clause-if-not-nil language {:projects.language language})))

(defn- today-date []
  (t/today-at 23 59))


(defn select-coverage-data-at
  "select coverage at TIME for PROJECT"
  [time project]
  (exec (build-select-coverage-data-at time project nil nil)))

(defn select-latest-coverage-data [project subproject language]
  (exec (build-select-coverage-data-at (today-date) project subproject language)))

;TODO Validate data (lines, covered)!!!
(defn insert-coverage [data]
  (let [project (lookup-project data)
        coverage-data (select-keys data [:covered :lines])]
    (if (coverage-for-today-exist? data)
      (update coverage_data (set-fields coverage-data) (where {:projects_id (:id project)}))
      (let [insert-data (add-today-timestamp (assoc coverage-data :projects_id (:id project)))
            id (insert coverage_data (values insert-data))]
        (println "insert" insert-data id)
        id))))
