package jaru.gps.logic;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.*;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import jaru.ori.gui.gpslog.android.R;


/**
 * Clase que representa el acceso a un puerto serie. Permite abrir, enviar, 
 * recibir y cerrar datos.<BR>
 * La versión Android de esta clase maneja GPS a través de una conexión
 * Bluetooth. Al no existir un paquete de clases para la gestión de puerto
 * serie (como en la versión de PC), la comunicación se tiene que gestionar
 * directamente a través de los paquetes de Bluetooth.
 * @author jarufe
 */
public class PuertoSerie {
    private static int nBaudios;
    private static int nBitsPalabra;
    private static int nBitsStop;
    private static String cParidad;
    private static String cNombre;

    private static BluetoothAdapter oBtInterface;
    private static Context oContext;
    private static Set<BluetoothDevice> vPairedDevices;
    private static BluetoothSocket oSocket;
    private static InputStreamReader is = null;
    private static OutputStreamWriter os = null;

    private static boolean bAbierto = false;
    private static int nModo = 0;

    private static SentenciaNMEA oSentencia = new SentenciaNMEA();
    private static PuertoSerieTicker oTicker = new PuertoSerieTicker();

    /**
     * Constructor de la clase. Como no tiene parámetros, establece los siguientes:<BR>
     * Puerto COM: 0<BR>
     * Baudios: 9600<BR>
     * Bits Palabra: 8<BR>
     * Bits Stop: 1<BR>
     * Paridad: none<BR>
     */
    public PuertoSerie () {
        nBaudios = 9600;
        nBitsPalabra = 8;
        nBitsStop = 1;
        cParidad = new String("none");
        cNombre = new String("0");
        bAbierto = false;
    }
    /**
     * Constructor de la clase.
     * @param pcPuerto String. Puerto COM.
     * @param pnBaudios int. Velocidad en Baudios.
     * @param pnBitsPalabra int. Número de bits por palabra.
     * @param pnBitsStop int. Número de bits de stop.
     * @param pcParidad String. Tipo de paridad.
     */
    public PuertoSerie (String pcPuerto, int pnBaudios, int pnBitsPalabra, int pnBitsStop, String pcParidad) {
        nBaudios = pnBaudios;
        nBitsPalabra = pnBitsPalabra;
        nBitsStop = pnBitsStop;
        cParidad = new String (pcParidad);
        cNombre = new String (pcPuerto);
        bAbierto = false;
    }
    /**
     * Constructor de la clase.
     * @param poParametro Parametro. Objeto que contiene todos los valores de configuración.
     */
    public PuertoSerie (Parametro poParametro) {
        nBaudios = Integer.parseInt(poParametro.getCBaudios());
        nBitsPalabra = Integer.parseInt(poParametro.getCBitsPalabra());
        nBitsStop = Integer.parseInt(poParametro.getCBitsStop());
        cParidad = new String (poParametro.getCParidad());
        cNombre = new String (poParametro.getCPuerto());
        bAbierto = false;
    }
    /**
     * Devuelve un valor booleano indicando si el puerto está abierto o no.
     * @return boolean.
     */
    public static boolean getBAbierto() {
        return bAbierto;
    }
    /**
     * Devuelve la última Sentencia NMEA procesada correctamente como valor de posición GPS.
     * @return SentenciaNMEA.
     */
    public static SentenciaNMEA getOSentencia() {
        return oSentencia;
    }
    /**
     * Devuelve el objeto que representa al hilo continuo de lectura.
     * @return PuertoSerieTicker.
     */
    public static PuertoSerieTicker getOTicker() {
        return oTicker;
    }
    /**
     * Devuelve el objeto que representa al canal de entrada.
     * @return java.io.InputStreamReader.
     */
    public static synchronized java.io.InputStreamReader getCanalEntrada() {
        return is;
    }
    /**
     * Devuelve el objeto que representa al canal de salida.
     * @return java.io.OutputStreamWriter.
     */
    public static synchronized java.io.OutputStreamWriter getCanalSalida() {
        return os;
    }

