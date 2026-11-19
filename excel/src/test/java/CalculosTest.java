import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Casos de prueba CALCULADOS A MANO (entregable del enunciado).
 * En cada test, el comentario muestra la cuenta hecha con papel y lápiz.
 */
class CalculosTest {

    // 01/01/2024 fue lunes
    private static Sesion sesion(String fecha, int minutos, double km, int rpe) {
        return new Sesion(LocalDate.parse(fecha), "A01", "Rodaje", minutos, km, 140, rpe);
    }

    @Test
    void cargaDeSesion() {
        // 50 min × RPE 7 = 350 UA (ejemplo del enunciado)
        assertEquals(350, sesion("2024-01-01", 50, 8, 7).carga());
    }

    @Test
    void cargaSemanalKmYTiempo() {
        // Lunes: 60 × 5 = 300 UA, 10 km. Miércoles: 40 × 7 = 280 UA, 7 km. -> 580 UA, 17 km, 100 min
        List<Calculos.Semana> semanas = Calculos.semanas(List.of(
                sesion("2024-01-01", 60, 10, 5), sesion("2024-01-03", 40, 7, 7)));
        assertEquals(1, semanas.size());
        assertEquals(580, semanas.get(0).carga());
        assertEquals(17, semanas.get(0).km());
        assertEquals(100, semanas.get(0).minutos());
    }

    @Test
    void mediaMovilDeTresSemanas() {
        // Cargas semanales: 300, 400, 500, 600 (una sesión cada lunes, RPE 5)
        // MM(3.ª semana) = (500 + 400 + 300) / 3 = 400 ; MM(4.ª) = (600 + 500 + 400) / 3 = 500
        // Las dos primeras semanas no tienen 3 semanas completas -> NaN ("datos insuficientes")
        List<Calculos.Semana> s = Calculos.semanas(List.of(
                sesion("2024-01-01", 60, 10, 5), sesion("2024-01-08", 80, 13, 5),
                sesion("2024-01-15", 100, 16, 5), sesion("2024-01-22", 120, 20, 5)));
        assertTrue(Double.isNaN(s.get(0).mediaMovil()));
        assertTrue(Double.isNaN(s.get(1).mediaMovil()));
        assertEquals(400, s.get(2).mediaMovil(), 1e-9);
        assertEquals(500, s.get(3).mediaMovil(), 1e-9);
    }

    @Test
    void semanaSinSesionesCuentaComoCero() {
        // Semana 1: 60 × 5 = 300 ; semana 2: sin sesiones = 0 ; semana 3: 45 × 4 = 180
        // MM(3.ª) = (180 + 0 + 300) / 3 = 160
        List<Calculos.Semana> s = Calculos.semanas(List.of(sesion("2024-01-01", 60, 10, 5), sesion("2024-01-15", 45, 8, 4)));
        assertEquals(3, s.size());
        assertEquals(0, s.get(1).carga());
        assertEquals(160, s.get(2).mediaMovil(), 1e-9);
    }

    @Test
    void monotoniaYTension() {
        // Una sesión cada día de la semana con cargas 10, 20, 30, 40, 50, 60, 70 (RPE 1)
        // media = 280 / 7 = 40 ; desviaciones²: 900, 400, 100, 0, 100, 400, 900 -> suma 2800
        // desviación = raíz(2800 / 7) = raíz(400) = 20 ; monotonía = 40 / 20 = 2 ; tensión = 280 × 2 = 560
        List<Sesion> semana = new ArrayList<>();
        for (int dia = 0; dia < 7; dia++) {
            semana.add(sesion("2024-01-0" + (1 + dia), 10 * (dia + 1), 2 * (dia + 1), 1));
        }
        Calculos.Semana s = Calculos.semanas(semana).get(0);
        assertEquals(280, s.carga());
        assertEquals(2.0, s.monotonia(), 1e-9);
        assertEquals(560, s.tension(), 1e-9);
    }

    @Test
    void monotoniaConDesviacionCeroNoDivide() {
        // Los 7 días con 50 UA: desviación 0 -> monotonía y tensión no definidas (NaN), sin dividir por cero
        List<Sesion> semana = new ArrayList<>();
        for (int dia = 0; dia < 7; dia++) {
            semana.add(sesion("2024-01-0" + (1 + dia), 50, 8, 1));
        }
        Calculos.Semana s = Calculos.semanas(semana).get(0);
        assertTrue(Double.isNaN(s.monotonia()));
        assertTrue(Double.isNaN(s.tension()));
    }

