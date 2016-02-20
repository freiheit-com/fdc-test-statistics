(ns fdc-ts.config-test
  (:require [clojure.test :refer :all]
            [fdc-ts.config :refer :all]))

(deftest should-read-empty-config-if-file-not-configured
  (is (= (#'fdc-ts.config/read-config) {})))

(deftest should-read-property-file-if-configured
  (try
    (System/setProperty +config-system-property+ "test/fdc_ts/testfiles/testconfig.conf")
    (is (= (#'fdc-ts.config/read-config) {:auth-token-publish "publish-test-token"}))
    (finally (System/clearProperty +config-system-property+))))
