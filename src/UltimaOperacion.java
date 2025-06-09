import javax.swing.*;
import java.awt.*;

public class UltimaOperacion extends JPanel {
    private static JLabel lblOperacion;

    public UltimaOperacion() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        lblOperacion = new JLabel("Última operación: Ninguna");
        add(lblOperacion);
    }

    public static void actualizarOperacion(String operacion) {
        EstadoApp.ultimaOperacion = operacion;
        if (lblOperacion != null) {
            lblOperacion.setText("Última operación: " + operacion);
        }
    }
}
