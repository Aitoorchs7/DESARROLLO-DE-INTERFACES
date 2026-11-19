import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFNoFillProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Genera el libro Excel con Apache POI (XSSFWorkbook = .xlsx).
 * Todos los cálculos se hacen en Java (clase Calculos); Excel solo los presenta.
 * Las únicas fórmulas de Excel son sencillas: carga de cada sesión y totales del resumen.
 */
public class InformeExcel {

    /** Todo lo de un deportista que hace falta para el informe. */
    private record Atleta(Deportista d, List<Sesion> sesiones, List<Test5k> tests, List<Calculos.Semana> semanas) {
    }

    private final XSSFWorkbook libro;

    // Estilos reutilizables: se crean UNA vez y se aplican a muchas celdas con setCellStyle
    private final CellStyle titulo, cabecera, texto, fecha, entero, decimal, segundos, km, porcentaje, ua;

    // Filas de datos de la hoja Entrenamientos (las usa el Resumen en sus fórmulas)
    private int primeraSesion, ultimaSesion;

    // ================================================================== punto de entrada

    /**
     * Crea el informe y lo guarda en el archivo elegido.
     * Lanza IOException si no se puede escribir (archivo abierto en Excel, sin permisos...).
     */
    public static void exportar(Datos datos, File destino) throws IOException {
        if (datos.deportistas.isEmpty()) {
            throw new IllegalArgumentException("No hay deportistas: no hay nada que exportar.");
        }
        // try-with-resources: el libro y el archivo se cierran siempre, aunque haya error
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            new InformeExcel(libro).crear(datos);
            try (FileOutputStream archivo = new FileOutputStream(destino)) {
                libro.write(archivo);
            }
        }
    }

    private InformeExcel(XSSFWorkbook libro) {
        this.libro = libro;

        titulo = libro.createCellStyle();
        Font fuenteTitulo = libro.createFont();
        fuenteTitulo.setBold(true);
        fuenteTitulo.setFontHeightInPoints((short) 16);
        fuenteTitulo.setColor(IndexedColors.DARK_BLUE.getIndex());
        titulo.setFont(fuenteTitulo);

        cabecera = estilo(null);
        Font fuenteCabecera = libro.createFont();
        fuenteCabecera.setBold(true);
        fuenteCabecera.setColor(IndexedColors.WHITE.getIndex());
        cabecera.setFont(fuenteCabecera);
        cabecera.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cabecera.setAlignment(HorizontalAlignment.CENTER);
        cabecera.setWrapText(true);

        texto = estilo(null);
        fecha = estilo("dd/mm/yyyy");
        fecha.setAlignment(HorizontalAlignment.CENTER);
        entero = estilo("0");
        decimal = estilo("0.00");
        segundos = estilo("0.0\" s\"");
        km = estilo("0.0\" km\"");
        porcentaje = estilo("0.0\"%\"");   // el CV ya viene multiplicado por 100
        ua = estilo("#,##0\" UA\"");
    }

    /** Estilo con bordes y, si se indica, formato numérico. */
    private CellStyle estilo(String formato) {
        CellStyle e = libro.createCellStyle();
        e.setBorderTop(BorderStyle.THIN);
        e.setBorderBottom(BorderStyle.THIN);
        e.setBorderLeft(BorderStyle.THIN);
        e.setBorderRight(BorderStyle.THIN);
        if (formato != null) {
            e.setDataFormat(libro.createDataFormat().getFormat(formato));
        }
        return e;
    }

    private void crear(Datos datos) {
        // Las hojas se crean primero para fijar su orden
        XSSFSheet resumen = libro.createSheet("Resumen");
        XSSFSheet hojaDeportistas = libro.createSheet("Deportistas");
        XSSFSheet entrenamientos = libro.createSheet("Entrenamientos");
        XSSFSheet analisis = libro.createSheet("Análisis");
        XSSFSheet rendimiento = libro.createSheet("Rendimiento");

        // Se calcula todo en Java una vez por deportista
        List<Atleta> atletas = new ArrayList<>();
        for (Deportista d : datos.deportistas) {
            List<Sesion> sesiones = datos.sesionesDe(d.id(), null, null);
            atletas.add(new Atleta(d, sesiones, datos.testsDe(d.id(), null, null), Calculos.semanas(sesiones)));
        }

        hojaEntrenamientos(entrenamientos, datos);
        hojaDeportistas(hojaDeportistas, atletas);
        hojaAnalisis(analisis, atletas);
        hojaRendimiento(rendimiento, atletas);
        hojaResumen(resumen, datos, atletas);

        // Calcula las fórmulas y ajusta el ancho de las columnas al contenido
        libro.getCreationHelper().createFormulaEvaluator().evaluateAll();
        for (Sheet hoja : libro) {
            for (int c = 0; c < 17; c++) {
                hoja.autoSizeColumn(c);
            }
        }
    }

    // ================================================================== hojas

    private void hojaEntrenamientos(XSSFSheet hoja, Datos datos) {
        titulo(hoja, "Registro de entrenamientos", 8);
        cabecera(hoja, 2, 0, "Fecha", "ID", "Deportista", "Tipo de sesión", "Duración (min)", "Distancia",
                "FC media (ppm)", "RPE (1-10)", "Carga de la sesión");

        List<Sesion> todas = new ArrayList<>(datos.sesiones);
        todas.sort(Comparator.comparing(Sesion::fecha));

        int f = 3;
        for (Sesion s : todas) {
            Row fila = hoja.createRow(f);
            celda(fila, 0, s.fecha(), fecha);
            celda(fila, 1, s.idDeportista(), texto);
            celda(fila, 2, datos.buscar(s.idDeportista()).nombre(), texto);
            celda(fila, 3, s.tipo(), texto);
            celda(fila, 4, s.minutos(), entero);
            celda(fila, 5, s.km(), km);
            celda(fila, 6, s.fc(), entero);
            celda(fila, 7, s.rpe(), entero);
            // FÓRMULA DE EXCEL: carga = duración × RPE (en Excel las filas empiezan en 1, por eso f + 1)
            Cell carga = fila.createCell(8);
            carga.setCellFormula("E" + (f + 1) + "*H" + (f + 1));
            carga.setCellStyle(ua);
            f++;
        }
        primeraSesion = 3;
        ultimaSesion = f - 1;

        // FORMATO CONDICIONAL: RPE >= 8 (esfuerzo alto) en rojo
        if (ultimaSesion >= primeraSesion) {
            SheetConditionalFormatting formato = hoja.getSheetConditionalFormatting();
            ConditionalFormattingRule regla = formato.createConditionalFormattingRule(ComparisonOperator.GE, "8");
            PatternFormatting relleno = regla.createPatternFormatting();
            relleno.setFillBackgroundColor(IndexedColors.ROSE.getIndex());
            relleno.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
            formato.addConditionalFormatting(
                    new CellRangeAddress[]{new CellRangeAddress(primeraSesion, ultimaSesion, 7, 7)}, regla);
        }

        hoja.setAutoFilter(new CellRangeAddress(2, Math.max(2, ultimaSesion), 0, 8)); // filtros en la cabecera
        hoja.createFreezePane(0, 3);                                                   // inmoviliza título y cabecera
    }

    private void hojaDeportistas(XSSFSheet hoja, List<Atleta> atletas) {
        titulo(hoja, "Deportistas del club", 7);
        cabecera(hoja, 2, 0, "ID", "Nombre", "Edad", "Categoría", "Objetivo de competición",
                "Marca inicial 5 km", "Sesiones", "Km totales");
        int f = 3;
        for (Atleta a : atletas) {
            Row fila = hoja.createRow(f++);
            double kmTotales = 0;
            for (Sesion s : a.sesiones()) {
                kmTotales += s.km();
            }
            celda(fila, 0, a.d().id(), texto);
            celda(fila, 1, a.d().nombre(), texto);
            celda(fila, 2, a.d().edad(), entero);
            celda(fila, 3, a.d().categoria(), texto);
            celda(fila, 4, a.d().objetivo(), texto);
            celda(fila, 5, a.d().marca5k(), segundos);
            celda(fila, 6, a.sesiones().size(), entero);
            celda(fila, 7, kmTotales, km);
        }
        hoja.setAutoFilter(new CellRangeAddress(2, f - 1, 0, 7));
        hoja.createFreezePane(0, 3);
    }

    private void hojaAnalisis(XSSFSheet hoja, List<Atleta> atletas) {
        titulo(hoja, "Análisis: carga semanal, estadística, regresión y correlación", 17);

        // ---- Tabla semanal de todos los deportistas (a la izquierda, columnas A-I)
        cabecera(hoja, 2, 0, "Deportista", "Semana", "Lunes", "Distancia", "Tiempo (min)", "Carga semanal",
                "Media móvil 3 sem.", "Monotonía", "Tensión semanal");
        int f = 3;
        int[] inicio = new int[atletas.size()];
        int[] fin = new int[atletas.size()];
        for (int i = 0; i < atletas.size(); i++) {
            inicio[i] = f;
            int numero = 1;
            for (Calculos.Semana s : atletas.get(i).semanas()) {
                Row fila = fila(hoja, f++);
                celda(fila, 0, atletas.get(i).d().nombre(), texto);
                celda(fila, 1, numero++, entero);
                celda(fila, 2, s.lunes(), fecha);
                celda(fila, 3, s.km(), km);
                celda(fila, 4, s.minutos(), entero);
                celda(fila, 5, s.carga(), ua);
                celda(fila, 6, Double.isNaN(s.mediaMovil()) ? "datos insuficientes" : s.mediaMovil(), ua);
                celda(fila, 7, Double.isNaN(s.monotonia()) ? "n/d (desv. 0)" : s.monotonia(), decimal);
                celda(fila, 8, Double.isNaN(s.tension()) ? "n/d (desv. 0)" : s.tension(), ua);
            }
            fin[i] = f - 1;
        }
        hoja.setAutoFilter(new CellRangeAddress(2, Math.max(2, f - 1), 0, 8));
        hoja.createFreezePane(0, 3);

        // ---- Estadística descriptiva (a la derecha, desde la columna K)
        cabecera(hoja, 2, 10, "Deportista", "Variable", "Media", "Mediana", "Mínimo", "Máximo", "Desv. típica", "CV");
        int g = 3;
        for (Atleta a : atletas) {
            estadistica(hoja, g++, a.d().nombre(), "Carga semanal", Calculos.cargas(a.semanas()), ua);
            estadistica(hoja, g++, a.d().nombre(), "Distancia por sesión", Calculos.distancias(a.sesiones()), km);
        }

        // ---- Regresión y correlación
        g++;
        cabecera(hoja, g++, 10, "Deportista", "Nº tests", "a (s)", "b (s/semana)", "R²",
                "Marca predicha próxima semana", "r de Pearson");
        for (Atleta a : atletas) {
            double[] reg = regresion(a);
            double[][] pares = Calculos.cargaYMarca(a.semanas(), a.tests());
            Row fila = fila(hoja, g++);
            celda(fila, 10, a.d().nombre(), texto);
            celda(fila, 11, a.tests().size(), entero);
            celda(fila, 12, reg[0], decimal);
            celda(fila, 13, reg[1], decimal);
            celda(fila, 14, reg[2], decimal);
            celda(fila, 15, prediccion(a), segundos);
            celda(fila, 16, Calculos.pearson(pares[0], pares[1]), decimal);
        }
        nota(hoja, g++, 10, 16, "Predicción = extrapolación de la recta, no una garantía. "
                + "Correlación no implica causalidad. Indicadores didácticos, no diagnósticos.");

        // ---- Un gráfico de carga semanal por deportista (debajo de las tablas de la derecha)
        int filaGrafico = g + 1;
        for (int i = 0; i < atletas.size(); i++) {
            if (atletas.get(i).semanas().isEmpty()) {
                continue;
            }
            graficoBarras(hoja, "Carga semanal: " + atletas.get(i).d().nombre(), "Semana", "Carga (UA)",
                    XDDFDataSourcesFactory.fromNumericCellRange(hoja, new CellRangeAddress(inicio[i], fin[i], 1, 1)),
                    new CellRangeAddress(inicio[i], fin[i], 5, 5), "Carga semanal", 10, filaGrafico);
            filaGrafico += 17;
        }
    }

    private void estadistica(XSSFSheet hoja, int f, String nombre, String variable, double[] x, CellStyle e) {
        Row fila = fila(hoja, f);
        celda(fila, 10, nombre, texto);
        celda(fila, 11, variable, texto);
        celda(fila, 12, Calculos.media(x), e);
        celda(fila, 13, Calculos.mediana(x), e);
        celda(fila, 14, Calculos.minimo(x), e);
        celda(fila, 15, Calculos.maximo(x), e);
        celda(fila, 16, Calculos.desviacion(x), e);
        celda(fila, 17, Calculos.cv(x), porcentaje);
    }

    private void hojaRendimiento(XSSFSheet hoja, List<Atleta> atletas) {
        titulo(hoja, "Rendimiento: tests de 5 km, evolución y predicción", 13);

        // ---- Histórico de tests (izquierda, columnas A-F)
        cabecera(hoja, 2, 0, "Deportista", "Fecha del test", "Semana (x)", "Marca real", "Recta ajustada",
                "Mejora vs. 1.er test");
        int f = 3;
        int[] inicio = new int[atletas.size()];
        int[] fin = new int[atletas.size()];
        for (int i = 0; i < atletas.size(); i++) {
            Atleta a = atletas.get(i);
            inicio[i] = f;
            double[] x = a.tests().isEmpty() ? new double[0] : Calculos.semanasDesdePrimero(a.tests());
            double[] reg = regresion(a);
            for (int t = 0; t < a.tests().size(); t++) {
                Row fila = fila(hoja, f++);
                celda(fila, 0, a.d().nombre(), texto);
                celda(fila, 1, a.tests().get(t).fecha(), fecha);
                celda(fila, 2, x[t], decimal);
                celda(fila, 3, a.tests().get(t).segundos(), segundos);
                celda(fila, 4, reg[0] + reg[1] * x[t], segundos); // NaN si no hay recta -> "n/d"
                celda(fila, 5, a.tests().get(0).segundos() - a.tests().get(t).segundos(), segundos);
            }
            fin[i] = f - 1;
        }
        hoja.setAutoFilter(new CellRangeAddress(2, Math.max(2, f - 1), 0, 5));
        hoja.createFreezePane(0, 3);

        // ---- Evolución y predicción por deportista (derecha, desde la columna H)
        cabecera(hoja, 2, 7, "Deportista", "Primera marca", "Mejor marca", "Evolución (1.º − último)",
                "Pendiente b (s/semana)", "R²", "Predicción semana siguiente");
        int g = 3;
        for (Atleta a : atletas) {
            double[] reg = regresion(a);
            Row fila = fila(hoja, g++);
            celda(fila, 7, a.d().nombre(), texto);
            celda(fila, 8, a.tests().isEmpty() ? Double.NaN : a.tests().get(0).segundos(), segundos);
            celda(fila, 9, Calculos.mejorMarca(a.tests()), segundos);
            celda(fila, 10, Calculos.evolucion(a.tests()), segundos);
            celda(fila, 11, reg[1], decimal);
            celda(fila, 12, reg[2], decimal);
            celda(fila, 13, prediccion(a), segundos);
        }
        nota(hoja, g++, 7, 13, "Evolución positiva = ahora es más rápido que en el primer test. "
                + "La predicción es una extrapolación, no una garantía.");

        // ---- Gráfico de marcas reales frente a recta ajustada (solo con 2 o más tests)
        int filaGrafico = g + 1;
        for (int i = 0; i < atletas.size(); i++) {
            Atleta a = atletas.get(i);
            if (a.tests().size() < 2 || Double.isNaN(regresion(a)[1])) {
                continue;
            }
            double min = Calculos.minimo(Calculos.marcas(a.tests()));
            double max = Calculos.maximo(Calculos.marcas(a.tests()));
            graficoLinea(hoja, "Marca real frente a recta ajustada: " + a.d().nombre(),
                    new CellRangeAddress(inicio[i], fin[i], 2, 2),
                    new CellRangeAddress(inicio[i], fin[i], 3, 3),
                    new CellRangeAddress(inicio[i], fin[i], 4, 4),
                    min - 10, max + 10, 7, filaGrafico);
            filaGrafico += 17;
        }
    }

    private void hojaResumen(XSSFSheet hoja, Datos datos, List<Atleta> atletas) {
        titulo(hoja, "Club de atletismo: resumen", 4);

        // ---- Indicadores generales
        cabecera(hoja, 2, 0, "Indicador", "Valor");
        indicador(hoja, 3, "Nº de deportistas", datos.deportistas.size(), entero);
        indicador(hoja, 4, "Nº de sesiones", datos.sesiones.size(), entero);
        indicador(hoja, 5, "Kilómetros totales", 0, km);
        indicador(hoja, 6, "Carga total", 0, ua);
        if (ultimaSesion >= primeraSesion) {
            // FÓRMULAS DE EXCEL que suman columnas de la hoja Entrenamientos
            fila(hoja, 5).getCell(1).setCellFormula("SUM(Entrenamientos!F" + (primeraSesion + 1) + ":F" + (ultimaSesion + 1) + ")");
            fila(hoja, 6).getCell(1).setCellFormula("SUM(Entrenamientos!I" + (primeraSesion + 1) + ":I" + (ultimaSesion + 1) + ")");
        }

        // ---- Comparativa: los 4 indicadores de cada atleta por separado
        cabecera(hoja, 8, 0, "Deportista", "Mejor marca 5 km", "Evolución vs. test inicial",
                "Km semanales medios", "Carga semanal media");
        int f = 9;
        for (Atleta a : atletas) {
            Row fila = hoja.createRow(f++);
            celda(fila, 0, a.d().nombre(), texto);
            celda(fila, 1, Calculos.mejorMarca(a.tests()), segundos);
            celda(fila, 2, Calculos.evolucion(a.tests()), segundos);
            celda(fila, 3, Calculos.kmSemanalesMedios(a.semanas()), km);
            celda(fila, 4, Calculos.cargaSemanalMedia(a.semanas()), ua);
        }
        nota(hoja, f + 1, 0, 4, "Evolución = marca del primer test − marca del último (positivo = ha mejorado). "
                + "Cada indicador va por separado porque tienen unidades distintas.");

        // ---- Gráficos comparativos
        graficoBarras(hoja, "Km semanales medios por deportista", "Deportista", "Km/semana",
                XDDFDataSourcesFactory.fromStringCellRange(hoja, new CellRangeAddress(9, f - 1, 0, 0)),
                new CellRangeAddress(9, f - 1, 3, 3), "Km semanales medios", 6, 2);
        graficoBarras(hoja, "Carga semanal media por deportista", "Deportista", "Carga (UA)",
                XDDFDataSourcesFactory.fromStringCellRange(hoja, new CellRangeAddress(9, f - 1, 0, 0)),
                new CellRangeAddress(9, f - 1, 4, 4), "Carga semanal media", 6, 19);
    }

    // ================================================================== gráficos (pasos de la teoría)

    /** Gráfico de barras verticales con una serie. */
    private void graficoBarras(XSSFSheet hoja, String tituloGrafico, String tituloEjeX, String tituloEjeY,
                               XDDFDataSource<?> categorias, CellRangeAddress rangoValores, String nombreSerie,
                               int columna, int fila) {
        // 1) dónde va el gráfico y cuánto ocupa
        XSSFDrawing dibujo = hoja.createDrawingPatriarch();
        XSSFClientAnchor ancla = dibujo.createAnchor(0, 0, 0, 0, columna, fila, columna + 8, fila + 15);
        XSSFChart grafico = dibujo.createChart(ancla);
        grafico.setTitleText(tituloGrafico);

        // 2) ejes
        XDDFCategoryAxis ejeInferior = grafico.createCategoryAxis(AxisPosition.BOTTOM);
        ejeInferior.setTitle(tituloEjeX);
        XDDFValueAxis ejeIzquierdo = grafico.createValueAxis(AxisPosition.LEFT);
        ejeIzquierdo.setTitle(tituloEjeY);

        // 3) de dónde salen los valores
        XDDFNumericalDataSource<Double> valores = XDDFDataSourcesFactory.fromNumericCellRange(hoja, rangoValores);

        // 4) tipo de gráfico y serie
        XDDFChartData datos = grafico.createData(ChartTypes.BAR, ejeInferior, ejeIzquierdo);
        ((XDDFBarChartData) datos).setBarDirection(BarDirection.COL);
        XDDFChartData.Series serie = datos.addSeries(categorias, valores);
        serie.setTitle(nombreSerie, null);

        // 5) pintar y leyenda
        grafico.plot(datos);
        XDDFChartLegend leyenda = grafico.getOrAddLegend();
        leyenda.setPosition(LegendPosition.TOP_RIGHT);
    }

    /** Gráfico de líneas con dos series: marcas reales (solo puntos) y recta ajustada (solo línea). */
    private void graficoLinea(XSSFSheet hoja, String tituloGrafico, CellRangeAddress rangoX,
                              CellRangeAddress rangoReales, CellRangeAddress rangoRecta,
                              double minimoY, double maximoY, int columna, int fila) {
        XSSFDrawing dibujo = hoja.createDrawingPatriarch();
        XSSFClientAnchor ancla = dibujo.createAnchor(0, 0, 0, 0, columna, fila, columna + 8, fila + 15);
        XSSFChart grafico = dibujo.createChart(ancla);
        grafico.setTitleText(tituloGrafico);

        XDDFCategoryAxis ejeInferior = grafico.createCategoryAxis(AxisPosition.BOTTOM);
        ejeInferior.setTitle("Semanas desde el primer test");
        XDDFValueAxis ejeIzquierdo = grafico.createValueAxis(AxisPosition.LEFT);
        ejeIzquierdo.setTitle("Marca 5 km (s)");
        ejeIzquierdo.setMinimum(minimoY); // el eje no empieza en 0 para que se vea la evolución
        ejeIzquierdo.setMaximum(maximoY);

        XDDFDataSource<Double> semanas = XDDFDataSourcesFactory.fromNumericCellRange(hoja, rangoX);
        XDDFNumericalDataSource<Double> reales = XDDFDataSourcesFactory.fromNumericCellRange(hoja, rangoReales);
        XDDFNumericalDataSource<Double> recta = XDDFDataSourcesFactory.fromNumericCellRange(hoja, rangoRecta);

        XDDFChartData datos = grafico.createData(ChartTypes.LINE, ejeInferior, ejeIzquierdo);
        XDDFLineChartData.Series serieReal = (XDDFLineChartData.Series) datos.addSeries(semanas, reales);
        serieReal.setTitle("Marca real", null);
        XDDFLineChartData.Series serieRecta = (XDDFLineChartData.Series) datos.addSeries(semanas, recta);
        serieRecta.setTitle("Recta ajustada", null);
        grafico.plot(datos);

        // marcas reales: solo puntos (línea sin relleno); recta: sin marcadores
        serieReal.setMarkerStyle(MarkerStyle.CIRCLE);
        XDDFLineProperties sinLinea = new XDDFLineProperties();
        sinLinea.setFillProperties(new XDDFNoFillProperties());
        XDDFShapeProperties propiedades = new XDDFShapeProperties();
        propiedades.setLineProperties(sinLinea);
        serieReal.setShapeProperties(propiedades);
        serieRecta.setMarkerStyle(MarkerStyle.NONE);

        XDDFChartLegend leyenda = grafico.getOrAddLegend();
        leyenda.setPosition(LegendPosition.TOP_RIGHT);
    }

    // ================================================================== auxiliares

    private double[] regresion(Atleta a) {
        if (a.tests().isEmpty()) {
            return new double[]{Double.NaN, Double.NaN, Double.NaN};
        }
        return Calculos.regresion(Calculos.semanasDesdePrimero(a.tests()), Calculos.marcas(a.tests()));
    }

    /** Marca estimada para la semana siguiente al último test (extrapolación). */
    private double prediccion(Atleta a) {
        double[] reg = regresion(a);
        if (a.tests().isEmpty()) {
            return Double.NaN;
        }
        double[] x = Calculos.semanasDesdePrimero(a.tests());
        return reg[0] + reg[1] * (x[x.length - 1] + 1);
    }

    private void indicador(Sheet hoja, int indiceFila, String etiqueta, double valor, CellStyle estilo) {
        Row fila = hoja.createRow(indiceFila);
        celda(fila, 0, etiqueta, texto);
        celda(fila, 1, valor, estilo);
    }

    /** Fila existente o nueva (createRow borraría una fila ya escrita). */
    private Row fila(Sheet hoja, int indice) {
        Row fila = hoja.getRow(indice);
        return fila != null ? fila : hoja.createRow(indice);
    }

    /** Escribe una celda según el tipo del valor. Un número NaN (no definido) se escribe como "n/d". */
    private void celda(Row fila, int columna, Object valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        if (valor instanceof String) {
            celda.setCellValue((String) valor);
        } else if (valor instanceof LocalDate) {
            celda.setCellValue((LocalDate) valor);
        } else if (valor instanceof Number) {
            double numero = ((Number) valor).doubleValue();
            if (Double.isNaN(numero)) {
                celda.setCellValue("n/d");
            } else {
                celda.setCellValue(numero);
            }
        }
        celda.setCellStyle(estilo);
    }

    /** Título de la hoja: fila 0 con las celdas combinadas hasta la última columna. */
    private void titulo(Sheet hoja, String texto, int ultimaColumna) {
        Row fila = hoja.createRow(0);
        fila.setHeightInPoints(28);
        Cell celda = fila.createCell(0);
        celda.setCellValue(texto);
        celda.setCellStyle(titulo);
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, ultimaColumna));
    }

    private void cabecera(Sheet hoja, int indiceFila, int columnaInicial, String... titulos) {
        Row fila = fila(hoja, indiceFila);
        fila.setHeightInPoints(32);
        for (int i = 0; i < titulos.length; i++) {
            celda(fila, columnaInicial + i, titulos[i], cabecera);
        }
    }

    /** Nota de varias columnas combinadas (así no ensancha la columna al autoajustar). */
    private void nota(Sheet hoja, int indiceFila, int columnaIni, int columnaFin, String contenido) {
        Row fila = fila(hoja, indiceFila);
        celda(fila, columnaIni, contenido, texto);
        hoja.addMergedRegion(new CellRangeAddress(indiceFila, indiceFila, columnaIni, columnaFin));
    }
}
