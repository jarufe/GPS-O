package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.app.Application;

import jaru.ori.logic.gpslog.*;
import jaru.ori.logic.gpslog.xml.*;
import jaru.ori.logic.localiza.ConfLocaliza;
import jaru.ori.logic.localiza.xml.ConfLocalizaXMLHandler;
import jaru.ori.utils.*;
import jaru.ori.utils.android.*;
import jaru.gps.logic.*;
import jaru.gps.logic.xml.*;
import java.util.Vector;
import jaru.ori.logic.campo.*;
import jaru.ori.logic.campo.xml.*;
import jaru.ori.utils.android.*;
import jaru.ori.web.controlcarrera.RegistroLocalizacion;
import jaru.red.logic.GestionTransmisiones;
import jaru.red.logic.HiloTransmisiones;
import jaru.sensor.logic.android.CombiOrientacion;

/**
 * Actividad principal de la aplicación GPSLogger. Se trata de una herramienta
 * que permite realizar tres tipos de tareas:
 * <P>
 * 1.- Almacenar datos vectoriales (puntos, líneas, áreas) a partir de las 
 * coordenadas proporcionadas por la lectura de un GPS en tiempo real, siendo
 * posible una caracterización de los elementos registrados con la notación de
 * objetos de OCAD (para mapas de O-Pie y de O-BM)
 * 2.- Realizar un trabajo de campo de forma gráfica (en modo raster y vectorial)
 * a partir de una imagen georreferenciada, dibujando en unos bocetos (disponiendo
 * de una capa para puntos/líneas, otra para áreas y otra para dibujo en sucio).
 * Este editor de trabajo de campo tiene una serie de herramientas que permiten hacer
 * zoom sobre la imagen, desplazarla, situar la posición del GPS en tiempo real, 
 * volcar el contenido vectorial almacenado hasta el momento y dibujar con varios 
 * lápices y colores sobre cada una de las capas de boceto.<BR>
 * 3.- Realizar una integración con otras herramientas y otros formatos. En concreto,
 * se han habilitado funciones para importar datos en formato GPX, exportar datos
 * en formato GPX y exportar datos directamente en forma de mapa OCAD
 * </P>
 * <P>
 * Cuando se instala la aplicación, la carpeta de trabajo se genera en /mnt/sdcard/JARU.
 * Allí estarán ubicados los ficheros de configuración de la aplicación. Se puede
 * utilizar la función de configuración para determinar en qué carpeta se van a guardar
 * los datos de trabajo. Normalmente será la misma carpeta.
 * </P>
 * <P>
 * La aplicación se puede comunicar con dispositivos GPS a través de Bluetooth. Cada vez
 * más dispositivos Android vienen con GPS incorporado, así que en el futuro se añadirá
 * la posibilidad de gestionar el GPS mediante las clases específicas que vienen con Android.
 * Por ahora, se ha realizado una traducción de las clases que gestionaban los GPS a través
 * de puerto serie de los PC Windows, pero a través de Bluetooth.
 * </P>
 * @author javier.arufe
 */
public class APrincipal extends Activity {
    public static final String PREFS_NAME = "GpsOPrefs";

    private static String cPathAplica = "JARU";
    private static String cPathDatos = "JARU";
    private static String cFichero = "";
    private static Vector<Registro> vRegistros = null;
    private static Parametro oParametro = null;
    private static ConfCampo oConfCampo = null;
    private static ConfLocaliza oConfLocaliza = null;
    private boolean bEntrada = true;
    private boolean bGrabaLoc = false;
    private static Resources oRes = null;
    private static Application oApp = null;
    private static TransfGeografica oTransf;
    private static String cHemisferio;
    private static String cMeridiano;
    private static int nTipo;
    private static String cTipoOCAD;
    private static String cTipoOBM;
    private static String cDesc;
    private static boolean bAceptar;
    private static GpsInterno oGpsInterno;
    private static CombiOrientacion oCombi = null;
    private static int nOpcionEntrada = 0;
    private static TickerLoc oTickerLoc = null;
    private static Vector<String> vIds = null;
    //Datos para gestionar el envío de transmisiones desde esta actividad
    private Thread oThreadTx;
    private int nRetardoTx = 5000;
    private HiloTransmisiones oTransTx = null;
    protected static final int MENSAJEPARALOCALIZACION = 0x201;

    final int ACTIVITY_CAMBIAPARAMETROS = 1;
    final int ACTIVITY_LISTADONMEA = 2;
    final int ACTIVITY_LECTURA = 3;
    final int ACTIVITY_CENTROIDE = 4;
    final int ACTIVITY_LOG = 5;
    final int ACTIVITY_GENERAROCAD = 6;
    final int ACTIVITY_IMPORTARGPX = 7;
    final int ACTIVITY_FILECHOOSER = 8;
    final int ACTIVITY_ELIMINAR = 9;
    final int ACTIVITY_CONFIGURARCAMPO = 10;
    final int ACTIVITY_EDITORCAMPO = 11;
    final int ACTIVITY_DUAL = 12;
    final int ACTIVITY_GENERARGPX = 13;
    final int ACTIVITY_BRUJULA = 14;
    final int ACTIVITY_VERMAPA = 15;
    final int ACTIVITY_CONFLOCALIZA = 16;
    final int ACTIVITY_SELIDS = 17;
    final int ACTIVITY_TESTCONN = 18;

