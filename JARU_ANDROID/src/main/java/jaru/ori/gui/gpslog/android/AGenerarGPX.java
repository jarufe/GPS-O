package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Vector;

import jaru.gps.logic.Parametro;
import jaru.ori.logic.gpslog.Registro;
import jaru.ori.logic.gpslog.TransfOCAD;
import jaru.ori.logic.gpslog.xml.RegistrosGpxXMLHandler;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Exportación de los datos vectoriales a GPX.
 * <P>
 * Con esta clase se configura y procesa la generación de un archivo GPX
 * a partir de los datos vectoriales almacenados hasta el momento por la aplicación.
 * Se puede elegir un parámetro, relacionado con la funcionalidad para crear caminos de forma dual.
 * Dicha funcionalidad permite coger caminos estableciendo un tipo OCAD principal (normalmente, un tipo de camino)
 * y un tipo OCAD dual, como objeto de OBM.
 * Para generar el fichero GPX, el usuario puede decidir que los registros duales se escriban en el GPX
 * usando el identificador del tipo principal o bien el del tipo dual OBM.
 * </p>
 * El fichero es generado con un nombre fijo "Registros.xml" y se
 * graba en el directorio que aparece especificado en los parámetros de configuración.
 * @author jarufe
 * @version 1.0
 */
public class AGenerarGPX extends Activity {
    private Parametro oParametro = null;
    private Vector<Registro> vRegistros = null;
    private String cPathAplica = "";
    private Application oApp = null;
    private Resources oRes = null;

    public CheckBox chkUsarDual;

    /**
     * Método que se llama durante la primera ejecución de la actividad.
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
        setContentView(R.layout.generargpx);
        try {
            //Recoge de la clase principal los elementos básicos que intervienen en el proceso
            oParametro = APrincipal.getOParametro();
            vRegistros = APrincipal.getVRegistros();
            cPathAplica = APrincipal.getCPathAplica();
            //Resto de objetos
            oApp = APrincipal.getOApp();
            oRes = APrincipal.getORes();
            this.chkUsarDual = (CheckBox)this.findViewById(R.id.chkUsarDual);
        } catch (Exception e) {}
        //Inicializa los demás elementos GUI
        this.limpiarDatos();
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
        inflater.inflate(R.menu.aceptarcancelar, menu);
        return true;
    }
    /**
     * Método que se llama cuando se preparan las opciones de menú
     * @param menu Menu
     * @return boolean Devuelve el resultado del método en super
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        boolean vbResul = super.onPrepareOptionsMenu(menu);
        return vbResul;
    }
    /**
     * Método que se llama cuando el usuario selecciona una opción de menú.<BR>
     * Si el usuario pulsa en aceptar, se procede a la generación del fichero GPX.<BR>
     * Si el usuario pulsa en cancelar, simplemente se sale de la pantalla.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.aceptar1) {
            this.generar();
            this.finish();
        } else if (item.getItemId()==R.id.cancelar1) {
            this.finish();
        }
        return true;
    }

    /**
     * Método que limpia los datos en pantalla
     */
    private void limpiarDatos () {
        try {
            this.chkUsarDual.setChecked(false);
        } catch (Exception e) {}
    }
    /**
     * Método que procesa la generación del fichero GPX a partir de los parámetros configurados
     */
    private void generar () {
        try {
            String vcMensaje = "";
            String vcPathDatos = "";
            boolean vbCorrecto = true;
            boolean vbDual = false;
            if (this.chkUsarDual.isChecked())
                vbDual = true;
            else
                vbDual = false;
            try {
                if (oParametro!=null)
                    vcPathDatos = oParametro.getCPathXML();
                if (vRegistros!=null)
                    RegistrosGpxXMLHandler.escribirXML(vRegistros, vcPathDatos + "Registros.gpx", vbDual);
            } catch (Exception e) {
                e.printStackTrace();
                vbCorrecto = false;
            }
            if (vbCorrecto)
                vcMensaje = this.getString(R.string.ORI_MI00009);
            else
                vcMensaje = this.getString(R.string.ORI_MI00010);
            //Se muestra un mensaje con el resultado correcto o incorrecto del proceso.
            Toast.makeText(this.getApplicationContext(), vcMensaje, Toast.LENGTH_LONG).show();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
