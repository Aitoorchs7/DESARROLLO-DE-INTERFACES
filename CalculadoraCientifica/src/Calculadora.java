import javax.swing.*;
import java.awt.*;

/**
 * Calculadora cientifica hecha con Swing y GridBagLayout "a mano".
 *
 * Primero se crean y colocan todos los botones en el grid, y al final
 * del constructor se conectan todos con su addActionListener.
 *
 * Funciona como una calculadora normal: vas metiendo un numero, pulsas
 * una operacion (o seno/coseno/tangente), y el resultado se va
 * acumulando en el orden en el que se pulsa (no hay prioridad entre
 * operaciones). Los parentesis permiten un unico nivel: al abrir "("
 * se guarda donde ibamos, y al cerrar ")" se retoma desde ahi.
 */
public class Calculadora extends JFrame {

    private JTextField pantallaOperacion;
    private JTextField pantallaResultado;
    private JRadioButton radioGrados;
    private JRadioButton radioRadianes;

    // ---- Estado de la calculadora ----
<<<<<<< HEAD
    private double resultado = 0;            // valor acumulado hasta ahora
    private String operadorPendiente = null; // "+", "-", "*", "/" o null si no hay ninguno
    private String numeroActual = "";        // lo que se esta escribiendo ahora mismo
    private String textoOperacion = "";      // todo lo que se ha ido pulsando, para la linea de arriba

    // ---- Estado guardado al abrir un parentesis (solo un nivel) ----
    private Double resultadoGuardado = null;
    private String operadorGuardado = null;
=======
    private String textoOperacion = "";
    private boolean operacionTerminada = false;
>>>>>>> 4953cd774ca99c02377b72a62ed6eed5ee575912

