package jaru.gps.logic;

/**
 * Clase que contiene los datos relacionados con parámetros propios de la configuración PDA.
 * @author: jarufe
 */
public class Parametro implements java.io.Serializable {
    private String cPathXML = null;
    private String cEscala = null;
    private String cTick = null;
    private String cPuerto = null;
    private String cBaudios = null;
    private String cBitsPalabra = null;
    private String cBitsStop = null;
    private String cParidad = null;
    private String cGpsInterno = null;
    /**
     * Constructor por defecto de la clase.
     * <P>
     * Class constructor by default
     * </P>
     */
    public Parametro() {
        cPathXML = "/mnt/sdcard/JARU/";
        cEscala = "5000";
        cTick = "5000";
        cPuerto = "0";
        cBaudios = "9600";
        cBitsPalabra = "8";
        cBitsStop = "1";
        cParidad = "none";
        cGpsInterno = "0";
    }
    /**
     * Constructor con parámetros
     * @param pcPathXML String. Path donde se encuentran los datos XML.
     */
    public Parametro(String pcPathXML) {
        cPathXML = pcPathXML;
        cEscala = "5000";
        cTick = "5000";
        cPuerto = "0";
        cBaudios = "9600";
        cBitsPalabra = "8";
        cBitsStop = "1";
        cParidad = "none";
        cGpsInterno = "0";
    }
    /**
     * Constructor con parámetros
     * @param pcPathXML String. Path donde se encuentran los datos XML.
     * @param pcPuerto String. Puerto COM donde se encuentra el dispositivo GPS.
     * @param pcBaudios String. Velocidad en baudios.
     * @param pcBitsPalabra String. Número de bits por palabra.
     * @param pcBitsStop String. Número de bits de stop.
     * @param pcParidad String. Tipo de paridad.
     */
    public Parametro(String pcPathXML, String pcPuerto, String pcBaudios, String pcBitsPalabra, String pcBitsStop, String pcParidad) {
        cPathXML = pcPathXML;
        cEscala = "5000";
        cTick = "5000";
        cPuerto = pcPuerto;
        cBaudios = pcBaudios;
        cBitsPalabra = pcBitsPalabra;
        cBitsStop = pcBitsStop;
        cParidad = pcParidad;
        cGpsInterno = "0";
    }
    /**
     * Constructor con parámetros
     * @param pcPathXML String. Path donde se encuentran los datos XML.
     * @param pcEscala String. Escala de exportación a OCAD.
     * @param pcPuerto String. Puerto COM donde se encuentra el dispositivo GPS.
     * @param pcBaudios String. Velocidad en baudios.
     * @param pcBitsPalabra String. Número de bits por palabra.
     * @param pcBitsStop String. Número de bits de stop.
     * @param pcParidad String. Tipo de paridad.
     */
    public Parametro(String pcPathXML, String pcEscala, String pcPuerto, String pcBaudios, String pcBitsPalabra, String pcBitsStop, String pcParidad) {
        cPathXML = pcPathXML;
        cEscala = pcEscala;
        cTick = "5000";
        cPuerto = pcPuerto;
        cBaudios = pcBaudios;
        cBitsPalabra = pcBitsPalabra;
        cBitsStop = pcBitsStop;
        cParidad = pcParidad;
        cGpsInterno = "0";
    }

    /**
     * Constructor con parámetros
     * @param pcPathXML String. Path donde se encuentran los datos XML.
     * @param pcEscala String. Escala de exportación a OCAD.
     * @param pcTick String. Intervalo de recogida de datos (mseg).
     * @param pcPuerto String. Puerto COM donde se encuentra el dispositivo GPS.
     * @param pcBaudios String. Velocidad en baudios.
     * @param pcBitsPalabra String. Número de bits por palabra.
     * @param pcBitsStop String. Número de bits de stop.
     * @param pcParidad String. Tipo de paridad.
     */
    public Parametro(String pcPathXML, String pcEscala, String pcTick, String pcPuerto, String pcBaudios, String pcBitsPalabra, String pcBitsStop, String pcParidad) {
        cPathXML = pcPathXML;
        cEscala = pcEscala;
        cTick = pcTick;
        cPuerto = pcPuerto;
        cBaudios = pcBaudios;
        cBitsPalabra = pcBitsPalabra;
        cBitsStop = pcBitsStop;
        cParidad = pcParidad;
        cGpsInterno = "0";
    }

