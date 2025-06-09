import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class VerTablasPanel extends JPanel {
    private DefaultListModel<String> modelo;
    private JList<String> listaTablas;

    public VerTablasPanel() {
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

        try (Connection conn = DriverManager.getConnection(ConexionBD.URL + bd, ConexionBD.USUARIO, ConexionBD.CONTRASENA)) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(bd, null, "%", new String[]{"TABLE"});

            boolean hayTablas = false;
            while (rs.next()) {
                modelo.addElement(rs.getString("TABLE_NAME"));
                hayTablas = true;
            }

            if (!hayTablas) {
                JOptionPane.showMessageDialog(this, "No hay tablas en la base de datos seleccionada.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener tablas: " + ex.getMessage());
        }
    }

    public String getTablaSeleccionada() {
        return listaTablas.getSelectedValue();
    }
}