    @Test
    void estadisticaDescriptiva() {
        // Datos 10, 12, 14, 16, 18: media 14, mediana 14, mín 10, máx 18
        // Σ(x − media)² = 16 + 4 + 0 + 4 + 16 = 40 ; desviación muestral = raíz(40 / 4) = raíz(10) = 3,1623
        // CV = 3,1623 / 14 × 100 = 22,5877 %
        double[] x = {10, 12, 14, 16, 18};
        assertEquals(14, Calculos.media(x), 1e-9);
        assertEquals(14, Calculos.mediana(x), 1e-9);
        assertEquals(10, Calculos.minimo(x), 1e-9);
        assertEquals(18, Calculos.maximo(x), 1e-9);
        assertEquals(Math.sqrt(10), Calculos.desviacion(x), 1e-9);
        assertEquals(Math.sqrt(10) / 14 * 100, Calculos.cv(x), 1e-9);
        // casos especiales: media 0 -> CV no definido ; un solo dato -> desviación no definida
        assertTrue(Double.isNaN(Calculos.cv(new double[]{0, 0, 0})));
        assertTrue(Double.isNaN(Calculos.desviacion(new double[]{5})));
    }

    @Test
    void regresionLineal() {
        // x = 0, 1, 2, 3 ; y = 1500, 1490, 1470, 1460 ; n = 4
        // Σx = 6, Σy = 5920, Σxy = 0 + 1490 + 2940 + 4380 = 8810, Σx² = 14
        // b = (4·8810 − 6·5920) / (4·14 − 36) = −280 / 20 = −14
        // a = 1480 − (−14)(1,5) = 1501
        // ajustados 1501, 1487, 1473, 1459 -> residuos −1, 3, −3, 1 -> suma de cuadrados 20 ; SC total = 400 + 100 + 100 + 400 = 1000
        // R² = 1 − 20/1000 = 0,98 ; predicción x = 4: 1501 − 56 = 1445
        double[] r = Calculos.regresion(new double[]{0, 1, 2, 3}, new double[]{1500, 1490, 1470, 1460});
        assertEquals(1501, r[0], 1e-9);
        assertEquals(-14, r[1], 1e-9);
        assertEquals(0.98, r[2], 1e-9);
        assertEquals(1445, r[0] + r[1] * 4, 1e-9);
        // casos especiales: un solo punto, o todos los x iguales
        assertTrue(Double.isNaN(Calculos.regresion(new double[]{1}, new double[]{1300})[1]));
        assertTrue(Double.isNaN(Calculos.regresion(new double[]{2, 2, 2}, new double[]{1, 2, 3})[1]));
    }

    @Test
    void pearson() {
        // x = 1..5 (media 3) ; y = 2, 4, 5, 4, 5 (media 4)
        // Σ(dx·dy) = 4 + 0 + 0 + 0 + 2 = 6 ; Σdx² = 10 ; Σdy² = 4 + 0 + 1 + 0 + 1 = 6
        // r = 6 / raíz(10·6) = 0,7746
        assertEquals(6 / Math.sqrt(60), Calculos.pearson(new double[]{1, 2, 3, 4, 5}, new double[]{2, 4, 5, 4, 5}), 1e-9);
        assertEquals(-1, Calculos.pearson(new double[]{1, 2, 3}, new double[]{9, 6, 3}), 1e-9);
        // casos especiales: serie constante y menos de 3 pares
        assertTrue(Double.isNaN(Calculos.pearson(new double[]{1, 2, 3}, new double[]{5, 5, 5})));
        assertTrue(Double.isNaN(Calculos.pearson(new double[]{1, 2}, new double[]{3, 4})));
    }

    @Test
    void validaciones() {
        Datos datos = new Datos();
        datos.altaDeportista(new Deportista("A01", "Lucía", 24, "Sénior", "10 km", 1300));
        LocalDate hoy = LocalDate.now();

        // RPE fuera de 1-10, valores no positivos, deportista inexistente, fecha futura, ritmo imposible, intervalo al revés
        assertThrows(IllegalArgumentException.class, () -> datos.registrarSesion(new Sesion(hoy, "A01", "Rodaje", 50, 8, 140, 11)));
        assertThrows(IllegalArgumentException.class, () -> datos.registrarSesion(new Sesion(hoy, "A01", "Rodaje", 0, 8, 140, 5)));
        assertThrows(IllegalArgumentException.class, () -> datos.registrarSesion(new Sesion(hoy, "A01", "Rodaje", 50, -2, 140, 5)));
        assertThrows(IllegalArgumentException.class, () -> datos.registrarSesion(new Sesion(hoy, "ZZZ", "Rodaje", 50, 8, 140, 5)));
        assertThrows(IllegalArgumentException.class, () -> datos.registrarSesion(new Sesion(hoy.plusDays(1), "A01", "Rodaje", 50, 8, 140, 5)));
        assertThrows(IllegalArgumentException.class, () -> datos.registrarSesion(new Sesion(hoy, "A01", "Rodaje", 10, 20, 140, 5)));
        assertThrows(IllegalArgumentException.class, () -> datos.sesionesDe("A01", hoy, hoy.minusDays(5)));
        // una sesión correcta sí entra
        datos.registrarSesion(new Sesion(hoy, "A01", "Rodaje", 50, 8, 140, 7));
        assertEquals(1, datos.sesiones.size());
    }
}
