(ns re-cytoscape.effects
  (:require [re-frame.core :as r]
            [re-cytoscape.db :refer [store]]))

(r/reg-fx
 :cytoscape
 (fn [id handler]
   (when-let [cy (get @store id)]
     (handler cy))))
