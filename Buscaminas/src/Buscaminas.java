import java.awt.*;
import javax.swing.*;

public class Buscaminas extends javax.swing.JFrame {

    // Componentes del panel superior que necesitaremos tocar despues
    private JLabel Tiempo;
    private JButton reiniciar;
    private JLabel Minas;
    private JLabel resultado;

    private JComboBox<String> dificultad;
    private JButton aplicarDificultad;

    // Timer de la partida
    private Timer timer;
    private int segundos = 0;

    // Items de menu
    private JMenuItem itemNuevaPartida;
    private JMenuItem itemSalir;

    // Tamaño del tablero
    private int filas = 10;
    private int columnas = 10;
    private int numMinas = 10;

    int[][] casillas = new int[filas][columnas];
    JButton[][] botones = new JButton[filas][columnas];
    boolean[][] reveladas = new boolean[filas][columnas];
    // para que se sepa cuales estan reveladas y si no hay bombas alrededor de
    // las reveladas se descubran tambien
    private JPanel tablero = panelTablero();

    public Buscaminas() {
        setTitle("Buscaminas de Aitor");
        setSize(1200, 1000);
        setMinimumSize(new Dimension(500, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Barra de menu
        setJMenuBar(crearMenu());

        // Contenedor de arriba
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.insets = new Insets(15, 10, 15, 10);

        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel superior = panelSuperior();
        add(superior, gbc);

        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(tablero, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel inferior = panelInferior();
        add(inferior, gbc);

        crearCasillas(casillas, numMinas);
        Minas.setText("Minas: " + contarMinas());
        iniciarTimer();
    }

    private JPanel panelSuperior() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        // Fila 0: titulo, ocupando las 4 columnas de la fila de abajo
        JLabel Titulo = new JLabel("Buscaminas");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(Titulo, gbc);

        // Fila 1: minas | tiempo | dificultad | aplicar
        gbc.gridwidth = 1;
        gbc.gridy = 1;

        Minas = new JLabel("Minas: " + contarMinas());
        gbc.gridx = 0;
        panel.add(Minas, gbc);

        Tiempo = new JLabel("Tiempo: ");
        gbc.gridx = 1;
        panel.add(Tiempo, gbc);

        dificultad = new JComboBox<>(new String[] { "Facil", "Medio", "Dificil" });
        gbc.gridx = 2;
        panel.add(dificultad, gbc);

        // La dificultad elegida en el combo no se aplica sola: hace falta
        // pulsar este boton para que la partida se reinicie con ella
        aplicarDificultad = new JButton("Aplicar dificultad");
        gbc.gridx = 3;
        panel.add(aplicarDificultad, gbc);

        aplicarDificultad.addActionListener(e -> {
            numMinas = calcularMinas(filas);
            reiniciarPartida();
        });

        return panel;
    }

    private JPanel panelInferior() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        reiniciar = new JButton("Nueva partida");
        panel.add(reiniciar, gbc);
        reiniciar.addActionListener(e -> reiniciarPartida());

        resultado = new JLabel("Resultado: ");
        gbc.gridx = 1;
        panel.add(resultado, gbc);

        return panel;
    }

    private JPanel panelTablero() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                JButton boton = new JButton();
                boton.setPreferredSize(new Dimension(40, 40));
                boton.setMargin(new Insets(0, 0, 0, 0));

                final int fila = i;
                final int columna = j;
                boton.addActionListener(e -> {
                    if (casillas[fila][columna] == 1) {
                        mostrarTodasLasMinas();
                        mostrarResultado("Has perdido");
                        JOptionPane.showMessageDialog(this, "Has perdido.");
                        reiniciarPartida();
                    } else {
                        revelarCasilla(fila, columna);
                        comprobarVictoria();
                    }
                });

                botones[i][j] = boton;

                gbc.gridx = j;
                gbc.gridy = i;
                panel.add(boton, gbc);
            }
        }
        return panel;
    }

    private void iniciarTimer() {
        timer = new Timer(1000, e -> {
            segundos++;
            Tiempo.setText("Tiempo: " + segundos);
        });
        timer.start();
    }

    private void crearCasillas(int[][] casillas, int numMinas) {
        int numBombas = numMinas;

        for (int i = 0; i < casillas.length; i++) {
            for (int j = 0; j < casillas[i].length; j++) {
                casillas[i][j] = 0;
            }
        }

        int bombasColocadas = 0;
        while (bombasColocadas < numBombas) {
            int fila = (int) (Math.random() * casillas.length);
            int columna = (int) (Math.random() * casillas[fila].length);
            if (casillas[fila][columna] == 0) {
                casillas[fila][columna] = 1;
                bombasColocadas++;
            }
        }
    }

    private void reiniciarPartida() {
        // Parar el tiempo
        timer.stop();
        segundos = 0;
        Tiempo.setText("Tiempo: 0");
        // Crear casillas nuevas
        crearCasillas(casillas, numMinas);
        Minas.setText("Minas: " + contarMinas());
        // Resetear los botones y las casillas reveladas
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                botones[i][j].setText("");
                botones[i][j].setEnabled(true);
                botones[i][j].setOpaque(false);
                botones[i][j].setBackground(null);
                reveladas[i][j] = false;
            }
        }

        iniciarTimer();
    }

    private int contarBombas(int fila, int columna) {
        int contador = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int f = fila + i, c = columna + j;
                if (f >= 0 && f < filas && c >= 0 && c < columnas && casillas[f][c] == 1) {
                    contador++;
                }
            }
        }
        // este metodo dice cuantas bombas hay alrededor de la casilla puldada
        // i y j son las filas y columnas y dependiendo de sus valores es una cordendada
        // u otra permitiendo tener 9 coordenadadas
        // entonces si la casilla es 1 aumenta el contador
        return contador;
    }

    private void revelarCasilla(int fila, int columna) {
        if (fila < 0 || fila >= filas || columna < 0 || columna >= columnas)
            return; // fuera de rango, se sale del metodo
        if (reveladas[fila][columna])
            return; // ya abierta
        if (casillas[fila][columna] == 1)
            return; // para no revelar una bomba

        reveladas[fila][columna] = true;
        int bombasAlrededor = contarBombas(fila, columna);
        botones[fila][columna].setText(bombasAlrededor == 0 ? "" : String.valueOf(bombasAlrededor));
        botones[fila][columna].setEnabled(false);
        // fondo distinto para que se note claramente que la casilla ya esta descubierta
        botones[fila][columna].setOpaque(true);
        botones[fila][columna].setBackground(new Color(220, 220, 220));

        if (bombasAlrededor == 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        revelarCasilla(fila + i, columna + j);
                    }
                }
            }
        }
    }

    private void comprobarVictoria() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                // si queda alguna casilla sin bomba y sin revelar, todavia no se ha ganado
                if (casillas[i][j] == 0 && !reveladas[i][j]) {
                    return;
                }
            }
        }

        timer.stop();
        mostrarTodasLasMinas();
        mostrarResultado("Has ganado. Tiempo: " + segundos + " segundos");
        JOptionPane.showMessageDialog(this, "Has ganado. Tiempo: " + segundos + " segundos");
        reiniciarPartida();
    }

    private void mostrarResultado(String texto) {
        resultado.setText("Resultado: " + texto);
    }

    private void mostrarTodasLasMinas() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (casillas[i][j] == 1) {
                    botones[i][j].setText("💣");
                    botones[i][j].setEnabled(false);
                    botones[i][j].setOpaque(true);
                    botones[i][j].setBackground(new Color(255, 150, 150));
                }
            }
        }
    }

    private int contarMinas() {
        int contador = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (casillas[i][j] == 1) {
                    contador++;
                }
            }
        }
        return contador;
    }

    private JMenuBar crearMenu() {

        JMenuBar barra = new JMenuBar();

        JMenu menuPartida = new JMenu("Partida");
        JMenu menuAyuda = new JMenu("Ayuda");
        JMenu menuTamano = new JMenu("Tamaño");

        itemNuevaPartida = new JMenuItem("Nueva partida");
        itemNuevaPartida.addActionListener(e -> reiniciarPartida());

        itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));

        JMenuItem itemAcerca = new JMenuItem("Acerca de");
        itemAcerca.addActionListener(e -> JOptionPane.showMessageDialog(this, "Buscaminas creado por Aitor Chicano"));

        JMenuItem itemInstrucciones = new JMenuItem("Instrucciones");
        itemInstrucciones.addActionListener(e -> {
            Instrucciones instrucciones = new Instrucciones();
            instrucciones.setVisible(true);
            instrucciones.setLocationRelativeTo(this);
        });
        JMenuItem personalizado = new JMenuItem("Personalizado");
        personalizado.addActionListener(e -> {
            String texto = JOptionPane.showInputDialog(this, "Introduce el numero de filas y columnas (5-15)");
            if (texto == null) {
                return; // ha pulsado Cancelar
            }
            String texto2 = JOptionPane.showInputDialog(this, "Introduce el numero de minas");
            if (texto2 == null) {
                return;
            }
            try {
                int tamano = Integer.parseInt(texto.trim());
                int minas = Integer.parseInt(texto2.trim());
                if (tamano < 5 || tamano > 15) {
                    JOptionPane.showMessageDialog(this, "El tamaño tiene que estar entre 5 y 15");
                } else if (minas < 1 || minas >= tamano * tamano) {
                    JOptionPane.showMessageDialog(this,
                            "Las minas tienen que ser al menos 1 y menos que el numero de casillas");
                } else {
                    // solo se guarda si todo es valido
                    numMinas = minas;
                    cambiarTamano(tamano);
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Introduce un numero valido");
            }
        });

        JRadioButtonMenuItem Peque = new JRadioButtonMenuItem("Pequeño");
        JRadioButtonMenuItem Medio = new JRadioButtonMenuItem("Medio");
        Medio.setSelected(true);
        JRadioButtonMenuItem Grande = new JRadioButtonMenuItem("Grande");

        // al elegir un tamaño fijo se vuelve a las 10 minas por defecto
        Peque.addActionListener(e -> {
            numMinas = calcularMinas(5);
            cambiarTamano(5);
        });
        Medio.addActionListener(e -> {
            numMinas = calcularMinas(10);
            cambiarTamano(10);
        });
        Grande.addActionListener(e -> {
            numMinas = calcularMinas(15);
            cambiarTamano(15);
        });

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(Peque);
        grupo.add(Medio);
        grupo.add(Grande);

        menuTamano.add(Peque);
        menuTamano.add(Medio);
        menuTamano.add(Grande);
        menuTamano.add(personalizado);

        menuPartida.add(itemNuevaPartida);
        menuPartida.addSeparator();
        menuPartida.add(itemSalir);

        menuAyuda.add(itemAcerca);
        menuAyuda.addSeparator();
        menuAyuda.add(itemInstrucciones);

        barra.add(menuPartida);
        barra.add(menuTamano);
        barra.add(menuAyuda);

        return barra;

    }

    private void cambiarTamano(int n) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        filas = n;
        columnas = n;

        casillas = new int[n][n];
        botones = new JButton[n][n];
        reveladas = new boolean[n][n];

        remove(tablero);
        tablero = panelTablero();

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(tablero, gbc);

        // Si no se vuelve a ajustar el tamaño de la ventana al nuevo tablero,
        // al pasar a un tablero mas grande que el hueco que ocupaba el anterior
        // GridBagLayout encoge las casillas para que quepan (se "minimizan") y
        // los clics posteriores quedan descuadrados con la casilla pulsada.
        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
        reiniciarPartida();
    }

    private int calcularMinas(int lado) {
        double[] porcentajes = { 0.10, 0.15, 0.20 };
        // como los tableros son cuadrados se le mete solo lado y no fila
        // Y se devuelve un 20% del tablero, para poner las minas
        return Math.max(1, (int) (lado * lado * porcentajes[dificultad.getSelectedIndex()]));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Buscaminas ventana = new Buscaminas();
            ventana.setVisible(true);
        });
    }

}
