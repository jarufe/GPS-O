package jaru.gps.logic;


/**
 * Clase que representa un hilo de ejecución para realizar lecturas periódicas 
 * de un puerto serie.
 * <P>
 * La gestión del puerto serie se realiza a través de la clase PuertoSerie. 
 * En el momento en que el puerto se abre, el método correspondiente de esa clase 
 * hace una llamada para iniciar este hilo de ejecución. Desde ese momento,
 * el hilo se encarga de leer permanentemente el puerto de comunicaciones, 
 * a intervalos definidos, para ir descargando el buffer de entrada del puerto.
 * </P>
 * @author jarufe
 */
public class PuertoSerieTicker  implements Runnable {
    private static Thread oThread;
    private static int nRetardo = 10;

    /**
     * Constructor de la clase.
     */
    public PuertoSerieTicker () {
    }


    /**
     * Método que gestiona la ejecución del hilo de refresco de las comunicaciones.
     * Lee la siguiente sentencia NMEA.
     */
    public void run() {
        while (Thread.currentThread() == oThread) {
            try {
                if (PuertoSerie.getBAbierto()) {
                    String cTexto = PuertoSerie.recibir('$','*');
                    if (cTexto.length() > 0) {
                        PuertoSerie.getOSentencia().procesaSentencia(cTexto);
                    }
                }
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
     * Comienzo de un hilo de ejecución que permitirá leer el puerto de comunicaciones
     * de forma periódica.
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
        //oThread.stop();
        oThread = null;
    }

}
