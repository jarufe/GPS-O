package jaru.gps.logic;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Iterator;

import jaru.ori.logic.gpslog.TransfGeografica;
import jaru.ori.utils.Utilidades;

public class GpsInterno extends Service implements LocationListener, GpsStatus.Listener {

    private final Context mContext;
    private LocationManager oLocationManager;
    private Location oLocation;
    private int nSatelites = 0;
    private SentenciaNMEA oSentencia = new SentenciaNMEA();

    private final long MIN_TIME_BW_UPDATES = 0;
    private final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    public GpsInterno(Context context) {
        this.mContext = context;
        iniciarGps();
    }

    private void iniciarGps() {
        Log.d("GPS-O", "GPS interno. Iniciando");
        oLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        Log.d("GPS-O", "GPS interno. Conseguido location manager");
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("GpsInterno", "Permisos de ubicación no concedidos");
            return;
        }
        Log.d("GPS-O", "GPS interno. Tengo permiso, comprobando que está habilitado por GPS");
        if (oLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            oLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this
            );
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                Log.d("GPS-O", "GPS interno. Versión Android antigua. Crea listener");
                oLocationManager.addGpsStatusListener(this);
                Log.d("GPS-O", "GPS interno. Versión Android antigua. Listener creado");
            }
        } else {
            Log.d("GPS-O", "GPS interno. Comprobando que está habilitado por red");
            if (oLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                oLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                );
            }
        }
        Log.d("GPS-O", "GPS interno. Inicialización terminada");
    }

    public void pararGps() {
        if (oLocationManager != null) {
            oLocationManager.removeUpdates(this);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                oLocationManager.removeGpsStatusListener(this);
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        oLocation = location;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("GpsInterno", "Permisos de ubicación no concedidos");
            return;
        }
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            GpsStatus status = oLocationManager.getGpsStatus(null);
            if (status != null) {
                nSatelites = 0;
                Iterator<GpsSatellite> it = status.getSatellites().iterator();
                while (it.hasNext()) {
                    GpsSatellite sat = it.next();
                    if (sat.usedInFix()) {
                        nSatelites++;
                    }
                }
            }
        }
    }

    public double getNLatitud() {
        return (oLocation != null) ? oLocation.getLatitude() : -999;
    }

    public double getNLongitud() {
        return (oLocation != null) ? oLocation.getLongitude() : -999;
    }

    public double getNAltitud() {
        return (oLocation != null) ? oLocation.getAltitude() : -999;
    }

    public long getNHora() {
        return (oLocation != null) ? oLocation.getTime() : -999;
    }

    public int getNSatelites() {
        return nSatelites;
    }

    public boolean getBPuedoObtenerPosicion() {
        return oLocation != null;
    }

    public SentenciaNMEA getOSentencia() {
        if (oLocation == null) return oSentencia;

        TransfGeografica cTransf = new TransfGeografica();
        oSentencia.setCLatitud(cTransf.transfGradosACoord(oLocation.getLatitude()));
        oSentencia.setCHemisferio(oLocation.getLatitude() < 0 ? "S" : "N");
        oSentencia.setCLongitud(cTransf.transfGradosACoord(oLocation.getLongitude()));
        oSentencia.setCMeridiano(oLocation.getLongitude() < 0 ? "W" : "E");
        oSentencia.setCAltura(String.valueOf(oLocation.getAltitude()));
        oSentencia.setCHora(Utilidades.obtenerHoraNMEADesdeMilisecs(oLocation.getTime()));
        oSentencia.setCSatelites(String.valueOf(nSatelites));
        oSentencia.setCFix(oLocation.hasAccuracy() ? "1" : "0");
        oSentencia.setCHdop(String.valueOf(oLocation.getAccuracy()));
        oSentencia.nOk = 3;

        return oSentencia;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
