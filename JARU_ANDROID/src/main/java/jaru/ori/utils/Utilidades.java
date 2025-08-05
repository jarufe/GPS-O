package jaru.ori.utils;

import java.text.*;
import java.util.*;
import java.io.*;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.*;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Clase que implementa algunas operaciones bÃ¡sicas, como por ejemplo con fechas y con ficheros.
 * <P>
 * Su objetivo es facilitar sobre todo el tratamiento de valores de fechas que provienen de una BD,
 * y tambiÃ©n la correcta transformaciÃ³n de una fecha en algÃºn formato de visualizaciÃ³n hacia el formato
 * de grabaciÃ³n en la BD, segÃºn la nomenclatura utilizada en JDBC.
 * </P>
 * AdemÃ¡s, se han aÃ±adido mÃ©todos que realizan cÃ¡lculos de conversiÃ³n de coordenadas.
 * </P>
 * @author jarufe
 * @version 1.0
 */
public class Utilidades {

    final static int[] diasmes = {29,31,28,31,30,31,30,31,31,30,31,30,31};
    /*
private static java.util.ResourceBundle conRes = java.util.ResourceBundle.getBundle("jaru.ori.library.messages.Configuracion");
private static int nTamX = Integer.parseInt(conRes.getString("nTamX")); //240;
private static int nTamY = Integer.parseInt(conRes.getString("nTamY")); //300;
private static int nIniX = Integer.parseInt(conRes.getString("nIniX")); //0;
private static int nIniY = Integer.parseInt(conRes.getString("nIniY")); //22;
private static int nTamFuente = Integer.parseInt(conRes.getString("nTamFuente")); //8;
*/
    private static int nTamX = 240;
    private static int nTamY = 300;
    private static int nIniX = 0;
    private static int nIniY = 22;
    private static int nTamFuente = 8;

    private static int nDecimalesNMEA = 4;
    private static int nLecturasNMEA = 1000;

    private static String cDirActual = "/sdcard";
    private static String cFicheroSel = "";
    private static String cFicheroSelNombre = "";

    /**
     * Constructor por defecto de la clase.
     */
    public Utilidades() {
        super();
    }
    /**
     * Devuelve el valor de la propiedad estÃ¡tica que globaliza el uso del parÃ¡metro
     * de tamaÃ±o mÃ¡ximo horizontal de pantalla.
     * @return int. tamaÃ±o mÃ¡ximo horizontal de pantalla.
     */
    public static int getNTamX() {
        return nTamX;
    }
    /**
     * Devuelve el valor de la propiedad estÃ¡tica que globaliza el uso del parÃ¡metro
     * de tamaÃ±o mÃ¡ximo vertical de pantalla.
     * @return int. tamaÃ±o mÃ¡ximo vertical de pantalla.
     */
    public static int getNTamY() {
        return nTamY;
    }
    /**
     * Devuelve el valor de la propiedad estÃ¡tica que globaliza el uso del parÃ¡metro
     * de posiciÃ³n inicial horizontal en pantalla.
     * @return int. posiciÃ³n inicial horizontal en pantalla.
     */
    public static int getNIniX() {
        return nIniX;
    }
    /**
     * Devuelve el valor de la propiedad estÃ¡tica que globaliza el uso del parÃ¡metro
     * de posiciÃ³n inicial vertical en pantalla.
     * @return int. posiciÃ³n inicial vertical en pantalla.
     */
    public static int getNIniY() {
        return nIniY;
    }
    /**
     * Devuelve el valor de la propiedad estÃ¡tica que globaliza el uso del parÃ¡metro
     * de tamaÃ±o de fuente.
     * @return int. tamaÃ±o de fuente.
     */
    public static int getNTamFuente() {
        return nTamFuente;
    }
    /**
     * Establece el valor de la propiedad estÃ¡tica que globaliza el uso del parÃ¡metro
     * de tamaÃ±o de fuente.
     * @param pnValor int. tamaÃ±o de fuente.
     */
    public static void setNTamFuente(int pnValor) {
        nTamFuente = pnValor;
    }
    /**
     * Devuelve el valor de la propiedad estÃ¡tica que globaliza el uso del valor de
     * nÃºmero de decimales utilizados por las sentencias NMEA para caracterizar los
     * valores de coordenadas.
     * @return int. nÃºmero de decimales.
     */
    public static int getNDecimalesNMEA() {
        return nDecimalesNMEA;
    }
    /**
     * Establece el valor de la propiedad estÃ¡tica que globaliza el uso del valor de
     * nÃºmero de decimales utilizados por las sentencias NMEA para caracterizar los
     * valores de coordenadas.
     * @param pnValor int. nÃºmero de decimales.
     */
    public static void setNDecimalesNMEA(int pnValor) {
        nDecimalesNMEA = pnValor;
    }
    /**
     * Devuelve el valor de la propiedad estÃ¡tica que globaliza el contador mÃ¡ximo
     * de lecturas que se pueden acumular en un objeto de la clase TransfGeografica.
     * @return int. nÃºmero mÃ¡ximo de lecturas.
     */
    public static int getNLecturasNMEA() {
        return nLecturasNMEA;
    }
    /**
     * Establece el valor de la propiedad estÃ¡tica que globaliza el contador mÃ¡ximo
     * de lecturas que se pueden acumular en un objeto de la clase TransfGeografica.
     * @param pnValor int. nÃºmero de decimales.
     */
    public static void setNLecturasNMEA(int pnValor) {
        nLecturasNMEA = pnValor;
    }
    /**
     * Establece los valores de posiciÃ³n inicial y tamaÃ±o mÃ¡ximo de pantalla, para
     * un uso global en toda la aplicaciÃ³n, desde su inicializaciÃ³n en la pantalla
     * inicial de bienvenida.
     * @param pnIniX int. posiciÃ³n inicial horizontal en pantalla.
     * @param pnIniY int. posiciÃ³n inicial vertical en pantalla.
     * @param pnTamX int. tamaÃ±o mÃ¡ximo horizontal de pantalla.
     * @param pnTamY int. tamaÃ±o mÃ¡ximo vertical de pantalla.
     */
    public static void setCoords (int pnIniX, int pnIniY, int pnTamX, int pnTamY) {
        nIniX = pnIniX;
        nIniY = pnIniY;
        nTamX = pnTamX;
        nTamY = pnTamY;
    }
    /**
     * Devuelve el valor de la propiedad que contiene un directorio. Se usa para
     * compartir ese dato dentro de una aplicaciÃ³n.
     * @return String
     */
    public static String getCDirActual() {
        return cDirActual;
    }
    /**
     * Establece el valor de la propiedad que contiene un directorio. Se usa para
     * compartir ese dato dentro de una aplicaciÃ³n.
     * @param pcDirActual String
     */
    public static void setCDirActual (String pcDirActual) {
        cDirActual = pcDirActual;
    }
    /**
     * Devuelve el valor de una propiedad que permite compartir un nombre de fichero
     * en una aplicaciÃ³n. En este caso es path+nombre.
     * @return String
     */
    public static String getCFicheroSel() {
        return cFicheroSel;
    }
    /**
     * Establece el valor de una propiedad que permite compartir un nombre de fichero
     * en una aplicaciÃ³n. En este caso es path+nombre.
     * @param pcFicheroSel String
     */
    public static void setCFicheroSel (String pcFicheroSel) {
        cFicheroSel = pcFicheroSel;
    }
    /**
     * Devuelve el valor de una propiedad que permite compartir un nombre de fichero
     * en una aplicaciÃ³n. En este caso es sÃ³lo el nombre.
     * @return String
     */
    public static String getCFicheroSelNombre() {
        return cFicheroSelNombre;
    }
    /**
     * Establece el valor de una propiedad que permite compartir un nombre de fichero
     * en una aplicaciÃ³n. En este caso es sÃ³lo el nombre.
     * @param pcFicheroSelNombre String
     */
    public static void setCFicheroSelNombre (String pcFicheroSelNombre) {
        cFicheroSelNombre = pcFicheroSelNombre;
    }

