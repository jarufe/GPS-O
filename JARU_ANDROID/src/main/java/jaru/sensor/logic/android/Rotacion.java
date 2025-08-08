package jaru.sensor.logic.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Clase que sirve para encapsular la utilización básica del sensor de rotación disponible
 * en algunos modelos de dispositivo Android
 * Created by JAVI on 4/09/13.
 */
public class Rotacion implements SensorEventListener {
    private double nX = 0.0;
    private double nY = 0.0;
    private double nZ = 0.0;
    private boolean bRotacion = false;
    private float nMax = 0;

    private SensorManager oManager;
    private Sensor oSensor;
    private Context oContext;

    public Rotacion() {
        nX = 0.0;
        nY = 0.0;
        nZ = 0.0;
        bRotacion = false;
        nMax = 0;
    }
    public Rotacion(Context poContext) {
        nX = 0.0;
        nY = 0.0;
        nZ = 0.0;
        bRotacion = false;
        oContext = poContext;
        nMax = 0;
    }

    public void establecerContexto (Context poContext) {
        oContext = poContext;
    }
    public double leerX () {
        return nX;
    }
    public double leerY () {
        return nY;
    }
    public double leerZ () {
        return nZ;
    }
    public float leerMaxRange () {
        return nMax;
    }
    public boolean existeRotacion () {
        return bRotacion;
    }
    public boolean iniciarRotacion () {
        boolean vbResul = false;
        try {
            oManager = (SensorManager) oContext.getSystemService(Context.SENSOR_SERVICE);
            oSensor = oManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (oSensor != null) {
                oManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_NORMAL);
                nMax = oSensor.getMaximumRange();
                vbResul = true;
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error iniciando sensor de rotación", e);
            vbResul = false;
        }
        bRotacion = vbResul;
        return vbResul;
    }
    public boolean pararRotacion () {
        boolean vbResul = false;
        try {
            if (oSensor != null) {
                oManager.unregisterListener(this);
            }
            vbResul = true;
        } catch (Exception e) {
            Log.e("GPS-O", "Error parando sensor de rotación", e);
            vbResul = false;
        }
        bRotacion = false;
        return vbResul;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                nX = event.values[0];
                nY = event.values[1];
                nZ = event.values[2];
            }
        }catch(Exception e) {
            Log.e("GPS-O", "Error en evento de cambio de valores del sensor de rotación", e);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor poSensor, int pnPrecision) {

    }
}
