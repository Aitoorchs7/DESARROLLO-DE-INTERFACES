import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Pantalla 3: alineación (1-4-3-3) sobre el campo. */
public class Plantilla extends JFrame {

    // De arriba abajo en el campo, y cuántos huecos tiene cada línea
    private static final String[] LINEAS = { "DELANTERO", "MEDIO", "DEFENSA", "PORTERO" };
    private static final int[] HUECOS = { 3, 3, 4, 1 };

    private final Manager manager;
    private final JLabel resumen = new JLabel();
    private final JPanel campo;

    /** Panel que dibuja el campo de fondo: césped verde y las líneas del icono en blanco. */
    private static class PanelCampo extends JPanel {
        private final BufferedImage lineas = cargarLineas();

        PanelCampo() {
            super(new FlowLayout(FlowLayout.CENTER, 0, 0));
        }

        /** El icono es negro con transparencia: lo pintamos de blanco. */
        private static BufferedImage cargarLineas() {
            try {
                URL url = Plantilla.class.getResource("/imagenes/iconos/campo-de-futbol.png");
                BufferedImage original = ImageIO.read(url);
                BufferedImage blanco = new BufferedImage(original.getWidth(), original.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = blanco.createGraphics();
                g.drawImage(original, 0, 0, null);
                g.setComposite(AlphaComposite.SrcIn);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, blanco.getWidth(), blanco.getHeight());
                g.dispose();
                return blanco;
            } catch (Exception e) {
                return null; // sin icono: solo se verá el césped
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(34, 139, 34));
            g.fillRect(0, 0, getWidth(), getHeight());
            if (lineas != null) {
                // se dibuja a la altura del panel, centrado y sin deformar
                int alto = getHeight();
                int ancho = alto * lineas.getWidth() / lineas.getHeight();
                // semitransparente para que no tape los nombres de los jugadores
                // (copia de g, para que la transparencia no afecte a lo que se pinta después)
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2.drawImage(lineas, (getWidth() - ancho) / 2, 0, ancho, alto, this);
                g2.dispose();
            }
        }
    }

    public Plantilla(Manager manager) {
        this.manager = manager;
        setTitle("Plantilla - " + manager.nombre);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 700);
        setResizable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 14));
        cabecera.setPreferredSize(new Dimension(900, 55));
        cabecera.setBackground(new Color(20, 40, 90));
        resumen.setForeground(Color.WHITE);
        resumen.setFont(new Font("SansSerif", Font.BOLD, 16));
        cabecera.add(resumen);

        campo = new PanelCampo();
        campo.setPreferredSize(new Dimension(900, 545));

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        pie.setPreferredSize(new Dimension(900, 50));
        JButton volver = new JButton("Volver al mercado");
        volver.setPreferredSize(new Dimension(180, 32));
        volver.addActionListener(e -> {
            new Tienda(manager).setVisible(true); // le devolvemos el mismo manager
            dispose();
        });
        pie.add(volver);

        add(cabecera);
        add(campo);
        add(pie);

        pintarCampo();
        setLocationRelativeTo(null);
    }

    /** Coloca a los jugadores por líneas y rellena con "Libre" los huecos vacíos. */
    private void pintarCampo() {
        campo.removeAll();
        for (int i = 0; i < LINEAS.length; i++) {
            JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            fila.setOpaque(false);
            fila.setPreferredSize(new Dimension(880, 135));

            int colocados = 0;
            for (Jugador j : manager.plantilla) {
                if (j.posicion.equals(LINEAS[i])) {
                    fila.add(crearFicha(j));
                    colocados++;
                }
            }
            for (int h = colocados; h < HUECOS[i]; h++) {
                fila.add(crearHueco());
            }
            campo.add(fila);
        }
        resumen.setText(manager.nombre + "  |  " + manager.plantilla.size() + "/11 jugadores  |  "
                + manager.puntosTotales() + " pts  |  " + manager.presupuesto + " M libres");
        campo.revalidate();
        campo.repaint();
    }

    private JPanel crearFicha(Jugador j) {
        JPanel ficha = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        ficha.setOpaque(false);
        ficha.setPreferredSize(new Dimension(130, 130));

        JLabel foto = new JLabel();
        foto.setPreferredSize(new Dimension(120, 60));
        foto.setHorizontalAlignment(SwingConstants.CENTER);
        foto.setForeground(Color.WHITE);
        Recursos.cargar(j.avatar, foto, 60);

        JLabel nombre = new JLabel(j.nombre);
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 11));
        nombre.setPreferredSize(new Dimension(125, 16));
        nombre.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel puntos = new JLabel(j.puntos + " pts");
        puntos.setForeground(Color.YELLOW);
        puntos.setPreferredSize(new Dimension(125, 16));
        puntos.setHorizontalAlignment(SwingConstants.CENTER);

        JButton vender = new JButton("Vender");
        vender.setPreferredSize(new Dimension(80, 22));
        vender.setMargin(new java.awt.Insets(0, 4, 0, 4));
        vender.addActionListener(e -> vender(j));

        ficha.add(foto);
        ficha.add(nombre);
        ficha.add(puntos);
        ficha.add(vender);
        return ficha;
    }

    private JPanel crearHueco() {
        JPanel hueco = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 45));
        hueco.setOpaque(false);
        hueco.setPreferredSize(new Dimension(130, 130));
        JLabel libre = new JLabel("Libre");
        libre.setForeground(new Color(255, 255, 255, 190));
        hueco.add(libre);
        return hueco;
    }

    private void vender(Jugador j) {
        // JDialog modal sencillo: pregunta Sí / No
        JDialog dialog = new JDialog(this, "Vender", true);
        dialog.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        dialog.setSize(380, 150);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("¿Vender a " + j.nombre + " por " + j.precio + " M?"));

        JButton si = new JButton("Sí");
        si.addActionListener(e -> {
            manager.vender(j);
            dialog.dispose();
            pintarCampo();
        });
        JButton no = new JButton("No");
        no.addActionListener(e -> dialog.dispose());

        dialog.add(si);
        dialog.add(no);
        dialog.setVisible(true);
    }
}
