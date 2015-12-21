(ns fdc-ts.statistics.latest-test
  (:require [clojure.test :refer :all]
            [fdc-ts.statistics.latest :refer :all]))

;;; project-coverage-statistics

(def +three-sub-project-data+ [{:covered 472 :lines 1334, :language "java" :subproject "test-sub1"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                               {:covered 4500 :lines 8766, :language "java" :subproject "test-sub2"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                               {:covered 0 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])

(def +three-sub-project-data-on-diff-dates+
                              [{:covered 472 :lines 1334, :language "java" :subproject "test-sub1"
                                :project "test" :timestamp "2015-11-03T00:00:00.000Z"}
                               {:covered 4500 :lines 8766, :language "java" :subproject "test-sub2"
                                :project "test" :timestamp "2015-11-30T00:00:00.000Z"}
                               {:covered 0 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])

(def +three-sub-project-data-on-diff-date-and-multiple+
                              [{:covered 472 :lines 1334, :language "java" :subproject "test-sub1"
                                :project "test" :timestamp "2015-11-03T00:00:00.000Z"}
                               {:covered 0 :lines 1334, :language "java" :subproject "test-sub1"
                                :project "test" :timestamp "2015-11-01T00:00:00.000Z"}
                               {:covered 4500 :lines 8766, :language "java" :subproject "test-sub2"
                                :project "test" :timestamp "2015-11-30T00:00:00.000Z"}
                               {:covered 0 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-11-15T00:00:00.000Z"}
                               {:covered 500 :lines 9788, :language "java" :subproject "test-sub3"
                                :project "test" :timestamp "2015-11-03T00:00:00.000Z"}])

(def +three-sub-project-expected-overall-coverage+ {:overall-coverage {:covered 4972 :lines 19888 :percentage 0.25}})

(deftest should-return-nothing-if-no-project-data-present
  (is (= (project-coverage-statistics []) {:overall-coverage {}})))

(deftest should-return-single-value-if-only-one-value-present
  (is (= (project-coverage-statistics [{:covered 666 :lines 1332, :language "java" :subproject "test-sub" :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])
         {:overall-coverage {:covered 666 :lines 1332 :percentage 0.50}})))

(deftest should-aggregate-latest-coverage-data-of-project
  (is (= (project-coverage-statistics [{:covered 75 :lines 100, :language "java" :subproject "test"
                                        :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                                       {:covered 25 :lines 100, :language "java" :subproject "test"
                                        :project "test" :timestamp "2015-12-02T00:00:00.000Z"}
                                       {:covered 0 :lines 100, :language "java" :subproject "test"
                                        :project "test" :timestamp "2015-12-01T00:00:00.000Z"}])
         {:overall-coverage {:covered 75 :lines 100 :percentage 0.75}})))

(deftest should-aggregate-sub-project-data
  (is (= (project-coverage-statistics +three-sub-project-data+)
         +three-sub-project-expected-overall-coverage+)))

(deftest should-aggregate-latest-coverage-of-sub-project-even-if-not-on-same-day
  (is (= (project-coverage-statistics +three-sub-project-data-on-diff-dates+)
         +three-sub-project-expected-overall-coverage+)))

(deftest should-aggregate-latest-coverage-of-sub-project-even-if-not-on-same-day-and-has-multiple
  (is (= (project-coverage-statistics +three-sub-project-data-on-diff-date-and-multiple+)
         +three-sub-project-expected-overall-coverage+)))

(deftest should-aggregate-coverage-data-across-all-subprojects-and-languages
  (is (= (project-coverage-statistics [{:covered 75 :lines 100, :language "java" :subproject "test1"
                                        :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                                       {:covered 50 :lines 100, :language "clojure" :subproject "test1"
                                        :project "test" :timestamp "2015-11-01T00:00:00.000Z"}
                                       {:covered 0 :lines 100, :language "java" :subproject "test1"
                                        :project "test" :timestamp "2015-12-02T00:00:00.000Z"} ;old data
                                       {:covered 25 :lines 100, :language "java" :subproject "test2"
                                        :project "test" :timestamp "2015-12-02T00:00:00.000Z"}
                                       {:covered 0 :lines 100, :language "clojure" :subproject "test2"
                                        :project "test" :timestamp "2015-12-01T00:00:00.000Z"}
                                       {:covered 0 :lines 100, :language "clojure" :subproject "test2"
                                        :project "test" :timestamp "2015-11-30T00:00:00.000Z"} ;old data
                                       {:covered 10 :lines 100, :language "javascript" :subproject "test2"
                                        :project "test" :timestamp "2015-12-01T00:00:00.000Z"}])
         {:overall-coverage {:covered 160 :lines 500 :percentage 0.32}})))
