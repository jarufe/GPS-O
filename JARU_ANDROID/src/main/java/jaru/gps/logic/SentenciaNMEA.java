package jaru.gps.logic;

/**
 * Clase que representa el contenido desglosado de las sentencias NMEA.
 * <P>
 * De todo el protocolo NMEA en realidad sólo interesa el tipo de sentencia que 
 * contiene los datos de posición, hora y número de satélites fijados. La clase 
 * contiene un método para desglosar la sentencia NMEA en cuestión
 * y almacenar cada valor en una propiedad de la clase.
 * </P> 
 * @author jarufe
 */
public class SentenciaNMEA {
    public int nOk;
    public String cLongitud;
    public String cLatitud;
    public String cHemisferio;
    public String cMeridiano;
    public String cAltura;
    public String cDatum;
    public String cHora;
    public String cFix;
    public String cSatelites;
    public String cHdop;
    public String cSentencia;

    /**
     * Constructor por defecto.
     *
     */
    public SentenciaNMEA() {
        nOk = 0;
        cLongitud = new String("00:00:00");
        cLatitud = new String("00:00:00");
        cHemisferio = new String("");
        cMeridiano = new String("");
        cAltura = new String("0");
        cDatum = new String("");
        cHora = new String("00:00:00");
        cFix = "0";
        cSatelites = new String("0");
        cHdop = new String("0.0");
        cSentencia = new String("");
    }
    /**
     * Devuelve el valor de la longitud.
     * @return String.
     */
    public synchronized String getCLongitud () {
        return cLongitud;
    }
    /**
     * Devuelve el valor de la latitud.
     * @return String.
     */
    public synchronized String getCLatitud () {
        return cLatitud;
    }
    /**
     * Devuelve una letra que indica el hemisferio al que corresponde la latitud => N, S
     * @return String.
     */
    public synchronized String getCHemisferio () {
        return cHemisferio;
    }
    /**
     * Devuelve una letra que indica en qué lado del meridiano 0 se encuentra la longitud => E, W
     * @return String.
     */
    public synchronized String getCMeridiano () {
        return cMeridiano;
    }
    /**
     * Devuelve el valor de altura.
     * @return String.
     */
    public synchronized String getCAltura () {
        return cAltura;
    }
    /**
     * Devuelve el nombre del Datum que utiliza el GPS. Por defecto es WGS84.
     * @return String.
     */
    public synchronized String getCDatum () {
        return cDatum;
    }
    /**
     * Devuelve el valor de hora UTC.
     * @return String.
     */
    public synchronized String getCHora () {
        return cHora;
    }
    /**
     * Devuelve una letra que indica si existen satélites fijados => 0 = No; !=0 = Sí.
     * @return String.
     */
    public synchronized String getCFix () {
        return cFix;
    }
    /**
     * Devuelve el número de satélites desde los que se está recogiendo información.
     * @return String.
     */
    public synchronized String getCSatelites () {
        return cSatelites;
    }
    /**
     * Devuelve el valor de la propiedad HDOP (Horizontal Dillution of Position).
     * @return String.
     */
    public synchronized String getCHdop () {
        return cHdop;
    }
    /**
     * Devuelve el valor de la propiedad que contiene la sentencia completa
     * @return String.
     */
    public synchronized String getCSentencia () {
        return cHdop;
    }
    /**
     * Establece el valor de longitud.
     * @param pcValor String.
     */
    public synchronized void setCLongitud (String pcValor) {
        cLongitud = pcValor;
    }
    /**
     * Establece el valor de latitud.
     * @param pcValor String.
     */
    public synchronized void setCLatitud (String pcValor) {
        cLatitud = pcValor;
    }
    /**
     * Establece la letra que indica el hemisferio.
     * @param pcValor String.
     */
    public synchronized void setCHemisferio (String pcValor) {
        cHemisferio = pcValor;
    }
    /**
     * Establece la letra que indica la posición respecto al meridiano 0.
     * @param pcValor String.
     */
    public synchronized void setCMeridiano (String pcValor) {
        cMeridiano = pcValor;
    }
    /**
     * Establece el valor de altura.
     * @param pcValor String.
     */
    public synchronized void setCAltura (String pcValor) {
        cAltura = pcValor;
    }
    /**
     * Establece el valor del Datum utilizado por el GPS.
     * @param pcValor String.
     */
    public synchronized void setCDatum (String pcValor) {
        cDatum = pcValor;
    }
    /**
     * Establece el valor de hora UTC.
     * @param pcValor String.
     */
    public synchronized void setCHora (String pcValor) {
        cHora = pcValor;
    }
    /**
     * Establece la letra que indica si existen satélites fijados.
     * @param pcValor String.
     */
    public synchronized void setCFix (String pcValor) {
        cFix = pcValor;
    }
    /**
     * Establece el número de satélites desde los que se recibe información.
     * @param pcValor String.
     */
    public synchronized void setCSatelites (String pcValor) {
        cSatelites = pcValor;
    }
    /**
     * Establece el valor de la propiedad HDOP (Horizontal Dillution of Position)
     * @param pcValor String.
     */
    public synchronized void setCHdop (String pcValor) {
        cHdop = pcValor;
    }
    /**
     * Establece el valor de la propiedad que contiene la sentencia completa
     * @param pcValor String.
     */
    public synchronized void setCSentencia (String pcValor) {
        cSentencia = pcValor;
    }
    /**
     * Dada una cadena que contiene una sentencia NMEA completa, la procesa.
     * <P>
     * Hay que señalar que la única sentencia NMEA interesante es la que se identifica
     * como $GPGGA, la cual contiene los valores de posición, hora y satélites fijados.
     * </P>
     * @param pcTexto String. Sentencia NMEA.
     * @return int. El valor 3 indica que la sentencia se ha procesado correctamente.
     */
    public synchronized int procesaSentencia(String pcTexto) {
        String vcID = new String("");
        int vnDevol = -1;

        try {
            if (pcTexto.length()>7) {
                cSentencia = pcTexto;
                vcID = pcTexto.substring(0, 6);
                if (vcID.equals("$GPGGA")) {
                    cLongitud = elemento(pcTexto, 5);
                    cMeridiano = elemento(pcTexto, 6);
                    cLatitud = elemento(pcTexto, 3);
                    cHemisferio = elemento(pcTexto, 4);
                    cAltura = elemento(pcTexto, 10);
                    cHora = elemento(pcTexto, 2);
                    cFix = elemento(pcTexto, 7);
                    cSatelites = elemento(pcTexto, 8);
                    cHdop = elemento(pcTexto, 9);
                    if (nOk==0 || nOk==2)
                        nOk = nOk + 1;
                    vnDevol = nOk;
                    nOk = 3;
                    vnDevol = 3;
                }
                if (vcID.equals("$PGRMM")) {
                    int i = pcTexto.indexOf("*");
                    if (i > 0)
                    {
                        cDatum = pcTexto.substring(7, i);
                    }
                    else
                        cDatum = " ";
				/*
				if (nOk==0 || nOk==1)
					nOk = nOk + 2;
				nDevol = nOk;
				*/
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vnDevol;
    }

    /**
     * Método que extrae de una cadena un elemento determinado por su posición en la cadena.
     * <P>
     * Los valores en la cadena de entrada vienen separados por el símbolo ",".
     * </P>
     * @param pcTexto String. Cadena de la que se quiere extraer un elemento.
     * @param pnPosicion int. Posicion del elemento en la cadena. Comienza a contar desde 1.
     * @return String. Valor del elemento buscado.
     */
    public synchronized String elemento(String pcTexto, int pnPosicion)
    {
        int i=1, vnCont=1, j=1;
        String vcValor = new String("");

        try {
            if (pnPosicion > 1) {
                while (vnCont<pnPosicion && i>0) {
                    i = pcTexto.indexOf(",", i);
                    i++;
                    vnCont++;
                }
                j = pcTexto.indexOf(",", i);
                if (j>0 & j>i)
                    vcValor = pcTexto.substring(i, j);
            }
            else {
                i = pcTexto.indexOf(",");
                if (i>0)
                    vcValor = pcTexto.substring(0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcValor;
    }

    /**
     * Crea una copia del objeto actual.
     * @return SentenciaNMEA.
     */
    public SentenciaNMEA copia () {
        SentenciaNMEA voResul = new SentenciaNMEA();
        voResul.setCAltura(this.cAltura);
        voResul.setCDatum(this.cDatum);
        voResul.setCFix(this.cFix);
        voResul.setCHdop(this.cHdop);
        voResul.setCHemisferio(this.cHemisferio);
        voResul.setCHora(this.cHora);
        voResul.setCLatitud(this.cLatitud);
        voResul.setCLongitud(this.cLongitud);
        voResul.setCMeridiano(this.cMeridiano);
        voResul.setCSatelites(this.cSatelites);
        voResul.setCSentencia(this.cSentencia);
        voResul.nOk = this.nOk;

        return voResul;
    }

}

