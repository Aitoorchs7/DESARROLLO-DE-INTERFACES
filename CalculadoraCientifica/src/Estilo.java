import javax.swing.*;
import java.awt.*;

/**
 * Colores, fuentes y metodos de estilo de la calculadora, separados
 * de Calculadora.java para que esa clase se centre en el grid y la logica.
 */
public class Estilo {

    // El fondo es el mas oscuro de todos, luego los botones de operacion,
    // luego los numericos (mas claros) y el igual en naranja.
    public static final Color FONDO = new Color(30, 30, 30);
    public static final Color PANTALLA = new Color(15, 15, 15);
    public static final Color BOTON_NUMERO = new Color(220, 220, 220);
    public static final Color BOTON_OPERACION = new Color(70, 70, 70);
    public static final Color BOTON_IGUAL = new Color(255, 140, 0);
    public static final Color TEXTO_CLARO = Color.WHITE;
    public static final Color TEXTO_OSCURO = Color.BLACK;

    public static final Font FUENTE_BOTON = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FUENTE_PANTALLA_OPERACION = new Font("Consolas", Font.PLAIN, 18);
    public static final Font FUENTE_PANTALLA_RESULTADO = new Font("Consolas", Font.BOLD, 32);

    /** Crea un boton ya con su color de fondo, color de letra y tipografia. */
    public static JButton crearBoton(String texto, Color fondo, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setBackground(fondo);
        boton.setForeground(colorTexto);
        boton.setFont(FUENTE_BOTON);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        return boton;
    }

    /** Da estilo a una de las dos lineas de la pantalla (operacion o resultado). */
    public static void aplicarPantalla(JTextField pantalla, Font fuente, Color colorTexto) {
        pantalla.setEditable(false);
        pantalla.setHorizontalAlignment(JTextField.RIGHT);
        pantalla.setFont(fuente);
        pantalla.setBackground(PANTALLA);
        pantalla.setForeground(colorTexto);
    }

    /** Da estilo a los radiobuttons de Grados / Radianes. */
    public static void aplicarRadio(JRadioButton radio) {
        radio.setForeground(TEXTO_CLARO);
        radio.setOpaque(false);
    }
}
