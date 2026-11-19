import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/** Ventana principal: una pestaña por apartado del enunciado. */
public class Ventana extends JFrame {

    private final Datos datos = new Datos();

    // Tablas
    private final DefaultTableModel modeloDeportistas = modelo("ID", "Nombre", "Edad", "Categoría", "Objetivo", "Marca 5 km (s)");
    private final DefaultTableModel modeloSesiones = modelo("Fecha", "Deportista", "Tipo", "Min", "Km", "FC", "RPE", "Carga (UA)");
    private final DefaultTableModel modeloTests = modelo("Fecha", "Deportista", "Marca 5 km (s)");
    private final JTable tablaDeportistas = new JTable(modeloDeportistas);

    // Combos de deportista (uno por pestaña que lo necesita)
    private final JComboBox<Deportista> comboSesion = new JComboBox<>();
    private final JComboBox<Deportista> comboTest = new JComboBox<>();
    private final JComboBox<Deportista> comboAnalisis = new JComboBox<>();

    // Campos de deportista
    private final JTextField id = new JTextField(10);
    private final JTextField nombre = new JTextField(10);
    private final JTextField edad = new JTextField(10);
    private final JTextField categoria = new JTextField(10);
    private final JTextField objetivo = new JTextField(10);
    private final JTextField marca = new JTextField(10);

    // Campos de sesión
    private final JSpinner fechaSesion = spinnerFecha(LocalDate.now());
    private final JComboBox<String> tipo = new JComboBox<>(new String[]{"Rodaje", "Series", "Tempo", "Fondo", "Recuperación"});
    private final JTextField minutos = new JTextField(10);
    private final JTextField km = new JTextField(10);
    private final JTextField fc = new JTextField(10);
    private final JTextField rpe = new JTextField(10);

    // Campos de test
    private final JSpinner fechaTest = spinnerFecha(LocalDate.now());
    private final JTextField marcaTest = new JTextField(10);

    // Análisis
    private final JSpinner desde = spinnerFecha(LocalDate.now().minusMonths(3));
    private final JSpinner hasta = spinnerFecha(LocalDate.now());
    private final JTextArea resultado = new JTextArea();

    // Segunda parte: informe PDF
    private final PanelInforme panelInforme = new PanelInforme(datos);

    public Ventana() {
        setTitle("Club de atletismo - Análisis de rendimiento");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        datos.cargarPrueba(); // arranca con datos de ejemplo; se pueden borrar desde la pestaña Deportistas

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Deportistas", panelDeportistas());
        pestanas.addTab("Entrenamientos", panelSesiones());
        pestanas.addTab("Pruebas de rendimiento", panelTests());
        pestanas.addTab("Análisis", panelAnalisis());
        pestanas.addTab("Exportación", panelExportacion());
        pestanas.addTab("Informe PDF", panelInforme);
        add(pestanas);

        refrescar();
        // Intervalo por defecto del análisis: desde la primera sesión hasta hoy
        if (!datos.sesiones.isEmpty()) {
            LocalDate primera = datos.sesiones.stream().map(Sesion::fecha).min(Comparator.naturalOrder()).get();
            desde.setValue(aDate(primera));
        }
    }

    // ================================================================ pestañas

