(ns dana.graph.ubergraph-test
  (:require [clojure.test :refer :all]
            [ubergraph.core]
            [dana.graph.ubergraph :refer :all]))

(defn re [pattern]
  (re-pattern (java.util.regex.Pattern/quote pattern)))

(deftest test-add-nodes

  (testing "pre"

    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(pre-valid-graph graph)")
                          (add-nodes nil nil)))
    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(pre-valid-graph graph)")
                          (add-nodes {} nil)))

    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(s/valid? :dana.graph.ubergraph/nodes nodes)")
                          (add-nodes (graph) [{}])))
    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(s/valid? :dana.graph.ubergraph/nodes nodes)")
                          (add-nodes (graph) [{:n.node-id :a}]))))

  (testing "function"

    (let [node1 {:n.node_id 1
                 :foo :bar}
          node2 {:n.node_id 2
                 :moo :cow}]
      (let [new-graph (add-nodes (graph) [])]
        (is (empty? (ubergraph.core/nodes new-graph))))
      (let [new-graph (add-nodes (graph) [node1])]
        (is (= 1 (count (ubergraph.core/nodes new-graph))))
        (is (= :node1 (first (ubergraph.core/nodes new-graph))))
        (is (= node1 (ubergraph.core/attrs new-graph :node1))))
      (let [new-graph (add-nodes (graph) [node2 node1])]
        (is (= 2 (count (ubergraph.core/nodes new-graph))))
        (is (= (sort [:node1 :node2]) (sort (ubergraph.core/nodes new-graph))))
        (is (= node1 (ubergraph.core/attrs new-graph :node1)))
        (is (= node2 (ubergraph.core/attrs new-graph :node2)))))))

(deftest test-add-edges

  (testing "pre"

    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(pre-valid-graph graph)")
                          (add-edges nil nil)))
    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(pre-valid-graph graph)")
                          (add-edges {} nil)))

    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(s/valid? :dana.graph.ubergraph/edges edges")
                          (add-edges (graph) [{}])))
    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(s/valid? :dana.graph.ubergraph/edges edges")
                          (add-edges (graph) [{:node_1 :a :node_2 :b}]))))

  (testing "function"

    (let [edge1 {:node_1 123
                 :node_2 456
                 :sort-id 1
                 :foo :bar}
          edge2 {:node_1 456
                 :node_2 789
                 :sort-id 2
                 :moo :cow}
          edge->map (fn [graph edge]
                      (merge {:node_1 (ubergraph.core/src edge)
                              :node_2 (ubergraph.core/dest edge)}
                             (ubergraph.core/attrs graph edge)))
          edge-sorter #(compare (:sort-id %1) (:sort-id %2))
          #_edge-data->map #_(fn [edge-data]
                               (-> edge-data
                                   (update :node_1 integer->node-id)
                                   (update :node_2 integer->node-id)))]
      (let [new-graph (add-edges (graph) [])]
        (is (empty? (ubergraph.core/edges new-graph))))
      (let [new-graph (add-edges (graph) [edge1])]
        (is (= 1 (count (ubergraph.core/edges new-graph))))
        (is (=  edge1
                (edge->map new-graph (first (ubergraph.core/edges new-graph))))))
      (let [new-graph (add-edges (graph) [edge1 edge2])]
          (is (= 2 (count (ubergraph.core/edges new-graph))))
          (is (= (sort edge-sorter [edge1 edge2])
                 (sort edge-sorter (map (fn [edge] (edge->map new-graph edge))
                                        (ubergraph.core/edges new-graph)))))))))

