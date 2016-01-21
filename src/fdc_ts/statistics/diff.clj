(ns fdc-ts.statistics.diff
  (:use fdc-ts.common)
  (:require [clj-time [core :as t][coerce :as tc][format :as tf]]))

;TODO use lib function!
(defn- round2
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

(defn project-coverage-diff [coverage-data-old coverage-data-new]
  (cond (or (empty? coverage-data-old) (empty? coverage-data-new)) {}
        :else {:diff-percentage (round2 3 (- (-> coverage-data-new :overall-coverage :percentage)
                                             (-> coverage-data-old :overall-coverage :percentage)))}))
