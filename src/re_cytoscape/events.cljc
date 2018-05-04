(ns re-cytoscape.events
  (:require [re-frame.core :as r]))

(defn reg-event-cy
  ([event-name interceptors handler]
   (r/reg-event-fx
    event-name
    (into [(r/inject-cofx :cytoscape)] interceptors)
    (fn [{:keys [cytoscape]} signal]
      {:cytoscape (handler cytoscape signal)})))
  ([event-name handler]
   (reg-event-cy event-name [] handler)))
