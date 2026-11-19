import java.time.LocalDate;

public record Sesion(LocalDate fecha, String idDeportista, String tipo, int minutos, double km, int fc, int rpe) {

    /** Carga de la sesión (método sesión-RPE) = duración (min) × RPE, en UA. Ej.: 50 × 7 = 350 UA. */
    public double carga() {
        return minutos * rpe;
    }
}
