import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SeleccionarBaseDeDatosPanel extends JPanel {
    public SeleccionarBaseDeDatosPanel() {
        setLayout(new BorderLayout());
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> lista = new JList<>(model);
        JButton btnSeleccionar = new JButton("Seleccionar BD");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", ConexionBD.USUARIO, ConexionBD.CONTRASENA);
             ResultSet rs = conn.getMetaData().getCatalogs()) {
            while (rs.next()) {
                model.addElement(rs.getString(1));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error cargando bases: " + ex.getMessage());
        }

        btnSeleccionar.addActionListener(e -> {
            EstadoApp.baseDeDatosActual = lista.getSelectedValue();
            JOptionPane.showMessageDialog(this, "BD seleccionada: " + EstadoApp.baseDeDatosActual);
        });

        add(new JScrollPane(lista), BorderLayout.CENTER);
        add(btnSeleccionar, BorderLayout.SOUTH);
    }
}
