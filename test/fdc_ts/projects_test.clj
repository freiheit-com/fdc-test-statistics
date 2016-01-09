(ns fdc-ts.projects-test
  (:require [clojure.test :refer :all]
            [fdc-ts.projects :refer :all]))

(def +valid-project-data+ {:project "project-name_test" :subproject "subproject-name_test" :language "language-name_test"})
(def +invalid-name+ "86#invalid-project-name+")

(deftest should-not-be-valid-if-project-name-not-set
  (is (not (validate-project-data {:subproject "subproject" :language "java"}))))

(deftest should-not-be-valid-if-subproject-not-set
  (is (not (validate-project-data {:project "project" :language "java"}))))

(deftest should-not-be-valid-if-language-not-set
  (is (not (validate-project-data {:project "project" :subproject "subproject"}))))

(deftest should-not-be-valid-if-project-name-contains-unwanted-chars
  (is (not (validate-project-data (assoc +valid-project-data+ :project +invalid-name+)))))

(deftest should-not-be-valid-if-subproject-name-contains-unwanted-chars
  (is (not (validate-project-data (assoc +valid-project-data+ :subproject +invalid-name+)))))

(deftest should-not-be-valid-if-language-contains-unwanted-chars
  (is (not (validate-project-data (assoc +valid-project-data+ :language +invalid-name+)))))

(deftest should-accept-valid-project-definition
  (is (validate-project-data +valid-project-data+)))
