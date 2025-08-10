package jaru.gps.logic.xml;

import android.content.ContentUris;
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
    private Vector<Parametro> vRegistros = new Vector<>();
    /** Buffer de almacenamiento de datos leidos. */
    protected StringBuffer vcBuffer = new StringBuffer();

    /**
     * Devuelve el vector de resultados del procesamiento.
     * Es un vector de elementos de la clase Parametro.
     * @return Vector Conjunto de registros
     */
    public Vector<Parametro> getVRegistros () {
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
        if (lname.equalsIgnoreCase("parametro")) {
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
        if (lname.equalsIgnoreCase("parametro")) {
            vRegistros.addElement(oRegistro);
        } else {
            String content = vcBuffer.toString().trim();
            //Datos de una línea de reparto
            if (lname.equals("cPathXML")) {
                if (content != null && !content.isEmpty()) {
                    // Eliminar el primer carácter si es / o \
                    if (content.startsWith("/") || content.startsWith("\\")) {
                        content = content.substring(1);
                    }
                    // Eliminar el último carácter si es / o \
                    if (content.endsWith("/") || content.endsWith("\\")) {
                        content = content.substring(0, content.length() - 1);
                    }
                }
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
    public static Vector<Parametro> obtenerDatosXML(Context context, String nombreCarpeta, String nombreArchivo) {
        Vector<Parametro> vvResul = new Vector<>();

        Log.i("GPS-O", "Comienza carga de parámetros en XML");
        try {
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+: usar MediaStore
                Cursor cursor = UtilsAndroid.buscarFicheroEnCarpeta(context, nombreCarpeta, nombreArchivo);
                Uri collection = UtilsAndroid.componerUriSegunAndroid();

                if (cursor != null && cursor.moveToFirst()) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                    long id = cursor.getLong(idColumn);
                    uri = ContentUris.withAppendedId(collection, id);
                    cursor.close();
                }
            } else {
                // Android 9 o inferior: usar File directamente
                File archivo = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS + "/" + nombreCarpeta), nombreArchivo);
                if (archivo.exists()) {
                    uri = Uri.fromFile(archivo);
                }
            }
            // Si se obtuvo una URI válida, abrir el archivo
            if (uri != null) {
                try {
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
                    if (inputStream!=null) inputStream.close();
                } catch (Exception e) {
                    Log.e("GPS-O", "Error leyendo/parsing XML", e);
                }
            } else {
                Log.e("GPS-O", "No se pudo obtener URI del archivo XML");
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error cargando XML", e);
            if (vvResul!=null) vvResul.removeAllElements();
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
            //Busca si existe el fichero y lo borra antes de crearlo de nuevo
            Cursor cursor = UtilsAndroid.buscarFicheroEnCarpeta(context, nombreCarpeta, nombreArchivo);
            UtilsAndroid.borrarArchivosEnCarpeta(context, cursor, nombreCarpeta, nombreArchivo);
            //Crea el archivo de nuevo
            Uri uri = UtilsAndroid.crearArchivoXml(context, nombreCarpeta, nombreArchivo);
            //Si se ha creado el archivo, se exporta el contenido XML
            if (uri != null) {
                os = context.getContentResolver().openOutputStream(uri);
                pStr = new PrintStream(new BufferedOutputStream(os), true, "ISO-8859-1");

                pStr.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                pStr.println("<!--<!DOCTYPE Parametros SYSTEM \"Parametros.dtd\">-->");
                pStr.println("<Parametros>");

                for (Parametro voRegistro : pvRegistros) {
                    pStr.println("  <Parametro>");
                    String content = voRegistro.getCPathXML();
                    if (content != null && !content.isEmpty()) {
                        // Eliminar el primer carácter si es / o \
                        if (content.startsWith("/") || content.startsWith("\\")) {
                            content = content.substring(1);
                        }
                        // Eliminar el último carácter si es / o \
                        if (content.endsWith("/") || content.endsWith("\\")) {
                            content = content.substring(0, content.length() - 1);
                        }
                    }
                    voRegistro.setCPathXML(content);
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

