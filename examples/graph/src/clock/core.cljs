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
(defn parse-line
  [line]
  (let [splt (map str/trim (str/split line #"--"))]
    (if (= (count splt) 2)
      {:data {:id line :source (first splt) :target (last splt)}}
      {:data {:id (first splt)}})))

(defn text->graph
  [txt]
  (for [x (str/split txt #"\n")]
    (parse-line x)))

;; subscription
(rf/reg-sub
 :graph-text
 (fn [db _]
   (:text db)))

(rf/reg-sub
 :graph-elements
 :<- [:graph-text]
 (fn [txt _]
   (text->graph txt)))

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
