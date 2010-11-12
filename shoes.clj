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
      (.add (JButton. "tu"))
      (.setComponentOrientation java.awt.ComponentOrientation/RIGHT_TO_LEFT)
      (.setLayout layout))
    (doseq [x  args]
      (.add panel x)
      )
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

(defn main
  []
  (let [ frame (JFrame. "border example")
        panel (stack
               (flow (JButton. "wah") (JButton. "weh"))
               (flow (JButton. "wah") (JButton. "weh")))              
               ]
    (doto frame
      (.setLocation 300 180)
      ;(.setPreferredSize (Dimension. 800 600))
      (.add panel)
      (.pack)
      (.setVisible true))))
