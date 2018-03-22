(ns fdc-ts.statistics.deployment
  (:require [googlecloud.credentials :as gc]
            [googlecloud.bigquery.service :as bs]
            [googlecloud.bigquery.datasets :as bd]
            [googlecloud.bigquery.tables :as bt]
            [googlecloud.bigquery.tabledata :as btd]
            [schema.core                     :as s]
            [environ.core :refer [env]]))

(s/defschema DeploymentRequest
  {:stage (s/enum  "test" "prod")
   :project s/Str
   :subproject s/Str
   :git-hash s/Str
   :event (s/enum "START" "ENDE")
   :uuid s/Str})

(def account-id (env :gce-account-id))
(def account-password (env :gce-account-password))
(def project-id "fdc-test-statistic")
(def dataset-id "fdc_deployment_statistic")
(def table-id "deployments")

(def table {:table-reference {:table-id   table-id
                              :project-id project-id
                              :dataset-id dataset-id}
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
                               :type :TIMESTAMP
                               :mode :nullable}]})

(defn ensure-table
  "ensures the required table is present in bigquery"
  [service project-id dataset-id table-id]
  (try
    (bt/get service project-id dataset-id table-id)
    (catch Exception e
      (bt/insert service table))))

(defn transform-data
  "transform request data to big query format"
  [data]
  {"stage" (:stage data)
   "project"   (:project data)
   "subproject"  (:subproject data)
   "git-hash" (:githash data)
   "event" (:event data)
   "uuid" (:uuid data)
   "timestamp" (quot (System/currentTimeMillis) 1000)})


(defn insert-deployment
  "persists the given deployment value to big query"
  [data]
  (if (not (or (nil? account-id) (nil? account-password)))
        (let [credentials (gc/service-credentials :account-id :account-password [(bs/scopes :manage)])
        service (bs/service credentials)
        bq-data (transform-data data)]
    (ensure-table service project-id dataset-id table-id)
    (btd/insert-all service project-id dataset-id table-id bq-data))))


(defn validate-deployment-request [json-data]
   (nil? (s/check DeploymentRequest json-data)))
