package jaru.ori.gui.gpslog.android;

import android.app.Application;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Vector;

import jaru.gps.logic.Parametro;
import jaru.ori.logic.gpslog.Registro;
import jaru.ori.utils.android.UtilsAndroid;
import jaru.ori.web.controlcarrera.RegistroLocalizacion;
import jaru.red.logic.GestionTransmisiones;
import jaru.red.logic.HiloTransmisiones;

/*
 * Consulta con el servidor web la lista de distintos Ids que han enviado datos de posicionamiento.<BR>
 * Esta actividad permite seleccionar uno o varios Ids para que se muestre su posición en un mapa.
 * @author jarufe
 * @version 1.0
 */
public class ASelIds extends ListActivity {
    private TextView txtVacio;
    private ListadoAdapter oAdapter;
    private ArrayList<String> oLista;
    public Application oApp = null;
    public Resources oRes = null;
    private Thread oThread;
    private int nRetardo = 500;
    private HiloTransmisiones oTrans = null;
    private int nOpcionHilo = 1;  //1=consulta datos; 2=lee posición Id seleccionado
    protected static final int GUIUPDATEIDENTIFIER = 0x101;

    public HiloTransmisiones getoTrans() {
        return oTrans;
    }
    public void setoTrans(HiloTransmisiones oTrans) {
        this.oTrans = oTrans;
    }

    Handler iHandler = new Handler() {
        @Override
        public void handleMessage(Message poMsg) {
            switch (poMsg.what) {
                case ATestConn.GUIUPDATEIDENTIFIER:
                    if (oTrans!=null) {
                        if (oTrans.isbResulPreparado()) {
                            /*
                            //
                            //CAMBIAR POR PROCESAMIENTO JSON
                            //
                            Vector<Object> vvRespuesta = oTrans.getvRespuesta();
                            if (nOpcionHilo==1) {
                                //Procesa respuesta del hilo de transmisiones
                                ASelIds.this.rellenarVectorFilas(vvRespuesta);
                            } else {
                                ASelIds.this.mostrarPosicionId(vvRespuesta);
                            }
                             */
                            //Resetea el hilo
                            oTrans.setbResulPreparado(false);
                            oTrans.stop();
                        }
                    }
                    break;
            }
            super.handleMessage(poMsg);
        }
    };

