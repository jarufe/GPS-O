/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jaru.red.logic;

/**
 * Implementación para un hilo de ejecución que se encarga de realizar la transmisión/recepción
 * de datos a un servidor web
 * @author jarufe
 * @version 1.0
 */
public class HiloTransmisiones implements Runnable{
    private boolean bResulPreparado = false;
    private UploadRequestResponse oRespuesta = null;
    private UploadRequestResponse oEnvio = null;

    private Thread oThread;
    //private int nRetardo = 20000;

    /**
     * Constructor por defecto.
     */
    public HiloTransmisiones() {
    }

    public boolean isbResulPreparado() {
        return bResulPreparado;
    }

    public void setbResulPreparado(boolean bResulPreparado) {
        this.bResulPreparado = bResulPreparado;
    }

    public UploadRequestResponse getoRespuesta() {
        return oRespuesta;
    }

    public void setoRespuesta(UploadRequestResponse oRespuesta) {
        this.oRespuesta = oRespuesta;
    }

    public UploadRequestResponse getoEnvio() {
        return oEnvio;
    }

    public void setoEnvio(UploadRequestResponse oEnvio) {
        this.oEnvio = oEnvio;
    }

    /**
     * Método que gestiona la ejecución del hilo de refresco de las comunicaciones.
     */
    public void run() {
        if ((Thread.currentThread() == oThread)) {
            oRespuesta = GestionTransmisiones.transmitirOrden(oEnvio);
            bResulPreparado = true;
        }
    }
    /**
     * Comienzo de un hilo de ejecución que ejecuta la comunicación
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

}
