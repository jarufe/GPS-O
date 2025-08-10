package jaru.ori.logic.campo.xml;

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

import jaru.ori.logic.campo.*;
import jaru.ori.utils.android.UtilsAndroid;

/**
 * Gestor SAX2 para poder transformar un archivo XML con datos de configuración
 * del editor de trabajo de campo.
 *
 * @author jarufe
 * @version 1.0
 */
public class ConfCampoXMLHandler extends DefaultHandler
{
    private ConfCampo oConfCampo = new ConfCampo();
    private Vector<ConfCampo> vRegistros = new Vector<>();
    /** Buffer de almacenamiento de datos leidos. */
    protected StringBuffer vcBuffer = new StringBuffer();

    /**
     * Devuelve el vector de resultados del procesamiento.
     * Es un vector de elementos de la clase ConfCampo.
     * @return Vector<ConfCampo> Conjunto de elementos de configuración
     */
    public Vector<ConfCampo> getVRegistros () {
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
        if (lname.equalsIgnoreCase("confcampo")) {
            oConfCampo = new ConfCampo();
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
        if (lname.equalsIgnoreCase("confcampo")) {
            vRegistros.addElement(oConfCampo);
        } else {
            String content = vcBuffer.toString().trim();
            //Datos de configuración para el editor de trabajo de campo
            if (lname.equals("cPlantilla")) {
                oConfCampo.setCPlantilla(content);
            } else if (lname.equals("nZoom")) {
                try {
                    oConfCampo.setNZoom(Integer.parseInt(content));
                } catch (Exception e) {
                    oConfCampo.setNZoom(0);
                }
            } else if (lname.equals("bCalidad")) {
                try {
                    int vnValor = Integer.parseInt(content);
                    boolean vbValor = true;
                    if (vnValor==0)
                        vbValor = false;
                    oConfCampo.setBCalidad(vbValor);
                } catch (Exception e) {
                    oConfCampo.setBCalidad(true);
                }
            } else if (lname.equals("cCX")) {
                oConfCampo.setCCX(content);
            } else if (lname.equals("cCY")) {
                oConfCampo.setCCY(content);
            } else if (lname.equals("cCX2")) {
                oConfCampo.setCCX2(content);
            } else if (lname.equals("cCY2")) {
                oConfCampo.setCCY2(content);
            } else if (lname.equals("cFactorX")) {
                oConfCampo.setCFactorX(content);
            } else if (lname.equals("cFactorY")) {
                oConfCampo.setCFactorY(content);
            } else if (lname.equals("cBoceto")) {
                oConfCampo.setCBoceto(content);
            } else if (lname.equals("cCXCentral")) {
                oConfCampo.setCCXCentral(content);
            } else if (lname.equals("cCYCentral")) {
                oConfCampo.setCCYCentral(content);
            } else if (lname.equals("nZona")) {
                try {
                    oConfCampo.setNZona(Integer.parseInt(content));
                } catch (Exception e) {
                    oConfCampo.setNZona(29);
                }
            }
        }
    }

    /**
     * Método que se encarga de recuperar los datos a partir del archivo XML.
     * Los datos son introducidos en un vector de elementos de la clase ConfCampo.
     * Los datos se recuperan de un archivo XML.
     * @param context Context. Contexto de la aplicación
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     * @return Vector. Elementos de la clase ConfCampo, conteniendo los datos recuperados del archivo XML.
     */
    public static Vector<ConfCampo> obtenerDatosXML(Context context, String nombreCarpeta, String nombreArchivo) {
        Vector<ConfCampo> vvResul = new Vector<>();
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
                ConfCampoXMLHandler handler = new ConfCampoXMLHandler();

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
     * @param pvRegistros Vector<ConfCampo>. Vector de elementos de la clase ConfCampo.
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     */
    public static boolean escribirXML (Context context, Vector<ConfCampo> pvRegistros, String nombreCarpeta, String nombreArchivo) {
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
                pStr.println("<!--<!DOCTYPE Registros SYSTEM \"ConfCampo.dtd\">-->");
                //Recorre el vector de elementos, para volcar uno a uno su contenido al fichero.
                int i = 0;
                while (i<pvRegistros.size()) {
                    //Obtiene el siguiente elemento
                    ConfCampo voConfCampo = pvRegistros.elementAt(i);
                    //Crea la estructura XML en el archivo
                    pStr.println("  <ConfCampo>");
                    pStr.println("    <cPlantilla>" + voConfCampo.getCPlantilla() + "</cPlantilla>");
                    pStr.println("    <cCX>" + voConfCampo.getCCX() + "</cCX>");
                    pStr.println("    <cCY>" + voConfCampo.getCCY() + "</cCY>");
                    pStr.println("    <cCX2>" + voConfCampo.getCCX2() + "</cCX2>");
                    pStr.println("    <cCY2>" + voConfCampo.getCCY2() + "</cCY2>");
                    pStr.println("    <nZona>" + voConfCampo.getNZona() + "</nZona>");
                    pStr.println("    <cFactorX>" + voConfCampo.getCFactorX() + "</cFactorX>");
                    pStr.println("    <cFactorY>" + voConfCampo.getCFactorY() + "</cFactorY>");
                    pStr.println("    <cBoceto>" + voConfCampo.getCBoceto() + "</cBoceto>");
                    pStr.println("    <nZoom>" + voConfCampo.getNZoom() + "</nZoom>");
                    pStr.println("    <cCXCentral>" + voConfCampo.getCCXCentral() + "</cCXCentral>");
                    pStr.println("    <cCYCentral>" + voConfCampo.getCCYCentral() + "</cCYCentral>");
                    String vcCalidad = "1";
                    if (!voConfCampo.getBCalidad())
                        vcCalidad = "0";
                    pStr.println("    <bCalidad>" + vcCalidad + "</bCalidad>");
                    pStr.println("  </ConfCampo>");
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

