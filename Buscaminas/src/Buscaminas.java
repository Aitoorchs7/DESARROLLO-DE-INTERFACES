import java.awt.*;
import javax.swing.*;

public class Buscaminas extends javax.swing.JFrame {

    // Componentes del panel superior que necesitaremos tocar despues
    private JLabel Puntuacion;
    private JLabel Tiempo;
    private JButton reiniciar;

    // Timer de la partida
    private Timer timer;
    private int segundos = 0;

    int[][] casillas = new int[10][10];
    JButton [][] botones = new JButton[10][10];

    public Buscaminas(){
        setTitle("Buscaminas de Aitor");
        setSize(1200, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //Contenedor de arriba
        setLayout(new GridBagLayout());
        GridBagConstraints gbcPrincipal = new GridBagConstraints();

        gbcPrincipal.gridx = 0;
        gbcPrincipal.weightx = 1;
        gbcPrincipal.insets = new Insets(15, 10, 15, 10);

        gbcPrincipal.gridy = 0;
        gbcPrincipal.weighty = 0;
        gbcPrincipal.fill = GridBagConstraints.HORIZONTAL;
        JPanel superior = panelSuperior();
        add(superior, gbcPrincipal);

        gbcPrincipal.gridy = 1;
        gbcPrincipal.weighty = 1;
        gbcPrincipal.fill = GridBagConstraints.BOTH;
        JPanel inferior = panelInferior();
        add(inferior, gbcPrincipal);

        crearCasillas(casillas);
        iniciarTimer();
    }

    private JPanel panelSuperior(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        // Fila 0: titulo, ocupando las 3 columnas de la fila de abajo
        JLabel Titulo = new JLabel("Buscaminas");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(Titulo, gbc);

        // Fila 1: puntuacion | boton reiniciar | tiempo
        gbc.gridwidth = 1;
        gbc.gridy = 1;

        Puntuacion = new JLabel("Puntuacion: ");
        gbc.gridx = 0;
        panel.add(Puntuacion, gbc);

        reiniciar = new JButton("Nueva partida");
        gbc.gridx = 1;
        panel.add(reiniciar, gbc);
        // TODO: reiniciar.addActionListener(...) cuando hagamos la logica de nueva partida

        Tiempo = new JLabel("Tiempo: ");
        gbc.gridx = 2;
        panel.add(Tiempo, gbc);
    
        return panel;
    }
    private JPanel panelInferior(){
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 10; j++) {
            JButton boton = new JButton();
            boton.setPreferredSize(new Dimension(40, 40));

            final int fila = i;
            final int columna = j;
            boton.addActionListener(e -> {
                // TODO: logica al pulsar (fila, columna)
            });

            botones[i][j] = boton;

            gbc.gridx = j;
            gbc.gridy = i;
            panel.add(boton, gbc);
        }
    }
    return panel;

    }

    private void iniciarTimer(){
        timer = new Timer(1000, e -> {
            segundos++;
            Tiempo.setText("Tiempo: " + segundos);
        });
        timer.start();
    }

    private void crearCasillas(int[][] casillas){
        for(int i = 0; i < casillas.length; i++){
            for(int j = 0; j < casillas[i].length; j++){
                casillas[i][j] = (Math.random() < 0.1) ? 1 : 0;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Buscaminas ventana = new Buscaminas();
            ventana.setVisible(true);
        });
    }
}
