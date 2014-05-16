(ns tonic.core-test
  (:require [clojure.test :refer :all]
            [tonic.core :refer :all]
            [datomic.api :only (q db) :as d]
            ))

(def test-uri-url
  (with-redefs [datomic-db (str "test-db")]
    (str "datomic:dev://" host ":" port "/" datomic-db)))

;; Opens db
(defn test-open-dev-db []
  (let [uri test-uri-url]
    ;;(d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/schema.edn")]
      @(d/transact conn schema)
      conn)))

(defn test-clean-db []
  (d/delete-database test-uri-url))

(defn test-add-person [lastName]
  (with-redefs [conn (open-dev-db)]
    (do
      (add-person (str lastName "-0"))
      (add-person (str lastName "-1"))
      (d/db conn))))

(defn test-show-persons []
  (with-redefs [conn (open-dev-db)]
    (do
      (d/q '[:find ?lastName
             :where [?e :person/lastName ?lastName]]
           (d/db conn)))))
