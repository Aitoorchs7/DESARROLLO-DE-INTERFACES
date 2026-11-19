import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Guarda los datos en listas y valida todo lo que entra.
 * Si un dato no es válido lanza IllegalArgumentException con un mensaje claro
 * (la ventana lo muestra: no hay errores silenciosos).
 */
public class Datos {

    public final List<Deportista> deportistas = new ArrayList<>();
    public final List<Sesion> sesiones = new ArrayList<>();
    public final List<Test5k> tests = new ArrayList<>();

    // ---------------------------------------------------------------- deportistas

    public Deportista buscar(String id) {
        for (Deportista d : deportistas) {
            if (d.id().equals(id)) {
                return d;
            }
        }
        return null;
    }

    public void altaDeportista(Deportista d) {
        validarDeportista(d);
        if (buscar(d.id()) != null) {
            throw new IllegalArgumentException("Ya existe un deportista con el id " + d.id());
        }
        deportistas.add(d);
    }

    public void modificarDeportista(Deportista d) {
        validarDeportista(d);
        Deportista antiguo = buscar(d.id());
        if (antiguo == null) {
            throw new IllegalArgumentException("No existe el deportista " + d.id());
        }
        deportistas.set(deportistas.indexOf(antiguo), d);
    }

    /** Al eliminar un deportista se eliminan también sus sesiones y tests. */
    public void eliminarDeportista(String id) {
        if (buscar(id) == null) {
            throw new IllegalArgumentException("No existe el deportista " + id);
        }
        deportistas.remove(buscar(id));
        sesiones.removeIf(s -> s.idDeportista().equals(id));
        tests.removeIf(t -> t.idDeportista().equals(id));
    }

    private void validarDeportista(Deportista d) {
        if (d.id().isBlank()) {
            throw new IllegalArgumentException("El identificador es obligatorio.");
        }
        if (d.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (d.edad() < 8 || d.edad() > 100) {
            throw new IllegalArgumentException("La edad debe estar entre 8 y 100.");
        }
        if (d.categoria().isBlank() || d.objetivo().isBlank()) {
            throw new IllegalArgumentException("La categoría y el objetivo son obligatorios.");
        }
        if (d.marca5k() <= 0 || d.marca5k() > 7200) {
            throw new IllegalArgumentException("La marca inicial en 5 km debe ser positiva (en segundos, máximo 7200).");
        }
    }

    // ---------------------------------------------------------------- sesiones y tests

    public void registrarSesion(Sesion s) {
        if (buscar(s.idDeportista()) == null) {
            throw new IllegalArgumentException("El deportista no existe.");
        }
        validarFecha(s.fecha());
        if (s.tipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de sesión es obligatorio.");
        }
        if (s.minutos() <= 0) {
            throw new IllegalArgumentException("La duración debe ser positiva.");
        }
        if (!(s.km() > 0)) { // también rechaza NaN
            throw new IllegalArgumentException("La distancia debe ser positiva.");
        }
        if (s.fc() < 30 || s.fc() > 230) {
            throw new IllegalArgumentException("La frecuencia cardiaca debe estar entre 30 y 230 ppm.");
        }
        if (s.rpe() < 1 || s.rpe() > 10) {
            throw new IllegalArgumentException("El RPE debe estar entre 1 y 10.");
        }
        // distancia y duración compatibles: el ritmo (min/km) tiene que ser posible
        double ritmo = s.minutos() / s.km();
        if (ritmo < 2 || ritmo > 20) {
            throw new IllegalArgumentException(String.format(
                    "Distancia y duración incompatibles: ritmo de %.2f min/km (debe estar entre 2 y 20).", ritmo));
        }
        sesiones.add(s);
    }

    public void registrarTest(Test5k t) {
        if (buscar(t.idDeportista()) == null) {
            throw new IllegalArgumentException("El deportista no existe.");
        }
        validarFecha(t.fecha());
        if (t.segundos() <= 0 || t.segundos() > 7200) {
            throw new IllegalArgumentException("La marca debe ser positiva (en segundos, máximo 7200).");
        }
        for (Test5k otro : tests) {
            if (otro.idDeportista().equals(t.idDeportista()) && otro.fecha().equals(t.fecha())) {
                throw new IllegalArgumentException("Ese deportista ya tiene un test en esa fecha.");
            }
        }
        tests.add(t);
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser futura.");
        }
    }

