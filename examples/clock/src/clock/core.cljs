(ns clock.core
(:require [re-cytoscape.component :as cycomp]
            [re-cytoscape.core :as cy]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(enable-console-print!)

;; -- Domino 1 - Event Dispatch -----------------------------------------------

(defn dispatch-timer-event
  []
  (let [now (js/Date.)]
    (rf/dispatch [:timer now])))  ;; <-- dispatch used

;; Call the dispatching function every second.
;; `defonce` is like `def` but it ensures only one instance is ever
;; created in the face of figwheel hot-reloading of this file.
(defonce do-timer (js/setInterval dispatch-timer-event 1000))


;; -- Domino 2 - Event Handlers -----------------------------------------------

(rf/reg-event-db              ;; sets up initial application state
  :initialize                 ;; usage:  (dispatch [:initialize])
  (fn [_ _]                   ;; the two parameters are not important here, so use _
    {:time (js/Date.)         ;; What it returns becomes the new application state
     :time-color "#f88"}))    ;; so the application state will initially be a map with two keys


(rf/reg-event-db                ;; usage:  (dispatch [:time-color-change 34562])
  :time-color-change            ;; dispatched when the user enters a new colour into the UI text field
  (fn [db [_ new-color-value]]  ;; -db event handlers given 2 parameters:  current application state and event (a vector)
    (assoc db :time-color new-color-value)))   ;; compute and return the new application state


(rf/reg-event-db                 ;; usage:  (dispatch [:timer a-js-Date])
  :timer                         ;; every second an event of this kind will be dispatched
  (fn [db [_ new-time]]          ;; note how the 2nd parameter is destructured to obtain the data value
    (assoc db :time new-time)))  ;; compute and return the new application state


;; -- Domino 4 - Query  -------------------------------------------------------

(rf/reg-sub
  :time
  (fn [db _]     ;; db is current app state. 2nd unused param is query vector
    (:time db))) ;; return a query computation over the application state

(rf/reg-sub
  :time-color
  (fn [db _]
    (:time-color db)))

(rf/reg-sub
 :seconds
 :<- [:time]
 (fn [time _]
   (.getSeconds time)))

(rf/reg-sub
 :second-graph
 :<- [:seconds]
 :<- [:time-color]
 (fn [[seconds color] _]
   {:elements (for [i (range 1 (inc seconds))]
                {:data {:id i :color color}
                 :position {:x (* 10 (mod i 10)) :y (* 10 (/ i 10))}})
    :config {:style [{:selector "node"
                      :style {:background-color "data(color)"}}]
             :layout {:name "preset"}}
    :key :cy-seconds}))

;; -- Domino 5 - View Functions ----------------------------------------------

(defn second-clock
  []
  [cy/graph {:id "cy-seconds" :style {:width "33%" :height "200px"
                                      :background-color "#eeeeff"}}
   [:second-graph]])

(defn ui
  []
    [:div
     [second-clock]])


(defn ^:export run
  []
  (rf/clear-subscription-cache!)
  (reagent/render [ui]
                  (js/document.getElementById "app")))
