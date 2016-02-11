(ns fdc-ts.statistics.testdata
  (:require [cheshire.core :as cheshire]
            [fdc-ts.projects :as projects]
            [fdc-ts.statistics.db :as coverage]
            [korma.core :as k]))

(def +first-project+ {:language "java" :subproject "test-sub1" :project "test"})
(def +second-project+ {:language "java" :subproject "test-sub2" :project "test"})
(def +third-project+ {:language "java" :subproject "test-sub3" :project "test"})
(def +other-project+ {:language "java" :subproject "test-sub1" :project "other"})
(def +empty-project+ {:language "java" :subproject "test-sub1" :project "empty"})

(def +coverage-entry-first-project+ {:covered 472 :lines 1334})

(def +coverage-meta-first-project+ {:language "java" :subproject "test-sub1"
                               :project "test" :timestamp "2015-12-03T00:00:00.000Z"})

(def +coverage-first-project+ (merge +coverage-meta-first-project+ +coverage-entry-first-project+))


(def +three-sub-project-data+ [+coverage-first-project+
                               {:covered 4500 :lines 8766, :language "java" :subproject "test-sub2"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                               {:covered 0 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])

(def +three-sub-project-data-on-diff-dates+
                              [+coverage-first-project+
                               {:covered 4500 :lines 8766, :language "java" :subproject "test-sub2"
                                :project "test" :timestamp "2015-11-30T00:00:00.000Z"}
                               {:covered 0 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])

(def +three-sub-project-data-on-diff-date-and-multiple+
                              [+coverage-first-project+
                               {:covered 0 :lines 1334, :language "java" :subproject "test-sub1"
                                :project "test" :timestamp "2015-11-01T00:00:00.000Z"}
                               {:covered 4500 :lines 8766, :language "java" :subproject "test-sub2"
                                :project "test" :timestamp "2015-11-30T00:00:00.000Z"}
                               {:covered 0 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-11-15T00:00:00.000Z"}
                               {:covered 500 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-11-03T00:00:00.000Z"}])

(def +three-sub-project-expected-overall-coverage+ {:overall-coverage {:covered 4972 :lines 19888 :percentage 0.25}})

(def +three-sub-project-expected-overall-coverage-json+ (cheshire/generate-string +three-sub-project-expected-overall-coverage+))

(def +three-sub-project-diff+ [{:covered 472 :lines 1330, :language "java" :subproject "test-sub1"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                               {:covered 4000 :lines 8766, :language "java" :subproject "test-sub2"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                               {:covered 0 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])

(def +three-sub-project-expected-diff+ {:diff-percentage 0.050 :diff-lines 0 :diff-covered 50})

(defn- insert-projects []
  (projects/add-project +first-project+)
  (projects/add-project +second-project+)
  (projects/add-project +third-project+)
  (projects/add-project +other-project+)
  (projects/add-project +empty-project+))

(defn- insert-coverage
  []
  (k/insert fdc-ts.statistics.db/coverage_data (k/values (fdc-ts.statistics.db/add-today-timestamp (assoc +coverage-entry-first-project+ :projects_id (:id (projects/lookup-project +coverage-first-project+)))))))


(defn- insert-testdata
  []
  (insert-projects)
  (insert-coverage))


(defn- drop-testdata []
  (k/delete fdc-ts.statistics.db/coverage_data)
  (k/delete projects/projects)
  (comment (k/exec-raw (format "ALTER TABLE %s ALTER COLUMN %s RESTART WITH 1" "coverage_data" "projects_id"))))

(defn with-prepared-db
  "inserts testdata into db"
  [func]
  (drop-testdata)
  (insert-testdata)
  (func)
  (drop-testdata))
