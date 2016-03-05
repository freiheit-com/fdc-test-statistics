(ns fdc-ts.statistics.latest
  (:use fdc-ts.common))

(defn- sum-coverage [[covered lines] coverage-data]
  [(+ (:covered coverage-data) covered) (+ (:lines coverage-data) lines)])

  ;TODO calc date (freshness) for overall-coverage -> oldest timestamp of language data?
  ;-> needed for build lamps!
(defn- coverage-overall [latest]
  (let [[covered lines] (reduce sum-coverage [0 0] latest)
        overall {:covered covered :lines lines}]
    (assoc overall :percentage (coverage-percentage overall))))

;TODO allow time series query: coverage-data from to
(defn project-coverage-statistics [latest-coverage-data]
  (if (empty? latest-coverage-data)
      {:overall-coverage {}}
      {:overall-coverage (coverage-overall latest-coverage-data)}))
