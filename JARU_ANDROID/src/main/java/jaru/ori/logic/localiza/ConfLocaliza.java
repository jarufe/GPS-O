package jaru.ori.logic.localiza;

/**
 * Clase que contiene los parámetros de configuración de la parte de localización.
 * <P>
 * Aquí se guarda una serie de valores que no se tienen por qué utilizar todos:
 * evento, categoría, dorsal (para el caso de pruebas deportivas) y nombre<BR>
 * Por otro lado, se guardan los datos de acceso al servidor web para envío del posicionamiento:
 * servidor, puerto, servlet
 * </P>
 * @author jarufe
 */
public class ConfLocaliza {
    public int nEvento;
    public int nCategoria;
    public String cDorsal;
    public String cNombre;
    public String cServidor;
    public int nPuerto;
    public String cServlet;
    public int nRetardo; //Retardo de envío de comunicación en segundos
    /**
     * Constructor por defecto de la clase.
     *
     */
    public ConfLocaliza() {
        nEvento = -1;
        nCategoria = -1;
        cDorsal = "";
        cNombre = "";
        cServidor = "jaru.ignitiondomain.com";
        nPuerto = 80;
        cServlet = "LocalizacionController";
        nRetardo = 20;
    }
    /**
     * Constructor de la clase con parámetros del participante.
     * @param pnEvento int. Id del evento.
     * @param pnCategoria int. Id de la categoría.
     * @param pcDorsal String. Dorsal del participante.
     * @param pcNombre String. Nombre del participante.
     */
    public ConfLocaliza(int pnEvento, int pnCategoria, String pcDorsal, String pcNombre) {
        nEvento = pnEvento;
        nCategoria = pnCategoria;
        cDorsal = pcDorsal;
        cNombre = pcNombre;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcServidor String. Nombre o IP del servidor web.
     * @param pnPuerto int. Puerto de escucha del servidor.
     * @param pcServlet String. Nombre del servlet que escucha por datos de localización
     * @param pnRetardo int. Retardo entre transmisiones, en segundos
     */
    public ConfLocaliza(String pcServidor, int pnPuerto, String pcServlet, int pnRetardo) {
        cServidor = pcServidor;
        nPuerto = pnPuerto;
        cServlet = pcServlet;
        nRetardo = pnRetardo;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pnEvento int. Id del evento.
     * @param pnCategoria int. Id de la categoría.
     * @param pcDorsal String. Dorsal del participante.
     * @param pcNombre String. Nombre del participante.
     * @param pcServidor String. Nombre o IP del servidor web.
     * @param pnPuerto int. Puerto de escucha del servidor.
     * @param pcServlet String. Nombre del servlet que escucha por datos de localización
     * @param pnRetardo int. Retardo entre transmisiones, en segundos
     */
    public ConfLocaliza(int pnEvento, int pnCategoria, String pcDorsal, String pcNombre,
                        String pcServidor, int pnPuerto, String pcServlet, int pnRetardo) {
        nEvento = pnEvento;
        nCategoria = pnCategoria;
        cDorsal = pcDorsal;
        cNombre = pcNombre;
        cServidor = pcServidor;
        nPuerto = pnPuerto;
        cServlet = pcServlet;
        nRetardo = pnRetardo;
    }

    public int getnEvento() {
        return nEvento;
    }

    public void setnEvento(int nEvento) {
        this.nEvento = nEvento;
    }

    public int getnCategoria() {
        return nCategoria;
    }

    public void setnCategoria(int nCategoria) {
        this.nCategoria = nCategoria;
    }

    public String getcDorsal() {
        return cDorsal;
    }

    public void setcDorsal(String cDorsal) {
        this.cDorsal = cDorsal;
    }

    public String getcNombre() {
        return cNombre;
    }

    public void setcNombre(String cNombre) {
        this.cNombre = cNombre;
    }

    public String getcServidor() {
        return cServidor;
    }

    public void setcServidor(String cServidor) {
        this.cServidor = cServidor;
    }

    public int getnPuerto() {
        return nPuerto;
    }

    public void setnPuerto(int nPuerto) {
        this.nPuerto = nPuerto;
    }

    public String getcServlet() {
        return cServlet;
    }

    public void setcServlet(String cServlet) {
        this.cServlet = cServlet;
    }

    public int getnRetardo() {
        return nRetardo;
    }

    public void setnRetardo(int nRetardo) {
        this.nRetardo = nRetardo;
    }
}

