import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

/** Carga de imágenes: del proyecto (classpath) o desde internet. */
public class Recursos {

    // Para no descargar ni escalar dos veces la misma imagen
    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    /**
     * Pone en la etiqueta una imagen escalada a la altura indicada.
     * Si la ruta empieza por "http" se descarga de internet (en segundo plano,
     * para no bloquear la ventana); si no, se carga del proyecto.
     */
    public static void cargar(String ruta, JLabel etiqueta, int alto) {
        String clave = ruta + "@" + alto;
        ImageIcon cacheada = CACHE.get(clave);
        if (cacheada != null) {
            etiqueta.setIcon(cacheada);
            return;
        }
        etiqueta.setText("...");
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                BufferedImage img;
                if (ruta.startsWith("http")) {
                    // Wikimedia rechaza el nombre por defecto de Java: usamos uno propio
                    HttpURLConnection c = (HttpURLConnection) URI.create(ruta).toURL().openConnection();
                    c.setRequestProperty("User-Agent", "FantasySelecciones/1.0 (clase DAM)");
                    c.setConnectTimeout(8000);
                    c.setReadTimeout(8000);
                    img = ImageIO.read(c.getInputStream());
                } else {
                    URL url = Recursos.class.getResource(ruta);
                    img = ImageIO.read(url);
                }
                return escalarPorAlto(new ImageIcon(img), alto);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icono = get();
                    CACHE.put(clave, icono);
                    etiqueta.setText(null);
                    etiqueta.setIcon(icono);
                } catch (Exception e) {
                    etiqueta.setText("sin imagen");
                }
            }
        }.execute();
    }

    /** Escala manteniendo la relación de aspecto. */
    private static ImageIcon escalarPorAlto(ImageIcon icono, int alto) {
        int ancho = alto * icono.getIconWidth() / icono.getIconHeight();
        Image escalada = icono.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(escalada);
    }
}
