import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.*;
import java.util.List;

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

        btnEjecutar.addActionListener(this::ejecutarOperacion);
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

            List<Map<String, String>> resultado;

            switch (operacion) {
                case "SELECCIÓN" -> resultado = ejecutarSeleccion(conn, tabla1, condicion);
                case "PROYECCIÓN" -> resultado = ejecutarProyeccion(conn, tabla1, condicion);
                case "UNION" -> resultado = ejecutarUnion(conn, tabla1, tabla2);
                case "INTERSECCIÓN" -> resultado = ejecutarInterseccion(conn, tabla1, tabla2);
                case "DIFERENCIA" -> resultado = ejecutarDiferencia(conn, tabla1, tabla2);
                case "PRODUCTO CARTESIANO" -> resultado = ejecutarProductoCartesiano(conn, tabla1, tabla2);
                case "JOIN (natural)" -> resultado = ejecutarJoinNatural(conn, tabla1, tabla2);
                default -> resultado = null;
            }

            mostrarResultado(resultado);
        } catch (SQLException ex) {
            areaResultado.setText("Error: " + ex.getMessage());
        }
    }

    private List<Map<String, String>> obtenerFilas(Connection conn, String tabla) {
        List<Map<String, String>> filas = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabla)) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, String> tupla = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    tupla.put(meta.getColumnName(i), rs.getString(i));
                }
                filas.add(tupla);
            }
        } catch (SQLException e) {
            areaResultado.setText("Error: " + e.getMessage());
        }
        return filas;
    }

    // SELECCIÓN
    private List<Map<String, String>> ejecutarSeleccion(Connection conn, String tabla, String condicionTexto) {
        List<Map<String, String>> datos = obtenerFilas(conn, tabla);
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> tupla : datos) {
            if (condicionTexto.isEmpty() || evaluarCondicion(tupla, condicionTexto)) {
                resultado.add(tupla);
            }
        }
        return resultado;
    }

    // PROYECCIÓN
    private List<Map<String, String>> ejecutarProyeccion(Connection conn, String tabla, String atributosTexto) {
        String[] atributos = atributosTexto.split(",");
        List<Map<String, String>> datos = obtenerFilas(conn, tabla);

        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> tupla : datos) {
            Map<String, String> nuevaTupla = new LinkedHashMap<>();
            for (String atributo : atributos) {
                if (tupla.containsKey(atributo.trim())) {
                    nuevaTupla.put(atributo.trim(), tupla.get(atributo.trim()));
                }
            }
            if (!resultado.contains(nuevaTupla)) {
                resultado.add(nuevaTupla);
            }
        }
        return resultado;
    }

    // UNION
    private List<Map<String, String>> ejecutarUnion(Connection conn, String t1, String t2) {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>(R);
        for (Map<String, String> tupla : S) {
            if (!resultado.contains(tupla)) {
                resultado.add(tupla);
            }
        }
        return resultado;
    }

    // INTERSECCIÓN
    private List<Map<String, String>> ejecutarInterseccion(Connection conn, String t1, String t2) {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> tupla : R) {
            if (S.contains(tupla)) {
                resultado.add(tupla);
            }
        }
        return resultado;
    }

    // DIFERENCIA
    private List<Map<String, String>> ejecutarDiferencia(Connection conn, String t1, String t2) {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> tupla : R) {
            if (!S.contains(tupla)) {
                resultado.add(tupla);
            }
        }
        return resultado;
    }

    // PRODUCTO CARTESIANO
    private List<Map<String, String>> ejecutarProductoCartesiano(Connection conn, String t1, String t2) {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Map<String, String> t1map : R) {
            for (Map<String, String> t2map : S) {
                Map<String, String> nueva = new LinkedHashMap<>(t1map);
                for (String k : t2map.keySet()) {
                    nueva.put("t2." + k, t2map.get(k));
                }
                resultado.add(nueva);
            }
        }
        return resultado;
    }

    // JOIN NATURAL
    private List<Map<String, String>> ejecutarJoinNatural(Connection conn, String t1, String t2) {
        List<Map<String, String>> R = obtenerFilas(conn, t1);
        List<Map<String, String>> S = obtenerFilas(conn, t2);
        List<Map<String, String>> resultado = new ArrayList<>();

        if (R.isEmpty() || S.isEmpty()) return resultado;

        Set<String> comunes = new HashSet<>(R.get(0).keySet());
        comunes.retainAll(S.get(0).keySet());

        for (Map<String, String> r : R) {
            for (Map<String, String> s : S) {
                boolean coincide = true;
                for (String atr : comunes) {
                    if (!Objects.equals(r.get(atr), s.get(atr))) {
                        coincide = false;
                        break;
                    }
                }
                if (coincide) {
                    Map<String, String> nueva = new LinkedHashMap<>(r);
                    for (Map.Entry<String, String> e : s.entrySet()) {
                        if (!comunes.contains(e.getKey())) {
                            nueva.put(e.getKey(), e.getValue());
                        }
                    }
                    resultado.add(nueva);
                }
            }
        }
        return resultado;
    }

    private boolean evaluarCondicion(Map<String, String> tupla, String condicion) {
        try {
            String[] partes = condicion.split("[<>=!]+", 2);
            if (partes.length < 2) return false;
            String atributo = partes[0].trim();
            String valor = partes[1].trim().replace("'", "");
            if (!tupla.containsKey(atributo)) return false;
            String actual = tupla.get(atributo);
            if (condicion.contains("==")) return actual.equals(valor);
            if (condicion.contains("!=")) return !actual.equals(valor);
            if (condicion.contains(">=")) return Double.parseDouble(actual) >= Double.parseDouble(valor);
            if (condicion.contains("<=")) return Double.parseDouble(actual) <= Double.parseDouble(valor);
            if (condicion.contains(">")) return Double.parseDouble(actual) > Double.parseDouble(valor);
            if (condicion.contains("<")) return Double.parseDouble(actual) < Double.parseDouble(valor);
        } catch (Exception ex) {
            // Si falla, asumimos false
        }
        return false;
    }

    private void mostrarResultado(List<Map<String, String>> resultado) {
        if (resultado == null || resultado.isEmpty()) {
            areaResultado.setText("No hay resultados para la operación.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        // Encabezado
        Map<String, String> primeraFila = resultado.get(0);
        for (String col : primeraFila.keySet()) {
            sb.append(col).append("\t");
        }
        sb.append("\n");

        // Filas
        for (Map<String, String> fila : resultado) {
            for (String val : fila.values()) {
                sb.append(val).append("\t");
            }
            sb.append("\n");
        }
        areaResultado.setText(sb.toString());
    }

    // Para probar rápido
    public static void main(String[] args) {
        JFrame frame = new JFrame("Operaciones Relacionales");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new OperacionesRelacionalesPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
