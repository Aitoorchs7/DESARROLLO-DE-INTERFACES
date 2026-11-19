import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Toda la parte matemática (apartados 3 y 4 del enunciado).
 *
 * CASOS ESPECIALES: cuando un indicador no se puede calcular (datos insuficientes,
 * desviación 0, media 0...) se devuelve Double.NaN. Quien lo muestra escribe "n/d"
 * en vez del número. Así nunca se divide por cero sin controlar.
 *
 * Los indicadores son herramientas didácticas: no son diagnósticos ni prescripciones.
 */
public class Calculos {

    /** Una semana (lunes a domingo) con sus indicadores. */
    public record Semana(LocalDate lunes, double km, int minutos, double carga,
                         double mediaMovil, double monotonia, double tension) {
    }

    // ================================================================ 3.1 - 3.3 CARGA SEMANAL

    public static LocalDate lunesDe(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Calcula las semanas desde la primera sesión hasta la última. Las semanas sin sesiones
     * valen 0 (así la media móvil siempre usa semanas consecutivas).
     */
    public static List<Semana> semanas(List<Sesion> sesiones) {
        List<Semana> res = new ArrayList<>();
        if (sesiones.isEmpty()) {
            return res;
        }
        LocalDate primera = lunesDe(sesiones.get(0).fecha());
        LocalDate ultima = primera;
        for (Sesion s : sesiones) {
            if (lunesDe(s.fecha()).isBefore(primera)) {
                primera = lunesDe(s.fecha());
            }
            if (lunesDe(s.fecha()).isAfter(ultima)) {
                ultima = lunesDe(s.fecha());
            }
        }

        for (LocalDate lunes = primera; !lunes.isAfter(ultima); lunes = lunes.plusWeeks(1)) {
            double[] cargaDia = new double[7]; // lunes..domingo; los días de descanso se quedan en 0
            double km = 0;
            int minutos = 0;
            for (Sesion s : sesiones) {
                if (lunesDe(s.fecha()).equals(lunes)) {
                    cargaDia[s.fecha().getDayOfWeek().getValue() - 1] += s.carga();
                    km += s.km();
                    minutos += s.minutos();
                }
            }
            double carga = 0;
            for (double c : cargaDia) {
                carga += c;
            }

            // Media móvil: MM(t) = [C(t) + C(t-1) + C(t-2)] / 3. Hacen falta 3 semanas -> si no, NaN
            double mm = Double.NaN;
            int n = res.size();
            if (n >= 2) {
                mm = (carga + res.get(n - 1).carga() + res.get(n - 2).carga()) / 3.0;
            }

            double mono = monotonia(cargaDia);
            double tension = Double.isNaN(mono) ? Double.NaN : carga * mono;
            res.add(new Semana(lunes, km, minutos, carga, mm, mono, tension));
        }
        return res;
    }

    /**
     * Monotonía = media de la carga diaria / desviación típica diaria (7 días).
     * Se usa la desviación poblacional (n) porque los 7 días son la semana completa.
     * Si la desviación es 0 NO se divide: devuelve NaN.
     */
    public static double monotonia(double[] cargaDia) {
        double media = media(cargaDia);
        double suma = 0;
        for (double c : cargaDia) {
            suma += (c - media) * (c - media);
        }
        double desviacion = Math.sqrt(suma / cargaDia.length);
        if (desviacion < 1e-9) {
            return Double.NaN;
        }
        return media / desviacion;
    }

    // ================================================================ 3.4 ESTADÍSTICA DESCRIPTIVA

    public static double media(double[] x) {
        if (x.length == 0) {
            return Double.NaN;
        }
        double suma = 0;
        for (double v : x) {
            suma += v;
        }
        return suma / x.length;
    }

    public static double mediana(double[] x) {
        if (x.length == 0) {
            return Double.NaN;
        }
        double[] orden = x.clone();
        Arrays.sort(orden);
        int m = orden.length / 2;
        return orden.length % 2 == 1 ? orden[m] : (orden[m - 1] + orden[m]) / 2.0;
    }

    public static double minimo(double[] x) {
        return x.length == 0 ? Double.NaN : Arrays.stream(x).min().getAsDouble();
    }

    public static double maximo(double[] x) {
        return x.length == 0 ? Double.NaN : Arrays.stream(x).max().getAsDouble();
    }

    /** Desviación típica muestral: raíz( Σ(x - media)² / (n - 1) ). Con un solo dato no está definida. */
    public static double desviacion(double[] x) {
        if (x.length < 2) {
            return Double.NaN;
        }
        double media = media(x);
        double suma = 0;
        for (double v : x) {
            suma += (v - media) * (v - media);
        }
        return Math.sqrt(suma / (x.length - 1));
    }

    /** CV = (desviación típica / media) × 100. Si la media es 0 no está definido. */
    public static double cv(double[] x) {
        double media = media(x);
        if (Double.isNaN(media) || Math.abs(media) < 1e-12) {
            return Double.NaN;
        }
        return desviacion(x) / media * 100; // NaN si n < 2
    }

    public static double[] cargas(List<Semana> semanas) {
        double[] r = new double[semanas.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = semanas.get(i).carga();
        }
        return r;
    }

    public static double[] distancias(List<Sesion> sesiones) {
        double[] r = new double[sesiones.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = sesiones.get(i).km();
        }
        return r;
    }

    // ================================================================ 4.1 REGRESIÓN LINEAL

    /** x de cada test = semanas transcurridas desde el primer test. */
    public static double[] semanasDesdePrimero(List<Test5k> tests) {
        double[] x = new double[tests.size()];
        for (int i = 0; i < x.length; i++) {
            x[i] = ChronoUnit.DAYS.between(tests.get(0).fecha(), tests.get(i).fecha()) / 7.0;
        }
        return x;
    }

    public static double[] marcas(List<Test5k> tests) {
        double[] y = new double[tests.size()];
        for (int i = 0; i < y.length; i++) {
            y[i] = tests.get(i).segundos();
        }
        return y;
    }

    /**
     * Recta de mínimos cuadrados y = a + b·x. Devuelve {a, b, R²}.
     * Con menos de 2 puntos, o con todos los x iguales (denominador 0), devuelve NaN.
     * R² es NaN si todas las y son iguales (no hay variabilidad que explicar).
     */
    public static double[] regresion(double[] x, double[] y) {
        int n = x.length;
        double[] noDefinida = {Double.NaN, Double.NaN, Double.NaN};
        if (n < 2) {
            return noDefinida;
        }
        double sx = 0, sy = 0, sxy = 0, sxx = 0;
        for (int i = 0; i < n; i++) {
            sx += x[i];
            sy += y[i];
            sxy += x[i] * y[i];
            sxx += x[i] * x[i];
        }
        double denominador = n * sxx - sx * sx;
        if (Math.abs(denominador) < 1e-12) {
            return noDefinida;
        }
        double b = (n * sxy - sx * sy) / denominador;
        double a = sy / n - b * sx / n;

        // R² = 1 - (suma de residuos²) / (suma total de cuadrados)
        double residuos = 0, total = 0;
        for (int i = 0; i < n; i++) {
            residuos += (y[i] - (a + b * x[i])) * (y[i] - (a + b * x[i]));
            total += (y[i] - sy / n) * (y[i] - sy / n);
        }
        double r2 = total < 1e-12 ? Double.NaN : 1 - residuos / total;
        return new double[]{a, b, r2};
    }

    // ================================================================ 4.2 CORRELACIÓN DE PEARSON

    /** r de Pearson. NaN con menos de 3 pares o si una de las series es constante. */
    public static double pearson(double[] x, double[] y) {
        int n = x.length;
        if (n < 3) {
            return Double.NaN;
        }
        double mx = media(x), my = media(y);
        double num = 0, sx = 0, sy = 0;
        for (int i = 0; i < n; i++) {
            num += (x[i] - mx) * (y[i] - my);
            sx += (x[i] - mx) * (x[i] - mx);
            sy += (y[i] - my) * (y[i] - my);
        }
        double den = Math.sqrt(sx * sy);
        return den < 1e-12 ? Double.NaN : num / den;
    }

    /**
     * Pares (carga de la semana en la que se hizo el test, marca del test).
     * Devuelve {cargas, marcas}. Solo cuentan los tests cuya semana está en la serie semanal.
     */
    public static double[][] cargaYMarca(List<Semana> semanas, List<Test5k> tests) {
        List<Double> cargas = new ArrayList<>();
        List<Double> marcas = new ArrayList<>();
        for (Test5k t : tests) {
            for (Semana s : semanas) {
                if (s.lunes().equals(lunesDe(t.fecha()))) {
                    cargas.add(s.carga());
                    marcas.add((double) t.segundos());
                }
            }
        }
        return new double[][]{
                cargas.stream().mapToDouble(Double::doubleValue).toArray(),
                marcas.stream().mapToDouble(Double::doubleValue).toArray()};
    }

    // ================================================================ 4.3 COMPARACIÓN (cada indicador por separado)

    /** Mejor marca de 5 km (la menor). NaN si no hay tests. */
    public static double mejorMarca(List<Test5k> tests) {
        return tests.isEmpty() ? Double.NaN : Arrays.stream(marcas(tests)).min().getAsDouble();
    }

    /** Evolución respecto al primer test = marca del primer test − marca del último (positivo = ha mejorado). */
    public static double evolucion(List<Test5k> tests) {
        if (tests.size() < 2) {
            return Double.NaN;
        }
        return tests.get(0).segundos() - tests.get(tests.size() - 1).segundos();
    }

    public static double kmSemanalesMedios(List<Semana> semanas) {
        double[] km = new double[semanas.size()];
        for (int i = 0; i < km.length; i++) {
            km[i] = semanas.get(i).km();
        }
        return media(km);
    }

    public static double cargaSemanalMedia(List<Semana> semanas) {
        return media(cargas(semanas));
    }
}
