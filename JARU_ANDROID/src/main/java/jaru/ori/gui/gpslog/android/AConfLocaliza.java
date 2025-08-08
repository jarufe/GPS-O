package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import jaru.ori.logic.localiza.ConfLocaliza;
import jaru.ori.utils.android.UtilsAndroid;

/**
 * Edición de los parámetros de configuración de la gestión de comunicación de posicionamientos
 * <P>
 * Los parámetros son de dos tipos. El primero es la especificación
 * del servidor web con el que se comunica la aplicación. El segundo tipo de parámetros conforma
 * la identificación del sujeto sobre el que se va a guardar la información de posicionamiento
 * </p>
 * @author jarufe
 * @version 1.0
 */
public class AConfLocaliza extends Activity {
    private ConfLocaliza oConfLocaliza = null;

    /**
     * Llamado cuando la actividad se crea por primera vez. 
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        try {
            setContentView(R.layout.conflocaliza);
            oConfLocaliza = APrincipal.getoConfLocaliza();
            this.setoConfLocaliza(oConfLocaliza);
        }catch(Exception e) {
            Log.e("GPS-O", "ConfLocaliza. Error en onCreate", e);
        }
    }

    /**
     * Método que se llama cuando la aplicación se cierra. 
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Método llamado cuando se crean las opciones de menú.
     * @param menu Menu
     * @return boolean. Devuelve true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.aceptarcancelar, menu);
        return true;
    }

    /**
     * Método que se llama cuando se preparan las opciones de menú
     * @param menu Menu
     * @return boolean. Devuelve el resultado de llamar al método de super.
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        boolean vbResul = super.onPrepareOptionsMenu(menu);
        return vbResul;
    }

    /**
     * Método que se lanza cuando el usuario selecciona una opción de menú
     * <BR>
     * Si se pulsa aceptar, se guardan los parámetros y se regresa a la actividad anterior.
     * @param item MenuItem. Elemento seleccionado
     * @return boolean. Devuelve true.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.aceptar1) {
            this.actualizarConfLocaliza();
            this.finish();
        } else if (item.getItemId()==R.id.cancelar1) {
            this.finish();
        }
        return true;
    }

    /**
     * Método que lee los datos de pantalla y los asigna al objeto de la clase ConfLocaliza
     */
    private void actualizarConfLocaliza () {
        try {
            oConfLocaliza.setcServidor(((EditText)findViewById(R.id.txtServidor)).getText().toString());
            oConfLocaliza.setnPuerto(Integer.parseInt(((EditText)findViewById(R.id.txtPuerto)).getText().toString()));
            oConfLocaliza.setcServlet(((EditText)findViewById(R.id.txtServlet)).getText().toString());
            oConfLocaliza.setnRetardo(Integer.parseInt(((EditText)findViewById(R.id.txtRetardo)).getText().toString()));
            oConfLocaliza.setnEvento(Integer.parseInt(((EditText)findViewById(R.id.txtEvento)).getText().toString()));
            oConfLocaliza.setnCategoria(Integer.parseInt(((EditText)findViewById(R.id.txtCategoria)).getText().toString()));
            oConfLocaliza.setcDorsal(((EditText)findViewById(R.id.txtDorsal)).getText().toString());
            oConfLocaliza.setcNombre(((EditText)findViewById(R.id.txtNombre)).getText().toString());
        } catch (Exception e) {
            Log.e("GPS-O", "ConfLocaliza. Error en actualización de parámetros", e);
        }
    }
    /**
     * Método que establece los datos actuales de configuración.<BR>
     * @param poConfLocaliza ConfLocaliza. Objeto con los parámetros actuales.
     */
    public void setoConfLocaliza(ConfLocaliza poConfLocaliza) {
        try {
            oConfLocaliza = poConfLocaliza;
            ((EditText) findViewById(R.id.txtServidor)).setText(oConfLocaliza.getcServidor(), TextView.BufferType.EDITABLE);
            ((EditText) findViewById(R.id.txtPuerto)).setText(oConfLocaliza.getnPuerto() + "", TextView.BufferType.EDITABLE);
            ((EditText) findViewById(R.id.txtServlet)).setText(oConfLocaliza.getcServlet(), TextView.BufferType.EDITABLE);
            ((EditText) findViewById(R.id.txtRetardo)).setText(oConfLocaliza.getnRetardo() + "", TextView.BufferType.EDITABLE);
            ((EditText) findViewById(R.id.txtEvento)).setText(oConfLocaliza.getnEvento() + "", TextView.BufferType.EDITABLE);
            ((EditText) findViewById(R.id.txtCategoria)).setText(oConfLocaliza.getnCategoria() + "", TextView.BufferType.EDITABLE);
            ((EditText) findViewById(R.id.txtDorsal)).setText(oConfLocaliza.getcDorsal(), TextView.BufferType.EDITABLE);
            ((EditText) findViewById(R.id.txtNombre)).setText(oConfLocaliza.getcNombre(), TextView.BufferType.EDITABLE);
        }catch(Exception e) {
            Log.e("GPS-O", "ConfLocaliza. Error al establecer parámetros", e);
        }
    }


}
