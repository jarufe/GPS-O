package jaru.ori.logic.gpslog;


import java.util.Vector;

import jaru.gps.logic.GpsInterno;
import jaru.gps.logic.Parametro;
import jaru.gps.logic.PuertoSerie;
import jaru.gps.logic.SentenciaNMEA;
import jaru.ori.utils.Utilidades;
import jaru.ori.web.controlcarrera.RegistroLocalizacion;
import jaru.red.logic.GestionTransmisiones;


/**
 * Implementación para un hilo de ejecución que permite realizar lecturas periódicas 
 * del GPS en el puerto de comunicaciones.
 * <P>
 * Se puede configurar el lapso de tiempo entre ticks (en mseg). A cada tick se realiza la
 * transmisión de los datos de posicionamiento a un servidor web.<BR>
 * Se utilizará un objeto de la clase RegistroLocalizacion para tener siempre disponible el valor
 * de la última lectura realizada.<BR>
 * </P>
 * @author jarufe
 * @version 1.0
 */
public class TickerLoc implements Runnable{
    private Thread oThread;
    private int nRetardo = 20000;
    private RegistroLocalizacion oRegistro = null;
    private SentenciaNMEA oSentencia = null;
    private Parametro oParametro = null;
    private GpsInterno oGpsInterno = null;
//    private ConfLocaliza oConfLocaliza = null;
    private String cResul = "";

    /**
     * Constructor por defecto.
     */
    public TickerLoc() {
    }
    /**
     * Constructor con parámetros.
     * @param pnRetardo int. Retardo en milisegundos.
     */
    public TickerLoc(int pnRetardo) {
        nRetardo = pnRetardo;
        oRegistro = null;
    }
    /**
     * Constructor con parámetros.
     * @param pnRetardo int. Retardo en milisegundos.
     * @param poRegistro RegistroLocalizacion. Objeto usado como plantilla.
     */
    public TickerLoc(int pnRetardo, RegistroLocalizacion poRegistro) {
        nRetardo = pnRetardo;
        oRegistro = poRegistro;
    }
    /**
     * Devuelve el número de milisegundos usado como retardo entre ticks.
     * @return int. Retardo en milisegundos.
     */
    public int getNRetardo() {
        return nRetardo;
    }
    /**
     * Establece el número de milisegundos usado como retardo entre ticks.
     * @param pnValor int. Retardo en milisegundos.
     */
    public void setNRetardo (int pnValor) {
        nRetardo = pnValor;
    }
    /**
     * Devuelve el objeto de la clase RegistroLocalizacion que se usa como base para almacenar
     * nuevos registros de forma automática mientras en temporizador está en marcha.
     * @return RegistroLocalizacion. Objeto usado como plantilla.
     */
    public RegistroLocalizacion getORegistro() {
        return oRegistro;
    }
    /**
     * Establece el objeto de la clase RegistroLocalizacion que se usa como base para almacenar
     * nuevos registros de forma automática mientras en temporizador está en marcha.
     * @param poRegistro RegistroLocalizacion. Objeto usado como plantilla.
     */
    public void setORegistro (RegistroLocalizacion poRegistro) {
        oRegistro = poRegistro;
    }
    /**
     * Devuelve el objeto que contiene los datos de la última lectura realizada
     * sobre el puerto de comunicaciones.
     * @return SentenciaNMEA. Objeto que contiene los datos de la última lectura.
     */
    public SentenciaNMEA getOSentencia() {
        return oSentencia;
    }
    /**
     * Devuelve el objeto de parámetros de configuración
     * @return Parametro. Comfiguración.
     */
    public Parametro getOParametro() {
        return oParametro;
    }
    /**
     * Establece el objeto de configuración
     * @param poValor Parametro. Parametro de configuración
     */
    public void setOParametro (Parametro poValor) {
        oParametro = poValor;
    }
    /**
     * Devuelve el objeto de gestión de GPS interno
     * @return GpsInterno. GPS interno.
     */
    public GpsInterno getOGpsInterno() {
        return oGpsInterno;
    }
    /**
     * Establece el objeto de gestión de GPS interno
     * @param poValor GpsInterno. Gestión de GPS interno
     */
    public void setOGpsInterno (GpsInterno poValor) {
        oGpsInterno = poValor;
    }

    public String getcResul() {
        return cResul;
    }

