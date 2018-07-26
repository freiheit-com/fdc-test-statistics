(ns fdc-ts.statistics.deployment
  (:use fdc-ts.statistics.big-query)
  (:require [googlecloud.credentials :as gc]
            [googlecloud.bigquery.service :as bs]
            [googlecloud.bigquery.datasets :as bd]
            [googlecloud.bigquery.tables :as bt]
            [googlecloud.bigquery.tabledata :as btd]
            [taoensso.timbre :refer [log error]]
            [schema.core                     :as s]
            [environ.core :refer [env]]))

(s/defschema DeploymentRequest
  {:stage (s/enum  "test" "prod")
   :project s/Str
   :subproject s/Str
   :githash s/Str
   :event s/Str
   :uuid s/Str
   (s/optional-key :tags) [s/Str]})

(def deployment-dataset-id "fdc_deployment_statistic")
(def deployment-table-id "deployments")

(def ^:private deployment-table {:table-reference {:table-id deployment-table-id
                              :project-id project-id
                              :dataset-id deployment-dataset-id}
            :description     "Contains the start and end tracking data of deployments."
            :schema          [{:name "stage"
                               :type :string
                               :mode :required}
                              {:name "project"
                               :type :string
                               :mode :required}
                              {:name "subproject"
                               :type :string
                               :mode :required}
                              {:name "githash"
                               :type :string
                               :mode :required}
                              {:name "event"
                               :type :string
                               :mode :required}
                              {:name "uuid"
                               :type :string
                               :mode :required}
                              {:name "timestamp"
                               :type :timestamp
                               :mode :nullable}
                              {:name "tags"
                               :type :string
                               :mode :repeated}]})

(defn- transform-data
  "transform request data to big query format"
  [data]
  {"stage" (:stage data)
   "project" (:project data)
   "subproject"  (:subproject data)
   "githash" (:githash data)
   "event" (:event data)
   "uuid" (:uuid data)
   "timestamp" (quot (System/currentTimeMillis) 1000)
   "tags" (:tags data)})


(defn insert-deployment
  "persists the given deployment value to big query"
  [data]
  (if (not  (nil? account-id))
        (let [credentials (gc/service-credentials account-id auth-file [(bs/scopes :manage)])
        service (bs/service credentials)
        bq-data (transform-data data)]
    (ensure-table service project-id deployment-dataset-id deployment-table-id deployment-table)
    (btd/insert-all service project-id deployment-dataset-id deployment-table-id [bq-data]))))

(defn validate-deployment-request [json-data]
   (nil? (s/check DeploymentRequest json-data)))
