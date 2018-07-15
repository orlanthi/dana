(ns dana.graph.core
  "Provides schema and functions relating to the Paradise Papers graph."
  (:require [clojure.spec.alpha :as s]))

(s/def :dana.graph.core.node/node-id integer?)

(s/def :dana.graph.core.node/description string?)

(s/def ::node (s/keys :req-un [:dana.graph.core.node/node-id
                               :dana.graph.core.node/description]))

(s/def :dana.graph.core.edge/relationship-type string?)

(s/def ::edge (s/keys :req-un [:dana.graph.core.edge/relationship-type]))

(s/def ::edge-and-node (s/cat :edge ::edge :node ::node))

(s/def ::node-path (s/or :no-path (s/and vector? empty?)
                         :path (s/cat :start ::node
                                      :path-elements (s/+ ::edge-and-node))))