    /**
     * Método que gestiona la ejecución del hilo de refresco de las comunicaciones.
     */
    public void run() {
        while (Thread.currentThread() == oThread) {
            try {
                //Procesa un nuevo registro si usamos GPS externo y hay datos, o bien si usamos GPS interno
                if (oParametro!=null) {
                    if ((oParametro.getCGpsInterno().equals("0") && PuertoSerie.getBAbierto()) ||
                            (oParametro.getCGpsInterno().equals("1"))) {
                        leerTransmitirNuevoRegistro();
                    }
                    try {
                        Thread.sleep(nRetardo);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception ie) {
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ie.printStackTrace();
            }
        }
    }
    /**
     * Comienzo de un hilo de ejecución que reinicia el canal de entrada de
     * puerto de comunicaciones de forma periódica.
     */
    public void start() {
        if (oThread == null) {
            oThread = new Thread(this);
            oThread.start();
        }
    }
    /**
     * Finalización del hilo de ejecución.
     */
    public void stop() {
        oThread = null;
    }

    /**
     * Método que se encarga de leer un nuevo valor de coordenada desde el puerto de
     * comunicaciones. En caso positivo, transforma la coordenada, crea un nuevo
     * registro de datos y lo comunica al servidor web de destino.
     */
    private void leerTransmitirNuevoRegistro() {
        oSentencia = new SentenciaNMEA();
        try {
            //Recoge los datos de posicionamiento del GPS interno o externo según la configuración establecida
            if (oParametro.getCGpsInterno().equals("0")) {
                oSentencia = PuertoSerie.getOSentencia().copia();
                //Como la sentencia viene de un GPS externo con NMEA, ajusta la hora según el desfase UTC
                int vnDesfase = Utilidades.obtenerDesfaseHorarioMinutos();
                oSentencia.ajustarHora(vnDesfase);
            } else {
                if (oGpsInterno!=null)
                    oSentencia = oGpsInterno.getOSentencia().copia();
            }
        } catch (Exception e) {
        }
        try {
            //Si hay datos correspondientes a una nueva lectura y además está configurado para registrar las lecturas, procede a ello.
            if (oSentencia.cLongitud.length()>0 && oSentencia.cLatitud.length()>0) {
                //Crea un nuevo registro.
                RegistroLocalizacion voRegistro = new RegistroLocalizacion();
                if (oRegistro!=null) {
                    voRegistro.setCat2cod(oRegistro.getCat2cod());
                    voRegistro.setEve2cod(oRegistro.getEve2cod());
                    voRegistro.setLoccdor(oRegistro.getLoccdor());
                    voRegistro.setLoccnom(oRegistro.getLoccnom());
                }
                else {
                    voRegistro.setCat2cod(-1);
                    voRegistro.setEve2cod(-1);
                    voRegistro.setLoccdor("");
                    voRegistro.setLoccnom("");
                }
                //Obtiene los valores transformados de la coordenada leída
                TransfGeografica poTransf = new TransfGeografica();
                String cLongitud = poTransf.transfCoordAGrados(poTransf.obtieneCadena(poTransf.obtieneLong(oSentencia.cLongitud)));
                if (oSentencia.cMeridiano.equals("W"))
                    cLongitud = "-" + cLongitud;
                String cLatitud = poTransf.transfCoordAGrados(poTransf.obtieneCadena(poTransf.obtieneLong(oSentencia.cLatitud)));
                if (oSentencia.cHemisferio.equals("S"))
                    cLatitud = "-" + cLatitud;
                voRegistro.setLocclon(cLongitud);
                voRegistro.setLocclat(cLatitud);
                //Establece la fecha/hora actual
                voRegistro.setLoctpas(Utilidades.getCurDate());
                //Obtiene valor de la elevación
                voRegistro.setLoccele(oSentencia.getCAltura());
                //Finalmente, trata de enviar los datos de localización al servidor web
                transmitirNuevoRegistro(voRegistro);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Método que conecta con el servidor web y le envía el último registro de localización
     * @param poRegistro RegistroLocalizacion. Datos de localización actuales
     */
    private void transmitirNuevoRegistro (RegistroLocalizacion poRegistro) {
        try {
            Vector<Object>vvEnvio = new Vector<Object>();
            //GestionTransmisiones.setoConfLocaliza(oConfLocaliza);
            vvEnvio.addElement("Anadir");
            vvEnvio.addElement(poRegistro);
            /*
            //
            //CAMBIAR LO SIGUIENTE PARA PROCESAR Y ENVIAR JSON
            //
            Vector<Object>vvRespuesta = GestionTransmisiones.transmitirOrden(vvEnvio);
            //Guarda el resultado "OK", "KO" del último envío en la propiedad correspondiente
            cResul = (String)vvRespuesta.elementAt(0);
             */
        } catch (Exception e) {
            cResul = "KO";
            e.printStackTrace();
        }
    }
}