    public Calculadora() {
        setTitle("Calculadora Cientifica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 600);
        setMinimumSize(new Dimension(420, 560));
        setLocationRelativeTo(null);

        getContentPane().setBackground(Estilo.FONDO);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;

        // ---- Pantalla: la operacion en una linea y el resultado en otra ----
        pantallaOperacion = new JTextField("0");
        Estilo.aplicarPantalla(pantallaOperacion, Estilo.FUENTE_PANTALLA_OPERACION, Color.LIGHT_GRAY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(pantallaOperacion, gbc);

        pantallaResultado = new JTextField("0");
        Estilo.aplicarPantalla(pantallaResultado, Estilo.FUENTE_PANTALLA_RESULTADO, Estilo.TEXTO_CLARO);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(pantallaResultado, gbc);

        // ---- Radiobuttons de grados / radianes ----
        radioGrados = new JRadioButton("Grados", true);
        radioRadianes = new JRadioButton("Radianes");
        Estilo.aplicarRadio(radioGrados);
        Estilo.aplicarRadio(radioRadianes);

        ButtonGroup grupoAngulos = new ButtonGroup();
        grupoAngulos.add(radioGrados);
        grupoAngulos.add(radioRadianes);

        JPanel panelRadios = new JPanel();
        panelRadios.setBackground(Estilo.FONDO);
        panelRadios.add(radioGrados);
        panelRadios.add(radioRadianes);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(panelRadios, gbc);

<<<<<<< HEAD
        // ---- A partir de aqui, la botonera: primero se crean y colocan ----
        // ---- todos los botones, y los addActionListener van al final. ----
=======
        
        JButton botonSeno = Estilo.crearBoton("sin", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonSeno.addActionListener(e -> accionSeno());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
>>>>>>> 4953cd774ca99c02377b72a62ed6eed5ee575912
        gbc.weightx = 1;
        gbc.weighty = 1;

        // Fila: sin, cos, tan, (, )
        JButton botonSeno = Estilo.crearBoton("sin", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(botonSeno, gbc);

        JButton botonCoseno = Estilo.crearBoton("cos", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
<<<<<<< HEAD
=======
        botonCoseno.addActionListener(e -> accionCoseno());
>>>>>>> 4953cd774ca99c02377b72a62ed6eed5ee575912
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(botonCoseno, gbc);

        JButton botonTangente = Estilo.crearBoton("tan", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
<<<<<<< HEAD
        gbc.gridx = 2;
        gbc.gridy = 3;
        add(botonTangente, gbc);
=======
        botonTangente.addActionListener(e -> accionTangente());
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(botonTangente, gbc);

        JButton botonAbre = Estilo.crearBoton("(", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonAbre.addActionListener(e -> escribirDigito("("));
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(botonAbre, gbc);

        JButton botonCierra = Estilo.crearBoton(")", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonCierra.addActionListener(e -> escribirDigito(")"));
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(botonCierra, gbc);
>>>>>>> 4953cd774ca99c02377b72a62ed6eed5ee575912

        JButton botonAbreParentesis = Estilo.crearBoton("(", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 3;
        gbc.gridy = 3;
        add(botonAbreParentesis, gbc);

        JButton botonCierraParentesis = Estilo.crearBoton(")", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 4;
        gbc.gridy = 3;
        add(botonCierraParentesis, gbc);

        // Fila: 7, 8, 9, /, C
        JButton boton7 = Estilo.crearBoton("7", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(boton7, gbc);

        JButton boton8 = Estilo.crearBoton("8", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(boton8, gbc);

        JButton boton9 = Estilo.crearBoton("9", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 2;
        gbc.gridy = 4;
        add(boton9, gbc);

        JButton botonDividir = Estilo.crearBoton("/", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 3;
        gbc.gridy = 4;
        add(botonDividir, gbc);

        JButton botonLimpiar = Estilo.crearBoton("C", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 4;
        gbc.gridy = 4;
        add(botonLimpiar, gbc);

        // Fila: 4, 5, 6, *, <-
        JButton boton4 = Estilo.crearBoton("4", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(boton4, gbc);

        JButton boton5 = Estilo.crearBoton("5", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 1;
        gbc.gridy = 5;
        add(boton5, gbc);

        JButton boton6 = Estilo.crearBoton("6", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 2;
        gbc.gridy = 5;
        add(boton6, gbc);

        JButton botonMultiplicar = Estilo.crearBoton("*", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 3;
        gbc.gridy = 5;
        add(botonMultiplicar, gbc);

        JButton botonBorrar = Estilo.crearBoton("<-", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 4;
        gbc.gridy = 5;
        add(botonBorrar, gbc);

        // Fila: 1, 2, 3, -, pi
        JButton boton1 = Estilo.crearBoton("1", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(boton1, gbc);

        JButton boton2 = Estilo.crearBoton("2", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 1;
        gbc.gridy = 6;
        add(boton2, gbc);

        JButton boton3 = Estilo.crearBoton("3", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 2;
        gbc.gridy = 6;
        add(boton3, gbc);

        JButton botonRestar = Estilo.crearBoton("-", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 3;
        gbc.gridy = 6;
        add(botonRestar, gbc);

        JButton botonPi = Estilo.crearBoton("π", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 4;
        gbc.gridy = 6;
        add(botonPi, gbc);

        // Fila: 0 (doble), . , + (doble)
        JButton boton0 = Estilo.crearBoton("0", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        add(boton0, gbc);

        JButton botonPunto = Estilo.crearBoton(".", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        add(botonPunto, gbc);

        JButton botonSumar = Estilo.crearBoton("+", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        gbc.gridx = 3;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        add(botonSumar, gbc);

        // Igual: ocupa toda la fila (uso no simple del grid)
        JButton botonIgual = Estilo.crearBoton("=", Estilo.BOTON_IGUAL, Estilo.TEXTO_OSCURO);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 5;
        gbc.weighty = 1.4;
        add(botonIgual, gbc);

        // ---- Aqui se conecta cada boton con su metodo ----
        botonSeno.addActionListener(e -> accionSeno());
        botonCoseno.addActionListener(e -> accionCoseno());
        botonTangente.addActionListener(e -> accionTangente());
        botonAbreParentesis.addActionListener(e -> accionAbrirParentesis());
        botonCierraParentesis.addActionListener(e -> accionCerrarParentesis());

        boton7.addActionListener(e -> escribirDigito("7"));
        boton8.addActionListener(e -> escribirDigito("8"));
        boton9.addActionListener(e -> escribirDigito("9"));
        botonDividir.addActionListener(e -> accionDividir());
        botonLimpiar.addActionListener(e -> accionLimpiar());

        boton4.addActionListener(e -> escribirDigito("4"));
        boton5.addActionListener(e -> escribirDigito("5"));
        boton6.addActionListener(e -> escribirDigito("6"));
        botonMultiplicar.addActionListener(e -> accionMultiplicar());
        botonBorrar.addActionListener(e -> accionBorrar());

        boton1.addActionListener(e -> escribirDigito("1"));
        boton2.addActionListener(e -> escribirDigito("2"));
        boton3.addActionListener(e -> escribirDigito("3"));
        botonRestar.addActionListener(e -> accionRestar());
        botonPi.addActionListener(e -> accionPi());

        boton0.addActionListener(e -> escribirDigito("0"));
        botonPunto.addActionListener(e -> accionPunto());
        botonSumar.addActionListener(e -> accionSumar());

        botonIgual.addActionListener(e -> accionIgual());
    }

    // =========================================================
    // Un metodo por cada boton de operar.
    // =========================================================

<<<<<<< HEAD
    private void accionSumar() {
        aplicarOperador("+");
    }

    private void accionRestar() {
        aplicarOperador("-");
    }

    private void accionMultiplicar() {
        aplicarOperador("*");
    }

    private void accionDividir() {
        aplicarOperador("/");
    }

    private void accionSeno() {
        aplicarTrigonometria("sin");
    }

    private void accionCoseno() {
        aplicarTrigonometria("cos");
    }

    private void accionTangente() {
        aplicarTrigonometria("tan");
    }

    /** Sustituye el numero que se este escribiendo por el valor de pi. */
    private void accionPi() {
        numeroActual = String.valueOf(Math.PI);
        textoOperacion = textoOperacion + "π";
        pantallaOperacion.setText(textoOperacion);
        pantallaResultado.setText(formatear(Math.PI));
    }

    private void accionPunto() {
        if (!numeroActual.contains(".")) {
            numeroActual = numeroActual + ".";
            textoOperacion = textoOperacion + ".";
            pantallaOperacion.setText(textoOperacion);
            pantallaResultado.setText(numeroActual);
        }
    }
=======
    private void accionSumar() { aplicarOperador("+"); }
    private void accionRestar() { aplicarOperador("-"); }
    private void accionMultiplicar() { aplicarOperador("*"); }
    private void accionDividir() { aplicarOperador("/"); }
    private void accionSeno() { escribirDigito("sin("); }
    private void accionCoseno() { escribirDigito("cos("); }
    private void accionTangente() { escribirDigito("tan("); }
    private void accionPi() { escribirDigito("π"); }
    private void accionPunto() { escribirDigito("."); }
>>>>>>> 4953cd774ca99c02377b72a62ed6eed5ee575912

    /**
     * Guarda el resultado y el operador que teniamos hasta ahora, y
     * empieza a contar desde cero como si fuera una cuenta nueva.
     * Solo se admite un nivel de parentesis (no anidados).
     */
    private void accionAbrirParentesis() {
        resultadoGuardado = resultado;
        operadorGuardado = operadorPendiente;
        resultado = 0;
        operadorPendiente = null;
        numeroActual = "";
        textoOperacion = textoOperacion + "(";
        pantallaOperacion.setText(textoOperacion);
    }

    /**
     * Calcula lo que hay dentro del parentesis y lo combina con lo
     * que se habia guardado al abrirlo.
     */
    private void accionCerrarParentesis() {
        if (resultadoGuardado == null) {
            return; // no se abrio ningun parentesis
        }
        if (!numeroActual.isEmpty()) {
            double valor = Double.parseDouble(numeroActual);
            if (operadorPendiente != null) {
                resultado = calcular(resultado, valor, operadorPendiente);
            } else {
                resultado = valor;
            }
        }

        double valorInterior = resultado;
        if (operadorGuardado != null) {
            resultado = calcular(resultadoGuardado, valorInterior, operadorGuardado);
        } else {
            resultado = valorInterior;
        }
        resultadoGuardado = null;
        operadorGuardado = null;

        numeroActual = String.valueOf(resultado);
        textoOperacion = textoOperacion + ")";
        pantallaOperacion.setText(textoOperacion);
        pantallaResultado.setText(formatear(resultado));
    }

    private void accionBorrar() {
        if (operacionTerminada) {
            textoOperacion = "";
            pantallaResultado.setText("0");
            pantallaOperacion.setText("0");
            operacionTerminada = false;
            return;
        }
        if (!textoOperacion.isEmpty()) {
            textoOperacion = textoOperacion.substring(0, textoOperacion.length() - 1);
        }
        pantallaOperacion.setText(textoOperacion.isEmpty() ? "0" : textoOperacion);
    }

    private void accionLimpiar() {
        textoOperacion = "";
<<<<<<< HEAD
        resultadoGuardado = null;
        operadorGuardado = null;
=======
        operacionTerminada = false;
>>>>>>> 4953cd774ca99c02377b72a62ed6eed5ee575912
        pantallaOperacion.setText("0");
        pantallaResultado.setText("0");
    }

    private void accionIgual() {
        if (textoOperacion.isEmpty()) return;
        try {
            double resultado = evaluarExpresion(textoOperacion);
            pantallaResultado.setText(formatear(resultado));
        } catch (Exception e) {
            pantallaResultado.setText("Error");
        }
<<<<<<< HEAD
        double valor = Double.parseDouble(numeroActual);
        if (operadorPendiente != null) {
            resultado = calcular(resultado, valor, operadorPendiente);
        } else {
            resultado = valor;
        }
        pantallaResultado.setText(formatear(resultado));

        // se deja todo listo para empezar una operacion nueva
        operadorPendiente = null;
        numeroActual = "";
        textoOperacion = "";
        resultadoGuardado = null;
        operadorGuardado = null;
        pantallaOperacion.setText("0");
=======
        operacionTerminada = true;
>>>>>>> 4953cd774ca99c02377b72a62ed6eed5ee575912
    }

    /** Escribe un digito (0-9) en el numero que se esta tecleando. */
    private void escribirDigito(String digito) {
        if (operacionTerminada) {
            textoOperacion = "";
            pantallaResultado.setText("0");
            operacionTerminada = false;
        }
        textoOperacion += digito;
        pantallaOperacion.setText(textoOperacion);
    }

    /**
     * Se usa para +, -, * y /. Si ya habia una operacion esperando
     * (por ejemplo, veniamos de un "+"), primero la calcula, y luego
     * deja preparado el operador nuevo para el siguiente numero.
     */
    private void aplicarOperador(String simbolo) {
        if (operacionTerminada) {
            textoOperacion = pantallaResultado.getText();
            if (textoOperacion.equals("Error")) textoOperacion = "0";
            operacionTerminada = false;
        }
        if (textoOperacion.isEmpty()) textoOperacion = "0";
        textoOperacion += simbolo;
        pantallaOperacion.setText(textoOperacion);
    }

    private double evaluarExpresion(final String str) {
        return new Object() {
            int pos = -1, ch;
            void nextChar() { ch = (++pos < str.length()) ? str.charAt(pos) : -1; }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }
<<<<<<< HEAD
            numeroActual = "";
        }
        operadorPendiente = simbolo;
        textoOperacion = textoOperacion + simbolo;
        pantallaResultado.setText(formatear(resultado));
        pantallaOperacion.setText(textoOperacion);
    }

    /** Hace la cuenta de dos numeros segun el simbolo del operador. */
    private double calcular(double a, double b, String simbolo) {
        if (simbolo.equals("+")) {
            return a + b;
        } else if (simbolo.equals("-")) {
            return a - b;
        } else if (simbolo.equals("*")) {
            return a * b;
        } else {
            return a / b;
        }
    }

    /**
     * Aplica seno, coseno o tangente al numero que se esta escribiendo
     * (o al resultado actual si no se ha escrito nada nuevo), teniendo
     * en cuenta si esta marcado Grados o Radianes.
     */
    private void aplicarTrigonometria(String funcion) {
        double valor = numeroActual.isEmpty() ? resultado : Double.parseDouble(numeroActual);
        double angulo = radioGrados.isSelected() ? Math.toRadians(valor) : valor;

        double resultadoFuncion;
        if (funcion.equals("sin")) {
            resultadoFuncion = Math.sin(angulo);
        } else if (funcion.equals("cos")) {
            resultadoFuncion = Math.cos(angulo);
        } else {
            resultadoFuncion = Math.tan(angulo);
        }

        textoOperacion = textoOperacion + funcion + "(" + formatear(valor) + ")";
        numeroActual = String.valueOf(resultadoFuncion);
        pantallaOperacion.setText(textoOperacion);
        pantallaResultado.setText(formatear(resultadoFuncion));
=======
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Inesperado: " + (char)ch);
                return x;
            }
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }
            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();
                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch == 'π') {
                    nextChar();
                    x = Math.PI;
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    boolean radianes = radioRadianes.isSelected();
                    if (!radianes && (func.equals("sin") || func.equals("cos") || func.equals("tan"))) {
                        x = Math.toRadians(x);
                    }
                    if (func.equals("sin")) x = Math.sin(x);
                    else if (func.equals("cos")) x = Math.cos(x);
                    else if (func.equals("tan")) x = Math.tan(x);
                    else throw new RuntimeException("Función desconocida: " + func);
                } else {
                    throw new RuntimeException("Inesperado: " + (char)ch);
                }
                return x;
            }
        }.parse();
>>>>>>> 4953cd774ca99c02377b72a62ed6eed5ee575912
    }

    /** Si el resultado es entero lo muestro sin decimales. */
    private String formatear(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            return "Error";
        }
        if (Math.abs(valor - Math.rint(valor)) < 1e-9) {
            return String.valueOf((long) Math.rint(valor));
        }
        return String.valueOf(Math.round(valor * 1e10) / 1e10);
    }

    public static void main(String[] args) {
        Calculadora ventana = new Calculadora();
        ventana.setVisible(true);
    }
}
