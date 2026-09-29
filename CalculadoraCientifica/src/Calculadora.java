import javax.swing.*;
import java.awt.*;


public class Calculadora extends JFrame {

    private JTextField pantallaOperacion;
    private JTextField pantallaResultado;
    private JRadioButton radioGrados;
    private JRadioButton radioRadianes;

    // ---- Estado de la calculadora ----
    private String textoOperacion = "";
    private boolean operacionTerminada = false;

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

        
        JButton botonSeno = Estilo.crearBoton("sin", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonSeno.addActionListener(e -> accionSeno());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(botonSeno, gbc);

        JButton botonCoseno = Estilo.crearBoton("cos", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonCoseno.addActionListener(e -> accionCoseno());
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(botonCoseno, gbc);

        JButton botonTangente = Estilo.crearBoton("tan", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
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

        // ---- Fila 7 8 9 / C ----
        JButton boton7 = Estilo.crearBoton("7", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton7.addActionListener(e -> escribirDigito("7"));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        add(boton7, gbc);

        JButton boton8 = Estilo.crearBoton("8", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton8.addActionListener(e -> escribirDigito("8"));
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(boton8, gbc);

        JButton boton9 = Estilo.crearBoton("9", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton9.addActionListener(e -> escribirDigito("9"));
        gbc.gridx = 2;
        gbc.gridy = 4;
        add(boton9, gbc);

        JButton botonDividir = Estilo.crearBoton("/", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonDividir.addActionListener(e -> accionDividir());
        gbc.gridx = 3;
        gbc.gridy = 4;
        add(botonDividir, gbc);

        JButton botonLimpiar = Estilo.crearBoton("C", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonLimpiar.addActionListener(e -> accionLimpiar());
        gbc.gridx = 4;
        gbc.gridy = 4;
        add(botonLimpiar, gbc);

        // ---- Fila 4 5 6 * <- ----
        JButton boton4 = Estilo.crearBoton("4", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton4.addActionListener(e -> escribirDigito("4"));
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(boton4, gbc);

        JButton boton5 = Estilo.crearBoton("5", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton5.addActionListener(e -> escribirDigito("5"));
        gbc.gridx = 1;
        gbc.gridy = 5;
        add(boton5, gbc);

        JButton boton6 = Estilo.crearBoton("6", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton6.addActionListener(e -> escribirDigito("6"));
        gbc.gridx = 2;
        gbc.gridy = 5;
        add(boton6, gbc);

        JButton botonMultiplicar = Estilo.crearBoton("*", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonMultiplicar.addActionListener(e -> accionMultiplicar());
        gbc.gridx = 3;
        gbc.gridy = 5;
        add(botonMultiplicar, gbc);

        JButton botonBorrar = Estilo.crearBoton("<-", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonBorrar.addActionListener(e -> accionBorrar());
        gbc.gridx = 4;
        gbc.gridy = 5;
        add(botonBorrar, gbc);

        // ---- Fila 1 2 3 - pi ----
        JButton boton1 = Estilo.crearBoton("1", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton1.addActionListener(e -> escribirDigito("1"));
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(boton1, gbc);

        JButton boton2 = Estilo.crearBoton("2", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton2.addActionListener(e -> escribirDigito("2"));
        gbc.gridx = 1;
        gbc.gridy = 6;
        add(boton2, gbc);

        JButton boton3 = Estilo.crearBoton("3", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton3.addActionListener(e -> escribirDigito("3"));
        gbc.gridx = 2;
        gbc.gridy = 6;
        add(boton3, gbc);

        JButton botonRestar = Estilo.crearBoton("-", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonRestar.addActionListener(e -> accionRestar());
        gbc.gridx = 3;
        gbc.gridy = 6;
        add(botonRestar, gbc);

        JButton botonPi = Estilo.crearBoton("π", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonPi.addActionListener(e -> accionPi());
        gbc.gridx = 4;
        gbc.gridy = 6;
        add(botonPi, gbc);

        JButton boton0 = Estilo.crearBoton("0", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        boton0.addActionListener(e -> escribirDigito("0"));
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        add(boton0, gbc);

        JButton botonPunto = Estilo.crearBoton(".", Estilo.BOTON_NUMERO, Estilo.TEXTO_OSCURO);
        botonPunto.addActionListener(e -> accionPunto());
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        add(botonPunto, gbc);

        JButton botonSumar = Estilo.crearBoton("+", Estilo.BOTON_OPERACION, Estilo.TEXTO_CLARO);
        botonSumar.addActionListener(e -> accionSumar());
        gbc.gridx = 3;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        add(botonSumar, gbc);


        JButton botonIgual = Estilo.crearBoton("=", Estilo.BOTON_IGUAL, Estilo.TEXTO_OSCURO);
        botonIgual.addActionListener(e -> accionIgual());
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 5;
        gbc.weighty = 1.4;
        add(botonIgual, gbc);
    }


    private void accionSumar() { aplicarOperador("+"); }
    private void accionRestar() { aplicarOperador("-"); }
    private void accionMultiplicar() { aplicarOperador("*"); }
    private void accionDividir() { aplicarOperador("/"); }
    private void accionSeno() { escribirDigito("sin("); }
    private void accionCoseno() { escribirDigito("cos("); }
    private void accionTangente() { escribirDigito("tan("); }
    private void accionPi() { escribirDigito("π"); }
    private void accionPunto() { escribirDigito("."); }

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
        operacionTerminada = false;
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
        operacionTerminada = true;
    }

    private void escribirDigito(String digito) {
        if (operacionTerminada) {
            textoOperacion = "";
            pantallaResultado.setText("0");
            operacionTerminada = false;
        }
        textoOperacion += digito;
        pantallaOperacion.setText(textoOperacion);
    }

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
    }

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
