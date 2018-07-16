(ns dana.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [dana.graph.core]
            [dana.graph.neo4j :as neo4j]
            [dana.graph.ubergraph :as in-memory]
            [dana.paradise-sql.core :as paradise-sql]))

(def in-memory-graph (atom nil))

(defn- load-in-memory-graph []
  (swap! in-memory-graph (fn [old]
                           (-> (in-memory/graph)
                               (in-memory/add-nodes (paradise-sql/nodes))
                               (in-memory/add-edges (paradise-sql/edges)))))
  in-memory-graph)

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "Dana"
                   :description "Paradise Papers Query API"}
            :tags [{:name "api", :description "API operartions"}]}}}

   (context "/api" []
            :tags ["api"]
            :coercion :spec

            (GET "/shortest-path" []
                 :no-doc false
                 :description (str "Returns the shortest path between two 'nodes' in the Paradise Papers. "
                                   "If no path can be found then the output will be an empty list. "
                                   "If a path is found then it will be output as an alternating  sequence of "
                                   "Nodes and Edges, starting with the start node provided and finishing with "
                                   "the end node.")
                 ;; The only reason this is commented out is because the schema description for the output is poor.
                 ;; I've tried to change no-doc to true and specify the paths explicitly in the swagger section but
                 ;; can't seem to make that work either. (TODO: contact metosin)
                 #_:return #_{:result :dana.graph.core/node-path }
                 :query-params [from-node-id :- :dana.graph.core.node/node-id
                                to-node-id :- :dana.graph.core.node/node-id]
       
                 (ok {:result (neo4j/shortest-path (neo4j/conn) from-node-id to-node-id)}))

            (GET "/in-memory/shortest-path" []
                 :no-doc false
                 :description (str "As per /shortest-path but uses an in-memory database. (Note:"
                                   " this can take up to 30 s to load the first time.")

                 :query-params [from-node-id :- :dana.graph.core.node/node-id
                                to-node-id :- :dana.graph.core.node/node-id]
       
                 (do
                   (if (nil? @in-memory-graph) (load-in-memory-graph))
                   (ok {:result (in-memory/shortest-path @in-memory-graph from-node-id to-node-id)}))))))