(deftest test-shortest-path

  (testing "pre"

    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(pre-valid-graph graph)")
                          (shortest-path nil nil nil)))
    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(pre-valid-graph graph)")
                          (shortest-path nil nil nil)))

    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(s/valid? :dana.graph.ubergraph/n.node_id node1)")
                          (shortest-path (graph) nil nil)))
    (is (thrown-with-msg? java.lang.AssertionError
                          (re "(s/valid? :dana.graph.ubergraph/n.node_id node2)")
                          (shortest-path (graph) 1 :b))))
  
  (testing "empty graph"

    (let [test-graph (graph)]
      (is (= [] (shortest-path test-graph 1 2)))))

  (testing "two-connected-nodes"

    (let [node1 {:n.node_id 1
                 :n.name "node 1"}
          node2 {:n.node_id 2
                 :n.name "node 2"}
          node3 {:n.node_id 3
                 :n.name "node 3"}
          edge1 {:node_1 1
                 :node_2 3
                 :rel_type "linked"}
          test-graph (-> (graph)
                         (add-nodes [node1 node2 node3])
                         (add-edges [edge1]))]
      (is (= []
             (shortest-path test-graph 1 1)))
      (is (= []
             (shortest-path test-graph 1 2)))
      (is (= [{:id 1 :name "node 1"}
              {:relationship-type "linked"}
              {:id 3 :name "node 3"}]
             (shortest-path test-graph 1 3)))
      (is (= []
             (shortest-path test-graph 2 1)))
      (is (= []
             (shortest-path test-graph 2 2)))
      (is (= []
             (shortest-path test-graph 2 3)))
      (is (= []
             (shortest-path test-graph 3 1)))
      (is (= []
             (shortest-path test-graph 3 2)))
      (is (= []
             (shortest-path test-graph 3 3)))))

  (testing "self-connected-node"

    (let [node1 {:n.node_id 1
                 :n.name "node 1"}
          edge1 {:node_1 1
                 :node_2 1
                 :rel_type "me"}
          test-graph (-> (graph)
                         (add-nodes [node1])
                         (add-edges [edge1]))]
      (is (= []
             (shortest-path test-graph 1 1)))))

  (testing "three-connected-nodes"

    (let [node1 {:n.node_id 1
                 :n.name "node 1"}
          node2 {:n.node_id 2
                 :n.name "node 2"}
          node3 {:n.node_id 3
                 :n.name "node 3"}
          edge1 {:node_1 1
                 :node_2 2
                 :rel_type "linked12"}
          edge2 {:node_1 2
                 :node_2 3
                 :rel_type "linked23"}
          test-graph (-> (graph)
                         (add-nodes [node1 node2 node3])
                         (add-edges [edge1 edge2]))]
      (is (= []
             (shortest-path test-graph 1 1)))
      (is (= [{:id 1 :name "node 1"}
              {:relationship-type "linked12"}
              {:id 2 :name "node 2"}]
             (shortest-path test-graph 1 2)))
      (is (= [{:id 1 :name "node 1"}
              {:relationship-type "linked12"}
              {:id 2 :name "node 2"}
              {:relationship-type "linked23"}
              {:id 3 :name "node 3"}]
             (shortest-path test-graph 1 3)))
      (is (= []
             (shortest-path test-graph 2 1)))
      (is (= []
             (shortest-path test-graph 2 2)))
      (is (= [{:id 2 :name "node 2"}
              {:relationship-type "linked23"}
              {:id 3 :name "node 3"}]
             (shortest-path test-graph 2 3)))
      (is (= []
             (shortest-path test-graph 3 1)))
      (is (= []
             (shortest-path test-graph 3 2)))
      (is (= []
             (shortest-path test-graph 3 3)))))

  (testing "three-connected-nodes-with-short-circuit"

    (let [node1 {:n.node_id 1
                 :n.name "node 1"}
          node2 {:n.node_id 2
                 :n.name "node 2"}
          node3 {:n.node_id 3
                 :n.name "node 3"}
          edge1 {:node_1 1
                 :node_2 2
                 :rel_type "linked12"}
          edge2 {:node_1 2
                 :node_2 3
                 :rel_type "linked23"}
          edge3 {:node_1 1
                 :node_2 3
                 :rel_type "linked13"}
          test-graph (-> (graph)
                         (add-nodes [node1 node2 node3])
                         (add-edges [edge1 edge2 edge3]))]
      (is (= []
             (shortest-path test-graph 1 1)))
      (is (= [{:id 1 :name "node 1"}
              {:relationship-type "linked12"}
              {:id 2 :name "node 2"}]
             (shortest-path test-graph 1 2)))
      (is (= [{:id 1 :name "node 1"}
              {:relationship-type "linked13"}
              {:id 3 :name "node 3"}]
             (shortest-path test-graph 1 3)))
      (is (= []
             (shortest-path test-graph 2 1)))
      (is (= []
             (shortest-path test-graph 2 2)))
      (is (= [{:id 2 :name "node 2"}
              {:relationship-type "linked23"}
              {:id 3 :name "node 3"}]
             (shortest-path test-graph 2 3)))
      (is (= []
             (shortest-path test-graph 3 1)))
      (is (= []
             (shortest-path test-graph 3 2)))
      (is (= []
             (shortest-path test-graph 3 3))))))
