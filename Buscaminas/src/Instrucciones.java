import javax.swing.*;
import java.awt.*;

public class Instrucciones extends JFrame {

    public Instrucciones() {
        setTitle("Instrucciones");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel titulo = new JLabel("Instrucciones del Buscaminas");
        JTextArea texto = new JTextArea("Objetivo del juego: \n"
                + "- El objetivo del juego es despejar el tablero de minas.\n"
                + "- Para despejar el tablero, debemos hacer clic en las casillas. \n"
                + "- Si hacemos clic en una casilla con una mina, perdemos.\n"
                + "- Si hacemos clic en una casilla sin una mina, se nos indicara cuantas minas hay alrededor de esa casilla. \n"
                + "- Si hacemos clic en todas las casillas sin minas, ganamos.\n");
        texto.setEditable(false);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(texto);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelPrincipal.add(titulo, gbc);

        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panelPrincipal.add(scroll, gbc);

        add(panelPrincipal);
        setVisible(true);

    }

}
