package jaru.ori.logic.gpslog;

import jaru.ori.utils.Utilidades;

/**
 * Clase que encapsula la operativa para poder transformar los datos de coordenadas 
 * que se reciben desde el GPS.
 * <P>
 * Con esta clase se pueden realizar varias acciones. Por un lado, se pueden 
 * realizar transformaciones para convertir los datos del formato en que vienen 
 * a través del protocolo NMEA a un formato numérico o bien a una transformación
 * del tipo "gg:mm:ss.sss".<BR>
 * Por otro lado, esta clase permite ir almacenando sucesivas lecturas en una 
 * posición concreta para calcular un centroide que represente un valor medio de todas 
 * las lecturas. Así se podrá reducir o promediar el error de lecturas individuales 
 * actuando sobre un conjunto amplio de lecturas.
 * </P>
 * @author jarufe
 */
public class TransfGeografica {
    public long[][] nDatos;           //Lecturas realizadas
    public long[][] nCentro;          //Valor del centroide de las lecturas para el máximo de satélites
    public long[][] nCentroGlobal;    //Valor del centroide para todas las lecturas, independientemente de número de satélites
    public int[] nSatelites;          //Número de satélites asociado a cada lectura
    public int nMaxSatelites;         //Número máximo de satélites
    public long nCont;
    private final int nTam = Utilidades.getNLecturasNMEA();
    public long nMinX, nMaxX, nMinY, nMaxY;
    /**
     * Constructor por defecto de la clase. Inicializa las estructuras de datos que
     * permiten realizar los cálculos y transformaciones.
     *
     */
    public TransfGeografica () {
        nDatos = new long[nTam][3];
        nCentro = new long[1][3];
        nCentroGlobal = new long[1][3];
        nSatelites = new int[nTam];
        nMaxSatelites = 0;
        nMinX = 999999999;
        nMaxX = -999999999;
        nMinY = 999999999;
        nMaxY = -999999999;
        int i=0;
        try {
            for (i=0; i<nTam; i++)
            {
                nDatos[i][0] = -9999;
                nDatos[i][1] = -9999;
                nDatos[i][2] = -9999;
                nSatelites[i] = 0;
            }
            nCentro[0][0] = -9999;
            nCentro[0][1] = -9999;
            nCentro[0][2] = -9999;
            nCentroGlobal[0][0] = -9999;
            nCentroGlobal[0][1] = -9999;
            nCentroGlobal[0][2] = -9999;
            nCont = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Reinicia las estructuras de datos utilizadas para los cálculos.
     */
    public synchronized void reinicia () {
        int i=0;
        nMaxSatelites = 0;
        nMinX = 999999999;
        nMaxX = -999999999;
        nMinY = 999999999;
        nMaxY = -999999999;
        try {
            for (i=0; i<nTam; i++)
            {
                nDatos[i][0] = -9999;
                nDatos[i][1] = -9999;
                nDatos[i][2] = -9999;
                nSatelites[i] = 0;
            }
            nCentro[0][0] = -9999;
            nCentro[0][1] = -9999;
            nCentro[0][2] = -9999;
            nCentroGlobal[0][0] = -9999;
            nCentroGlobal[0][1] = -9999;
            nCentroGlobal[0][2] = -9999;
            nCont = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Calcula el punto que sirve como centroide de una lista de puntos.
     */
    public synchronized void centroide() {
        int i = 0, j = 0, k = 0;
        long vnX = 0, vnY = 0, vnZ = 0;
        long vnXGlobal = 0, vnYGlobal = 0, vnZGlobal = 0;

        try {
            i=0;
            while (i<nTam && nDatos[i][0]!=-9999) {
                if (nSatelites[i]==nMaxSatelites || nMaxSatelites==0) {
                    vnX = vnX + nDatos[i][0];
                    vnY = vnY + nDatos[i][1];
                    vnZ = vnZ + nDatos[i][2];
                    j++;
                }
                vnXGlobal = vnXGlobal + nDatos[i][0];
                vnYGlobal = vnYGlobal + nDatos[i][1];
                vnZGlobal = vnZGlobal + nDatos[i][2];
                k++;
                i++;
            }
            if (j>0) {
                nCentro[0][0] = vnX / j;
                nCentro[0][1] = vnY / j;
                nCentro[0][2] = vnZ / j;
            }
            else {
                nCentro[0][0] = -9999;
                nCentro[0][1] = -9999;
                nCentro[0][2] = -9999;
            }
            if (k>0) {
                nCentroGlobal[0][0] = vnXGlobal / k;
                nCentroGlobal[0][1] = vnYGlobal / k;
                nCentroGlobal[0][2] = vnZGlobal / k;
            }
            else {
                nCentroGlobal[0][0] = -9999;
                nCentroGlobal[0][1] = -9999;
                nCentroGlobal[0][2] = -9999;
            }
        } catch (Exception e) {
            nCentro[0][0] = -9999;
            nCentro[0][1] = -9999;
            nCentro[0][2] = -9999;
            e.printStackTrace();
        }

    }
    /**
     * Dado un nuevo punto, actualiza los valores extremos para el posterior dibujado gráfico.
     * @return void.
     */
    public synchronized void actualizarExtremos(long pnX, long pnY, int pnSatelites) {
        try {
            if (pnX<nMinX) {
                nMinX = pnX;
            }
            if (pnX>nMaxX) {
                nMaxX = pnX;
            }
            if (pnY<nMinY) {
                nMinY = pnY;
            }
            if (pnY>nMaxY) {
                nMaxY = pnY;
            }
            if (pnSatelites>nMaxSatelites)
                nMaxSatelites = pnSatelites;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Calcula el valor mínimo de la coordenada X de entre los datos existentes
     * @return long. Valor mínimo de X.
     */
    public synchronized long minimoX() {
        int i = 0;
        long vnMinX = -9999;

        try {
            vnMinX = nDatos[0][0];
            i=0;
            while (i<nTam && nDatos[i][0]!=-9999) {
                if (nDatos[i][0]<vnMinX) {
                    vnMinX = nDatos[i][0];
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vnMinX;
    }
    /**
     * Calcula el valor máximo de la coordenada X de entre los datos existentes
     * @return long. Valor máximo de X.
     */
    public synchronized long maximoX() {
        int i = 0;
        long vnMaxX = -9999;

        try {
            vnMaxX = nDatos[0][0];
            i=0;
            while (i<nTam && nDatos[i][0]!=-9999) {
                if (nDatos[i][0]>vnMaxX) {
                    vnMaxX = nDatos[i][0];
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vnMaxX;
    }
    /**
     * Calcula el valor mínimo de la coordenada Y de entre los datos existentes
     * @return long. Valor mínimo de Y.
     */
    public synchronized long minimoY() {
        int i = 0;
        long vnMinY = -9999;

        try {
            vnMinY = nDatos[0][1];
            i=0;
            while (i<nTam && nDatos[i][1]!=-9999) {
                if (nDatos[i][1]<vnMinY) {
                    vnMinY = nDatos[i][1];
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vnMinY;
    }
    /**
     * Calcula el valor maximo de la coordenada Y de entre los datos existentes
     * @return long. Valor máximo de Y.
     */
    public synchronized long maximoY() {
        int i = 0;
        long vnMaxY = -9999;

        try {
            vnMaxY = nDatos[0][1];
            i=0;
            while (i<nTam && nDatos[i][1]!=-9999) {
                if (nDatos[i][1]>vnMaxY) {
                    vnMaxY = nDatos[i][1];
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vnMaxY;
    }
    /**
     * Calcula el valor máximo de satélites tomados en las lecturas individuales
     * @return int. Valor máximo de satélites.
     */
    public synchronized int maximoSatelites() {
        int i = 0;
        int vnMax = 0;

        try {
            vnMax = nSatelites[0];
            i=0;
            while (i<nTam && nDatos[i][0]!=-9999) {
                if (nSatelites[i]>vnMax) {
                    vnMax = nSatelites[i];
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vnMax;
    }
    /**
     * Genera un valor long a partir de un dato que viene en forma de cadena.
     * <P>
     * La forma del dato de entrada es mmmm.nnnn
     * </P>
     * @param pcTexto String. Texto que contiene un valor numérico.
     * @return long. Transformación del dato de entrada en valor numérico de salida.
     */
    public synchronized long obtieneLong (String pcTexto) {
        int i=0;
        Integer vnEntero = new Integer(0);
        Integer vnDecimal = new Integer(0);
        int vnTam=0;
        long vnLong=0;

        try {
            if (pcTexto.length() > 0) {
                i = pcTexto.indexOf('.');
                if (i>0)
                {
                    vnEntero = Integer.valueOf(pcTexto.substring(0, i));
                    vnDecimal = Integer.valueOf(pcTexto.substring(i+1));
                    vnTam = (pcTexto.length() - (i + 1));
                    Utilidades.setNDecimalesNMEA(vnTam);
                    vnLong = vnEntero.intValue()*pow(10,vnTam) + vnDecimal.intValue();
                }
                else
                {
                    vnEntero = Integer.valueOf(pcTexto);
                    vnLong = vnEntero.intValue();
                    Utilidades.setNDecimalesNMEA(0);
                }
            }
            else
                vnLong = -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vnLong;
    }
    /**
     * Genera un texto a partir de un valor numérico de entrada.
     * <P>
     * El valor numérico de entrada es un long, y lo convierte a una cadena que representa a un valor
     * decimal en la forma mmmm.nnnn
     * </P>
     * @param pnValor long. Valor de entrada.
     * @return String. Texto equivalente de salida.
     */
    public synchronized String obtieneCadena (long pnValor) {
        int i=0;
        int vnTam=0;
        String vcTexto = "";

        try {
            vcTexto = "" + pnValor;
            vnTam = vcTexto.length() - Utilidades.getNDecimalesNMEA();
            if (vnTam > 0)
                vcTexto = vcTexto.substring(0, vnTam) + "." + vcTexto.substring(vnTam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcTexto;
    }
    /**
     * Calcula la potencia "b" de un número "a".
     * @param a long. Valor.
     * @param b long. Potencia.
     * @return long. Resultado de "a" elevado a "b".
     */
    private synchronized long pow(long a, long b) {
        long vnResul = 1;

        try {
            for (int i=1; i<=b; i++)
                vnResul = vnResul * a;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vnResul;
    }
    /**
     * Realiza la transformación de un texto de entrada con un valor de coordenada, en otro texto con formato
     * "gg:mm:ss.sss"
     * @param pcTexto String. Texto de entrada con un valor de coordenada en formato "mmmm.nnnn"
     * @return String. Coordenada transformada.
     */
    public synchronized String transfCoord (String pcTexto) {
        int i=0;
        Integer vnDecimal = new Integer(0);
        String vcCoord = new String("00:00:00.000");
        String vcSeg = new String("");
        int vnTamDec = 0;
        double vnSegundos = 0;

        try {
            if (pcTexto.length() > 5 && !pcTexto.equals("-.9999") && !pcTexto.equals(".9999")) {
                i = pcTexto.indexOf('.');
                if (i>0)
                {
                    vcCoord = pcTexto.substring(0, i-2) + ":" +
                            pcTexto.substring(i-2, i);
                    vnDecimal = Integer.valueOf(pcTexto.substring(i+1));
                    vnTamDec = pcTexto.substring(i+1).length();
                    vnSegundos = (vnDecimal.doubleValue() * 60.0) / (pow(10,vnTamDec));
                    if (vnSegundos<10)
                        vcSeg = "0" + vnSegundos;
                    else
                        vcSeg = "" + vnSegundos;
                    if (vcSeg.length()>6)
                        vcSeg = vcSeg.substring(0, 6);
                    vcCoord = vcCoord + ":" + vcSeg.substring(0, 2) + "." + vcSeg.substring(3);
                }
                else
                {
                    vcCoord = "00:00:00.000";
                }
            }
            else
                vcCoord = "00:00:00.000";
        } catch (Exception e) {
            System.out.println ("Error transformando: " + pcTexto);
            //e.printStackTrace();
        }
        return vcCoord;
    }
    /**
     * Realiza la transformación de un texto de entrada con un valor de coordenada, en otro texto con formato
     * "gg.ggggg"
     * @param pcTexto String. Texto de entrada con un valor de coordenada en formato "ddmm.nnnn"
     * @return String. Coordenada transformada.
     */
    public synchronized String transfCoordAGrados (String pcTexto) {
        int i=0;
        Integer vnDecimal = new Integer(0);
        String vcCoord = new String("00.000");
        String vcSeg = new String("");
        double vnGrados = 0;
        double vnMinutos = 0;
        double vnSegundos = 0;

        try {
            if (pcTexto.length() > 5 && !pcTexto.equals("-.9999") && !pcTexto.equals(".9999")) {
                i = pcTexto.indexOf('.');
                if (i>0)
                {
                    vnGrados = Double.parseDouble(pcTexto.substring(0, i-2));
                    vnMinutos = Double.parseDouble(pcTexto.substring(i-2, i));
                    vnSegundos = (Double.parseDouble(pcTexto.substring(i)))*60.0;
                    vnGrados = vnGrados + (vnMinutos / 60.0) + (vnSegundos / 3600.0);
                    vcCoord = vnGrados + "";
                }
                else
                {
                    vcCoord = "00.000";
                }
            }
            else
                vcCoord = "00.000";
        } catch (Exception e) {
            System.out.println ("Error transformando: " + pcTexto);
            //e.printStackTrace();
        }
        return vcCoord;
    }
    /**
     * Realiza la transformación de un texto de entrada con un valor de coordenada en gg.ggggg,
     * en un texto con formato al estilo NMEA ddmm.nnnn
     * @param pnGrados double. Grados en gg.ggggg
     * @return String. Texto de salida con un valor de coordenada en formato "ddmm.nnnn"
     */
    public synchronized String transfGradosACoord (double pnGrados) {
        int i=0;
        String vcCoord = new String("00.000");
        double vnGrados = 0;
        double vnMinutos = 0;
        String vcTexto = "";
        String vcMinutos = "";

        try {
            //Pone el número en positivo. No interesa el signo porque NMEA usa una letra para N, S, E, W
            if (pnGrados<0)
                pnGrados = pnGrados * -1;
            //Convierte a texto para desglosar las partes del valor
            vcTexto = pnGrados + "";
            i = vcTexto.indexOf('.');
            if (i>0) {
                vnGrados = Double.parseDouble(vcTexto.substring(0, i));
                vnMinutos = Double.parseDouble(vcTexto.substring(i)) * 60.0;
                if (vnMinutos<10)
                    vcMinutos = "0" + vnMinutos;
                else
                    vcMinutos = "" + vnMinutos;
                if (vcMinutos.length()>7)
                    vcMinutos = vcMinutos.substring(0, 7);
                vcCoord = vcTexto.substring(0, i) + "" + vcMinutos;
            }
            else {
                vcCoord = vcTexto;
            }
        } catch (Exception e) {
            System.out.println ("Error transformando: " + pnGrados);
            //e.printStackTrace();
        }
        return vcCoord;
    }

}

