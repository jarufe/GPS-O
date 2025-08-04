package jaru.ori.utils.android;

/**
 * Clase que permite gestionar los datos relativos a los ficheros que se muestran
 * en las clases de selecciÃ³n de ficheros
 * @author javier.arufe
 */
public class Option implements Comparable<Option>{
    private String name;
    private String data;
    private String path;

    /**
     * Constructor de la clase
     * @param n String
     * @param d String
     * @param p String
     */
    public Option(String n,String d,String p) {
        name = n;
        data = d;
        path = p;
    }
    /**
     * Devuelve el nombre de un fichero
     * @return String
     */
    public String getName() {
        return name;
    }
    /**
     * Devuelve una cadena de texto con datos acerca de un fichero
     * @return String
     */
    public String getData() {
        return data;
    }
    /**
     * Devuelve el camino completo dentro del sistema de archivos
     * @return String
     */
    public String getPath() {
        return path;
    }
    /**
     * MÃ©todo que permite comparar el fichero actual con un fichero procedente
     * de un listado de ficheros
     * @param o Option
     * @return
     */
    @Override
    public int compareTo(Option o) {
        if(this.name != null)
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}