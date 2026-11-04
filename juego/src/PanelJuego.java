import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// ESCENARIO DEL JUEGO: hereda de JPanel y dibuja todo en paintComponent.
// Patron: botones -> Timer actualiza -> colisiones y reglas -> repaint -> dibujo
public class PanelJuego extends JPanel {

    public static final int ANCHO = 640;
    public static final int ALTO = 420;

    private static final int VIDAS_INICIALES = 3;
    private static final int PUNTOS_PARA_GANAR = 10;
    private static final int PUNTOS_POR_ENEMIGO_NUEVO = 3;   // cada 3 puntos sale otro enemigo
    private static final int MAX_ENEMIGOS = 5;

    // Modelo
    private Jugador jugador;
    private Moneda moneda;
    private final List<Enemigo> enemigos = new ArrayList<>();
    private int puntos;
    private int vidas;
    private Estado estado = Estado.INICIO;

    // Direcciones que estan pulsadas ahora mismo (las cambian los botones)
    private boolean izquierda;
    private boolean derecha;
    private boolean arriba;
    private boolean abajo;

    private final Random aleatorio = new Random();
    private final Timer temporizador;

    public PanelJuego() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(new Color(245, 248, 240));

        prepararPartida();

        // Un unico Timer que se reutiliza durante toda la ejecucion
        temporizador = new Timer(16, e -> actualizarJuego());
    }

    // ---------------------------------------------------------------
    // CONTROL DE LA PARTIDA (los llaman los botones de la ventana)
    // ---------------------------------------------------------------

    public void iniciar() {
        if (estado == Estado.INICIO) {
            estado = Estado.JUGANDO;
            temporizador.start();
            repaint();
        }
    }

    public void alternarPausa() {
        if (estado == Estado.JUGANDO) {
            estado = Estado.PAUSA;
            temporizador.stop();
        } else if (estado == Estado.PAUSA) {
            estado = Estado.JUGANDO;
            temporizador.start();
        }
        repaint();
    }

    public void reiniciar() {
        prepararPartida();
        estado = Estado.JUGANDO;
        if (!temporizador.isRunning()) {
            temporizador.start();
        }
        repaint();
    }

    // Deja todo como al principio de una partida nueva
    private void prepararPartida() {
        puntos = 0;
        vidas = VIDAS_INICIALES;
        izquierda = derecha = arriba = abajo = false;

        jugador = new Jugador((ANCHO - Jugador.TAMANO) / 2, ALTO - Jugador.TAMANO - 20);

        moneda = new Moneda(0, 0);
        moneda.reaparecerLejosDe(jugador, ANCHO, ALTO, aleatorio);

        enemigos.clear();
        anadirEnemigo();
        anadirEnemigo();
    }

    private void anadirEnemigo() {
        Enemigo nuevo = new Enemigo(0, 0, aleatorio);
        nuevo.reaparecerLejosDe(jugador, ANCHO, ALTO, aleatorio);
        enemigos.add(nuevo);
    }

    // ---------------------------------------------------------------
    // MOVIMIENTO DEL JUGADOR (lo llaman los botones de flecha)
    // pulsado = true al apretar el boton, false al soltarlo.
    // Al apretar se mueve ya un paso, y mientras se mantiene
    // pulsado el Timer sigue moviendo al jugador.
    // ---------------------------------------------------------------

    public void pulsarIzquierda(boolean pulsado) {
        izquierda = pulsado;
        if (pulsado && estado == Estado.JUGANDO) {
            jugador.moverIzquierda();
            repaint();
        }
    }

    public void pulsarDerecha(boolean pulsado) {
        derecha = pulsado;
        if (pulsado && estado == Estado.JUGANDO) {
            jugador.moverDerecha(ANCHO);
            repaint();
        }
    }

    public void pulsarArriba(boolean pulsado) {
        arriba = pulsado;
        if (pulsado && estado == Estado.JUGANDO) {
            jugador.moverArriba();
            repaint();
        }
    }

    public void pulsarAbajo(boolean pulsado) {
        abajo = pulsado;
        if (pulsado && estado == Estado.JUGANDO) {
            jugador.moverAbajo(ALTO);
            repaint();
        }
    }

    // ---------------------------------------------------------------
    // ACTUALIZACION (se ejecuta en cada tick del Timer)
    // ---------------------------------------------------------------

    private void actualizarJuego() {
        if (estado != Estado.JUGANDO) {
            return;
        }

        moverJugador();

        for (Enemigo enemigo : enemigos) {
            enemigo.moverAutomaticamente(ANCHO, ALTO);
        }

        comprobarColisiones();
        repaint();
    }

    private void moverJugador() {
        if (izquierda) {
            jugador.moverIzquierda();
        }
        if (derecha) {
            jugador.moverDerecha(ANCHO);
        }
        if (arriba) {
            jugador.moverArriba();
        }
        if (abajo) {
            jugador.moverAbajo(ALTO);
        }
    }

    private void comprobarColisiones() {
        // Jugador con la moneda: suma un punto
        if (jugador.colisionaCon(moneda)) {
            puntos++;
            moneda.reaparecerLejosDe(jugador, ANCHO, ALTO, aleatorio);

            if (puntos >= PUNTOS_PARA_GANAR) {
                terminar(Estado.VICTORIA);
                return;
            }
            // Dificultad progresiva
            if (puntos % PUNTOS_POR_ENEMIGO_NUEVO == 0 && enemigos.size() < MAX_ENEMIGOS) {
                anadirEnemigo();
            }
        }

        // Jugador con un enemigo: pierde una vida y el enemigo reaparece lejos
        for (Enemigo enemigo : enemigos) {
            if (jugador.colisionaCon(enemigo)) {
                vidas--;
                enemigo.reaparecerLejosDe(jugador, ANCHO, ALTO, aleatorio);

                if (vidas <= 0) {
                    terminar(Estado.DERROTA);
                    return;
                }
            }
        }
    }

    private void terminar(Estado resultado) {
        estado = resultado;
        izquierda = derecha = arriba = abajo = false;
        temporizador.stop();
    }

    // ---------------------------------------------------------------
    // DIBUJO: solo representa el estado actual, no lo modifica
    // ---------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        moneda.dibujar(g2);
        for (Enemigo enemigo : enemigos) {
            enemigo.dibujar(g2);
        }
        jugador.dibujar(g2);

        dibujarMarcador(g2);

        switch (estado) {
            case INICIO:
                dibujarMensaje(g2, "INVASORES",
                        "Recoge " + PUNTOS_PARA_GANAR + " monedas y esquiva a los enemigos",
                        "Pulsa \"Jugar\" para empezar");
                break;
            case PAUSA:
                dibujarMensaje(g2, "PAUSA", "Pulsa \"Pausa / Reanudar\" para continuar", null);
                break;
            case VICTORIA:
                dibujarMensaje(g2, "¡HAS GANADO!", "Puntos: " + puntos,
                        "Pulsa \"Reiniciar\" para jugar otra vez");
                break;
            case DERROTA:
                dibujarMensaje(g2, "FIN DEL JUEGO", "Puntos: " + puntos,
                        "Pulsa \"Reiniciar\" para jugar otra vez");
                break;
            default:
                break;
        }
    }

    private void dibujarMarcador(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Puntos: " + puntos + " / " + PUNTOS_PARA_GANAR, 12, 22);

        String textoVidas = "Vidas: " + vidas;
        int anchoTexto = g2.getFontMetrics().stringWidth(textoVidas);
        g2.drawString(textoVidas, ANCHO - anchoTexto - 12, 22);
    }

    // Capa oscura con un titulo y hasta dos lineas de texto centradas
    private void dibujarMensaje(Graphics2D g2, String titulo, String linea1, String linea2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 38));
        dibujarCentrado(g2, titulo, ALTO / 2 - 20);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        if (linea1 != null) {
            dibujarCentrado(g2, linea1, ALTO / 2 + 18);
        }
        if (linea2 != null) {
            dibujarCentrado(g2, linea2, ALTO / 2 + 46);
        }
    }

    private void dibujarCentrado(Graphics2D g2, String texto, int y) {
        int anchoTexto = g2.getFontMetrics().stringWidth(texto);
        g2.drawString(texto, (getWidth() - anchoTexto) / 2, y);
    }
}
