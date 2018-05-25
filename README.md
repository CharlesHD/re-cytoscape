# re-cytoscape

A bridge for using [cytoscape.js](http://js.cytoscape.org/) in [re-frame](https://github.com/Day8/re-frame/).

## Warning
This project is still in developpement. API and stuff may change.

## Insert graphs in a re-frame app

``` clojure
(ns my-graph
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [re-cytoscape.core :as cy]))

(rf/reg-sub
  :graph-nodes
  (fn [_ _]
    [{:data {:id "0"}}
     {:data {:id "1"}}
     {:data {:id "2"}}
     {:data {:id "3"}}]))

(rf/reg-sub
  :graph-links
  (fn [_ _]
    [{:data {:id "0->1" :source "0" :target "1"}}
     {:data {:id "1->2" :source "1" :target "2"}}
     {:data {:id "2->3" :source "2" :target "3"}}
     {:data {:id "3->0" :source "3" :target "0"}}]))

(rf/reg-sub
  :graph-config
  (fn [_ _]
    {:style [{:selector "node"
              :style {:background-color "black"
                      :label "data(id)"}}]
     :layout {:name "breadthfirst"}}))

(rf/reg-sub
  :graph-data
  :<- [:graph-nodes]
  :<- [:graph-links]
  :<- [:graph-config]
  (fn [[nodes edges config] _]
    {:elements (concat nodes edges)
     :key :cy
     :config config}))

(reagent/render [cy/graph {:id "cy"
                           :style {:width "100px" :height "200px"
                                   :background-color "#eeeeff"}}
                  [:graph-data]] ;; pass the subscription vector to cy/graph
                (.getElementById js/document "app"))
```

## License

Copyright © 2018 Charles Huyghues-Despointes

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
