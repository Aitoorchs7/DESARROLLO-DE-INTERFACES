// FLECHA IZQUIERDA
public class BotonIzquierda extends BotonDireccion {

    public BotonIzquierda(PanelJuego escenario) {
        super("←", escenario);
    }

    @Override
    protected void mover(boolean pulsado) {
        escenario.pulsarIzquierda(pulsado);
    }
}
