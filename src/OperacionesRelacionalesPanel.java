import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.*;
import java.util.List;

public class OperacionesRelacionalesPanel extends JPanel { private final JTextField campoTabla1; private final JTextField campoTabla2; private final JComboBox<String> comboOperacion; private final JTextField campoCondicion; private final JButton botonEjecutar; private final JTable tablaResultado; private final DefaultTableModel modeloTabla;

    private ResultadoRelacion resultadoAnterior = null;

    public OperacionesRelacionalesPanel() {
        setLayout(new BorderLayout());

        JPanel panelEntrada = new JPanel(new GridLayout(6, 2, 5, 5));
        panelEntrada.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        campoTabla1 = new JTextField();
        campoTabla2 = new JTextField();
        comboOperacion = new JComboBox<>(new String[]{
                "SELECCIÓN", "PROYECCIÓN", "UNION", "INTERSECCIÓN",
                "DIFERENCIA", "PRODUCTO CARTESIANO", "JOIN (natural)"
        });
        campoCondicion = new JTextField();
        botonEjecutar = new JButton("Ejecutar Operación");
        modeloTabla = new DefaultTableModel();
        tablaResultado = new JTable(modeloTabla);
        tablaResultado.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollResultado = new JScrollPane(tablaResultado);

        panelEntrada.add(new JLabel("Tabla 1 (o 'Resultado anterior')"));
        panelEntrada.add(campoTabla1);
        panelEntrada.add(new JLabel("Tabla 2 (si aplica)"));
        panelEntrada.add(campoTabla2);
        panelEntrada.add(new JLabel("Operación"));
        panelEntrada.add(comboOperacion);
        panelEntrada.add(new JLabel("Condición o atributos"));
        panelEntrada.add(campoCondicion);
        panelEntrada.add(new JLabel());
        panelEntrada.add(botonEjecutar);

        add(panelEntrada, BorderLayout.NORTH);
        add(scrollResultado, BorderLayout.CENTER);

        botonEjecutar.addActionListener(this::ejecutarOperacion);
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

        if (bd == null && resultadoAnterior == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una base de datos primero.");
            return;
        }

        try (Connection conn = (bd != null) ? DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/" + bd, ConexionBD.USUARIO, ConexionBD.CONTRASENA) : null) {

            ResultadoRelacion resultadoRelacion = null;

            switch (operacion) {
                case "SELECCIÓN" -> {
                    List<Map<String, String>> datos = tabla1.equalsIgnoreCase("Resultado anterior") && resultadoAnterior != null
                            ? obtenerFilasDesdeResultadoAnterior()
                            : ejecutarSeleccion(conn, tabla1, condicion);
                    List<String> columnas = (datos.isEmpty() && resultadoAnterior != null)
                            ? resultadoAnterior.columnas
                            : new ArrayList<>(datos.isEmpty() ? obtenerColumnas(conn, tabla1) : datos.get(0).keySet());
                    resultadoRelacion = new ResultadoRelacion("Seleccion_" + tabla1, datos, columnas);
                }
                case "PROYECCIÓN" -> {
                    List<Map<String, String>> datos = tabla1.equalsIgnoreCase("Resultado anterior") && resultadoAnterior != null
                            ? ejecutarProyeccionDirecta(resultadoAnterior.filas, condicion)
                            : ejecutarProyeccion(conn, tabla1, condicion);
                    List<String> columnas = Arrays.stream(condicion.split(",")).map(String::trim).toList();
                    resultadoRelacion = new ResultadoRelacion("Proyeccion_" + tabla1, datos, columnas);
                }
                case "UNION", "INTERSECCIÓN", "DIFERENCIA", "PRODUCTO CARTESIANO", "JOIN (natural)" -> {
                    List<Map<String, String>> R = tabla1.equalsIgnoreCase("Resultado anterior") && resultadoAnterior != null
                            ? resultadoAnterior.filas
                            : obtenerFilas(conn, tabla1);
                    List<Map<String, String>> S = obtenerFilas(conn, tabla2);
                    List<Map<String, String>> datos = switch (operacion) {
                        case "UNION" -> ejecutarUnion(R, S);
                        case "INTERSECCIÓN" -> ejecutarInterseccion(R, S);
                        case "DIFERENCIA" -> ejecutarDiferencia(R, S);
                        case "PRODUCTO CARTESIANO" -> ejecutarProductoCartesiano(R, S, tabla1, tabla2);
                        case "JOIN (natural)" -> ejecutarJoinNatural(R, S);
                        default -> new ArrayList<>();
                    };
                    List<String> columnas;
                    if (!datos.isEmpty()) {
                        columnas = new ArrayList<>(datos.get(0).keySet());
                    } else if (!R.isEmpty()) {
                        columnas = new ArrayList<>(R.get(0).keySet());
                    } else if (!S.isEmpty()) {
                        columnas = new ArrayList<>(S.get(0).keySet());
                    } else if (resultadoAnterior != null && resultadoAnterior.columnas != null) {
                        columnas = resultadoAnterior.columnas;
                    } else {
                        columnas = obtenerColumnas(conn, tabla1);
                    }
                    resultadoRelacion = new ResultadoRelacion(operacion + "" + tabla1 + "" + tabla2, datos, columnas);
                }
            }

            if (resultadoRelacion != null) {
                resultadoAnterior = resultadoRelacion;
                mostrarResultadoConNombre(resultadoRelacion);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo obtener resultado.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error de SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Map<String, String>> obtenerFilasDesdeResultadoAnterior() {
        return resultadoAnterior != null ? resultadoAnterior.filas : new ArrayList<>();
    }

    private List<Map<String, String>> ejecutarProyeccionDirecta(List<Map<String, String>> datos, String atributos) {
        List<Map<String, String>> resultado = new ArrayList<>();
        String[] columnas = atributos.split(",");

        for (Map<String, String> fila : datos) {
            Map<String, String> nueva = new LinkedHashMap<>();
            for (String col : columnas) {
                col = col.trim();
                if (fila.containsKey(col)) {
                    nueva.put(col, fila.get(col));
                } else {
                    nueva.put(col, "NULL");
                }
            }
            resultado.add(nueva);
        }
        return resultado;
    }

    private List<Map<String, String>> ejecutarUnion(List<Map<String, String>> R, List<Map<String, String>> S) {
        List<Map<String, String>> resultado = new ArrayList<>(R);
        for (Map<String, String> tupla : S) {
            if (!resultado.contains(tupla)) {
                resultado.add(tupla);
            }
        }
        return resultado;
    }

    private List<Map<String, String>> ejecutarInterseccion(List<Map<String, String>> R, List<Map<String, String>> S) {
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> tupla : R) {
            if (S.contains(tupla)) {
                resultado.add(tupla);
            }
        }
        return resultado;
    }

    private List<Map<String, String>> ejecutarDiferencia(List<Map<String, String>> R, List<Map<String, String>> S) {
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> tupla : R) {
            if (!S.contains(tupla)) {
                resultado.add(tupla);
            }
        }
        return resultado;
    }

    private List<Map<String, String>> ejecutarProductoCartesiano(List<Map<String, String>> R, List<Map<String, String>> S, String t1, String t2) {
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> filaR : R) {
            for (Map<String, String> filaS : S) {
                Map<String, String> nuevaFila = new LinkedHashMap<>();
                for (String colR : filaR.keySet()) {
                    nuevaFila.put(t1 + "." + colR, filaR.get(colR));
                }
                for (String colS : filaS.keySet()) {
                    nuevaFila.put(t2 + "." + colS, filaS.get(colS));
                }
                resultado.add(nuevaFila);
            }
        }
        return resultado;
    }

