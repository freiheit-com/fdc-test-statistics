(ns fdc-ts.statistics.db-test
  (:use testdb)
  (:require [clojure.test :refer :all]
            [fdc-ts.statistics.db :as db :refer :all]
            [fdc-ts.statistics.testdata :refer :all]
            [clj-time [core :as t][coerce :as tc][format :as tf][predicates :as tp]]))

(defn- setup
  [test-suite]
  (testdb/with-inmemory #(with-prepared-db test-suite)))

(use-fixtures :each setup)

;; select-most-recent-coverage-at-for-project

(defn- extract-coverage [m]
  (select-keys m [:covered :lines]))

(defn- select-most-recent [time project-def]
  (#'fdc-ts.statistics.db/select-most-recent-coverage-at-for-project time
                 (:project project-def)
                 (:subproject project-def)
                 (:language project-def)))

(defn- select-most-recent-coverage-with [time project-def]
  (select-most-recent-coverages-at time
                (:project project-def)
                (:subproject project-def)
                (:language project-def)))

(deftest ^:integration should-select-nothing-if-no-data-for-project
  (is (= nil (#'fdc-ts.statistics.db/select-most-recent-coverage-at-for-project
                   (t/today-at 23 59) "non-existing" "subproject" "java"))))

(deftest ^:integration should-select-latest-coverage-for-project-if-single-entry-for-this-project
  (is (= (extract-coverage +coverage-latest-project-1+)
         (select-most-recent (t/today-at 23 59) +select-latest-project-1+))))

(deftest ^:integration should-select-nothing-if-timepoint-before-first-entry
  (is (= nil (select-most-recent (t/date-time 2010 5 31) +select-latest-project-2+))))

(deftest ^:integration should-select-entry-from-2010-if-asked-slightly-before-push-in-2011
  (is (= (extract-coverage +coverage-latest-project-2-entry1+)
         (select-most-recent (t/date-time 2011 5 31) +select-latest-project-2+))))

(deftest ^:integration should-select-entry-from-2011-if-asked-slightly-before-push-in-2012
  (is (= (extract-coverage +coverage-latest-project-2-entry2+)
         (select-most-recent (t/date-time 2012 5 31) +select-latest-project-2+))))

(deftest ^:integration should-select-entry-from-2012-if-asked-on-same-day-as-push
  (is (= (extract-coverage +coverage-latest-project-2-entry3+)
         (select-most-recent (t/date-time 2012 6 1) +select-latest-project-2+))))

;; select-most-recent-coverages-at

(deftest ^:integration should-select-one-coverage-if-fully-specified
  (is (= #{(extract-coverage +coverage-latest-project-1+)}
         (select-most-recent-coverage-with (t/today-at 23 59) +select-latest-project-1+))))

(deftest ^:integration should-select-across-all-subprojects-with-language
  (is (= #{(extract-coverage +coverage-latest-project-3-1+)
           (extract-coverage +coverage-latest-project-3-3+)}
         (select-most-recent-coverages-at (t/today-at 23 59) "test-latest-3" :all "java"))))

(deftest ^:integration should-select-across-all-languages-with-subproject
  (is (= #{(extract-coverage +coverage-latest-project-3-1+)
           (extract-coverage +coverage-latest-project-3-1-2+)
           (extract-coverage +coverage-latest-project-3-1-3+)}
         (select-most-recent-coverages-at (t/today-at 23 59) "test-latest-3" "sub-test-latest-3-1" :all))))

(deftest ^:integration should-select-across-all
  (is (= #{(extract-coverage +coverage-latest-project-3-1+)
           (extract-coverage +coverage-latest-project-3-1-2+)
           (extract-coverage +coverage-latest-project-3-1-3+)
           (extract-coverage +coverage-latest-project-3-2+)
           (extract-coverage +coverage-latest-project-3-3+)
           (extract-coverage +coverage-latest-project-3-3-2+)}
          (select-most-recent-coverages-at (t/today-at 23 59) "test-latest-3" :all :all))))

;; insert-coverage

(deftest ^:integration should-not-insert-unknown
  (let [non-existing {}
        data (merge non-existing {:covered 23 :lines 42})]
    (is (= nil (insert-coverage data)))))

(deftest ^:integration should-insert
  (let [other +other-project+
        data-to-insert {:covered 23 :lines 42}
        data (merge other data-to-insert)]
    (is (= :inserted (insert-coverage data)))
    (is (= (select-keys (first (select-most-recent-coverages (:project +other-project+) (:subproject +other-project+) (:language +other-project+)))
                        [:covered :lines])
           data-to-insert))))

(deftest ^:integration should-update
  (let [other +first-project+
        new-coverage-data {:covered 24 :lines 42}
        data (merge other new-coverage-data)]
    (is (= :updated (insert-coverage data)))
    (is (= (select-keys (first (select-most-recent-coverages (:project +first-project+) (:subproject +first-project+) (:language +first-project+)))
                        [:covered :lines])
           new-coverage-data))
    ;we do not update older data by accident (bugfix test :)
    (is (= (select-keys (first (select-most-recent-coverages-at (t/yesterday) (:project +first-project+) (:subproject +first-project+) (:language +first-project+)))
                        [:covered :lines])
           +coverage-data-older-first-project+))))

;; coverage-for-today-exist?

(deftest ^:integration should-return-true-if-data
  (is (true? (#'db/coverage-for-today-exist? +first-project+))))

(deftest ^:integration should-return-false-if-no-data
  (is (false? (#'db/coverage-for-today-exist? +empty-project+))))

(deftest ^:integration should-return-false-if-project-not-found
  (is (false? (#'db/coverage-for-today-exist? {:project "doesntexist"}))))

;; select-latest-coverage-data

(deftest ^:integration should-select-coverage
  (is (empty? (select-most-recent-coverages (:project +empty-project+) :all :all))))

(defn- _coverage?
  [l r]
  (let [fields [:project :subproject :language :covered :lines :timestamp]]
    (= (select-keys fields l) (select-keys fields r))))

(deftest ^:integration should-select-project-coverage
  (is (_coverage? +coverage-first-project+ (select-most-recent-coverages (:project +first-project+) :all :all))))

(deftest ^:integration should-select-subproject-coverage
  (let [{:keys [:project :subproject]} +first-project+]
    (is (_coverage? +coverage-first-project+ (select-most-recent-coverages project subproject :all)))))

(deftest ^:integration should-select-subproject-coverage
  (let [{:keys [:project :subproject]} +empty-project+]
    (is (empty? (select-most-recent-coverages project subproject :all)))))

(deftest ^:integration should-select-language-coverage
  (let [{:keys [:project :subproject :language]} +first-project+]
    (is (_coverage? +coverage-first-project+ (select-most-recent-coverages project subproject language)))))

;; select-coverage-data-at

(deftest ^:integration should-select-todays-coverage
  (is (_coverage? +coverage-first-project+ (select-most-recent-coverages-at (t/today-at 23 59) (:project +first-project+) :all :all))))

(deftest ^:integration should-select-older-coverage
  (is (_coverage? +coverage-old+ (select-most-recent-coverages-at (t/yesterday) (:project +first-project+) :all :all))))
