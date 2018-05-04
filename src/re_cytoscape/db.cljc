(ns re-cytoscape.db)

(def store (atom nil))

(defn set-cytoscape!
  [k cyobject]
  (swap! store assoc k cyobject))
