# Informe PDF: cómo está hecho

## Estructura
| Clase | Qué hace |
|---|---|
| `PanelInforme` | Nueva pestaña «Informe PDF» de la ventana: deportista, periodo y botón. Usa `JFileChooser` y muestra los errores y la confirmación |
| `ServicioInforme` | Pide los datos, comprueba que hay información suficiente (al menos 1 sesión y 1 test en el periodo) y ordena qué se dibuja: portada, resumen, entrenamiento, rendimiento e interpretación |
| `DatosInforme` | Agrupa los datos ya calculados. **Todo sale de `Calculos`** (la misma clase de la ventana y el Excel): el PDF no tiene ninguna fórmula propia |
| `GeneradorPDF` | Maquetación con PDFBox: páginas, textos, líneas, imágenes, tablas y pie de página |
| `GeneradorGraficos` | Gráficos con JFreeChart guardados como PNG |

Para ampliar la primera parte solo se tocó `Calculos`: `Semana` guarda ahora el nº de sesiones y se añadió `variacionCarga`.

## Texto e imágenes
- **Texto**: fuentes `.ttf` (Liberation Sans normal, negrita y cursiva) cargadas con `PDType0Font.load`; se escribe con `beginText`, `setFont`, `setNonStrokingColor`, `newLineAtOffset` y `showText`. Hay 5 jerarquías (título 32 pt, sección 17, subtítulo 12, párrafo 10,5 y notas 8,5), negrita, cursiva y alineación izquierda, centrada y derecha (`textoCentrado`, `textoDerecha`).
- **Párrafos**: `parrafo()` calcula el ancho de cada palabra con `getStringWidth` y parte las líneas para no pasar del margen.
- **Imágenes**: `PDImageXObject.createFromFile` y `drawImage`. `dibujarImagen()` usa **un único factor de escala** `min(maxAncho/ancho, maxAlto/alto)`, así nunca se deforman. El logotipo sale en la portada y en el encabezado, y la fotografía en la portada (`recursos/fotos/<id>.png|jpg`; si no existe se usa `recursos/generica.png`). Si falta una imagen o no es válida, `cargarImagen()` devuelve `null`, añade un aviso y el informe se genera igual.
- **Gráficos**: carga semanal (barras) y evolución de la marca con la recta ajustada y la predicción. Se dibujan a 1600×800 px, se guardan en PNG temporal, se insertan como imagen y se borran.
- **Tablas**: `tabla()` con cabecera azul, filas alternadas, bordes y columnas numéricas a la derecha (resumen semanal con totales e histórico de tests).

## Paginación
El cursor `y` baja con cada elemento. Antes de dibujar, `asegurarEspacio(alto)` comprueba si cabe por encima del margen inferior y, si no, llama a `nuevaPagina()`. Las tablas **repiten la cabecera** al continuar en la página siguiente. Las páginas 2 en adelante llevan encabezado (logo y nombre); al final se recorren todas las páginas para poner «Página x de n». Con los datos de prueba salen 4 páginas.

## Pruebas
`InformeTest` genera el informe de **dos deportistas distintos** (Lucía y Carlos) y comprueba que cada PDF tiene 3–5 páginas, solo contiene los datos de su deportista, usa los mismos números que `Calculos` y que **ningún texto sale de los márgenes**. También prueba los datos insuficientes, la ausencia o invalidez de imágenes, la proporción de las imágenes y los errores de escritura. Los PDF de ejemplo están en `ejemplos/`.

> Los indicadores son herramientas didácticas: no son diagnósticos médicos ni recomendaciones de entrenamiento.
