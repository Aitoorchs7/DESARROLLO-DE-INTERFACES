import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

// MONEDA: objeto recogible. Al recogerla cambia de sitio.
public class Moneda extends Entidad {

    public static final int TAMANO = 22;

    public Moneda(int x, int y) {
        super(x, y, TAMANO, TAMANO);
    }

    // Reaparece en otro punto, sin quedar justo encima del jugador
    public void reaparecerLejosDe(Entidad jugador, int anchoEscena, int altoEscena, Random aleatorio) {
        do {
            colocarAleatoriamente(anchoEscena, altoEscena, aleatorio);
        } while (colisionaCon(jugador));
    }

    @Override
    public void dibujar(Graphics g) {
        g.setColor(new Color(240, 170, 20));
        g.fillOval(x, y, ancho, alto);
        g.setColor(new Color(160, 100, 0));
        g.drawOval(x, y, ancho, alto);
        g.drawOval(x + 5, y + 5, ancho - 10, alto - 10);
    }
}
