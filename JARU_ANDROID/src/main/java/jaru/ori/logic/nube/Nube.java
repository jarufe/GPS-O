package jaru.ori.logic.nube;

/**
 * Clase que encapsula los dos elementos importantes de una nube de puntos.
 * <P>
 * Los dos elementos son la configuración de la nube y los datos de la propia nube.
 * <BR>
 * El primero es un objeto de la clase ConfNube. El segundo es una matriz que contiene
 * dos elementos: sumatorio de cotas leidas desde el GPS y la cuenta de dichas lecturas.
 * </P>
 * @author jarufe
 */
public class Nube {
    public ConfNube oConfNube;
    public int[][] aNube;
    /**
     * Constructor por defecto de la clase.
     *
     */
    public Nube() {
        oConfNube = new ConfNube();
        aNube = null;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param poConfNube ConfNube. Objeto con los parámetros de configuración de la nube.
     * @param paNube int[][]. Datos de la nube.
     */
    public Nube(ConfNube poConfNube, int[][] paNube) {
        aNube = paNube;
        oConfNube = poConfNube;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param poConfNube ConfNube. Objeto con los parámetros de configuración de la nube.
     */
    public Nube(ConfNube poConfNube) {
        oConfNube = poConfNube;
    }
    /**
     * Devuelve el objeto de configuración.
     * @return ConfNube.
     */
    public ConfNube getOConfNube () {
        return oConfNube;
    }
    /**
     * Devuelve la matriz de datos de la nube.
     * @return int[][].
     */
    public int[][] getANube () {
        return aNube;
    }
    /**
     * Establece el objeto de configuración.
     * @param poValor ConfNube.
     */
    public void setOConfNube (ConfNube poValor) {
        oConfNube = poValor;
    }
    /**
     * Establece la matriz de datos de la nube.
     * @param paValor int[][].
     */
    public void setANube (int[][] paValor) {
        aNube = paValor;
    }


}

