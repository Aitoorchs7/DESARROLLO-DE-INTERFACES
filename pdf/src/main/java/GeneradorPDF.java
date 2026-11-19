import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Métodos de maquetación con PDFBox: páginas, textos, líneas, imágenes, tablas y pie de página.
 * No sabe nada de deportistas: solo dibuja lo que se le pide.
 *
 * Las coordenadas de PDF tienen el (0,0) abajo a la izquierda. Para trabajar más cómodo se guarda en "y"
 * la posición del cursor (de arriba hacia abajo): cada método dibuja en "y" y luego la baja.
 */
public class GeneradorPDF implements AutoCloseable {

    public static final float MARGEN = 56;          // margen izquierdo, derecho
    public static final float MARGEN_SUPERIOR = 78; // las páginas con encabezado empiezan más abajo
    public static final float MARGEN_INFERIOR = 64; // deja sitio al pie de página

    public static final Color AZUL = new Color(23, 51, 79);
    public static final Color NARANJA = new Color(242, 140, 40);
    public static final Color GRIS = new Color(105, 105, 105);
    public static final Color GRIS_CLARO = new Color(240, 243, 246);
    public static final Color BORDE = new Color(190, 196, 202);

    private final PDDocument documento = new PDDocument();
    private final PDFont normal, negrita, cursiva;   // fuentes .ttf cargadas con PDType0Font.load
    private final PDImageXObject logo;               // puede ser null si no se encuentra el archivo
    private final String textoEncabezado;
    private final String textoPie;

    private PDPage pagina;
    private PDPageContentStream contenido;
    private float anchoPagina, altoPagina;
    private float y;                                 // cursor vertical (distancia al borde superior)
    private int numeroPagina = 0;

    /** Avisos para el usuario (p. ej. «no se encontró la fotografía»). No bloquean la generación. */
    public final List<String> avisos = new ArrayList<>();

    public GeneradorPDF(File carpetaFuentes, File archivoLogo, String textoEncabezado, String textoPie) throws IOException {
        this.textoEncabezado = textoEncabezado;
        this.textoPie = textoPie;
        try {
            normal = PDType0Font.load(documento, new File(carpetaFuentes, "LiberationSans-Regular.ttf"));
            negrita = PDType0Font.load(documento, new File(carpetaFuentes, "LiberationSans-Bold.ttf"));
            cursiva = PDType0Font.load(documento, new File(carpetaFuentes, "LiberationSans-Italic.ttf"));
        } catch (IOException e) {
            documento.close();
            throw new IOException("No se pueden cargar las fuentes .ttf de " + carpetaFuentes.getPath()
                    + " (" + e.getMessage() + ")", e);
        }
        logo = cargarImagen(archivoLogo, "logotipo");
    }

    // ================================================================== páginas

    /** Cierra la página actual y empieza otra. Las páginas 2 en adelante llevan encabezado. */
    public void nuevaPagina() throws IOException {
        if (contenido != null) {
            contenido.close();
        }
        pagina = new PDPage(PDRectangle.A4);
        documento.addPage(pagina);
        contenido = new PDPageContentStream(documento, pagina);
        anchoPagina = pagina.getMediaBox().getWidth();
        altoPagina = pagina.getMediaBox().getHeight();
        numeroPagina++;
        y = altoPagina - MARGEN;
        if (numeroPagina > 1) {
            dibujarEncabezado();
            y = altoPagina - MARGEN_SUPERIOR;
        }
    }

    public int numeroPagina() {
        return numeroPagina;
    }

    /** Si no caben "alto" puntos en esta página, salta a la siguiente. Devuelve true si saltó. */
    public boolean asegurarEspacio(float alto) throws IOException {
        if (y - alto < MARGEN_INFERIOR) {
            nuevaPagina();
            return true;
        }
        return false;
    }

    public void espacio(float puntos) {
        y -= puntos;
    }

    public float anchoUtil() {
        return anchoPagina - 2 * MARGEN;
    }

