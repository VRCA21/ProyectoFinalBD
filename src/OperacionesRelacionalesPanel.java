import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class OperacionesRelacionalesPanel extends JPanel {
    private JComboBox<String> comboTabla1;
    private JComboBox<String> comboTabla2;
    private JComboBox<String> comboOperacion;
    private JTextArea areaResultado;
    private JButton btnEjecutar;

    public OperacionesRelacionalesPanel() {
        setLayout(new BorderLayout());

        JPanel panelTop = new JPanel(new GridLayout(3, 2, 5, 5));

        comboTabla1 = new JComboBox<>();
        comboTabla2 = new JComboBox<>();
        comboOperacion = new JComboBox<>(new String[] {
                "UNION",
                "INTERSECCIÓN",
                "DIFERENCIA",
                "PRODUCTO CARTESIANO",
                "JOIN (natural)"
        });

        panelTop.add(new JLabel("Tabla 1:"));
        panelTop.add(comboTabla1);
        panelTop.add(new JLabel("Tabla 2:"));
        panelTop.add(comboTabla2);
        panelTop.add(new JLabel("Operación:"));
        panelTop.add(comboOperacion);

        btnEjecutar = new JButton("Ejecutar Operación");

        areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaResultado);

        add(panelTop, BorderLayout.NORTH);
        add(btnEjecutar, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);

        // Ajustar tamaño del scroll para que se vea mejor
        scroll.setPreferredSize(new Dimension(800, 350));

        btnEjecutar.addActionListener(e -> ejecutarOperacion());

        cargarTablas();
    }

    private void cargarTablas() {
        comboTabla1.removeAllItems();
        comboTabla2.removeAllItems();
        String bd = EstadoApp.baseDeDatosActual;
        if (bd == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una base de datos primero.");
            return;
        }
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + bd,
                ConexionBD.USUARIO, ConexionBD.CONTRASENA);
             ResultSet rs = conn.getMetaData().getTables(bd, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tabla = rs.getString("TABLE_NAME");
                comboTabla1.addItem(tabla);
                comboTabla2.addItem(tabla);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas: " + ex.getMessage());
        }
    }

    private void ejecutarOperacion() {
        String tabla1 = (String) comboTabla1.getSelectedItem();
        String tabla2 = (String) comboTabla2.getSelectedItem();
        String operacion = (String) comboOperacion.getSelectedItem();
        String bd = EstadoApp.baseDeDatosActual;

        if (tabla1 == null || tabla2 == null) {
            JOptionPane.showMessageDialog(this, "Selecciona ambas tablas.");
            return;
        }

        if (tabla1.equals(tabla2) && (operacion.equals("DIFERENCIA") || operacion.equals("UNION") || operacion.equals("INTERSECCIÓN"))) {
            // Operaciones entre la misma tabla sí son válidas, pero puede avisar o dejar pasar.
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
        // Para operaciones que requieren que las tablas tengan la misma estructura:
        // UNION, INTERSECCIÓN, DIFERENCIA: las tablas deben tener mismas columnas compatibles

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

        while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
                sb.append(rs.getString(i)).append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
