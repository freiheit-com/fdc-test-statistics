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
    (is (= (select-keys (first (select-latest-coverage-data (:project +other-project+) (:subproject +other-project+) (:language +other-project+)))
                        [:covered :lines])
           data-to-insert))))

(deftest ^:integration should-update
  (let [other +first-project+
        new-coverage-data {:covered 24 :lines 42}
        data (merge other new-coverage-data)]
    (is (= :updated (insert-coverage data)))
    (is (= (select-keys (first (select-latest-coverage-data (:project +first-project+) (:subproject +first-project+) (:language +first-project+)))
                        [:covered :lines])
           new-coverage-data))
    ;we do not update older data by accident (bugfix test :)
    (is (= (select-keys (first (select-coverage-data-at (t/yesterday) (:project +first-project+) (:subproject +first-project+) (:language +first-project+)))
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
  (is (empty? (select-latest-coverage-data (:project +empty-project+) nil nil))))

(defn- _coverage?
  [l r]
  (let [fields [:project :subproject :language :covered :lines :timestamp]]
    (= (select-keys fields l) (select-keys fields r))))

(deftest ^:integration should-select-project-coverage
  (is (_coverage? +coverage-first-project+ (select-latest-coverage-data (:project +first-project+) nil nil))))

(deftest ^:integration should-select-subproject-coverage
  (let [{:keys [:project :subproject]} +first-project+]
    (is (_coverage? +coverage-first-project+ (select-latest-coverage-data project subproject nil)))))

(deftest ^:integration should-select-subproject-coverage
  (let [{:keys [:project :subproject]} +empty-project+]
    (is (empty? (select-latest-coverage-data project subproject nil)))))

(deftest ^:integration should-select-language-coverage
  (let [{:keys [:project :subproject :language]} +first-project+]
    (is (_coverage? +coverage-first-project+ (select-latest-coverage-data project subproject language)))))

;; select-coverage-data-at

(deftest ^:integration should-select-todays-coverage
  (is (_coverage? +coverage-first-project+ (select-coverage-data-at (t/today-at 23 59) (:project +first-project+)))))

(deftest ^:integration should-select-older-coverage
  (is (_coverage? +coverage-old+ (select-coverage-data-at (t/yesterday) (:project +first-project+)))))
