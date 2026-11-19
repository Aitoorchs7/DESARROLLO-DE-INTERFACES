import java.time.LocalDate;
import java.util.List;

/**
 * Los datos YA CALCULADOS que necesita el PDF. Todos los números salen de la clase Calculos
 * (la misma que usan la ventana y el Excel): aquí no hay ninguna fórmula nueva.
 */
public class DatosInforme {

    public final Deportista deportista;
    public final LocalDate desde, hasta;
    public final List<Sesion> sesiones;
    public final List<Test5k> tests;
    public final List<Calculos.Semana> semanas;

    // Totales del periodo (suma de las semanas)
    public final int numSesiones, minutos;
    public final double km, carga;

    // Indicadores (NaN = no definido / datos insuficientes)
    public final double mejorMarca, evolucion, kmSemanalesMedios, cargaSemanalMedia, cvCarga, variacionCarga;
    public final double[] x;          // semanas desde el primer test de cada test
    public final double[] regresion;  // {a, b, R²}
    public final double prediccion;   // marca estimada la semana siguiente al último test
    public final double pearson;
    public final int paresPearson;

    public DatosInforme(Deportista d, LocalDate desde, LocalDate hasta, List<Sesion> sesiones, List<Test5k> tests) {
        this.deportista = d;
        this.desde = desde;
        this.hasta = hasta;
        this.sesiones = sesiones;
        this.tests = tests;
        this.semanas = Calculos.semanas(sesiones);

        int ns = 0, min = 0;
        double k = 0, c = 0;
        for (Calculos.Semana s : semanas) {
            ns += s.sesiones();
            min += s.minutos();
            k += s.km();
            c += s.carga();
        }
        numSesiones = ns;
        minutos = min;
        km = k;
        carga = c;

        mejorMarca = Calculos.mejorMarca(tests);
        evolucion = Calculos.evolucion(tests);
        kmSemanalesMedios = Calculos.kmSemanalesMedios(semanas);
        cargaSemanalMedia = Calculos.cargaSemanalMedia(semanas);
        cvCarga = Calculos.cv(Calculos.cargas(semanas));
        variacionCarga = Calculos.variacionCarga(semanas);

        x = tests.isEmpty() ? new double[0] : Calculos.semanasDesdePrimero(tests);
        regresion = Calculos.regresion(x, Calculos.marcas(tests));
        prediccion = (tests.isEmpty() || Double.isNaN(regresion[1])) ? Double.NaN
                : regresion[0] + regresion[1] * (x[x.length - 1] + 1);

        double[][] pares = Calculos.cargaYMarca(semanas, tests);
        pearson = Calculos.pearson(pares[0], pares[1]);
        paresPearson = pares[0].length;
    }

    /** ¿Hay recta de regresión (al menos 2 tests en fechas distintas)? */
    public boolean hayRecta() {
        return !Double.isNaN(regresion[1]);
    }
}