    /**
     * Constructor con parámetros
     * @param pcPathXML String. Path donde se encuentran los datos XML.
     * @param pcEscala String. Escala de exportación a OCAD.
     * @param pcTick String. Intervalo de recogida de datos (mseg).
     * @param pcPuerto String. Puerto COM donde se encuentra el dispositivo GPS.
     * @param pcBaudios String. Velocidad en baudios.
     * @param pcBitsPalabra String. Número de bits por palabra.
     * @param pcBitsStop String. Número de bits de stop.
     * @param pcParidad String. Tipo de paridad.
     * @param pcGpsInterno String. Usa GPS interno del dispositivo o no.
     */
    public Parametro(String pcPathXML, String pcEscala, String pcTick, String pcPuerto, String pcBaudios, String pcBitsPalabra, String pcBitsStop, String pcParidad, String pcGpsInterno) {
        cPathXML = pcPathXML;
        cEscala = pcEscala;
        cTick = pcTick;
        cPuerto = pcPuerto;
        cBaudios = pcBaudios;
        cBitsPalabra = pcBitsPalabra;
        cBitsStop = pcBitsStop;
        cParidad = pcParidad;
        cGpsInterno = pcGpsInterno;
    }

    /**
     * Devuelve el Path XML.
     * @return String.
     */
    public String getCPathXML() {
        return cPathXML;
    }
    /**
     * Establece el Path XML.
     * @param pcVal String.
     */
    public void setCPathXML(String pcVal) {
        cPathXML = pcVal;
    }
    /**
     * Devuelve el valor de la escala de exportación.
     * @return String.
     */
    public String getCEscala() {
        return cEscala;
    }
    /**
     * Establece el valor de la escala de exportación.
     * @param pcVal String.
     */
    public void setCEscala(String pcVal) {
        cEscala = pcVal;
    }
    /**
     * Devuelve el valor del intervalo de recogida de datos (mseg).
     * @return String.
     */
    public String getCTick() {
        return cTick;
    }
    /**
     * Establece el valor del intervalo de recogida de datos (mseg).
     * @param pcVal String.
     */
    public void setCTick(String pcVal) {
        cTick = pcVal;
    }
    /**
     * Devuelve el Id de puerto serie.
     * @return String.
     */
    public String getCPuerto() {
        return cPuerto;
    }
    /**
     * Establece el Id de puerto serie.
     * @param pcVal String.
     */
    public void setCPuerto(String pcVal) {
        cPuerto = pcVal;
    }
    /**
     * Devuelve la velocidad en baudios.
     * @return String.
     */
    public String getCBaudios() {
        return cBaudios;
    }
    /**
     * Establece la velocidad en baudios.
     * @param pcVal String.
     */
    public void setCBaudios(String pcVal) {
        cBaudios = pcVal;
    }
    /**
     * Devuelve el número de bits por palabra.
     * @return String.
     */
    public String getCBitsPalabra() {
        return cBitsPalabra;
    }
    /**
     * Establece el número de bits por palabra.
     * @param pcVal String.
     */
    public void setCBitsPalabra(String pcVal) {
        cBitsPalabra = pcVal;
    }
    /**
     * Devuelve el número de bits de stop.
     * @return String.
     */
    public String getCBitsStop() {
        return cBitsStop;
    }
    /**
     * Establece el número de bits de stop.
     * @param pcVal String.
     */
    public void setCBitsStop(String pcVal) {
        cBitsStop = pcVal;
    }
    /**
     * Devuelve el tipo de paridad empleado.
     * @return String.
     */
    public String getCParidad() {
        return cParidad;
    }
    /**
     * Establece el tipo de paridad empleado.
     * @param pcVal String.
     */
    public void setCParidad(String pcVal) {
        cParidad = pcVal;
    }
    /**
     * Devuelve si usa GPS interno o no
     * @return String.
     */
    public String getCGpsInterno() {
        return cGpsInterno;
    }
    /**
     * Establece si usa GPS interno o no
     * @param pcVal String.
     */
    public void setCGpsInterno(String pcVal) {
        cGpsInterno = pcVal;
    }

}
