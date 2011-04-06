(ns shoes
  (:import (java.awt FlowLayout Container Dimension Component))
  (:import (javax.swing Box JButton JFrame JLabel JPanel JTextField JOptionPane JScrollPane JList ImageIcon JComboBox JSeparator JTable UIManager SwingUtilities AbstractButton JFileChooser JDialog JProgressBar JTabbedPane BoxLayout))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter MouseListener KeyEvent))
  (:import (java.awt Toolkit BorderLayout Dimension Color Dialog$ModalityType)))

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

(defn button
  [label]
  (JButton. label))

(defn -main
  []
  (let [frame (JFrame. "shoes!")
        panel (stack
               (para "basic para")
               (flow (button "1") (button "2"))
               (flow (button "3") (button "4")))
               ]
    (doto frame
      (.setLocation 300 180)
      (.add panel)
      (.pack)
      (.setVisible true))))
