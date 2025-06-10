import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class OperacionesRelacionalesPanel extends JPanel {
    private JTextField campoTabla1;
    private JTextField campoTabla2;
    private JComboBox<String> comboOperacion;
    private JTextField campoCondicion;
    private JTextArea areaResultado;
    private JButton btnEjecutar;

    public OperacionesRelacionalesPanel() {
        setLayout(new BorderLayout());

        JPanel panelTop = new JPanel(new GridLayout(4, 2, 5, 5));

        campoTabla1 = new JTextField();
        campoTabla2 = new JTextField();
        comboOperacion = new JComboBox<>(new String[]{
                "SELECCIÓN",
                "PROYECCIÓN",
                "UNION",
                "INTERSECCIÓN",
                "DIFERENCIA",
                "PRODUCTO CARTESIANO",
                "JOIN (natural)"
        });
        campoCondicion = new JTextField();

        panelTop.add(new JLabel("Tabla 1:"));
        panelTop.add(campoTabla1);
        panelTop.add(new JLabel("Tabla 2:"));
        panelTop.add(campoTabla2);
        panelTop.add(new JLabel("Operación:"));
        panelTop.add(comboOperacion);
        panelTop.add(new JLabel("Condición / Atributos:"));
        panelTop.add(campoCondicion);

        btnEjecutar = new JButton("Ejecutar Operación");

        areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaResultado);
        scroll.setPreferredSize(new Dimension(800, 350));

        add(panelTop, BorderLayout.NORTH);
        add(btnEjecutar, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        btnEjecutar.addActionListener(e -> ejecutarOperacion());
    }

    private void ejecutarOperacion() {
        String tabla1 = campoTabla1.getText().trim();
        String tabla2 = campoTabla2.getText().trim();
        String operacion = (String) comboOperacion.getSelectedItem();
        String condicion = campoCondicion.getText().trim();
        String bd = EstadoApp.baseDeDatosActual;

        if (tabla1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes ingresar el nombre de la Tabla 1.");
            return;
        }

        if ((operacion.equals("UNION") || operacion.equals("INTERSECCIÓN") ||
                operacion.equals("DIFERENCIA") || operacion.equals("PRODUCTO CARTESIANO") ||
                operacion.equals("JOIN (natural)")) && tabla2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes ingresar el nombre de la Tabla 2 para esta operación.");
            return;
        }

        if (bd == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una base de datos primero.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + bd,
                ConexionBD.USUARIO, ConexionBD.CONTRASENA);
             Statement st = conn.createStatement()) {

            String consultaSQL = generarConsulta(tabla1, tabla2, operacion, condicion, conn);

            if (consultaSQL == null) {
                areaResultado.setText("Operación no soportada o error en la generación de consulta.");
                return;
            }

            EstadoApp.ultimaOperacion = consultaSQL;

            ResultSet rs = st.executeQuery(consultaSQL);
            String resultadoTexto = convertirResultSetATexto(rs);
            areaResultado.setText(resultadoTexto);

        } catch (SQLException ex) {
            areaResultado.setText("Error SQL: " + ex.getMessage());
        }
    }

    private String generarConsulta(String t1, String t2, String op, String condicion, Connection conn) throws SQLException {
        switch (op) {
            case "SELECCIÓN":
                if (condicion.isEmpty()) {
                    return "SELECT * FROM " + t1;
                }
                return "SELECT * FROM " + t1 + " WHERE " + condicion;

            case "PROYECCIÓN":
                if (condicion.isEmpty()) {
                    return "SELECT * FROM " + t1;
                }
                return "SELECT " + condicion + " FROM " + t1;

            case "UNION":
                if (!tienenMismasColumnas(conn, t1, t2)) return null;
                return "SELECT * FROM " + t1 + " UNION SELECT * FROM " + t2;

            case "INTERSECCIÓN":
                if (!tienenMismasColumnas(conn, t1, t2)) return null;
                return "SELECT * FROM " + t1 + " INTERSECT SELECT * FROM " + t2;

            case "DIFERENCIA":
                if (!tienenMismasColumnas(conn, t1, t2)) return null;
                return "SELECT * FROM " + t1 + " EXCEPT SELECT * FROM " + t2;

            case "PRODUCTO CARTESIANO":
                return "SELECT * FROM " + t1 + ", " + t2;

            case "JOIN (natural)":
                return "SELECT * FROM " + t1 + " NATURAL JOIN " + t2;

            default:
                return null;
        }
    }

    private boolean tienenMismasColumnas(Connection conn, String t1, String t2) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs1 = meta.getColumns(null, null, t1, null);
        ResultSet rs2 = meta.getColumns(null, null, t2, null);

        StringBuilder cols1 = new StringBuilder();
        StringBuilder cols2 = new StringBuilder();

        while (rs1.next()) cols1.append(rs1.getString("COLUMN_NAME")).append(",");
        while (rs2.next()) cols2.append(rs2.getString("COLUMN_NAME")).append(",");

        return cols1.toString().equalsIgnoreCase(cols2.toString());
    }

    private String convertirResultSetATexto(ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        for (int i = 1; i <= cols; i++) {
            sb.append(meta.getColumnName(i)).append("\t");
        }
        sb.append("\n");

        while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
                sb.append(rs.getString(i)).append("\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
