(ns lobos.migrations
  (:refer-clojure :exclude [alter drop
                            bigint boolean char double float time])
  (:use [lobos migration core schema]))

(defmigration add-project-table
  (up [] (create
              (table :projects
                (integer :id :auto-inc :primary-key)
                (varchar :project 100)
                (varchar :subproject 100)
                (varchar :language 100)
                (unique [:project :subproject :language]))))
  (down [] (drop (table :projects))))

(defmigration add-coverage-data-table
  (up [] (create
            (table :coverage_data
              (timestamp :timestamp)
              (integer :projects_id [:refer :projects :id])
              (integer :lines)
              (integer :covered))))
  (down [] (drop (table :coverage_data))))
