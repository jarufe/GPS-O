package jaru.ori.logic.gpslog.xml;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;

import java.util.Vector;
import java.io.*;
import jaru.ori.logic.gpslog.*;
import jaru.ori.utils.Utilidades;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import jaru.ori.gui.gpslog.android.R;
import jaru.ori.utils.android.UtilsAndroid;

/**
 * Gestor SAX2 para poder transformar un archivo GPX con datos de coordenadas de puntos GPS.
 * Los datos se transforman y se integran dentro de la estructura de esta aplicación.
 *
 * @author jarufe
 * @version 1.0
 */
public class RegistrosGpxXMLHandler extends DefaultHandler
{
    private Registro oRegistro = new Registro();
    private Vector<Registro> vRegistros = new Vector<>();
    /** Buffer de almacenamiento de datos leidos. */
    protected StringBuffer vcBuffer = new StringBuffer();
    //Campo que contiene el tipo OCAD (0=name; 1=desc; 2=ninguno)
    private String cCampo = "2";
    //Tipo OCAD para elementos de tipo punto por defecto
    private String cTipoPuntos = "540.0";
    //Tipo OCAD para elementos de tipo línea por defecto
    private String cTipoLineas = "506.0";
    //Valor que indica si se ha de sobreescribir el conjunto de registros o si se añade
    private boolean bSobreescribir = true;
    //Valor que indica el contador para establecer el campo ID del conjunto de registros
    private int nContador = 0;
    //Durante un procesamiento de datos, almacena el tipo activo y los datos del elemento activo
    private String cTipoActivo = "";
    private boolean bReconocer = false;
    private String cName = "";
    private String cDesc = "";
    private String cCmt = "";
    private String cTipoOCAD = "";
    private String cTipoOBM = "";

    private static android.app.Application oApp = null;
    private static android.content.res.Resources oRes = null;

