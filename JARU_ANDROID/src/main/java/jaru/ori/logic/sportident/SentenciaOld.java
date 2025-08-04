/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jaru.ori.logic.sportident;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Clase que procesa sentencias Sportident del tipo 0x53: pinza en estación
 * con el modo Autosend activado, para pinzas de tipo SiCard 5
 * @author JAVI
 */
public class SentenciaOld extends SentenciaSI implements java.io.Serializable {

    public SentenciaOld() {
        super();
    }

    /**
     * Método que procesa la entrada de una sentencia de lectura de pinza cuando
     * una estación está en modo AutoSend
     * @param paEntrada byte[] Sentencia leída de la estación
     * @return boolean Estado del procesamiento
     */
    @Override
    public boolean procesarSentencia (byte[] paEntrada) {
        boolean vbResul = true;
        int vnAMPM = 0;
        try {
            //Antes de continuar, se comprueba el CRC de la sentencia
            if (comprobarCRC(paEntrada, 11, 12, 10)) {
                //También se tiene que comprobar si la sentencia es la 0x53
                if (paEntrada[1]==(byte)0x53) {
                    //Obtiene el número de estación
                    int vnEstacion = (int)(paEntrada[3]&0xFF);
                    super.setcEstacion(Integer.toString(vnEstacion));
                    //Obtiene el número de pinza
                    int vnPinza = (int)((paEntrada[6]&0xFF) + (256*(paEntrada[5]&0xFF)));
                    String vcCeros = "";
                    if (vnPinza<10000) {
                        vcCeros = "0";
                        if (vnPinza<1000)
                            vcCeros = "00";
                        if (vnPinza<100)
                            vcCeros = "000";
                        if (vnPinza<10)
                            vcCeros = "0000";
                    }
                    super.setcSiCard(Integer.toString(paEntrada[4]&0xFF) + vcCeros + Integer.toString(vnPinza));
                    //Obtiene el flag de AM/PM (byte 2, bit 0)
                    vnAMPM = (int)(paEntrada[2]&0x01);
                    //Obtiene la hora en formato 12h
                    int vnHoraGlobal = (int)((paEntrada[9]&0xFF) + (256*(paEntrada[8]&0xFF)));
                    String vcCeroH = "";
                    String vcCeroM = "";
                    String vcCeroS = "";
                    int vnHoras = (int)vnHoraGlobal/3600;
                    //Si estamos en PM, suma 12 horas
                    vnHoras = vnHoras + (vnAMPM*12);
                    int vnMinutos = (int)(vnHoraGlobal%3600)/60;
                    int vnSegundos = (int)(vnHoraGlobal%3600)%60;
                    if (vnHoras<10)
                        vcCeroH = "0";
                    if (vnMinutos<10)
                        vcCeroM = "0";
                    if (vnSegundos<10)
                        vcCeroS = "0";
                    super.setcHoraPaso(vcCeroH + vnHoras + ":" + vcCeroM + vnMinutos + ":" + vcCeroS + vnSegundos);
                    super.setcFechaPaso("");
                    super.setnTipoId(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            vbResul = false;
        }
        return vbResul;
    }
    /**
     * Método que dados los valores de Estación, SiCard
     * crea la sentencia en el formato propio de Sportident
     * La fecha y hora se toma de la actual del sistema
     * @param pcEstacion String. Número de estación
     * @param pcSiCard String. Número de pinza electrónica
     * @return
     */
    @Override
    public byte[] crearSentencia (String pcEstacion, String pcSiCard) {
        try {
            super.setcEstacion(pcEstacion);
            super.setcSiCard(pcSiCard);
            //Valor de la hora actual
            java.util.Calendar voHora = java.util.Calendar.getInstance();
            int vnHoras = voHora.get(java.util.Calendar.HOUR);
            int vnMinutos = voHora.get(java.util.Calendar.MINUTE);
            int vnSegundos = voHora.get(java.util.Calendar.SECOND);
            String vcFecha = format(voHora.getTime(), "yyyy-MM-dd");
            //int vnHora = (vnHoras*3600) + (vnMinutos*60) + vnSegundos;
            String vcCeroH = "";
            String vcCeroM = "";
            String vcCeroS = "";
            if (vnHoras<10)
                vcCeroH = "0";
            if (vnMinutos<10)
                vcCeroM = "0";
            if (vnSegundos<10)
                vcCeroS = "0";
            super.setcHoraPaso(vcCeroH + vnHoras + ":" + vcCeroM + vnMinutos + ":" + vcCeroS + vnSegundos);
            super.setcFechaPaso(vcFecha);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return crearSentencia(pcEstacion, pcSiCard, super.getcFechaPaso(),
                super.getcHoraPaso());
    }
    /**
     * Método que dados los valores de Estación, SiCard, fecha (yyyy-mm-dd)
     * y Hora de paso (HH:MM:SS) crea la sentencia en el formato propio de Sportident
     * @param pcEstacion String. Número de estación
     * @param pcSiCard String. Número de pinza electrónica
     * @param pcFecha String. Fecha en yyyy-mm-dd
     * @param pcHora String. Hora en formato HH:MM:SS
     * @return byte[] ristra de bytes en formato de lectura Sportident
     */
    @Override
    public byte[] crearSentencia (String pcEstacion, String pcSiCard,
                                  String pcFecha, String pcHora) {
        byte[] vaSentencia = new byte[]{(byte)0x02, (byte)0x53, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x03};
        int vnAMPM = 0;
        byte[] vaDiaSemana = new byte[]{(byte)0x00, (byte)0x02, (byte)0x04,
                (byte)0x06, (byte)0x08, (byte)0x0A, (byte)0x0C};
        //byte[] vaSemanaMes = new byte[]{(byte)0x00, (byte)0x10, (byte)0x20, (byte)0x30};
        try {
            super.setcEstacion(pcEstacion);
            super.setcSiCard(pcSiCard);
            super.setcFechaPaso(pcFecha);
            super.setcHoraPaso(pcHora);
            //Comprueba si la hora es mayor o igual a 12
            String vcHora = pcHora.substring(0, 2);
            int vnHora = Integer.parseInt(vcHora);
            if (vnHora>=12) {
                //En ese caso se pone el flag de AM/PM y se restan 12 horas
                //para dejarlo en un valor entre 0 y 11
                vnAMPM = 1;
                vnHora = vnHora - 12;
                //Se vuelve a componer la hora, añadiendo un 0 si necesario
                if (vnHora<10)
                    vcHora = "0" + vnHora;
                else
                    vcHora = "" + vnHora;
                vcHora = vcHora + pcHora.substring(2);
            } else {
                vcHora = pcHora;
            }
            //Valor del número de la estación
            vaSentencia[3] = (byte)(Integer.parseInt(pcEstacion)&0xFF);
            //Valor de la serie de la pinza y del número de pinza
            String vcPinza = pcSiCard;
            int vnSerie = 0;
            int vnValor = 0;
            if (vcPinza.length()<=5) {
                vnSerie = 1;
                vnValor = Integer.parseInt(vcPinza);
            } else {
                vnSerie = Integer.parseInt(vcPinza.substring(0, vcPinza.length()-5));
                vnValor = Integer.parseInt(vcPinza.substring(vcPinza.length()-5));
            }
            vaSentencia[4] = (byte)(vnSerie&0xFF);
            vaSentencia[5] = (byte)((vnValor>>8)&0xFF);
            vaSentencia[6] = (byte)(vnValor&0x00FF);
            //Calcula el valor de la hora como segundos
            int vnHoras = Integer.parseInt(vcHora.substring(0, 2));
            int vnMinutos = Integer.parseInt(vcHora.substring(3, 5));
            int vnSegundos = Integer.parseInt(vcHora.substring(6));
            vnHora = (vnHoras*3600) + (vnMinutos*60) + vnSegundos;
            vaSentencia[8] = (byte)((vnHora>>8)&0xFF);
            vaSentencia[9] = (byte)(vnHora&0xFF);
            //Obtiene el día de la semana
            Date voFecha = parse(pcFecha, "yyyy-MM-dd");
            java.util.Calendar voCalendar = java.util.Calendar.getInstance();
            voCalendar.setTimeInMillis(voFecha.getTime());
            //Calendar devuelve 1 a 7 de domingo a sábado. Hay que restar 1, para pasar de 0 a 6
            int vnDiaSemana=voCalendar.get(java.util.Calendar.DAY_OF_WEEK)-1;
            //Idem para semana del mes. Devuelve de 1 a 4. Hay que restar 1, para pasar de 0 a 3
            /*
            int vnDiaMes = voCalendar.get(java.util.Calendar.DAY_OF_MONTH);
            int vnSemanaMes = (vnDiaMes-1)/7;
            if (vnSemanaMes>3)
                vnSemanaMes = 3;
            */
            vaSentencia[2] = (byte)(vaSentencia[2]|vaDiaSemana[vnDiaSemana]);
            //vaSentencia[2] = (byte)(vaSentencia[2]|vaSemanaMes[vnSemanaMes]);
            //Escribe la parte de la hora y fecha
            if (vnAMPM==1)
                vaSentencia[2] = (byte)(vaSentencia[2]|(byte)0x01);
            //Valor del CRC
            byte[] vaSemi = new byte[10];
            for (int i=0; i<10; i++)
                vaSemi[i] = vaSentencia[i+1];
            int vnCRC = CRCCalculator.crc(vaSemi);
            vaSentencia[11] = (byte)((vnCRC>>8)&0xFF);
            vaSentencia[12] = (byte)(vnCRC&0xFF);
            super.setnTipoId(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vaSentencia;
    }

    /**
     * Dado un objeto de tipo fecha, que contiene un valor de fecha, y dado un formato
     * especificado en el segundo parámetro, este método se encarga de devolver una
     * cadena de caracteres con el valor de la fecha expresado en dicho formato.
     * @return java.lang.String
     * @param pdFecha java.util.Date
     * @param pcFormato java.lang.String
     * @exception Exception Excepción
     */
    public String format(Date pdFecha, String pcFormato) throws Exception {
        String vcResul = "";
        SimpleDateFormat df = new SimpleDateFormat (pcFormato);

        df.setLenient(false);
        vcResul = df.format(pdFecha);

        return vcResul;
    }
    /**
     * Dado un objeto de tipo cadena, que contiene un valor de fecha, y dado un formato
     * especificado en el segundo parámetro, este método se encarga de devolver un
     * objeto de tipo Date.
     * @return java.util.Date
     * @param pcFecha java.lang.String
     * @param pcFormato java.lang.String
     * @exception Exception Excepción
     */
    public Date parse(String pcFecha, String pcFormato) throws Exception {
        Date vdResul = null;
        SimpleDateFormat df = new SimpleDateFormat (pcFormato);

        df.setLenient(false);
        vdResul = df.parse(pcFecha);

        return vdResul;
    }

}