    private void dibujarEncabezado() throws IOException {
        float alto = 24;
        if (logo != null) {
            dibujarImagen(logo, MARGEN, altoPagina - 30 - alto, 100, alto);
        }
        textoDerecha(textoEncabezado, anchoPagina - MARGEN, altoPagina - 42, normal, 9, GRIS);
        linea(MARGEN, altoPagina - 54, anchoPagina - MARGEN, altoPagina - 54, BORDE, 0.8f);
    }

    // ================================================================== texto

    /** Escribe una línea de texto con la esquina inferior izquierda en (x, yBase). */
    public void texto(String t, float x, float yBase, PDFont fuente, float tamano, Color color) throws IOException {
        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.setNonStrokingColor(color);
        contenido.newLineAtOffset(x, yBase);
        contenido.showText(t);
        contenido.endText();
    }

    public void textoDerecha(String t, float xDerecha, float yBase, PDFont fuente, float tamano, Color color) throws IOException {
        texto(t, xDerecha - anchoTexto(t, fuente, tamano), yBase, fuente, tamano, color);
    }

    public void textoCentrado(String t, float xCentro, float yBase, PDFont fuente, float tamano, Color color) throws IOException {
        texto(t, xCentro - anchoTexto(t, fuente, tamano) / 2, yBase, fuente, tamano, color);
    }

    public float anchoTexto(String t, PDFont fuente, float tamano) throws IOException {
        return fuente.getStringWidth(t) / 1000 * tamano;
    }

    /** Título de sección: texto grande en azul y una línea naranja debajo. */
    public void seccion(String t) throws IOException {
        asegurarEspacio(70);
        espacio(8);
        texto(t, MARGEN, y - 16, negrita, 17, AZUL);
        espacio(24);
        linea(MARGEN, y, anchoPagina - MARGEN, y, NARANJA, 2);
        espacio(14);
    }

    public void subtitulo(String t) throws IOException {
        asegurarEspacio(46);
        texto(t, MARGEN, y - 12, negrita, 12, AZUL);
        espacio(20);
    }

    /** Párrafo con ajuste de línea automático dentro de los márgenes. */
    public void parrafo(String t, PDFont fuente, float tamano, Color color) throws IOException {
        float interlineado = tamano * 1.45f;
        for (String linea : ajustar(t, fuente, tamano, anchoUtil())) {
            asegurarEspacio(interlineado);
            texto(linea, MARGEN, y - tamano, fuente, tamano, color);
            espacio(interlineado);
        }
        espacio(tamano * 0.5f);
    }

    public void parrafo(String t) throws IOException {
        parrafo(t, normal, 10.5f, Color.BLACK);
    }

