(ns fdc-ts.core-test
  (:require [clojure.test :refer :all]
            [fdc-ts.core :refer :all]))

(deftest should-return-nothing-if-no-project-data-present
  (is (= (project-coverage-statistics []) {:overall-coverage {}})))


(deftest should-return-single-value-if-only-one-value-present
  (is (= (project-coverage-statistics [{:covered 666 :lines 1332, :language "java" :subproject "test-sub" :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])
         {:overall-coverage {:covered 666 :lines 1332 :percentage 0.50}})))

(deftest should-aggregate-sub-project-data
  (is (= (project-coverage-statistics [{:covered 472 :lines 1334, :language "java" :subproject "test-sub1"
                                        :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                                       {:covered 4500 :lines 8766, :language "java" :subproject "test-sub2"
                                        :project "test" :timestamp "2015-12-03T00:00:00.000Z"}
                                       {:covered 0 :lines 9788, :language "java" :subproject "test-sub3"
                                         :project "test" :timestamp "2015-12-03T00:00:00.000Z"}])
        {:overall-coverage {:covered 4972 :lines 19888 :percentage 0.25}})))

;TODO Test: three sub-projects, two languages each
;TODO Test: three sub-projects, more languages, coverage data from different days
