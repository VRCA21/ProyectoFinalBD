import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CrearTablaPanel extends JPanel {
    public CrearTablaPanel() {
        setLayout(new GridLayout(5, 2));
        JTextField campoNombre = new JTextField();
        JTextField campoDef = new JTextField();
        JButton btnCrear = new JButton("Crear Tabla");

        add(new JLabel("Nombre Tabla:"));
        add(campoNombre);
        add(new JLabel("Definición (ej: id INT, nombre VARCHAR(50)):"));
        add(campoDef);
        add(new JLabel());
        add(btnCrear);

        btnCrear.addActionListener(e -> {
            String bd = EstadoApp.baseDeDatosActual;
            if (bd == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una BD primero.");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + bd, ConexionBD.USUARIO, ConexionBD.CONTRASENA);
                 PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + campoNombre.getText() + " (" + campoDef.getText() + ")")) {
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Tabla creada exitosamente.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al crear tabla: " + ex.getMessage());
            }
        });
    }
}
