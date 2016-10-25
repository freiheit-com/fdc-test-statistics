(ns fdc-ts.statistics.testdata
  (:require [cheshire.core :as cheshire]
            [fdc-ts.projects :as projects]
            [fdc-ts.statistics.db :as coverage]
            [korma.core :as k]
            [clj-time [core :as t][coerce :as tc][format :as tf][predicates :as tp]]))

(def +first-project+ {:language "java" :subproject "test-sub1" :project "test"})
(def +second-project+ {:language "java" :subproject "test-sub2" :project "test"})
(def +third-project+ {:language "java" :subproject "test-sub3" :project "test"})
(def +empty-subproject+ {:language "java" :subproject "test-sub4" :project "test"})
(def +other-project+ {:language "java" :subproject "test-sub1" :project "other"})
(def +empty-project+ {:language "java" :subproject "test-sub1" :project "empty"})
(def +inactive-project+ {:language "java" :subproject "test-sub1" :project "inactive"})



(def +coverage-data-first-project+ {:covered 430 :lines 1334})

(def +coverage-entry-first-project+ (merge +coverage-data-first-project+ {:projects_id 1 :timestamp (tc/to-timestamp (t/today-at 00 00))}))

(def +coverage-data-older-first-project+ {:covered 472 :lines 1334})

(def +coverage-entry-older-first-project+ (merge +coverage-data-older-first-project+ {:projects_id 1 :timestamp (tc/to-timestamp (t/yesterday))}))


(def +coverage-meta-first-project+ {:language "java" :subproject "test-sub1"
                                    :project "test" :timestamp "2015-12-03T00:00:00.000Z"})

(def +coverage-old+ (merge +coverage-meta-first-project+ +coverage-data-older-first-project+))
(def +coverage-first-project+ (merge +coverage-meta-first-project+ +coverage-data-first-project+))


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

(def +three-sub-project-expected-overall-coverage+ {:overall-coverage {:covered 4930 :lines 19888 :percentage 0.2478881737731295}})

(def +three-sub-project-expected-overall-coverage-json+ (cheshire/generate-string +three-sub-project-expected-overall-coverage+))

(def +three-sub-project-diff+ [{:covered 472 :lines 1330, :language "java" :subproject "test-sub1"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                               {:covered 4000 :lines 8766, :language "java" :subproject "test-sub2"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                               {:covered 0 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])

(def +three-sub-project-expected-diff+ {:diff-percentage 0.050 :diff-lines 0 :diff-covered 50})


; test projects for selecting latest coverage data from db
(def +select-latest-project-1+ {:language "java" :subproject "sub-test-latest-1" :project "test-latest-1"})
(def +coverage-latest-project-1+ {:projects_id 6 :timestamp (tc/to-timestamp (t/date-time 2010 8 7)) :covered 56 :lines 786})

(def +select-latest-project-2+ {:language "java" :subproject "sub-test-latest-2" :project "test-latest-2"})
(def +coverage-latest-project-2-entry1+ {:projects_id 7 :timestamp (tc/to-timestamp (t/date-time 2010 6 1)) :covered 100 :lines 1000})
(def +coverage-latest-project-2-entry2+ {:projects_id 7 :timestamp (tc/to-timestamp (t/date-time 2011 6 1)) :covered 200 :lines 2000})
(def +coverage-latest-project-2-entry3+ {:projects_id 7 :timestamp (tc/to-timestamp (t/date-time 2012 6 1)) :covered 300 :lines 3000})

(def +select-latest-project-3-1+ {:language "java" :subproject "sub-test-latest-3-1" :project "test-latest-3"})
(def +select-latest-project-3-1-2+ {:language "clojure" :subproject "sub-test-latest-3-1" :project "test-latest-3"})
(def +select-latest-project-3-1-3+ {:language "javascript" :subproject "sub-test-latest-3-1" :project "test-latest-3"})
(def +select-latest-project-3-2+ {:language "clojure" :subproject "sub-test-latest-3-2" :project "test-latest-3"})
(def +select-latest-project-3-3+ {:language "java" :subproject "sub-test-latest-3-3" :project "test-latest-3"})
(def +select-latest-project-3-3-2+ {:language "clojure" :subproject "sub-test-latest-3-3" :project "test-latest-3"})
(def +coverage-latest-project-3-1+ {:projects_id 8 :timestamp (tc/to-timestamp (t/date-time 2016 3 3)) :covered 1000 :lines 10000})
(def +coverage-latest-project-3-1-2+ {:projects_id 9 :timestamp (tc/to-timestamp (t/date-time 2016 3 3)) :covered 1100 :lines 11000})
(def +coverage-latest-project-3-1-3+ {:projects_id 10 :timestamp (tc/to-timestamp (t/date-time 2016 3 3)) :covered 1200 :lines 12000})
(def +coverage-latest-project-3-2+ {:projects_id 11 :timestamp (tc/to-timestamp (t/date-time 2016 3 3)) :covered 2000 :lines 20000})
(def +coverage-latest-project-3-3+ {:projects_id 12 :timestamp (tc/to-timestamp (t/date-time 2016 3 3)) :covered 3000 :lines 30000})
(def +coverage-latest-project-3-3-2+ {:projects_id 13 :timestamp (tc/to-timestamp (t/date-time 2016 3 3)) :covered 3100 :lines 31000})

(defn- insert-projects []
  (projects/add-project +first-project+)
  (projects/add-project +second-project+)
  (projects/add-project +third-project+)
  (projects/add-project +other-project+)
  (projects/add-project +empty-project+)
  (projects/add-project +inactive-project+)

  (projects/add-project +select-latest-project-1+)
  (projects/add-project +select-latest-project-2+)

  (projects/add-project +select-latest-project-3-1+)
  (projects/add-project +select-latest-project-3-1-2+)
  (projects/add-project +select-latest-project-3-1-3+)
  (projects/add-project +select-latest-project-3-2+)
  (projects/add-project +select-latest-project-3-3+)
  (projects/add-project +select-latest-project-3-3-2+))

(defn- insert-coverage
  []
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-entry-older-first-project+))
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-entry-first-project+))

  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-1+))
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-2-entry1+))
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-2-entry2+))
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-2-entry3+))

  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-3-1+))
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-3-1-2+))
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-3-1-3+))
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-3-2+))
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-3-3+))
  (k/insert fdc-ts.statistics.db/coverage_data (k/values +coverage-latest-project-3-3-2+)))

(defn- insert-testdata
  []
  (insert-projects)
  (insert-coverage))


(defn- reset-autoincrement
  [table column]
  (k/exec-raw (format "ALTER TABLE \"%s\" ALTER COLUMN \"%s\" RESTART WITH 1" table column)))

(defn- drop-testdata []
  (k/delete fdc-ts.statistics.db/coverage_data)
  (k/delete projects/projects)
  (reset-autoincrement "projects" "id"))

(defn with-prepared-db
  "inserts testdata into db"
  [func]
  (drop-testdata)
  (insert-testdata)
  (func)
  (drop-testdata))
