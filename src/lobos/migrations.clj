(ns lobos.migrations
  (:refer-clojure :exclude [alter drop
                            bigint boolean char double float time])
  (:use [lobos migration core schema]))

;(defn run-migrations []
;  (binding [lobos.migration/*migrations-namespace* 'fdc-ts.migrations]
;    (migrate)))

(defmigration add-coverage-data-table
  (up [] (create
            (table :coverage_data
              (varchar :project_name 100)
              (varchar :subproject 100)
              (varchar :language 100))))
  (down [] (drop (table :coverage_data))))