    /**
     * Recibe dos strings que son fechas y las compara.
     * @return boolean. Flag que indica si la primera fecha es mayor que la segunda (valor=true en este caso)
     * @param formato String. Formato en el que se representan las fechas pasadas como parÃ¡metros.
     * @param dateMin String. Texto de la fecha que se quiere comprobar. Viene en el formato indicado en el primer parÃ¡metro.
     * @param dateMax String. Texto de la fecha que sirve de lÃ­mite de comprobaciÃ³n. Viene en el formato indicado en el primer parÃ¡metro.
     */
    public static boolean compareDate(String formato, String dateMin, String dateMax) throws Exception{
        if (dateMin == null || dateMax == null || dateMin.equals("") || dateMax.equals(""))
            return true;
        else{
            String voFormatoAux = "yyyyMMddHHmm";
            long firstDate = Long.parseLong(transform(dateMin, formato, voFormatoAux).toString());
            long secondDate = Long.parseLong(transform(dateMax, formato, voFormatoAux).toString());
            if (firstDate > secondDate)
                return false;
            else
                return true;
        }
    }
    /**
     * devuelve el dÃ­a de la semana del primer dÃ­a de un mes
     * @param anno int
     * @param mes int
     */
    public static int Devdiaini(int anno, int mes){  //devuelve el dia de inicio
        if (mes < 0 || mes > 12)
            return -1;

        int inter = 365*(anno - 1980) + (anno - 1981)/4;
        inter = inter + 2 ; //incrementar adecuadamente
        if(mes>1) { for (int i=1;i< mes; i++){inter = inter + diasmes[i];} }
        int salida =  inter % 7 ;
        return salida;      // salida = inter.mod(7);
    }
    /**
     * devuelve el nÃºmero de dÃ­as de un mes
     * @param anno int
     * @param mes int
     */
    public static int Devdiasmes(int anno,int mes){
        if (mes < 0 || mes > 12)
            return -1;

        if (anno%4 == 0){  diasmes[2]=29; }     //ComprobaciÃ¯Â¿Â½n bisiesto
        else {diasmes[2]=28;}
        return  diasmes[mes];
    }
    /**
     * Recibe dos strings que son fechas y las compara
     * @return boolean. Flag que indica si la fecha de entrada se encuentra en un intervalo (lÃ­mites incluidos)
     * @param date java.util.Date. Fecha que se quiere comprobar.
     * @param dateMin java.util.Date. Fecha que representa el lÃ­mite inferior del intervalo.
     * @param dateMax java.util.Date. Fecha que representa el lÃ­mite superior del intervalo.
     */
    public static boolean fechaEnIntervalo(Date date, Date dateMin, Date dateMax) throws Exception{
        if (date == null || dateMin == null || dateMax == null || dateMin.equals("") || dateMax.equals("") || dateMin.equals(""))
            throw new Exception("Alguna fecha vacia");
        else{
            String voFormatoAux = "yyyyMMdd";
            int nDate = Integer.parseInt(format(date, voFormatoAux).toString());
            int firstDate = Integer.parseInt(format(dateMin, voFormatoAux).toString());
            int secondDate = Integer.parseInt(format(dateMax, voFormatoAux).toString());
            if (nDate >= firstDate && nDate <= secondDate)
                return true;
            else
                return false;
        }
    }
    /**
     * Dado un objeto de tipo fecha, que contiene un valor de fecha, y dado un formato
     * especificado en el segundo parÃ¡metro, este mÃ©todo se encarga de devolver una
     * cadena de caracteres con el valor de la fecha expresado en dicho formato.
     * @return java.lang.String
     * @param pdFecha java.util.Date
     * @param pcFormato java.lang.String
     * @exception java.lang.Exception ExcepciÃ³n
     */
    public static String format(Date pdFecha, String pcFormato) throws java.lang.Exception {
        String vcResul = "";
        SimpleDateFormat df = new SimpleDateFormat (pcFormato);

        df.setLenient(false);
        vcResul = df.format(pdFecha);

        return vcResul;
    }
    /**
     * Dado un valor en forma de cadena de caracteres (que se supone que tiene una fecha),
     * y dos formatos (origen y destino), este mÃ©todo devuelve la misma fecha de inicio,
     * pero transformada desde su formato de origen al formato de destino.
     * Por lo tanto, se supone que el formato de origen es la declaraciÃ³n del formato
     * en el que se supone que estÃ¡ la fecha. El segundo formato es aquÃ©l en el que
     * se va a representar la fecha como valor de salida.
     * @return java.lang.String Texto con la fecha transformada al segundo formato.
     * @param pcTexto java.lang.String. Texto con una fecha expresada en el primer formato.
     * @param pcFormato1 java.lang.String. DefiniciÃ³n del formato en el que se encuentra la fecha de origen. Por ejemplo: DD/MM/YYYY
     * @param pcFormato2 java.lang.String. DefiniciÃ³n del formato en el que se encuentra la fecha de origen. Por ejemplo: YYYY-MM-DD
     */
    public static String transform(String pcTexto, String pcFormato1, String pcFormato2) throws java.lang.Exception {
        String vcResul = "";

        SimpleDateFormat df1 = new SimpleDateFormat (pcFormato1);
        SimpleDateFormat df2 = new SimpleDateFormat (pcFormato2);
        Date vdFecha = new Date();

        df1.setLenient(false);
        df2.setLenient(false);
        try {
            vdFecha = df1.parse (pcTexto);
            vcResul = df2.format(vdFecha);
        } catch (Exception e){
            vcResul = "";
        }

        return vcResul;
    }

