package jaru.ori.gui.gpslog.android;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.content.Context;

import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import jaru.gps.logic.*;
import jaru.ori.logic.gpslog.Registro;
import jaru.ori.utils.android.UtilsAndroid;

/**
 * Muestra los últimos registros almacenados para permitir al usuario borrar
 * de forma controlada uno o más de ellos.<BR>
 * Esta actividad se puede llamar desde el menú de gestión de la aplicación, pero 
 * también desde el editor de trabajo de campo. Así, si el usuario ha cometido
 * un error durante el trabajo de campo puede repararlo sin tener que salir 
 * de la pantalla de edición.
 * @author jarufe
 * @version 1.0
 */
public class AEliminarRegistros extends AppCompatActivity {
    private TextView txtVacio;
    private RecyclerView recyclerView;
    private NMEASelectableAdapter adapter;
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
        setContentView(R.layout.listadonmea);
        //Recoge de la clase principal los elementos básicos que intervienen en el proceso
        oParametro = APrincipal.getOParametro();
        vRegistros = APrincipal.getVRegistros();
        //Crea el vector que va a ir conteniendo los ID de los registros eliminados por el usuario.
        vEliminados = new Vector<String>();
        //
        this.txtVacio = (TextView)findViewById(R.id.lblVacio);
        recyclerView = findViewById(R.id.recyclerNMEA);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        return super.onPrepareOptionsMenu(menu);
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
        // Handle item selection
        if (item.getItemId()==R.id.aceptar2) {
            //Al salir aceptando, se eliminan definitivamente todos los registros quitados
            this.eliminarRegistros();
            this.finish();
            return true;
        } else if (item.getItemId()==R.id.cancelar2) {
            this.finish();
            return true;
        } else if (item.getItemId()==R.id.eliminar2) {
            //Quita temporalmente de la lista de registros aquéllos que han sido seleccionados
            this.quitarRegistros();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Método que realiza las tareas para rellenar la lista con datos. La configura
     * y lee los datos de Registros vectoriales para darle contenido.
     */
    private void inicializarValores () {
        try {
            oLista = new ArrayList<Registro>();
            //Abre el puerto de comunicaciones y lee datos
            this.rellenarVectorFilas();
            if (oLista.isEmpty()) {
                txtVacio.setText(R.string.ORI_ML00128);
                txtVacio.setVisibility(View.VISIBLE);
            } else {
                txtVacio.setVisibility(View.GONE);
                adapter = new NMEASelectableAdapter(oLista);
                recyclerView.setAdapter(adapter);
            }
        } catch (Exception e) {}
    }
    /**
     * Método que se lanza cuando el usuario pulsa el botón eliminar.<BR>
     * Se borran del conjunto de registros los que están seleccionados en la tabla.
     */
    private void quitarRegistros () {
        try {
            if (adapter != null) {
                List<Registro> seleccionados = adapter.getSeleccionados();
                for (Registro r : seleccionados) {
                    vEliminados.add(r.getCID());
                    oLista.remove(r);
                }
                //Vuelve a rellenar la lista con otros elementos, sin tener en cuenta los ya borrados
                this.inicializarValores();
            }
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
            if (vRegistros != null && !vRegistros.isEmpty()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que se lanza cuando el usuario acepta los cambios que ha realizado.<BR>
     * Se encarga de eliminar del vector de registros los elementos que han sido eliminados
     * de la tabla.
     */
    public void eliminarRegistros() {
        if (vRegistros != null && vEliminados != null && !vEliminados.isEmpty()) {
            for (String vcIdActual : vEliminados) {
                vRegistros.removeIf(r -> r.getCID().equals(vcIdActual));
            }
        }
    }
}
