import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SeleccionarBaseDeDatosPanel extends JPanel {
    private JComboBox<String> comboBD;
    private JButton btnSeleccionar;
    private JButton btnActualizar;

    public SeleccionarBaseDeDatosPanel() {
        setLayout(new FlowLayout());

        comboBD = new JComboBox<>();
        btnSeleccionar = new JButton("Seleccionar");
        btnActualizar = new JButton("Actualizar");

        add(new JLabel("Base de Datos:"));
        add(comboBD);
        add(btnSeleccionar);
        add(btnActualizar);

        cargarBasesDeDatos();

        btnSeleccionar.addActionListener(e -> {
            String seleccion = (String) comboBD.getSelectedItem();
            if (seleccion != null && !seleccion.isEmpty()) {
                EstadoApp.baseDeDatosActual = seleccion;
                JOptionPane.showMessageDialog(this, "Base seleccionada: " + seleccion);
            }
        });

        btnActualizar.addActionListener(e -> cargarBasesDeDatos());
    }

    private void cargarBasesDeDatos() {
        comboBD.removeAllItems();
        try (Connection conn = DriverManager.getConnection(ConexionBD.URL, ConexionBD.USUARIO, ConexionBD.CONTRASENA);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {

            while (rs.next()) {
                comboBD.addItem(rs.getString(1));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar bases: " + e.getMessage());
        }
    }
}
