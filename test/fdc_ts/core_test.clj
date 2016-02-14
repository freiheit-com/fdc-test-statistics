(ns fdc-ts.core-test
  (:require [clojure.test :refer :all]
            [fdc-ts.core :as core :refer :all]
            [fdc-ts.config :refer :all]
            [fdc-ts.statistics.testdata :refer :all]
            [fdc-ts.projects :as projects]
            [fdc-ts.statistics.db :as db]
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

;; weekday-days-back

(deftest should-be-the-same-as-previous-weekdays-with-one-day-back
  (is (tp/friday? (#'fdc-ts.core/weekday-days-back (t/date-time 2010 8 7) 1)))
  (is (tp/thursday? (#'fdc-ts.core/weekday-days-back (t/date-time 2016 1 8) 1))))

(deftest should-go-back-three-working-days
  (is (tp/wednesday? (#'fdc-ts.core/weekday-days-back (t/date-time 2010 8 7) 3)))
  (is (tp/friday? (#'fdc-ts.core/weekday-days-back (t/date-time 2010 8 10) 3))))

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

(defmacro with-test-config [body]
  `(with-redefs [*config* +test-config+]
    (~@body)))

(defn- is-401-with-test-token [request]
  (with-test-config
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

(deftest should-reject-request-with-invalid-project-name
  (with-test-config
      (is (not (:status (handler (with-valid-statistic-token (mock/request :get "/statistics/coverage/latest/86invalid-project-name+"))))))))

;;;; put-project

(deftest should-reject-put-project-with-missing-project-and-subproject-name
  (with-test-config
      (is (= (:status (handler (with-valid-meta-token (mock/request :put "/meta/project" "{\"language\": \"java\"}"))))
             400))))

;; get-projects

(deftest should-get-projects
  (with-test-config
    (with-redefs-fn {#'projects/get-all-projects (fn [] :valid-projects)}
      #(let [response (handler (with-valid-meta-token (mock/request :get "/meta/projects")))]
         (is (= 200 (:status response)))
         (is (= "\"valid-projects\"" (:body response)))))))

;; get-project-coverage-statistic

(deftest should-get-project-coverage
  (with-test-config
    (with-redefs-fn {#'db/select-latest-coverage-data
                     (fn [project & _]
                       (is (= "test" project))
                       +three-sub-project-data+)}
      #(let [response (handler (with-valid-statistic-token (mock/request :get "/statistics/coverage/latest/test")))]
         (is (= 200 (:status response)))
         (is (= +three-sub-project-expected-overall-coverage-json+ (:body response)))))))

(deftest should-get-subproject-coverage
  (with-test-config
    (with-redefs-fn {#'db/select-latest-coverage-data
                     (fn [project subproject & _]
                       (is (= "test" project))
                       (is (= "test-sub1" subproject))
                       +three-sub-project-data+)}
      #(let [response (handler (with-valid-statistic-token (mock/request :get "/statistics/coverage/latest/test/test-sub1")))]
         (is (= 200 (:status response)))
         (is (= +three-sub-project-expected-overall-coverage-json+ (:body response)))))))

(deftest should-get-language-coverage
  (with-test-config
    (with-redefs-fn {#'db/select-latest-coverage-data
                     (fn [project subproject language & _]
                       (is (= "test" project))
                       (is (= "test-sub1" subproject))
                       (is (= "java" language))
                       +three-sub-project-data+)}
      #(let [response (handler (with-valid-statistic-token (mock/request :get "/statistics/coverage/latest/test/test-sub1/java")))]
         (is (= 200 (:status response)))
         (is (= +three-sub-project-expected-overall-coverage-json+ (:body response)))))))

;; project-diff-date

(deftest should-diff-dates
  (with-redefs-fn {#'db/select-latest-coverage-data (fn [project & _]
                                                      +three-sub-project-data+)
                   #'db/select-coverage-data-at     (fn [project & _] +three-sub-project-diff+)}
    #(is (= {:diff-percentage 0.0251, :diff-lines 4, :diff-covered 500} (project-diff-date "test" (t/now))))))

;; get-project-coverage-diff

(deftest should-get-project-diff-if-days-are-not-supplied
  (with-test-config
    (with-redefs-fn {#'core/project-diff-date (fn [& _] :called)}
      #(let [response (handler (with-valid-statistic-token (mock/request :get "/statistics/coverage/diff/test")))]
         (is (= 200 (:status response)))
         (is (= "\"called\"" (:body response)))))))

(deftest should-get-project-diff-with-supplied-days-back
  (with-test-config
    (with-redefs-fn {#'core/project-diff-date (fn [& _] :called)}
      #(let [response (handler (with-valid-statistic-token (mock/request :get "/statistics/coverage/diff/test/days/4")))]
          (is (= 200 (:status response)))
          (is (= "\"called\"" (:body response)))))))

(deftest should-not-be-found-if-illegal-day-supplied
  (with-test-config
    (with-redefs-fn {#'core/project-diff-date (fn [& _] :called)}
       #(let [response (handler (with-valid-statistic-token (mock/request :get "/statistics/coverage/diff/test/days/invalidDay")))]
          (is (= nil response))))))
