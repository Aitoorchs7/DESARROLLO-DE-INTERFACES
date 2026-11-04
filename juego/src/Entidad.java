import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Random;

// CLASE BASE: todo lo que aparece en la escena tiene posicion, tamano,
// sabe darse un rectangulo (para colisiones) y sabe dibujarse.
// No hereda de ningun componente Swing: solo guarda datos.
public abstract class Entidad {

    protected int x;
    protected int y;
    protected int ancho;
    protected int alto;

    public Entidad(int x, int y, int ancho, int alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    // Rectangulo envolvente, se usa para detectar colisiones
    public Rectangle getLimites() {
        return new Rectangle(x, y, ancho, alto);
    }

    public boolean colisionaCon(Entidad otra) {
        return getLimites().intersects(otra.getLimites());
    }

    // Coloca la entidad en un punto aleatorio de la escena
    public void colocarAleatoriamente(int anchoEscena, int altoEscena, Random aleatorio) {
        x = aleatorio.nextInt(Math.max(1, anchoEscena - ancho));
        y = aleatorio.nextInt(Math.max(1, altoEscena - alto));
    }

    // Cada entidad sabe como dibujarse; el panel solo la llama
    public abstract void dibujar(Graphics g);
}
