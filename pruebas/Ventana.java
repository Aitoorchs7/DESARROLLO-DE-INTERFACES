/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author alvarovs86
 */
public class Ventana extends javax.swing.JFrame {

    private JLabel etiquetaImagen;
    private Image imagenOriginal;

    /**
     * Creates new form Ventana
     */
    public Ventana() {

        setTitle("Imagen redimensionable");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Cargamos la imagen desde el proyecto
        ImageIcon icono = new ImageIcon("pruebas/asterix.png");

        imagenOriginal = icono.getImage();

        etiquetaImagen = new JLabel();
        etiquetaImagen.setHorizontalAlignment(JLabel.CENTER);
        etiquetaImagen.setVerticalAlignment(JLabel.CENTER);
        etiquetaImagen.setMinimumSize(new Dimension(1, 1));

        // Panel con GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.weightx = 0.3;
        gbc.weighty = 1;

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(etiquetaImagen, gbc);

        // Cuando cambia el tamaño del JFrame
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                redimensionarImagen();
            }
        });

        // Primera actualización
        redimensionarImagen();

        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JButton("Botón"), gbc);

        // Creamos el contenedor de pestañas
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        JTabbedPane pestañas = new JTabbedPane();

        // Primera pestaña
        JPanel panelInicio = new JPanel();
        panelInicio.add(new JLabel("Bienvenido a la aplicación"));

        // Segunda pestaña
        JPanel panelAlumnos = new JPanel();
        panelAlumnos.add(new JLabel("Gestión de alumnos"));
        panelAlumnos.add(new JButton("Añadir alumno"));

        // Tercera pestaña
        JPanel panelConfiguracion = new JPanel();
        panelConfiguracion.add(new JCheckBox("Activar notificaciones"));

        // Añadimos los paneles al JTabbedPane
        pestañas.addTab("Inicio", panelInicio);
        pestañas.addTab("Alumnos", panelAlumnos);
        pestañas.addTab("Configuración", panelConfiguracion);

        gbc.weightx = 0.7;
        gbc.gridheight = 2;
        panel.add(pestañas, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        gbc.weighty = 0;

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JSpinner spinner = new JSpinner(
                new SpinnerNumberModel(1, 1, 10, 1));
        panel.add(spinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel contenido = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();

        gbc2.fill = GridBagConstraints.NONE;
        gbc2.anchor = GridBagConstraints.CENTER;

        // Añadimos 40 botones en dos columnas
        for (int i = 1; i <= 40; i++) {
            JButton boton = new JButton("Botón " + i);

            // Tamaño fijo de cada botón
            boton.setPreferredSize(new Dimension(150, 35));
            boton.setMinimumSize(new Dimension(150, 35));

            gbc2.gridx = (i - 1) % 2;
            gbc2.gridy = (i - 1) / 2;

            contenido.add(boton, gbc2);
        }

        // Zona desplazable
        JScrollPane scroll = new JScrollPane(contenido);
        // scroll.setPreferredSize(new Dimension(400, 280));

        panel.add(scroll, gbc);
        add(panel);

        // initComponents();
    }

    private void redimensionarImagen() {

        /*
         * int ancho = etiquetaImagen.getWidth();
         * int alto = etiquetaImagen.getHeight();
         *
         * if (ancho <= 0 || alto <= 0) {
         * return;
         * }
         *
         * Image imagenRedimensionada = imagenOriginal.getScaledInstance(
         * ancho,
         * alto,
         * Image.SCALE_SMOOTH
         * );
         *
         * etiquetaImagen.setIcon(
         * new ImageIcon(imagenRedimensionada)
         * );
         */

        int anchoDisponible = etiquetaImagen.getWidth();
        int altoDisponible = etiquetaImagen.getHeight();

        if (anchoDisponible <= 0 || altoDisponible <= 0) {
            return;
        }

        int anchoOriginal = imagenOriginal.getWidth(null);
        int altoOriginal = imagenOriginal.getHeight(null);

        double proporcion = Math.min(
                (double) anchoDisponible / anchoOriginal,
                (double) altoDisponible / altoOriginal);

        int nuevoAncho = (int) (anchoOriginal * proporcion);
        int nuevoAlto = (int) (altoOriginal * proporcion);

        Image imagenRedimensionada = imagenOriginal.getScaledInstance(
                nuevoAncho,
                nuevoAlto,
                Image.SCALE_SMOOTH);

        etiquetaImagen.setIcon(
                new ImageIcon(imagenRedimensionada));

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Ventana().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
