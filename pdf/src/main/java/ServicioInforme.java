import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Coordina el informe: obtiene los datos (Datos + Calculos), comprueba que hay información suficiente
 * y le dice a GeneradorPDF qué dibujar y en qué orden.
 */
public class ServicioInforme {

    public static final String CLUB = "Club de Atletismo Alcarria";   // club ficticio
    private static final File CARPETA_FUENTES = new File("recursos/fuentes");
    private static final File LOGO = new File("recursos/logo.png");
    private static final File FOTO_GENERICA = new File("recursos/generica.png");
    private static final File CARPETA_FOTOS = new File("recursos/fotos");

    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ================================================================== 1. obtener y comprobar los datos

    /**
     * Reúne los datos del deportista en el periodo.
     * Lanza IllegalArgumentException (con un mensaje claro) si no hay información suficiente.
     */
    public static DatosInforme preparar(Datos datos, Deportista d, LocalDate desde, LocalDate hasta) {
        if (d == null) {
            throw new IllegalArgumentException("Selecciona un deportista.");
        }
        List<Sesion> sesiones = datos.sesionesDe(d.id(), desde, hasta); // lanza error si el intervalo es incoherente
        List<Test5k> tests = datos.testsDe(d.id(), desde, hasta);
        if (sesiones.isEmpty()) {
            throw new IllegalArgumentException(d.nombre() + " no tiene sesiones de entrenamiento entre "
                    + desde.format(FECHA) + " y " + hasta.format(FECHA) + ": no hay información suficiente.");
        }
        if (tests.isEmpty()) {
            throw new IllegalArgumentException(d.nombre() + " no tiene ningún test de 5 km en ese periodo: "
                    + "no hay información suficiente para el apartado de rendimiento.");
        }
        return new DatosInforme(d, desde, hasta, sesiones, tests);
    }

    // ================================================================== 2. generar el PDF

    /**
     * Crea el PDF en el archivo indicado. Devuelve los avisos no graves (p. ej. «sin fotografía»).
     * Lanza IOException si no se puede escribir o faltan las fuentes.
     */
    public static List<String> generar(DatosInforme d, File destino) throws IOException {
        File carpeta = destino.getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) { // como en la teoría: se crea si no existe
            throw new IOException("No se pudo crear la carpeta " + carpeta.getPath());
        }

