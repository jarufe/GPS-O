package jaru.ori.utils.android;

import android.content.Context;
import android.content.res.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Clase con utilidades comunes para aplicaciones Android
 */
public class UtilsAndroid {
    private static android.content.res.Resources oRes = null;

    /**
     * Constructor por defecto de la clase
     */
    public UtilsAndroid () {
        super();
    }
    /**
     * Establece un objeto que representa a los recursos, para que se
     * pueda acceder a los recursos de la misma.
     * @param poRes android.content.res.Resources.
     */
    public static void setORes (android.content.res.Resources poRes) {
        oRes = poRes;
    }

    /**
     * Devuelve un valor booleano indicando si la pantalla del dispositivo es más ancha que larga.
     * @param poRes android.content.res.Resources.
     * @return boolean Verdadero si la pantalla es más ancha que alta
     */
    public static boolean esPantallaAncha(Resources poRes) {
        Configuration config = poRes.getConfiguration();
        return config.smallestScreenWidthDp >= 600;
    }

    /**
     * Comprueba si el dispositivo tiene una conexión de datos. Así podemos evitar el intento de transmisión
     * de datos sabiendo que no disponemos de conectividad.
     * @param poContext Context Contexto de la aplicación
     * @return boolean Si existe conectividad o no
     */
    public static boolean existeConectividadDatos(Context poContext) {
        boolean isConnected = false;
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) poContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            //isConnected = activeNetwork.isConnectedOrConnecting();
            isConnected = activeNetwork.isConnected();
        } catch (Exception e) {
        }
        return isConnected;
    }
}
