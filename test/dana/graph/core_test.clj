(ns dana.graph.core-test
  (:require [clojure.test :refer :all]
            [dana.graph.core]
            [clojure.spec.alpha :as s]))

(deftest test-node-schema

  (testing "spec: :dana.graph.core.node/node-id"
    
    (is (s/valid? :dana.graph.core.node/node-id
                  123))
    (is (not (s/valid? :dana.graph.core.node/node-id
                       "abc"))))

  (testing "spec :dana.graph.core.node/description"
    
    (is (s/valid? :dana.graph.core.node/description
                  "abc"))
    (is (not (s/valid? :dana.graph.core.node/description
                       1))))

  (testing "spec :dana.graph.core/node"

    (is (s/valid? :dana.graph.core/node
                  {:node-id 123 :description "abc"}))
    (is (not (s/valid? :dana.graph.core/node
                       {:node-id :a :description "abc"})))
    (is (not (s/valid? :dana.graph.core/node
                       {:node-id 123 :description :a})))
    (is (not (s/valid? :dana.graph.core/node
                       {:node-id :a :description :a})))))

(deftest test-edge-schema

  (testing "spec: dana.graph.core.edge/relationship-type"

    (is (s/valid? :dana.graph.core.edge/relationship-type
                  "abc"))
    (is (not (s/valid? :dana.graph.core.edge/relationship-type
                       1))))

  (testing "spec: dana.graph.core/edge"

    (is (s/valid? :dana.graph.core/edge
                  {:relationship-type "spouse"}))
    (is (not (s/valid? :dana.graph.core/edge
                       {:relationship-type 1})))))

(deftest test-node-path-schema

  (testing "spec: dana.graph.core/node-path"

    (is (not (s/valid? :dana.graph.core/node-path
                       nil)))
    (is (not (s/valid? :dana.graph.core/node-path
                       [nil])))
    (is (not (s/valid? :dana.graph.core/node-path
                       {})))
    (is (s/valid? :dana.graph.core/node-path
                  []))
    (is (s/valid? :dana.graph.core/node-path
                  [{:node-id 123 :description "foo"}
                   {:relationship-type "friends"}
                   {:node-id 456 :description "bar"}]))
    (is (not (s/valid? :dana.graph.core/node-path
                       [{:node-id 123 :description "foo"}])))
    (is (not (s/valid? :dana.graph.core/node-path
                       [{:relationship-type "friends"}
                        {:node-id 456 :description "bar"}])))
    (is (not (s/valid? :dana.graph.core/node-path
                       [{:node-id 123 :description "foo"}
                        {:node-id 456 :description "bar"}])))))
