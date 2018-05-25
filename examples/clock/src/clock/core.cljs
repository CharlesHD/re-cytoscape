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
   {:time (js/Date.)}))    ;; so the application state will initially be a map with two keys


(rf/reg-event-db                 ;; usage:  (dispatch [:timer a-js-Date])
 :timer                         ;; every second an event of this kind will be dispatched
 (fn [db [_ new-time]]          ;; note how the 2nd parameter is destructured to obtain the data value
   (assoc db :time new-time)))  ;; compute and return the new application state

(cy/reg-event-cy
 :graph-change-elements
 (fn [cys [_ k elements]]
   (doto (get cys k)
     (.remove "*")
     (.add (clj->js elements)))))

;; -- Domino 4 - Query  -------------------------------------------------------
(defn update-comp
  [_ data]
  (let [graph (:elements data)]
    (rf/dispatch [:graph-change-elements (:key data) graph])))

(rf/reg-sub
  :time
  (fn [db _]     ;; db is current app state. 2nd unused param is query vector
    (:time db))) ;; return a query computation over the application state

(rf/reg-sub
 :time-color
 :<- [:hours]
 :<- [:minutes]
 :<- [:seconds]
 (fn [[h m s] _]
   [(int (* h (/ 255 24))) (int (* m (/ 255 60))) (int (* s (/ 255 60)))]))

(rf/reg-sub
 :seconds
 :<- [:time]
 (fn [time _]
   (.getSeconds time)))

(rf/reg-sub
 :minutes
 :<- [:time]
 (fn [time _]
   (.getMinutes time)))

(rf/reg-sub
 :hours
 :<- [:time]
 (fn [time _]
   (.getHours time)))

(def node-size 30.0)

(defn nodes-range
  [[start color] _]
  (for [i (range start)]
    {:data {:id i :color color}
     :position {:x (* node-size (mod i 10)) :y (* node-size (int (/ i 10)))}}))

(def base-conf
  {:style [{:selector "node"
            :style {:background-color "data(color)"
                    :width node-size
                    :height node-size}}]
   :zoom 1
   :pan {:x (* 0.5 node-size) :y (* 0.5 node-size)}
   :layout {:name "preset"
            :fit false}})

(rf/reg-sub
 :second-graph/elements
 :<- [:seconds]
 :<- [:time-color]
 nodes-range)

(rf/reg-sub
 :minute-graph/elements
 :<- [:minutes]
 :<- [:time-color]
 nodes-range)

(rf/reg-sub
 :hour-graph/elements
 :<- [:hours]
 :<- [:time-color]
 nodes-range)

(rf/reg-sub
 :second-graph
 :<- [:second-graph/elements]
 (fn [elems _]
   {:elements elems
    :config base-conf
    :key :cy-seconds}))

(rf/reg-sub
 :minute-graph
 :<- [:minute-graph/elements]
 (fn [elems _]
   {:elements elems
    :config (assoc-in base-conf [:style 0 :style :shape] "vee")
    :key :cy-minutes}))

(rf/reg-sub
 :hour-graph
 :<- [:hour-graph/elements]
 (fn [elems _]
   {:elements elems
    :config (assoc-in base-conf [:style 0 :style :shape] "star")
    :key :cy-hours}))
;; -- Domino 5 - View Functions ----------------------------------------------

(defn second-clock
  []
  [:div
   [cy/graph {:id "cy-seconds" :style {:width (* 10 node-size) :height (* 6 node-size)
                                       :background-color "#000000"}}
    [:second-graph]]])

(defn minute-clock
  []
  [cy/graph {:id "cy-minutes" :style {:width (* 10 node-size) :height (* 6 node-size)
                                      :background-color "#000000"}}
   [:minute-graph]])

(defn hour-clock
  []
  [cy/graph {:id "cy-hours" :style {:width (* 10 node-size) :height (* 3 node-size)
                                    :background-color "#000000"}}
   [:hour-graph]])

(defn ui
  []
  [:div
   [hour-clock]
   [minute-clock]
   [second-clock]])

(defn ^:export run
  []
  (rf/clear-subscription-cache!)
  (rf/dispatch [:initialize])
  (reagent/render [ui]
                  (js/document.getElementById "app")))
