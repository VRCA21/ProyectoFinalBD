import javax.swing.*;
import java.awt.*;

public class PantallaInicio extends JFrame {

    public PantallaInicio() {
        setTitle("Bienvenido");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);
        JLabel titulo = new JLabel("<html><div style='text-align: center;'>Proyecto Final<br>'Calculadora de Álgebra Relacional'</div></html>", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 34));
        panel.add(titulo, BorderLayout.NORTH);
        JPanel centro = new JPanel(new GridLayout(2, 1, 20, 20));
        centro.setBackground(Color.WHITE);
        JLabel integrantes = new JLabel("<html><div style='text-align: center;'>Elaborado por:<br>Ramos Sánchez Jared<br>Rosas Lezama Carlos<br>Valencia Reséndiz Carlos Alfonso</div></html>", SwingConstants.CENTER);
        integrantes.setFont(new Font("SansSerif", Font.PLAIN, 26));
        centro.add(integrantes);
        JLabel profesora = new JLabel("<html><div style='text-align: center;'>Profesora:<br>Nancy Ocotitla Rojas</div></html>", SwingConstants.CENTER);
        profesora.setFont(new Font("SansSerif", Font.PLAIN, 26));
        centro.add(profesora);
        panel.add(centro, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        botones.setBackground(Color.WHITE);
        JButton btnEntrar = new JButton("Entrar");
        JButton btnSalir = new JButton("Salir");
        btnEntrar.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnSalir.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btnEntrar.setPreferredSize(new Dimension(140, 40));
        btnSalir.setPreferredSize(new Dimension(140, 40));
        btnEntrar.addActionListener(e -> {
            dispose(); // Cierra la pantalla de inicio
            new MainFrame().setVisible(true); // Abre la app principal
        });
        btnSalir.addActionListener(e -> System.exit(0));
        botones.add(btnEntrar);
        botones.add(btnSalir);
        panel.add(botones, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PantallaInicio::new);
    }
}
