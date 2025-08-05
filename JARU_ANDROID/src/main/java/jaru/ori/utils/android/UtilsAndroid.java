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
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.os.Build;

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

    /**
     * Busca si existe un fichero en la subcarpeta indicada dentro de la carpeta Documents
     * @param context Context Contexto de la aplicación
     * @param nombreCarpeta String Subcarpeta dentro de Documents
     * @param nombreArchivo String Nombre del archivo XML
     * @return Cursor Cursor apuntando al fichero o ficheros encontrados
     */
    public static Cursor buscarFicheroEnCarpeta(Context context, String nombreCarpeta, String nombreArchivo) {
        Cursor cursor = null;

        Log.i("GPS-O", "Comienza búsqueda de fichero");
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri collection;
            // Definir la URI base según la versión de Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
            } else {
                collection = Uri.parse("content://media/external/file");
            }
            // Construir la ruta de búsqueda
            String relativePath = Environment.DIRECTORY_DOCUMENTS + "/" + nombreCarpeta;
            Log.i("GPS-O", "Buscando archivo en: " + relativePath + "/" + nombreArchivo);
            String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + "=? AND " +
                    MediaStore.Files.FileColumns.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[]{relativePath + "/", nombreArchivo};
            // Columnas que queremos recuperar (solo necesitamos saber si existe)
            String[] projection = new String[]{MediaStore.Files.FileColumns._ID};

            cursor = resolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    null);
            if (cursor == null) {
                Log.e("GPS-O", "Cursor nulo: la consulta falló");
            } else if (cursor.getCount()<=0) {
                Log.e("GPS-O", "Cursor vacío: no se encontró el archivo");
            } else {
                Log.i("GPS-O", "Total resultados: " + cursor.getCount());
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error buscando fichero", e);
        }

        return cursor;
    }

    /**
     * En función de la versión de Android, crea un Uri del almacenamiento externo para poder buscar archivos
     * @return Uri
     */
    public static Uri componerUriSegunAndroid () {
        Uri collection;
        // Definir la URI base según la versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = Uri.parse("content://media/external/file");
        }
        return collection;
    }

    /**
     * Crea un archivo de tipo XML usando MediaStore, dentro de la carpeta Documents y subcarpeta indicada en el parámetro
     * @param context Context Contexto de la aplicación
     * @param nombreCarpeta String Subcarpeta dentro de Documents
     * @param nombreArchivo String Nombre del archivo XML
     * @return Uri Apunta al archivo creado
     */
    public static Uri crearArchivoXml (Context context, String nombreCarpeta, String nombreArchivo) {
        ContentResolver resolver = context.getContentResolver();
        Uri collection;
        // Definir la URI base según la versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = Uri.parse("content://media/external/file");
        }
        // Construir la ruta de búsqueda
        String relativePath = Environment.DIRECTORY_DOCUMENTS + "/" + nombreCarpeta;
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/xml");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);

        Uri uri = context.getContentResolver().insert(collection, values);

        return uri;
    }
}
