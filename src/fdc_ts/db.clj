(ns fdc-ts.db
  (:require [korma.core :as korma-core]
            [korma.db :as korma-db]
            [lobos.connectivity :as lobos]
            [schema.core :as s]))

;TODO Bug in lobos. :db has to start with ./ -> otherwise schema exception is thrown!!
(def db-connection-info (korma-db/sqlite3 {:db "./database.db" :subname "./database.db"}))

(korma-db/defdb db db-connection-info)
(lobos/open-global db-connection-info)


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
