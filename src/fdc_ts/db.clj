(ns fdc-ts.db
  (:require [korma.db :as korma]
            [lobos.connectivity :as lobos]))

;TODO Bug in lobos. :db has to start with ./ -> otherwise schema exception is thrown!!
(def db-connection-info (korma/sqlite3 {:db "./database.db" :subname "./database.db"}))

(korma/defdb db db-connection-info)
(lobos/open-global db-connection-info)
