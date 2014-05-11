(ns tonic.core-test
  (:require [clojure.test :refer :all]
            [tonic.core :refer :all]
            [datomic.api :only (q db) :as d]
            ))

(defn create-empty-dev-db []
  (let [uri "datomic:dev://localhost:4334/test-db"]
    ;;(d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/schema.edn")]
      @(d/transact conn schema)
      conn)))

(create-empty-dev-db)



(defn test-add-person [lastName]
  (with-redefs [conn (create-empty-dev-db)]
    (do
      (add-person (str lastName "-0"))
      (add-person (str lastName "-1"))
      (d/q '[:find ?lastName
             :where [?e :person/lastName ?lastName]]
           (d/db conn)))))

(test-add-person "Smith")
