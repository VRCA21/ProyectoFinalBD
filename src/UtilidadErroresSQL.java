import java.sql.SQLException;

public class UtilidadErroresSQL {
    public static String traducir(SQLException ex) {
        String mensajeOriginal = ex.getMessage().toLowerCase();

        if (mensajeOriginal.contains("unknown column")) {
            return "Uno o más nombres de columnas no existen. Revisa que los nombres de los atributos estén escritos correctamente.";
        } else if (mensajeOriginal.contains("unknown table")) {
            return "El nombre de la tabla no existe. Asegúrate de escribir correctamente el nombre de la tabla.";
        } else if (mensajeOriginal.contains("syntax")) {
            return "Por favor ingrese un nombre";
        } else if (mensajeOriginal.contains("doesn't exist")) {
            return "La tabla o base de datos especificada no existe.";
        } else if (mensajeOriginal.contains("access denied")) {
            return "No se pudo conectar con la base de datos. Verifica usuario y contraseña.";
        } else if (mensajeOriginal.contains("duplicate")) {
            return "Ya existe un dato igual en la tabla. No se permiten duplicados.";
        } else if (mensajeOriginal.contains("data truncated")) {
            return "Uno de los valores es demasiado largo para el campo correspondiente.";
        } else if (mensajeOriginal.contains("cannot be null")) {
            return "Un campo obligatorio está vacío. Completa todos los datos requeridos.";
        } else {
            return "Ocurrió un error inesperado: " + ex.getMessage();
        }
    }
}
