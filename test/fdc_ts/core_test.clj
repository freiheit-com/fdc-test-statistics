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

(def +test-config+ {:auth-token-publish "test-token-pub"
                    :auth-token-statistics "test-token-stat"})

(defn- with-invalid-token [request]
  (mock/header request "auth-token" "invalid-token"))

(defn- is-401-with-test-token [request]
  (binding [*config* +test-config+]
    (is (= (:status (handler (with-invalid-token request)))
           401))))

(defn- is-503-without-config [request]
  (binding [*config* nil]
    (is (= (:status (handler request))
           503))))

;;;; publish coverage
(def put-publish-coverage (-> (mock/request :put "/publish/coverage" "{}") (mock/content-type "application/json")))

(deftest should-reject-publish-coverage-request-without-wrong-auth-token
  (is-401-with-test-token (with-invalid-token put-publish-coverage)))

(deftest should-reject-publish-coverage-if-auth-token-not-set
  (is-503-without-config put-publish-coverage))

;;;; statistics

(def get-statistic-latest (-> (mock/request :get "/statistics/coverage/latest/testproject")))

(deftest should-reject-coverage-latest-if-wrong-auth-token
  (is-401-with-test-token (with-invalid-token get-statistic-latest)))

(deftest should-reject-coverage-latest-if-auth-token-not-set
  (is-503-without-config get-statistic-latest))

;TODO Write test for ok-handle (200) -> mock database out?
