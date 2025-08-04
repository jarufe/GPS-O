package jaru.ori.utils.android;

import android.content.Context;
import android.content.res.*;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

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
    public static void listarTodosLosArchivos(Context context, String nombreCarpeta) {
        String relativePath = Environment.DIRECTORY_DOCUMENTS + "/" + nombreCarpeta;
        Uri collection = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[] { relativePath };

        Cursor cursor = context.getContentResolver().query(
                collection,
                new String[] {
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.MIME_TYPE,
                        MediaStore.MediaColumns.SIZE,
                        MediaStore.MediaColumns.DATE_MODIFIED
                },
                selection,
                selectionArgs,
                MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
        );

        if (cursor != null) {
            Log.i("GPS-O", "Archivos en " + relativePath + ":");
            while (cursor.moveToNext()) {
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
                long tamano = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE));
                long fechaMod = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                Log.i("GPS-O", " - " + nombre + " | Tipo: " + tipo + " | Tamaño: " + tamano + " bytes | Modificado: " + fechaMod);
            }
            cursor.close();
        } else {
            Log.i("GPS-O", "No se encontraron archivos en " + relativePath);
        }
    }
    public static void depurarArchivosEnCarpeta(Context context, String nombreCarpeta) {
        String relativePath = Environment.DIRECTORY_DOCUMENTS + "/" + nombreCarpeta;
        File carpeta = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), nombreCarpeta);

        if (!carpeta.exists()) {
            Log.i("GPS-O", "La carpeta no existe: " + carpeta.getAbsolutePath());
            return;
        }

        File[] archivos = carpeta.listFiles();
        if (archivos == null || archivos.length == 0) {
            Log.i("GPS-O", "No hay archivos en la carpeta: " + carpeta.getAbsolutePath());
            return;
        }

        Log.i("GPS-O", "Archivos encontrados en el sistema de archivos:");
        for (File archivo : archivos) {
            Log.i("GPS-O", " - " + archivo.getName());

            // Forzar indexación en MediaStore
            MediaScannerConnection.scanFile(
                    context,
                    new String[] { archivo.getAbsolutePath() },
                    null,
                    (path, uri) -> Log.i("GPS-O", "Indexado en MediaStore: " + path)
            );
        }

        // Comprobación en MediaStore
        Uri collection = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[] { relativePath };

        Cursor cursor = context.getContentResolver().query(
                collection,
                new String[] {
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.RELATIVE_PATH
                },
                selection,
                selectionArgs,
                null
        );

        if (cursor != null) {
            Log.i("GPS-O", "Archivos registrados en MediaStore:");
            while (cursor.moveToNext()) {
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                String ruta = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH));
                Log.i("GPS-O", " - " + ruta + nombre);
            }
            cursor.close();
        } else {
            Log.i("GPS-O", "No se encontraron archivos en MediaStore para: " + relativePath);
        }
    }
    public static void depurarArchivosEnDocumentsJARU(Context context) {
        String nombreCarpeta = "JARU";
        String relativePath = Environment.DIRECTORY_DOCUMENTS + "/" + nombreCarpeta;
        File carpeta = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), nombreCarpeta);

        Log.i("GPS-O", "📁 Ruta física: " + carpeta.getAbsolutePath());

        // 1. Listar archivos físicos
        File[] archivos = carpeta.listFiles();
        if (archivos == null || archivos.length == 0) {
            Log.i("GPS-O", "No hay archivos físicos en la carpeta.");
        } else {
            Log.i("GPS-O", "Archivos físicos encontrados:");
            for (File archivo : archivos) {
                Log.i("GPS-O", " - " + archivo.getName());

                // 2. Forzar indexación en MediaStore
                MediaScannerConnection.scanFile(
                        context,
                        new String[] { archivo.getAbsolutePath() },
                        null,
                        (path, uri) -> Log.i("GPS-O", "Indexado en MediaStore: " + path)
                );
            }
        }

        // 3. Listar archivos registrados en MediaStore
        Uri collection = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[] { relativePath };

        Cursor cursor = context.getContentResolver().query(
                collection,
                new String[] {
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.MIME_TYPE,
                        MediaStore.MediaColumns.SIZE,
                        MediaStore.MediaColumns.DATE_MODIFIED
                },
                selection,
                selectionArgs,
                MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
        );

        if (cursor != null && cursor.getCount() > 0) {
            Log.i("GPS-O", "Archivos registrados en MediaStore:");
            while (cursor.moveToNext()) {
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
                long tamano = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE));
                long fechaMod = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                Log.i("GPS-O", " - " + nombre + " | Tipo: " + tipo + " | Tamaño: " + tamano + " bytes | Modificado: " + fechaMod);
            }
            cursor.close();
        } else {
            Log.i("GPS-O", "No se encontraron archivos en MediaStore para: " + relativePath);
        }
    }

}
