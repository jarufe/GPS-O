package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Vector;

import jaru.gps.logic.GpsInterno;
import jaru.gps.logic.Parametro;
import jaru.gps.logic.PuertoSerie;
import jaru.gps.logic.SentenciaNMEA;
import jaru.ori.logic.gpslog.TransfGeografica;
import jaru.ori.utils.Utilidades;
import jaru.ori.utils.android.UtilsAndroid;
import jaru.ori.web.controlcarrera.RegistroLocalizacion;
import jaru.red.logic.GestionTransmisiones;
import jaru.red.logic.HiloTransmisiones;

public class AVerMapa extends Activity {
    private Thread oThread;
    private Thread oThread2;
    private int nRetardo = 500;
    private int nRetardo2 = 10000;
    private SentenciaNMEA oSentencia = null;
    private Parametro oParametro = null;
    private GpsInterno oGpsInterno = null;
    public Application oApp = null;
    public Resources oRes = null;
    // Google Map
    private GoogleMap googleMap;
    private boolean bPrimera = true;
    private boolean bPropia = false;
    private boolean bTrack = false;
    //Hilo de transmisiones
    private HiloTransmisiones oTrans1 = null;
    private HiloTransmisiones oTrans2 = null;

    final int ACTIVITY_SELIDS = 40;
    protected static final int GUIUPDATEIDENTIFIER = 0x101;
    protected static final int GUIUPDATEIDENTIFIER2 = 0x102;

