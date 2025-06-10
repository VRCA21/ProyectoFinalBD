import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class VerTablasPanel extends JPanel {
    private DefaultListModel<String> modelo;
    private JList<String> listaTablas;

    public VerTablasPanel() {
        System.out.println("Base de datos en VerTablasPanel: " + EstadoApp.baseDeDatosActual);
        setLayout(new BorderLayout());

        modelo = new DefaultListModel<>();
        listaTablas = new JList<>(modelo);
        JScrollPane scrollPane = new JScrollPane(listaTablas);
        JButton btnActualizar = new JButton("Actualizar");

        btnActualizar.addActionListener(e -> cargarTablas());

        add(scrollPane, BorderLayout.CENTER);
        add(btnActualizar, BorderLayout.SOUTH);
    }

    private void cargarTablas() {
        modelo.clear();
        String bd = EstadoApp.baseDeDatosActual;

        if (bd == null || bd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona una base de datos primero.");
            return;
        }

        try {
            Vector<String> tablas = UtilidadesBD.obtenerNombresTablas(bd);
            if (tablas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay tablas en la base de datos seleccionada.");
            } else {
                for (String tabla : tablas) {
                    modelo.addElement(tabla);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener tablas: " + ex.getMessage());
        }
    }


    public String getTablaSeleccionada() {
        return listaTablas.getSelectedValue();
    }
}