    /**
     * Se llema cuando se crea la actividad por primera vez
     * @param icicle Bundle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // ToDo add your GUI initialization code here
        //Establece la orientación según el dispositivo sea más ancho (horizontal) o alto (vertical)
        /*
        if(UtilsAndroid.esPantallaAncha(this.getResources())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
         */
        setContentView(R.layout.listadonmea);
        //Resto de objetos
        oApp = APrincipal.getOApp();
        oRes = APrincipal.getORes();
        GestionTransmisiones.setcServidor(APrincipal.getoConfLocaliza().getcServidor());
        GestionTransmisiones.setnPuerto(APrincipal.getoConfLocaliza().getnPuerto());
        GestionTransmisiones.setcServlet(APrincipal.getoConfLocaliza().getcServlet());
        oTrans = new HiloTransmisiones();
        //
        this.txtVacio = (TextView)findViewById(R.id.lblVacio);
        //Solicita datos de Ids al servidor web
        this.inicializarValores();
        //Crea el nuevo hilo de ejecución y lo pone a correr para refrescar la gestión de transmisiones
        oThread = new Thread(new SelIdsRun());
        oThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(oThread!=null)
            oThread.interrupt();
        oThread = new Thread(new SelIdsRun());
        oThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(oThread!=null)
            oThread.interrupt();
    }

    /**
     * Método que se llama cuando se crean las opciones de menú
     * @param menu Menu
     * @return boolean. Devuelve true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.aceptarcancelarver, menu);
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
        return vbResul;
    }
    /**
     * Método que se lanza cuando el usuario selecciona una opción de menú.<BR>
     * Si se pulsa aceptar, se pasan los registros seleccionados a la actividad llamante.<BR>
     * Si se pulsa cancelar, se sale de la pantalla.<BR>
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.aceptar3) {
            //Al salir aceptando, se devuelven los registros seleccionados
            this.comunicarIds();
            this.finish();
        } else if (item.getItemId()==R.id.cancelar3) {
            this.finish();
        } else if (item.getItemId()==R.id.ver3) {
            this.verPrimerId();
        }
        return true;
    }
    /**
     * Método que se lanza cuando el usuario hace clic en algún elemento de la
     * lista de registros que se muestra en pantalla.<BR>
     * Del adaptador que permite gestionar la lista, se recoge el elemento que
     * ha sido seleccionado
     * @param parent LisView
     * @param v View
     * @param position int
     * @param id long
     */
    public void onListItemClick(ListView parent, View v, int position, long id) {
        ListadoAdapter voAdapter = (ListadoAdapter) parent.getAdapter();
        String voDato = (String)voAdapter.getItem(position);
    }

    /**
     * Método que realiza las tareas para rellenar la lista con datos.
     * Pide al servidor web que le comunique los distintos Ids existentes
     */
    private void inicializarValores () {
        try {
            oLista = new ArrayList<String>();
            ListView viListView = getListView();
            viListView.setItemsCanFocus(false);
            viListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            viListView.setEmptyView(this.txtVacio);
            //Abre el puerto de comunicaciones y lee datos
            this.solicitarDatos();
            //this.rellenarVectorFilas();
        } catch (Exception e) {}
    }
    /**
     * Método que se lanza cuando el usuario pulsa el botón aceptar.<BR>
     * Se devuelven los Ids seleccionados a la actividad llamante
     */
    private void comunicarIds () {
        try {
            Vector<String> vvRegistros = new Vector<String>();
            ListadoAdapter voAdapter = (ListadoAdapter) getListAdapter();
            //Recorre el array que determina si los checks de cada fila están seleccionados o no
            //Los números de fila de la tabla están siempre ordenados de menor a mayor
            for (int i=0; i<voAdapter.getASelec().length; i++) {
                if (voAdapter.getASelec()[i]==true) {
                    String vcId = ((String)voAdapter.getItem(i));
                    vvRegistros.addElement(vcId);
                }
            }
            APrincipal.setvIds(vvRegistros);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que se lanza cuando el usuario pulsa el botón ver.<BR>
     * Muestra en Google Maps la última posición conocida del primer elemento seleccionado en la lista.
     */
    private void verPrimerId () {
        try {
            boolean vbEncontrado = false;
            String vcId = "";
            ListadoAdapter voAdapter = (ListadoAdapter) getListAdapter();
            //Recorre el array que determina si los checks de cada fila están seleccionados o no
            //Los números de fila de la tabla están siempre ordenados de menor a mayor
            int i=0;
            while (i<voAdapter.getASelec().length && !vbEncontrado) {
                if (voAdapter.getASelec()[i]==true) {
                    vcId = ((String)voAdapter.getItem(i));
                    vbEncontrado = true;
                }
                i++;
            }
            //Si encontró un elemento activado, muestra sus coordenadas en Google Maps
            if (vbEncontrado)
                leerPosicionId(vcId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Consultar acerca de la posición de un Id al servidor web.
     */
    private void leerPosicionId(String pcId) {
        try {
            nOpcionHilo = 2;
            //Primero, crea la orden para enviar al servidor web y obtener las coordenadas del Id
            Vector<Object> vvEnvio = new Vector<Object>();
            vvEnvio.addElement(new String("PosicionMulti")); //Orden
            vvEnvio.addElement(new Integer(-1));  //Evento
            vvEnvio.addElement(new Integer(-1));  //Categoria
            vvEnvio.addElement(pcId);
            /*
            //
            //CAMBIAR POR PROCESAMIENTO JSON
            //
            oTrans.setvEnvio(vvEnvio);
             */
            oTrans.start();
        } catch (Exception e) {
            Toast.makeText(oApp.getApplicationContext(), "Error Posición", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Al obtener la respuesta, dibuja una marca en Google Maps a través del intent correspondiente
     */
    private void mostrarPosicionId(Vector<Object>pvPosiciones) {
        try {
            nOpcionHilo = 2;
            //Luego, crea una marca en el mapa para cada posición
            for (int i=0; i<pvPosiciones.size(); i++) {
                RegistroLocalizacion voPos = (RegistroLocalizacion)pvPosiciones.elementAt(i);
                //Muestra la coordenada
                double vnLat = Double.parseDouble(voPos.getLocclat());
                double vnLon = Double.parseDouble(voPos.getLocclon());
                //Muestra las coordenadas en Google Maps a través de un Intent
                Uri geoUri = Uri.parse("geo:" + vnLat + "," + vnLon + "?z=18");
                Intent voIntent = new Intent(Intent.ACTION_VIEW, geoUri);
                voIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(voIntent);
            }
        } catch (Exception e) {
            Toast.makeText(oApp.getApplicationContext(), "Error Posición", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Hace la solicitud de datos de Ids al servidor web
     */
    private void solicitarDatos () {
        try {
            nOpcionHilo = 1;
            //Usa la clase de gestión de transmisiones para obtener la lista de resultados
            Vector<Object>vvEnvio = new Vector<Object>();
            vvEnvio.addElement(new String("IdRegistrados"));  //Orden
            vvEnvio.addElement(new Integer(-1));  //Evento=-1
            vvEnvio.addElement(new Integer(-1));  //Categoría=-1
            /*
            //
            //CAMBIAR POR PROCESAMIENTO JSON
            //
            oTrans.setvEnvio(vvEnvio);
             */
            oTrans.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Dado un conjunto de datos resultado, conteniendo Ids, rellena la lista en pantalla
     * @param pvRegistros Vector<Object> Resultados que proceden de consulta al servidor web
     */
    private void rellenarVectorFilas (Vector<Object>pvRegistros) {
        try {
            //Ahora rellena la estructura que se verá en pantalla
            oLista = new ArrayList<String>();
            if (pvRegistros!=null) {
                if (pvRegistros.size()>0) {
                    //Recorre todos los registros de datos
                    for (int i=0; i<pvRegistros.size(); i++) {
                        //Lee el siguiente registro.
                        String voRegistro = (String)pvRegistros.elementAt(i);
                        oLista.add(voRegistro);
                    }
                }
            }
            //Añade la lista de datos al objeto en pantalla
            if ((oLista == null) || (oLista.size() == 0)) {
                txtVacio.setText(R.string.ORI_ML00128);
            } else {
                oAdapter = new ListadoAdapter(ASelIds.this, oLista);
                setListAdapter(oAdapter);
            }
        } catch (Exception e) {
            Toast.makeText(oApp.getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Clase interna que permite gestionar el Thread que periódicamente comprubea la comunicación
     */
    class SelIdsRun implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = ASelIds.GUIUPDATEIDENTIFIER;
                ASelIds.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    /**
     * Clase interna que se encarga de gestionar la forma y contenido de la
     * lista de datos que se presenta en pantalla. Se trata de un listado de elementos
     * de tipo String. Se añade un checkbox que permite seleccionar múltiples elementos
     */
    public class ListadoAdapter extends BaseAdapter {

        private final String CLASSTAG = ListadoAdapter.class.getSimpleName();
        private final Context oContext;
        private final ArrayList<String> oData;
        private boolean[] aSelec;

        /**
         * Constructor de la clase
         * @param poContext Context
         * @param poData ArrayList<String>
         */
        public ListadoAdapter(Context poContext, ArrayList<String> poData) {
            this.oContext = poContext;
            this.oData = poData;
            this.aSelec = new boolean[poData.size()];
        }
        /**
         * Devuelve un array de elementos de tipo boolean que dicen si los registros
         * correspondientes están marcados o no.
         * @return boolean[]. Array de elementos booleanos que corresponden con los elementos de la lista
         */
        public boolean[] getASelec () {
            return aSelec;
        }
        /**
         * Método que inicializa el array de valores booleanos que permite conocer
         * qué elementos de la lista están marcados.
         */
        public void initASelec () {
            aSelec = new boolean[oData.size()];
        }
        /**
         * Devuelve un ArrayList de elementos de la clase String, es decir, los
         * Ids que alimentan el listado que aparece en pantalla.
         * @return ArrayList<String>
         */
        public ArrayList<String> getOData() {
            return oData;
        }
        /**
         * Devuelve el número de elementos en la lista
         * @return int
         */
        public int getCount() {
            return this.oData.size();
        }
        /**
         * Devuelve un objeto que corresponde al elemento en una posición determinada
         * de la lista.
         * @param position int Posición de la lista
         * @return Object Objeto genérico de la lista
         */
        public Object getItem(int position) {
            return this.oData.get(position);
        }
        /**
         * Devuelve el Id del elemento que se encuentra en una determinada posición.
         * @param position int Posición de la lista
         * @return long Id
         */
        public long getItemId(int position) {
            return position;
        }
        /**
         * Añade un elemento de la clase String al listado en pantalla
         * @param item String
         */
        public void addItem(final String item) {
            oData.add(item);
            notifyDataSetChanged();
        }
        /**
         * Devuelve un objeto que corresponde a la vista en pantalla del listado.<BR>
         * Dentro de este método, básicamente se crea un objeto de la clase interna
         * que permite gestionar la interacción con el listado y también añade un
         * listener al checkbox para poder controlar qué elementos se marcan para
         * borrado.
         * @param position int
         * @param convertView View
         * @param parent ViewGroup
         * @return View
         */
        public View getView(final int position, View convertView, ViewGroup parent) {
            String voDato = this.oData.get(position);
            //return new ListadoView(this.oContext, voDato);
            ListadoView oVista = new ListadoView(this.oContext, voDato);

            final ViewHolder holder = new ViewHolder();
            holder.chkItem = oVista.getChkSel();
            holder.chkItem.setOnCheckedChangeListener(new OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    aSelec[position] = holder.chkItem.isChecked();
                }
            });
            holder.chkItem.setChecked(aSelec[position]);
            return oVista;
        }
        /**
         * Establece la marca de seleccionado/no seleccionado de un elemento determinado
         * de la lista.
         * @param i int Posición del elemento que se quiere marcar
         * @param b boolean Marca o desmarca el elemento
         */
        public void set(int i, boolean b) {
            aSelec[i] = b;
        }
        /**
         * Clase interna que permite gestionar la apariencia y contenido de los
         * elementos del listado en pantalla.<BR>
         * En este caso, se muestra un checkbox que permite seleccionar cada elemento y
         * el String con el Id
         */
        private final class ListadoView extends LinearLayout {
            private CheckBox chkSel;
            private TextView txtDesc;
            /**
             * Constructor de la clase. Añade el checkbox y el cuadro de texto que
             * contienen los datos de cada elemento de la lista
             * @param poContext Context
             * @param poDato String
             */
            public ListadoView(Context poContext, String poDato) {

                super(poContext);
                setOrientation(LinearLayout.VERTICAL);

                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 3, 5, 0);

                this.chkSel = new CheckBox(poContext);
                this.chkSel.setText(poDato);
                this.chkSel.setTextSize(16f);
                this.txtDesc = new TextView(poContext);
                this.txtDesc.setText(poDato);
                this.txtDesc.setTextSize(14f);
                this.addView(this.chkSel, params);
                this.addView(this.txtDesc, params);
            }
            /**
             * Devuelve el objeto correspondiente al checkbox, para poder inspeccionar
             * su valor
             * @return CheckBox
             */
            public CheckBox getChkSel () {
                return chkSel;
            }
        }

    }
    /**
     * Clase interna que representa a un contenedor del checkbox que se muestra
     * en cada elemento del listado
     */
    public static class ViewHolder {
        public CheckBox chkItem;
    }

}
