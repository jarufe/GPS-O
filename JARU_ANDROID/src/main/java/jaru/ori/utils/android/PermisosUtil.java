package jaru.ori.utils.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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

import jaru.ori.gui.gpslog.android.R;

public class PermisosUtil {

    public static final int CODIGO_SOLICITUD_PERMISOS = 100;
    public static final int CODIGO_SOLICITUD_BACKGROUND = 101;
    public static final int CODIGO_SOLICITUD_WRITE_STORAGE = 102;

    // Lista de permisos que deben solicitarse en tiempo de ejecución
    public static String[] obtenerPermisosNecesarios() {
        List<String> permisos = new ArrayList<>();

        // Ubicación
        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);

        // Bluetooth (solo BLUETOOTH_CONNECT en Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permisos.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        // Lectura de almacenamiento externo (solo en Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permisos.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        return permisos.toArray(new String[0]);
    }

    // Verifica si todos los permisos necesarios están concedidos
    public static boolean tieneTodosLosPermisos(Context context) {
        for (String permiso : obtenerPermisosNecesarios()) {
            if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Verifica también ACCESS_BACKGROUND_LOCATION si aplica
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Verifica WRITE_EXTERNAL_STORAGE en Android 9 o inferior
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
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

        // Solicita ACCESS_BACKGROUND_LOCATION por separado si aplica
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(actividad, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(actividad,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        CODIGO_SOLICITUD_BACKGROUND);
            }
        }

        // Solicita WRITE_EXTERNAL_STORAGE por separado si aplica
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(actividad, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(actividad,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        CODIGO_SOLICITUD_WRITE_STORAGE);
            }
        }
    }

    // Redirige al usuario a la configuración de la app si ha denegado permisos
    public static void abrirConfiguracionApp(Activity actividad) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", actividad.getPackageName(), null);
        intent.setData(uri);
        actividad.startActivity(intent);
    }

    public static void solicitarPermisoBackgroundSiAplica(Activity actividad) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(actividad, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                new AlertDialog.Builder(actividad)
                        .setTitle(R.string.ORI_MI00024)
                        .setMessage(R.string.ORI_MI00025)
                        .setPositiveButton(R.string.ORI_ML00003, (dialog, which) -> {
                            ActivityCompat.requestPermissions(actividad,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    CODIGO_SOLICITUD_BACKGROUND);
                        })
                        .setNegativeButton(R.string.ORI_ML00004, null)
                        .show();
            }
        }
    }
}