    public static Context getoContext() {
        return oContext;
    }

    public static void setoContext(Context oContext) {
        PuertoSerie.oContext = oContext;
    }

    /**
     * Constructor de la clase.
     * @param poParametro Parametro. Objeto que contiene todos los valores de configuración.
     */
    public static synchronized void establecerConfiguracion (Parametro poParametro) {
        try {
            if (bAbierto) {
                cerrar();
                bAbierto = false;
            }
            nBaudios = Integer.parseInt(poParametro.getCBaudios());
            nBitsPalabra = Integer.parseInt(poParametro.getCBitsPalabra());
            nBitsStop = Integer.parseInt(poParametro.getCBitsStop());
            cParidad = new String (poParametro.getCParidad());
            cNombre = new String (poParametro.getCPuerto());
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }
    /**
     * Constructor de la clase.
     * @param poParametro Parametro. Objeto que contiene todos los valores de configuración.
     * @param poContext Context. Contexto de la aplicación, necesario para algunas tareas dentro de la clase
     */
    public static synchronized void establecerConfiguracion (Parametro poParametro, Context poContext) {
        try {
            if (bAbierto) {
                cerrar();
                bAbierto = false;
            }
            nBaudios = Integer.parseInt(poParametro.getCBaudios());
            nBitsPalabra = Integer.parseInt(poParametro.getCBitsPalabra());
            nBitsStop = Integer.parseInt(poParametro.getCBitsStop());
            cParidad = new String (poParametro.getCParidad());
            cNombre = new String (poParametro.getCPuerto());
            oContext = poContext;
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }
    /**
     * Trata de realizar la apertura del puerto de comunicaciones, para obtener un
     * canal de entrada y otro de salida.
     * @param pnModo int. 0=>lectura; 1=>escritura; 2=lectura+escritura
     * @throws java.io.IOException
     */
    public static synchronized boolean abrir(int pnModo) {
        boolean vbResul = true;
        try {
            nModo = pnModo;
            //Si había una conexión abierta, primero la cierra
            if (oSocket!=null)
                cerrar();
            //Busca el identificador del dispositivo al que nos queremos conectar
            oSocket = buscarPuertos(cNombre);
            if (oSocket!=null) {
                if (pnModo==0 || pnModo==2)
                    is = new InputStreamReader(oSocket.getInputStream());
                if (pnModo==1 || pnModo==2)
                    os = new OutputStreamWriter(oSocket.getOutputStream());
                oTicker.start();
                bAbierto = true;
            }
            else {
                Log.i("Error", "No encuentro el dispositivo.");
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            Log.e("Error", "Error durante la apertura del puerto.");
            vbResul = false;
            bAbierto = false;
        }
        return vbResul;
    }
    /**
     * Trata de cerrar el puerto de comunicaciones.
     * @throws java.io.IOException
     */
    public static synchronized boolean cerrar() { //throws java.io.IOException {
        boolean vbResul = true;
        try {
            if (oSocket!=null) {
                if (getCanalEntrada()!=null)
                    getCanalEntrada().close();
                if (getCanalSalida()!=null)
                    getCanalSalida().close();
                oSocket.close();
            }
            is = null;
            os = null;
            oSocket = null;
            oTicker.stop();
            bAbierto = false;
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            vbResul = false;
            bAbierto = false;
        }
        return vbResul;
    }
    /**
     * Resetea el canal de entrada
     */
    public static synchronized void resetear () {
        try {
            if (bAbierto && getCanalEntrada()!=null) {
                //getCanalEntrada().reset();
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }
    /**
     * Envía una cadena de texto por el canal de salida.
     * @param cTexto String. Texto que se envía por canal de salida del puerto de comunicaciones.
     */
    public static synchronized void enviar (String cTexto) {
        StringBuffer messagebuffer = new StringBuffer();
        try {
            if (!bAbierto) {
                abrir(nModo);
            }
            byte[] cNuevoTexto = cTexto.getBytes();
            for (int i = 0; i<cNuevoTexto.length; i++) {
                getCanalSalida().write(cNuevoTexto[i]);
            }
            getCanalSalida().flush();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }
    /**
     * Recibe un Número máximo de caracteres por el canal de entrada del puerto de
     * comunicaciones.
     * @param nMax int. Límite de bytes que se quieren recibir.
     * @return String. Texto completo que se ha recibido.
     */
    public static synchronized String recibir (int nMax) {
        StringBuffer cBuffer = new StringBuffer();
        int ch = 0, nCont = 0;

        try {
            if (!bAbierto) {
                abrir(nModo);
            }
            cBuffer.setLength(0);
            // Check the Content-Length first
            while (((ch=getCanalEntrada().read())!= -1) && nCont<nMax)
            {
                cBuffer.append((char) ch);
                nCont++;
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            cBuffer.append("ERROR");
        }
        return cBuffer.toString();
    }
    /**
     * Recibe un conjunto de caracteres, entre uno específico de comienzo y otro de finalización.
     * @param cIni char. Byte de comienzo de la lectura.
     * @param cFin char. Byte que señala el final de la lectura.
     * @return String. Texto completo que se ha recibido.
     */
    public static synchronized String recibir (char cIni, char cFin) {
        StringBuffer cBuffer = new StringBuffer();
        int ch = 0;
        boolean bInicio = false;
        boolean bFinal = false;

        try {
            if (!bAbierto) {
                abrir(nModo);
            }
            cBuffer.setLength(0);
            // Check the Content-Length first
            while ((ch=getCanalEntrada().read())!=-1 && bFinal==false)
            {
                if ((cIni == (char) ch) && !bInicio)
                    bInicio = true;
                if (bInicio)
                    cBuffer.append((char) ch);
                if ((cFin == (char) ch) && bInicio)
                    bFinal = true;
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            cBuffer.append("ERROR");
        }
        return cBuffer.toString();
    }
    /**
     * Devuelve el nombre del primer dispositivo Bluetooth encontrado a
     * partir de un procedimiento de búsqueda.
     * @return String. Id del primer puerto de comunicaciones encontrado.
     */
    public static synchronized String buscarPuertos () {
        String cPort1 = new String("");

        try {
            oBtInterface = BluetoothAdapter.getDefaultAdapter();
            // Comprobación de si existe permiso otorgado para Bluetooth
            // Bluetooth (solo BLUETOOTH_CONNECT en Android 12+)
            if (!(ActivityCompat.checkSelfPermission(oContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S))) {
                Log.i("Info", "Local BT Interface name is [" + oBtInterface.getName() + "]");
                vPairedDevices = oBtInterface.getBondedDevices();
                Log.i("Info","Found [" + vPairedDevices.size() + "] devices.");
                Iterator<BluetoothDevice> voIt = vPairedDevices.iterator();
                while (voIt.hasNext())
                {
                    BluetoothDevice voBD = voIt.next();
                    Log.i("Info", "Name of peer is [" + voBD.getName() + "]");
                    cPort1 = voBD.getName();
                }
            }
        } catch (Exception e) {
            Log.e("Error","Failed in findRobot() " + e.getMessage());
            cPort1 = "";
        }
        return cPort1;
    }
    /**
     * Devuelve los nombres de los dispositivos Bluetooth encontrados a
     * partir de un procedimiento de búsqueda.
     * @return Vector<String>. Nombre de los dispositivos encontrados.
     */
    public static synchronized Vector<String> buscarPuertosLista () {
        Vector<String> vvResul = new Vector<String>();

        try {
            oBtInterface = BluetoothAdapter.getDefaultAdapter();
            // Comprobación de si existe permiso otorgado para Bluetooth
            // Bluetooth (solo BLUETOOTH_CONNECT en Android 12+)
            if (!(ActivityCompat.checkSelfPermission(oContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S))) {
                Log.i("Info", "Local BT Interface name is [" + oBtInterface.getName() + "]");
                vPairedDevices = oBtInterface.getBondedDevices();
                Log.i("Info","Found [" + vPairedDevices.size() + "] devices.");
                Iterator<BluetoothDevice> voIt = vPairedDevices.iterator();
                while (voIt.hasNext()) {
                    BluetoothDevice voBD = voIt.next();
                    Log.i("Info", "Name of peer is [" + voBD.getName() + "]");
                    vvResul.addElement(voBD.getName());
                }
            }
        } catch (Exception e) {
            Log.e("Error","Failed in findRobot() " + e.getMessage());
        }
        return vvResul;
    }

    /**
     * Devuelve la identificacion del puerto de comunicaciones que coincide con el
     * nombre buscado.
     * @return BluetoothSocket. Id del puerto encontrado.
     */
    public static synchronized BluetoothSocket buscarPuertos (String pcNombre) {
        BluetoothSocket voSocket = null;
        boolean bEncontrado = false;

        try {
            oBtInterface = BluetoothAdapter.getDefaultAdapter();
            // Comprobación de si existe permiso otorgado para Bluetooth
            // Bluetooth (solo BLUETOOTH_CONNECT en Android 12+)
            if (!(ActivityCompat.checkSelfPermission(oContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S))) {
                Log.i("Info", "Local BT Interface name is [" + oBtInterface.getName() + "]");
                vPairedDevices = oBtInterface.getBondedDevices();
                Log.i("Info","Found [" + vPairedDevices.size() + "] devices.");
                Iterator<BluetoothDevice> voIt = vPairedDevices.iterator();
                while (voIt.hasNext()) {
                    BluetoothDevice voBD = voIt.next();
                    Log.i("Info", "Name of peer is [" + voBD.getName() + "]");
                    if (voBD.getName().equalsIgnoreCase(pcNombre)) {
                        Log.i("Info", "Found Robot!");
                        Log.i("Info", voBD.getAddress());
                        Log.i("Info", voBD.getBluetoothClass().toString());
                        //Crea un socket de comunicaciones con el Id de RFCOMM de Bluetooth
                        voSocket = voBD.createRfcommSocketToServiceRecord(java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        voSocket.connect();
                        bEncontrado = true;
                    }
                }
                if (!bEncontrado) {
                    voSocket = null;
                }
            }
        } catch (Exception e) {
            Log.e("Error","Failed in findRobot() " + e.getMessage());
            voSocket = null;
        }
        return voSocket;
    }


    /**
     * Realiza un proceso completo de lectura de datos geográficos, a través del puerto serie.
     * @param pnModo int. 0=>lectura; 1=>escritura; 2=lectura+escritura
     */
    public static synchronized SentenciaNMEA lecturaCompleta(int pnModo) {
        SentenciaNMEA vcSentencia = new SentenciaNMEA();
        String cError = new String("");
        String cTexto = new String("");
        int nIntentos = 0;

        try {
            cError = "NO ABRE";
            if (!bAbierto)
                bAbierto = abrir(pnModo);
            if (bAbierto) {
                while (nIntentos<2000) {
                    cError = "NO LEE";
                    cTexto = recibir('$','*');
                    if (cTexto.length() > 0) {
                        //Los datos se muestran cuando han llegado los dos tipos de sentencias
                        //necesarias==> 1+2=3
                        if (vcSentencia.procesaSentencia(cTexto)==3) {
                            nIntentos=10000;
                        }
                    }
                    else {
                        nIntentos = 10000;
                    }
                    nIntentos++;
                }
                cError = "NO CIERRA";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcSentencia;
    }

}
