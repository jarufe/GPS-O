/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jaru.ori.logic.sportident;

/**
 * Superclase abstracta que sirve como base para procesar sentencias sportident
 * de los tipos 0x53 (pinza SiCard 5 en estación con modo Autosend) y 0xD3
 * (cualquier tipo de pinza en estación con protocolo extendido y modo Autosend)
 * @author JAVI
 */
public abstract class SentenciaSI implements java.io.Serializable {
    private String cEstacion;
    private String cSiCard;
    private String cFechaPaso;
    private String cHoraPaso;
    private int nTipoId; //0=SiCard; 1=Dorsal corredor; 2=Dorsal equip(relevos)
    private int nBateria;
    private String cFechaHoraLectura;
    private byte[] aLectura;

    public SentenciaSI() {
        super();
        cEstacion = "";
        cSiCard = "";
        cFechaPaso = "";
        cHoraPaso = "";
        nTipoId = 0;
        int nBateria = 0;
        cFechaHoraLectura = "";
        aLectura = null;
    }

    public String getcEstacion() {
        return cEstacion;
    }

    public void setcEstacion(String cEstacion) {
        this.cEstacion = cEstacion;
    }

    public String getcSiCard() {
        return cSiCard;
    }

    public void setcSiCard(String cSiCard) {
        this.cSiCard = cSiCard;
    }

    public String getcFechaPaso() {
        return cFechaPaso;
    }

    public void setcFechaPaso(String cFechaPaso) {
        this.cFechaPaso = cFechaPaso;
    }

    public String getcHoraPaso() {
        return cHoraPaso;
    }

    public void setcHoraPaso(String cHoraPaso) {
        this.cHoraPaso = cHoraPaso;
    }

    public int getnTipoId() {
        return nTipoId;
    }

    public void setnTipoId(int nTipoId) {
        this.nTipoId = nTipoId;
    }

    public int getnBateria() {
        return nBateria;
    }

    public void setnBateria(int nBateria) {
        this.nBateria = nBateria;
    }

    public String getcFechaHoraLectura() {
        return cFechaHoraLectura;
    }

    public void setcFechaHoraLectura(String cFechaHoraLectura) {
        this.cFechaHoraLectura = cFechaHoraLectura;
    }

    public byte[] getaLectura() {
        return aLectura;
    }

    public void setaLectura(byte[] aLectura) {
        this.aLectura = aLectura;
    }



    /**
     * Método que limpia los valores de las propiedades de la clase
     */
    public void limpiarDatos() {
        cEstacion = "";
        cSiCard = "";
        cFechaPaso = "";
        cHoraPaso = "";
        nTipoId = 0;
        nBateria = 0;
        cFechaHoraLectura = "";
        aLectura = null;
    }
    /**
     * Dada la ristra de bytes guardada en la propiedad aLectura, convierte el
     * valor a una cadena de texto en hexadecimal
     * @return String
     */
    public String convertirRawHexa() {
        String vcResul = "";
        try {
            if (aLectura!=null) {
                for (int i=0; i < aLectura.length; i++) {
                    vcResul +=
                            Integer.toString( ( aLectura[i] & 0xff ) + 0x100, 16).substring( 1 );
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return vcResul;
    }
    /**
     * Pasa una ristra de bytes que se guarda en la propiedad aLectura y
     * convierte a una cadena de texto en hexadecimal
     * @param paLectura byte[]
     * @return String
     */
    public String convertirRawHexa(byte[] paLectura) {
        aLectura = paLectura;
        return convertirRawHexa();
    }

    /**
     * Método que procesa la entrada de una sentencia de lectura de pinza cuando
     * una estación está en modo AutoSend
     * @param paEntrada byte[] Sentencia leída de la estación
     * @return boolean Estado del procesamiento
     */
    public abstract boolean procesarSentencia (byte[] paEntrada);
    /**
     * Método que dados los valores de Estación, SiCard crea la sentencia en el formato propio de Sportident
     * El método toma automáticamente la hora (HH:MM:SS) del sistema
     * @param pcEstacion String. Número de estación
     * @param pcSiCard String. Número de pinza electrónica
     * @return byte[] Sentencia Sportident como ristra de bytes
     */
    public abstract byte[] crearSentencia (String pcEstacion, String pcSiCard);
    /**
     * Método que dados los valores de Estación, SiCard, fecha y hora crea la
     * sentencia en el formato propio de Sportident
     * @param pcEstacion String. Número de estación
     * @param pcSiCard String. Número de pinza electrónica
     * @param pcFecha String. Fecha (yyyy-mm-dd)
     * @param pcHora String. Hora (hh:mm:ss)
     * @return byte[] Sentencia Sportident como ristra de bytes
     */
    public abstract byte[] crearSentencia (String pcEstacion, String pcSiCard,
                                           String pcFecha, String pcHora);
    /**
     * Método que comprueba el CRC de una sentencia, comparando el CRC que llega
     * dentro de la sentencia con el cálculo del CRC a partir de los bytes leídos
     * @param paEntrada byte[] Sentencia leída
     * @param pnPos1 int Posición del primer byte correspondiente al CRC
     * @param pnPos2 int Posición del segundo byte correspondiente al CRC
     * @param pnTam int Número de bytes computables para el CRC
     * @return boolean
     */
    protected boolean comprobarCRC (byte[] paEntrada, int pnPos1, int pnPos2, int pnTam) {
        boolean vbResul = true;
        try {
            int vnCrcLeido = (int)(((paEntrada[pnPos1]&0xFF)<<8) + (paEntrada[pnPos2]&0xFF));
            //Calcula el CRC de la sentencia leída
            byte[] vaSemi = new byte[pnTam];
            for (int i=0; i<pnTam; i++)
                vaSemi[i] = paEntrada[i+1];
            int vnCrcCalculado = CRCCalculator.crc(vaSemi);
            //Si los 2 valores no coinciden, devuelve false
            if (vnCrcLeido!=vnCrcCalculado)
                vbResul = false;
        } catch (Exception e) {
            e.printStackTrace();
            vbResul = false;
        }
        return vbResul;
    }
}
