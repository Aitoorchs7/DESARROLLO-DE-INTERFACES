// Estados posibles de la partida
public enum Estado {
    INICIO,     // pantalla de bienvenida, esperando a pulsar "Jugar"
    JUGANDO,    // la partida esta en marcha
    PAUSA,      // el Timer esta parado
    VICTORIA,   // se han conseguido todos los puntos
    DERROTA     // se han perdido todas las vidas
}
