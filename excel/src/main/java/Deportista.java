// record = clase de datos inmutable con constructor y getters automáticos (id(), nombre()...)
public record Deportista(String id, String nombre, int edad, String categoria, String objetivo, int marca5k) {

    @Override
    public String toString() {
        return id + " - " + nombre; // así se ve en los JComboBox
    }
}