    /**
     * Devuelve la fecha/hora actual del sistema, como objeto de la clase java.util.Date.
     * Una transformaciÃ³n posterior desde el cÃ³digo del programa podrÃ¡ quedarse con
     * la parte que mÃ¡s interese (fecha, hora o cualquier otro formateo).<BR>
     * El objetivo de este mÃ©todo es hacer que la aplicaciÃ³n, se encuentre en el cliente
     * que se encuentre, recupere la hora actual de un lugar unico.
     * En este caso se intenta recuperar la fecha/hora de la BD que se utiliza en la
     * aplicaciÃ³n, y que normalmente estarÃ¡ situada en un servidor central.<BR>
     * Como la aplicaciÃ³n puede ejecutarse en una variedad de gestores de bases de datos,
     * este mÃ©todo lee del archivo de propiedades (ConfMessages.properties) de quÃ© tipo
     * de BD se trata y lanza la consulta SQL adecuada.
     * @return java.util.Date Fecha/Hora actual del sistema
     * @exception java.lang.Exception ExcepciÃ³n
     */
    public static java.util.Date getCurDate() {
        java.util.Date voFechaActual = null;
        try {
		/*
		com.ibm.ivj.db.uibeans.Select voSelect = new com.ibm.ivj.db.uibeans.Select();
		voSelect.setReadOnly(false);
		if (conRes.getString("BD").toLowerCase().equals("oracle"))
			voSelect.setQuery(new com.ibm.ivj.db.uibeans.Query(ar.zsv.model.Connection1.Con(), ar.zsv.model.Connection1.ORACLE_LeerFechaHora()));
		else if (conRes.getString("BD").toLowerCase().equals("db2"))
			voSelect.setQuery(new com.ibm.ivj.db.uibeans.Query(ar.zsv.model.Connection1.Con(), ar.zsv.model.Connection1.DB2_LeerFechaHora()));
		else if (conRes.getString("BD").toLowerCase().equals("mysql"))
			voSelect.setQuery(new com.ibm.ivj.db.uibeans.Query(ar.zsv.model.Connection1.Con(), ar.zsv.model.Connection1.MYSQL_LeerFechaHora()));
		else if (conRes.getString("BD").toLowerCase().equals("solid"))
			voSelect.setQuery(new com.ibm.ivj.db.uibeans.Query(ar.zsv.model.Connection1.Con(), ar.zsv.model.Connection1.SOLID_LeerFechaHora()));
		else if (conRes.getString("BD").toLowerCase().equals("access"))
			voSelect.setQuery(new com.ibm.ivj.db.uibeans.Query(ar.zsv.model.Connection1.Con(), ar.zsv.model.Connection1.ACCESS_LeerFechaHora()));
		else if (conRes.getString("BD").toLowerCase().equals("sybase"))
			voSelect.setQuery(new com.ibm.ivj.db.uibeans.Query(ar.zsv.model.Connection1.Con(), ar.zsv.model.Connection1.SYBASE_LeerFechaHora()));
		voSelect.execute();
		if (voSelect.getRowCount() <= 0)
			voFechaActual = new java.util.Date();
		else {
			voFechaActual = (java.util.Date)voSelect.getColumnValue("FECHA");
		}
		voSelect.close();
		*/
            voFechaActual = new java.util.Date();
        } catch (Exception e1) {
            voFechaActual = new java.util.Date();
        } catch (java.lang.Throwable ivjExc) {
            voFechaActual = new java.util.Date();
        }
        return voFechaActual;
    }
    /**
     * MÃ©todo que devuelve una cadena con la fecha y hora actuales, en el formato
     * necesario para grabar en un archivo GPX, esto es, "yyyy-mm-ddTHH:mm:ss.S"
     * @return String Fecha/hora actual en el formato requerido
     */
    public static String obtenerFechaHoraParaGpx () {
        String vcResul = "";
        try {
            java.util.Date vdActual = Utilidades.getCurDate();
            String vcFecha = Utilidades.format(vdActual, "yyyy-MM-dd");
            String vcHora = Utilidades.format(vdActual, "HH:mm:ss.S");
            vcResul = vcFecha + "T" + vcHora;
        } catch (Exception e) {
            e.printStackTrace();
            vcResul = "";
        }
        return vcResul;
    }
    /**
     * MÃ©todo que devuelve una cadena con una hora, en el formato que usa NMEA para devolver ese dato,
     * hhmmss (cadena de texto que no separa mediante ":"), a partir de una hora en milisegundos desde
     * el 1 de enero de 1970
     * @return String Hora en el formato requerido
     */
    public static String obtenerHoraNMEADesdeMilisecs (long pnMilis) {
        String vcResul = "";
        try {
            SimpleDateFormat df2 = new SimpleDateFormat ("HHmmss");
            df2.setLenient(false);
            Date vdFecha = new Date(pnMilis);
            vcResul = df2.format(vdFecha);
        } catch (Exception e) {
            e.printStackTrace();
            vcResul = "";
        }
        return vcResul;
    }
    /**
     * Dados dos valores de tiempos, en formato HH:MM:SS, este mÃ©todo se encarga de
     * restarlos para obtener un valor de tiempo total entre la hora de salida y la
     * hora de llegada. El resultado se pasa tambiÃ©n al formato HH:MM:SS y
     * se devuelve.
     * @param pcLlegada java.lang.String. Hora de llegada, en formato HH:MM:SS
     * @param pcSalida java.lang.String. Hora de salida, en formato HH:MM:SS
     * @return String. Tiempo resultante de restar la llegada y la salida, en formato HH:MM:SS
     */
    public static String restarTiempos(String pcLlegada, String pcSalida) {
        String vcResul = "";

        SimpleDateFormat df1 = new SimpleDateFormat ("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat df2 = new SimpleDateFormat ("HH:mm:ss");
        df1.setLenient(false);
        df2.setLenient(false);
        try {
            Date vdFecha2 = df1.parse ("01/01/2005 " + pcLlegada);
            Date vdFecha1 = df1.parse ("01/01/2005 " + pcSalida);
            Date vdFecha = new Date(vdFecha2.getTime()-vdFecha1.getTime()-3600000);
            vcResul = df2.format(vdFecha);
        } catch (Exception e){
            e.printStackTrace();
            vcResul = "";
        }

        return vcResul;
    }

    /**
     * Dado un nombre completo de archivo (path + nombre), este mÃ©todo devuelve un
     * booleano indicando si el archivo existe o no.
     * @param pcFichero String. Path + nombre de archivo del que se quiere conocer su existencia.
     * @return boolean. Existencia o no del archivo.
     */
    public static boolean existeFichero (String pcFichero) {
        boolean vbResul = false;
        try {
            File voFic = new File(pcFichero);
            if(voFic.exists() && voFic.isFile())
                vbResul = true;
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return vbResul;
    }

    /**
     * Dado un nombre completo de directorio, este mÃ©todo devuelve un booleano
     * indicando si el directorio existe o no.
     * @param pcDir String. Path de directorio del que se quiere conocer su existencia.
     * @return boolean. Existencia o no del directorio.
     */
    public static boolean existeDirectorio (String pcDir) {
        boolean vbResul = false;
        try {
            File voFic = new File(pcDir);
            if (voFic.exists() && voFic.isDirectory())
                vbResul = true;
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return vbResul;
    }

    /**
     * MÃ©todo que crea un directorio dado un path completo. Valores vÃ¡lidos del path
     * son: /dir1 o bien /dir1/dir2/dir3
     * <BR>
     * Primero comprueba si el directorio ya existe, antes de intentar crearlo
     * @param pcPath String Path completo que indica el directorio que se quiere crear
     * @return boolean Resultado de la operaciÃ³n
     */
    public static boolean crearDirectorio (String pcPath) {
        boolean vbResul = true;
        try {
            File voFic = new File(pcPath);
            if (!(voFic.exists() && voFic.isDirectory())) {
                vbResul = (new File(pcPath)).mkdirs();
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            vbResul = false;
        }
        return vbResul;
    }

    /**
     * Lee el contenido de un fichero y lo devuelve en forma de array de bytes.
     * @param poFile File. Fichero de entrada.
     * @return byte[]. Ristra de bytes con el contenido del fichero.
     * @throws IOException
     */
    public static byte[] getBytesFromFile(File poFile) throws IOException {
        InputStream voIs = new FileInputStream(poFile);
        long vnLength = poFile.length();
        byte[] vaBytes = new byte[(int)vnLength];
        int offset = 0;
        int numRead = 0;
        while (offset < vaBytes.length && (numRead=voIs.read(vaBytes, offset, vaBytes.length-offset)) >= 0) {
            offset += numRead;
        }
        if (offset < vaBytes.length) {
            throw new IOException("Could not completely read file "+poFile.getName());
        }
        voIs.close();
        return vaBytes;
    }
    /**
     * Lee el contenido de un InputStream y lo devuelve en forma de array de bytes.
     * @param poIs InputStream. Fichero de entrada.
     * @return byte[]. Ristra de bytes con el contenido del fichero.
     * @throws IOException
     */
    public static byte[] getBytesFromInputStream(InputStream poIs) throws IOException {
        long vnLength = poIs.available();
        byte[] vaBytes = new byte[(int)vnLength];
        int offset = 0;
        int numRead = 0;
        while (offset < vaBytes.length && (numRead=poIs.read(vaBytes, offset, vaBytes.length-offset)) >= 0) {
            offset += numRead;
        }
        if (offset < vaBytes.length) {
            throw new IOException("Could not completely read file");
        }
        poIs.close();
        return vaBytes;
    }
    /**
     * Este mÃ©todo crea un fichero a partir de realizar una copia sobre un fichero de origen.
     * @param pcFicheroOrig String. Path completo y nombre del fichero que se quiere copiar.
     * @param pcFicheroDest String. Path completo y nombre del nuevo fichero.
     * @return boolean. Indica si la operaciÃ³n ha tenido Ã©xito.
     */
    public static boolean copiarFichero (String pcFicheroOrig, String pcFicheroDest) {
        boolean vbResul = true;
        try {
            File voFile = new File(pcFicheroOrig);
            File voFile2 = new File(pcFicheroDest);
            byte[] vaBytes = Utilidades.getBytesFromFile(voFile);
            FileOutputStream voOs = new FileOutputStream(voFile2);
            voOs.write(vaBytes);
            voOs.flush();
            voOs.close();
        } catch (Exception e) {
            vbResul = false;
        }
        return vbResul;
    }
    /**
     * Este mÃ©todo crea un fichero a partir de realizar una copia sobre un fichero de origen.
     * @param poFicheroOrig InputStream. Objeto que representa al fichero de origen.
     * @param pcFicheroDest String. Path completo y nombre del nuevo fichero.
     * @return boolean. Indica si la operaciÃ³n ha tenido Ã©xito.
     */
    public static boolean copiarFichero (InputStream poFicheroOrig, String pcFicheroDest) {
        boolean vbResul = true;
        try {
            File voFile2 = new File(pcFicheroDest);
            byte[] vaBytes = Utilidades.getBytesFromInputStream(poFicheroOrig);
            FileOutputStream voOs = new FileOutputStream(voFile2);
            voOs.write(vaBytes);
            voOs.flush();
            voOs.close();
        } catch (Exception e) {
            vbResul = false;
        }
        return vbResul;
    }

    /**
     * Dadas dos coordenadas en forma de grados de latitud y longitud,
     * convierte el valor a una coordenada UTM,
     * asumiendo que el datum en el que se representan las coordenadas es el WGS84.
     * @param pcCX String. Valor de la longitud, en formato "gg:mm:ss".
     * @param pcCY String. Valor de la latitud, en formato "gg:mm:ss".
     * @return double[]. Conjunto de tres valores double: Zona UTM; Easting; Northing.
     */
    public static double[] convertirLatLongToUTM (String pcCX, String pcCY) {
        double vaResul[] = new double[3];
        try {
            //La conversiÃ³n se realiza en otro mÃ©todo que permite forzar una
            //conversiÃ³n a una zona UTM en concreto.
            vaResul = convertirLatLongToUTM (pcCX, pcCY, false, 0);
        } catch (Exception e) {
            vaResul[0] = 29;
            vaResul[1] = 0;
            vaResul[2] = 0;
        }
        return vaResul;
    }
    /**
     * Dadas dos coordenadas en forma de grados de latitud y longitud, convierte el
     * valor a una coordenada UTM,
     * asumiendo que el datum en el que se representan las coordenadas es el WGS84.
     * Este mÃ©todo puede forzar la conversiÃ³n para que las coordenadas UTM se expresen
     * relativas a una zona en concreto, en lugar de obtener el valor de Zona,
     * Easting y Northing que le corresponde.
     * Esto vale para cuando tenemos un conjunto de valores que se mueven entre dos
     * zonas adyacentes y queremos representarlas a travÃ©s de un mismo sistema de coordenadas.
     * @param pcCX String. Valor de la longitud, en formato "gg:mm:ss".
     * @param pcCY String. Valor de la latitud, en formato "gg:mm:ss".
     * @param pbForzarZona boolean. Dice si hay que forzar la conversiÃ³n para expresar la coordenada respecto a una zona en concreto.
     * @param pnZona int. NÃºmero de la zona a la que se quiere forzar la conversiÃ³n.
     * @return double[]. Conjunto de tres valores double: Zona UTM; Easting; Northing.
     */
    public static double[] convertirLatLongToUTM (String pcCX, String pcCY, boolean pbForzarZona, int pnZona) {
        double vaResul[] = new double[3];
        try {
            //East Longitudes are positive, West longitudes are negative.
            //North latitudes are positive, South latitudes are negative
            //Lat and Long are in decimal degrees
            double vnLongOrigin = 0;
            double vnEccPrimeSquared = 0;
            double vnN = 0;
            double vnT = 0;
            double vnC = 0;
            double vnM = 0;
            double vnA = 0;
            double vnLatRad = 0;
            double vnLongRad = 0;
            double vnLongOriginRad = 0;
            double vnLongtemp = 0;
            double vnK0 = 0;
            double vnEradius = 0;
            double vnEccSquared = 0;
            double vnECCcube = 0;
            double vnLong = 0;
            double vnLat = 0;
            int vnZona = 0;
            double vnX = 0;
            double vnY = 0;

            vnLong = grados(pcCX);
            vnLat = grados(pcCY);
            vnK0 = 0.9996;
            vnEradius = 6378137;  //WGS84 EquatorialRadius
            vnEccSquared = 0.00669438; //WGS84 eccentricitySquared
            vnECCcube = pow(vnEccSquared, 3);
            vnLongtemp = (vnLong + 180) - (int)((vnLong + 180) / 360.0) * 360 - 180; //-180.00 .. 179.9
            vnLatRad = gradosARadianes(vnLat);
            vnLongRad = gradosARadianes(vnLongtemp);
            vnZona = (int)((vnLongtemp + 180) / 6.0) + 1;
            if ((vnLat >= 56) && (vnLat < 64) && (vnLongtemp >= 3) && (vnLongtemp < 12))
                vnZona = 32;
            //Si hay que forzar la conversiÃ³n a una zona en concreto, se establece dicha zona
            if (pbForzarZona) {
                vnZona = pnZona;
            }
            vnLongOrigin = (vnZona - 1) * 6 - 180 + 3; //+3 puts origin in middle of zone
            vnLongOriginRad = gradosARadianes(vnLongOrigin);
            vnEccPrimeSquared = (vnEccSquared) / (1.0 - vnEccSquared);
            vnN = vnEradius / Math.sqrt(1 - vnEccSquared * pow(Math.sin(vnLatRad), 2));
            vnT = pow(Math.tan(vnLatRad), 2);
            vnC = vnEccPrimeSquared * pow(Math.cos(vnLatRad), 2);
            vnA = Math.cos(vnLatRad) * (vnLongRad - vnLongOriginRad);
            vnM = (1 - vnEccSquared / 4.0 - 3 * pow(vnEccSquared, 2) / 64.0 - 5 * vnECCcube / 256.0) * vnLatRad;
            vnM = vnM - (3 * vnEccSquared / 8.0 + 3 * pow(vnEccSquared, 2) / 32.0 + 45 * vnECCcube / 1024.0) * Math.sin(2 * vnLatRad);
            vnM = vnM + (15 * pow(vnEccSquared, 2) / 256.0 + 45 * vnECCcube / 1024.0) * Math.sin(4 * vnLatRad);
            vnM = vnM - (35 * pow(vnEccSquared, 3) / 3072.0) * Math.sin(6 * vnLatRad);
            vnM = vnEradius * vnM;
            vnX = vnA + (1 - vnT + vnC) * pow(vnA, 3) / 6.0;
            vnX = vnX + (5 - 18 * vnT + pow(vnT, 2) + 72 * vnC - 58 * vnEccPrimeSquared) * pow(vnA, 5) / 120.0;
            vnX = vnK0 * vnN * vnX + 500000;
            vnY = vnM + vnN * Math.tan(vnLatRad) * (pow(vnA, 2) / 2.0 + (5 - vnT + 9 * vnC + 4 * pow(vnC, 2)) * pow(vnA, 4) / 24.0);
            vnY = vnY + (61 - 58 * vnT + pow(vnT, 2) + 600 * vnC - 330 * vnEccPrimeSquared) * pow(vnA, 6) / 720.0;
            vnY = vnK0 * vnY;
            if (vnLat < 0)
                vnY = vnY + 10000000; //10000000 meter offset for southern hemisphere'
            //Introduce los resultados en el array de salida
            vaResul[0] = vnZona;
            vaResul[1] = vnX;
            vaResul[2] = vnY;
        } catch (Exception e) {
            vaResul[0] = 29;
            vaResul[1] = 0;
            vaResul[2] = 0;
        }
        return vaResul;
    }
    /**
     * Dadas dos coordenadas en forma de proyecciÃ³n UTM, convierte el valor a una coordenada
     * grados de latitud y longitud, asumiendo que el datum es el WGS84.
     * @param pcCX String. Valor del Easting, en formato "XXXXXX.XXX".
     * @param pcCY String. Valor del Northing, en formato "YYYYYYY.YYY".
     * @return String[]. Conjunto de dos valores de clase String: longitud, latitud; ambas en formato "gg:mm:ss".
     */
    public static String[] convertirUTMToLatLong (String pcCX, String pcCY, int pnZona) {
        String vaResul[] = new String[2];
        try {
            double b = 6356752.314;
            double a = 6378137;
            double e = 0.081819191;
            double e1sq = 0.006739497;
            double k0 = 0.9996;
            double latitude = 0.0;
            double longitude = 0.0;
            double vnNorthing = Double.parseDouble(pcCY);
            double vnEasting = Double.parseDouble(pcCX);
            //Si estÃ¡ en el hemisferio sur, le resta el valor artificial que sirve para distinguirlo
            if (vnNorthing > 10000000)
                vnNorthing = 10000000 - vnNorthing;
            //Realiza los cÃ¡lculos trigonomÃ©tricos correspondientes
            double arc = vnNorthing / k0;
            double mu = arc / (a * (1 - pow(e, 2) / 4.0 - 3 * pow(e, 4) / 64.0 - 5 * pow(e, 6) / 256.0));
            double ei = (1 - pow((1 - e * e), (1 / 2.0))) / (1 + pow((1 - e * e), (1 / 2.0)));
            double ca = 3 * ei / 2 - 27 * pow(ei, 3) / 32.0;
            double cb = 21 * pow(ei, 2) / 16 - 55 * pow(ei, 4) / 32;
            double cc = 151 * pow(ei, 3) / 96;
            double cd = 1097 * pow(ei, 4) / 512;
            double phi1 = mu + ca * Math.sin(2 * mu) + cb * Math.sin(4 * mu) + cc * Math.sin(6 * mu) + cd
                    * Math.sin(8 * mu);
            double n0 = a / pow((1 - pow((e * Math.sin(phi1)), 2)), (1 / 2.0));
            double r0 = a * (1 - e * e) / pow((1 - pow((e * Math.sin(phi1)), 2)), (3 / 2.0));
            double fact1 = n0 * Math.tan(phi1) / r0;
            double _a1 = 500000 - vnEasting;
            double dd0 = _a1 / (n0 * k0);
            double fact2 = dd0 * dd0 / 2;
            double t0 = pow(Math.tan(phi1), 2);
            double Q0 = e1sq * pow(Math.cos(phi1), 2);
            double fact3 = (5 + 3 * t0 + 10 * Q0 - 4 * Q0 * Q0 - 9 * e1sq) * pow(dd0, 4) / 24;
            double fact4 = (61 + 90 * t0 + 298 * Q0 + 45 * t0 * t0 - 252 * e1sq - 3 * Q0
                    * Q0)
                    * pow(dd0, 6) / 720;
            //
            double lof1 = _a1 / (n0 * k0);
            double lof2 = (1 + 2 * t0 + Q0) * pow(dd0, 3) / 6.0;
            double lof3 = (5 - 2 * Q0 + 28 * t0 - 3 * pow(Q0, 2) + 8 * e1sq + 24 * pow(t0, 2))
                    * pow(dd0, 5) / 120;
            double _a2 = (lof1 - lof2 + lof3) / Math.cos(phi1);
            double _a3 = _a2 * 180 / Math.PI;
            //CÃ¡lculos finales
            latitude = 180 * (phi1 - fact1 * (fact2 + fact3 + fact4)) / Math.PI;
            double zoneCM = 3.0;
            if (pnZona > 0)
                zoneCM = 6 * pnZona - 183.0;
            longitude = zoneCM - _a3;
            if (vnNorthing > 10000000)
                latitude = -latitude;
            //Introduce los resultados en el vector de salida
            vaResul[0]=(grados(longitude));
            vaResul[1]=(grados(latitude));
        } catch (Exception e) {
            vaResul[0]="";
            vaResul[1]="";
        }
        return vaResul;
    }
    /**
     * Eleva un nÃºmero a una potencia.
     * @param pnValor double. NÃºmero.
     * @param pnVeces int. Potencia.
     * @return double. Resultado.
     */
    public static double pow (double pnValor, int pnVeces) {
        double vnResul = 1;
        for (int i=1; i<=pnVeces; i++)
            vnResul = vnResul * pnValor;
        return vnResul;
    }
    /**
     * Eleva un nÃºmero a una potencia.
     * @param pnValor double. NÃºmero.
     * @param pnVeces int. Potencia.
     * @return double. Resultado.
     */
    private static double pow(double pnValor, double pnVeces)
    {
        return Math.pow(pnValor, pnVeces);
    }
    /**
     * Pasa un valor de Ã¡ngulo en grados a otro en radianes.
     * @param pnValor double. Valor de Ã¡ngulo en grados.
     * @return double. Mismo valor, pero en radianes.
     */
    public static double gradosARadianes(double pnValor) {
        double vnResul = (3.141592653 * pnValor) / 180.0;
        return vnResul;
    }
    /**
     * Convierte una cadena de caracteres en formato gg:mm:ss.ss a un valor de grados
     * en formato numÃ©rico
     * @param pcCoord String. Grados expresados en gg:mm:ss.ss
     * @return double. Grados expresados en valor numÃ©rico.
     */
    public static double grados(String pcCoord) {
        double vnResul = 0;
        int vnGrados;
        int vnMinutos;
        double vnSegundos;
        double vnValor;
        int vnSigno;

        try {
            if (pcCoord.charAt(0)== '-')
                vnSigno = -1;
            else
                vnSigno = 1;
            int vnPos = pcCoord.indexOf(":");
            if (vnSigno == 1)
                vnGrados = Integer.parseInt(pcCoord.substring(0, vnPos));
            else
                vnGrados = Integer.parseInt(pcCoord.substring(1, vnPos));
            int vnPos2 = pcCoord.indexOf(":", vnPos+1);
            vnMinutos = Integer.parseInt(pcCoord.substring(vnPos+1, vnPos2));
            vnSegundos = Double.parseDouble(pcCoord.substring(vnPos2+1));
            vnResul = vnGrados + (vnMinutos / 60.0) + (vnSegundos / 3600.0);
            vnResul = vnResul * vnSigno;
        } catch (Exception e) {
        }
        return vnResul;
    }
    /**
     * Convierte un dato numÃ©rico de grados a una cadena de caracteres en formato gg:mm:ss.ss
     * @param pnCoord double. Grados expresados en valor numÃ©rico.
     * @return String. Grados expresados en gg:mm:ss.ss
     */
    public static String grados(double pnCoord) {
        String vcResul = "";
        String vcSigno = "";
        double vnValor = 0;
        double vnResto = 0;
        int vnGrados = 0;
        int vnMinutos = 0;
        double vnSegundos = 0;

        try {
            //Establece el signo de la coordenada
            if (pnCoord < 0) {
                vcSigno = "-";
                pnCoord = pnCoord * -1.0;
            }
            //Establece el valor de grados
            vnGrados = (int) pnCoord;
            vnResto = pnCoord - vnGrados;
            //Establece el valor de minutos
            vnValor = vnResto * 60.0;
            vnMinutos = (int) vnValor;
            vnResto = vnValor - vnMinutos;
            //Establece el valor de segundos
            vnSegundos = vnResto * 60.0;
            //Compone el resultado
            vcResul = vcSigno + vnGrados + ":" + vnMinutos + ":" + vnSegundos;
            vcResul.replace(",", ".");
        } catch (Exception e) {
            vcResul = "00:00:00";
        }
        return vcResul;
    }

    /**
     * mÃ©todo que comprueba y extrae los datos de georreferenciaciÃ³n de un archivo de imagen
     * JPG que tiene un fichero de mundo asociado a Ã©l. Para ello, se comprueba la existencia
     * del fichero, la existencia de un fichero de mundo y por Ãºltimo se lee el archivo de mundo
     * para extraer los datos.<BR>
     * El resultado se devuelve en forma de array de datos de tipo String. Son, por orden:
     * factor X de resoluciÃ³n, ---, ---, factor Y de resoluciÃ³n, coordenada X de la
     * esquina superior-izquierda de la imagen y coordenada Y de esa misma esquina.
     * @param pcFichero String. Nombre completo del archivo JPG que se quiere comprobar.
     * @return String[]. Conjunto de 6 cadenas con los datos anteriormente descritos.
     */
    public static String[] obtenerGeorreferencia (String pcFichero) {
        String vaResul[] = new String[6];
        int vnConta = 0;
        try {
            //Primero, comprueba que el fichero existe
            if (Utilidades.existeFichero(pcFichero)) {
                //Ahora, comprueba que existe fichero de mundo.
                //El nombre de fichero de mundo se compone de forma directa
                String pcFicMundo = pcFichero.substring(0, pcFichero.length()-2) +
                        pcFichero.charAt(pcFichero.length()-1) + "w";
                if (Utilidades.existeFichero(pcFichero)) {
                    //Se tiene que abrir el fichero de mundo y extraer los datos de georreferenciaciÃ¯Â¿Â½n
                    FileInputStream voFic = new FileInputStream(pcFicMundo);
                    DataInputStream voIn = new DataInputStream(voFic);
                    BufferedReader voBuffer = new BufferedReader(new InputStreamReader(voIn));
                    String vcLinea = "";
                    //Se lee el fichero lÃ­nea a lÃ­nea
                    while ((vcLinea = voBuffer.readLine()) != null)   {
                        //Inserta la lÃ­nea en el vector de resultados
                        vaResul[vnConta++]=vcLinea;
                    }
                    //Por Ãºltimo, cierra el fichero
                    voIn.close();
                }
            }
        } catch (Exception e) {
            vaResul = null;
            e.printStackTrace();
        }
        return vaResul;
    }
    /**
     * Dado el nombre de un fichero de origen (supuestamente una imagen georreferenciada),
     * este mÃ©todo escribe un fichero de mundo asociado a Ã©l.<BR>
     * Los datos de identificaciÃ³n de la georreferenciaciÃ³n son pasados al mÃ©todo, por orden:
     * factor X de resoluciÃ³n, rotaciÃ³n Y, rotaciÃ³n X, factor Y de resoluciÃ³n, coordenada X de la
     * esquina superior-izquierda de la imagen y coordenada Y de esa misma esquina.<BR>
     * El fichero de mundo tendrÃ¡ el mismo nombre pero con extensiÃ³n adecuada a los ficheros
     * de mundo segÃºn el tipo de imagen que sea. Se guardarÃ¡ en la misma carpeta donde
     * se encuentra el fichero origen.
     * @param pcFichero String. Nombre completo del archivo origen para el que se quiere crear un fichero de mundo.
     * @param pcFactorX String. Factor de resoluciÃ³n X de los pixels de la imagen.
     * @param pcRotY String. RotaciÃ³n Y de la imagen.
     * @param pcRotX String. RotaciÃ³n X de la imagen.
     * @param pcFactorY String. Factor de resoluciÃ³n Y de los pixels de la imagen.
     * @param pcCoordX String. Coordenada X de la esquina superior-izquierda.
     * @param pcCoordY String. Coordenada Y de la esquina superior-izquierda.
     * @return boolean. Indica si el proceso salÃ­o bien o mal.
     */
    public static boolean escribirGeorreferencia (String pcFichero, String pcFactorX,
                                                  String pcRotY, String pcRotX, String pcFactorY, String pcCoordX, String pcCoordY) {
        boolean vbResul = true;
        try {
            //Primero, comprueba que el fichero existe
            if (Utilidades.existeFichero(pcFichero)) {
                //Ahora, comprueba que no existe fichero de mundo, para poder crearlo.
                //El nombre de fichero de mundo se compone de forma directa
                String pcFicMundo = pcFichero.substring(0, pcFichero.length()-2) +
                        pcFichero.charAt(pcFichero.length()-1) + "w";
                //Se tiene que crear el fichero de mundo y grabar los datos de georreferenciaciÃ³n
                FileOutputStream voFic = new FileOutputStream(pcFicMundo);
                DataOutputStream voOut = new DataOutputStream(voFic);
                BufferedWriter voBuffer = new BufferedWriter(new OutputStreamWriter(voOut));
                voBuffer.write(pcFactorX);
                voBuffer.newLine();
                voBuffer.write(pcRotY);
                voBuffer.newLine();
                voBuffer.write(pcRotX);
                voBuffer.newLine();
                voBuffer.write(pcFactorY);
                voBuffer.newLine();
                voBuffer.write(pcCoordX);
                voBuffer.newLine();
                voBuffer.write(pcCoordY);
                voBuffer.newLine();
                //Por Ãºltimo, cierra el fichero
                voBuffer.flush();
                voOut.close();
            }
        } catch (Exception e) {
            vbResul = false;
            e.printStackTrace();
        }
        return vbResul;
    }

    /**
     * Dada un dato de coordenada en UTM (sea X o Y) un tamaÃ±o en pixels y un factor
     * de resoluciÃ³n (extraÃ­do de un fichero de mundo) este mÃ©todo calcula la
     * coordenada final, tambiÃ©n en UTM.<BR>
     * La fÃ³rmula de cÃ¡lculo es: C1 = C0 + (Pixels * Factor)
     * Un factor de resoluciÃ³n de 1 pixel por metro implica que la coordenada final se calcula
     * simplemente con sumar los pixels a la coordenada inicial.
     * @param pcCoord String. Valor de la coordenada en UTM
     * @param pcFactor String. Valor del factor de resoluciÃ³n (pixels por metro)
     * @param pnPixels int. Valor del tamaÃ±o de la imagen en pixels.
     * @return String. Valor de la coordenada final, convertida de nuevo en cadena de caracteres.
     */
    public static String calcularCoordenadaFinalDesdeInicialMasFactor (String pcCoord,
                                                                       String pcFactor, int pnPixels) {
        String vcResul = "";
        try {
            double pnC0 = Double.parseDouble(pcCoord);
            double pnFactor = Double.parseDouble(pcFactor);
            double pnC1 = pnC0 + (pnPixels * pnFactor);
            vcResul = pnC1 + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcResul;
    }
    /**
     * Dados unos valores de tipo cadena que contienen una coordenada en formato Lat-Lon,
     * este mÃ©todo devuelve la coordenada de pantalla que le corresponde
     * @param pcCX String Coordenada X (Longitud)
     * @param pcCY String Coordenada Y (Latitud)
     * @param pcOrigenX String Coordenada X, en UTM, del origen de coordenadas de la imagen de pantalla
     * @param pcOrigenY String Coordenada Y, en UTM, del origen de coordenadas de la imagen de pantalla
     * @param pcFactorX String Factor de resoluciÃ³n horizontal de la imagen (metros que hay en cada pixel)
     * @param pcFactorY String Factor de resoluciÃ³n vertical de la imagen (metros que hay en cada pixel)
     * @param pnEscala double Escala de zoom que estÃ¡ siendo aplicada a la imagen
     * @return Point Coordenadas X e Y del punto en la pantalla
     */
    public static Point calcularCoordenadaDePantallaDesdeLatLon (String pcCX, String pcCY,
                                                                 String pcOrigenX, String pcOrigenY, String pcFactorX, String pcFactorY, double pnEscala) {
        Point voResul = new Point(0,0);
        try {
            //Convierte coordenadas Lat-Lon a UTM
            double[] vaResul = Utilidades.convertirLatLongToUTM(pcCX, pcCY);
            //vnZona = (int)vaResul[0];
            double nX = vaResul[1];
            double nY = vaResul[2];
            //Calcula la coordenada central en funciÃ³n de los valores almacenados
            double vnOrigX = Double.parseDouble(pcOrigenX);
            double vnOrigY = Double.parseDouble(pcOrigenY);
            //Calcula la coordenada del punto segÃºn la escala y el factor de resoluciÃ³n aplicados
            double vnFactorX = Double.parseDouble(pcFactorX);
            double vnFactorY = Double.parseDouble(pcFactorY);
            //Calcula el desplazamiento del punto respecto del origen, en funciÃ³n de cÃ³mo se incrementan las coordenadas
            if (vnFactorX >= 0)
                voResul.x = (int)(nX - vnOrigX);
            else
                voResul.x = (int)(vnOrigX - nX);
            if (vnFactorY >= 0)
                voResul.y = (int)(nY - vnOrigY);
            else
                voResul.y = (int)(vnOrigY - nY);
            //Calcula la coordenada del punto, segÃºn la escala y el factor de resoluciÃ³n
            voResul.x = (int)((voResul.x * pnEscala) / Math.abs(vnFactorX));
            voResul.y = (int)((voResul.y * pnEscala) / Math.abs(vnFactorY));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return voResul;
    }
    /**
     * Dada un dato de coordenada en UTM (sea X o Y) un tamaÃ±o en pixels y un factor
     * de resoluciÃ³n (extraÃ­do de un fichero de mundo) este mÃ©todo calcula la
     * coordenada final, tambiÃ©n en UTM.<BR>
     * La fÃ³rmula de cÃ¡lculo es: C1 = C0 + (Pixels * Factor)<BR>
     * Un factor de resoluciÃ³n de 1 pixel por metro implica que la coordenada final
     * se calcula simplemente con sumar los pixels a la coordenada inicial.
     * @param pcCoord String. Valor de la coordenada en UTM
     * @param pcCoord2 String. Valor de la coordenada final en UTM.
     * @param pnPixels int. Valor del tamaÃ±o de la imagen en pixels.
     * @return String. Valor del factor de resoluciÃ³n (pixels por metro)
     */
    public static String calcularFactorDesdeCoordenadaInicialMasFinal (String pcCoord,
                                                                       String pcCoord2, int pnPixels) {
        String vcResul = "";
        try {
            double pnC0 = Double.parseDouble(pcCoord);
            double pnC1 = Double.parseDouble(pcCoord2);
            double pnFactor = (pnC1 - pnC0) / pnPixels;
            vcResul = pnFactor + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcResul;
    }

    /**
     * MÃ©todo que se encarga de obtener el tamaÃ±o en pixels de un archivo que contiene
     * una imagen en algÃºn formato soportado.
     * @param pcFichero String. Nombre completo del fichero.
     * @return android.graphics.Point. Dimensiones en pixels de la imagen.
     */
    public static Point obtenerTamanoImagen (String pcFichero) {
        Point voResul = new Point(0,0);
        try {
            String vcExtension = Utilidades.obtenerSufijoFichero(pcFichero);
            if (vcExtension.equalsIgnoreCase("jpg") || vcExtension.equalsIgnoreCase("jpeg")) {
                voResul = Utilidades.obtenerTamanoJPG(pcFichero);
            } else if (vcExtension.equalsIgnoreCase("tif") || vcExtension.equalsIgnoreCase("tiff")) {
                voResul = Utilidades.obtenerTamanoTIF(pcFichero);
            } else {
                voResul = Utilidades.obtenerTamanoGenerico(pcFichero);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return voResul;
    }
    /**
     * MÃ©todo que se encarga de obtener el tamaÃ±o en pixels de un archivo que contiene
     * una imagen en formato JPG.
     * @param pcFichero String. Nombre completo del fichero.
     * @return android.graphics.Point. Dimensiones en pixels de la imagen.
     */
    public static Point obtenerTamanoJPG (String pcFichero) {
        Point voResul = new Point(0,0);
        try {
            //Primero, comprueba que el fichero existe
            if (Utilidades.existeFichero(pcFichero)) {
                FileInputStream voFis = new FileInputStream(new File(pcFichero));
                // Busca la marca SOI
                if (voFis.read() != 255 || voFis.read() != 216)
                    throw new RuntimeException("SOI (Start Of Image) marker 0xff 0xd8 missing");
                while (voFis.read() == 255) {
                    int vnMarker = voFis.read();
                    int vnLen = voFis.read() << 8 | voFis.read();
                    if (vnMarker == 192) {
                        voFis.skip(1);
                        int vnHeight = voFis.read() << 8 | voFis.read();
                        int vnWidth = voFis.read() << 8 | voFis.read();
                        voResul = new Point(vnWidth, vnHeight);
                        break;
                    }
                    voFis.skip(vnLen - 2);
                }
                voFis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return voResul;
    }
    /**
     * MÃ©todo que se encarga de obtener el tamaÃ±o en pixels de un archivo que contiene
     * una imagen en formato TIF.
     * @param pcFichero String. Nombre completo del fichero.
     * @return android.graphics.Point. Dimensiones en pixels de la imagen.
     */
    public static Point obtenerTamanoTIF (String pcFichero) {
        Point voResul = new Point(0,0);
        try {
            //Primero, comprueba que el fichero existe
            if (Utilidades.existeFichero(pcFichero)) {
                Bitmap voImg = BitmapFactory.decodeFile(pcFichero);
                voResul = new Point(voImg.getWidth(), voImg.getHeight());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return voResul;
    }
    /**
     * mÃ©todo genÃ©rico para obtener el tamaÃ±o en pixels (ancho y alto) de un fichero
     * que contiene una imagen.
     * @param pcFichero String. Nombre completo del archivo.
     * @return android.graphics.Point tamaÃ±o en pixels de la imagen.
     */
    public static Point obtenerTamanoGenerico(String pcFichero) {
        Point voResul = new Point(0,0);
        try {
            //Primero, comprueba que el fichero existe
            if (Utilidades.existeFichero(pcFichero)) {
                Bitmap voImg = BitmapFactory.decodeFile(pcFichero);
                voResul = new Point(voImg.getWidth(), voImg.getHeight());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return voResul;
    }
    /**
     * Devuelve la extensiÃ³n de un archivo que se pasa con su nombre completo.
     * @param pcFichero String. Nombre completo de un archivo
     * @return String ExtensiÃ¯Â¿Â½n del archivo
     */
    public static String obtenerSufijoFichero(String pcFichero) {
        String vcResul = null;
        try {
            if (pcFichero != null) {
                vcResul = "";
                if (pcFichero.lastIndexOf('.') != -1) {
                    vcResul = pcFichero.substring(pcFichero.lastIndexOf('.'));
                    if (vcResul.startsWith(".")) {
                        vcResul = vcResul.substring(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcResul;
    }
/**
 * Crea un fichero JPG vacÃ­o de un tamaÃ±o concreto en pixels
 * @param pcFichero String Nombre completo del fichero que se quiere crear
 * @param poTam android.graphics.Point tamaÃ±o en pixels de la imagen que se quiere crear
 * @return boolean Devuelve si el proceso ha sido correcto o no
 */
/**
 public static boolean crearArchivoJPG (String pcFichero, Point poTam) {
 boolean vbResul = false;
 try {
 //Crea un BufferedImage donde dibujar
 BufferedImage voBI = new BufferedImage((int)poTam.getWidth(), (int)poTam.getHeight(), BufferedImage.TYPE_INT_ARGB);
 //Crea un contenedor de grÃ¡ficos asociado a la imagen
 java.awt.Graphics2D voG2d = voBI.createGraphics();
 // Make all filled pixels transparent
 //Dibuja un recuadro, para poder usar esa imagen fÃ¡cilmente desde algÃºn programa externo
 voG2d.setColor(java.awt.Color.blue);
 voG2d.drawRect(1, 1, (int)poTam.getWidth()-2, (int)poTam.getHeight()-2);
 //Descarga el contexto grÃ¡fico
 voG2d.dispose();
 //Ahora llama al mÃ©todo genÃ©rico para crear un fichero a partir de un nombre y de una imagen
 vbResul = crearArchivoJPG (pcFichero, voBI);
 } catch (Exception e) {
 e.printStackTrace();
 }
 return vbResul;
 }
 */
/**
 * MÃ©todo que graba un archivo JPG a partir del contenido de un objeto BufferedImage.
 * @param pcFichero String. Nombre completo del fichero que se va a grabar.
 * @param poBI BufferedImage. Contenido de la imagen.
 * @return boolean Ã‰xito de la grabaciÃ³n.
 */
/**
 public static boolean crearArchivoJPG (String pcFichero, BufferedImage poBI) {
 boolean vbResul = false;
 try {
 //Graba el fichero como JPG
 File voFic = new File(pcFichero);
 vbResul = ImageIO.write(poBI, "jpg", voFic);
 } catch (Exception e) {
 e.printStackTrace();
 }
 return vbResul;
 }
 */
    /**
     * Crea un fichero PNG vacÃ­o de un tamaÃ±o concreto en pixels
     * @param pcFichero String Nombre completo del fichero que se quiere crear
     * @param poTam Point TamaÃ±o en pixels de la imagen que se quiere crear
     * @return boolean Devuelve si el proceso ha sido correcto o no
     */
    public static boolean crearArchivoBoceto (String pcFichero, Point poTam) {
        boolean vbResul = false;
        try {
            //Crea un array de pixels
            int[] vaPix = new int[poTam.x * poTam.y];
            //Crea un Bitmap donde dibujar
            Bitmap voBm = Bitmap.createBitmap(poTam.x, poTam.y, Bitmap.Config.ARGB_8888);
            //Da valores a los pixels
        /*
        for (int y = 0; y < poTam.y; y++)
            for (int x = 0; x < poTam.x; x++) {
                int index = y * poTam.x + x;
                vaPix[index] = 0xffffffff;
            }
         *
         */
            //Aprovecho para pintar una marca azul en las esquinas sup-izq e inf-der
            vaPix[0] = 0xFF0000FF;
            vaPix[1] = 0xFF0000FF;
            vaPix[2] = 0xFF0000FF;
            vaPix[poTam.x] = 0xFF0000FF;
            vaPix[(poTam.x*2)] = 0xFF0000FF;
            vaPix[((poTam.y-1) * poTam.x) + (poTam.x-1)] = 0xFF0000FF;
            vaPix[((poTam.y-1) * poTam.x) + (poTam.x-2)] = 0xFF0000FF;
            vaPix[((poTam.y-1) * poTam.x) + (poTam.x-3)] = 0xFF0000FF;
            vaPix[((poTam.y-2) * poTam.x) + (poTam.x-1)] = 0xFF0000FF;
            vaPix[((poTam.y-3) * poTam.x) + (poTam.x-1)] = 0xFF0000FF;
            //Asigno los pixels al bitmap
            voBm.setPixels(vaPix, 0, poTam.x, 0, 0, poTam.x, poTam.y);

            //Ahora llama al mÃ©todo genÃ©rico para crear un fichero a partir de un nombre y de una imagen
            vbResul = crearArchivoBoceto (pcFichero, voBm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vbResul;
    }
    /**
     * MÃ©todo que graba un archivo PNG a partir del contenido de un objeto Bitmap.
     * @param pcFichero String. Nombre completo del fichero que se va a grabar.
     * @param poBI Bitmap. Contenido de la imagen.
     * @return boolean Ã‰xito de la grabaciÃ³n.
     */
    public static boolean crearArchivoBoceto (String pcFichero, Bitmap poBI) {
        boolean vbResul = false;
        try {
            //Graba el fichero como PNG
            File voFile = new File(pcFichero);
            FileOutputStream voFos = new FileOutputStream(voFile);
            //FileOutputStream voFos = poContext.openFileOutput(pcFichero, Context.MODE_WORLD_WRITEABLE);
            vbResul = poBI.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, voFos);
            voFos.flush();
            voFos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vbResul;
    }
    /**
     * MÃ©todo que graba un archivo JPG a partir de un archivo PNG.
     * @param pcOrigen String. Nombre completo del fichero origen (un boceto => PNG)
     * @param pcDestino String. Nombre completo del fichero destino (JPG)
     * @return boolean Ã‰xito de la grabaciÃ³n.
     */
    public static boolean crearJPGDesdePNG (String pcOrigen, String pcDestino) {
        boolean vbResul = false;
        try {
            //Primero, lee el archivo de origen y lo guarda temporalmente como Bitmap
            FileInputStream voIn;
            BufferedInputStream voBuf;
            Bitmap voImg;
            voIn = new FileInputStream(pcOrigen);
            voBuf = new BufferedInputStream(voIn);
            //Esto crea un bitmap mutable, en el cual se puede escribir
            voImg = BitmapFactory.decodeStream(voBuf).copy(Bitmap.Config.ARGB_8888, true);
            if (voIn != null) {
                voIn.close();
            }
            if (voBuf != null) {
                voBuf.close();
            }
            //Ahora, cambia la transparencia por blancos, ya que JPG no soporta transparencia
            Bitmap voImg2 = Bitmap.createBitmap(voImg.getWidth(), voImg.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas voCanvas = new Canvas(voImg2);
            voCanvas.drawRGB(255, 255, 255);
            voCanvas.drawBitmap(voImg, 0, 0, null);
            //Ahora, guarda el bitmap como JPG
            FileOutputStream voFos = new FileOutputStream(pcDestino);
            vbResul = voImg2.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, voFos);
            voFos.flush();
            voFos.close();
            vbResul = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vbResul;
    }

}
