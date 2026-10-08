import javax.swing.JFrame;

public class Tienda extends JFrame {

    private final Manager manager;

    // Provisional: solo recibe el manager para poder probar Inicio
    public Tienda(Manager manager) {
        this.manager = manager;
        setTitle("Mercado - " + manager.nombre);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 700);
        setResizable(false);
        setLocationRelativeTo(null);

        
    }
}
