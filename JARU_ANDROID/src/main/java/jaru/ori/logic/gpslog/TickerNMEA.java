package jaru.ori.logic.gpslog;

import jaru.gps.logic.*;
import java.util.Vector;
import jaru.ori.utils.*;


/**
 * Implementación para un hilo de ejecución que permite realizar lecturas periódicas 
 * del GPS en el puerto de comunicaciones.
 * <P>
 * Se puede configurar el lapso de tiempo entre ticks (en mseg). También se puede 
 * pasar un vector de objetos de la clase Registro, en el cual se podrán ir almacenando 
 * las lecturas. La decisión de almacenar o no las lecturas se toma en base al 
 * valor de una propiedad de tipo boolean. También se puede pasar un objeto de 
 * la clase Registro. Este se utiliza como plantilla para las lecturas que se van 
 * a almacenar. Se pase o no tal objeto, se utilizará un objeto de la clase Registro 
 * para tener siempre disponible el valor de la última lectura realizada.<BR>
 * Como resumen, se puede entonces decir que esta clase permite ir almacenando 
 * las lecturas realizadas periódicamente e, independientemente de esto, también 
 * permite leer en cualquier momento el valor de la última lectura realizada.
 * </P>
 * @author jarufe
 * @version 1.0
 */
public class TickerNMEA implements Runnable{
    private Thread oThread;
    private int nRetardo = 10000;
    private Vector<Registro> vRegistros = null;
    private Registro oRegistro = null;
    private boolean bRegistrar = false;
    private SentenciaNMEA oSentencia = null;
    private Parametro oParametro = null;
    private GpsInterno oGpsInterno = null;

