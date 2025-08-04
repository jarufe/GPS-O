package jaru.gps.logic;

import java.util.Iterator;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import jaru.ori.gui.gpslog.android.R;

import jaru.ori.logic.gpslog.*;
import jaru.ori.utils.Utilidades;
import java.util.Set;

/**
 * Clase que gestiona el GPS interno de un dispositivo Android.
 * Contiene los métodos básicos para poder conectarse a un GPS interno, obtener
 * datos de posicionamiento y refrescar esos datos periódicamente
 * @author jarufe
 */
public class GpsInterno extends Service implements LocationListener, GpsStatus.Listener{
    private final Context mContext;
    // Estado del GPS
    boolean bGpsHabilitado = false;
    // Estado de la conectividad por red
    boolean bRedHabilitada = false;
    // Propiedad que indica si se puede obtener posicionamiento por GPS interno
    boolean bPuedoObtenerPosicion = false;
    Location oLocation; // location
    double nLatitud; // latitude
    double nLongitud; // longitude
    double nAltitud;
    long nHora;
    int nSatelites;
    // Distancia mínima para actualizar, en metros
    private final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; //5; // 5 meters
    // Tiempo mínimo para actualizar, en milisegundos
    private final long MIN_TIME_BW_UPDATES = 0; //1000 * 5; // 5 segundos
    // Declaración del gestor de posicionamiento
    protected LocationManager oLocationManager;
    // Propiedad para guardar los datos de posicionamiento de forma que sea compatible con la versión anterior
    private SentenciaNMEA oSentencia = new SentenciaNMEA();

    /**
     * Constructor por defecto de la clase
     * @param context Context
     */
    public GpsInterno(Context context) {
        this.mContext = context;
        getOLocation();
    }

    /**
     * Método que trata de inicializar el GPS interno del dispositivo para obtener datos de posicionamiento.
     * Intenta obtener los datos de posicionamiento primero a través de la red
     * y luego a través del GPS. Se podría mejorar para tratar de coger siempre el mejor posicionamiento.
     * @return Location
     */
    public Location getOLocation() {
        try {
            bPuedoObtenerPosicion = false;
            oLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            // Estado del GPS
            bGpsHabilitado = oLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // Estado de la red
            bRedHabilitada = oLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!bGpsHabilitado && !bRedHabilitada) {
                // no hay proveedor habilitado
            } else {
                bPuedoObtenerPosicion = true;
                // Primero obtiene posicionamiento del proveedor de red
                if (bRedHabilitada) {
                    oLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (oLocationManager != null) {
                        oLocation = oLocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (oLocation != null) {
                            nLatitud = oLocation.getLatitude();
                            nLongitud = oLocation.getLongitude();
                            nAltitud = oLocation.getAltitude();
                            nHora = oLocation.getTime();
                        }
                    }
                }
                // Si el GPS está habilitado, obtiene lat/lon usando el GPS
                if (bGpsHabilitado) {
                    oLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (oLocationManager != null) {
                        oLocation = oLocationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (oLocation != null) {
                            nLatitud = oLocation.getLatitude();
                            nLongitud = oLocation.getLongitude();
                            nAltitud = oLocation.getAltitude();
                            nHora = oLocation.getTime();
                        }
                    }
                }
                //Añado un listener para los cambios de status del GPS
                oLocationManager.addGpsStatusListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oLocation;
    }

    /**
     * Método para forzar la parada en el uso del listener del GPS
     **/
    public void pararGps(){
        if(oLocationManager != null){
            oLocationManager.removeUpdates(GpsInterno.this);
            oLocationManager.removeGpsStatusListener(GpsInterno.this);
        }
    }

    /**
     * Método que devuelve el valor de latitud
     * @return double. Latitud en formato decimal
     **/
    public double getNLatitud(){
        if(oLocation != null){
            nLatitud = oLocation.getLatitude();
        } else {
            nLatitud = -999;
        }
        return nLatitud;
    }

    /**
     * Método que devuelve el valor de longitud
     * @return double. Longitud en formato decimal
     **/
    public double getNLongitud(){
        if(oLocation != null){
            nLongitud = oLocation.getLongitude();
        } else {
            nLongitud = -999;
        }
        return nLongitud;
    }

    /**
     * Método que devuelve el valor de altitud
     * @return double. Altitud en formato decimal
     **/
    public double getNAltitud(){
        if(oLocation != null){
            nAltitud = oLocation.getAltitude();
        } else {
            nAltitud = -999;
        }
        return nAltitud;
    }

    /**
     * Método que devuelve el valor de la hora (en milisegundos desde el 1 de enero de 1970)
     * @return double. Hora en milisegundos
     **/
    public long getNHora(){
        if(oLocation != null){
            nHora = oLocation.getTime();
        } else {
            nHora = -999;
        }
        return nHora;
    }
    /**
     * Método que devuelve el número de satélites usados en el Fix
     * @return int. Número de satélites
     **/
    public int getNSatelites(){
        if(oLocation == null)
            nSatelites = 0;
        return nSatelites;
    }

    /**
     * Método que devuelve si existe proveedor para obtener los datos de posicionamiento (GPS/wifi)
     * @return boolean
     **/
    public boolean getBPuedoObtenerPosicion() {
        return this.bPuedoObtenerPosicion;
    }

    /**
     * Método que muestra un aviso para abrir la pantalla de configuración de GPS del dispositivo
     * Pulsando el botón adecuado se lanza el cuadro de diálogo de configuración
     **/
    public void mostrarAlertaConfiguracion(){
        AlertDialog.Builder voAlertDialog = new AlertDialog.Builder(mContext);
        // Título del aviso
        voAlertDialog.setTitle(this.mContext.getString(R.string.ORI_ML00194));
        // Mensaje del aviso
        voAlertDialog.setMessage(this.mContext.getString(R.string.ORI_MI00016));
        // Botón para aceptar
        voAlertDialog.setPositiveButton(this.mContext.getString(R.string.ORI_ML00003), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent voIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(voIntent);
            }
        });
        // Botón para cancelar
        voAlertDialog.setNegativeButton(this.mContext.getString(R.string.ORI_ML00004), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Mostrar la alerta
        voAlertDialog.show();
    }