    /**
     * Devuelve el objeto que contiene los registros grabados.
     * Se usa en un entorno estático para poder compartirlo en todas las actividades.
     * @return Vector<Registro>
     */
    public static Vector<Registro> getVRegistros () {
        return vRegistros;
    }
    /**
     * Devuelve el objeto que contiene los parámetros de configuración básicos.
     * Se usa en un entorno estático para poder compartirlo en todas las actividades.
     * @return Parametro
     */
    public static Parametro getOParametro () {
        return oParametro;
    }
    /**
     * Devuelve el path de la aplicación
     * @return String
     */
    public static String getCPathAplica() {
        return cPathAplica;
    }
    /**
     * Devuelve el objeto que contiene los parámetros de configuración del editor
     * Se usa en un entorno estático para poder compartirlo en todas las actividades.
     * @return ConfCampo
     */
    public static ConfCampo getOConfCampo () {
        return oConfCampo;
    }
    /**
     * Método que devuelve el objeto que representa a los recursos de la aplicación.
     * @return Resources
     */
    public static Resources getORes() {
        return oRes;
    }
    /**
     * Método que devuelve el objeto que representa a la aplicación.
     * @return Application
     */
    public static Application getOApp() {
        return oApp;
    }
    /**
     * Método que devuelve el objeto que contiene la transformación geográfica
     * de la coordenada
     * @return TransfGeografica
     */
    public static TransfGeografica getOTransf() {
        return oTransf;
    }
    /**
     * Devuelve la letra que corresponde al hemisferio de la coordenada (N, S)
     * @return String
     */
    public static String getCHemisferio() {
        return cHemisferio;
    }
    /**
     * Devuelve la letra que corresponde al meridiano de la coordenada (W, E)
     * @return String
     */
    public static String getCMeridiano() {
        return cMeridiano;
    }
    /**
     * Devuelve el path+nombre del fichero actualmente seleccionado
     * @return String
     */
    public static String getCFichero() {
        return cFichero;
    }
    /**
     * Devuelve el indicador de tipo de elemento OCAD
     * @return int
     */
    public static int getNTipo() {
        return nTipo;
    }
    /**
     * Devuelve el id de objeto OCAD seleccionado
     * @return String
     */
    public static String getCTipoOCAD() {
        return cTipoOCAD;
    }
    /**
     * Devuelve el id de objeto OCAD dual para OBM seleccionado
     * @return String
     */
    public static String getCTipoOBM() {
        return cTipoOBM;
    }
    /**
     * Devuelve una descripción
     * @return String
     */
    public static String getCDesc() {
        return cDesc;
    }
    /**
     * Devuelve si se ha aceptado
     * @return boolean
     */
    public static boolean getBAceptar() {
        return bAceptar;
    }

    public boolean isbGrabaLoc() {
        return bGrabaLoc;
    }

    public void setbGrabaLoc(boolean bGrabaLoc) {
        this.bGrabaLoc = bGrabaLoc;
    }

    /**
     * Devuelve el objeto de la clase GpsInterno
     * @return GpsInterno
     */

    public static GpsInterno getOGpsInterno() {
        return oGpsInterno;
    }
    /**
     * Establece el objeto que representa a la transformación geográfica de la coordenada
     * @param poTransf TransfGeografica
     */
    public static void setOTransf (TransfGeografica poTransf) {
        oTransf = poTransf;
    }
    /**
     * Establece la letra que corresponde al hemisferio de la coordenada actual
     * @param pcHemisferio String
     */
    public static void setCHemisferio (String pcHemisferio) {
        cHemisferio = pcHemisferio;
    }
    /**
     * Establece la letra que corresponde al meridiano de la coordenada actual
     * @param pcMeridiano String
     */
    public static void setCMeridiano (String pcMeridiano) {
        cMeridiano = pcMeridiano;
    }
    /**
     * Establece el path+nombre del fichero actualmente seleccionado
     * @param pcFichero String
     */
    public static void setCFichero (String pcFichero) {
        cFichero = pcFichero;
    }
    /**
     * Establece el indicador de tipo de elemento OCAD
     * @param pnTipo int
     */
    public static void setNTipo (int pnTipo) {
        nTipo = pnTipo;
    }
    /**
     * Establece el id de objeto OCAD
     * @param pcTipoOCAD String
     */
    public static void setCTipoOCAD (String pcTipoOCAD) {
        cTipoOCAD = pcTipoOCAD;
    }
    /**
     * Establece el id de objeto OCAD dual para OBM
     * @param pcTipoOBM String
     */
    public static void setCTipoOBM (String pcTipoOBM) {
        cTipoOBM = pcTipoOBM;
    }
    /**
     * Establece una descripción
     * @param pcDesc String
     */
    public static void setCDesc (String pcDesc) {
        cDesc = pcDesc;
    }
    /**
     * Establece un valor de aceptación
     * @param pbAceptar boolean
     */
    public static void setBAceptar (boolean pbAceptar) {
        bAceptar = pbAceptar;
    }
    /**
     * Establece el objeto de la clase GpsInterno
     * @param poGpsInterno GpsInterno
     */
    public static void setOGpsInterno (GpsInterno poGpsInterno) {
        oGpsInterno = poGpsInterno;
    }
    /**
     * Establece el objeto que contiene los parámetros de configuración del editor
     * Se usa en un entorno estático para poder compartirlo en todas las actividades.
     * @param poConfCampo ConfCampo
     */
    public static void setOConfCampo (ConfCampo poConfCampo) {
        oConfCampo = poConfCampo;
    }
    /**
     * Devuelve el objeto de la clase CombiOrientacion
     * @return CombiOrientacion
     */
    public static CombiOrientacion getOCombi() {
        return oCombi;
    }
    public static int getNOpcionEntrada () {
        return nOpcionEntrada;
    }
    /**
     * Establece el objeto de la clase CombiOrientacion
     * @param poCombi CombiOrientacion
     */
    public static void setOCombi (CombiOrientacion poCombi) {
        oCombi = poCombi;
    }
    public static void setNOpcionEntrada (int pnValor) {
        nOpcionEntrada = pnValor;
    }

