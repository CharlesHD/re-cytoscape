(ns clock.core
(:require [re-cytoscape.component :as cycomp
            [re-cytoscape.core :as cy]
            [re-frame.core :as rf]
            [reagent.core :as reagent]])

(enable-console-print!)
;; events
(rf/reg-event-db
 :set-graph-text
 (fn [db [_ txt]]
   (assoc db :text txt)))

;; graph helper

;; subscription
(rf/reg-sub
 :graph-text
 (fn [db _]
   (:text db)))

;; Visualisation
(defn graph-text []
  [:text-area {:rows 8 :cols 50
               :on-change #(rf/dispatch [:set-graph-text (.-value (.-target %))])
               :value @(rf/subscribe [:graph-text])}])
(defn graph [])

(defn ui
  []
  [:div
   [graph-text]
   [graph]])

(defn ^:export run
  []
  (rf/clear-subscription-cache!)
  (rf/dispatch [:initialize])
  (reagent/render [ui]
                  (js/document.getElementById "app")))
