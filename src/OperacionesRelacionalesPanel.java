import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class OperacionesRelacionalesPanel extends JPanel {
    private JTextField campoTabla1;
    private JTextField campoTabla2;
    private JComboBox<String> comboOperacion;
    private JTextArea areaResultado;
    private JButton btnEjecutar;

    public OperacionesRelacionalesPanel() {
        setLayout(new BorderLayout());

        JPanel panelTop = new JPanel(new GridLayout(3, 2, 5, 5));

        campoTabla1 = new JTextField();
        campoTabla2 = new JTextField();
        comboOperacion = new JComboBox<>(new String[] {
                "UNION",
                "INTERSECCIÓN",
                "DIFERENCIA",
                "PRODUCTO CARTESIANO",
                "JOIN (natural)"
        });

        panelTop.add(new JLabel("Tabla 1:"));
        panelTop.add(campoTabla1);
        panelTop.add(new JLabel("Tabla 2:"));
        panelTop.add(campoTabla2);
        panelTop.add(new JLabel("Operación:"));
        panelTop.add(comboOperacion);

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
        String bd = EstadoApp.baseDeDatosActual;

        if (tabla1.isEmpty() || tabla2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa los nombres de ambas tablas.");
            return;
        }

        if (bd == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una base de datos primero.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + bd,
                ConexionBD.USUARIO, ConexionBD.CONTRASENA);
             Statement st = conn.createStatement()) {

            String consultaSQL = generarConsulta(tabla1, tabla2, operacion);

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

    private String generarConsulta(String t1, String t2, String op) throws SQLException {
        switch (op) {
            case "UNION":
                if (!tienenMismasColumnas(t1, t2))
                    return null;
                return "SELECT * FROM " + t1 + " UNION SELECT * FROM " + t2;

            case "INTERSECCIÓN":
                if (!tienenMismasColumnas(t1, t2))
                    return null;
                return "SELECT * FROM " + t1 + " INTERSECT SELECT * FROM " + t2;

            case "DIFERENCIA":
                if (!tienenMismasColumnas(t1, t2))
                    return null;
                return "SELECT * FROM " + t1 + " EXCEPT SELECT * FROM " + t2;

            case "PRODUCTO CARTESIANO":
                return "SELECT * FROM " + t1 + ", " + t2;

            case "JOIN (natural)":
                return "SELECT * FROM " + t1 + " NATURAL JOIN " + t2;

            default:
                return null;
        }
    }

    private boolean tienenMismasColumnas(String t1, String t2) throws SQLException {
        String bd = EstadoApp.baseDeDatosActual;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + bd,
                ConexionBD.USUARIO, ConexionBD.CONTRASENA)) {

            Vector<String> colsT1 = obtenerColumnas(conn, bd, t1);
            Vector<String> colsT2 = obtenerColumnas(conn, bd, t2);

            if (colsT1.size() != colsT2.size())
                return false;

            for (int i = 0; i < colsT1.size(); i++) {
                if (!colsT1.get(i).equalsIgnoreCase(colsT2.get(i)))
                    return false;
            }
            return true;
        }
    }

    private Vector<String> obtenerColumnas(Connection conn, String bd, String tabla) throws SQLException {
        Vector<String> columnas = new Vector<>();
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getColumns(bd, null, tabla, null);
        while (rs.next()) {
            columnas.add(rs.getString("COLUMN_NAME"));
        }
        rs.close();
        return columnas;
    }

    private String convertirResultSetATexto(ResultSet rs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        // Encabezados
        for (int i = 1; i <= cols; i++) {
            sb.append(meta.getColumnName(i)).append("\t");
        }
        sb.append("\n");

        // Filas
        while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
                sb.append(rs.getString(i)).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
