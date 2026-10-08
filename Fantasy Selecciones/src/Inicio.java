import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Pantalla 1: el usuario escribe el nombre de su equipo. */
public class Inicio extends JFrame {

    private final JTextField campoNombre = new JTextField(16);
    private final JLabel aviso = new JLabel(" ");

    public Inicio() {
        setTitle("Fantasy Selecciones");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 520);
        setResizable(false);
        // hgap grande: cada componente ocupa su propia línea
        setLayout(new FlowLayout(FlowLayout.CENTER, 400, 14));
        getContentPane().setBackground(new Color(20, 40, 90));

        JLabel titulo = new JLabel("FANTASY SELECCIONES");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(Color.WHITE);

        JLabel pedir = new JLabel("Nombre de tu equipo:");
        pedir.setForeground(Color.WHITE);

        aviso.setForeground(new Color(255, 120, 120));

        JButton empezar = new JButton("Empezar");
        empezar.setPreferredSize(new Dimension(140, 36));
        empezar.addActionListener(e -> empezar());
        campoNombre.addActionListener(e -> empezar());

        add(crearLogo(200));
        add(titulo);
        add(pedir);
        add(campoNombre);
        add(aviso);
        add(empezar);

        setLocationRelativeTo(null);
    }

    /**
     * Carga /imagenes/app/logo.jpg del proyecto y lo escala al alto indicado (el logo
     * es vertical).
     */
    private JLabel crearLogo(int alto) {
        URL url = getClass().getResource("/imagenes/app/logo.jpg");
        if (url == null) {
            JLabel sinLogo = new JLabel("(logo no encontrado)");
            sinLogo.setForeground(Color.LIGHT_GRAY);
            sinLogo.setPreferredSize(new Dimension(alto, alto));
            sinLogo.setHorizontalAlignment(SwingConstants.CENTER);
            return sinLogo;
        }
        ImageIcon original = new ImageIcon(url);
        int ancho = alto * original.getIconWidth() / original.getIconHeight();
        Image escalada = original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(escalada));
    }

    private void empezar() {
        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            aviso.setText("Escribe un nombre para continuar");
            return;
        }
        Manager manager = new Manager(nombre);
        new Tienda(manager).setVisible(true); // le pasamos el manager a la otra pantalla
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Inicio().setVisible(true));
    }
}
