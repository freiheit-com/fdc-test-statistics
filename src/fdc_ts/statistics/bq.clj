(ns fdc-ts.statistics.bq
  (:require [googlecloud.credentials :as gc]
            [googlecloud.bigquery.service :as bs]
            [googlecloud.bigquery.datasets :as bd]
            [googlecloud.bigquery.tables :as bt]
            [googlecloud.bigquery.tabledata :as btd]
            [taoensso.timbre :refer [log error]]
            [environ.core :refer [env]]))

(def ^:private account-id (env :gce-account-id))
(def ^:private auth-file (env :gce-auth-file))
(def ^:private project-id "fdc-test-statistic")
(def ^:private dataset-id "fdc_test_statistics")
(def ^:private table-id "statistics")

(def ^:private table {:table-reference {:table-id   table-id
                              :project-id project-id
                              :dataset-id dataset-id}
            :description     "Contains coverage data with a timestamp."
            :schema          [{:name "project"
                               :type :string
                               :mode :required}
                              {:name "subproject"
                               :type :string
                               :mode :required}
                              {:name "timestamp"
                               :type :timestamp
                               :mode :required}
                              {:name "lines"
                               :type :integer
                               :mode :required}
                              {:name "covered"
                               :type :integer
                               :mode :required}]})

(defn- ensure-table
  "ensures the required table is present in bigquery"
  [service project-id dataset-id table-id]
  (try
    (bt/get service project-id dataset-id table-id)
    (catch Exception e
      (bt/insert service table))))

(defn- transform-data
  "transform request data to big query format"
  [data]
  {"project" (:project data)
   "subproject"  (:subproject data)
   "timestamp" (quot (System/currentTimeMillis) 1000)
   "lines" (:lines data)
   "covered" (:covered data)})


(defn insert-coverage-in-bq
  "persists the given coverage value to big query"
  [data]
  (if (not  (nil? account-id))
        (let [credentials (gc/service-credentials account-id auth-file [(bs/scopes :manage)])
        service (bs/service credentials)
        bq-data (transform-data data)]
    (ensure-table service project-id dataset-id table-id)
    (btd/insert-all service project-id dataset-id table-id [bq-data]))))
