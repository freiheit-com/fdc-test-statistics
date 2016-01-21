(ns fdc-ts.core-test
  (:require [clojure.test :refer :all]
            [fdc-ts.core :refer :all]
            [fdc-ts.config :refer :all]
            [ring.mock.request :as mock]
            [clj-time [core :as t][coerce :as tc][format :as tf][predicates :as tp]]))

;;;- get-json-body

(deftest should-parse-json-from-context
  (is (= (#'fdc-ts.core/get-json-body {:request {:body (new java.io.StringReader "{\"foo\": 23, \"bar\": \"blubb\"}")}})
         {:foo 23 :bar "blubb"})))

;; previous-weekday

(deftest should-be-friday-on-monday
  (is (tp/friday? (#'fdc-ts.core/previous-weekday (t/date-time 2016 1 11)))))

(deftest should-be-friday-on-sunday
  (is (tp/friday? (#'fdc-ts.core/previous-weekday (t/date-time 2016 1 10)))))

(deftest should-be-friday-on-saturday
  (is (tp/friday? (#'fdc-ts.core/previous-weekday (t/date-time 2016 1 9)))))

(deftest should-be-thursday-on-friday
  (is (tp/thursday? (#'fdc-ts.core/previous-weekday (t/date-time 2016 1 8)))))

;;; handler test

(def +valid-statistic-token+ "test-token-stat")
(def +valid-meta-token+ "test-token-meta")

(def +test-config+ {:auth-token-publish "test-token-pub"
                    :auth-token-statistics +valid-statistic-token+
                    :auth-token-meta +valid-meta-token+})

(defn- with-valid-statistic-token [request]
  (mock/header request "auth-token" +valid-statistic-token+))

(defn- with-valid-meta-token [request]
  (mock/header request "auth-token" +valid-meta-token+))

(defn- with-invalid-token [request]
  (mock/header request "auth-token" "invalid-token"))

(defn- with-test-config [f]
  (binding [*config* +test-config+]
    (f)))

(defn- is-401-with-test-token [request]
  (with-test-config
    (fn []
      (is (= (:status (handler (with-invalid-token request)))
             401)))))

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

(deftest should-reject-request-with-invalid-project-name
  (with-test-config
    (fn []
      (is (not (:status (handler (with-valid-statistic-token (mock/request :get "/statistics/coverage/latest/86invalid-project-name+")))))))))

;;;; put-project

(deftest should-reject-put-project-with-missing-project-and-subproject-name
  (with-test-config
    (fn []
      (is (= (:status (handler (with-valid-meta-token (mock/request :put "/meta/project" "{\"language\": \"java\"}"))))
             400)))))

;TODO Write test for ok-handle (200) -> mock database out?
