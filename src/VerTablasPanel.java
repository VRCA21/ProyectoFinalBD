import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class VerTablasPanel extends JPanel {
    public VerTablasPanel() {
        setLayout(new BorderLayout());
        DefaultListModel<String> modelo = new DefaultListModel<>();
        JList<String> listaTablas = new JList<>(modelo);
        add(new JScrollPane(listaTablas), BorderLayout.CENTER);

        JButton btnActualizar = new JButton("Actualizar Listado");
        add(btnActualizar, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> {
            modelo.clear();
            String bd = EstadoApp.baseDeDatosActual;
            if (bd == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una BD primero.");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + bd, ConexionBD.USUARIO, ConexionBD.CONTRASENA)) {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet rs = meta.getTables(bd, null, "%", new String[]{"TABLE"});
                while (rs.next()) {
                    modelo.addElement(rs.getString("TABLE_NAME"));
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al obtener tablas: " + ex.getMessage());
            }
        });
    }
}
