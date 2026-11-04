import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

// ENEMIGO: se mueve solo y rebota en los bordes de la escena
public class Enemigo extends Entidad {

    public static final int TAMANO = 32;

    private int dx;
    private int dy;

    public Enemigo(int x, int y, Random aleatorio) {
        super(x, y, TAMANO, TAMANO);
        // Velocidad entre 2 y 4 en cada eje, con direccion aleatoria
        dx = (2 + aleatorio.nextInt(3)) * (aleatorio.nextBoolean() ? 1 : -1);
        dy = (2 + aleatorio.nextInt(3)) * (aleatorio.nextBoolean() ? 1 : -1);
    }

    public void moverAutomaticamente(int anchoEscena, int altoEscena) {
        x += dx;
        y += dy;

        // Rebote horizontal
        if (x <= 0) {
            x = 0;
            dx = Math.abs(dx);
        } else if (x + ancho >= anchoEscena) {
            x = anchoEscena - ancho;
            dx = -Math.abs(dx);
        }

        // Rebote vertical
        if (y <= 0) {
            y = 0;
            dy = Math.abs(dy);
        } else if (y + alto >= altoEscena) {
            y = altoEscena - alto;
            dy = -Math.abs(dy);
        }
    }

    // Tras chocar con el jugador, reaparece lejos de el para no quitar vidas
    // seguidas
    public void reaparecerLejosDe(Entidad jugador, int anchoEscena, int altoEscena, Random aleatorio) {
        do {
            colocarAleatoriamente(anchoEscena, altoEscena, aleatorio);
        } while (distanciaA(jugador) < 150);
    }

    private double distanciaA(Entidad otra) {
        return Math.hypot(x - otra.getX(), y - otra.getY());
    }

    @Override
    public void dibujar(Graphics g) {
        // Cuerpo
        g.setColor(new Color(200, 40, 50));
        g.fillOval(x, y, ancho, alto);
        // Ojos enfadados
        g.setColor(Color.WHITE);
        g.fillOval(x + 7, y + 9, 7, 7);
        g.fillOval(x + 18, y + 9, 7, 7);
        g.setColor(Color.BLACK);
        g.fillOval(x + 9, y + 11, 3, 3);
        g.fillOval(x + 20, y + 11, 3, 3);
        g.drawLine(x + 6, y + 6, x + 14, y + 9);
        g.drawLine(x + 26, y + 6, x + 18, y + 9);
    }
}