    // ---------------------------------------------------------------- consultas

    /** Sesiones de un deportista entre dos fechas (incluidas), ordenadas. Una fecha null = sin límite. */
    public List<Sesion> sesionesDe(String id, LocalDate desde, LocalDate hasta) {
        validarIntervalo(desde, hasta);
        List<Sesion> res = new ArrayList<>();
        for (Sesion s : sesiones) {
            if (s.idDeportista().equals(id) && dentro(s.fecha(), desde, hasta)) {
                res.add(s);
            }
        }
        res.sort(Comparator.comparing(Sesion::fecha));
        return res;
    }

    public List<Test5k> testsDe(String id, LocalDate desde, LocalDate hasta) {
        validarIntervalo(desde, hasta);
        List<Test5k> res = new ArrayList<>();
        for (Test5k t : tests) {
            if (t.idDeportista().equals(id) && dentro(t.fecha(), desde, hasta)) {
                res.add(t);
            }
        }
        res.sort(Comparator.comparing(Test5k::fecha));
        return res;
    }

    private void validarIntervalo(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final es anterior a la inicial.");
        }
    }

    private boolean dentro(LocalDate f, LocalDate desde, LocalDate hasta) {
        return (desde == null || !f.isBefore(desde)) && (hasta == null || !f.isAfter(hasta));
    }

    // ---------------------------------------------------------------- datos de prueba

    /** 3 atletas, 9 semanas de entrenamiento (3 sesiones/semana) y un test de 5 km cada 2 semanas. */
    public void cargarPrueba() {
        deportistas.clear();
        sesiones.clear();
        tests.clear();

        altaDeportista(new Deportista("A01", "Lucía Ramos", 24, "Sénior", "Media maratón", 1290));
        altaDeportista(new Deportista("A02", "Carlos Méndez", 31, "Sénior", "10 km", 1380));
        altaDeportista(new Deportista("A03", "Marta Gil", 17, "Juvenil", "5 km pista", 1350));

        LocalDate inicio = LocalDate.of(2026, 8, 31); // es lunes
        String[] ids = {"A01", "A02", "A03"};
        int[] minutosBase = {40, 50, 45};  // minutos de una sesión normal de cada atleta
        // Marcas de los 5 tests (semanas 0, 2, 4, 6 y 8)
        int[][] marcas = {
                {1290, 1281, 1270, 1262, 1248}, // Lucía mejora
                {1380, 1372, 1379, 1365, 1360}, // Carlos mejora de forma irregular
                {1350, 1348, 1352, 1349, 1351}  // Marta se mantiene
        };
        for (int a = 0; a < ids.length; a++) {
            for (int semana = 0; semana < 9; semana++) {
                // La carga de Lucía sube cada semana; la de Carlos y Marta sube y baja de forma irregular
                int extra = (a == 0) ? semana * 3 : ((semana * 3 + a * 2) % 5 - 2) * 6;
                if (a == 1 && semana == 5) {
                    continue; // Carlos descansa una semana entera
                }
                // martes: rodaje suave, jueves: series, domingo: tirada larga
                registrarSesion(new Sesion(inicio.plusWeeks(semana).plusDays(1), ids[a], "Rodaje",
                        minutosBase[a] + extra, (minutosBase[a] + extra) / 6.0, 140, 4));
                registrarSesion(new Sesion(inicio.plusWeeks(semana).plusDays(3), ids[a], "Series",
                        minutosBase[a] + extra, (minutosBase[a] + extra) / 5.0, 168, 8));
                registrarSesion(new Sesion(inicio.plusWeeks(semana).plusDays(6), ids[a], "Fondo",
                        minutosBase[a] * 2 + extra, (minutosBase[a] * 2 + extra) / 6.2, 150, 6));
            }
            for (int t = 0; t < 5; t++) {
                registrarTest(new Test5k(inicio.plusWeeks(2L * t).plusDays(5), ids[a], marcas[a][t]));
            }
        }
    }
}
