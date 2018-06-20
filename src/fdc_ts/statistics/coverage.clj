(ns fdc-ts.statistics.coverage
  (:use fdc-ts.statistics.big-query)
  (:require [googlecloud.credentials :as gc]
            [googlecloud.bigquery.service :as bs]
            [googlecloud.bigquery.datasets :as bd]
            [googlecloud.bigquery.tables :as bt]
            [googlecloud.bigquery.tabledata :as btd]
            [taoensso.timbre :refer [log error]]
            [environ.core :refer [env]]))

(def statistics-dataset-id "fdc_test_statistics")
(def statistics-table-id "statistics")

(def statistics-table {:table-reference {:table-id   statistics-table-id
                              :project-id project-id
                              :dataset-id statistics-dataset-id}
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

(defn- transform-coverage-data
  "transform coverage request data to big query format"
  [data]
  {"project" (:project data)
   "subproject"  (:subproject data)
   "timestamp" (quot (System/currentTimeMillis) 1000)
   "lines" (:lines data)
   "covered" (:covered data)})


(defn insert-coverage-into-bq
  "persists the given coverage value to big query"
  [data]
  (if (not  (nil? account-id))
        (let [credentials (gc/service-credentials account-id auth-file [(bs/scopes :manage)])
        service (bs/service credentials)
        bq-data (transform-coverage-data data)]
    (ensure-table service project-id statistics-dataset-id statistics-table-id statistics-table)
    (btd/insert-all service project-id statistics-dataset-id statistics-table-id [bq-data]))))