        List<File> temporales = new ArrayList<>(); // PNG de los gráficos: se borran al terminar
        try (GeneradorPDF pdf = new GeneradorPDF(CARPETA_FUENTES, LOGO,
                CLUB + "  |  Informe de rendimiento  |  " + d.deportista.nombre(),
                "Indicadores didácticos de análisis de datos: no son diagnósticos médicos ni prescripciones de entrenamiento.")) {

            portada(pdf, d);
            pdf.nuevaPagina();
            resumen(pdf, d);
            entrenamiento(pdf, d, temporales);
            rendimiento(pdf, d, temporales);
            interpretacion(pdf, d);
            pdf.guardar(destino);
            return pdf.avisos;
        } finally {
            for (File f : temporales) {
                f.delete();
            }
        }
    }

    // ================================================================== secciones

    private static void portada(GeneradorPDF pdf, DatosInforme d) throws IOException {
        pdf.nuevaPagina();
        float ancho = pdf.ancho();
        float alto = pdf.alto();

        // Banda superior con logotipo y nombre del club
        pdf.rectangulo(0, alto - 150, ancho, 150, GeneradorPDF.AZUL);
        if (pdf.logo() != null) {
            pdf.dibujarImagen(pdf.logo(), GeneradorPDF.MARGEN, alto - 125, 100, 100);
        }
        pdf.texto(CLUB, GeneradorPDF.MARGEN + 120, alto - 78, pdf.negrita(), 24, Color.WHITE);
        pdf.texto("Área de análisis de rendimiento deportivo", GeneradorPDF.MARGEN + 120, alto - 100,
                pdf.normal(), 12, GeneradorPDF.NARANJA);

        // Título y deportista
        pdf.espacio(134);
        pdf.texto("Informe de rendimiento", GeneradorPDF.MARGEN, pdf.y() - 32, pdf.negrita(), 32, GeneradorPDF.AZUL);
        pdf.espacio(50);
        pdf.texto(d.deportista.nombre(), GeneradorPDF.MARGEN, pdf.y() - 24, pdf.normal(), 24, Color.BLACK);
        pdf.espacio(38);
        pdf.linea(GeneradorPDF.MARGEN, pdf.y(), GeneradorPDF.MARGEN + 90, pdf.y(), GeneradorPDF.NARANJA, 3);
        pdf.espacio(20);

        // Datos personalizados
        pdf.etiquetaValor("Categoría:", d.deportista.categoria(), GeneradorPDF.MARGEN, 13);
        pdf.etiquetaValor("Objetivo de competición:", d.deportista.objetivo(), GeneradorPDF.MARGEN, 13);
        pdf.etiquetaValor("Periodo analizado:", d.desde.format(FECHA) + " - " + d.hasta.format(FECHA), GeneradorPDF.MARGEN, 13);
        pdf.etiquetaValor("Fecha de generación:", LocalDate.now().format(FECHA), GeneradorPDF.MARGEN, 13);
        pdf.espacio(16);

        // Fotografía del deportista (recursos/fotos/<id>.png o .jpg) o imagen genérica
        File foto = buscarFoto(d.deportista.id());
        boolean esGenerica = foto == null;
        if (esGenerica) {
            pdf.avisos.add("No hay fotografía individual de " + d.deportista.nombre() + ": se usa una imagen genérica.");
            foto = FOTO_GENERICA;
        }
        PDImageXObject imagen = pdf.cargarImagen(foto, "la imagen de la portada");
        if (imagen != null) {
            pdf.imagenCentrada(imagen, 330, 250);
            pdf.pieDeImagen(esGenerica ? "Imagen ilustrativa de atletismo" : "Fotografía de " + d.deportista.nombre());
        }
    }

    private static void resumen(GeneradorPDF pdf, DatosInforme d) throws IOException {
        pdf.seccion("1. Resumen del periodo");

        String texto = String.format(ES, "Entre el %s y el %s, %s realizó %d sesiones de entrenamiento en %d semanas. "
                        + "Acumuló %s km en %s y una carga total de %s UA (media semanal: %s UA). "
                        + "Su mejor marca de 5 km en el periodo fue %s (%s s).",
                d.desde.format(FECHA), d.hasta.format(FECHA), d.deportista.nombre(), d.numSesiones, d.semanas.size(),
                num(d.km, 1), horas(d.minutos), num(d.carga, 0), num(d.cargaSemanalMedia, 0),
                mmss(d.mejorMarca), num(d.mejorMarca, 0));
        pdf.parrafo(texto);
        pdf.parrafo(String.format(ES, "Su objetivo de competición es «%s» y su marca inicial registrada en 5 km era %s.",
                d.deportista.objetivo(), mmss(d.deportista.marca5k())));
        pdf.espacio(4);

        pdf.tarjetas(new String[]{"Sesiones", "Kilómetros", "Tiempo total", "Carga acumulada", "Mejor 5 km"},
                new String[]{String.valueOf(d.numSesiones), num(d.km, 1), horas(d.minutos), num(d.carga, 0) + " UA", mmss(d.mejorMarca)});

        pdf.subtitulo("Indicadores del periodo");
        float a = pdf.anchoUtil();
        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"Kilómetros semanales medios", num(d.kmSemanalesMedios, 1) + " km"});
        filas.add(new String[]{"Carga semanal media", num(d.cargaSemanalMedia, 0) + " UA"});
        filas.add(new String[]{"Variabilidad de la carga semanal (coeficiente de variación)", num(d.cvCarga, 1) + " %"});
        filas.add(new String[]{"Variación de la carga (2.ª mitad frente a 1.ª)", signo(d.variacionCarga, 1) + " %"});
        filas.add(new String[]{"Evolución de la marca (1.er test - último test)", signo(d.evolucion, 0) + " s"});
        pdf.tabla(new String[]{"Indicador", "Valor"}, new float[]{a * 0.72f, a * 0.28f}, new boolean[]{false, true}, filas, false);
    }

    private static void entrenamiento(GeneradorPDF pdf, DatosInforme d, List<File> temporales) throws IOException {
        pdf.seccion("2. Entrenamiento");
        pdf.parrafo("La carga de cada sesión se calcula como duración (min) x RPE (esfuerzo percibido, 1 a 10) y se expresa "
                + "en unidades arbitrarias (UA). La carga semanal suma las sesiones de cada semana, de lunes a domingo; "
                + "la media móvil promedia las últimas tres semanas.");

        pdf.subtitulo("Resumen semanal");
        float a = pdf.anchoUtil();
        float[] anchos = {a * 0.10f, a * 0.17f, a * 0.12f, a * 0.14f, a * 0.14f, a * 0.15f, a * 0.18f};
        List<String[]> filas = new ArrayList<>();
        int n = 1;
        for (Calculos.Semana s : d.semanas) {
            filas.add(new String[]{String.valueOf(n++), s.lunes().format(FECHA), String.valueOf(s.sesiones()),
                    num(s.km(), 1), String.valueOf(s.minutos()), num(s.carga(), 0),
                    Double.isNaN(s.mediaMovil()) ? "insuf." : num(s.mediaMovil(), 0)});
        }
        filas.add(new String[]{"Total", "", String.valueOf(d.numSesiones), num(d.km, 1), String.valueOf(d.minutos),
                num(d.carga, 0), ""});
        pdf.tabla(new String[]{"Semana", "Lunes", "Sesiones", "Km", "Minutos", "Carga (UA)", "Media móvil"},
                anchos, new boolean[]{false, false, true, true, true, true, true}, filas, true);
        pdf.parrafo("Media móvil = promedio de la semana y las dos anteriores. insuf. = datos insuficientes: hacen falta tres semanas completas.", pdf.cursiva(), 8.5f, GeneradorPDF.GRIS);

        File grafico = GeneradorGraficos.cargaSemanal(d.semanas);
        temporales.add(grafico);
        PDImageXObject imagen = pdf.cargarImagen(grafico, "el gráfico de carga semanal");
        if (imagen != null) {
            pdf.imagenCentrada(imagen, a, 250);
            pdf.pieDeImagen("Figura 1. Carga semanal en unidades arbitrarias (UA).");
        }
    }

    private static void rendimiento(GeneradorPDF pdf, DatosInforme d, List<File> temporales) throws IOException {
        pdf.seccion("3. Rendimiento en 5 km");
        pdf.parrafo("Histórico de tests de 5 km. La diferencia se mide respecto al primer test del periodo: "
                + "un valor positivo significa que la marca es más rápida (menos segundos).");

        float a = pdf.anchoUtil();
        float[] anchos = {a * 0.17f, a * 0.15f, a * 0.15f, a * 0.15f, a * 0.20f, a * 0.18f};
        List<String[]> filas = new ArrayList<>();
        for (int i = 0; i < d.tests.size(); i++) {
            double ajustada = d.hayRecta() ? d.regresion[0] + d.regresion[1] * d.x[i] : Double.NaN;
            filas.add(new String[]{d.tests.get(i).fecha().format(FECHA), num(d.x[i], 1),
                    String.valueOf(d.tests.get(i).segundos()), mmss(d.tests.get(i).segundos()),
                    signo(d.tests.get(0).segundos() - d.tests.get(i).segundos(), 0) + " s", num(ajustada, 1)});
        }
        pdf.tabla(new String[]{"Fecha", "Semana (x)", "Marca (s)", "Marca (mm:ss)", "Dif. vs. 1.er test", "Recta (s)"},
                anchos, new boolean[]{false, true, true, true, true, true}, filas, false);

        if (d.hayRecta()) {
            pdf.parrafo(String.format(ES, "Recta de regresión por mínimos cuadrados: y = %s + %s · x (x = semanas desde el primer test; "
                            + "y = segundos). R² = %s. Estimación para la semana siguiente: %s s (extrapolación, no una garantía).",
                    num(d.regresion[0], 1), num(d.regresion[1], 2), num(d.regresion[2], 3), num(d.prediccion, 1)));
        } else {
            pdf.parrafo("Con un solo test (o varios en la misma fecha) no se puede ajustar una recta: datos insuficientes.");
        }

        File grafico = GeneradorGraficos.evolucionMarca(d);
        temporales.add(grafico);
        PDImageXObject imagen = pdf.cargarImagen(grafico, "el gráfico de evolución de la marca");
        if (imagen != null) {
            pdf.imagenCentrada(imagen, a, 250);
            pdf.pieDeImagen("Figura 2. Marcas reales de los tests y recta de regresión ajustada.");
        }
    }

    private static void interpretacion(GeneradorPDF pdf, DatosInforme d) throws IOException {
        pdf.seccion("4. Interpretación de los resultados");
        for (String p : textoInterpretacion(d)) {
            pdf.parrafo(p);
        }
        pdf.subtitulo("Limitaciones de los indicadores");
        pdf.parrafo("La carga se basa en el RPE, una valoración subjetiva del propio deportista. La media móvil, la monotonía, "
                + "la regresión y la correlación son modelos simplificados con fines didácticos: no son diagnósticos médicos "
                + "ni recomendaciones individualizadas de entrenamiento.");
        pdf.parrafo("Con pocos tests, la recta y el coeficiente de correlación son poco fiables, y una marca concreta también "
                + "depende del día, el clima o el circuito. La predicción es una extrapolación y no garantiza el rendimiento futuro.");
    }

    // ================================================================== textos automáticos (prudentes)

    /** Comentario automático según reglas: mejora, empeora, estable o datos insuficientes. Sin afirmaciones causales. */
    public static List<String> textoInterpretacion(DatosInforme d) {
        List<String> p = new ArrayList<>();

        // --- Tendencia de la marca
        if (Double.isNaN(d.evolucion)) {
            p.add("Solo hay " + d.tests.size() + " test en el periodo, así que no se puede valorar la evolución de la marca "
                    + "(hacen falta al menos 2).");
        } else {
            String tendencia;
            if (d.evolucion > 2) {
                tendencia = String.format(ES, "La marca ha mejorado %.0f s respecto al primer test (de %s a %s).",
                        d.evolucion, mmss(d.tests.get(0).segundos()), mmss(d.tests.get(d.tests.size() - 1).segundos()));
            } else if (d.evolucion < -2) {
                tendencia = String.format(ES, "La marca ha empeorado %.0f s respecto al primer test (de %s a %s).",
                        -d.evolucion, mmss(d.tests.get(0).segundos()), mmss(d.tests.get(d.tests.size() - 1).segundos()));
            } else {
                tendencia = "La marca se mantiene estable: la diferencia respecto al primer test es de 2 s o menos.";
            }
            if (d.hayRecta()) {
                double r2 = d.regresion[2];
                String ajuste = Double.isNaN(r2) ? "no es valorable" : r2 >= 0.8 ? "describe bien los resultados"
                        : r2 >= 0.5 ? "describe de forma moderada los resultados" : "describe poco los resultados";
                tendencia += String.format(ES, " La pendiente de la recta es de %s s/semana (%s) y la recta %s (R² = %s).",
                        num(d.regresion[1], 2), d.regresion[1] < 0 ? "la marca tiende a disminuir" : d.regresion[1] > 0
                                ? "la marca tiende a aumentar" : "sin tendencia", ajuste, num(r2, 3));
            }
            p.add(tendencia);
        }

        // --- Carga y variabilidad
        String carga = String.format(ES, "La carga semanal media fue de %s UA.", num(d.cargaSemanalMedia, 0));
        if (!Double.isNaN(d.cvCarga)) {
            String nivel = d.cvCarga < 15 ? "baja" : d.cvCarga < 30 ? "moderada" : "alta";
            carga += String.format(ES, " La variabilidad entre semanas es %s (coeficiente de variación del %s %%).", nivel, num(d.cvCarga, 1));
        }
        if (!Double.isNaN(d.variacionCarga)) {
            carga += String.format(ES, " La carga media de la segunda mitad del periodo fue un %s %% %s que la de la primera.",
                    num(Math.abs(d.variacionCarga), 1), d.variacionCarga >= 0 ? "mayor" : "menor");
        }
        p.add(carga);

        // --- Relación carga - marca
        if (Double.isNaN(d.pearson)) {
            p.add("No hay datos suficientes para estudiar la relación entre carga semanal y marca (hacen falta al menos "
                    + Calculos.MIN_PARES_PEARSON + " tests con carga conocida).");
        } else {
            double abs = Math.abs(d.pearson);
            String fuerza = abs < 0.3 ? "débil o sin asociación lineal clara" : abs < 0.7 ? "moderada" : "fuerte";
            p.add(String.format(ES, "La correlación entre la carga semanal y la marca es %s (r = %s, %d tests). "
                            + "Correlación no implica causalidad: con estos datos no se puede concluir que la carga explique por sí sola "
                            + "la evolución de la marca.",
                    fuerza, num(d.pearson, 2), d.paresPearson));
        }
        return p;
    }

    // ================================================================== auxiliares

    /** Busca recursos/fotos/<id>.png|jpg|jpeg. Devuelve null si el deportista no tiene fotografía. */
    private static File buscarFoto(String id) {
        for (String extension : new String[]{".png", ".jpg", ".jpeg"}) {
            File f = new File(CARPETA_FOTOS, id + extension);
            if (f.isFile()) {
                return f;
            }
        }
        return null;
    }

    private static String num(double v, int decimales) {
        return Double.isNaN(v) ? "n/d" : String.format(ES, "%,." + decimales + "f", v);
    }

    private static String signo(double v, int decimales) {
        return Double.isNaN(v) ? "n/d" : String.format(ES, "%+." + decimales + "f", v);
    }

    private static String mmss(double segundos) {
        if (Double.isNaN(segundos)) {
            return "n/d";
        }
        long s = Math.round(segundos);
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static String horas(int minutos) {
        return (minutos / 60) + " h " + String.format("%02d", minutos % 60) + " min";
    }
}
