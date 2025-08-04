package jaru.ori.logic.gpslog;

import java.io.*;
import java.util.Vector;

/**
 * Clase que contiene los datos de un punto concreto.
 * <P>
 * El usuario asocia a una coordenada unos valores adicionales. 
 * Estos son: un identificador del punto (para poder relacionarlo con otros puntos), 
 * un identificador de tipo de elemento al que pertenece el punto (objeto puntual, 
 * lineal o de superficie) y una descripción.
 * </P>
 * @author jarufe
 */
public class Registro {
    public String cID;
    public int nTipo;
    public String cTipoOCAD;
    public String cTipoOBM;
    public String cDesc;
    public String cCX;
    public String cCY;
    public String cElev;
    public String cFecha;
    /**
     * Constructor por defecto de la clase.
     *
     */
    public Registro() {
        cID = new String("");
        nTipo = 0;
        cTipoOCAD = "101.0";
        cTipoOBM = new String("");
        cDesc = new String("");
        cCX = new String("");
        cCY = new String("");
        cElev = new String("");
        cFecha = new String("");
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcID String. Id del punto.
     * @param pnTipo int. Tipo de punto. 0=>punto; 1=>línea; 2=>superficie.
     * @param pcTipoOCAD String. Id del tipo de objeto OCAD al que pertenece.
     * @param pcDesc String. Descripción adicional del punto para mejor conocimiento por parte del usuario.
     * @param pcCX String. Coordenada X.
     * @param pcCY String. Coordenada Y.
     */
    public Registro(String pcID, int pnTipo, String pcTipoOCAD, String pcDesc, String pcCX, String pcCY) {
        cID = pcID;
        nTipo = pnTipo;
        cTipoOCAD = pcTipoOCAD;
        cTipoOBM = "";
        cDesc = pcDesc;
        cCX = pcCX;
        cCY = pcCY;
        cElev = "0";
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcID String. Id del punto.
     * @param pnTipo int. Tipo de punto. 0=>punto; 1=>línea; 2=>superficie.
     * @param pcTipoOCAD String. Id del tipo de objeto OCAD al que pertenece.
     * @param pcTipoOBM String. Id del tipo de objeto OCAD dual para OBM
     * @param pcDesc String. Descripción adicional del punto para mejor conocimiento por parte del usuario.
     * @param pcCX String. Coordenada X.
     * @param pcCY String. Coordenada Y.
     */
    public Registro(String pcID, int pnTipo, String pcTipoOCAD, String pcTipoOBM, String pcDesc, String pcCX, String pcCY) {
        cID = pcID;
        nTipo = pnTipo;
        cTipoOCAD = pcTipoOCAD;
        cTipoOBM = pcTipoOBM;
        cTipoOBM = "";
        cDesc = pcDesc;
        cCX = pcCX;
        cCY = pcCY;
        cElev = "0";
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcID String. Id del punto.
     * @param pnTipo int. Tipo de punto. 0=>punto; 1=>línea; 2=>superficie.
     * @param pcTipoOCAD String. Id del tipo de objeto OCAD al que pertenece.
     * @param pcDesc String. Descripción adicional del punto para mejor conocimiento por parte del usuario.
     * @param pcCX String. Coordenada X.
     * @param pcCY String. Coordenada Y.
     * @param pcElev String. Elevación.
     * @param pcFecha String. Fecha y hora en que se tomó el registro.
     */
    public Registro(String pcID, int pnTipo, String pcTipoOCAD, String pcDesc, String pcCX, String pcCY,
                    String pcElev, String pcFecha) {
        cID = pcID;
        nTipo = pnTipo;
        cTipoOCAD = pcTipoOCAD;
        cDesc = pcDesc;
        cCX = pcCX;
        cCY = pcCY;
        cElev = pcElev;
        cFecha = pcFecha;
    }
    /**
     * Constructor de la clase con parámetros.
     * @param pcID String. Id del punto.
     * @param pnTipo int. Tipo de punto. 0=>punto; 1=>línea; 2=>superficie.
     * @param pcTipoOCAD String. Id del tipo de objeto OCAD al que pertenece.
     * @param pcTipoOBM String. Id del tipo de objeto OCAD dual para OBM.
     * @param pcDesc String. Descripción adicional del punto para mejor conocimiento por parte del usuario.
     * @param pcCX String. Coordenada X.
     * @param pcCY String. Coordenada Y.
     * @param pcElev String. Elevación.
     * @param pcFecha String. Fecha y hora en que se tomó el registro.
     */
    public Registro(String pcID, int pnTipo, String pcTipoOCAD, String pcTipoOBM, String pcDesc, String pcCX, String pcCY,
                    String pcElev, String pcFecha) {
        cID = pcID;
        nTipo = pnTipo;
        cTipoOCAD = pcTipoOCAD;
        cTipoOBM = pcTipoOBM;
        cDesc = pcDesc;
        cCX = pcCX;
        cCY = pcCY;
        cElev = pcElev;
        cFecha = pcFecha;
    }
    /**
     * Devuelve el ID dado al punto.
     * @return String.
     */
    public String getCID () {
        return cID;
    }
    /**
     * Devuelve el Tipo de registro del que se trata: punto, línea, superficie.
     * @return int.
     */
    public int getNTipo () {
        return nTipo;
    }
    /**
     * Devuelve el Id OCAD del elemento al que pertenece el registro.
     * @return String.
     */
    public String getCTipoOCAD () {
        return cTipoOCAD;
    }
    /**
     * Devuelve el Id OCAD dual para OBM del elemento al que pertenece el registro.
     * @return String.
     */
    public String getCTipoOBM () {
        return cTipoOBM;
    }
    /**
     * Devuelve una descripción adicional del punto.
     * @return String.
     */
    public String getCDesc () {
        return cDesc;
    }
    /**
     * Devuelve la coordenada X del punto.
     * @return String.
     */
    public String getCCX () {
        return cCX;
    }
    /**
     * Devuelve la coordenada Y del punto.
     * @return String.
     */
    public String getCCY () {
        return cCY;
    }
    /**
     * Devuelve la elevación del punto.
     * @return String.
     */
    public String getCElev () {
        return cElev;
    }
    /**
     * Devuelve la fecha/hora en que se tomó el punto.
     * @return String.
     */
    public String getCFecha () {
        return cFecha;
    }
    /**
     * Establece el valor de Id del punto.
     * @param pcValor String.
     */
    public void setCID (String pcValor) {
        cID = pcValor;
    }
    /**
     * Establece el tipo de elemento del que se trata: punto, línea, superficie.
     * @param pnValor int.
     */
    public void setNTipo (int pnValor) {
        nTipo = pnValor;
    }
    /**
     * Establece el Id OCAD del elemento al que pertenece el registro.
     * @param pcValor String.
     */
    public void setCTipoOCAD (String pcValor) {
        cTipoOCAD = pcValor;
    }
    /**
     * Establece el Id OCAD dual para OBM del elemento al que pertenece el registro.
     * @param pcValor String.
     */
    public void setCTipoOBM (String pcValor) {
        cTipoOBM = pcValor;
    }
    /**
     * Establece una descripción adicional del punto.
     * @param pcValor String.
     */
    public void setCDesc (String pcValor) {
        cDesc = pcValor;
    }
    /**
     * Establece el valor de coordenada X del punto.
     * @param pcValor String.
     */
    public void setCCX (String pcValor) {
        cCX = pcValor;
    }
    /**
     * Establece el valor de coordenada Y del punto.
     * @param pcValor String.
     */
    public void setCCY (String pcValor) {
        cCY = pcValor;
    }
    /**
     * Establece el valor de elevación del punto.
     * @param pcValor String.
     */
    public void setCElev (String pcValor) {
        cElev = pcValor;
    }
    /**
     * Establece el valor de fecha/hora en que se tomó el punto.
     * @param pcValor String.
     */
    public void setCFecha (String pcValor) {
        cFecha = pcValor;
    }
    /**
     * Dado un Vector de la clase Registro, comprueba cuál es el mayor ID asignado
     * hasta el momento y devuelve ese valor incrementado en una unidad.<BR>
     * Este método es útil para asignar IDs automáticamente.
     * @param pvRegistros Vector
     * @return int Siguiente ID asignable
     */
    public static int getIdAsignable (Vector<Registro> pvRegistros) {
        int vnContador = 0;
        int vnActual = 0;
        try {
            //Recorre el vector de elementos, para establecer el mayor número utilizado
            int i = 0;
            while (i<pvRegistros.size()) {
                //Obtiene el siguiente elemento
                Registro voRegistro = (Registro)pvRegistros.elementAt(i);
                //Intenta procesar el campo ID como numérico, para conocer el mayor número utilizado
                try {
                    vnActual = Integer.parseInt(voRegistro.getCID());
                    if (vnActual > vnContador)
                        vnContador = vnActual;
                } catch (Exception e2) {
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            vnContador = 0;
        }
        return (vnContador+1);
    }

}

