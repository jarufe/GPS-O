package jaru.sensor.logic.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Clase que sirve para encapsular la utilización básica de la brújula electrónica disponible
 * en algunos modelos de dispositivo Android
 * Created by JAVI on 4/09/13.
 */
public class Brujula implements SensorEventListener {
    private double nGrados = 0.0;
    private boolean bBrujula = false;

    private SensorManager oManager;
    private Sensor oSensor;
    private Context oContext;

    public Brujula () {
        nGrados = 0.0;
        bBrujula = false;
    }
    public Brujula (Context poContext) {
        nGrados = 0.0;
        bBrujula = false;
        oContext = poContext;
    }

    public void establecerContexto (Context poContext) {
        oContext = poContext;
    }
    public double leerGrados () {
        return nGrados;
    }
    public boolean existeBrujula () {
        return bBrujula;
    }
    public boolean iniciarBrujula () {
        boolean vbResul = false;
        try {
            oManager = (SensorManager) oContext.getSystemService(Context.SENSOR_SERVICE);
            oSensor = oManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (oSensor != null) {
                oManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_NORMAL);
                vbResul = true;
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error iniciando sensor brújula", e);
            vbResul = false;
        }
        bBrujula = vbResul;
        return vbResul;
    }
    public boolean pararBrujula () {
        boolean vbResul = false;
        try {
            if (oSensor != null) {
                oManager.unregisterListener(this);
            }
            vbResul = true;
        } catch (Exception e) {
            Log.e("GPS-O", "Error parando sensor brújula", e);
            vbResul = false;
        }
        bBrujula = false;
        return vbResul;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                nGrados = event.values[0];
            }
        }catch(Exception e) {
            Log.e("GPS-O", "Error capturando evento de cambio de valores de sensor brújula", e);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor poSensor, int pnPrecision) {

    }



}
