(ns clj-shoes.examples
  (:use [clj-shoes.core :as shoes])
  (:use (clojure.contrib
         [swing-utils :only (add-action-listener)]
         [string :only (as-str)]))
  (:import (javax.swing JOptionPane JPanel JProgressBar)
           (java.awt Color)))

(def running (ref true))

(defn- type1-handler
  [event]
  (JOptionPane/showMessageDialog
   (JPanel.) (str "Button " (.getActionCommand event) " clicked.")))


(defn- type2-handler
  [event & args]
  (JOptionPane/showMessageDialog
   (JPanel.) (apply as-str args)))

(defn -main
  []
  (let [panel (stack
               (para "basic para")
               (flow (button "1" type1-handler)
                     (button "2" type2-handler :a :s :d :f))
               (flow (button "3") (button "4")))]
    (background panel Color/RED)
    (doto (frame panel :title "demo")
      (.pack)
      (.setVisible true)
      (.setLocationRelativeTo nil))))

(defn info
  ([x msg]
     (if @running
       (JOptionPane/showMessageDialog
        (JPanel.) (str x msg))
       {:running false}))
  ([msg]
     (if @running
       (JOptionPane/showMessageDialog
        (JPanel.) (str msg))
       {:running false})))

(defn progress-example-type1
  []
  (dosync (ref-set running true))
  (let [[pb agent] (progress-bar [:a :b :c :d :e :f :g :h] info " done")
         stop-button (button "stop")
         panel (flow pb stop-button)]
    (doto (frame panel)
      (.pack)
      (.setVisible true))
    (add-action-listener stop-button (fn [_](dosync (ref-set running false))))
    agent))

(def data (agent 0))

(defn adder
  [x]
  (send data + x)
  (if-not @running {:element "stopped" :running false} {:element "overridden"}))

(defn progress-example-type2
  []
  (dosync (ref-set running true))
  (let [[pb agent] (progress-bar (range 10 20) adder)
         stop-button (button "stop")
         panel (flow pb stop-button)]
    (doto (frame panel)
      (.pack)
      (.setVisible true))
    (add-action-listener stop-button (fn [_](dosync (ref-set running false))))
    agent))

(defn ind-fn
  [msecs]
  (Thread/sleep msecs)
  (range 0 100))

(defn progress-example-type3
  []
  (dosync (ref-set running true))
  (let [[pb agent] (indeterminate-progress-bar [ind-fn 3000] [info ""])
         stop-button (button "stop")
         panel (flow pb stop-button)]
    (doto (frame panel)
      (.pack)
      (.setVisible true))
    (add-action-listener stop-button (fn [_](dosync (ref-set running false))))
    agent))

(defn add-progress-listener-example
  []
  (dosync (ref-set running true))
  (let [pb (JProgressBar.)
        stop-button (button "stop")
        panel (flow pb stop-button)]
    (doto (frame panel)
      (.pack)
      (.setVisible true))
    (add-action-listener stop-button (fn [_](dosync (ref-set running false))))
    (add-progress-listener pb (range 10 20) info "")))
