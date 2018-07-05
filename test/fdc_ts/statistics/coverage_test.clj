(ns fdc-ts.statistics.coverage-test
  (:require [clojure.test :refer :all]
            [fdc-ts.statistics.big-query :as bq :refer :all]
            [fdc-ts.statistics.coverage :as d :refer :all]
            [environ.core :refer [env]]
            [googlecloud.credentials :as gc]
            [googlecloud.bigquery.service :as bs]
            [googlecloud.bigquery.datasets :as bd]
            [googlecloud.bigquery.tables :as bt]
            [googlecloud.bigquery.tabledata :as btd]))

(deftest should-insert-coverage-into-bq
           (with-redefs [bq/account-id "test"
                   gc/service-credentials (fn [_ _ _])
                   bs/service (fn [_])
                   bt/get (fn [_ _ _])
                   bt/insert (fn [_ _])
                   btd/insert-all
                   (fn [_ _ _ _ data]
                                        (is (= "foo" (get (first data) "project")))
                                        (is (< 1521727923 (get (first data) "timestamp"))))]
    (let [data {:project "foo"
                      :subproject "sub"
                      :lines 100
                      :covered 2}]
      (d/insert-coverage-into-bq data))))
