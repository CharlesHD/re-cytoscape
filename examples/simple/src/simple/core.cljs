(ns simple.core
  (:require [re-cytoscape.component :as cycomp]
            [re-cytoscape.core :as cy]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(enable-console-print!)

(rf/reg-event-db
 :add-node
 (fn [db _]
   (let [count (get db :count 0)]
     (-> db
         (update :nodes (fnil conj []) {:data {:id count
                                               :color "black"}})
         (update :count inc)))))

(rf/reg-event-db
 :change-last-node-color
 (fn [db _]
   (let [nb-nodes (-> db :nodes count)]
     (assoc-in db [:nodes (dec nb-nodes) :data :color] (rand-nth ["black" "red" "blue"
                                                                  "green" "orange"])))))
(rf/reg-event-db
 :clear-graph
 (fn [db _]
   (assoc db
          :count 0
          :nodes [])))

(rf/reg-sub
 :graph-elements
 (fn [db _]
   (:nodes db)))

(rf/reg-sub
 :nb-nodes
 (fn [db _]
   (:count db)))

(def cy-config
  {:style [{:selector "node"
            :style {:background-color "data(color)"
                    :label "data(id)"}}]
   :layout {:name "breadthfirst"
            :randomize "true"}})

(rf/reg-sub
 :graph
 :<- [:graph-elements]
 (fn [elems _]
   {:elements elems
    :key :cy
    :config cy-config
    :mount (fn [p d] (.log js/console "mounted graph")
             (cycomp/init-comp p d))
    :update (fn [p d] (.log js/console "updated graph with " (str p) " and " (str d))
              (cycomp/init-comp p d))}))

(defn ui
  []
    [:div
     [cy/graph
      {:id "cy"
       :style {:width "100%" :height "200px"
               :background-color "#eeeeff"}}
      [:graph]]
     [:button {:on-click #(rf/dispatch [:add-node])} "Add a node"]
     [:button {:on-click #(rf/dispatch [:change-last-node-color])} "Change last node color"]
     [:button {:on-click #(rf/dispatch [:clear-graph])} "Reset"]])

(defn ^:export run
  []
  (rf/clear-subscription-cache!)
  (reagent/render [ui]
                  (js/document.getElementById "app")))
