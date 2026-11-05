// FLECHA DERECHA
public class BotonDerecha extends BotonDireccion {

    public BotonDerecha(PanelJuego escenario) {
        super("→", escenario);
    }

    @Override
    protected void mover(boolean pulsado) {
        escenario.pulsarDerecha(pulsado);
    }
}
