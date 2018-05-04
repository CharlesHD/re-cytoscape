(ns re-cytoscape.component
  (:require [cljsjs.cytoscape]
            [reagent.core :as r]
            [re-cytoscape.db :as db]))

(defn graph-inner [props]
  (r/create-class
   {:reagent-render
    (fn [props]
      [:div (dissoc props :graph :mount :update :events)])
    :component-did-mount (:mount props)
    :component-did-update (:update props)
    :display-name "graph-inner"}))

(defn init-comp
  [comp]
  (when-let
      [{:keys [id key config graph]} (r/props comp)]
    (let [config (-> config
                     (assoc :container (.getElementById js/document id)
                            :elements graph))
          cy (js/cytoscape (clj->js config))]
      (db/set-cytoscape! key cy))))

(defn graph [props]
    (fn []
      [graph-inner (merge props
                          {:graph @graph
                           :mount init-comp
                           :update init-comp})]))
