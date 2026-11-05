// FLECHA ABAJO
public class BotonAbajo extends BotonDireccion {

    public BotonAbajo(PanelJuego escenario) {
        super("↓", escenario);
    }

    @Override
    protected void mover(boolean pulsado) {
        escenario.pulsarAbajo(pulsado);
    }
}
