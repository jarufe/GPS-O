package jaru.ori.logic.gpslog.xml;

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
import java.io.*;

import java.util.Vector;

import jaru.ori.logic.gpslog.*;
import jaru.ori.utils.android.UtilsAndroid;

/**
 * Gestor SAX2 para poder transformar un archivo XML con datos de coordenadas de puntos GPS
 *
 * @author jarufe
 * @version 1.0
 */
public class RegistrosXMLHandler extends DefaultHandler
{
    private Registro oRegistro = new Registro();
    private Vector<Registro> vRegistros = new Vector<>();
    /** Buffer de almacenamiento de datos leidos. */
    protected StringBuffer vcBuffer = new StringBuffer();

    /**
     * Devuelve el vector de resultados del procesamiento.
     * Es un vector de elementos de la clase Producto.
     * @return Vector<Registro> Conjunto de registros
     */
    public Vector<Registro> getVRegistros () {
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
        if (lname.equalsIgnoreCase("registro")) {
            oRegistro = new Registro();
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
        if (lname.equalsIgnoreCase("registro")) {
            vRegistros.addElement(oRegistro);
        } else {
            String content = vcBuffer.toString().trim();
            //Datos de un punto de GPS
            if (lname.equals("cID")) {
                oRegistro.setCID(content);
            } else if (lname.equals("nTipo")) {
                try {
                    oRegistro.setNTipo(Integer.parseInt(content));
                } catch (Exception e) {
                    oRegistro.setNTipo(0);
                }
            } else if (lname.equals("cTipoOCAD")) {
                oRegistro.setCTipoOCAD(content);
            } else if (lname.equals("cTipoOBM")) {
                oRegistro.setCTipoOBM(content);
            } else if (lname.equals("cDesc")) {
                oRegistro.setCDesc(content);
            } else if (lname.equals("cCX")) {
                oRegistro.setCCX(content);
            } else if (lname.equals("cCY")) {
                oRegistro.setCCY(content);
            } else if (lname.equals("cElev")) {
                oRegistro.setCElev(content);
            } else if (lname.equals("cFecha")) {
                oRegistro.setCFecha(content);
            }
        }
    }

    /**
     * Método que se encarga de recuperar los datos a partir del archivo XML. Los datos son introducidos en un vector de
     * elementos de la clase Registro. Los datos se recuperan de un archivo XML.
     * @param context Context. Contexto de la aplicación
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     * @return Vector. Elementos de la clase Registro, conteniendo los datos recuperados del archivo XML.
     */
    public static Vector<Registro> obtenerDatosXML(Context context, String nombreCarpeta, String nombreArchivo) {
        Vector<Registro> vvResul = new Vector<>();

        Log.i("GPS-O", "Comienza carga de registros vectoriales en XML");
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
                // Parsear el XML desde el InputStream
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                spf.setNamespaceAware(true);

                SAXParser parser = spf.newSAXParser();
                RegistrosXMLHandler handler = new RegistrosXMLHandler();

                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                InputSource source = new InputSource(inputStream);
                parser.parse(source, handler);

                vvResul = handler.getVRegistros();
                Log.i("GPS-O", "Archivo procesado. Elementos: " + (vvResul != null ? vvResul.size() : 0));
                if (inputStream!=null) inputStream.close();
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
     * @param pvRegistros Vector<Registro>. Vector de elementos de la clase Registro.
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     */
    public static boolean escribirXML (Context context, Vector<Registro> pvRegistros, String nombreCarpeta, String nombreArchivo) {
        boolean resultado = true;
        OutputStream os = null;
        PrintStream pStr = null;

        try {
            Log.i("GPS-O", "Comienza grabación de registros vectoriales en XML");
            //Busca si existe el fichero y lo borra antes de crearlo de nuevo
            Cursor cursor = UtilsAndroid.buscarFicheroEnCarpeta(context, nombreCarpeta, nombreArchivo);
            UtilsAndroid.borrarArchivosEnCarpeta(context, cursor, nombreCarpeta, nombreArchivo);
            //Crea el archivo de nuevo
            Uri uri = UtilsAndroid.crearArchivoXml(context, nombreCarpeta, nombreArchivo);
            //Si se ha creado el archivo, se exporta el contenido XML
            if (uri != null) {
                Log.i("GPS-O", "Comienza escritura de registros vectoriales en el fichero");
                os = context.getContentResolver().openOutputStream(uri);
                pStr = new PrintStream(new BufferedOutputStream(os), true, "ISO-8859-1");
                //Comienza escribiendo la cabecera del archivo XML
                pStr.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                pStr.println("<!--<!DOCTYPE Registros SYSTEM \"Registros.dtd\">-->");
                pStr.println("<Registros>");
                //Recorre el vector de elementos, para volcar uno a uno su contenido al fichero.
                int i = 0;
                while (i<pvRegistros.size()) {
                    //Obtiene el siguiente elemento
                    Registro voRegistro = pvRegistros.elementAt(i);
                    //Crea la estructura XML en el archivo
                    pStr.println("  <Registro>");
                    pStr.println("    <cID>" + voRegistro.getCID() + "</cID>");
                    pStr.println("    <nTipo>" + voRegistro.getNTipo() + "</nTipo>");
                    pStr.println("    <cTipoOCAD>" + voRegistro.getCTipoOCAD() + "</cTipoOCAD>");
                    pStr.println("    <cTipoOBM>" + voRegistro.getCTipoOBM() + "</cTipoOBM>");
                    pStr.println("    <cDesc>" + voRegistro.getCDesc() + "</cDesc>");
                    pStr.println("    <cCX>" + voRegistro.getCCX() + "</cCX>");
                    pStr.println("    <cCY>" + voRegistro.getCCY() + "</cCY>");
                    pStr.println("    <cElev>" + voRegistro.getCElev() + "</cElev>");
                    pStr.println("    <cFecha>" + voRegistro.getCFecha() + "</cFecha>");
                    pStr.println("  </Registro>");
                    i++;
                }
                //Cierra la estructura XML en el archivo, y el propio archivo.
                pStr.println("</Registros>");
                Log.i("GPS-O", pvRegistros.size() + " registros vectoriales grabados en XML");
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