    private JPanel panelDeportistas() {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Identificador"));
        form.add(id);
        form.add(new JLabel("Nombre"));
        form.add(nombre);
        form.add(new JLabel("Edad"));
        form.add(edad);
        form.add(new JLabel("Categoría"));
        form.add(categoria);
        form.add(new JLabel("Objetivo de competición"));
        form.add(objetivo);
        form.add(new JLabel("Marca inicial 5 km (segundos)"));
        form.add(marca);

        JButton alta = new JButton("Añadir");
        JButton modificar = new JButton("Modificar");
        JButton eliminar = new JButton("Eliminar");
        JPanel botones = new JPanel();
        botones.add(alta);
        botones.add(modificar);
        botones.add(eliminar);

        // Al elegir una fila de la tabla se cargan sus datos en el formulario
        tablaDeportistas.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaDeportistas.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                id.setText(modeloDeportistas.getValueAt(fila, 0).toString());
                nombre.setText(modeloDeportistas.getValueAt(fila, 1).toString());
                edad.setText(modeloDeportistas.getValueAt(fila, 2).toString());
                categoria.setText(modeloDeportistas.getValueAt(fila, 3).toString());
                objetivo.setText(modeloDeportistas.getValueAt(fila, 4).toString());
                marca.setText(modeloDeportistas.getValueAt(fila, 5).toString());
            }
        });

        alta.addActionListener(e -> {
            try {
                datos.altaDeportista(leerDeportista());
                refrescar();
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        });
        modificar.addActionListener(e -> {
            try {
                datos.modificarDeportista(leerDeportista());
                refrescar();
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        });
        eliminar.addActionListener(e -> {
            int fila = tablaDeportistas.getSelectedRow();
            if (fila < 0) {
                error("Selecciona un deportista de la tabla.");
                return;
            }
            String idEliminar = modeloDeportistas.getValueAt(fila, 0).toString();
            if (JOptionPane.showConfirmDialog(this, "Se eliminará " + idEliminar
                    + " junto con sus sesiones y tests. ¿Continuar?", "Eliminar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    datos.eliminarDeportista(idEliminar);
                    refrescar();
                } catch (IllegalArgumentException ex) {
                    error(ex.getMessage());
                }
            }
        });

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(form, BorderLayout.CENTER);
        arriba.add(botones, BorderLayout.SOUTH);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaDeportistas), BorderLayout.CENTER);
        return panel;
    }

    private JPanel panelSesiones() {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Deportista"));
        form.add(comboSesion);
        form.add(new JLabel("Fecha"));
        form.add(fechaSesion);
        form.add(new JLabel("Tipo de sesión"));
        form.add(tipo);
        form.add(new JLabel("Duración (min)"));
        form.add(minutos);
        form.add(new JLabel("Distancia (km)"));
        form.add(km);
        form.add(new JLabel("Frecuencia cardiaca media"));
        form.add(fc);
        form.add(new JLabel("RPE (1-10)"));
        form.add(rpe);

        JButton registrar = new JButton("Registrar sesión");
        registrar.addActionListener(e -> {
            try {
                Deportista d = (Deportista) comboSesion.getSelectedItem();
                if (d == null) {
                    throw new IllegalArgumentException("Primero hay que dar de alta un deportista.");
                }
                datos.registrarSesion(new Sesion(fecha(fechaSesion), d.id(), (String) tipo.getSelectedItem(),
                        entero(minutos, "Duración"), decimal(km, "Distancia"), entero(fc, "Frecuencia cardiaca"),
                        entero(rpe, "RPE")));
                minutos.setText("");
                km.setText("");
                fc.setText("");
                rpe.setText("");
                refrescar();
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        });

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(form, BorderLayout.CENTER);
        arriba.add(registrar, BorderLayout.SOUTH);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTable(modeloSesiones)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel panelTests() {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Deportista"));
        form.add(comboTest);
        form.add(new JLabel("Fecha del test"));
        form.add(fechaTest);
        form.add(new JLabel("Marca (segundos)"));
        form.add(marcaTest);

        JButton registrar = new JButton("Registrar test");
        registrar.addActionListener(e -> {
            try {
                Deportista d = (Deportista) comboTest.getSelectedItem();
                if (d == null) {
                    throw new IllegalArgumentException("Primero hay que dar de alta un deportista.");
                }
                datos.registrarTest(new Test5k(fecha(fechaTest), d.id(), entero(marcaTest, "Marca")));
                marcaTest.setText("");
                refrescar();
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        });

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(form, BorderLayout.CENTER);
        arriba.add(registrar, BorderLayout.SOUTH);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTable(modeloTests)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel panelAnalisis() {
        JButton calcular = new JButton("Calcular");
        calcular.addActionListener(e -> calcular());

        JPanel filtros = new JPanel();
        filtros.add(new JLabel("Deportista"));
        filtros.add(comboAnalisis);
        filtros.add(new JLabel("Desde"));
        filtros.add(desde);
        filtros.add(new JLabel("Hasta"));
        filtros.add(hasta);
        filtros.add(calcular);

        resultado.setEditable(false);
        resultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(filtros, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultado), BorderLayout.CENTER);
        return panel;
    }

    private JPanel panelExportacion() {
        JButton exportar = new JButton("Exportar a Excel...");
        exportar.addActionListener(e -> exportar());
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panel.add(exportar);
        return panel;
    }

    // ================================================================ acciones

    private void calcular() {
        Deportista d = (Deportista) comboAnalisis.getSelectedItem();
        if (d == null) {
            error("No hay deportistas.");
            return;
        }
        try {
            LocalDate fechaDesde = fecha(desde);
            LocalDate fechaHasta = fecha(hasta);
            List<Sesion> sesiones = datos.sesionesDe(d.id(), fechaDesde, fechaHasta); // lanza error si el intervalo es incoherente
            List<Test5k> tests = datos.testsDe(d.id(), fechaDesde, fechaHasta);
            List<Calculos.Semana> semanas = Calculos.semanas(sesiones);

            StringBuilder t = new StringBuilder();
            t.append("CARGA SEMANAL de ").append(d.nombre()).append("\n");
            t.append(String.format("%-4s %-10s %6s %6s %10s  %-19s %-14s %s%n", "Sem", "Lunes", "Km", "Min", "Carga(UA)",
                    "Media móvil 3 sem.", "Monotonía", "Tensión"));
            int n = 1;
            for (Calculos.Semana s : semanas) {
                t.append(String.format("%-4d %s %6.1f %6d %10.0f  %-19s %-14s %s%n", n++, s.lunes(), s.km(), s.minutos(),
                        s.carga(), f(s.mediaMovil(), 1, "datos insuficientes"), f(s.monotonia(), 2, "n/d (desv. 0)"),
                        f(s.tension(), 0, "n/d (desv. 0)")));
            }

            double[] cargas = Calculos.cargas(semanas);
            double[] dist = Calculos.distancias(sesiones);
            t.append("\nESTADÍSTICA DESCRIPTIVA\n");
            t.append(estadistica("Carga semanal (UA)", cargas));
            t.append(estadistica("Distancia por sesión (km)", dist));

            t.append("\nREGRESIÓN LINEAL DE LAS MARCAS (y = a + b·x, x = semanas desde el primer test)\n");
            if (tests.size() >= 2) {
                double[] x = Calculos.semanasDesdePrimero(tests);
                double[] reg = Calculos.regresion(x, Calculos.marcas(tests));
                t.append(String.format("a = %s s   b = %s s/semana   R² = %s%n", f(reg[0], 2, "n/d"), f(reg[1], 3, "n/d"), f(reg[2], 4, "n/d")));
                if (!Double.isNaN(reg[1])) {
                    t.append(reg[1] < 0 ? "b < 0: la marca tiende a disminuir (mejora)\n"
                            : reg[1] > 0 ? "b > 0: la marca tiende a aumentar (empeora)\n" : "b = 0: marca estable\n");
                    t.append(String.format("Marca estimada para la semana siguiente: %.1f s (extrapolación, no una garantía)%n",
                            reg[0] + reg[1] * (x[x.length - 1] + 1)));
                }
            } else {
                t.append("Datos insuficientes: hacen falta al menos 2 tests en el intervalo.\n");
            }

            double[][] pares = Calculos.cargaYMarca(semanas, tests);
            t.append("\nCORRELACIÓN DE PEARSON (carga semanal vs marca del test)\n");
            t.append("r = ").append(f(Calculos.pearson(pares[0], pares[1]), 3, "n/d (hacen falta 3 tests con carga conocida)"))
                    .append("   (correlación no implica causalidad)\n");

            t.append("\nCOMPARATIVA DE TODOS LOS DEPORTISTAS EN EL INTERVALO\n");
            t.append(String.format("%-16s %-14s %-18s %-18s %-18s%n", "Deportista", "Mejor marca", "Evolución vs 1.º", "Km semanales", "Carga semanal"));
            for (Deportista otro : datos.deportistas) {
                List<Calculos.Semana> sem = Calculos.semanas(datos.sesionesDe(otro.id(), fechaDesde, fechaHasta));
                List<Test5k> tst = datos.testsDe(otro.id(), fechaDesde, fechaHasta);
                t.append(String.format("%-16s %-14s %-18s %-18s %-18s%n", otro.nombre(),
                        f(Calculos.mejorMarca(tst), 0, "n/d") + " s", f(Calculos.evolucion(tst), 0, "n/d") + " s",
                        f(Calculos.kmSemanalesMedios(sem), 1, "n/d") + " km", f(Calculos.cargaSemanalMedia(sem), 0, "n/d") + " UA"));
            }
            resultado.setText(t.toString());
            resultado.setCaretPosition(0);
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private String estadistica(String nombre, double[] x) {
        return String.format("%-26s media=%s mediana=%s mín=%s máx=%s desv=%s CV=%s%%%n", nombre, f(Calculos.media(x), 2, "n/d"),
                f(Calculos.mediana(x), 2, "n/d"), f(Calculos.minimo(x), 2, "n/d"), f(Calculos.maximo(x), 2, "n/d"),
                f(Calculos.desviacion(x), 2, "n/d"), f(Calculos.cv(x), 1, "n/d"));
    }

    private void exportar() {
        // JFileChooser: el usuario elige dónde guardar el archivo
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter("Libro de Excel (*.xlsx)", "xlsx"));
        selector.setSelectedFile(new File("informe_club.xlsx"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".xlsx")) {
            destino = new File(destino.getPath() + ".xlsx");
        }
        if (destino.exists() && JOptionPane.showConfirmDialog(this, "El archivo ya existe. ¿Sobrescribirlo?",
                "Exportar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            InformeExcel.exportar(datos, destino);
            JOptionPane.showMessageDialog(this, "Informe guardado en:\n" + destino.getAbsolutePath());
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        } catch (IOException ex) {
            // Típico: el archivo está abierto en Excel o no hay permisos en esa carpeta
            error("No se pudo guardar el archivo (¿está abierto en Excel?):\n" + ex.getMessage());
        }
    }

    // ================================================================ auxiliares

    /** Vuelve a cargar tablas y combos desde los datos. */
    private void refrescar() {
        panelInforme.refrescar();
        modeloDeportistas.setRowCount(0);
        for (Deportista d : datos.deportistas) {
            modeloDeportistas.addRow(new Object[]{d.id(), d.nombre(), d.edad(), d.categoria(), d.objetivo(), d.marca5k()});
        }
        modeloSesiones.setRowCount(0);
        List<Sesion> ordenadas = new ArrayList<>(datos.sesiones);
        ordenadas.sort(Comparator.comparing(Sesion::fecha));
        for (Sesion s : ordenadas) {
            modeloSesiones.addRow(new Object[]{s.fecha(), datos.buscar(s.idDeportista()).nombre(), s.tipo(), s.minutos(),
                    s.km(), s.fc(), s.rpe(), s.carga()});
        }
        modeloTests.setRowCount(0);
        List<Test5k> testsOrdenados = new ArrayList<>(datos.tests);
        testsOrdenados.sort(Comparator.comparing(Test5k::fecha));
        for (Test5k t : testsOrdenados) {
            modeloTests.addRow(new Object[]{t.fecha(), datos.buscar(t.idDeportista()).nombre(), t.segundos()});
        }
        for (JComboBox<Deportista> combo : List.of(comboSesion, comboTest, comboAnalisis)) {
            Object elegido = combo.getSelectedItem();
            combo.removeAllItems();
            for (Deportista d : datos.deportistas) {
                combo.addItem(d);
            }
            if (elegido != null) {
                combo.setSelectedItem(elegido);
            }
        }
    }

    private Deportista leerDeportista() {
        return new Deportista(id.getText().trim(), nombre.getText().trim(), entero(edad, "Edad"),
                categoria.getText().trim(), objetivo.getText().trim(), entero(marca, "Marca inicial"));
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Lee un entero del campo; si no lo es, avisa con un mensaje claro (nada de errores silenciosos). */
    private static int entero(JTextField campo, String nombre) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + nombre + "' debe ser un número entero.");
        }
    }

    private static double decimal(JTextField campo, String nombre) {
        try {
            return Double.parseDouble(campo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + nombre + "' debe ser un número.");
        }
    }

    /** Número con decimales o el texto indicado si no está definido (NaN). */
    private static String f(double valor, int decimales, String siNoDefinido) {
        return Double.isNaN(valor) ? siNoDefinido : String.format("%." + decimales + "f", valor);
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false; // las tablas solo muestran datos
            }
        };
    }

    static JSpinner spinnerFecha(LocalDate inicial) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(aDate(inicial), null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }

    static Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    static LocalDate fecha(JSpinner spinner) {
        return ((Date) spinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ventana().setVisible(true));
    }
}
