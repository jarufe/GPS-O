package jaru.ori.logic.localiza.xml;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jaru.ori.logic.localiza.ConfLocaliza;
import jaru.ori.utils.android.UtilsAndroid;

/**
 * Gestor SAX2 para poder transformar un archivo XML con datos de configuración
 * de la parte de transmisión de datos de localización a un servidor web
 *
 * @author jarufe
 * @version 1.0
 */
public class ConfLocalizaXMLHandler extends DefaultHandler
{
    private ConfLocaliza oConfLocaliza = new ConfLocaliza();
    private Vector<ConfLocaliza> vRegistros = new Vector<>();
    /** Buffer de almacenamiento de datos leidos. */
    protected StringBuffer vcBuffer = new StringBuffer();

    /**
     * Devuelve el vector de resultados del procesamiento.
     * Es un vector de elementos de la clase ConfLocaliza.
     * @return Vector<ConfLocaliza> Conjunto de registros
     */
    public Vector<ConfLocaliza> getVRegistros () {
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
        if (lname.equalsIgnoreCase("conflocaliza")) {
            oConfLocaliza = new ConfLocaliza();
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
        if (lname.equalsIgnoreCase("conflocaliza")) {
            vRegistros.addElement(oConfLocaliza);
        } else {
            String content = vcBuffer.toString().trim();
            //Datos de configuración para el gestor de transmisión de datos
            if (lname.equals("cDorsal")) {
                oConfLocaliza.setcDorsal(content);
            } else if (lname.equals("cNombre")) {
                oConfLocaliza.setcNombre(content);
            } else if (lname.equals("nEvento")) {
                try {
                    oConfLocaliza.setnEvento(Integer.parseInt(content));
                } catch (Exception e) {
                    oConfLocaliza.setnEvento(-1);
                }
            } else if (lname.equals("nCategoria")) {
                try {
                    oConfLocaliza.setnCategoria(Integer.parseInt(content));
                } catch (Exception e) {
                    oConfLocaliza.setnCategoria(-1);
                }
            } else if (lname.equals("nPuerto")) {
                try {
                    oConfLocaliza.setnPuerto(Integer.parseInt(content));
                } catch (Exception e) {
                    oConfLocaliza.setnPuerto(-1);
                }
            } else if (lname.equals("cServidor")) {
                oConfLocaliza.setcServidor(content);
            } else if (lname.equals("cServlet")) {
                oConfLocaliza.setcServlet(content);
            } else if (lname.equals("nRetardo")) {
                try {
                    oConfLocaliza.setnRetardo(Integer.parseInt(content));
                } catch (Exception e) {
                    oConfLocaliza.setnRetardo(20);
                }
            }
        }
    }

    /**
     * Método que se encarga de recuperar los datos a partir del archivo XML.
     * Los datos son introducidos en un vector de elementos de la clase ConfLocaliza.
     * Los datos se recuperan de un archivo XML.
     * @param context Context. Contexto de la aplicación
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     * @return Vector. Elementos de la clase ConfLocaliza, conteniendo los datos recuperados del archivo XML.
     */
    public static Vector<ConfLocaliza> obtenerDatosXML(Context context, String nombreCarpeta, String nombreArchivo) {
        Vector<ConfLocaliza> vvResul = new Vector<>();

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
                // Parsear el XML desde el InputStream
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                spf.setNamespaceAware(true);

                SAXParser parser = spf.newSAXParser();
                ConfLocalizaXMLHandler handler = new ConfLocalizaXMLHandler();

                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                InputSource source = new InputSource(inputStream);
                parser.parse(source, handler);

                vvResul = handler.getVRegistros();
                Log.i("GPS-O", "Archivo procesado. Registros: " + (vvResul != null ? vvResul.size() : 0));
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
     * Vuelca los datos de parámetros a un archivo XML, cuyo nombre es el
     * que se proporciona en el segundo argumento.
     * @param context Context. Contexto de la aplicación
     * @param pvRegistros Vector<ConfLocaliza>. Vector de elementos de la clase ConfLocaliza.
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     */
    public static boolean escribirXML (Context context, Vector<ConfLocaliza> pvRegistros, String nombreCarpeta, String nombreArchivo) {
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
                //Comienza escribiendo la cabecera del archivo XML
                pStr.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                pStr.println("<!--<!DOCTYPE ConfLocaliza SYSTEM \"ConfLocaliza.dtd\">-->");
                //Recorre el vector de elementos, para volcar uno a uno su contenido al fichero.
                int i = 0;
                while (i<pvRegistros.size()) {
                    //Obtiene el siguiente elemento
                    ConfLocaliza voConf = pvRegistros.elementAt(i);
                    //Crea la estructura XML en el archivo
                    pStr.println("  <ConfLocaliza>");
                    pStr.println("    <nEvento>" + voConf.getnEvento() + "</nEvento>");
                    pStr.println("    <nCategoria>" + voConf.getnCategoria() + "</nCategoria>");
                    pStr.println("    <cDorsal>" + voConf.getcDorsal() + "</cDorsal>");
                    pStr.println("    <cNombre>" + voConf.getcNombre() + "</cNombre>");
                    pStr.println("    <cServidor>" + voConf.getcServidor() + "</cServidor>");
                    pStr.println("    <nPuerto>" + voConf.getnPuerto() + "</nPuerto>");
                    pStr.println("    <cServlet>" + voConf.getcServlet() + "</cServlet>");
                    pStr.println("    <nRetardo>" + voConf.getnRetardo() + "</nRetardo>");
                    pStr.println("  </ConfLocaliza>");
                    i++;
                }
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

