import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class OperacionesRelacionalesPanel extends JPanel {

    private JTextField campoTabla1;
    private JTextField campoTabla2;
    private JTextField campoCondicion;
    private JComboBox<String> comboOperacion;
    private JTextArea areaResultado;
    private JButton botonEjecutar;

    public OperacionesRelacionalesPanel() {
        setLayout(new BorderLayout());

        // Panel de entrada
        JPanel panelEntrada = new JPanel(new GridLayout(4, 2, 5, 5));
        panelEntrada.add(new JLabel("Tabla 1:"));
        campoTabla1 = new JTextField();
        panelEntrada.add(campoTabla1);

        panelEntrada.add(new JLabel("Tabla 2:"));
        campoTabla2 = new JTextField();
        panelEntrada.add(campoTabla2);

        panelEntrada.add(new JLabel("Condición / Atributos (según operación):"));
        campoCondicion = new JTextField();
        panelEntrada.add(campoCondicion);

        panelEntrada.add(new JLabel("Operación:"));
        comboOperacion = new JComboBox<>(new String[]{
                "SELECCIÓN", "PROYECCIÓN", "UNION", "INTERSECCIÓN",
                "DIFERENCIA", "PRODUCTO CARTESIANO", "JOIN (natural)"
        });
        panelEntrada.add(comboOperacion);

        add(panelEntrada, BorderLayout.NORTH);

        // Área de resultado
        areaResultado = new JTextArea(15, 50);
        areaResultado.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaResultado);
        add(scroll, BorderLayout.CENTER);

        // Botón ejecutar
        botonEjecutar = new JButton("Ejecutar");
        add(botonEjecutar, BorderLayout.SOUTH);

        botonEjecutar.addActionListener(this::ejecutarOperacion);
    }

    // Clase para retornar nombre de la relación y datos juntos
    private static class ResultadoRelacion {
        String nombreRelacion;
        List<Map<String, String>> filas;

        ResultadoRelacion(String nombreRelacion, List<Map<String, String>> filas) {
            this.nombreRelacion = nombreRelacion;
            this.filas = filas;
        }
    }

    private void ejecutarOperacion(ActionEvent e) {
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
                ConexionBD.USUARIO, ConexionBD.CONTRASENA)) {

            ResultadoRelacion resultadoRelacion = null;

            switch (operacion) {
                case "SELECCIÓN" -> resultadoRelacion = new ResultadoRelacion(
                        "Seleccion_" + tabla1, ejecutarSeleccion(conn, tabla1, condicion));
                case "PROYECCIÓN" -> resultadoRelacion = new ResultadoRelacion(
                        "Proyeccion_" + tabla1, ejecutarProyeccion(conn, tabla1, condicion));
                case "UNION" -> resultadoRelacion = ejecutarUnion(conn, tabla1, tabla2);
                case "INTERSECCIÓN" -> resultadoRelacion = ejecutarInterseccion(conn, tabla1, tabla2);
                case "DIFERENCIA" -> resultadoRelacion = ejecutarDiferencia(conn, tabla1, tabla2);
                case "PRODUCTO CARTESIANO" -> resultadoRelacion = ejecutarProductoCartesiano(conn, tabla1, tabla2);
                case "JOIN (natural)" -> resultadoRelacion = ejecutarJoinNatural(conn, tabla1, tabla2);
            }

            if (resultadoRelacion != null) {
                mostrarResultadoConNombre(resultadoRelacion);
            } else {
                areaResultado.setText("No se pudo obtener resultado.");
            }
        } catch (SQLException ex) {
            areaResultado.setText("Error: " + ex.getMessage());
        }
    }

    private void mostrarResultadoConNombre(ResultadoRelacion resultadoRelacion) {
        List<Map<String, String>> resultado = resultadoRelacion.filas;
        StringBuilder sb = new StringBuilder();

        sb.append("Relación resultado: ").append(resultadoRelacion.nombreRelacion).append("\n\n");

        if (resultado == null || resultado.isEmpty()) {
            sb.append("(No hay resultados para la operación)");
            areaResultado.setText(sb.toString());
            return;
        }

        Map<String, String> primeraFila = resultado.get(0);
        for (String col : primeraFila.keySet()) {
            sb.append(col).append("\t");
        }
        sb.append("\n");

        for (Map<String, String> fila : resultado) {
            for (String val : fila.values()) {
                sb.append(val).append("\t");
            }
            sb.append("\n");
        }
        areaResultado.setText(sb.toString());
    }


    // Métodos para obtener filas desde BD y ejecutar operaciones

    private List<Map<String, String>> obtenerFilas(Connection conn, String tabla) throws SQLException {
        List<Map<String, String>> filas = new ArrayList<>();
        String sql = "SELECT * FROM " + tabla;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int columnas = md.getColumnCount();
            while (rs.next()) {
                Map<String, String> fila = new LinkedHashMap<>();
                for (int i = 1; i <= columnas; i++) {
                    fila.put(md.getColumnName(i), rs.getString(i));
                }
                filas.add(fila);
            }
        }
        return filas;
    }

    private List<Map<String, String>> ejecutarSeleccion(Connection conn, String tabla, String condicion) throws SQLException {
        List<Map<String, String>> resultado = new ArrayList<>();
        String sql = "SELECT * FROM " + tabla + " WHERE " + condicion;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int columnas = md.getColumnCount();
            while (rs.next()) {
                Map<String, String> fila = new LinkedHashMap<>();
                for (int i = 1; i <= columnas; i++) {
                    fila.put(md.getColumnName(i), rs.getString(i));
                }
                resultado.add(fila);
            }
        }
        return resultado;
    }

    private List<Map<String, String>> ejecutarProyeccion(Connection conn, String tabla, String atributos) throws SQLException {
        List<Map<String, String>> resultado = new ArrayList<>();
        String sql = "SELECT " + atributos + " FROM " + tabla;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int columnas = md.getColumnCount();
            while (rs.next()) {
                Map<String, String> fila = new LinkedHashMap<>();
                for (int i = 1; i <= columnas; i++) {
                    fila.put(md.getColumnName(i), rs.getString(i));
                }
                resultado.add(fila);
            }
        }
        return resultado;
    }

    private ResultadoRelacion ejecutarUnion(Connection conn, String t1, String t2) throws SQLException {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>(R);
        for (Map<String, String> tupla : S) {
            if (!resultado.contains(tupla)) {
                resultado.add(tupla);
            }
        }
        return new ResultadoRelacion("Union_" + t1 + "_" + t2, resultado);
    }

    private ResultadoRelacion ejecutarInterseccion(Connection conn, String t1, String t2) throws SQLException {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> tupla : R) {
            if (S.contains(tupla)) {
                resultado.add(tupla);
            }
        }
        return new ResultadoRelacion("Interseccion_" + t1 + "_" + t2, resultado);
    }

    private ResultadoRelacion ejecutarDiferencia(Connection conn, String t1, String t2) throws SQLException {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> tupla : R) {
            if (!S.contains(tupla)) {
                resultado.add(tupla);
            }
        }
        return new ResultadoRelacion("Diferencia_" + t1 + "_" + t2, resultado);
    }

    private ResultadoRelacion ejecutarProductoCartesiano(Connection conn, String t1, String t2) throws SQLException {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>();

        for (Map<String, String> filaR : R) {
            for (Map<String, String> filaS : S) {
                Map<String, String> nuevaFila = new LinkedHashMap<>();
                // Agregar columnas de la primera tabla con prefijo para evitar confusión
                for (String colR : filaR.keySet()) {
                    nuevaFila.put(t1 + "." + colR, filaR.get(colR));
                }
                // Agregar columnas de la segunda tabla con prefijo
                for (String colS : filaS.keySet()) {
                    nuevaFila.put(t2 + "." + colS, filaS.get(colS));
                }
                resultado.add(nuevaFila);
            }
        }
        return new ResultadoRelacion("ProductoCartesiano_" + t1 + "_" + t2, resultado);
    }

    private ResultadoRelacion ejecutarJoinNatural(Connection conn, String t1, String t2) throws SQLException {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>();

        if (R.isEmpty() || S.isEmpty()) {
            return new ResultadoRelacion("JoinNatural_" + t1 + "_" + t2, resultado);
        }

        Set<String> columnasR = R.get(0).keySet();
        Set<String> columnasS = S.get(0).keySet();

        // Obtener columnas comunes para join natural
        Set<String> columnasComunes = new HashSet<>(columnasR);
        columnasComunes.retainAll(columnasS);

        for (Map<String, String> filaR : R) {
            for (Map<String, String> filaS : S) {
                boolean coinciden = true;
                for (String col : columnasComunes) {
                    if (!Objects.equals(filaR.get(col), filaS.get(col))) {
                        coinciden = false;
                        break;
                    }
                }
                if (coinciden) {
                    Map<String, String> nuevaFila = new LinkedHashMap<>();
                    // Agregar todas las columnas de R
                    nuevaFila.putAll(filaR);
                    // Agregar columnas de S que no estén en comunes para no repetir
                    for (String col : columnasS) {
                        if (!columnasComunes.contains(col)) {
                            nuevaFila.put(col, filaS.get(col));
                        }
                    }
                    resultado.add(nuevaFila);
                }
            }
        }

        return new ResultadoRelacion("JoinNatural_" + t1 + "_" + t2, resultado);
    }
}
