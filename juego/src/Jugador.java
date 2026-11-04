import java.awt.Color;
import java.awt.Graphics;

// JUGADOR: se mueve cuando el usuario pulsa los botones de flecha
public class Jugador extends Entidad {

    public static final int TAMANO = 34;

    private final int velocidad = 6;

    public Jugador(int x, int y) {
        super(x, y, TAMANO, TAMANO);
    }

    // Cada metodo mueve al jugador sin dejarle salir de la escena
    public void moverIzquierda() {
        x = Math.max(0, x - velocidad);
    }

    public void moverDerecha(int anchoEscena) {
        x = Math.min(anchoEscena - ancho, x + velocidad);
    }

    public void moverArriba() {
        y = Math.max(0, y - velocidad);
    }

    public void moverAbajo(int altoEscena) {
        y = Math.min(altoEscena - alto, y + velocidad);
    }

    // Vuelve a la posicion inicial (para reiniciar la partida)
    public void recolocar(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void dibujar(Graphics g) {
        // Cuerpo
        g.setColor(new Color(35, 100, 200));
        g.fillRoundRect(x, y, ancho, alto, 10, 10);
        // Ojos
        g.setColor(Color.WHITE);
        g.fillOval(x + 7, y + 9, 8, 8);
        g.fillOval(x + 19, y + 9, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(x + 10, y + 12, 3, 3);
        g.fillOval(x + 22, y + 12, 3, 3);
    }
}
