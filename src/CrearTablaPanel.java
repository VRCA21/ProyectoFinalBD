import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CrearTablaPanel extends JPanel {

    public CrearTablaPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));
        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 15, 15));
        Font fontCampos = new Font("SansSerif", Font.PLAIN, 18);
        Font fontEtiquetas = new Font("SansSerif", Font.BOLD, 18);
        Font fontBoton = new Font("SansSerif", Font.BOLD, 18);

        JLabel lblNombre = new JLabel("Nombre de la Tabla:");
        lblNombre.setFont(fontEtiquetas);
        JTextField campoNombre = new JTextField();
        campoNombre.setFont(fontCampos);

        JLabel lblDef = new JLabel("Definición de Atributos:");
        lblDef.setFont(fontEtiquetas);
        JTextField campoDef = new JTextField();
        campoDef.setFont(fontCampos);

        JButton btnCrear = new JButton("Crear Tabla");
        btnCrear.setFont(fontBoton);
        btnCrear.setPreferredSize(new Dimension(160, 40));

        panelFormulario.add(lblNombre);
        panelFormulario.add(campoNombre);
        panelFormulario.add(lblDef);
        panelFormulario.add(campoDef);
        panelFormulario.add(new JLabel());
        panelFormulario.add(btnCrear);

        add(panelFormulario, BorderLayout.NORTH);

        JTextArea infoTipos = new JTextArea("""
Tipos de atributos permitidos:

• INT — Números enteros (ej. edad INT)
• VARCHAR(n) — Texto de longitud n (ej. nombre VARCHAR(50))
• DATE — Fechas (ej. nacimiento DATE)
• DOUBLE — Decimales (ej. precio DOUBLE)
• BOOLEAN — Verdadero/falso (ej. activo BOOLEAN)
• TEXT — Texto largo
""");
        infoTipos.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoTipos.setMargin(new Insets(8, 12, 8, 12));
        infoTipos.setEditable(false);
        infoTipos.setLineWrap(true);
        infoTipos.setWrapStyleWord(true);

        JScrollPane scrollTipos = new JScrollPane(infoTipos);
        scrollTipos.setBorder(BorderFactory.createTitledBorder("Guía rápida de tipos de datos"));
        scrollTipos.setPreferredSize(new Dimension(700, 180));
        scrollTipos.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollTipos, BorderLayout.CENTER);
        btnCrear.addActionListener(e -> {
            String bd = EstadoApp.baseDeDatosActual;
            if (bd == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una base de datos primero.");
                return;
            }

            String nombreTabla = campoNombre.getText().trim();
            String definicion = campoDef.getText().trim();

            if (nombreTabla.isEmpty() || definicion.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debes completar ambos campos.");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + bd, ConexionBD.USUARIO, ConexionBD.CONTRASENA);
                 PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + nombreTabla + " (" + definicion + ")")) {
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Tabla creada exitosamente.");
                UtilidadesGUI.limpiarCamposTexto(campoNombre, campoDef);
            } catch (SQLException ex) {
                String mensaje = UtilidadErroresSQL.traducir(ex);
                JOptionPane.showMessageDialog(this, mensaje, "Error en la operación", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
