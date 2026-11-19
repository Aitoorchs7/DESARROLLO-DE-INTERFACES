import javax.swing.*;
import java.awt.*;

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

        panel.add(new BotonIzquierda(escenario));
        panel.add(new BotonArriba(escenario));
        panel.add(new BotonAbajo(escenario));
        panel.add(new BotonDerecha(escenario));

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Juego());
    }
}
