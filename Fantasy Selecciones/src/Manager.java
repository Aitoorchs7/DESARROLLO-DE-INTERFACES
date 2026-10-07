import java.util.List;
import java.util.ArrayList;

public class Manager {
    String nombre;
    int presupuesto = 120;
    List<Jugador> plantilla = new ArrayList<>();

    public Manager(String nombre) {
        this.nombre = nombre;
    }

    String fichar(Jugador j) {
        if (j.precio <= presupuesto && plantilla.size() < 11 && !plantilla.contains(j) && !maximo(j.posicion)) {
            presupuesto -= j.precio;
            plantilla.add(j);
            return "Fichado";
        } else {
            return "No fichado";
        }
    }

    String vender(Jugador j) {
        if (plantilla.contains(j)) {
            presupuesto += j.precio;
            plantilla.remove(j);
            return "Vendido";
        } else {
            return "No vendido";
        }
    }

    int puntosTotales() {
        int puntos = 0;
        for (Jugador j : plantilla) {
            puntos += j.puntos;
        }
        return puntos;
    }

    void mostrarPlantilla() {
        System.out.println(
                "Nombre " + " | " + "Posicion " + " | " + "Seleccion " + " | " + "Precio " + " | " + "Puntos");
        for (Jugador j : plantilla) {
            System.out.println(j.nombre + " | " + j.posicion + " | " + j.seleccion + " | " + j.precio + " | "
                    + j.puntos);
        }
        System.out.println("Puntos totales: " + puntosTotales());
        System.out.println("Presupuesto: " + presupuesto);
    }

    int contar(String posicion) {
        int contador = 0;
        for (Jugador j : plantilla) {
            if (j.posicion.equals(posicion)) {
                contador++;
            }
        }
        return contador;
    }

    boolean maximo(String posicion) {
        if (posicion.equals("PORTERO") && contar(posicion) >= 1) {
            return true;
        } else if (posicion.equals("DEFENSA") && contar(posicion) >= 4) {
            return true;
        } else if (posicion.equals("MEDIO") && contar(posicion) >= 3) {
            return true;
        } else if (posicion.equals("DELANTERO") && contar(posicion) >= 3) {
            return true;
        } else {
            return false;
        }
    }

}
