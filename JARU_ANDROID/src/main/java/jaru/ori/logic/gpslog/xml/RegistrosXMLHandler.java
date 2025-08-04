package jaru.ori.logic.gpslog.xml;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import java.io.*;

import java.util.Vector;
import java.io.*;

import jaru.gps.logic.xml.ParametrosXMLHandler;
import jaru.ori.logic.gpslog.*;

/**
 * Gestor SAX2 para poder transformar un archivo XML con datos de coordenadas de puntos GPS
 *
 * @author jarufe
 * @version 1.0
 */
public class RegistrosXMLHandler extends DefaultHandler
{
    private Registro oRegistro = new Registro();
    private Vector<Registro> vRegistros = new Vector<Registro>();
    /** Buffer de almacenamiento de datos leidos. */
    protected StringBuffer vcBuffer = new StringBuffer();

    /**
     * Devuelve el vector de resultados del procesamiento.
     * Es un vector de elementos de la clase Producto.
     * @return
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
        if (lname.toLowerCase().equals("registro")) {
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
        if (lname.toLowerCase().equals("registro")) {
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
        Vector<Registro> vvResul = new Vector<Registro>();

        try {
            // Buscar el archivo en MediaStore
            Uri collection = MediaStore.Files.getContentUri("external");

            String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " +
                    MediaStore.MediaColumns.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[] {
                    Environment.DIRECTORY_DOCUMENTS + "/" + nombreCarpeta,
                    nombreArchivo
            };

            Cursor cursor = context.getContentResolver().query(
                    collection,
                    null,
                    selection,
                    selectionArgs,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                long id = cursor.getLong(idColumn);
                Uri uri = ContentUris.withAppendedId(collection, id);

                // Parsear el XML desde el InputStream
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                spf.setNamespaceAware(true);

                SAXParser parser = spf.newSAXParser();
                ParametrosXMLHandler handler = new ParametrosXMLHandler();

                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                InputSource source = new InputSource(inputStream);
                parser.parse(source, handler);

                vvResul = handler.getVRegistros();
                inputStream.close();
            }

            if (cursor != null) {
                cursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            vvResul.removeAllElements();
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
            // Buscar y eliminar archivo existente
            Uri collection = MediaStore.Files.getContentUri("external");
            String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " +
                    MediaStore.MediaColumns.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[] {
                    Environment.DIRECTORY_DOCUMENTS + "/" + nombreCarpeta,
                    nombreArchivo
            };

            Cursor cursor = context.getContentResolver().query(
                    collection,
                    new String[] { MediaStore.MediaColumns._ID },
                    selection,
                    selectionArgs,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                long id = cursor.getLong(idColumn);
                Uri uriExistente = ContentUris.withAppendedId(collection, id);
                context.getContentResolver().delete(uriExistente, null, null);
                cursor.close();
            }
            // Crear entrada en MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/xml");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/" + nombreCarpeta);

            Uri uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
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
                    Registro voRegistro = (Registro)pvRegistros.elementAt(i);
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
            } else {
                resultado = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultado = false;
        } finally {
            if (pStr != null) pStr.close();
            else if (os != null) try { os.close(); } catch (IOException ignored) {}
        }

        return resultado;
    }
}

