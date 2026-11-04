import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

// VENTANA PRINCIPAL: contiene el escenario y los botones de control.
// No se usa el teclado: todo se maneja con botones.
public class Juego extends JFrame {

    private final PanelJuego escenario;

    public Juego() {
        setTitle("Invasores");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ESCENARIO
        escenario = new PanelJuego();
        add(escenario, BorderLayout.CENTER);

        // BOTONES (debajo del escenario)
        JPanel controles = new JPanel(new GridLayout(2, 1));
        controles.add(crearPanelFlechas());
        controles.add(crearPanelPartida());
        add(controles, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    // Fila con las flechas de movimiento
    private JPanel crearPanelFlechas() {
        JPanel panel = new JPanel(new FlowLayout());

        panel.add(crearBotonDireccion("←", escenario::pulsarIzquierda));
        panel.add(crearBotonDireccion("↑", escenario::pulsarArriba));
        panel.add(crearBotonDireccion("↓", escenario::pulsarAbajo));
        panel.add(crearBotonDireccion("→", escenario::pulsarDerecha));

        return panel;
    }

    // Fila con los botones de la partida
    private JPanel crearPanelPartida() {
        JPanel panel = new JPanel(new FlowLayout());

        JButton jugar = new JButton("Jugar");
        JButton pausa = new JButton("Pausa / Reanudar");
        JButton reiniciar = new JButton("Reiniciar");

        jugar.addActionListener(e -> escenario.iniciar());
        pausa.addActionListener(e -> escenario.alternarPausa());
        reiniciar.addActionListener(e -> escenario.reiniciar());

        panel.add(jugar);
        panel.add(pausa);
        panel.add(reiniciar);

        return panel;
    }

    // Boton de direccion: avisa al escenario al pulsarlo (true) y al soltarlo (false)
    // para que el jugador siga moviendose mientras se mantiene apretado.
    private JButton crearBotonDireccion(String texto, Consumer<Boolean> accion) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 18));
        boton.setPreferredSize(new Dimension(60, 35));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                accion.accept(true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                accion.accept(false);
            }
        });

        return boton;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Juego());
    }
}
