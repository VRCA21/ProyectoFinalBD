import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CrearBaseDeDatosPanel extends JPanel {
    public CrearBaseDeDatosPanel() {
        setLayout(new BorderLayout());
        JTextField campoBD = new JTextField();
        JButton btnCrear = new JButton("Crear BD");

        add(new JLabel("Nombre de la nueva BD:"), BorderLayout.NORTH);
        add(campoBD, BorderLayout.CENTER);
        add(btnCrear, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", ConexionBD.USUARIO, ConexionBD.CONTRASENA);
                 Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE DATABASE " + campoBD.getText());
                JOptionPane.showMessageDialog(this, "Base de datos creada exitosamente.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al crear BD: " + ex.getMessage());
            }
        });
    }
}
