import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.*;
import java.util.List;

public class OperacionesRelacionalesPanel extends JPanel {
    private final JTextField campoTabla1;
    private final JTextField campoTabla2;
    private final JComboBox<String> comboOperacion;
    private final JTextField campoCondicion;
    private final JButton botonEjecutar;
    private final JTable tablaResultado;
    private final DefaultTableModel modeloTabla;
    private final JButton botonRefrescar;
    private final JCheckBox checkResultadoAnterior;
    private JLabel etiquetaTabla2;
    private JLabel etiquetaCondicion;


    private ResultadoRelacion resultadoAnterior = null;

    public OperacionesRelacionalesPanel() {
        setLayout(new BorderLayout());

        JPanel panelEntrada = new JPanel(new GridLayout(8, 2, 5, 5));
        panelEntrada.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkResultadoAnterior = new JCheckBox("Usar resultado anterior");
        campoTabla1 = new JTextField();
        campoTabla2 = new JTextField();
        JLabel etiquetaTabla2 = new JLabel("Tabla 2");
        JLabel etiquetaCondicion = new JLabel("Condición");

        comboOperacion = new JComboBox<>(new String[]{
                "SELECCIÓN", "PROYECCIÓN", "UNION", "INTERSECCIÓN",
                "DIFERENCIA", "PRODUCTO CARTESIANO", "JOIN (natural)"
        });

        campoCondicion = new JTextField();
        botonEjecutar = new JButton("Ejecutar Operación");
        botonRefrescar = new JButton("Borrar Consultas");

        modeloTabla = new DefaultTableModel();
        tablaResultado = new JTable(modeloTabla);
        tablaResultado.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollResultado = new JScrollPane(tablaResultado);

        // Inicialmente ocultar tabla2 y su etiqueta
        etiquetaTabla2.setVisible(false);
        campoTabla2.setVisible(false);

        // Estructura de entrada
        panelEntrada.add(checkResultadoAnterior);
        panelEntrada.add(new JLabel());

        panelEntrada.add(new JLabel("Tabla 1"));
        panelEntrada.add(campoTabla1);

        panelEntrada.add(etiquetaTabla2);  // ahora tenemos referencia
        panelEntrada.add(campoTabla2);

        panelEntrada.add(new JLabel("Operación"));
        panelEntrada.add(comboOperacion);

        panelEntrada.add(etiquetaCondicion);
        panelEntrada.add(campoCondicion);

        panelEntrada.add(new JLabel());
        panelEntrada.add(botonEjecutar);
        panelEntrada.add(new JLabel());
        panelEntrada.add(botonRefrescar);

        // Mostrar/ocultar campo tabla2 según operación
        comboOperacion.addActionListener(e -> {
            String operacion = (String) comboOperacion.getSelectedItem();
            boolean requiereTabla2 = operacion.equals("UNION") || operacion.equals("INTERSECCIÓN")
                    || operacion.equals("DIFERENCIA") || operacion.equals("PRODUCTO CARTESIANO")
                    || operacion.equals("JOIN (natural)");

            etiquetaTabla2.setVisible(requiereTabla2);
            campoTabla2.setVisible(requiereTabla2);
            campoTabla2.setText(""); // Limpia para evitar confusión
            boolean requiereCondicion = operacion.equals("SELECCIÓN") || operacion.equals("PROYECCIÓN");
            etiquetaCondicion.setVisible(requiereCondicion);
            campoCondicion.setVisible(requiereCondicion);
            if (requiereCondicion) {
                etiquetaCondicion.setText(operacion.equals("SELECCIÓN") ? "Condición" : "Atributos");
            }
            revalidate();  // Actualiza el layout
            repaint();
        });

        checkResultadoAnterior.addActionListener(e -> {
            boolean usarAnterior = checkResultadoAnterior.isSelected();
            campoTabla1.setEnabled(!usarAnterior);
            if (usarAnterior) {
                campoTabla1.setText("");
            }
        });

        botonRefrescar.addActionListener(e -> {
            resultadoAnterior = null;
            campoTabla1.setText("");
            campoTabla2.setText("");
            campoCondicion.setText("");
            checkResultadoAnterior.setSelected(false);
            campoTabla1.setEnabled(true);
            campoTabla2.setVisible(false);
            etiquetaTabla2.setVisible(false);

            BorderLayout layout = (BorderLayout) getLayout();
            Component central = layout.getLayoutComponent(BorderLayout.CENTER);
            if (central != null) {
                remove(central);
            }

            JTable vacia = new JTable(new DefaultTableModel());
            JScrollPane scrollVacio = new JScrollPane(vacia);
            scrollVacio.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollVacio.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            add(scrollVacio, BorderLayout.CENTER);

            revalidate();
            repaint();
        });

        add(panelEntrada, BorderLayout.NORTH);
        add(scrollResultado, BorderLayout.CENTER);

        botonEjecutar.addActionListener(this::ejecutarOperacion);
    }

    private void ejecutarOperacion(ActionEvent e) {
        String tabla1 = checkResultadoAnterior.isSelected() ? "Resultado anterior" : campoTabla1.getText().trim();
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

                    List<String> columnasR = obtenerColumnas(conn, tabla1);
                    List<String> columnasS = obtenerColumnas(conn, tabla2);
                    List<String> tiposR = ValidadorRelacional.obtenerTipos(conn, tabla1);
                    List<String> tiposS = ValidadorRelacional.obtenerTipos(conn, tabla2);

                    switch (operacion) {
                        case "UNION", "INTERSECCIÓN", "DIFERENCIA" -> {
                            if (!ValidadorRelacional.columnasCompatibles(columnasR, columnasS)) {
                                JOptionPane.showMessageDialog(this, "Las columnas deben coincidir en nombre y orden.", "Error de columnas", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            if (!ValidadorRelacional.tiposCompatibles(tiposR, tiposS)) {
                                JOptionPane.showMessageDialog(this, "Los tipos de datos deben coincidir en orden.", "Error de tipos", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                        case "JOIN (natural)" -> {
                            if (!ValidadorRelacional.hayColumnasComunes(columnasR, columnasS)) {
                                JOptionPane.showMessageDialog(this, "Para un JOIN NATURAL debe haber al menos una columna con el mismo nombre entre ambas tablas.", "Sin columnas en común", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                    }

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
                    } else {
                        switch (operacion) {
                            case "UNION", "INTERSECCIÓN", "DIFERENCIA" -> columnas = columnasR;
                            case "PRODUCTO CARTESIANO" -> {
                                columnas = new ArrayList<>();
                                for (String col : columnasR) columnas.add(tabla1 + "." + col);
                                for (String col : columnasS) columnas.add(tabla2 + "." + col);
                            }
                            case "JOIN (natural)" -> {
                                columnas = new ArrayList<>();
                                Set<String> comunes = new HashSet<>(columnasR);
                                comunes.retainAll(columnasS);
                                for (String col : columnasR) columnas.add(col);
                                for (String col : columnasS)
                                    if (!comunes.contains(col)) columnas.add(col);
                            }
                            default -> columnas = new ArrayList<>();
                        }
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
            String mensaje = UtilidadErroresSQL.traducir(ex);
            JOptionPane.showMessageDialog(this, mensaje, "Error en la operación", JOptionPane.ERROR_MESSAGE);
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