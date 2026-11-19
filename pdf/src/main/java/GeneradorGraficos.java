import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Crea los gráficos con JFreeChart (como en la teoría) y los guarda como PNG en un archivo temporal.
 * Se dibujan a 1600x800 px (el doble de lo que ocupan en el PDF) para que se vean nítidos.
 */
public class GeneradorGraficos {

    private static final int ANCHO = 1600;
    private static final int ALTO = 800;
    private static final Color AZUL = new Color(23, 51, 79);
    private static final Color NARANJA = new Color(242, 140, 40);

    private static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 30);
    private static final Font FUENTE_EJE = new Font("SansSerif", Font.PLAIN, 24);
    private static final Font FUENTE_MARCAS = new Font("SansSerif", Font.PLAIN, 22);

    /** Barras con la carga de cada semana. */
    public static File cargaSemanal(List<Calculos.Semana> semanas) throws IOException {
        // Se mete cada dato en el dataset: valor, serie (rowKey) y categoría (columnKey)
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int n = 1;
        for (Calculos.Semana s : semanas) {
            dataset.addValue(s.carga(), "Carga semanal (UA)", "S" + n++);
        }
        JFreeChart grafico = ChartFactory.createBarChart("Carga semanal", "Semana", "Carga (UA)", dataset);
        estiloComun(grafico);

        CategoryPlot plot = grafico.getCategoryPlot();
        plot.getDomainAxis().setLabelFont(FUENTE_EJE);
        plot.getDomainAxis().setTickLabelFont(FUENTE_MARCAS);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        plot.getRangeAxis().setLabelFont(FUENTE_EJE);
        plot.getRangeAxis().setTickLabelFont(FUENTE_MARCAS);
        BarRenderer barras = (BarRenderer) plot.getRenderer();
        barras.setBarPainter(new StandardBarPainter()); // sin degradado
        barras.setShadowVisible(false);
        barras.setSeriesPaint(0, AZUL);
        barras.setMaximumBarWidth(0.08);
        return guardar(grafico);
    }

    /**
     * Marcas reales (puntos) frente a la recta ajustada (línea). Si hay recta, la línea se alarga hasta
     * la semana siguiente al último test y ahí se marca la predicción (extrapolación).
     */
    public static File evolucionMarca(DatosInforme d) throws IOException {
        XYSeries reales = new XYSeries("Marca real");
        for (int i = 0; i < d.tests.size(); i++) {
            reales.add(d.x[i], d.tests.get(i).segundos());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(reales);

        if (d.hayRecta()) {
            double xFinal = d.x[d.x.length - 1] + 1;
            XYSeries recta = new XYSeries("Recta ajustada");
            recta.add(0, d.regresion[0]);
            recta.add(xFinal, d.prediccion);
            XYSeries prediccion = new XYSeries("Predicción (extrapolación)");
            prediccion.add(xFinal, d.prediccion);
            dataset.addSeries(recta);
            dataset.addSeries(prediccion);
        }

        JFreeChart grafico = ChartFactory.createXYLineChart("Evolución de la marca en 5 km",
                "Semanas desde el primer test", "Marca (s)", dataset, PlotOrientation.VERTICAL, true, false, false);
        estiloComun(grafico);

        XYPlot plot = grafico.getXYPlot();
        plot.getDomainAxis().setLabelFont(FUENTE_EJE);
        plot.getDomainAxis().setTickLabelFont(FUENTE_MARCAS);
        plot.getRangeAxis().setLabelFont(FUENTE_EJE);
        plot.getRangeAxis().setTickLabelFont(FUENTE_MARCAS);
        ((NumberAxis) plot.getRangeAxis()).setAutoRangeIncludesZero(false); // el eje no empieza en 0
        ((NumberAxis) plot.getDomainAxis()).setAutoRangeIncludesZero(false);
        ((NumberAxis) plot.getDomainAxis()).setAutoRangeStickyZero(false);  // si no, el eje se "pega" al 0 y corta el primer punto
        plot.getDomainAxis().setLowerMargin(0.04);                          // para que no se corten los puntos de los bordes
        plot.getDomainAxis().setUpperMargin(0.04);

        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
        r.setSeriesLinesVisible(0, false);                       // marcas reales: solo puntos
        r.setSeriesShapesVisible(0, true);
        r.setSeriesShape(0, new Ellipse2D.Double(-8, -8, 16, 16));
        r.setSeriesPaint(0, AZUL);
        if (d.hayRecta()) {
            r.setSeriesLinesVisible(1, true);                    // recta: solo línea
            r.setSeriesShapesVisible(1, false);
            r.setSeriesPaint(1, NARANJA);
            r.setSeriesStroke(1, new BasicStroke(4f));
            r.setSeriesLinesVisible(2, false);                   // predicción: un punto
            r.setSeriesShapesVisible(2, true);
            r.setSeriesShape(2, new Ellipse2D.Double(-9, -9, 18, 18));
            r.setSeriesPaint(2, Color.RED);
        }
        plot.setRenderer(r);
        return guardar(grafico);
    }

    private static void estiloComun(JFreeChart grafico) {
        grafico.setBackgroundPaint(Color.WHITE);
        grafico.getTitle().setFont(FUENTE_TITULO);
        if (grafico.getLegend() != null) {
            grafico.getLegend().setItemFont(FUENTE_EJE);
        }
        grafico.getPlot().setBackgroundPaint(Color.WHITE);
        if (grafico.getPlot() instanceof CategoryPlot plot) {
            plot.setRangeGridlinePaint(new Color(210, 210, 210));
        } else if (grafico.getPlot() instanceof XYPlot plot) {
            plot.setRangeGridlinePaint(new Color(210, 210, 210));
            plot.setDomainGridlinePaint(new Color(230, 230, 230));
        }
    }

    /** Guarda el gráfico como PNG en un archivo temporal (el servicio lo borra después). */
    private static File guardar(JFreeChart grafico) throws IOException {
        File archivo = Files.createTempFile("grafico_", ".png").toFile();
        ChartUtils.saveChartAsPNG(archivo, grafico, ANCHO, ALTO);
        return archivo;
    }
}