    /**
     * Constructor por defecto.
     */
    public TickerNMEA() {
    }
    /**
     * Constructor con parámetros.
     * @param pnRetardo int. Retardo en milisegundos.
     */
    public TickerNMEA(int pnRetardo) {
        nRetardo = pnRetardo;
        oRegistro = null;
        vRegistros = null;
        bRegistrar = false;
    }
    /**
     * Constructor con parámetros.
     * @param pnRetardo int. Retardo en milisegundos.
     * @param pbRegistrar boolean. Indica si se desean registrar los ticks en el vector de datos o no.
     */
    public TickerNMEA(int pnRetardo, boolean pbRegistrar) {
        nRetardo = pnRetardo;
        oRegistro = null;
        vRegistros = null;
        bRegistrar = pbRegistrar;
    }
    /**
     * Constructor con parámetros.
     * @param pnRetardo int. Retardo en milisegundos.
     * @param poRegistro Registro. Objeto usado como plantilla.
     * @param pvRegistros Vector. Objetos de la clase Registro.
     */
    public TickerNMEA(int pnRetardo, Registro poRegistro, Vector<Registro> pvRegistros) {
        nRetardo = pnRetardo;
        oRegistro = poRegistro;
        vRegistros = pvRegistros;
        bRegistrar = true;
    }
    /**
     * Constructor con parámetros.
     * @param pnRetardo int. Retardo en milisegundos.
     * @param poRegistro Registro. Objeto usado como plantilla.
     * @param pvRegistros Vector. Objetos de la clase Registro.
     * @param pbRegistrar boolean. Indica si se desean registrar los ticks en el vector de datos o no.
     */
    public TickerNMEA(int pnRetardo, Registro poRegistro, Vector<Registro> pvRegistros, boolean pbRegistrar) {
        nRetardo = pnRetardo;
        oRegistro = poRegistro;
        vRegistros = pvRegistros;
        bRegistrar = pbRegistrar;
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
     * Devuelve el objeto de la clase Registro que se usa como base para almacenar
     * nuevos registros de forma automática mientras en temporizador está en marcha.
     * @return Registro. Objeto usado como plantilla.
     */
    public Registro getORegistro() {
        return oRegistro;
    }
    /**
     * Establece el objeto de la clase Registro que se usa como base para almacenar
     * nuevos registros de forma automática mientras en temporizador está en marcha.
     * @param poRegistro Registro. Objeto usado como plantilla.
     */
    public void setORegistro (Registro poRegistro) {
        oRegistro = poRegistro;
    }
    /**
     * Devuelve el conjunto de datos.
     * @return Vector. Conjunto de objetos de la clase Registro.
     */
    public Vector<Registro> getVRegistros() {
        return vRegistros;
    }
    /**
     * Establece el conjunto de datos.
     * @param pvRegistros Vector. Objetos de la clase Registro.
     */
    public void setVRegistros (Vector<Registro> pvRegistros) {
        vRegistros = pvRegistros;
    }
    /**
     * Devuelve el indicador de grabación de los ticks en el vector de datos.
     * @return boolean. Indicador de grabación.
     */
    public boolean getBRegistrar() {
        return bRegistrar;
    }
    /**
     * Establece el indicador de grabación de los ticks en el vector de datos.
     * @param pbRegistrar boolean. Indicador de grabación.
     */
    public void setBRegistrar (boolean pbRegistrar) {
        bRegistrar = pbRegistrar;
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
                        leerAlmacenarNuevoRegistro();
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
     * registro de datos y añade éste al conjunto de datos.
     */
    private void leerAlmacenarNuevoRegistro() {
        oSentencia = new SentenciaNMEA();
        try {
            //Recoge los datos de posicionamiento del GPS interno o externo según la configuración establecida
            if (oParametro.getCGpsInterno().equals("0"))
                oSentencia = PuertoSerie.getOSentencia().copia();
            else {
                if (oGpsInterno!=null)
                    oSentencia = oGpsInterno.getOSentencia().copia();
            }
        } catch (Exception e) {
        }
        try {
            //Si hay datos correspondientes a una nueva lectura y además está configurado para registrar las lecturas, procede a ello.
            if (oSentencia.cLongitud.length()>0 && oSentencia.cLatitud.length()>0 && bRegistrar) {
                //Crea un nuevo registro.
                Registro voRegistro = new Registro();
                if (oRegistro!=null) {
                    voRegistro.setCID(oRegistro.getCID());
                    voRegistro.setNTipo(oRegistro.getNTipo());
                    voRegistro.setCTipoOCAD(oRegistro.getCTipoOCAD());
                    voRegistro.setCTipoOBM(oRegistro.getCTipoOBM());
                    voRegistro.setCDesc(oRegistro.getCDesc());
                }
                else {
                    voRegistro.setCID("0");
                    voRegistro.setNTipo(1);
                    voRegistro.setCTipoOCAD("506.0");
                    voRegistro.setCTipoOBM("");
                    voRegistro.setCDesc("AUTO");
                }
                //Obtiene los valores transformados de la coordenada leída
                TransfGeografica poTransf = new TransfGeografica();
                String cLongitud = poTransf.transfCoord(poTransf.obtieneCadena(poTransf.obtieneLong(oSentencia.cLongitud)));
                if (oSentencia.cMeridiano.equals("W"))
                    cLongitud = "-" + cLongitud;
                String cLatitud = poTransf.transfCoord(poTransf.obtieneCadena(poTransf.obtieneLong(oSentencia.cLatitud)));
                if (oSentencia.cHemisferio.equals("S"))
                    cLatitud = "-" + cLatitud;
                voRegistro.setCCX(cLongitud);
                voRegistro.setCCY(cLatitud);
                //Establece la fecha/hora actual
                voRegistro.setCFecha(Utilidades.obtenerFechaHoraParaGpx());
                //Obtiene valor de la elevación
                voRegistro.setCElev(oSentencia.getCAltura());
                //Añade el nuevo registro al conjunto de datos
                vRegistros.add(voRegistro);
            }
        } catch (Exception e) {
        }

    }

}
