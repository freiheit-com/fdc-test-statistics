(ns fdc-ts.statistics.latest
  (:use fdc-ts.common)
  (:require [taoensso.timbre :refer [log logf]]
            [clj-time [core :as t][coerce :as tc][format :as tf]]))

(def latest-coverage-start-state {:seen [] :collect []})

(defn- seen [data]
  [(:subproject data) (:language data)])

(defn- parse-date [long-or-string]
  (let [date (if (number? long-or-string)
               (tc/from-long long-or-string)
               (tf/parse long-or-string))]
    (tf/unparse (tf/formatter "yyyy-MM-dd") date)))


(defn- db-to-api-data [data]
  (assoc (select-keys data [:project :subproject :lines :covered :language])
         :day (parse-date (:timestamp data))
         :percentage (coverage-percentage data)))

(defn- latest-coverage-data [state data]
  (if (some #(= % (seen data)) (:seen state))
      state
      {:seen (conj (:seen state) (seen data))
       :collect (conj (:collect state) (db-to-api-data data))}))

(defn- sum-coverage [[covered lines] coverage-data]
  [(+ (:covered coverage-data) covered) (+ (:lines coverage-data) lines)])

  ;TODO calc date (freshness) for overall-coverage -> oldest timestamp of language data?
  ;-> needed for build lamps!
(defn- coverage-overall [latest]
  (let [[covered lines] (reduce sum-coverage [0 0] latest)
        overall {:covered covered :lines lines}]
    (assoc overall :percentage (coverage-percentage overall))))

  ;TODO allow time series query: coverage-data from to
(defn project-coverage-statistics [coverage-data]
  (let [latest-for-subproject (:collect (reduce latest-coverage-data latest-coverage-start-state coverage-data))]
    (log :debug "formating coverage: " latest-for-subproject)
    (if (empty? latest-for-subproject)
        {:overall-coverage {}}
        {:overall-coverage (coverage-overall latest-for-subproject)})))
