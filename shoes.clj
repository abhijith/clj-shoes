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
  [x msg]
  (JOptionPane/showMessageDialog
   (JPanel.) (str x msg)))

(def running (ref true))

(defn progress-bar
  [coll f & args]
  (let [cnt (count coll)
        pb (JProgressBar. 0 cnt)
        task-agent (agent {:start 0 :end cnt :current 0 :element (first coll)})]
    (.setString pb (str (first coll)))
    (add-watch task-agent :task-agent
               (fn [k r o n]
                 (doto pb
                   (.setValue (:current n))
                   (.setString (str (:element n)))
                   (.setStringPainted true))))
    (letfn [(task-fn
             [agent-val lst task-args]
             (if (and @running
                      (not (empty? lst)))
               (do 
                 (apply f (first lst) task-args)
                 (Thread/sleep 100)
                 (send *agent* task-fn (rest lst) task-args)
                 (assoc (merge-with + agent-val {:current 1}) :element (first (rest lst))))agent-val))]
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
  (let [[pb agent] (progress-bar [:a :b :c :d :e :f :g :h] info "completed")
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
  (send data + x))

(defn progress-example-type1
  []
  (dosync (ref-set running true))
  (let [[pb agent] (progress-bar (range 0 100) progress-example-type2)
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
