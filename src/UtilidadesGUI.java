import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UtilidadesGUI {
    public static void limpiarCamposTexto(JComponent... campos) {
        for (JComponent campo : campos) {
            if (campo instanceof JTextField textField) {
                textField.setText("");
            } else if (campo instanceof JTextArea textArea) {
                textArea.setText("");
            }
        }
    }
    public static void limpiarTabla(JTable tabla) {
        if (tabla.getModel() instanceof DefaultTableModel modelo) {
            modelo.setRowCount(0);
            modelo.setColumnCount(0);
        }
    }

    public static void reemplazarCentroConTablaVacia(JPanel panel) {
        Component central = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (central != null) panel.remove(central);

        JTable vacia = new JTable(new DefaultTableModel());
        JScrollPane scroll = new JScrollPane(vacia);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(scroll, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }
}
