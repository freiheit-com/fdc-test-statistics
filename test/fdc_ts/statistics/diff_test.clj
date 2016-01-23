(ns fdc-ts.statistics.diff-test
  (:require [clojure.test :refer :all]
            [fdc-ts.statistics.diff :refer :all]))

(deftest should-calc-positive-diff
  (is (= (project-coverage-diff {:overall-coverage {:covered 700 :lines 1000 :percentage 0.7}}
                                {:overall-coverage {:covered 750 :lines 1000 :percentage 0.75}})
         {:diff-percentage 0.050 :diff-lines 0 :diff-covered 50})))

(deftest should-calc-negative-diff
  (is (= (project-coverage-diff {:overall-coverage {:covered 500 :lines 1000 :percentage 0.5}}
                                {:overall-coverage {:covered 400 :lines 1000 :percentage 0.40}})
         {:diff-percentage -0.100 :diff-lines 0 :diff-covered -100})))

(deftest should-calc-zero-diff
  (is (= (project-coverage-diff {:overall-coverage {:covered 1000 :lines 1000 :percentage 1.0}}
                                {:overall-coverage {:covered 1000 :lines 1000 :percentage 1.0}})
         {:diff-percentage 0.00 :diff-lines 0 :diff-covered 0})))

(deftest should-calc-line-diff
  (is (= (project-coverage-diff {:overall-coverage {:covered 500 :lines 1000 :percentage 0.5}}
                                {:overall-coverage {:covered 1000 :lines 2000 :percentage 0.5}})
         {:diff-percentage 0.00 :diff-lines 1000 :diff-covered 500})))

(deftest should-calc-new-diff-if-old-is-empty
  (is (= (project-coverage-diff {} {:overall-coverage {:covered 1000 :lines 1000 :percentage 1.0}})
         {:diff-percentage 1.0 :diff-covered 1000 :diff-lines 1000})))

(deftest should-calc-negative-diff-if-new-is-empty
   (is (= (project-coverage-diff {:overall-coverage {:covered 1000 :lines 1000 :percentage 1.0}} {})
          {:diff-percentage -1.0 :diff-covered -1000 :diff-lines -1000})))

(deftest should-calc-zero-diff-if-both-are-empty
   (is (= (project-coverage-diff {} {})
          {:diff-covered 0 :diff-lines 0 :diff-percentage 0.0})))
