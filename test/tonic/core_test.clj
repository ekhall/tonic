(ns tonic.core-test
  (:require [clojure.test :refer :all]
            [tonic.core :refer :all]
            [datomic.api :only (q db) :as d]
            ))

(def uri-url "datomic:dev://localhost:4334/test-db")

;; Opens db
(defn open-dev-db []
  (let [uri uri-url]
    ;;(d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/schema.edn")]
      @(d/transact conn schema)
      conn)))

(defn clean-db []
  (d/delete-database uri-url))

(defn show-schema []
  (with-redefs [conn (open-dev-db)]
    (do
      (d/q '[:find ?ident
       :where [_ :db/ident ?ident]]
           (d/db conn)))))

(defn show-persons []
  (with-redefs [conn (open-dev-db)]
    (do
      (d/q '[:find ?lastName
             :where [?e :person/lastName ?lastName]]
           (d/db conn)))))

(defn test-add-person [lastName]
  (with-redefs [conn (open-dev-db)]
    (do
      (add-person (str lastName "-0"))
      (add-person (str lastName "-1"))
      (d/db conn))))
