import java.sql.*;
import java.util.Vector;

public class UtilidadesBD {

    public static Vector<String> obtenerNombresTablas(String nombreBD) throws SQLException {
        Vector<String> tablas = new Vector<>();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/" + nombreBD,
                ConexionBD.USUARIO,
                ConexionBD.CONTRASENA)) {

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(nombreBD, null, "%", new String[]{"TABLE"});

            while (rs.next()) {
                String nombre = rs.getString("TABLE_NAME");
                tablas.add(nombre);
            }

            rs.close();
        }

        System.out.println("Tablas encontradas:");
        for (String t : tablas) {
            System.out.println("- " + t);
        }


        return tablas;
    }
}
