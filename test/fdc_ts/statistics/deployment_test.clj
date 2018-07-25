(ns fdc-ts.statistics.deployment-test
  (:require [clojure.test :refer :all]
            [fdc-ts.statistics.big-query :as bq :refer :all]
            [fdc-ts.statistics.deployment :as d :refer :all]
            [environ.core :refer [env]]
            [googlecloud.credentials :as gc]
            [googlecloud.bigquery.service :as bs]
            [googlecloud.bigquery.datasets :as bd]
            [googlecloud.bigquery.tables :as bt]
            [googlecloud.bigquery.tabledata :as btd]))

(deftest should-valdate-json
  (is  (d/validate-deployment-request {:stage "test"
                                       :project "test"
                                       :subproject "sub project"
                                       :githash "git hash"
                                       :event "START"
                                       :uuid "uuid"})))

(deftest should-fail-if-json-parameter-absent
  (is  (not (d/validate-deployment-request {:stage "test"
                                       :project "test"
                                       :subproject "sub project"
                                       :githash "git hash"
                                       :event "START"}))))


(deftest should-fail-if-not-enum-value
  (is  (not (d/validate-deployment-request {:stage "Große Bühne"
                                       :project "test"
                                       :subproject "sub project"
                                       :githash "git hash"
                                       :event "BEGIN"
                                       :uuid "uuid"}))))
(deftest should-valdate-json-with-tags
         (is  (d/validate-deployment-request {:stage "test"
                                              :project "test"
                                              :subproject "sub project"
                                              :githash "git hash"
                                              :event "START"
                                              :uuid "uuid"
                                              :tags ["dev" "integration test" "core"]})))


(deftest should-insert-in-bq
           (with-redefs [bq/account-id "test"
                   gc/service-credentials (fn [_ _ _])
                   bs/service (fn [_])
                   bt/get (fn [_ _ _])
                   bt/insert (fn [_ _])
                   btd/insert-all
                   (fn [_ _ _ _ data]
                                        (is  (= "foo" (get (first data) "project")))
                                        (is (< 1521727923 (get (first data) "timestamp"))))]
    (let [data {:stage "test"
                      :project "foo"
                      :subproject "sub"
                      :githash "git hash"
                      :event "START"
                      :uuid "uuid"
                      :tags ["dev" "main"]}]
      (d/insert-deployment data))))
