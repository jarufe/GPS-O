package jaru.ori.gui.gpslog.android;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.content.Context;
import java.util.Vector;
import java.util.ArrayList;
import android.widget.CompoundButton.OnCheckedChangeListener;

import jaru.gps.logic.*;
import jaru.ori.logic.gpslog.Registro;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Muestra los últimos registros almacenados para permitir al usuario borrar
 * de forma controlada uno o más de ellos.<BR>
 * Esta actividad se puede llamar desde el menú de gestión de la aplicación, pero 
 * también desde el editor de trabajo de campo. Así, si el usuario ha cometido
 * un error durante el trabajo de campo puede repararlo sin tener que salir 
 * de la pantalla de edición.
 * @author jarufe
 * @version 1.0
 */
public class AEliminarRegistros extends ListActivity {
    private TextView txtVacio;
    private ListadoAdapter oAdapter;
    private ArrayList<Registro> oLista;

    private Parametro oParametro = null;
    private Vector<Registro> vRegistros = null;

    private Vector<String> vEliminados = null;

    private final int MAX_LISTA = 20;

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
        //Recoge de la clase principal los elementos básicos que intervienen en el proceso
        oParametro = APrincipal.getOParametro();
        vRegistros = APrincipal.getVRegistros();
        //Crea el vector que va a ir conteniendo los ID de los registros eliminados por el usuario.
        vEliminados = new Vector<String>();
        //
        this.txtVacio = (TextView)findViewById(R.id.lblVacio);
        //Rellena la lista con datos
        this.inicializarValores();
    }

    /**
     * Método que se llama cuando la aplicación se cierra. 
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
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
        inflater.inflate(R.menu.aceptarcancelareliminar, menu);
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
     * Si se pulsa aceptar, se eliminan los registros que ha sido seleccionados.<BR>
     * Si se pulsa cancelar, se sale de la pantalla.<BR>
     * Si se pulsa eliminar, quita temporalmente de la lista de registros aquéllos
     * que han sido seleccionados.
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.aceptar2) {
            //Al salir aceptando, se eliminan definitivamente todos los registros quitados
            this.eliminarRegistros();
            this.finish();
        } else if (item.getItemId()==R.id.cancelar2) {
            this.finish();
        } else if (item.getItemId()==R.id.eliminar2) {
            //Quita temporalmente de la lista de registros aquéllos que han sido seleccionados
            this.quitarRegistros();
        }
        return true;
    }
    /**
     * Método que se lanza cuando el usuario hace clic en algún elemento de la 
     * lista de registros que se muestra en pantalla.<BR>
     * Del adaptador que permite gestionar la lista, se recoge el elemento que
     * ha sido seleccionado, como objeto de la clase Registro
     * @param parent LisView
     * @param v View
     * @param position int
     * @param id long
     */
    public void onListItemClick(ListView parent, View v, int position, long id) {
        ListadoAdapter voAdapter = (ListadoAdapter) parent.getAdapter();
        Registro voDato = (Registro)voAdapter.getItem(position);
    }

    /**
     * Método que realiza las tareas para rellenar la lista con datos. La configura
     * y lee los datos de Registros vectoriales para darle contenido.
     */
    private void inicializarValores () {
        try {
            oLista = new ArrayList<Registro>();
            ListView viListView = getListView();
            viListView.setItemsCanFocus(false);
            viListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            viListView.setEmptyView(this.txtVacio);
            //Abre el puerto de comunicaciones y lee datos
            this.rellenarVectorFilas();
        } catch (Exception e) {}
    }
    /**
     * Método que se lanza cuando el usuario pulsa el botón eliminar.<BR>
     * Se borran del conjunto de registros los que están seleccionados en la tabla.
     */
    private void quitarRegistros () {
        try {
            ListadoAdapter voAdapter = (ListadoAdapter) getListAdapter();
            //Recorre el array que determina si los checks de cada fila están seleccionados o no
            //Los números de fila de la tabla están siempre ordenados de menor a mayor
            //Se recorre de mayor a menor para poder extraer del vector de datos fácilmente
            for (int i=voAdapter.getASelec().length-1; i>=0; i--) {
                if (voAdapter.getASelec()[i]==true) {
                    String vcId = ((Registro)voAdapter.getItem(i)).getCID();
                    vEliminados.addElement(vcId);
                    voAdapter.getOData().remove(i);
                }
            }
            //Vuelve a rellenar la lista con otros elementos, sin tener en cuenta los ya borrados
            this.inicializarValores();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Dado un conjunto de registros, crea el conjunto que alimenta a la tabla
     */
    private void rellenarVectorFilas () {
        String vcIdActual = "";
        int vnCuenta = 0;
        try {
            oLista = new ArrayList<Registro>();
            if (vRegistros!=null) {
                if (vRegistros.size()>0) {
                    //Recorre todos los registros de datos, de moderno a antigüo
                    for (int i=vRegistros.size()-1; i>=0; i--) {
                        //Lee el siguiente registro.
                        Registro voRegistro = (Registro)vRegistros.elementAt(i);
                        //Si cambia el objeto, lo añade al vector de filas de la tabla
                        if (!voRegistro.getCID().equals(vcIdActual)) {
                            //Actualiza el valor de Id actual.
                            vcIdActual = voRegistro.getCID();
                            //No muestra todos los registros; sólo los más modernos
                            //Siempre que no hayan sido ya eliminados
                            if (vnCuenta < MAX_LISTA && !vEliminados.contains(vcIdActual)) {
                                oLista.add(voRegistro);
                                vnCuenta++;
                            }
                        }
                    }
                }
            }
            //Añade la lista de datos al objeto en pantalla
            if ((oLista == null) || (oLista.size() == 0)) {
                txtVacio.setText(R.string.ORI_ML00128);
            } else {
                oAdapter = new ListadoAdapter(AEliminarRegistros.this, oLista);
                setListAdapter(oAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que se lanza cuando el usuario acepta los cambios que ha realizado.<BR>
     * Se encarga de eliminar del vector de registros los elementos que han sido eliminados
     * de la tabla.
     */
    private void eliminarRegistros () {
        try {
            if (vRegistros!=null && vEliminados!=null) {
                if (vRegistros.size()>0 && vEliminados.size()>0) {
                    //Recorre todos los datos eliminados
                    for (int i=0; i<vEliminados.size(); i++) {
                        //Lee el siguiente ID de elemento a eliminar.
                        String vcIdActual = (String)vEliminados.elementAt(i);
                        //Busca todos los registros que tengan este ID y los elimina del conjunto
                        int j = 0;
                        while (j<vRegistros.size()) {
                            //Lee el siguiente registro.
                            Registro voRegistro = (Registro)vRegistros.elementAt(j);
                            //Si coincide el ID, lo elimina
                            //Sino, simplemente incrementa el contador para continuar con el siguiente registro.
                            if (voRegistro.getCID().equals(vcIdActual)) {
                                vRegistros.removeElementAt(j);
                            } else {
                                j++;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Clase interna que se encarga de gestionar la forma y contenido de la
     * lista de datos que se presenta en pantalla. Se trata de un listado de elementos
     * de la clase Registro, de los cuales se muestran varios datos básicos: tipo
     * OCAD, descripción. Se añade un checkbox que permite seleccionar los elementos
     * que se van a borrar.
     */
    public class ListadoAdapter extends BaseAdapter {

        private final String CLASSTAG = ListadoAdapter.class.getSimpleName();
        private final Context oContext;
        private final ArrayList<Registro> oData;
        private boolean[] aSelec;

        /**
         * Constructor de la clase
         * @param poContext Context
         * @param poData ArrayList<Registro>
         */
        public ListadoAdapter(Context poContext, ArrayList<Registro> poData) {
            this.oContext = poContext;
            this.oData = poData;
            this.aSelec = new boolean[poData.size()];
        }
        /**
         * Devuelve un array de elementos de tipo boolean que dicen si los registros
         * correspondientes están marcados para ser borrados o no.
         * @return boolean[]. Array de elementos booleanos que corresponden con los elementos de la lista
         */
        public boolean[] getASelec () {
            return aSelec;
        }
        /**
         * Método que inicializa el array de valores booleanos que permite conocer
         * qué elementos de la lista están marcados para su eliminación.
         */
        public void initASelec () {
            aSelec = new boolean[oData.size()];
        }
        /**
         * Devuelve un ArrayList de elementos de la clase Registro, es decir, los
         * registros que alimentan el listado que aparece en pantalla.
         * @return ArrayList<Registro>
         */
        public ArrayList<Registro> getOData() {
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
         * Añade un elemento de la clase Registro al listado en pantalla
         * @param item Registro
         */
        public void addItem(final Registro item) {
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
            Registro voDato = this.oData.get(position);
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
         * En este caso, se muestra un checkbox que permite seleccionar cada elemento,
         * el Id del registro, el tipo OCAD, y la descripción
         */
        private final class ListadoView extends LinearLayout {
            private CheckBox chkSel;
            private TextView txtDesc;
            /**
             * Constructor de la clase. Añade el checkbox y el cuadro de texto que
             * contienen los datos de cada elemento de la lista
             * @param poContext Context
             * @param poDato Registro
             */
            public ListadoView(Context poContext, Registro poDato) {

                super(poContext);
                setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 3, 5, 0);

                this.chkSel = new CheckBox(poContext);
                this.chkSel.setText(poDato.getCID() + ", " + poDato.getCTipoOCAD());
                this.chkSel.setTextSize(16f);
                this.txtDesc = new TextView(poContext);
                this.txtDesc.setText(poDato.getCDesc());
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