    /**
     * Devuelve el vector de resultados del procesamiento.
     * Es un vector de elementos de la clase Producto.
     * @return Vector. Conjunto de datos de la clase Registro.
     */
    public Vector<Registro> getVRegistros () {
        return vRegistros;
    }
    /**
     * Establece el vector de resultados, por si se quiere añadir registros
     * a un conjunto de datos existente
     * @param pvRegistros Vector. Conjunto de registros existente.
     */
    public void setVRegistros (Vector<Registro> pvRegistros) {
        vRegistros = pvRegistros;
    }
    /**
     * Establece la propiedad que indica qué campo del fichero GPX se usará
     * para extraer el tipo OCAD que se va a usar para caracterizar al punto o línea actual.
     * 0=name; 1=desc; 2=ninguno
     * @param pcCampo String.
     */
    public void setCCampo (String pcCampo) {
        cCampo = pcCampo;
    }
    /**
     * Establece la propiedad que indica el tipo OCAD que se usará por defecto
     * para los elementos puntuales
     * @param pcTipoPuntos String.
     */
    public void setCTipoPuntos (String pcTipoPuntos) {
        cTipoPuntos = pcTipoPuntos;
    }
    /**
     * Establece la propiedad que indica el tipo OCAD que se usará por defecto
     * para los elementos lineales
     * @param pcTipoLineas String.
     */
    public void setCTipoLineas (String pcTipoLineas) {
        cTipoLineas = pcTipoLineas;
    }
    /**
     * Establece la propiedad que indica si se va a sobreescribir el conjunto
     * de registros o si se los nuevos se van a añadir a los existentes.
     * @param pbSobreescribir boolean.
     */
    public void setBSobreescribir (boolean pbSobreescribir) {
        bSobreescribir = pbSobreescribir;
    }
    /**
     * Establece la propiedad que indica el contador para el campo ID del conjunto de datos.
     * @param pnContador int.
     */
    public void setNContador (int pnContador) {
        nContador = pnContador;
    }
    /**
     * Establece un objeto que representa a la aplicación, para que se
     * pueda acceder a los recursos de la misma, en este caso las cadenas.
     * @param poApp android.app.Application.
     */
    public static void setOApp (android.app.Application poApp) {
        oApp = poApp;
    }
    /**
     * Establece un objeto que representa a los recursos, para que se
     * pueda acceder a los recursos de la misma, en este caso las cadenas.
     * @param poRes android.content.res.Resources.
     */
    public static void setORes (android.content.res.Resources poRes) {
        oRes = poRes;
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
        if (lname.equalsIgnoreCase("wpt")) {
            cTipoActivo = "wpt";
            bReconocer = true;
            cName = "";
            cDesc = "";
            cCmt = "";
            cTipoOCAD = "";
            cTipoOBM = "";
            nContador = nContador + 1;
            oRegistro = new Registro();
            oRegistro.setCID(nContador + "");
            oRegistro.setNTipo(0);
            oRegistro.setCTipoOCAD(cTipoPuntos);
            oRegistro.setCTipoOBM(cTipoOBM);
            String vcLat = attributes.getValue("lat");
            String vcLon = attributes.getValue("lon");
            oRegistro.setCCY(Utilidades.grados(Double.parseDouble(vcLat)));
            oRegistro.setCCX(Utilidades.grados(Double.parseDouble(vcLon)));
        } else if (lname.equalsIgnoreCase("trk")) {
            cTipoActivo = "trk";
            bReconocer = true;
            cName = "";
            cDesc = "";
            cCmt = "";
            cTipoOCAD = cTipoLineas;
            cTipoOBM = "";
        } else if (lname.equalsIgnoreCase("trkseg")) {
            nContador = nContador + 1;
        } else if (lname.equalsIgnoreCase("trkpt")) {
            bReconocer = true;
            oRegistro = new Registro();
            oRegistro.setCID(nContador + "");
            oRegistro.setNTipo(1);
            oRegistro.setCTipoOCAD(cTipoOCAD);
            oRegistro.setCTipoOBM("");
            String vcLat = attributes.getValue("lat");
            String vcLon = attributes.getValue("lon");
            oRegistro.setCCY(Utilidades.grados(Double.parseDouble(vcLat)));
            oRegistro.setCCX(Utilidades.grados(Double.parseDouble(vcLon)));
        } else if (lname.equalsIgnoreCase("rte")) {
            nContador = nContador + 1;
            cTipoActivo = "rte";
            bReconocer = true;
            cName = "";
            cDesc = "";
            cCmt = "";
            cTipoOCAD = cTipoLineas;
            cTipoOBM = "";
        } else if (lname.equalsIgnoreCase("rtept")) {
            bReconocer = true;
            oRegistro = new Registro();
            oRegistro.setCID(nContador + "");
            oRegistro.setNTipo(1);
            oRegistro.setCTipoOCAD(cTipoOCAD);
            oRegistro.setCTipoOBM("");
            String vcLat = attributes.getValue("lat");
            String vcLon = attributes.getValue("lon");
            oRegistro.setCCY(Utilidades.grados(Double.parseDouble(vcLat)));
            oRegistro.setCCX(Utilidades.grados(Double.parseDouble(vcLon)));
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
        if (lname.equalsIgnoreCase("wpt")) {
            oRegistro.setCTipoOCAD(cTipoOCAD);
            oRegistro.setCTipoOBM("");
            oRegistro.setCDesc(cDesc);
            vRegistros.addElement(oRegistro);
            bReconocer = false;
        }else if (lname.equalsIgnoreCase("trkpt")) {
            oRegistro.setCTipoOCAD(cTipoOCAD);
            oRegistro.setCTipoOBM("");
            oRegistro.setCDesc(cDesc);
            vRegistros.addElement(oRegistro);
            bReconocer = false;
        }else if (lname.equalsIgnoreCase("trkseg")) {
            bReconocer = false;
        }else if (lname.equalsIgnoreCase("trk")) {
            bReconocer = false;
        }else if (lname.equalsIgnoreCase("rtept")) {
            oRegistro.setCTipoOCAD(cTipoOCAD);
            oRegistro.setCTipoOBM("");
            oRegistro.setCDesc(cDesc);
            vRegistros.addElement(oRegistro);
            bReconocer = false;
        }else if (lname.equalsIgnoreCase("rte")) {
            bReconocer = false;
        } else {
            if (bReconocer) {
                String content = vcBuffer.toString().trim();
                //Datos de un punto de GPS
                if (lname.equalsIgnoreCase("elev")) {
                    oRegistro.setCElev(content);
                } else if (lname.equalsIgnoreCase("time")) {
                    oRegistro.setCFecha(content);
                } else if (lname.equalsIgnoreCase("desc")) {
                    //Si el usuario ha dicho que el tipo OCAD viene en el campo desc, trata de escribirlo
                    //pero sólo si lo valida como un tipo OCAD correcto.
                    if (cCampo.equals("1")) {
                        if (esTipoOCAD (content)) {
                            cTipoOCAD = content;
                        }
                    } else {
                        cDesc = content;
                    }
                } else if (lname.equalsIgnoreCase("name")) {
                    //Si el usuario ha dicho que el tipo OCAD viene en el campo name, trata de escribirlo
                    //pero sólo si lo valida como un tipo OCAD correcto.
                    if (cCampo.equals("0")) {
                        if (esTipoOCAD (content))
                            cTipoOCAD = content;
                    }
                } else if (lname.equalsIgnoreCase("cmt")) {
                    //Si el usuario dice que en el campo desc viene el tipo OCAD
                    //los comentarios del punto se meten como descripción del registro.
                    if (cCampo.equals("1")) {
                        cDesc = content;
                    }
                }
            }
        }
    }

    /**
     * Método que se encarga de recuperar los datos a partir del archivo XML.
     * Los datos son introducidos en un vector de elementos de la clase Registro.
     * Los datos se recuperan de un archivo XML.
     * @param context Context. Contexto de la aplicación
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     * @return Vector. Elementos de la clase Registro, conteniendo los datos recuperados del archivo XML.
     */
    public static Vector<Registro> obtenerDatosXML(Context context, String nombreCarpeta, String nombreArchivo) {
        Vector<Registro> vvResul = new Vector<>();

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
                // parse the document
                SAXParser parser = spf.newSAXParser();
                RegistrosGpxXMLHandler handler = new RegistrosGpxXMLHandler();
                //En este metodo, no se usa un campo OCAD para los tipos de objetos
                //Tambien, se sobreescribe el conjunto de datos.
                handler.setBSobreescribir(true);
                handler.setCCampo("2");
                handler.setCTipoPuntos("540.0");
                handler.setCTipoLineas("506.0");
                handler.setNContador(0);
                //Se obtiene el archivo y se procesa
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                InputSource source = new InputSource(inputStream);
                parser.parse(source, (DefaultHandler) handler);
                //Después de trabajar con el fichero XML, los datos se encuentran en forma de vector
                vvResul = handler.getVRegistros();
                Log.i("GPS-O", "Archivo procesado. Registros: " + (vvResul != null ? vvResul.size() : 0));
                if (inputStream!=null) inputStream.close();
            }
            if (cursor != null) cursor.close();
        }
        catch (Exception e) {
            Log.e("GPS-O", "Error cargando XML", e);
            if (vvResul!=null) vvResul.removeAllElements();
        }
        return vvResul;
    }
    /**
     * Método que se encarga de recuperar los datos a partir del archivo XML.
     * Los datos son introducidos en un vector de elementos de la clase Registro.
     * Los datos se recuperan de un archivo XML.
     * @param context Context. Contexto de la aplicación
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     * @param pvRegistros Vector. Conjunto de datos actuales.
     * @param pbSobreescribir boolean. El conjunto de datos se sobreescribe o se aÃ±aden a uno existente.
     * @param pcCampo String. El campo que se va a utilizar para obtener el tipo OCAD (0=name; 1=desc; 2=ninguno)
     * @param pcTipoPuntos String. Objeto OCAD por defecto para elementos puntuales.
     * @param pcTipoLineas String. Objeto OCAD por defecto para elementos lineales.
     * @return Vector. Elementos de la clase Registro, conteniendo los datos recuperados del archivo XML.
     */
    public static Vector<Registro> obtenerDatosXML(Context context, String nombreCarpeta, String nombreArchivo,
                                                   Vector<Registro> pvRegistros,
                                                   boolean pbSobreescribir, String pcCampo, String pcTipoPuntos, String pcTipoLineas) {
        Vector<Registro> vvResul = new Vector<>();
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
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                spf.setNamespaceAware(true);
                // parse the document
                SAXParser parser = spf.newSAXParser();
                RegistrosGpxXMLHandler handler = new RegistrosGpxXMLHandler();
                //En este metodo, se puede usar un campo OCAD para los tipos de objetos
                //Tambien, se puede sobreescribir o añadir el conjunto de datos.
                handler.setBSobreescribir(pbSobreescribir);
                if (pbSobreescribir) {
                    pvRegistros.removeAllElements();
                    handler.setVRegistros(pvRegistros);
                    handler.setNContador(0);
                } else {
                    handler.setVRegistros(pvRegistros);
                    handler.setNContador(obtenerContador(pvRegistros));
                }
                handler.setCCampo(pcCampo);
                handler.setCTipoPuntos(pcTipoPuntos);
                handler.setCTipoLineas(pcTipoLineas);
                //Se obtiene el archivo y se procesa
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                InputSource source = new InputSource(inputStream);
                parser.parse(source, (DefaultHandler) handler);
                //Después de trabajar con el fichero XML, los datos se encuentran en forma de vector
                vvResul = handler.getVRegistros();
                Log.i("GPS-O", "Archivo procesado. Registros: " + (vvResul != null ? vvResul.size() : 0));
                if (inputStream!=null) inputStream.close();
            } else {
                Log.e("GPS-O", "No se pudo obtener URI del archivo XML");
            }
        }
        catch (Exception e) {
            Log.e("GPS-O", "Error cargando XML", e);
            if (vvResul!=null) vvResul.removeAllElements();
        }
        return vvResul;
    }
    /**
     * Vuelca los datos de parámetros a un archivo XML, cuyo nombre es el que 
     * se proporciona en el segundo argumento.
     * @param context Context. Contexto de la aplicación
     * @param pvRegistros Vector<Registro>. Vector de elementos de la clase Registro.
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     */
    public static boolean escribirXML (Context context, Vector<Registro> pvRegistros, String nombreCarpeta, String nombreArchivo) {
        boolean resultado = true;
        //Llama al método que crea el fichero indicando que se va a usar el campo Tipo OCAD principal
        resultado = escribirXML(context, pvRegistros, nombreCarpeta, nombreArchivo, false);
        return resultado;
    }

    /**
     * Vuelca los datos de parámetros a un archivo XML, cuyo nombre es el que
     * se proporciona en el segundo argumento.
     * @param context Context. Contexto de la aplicación
     * @param pvRegistros Vector<Registro>. Vector de elementos de la clase Registro.
     * @param nombreCarpeta String. Carpeta donde se guardan los datos
     * @param nombreArchivo String. Nombre del archivo XML donde se encuentran los datos que se han de recuperar.
     * @param pbDual boolean. Indica si se quiere usar el valor dual para OBM, en lugar del tipo OCAD principal
     */
    public static boolean escribirXML (Context context, Vector<Registro> pvRegistros, String nombreCarpeta, String nombreArchivo, boolean pbDual) {
        boolean resultado = true;
        OutputStream os = null;
        PrintStream pStr = null;
        String vcIdAct = "";
        String vcTipoAct = "";
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
                pStr.println("<?xml version=\"1.0\"?>");
                pStr.println("<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">");
                pStr.println("<metadata>");
                pStr.println("<name>Export from GPS-O</name>");
                pStr.println("<copyright></copyright>");
                pStr.println("<author></author>");
                pStr.println("<keyword>GPSLog</keyword>");
                pStr.println("<link></link>");
                pStr.println("<description>Export from GPS-O</description>");
                java.util.Date vdActual = Utilidades.getCurDate();
                String vcFecha = Utilidades.format(vdActual, "yyyy-MM-dd");
                String vcHora = Utilidades.format(vdActual, "HH:mm:ss");
                pStr.println("<time>" + vcFecha + "T" + vcHora + "Z</time>");
                pStr.println("</metadata>");
                //Recorre el vector de elementos, para volcar uno a uno su contenido al fichero.
                int i = 0;
                while (i < pvRegistros.size()) {
                    //Obtiene el siguiente elemento
                    Registro voRegistro = pvRegistros.elementAt(i);
                    //Crea la estructura XML en el archivo
                    //Si cambia el ID, se tiene que cerrar el objeto actual y comenzar otro
                    if (!voRegistro.getCID().equals(vcIdAct)) {
                        //Si se había abierto un objeto, primero se cierra (trk o wpt)
                        if (vcTipoAct.equals("trk")) {
                            pStr.println("</trkseg>");
                            pStr.println("</trk>");
                        }
                        //Ahora se abre uno nuevo
                        if (voRegistro.getNTipo() == 0) {
                            vcTipoAct = "wpt";
                        } else {
                            vcTipoAct = "trk";
                            pStr.println("<trk>");
                            //Sólo si es línea y pide usar el valor dual OBM, entonces escribe el valor de ese campo
                            if (voRegistro.getNTipo() == 1 && pbDual && !voRegistro.getCTipoOBM().isEmpty()) {
                                pStr.println("<name>" + voRegistro.getCID() + "(" + voRegistro.getCTipoOBM() + ")</name>");
                                pStr.println("<desc>" + voRegistro.getCTipoOBM() + "</desc>");
                                //en caso contrario escribe el valor principal de tipo OCAD
                            } else {
                                pStr.println("<name>" + voRegistro.getCID() + "(" + voRegistro.getCTipoOCAD() + ")</name>");
                                pStr.println("<desc>" + voRegistro.getCTipoOCAD() + "</desc>");
                            }
                            pStr.println("<cmt>" + voRegistro.getCDesc() + "</cmt>");
                            pStr.println("<number>" + voRegistro.getCID() + "</number>");
                            pStr.println("<trkseg>");
                        }
                        //Ahora se guarda el valor del ID actual
                        vcIdAct = voRegistro.getCID();
                    }
                    //Se calcula el dato de latitud y longitud
                    double vnLat = Utilidades.grados(voRegistro.getCCY());
                    double vnLon = Utilidades.grados(voRegistro.getCCX());
                    //Se escriben los datos correspondientes al punto actual
                    if (vcTipoAct.equals("trk")) {
                        pStr.println("<trkpt lat=\"" + vnLat + "\" lon=\"" + vnLon + "\">");
                        String vcElev = "0";
                        if (!voRegistro.getCElev().isEmpty())
                            vcElev = voRegistro.getCElev();
                        pStr.println("<ele>" + vcElev + "</ele>");
                        pStr.println("<time>" + voRegistro.getCFecha() + "</time>");
                        pStr.println("</trkpt>");
                    } else {
                        pStr.println("<wpt lat=\"" + vnLat + "\" lon=\"" + vnLon + "\">");
                        String vcElev = "0";
                        if (!voRegistro.getCElev().isEmpty())
                            vcElev = voRegistro.getCElev();
                        pStr.println("<ele>" + vcElev + "</ele>");
                        pStr.println("<name>" + voRegistro.getCID() + "(" + voRegistro.getCTipoOCAD() + ")</name>");
                        pStr.println("<desc>" + voRegistro.getCTipoOCAD() + "</desc>");
                        pStr.println("<cmt>" + voRegistro.getCDesc() + "</cmt>");
                        pStr.println("<sym>Flag</sym>");
                        pStr.println("</wpt>");
                    }
                    i++;
                }
                //Si se habï¿½a abierto un objeto de tipo trk, se cierra
                if (vcTipoAct.equals("trk")) {
                    pStr.println("</trkseg>");
                    pStr.println("</trk>");
                }
                //Cierra la estructura XML en el archivo, y el propio archivo.
                pStr.println("</gpx>");
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

    /**
     * Método que obtiene el mayor número asignado al campo ID del conjunto de datos actual.
     * De esta forma, se puede asignar un contador para los nuevos datos que se van
     * a añadir al resultado.
     * @param pvRegistros Vector. Conjunto de datos de tipo Registro.
     * @return int. Mayor nÃºmero asignado al campo ID.
     */
    private static int obtenerContador (Vector<Registro> pvRegistros) {
        int vnContador = 0;
        int vnActual = 0;
        try {
            //Recorre el vector de elementos, para establecer el mayor número utilizado
            int i = 0;
            while (i<pvRegistros.size()) {
                //Obtiene el siguiente elemento
                Registro voRegistro = pvRegistros.elementAt(i);
                //Intenta procesar el campo ID como numérico, para conocer el mayor número utilizado
                try {
                    vnActual = Integer.parseInt(voRegistro.getCID());
                    if (vnActual > vnContador)
                        vnContador = vnActual;
                } catch (Exception e2) {
                    Log.e("GPS-O", "Campo ID no numérico", e2);
                }
                i++;
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error obteniendo contador", e);
            vnContador = 0;
        }
        return vnContador;
    }

    /**
     * Método que comprueba si el dato que se pasa corresponde a un tipo OCAD válido.
     * Para realizar la comprobación se hace un seguimiento de los mensajes que
     * contienen los tipos OCAD válidos, en la clase de mensajes OCAD.
     * @param pcValor String. Valor que supuestamente es un tipo OCAD válido.
     * @return boolean. Resultado de la comprobación.
     */
    public static boolean esTipoOCAD (String pcValor) {
        boolean vbResult = false;
        try {
            int vnMin = Integer.parseInt(oApp.getString(R.string.ORI_ML00994));
            int vnMax = Integer.parseInt(oApp.getString(R.string.ORI_ML00995));
            int i = vnMin;
            while (i<=vnMax && !vbResult) {
                int vnId = oRes.getIdentifier("ORI_ML0" + i, "string", "jaru.ori.gui.gpslog.android");
                String vcTexto = oApp.getString(vnId);
                if (vcTexto.substring(0, 5).equals(pcValor))
                    vbResult = true;
                i++;
            }
            vnMin = Integer.parseInt(oApp.getString(R.string.ORI_ML00996));
            vnMax = Integer.parseInt(oApp.getString(R.string.ORI_ML00997));
            i = vnMin;
            while (i<=vnMax && !vbResult) {
                int vnId = oRes.getIdentifier("ORI_ML0" + i, "string", "jaru.ori.gui.gpslog.android");
                String vcTexto = oApp.getString(vnId);
                if (vcTexto.substring(0, 5).equals(pcValor))
                    vbResult = true;
                i++;
            }
            vnMin = Integer.parseInt(oApp.getString(R.string.ORI_ML00998));
            vnMax = Integer.parseInt(oApp.getString(R.string.ORI_ML00999));
            i = vnMin;
            while (i<=vnMax && !vbResult) {
                int vnId = oRes.getIdentifier("ORI_ML0" + i, "string", "jaru.ori.gui.gpslog.android");
                String vcTexto = oApp.getString(vnId);
                if (vcTexto.substring(0, 5).equals(pcValor))
                    vbResult = true;
                i++;
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error comprobando tipo OCAD", e);
            vbResult = false;
        }
        return vbResult;
    }
}

