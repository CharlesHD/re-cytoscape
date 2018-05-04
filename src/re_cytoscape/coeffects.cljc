(ns re-cytoscape.coeffects
  (:require [re-frame.core :as r]
            [re-cytoscape.db :refer [store]]))

(r/reg-cofx
 :cytoscape
 (fn [coeffects _]
   (assoc coeffects :cytoscape @store)))