    Handler iHandler = new Handler() {
        @Override
        public void handleMessage(Message poMsg) {
            switch (poMsg.what) {
                case AVerMapa.GUIUPDATEIDENTIFIER2:
                    //Primero limpia los marcadores existentes
                    if (googleMap!=null)
                        googleMap.clear();
                    //Ahora los recrea, empezando por la posición propia
                    if (bPropia) {
                        leerMostrarPosicionPropia();
                    }
                    //y continuando con la posición de los Ids seleccionados
                    if (APrincipal.getvIds()!=null) {
                        if (APrincipal.getvIds().size()>0) {
                            leerPosicionIds();
                            //Si está activada la opción de ver tracks, los lee y muestra
                            if (bTrack) {
                                leerTracks();
                            }
                        }
                    }
                    break;
                case AVerMapa.GUIUPDATEIDENTIFIER:
                    if (oTrans1!=null) {
                        if (oTrans1.isbResulPreparado()) {
                            /*
                            //
                            //CAMBIAR POR PROCESAMIENTO JSON
                            //
                            Vector<Object> vvRespuesta = oTrans1.getvRespuesta();
                            //Procesa respuesta del hilo de transmisiones
                            AVerMapa.this.mostrarPosicionIds(vvRespuesta);
                             */
                            //Resetea el hilo
                            oTrans1.setbResulPreparado(false);
                            oTrans1.stop();
                        }
                    }
                    if (oTrans2!=null) {
                        if (oTrans2.isbResulPreparado()) {
                            /*
                            //
                            //CAMBIAR POR PROCESAMIENTO JSON
                            //
                            Vector<Object> vvRespuesta = oTrans2.getvRespuesta();
                            AVerMapa.this.mostrarTracks(vvRespuesta);
                             */
                            //Resetea el hilo
                            oTrans2.setbResulPreparado(false);
                            oTrans2.stop();
                        }
                    }
                    break;
            }
            super.handleMessage(poMsg);
        }
    };
    /**
     * Devuelve el número de milisegundos usado como retardo entre ticks.
     * @return int. Retardo en milisegundos.
     */
    public int getNRetardo() {
        return nRetardo;
    }
    /**
     * Establece el número de milisegundos usado como retardo entre ticks.
     * @param pnValor int. Retardo en milisegundos.
     */
    public void setNRetardo (int pnValor) {
        nRetardo = pnValor;
    }
    /**
     * Devuelve el objeto que contiene los datos de la última lectura realizada
     * sobre el puerto de comunicaciones.
     * @return SentenciaNMEA. Objeto que contiene los datos de la última lectura.
     */
    public SentenciaNMEA getOSentencia() {
        return oSentencia;
    }
    /**
     * Devuelve el objeto de parámetros de configuración
     * @return Parametro. Comfiguración.
     */
    public Parametro getOParametro() {
        return oParametro;
    }
    /**
     * Establece el objeto de configuración
     * @param poValor Parametro. Parametro de configuración
     */
    public void setOParametro (Parametro poValor) {
        oParametro = poValor;
    }
    /**
     * Devuelve el objeto de gestión de GPS interno
     * @return GpsInterno. GPS interno.
     */
    public GpsInterno getOGpsInterno() {
        return oGpsInterno;
    }
    /**
     * Establece el objeto de gestión de GPS interno
     * @param poValor GpsInterno. Gestión de GPS interno
     */
    public void setOGpsInterno (GpsInterno poValor) {
        oGpsInterno = poValor;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Establece la orientación según el dispositivo sea más ancho (horizontal) o alto (vertical)
        /*
        if(UtilsAndroid.esPantallaAncha(this.getResources())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
         */
        setContentView(R.layout.vermapa);
        try {
            //Recoge de la clase principal los elementos básicos que intervienen en el proceso
            oParametro = APrincipal.getOParametro();
            oGpsInterno = APrincipal.getOGpsInterno();
            GestionTransmisiones.setcServidor(APrincipal.getoConfLocaliza().getcServidor());
            GestionTransmisiones.setnPuerto(APrincipal.getoConfLocaliza().getnPuerto());
            GestionTransmisiones.setcServlet(APrincipal.getoConfLocaliza().getcServlet());
            oTrans1 = new HiloTransmisiones();
            oTrans2 = new HiloTransmisiones();
            //Resto de objetos
            oApp = APrincipal.getOApp();
            oRes = APrincipal.getORes();
            // Inicializa la visualización del mapa
            initializeMap();
            //Crea el nuevo hilo de ejecución y lo pone a correr para refrescar posiciones en el mapa periódicamente
            oThread = new Thread(new HiloRun());
            oThread.start();
            oThread2 = new Thread(new VerMapaRun());
            oThread2.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initializeMap() {
        if (googleMap == null) {
            /*
            //
            //CAMBIAR POR PROCESAMIENTO JSON
            //
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

             */
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(oApp.getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            } else {
                //Establece el tipo de mapa para visualizar
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                //googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                //googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                //googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                //googleMap.setMyLocationEnabled(true); // false to disable
                //googleMap.getUiSettings().setZoomControlsEnabled(false); // true to enable
            }
        }
    }
    public void onNormalMap(View view) {
        if (googleMap!=null)
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public void onSatelliteMap(View view) {
        if (googleMap!=null)
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    public void onTerrainMap(View view) {
        if (googleMap!=null)
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    public void onHybridMap(View view) {
        if (googleMap!=null)
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeMap();
        //Crea el nuevo hilo de ejecución y lo pone a correr para refrescar posiciones en el mapa periódicamente
        if(oThread2!=null)
            oThread2.interrupt();
        oThread2 = new Thread(new VerMapaRun());
        oThread2.start();
        if(oThread!=null)
            oThread.interrupt();
        oThread = new Thread(new HiloRun());
        oThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(oThread!=null)
            oThread.interrupt();
        if(oThread2!=null)
            oThread2.interrupt();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_SELIDS:
                //Cuando vuelve del cuadro de diálogo de selección de Ids,
                //el vector vIds ya contiene la lista de Ids seleccionados
                break;
        }
    }
    /**
     * Método que se llama cuando se crean las opciones de menú
     * @param menu Menu
     * @return boolean. Devuelve true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vermapa, menu);
        return true;
    }
    /**
     * Método que se llama cuando se preparan las opciones de menú
     * @param menu Menu
     * @return boolean. Devuelve el resultado del método en super.
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        boolean vbResul = super.onPrepareOptionsMenu(menu);
        MenuItem oItem = menu.findItem(R.id.posicionPropia8);
        if (!bPropia) {
            oItem.setTitle(this.getString(R.string.ORI_ML00212));
        } else {
            oItem.setTitle(this.getString(R.string.ORI_ML00213));
        }
        MenuItem oItem2 = menu.findItem(R.id.verTrack8);
        if (!bTrack) {
            oItem2.setTitle(this.getString(R.string.ORI_ML00214));
        } else {
            oItem2.setTitle(this.getString(R.string.ORI_ML00215));
        }
        return vbResul;
    }
    /**
     * Método que se llama cuando el usuario selecciona una opción de menú.<BR>
     * Si se pulsa el botón grabar, se guardan los parámetros actuales de configuración
     * y las imágenes que se están editando.<BR>
     * Si se pulsa el botón cerrar, se para el hilo de ejecución que recoge periódicamente
     * la señal de satélite, se guardan los parámetros de configuración y las
     * imágenes editadas.
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.seleccionar8) {
            this.seleccionarIds();
        } else if (item.getItemId()==R.id.posicionPropia8) {
            bPropia = !bPropia;
        } else if (item.getItemId()==R.id.verTrack8) {
            bTrack = !bTrack;
        } else if (item.getItemId()==R.id.cerrar8) {
            if(oThread!=null)
                oThread.interrupt();
            if(oThread2!=null)
                oThread2.interrupt();
            this.finish();
        }
        return true;
    }
    /**
     * Método que se encarga de mostrar los registros vectoriales y le permite
     * al usuario eliminar uno o varios.
     */
    public void seleccionarIds () {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ASelIds.class);
            startActivityForResult(viIntent, ACTIVITY_SELIDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que se encarga de leer un nuevo valor de coordenada desde el puerto de
     * comunicaciones. En caso positivo, transforma la coordenada, crea una nueva
     * marca y la ubica en el mapa
     */
    private void leerMostrarPosicionPropia() {
        oSentencia = new SentenciaNMEA();
        try {
            //Recoge los datos de posicionamiento del GPS interno o externo según la configuración establecida
            if (oParametro.getCGpsInterno().equals("0")) {
                oSentencia = PuertoSerie.getOSentencia().copia();
                //Como la sentencia viene de un GPS externo con NMEA, ajusta la hora según el desfase UTC
                int vnDesfase = Utilidades.obtenerDesfaseHorarioMinutos();
                oSentencia.ajustarHora(vnDesfase);
            } else {
                if (oGpsInterno!=null)
                    oSentencia = oGpsInterno.getOSentencia().copia();
            }
        } catch (Exception e) {
        }
        try {
            //Si hay datos correspondientes a una nueva lectura, procede a mostrar la ubicación en el mapa.
            if (oSentencia.cLongitud.length()>0 && oSentencia.cLatitud.length()>0) {
                //Crea un nuevo registro.
                //Obtiene los valores transformados de la coordenada leída
                TransfGeografica poTransf = new TransfGeografica();
                String cLongitud = poTransf.transfCoordAGrados(poTransf.obtieneCadena(poTransf.obtieneLong(oSentencia.cLongitud)));
                if (oSentencia.cMeridiano.equals("W"))
                    cLongitud = "-" + cLongitud;
                String cLatitud = poTransf.transfCoordAGrados(poTransf.obtieneCadena(poTransf.obtieneLong(oSentencia.cLatitud)));
                if (oSentencia.cHemisferio.equals("S"))
                    cLatitud = "-" + cLatitud;
                //Muestra la coordenada
                double vnLat = Double.parseDouble(cLatitud);
                double vnLon = Double.parseDouble(cLongitud);
                // create marker
                MarkerOptions marker = new MarkerOptions().position(new LatLng(vnLat, vnLon)).title("Yo");
                // ROSE color icon
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                // adding marker
                Marker voMarca = googleMap.addMarker(marker);
                voMarca.showInfoWindow();
                if (bPrimera) {
                    //Posicionamiento de la cámara en una coordenada concreta
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            new LatLng(vnLat, vnLon)).zoom(15).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    bPrimera = false;
                }
            }
        } catch (Exception e) {
        }
    }
    /**
     * Método que se encarga de recorrer el vector de identificadores, para consultar acerca de
     * su posición al servidor web.
     */
    private void leerPosicionIds() {
        try {
            //Primero, crea la orden para enviar al servidor web y obtener las coordenadas de cada Id
            Vector<Object> vvEnvio = new Vector<Object>();
            vvEnvio.addElement(new String("PosicionMulti")); //Orden
            vvEnvio.addElement(new Integer(-1));  //Evento
            vvEnvio.addElement(new Integer(-1));  //Categoria
            for (int i=0; i<APrincipal.getvIds().size(); i++) {
                vvEnvio.addElement(APrincipal.getvIds().elementAt(i));
            }
            /*
            //
            //CAMBIAR POR PROCESAMIENTO JSON
            //
            oTrans1.setvEnvio(vvEnvio);
             */
            oTrans1.start();
        } catch (Exception e) {
            Toast.makeText(oApp.getApplicationContext(), "Error Posición", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Al obtener la respuesta, dibuja una marca en el mapa para
     * cada identificador
     */
    private void mostrarPosicionIds(Vector<Object>pvPosiciones) {
        try {
            //Luego, crea una marca en el mapa para cada posición
            for (int i=0; i<pvPosiciones.size(); i++) {
                RegistroLocalizacion voPos = (RegistroLocalizacion)pvPosiciones.elementAt(i);
                //Muestra la coordenada
                double vnLat = Double.parseDouble(voPos.getLocclat());
                double vnLon = Double.parseDouble(voPos.getLocclon());
                // create marker
                MarkerOptions marker = new MarkerOptions().position(new LatLng(vnLat, vnLon)).title(voPos.getLoccdor());
                // ROSE color icon
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                // adding marker
                Marker voMarca = googleMap.addMarker(marker);
                voMarca.showInfoWindow();
                if (bPrimera) {
                    //Posicionamiento de la cámara en una coordenada concreta
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            new LatLng(vnLat, vnLon)).zoom(15).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    bPrimera = false;
                }
            }
        } catch (Exception e) {
            Toast.makeText(oApp.getApplicationContext(), "Error Posición", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Método que se encarga de recorrer el vector de identificadores, para consultar acerca de
     * todas las posiciones al servidor web.
     */
    private void leerTracks() {
        try {
            //Primero, crea la orden para enviar al servidor web y obtener los tracks de cada Id
            Vector<Object> vvEnvio = new Vector<Object>();
            vvEnvio.addElement(new String("TrackMulti")); //Orden
            vvEnvio.addElement(new Integer(-1));  //Evento
            vvEnvio.addElement(new Integer(-1));  //Categoria
            for (int i=0; i<APrincipal.getvIds().size(); i++) {
                vvEnvio.addElement(APrincipal.getvIds().elementAt(i));
            }
            /*
            //
            //CAMBIAR POR PROCESAMIENTO JSON
            //
            oTrans2.setvEnvio(vvEnvio);
             */
            oTrans2.start();
        } catch (Exception e) {
            Toast.makeText(oApp.getApplicationContext(), "Error Tracks", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Al obtener la respuesta, dibuja una polilínea en el mapa para
     * cada identificador
     */
    private void mostrarTracks(Vector<Object>pvTracks) {
        try {
            //Luego, crea una polilínea en el mapa para las posiciones de cada Id
            for (int i=0; i<pvTracks.size(); i++) {
                //Para cada track
                Vector<RegistroLocalizacion> vvTrack = (Vector<RegistroLocalizacion>)pvTracks.elementAt(i);
                if (vvTrack!=null) {
                    if (vvTrack.size()>0) {
                        //Crea la polilínea
                        PolylineOptions voPoli = new PolylineOptions();
                        voPoli.width(2);
                        voPoli.color(Color.GRAY);
                        //Añade segmentos entre coordenadas a partir de las lecturas recogidas
                        for (int j=0; j<vvTrack.size(); j++) {
                            RegistroLocalizacion voPos = (RegistroLocalizacion)vvTrack.elementAt(j);
                            double vnLat = Double.parseDouble(voPos.getLocclat());
                            double vnLon = Double.parseDouble(voPos.getLocclon());
                            voPoli.add(new LatLng(vnLat, vnLon));
                        }
                        googleMap.addPolyline(voPoli);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(oApp.getApplicationContext(), "Error Tracks", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Clase interna que permite gestionar el Thread que periódicamente lee
     * las posiciones de los Ids seleccionados y los muestra en el mapa
     */
    class VerMapaRun implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = AVerMapa.GUIUPDATEIDENTIFIER2;
                AVerMapa.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardo2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    /**
     * Clase interna que permite gestionar el Thread que periódicamente lee
     * si hay algún dato nuevo proporcionado por el servidor
     */
    class HiloRun implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = AVerMapa.GUIUPDATEIDENTIFIER;
                AVerMapa.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
