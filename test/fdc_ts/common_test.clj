(ns fdc-ts.common-test
  (:require [clojure.test :refer :all]
            [fdc-ts.common :refer :all]))

;;;- coverage-percentage

(deftest should-calc-1-if-lines-are-zero ;covered 100 makes no sense, but says 1.0 anyway
  (is (= (coverage-percentage {:lines 0 :covered 100}) 1.0)))

(deftest should-calc-percentage
  (is (= (coverage-percentage {:lines 1000 :covered 300}) 0.3)))
