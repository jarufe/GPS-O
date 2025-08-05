package jaru.gps.logic.xml;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;

import java.util.Vector;
import java.io.*;
import jaru.gps.logic.*;
import jaru.ori.utils.android.UtilsAndroid;

/**
 * Gestor SAX2 para poder transformar un archivo XML con datos de Parámetros de configuración
 *
 * @author jarufe
 * @version 1.0
 */
public class ParametrosXMLHandler extends DefaultHandler
{
    private Parametro oRegistro = new Parametro();
    private Vector vRegistros = new Vector();
    /** Buffer de almacenamiento de datos leidos. */
    protected StringBuffer vcBuffer = new StringBuffer();

    /**
     * Devuelve el vector de resultados del procesamiento.
     * Es un vector de elementos de la clase Parametro.
     * @return
     */
    public Vector getVRegistros () {
        return vRegistros;
    }

    /**
     * Método que se lanza cuando comienza un nuevo elemento del árbol XML
     * @param uri String. Cadena de texto.
     * @param lname String. Nombre del elemento XML que comienza
     * @param qname String. Otro nombre
     * @param attributes Attributes. Atributos asociados al elemento que llega
     */
    public void startElement(String uri, String lname, String qname,
                             Attributes attributes) {
        vcBuffer.setLength(0);
        if (lname.toLowerCase().equals("parametro")) {
            oRegistro = new Parametro();
        }
    }

    /**
     * Método que se lanza cuando se leen caracteres dentro de un elemento XML
     * @param chars char[]
     * @param start int
     * @param length int
     */
    public void characters(char[] chars, int start, int length) {
        vcBuffer.append(chars, start, length);
    }
    /**
     * Método que se lanza cuando termina un elemento XML, lo cual permite
     * procesar todo el contenido leído y asociarlo a las propiedades correspondientes
     * @param uri String
     * @param lname String
     * @param qname String
     */
    public void endElement(String uri, String lname, String qname) {
        if (lname.toLowerCase().equals("parametro")) {
            vRegistros.addElement(oRegistro);
        } else {
            String content = vcBuffer.toString().trim();
            //Datos de una línea de reparto
            if (lname.equals("cPathXML")) {
                oRegistro.setCPathXML(content);
            } else if (lname.equals("cEscala")) {
                oRegistro.setCEscala(content);
            } else if (lname.equals("cTick")) {
                oRegistro.setCTick(content);
            } else if (lname.equals("cPuerto")) {
                oRegistro.setCPuerto(content);
            } else if (lname.equals("cBaudios")) {
                oRegistro.setCBaudios(content);
            } else if (lname.equals("cBitsPalabra")) {
                oRegistro.setCBitsPalabra(content);
            } else if (lname.equals("cBitsStop")) {
                oRegistro.setCBitsStop(content);
            } else if (lname.equals("cParidad")) {
                oRegistro.setCParidad(content);
            } else if (lname.equals("cGpsInterno")) {
                oRegistro.setCGpsInterno(content);
            }
        }
    }

