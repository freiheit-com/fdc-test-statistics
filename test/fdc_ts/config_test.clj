(ns fdc-ts.config-test
  (:require [clojure.test :refer :all]
            [fdc-ts.config :refer :all]))

(deftest should-read-empty-config-if-file-not-configured
  (is (= (#'fdc-ts.config/read-config) {})))

;TODO Test, set property to file -> reads content of file as config
