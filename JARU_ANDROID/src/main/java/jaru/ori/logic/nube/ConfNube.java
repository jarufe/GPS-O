package jaru.ori.logic.nube;

/**
 * Clase que contiene los parámetros de configuración de la recogida de nube de puntos.
 * <P>
 * Se almacenan las coordenadas de comienzo o centro del área de interés (Easting, Northing y Zona UTM),
 * el tamaño en metros de la rejilla, tanto vertical como horizontal, el tamaño
 * de cada celda de la rejilla (en metros) y el intervalo en mseg de recogida
 * de datos.
 * <BR>
 * La recogida de datos la inicia el usuario de forma manual y se trata de un
 * hilo de ejecución independiente que va realizando lecturas en el GPS. Para
 * cada coordenada leida se calcula en qué posición de la rejilla va y se acumula
 * el valor de la altura y un contador de lecturas. Esto permite realizar el 
 * cálculo final de la altura de cada celda de la rejilla como una media de las
 * lecturas acumuladas.
 * </P>
 * @author jarufe
 */
public class ConfNube {
    public String cEasting;
    public String cNorthing;
    public int nZona;
    public int nTamX;
    public int nTamY;
    public int nIntervalo;
    public int nTick;
    public boolean bEsqSupIzq;
    /**
     * Constructor por defecto de la clase.
     *
     */
    public ConfNube() {
        cEasting = "548000";
        cNorthing = "4804000";
        nZona = 29;
        nTamX = 0;
        nTamY = 0;
        nIntervalo = 0;
        nTick = 1000;
        bEsqSupIzq = true;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcEasting String. Coordenada X sup-izq o central (según el valor de bEsqSupIzq).
     * @param pcNorthing String. Coordenada Y sup-izq o central (según el valor de bEsqSupIzq).
     * @param pnZona int. Número de zona UTM de la coordenada.
     * @param pnTamX int. Tamaño horizontal de la nube, en metros.
     * @param pnTamY int. Tamaño vertical de la nube, en metros.
     * @param pnIntervalo int. Tamaño de cada celda de la nube, tanto en vertical como en horizontal, en metros.
     * @param pnTick int. Intervalo en mseg entre lecturas del GPS.
     * @param pbEsqSupIzq boolean. Indica si la coordenada inicial se refiere a la esquina superior izquierda o central de la nube.
     */
    public ConfNube(String pcEasting, String pcNorthing, int pnZona, int pnTamX, int pnTamY,
                    int pnIntervalo, int pnTick, boolean pbEsqSupIzq) {
        cEasting = pcEasting;
        cNorthing = pcNorthing;
        nZona = pnZona;
        nTamX = pnTamX;
        nTamY = pnTamY;
        nIntervalo = pnIntervalo;
        nTick = pnTick;
        bEsqSupIzq = pbEsqSupIzq;
    }
    /**
     * Devuelve la coordenada X (UTM, WGS84) del punto sup-izq o central de la nube (según bEsqSupIzq).
     * @return String.
     */
    public String getCEasting () {
        return cEasting;
    }
    /**
     * Devuelve la coordenada (UTM, WGS84) del punto sup-izq  o central de la nube (según bEsqSupIzq).
     * @return String.
     */
    public String getCNorthing () {
        return cNorthing;
    }
    /**
     * Devuelve la Zona (UTM, WGS84) del punto sup-izq  o central de la nube (según bEsqSupIzq).
     * @return int.
     */
    public int getNZona () {
        return nZona;
    }
    /**
     * Devuelve el tamaño horizontal, en metros, de la nube.
     * @return int.
     */
    public int getNTamX () {
        return nTamX;
    }
    /**
     * Devuelve el tamaño vertical, en metros, de la nube.
     * @return int.
     */
    public int getNTamY () {
        return nTamY;
    }
    /**
     * Devuelve el tamaño, en metros, de cada celda de la nube, tanto vertical como horizontal.
     * @return int.
     */
    public int getNIntervalo () {
        return nIntervalo;
    }
    /**
     * Devuelve el intervalo en mseg utilizado para leer periódicamente una posición del GPS.
     * @return int.
     */
    public int getNTick () {
        return nTick;
    }
    /**
     * Devuelve el valor de la propiedad que indica si se quiere utilizar como coordenada de referencia
     * de la nube la esquina superior izquierda o la posición central de la nube.
     * @return boolean.
     */
    public boolean getBEsqSupIzq () {
        return bEsqSupIzq;
    }
    /**
     * Establece la coordenada X (UTM, WGS84) del punto sup-izq o central de la nube (según bEsqSupIzq).
     * @param pcValor String.
     */
    public void setCEasting (String pcValor) {
        cEasting = pcValor;
    }
    /**
     * Establece la coordenada Y (UTM, WGS84) del punto sup-izq o central de la nube (según bEsqSupIzq).
     * @param pcValor String.
     */
    public void setCNorthing (String pcValor) {
        cNorthing = pcValor;
    }
    /**
     * Establece la Zona (UTM, WGS84) del punto sup-izq  o central de la nube (según bEsqSupIzq).
     * @param pnValor int.
     */
    public void setNZona (int pnValor) {
        nZona = pnValor;
    }
    /**
     * Establece el tamaño horizontal, en metros, de la nube.
     * @param pnValor int.
     */
    public void setNTamX (int pnValor) {
        nTamX = pnValor;
    }
    /**
     * Establece el tamaño vertical, en metros, de la nube.
     * @param pnValor int.
     */
    public void setNTamY (int pnValor) {
        nTamY = pnValor;
    }
    /**
     * Establece el tamaño, en metros, de cada celda de la nube, tanto vertical como horizontal.
     * @param pnValor int.
     */
    public void setNIntervalo (int pnValor) {
        nIntervalo = pnValor;
    }
    /**
     * Establece el intervalo en mseg utilizado para leer periódicamente una posición del GPS.
     * @param pnValor int.
     */
    public void setNTick (int pnValor) {
        nTick = pnValor;
    }
    /**
     * Establece el valor de la propiedad que indica si se quiere utilizar como coordenada de referencia
     * de la nube la esquina superior izquierda o la posición central de la nube.
     * @param pbValor boolean.
     */
    public void setBEsqSupIzq (boolean pbValor) {
        bEsqSupIzq = pbValor;
    }


}

