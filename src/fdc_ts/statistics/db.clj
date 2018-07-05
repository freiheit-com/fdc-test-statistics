(ns fdc-ts.statistics.db
  (:use fdc-ts.common
        fdc-ts.projects
        fdc-ts.statistics.coverage)
  (:require [clojure.set :as set]
            [korma.core :refer :all]
            [taoensso.timbre :refer [log logf]]
            [clj-time [core :as t][coerce :as tc][format :as tf][predicates :as tp]]
            [clojure.math.combinatorics :as combo]))

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

(defn- today-date []
  (t/today-at 23 59))

(defn- select-most-recent-coverage-at-for-project
  "selects the most recent coverage data for the supplied time-point.
   project, subproject and language are here not optional."
  [time project subproject language]
  (first (select coverage_data
            (with projects)
            (fields :covered :lines)
            (where {:projects.project project
                    :projects.subproject subproject
                    :projects.language language
                    :timestamp [<= (tc/to-timestamp time)]})
            (order :timestamp :DESC)
            (limit 1))))
;; timestamps in entries older than 2016-02-16 are in UTC string format and won't be found with to-timestamp

(defn- resolve-all [project]
  (set (map #(list (:project %) (:subproject %) (:language %))
          (select projects
            (fields :project :subproject :language)
            (where {:project project})))))

(defn- resolve-all-subprojects-for [project language]
  (let [where-clause (if (= :all language) {:project project} {:project project :language language})]
    (set (map :subproject (select projects
                            (modifier "DISTINCT")
                            (fields :subproject)
                            (where where-clause))))))

(defn- resolve-all-languages-for [project subproject]
  (let [where-clause (if (= :all subproject) {:project project} {:project project :subproject subproject})]
    (set (map :language (select projects
                            (modifier "DISTINCT")
                            (fields :language)
                            (where where-clause))))))

(defn- subproject-set [project subproject-spec language-spec]
  (if (= :all subproject-spec)
    (resolve-all-subprojects-for project language-spec)
    #{subproject-spec}))

(defn- language-set [project subproject-spec lang-spec]
  (if (= :all lang-spec)
    (resolve-all-languages-for project subproject-spec)
    #{lang-spec}))

(defn- project-selection [project subproject lang]
  (if (and (= subproject :all) (= lang :all))
    (resolve-all project)
    (combo/cartesian-product #{project}
                             (subproject-set project subproject lang)
                             (language-set project subproject lang))))

(defn select-most-recent-coverages-at
  "selects the most recent coverage data for the supplied time-point.
   subproject and language may be supplied as the special keyword :all, meaning
   the coverage data should be aggregate across all subproject and languages.
   The project is mandantory and may not use the :all keyword."
  [time project subproject lang]
  (let [selection (project-selection project subproject lang)]
    (set/select #(not (= nil %)) (set (map (partial apply (partial select-most-recent-coverage-at-for-project time)) selection)))))

(defn select-most-recent-coverages [project subproject language]
  (select-most-recent-coverages-at (today-date) project subproject language))

(defn- insert-new-coverage-for-today
  "inserts a row into db for when COVERAGE-DATA does not yet exist in PROJECT"
  [coverage-data project]
  (let [insert-data (add-today-timestamp (assoc coverage-data :projects_id (:id project)))
        id (insert coverage_data (values insert-data))]
    :inserted))

(defn- update-todays-coverage
  "updates row in db with COVERAGE-DATA for PROJECT"
  [coverage-data project]
  (update coverage_data (set-fields coverage-data) (where (add-today-timestamp {:projects_id (:id project)})))
  :updated)

(defn insert-coverage [data]
  (let [project (lookup-project data)
        coverage-data (select-keys data [:covered :lines])]
    (when project
      (log :info "inserting coverage for project " project coverage-data)
      (if (coverage-for-today-exist? data)
        (update-todays-coverage coverage-data project)
        (insert-new-coverage-for-today coverage-data project))
      (log :info "inserting coverage into big query")
      (insert-coverage-into-bq data)
      )))
