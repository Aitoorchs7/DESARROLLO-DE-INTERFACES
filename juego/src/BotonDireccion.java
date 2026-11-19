import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// BOTON DE DIRECCION: clase base de las flechas.
// Avisa al escenario al pulsarlo (true) y al soltarlo (false)
// para que el jugador siga moviendose mientras se mantiene apretado.
public abstract class BotonDireccion extends JButton {

    protected final PanelJuego escenario;

    public BotonDireccion(String texto, PanelJuego escenario) {
        super(texto);
        this.escenario = escenario;

        setFont(new Font("Arial", Font.BOLD, 18));
        setPreferredSize(new Dimension(60, 35));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mover(true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mover(false);
            }
        });
    }

    // Cada flecha indica hacia donde mueve al jugador
    protected abstract void mover(boolean pulsado);
}
