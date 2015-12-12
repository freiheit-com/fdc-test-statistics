(ns lobos.migrations
  (:refer-clojure :exclude [alter drop
                            bigint boolean char double float time])
  (:use [lobos migration core schema]))

(defmigration add-coverage-data-table
  (up [] (create
            (table :coverage_data
              (timestamp :timestamp)
              (varchar :project 100)
              (varchar :subproject 100)
              (varchar :language 100)
              (integer :lines)
              (integer :covered))))
  (down [] (drop (table :coverage_data))))
