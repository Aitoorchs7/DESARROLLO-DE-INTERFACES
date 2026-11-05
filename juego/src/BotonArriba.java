// FLECHA ARRIBA
public class BotonArriba extends BotonDireccion {

    public BotonArriba(PanelJuego escenario) {
        super("↑", escenario);
    }

    @Override
    protected void mover(boolean pulsado) {
        escenario.pulsarArriba(pulsado);
    }
}
