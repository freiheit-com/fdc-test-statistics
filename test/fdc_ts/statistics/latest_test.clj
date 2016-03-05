(ns fdc-ts.statistics.latest-test
  (:require [clojure.test :refer :all]
            [fdc-ts.statistics.latest :refer :all]
            [fdc-ts.statistics.testdata :refer :all]))

;;; project-coverage-statistics

(deftest should-return-nothing-if-no-project-data-present
  (is (= (project-coverage-statistics #{})
         {:overall-coverage {}})))

(deftest should-return-single-value-if-only-one-value-present
  (is (= (project-coverage-statistics #{{:covered 666 :lines 1332}})
         {:overall-coverage {:covered 666 :lines 1332 :percentage 0.50}})))

(deftest should-aggregate-coverage-data-across-all-subprojects-and-languages
  (is (= (project-coverage-statistics #{{:covered 75 :lines 100, :language "java" :subproject "test1"
                                         :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                                        {:covered 50 :lines 100, :language "clojure" :subproject "test1"
                                         :project "test" :timestamp "2015-11-01T00:00:00.000Z"}
                                        {:covered 25 :lines 100, :language "java" :subproject "test2"
                                         :project "test" :timestamp "2015-12-02T00:00:00.000Z"}
                                        {:covered 0 :lines 100, :language "clojure" :subproject "test2"
                                         :project "test" :timestamp "2015-12-01T00:00:00.000Z"}
                                        {:covered 10 :lines 100, :language "javascript" :subproject "test2"
                                         :project "test" :timestamp "2015-12-01T00:00:00.000Z"}})
         {:overall-coverage {:covered 160 :lines 500 :percentage 0.32}})))
