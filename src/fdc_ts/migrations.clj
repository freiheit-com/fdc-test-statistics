(ns fdc-ts.migrations
  (:require [migratus.core :as migratus]
            [fdc-ts.config :refer :all]))

(def config {:store                :database
             :migration-dir        "migrations/"
             :migration-table-name "migrations"
             :db {:classname   "com.mysql.jdbc.Driver"
                  :subprotocol "mysql"
                  :subname     (str "//" (get-property :db-host) ":" (get-property :db-port) "/" (get-property :db-schema)) ; TODO load from config
                  :user        (get-property :db-user)
                  :password    (get-property :db-pass)}})

(defn migrate []
  (migratus/migrate config))