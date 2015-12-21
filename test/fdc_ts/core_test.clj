(ns fdc-ts.core-test
  (:require [clojure.test :refer :all]
            [fdc-ts.core :refer :all]
            [fdc-ts.config :refer :all]
            [ring.mock.request :as mock]))

;;;- get-json-body

(deftest should-parse-json-from-context
  (is (= (#'fdc-ts.core/get-json-body {:request {:body (new java.io.StringReader "{\"foo\": 23, \"bar\": \"blubb\"}")}})
         {:foo 23 :bar "blubb"})))

;;; handler test

(def put-publish-coverage (-> (mock/request :put "/publish/coverage" "{}") (mock/content-type "application/json")))

(deftest should-reject-publish-coverage-request-without-wrong-auth-token
  (binding [*config* {:auth-token-publish "test-token"}]
    (is (= (:status (handler (mock/header put-publish-coverage "auth-token" "invalid-token")))
           401))))

(deftest should-always-reject-if-auth-token-not-set
  (binding [*config* nil]
    (is (= (:status (handler put-publish-coverage))
          503))))