    /**
     * Método que se encarga de recuperar los datos a partir del archivo XML. Los datos son introducidos en un vector de
     * elementos de la clase Parametro. Los datos se recuperan de un archivo XML.
     * @param context Context. Contexto de la aplicación
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     * @return Vector. Elementos de la clase Parametro, conteniendo los datos recuperados del archivo XML.
     */
    public static Vector obtenerDatosXML(Context context, String nombreCarpeta, String nombreArchivo) {
        Vector vvResul = new Vector();

        Log.i("GPS-O", "Comienza carga de parámetros en XML");
        try {
            Cursor cursor = UtilsAndroid.buscarFicheroEnCarpeta(context, nombreCarpeta, nombreArchivo);
            Uri collection = UtilsAndroid.componerUriSegunAndroid();
            if (cursor!=null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                long id = cursor.getLong(idColumn);
                Uri uri = ContentUris.withAppendedId(collection, id);

                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                spf.setNamespaceAware(true);

                SAXParser parser = spf.newSAXParser();
                ParametrosXMLHandler handler = new ParametrosXMLHandler();

                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                InputSource source = new InputSource(inputStream);
                parser.parse(source, handler);
                vvResul = handler.getVRegistros();
                Log.i("GPS-O", "Archivo procesado. Registros: " + (vvResul != null ? vvResul.size() : 0));
                inputStream.close();
            }
            if (cursor != null) cursor.close();
            /*
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

            Cursor cursor = resolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    null);
            if (cursor == null) {
                Log.e("GPS-O", "Cursor nulo: la consulta falló");
            } else if (!cursor.moveToFirst()) {
                Log.e("GPS-O", "Cursor vacío: no se encontró el archivo");
                Log.i("GPS-O", "Total resultados: " + cursor.getCount());
            } else {
                Log.i("GPS-O", "Archivo encontrado");
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                long id = cursor.getLong(idColumn);
                Uri uri = ContentUris.withAppendedId(collection, id);

                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                spf.setNamespaceAware(true);

                SAXParser parser = spf.newSAXParser();
                ParametrosXMLHandler handler = new ParametrosXMLHandler();

                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                InputSource source = new InputSource(inputStream);
                parser.parse(source, handler);
                vvResul = handler.getVRegistros();
                Log.i("GPS-O", "Archivo procesado. Registros: " + (vvResul != null ? vvResul.size() : 0));
                inputStream.close();
            }

            if (cursor != null) cursor.close();
             */

        } catch (Exception e) {
            Log.e("GPS-O", "Error cargando XML", e);
            vvResul.removeAllElements();
        }

        return vvResul;
    }
    /**
     * Vuelca los datos de parámetros a un archivo XML, cuyo nombre es el que se proporciona en el segundo argumento.
     * @param context Context. Contexto de la aplicación
     * @param pvRegistros Vector. Vector de elementos de la clase Parametro.
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     */
    public static boolean escribirXML(Context context, Vector<Parametro> pvRegistros, String nombreCarpeta, String nombreArchivo) {
        boolean resultado = true;
        OutputStream os = null;
        PrintStream pStr = null;

        try {
            Cursor cursor = UtilsAndroid.buscarFicheroEnCarpeta(context, nombreCarpeta, nombreArchivo);
            Uri collection = UtilsAndroid.componerUriSegunAndroid();
            if (cursor!=null && cursor.moveToFirst()) {
                do {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    long id = cursor.getLong(idColumn);
                    Uri uriExistente = ContentUris.withAppendedId(collection, id);
                    context.getContentResolver().delete(uriExistente, null, null);
                    Log.i("GPS-O", "Archivo existente borrado: " + uriExistente.toString());
                } while (cursor.moveToNext());

                cursor.close();
            }
            /*
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
            Log.i("GPS-O", "Escribir. Buscando archivo en: " + relativePath + "/" + nombreArchivo);
            String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + "=? AND " +
                    MediaStore.Files.FileColumns.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[]{relativePath + "/", nombreArchivo};
            // Columnas que queremos recuperar (solo necesitamos saber si existe)
            String[] projection = new String[]{MediaStore.Files.FileColumns._ID};

            Cursor cursor = resolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    null);
            if (cursor == null) {
                Log.e("GPS-O", "Escribir. Cursor nulo: la consulta falló");
            } else if (!cursor.moveToFirst()) {
                Log.e("GPS-O", "Escribir. Cursor vacío: no se encontró el archivo");
                Log.i("GPS-O", "Escribir. Total resultados: " + cursor.getCount());
            } else {
                do {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    long id = cursor.getLong(idColumn);
                    Uri uriExistente = ContentUris.withAppendedId(collection, id);
                    context.getContentResolver().delete(uriExistente, null, null);
                    Log.i("GPS-O", "Archivo existente borrado: " + uriExistente.toString());
                } while (cursor.moveToNext());

                cursor.close();
            }
            */
            /*
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/xml");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);

            Uri uri = context.getContentResolver().insert(collection, values);
            */
            Uri uri = UtilsAndroid.crearArchivoXml(context, nombreCarpeta, nombreArchivo);
            if (uri != null) {
                os = context.getContentResolver().openOutputStream(uri);
                pStr = new PrintStream(new BufferedOutputStream(os), true, "ISO-8859-1");

                pStr.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                pStr.println("<!--<!DOCTYPE Parametros SYSTEM \"Parametros.dtd\">-->");
                pStr.println("<Parametros>");

                for (Parametro voRegistro : pvRegistros) {
                    pStr.println("  <Parametro>");
                    pStr.println("    <cPathXML>" + voRegistro.getCPathXML() + "</cPathXML>");
                    pStr.println("    <cEscala>" + voRegistro.getCEscala() + "</cEscala>");
                    pStr.println("    <cPuerto>" + voRegistro.getCPuerto() + "</cPuerto>");
                    pStr.println("    <cBaudios>" + voRegistro.getCBaudios() + "</cBaudios>");
                    pStr.println("    <cBitsPalabra>" + voRegistro.getCBitsPalabra() + "</cBitsPalabra>");
                    pStr.println("    <cBitsStop>" + voRegistro.getCBitsStop() + "</cBitsStop>");
                    pStr.println("    <cParidad>" + voRegistro.getCParidad() + "</cParidad>");
                    pStr.println("    <cTick>" + voRegistro.getCTick() + "</cTick>");
                    pStr.println("    <cGpsInterno>" + voRegistro.getCGpsInterno() + "</cGpsInterno>");
                    pStr.println("  </Parametro>");
                }

                pStr.println("</Parametros>");
            } else {
                Log.e("GPS-O", "No se pudo crear el archivo en MediaStore");
                resultado = false;
            }

        } catch (Exception e) {
            Log.e("GPS-O", "Error escribiendo XML", e);
            resultado = false;
        } finally {
            if (pStr != null) pStr.close();
            else if (os != null) try { os.close(); } catch (IOException ignored) {}
        }

        return resultado;
    }
}

