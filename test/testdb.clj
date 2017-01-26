
(ns testdb
  (:require [korma.core :as korma-core]
            [korma.db :as korma-db]))

(def test-db (korma-db/h2 {:db "mem:test_mem"}))

(defn with-inmemory
  "runs the given fn with an in memory database"
  [fn]
  (korma-db/defdb db test-db))
