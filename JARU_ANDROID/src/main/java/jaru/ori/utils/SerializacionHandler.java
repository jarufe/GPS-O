/*
 * Creado el 19-may-05
 *
 * Paquete de clases de utilidades para programas Java
 */
package jaru.ori.utils;

import java.util.Vector;
import java.io.*;

/**
 * Clase para leer y escribir datos serializados.
 * <P>
 * Se trata de una clase genÃ©rica que se puede utilizar para escribir en archivo o leer de archivo cualquier
 * clase de datos que sean serializables. Para ello cuenta con dos mÃ©todos estÃ¡ticos que permiten realizar 
 * cada una de esas acciones. SÃ³lo hay dos cosas a tener en cuenta:<BR>
 * - La clase de datos que se va a serializar tiene que implementar el interface java.io.Serializable.<BR>
 * - La clase que use este gestor tiene que saber quÃ© tipos de datos espera, para poder hacer una correcta 
 * transformaciÃ³n desde la clase Object, que es la que realmente se utiliza en la transferencia.
 * </P>
 * @author jarufe
 * @version 1.0
 */
public class SerializacionHandler {
    /**
     * MÃ©todo que se encarga de recuperar los datos a partir de archivo binario serializado.
     * @param poArchivo InputStream. Stream que representa el archivo con los datos serializados.
     * @return Object.
     */
    public static Object obtenerDatosBinario(InputStream poArchivo) {
        Object voResul = new Vector();
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(poArchivo);
            voResul = (Object)in.readObject();
            in.close();
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        catch(ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return voResul;
    }
    /**
     * Vuelca los datos de una clase serializable a un archivo binario serializado.
     * @param poObjetos Object. Objetos que se quieren serializar.
     * @param pcFichero String. Path completo + nombre de fichero, del archivo serializado que se va a escribir.
     */
    public static void escribirBinario (Object poObjetos, String pcFichero) {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(pcFichero);
            out = new ObjectOutputStream(fos);
            out.writeObject(poObjetos);
            out.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

}

