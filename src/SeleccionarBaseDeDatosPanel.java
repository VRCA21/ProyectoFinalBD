import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SeleccionarBaseDeDatosPanel extends JPanel {
    private JComboBox<String> comboBD;
    private JButton btnSeleccionar;
    private JButton btnActualizar;

    public SeleccionarBaseDeDatosPanel() {
        setLayout(new BorderLayout());

        // Título
        JLabel titulo = new JLabel("Seleccionar Base de Datos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        // Panel central
        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));
        panelCentro.setPreferredSize(new Dimension(500, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel labelBD = new JLabel("Base de Datos:");
        labelBD.setFont(new Font("SansSerif", Font.PLAIN, 18));
        panelCentro.add(labelBD, gbc);

        gbc.gridy++;
        comboBD = new JComboBox<>();
        comboBD.setFont(new Font("SansSerif", Font.PLAIN, 18));
        comboBD.setPreferredSize(new Dimension(300, 30));
        panelCentro.add(comboBD, gbc);

        gbc.gridy++;
        btnSeleccionar = new JButton("Seleccionar");
        btnSeleccionar.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnSeleccionar.setPreferredSize(new Dimension(200, 40));
        panelCentro.add(btnSeleccionar, gbc);

        gbc.gridy++;
        btnActualizar = new JButton("Actualizar Lista");
        btnActualizar.setFont(new Font("SansSerif", Font.PLAIN, 16));
        btnActualizar.setPreferredSize(new Dimension(200, 35));
        panelCentro.add(btnActualizar, gbc);

        add(panelCentro, BorderLayout.CENTER);

        // Eventos
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
        } catch (SQLException ex) {
            String mensaje = UtilidadErroresSQL.traducir(ex);
            JOptionPane.showMessageDialog(this, mensaje, "Error en la operación", JOptionPane.ERROR_MESSAGE);
        }
    }
}
