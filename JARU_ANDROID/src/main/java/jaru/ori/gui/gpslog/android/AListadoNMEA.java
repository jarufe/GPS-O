package jaru.ori.gui.gpslog.android;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.content.Context;
import java.util.ArrayList;

import jaru.gps.logic.*;
import jaru.ori.utils.Utilidades;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Muestra lecturas contínuas que llegan por el interface de comunicaciones desde el GPS.
 * <P>
 * Esta clase es un listado que va mostrando periódicamente las lecturas que se 
 * van realizando desde el GPS. Para ello se lee un contenido amplio desde el 
 * puerto serie y se queda a la espera de lo que decida el usuario.
 * Este puede refrescar para volver a mostrar otra lectura.
 * </p>
 * Esta actividad se tiene que utilizar obligatoriamente en primer lugar, 
 * siempre que se quiera manejar un dispositivo GPS, ya que se encarga de abrir
 * el puerto de comunicaciones.
 * @author jarufe
 * @version 1.0
 */
public class AListadoNMEA extends ListActivity {
    private TextView txtVacio;
    private ListadoNMEAAdapter oAdapter;
    private ArrayList<String> oLista;

    private Parametro oParametro = null;
    private GpsInterno oGpsInterno = null;

    /**
     * Método que se llama cuando la actividad se ejecuta por primera vez.
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
        this.txtVacio = (TextView)findViewById(R.id.lblVacio);
        oLista = new ArrayList<String>();
        ListView viListView = getListView();
        viListView.setItemsCanFocus(false);
        viListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        viListView.setEmptyView(this.txtVacio);
        //Recoge objetos de la actividad principal
        oParametro = APrincipal.getOParametro();
        oGpsInterno = APrincipal.getOGpsInterno();
        //Abre el puerto de comunicaciones y lee datos
        this.realizarLectura();
    }

    /**
     * Método que se llama cuando la aplicación se cierra. 
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    /**
     * Método que se llama cuando se crean las opciones de menú.
     * @param menu Menu
     * @return boolean Devuelve true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refrescarcancelar, menu);
        return true;
    }
    /**
     * Método que se llama cuando se preparan las opciones de menú.
     * @param menu Menu
     * @return boolean Devuelve el resultado del método en super
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        boolean vbResul = super.onPrepareOptionsMenu(menu);
        return vbResul;
    }
    /**
     * Método que se lanza cuando el usuario selecciona una opción de menú.<BR>
     * Si el usuario pulsa la opción refrescar, se vuelve a realizar una lectura
     * de datos del GPS y se vuelca en pantalla.<BR>
     * Si el usuario pulsa cancelar, simplemente se sale de la actividad.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.refrescar7) {
            this.realizarLectura();
        } else if (item.getItemId()==R.id.cancelar7) {
            APrincipal.setOGpsInterno(oGpsInterno);
            this.finish();
        }
        return true;
    }
    /**
     * Método que se lanza cuando el usuario selecciona un elemento de la lista.
     * En este caso, tan solo se obtiene la cadena de texto que contiene la fila.
     * @param parent ListView
     * @param v View
     * @param position int
     * @param id long
     */
    public void onListItemClick(ListView parent, View v, int position, long id) {
        ListadoNMEAAdapter voAdapter = (ListadoNMEAAdapter) parent.getAdapter();
        String vcTexto = (String)voAdapter.getItem(position);
    }

    /**
     * Realiza una lectura de GPS y llama al método que refresca los datos en el 
     * componente.
     */
    private void realizarLectura () {
        String vcTextoParcial = "";
        try {
            //Si se usa GPS externo, se abre el puerto y se leen datos
            if (oParametro.getCGpsInterno().equals("0")) {
                if (!PuertoSerie.getBAbierto())
                    PuertoSerie.abrir(0);
                PuertoSerie.getOTicker().stop();
                String vcTexto = PuertoSerie.recibir(500);
                PuertoSerie.getOTicker().start();
                while (vcTexto.length()>0) {
                    int vnPos = vcTexto.indexOf('\n');
                    if (vnPos>0) {
                        vcTextoParcial = vcTexto.substring(0, vnPos-1);
                        vcTexto = vcTexto.substring(vnPos+1);
                    }
                    else {
                        vcTextoParcial = vcTexto;
                        vcTexto = "";
                    }
                    oLista.add(vcTextoParcial);
                }
            } else {
                //Si se usa GPS interno, se crea una nueva instancia y se lee
                if (oGpsInterno==null)
                    oGpsInterno = new GpsInterno(this);
                if (!oGpsInterno.getBPuedoObtenerPosicion()) {
                    oGpsInterno.mostrarAlertaConfiguracion();
                } else {
                    vcTextoParcial = Utilidades.obtenerHoraNMEADesdeMilisecs(oGpsInterno.getNHora()) + ": " +
                            oGpsInterno.getNLatitud() + ";" + oGpsInterno.getNLongitud();
                    oLista.add(vcTextoParcial);
                }
            }
            //Añade la lista de datos al objeto en pantalla
            if ((oLista == null) || (oLista.size() == 0)) {
                txtVacio.setText(R.string.ORI_ML00128);
            } else {
                oAdapter = new ListadoNMEAAdapter(AListadoNMEA.this, oLista);
                setListAdapter(oAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clase interna que representa al adaptador que permite gestionar el listado
     * en pantalla. En este caso se trata de una simple lista de cadenas de
     * caracteres.
     */
    public class ListadoNMEAAdapter extends BaseAdapter {

        private final String CLASSTAG = ListadoNMEAAdapter.class.getSimpleName();
        private final Context oContext;
        private final ArrayList<String> oData;
        /**
         * Constructor de la clase
         * @param poContext Context
         * @param poData ArrayList<String>
         */
        public ListadoNMEAAdapter(Context poContext, ArrayList<String> poData) {
            this.oContext = poContext;
            this.oData = poData;
        }
        /**
         * Devuelve el número de filas del listado
         * @return int
         */
        public int getCount() {
            return this.oData.size();
        }
        /**
         * Devuelve un objeto que representa al contenido de un elemento de la lista
         * @param position int Fila del listado
         * @return Object Contenido de esa fila
         */
        public Object getItem(int position) {
            return this.oData.get(position);
        }
        /**
         * Devuelve el Id del elemento de la lista que está en una determinada posición
         * @param position int
         * @return long
         */
        public long getItemId(int position) {
            return position;
        }
        /**
         * Devuelve un objeto de la clase View que representa a la vista en pantalla
         * del listado
         * @param position int
         * @param convertView View
         * @param parent ViewGroup
         * @return View
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            String voDato = this.oData.get(position);
            return new ListadoNMEAView(this.oContext, voDato);
        }
        /**
         * Clase interna que gestiona la vista en pantalla del listado
         */
        private final class ListadoNMEAView extends LinearLayout {
            private TextView txtDato;
            /**
             * Constructor de la clase. Simplemente genera un cuadro de texto y establece
             * su valor con una cadena de texto que representa a algunos datos leídos
             * del GPS.
             * @param poContext Context
             * @param pcDato String
             */
            public ListadoNMEAView(Context poContext, String pcDato) {

                super(poContext);
                setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 3, 5, 0);

                this.txtDato = new TextView(poContext);
                this.txtDato.setText(pcDato);
                this.txtDato.setTextSize(16f);
                this.addView(this.txtDato, params);
            }
        }
    }

}