    private List<Map<String, String>> ejecutarJoinNatural(List<Map<String, String>> R, List<Map<String, String>> S) {
        List<Map<String, String>> resultado = new ArrayList<>();
        if (R.isEmpty() || S.isEmpty()) return resultado;

        Set<String> columnasComunes = new HashSet<>(R.get(0).keySet());
        columnasComunes.retainAll(S.get(0).keySet());

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
                    Map<String, String> nueva = new LinkedHashMap<>(filaR);
                    for (Map.Entry<String, String> entry : filaS.entrySet()) {
                        if (!columnasComunes.contains(entry.getKey())) {
                            nueva.put(entry.getKey(), entry.getValue());
                        }
                    }
                    resultado.add(nueva);
                }
            }
        }
        return resultado;
    }

    private void mostrarResultadoConNombre(ResultadoRelacion resultado) {
        DefaultTableModel modelo = new DefaultTableModel();
        for (String columna : resultado.columnas) {
            modelo.addColumn(columna);
        }

        for (Map<String, String> fila : resultado.filas) {
            Object[] datosFila = resultado.columnas.stream()
                    .map(col -> fila.getOrDefault(col, "NULL"))
                    .toArray();
            modelo.addRow(datosFila);
        }

        JTable tabla = new JTable(modelo);
        ajustarAnchoColumnas(tabla);

        JScrollPane nuevoScroll = new JScrollPane(tabla);
        nuevoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        BorderLayout layout = (BorderLayout) getLayout();
        Component componenteCentral = layout.getLayoutComponent(BorderLayout.CENTER);
        if (componenteCentral != null) {
            remove(componenteCentral);
        }

        add(nuevoScroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }




    private List<String> obtenerColumnas(Connection conn, String tabla) throws SQLException {
        List<String> columnas = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabla + " LIMIT 0")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columnas.add(meta.getColumnName(i));
            }
        }
        return columnas;
    }

    private void ajustarAnchoColumnas(JTable tabla) {
        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            TableColumn col = tabla.getColumnModel().getColumn(columna);
            int anchoMax = 50;
            for (int fila = 0; fila < tabla.getRowCount(); fila++) {
                TableCellRenderer renderer = tabla.getCellRenderer(fila, columna);
                Component comp = tabla.prepareRenderer(renderer, fila, columna);
                anchoMax = Math.max(comp.getPreferredSize().width + 10, anchoMax);
            }
            col.setPreferredWidth(anchoMax);
        }
    }


    private List<Map<String, String>> ejecutarSeleccion(Connection conn, String tabla, String condicion) throws SQLException {
        List<Map<String, String>> resultado = new ArrayList<>();
        String query = "SELECT * FROM " + tabla + (condicion.isEmpty() ? "" : " WHERE " + condicion);
        System.out.println("Ejecutando query de selección: " + query);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, String> fila = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    fila.put(meta.getColumnName(i), rs.getString(i));
                }
                resultado.add(fila);
            }
        }
        return resultado;
    }

    private List<Map<String, String>> ejecutarProyeccion(Connection conn, String tabla, String columnas) throws SQLException {
        List<Map<String, String>> resultado = new ArrayList<>();
        String query = "SELECT " + columnas + " FROM " + tabla;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, String> fila = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    fila.put(meta.getColumnName(i), rs.getString(i));
                }
                resultado.add(fila);
            }
        }
        return resultado;
    }

    private List<Map<String, String>> obtenerFilas(Connection conn, String tabla) throws SQLException {
        return ejecutarSeleccion(conn, tabla, "");
    }

    private static class ResultadoRelacion {
        String nombre;
        List<Map<String, String>> filas;
        List<String> columnas;

        ResultadoRelacion(String nombre, List<Map<String, String>> filas) {
            this(nombre, filas, filas.isEmpty() ? new ArrayList<>() : new ArrayList<>(filas.get(0).keySet()));
        }

        ResultadoRelacion(String nombre, List<Map<String, String>> filas, List<String> columnas) {
            this.nombre = nombre;
            this.filas = filas;
            this.columnas = columnas;
        }
    }

}