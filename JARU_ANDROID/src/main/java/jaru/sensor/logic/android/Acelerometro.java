package jaru.sensor.logic.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Clase que sirve para encapsular la utilización básica del acelerómetro disponible
 * en algunos modelos de dispositivo Android
 * Created by JAVI on 4/09/13.
 */
public class Acelerometro implements SensorEventListener {
    private double nRotacion = 0.0;
    private double nInclinacion = 0.0;
    private double nAlabeo = 0.0;
    private boolean bAcelerometro = false;
    private float nMax = 0;

    private SensorManager oManager;
    private Sensor oSensor;
    private Context oContext;

    public Acelerometro () {
        nRotacion = 0.0;
        nInclinacion = 0.0;
        nAlabeo = 0.0;
        bAcelerometro = false;
        nMax = 0;
    }
    public Acelerometro (Context poContext) {
        nRotacion = 0.0;
        nInclinacion = 0.0;
        nAlabeo = 0.0;
        bAcelerometro = false;
        oContext = poContext;
        nMax = 0;
    }

    public void establecerContexto (Context poContext) {
        oContext = poContext;
    }
    public double leerRotacion () {
        return nRotacion;
    }
    public double leerInclinacion () {
        return nInclinacion;
    }
    public double leerAlabeo () {
        return nAlabeo;
    }
    public float leerMaxRange () {
        return nMax;
    }
    public boolean existeAcelerometro () {
        return bAcelerometro;
    }
    public boolean iniciarAcelerometro () {
        boolean vbResul = false;
        try {
            oManager = (SensorManager) oContext.getSystemService(Context.SENSOR_SERVICE);
            oSensor = oManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (oSensor != null) {
                oManager.registerListener(this, oSensor, SensorManager.SENSOR_DELAY_NORMAL);
                nMax = oSensor.getMaximumRange();
                vbResul = true;
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error iniciando acelerómetro", e);
            vbResul = false;
        }
        bAcelerometro = vbResul;
        return vbResul;
    }
    public boolean pararAcelerometro () {
        boolean vbResul = false;
        try {
            if (oSensor != null) {
                oManager.unregisterListener(this);
            }
            vbResul = true;
        } catch (Exception e) {
            Log.e("GPS-O", "Error parando acelerómetro", e);
            vbResul = false;
        }
        bAcelerometro = false;
        return vbResul;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                nRotacion = event.values[0];
                nInclinacion = event.values[1];
                nAlabeo = event.values[2];
            }
        }catch(Exception e) {
            Log.e("GPS-O", "Error al capturar cambio de valores en el sensor acelerómetro", e);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor poSensor, int pnPrecision) {

    }
}
