import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del módulo PDF (se ejecutan desde la carpeta del proyecto, donde están recursos/). */
class InformeTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 8, 31);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 8);

    private Datos datos;

    @BeforeEach
    void preparar() {
        datos = new Datos();
        datos.cargarPrueba();
    }

    private File generar(String id, File carpeta) throws IOException {
        DatosInforme informe = ServicioInforme.preparar(datos, datos.buscar(id), DESDE, HASTA);
        File destino = new File(carpeta, "informe_" + id + ".pdf");
        ServicioInforme.generar(informe, destino);
        return destino;
    }

    private static String textoDe(File pdf) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    @Test
    void informeDinamicoParaDosDeportistas(@TempDir Path carpeta) throws IOException {
        File pdfLucia = generar("A01", carpeta.toFile());
        File pdfCarlos = generar("A02", carpeta.toFile());

        for (File pdf : new File[]{pdfLucia, pdfCarlos}) {
            try (PDDocument doc = Loader.loadPDF(pdf)) {
                assertTrue(doc.getNumberOfPages() >= 3 && doc.getNumberOfPages() <= 5,
                        "páginas: " + doc.getNumberOfPages());
            }
        }

        // Cada PDF contiene SOLO los datos de su deportista
        String lucia = textoDe(pdfLucia);
        String carlos = textoDe(pdfCarlos);
        assertTrue(lucia.contains("Lucía Ramos") && lucia.contains("Media maratón"));
        assertFalse(lucia.contains("Carlos Méndez"));
        assertTrue(carlos.contains("Carlos Méndez") && carlos.contains("10 km"));
        assertFalse(carlos.contains("Lucía Ramos"));

        // Los números son los de Calculos (los mismos que la ventana y el Excel)
        DatosInforme dl = ServicioInforme.preparar(datos, datos.buscar("A01"), DESDE, HASTA);
        assertTrue(lucia.contains("realizó " + dl.numSesiones + " sesiones"));
        assertTrue(lucia.contains("20:48"));               // mejor marca de Lucía (1248 s)
        assertTrue(carlos.contains("22:40"));              // mejor marca de Carlos (1360 s)
        assertNotEquals(lucia, carlos);
    }

    @Test
    void todoElContenidoEstaDentroDeLosMargenes(@TempDir Path carpeta) throws IOException {
        File pdf = generar("A01", carpeta.toFile());
        List<String> fuera = new ArrayList<>();
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDFTextStripper revisor = new PDFTextStripper() {
                @Override
                protected void writeString(String texto, List<TextPosition> posiciones) {
                    for (TextPosition p : posiciones) {
                        float izquierda = p.getXDirAdj();
                        float derecha = izquierda + p.getWidthDirAdj();
                        if (izquierda < GeneradorPDF.MARGEN - 1 || derecha > getCurrentPage().getMediaBox().getWidth() - GeneradorPDF.MARGEN + 1) {
                            fuera.add(texto);
                        }
                    }
                }
            };
            revisor.getText(doc);
        }
        assertTrue(fuera.isEmpty(), "texto fuera de los márgenes: " + fuera);
    }

    @Test
    void sinInformacionSuficienteSeAvisaConUnMensaje() {
        // Intervalo sin ninguna sesión
        IllegalArgumentException sinSesiones = assertThrows(IllegalArgumentException.class, () ->
                ServicioInforme.preparar(datos, datos.buscar("A01"), LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 1)));
        assertTrue(sinSesiones.getMessage().contains("sesiones"));
        // Deportista con sesiones pero sin tests
        datos.altaDeportista(new Deportista("A09", "Nuevo", 20, "Sénior", "5 km", 1300));
        datos.registrarSesion(new Sesion(LocalDate.of(2026, 9, 1), "A09", "Rodaje", 40, 7, 140, 5));
        IllegalArgumentException sinTests = assertThrows(IllegalArgumentException.class, () ->
                ServicioInforme.preparar(datos, datos.buscar("A09"), DESDE, HASTA));
        assertTrue(sinTests.getMessage().contains("test"));
        // Intervalo al revés y sin deportista
        assertThrows(IllegalArgumentException.class, () -> ServicioInforme.preparar(datos, datos.buscar("A01"), HASTA, DESDE));
        assertThrows(IllegalArgumentException.class, () -> ServicioInforme.preparar(datos, null, DESDE, HASTA));
    }

    @Test
    void unSoloTestGeneraElInformeConDatosInsuficientesEnLaRecta(@TempDir Path carpeta) throws IOException {
        datos.altaDeportista(new Deportista("A09", "Nuevo", 20, "Sénior", "5 km", 1300));
        for (int i = 0; i < 4; i++) {
            datos.registrarSesion(new Sesion(LocalDate.of(2026, 9, 1).plusDays(2 * i), "A09", "Rodaje", 40, 7, 140, 5));
        }
        datos.registrarTest(new Test5k(LocalDate.of(2026, 9, 5), "A09", 1300));
        File pdf = generar("A09", carpeta.toFile());
        String texto = textoDe(pdf);
        assertTrue(texto.contains("datos insuficientes"));
        assertTrue(texto.contains("no se puede valorar la evolución"));
    }

    @Test
    void laFaltaDeImagenesNoBloqueaLaGeneracion(@TempDir Path carpeta) throws IOException {
        File pdf = carpeta.resolve("sin_imagenes.pdf").toFile();
        try (GeneradorPDF generador = new GeneradorPDF(new File("recursos/fuentes"), new File("no_existe/logo.png"),
                "Encabezado", "Pie")) {
            generador.nuevaPagina();
            assertNull(generador.cargarImagen(new File("no_existe/foto.png"), "la foto"));
            File noEsImagen = carpeta.resolve("texto.png").toFile();
            java.nio.file.Files.writeString(noEsImagen.toPath(), "esto no es una imagen");
            assertNull(generador.cargarImagen(noEsImagen, "un archivo inválido"));
            assertEquals(3, generador.avisos.size());
            generador.guardar(pdf);
        }
        assertTrue(pdf.length() > 0);
    }

    @Test
    void lasImagenesConservanLaProporcion(@TempDir Path carpeta) throws IOException {
        try (GeneradorPDF generador = new GeneradorPDF(new File("recursos/fuentes"), new File("recursos/logo.png"), "E", "P")) {
            generador.nuevaPagina();
            // generica.png mide 900 x 600 (3:2). En un recuadro de 300 x 300 tiene que salir 300 x 200.
            float[] medidas = generador.dibujarImagen(generador.cargarImagen(new File("recursos/generica.png"), "imagen"),
                    100, 100, 300, 300);
            assertEquals(300f, medidas[0], 0.01f);
            assertEquals(200f, medidas[1], 0.01f);
            // En un recuadro de 300 x 100 manda el alto: 150 x 100
            float[] estrecha = generador.dibujarImagen(generador.cargarImagen(new File("recursos/generica.png"), "imagen"),
                    100, 100, 300, 100);
            assertEquals(150f, estrecha[0], 0.01f);
            assertEquals(100f, estrecha[1], 0.01f);
        }
    }

    @Test
    void erroresDeEscrituraSeComunicanComoIOException(@TempDir Path carpeta) throws IOException {
        DatosInforme informe = ServicioInforme.preparar(datos, datos.buscar("A01"), DESDE, HASTA);
        // El destino es una carpeta existente: no se puede escribir como archivo
        assertThrows(IOException.class, () -> ServicioInforme.generar(informe, carpeta.toFile()));
        // Una carpeta que no existe se crea (como en la teoría)
        File enSubcarpeta = carpeta.resolve("nueva").resolve("informe.pdf").toFile();
        ServicioInforme.generar(informe, enSubcarpeta);
        assertTrue(enSubcarpeta.isFile());
    }
}
