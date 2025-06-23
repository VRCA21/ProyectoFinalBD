import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ValidadorRelacional {

    public static boolean columnasCompatibles(List<String> colsA, List<String> colsB) {
        if (colsA.size() != colsB.size()) return false;

        for (int i = 0; i < colsA.size(); i++) {
            if (!colsA.get(i).equalsIgnoreCase(colsB.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean tiposCompatibles(List<String> tiposA, List<String> tiposB) {
        if (tiposA.size() != tiposB.size()) return false;

        for (int i = 0; i < tiposA.size(); i++) {
            if (!tiposA.get(i).equalsIgnoreCase(tiposB.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static List<String> obtenerTipos(Connection conn, String tabla) throws SQLException {
        List<String> tipos = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabla + " LIMIT 0")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                tipos.add(meta.getColumnTypeName(i));
            }
        }
        return tipos;
    }

    public static boolean hayColumnasComunes(List<String> columnasA, List<String> columnasB) {
        for (String col : columnasA) {
            if (columnasB.contains(col)) return true;
        }
        return false;
    }
}

