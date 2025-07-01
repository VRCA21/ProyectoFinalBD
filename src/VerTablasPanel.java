import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class VerTablasPanel extends JPanel {
    private DefaultListModel<String> modelo;
    private JList<String> listaTablas;
    private JTable tablaEstructura;
    private DefaultTableModel modeloEstructura;

    public VerTablasPanel() {
        setLayout(new BorderLayout());

        modelo = new DefaultListModel<>();
        listaTablas = new JList<>(modelo);
        JScrollPane scrollLista = new JScrollPane(listaTablas);

        modeloEstructura = new DefaultTableModel();
        tablaEstructura = new JTable(modeloEstructura);
        JScrollPane scrollEstructura = new JScrollPane(tablaEstructura);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarTablas());
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.add(new JLabel("Tablas:"), BorderLayout.NORTH);
        panelIzquierdo.add(scrollLista, BorderLayout.CENTER);
        panelIzquierdo.add(btnActualizar, BorderLayout.SOUTH);

        add(panelIzquierdo, BorderLayout.WEST);
        add(scrollEstructura, BorderLayout.CENTER);

        listaTablas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String tabla = listaTablas.getSelectedValue();
                if (tabla != null) {
                    cargarEstructuraTabla(tabla);
                }
            }
        });
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
            String mensaje = UtilidadErroresSQL.traducir(ex);
            JOptionPane.showMessageDialog(this, mensaje, "Error en la operación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarEstructuraTabla(String tabla) {
        String bd = EstadoApp.baseDeDatosActual;
        if (bd == null) return;

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/" + bd,
                ConexionBD.USUARIO, ConexionBD.CONTRASENA);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("DESCRIBE " + tabla)) {

            modeloEstructura.setColumnIdentifiers(new String[]{
                    "Campo", "Tipo", "Nulo", "Clave", "Predeterminado", "Extra"
            });
            modeloEstructura.setRowCount(0);

            while (rs.next()) {
                modeloEstructura.addRow(new Object[]{
                        rs.getString("Field"),
                        rs.getString("Type"),
                        rs.getString("Null"),
                        rs.getString("Key"),
                        rs.getString("Default"),
                        rs.getString("Extra")
                });
            }

        } catch (SQLException ex) {
            String mensaje = UtilidadErroresSQL.traducir(ex);
            JOptionPane.showMessageDialog(this, mensaje, "Error al cargar estructura", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getTablaSeleccionada() {
        return listaTablas.getSelectedValue();
    }
}
