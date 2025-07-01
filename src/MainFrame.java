import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel panelPrincipal;

    public MainFrame() {
        setTitle("Proyecto Álgebra Relacional");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);


        cardLayout = new CardLayout();
        panelPrincipal = new JPanel(cardLayout);

        panelPrincipal.add(new SeleccionarBaseDeDatosPanel(), "SeleccionarBD");
        panelPrincipal.add(new VerTablasPanel(), "VerTablas");
        panelPrincipal.add(new CrearBaseDeDatosPanel(), "CrearBD");
        panelPrincipal.add(new OperacionesRelacionalesPanel(), "Operaciones");
        panelPrincipal.add(new CrearTablaPanel(), "CrearTabla");

        JPanel menu = new JPanel(new GridLayout(0, 1));
        JButton btnSel = new JButton("Seleccionar BD");
        JButton btnVer = new JButton("Ver Tablas");
        JButton btnCrear = new JButton("Crear BD");
        JButton btnCrearTabla = new JButton("Crear Tabla");
        JButton btnOperar = new JButton("Operaciones");

        btnSel.addActionListener(e -> cardLayout.show(panelPrincipal, "SeleccionarBD"));
        btnVer.addActionListener(e -> cardLayout.show(panelPrincipal, "VerTablas"));
        btnCrear.addActionListener(e -> cardLayout.show(panelPrincipal, "CrearBD"));
        btnCrearTabla.addActionListener(e -> cardLayout.show(panelPrincipal, "CrearTabla"));
        btnOperar.addActionListener(e -> cardLayout.show(panelPrincipal, "Operaciones"));

        menu.add(btnSel);
        menu.add(btnVer);
        menu.add(btnCrear);
        menu.add(btnCrearTabla);
        menu.add(btnOperar);

        add(menu, BorderLayout.WEST);
        add(panelPrincipal, BorderLayout.CENTER);
    }
}