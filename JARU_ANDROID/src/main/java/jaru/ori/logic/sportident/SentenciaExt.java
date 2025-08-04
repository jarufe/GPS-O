/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jaru.ori.logic.sportident;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Clase que procesa sentencias Sportident del tipo 0xD3: pinza en estación
 * con protocolo extendido y con el modo Autosend activado, para pinzas de cualquier tipo
 * @author JAVI
 */
public class SentenciaExt extends SentenciaSI implements java.io.Serializable {

    public SentenciaExt() {
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
            if (comprobarCRC(paEntrada, 16, 17, 15)) {
                //También se tiene que comprobar si la sentencia es la 0xD3
                if (paEntrada[1]==(byte)0xD3) {
                    //Obtiene el número de estación
                    int vnEstacion = 0;
                    int vnCN1 = 0;
                    //Si el bit 7 está a 1, se trata de una 0xD3 especial
                    //que consiste en que una SIAC está enviando contenido vía radio
                    int vnEspecial = (int)(paEntrada[3]&0x80);
                    if (vnEspecial<=0)
                        vnCN1 = (int)(paEntrada[3]&0xFF);
                    else
                        vnCN1 = (int)(paEntrada[3]&0x01);
                    int vnCN0 = (int)(paEntrada[4]&0xFF);
                    vnEstacion = (vnCN1*256)+vnCN0;
                    super.setcEstacion(Integer.toString(vnEstacion));
                    //Obtiene el número de pinza
                    int vnPinza = 0;
                    int vnSN3 = (int)(paEntrada[5]&0xFF);
                    int vnSN2 = (int)(paEntrada[6]&0xFF);
                    int vnSN1 = (int)(paEntrada[7]&0xFF);
                    int vnSN0 = (int)(paEntrada[8]&0xFF);
                    if (vnSN3==0) {
                        if (vnSN2<=1) {
                            vnPinza = (vnSN1*256)+vnSN0;
                        } else if (vnSN2<=4) {
                            vnPinza = (100000*vnSN2)+((vnSN1*256)+vnSN0);
                        } else {
                            vnPinza = (vnSN2*65536)+(vnSN1*256)+vnSN0;
                        }
                    } else {
                        vnPinza = (vnSN2*65536)+(vnSN1*256)+vnSN0;
                    }
                    super.setcSiCard(vnPinza+"");
                    //Obtiene el flag de AM/PM (byte 9, bit 0)
                    vnAMPM = (int)(paEntrada[9]&0x01);
                    //Obtiene la hora en formato 12h
                    int vnHoraGlobal = (int)((paEntrada[11]&0xFF) + (256*(paEntrada[10]&0xFF)));
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
                    super.setcFechaPaso("");
                    super.setcHoraPaso(vcCeroH + vnHoras + ":" + vcCeroM + vnMinutos + ":" + vcCeroS + vnSegundos);
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
        byte[] vaSentencia = new byte[]{(byte)0x02, (byte)0xD3, (byte)0x0D,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
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
            vaSentencia[3] = (byte)((Integer.parseInt(pcEstacion)>>8)&0xFF);
            vaSentencia[4] = (byte)(Integer.parseInt(pcEstacion)&0x00FF);
            //Valor de la serie de la pinza y del número de pinza
            int vnValor = Integer.parseInt(pcSiCard);
            int vnValorParcial = 0;
            int vnSerie = 0;
            int vnSerieAnt = 0;
            int vnSN3 = 0;
            int vnSN2 = 0;
            int vnSN1 = 0;
            int vnSN0 = 0;
            if ((vnValor<1000000) || (vnValor>=16711680 && vnValor<=16777215)) {
                vnSerie = 0;
                if (vnValor<=65000) {
                    vnValorParcial = vnValor;
                    vnSerieAnt = 1;
                } else if (vnValor<=265000) {
                    vnValorParcial = vnValor-200000;
                    vnSerieAnt = 2;
                } else if (vnValor<=365000) {
                    vnValorParcial = vnValor-300000;
                    vnSerieAnt = 3;
                } else if (vnValor<=465000) {
                    vnValorParcial = vnValor-400000;
                    vnSerieAnt = 4;
                } else if (vnValor<=1000000) {
                    vnValorParcial = vnValor;
                } else if (vnValor>=16711680 && vnValor<=16777215) {
                    vnValorParcial = vnValor;
                }
            } else if (vnValor>=1000000 && vnValor<=1999999) {
                vnValorParcial = vnValor;
                vnSerie = 1;
            } else if (vnValor>=2000000 && vnValor<=2999999) {
                vnValorParcial = vnValor;
                vnSerie = 2;
            } else if (vnValor>=4000000 && vnValor<=4999999) {
                vnValorParcial = vnValor;
                vnSerie = 4;
            } else if (vnValor>=6000000 && vnValor<=6999999) {
                vnValorParcial = vnValor;
                vnSerie = 6;
            } else if (vnValor>=7000000 && vnValor<=9999999) {
                vnValorParcial = vnValor;
                vnSerie = 15;
            } else if (vnValor>=14000000 && vnValor<=14999999) {
                vnValorParcial = vnValor;
                vnSerie = 14;
            }
            vnSN2 = (int)(vnValorParcial/65536);
            vnSN1 = (int)((vnValorParcial%65536)/256);
            vnSN0 = (int)((vnValorParcial%65536)%256);
            if (vnValor>=500000) {
                vnSerieAnt = vnSN2;
            }
            vnSN3 = vnSerie;
            vnSN2 = vnSerieAnt;
            vaSentencia[5] = (byte)(vnSN3&0xFF);
            vaSentencia[6] = (byte)(vnSN2&0xFF);
            vaSentencia[7] = (byte)(vnSN1&0xFF);
            vaSentencia[8] = (byte)(vnSN0&0xFF);
            //Calcula el valor de la hora como segundos
            int vnHoras = Integer.parseInt(vcHora.substring(0, 2));
            int vnMinutos = Integer.parseInt(vcHora.substring(3, 5));
            int vnSegundos = Integer.parseInt(vcHora.substring(6));
            vnHora = (vnHoras*3600) + (vnMinutos*60) + vnSegundos;
            vaSentencia[10] = (byte)((vnHora>>8)&0xFF);
            vaSentencia[11] = (byte)(vnHora&0xFF);
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
            vaSentencia[9] = (byte)(vaSentencia[9]|vaDiaSemana[vnDiaSemana]);
            //vaSentencia[9] = (byte)(vaSentencia[9]|vaSemanaMes[vnSemanaMes]);
            //Escribe la parte de la hora y fecha
            if (vnAMPM==1)
                vaSentencia[9] = (byte)(vaSentencia[9]|(byte)0x01);
            //Valor del CRC
            byte[] vaSemi = new byte[15];
            for (int i=0; i<15; i++)
                vaSemi[i] = vaSentencia[i+1];
            int vnCRC = CRCCalculator.crc(vaSemi);
            vaSentencia[16] = (byte)((vnCRC>>8)&0xFF);
            vaSentencia[17] = (byte)(vnCRC&0xFF);
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
