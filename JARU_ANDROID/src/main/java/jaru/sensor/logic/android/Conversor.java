package jaru.sensor.logic.android;

import android.util.Log;

/**
 * Clase que permite guardar valores de ángulos y desvíos. A partir de ahí, realiza correcciones
 * de dichos ángulos en función del valor de desvío. También se cuenta con métodos para la conversión
 * de grados entre sexagesimal y centesimal
 * Created by JAVI on 4/09/13.
 */
public class Conversor {
    //Valor del ángulo de desvío
    private static double nDesvio = 0.0;
    //Valor del ángulo
    private static double nGrados = 0.0;

    /**
     * Devuelve el valor del ángulo de desvío
     * @return double
     */
    public static double getNDesvio () {
        return nDesvio;
    }

    /**
     * Devuelve el valor del ángulo principal
     * @return double
     */
    public static double getNGrados () {
        return nGrados;
    }

    /**
     * Establece el valor del ángulo de desvío
     * @param pnValor double
     */
    public static void setNDesvio (double pnValor) {
        nDesvio = pnValor;
    }

    /**
     * Establece el valor del ángulo principal
     * @param pnValor double
     */
    public static void setNGrados (double pnValor) {
        nGrados = pnValor;
    }

    /**
     * Realiza la corrección de un ángulo en función de un valor de desvío.
     * @param pnDesvio double. Ángulo de desvío.
     * @return double. Ángulo corregido según un desvío
     */
    public static double corregirLectura (double pnDesvio) {
        double vnCorregido = 0.0;
        nDesvio = pnDesvio;
        return corregirLectura ();
    }

    /**
     * Realiza la corrección de un ángulo en función de un valor de desvío.
     * @param pnGrados double. Ángulo en grados sexagesimales.
     * @param pnDesvio double. Ángulo de desvío.
     * @return double. Ángulo corregido según un desvío
     */
    public static double corregirLectura (double pnGrados, double pnDesvio) {
        try {
            double vnCorregido = 0.0;
            nGrados = pnGrados;
            nDesvio = pnDesvio;
        }catch (Exception e) {
            Log.e("GPS-O", "Error corrigiendo lectura", e);
        }
        return corregirLectura ();
    }

    /**
     * Realiza la corrección de un ángulo en función de un valor de desvío.
     * @return double. Ángulo corregido según un desvío
     */
    public static double corregirLectura () {
        double vnCorregido = 0.0;
        try {
            vnCorregido = nGrados - nDesvio;
            if (vnCorregido<0) {
                vnCorregido = 360.0 - vnCorregido;
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error corrigiendo lectura", e);
            vnCorregido = 0.0;
        }
        return vnCorregido;
    }

    /**
     * Convierte un valor de grados sexagesimales en grados centesimales.
     * @param pnValor double. Valor de entrada en grados sexagesimales
     * @return double. Grados sexagesimales convertidos a centesimales, para operaciones topográficas
     */
    public static double convertirACentesimal (double pnValor) {
        double vnCent = 0.0;
        try {
            vnCent = (pnValor * 400.0) / 360.0;
        } catch (Exception e) {
            Log.e("GPS-O", "Error convirtiendo a centesimal", e);
            vnCent = 0.0;
        }
        return vnCent;
    }

    /**
     * Convierte un valor de grados centesimales en grados sexagesimales.
     * @param pnValor double. Valor de entrada en grados centesimales
     * @return double. Grados centesimales convertidos a sexagesimales
     */
    public static double convertirASexagesimal (double pnValor) {
        double vnSexa = 0.0;
        try {
            vnSexa = (pnValor * 360.0) / 400.0;
        } catch (Exception e) {
            Log.e("GPS-O", "Error convirtiendo a sexagesimal", e);
            vnSexa = 0.0;
        }
        return vnSexa;
    }
}
