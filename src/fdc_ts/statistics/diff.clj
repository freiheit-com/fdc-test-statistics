(ns fdc-ts.statistics.diff
  (:use fdc-ts.common)
  (:require [clj-time [core :as t][coerce :as tc][format :as tf]]))

(def ^:private null-coverage {:overall-coverage {:covered 0 :lines 0 :percentage 0.0}})

;TODO use lib function!?
(defn- round-to-precision
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

(defn- coverage-field [coverage field]
  (-> coverage :overall-coverage field))

(defn- diff-percentage [old newd]
  (round-to-precision 3 (- (coverage-field newd :percentage)
                           (coverage-field old :percentage))))

(defn- diff-lines [old newd]
  (- (coverage-field newd :lines)
     (coverage-field old :lines)))

(defn- diff-covered [old newd]
  (- (coverage-field newd :covered)
     (coverage-field old :covered)))

(defn- calc-diff [old newd]
  {:diff-percentage (diff-percentage old newd)
   :diff-lines (diff-lines old newd)
   :diff-covered (diff-covered old newd)})

(defn- null-if-empty
  [cov]
  (if (or (empty? cov) (empty? (:overall-coverage cov))) null-coverage cov))

(defn project-coverage-diff [old newd]
  (calc-diff (null-if-empty old) (null-if-empty newd)))
