package jaru.ori.logic.campo;

import jaru.ori.utils.*;
import android.graphics.Point;
/**
 * Clase que contiene los parámetros de configuración del editor de trabajo de campo.
 * <P>
 * Aquí se guardan los valores que definen la imagen que se usa como plantilla, las
 * imágenes que se usan como bocetos (para puntos/líneas, áreas y para sucio), las
 * coordenadas que abarca la plantilla (UTM y WGS84), así como el nivel de zoom
 * y la coordenada central del editor, tal y como estaba la última vez que se usó.
 * </P>
 * @author jarufe
 */
public class ConfCampo {
    public String cPlantilla;
    public String cCX;
    public String cCY;
    public String cCX2;
    public String cCY2;
    public int nZona;
    public String cFactorX;
    public String cFactorY;
    public String cBoceto;
    public int nZoom;
    public String cCXCentral;
    public String cCYCentral;
    public boolean bCalidad;
    /**
     * Constructor por defecto de la clase.
     *
     */
    public ConfCampo() {
        cPlantilla = "";
        cCX = "";
        cCY = "";
        cCX2 = "";
        cCY2 = "";
        nZona = 29;
        cFactorX = "";
        cFactorY = "";
        cBoceto = "";
        nZoom = 0;
        cCXCentral = "";
        cCYCentral = "";
        bCalidad = true;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcPlantilla String. Id del punto.
     * @param pcCX String. Coordenada X sup-izq.
     * @param pcCY String. Coordenada Y sup-izq.
     * @param pcCX2 String. Coordenada X inf-der.
     * @param pcCY2 String. Coordenada Y inf-der.
     * @param pcFactorX String. Factor de resolución X de la plantilla (por ej., en coordenadas de mundo real)
     * @param pcFactorY String. Factor de resolución Y de la plantilla
     * @param pcBoceto String. Nombre de la imagen usada como boceto (tanto para puntos/líneas, como para áreas, como para sucio)
     * @param pnZoom int. Nivel de zoom del editor la última vez que se usó.
     * @param pcCXCentral String. Coordenada X central de la imagen en el editor, la última vez que se usó.
     * @param pcCYCentral String. Coordenada Y central de la imagen en el editor, la última vez que se usó.
     * @param pbCalidad boolean. Indica si se quiere procesar la imagen con la mayor calidad (true) o con la mayor velocidad (false).
     */
    public ConfCampo(String pcPlantilla, String pcCX, String pcCY, String pcCX2, String pcCY2,
                     String pcFactorX, String pcFactorY, String pcBoceto, int pnZoom, String pcCXCentral,
                     String pcCYCentral, boolean pbCalidad) {
        cPlantilla = pcPlantilla;
        cCX = pcCX;
        cCY = pcCY;
        cCX2 = pcCX2;
        cCY2 = pcCY2;
        nZona = 29;
        cFactorX = pcFactorX;
        cFactorY = pcFactorY;
        cBoceto = pcBoceto;
        nZoom = pnZoom;
        cCXCentral = pcCXCentral;
        cCYCentral = pcCYCentral;
        bCalidad = pbCalidad;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcPlantilla String. Id del punto.
     * @param pcCX String. Coordenada X sup-izq.
     * @param pcCY String. Coordenada Y sup-izq.
     * @param pcCX2 String. Coordenada X inf-der.
     * @param pcCY2 String. Coordenada Y inf-der.
     * @param pcFactorX String. Factor de resolución X de la plantilla (por ej., en coordenadas de mundo real)
     * @param pcFactorY String. Factor de resolución Y de la plantilla
     * @param pcBoceto String. Nombre de la imagen usada como boceto (tanto para puntos/líneas, como para áreas, como para sucio)
     * @param pnZoom int. Nivel de zoom del editor la última vez que se usó.
     * @param pcCXCentral String. Coordenada X central de la imagen en el editor, la última vez que se usó.
     * @param pcCYCentral String. Coordenada Y central de la imagen en el editor, la última vez que se usó.
     */
    public ConfCampo(String pcPlantilla, String pcCX, String pcCY, String pcCX2, String pcCY2,
                     String pcFactorX, String pcFactorY, String pcBoceto, int pnZoom, String pcCXCentral,
                     String pcCYCentral) {
        cPlantilla = pcPlantilla;
        cCX = pcCX;
        cCY = pcCY;
        cCX2 = pcCX2;
        cCY2 = pcCY2;
        nZona = 29;
        cFactorX = pcFactorX;
        cFactorY = pcFactorY;
        cBoceto = pcBoceto;
        nZoom = pnZoom;
        cCXCentral = pcCXCentral;
        cCYCentral = pcCYCentral;
        bCalidad = true;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcPlantilla String. Id del punto.
     * @param pcCX String. Coordenada X sup-izq.
     * @param pcCY String. Coordenada Y sup-izq.
     * @param pcCX2 String. Coordenada X inf-der.
     * @param pcCY2 String. Coordenada Y inf-der.
     */
    public ConfCampo(String pcPlantilla, String pcCX, String pcCY, String pcCX2, String pcCY2) {
        cPlantilla = pcPlantilla;
        cCX = pcCX;
        cCY = pcCY;
        cCX2 = pcCX2;
        cCY2 = pcCY2;
        cFactorX = "";
        cFactorY = "";
        cBoceto = "";
        nZoom = 0;
        cCXCentral = "";
        cCYCentral = "";
        bCalidad = true;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcPlantilla String. Id del punto.
     * @param pcCX String. Coordenada X sup-izq.
     * @param pcCY String. Coordenada Y sup-izq.
     * @param pcCX2 String. Coordenada X inf-der.
     * @param pcCY2 String. Coordenada Y inf-der.
     * @param pnZona int. Zona UTM a la que corresponden las coordenadas.
     * @param pcFactorX String. Factor de resolución X de la plantilla (por ej., en coordenadas de mundo real)
     * @param pcFactorY String. Factor de resolución Y de la plantilla
     * @param pcBoceto String. Nombre de la imagen usada como boceto (tanto para puntos/líneas, como para áreas, como para sucio)
     * @param pnZoom int. Nivel de zoom del editor la última vez que se usó.
     * @param pcCXCentral String. Coordenada X central de la imagen en el editor, la última vez que se usó.
     * @param pcCYCentral String. Coordenada Y central de la imagen en el editor, la última vez que se usó.
     * @param pbCalidad boolean. Indica si se quiere procesar la imagen con la mayor calidad (true) o con la mayor velocidad (false).
     */
    public ConfCampo(String pcPlantilla, String pcCX, String pcCY, String pcCX2, String pcCY2,
                     int pnZona, String pcFactorX, String pcFactorY, String pcBoceto, int pnZoom, String pcCXCentral,
                     String pcCYCentral, boolean pbCalidad) {
        cPlantilla = pcPlantilla;
        cCX = pcCX;
        cCY = pcCY;
        cCX2 = pcCX2;
        cCY2 = pcCY2;
        nZona = pnZona;
        cFactorX = pcFactorX;
        cFactorY = pcFactorY;
        cBoceto = pcBoceto;
        nZoom = pnZoom;
        cCXCentral = pcCXCentral;
        cCYCentral = pcCYCentral;
        bCalidad = pbCalidad;
    }
    /**
     * Devuelve el nombre de la plantilla que se usa como imagen de fondo.
     * @return String.
     */
    public String getCPlantilla () {
        return cPlantilla;
    }
    /**
     * Devuelve la coordenada X (UTM, WGS84) del punto sup-izq de la imagen.
     * @return String.
     */
    public String getCCX () {
        return cCX;
    }
    /**
     * Devuelve la coordenada (UTM, WGS84) del punto sup-izq de la imagen.
     * @return String.
     */
    public String getCCY () {
        return cCY;
    }
    /**
     * Devuelve la coordenada X (UTM, WGS84) del punto inf-der de la imagen.
     * @return String.
     */
    public String getCCX2 () {
        return cCX2;
    }
    /**
     * Devuelve la coordenada Y (UTM, WGS84) del punto inf-der de la imagen.
     * @return String.
     */
    public String getCCY2 () {
        return cCY2;
    }
    /**
     * Devuelve la zona (UTM, WGS84) de la imagen.
     * @return int.
     */
    public int getNZona () {
        return nZona;
    }
    /**
     * Devuelve el factor de resolución X de la imagen. Método alternativo a establecer la
     * coordenada inf-der de la imagen.
     * @return String.
     */
    public String getCFactorX () {
        return cFactorX;
    }
    /**
     * Devuelve el factor de resolución Y de la imagen. Método alternativo a establecer la
     * coordenada inf-der de la imagen.
     * @return String.
     */
    public String getCFactorY () {
        return cFactorY;
    }
    /**
     * Devuelve el nombre de la imagen que se usa como boceto para dibujar.
     * Son tres bocetos: puntos/líneas, áreas y dibujo en sucio
     * @return String.
     */
    public String getCBoceto () {
        return cBoceto;
    }
    /**
     * Devuelve el nivel de zoom usado en el editor de trabajo de campo la última vez que se usó.
     * @return int.
     */
    public int getNZoom () {
        return nZoom;
    }
    /**
     * Devuelve la coordenada X (UTM, WGS84) del punto central en el editor de trabajo de campo
     * la última vez que se usó.
     * @return String.
     */
    public String getCCXCentral () {
        return cCXCentral;
    }
    /**
     * Devuelve la coordenada Y (UTM, WGS84) del punto central en el editor de trabajo de campo
     * la última vez que se usó.
     * @return String.
     */
    public String getCCYCentral () {
        return cCYCentral;
    }
    /**
     * Devuelve el valor de la propiedad que indica si se quieren procesar las imágenes con
     * la mayor calidad (true) o con la mayor velocidad (false).
     * @return boolean.
     */
    public boolean getBCalidad () {
        return bCalidad;
    }
    /**
     * Establece el valor del nombre de la plantilla que se usa como imagen de fondo.
     * @param pcValor String.
     */
    public void setCPlantilla (String pcValor) {
        cPlantilla = pcValor;
    }
    /**
     * Establece el valor de la coordenada (UTM, WGS84) del punto sup-izq de la imagen.
     * @param pcValor String.
     */
    public void setCCX (String pcValor) {
        cCX = pcValor;
    }
    /**
     * Establece el valor de la coordenada (UTM, WGS84) del punto sup-izq de la imagen.
     * @param pcValor String.
     */
    public void setCCY (String pcValor) {
        cCY = pcValor;
    }
    /**
     * Establece el valor de la coordenada X (UTM, WGS84) del punto inf-der de la imagen.
     * @param pcValor String.
     */
    public void setCCX2 (String pcValor) {
        cCX2 = pcValor;
    }
    /**
     * Establece el valor de la coordenada Y (UTM, WGS84) del punto inf-der de la imagen.
     * @param pcValor String.
     */
    public void setCCY2 (String pcValor) {
        cCY2 = pcValor;
    }
    /**
     * Establece el valor de la zona (UTM, WGS84) de la imagen.
     * @param pnValor int.
     */
    public void setNZona (int pnValor) {
        nZona = pnValor;
    }
    /**
     * Establece el valor del factor de resolución X de la imagen. Método alternativo a establecer la
     * coordenada inf-der de la imagen.
     * @param pcValor String.
     */
    public void setCFactorX (String pcValor) {
        cFactorX = pcValor;
    }
    /**
     * Establece el valor del factor de resolución Y de la imagen. Método alternativo a establecer la
     * coordenada inf-der de la imagen.
     * @param pcValor String.
     */
    public void setCFactorY (String pcValor) {
        cFactorY = pcValor;
    }
    /**
     * Establece el valor del nombre de la imagen que se usa como boceto para dibujar.
     * Son tres bocetos: puntos/líneas, áreas y dibujo en sucio
     * @param pcValor String.
     */
    public void setCBoceto (String pcValor) {
        cBoceto = pcValor;
    }
    /**
     * Establece el nivel de zoom usado en el editor de trabajo de campo la última vez que se usó.
     * @param pnValor int.
     */
    public void setNZoom (int pnValor) {
        nZoom = pnValor;
    }
    /**
     * Establece el valor de la coordenada X (UTM, WGS84) del punto central en el editor de trabajo de campo
     * la última vez que se usó.
     * @param pcValor String.
     */
    public void setCCXCentral (String pcValor) {
        cCXCentral = pcValor;
    }
    /**
     * Establece el valor de la coordenada Y (UTM, WGS84) del punto central en el editor de trabajo de campo
     * la última vez que se usó.
     * @param pcValor String.
     */
    public void setCCYCentral (String pcValor) {
        cCYCentral = pcValor;
    }
    /**
     * Establece el valor de la propiedad que indica si se quieren procesar las imágenes con
     * la mayor calidad (true) o con la mayor velocidad (false).
     * @param pbValor boolean.
     */
    public void setBCalidad (boolean pbValor) {
        bCalidad = pbValor;
    }

    /**
     * Si el usuario no selecciona nada como imagen de boceto existente, o si
     * la imagen seleccionada no tiene la apariencia de ser un boceto, entonces
     * se crean unos archivos por defecto.<BR>
     * La "apariencia" de boceto se determina según cumpla las siguientes condiciones:<BR>
     * 1.- Es un fichero que tiene una estructura de nombre como esta: path\nombre_tipo.ext,
     * siendo "tipo" uno de estos valores: puntos, areas, sucio.<BR>
     * 2.- Tienen que existir tres ficheros con esa estructura, uno para cada "tipo"
     * 3.- Los ficheros tienen que ser imágenes del mismo tamaño que la imagen de fondo.<BR>
     * @param pcPath String Path de la aplicación, por si hay que crear imágenes por defecto.
     */
    public boolean crearImagenesDeBocetos (String pcPath) {
        Point voTam = new Point(0,0);
        String vcComun = "";
        boolean vbCorrecto = false;
        try {
            //Obtiene el tamaño de la imagen de plantilla, ya que va a ser usado posteriormente
            if (Utilidades.existeFichero(cPlantilla))
                voTam = Utilidades.obtenerTamanoImagen(cPlantilla);
            //Extrae el nombre del fichero antes de "_tipo.ext"
            if (!cBoceto.equals("")) {
                int vnPosGuion = cBoceto.lastIndexOf('_');
                if (vnPosGuion != -1) {
                    vcComun = cBoceto.substring(0, vnPosGuion);
                    //También extrae la extensión del archivo
                    String vcExt = Utilidades.obtenerSufijoFichero(cBoceto);
                    //Comprueba la existencia de los ficheros vcComun_puntos.vcExt, _lineas y _sucio
                    if (Utilidades.existeFichero(vcComun + "_Puntos." + vcExt) &&
                            Utilidades.existeFichero(vcComun + "_Areas." + vcExt) &&
                            Utilidades.existeFichero(vcComun + "_Sucio." + vcExt)) {
                        //Comprueba que sean imï¿½genes con el mismo tamaï¿½o que la imagen de fondo
                        if (voTam.equals(Utilidades.obtenerTamanoImagen(vcComun + "_Puntos." + vcExt)) &&
                                voTam.equals(Utilidades.obtenerTamanoImagen(vcComun + "_Areas." + vcExt)) &&
                                voTam.equals(Utilidades.obtenerTamanoImagen(vcComun + "_Sucio." + vcExt))) {
                            vbCorrecto = true;
                        }
                    }
                }
            }
            //Finalmente, si no cumple con las condiciones de formato, crea imágenes con nombre por defecto
            if (!vbCorrecto) {
                java.util.Date vdActual = Utilidades.getCurDate();
                String vcFecha = Utilidades.format(vdActual, "yyyyMMdd_HHmmss");
                String vcDest = pcPath + "Boceto_" + vcFecha + "_Puntos.png";
                if (Utilidades.crearArchivoBoceto (vcDest, voTam)) {
                    vcDest = pcPath + "Boceto_" + vcFecha + "_Areas.png";
                    if (Utilidades.crearArchivoBoceto (vcDest, voTam)) {
                        vcDest = pcPath + "Boceto_" + vcFecha + "_Sucio.png";
                        if (Utilidades.crearArchivoBoceto (vcDest, voTam)) {
                            cBoceto = vcDest;
                            vbCorrecto = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vbCorrecto;
    }

    /**
     * Método que comprueba la existencia de todos los ficheros de imagen que son necesarios,
     * esto es, el de plantilla más los 3 bocetos (puntos/líneas, áreas y sucio)
     * @return boolean Devuelve si la comprobación ha sido correcta o no
     */
    public boolean existenTodosLosFicheros () {
        boolean vbResul = false;
        String vcComun = "";
        try {
            //Si existe la plantilla, se tiene que seguir comprobando si existen los bocetos
            if (Utilidades.existeFichero(cPlantilla) && !cBoceto.equals("")) {
                int vnPosGuion = cBoceto.lastIndexOf('_');
                if (vnPosGuion != -1) {
                    vcComun = cBoceto.substring(0, vnPosGuion);
                    //También extrae la extensión del archivo
                    String vcExt = Utilidades.obtenerSufijoFichero(cBoceto);
                    //Comprueba la existencia de los ficheros vcComun_puntos.vcExt, _lineas y _sucio
                    if (Utilidades.existeFichero(vcComun + "_Puntos." + vcExt) &&
                            Utilidades.existeFichero(vcComun + "_Areas." + vcExt) &&
                            Utilidades.existeFichero(vcComun + "_Sucio." + vcExt)) {
                        vbResul = true;
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return vbResul;
    }
    /**
     * Método que, dados unos bocetos (que están en PNG), genera las mismas imágenes
     * pero en formato JPG con fichero de mundo (JGW)
     * @return boolean Devuelve si la ejecución ha sido correcta o no
     */
    public boolean exportarBocetos () {
        boolean vbResul = false;
        String vcComun = "";
        try {
            //Si existe la plantilla, se tiene que seguir comprobando si existen los bocetos
            if (Utilidades.existeFichero(cPlantilla) && !cBoceto.equals("")) {
                int vnPosGuion = cBoceto.lastIndexOf('_');
                if (vnPosGuion != -1) {
                    vcComun = cBoceto.substring(0, vnPosGuion);
                    //También extrae la extensión del archivo
                    String vcExt = Utilidades.obtenerSufijoFichero(cBoceto);
                    //Comprueba la existencia de los ficheros vcComun_puntos.vcExt, _lineas y _sucio
                    if (Utilidades.existeFichero(vcComun + "_Puntos." + vcExt) &&
                            Utilidades.existeFichero(vcComun + "_Areas." + vcExt) &&
                            Utilidades.existeFichero(vcComun + "_Sucio." + vcExt)) {
                        //Para cada boceto, lo abre, genera JPG y fichero de mundo
                        if (Utilidades.crearJPGDesdePNG(vcComun + "_Puntos." + vcExt, vcComun + "_Puntos.jpg")) {
                            if (Utilidades.escribirGeorreferencia(vcComun + "_Puntos.jpg", cFactorX, "0.00", "0.00",
                                    cFactorY, cCX, cCY)) {
                                if (Utilidades.crearJPGDesdePNG(vcComun + "_Areas." + vcExt, vcComun + "_Areas.jpg")) {
                                    if (Utilidades.escribirGeorreferencia(vcComun + "_Areas.jpg", cFactorX, "0.00", "0.00",
                                            cFactorY, cCX, cCY)) {
                                        if (Utilidades.crearJPGDesdePNG(vcComun + "_Sucio." + vcExt, vcComun + "_Sucio.jpg")) {
                                            if (Utilidades.escribirGeorreferencia(vcComun + "_Sucio.jpg", cFactorX, "0.00", "0.00",
                                                    cFactorY, cCX, cCY)) {
                                                vbResul = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return vbResul;
    }

}

