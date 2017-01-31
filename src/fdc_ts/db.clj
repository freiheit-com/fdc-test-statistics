(ns fdc-ts.db
  (:require [fdc-ts.config :refer :all]
            [korma.core :as korma-core]
            [korma.db :as korma-db]
            [schema.core :as s]))

; load test_config.clj db config to configure database connector
(def db-connection-info (korma-db/mysql
                          {:host (get-property :db-host)
                           :port (get-property :db-port)
                           :db (get-property :db-schema)
                           :user (get-property :db-user)
                           :password (get-property :db-pass)}))

(korma-db/defdb db db-connection-info)

(def Meta-Wire {(s/required-key :projects)
                [{(s/required-key :project) s/Str
                  (s/required-key :subprojects)
                                            [{(s/required-key :subproject) s/Str
                                              (s/required-key :languages)
                                                                           [{:language s/Str}]}]}]})

(def +project-name-pattern+ "[\\w\\-]+")
(def +project-path-pattern+ (re-pattern +project-name-pattern+))
(def +project-field-pattern+ (re-pattern (str "^" +project-name-pattern+ "$")))

(def ProjectField +project-field-pattern+)
(def Project-JSON {:project ProjectField :subproject ProjectField :language ProjectField})