    public static ConfLocaliza getoConfLocaliza() {
        return oConfLocaliza;
    }

    public static void setoConfLocaliza(ConfLocaliza oConfLocaliza) {
        APrincipal.oConfLocaliza = oConfLocaliza;
    }
    public static Vector<String> getvIds() {
        return vIds;
    }

    public static void setvIds(Vector<String> vIds) {
        APrincipal.vIds = vIds;
    }

    /**
     * Método que se lanza la primera vez que se ejecuta la actividad
     * @param icicle Bundle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // ToDo add your GUI initialization code here        
        //Establece la orientación según el dispositivo sea más ancho (horizontal) o alto (vertical)
        /*
        if(UtilsAndroid.esPantallaAncha(this.getResources())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        */
        setContentView(R.layout.principal);
        Log.i("GPS-O","Entrando en la aplicación");
        //Pide permisos si no los tiene asignados todavía
        if (!PermisosUtil.tieneTodosLosPermisos(this)) {
            PermisosUtil.solicitarPermisos(this);
        }
        //Comprueba y crea el directorio que va a contener los distintos archivos
        Utilidades.crearDirectorioPublico(this, cPathAplica);
        //Establezco los valores de las propiedades en el contexto estático, para usarlas más adelante
        oApp = this.getApplication();
        oRes = this.getResources();
        jaru.ori.logic.gpslog.xml.RegistrosGpxXMLHandler.setOApp(this.getApplication());
        jaru.ori.logic.gpslog.xml.RegistrosGpxXMLHandler.setORes(this.getResources());
        //Valores por defecto
        vRegistros = new Vector<Registro>();
        oParametro = new Parametro(cPathAplica, "5000", "5000", "0", "9600", "8", "1", "none", "0");
        oConfCampo = new ConfCampo();
        oTransf = new TransfGeografica();
        cHemisferio = "N";
        cMeridiano = "W";
        //Recupera parámetros de configuración y datos
        if (bEntrada) {
            Log.i("GPS-O","Procesamiento de entrada y carga de XML");
            //Recupera los parámetros de configuración
            Vector vvParams = ParametrosXMLHandler.obtenerDatosXML(this, cPathAplica, "Parametros.xml");
            if (vvParams.size()>0)
                oParametro = (Parametro)vvParams.elementAt(0);
            if (oParametro!=null) {
                PuertoSerie.establecerConfiguracion(oParametro);
            }
            //Recupera los datos que se encuentran almacenados en el terminal.
            this.obtenerDatosXML();
            bEntrada = false;
            bGrabaLoc = false;
            oCombi = new CombiOrientacion(this.getApplicationContext());
        }
        //Crea el hilo para las comunicaciones
        oTransTx = new HiloTransmisiones();
        oThreadTx = new Thread(new HiloRunMensajeria());
        oThreadTx.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (oThreadTx != null)
                oThreadTx.interrupt();
            oThreadTx = new Thread(new HiloRunMensajeria());
            oThreadTx.start();
            if (oTransTx == null)
                oTransTx = new HiloTransmisiones();
        }catch (Exception e) {

        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (oThreadTx != null)
                oThreadTx.interrupt();
            if (oTransTx != null)
                oTransTx.stop();
        }catch (Exception e) {

        }
    }

    /**
     * Método que se llama cuando la aplicación se cierra. 
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("GPS-O","Saliendo de la aplicación");
        //Si se está grabando localización periódica, para el hilo
        if (bGrabaLoc) {
            oTickerLoc.stop();
        }
        //Si el puerto serie estaba abierto, entonces se cierra
        //Cuando se sale normalmente, esta acción ya se habrá realizado.
        //Se deja por si acaso se llega hasta este punto de forma anormal.
        if (PuertoSerie.getBAbierto())
            PuertoSerie.cerrar();
        //Cierra el GPS interno
        if (oGpsInterno!=null) {
            oGpsInterno.pararGps();
            oGpsInterno = null;
        }
        if (oCombi!=null) {
            if (oCombi.existeOrientacion()) {
                oCombi.pararCombi();
                oCombi = null;
            }
        }
        if(oThreadTx!=null)
            oThreadTx.interrupt();
    }
    /**
     * Método que se lanza cuando se crean las opciones de menú
     * @param menu Menu
     * @return boolean Devuelve true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.principal, menu);
        return true;
    }
    /**
     * Método que se lanza cuando se preparan las opciones de menú
     * @param menu Menu
     * @return boolean Devuelve el resultado del método en super
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        boolean vbResul = super.onPrepareOptionsMenu(menu);
        MenuItem oItem = menu.findItem(R.id.grabarLoc0);
        if (!bGrabaLoc) {
            oItem.setTitle(this.getString(R.string.ORI_ML00106));
        } else {
            oItem.setTitle(this.getString(R.string.ORI_ML00107));
        }
        return vbResul;
    }
    /**
     * Método que se lanza cuando el usuario selecciona una opción de menú.<BR>
     * Esta es la actividad principal de la aplicación, así que el menú contiene
     * el acceso a todas las funciones que se han desarrollado.
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.grabar0) {
            this.grabarXML(true);
        } else if (item.getItemId()==R.id.reiniciar0) {
            this.reiniciarDatos();
        } else if (item.getItemId()==R.id.importarGpx0) {
            this.importarGPX();
        } else if (item.getItemId()==R.id.generarOcad0) {
            this.generarOCAD();
        } else if (item.getItemId()==R.id.generarGpx0) {
            this.generarGPX();
        } else if (item.getItemId()==R.id.salir0) {
            this.confirmacionGrabarXML();
        } else if (item.getItemId()==R.id.abrir0) {
            this.realizarListado();
        } else if (item.getItemId()==R.id.lectura0) {
            this.realizarLectura();
        } else if (item.getItemId()==R.id.centroide0) {
            this.realizarCentroide();
        } else if (item.getItemId()==R.id.registro0) {
            this.realizarRegistro();
        } else if (item.getItemId()==R.id.dual0) {
            this.realizarDual();
        } else if (item.getItemId()==R.id.eliminar0) {
            this.realizarEliminar();
        } else if (item.getItemId()==R.id.configurarGps0) {
            this.configurar();
        } else if (item.getItemId()==R.id.configurarCampo0) {
            this.configurarCampo();
        } else if (item.getItemId()==R.id.editorCampo0) {
            this.realizarEditor();
        } else if (item.getItemId()==R.id.calibrarBrujula0) {
            this.realizarBrujula(1);
        } else if (item.getItemId()==R.id.mostrarBrujula0) {
            this.realizarBrujula(0);
        } else if (item.getItemId()==R.id.exportarBocetos0) {
            this.realizarExportarBocetos();
        } else if (item.getItemId()==R.id.cargarNube0) {
            Toast.makeText(this.getApplicationContext(), "Has pulsado cargar nube.", Toast.LENGTH_LONG).show();
        } else if (item.getItemId()==R.id.grabarNube0) {
            Toast.makeText(this.getApplicationContext(), "Has pulsado grabar nube.", Toast.LENGTH_LONG).show();
        } else if (item.getItemId()==R.id.generarASCII0) {
            Toast.makeText(this.getApplicationContext(), "Has pulsado generar ASCII Grid.", Toast.LENGTH_LONG).show();
        } else if (item.getItemId()==R.id.configurarNube0) {
            Toast.makeText(this.getApplicationContext(), "Has pulsado configurar nube.", Toast.LENGTH_LONG).show();
        } else if (item.getItemId()==R.id.configurarLoc0) {
            this.configurarLocaliza();
        } else if (item.getItemId()==R.id.grabarLoc0) {
            this.realizarGrabarLoc();
        } else if (item.getItemId()==R.id.verLoc0) {
            this.realizarVerMapa();
        } else if (item.getItemId()==R.id.testLoc0) {
            this.realizarTestConexion();
        } else if (item.getItemId()==R.id.seleccionarIds0) {
            this.realizarSeleccionarIds();
        }
        return true;
    }
    /**
     * Método que se lanza cuando se regresa de otra actividad. En este caso, interesa
     * realizar procesamiento adicional cuando se regresa de las actividades que 
     * permiten configurar los diferentes parámetros de la aplicación, para grabarlos
     * de nuevo en fichero.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_CAMBIAPARAMETROS:
                //Cuando vuelve del cuadro de diálogo de configuración de parámetros, graba los nuevos valores
                APrincipal.this.actualizarConfiguracionParametros(oParametro);
                break;
            case ACTIVITY_CONFIGURARCAMPO:
                //Cuando vuelve del cuadro de diálogo de configuración del editor, graba los nuevos valores
                APrincipal.this.actualizarConfCampo(oParametro, oConfCampo);
                break;
            case ACTIVITY_EDITORCAMPO:
                //Cuando vuelve del editor, recoge los nuevos valores para grabarlos.
                APrincipal.this.actualizarConfCampo(oParametro, oConfCampo);
                break;
            case ACTIVITY_CONFLOCALIZA:
                //Cuando vuelve del cuadro de diálogo de configuración de la localización, graba los nuevos valores
                APrincipal.this.actualizarConfLocaliza(oParametro, oConfLocaliza);
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermisosUtil.CODIGO_SOLICITUD_PERMISOS) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso denegado: " + permissions[i], Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Al iniciar la aplicación, se comprueba si existen archivos XML con datos 
     * previamente guardados.
     */
    private void obtenerDatosXML() {
        try {
            if (oParametro!=null)
                cPathDatos = oParametro.getCPathXML();
            //Compone el path completo a los datos y comprueba que existen.
            String vcRegistros = "Registros.xml";
            //Si existe alguno de los ficheros de datos, los carga.
            if (Utilidades.existeFicheroPublico(this, cPathDatos, vcRegistros)) {
                //Recupera los datos que se encuentran en los ficheros XML.
                vRegistros = RegistrosXMLHandler.obtenerDatosXML(this, cPathDatos, vcRegistros);
            }
            //Compone el path completo a los datos de configuración del editor 
            //de trabajo de campo y comprueba que existen.
            String vcConfCampo = "ConfCampo.xml";
            //Si existe alguno de los ficheros de datos, los carga.
            if (Utilidades.existeFicheroPublico(this, cPathAplica, vcConfCampo)) {
                Vector vvConfCampo = ConfCampoXMLHandler.obtenerDatosXML(this, cPathAplica, "ConfCampo.xml");
                if (vvConfCampo.size()>0)
                    oConfCampo = (ConfCampo)vvConfCampo.elementAt(0);
            }
            //Compone el path completo a los datos de configuración de la parte de transión de
            //datos de localización
            String vcConfLocaliza = "ConfLocaliza.xml";
            //Si existe alguno de los ficheros de datos, los carga.
            if (Utilidades.existeFicheroPublico(this, cPathAplica, vcConfLocaliza)) {
                Vector vvConfLocaliza = ConfLocalizaXMLHandler.obtenerDatosXML(this, cPathAplica, "ConfLocaliza.xml");
                if (vvConfLocaliza.size()>0) {
                    oConfLocaliza = (ConfLocaliza) vvConfLocaliza.elementAt(0);
                    GestionTransmisiones.setcServidor(oConfLocaliza.getcServidor());
                    GestionTransmisiones.setnPuerto(oConfLocaliza.getnPuerto());
                    GestionTransmisiones.setcServlet(oConfLocaliza.getcServlet());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que realiza las acciones concretas para almacenar los datos en XML.
     * @param pbAvisar boolean Flag que indica si se ha de avisar al usuario.
     */
    private void grabarXML(boolean pbAvisar) {
        try {
            String vcMensaje = "";
            boolean vbCorrecto = true;
            try {
                if (oParametro!=null)
                    cPathDatos = oParametro.getCPathXML();
                if (vRegistros!=null)
                    RegistrosXMLHandler.escribirXML(this, vRegistros, cPathDatos, "Registros.xml");
            } catch (Exception e) {
                e.printStackTrace();
                vbCorrecto = false;
            }
            if (vbCorrecto)
                vcMensaje = this.getString(R.string.ORI_MI00004);
            else
                vcMensaje = this.getString(R.string.ORI_MI00005);
            if (pbAvisar) {
                //Se muestra un mensaje con el resultado correcto o incorrecto del proceso.
                Toast.makeText(this.getApplicationContext(), vcMensaje, Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que pide al usuario confirmación para almacenar los datos en un 
     * archivo XML antes de salir de la aplicación.
     */
    private void confirmacionGrabarXML() {
        try {
            if ((vRegistros!=null & vRegistros.size()>0)) {
                AlertDialog viConfirma = new AlertDialog.Builder(APrincipal.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.ORI_MI00001)
                        .setPositiveButton(R.string.ORI_ML00001, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                APrincipal.this.salir(true);
                            }
                        })
                        .setNegativeButton(R.string.ORI_ML00002, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                APrincipal.this.salir(false);
                            }
                        })
                        .create();
                viConfirma.show();
            } else
                APrincipal.this.salir(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que se llama cuando se pulsa el botón de salir, tanto si el usuario
     * quiere grabar los datos como si no.
     * @param pbGrabar boolean
     */
    private void salir (boolean pbGrabar) {
        try {
            if (pbGrabar) {
                APrincipal.this.grabarXML(false);
            }
            //Si se está grabando localización periódica, para el hilo
            if (bGrabaLoc) {
                oTickerLoc.stop();
            }
            //Cierra el puerto de comunicaciones si estaba abierto
            if (PuertoSerie.getBAbierto())
                PuertoSerie.cerrar();
            //Cierra el GPS interno
            if (oGpsInterno!=null) {
                oGpsInterno.pararGps();
                oGpsInterno = null;
            }
            if (oCombi!=null) {
                if (oCombi.existeOrientacion()) {
                    oCombi.pararCombi();
                    oCombi = null;
                }
            }
        } catch (Exception e) {}
        //Finaliza la aplicación
        this.finish();
    }
    /**
     * Si el usuario está de acuerdo, se eliminan los datos de trabajo y se 
     * comienza un nuevo conjunto.
     */
    private void reiniciarDatos() {
        try {
            final AlertDialog viConfirma = new AlertDialog.Builder(APrincipal.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.ORI_MI00006)
                    .setPositiveButton(R.string.ORI_ML00001, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            APrincipal.this.finalizarReiniciarDatos();
                        }
                    })
                    .setNegativeButton(R.string.ORI_ML00002, null)
                    .create();
            viConfirma.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Finaliza el procedimiento para reiniciar el conjunto de datos.
     */
    private void finalizarReiniciarDatos() {
        vRegistros = new Vector<Registro>();
    }

    /**
     * Accede al componente que gestiona la creación de un archivo OCAD 6 ó 7 a 
     * partir de los datos de trabajo.
     */
    private void generarOCAD() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AGenerarOCAD.class);
            startActivityForResult(viIntent, ACTIVITY_GENERAROCAD);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Accede al componente que gestiona la creación de un archivo GPX a partir 
     * de los datos de trabajo.
     */
    private void generarGPX() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AGenerarGPX.class);
            startActivityForResult(viIntent, ACTIVITY_GENERARGPX);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Método que permite importar el contenido de un archivo GPX externo
     */
    private void importarGPX() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AImportarGPX.class);
            startActivityForResult(viIntent, ACTIVITY_IMPORTARGPX);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Accede al componente que gestiona la visualización de los datos de una lectura de GPS.
     */
    private void realizarLectura() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ALectura.class);
            startActivityForResult(viIntent, ACTIVITY_LECTURA);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Accede al componente que realiza una visualización gráfica del centroide 
     * correspondiente a una lectura de varios puntos de GPS.
     */
    private void realizarCentroide() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ACentroide.class);
            startActivityForResult(viIntent, ACTIVITY_CENTROIDE);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Accede al componente que gestiona la adición de puntos al conjunto de datos.
     */
    private void realizarRegistro() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ALog.class);
            startActivityForResult(viIntent, ACTIVITY_LOG);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Accede al componente que gestiona la adición de puntos al conjunto de datos en un formato dual.
     * Este formato significa que se almacenan sólo caminos, asignando un tipo OCAD principal y un
     * tipo OCAD en formato OBM.
     */
    private void realizarDual() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ADual.class);
            startActivityForResult(viIntent, ACTIVITY_DUAL);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Accede al componente que gestiona la visualización de los datos NMEA recogidos del GPS.
     */
    private void realizarListado() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AListadoNMEA.class);
            startActivityForResult(viIntent, ACTIVITY_LISTADONMEA);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Accede al componente que gestiona la eliminación de registros del conjunto de datos.
     */
    private void realizarEliminar() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AEliminarRegistros.class);
            startActivityForResult(viIntent, ACTIVITY_ELIMINAR);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    private void realizarBrujula(int pnValor) {
        try {
            nOpcionEntrada = pnValor;
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.sensor.gui.android.ABrujula.class);
            startActivityForResult(viIntent, ACTIVITY_BRUJULA);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Accede al componente que gestiona los parámetros de configuración.
     */
    private void configurar() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ACambiaParametros.class);
            startActivityForResult(viIntent, ACTIVITY_CAMBIAPARAMETROS);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Se ejecuta cuando se regresa de la pantalla de configuración de parámetros 
     * de la aplicación. Si se ha aceptado algún cambio realizado, se vuelven a 
     * almacenar los parámetros en XML y se actualiza el contenido del componente.
     * @param poParametro Parametro. Nuevos valores de los parámetros de configuración.
     */
    private void actualizarConfiguracionParametros (Parametro poParametro) {
        if (poParametro!=null) {
            //Actualiza los parámetros y almacena en XML
            oParametro = new Parametro(poParametro.getCPathXML(), poParametro.getCEscala(), poParametro.getCTick(), poParametro.getCPuerto(), poParametro.getCBaudios(),
                    poParametro.getCBitsPalabra(), poParametro.getCBitsStop(), poParametro.getCParidad(),
                    poParametro.getCGpsInterno());
            Vector<Parametro> vvParams = new Vector<Parametro>();
            vvParams.addElement((Parametro)oParametro);
            ParametrosXMLHandler.escribirXML(this, vvParams, cPathAplica, "Parametros.xml");
            //Al establecer la nueva configuración del puerto serie, si estaba abierto, lo cierra.
            PuertoSerie.establecerConfiguracion(oParametro);
        }
    }

    /**
     * Accede al componente que gestiona los parámetros de configuración del 
     * editor de trabajo de campo.
     */
    private void configurarCampo() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AConfCampo.class);
            startActivityForResult(viIntent, ACTIVITY_CONFIGURARCAMPO);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Se ejecuta cuando se regresa de la pantalla de configuración de parámetros 
     * del editor de trabajo de campo.<BR>
     * Si se ha aceptado algún cambio realizado, se vuelven a almacenar los 
     * parámetros en XML y se actualiza el contenido del componente.
     * @param poParametro Parametro. Parámetros de configuración generales.
     * @param poConfCampo ConfCampo. Parámetros de configuración del editor de trabajo de campo.
     */
    private void actualizarConfCampo (Parametro poParametro, ConfCampo poConfCampo) {
        if (poParametro!=null && poConfCampo!=null) {
            //Actualiza los parámetros y almacena en XML
            oConfCampo = new ConfCampo(poConfCampo.getCPlantilla(), poConfCampo.getCCX(), poConfCampo.getCCY(),
                    poConfCampo.getCCX2(), poConfCampo.getCCY2(), poConfCampo.getNZona(), poConfCampo.getCFactorX(),
                    poConfCampo.getCFactorY(), poConfCampo.getCBoceto(), poConfCampo.getNZoom(),
                    poConfCampo.getCCXCentral(), poConfCampo.getCCYCentral(), poConfCampo.getBCalidad());
            Vector<ConfCampo> vvParams = new Vector<ConfCampo>();
            vvParams.addElement((ConfCampo)oConfCampo);
            ConfCampoXMLHandler.escribirXML(this, vvParams, cPathAplica, "ConfCampo.xml");
        }
    }

    /**
     * Accede al componente del editor de trabajo de campo.
     */
    private void realizarEditor() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AEditorCampo.class);
            startActivityForResult(viIntent, ACTIVITY_EDITORCAMPO);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Realiza los procesos para exportar los bocetos como imágenes JPG georreferenciadas
     */
    private void realizarExportarBocetos() {
        String vcMensaje = "";
        try {
            //El proceso de la exportación está encapsulado en la clase ConfCampo
            if (oConfCampo.exportarBocetos()) {
                //Se muestra un mensaje con el resultado correcto del proceso.
                vcMensaje = this.getString(R.string.ORI_MI00004);
            } else {
                //Se muestra un mensaje con el resultado incorrecto del proceso.
                vcMensaje = this.getString(R.string.ORI_MI00015);
            }
            Toast.makeText(this.getApplicationContext(), vcMensaje, Toast.LENGTH_LONG).show();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void realizarGrabarLoc() {
        try {
            if (!bGrabaLoc) {
                //Prepara y lanza la grabación de nuevos registros de localización
                RegistroLocalizacion voRegLoc = new RegistroLocalizacion();
                voRegLoc.setEve2cod(oConfLocaliza.getnEvento());
                voRegLoc.setCat2cod(oConfLocaliza.getnCategoria());
                voRegLoc.setLoccdor(oConfLocaliza.getcDorsal());
                voRegLoc.setLoccnom(oConfLocaliza.getcNombre());
                //Primero pide confirmación para borrar los registros existentes
                pedirConfirmacionBorradoPrevios();
                //Crea el nuevo hilo de ejecución y lo pone a correr
                oTickerLoc = new TickerLoc();
                oTickerLoc.setOParametro(oParametro);
                oTickerLoc.setOGpsInterno(oGpsInterno);
                //oTickerLoc.setoConfLocaliza(oConfLocaliza);
                oTickerLoc.setORegistro(voRegLoc);
                oTickerLoc.setNRetardo(oConfLocaliza.nRetardo*1000);
                oTickerLoc.start();
            } else {
                oTickerLoc.stop();
            }
            bGrabaLoc = !bGrabaLoc;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Antes de comenzar una grabación de registros de localización, pregunta al usuario si quiere borrar
     * los registros existentes. En caso afirmativo, llama a un método que se encarga de comunicarse con el servidor.
     */
    private void pedirConfirmacionBorradoPrevios() {
        try {
            AlertDialog viConfirma = new AlertDialog.Builder(APrincipal.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.ORI_MI00017)
                    .setPositiveButton(R.string.ORI_ML00001, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            APrincipal.this.solicitarBorradoPrevios(true);
                        }
                    })
                    .setNegativeButton(R.string.ORI_ML00002, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            APrincipal.this.solicitarBorradoPrevios(false);
                        }
                    })
                    .create();
            viConfirma.show();
        }catch (Exception e) {
        }
    }
    /**
     * Llama al servidor web para que se borren los datos correspondientes al usuario y evento.
     */
    private void solicitarBorradoPrevios(boolean pbBorrar) {
        try {
            //Si se desea borrar y existe conectividad de datos
            if (pbBorrar && UtilsAndroid.existeConectividadDatos(this)) {
                //Crea un registro de localización básico con los datos de evento, categoría y dorsal
                RegistroLocalizacion voRegLoc = new RegistroLocalizacion();
                voRegLoc.setEve2cod(oConfLocaliza.getnEvento());
                voRegLoc.setCat2cod(oConfLocaliza.getnCategoria());
                voRegLoc.setLoccdor(oConfLocaliza.getcDorsal());
                voRegLoc.setLoccnom(oConfLocaliza.getcNombre());
                //Configura el acceso al servlet adecuado del servidor web
                GestionTransmisiones.setcServidor(oConfLocaliza.getcServidor());
                GestionTransmisiones.setnPuerto(oConfLocaliza.getnPuerto());
                GestionTransmisiones.setcServlet(oConfLocaliza.getcServlet());
                //Crea el elemento con la orden y datos a transmitir
                Vector<Object> vvEnvio = new Vector<Object>();
                vvEnvio.addElement("EliminarLocalizacionesPrevias");
                vvEnvio.addElement(voRegLoc);
                //Lanza la transmisión a través del hilo de comunicaciones
                //La respuesta se consulta periódicamente a través de un hilo separado
                /*
                //
                //CAMBIAR ESTA LLAMADA POR CONVERTIR Y ENVIAR JSON
                //
                oTransTx.setvEnvio(vvEnvio);
                 */
                oTransTx.start();
            }
        }catch (Exception e) {

        }
    }
    /**
     * Método que conecta con el servidor web y le envía una orden de prueba para ver si responde
     */
    private void realizarTestConexion () {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ATestConn.class);
            startActivityForResult(viIntent, ACTIVITY_TESTCONN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Accede al componente que permite ver un mapa de Google Maps y la ubicación de dispositivos
     * dados de alta para seguimiento.
     */
    private void realizarVerMapa() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AVerMapa.class);
            startActivityForResult(viIntent, ACTIVITY_VERMAPA);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Accede al componente que gestiona los parámetros de configuración del
     * gestor de comunicaciones de posicionamientos.
     */
    private void configurarLocaliza() {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AConfLocaliza.class);
            startActivityForResult(viIntent, ACTIVITY_CONFLOCALIZA);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    /**
     * Se ejecuta cuando se regresa de la pantalla de configuración de parámetros
     * del gestor de comunicaciones.<BR>
     * Si se ha aceptado algún cambio realizado, se vuelven a almacenar los
     * parámetros en XML y se actualiza el contenido del componente.
     * @param poParametro Parametro. Parámetros de configuración generales.
     * @param poConfLocaliza ConfLocaliza. Parámetros de configuración del gestor de comunicaciones
     */
    private void actualizarConfLocaliza (Parametro poParametro, ConfLocaliza poConfLocaliza) {
        if (poParametro!=null && poConfLocaliza!=null) {
            //Actualiza los parámetros y almacena en XML
            oConfLocaliza = new ConfLocaliza(poConfLocaliza.getnEvento(), poConfLocaliza.getnCategoria(),
                    poConfLocaliza.getcDorsal(), poConfLocaliza.getcNombre(),
                    poConfLocaliza.getcServidor(), poConfLocaliza.getnPuerto(),
                    poConfLocaliza.getcServlet(), poConfLocaliza.getnRetardo());
            Vector<ConfLocaliza> vvParams = new Vector<ConfLocaliza>();
            vvParams.addElement((ConfLocaliza)oConfLocaliza);
            ConfLocalizaXMLHandler.escribirXML(this, vvParams, cPathAplica, "ConfLocaliza.xml");
        }
    }
    /**
     * Método que se encarga de mostrar los diferentes Ids con información de localización
     */
    public void realizarSeleccionarIds () {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ASelIds.class);
            startActivityForResult(viIntent, ACTIVITY_SELIDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Handler iHandler = new Handler() {
        @Override
        public void handleMessage(Message poMsg) {
            switch (poMsg.what) {
                case APrincipal.MENSAJEPARALOCALIZACION:
                    if (oTransTx!=null) {
                        if (oTransTx.isbResulPreparado()) {
                            /*
                            //
                            //CAMBIAR LO SIGUIENTE POR RECIBIR JSON, CONVERTIR Y PROCESAR
                            //
                            Vector<Object> vvRespuesta = oTransTx.getvRespuesta();
                            //Procesa respuesta del hilo de transmisiones
                            String vcResul = (String) vvRespuesta.elementAt(0);
                            Toast.makeText(APrincipal.this.getApplicationContext(),
                                    APrincipal.this.getString(R.string.ORI_MI00018) + " " + vcResul, Toast.LENGTH_LONG).show();
                             */
                            //Resetea el hilo
                            oTransTx.setbResulPreparado(false);
                            oTransTx.stop();
                        }
                    }
                    break;
            }
            super.handleMessage(poMsg);
        }
    };
    /**
     * Clase interna que permite gestionar el Thread que periódicamente lee
     * si hay algún dato nuevo proporcionado por el servidor
     */
    class HiloRunMensajeria implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = APrincipal.MENSAJEPARALOCALIZACION;
                APrincipal.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardoTx);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
