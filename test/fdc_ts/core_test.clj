(ns fdc-ts.core-test
  (:require [clojure.test :refer :all]
            [fdc-ts.core :refer :all]))

(deftest should-return-nothing-if-no-project-data-present
  (is (= (project-coverage-statistics []) {:overall-coverage {}
                                           :by-language []})))

(deftest should-return-single-value-if-only-one-value-present
  (is (= (project-coverage-statistics [{:covered 666 :lines 1332, :language "java" :subproject "test-sub" :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])
         {:overall-coverage {:covered 666 :lines 1332 :percentage 0.50}
          :by-language [{:covered 666 :lines 1332 :percentage 0.50 :language "java" :day "2015-12-03"}]})))

;TODO Test: three sub-projects, one language
;TODO Test: three sub-projects, two languages each
;TODO Test: three sub-projects, more languages, coverage data from different days
