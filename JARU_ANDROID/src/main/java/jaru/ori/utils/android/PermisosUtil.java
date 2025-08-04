package jaru.ori.utils.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermisosUtil {

    public static final int CODIGO_SOLICITUD_PERMISOS = 100;

    // Lista de permisos necesarios según versión
    public static String[] obtenerPermisosNecesarios() {
        List<String> permisos = new ArrayList<>();

        // Ubicación
        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);

        // Bluetooth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permisos.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            permisos.add(Manifest.permission.BLUETOOTH);
            permisos.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        //Almacenamiento (solo si usas métodos antiguos)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        return permisos.toArray(new String[0]);
    }

    // Verifica si todos los permisos están concedidos
    public static boolean tieneTodosLosPermisos(Context context) {
        for (String permiso : obtenerPermisosNecesarios()) {
            if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // Solicita los permisos faltantes
    public static void solicitarPermisos(Activity actividad) {
        List<String> permisosFaltantes = new ArrayList<>();

        for (String permiso : obtenerPermisosNecesarios()) {
            if (ContextCompat.checkSelfPermission(actividad, permiso) != PackageManager.PERMISSION_GRANTED) {
                permisosFaltantes.add(permiso);
            }
        }

        if (!permisosFaltantes.isEmpty()) {
            ActivityCompat.requestPermissions(actividad,
                    permisosFaltantes.toArray(new String[0]),
                    CODIGO_SOLICITUD_PERMISOS);
        }
    }

    // Redirige a la configuración de la app
    public static void abrirConfiguracionApp(Activity actividad) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", actividad.getPackageName(), null);
        intent.setData(uri);
        actividad.startActivity(intent);
    }
}
