(ns re-cytoscape.component
  (:require [cljsjs.cytoscape]
            [reagent.core :as r]
            [re-cytoscape.db :as db]
            [re-frame.core :as rf]))

(defn graph-inner [props data]
  (let [mnt (:mount data)
        upd (:update data)]
    (r/create-class
     {:reagent-render
      (fn [props _]
        [:div props])
      :component-did-mount (fn [comp] (mnt (r/props comp) (first (r/children comp))))
      :component-did-update (fn [comp] (upd (r/props comp) (first (r/children comp))))
      :display-name "graph-inner"})))

(defn init-comp
  [props data]
  (when-let
      [{:keys [key config elements]} data]
    (let [config (-> config
                     (assoc :container (.getElementById js/document (:id props))
                            :elements elements))
          cy (js/cytoscape (clj->js config))]
      (db/set-cytoscape! key cy)
      cy)))

(defn graph [props graph-sub-v]
  (let [sub (rf/subscribe graph-sub-v)]
    (fn []
      [graph-inner
       props
       (merge {:elements []
               :key key
               :config {}
               :mount init-comp
               :update init-comp}
              @sub)])))
