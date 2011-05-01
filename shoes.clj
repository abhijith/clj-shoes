(ns shoes
  (:import (java.awt FlowLayout Container Dimension Component))
  (:import (javax.swing Box JButton JFrame JLabel JPanel JTextField JOptionPane JScrollPane JList ImageIcon JComboBox JSeparator JTable UIManager SwingUtilities AbstractButton JFileChooser JDialog JProgressBar JTabbedPane BoxLayout))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter MouseListener KeyEvent))
  (:import (java.awt Toolkit BorderLayout Dimension Color Dialog$ModalityType))
  (:use (clojure.contrib
         [string :only (as-str)]
         [swing-utils :only (add-action-listener)])))

(defn flow
  [& args]
  (let [ panel (JPanel.)
        layout (BoxLayout. panel BoxLayout/X_AXIS)]
    (doto panel
      (.setComponentOrientation java.awt.ComponentOrientation/RIGHT_TO_LEFT)
      (.setLayout layout))
    (doseq [x  args]
      (.add panel x))
    (.add panel (Box/createHorizontalGlue))
    panel))

(defn stack
  [& args]
  (let [ panel (JPanel.)
        layout (BoxLayout. panel BoxLayout/Y_AXIS) ]
    (doto panel
      (.setLayout layout))
    (doseq [x  args]
      (.add panel x))
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

(defn- type1-handler
  [event]
  (JOptionPane/showMessageDialog
   (JPanel.) (str "Button " (.getActionCommand event) " clicked.")))

(defn- type2-handler
  [event & args]
  (JOptionPane/showMessageDialog
   (JPanel.) (apply as-str args)))

(defn frame
  ([panel & [{:keys [title] :or {title "woah!"}}]]
     (doto (JFrame. title)
       (.add panel))))

(defn info
  ([x msg]
  (JOptionPane/showMessageDialog
   (JPanel.) (str x msg)))
  ([msg]
     (JOptionPane/showMessageDialog
      (JPanel.) (str msg))))

(def running (ref true))

;; initializer for progress bar? this would probably be required to support (fn [args] ...   {:element "something"})
;; otherwise the first element would be treated specially - for example
;; (defn info [x msg] (JOptionPane/showMessageDialog (JPanel.) (str x msg)) {:element ""})
;; would still display the first element since the progress bar has no idea that I don't want to display anything

(defn progress-bar
  [coll f & args]
  (let [cnt (count coll)
        pb (JProgressBar. 0 cnt)
        task-agent (agent {:start 0 :end cnt :current 0 :element (first coll) :determinate true})]
    (doto pb
      (.setString (str (first coll)))
      (.setStringPainted true))
    (add-watch task-agent :task-agent
               (fn [k r o n]
                 (doto pb
                   (.setValue (:current n))
                   (.setString (str (:element n))))))
    (letfn [(task-fn
             [agent-val lst task-args]
             (if (and @running
                      (not (empty? lst)))
               (do
                 (let [{:keys [current element] :or {current 1 element (first (rest lst))}}
                       (apply f (first lst) task-args)]
                   (Thread/sleep 1000)
                   (send *agent* task-fn (rest lst) task-args)
                   (assoc (merge-with + agent-val {:current current}) :element element))) agent-val))]
      (send task-agent task-fn coll args))
    [pb task-agent]))

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
  {})

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

;; use protocols or generic functions?
(defn progress-bar
  [coll f & args]
  (let [cnt (count coll)
        pb (JProgressBar. 0 cnt)
        task-agent (agent {:start 0 :end cnt :current 0 :element (first coll)})]
    (doto pb
      (.setString (str (first coll)))
      (.setStringPainted true))
    (add-watch task-agent :task-agent
               (fn [k r o n]
                 (doto pb
                   (.setValue (:current n))
                   (.setString (str (:element n))))))
    (letfn [(task-fn
             [agent-val lst task-args]
             (if (and @running
                      (not (empty? lst)))
               (do
                 (let [{:keys [current element] :or {current 1 element (first (rest lst))}}
                       (apply f (first lst) task-args)]
                   (Thread/sleep 1000)
                   (send *agent* task-fn (rest lst) task-args)
                   (assoc (merge-with + agent-val {:current current}) :element element))) agent-val))]
      (send task-agent task-fn coll args))
    [pb task-agent]))

(defn progress-bar
  [pb coll f & args]
  (let [cnt (count coll)
        task-agent (agent {:start 0 :end cnt :current 0 :element (first coll)})]
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
             [agent-val lst task-args]
             (if (and @running
                      (not (empty? lst)))
               (do
                 (let [{:keys [current element] :or {current 1 element (first (rest lst))}}
                       (apply f (first lst) task-args)]
                   (Thread/sleep 1000)
                   (send *agent* task-fn (rest lst) task-args)
                   (assoc (merge-with + agent-val {:current current}) :element element))) agent-val))]
      (send task-agent task-fn coll args))
    [pb task-agent]))

(defn indeterminate-progress-bar
  [[indeterminate-fn & indeterminate-args] [determinate-fn & determinate-args]]
  (let [pb (JProgressBar.)
        indeterminate-agent (agent nil)]
    (doto pb
      (.setIndeterminate true)
      (.setString "wah")
      (.setStringPainted true))
    (send indeterminate-agent (fn [a args] (apply indeterminate-fn args)) indeterminate-args)
    (add-watch indeterminate-agent :indeterminate-agent
               (fn [k r o n]
                 (if (coll? n)
                   (progress-bar pb n determinate-fn determinate-args)
                   (doto pb
                     (.setString "wah")))))
    [pb indeterminate-agent]))


(defn ind-fn
  [msecs]
  (Thread/sleep msecs)
  (range 0 100))

(defn type3
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
