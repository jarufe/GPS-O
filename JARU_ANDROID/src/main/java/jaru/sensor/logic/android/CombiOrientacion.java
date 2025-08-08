package jaru.sensor.logic.android;

import android.content.Context;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Clase que encapsula el uso combinado de dos sensores para ser utilizados como brújula.
 * Por un lado está el sensor de orientación. Por otro, el sensor llamado de rotación, que permite
 * evaluar el movimiento del dispositivo en 3 ejes.
 * Created by JAVI on 4/09/13.
 */
public class CombiOrientacion {
    private Rotacion oRot = null;
    private Brujula oBru = null;
    private Context oContext = null;

    private double nDesvio = 0.0;
    private boolean bCombi = false;

    public CombiOrientacion () {
        try {
            oRot = new Rotacion();
            oBru = new Brujula();
            bCombi = oBru.iniciarBrujula();
            if (bCombi)
                oRot.iniciarRotacion();
            else
                bCombi = oRot.iniciarRotacion();
        } catch (Exception e) {
            oRot = null;
            oBru = null;
            bCombi = false;
        }
    }
    public CombiOrientacion (Context poContext) {
        try {
            oContext = poContext;
            oRot = new Rotacion(oContext);
            oBru = new Brujula(oContext);
            bCombi = oBru.iniciarBrujula();
            if (bCombi)
                oRot.iniciarRotacion();
            else
                bCombi = oRot.iniciarRotacion();
        } catch (Exception e) {
            oRot = null;
            oBru = null;
            bCombi = false;
        }
    }

    public void establecerContexto (Context poContext) {
        oContext = poContext;
        if (oRot!=null)
            oRot.establecerContexto(oContext);
        if (oBru!=null)
            oBru.establecerContexto(oContext);
    }
    public Rotacion getRotacion () {
        return oRot;
    }
    public Brujula getBrujula () {
        return oBru;
    }
    public double getNDesvio () {
        return nDesvio;
    }

    /**
     * Devuelve true si existe alguno de los dos sensores: brújula digital o acelerómetro
     * Si existe brújula se usa la brújula. Sino, se usa acelerómetro
     * @return boolean.
     */
    public boolean existeOrientacion () {
        return bCombi;
    }

    /**
     * Devuelve true si existe el sensor brújula digital
     * @return boolean
     */
    public boolean existeBrujula () {
        boolean vbResul = false;
        if (oBru!=null) {
            vbResul = oBru.existeBrujula();
        }
        return vbResul;
    }

    /**
     * Devuelve true si existe el sensor de rotación
     * @return boolean
     */
    public boolean existeRotacion () {
        boolean vbResul = false;
        if (oRot!=null) {
            vbResul = oRot.existeRotacion();
        }
        return vbResul;
    }

    public void setRotacion (Rotacion poRot) {
        oRot = poRot;
    }
    public void setBrujula (Brujula poBru) {
        oBru = poBru;
    }
    public void setNDesvio (double pnDesvio) {
        nDesvio = pnDesvio;
    }

    /**
     * Devuelve la lectura del sensor que esté activo. Si existe brújula digital, se devuelve
     * la lectura en grados. Si no existe brújula pero existe sensor de rotación, entonces se devuelve
     * la rotación marcada por el sensor.
     * @return double
     */
    public double leerGrados () {
        double vnGrados = 0.0;
        try {
            if (oBru != null && oRot != null) {
                if (oBru.existeBrujula()) {
                    vnGrados = oBru.leerGrados();
                } else if (oRot.existeRotacion()) {
                    vnGrados = oRot.leerX();
                }
            }
            vnGrados = new BigDecimal(vnGrados).setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            Log.e("GPS-O", "Error leyendo grados de brújula o rotación", e);
            vnGrados = 0.0;
        }
        return vnGrados;
    }

    /**
     * Método que permite parar la lectura de los sensores de orientación: brújula y/o rotación
     */
    public void pararCombi () {
        if (oRot != null) {
            oRot.pararRotacion();
        }
        if (oBru != null) {
            oBru.pararBrujula();
        }
        bCombi = false;
    }

}
