import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/** Pantalla 2: mercado de fichajes. */
public class Tienda extends JFrame {

    private final Manager manager;
    private final JLabel presupuesto = new JLabel();
    private final JLabel aviso = new JLabel(" ");
    private final JComboBox<String> filtro = new JComboBox<>(
            new String[] { "TODAS", "PORTERO", "DEFENSA", "MEDIO", "DELANTERO" });
    private final JPanel panelTarjetas = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));

    public Tienda(Manager manager) {
        this.manager = manager;
        setTitle("Mercado - " + manager.nombre);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 700);
        setResizable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        // Cabecera: manager, presupuesto y filtro por posición
        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 12));
        cabecera.setPreferredSize(new Dimension(900, 55));
        cabecera.setBackground(new Color(20, 40, 90));

        JLabel nombre = new JLabel("Manager: " + manager.nombre);
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 16));

        presupuesto.setForeground(new Color(120, 255, 140));
        presupuesto.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel etiquetaFiltro = new JLabel("Posición:");
        etiquetaFiltro.setForeground(Color.WHITE);
        filtro.addActionListener(e -> pintarTarjetas());

        cabecera.add(nombre);
        cabecera.add(presupuesto);
        cabecera.add(etiquetaFiltro);
        cabecera.add(filtro);

        // Zona central con scroll
        JScrollPane scroll = new JScrollPane(panelTarjetas);
        scroll.setPreferredSize(new Dimension(900, 500));
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        // Pie: mensajes (fichado / no fichado) y paso a la plantilla
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        pie.setPreferredSize(new Dimension(900, 50));
        JButton verPlantilla = new JButton("Ver mi plantilla");
        verPlantilla.addActionListener(e -> {
            new Plantilla(manager).setVisible(true); // pasamos el manager a la otra pantalla
            dispose();
        });
        pie.add(aviso);
        pie.add(verPlantilla);

        add(cabecera);
        add(scroll);
        add(pie);

        pintarTarjetas();
        setLocationRelativeTo(null);
    }

    /** Vuelve a pintar las tarjetas según el filtro y actualiza el presupuesto. */
    private void pintarTarjetas() {
        panelTarjetas.removeAll();
        String posicion = (String) filtro.getSelectedItem();
        int n = 0;
        for (Jugador j : Datos.JUGADORES) {
            if (posicion.equals("TODAS") || j.posicion.equals(posicion)) {
                panelTarjetas.add(crearTarjeta(j));
                n++;
            }
        }
        // 4 tarjetas por fila: el scroll necesita saber la altura total
        int filas = (n + 3) / 4;
        panelTarjetas.setPreferredSize(new Dimension(860, filas * 252 + 12));
        presupuesto.setText("Presupuesto: " + manager.presupuesto + " M");
        panelTarjetas.revalidate();
        panelTarjetas.repaint();
    }

    private JPanel crearTarjeta(Jugador j) {
        JPanel t = new JPanel(new FlowLayout(FlowLayout.CENTER, 200, 4));
        t.setPreferredSize(new Dimension(200, 240));
        t.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Foto: viene de internet. Escudo: del proyecto (o de internet si es una URL)
        JLabel foto = new JLabel();
        foto.setPreferredSize(new Dimension(190, 90));
        foto.setHorizontalAlignment(SwingConstants.CENTER);
        Recursos.cargar(j.avatar, foto, 90);

        JLabel escudo = new JLabel();
        escudo.setPreferredSize(new Dimension(190, 26));
        escudo.setHorizontalAlignment(SwingConstants.CENTER);
        Recursos.cargar(Datos.ESCUDOS.get(j.seleccion), escudo, 24);

        JLabel nombre = new JLabel(j.nombre);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel datos = new JLabel(j.seleccion + " · " + j.posicion);
        JLabel precio = new JLabel(j.precio + " M  |  " + j.puntos + " pts");

        JButton fichar = new JButton("Fichar");
        fichar.addActionListener(e -> fichar(j));

        t.add(foto);
        t.add(escudo);
        t.add(nombre);
        t.add(datos);
        t.add(precio);
        t.add(fichar);
        return t;
    }

    private void fichar(Jugador j) {
        // JDialog modal sencillo: pregunta Sí / No
        JDialog dialog = new JDialog(this, "Fichar", true);
        dialog.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        dialog.setSize(380, 150);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("¿Fichar a " + j.nombre + " por " + j.precio + " M?"));

        JButton si = new JButton("Sí");
        si.addActionListener(e -> {
            // Se cierra y se muestra el resultado en el pie de la ventana
            aviso.setText(j.nombre + ": " + manager.fichar(j));
            presupuesto.setText("Presupuesto: " + manager.presupuesto + " M");
            dialog.dispose();
        });
        JButton no = new JButton("No");
        no.addActionListener(e -> dialog.dispose());

        dialog.add(si);
        dialog.add(no);
        dialog.setVisible(true); // al ser modal, espera aquí hasta que se cierre
    }
}