    /**
     * Se lanza cada vez que el GPS interpreta que se ha cambiado de posición.
     * Se actualizan las propiedades que almacenan los valores de posición.
     * @param poLocation Location
     */
    @Override
    public void onLocationChanged(Location poLocation) {
        try {
            if (poLocation != null) {
                oLocation = poLocation;
                nLatitud = oLocation.getLatitude();
                nLongitud = oLocation.getLongitude();
                nAltitud = oLocation.getAltitude();
                nHora = oLocation.getTime();
            }
        } catch (Exception e) {}
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onGpsStatusChanged(int pnEvento) {
        switch(pnEvento){
            case GpsStatus.GPS_EVENT_STARTED:
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                //Recoge el estado del GPS, para conocer si hay Fix y número de satélites
                GpsStatus voGpsStatus = oLocationManager.getGpsStatus(null);
                if (voGpsStatus!=null) {
                    //La cuenta de satélites va a ser la de aquellos usados en el Fix
                    nSatelites = 0;
                    Iterator voItr = voGpsStatus.getSatellites().iterator();
                    while(voItr.hasNext()) {
                        GpsSatellite voSat = (GpsSatellite)voItr.next();
                        if (voSat.usedInFix())
                            nSatelites++;
                    }
                }
                break ;
        }
    }

    /**
     * Devuelve la última Sentencia NMEA procesada correctamente como valor de posición GPS.
     * @return SentenciaNMEA.
     */
    public SentenciaNMEA getOSentencia() {
        try {
            if (getBPuedoObtenerPosicion()) {
                TransfGeografica cTransf = new TransfGeografica();
                oSentencia.setCLatitud(cTransf.transfGradosACoord(oLocation.getLatitude()));
                //Si la latitud es <0 estamos en el hemisferio sur.
                if (oLocation.getLatitude()<0)
                    oSentencia.setCHemisferio("S");
                else
                    oSentencia.setCHemisferio("N");
                oSentencia.setCLongitud(cTransf.transfGradosACoord(oLocation.getLongitude()));
                //Si la longitud es <0 estamos en el meridiano oeste.
                if (oLocation.getLongitude()<0)
                    oSentencia.setCMeridiano("W");
                else
                    oSentencia.setCMeridiano("E");
                oSentencia.setCAltura(oLocation.getAltitude()+"");
                oSentencia.setCHora(Utilidades.obtenerHoraNMEADesdeMilisecs(oLocation.getTime()));
                oSentencia.nOk = 3;
                oSentencia.setCSatelites(nSatelites+"");
                if (oLocation.hasAccuracy())
                    oSentencia.setCFix("1");
                else
                    oSentencia.setCFix("0");
                oSentencia.setCHdop(oLocation.getAccuracy()+"");
            }
        } catch (Exception e) {
            oSentencia.setCAltura("-999");
            oSentencia.setCLatitud("-999");
            oSentencia.setCLongitud("-999");
        }
        return oSentencia;
    }

}