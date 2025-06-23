import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CrearBaseDeDatosPanel extends JPanel {
    public CrearBaseDeDatosPanel() {
        setLayout(new BorderLayout());

        // Título
        JLabel titulo = new JLabel("Crear Nueva Base de Datos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        // Panel central con GridBagLayout
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60)); // margen más grande
        panelFormulario.setPreferredSize(new Dimension(500, 250)); // tamaño visual mayor

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel labelNombre = new JLabel("Nombre de la base de datos:");
        labelNombre.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JTextField campoBD = new JTextField(20);
        campoBD.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JButton btnCrear = new JButton("Crear Base de Datos");
        btnCrear.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnCrear.setPreferredSize(new Dimension(200, 40));

        panelFormulario.add(labelNombre, gbc);

        gbc.gridy++;
        panelFormulario.add(campoBD, gbc);

        gbc.gridy++;
        panelFormulario.add(btnCrear, gbc);

        add(panelFormulario, BorderLayout.CENTER);

        // Acción del botón
        btnCrear.addActionListener(e -> {
            String nombreBD = campoBD.getText().trim();
            if (nombreBD.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa un nombre para la base de datos.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/",
                    ConexionBD.USUARIO,
                    ConexionBD.CONTRASENA);
                 Statement st = conn.createStatement()) {

                st.executeUpdate("CREATE DATABASE " + nombreBD);
                JOptionPane.showMessageDialog(this, "Base de datos creada exitosamente.");
                UtilidadesGUI.limpiarCamposTexto(campoBD);

            } catch (SQLException ex) {
                String mensaje = UtilidadErroresSQL.traducir(ex);
                JOptionPane.showMessageDialog(this, mensaje, "Error en la operación", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
