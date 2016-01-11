(ns fdc-ts.db
  (:require [korma.core :as korma-core]
            [korma.db :as korma-db]
            [lobos.connectivity :as lobos]))

;TODO Bug in lobos. :db has to start with ./ -> otherwise schema exception is thrown!!
(def db-connection-info (korma-db/sqlite3 {:db "./database.db" :subname "./database.db"}))

(korma-db/defdb db db-connection-info)
(lobos/open-global db-connection-info)
