(ns testdb
  (:require [korma.core :as korma-core]
            [korma.db :as korma-db]
            [lobos
             [connectivity :as lobos.connectivity]
             [core :as lobos.core]]))

(def test-db (korma-db/h2 {:db "mem:test_mem"}))

(defn- run-with-db
  [fn]
  (lobos.connectivity/with-connection :testdb
    (lobos.core/migrate)
    (fn)))

(defn with-inmemory
  "runs the given fn with an in memory database"
  [fn]
  (lobos.connectivity/open-global :testdb test-db)
  (korma-db/defdb db test-db)
  (try
    (run-with-db fn)
    (finally (lobos.connectivity/close-global :testdb true))))
