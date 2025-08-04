package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;
import android.app.Application;
import android.content.res.Resources;
import android.view.View;

import jaru.ori.utils.android.UtilsAndroid;

/*
 * Cuadro de diálogo que permite a un usuario seleccionar el tipo de objeto OCAD
 * que se va a almacenar. También se puede dar una descripción.<BR>
 * Se utiliza en el editor de trabajo de campo para almacenar un nuevo elemento
 * en el Registro de objetos vectoriales.
 *
 */
public class ASelOCAD extends Activity {
    private int nTipo = 0;
    private Application oApp = null;
    private Resources oRes = null;

    private Spinner oTipos;

    /**
     * Método que se lanza cuando la actividad se ejecuta por primera vez
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
        setContentView(R.layout.selocad);
        try {
            //Resto de objetos
            oApp = APrincipal.getOApp();
            oRes = APrincipal.getORes();
            APrincipal.setBAceptar(false);
            nTipo = APrincipal.getNTipo();
        } catch (Exception e) {}
        try {
            //Inicializa la lista de tipos, para añadir el listener de selección
            this.oTipos = (Spinner)this.findViewById(R.id.lstTipo);
            this.oTipos.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    ASelOCAD.this.actualizarListaTiposOCAD();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        } catch (Exception e) {}
        //Inicializa los demás elementos GUI
        this.limpiarDatos(true);
    }

    /**
     * Método que se llama cuando la aplicación se cierra. 
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    /**
     * Método que se lanza cuando se crean las opciones de menú
     * @param menu Menu
     * @return boolean Devuelve true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.aceptarcancelar, menu);
        return true;
    }
    /**
     * Método que se lanza cuando se preparan las opciones de menú
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
     * Si el usuario selecciona aceptar, entonces se escriben los valores seleccionados
     * en las propiedades estáticas de la actividad principal y se regresa (esto
     * permite que la actividad llamante pueda recuperar los valores seleccionados aquí)<BR>
     * Si el usuario pulsa cancelar, simplemente se regresa a la pantalla anterior.
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.aceptar1) {
            APrincipal.setBAceptar(true);
            nTipo = ((Spinner) findViewById(R.id.lstTipo)).getSelectedItemPosition();
            //Tipos 3 y 4 corresponden a líneas O-Pie y líneas O-BM => tipo 1
            if (nTipo > 2)
                nTipo = 1;
            APrincipal.setNTipo(nTipo);
            APrincipal.setCTipoOCAD(this.obtenerTipoOCADSeleccionado());
            APrincipal.setCDesc(((EditText) findViewById(R.id.txtDescripcion)).getText().toString());
            this.finish();
        } else if (item.getItemId()==R.id.cancelar1) {
            APrincipal.setBAceptar(false);
            this.finish();
        }
        return true;
    }

    /**
     * Método que limpia los datos en pantalla
     */
    private void limpiarDatos (boolean pbTodos) {
        try {
            //Inicialización de la lista desplegable de Tipos
            if (pbTodos) {
                try {
                    ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                    voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00094));
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00095));
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00096));
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00111));
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00112));
                    this.oTipos.setAdapter(voAdapter1);
                } catch (Exception e) {}
                this.oTipos.setSelection(nTipo);
                actualizarListaTiposOCAD();
            }
            ((EditText)findViewById(R.id.txtDescripcion)).setText("", TextView.BufferType.EDITABLE);
        } catch (Exception e) {}
    }
    /**
     * Dada una selección de tipo de punto, actualiza el listado de tipos OCAD 
     * que corresponden a esa selección.
     */
    private void actualizarListaTiposOCAD () {
        try {
            //Dado el tipo de objeto seleccionado, calcula el valor de comienzo 
            //y de fin de los mensajes que corresponden a objetos del tipo relacionado.
            int vnTipo = ((Spinner)findViewById(R.id.lstTipo)).getSelectedItemPosition();
            int vnComienzo = 1000;
            int vnFinal = 0;
            if (vnTipo == 0) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00994));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00995));
            }
            else if (vnTipo == 1) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00996));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00997));
            }
            else if (vnTipo == 2) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00998));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00999));
            }
            else if (vnTipo == 3) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00990));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00991));
            }
            else if (vnTipo == 4) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00992));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00993));
            }
            //Inicialización de la lista desplegable de Tipos OCAD para un tipo dado
            Spinner voSpinner1 = (Spinner) findViewById(R.id.lstTipoOCAD);
            ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
            voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //Rellena los nuevos datos de la lista de tipos OCAD
            for (int i=vnComienzo; i<=vnFinal; i++) {
                int vnId = oRes.getIdentifier("ORI_ML0" + i, "string", "jaru.ori.gui.gpslog.android");
                String vcTexto = oApp.getString(vnId);
                voAdapter1.add(vcTexto);
            }
            voSpinner1.setAdapter(voAdapter1);
            voSpinner1.setSelection(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Lee los tipos seleccionados de las listas desplegables y recupera a partir 
     * de ellos el ID OCAD del objeto.
     */
    private String obtenerTipoOCADSeleccionado () {
        String vcResul = "101.0";
        try {
            //Dado el tipo de objeto seleccionado, calcula el valor de comienzo 
            //y de fin de los mensajes que corresponden a objetos del tipo relacionado.
            int vnTipo = ((Spinner)findViewById(R.id.lstTipo)).getSelectedItemPosition();
            int vnComienzo = 1000;
            if (vnTipo == 0) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00994));
            }
            else if (vnTipo == 1) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00996));
            }
            else if (vnTipo == 2) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00998));
            }
            else if (vnTipo == 3) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00990));
            }
            else if (vnTipo == 4) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00992));
            }
            int vnElemento = vnComienzo + ((Spinner)findViewById(R.id.lstTipoOCAD)).getSelectedItemPosition();
            //Recupera el texto del elemento seleccionado y se queda sólo con la 
            //parte que representa al código OCAD
            int vnId = oRes.getIdentifier("ORI_ML0" + vnElemento, "string", "jaru.ori.gui.gpslog.android");
            vcResul = oApp.getString(vnId);
            int vnPos = vcResul.indexOf(" ");
            if (vnPos>0)
                vcResul = vcResul.substring(0, vnPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcResul;
    }

}
