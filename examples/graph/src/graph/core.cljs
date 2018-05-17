(ns graph.core
  (:require [clojure.string :as str]
            [re-cytoscape.component :as cycomp]
            [re-cytoscape.core :as cy]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

;; This app show a simple text to graph renderer.
;; Text specification of a graph should be as follow :
;; on each line you can have either :
;; + a node name (without the minus char '-')
;; + a link between two node like 'a - b', link is encoded using the minus '-' char.

(enable-console-print!)
;; events
(rf/reg-event-db
 :set-graph-text
 (fn [db [_ txt]]
   (assoc db :text txt)))

;; graph helper
(defn parse-node
  [a]
  (if (empty? a)
    []
    [{:data {:id a}}]))

(defn parse-link
  [a b]
  (concat (parse-node a) (parse-node b)
          (if (or (empty? a) (empty? b))
            []
            [{:data {:id (str a "-" b) :source a :target b}}])))

(defn parse-line
  [line]
  (let [splt (map str/trim (str/split line #"-"))]
    (if (= (count splt) 2)
      (parse-link (first splt) (last splt))
      (parse-node (first splt)))))

(defn text->graph
  [txt]
  (remove
   nil?
   (mapcat parse-line (str/split txt #"\n"))))

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

(rf/reg-sub
 :graph-config
 :<- [:graph-elements]
 (fn [elems _]
   {:elements elems
    :config {:style [{:selector "node"
                      :style {:label "data(id)"}}]
             :layout {:name "cose"}}
    :key :textgraph}))

;; Visualisation
(defn graph-text []
  [:textarea {:rows 20 :cols 70
              :style {:width "500px" :height "300px"}
              :on-change #(rf/dispatch [:set-graph-text (.-value (.-target %))])
              :value (or @(rf/subscribe [:graph-text]) "enter something")}])

(defn graph []
  [cy/graph {:id "textgraph" :style {:width "500px"
                                     :height "300px"
                                     :background-color  "#eeeeff"}}
   [:graph-config]])

(defn ui
  []
  [:div {:style {:width "100%" :height "600px"}}
   [graph-text]
   [graph]])

(defn ^:export run
  []
  (rf/clear-subscription-cache!)
  ;; (rf/dispatch [:initialize])
  (reagent/render [ui]
                  (js/document.getElementById "app")))
