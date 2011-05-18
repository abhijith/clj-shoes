(ns clj-shoes.core
  (:import (java.awt FlowLayout Container Dimension Component))
  (:import (javax.swing Box BoxLayout JButton JFrame JLabel JPanel JOptionPane JDialog JFileChooser JProgressBar))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter MouseListener KeyEvent))
  (:import (java.awt Toolkit BorderLayout Dimension Color Dialog$ModalityType))
  (:use (clojure.contrib
         [swing-utils :only (add-action-listener)])))

(defn flow
  [& args]
  (let [ panel (JPanel.)
        layout (BoxLayout. panel BoxLayout/X_AXIS)]
    (doto panel
      (.setComponentOrientation java.awt.ComponentOrientation/RIGHT_TO_LEFT)
      (.setLayout layout))
    (doseq [x args]
      (.add panel x)
      (.add panel (Box/createRigidArea (Dimension. 5 0))))
    panel))

(defn stack
  [& args]
  (let [ panel (JPanel.)
        layout (BoxLayout. panel BoxLayout/Y_AXIS) ]
    (doto panel
      (.setLayout layout))
    (doseq [x  args]
      (.add panel x)
      (.add panel (Box/createRigidArea (Dimension. 0 5))))
    panel))

(defn para
  [txt]
  (JLabel. txt))

(defn background
  [component color]
  (.setBackground component color))

(defn button
  ([label f & args]
     (let [btn (JButton. label)]
       (apply add-action-listener btn f args)
       btn))
  ([label]
     (JButton. label)))

(defn frame
  ([panel & [{:keys [title] :or {title "woah!"}}]]
     (doto (JFrame. title)
       (.add panel))))

(defn add-progress-listener
  [pb coll f & args]
  (let [cnt (count coll)
        task-agent (agent {:start 0 :end cnt :current 0 :element (first coll) :running true :post true})]
    (doto pb
      (.setMaximum cnt)
      (.setIndeterminate false)
      (.setString (str (first coll)))
      (.setStringPainted true))
    (add-watch task-agent :task-agent
               (fn [k r o n]
                 (doto pb
                   (.setValue (:current n))
                   (.setString (str (:element n))))))
    (letfn [(task-fn
             [agent-val lst & task-args]
             (if (and (:running agent-val)
                      (not (empty? lst)))
               (do
                 (let [{:keys [current element running] :or {current 1 running true element (first (rest lst))}}
                       (apply f agent-val (first lst) task-args)]
                   (Thread/sleep 1000)
                   (apply send *agent* task-fn (rest lst) task-args)
                   (assoc (merge-with + agent-val {:current current}) :element element :running running)))
               (if (:post agent-val) (apply f agent-val :nil task-args) agent-val)))]
      (apply send task-agent task-fn coll args))
    [pb task-agent]))

;; initializer for progress bar? this would probably be required to support (fn [args] ...   {:element "something"})
;; otherwise the first element would be treated specially - for example
;; (defn info [x msg] (JOptionPane/showMessageDialog (JPanel.) (str x msg)) {:element ""})
;; would still display the first element since the progress bar has no idea that I don't want to display anything

;; use protocols or generic functions?
(defn progress-bar
  [coll f & args]
  (let [cnt (count coll)
        pb (JProgressBar.)]
    (apply add-progress-listener pb coll f args)))

;; can currently only apply a fn if something is chosen. What if I want to run something if cancelled?
;; Approach/theme for the library probably should be given a better thought.
(defn ask-open-dir
  [parent f & args]
  (let [chooser (JFileChooser.)]
    (.setFileSelectionMode chooser JFileChooser/DIRECTORIES_ONLY)
    (let [ ret (.showOpenDialog chooser parent)]
      (cond
       (= JFileChooser/APPROVE_OPTION ret) (apply f chooser args)
       (= JFileChooser/CANCEL_OPTION ret) nil
       :else :error))))

(defn indeterminate-progress-bar
  [pb [indeterminate-fn & indeterminate-args] [determinate-fn & determinate-args]]
  (let [indeterminate-agent (agent nil)]
    (doto pb
      (.setIndeterminate true)
      (.setString "wah")
      (.setStringPainted true))
    (send indeterminate-agent (fn [a args] (apply indeterminate-fn args)) indeterminate-args)
    (add-watch indeterminate-agent :indeterminate-agent
               (fn [k r o n]
                 (if (coll? n)
                   (add-progress-listener pb n determinate-fn determinate-args)
                   (doto pb
                     (.setString "wah")))))
    [pb indeterminate-agent]))
