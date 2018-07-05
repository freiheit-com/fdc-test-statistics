(ns fdc-ts.statistics.big-query
  (:require [googlecloud.bigquery.tables :as bt]
            [environ.core :refer [env]]))

(def account-id (env :gce-account-id))
(def auth-file (env :gce-auth-file))
(def project-id "fdc-test-statistic")

(defn ensure-table
  "ensures the required table is present in bigquery"
  [service project-id dataset-id table-id table]
  (try
    (bt/get service project-id dataset-id table-id)
    (catch Exception e
      (bt/insert service table))))
