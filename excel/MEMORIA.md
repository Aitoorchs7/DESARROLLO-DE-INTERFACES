# Memoria: análisis de rendimiento deportivo

## Diseño
- `Deportista`, `Sesion`, `Test5k`: datos. `Datos`: listas + **validaciones** (todo error lanza `IllegalArgumentException` con mensaje y la ventana lo muestra).
- `Calculos`: toda la matemática, sin Swing ni POI, para poder probarla con JUnit.
- `InformeExcel`: genera el libro con POI (`XSSFWorkbook`). `Ventana`: Swing con 5 pestañas.
- Los cálculos se hacen en Java; Excel solo los presenta, con fórmulas sencillas: carga de cada sesión (`=duración×RPE`) y totales del Resumen (`SUM` sobre la hoja Entrenamientos).
- **Casos especiales**: un indicador no calculable es `NaN` y se muestra «n/d» o «datos insuficientes». Nunca se divide por cero.

## Fórmulas
| Indicador | Cómo se calcula | Caso especial |
|---|---|---|
| Carga de sesión | duración × RPE (UA) | |
| Carga semanal / km / tiempo | suma de la semana (lunes–domingo); semanas sin sesiones = 0 | |
| Media móvil | [C(t)+C(t−1)+C(t−2)]/3 | 2 primeras semanas: «datos insuficientes» |
| Monotonía y tensión | media diaria / desviación diaria (7 días, descansos = 0); tensión = carga × monotonía | desviación 0 → no definida |
| Estadística | media, mediana, mín., máx., desviación muestral, CV = desv./media×100 | media 0 o n < 2 → «n/d» |
| Regresión | b = [nΣxy−ΣxΣy]/[nΣx²−(Σx)²], a = ȳ−b·x̄, R² = 1−SCres/SCtotal; x = semanas desde el 1.er test | n < 2 o x iguales → «n/d» |
| Pearson | Σ(dx·dy)/√(Σdx²·Σdy²), con (carga de la semana del test, marca) | < 3 pares o serie constante → «n/d» |
| Comparativa | mejor marca, evolución (1.er test − último), km semanales medios y carga semanal media, **cada uno por separado** (no se crea puntuación compuesta) | |

La monotonía usa desviación poblacional (los 7 días son toda la semana); la estadística descriptiva usa la muestral (n−1).

## Caso de prueba calculado a mano (`CalculosTest.regresionLineal`)
x = 0, 1, 2, 3 y y = 1500, 1490, 1470, 1460: Σx = 6, Σy = 5920, Σxy = 8810, Σx² = 14.
b = (4·8810 − 6·5920)/(4·14 − 36) = −280/20 = **−14**; a = 1480 + 14·1,5 = **1501**; residuos −1, 3, −3, 1 → 20 y suma total 1000 → **R² = 0,98**; para x = 4: 1501 − 56 = **1445 s**. Java da lo mismo. Hay otros 9 tests con cuentas a mano (monotonía = 40/20 = 2, tensión 560, Pearson = 6/√60…).

## Interpretación (datos de prueba)
- **Lucía** mejora 42 s (b = −5,15 s/semana, R² = 0,99) y su carga sube semana a semana; r = −0,996. Es una asociación muy fuerte, pero con 5 tests y dos magnitudes que evolucionan con el tiempo **no se puede concluir que la carga cause la mejora**.
- **Carlos** mejora 20 s de forma irregular (R² = 0,73) y r = +0,73: sin una relación clara entre carga y marca.
- **Marta** no cambia (b = 0,15, R² = 0,09; r = 0,30): la recta explica casi nada y su predicción es poco fiable.
- La predicción de la semana siguiente es una **extrapolación**, no una garantía. Son indicadores didácticos, no diagnósticos ni prescripciones de entrenamiento.
