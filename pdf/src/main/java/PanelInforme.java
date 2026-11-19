import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Pestaña "Informe PDF": elige deportista y periodo, comprueba los datos y guarda el PDF con JFileChooser. */
public class PanelInforme extends JPanel {

    private final Datos datos;
    private final JComboBox<Deportista> combo = new JComboBox<>();
    private final JSpinner desde = Ventana.spinnerFecha(LocalDate.now().minusMonths(3));
    private final JSpinner hasta = Ventana.spinnerFecha(LocalDate.now());
    private final JTextArea estado = new JTextArea();
    private boolean periodoInicializado = false;

    public PanelInforme(Datos datos) {
        this.datos = datos;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton generar = new JButton("Generar informe PDF...");
        generar.addActionListener(e -> generar());

        JPanel filtros = new JPanel();
        filtros.add(new JLabel("Deportista"));
        filtros.add(combo);
        filtros.add(new JLabel("Desde"));
        filtros.add(desde);
        filtros.add(new JLabel("Hasta"));
        filtros.add(hasta);
        filtros.add(generar);

        estado.setEditable(false);
        estado.setLineWrap(true);
        estado.setWrapStyleWord(true);
        estado.setText("Elige un deportista y un periodo y pulsa «Generar informe PDF...».\n\n"
                + "El informe necesita al menos una sesión y un test de 5 km del deportista dentro del periodo.\n"
                + "Si existe recursos/fotos/<id>.png (o .jpg) se usa como fotografía; si no, se usa una imagen genérica.");

        add(filtros, BorderLayout.NORTH);
        add(new JScrollPane(estado), BorderLayout.CENTER);
    }

    /** Recarga la lista de deportistas (se llama cuando cambian los datos). */
    public void refrescar() {
        Object elegido = combo.getSelectedItem();
        combo.removeAllItems();
        for (Deportista d : datos.deportistas) {
            combo.addItem(d);
        }
        if (elegido != null) {
            combo.setSelectedItem(elegido);
        }
        // la primera vez, el periodo empieza en la primera sesión registrada
        if (!periodoInicializado && !datos.sesiones.isEmpty()) {
            LocalDate primera = datos.sesiones.stream().map(Sesion::fecha).min(Comparator.naturalOrder()).get();
            desde.setValue(Ventana.aDate(primera));
            periodoInicializado = true;
        }
    }

    private void generar() {
        try {
            // 1) obtener los datos y comprobar que hay información suficiente
            Deportista d = (Deportista) combo.getSelectedItem();
            DatosInforme informe = ServicioInforme.preparar(datos, d, Ventana.fecha(desde), Ventana.fecha(hasta));

            // 2) elegir dónde guardar
            JFileChooser selector = new JFileChooser();
            selector.setFileFilter(new FileNameExtensionFilter("Documento PDF (*.pdf)", "pdf"));
            selector.setSelectedFile(new File("informe_" + d.id() + ".pdf"));
            if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File destino = selector.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith(".pdf")) {
                destino = new File(destino.getPath() + ".pdf");
            }
            if (destino.exists() && JOptionPane.showConfirmDialog(this, "El archivo ya existe. ¿Sobrescribirlo?",
                    "Informe PDF", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            // 3) generar
            List<String> avisos = ServicioInforme.generar(informe, destino);
            StringBuilder texto = new StringBuilder("Informe creado en:\n" + destino.getAbsolutePath());
            for (String aviso : avisos) {
                texto.append("\n\nAviso: ").append(aviso);
            }
            estado.setText(texto.toString());
            JOptionPane.showMessageDialog(this, texto.toString(), "Informe PDF", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            // datos insuficientes o intervalo incoherente
            estado.setText("No se puede generar el informe: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Información insuficiente", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            // carpeta sin permisos, archivo abierto en otro programa, fuentes que faltan...
            String mensaje = "No se pudo crear el PDF:\n" + ex.getMessage()
                    + "\n\nComprueba que el archivo no esté abierto en otro programa y que tengas permiso en esa carpeta.";
            estado.setText(mensaje);
            JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
