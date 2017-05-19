(ns fdc-ts.migrations
  (:require [migratus.core :as migratus]
            [environ.core :refer [env]]))

(def config {:store                :database
             :migration-dir        "migrations/"
             :migration-table-name "migrations"
             :db {:classname   "com.mysql.jdbc.Driver"
                  :subprotocol "mysql"
                  :subname     (str "//" (env :db-host) ":" (env :db-port) "/" (env :db-schema))
                  :user        (env :db-user)
                  :password    (env :db-pass)}})

(defn migrate []
  (migratus/migrate config))