    /** Parte el texto en líneas que no superan el ancho máximo. */
    private List<String> ajustar(String t, PDFont fuente, float tamano, float anchoMaximo) throws IOException {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : t.split(" ")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (anchoTexto(prueba, fuente, tamano) > anchoMaximo && actual.length() > 0) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) {
            lineas.add(actual.toString());
        }
        return lineas;
    }

    /** "Etiqueta: valor" con la etiqueta en negrita y gris. */
    public void etiquetaValor(String etiqueta, String valor, float x, float tamano) throws IOException {
        texto(etiqueta, x, y - tamano, negrita, tamano, GRIS);
        texto(valor, x + anchoTexto(etiqueta, negrita, tamano) + 6, y - tamano, normal, tamano, Color.BLACK);
        espacio(tamano * 1.7f);
    }

    // ================================================================== líneas y rectángulos

    public void linea(float x1, float y1, float x2, float y2, Color color, float grosor) throws IOException {
        contenido.setStrokingColor(color);
        contenido.setLineWidth(grosor);
        contenido.moveTo(x1, y1);
        contenido.lineTo(x2, y2);
        contenido.stroke();
    }

    public void rectangulo(float x, float yInferior, float ancho, float alto, Color relleno) throws IOException {
        contenido.setNonStrokingColor(relleno);
        contenido.addRect(x, yInferior, ancho, alto);
        contenido.fill();
    }

    /** Fila de "tarjetas" con un valor grande y una etiqueta pequeña (indicadores del resumen). */
    public void tarjetas(String[] etiquetas, String[] valores) throws IOException {
        float alto = 54;
        float hueco = 8;
        float ancho = (anchoUtil() - hueco * (etiquetas.length - 1)) / etiquetas.length;
        asegurarEspacio(alto + 10);
        for (int i = 0; i < etiquetas.length; i++) {
            float x = MARGEN + i * (ancho + hueco);
            rectangulo(x, y - alto, ancho, alto, GRIS_CLARO);
            rectangulo(x, y - 3, ancho, 3, NARANJA);
            textoCentrado(valores[i], x + ancho / 2, y - 29, negrita, 14, AZUL);
            textoCentrado(etiquetas[i], x + ancho / 2, y - 44, normal, 8, GRIS);
        }
        espacio(alto + 14);
    }

    // ================================================================== imágenes

    /**
     * Carga una imagen desde archivo. Si no existe o no es válida devuelve null y añade un aviso:
     * la ausencia de una imagen NO impide generar el informe.
     */
    public PDImageXObject cargarImagen(File archivo, String descripcion) {
        if (archivo == null || !archivo.isFile()) {
            avisos.add("No se encontró " + descripcion + (archivo != null ? " (" + archivo.getPath() + ")" : "") + ".");
            return null;
        }
        try {
            return PDImageXObject.createFromFile(archivo.getPath(), documento);
        } catch (IOException | RuntimeException e) {
            // PDFBox lanza una RuntimeException (no IOException) con algunos archivos que no son imágenes
            avisos.add("El archivo de " + descripcion + " no es una imagen válida (" + archivo.getName() + ").");
            return null;
        }
    }

    /**
     * Dibuja la imagen DENTRO de un recuadro maxAncho x maxAlto con su esquina inferior izquierda en (x, yInferior)
     * manteniendo la proporción original (se escala con un único factor, nunca se deforma).
     * Devuelve {ancho, alto} reales.
     */
    public float[] dibujarImagen(PDImageXObject imagen, float x, float yInferior, float maxAncho, float maxAlto) throws IOException {
        float factor = Math.min(maxAncho / imagen.getWidth(), maxAlto / imagen.getHeight());
        float ancho = imagen.getWidth() * factor;
        float alto = imagen.getHeight() * factor;
        contenido.drawImage(imagen, x, yInferior, ancho, alto);
        return new float[]{ancho, alto};
    }

    /** Imagen centrada en el cursor, con salto de página si no cabe. Devuelve el alto usado. */
    public float imagenCentrada(PDImageXObject imagen, float maxAncho, float maxAlto) throws IOException {
        float factor = Math.min(maxAncho / imagen.getWidth(), maxAlto / imagen.getHeight());
        float alto = imagen.getHeight() * factor;
        asegurarEspacio(alto + 10);
        float ancho = imagen.getWidth() * factor;
        dibujarImagen(imagen, (anchoPagina - ancho) / 2, y - alto, maxAncho, maxAlto);
        espacio(alto + 6);
        return alto;
    }

    public void pieDeImagen(String t) throws IOException {
        textoCentrado(t, anchoPagina / 2, y - 9, cursiva, 9, GRIS);
        espacio(18);
    }

    // ================================================================== tablas

    /**
     * Tabla con cabecera diferenciada, filas alternadas y bordes.
     * Si no caben más filas continúa en una página nueva REPITIENDO la cabecera.
     *
     * @param anchos   ancho de cada columna (la suma debe ser anchoUtil())
     * @param derecha  true = columna numérica alineada a la derecha
     * @param negritaUltimaFila  true = la última fila es de totales
     */
    public void tabla(String[] cabecera, float[] anchos, boolean[] derecha, List<String[]> filas,
                      boolean negritaUltimaFila) throws IOException {
        float altoFila = 19;
        float tamano = 9.5f;
        asegurarEspacio(altoFila * 3);
        filaDeCabecera(cabecera, anchos, altoFila, tamano);
        for (int i = 0; i < filas.size(); i++) {
            if (asegurarEspacio(altoFila + 2)) {
                filaDeCabecera(cabecera, anchos, altoFila, tamano); // cabecera repetida en la página nueva
            }
            boolean ultimaDeTotales = negritaUltimaFila && i == filas.size() - 1;
            Color fondo = ultimaDeTotales ? new Color(222, 230, 238) : (i % 2 == 0 ? Color.WHITE : GRIS_CLARO);
            rectangulo(MARGEN, y - altoFila, anchoUtil(), altoFila, fondo);
            float x = MARGEN;
            for (int c = 0; c < anchos.length; c++) {
                PDFont fuente = ultimaDeTotales ? negrita : normal;
                if (derecha[c]) {
                    textoDerecha(filas.get(i)[c], x + anchos[c] - 6, y - altoFila + 6, fuente, tamano, Color.BLACK);
                } else {
                    texto(filas.get(i)[c], x + 6, y - altoFila + 6, fuente, tamano, Color.BLACK);
                }
                x += anchos[c];
            }
            bordeFila(anchos, altoFila);
            espacio(altoFila);
        }
        espacio(10);
    }

    private void filaDeCabecera(String[] cabecera, float[] anchos, float altoFila, float tamano) throws IOException {
        rectangulo(MARGEN, y - altoFila, anchoUtil(), altoFila, AZUL);
        float x = MARGEN;
        for (int c = 0; c < anchos.length; c++) {
            textoCentrado(cabecera[c], x + anchos[c] / 2, y - altoFila + 6, negrita, tamano, Color.WHITE);
            x += anchos[c];
        }
        espacio(altoFila);
    }

    private void bordeFila(float[] anchos, float altoFila) throws IOException {
        contenido.setStrokingColor(BORDE);
        contenido.setLineWidth(0.5f);
        contenido.addRect(MARGEN, y - altoFila, anchoUtil(), altoFila);
        contenido.stroke();
        float x = MARGEN;
        for (int c = 0; c < anchos.length - 1; c++) {
            x += anchos[c];
            contenido.moveTo(x, y);
            contenido.lineTo(x, y - altoFila);
            contenido.stroke();
        }
    }

    // ================================================================== fuentes y cierre

    public PDFont normal() { return normal; }
    public PDFont negrita() { return negrita; }
    public PDFont cursiva() { return cursiva; }
    public float ancho() { return anchoPagina; }
    public float alto() { return altoPagina; }
    public float y() { return y; }
    public PDImageXObject logo() { return logo; }

    /** Pone el pie de página (texto y "Página x de n") en todas las páginas y guarda el archivo. */
    public void guardar(File destino) throws IOException {
        contenido.close();
        contenido = null;
        int total = documento.getNumberOfPages();
        for (int i = 0; i < total; i++) {
            // AppendMode.APPEND: se añade encima de lo que ya hay en la página
            try (PDPageContentStream pie = new PDPageContentStream(documento, documento.getPage(i),
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                pie.setStrokingColor(BORDE);
                pie.setLineWidth(0.8f);
                pie.moveTo(MARGEN, 46);
                pie.lineTo(anchoPagina - MARGEN, 46);
                pie.stroke();

                String numero = "Página " + (i + 1) + " de " + total;
                pie.beginText();
                pie.setFont(normal, 8.5f);
                pie.setNonStrokingColor(GRIS);
                pie.newLineAtOffset(MARGEN, 32);
                pie.showText(textoPie);
                pie.endText();
                pie.beginText();
                pie.setFont(negrita, 8.5f);
                pie.setNonStrokingColor(GRIS);
                pie.newLineAtOffset(anchoPagina - MARGEN - anchoTexto(numero, negrita, 8.5f), 32);
                pie.showText(numero);
                pie.endText();
            }
        }
        documento.save(destino);
    }

    /** Cierra el documento y la página abierta (siempre, aunque haya habido un error). */
    @Override
    public void close() throws IOException {
        if (contenido != null) {
            contenido.close();
            contenido = null;
        }
        documento.close();
    }
}
