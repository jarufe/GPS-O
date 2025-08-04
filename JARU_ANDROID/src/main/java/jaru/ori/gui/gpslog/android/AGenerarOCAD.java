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

import jaru.gps.logic.*;
import java.util.Vector;
import jaru.ori.logic.gpslog.*;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Exportación de los datos vectoriales a OCAD.
 * <P>
 * Con esta clase se configura y procesa la generación de un archivo OCAD 6 ó 7 
 * a partir de los datos vectoriales almacenados hasta el momento por la aplicación.
 * Además de la versión de OCAD, también se puede configurar el tipo de sistema de coordenadas.
 * Se puede elegir entre coordenadas de papel o de mundo real. El sistema de mundo real
 * soportado es el UTM, suponiendo que los datos originales están en coordenadas geográficas
 * según el datum WGS84. El usuario podrá especificar una zona UTM. Aunque las coordenadas
 * geográficas (lat, lon) se convierten a un valor de Easting, Northing y Zona, se puede
 * forzar al sistema de exportación para que las coordenadas se expresen en una zona en 
 * concreto. Esto sólo es útil si los datos se encuentran en el límite entre dos zonas, para
 * que la conversión resulte homogénea. Si estamos trabajando en una zona en concreto,
 * lo más saludable es especificar dicha zona y no hacer inventos.<BR>
 * Para utilizar cualquier otro sistema de coordenadas, habrá que realizar un
 * procesamiento posterior con algún software que permita convertir datos entre
 * sistemas de coordenadas.
 * </p>
 * El fichero es generado con un nombre que se proporciona de forma automática y se
 * graba en el directorio que aparece especificado en los parámetros de configuración.
 * El nombre es "Mapa" + la fecha actual + la hora actual + ".ocd".
 * @author jarufe
 * @version 1.0
 */
public class AGenerarOCAD extends Activity {
    private Parametro oParametro = null;
    private Vector<Registro> vRegistros = null;
    private String cPathAplica = "";
    private Application oApp = null;
    private Resources oRes = null;

    private Spinner lstVersion;
    private Spinner lstCoordenadas;
    private Spinner lstZona;

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
        setContentView(R.layout.generarocad);
        try {
            //Recoge de la clase principal los elementos básicos que intervienen en el proceso
            oParametro = APrincipal.getOParametro();
            vRegistros = APrincipal.getVRegistros();
            cPathAplica = APrincipal.getCPathAplica();
            //Resto de objetos
            oApp = APrincipal.getOApp();
            oRes = APrincipal.getORes();
        } catch (Exception e) {}
        try {
            //Inicializa la lista de versiones de OCAD
            this.lstVersion = (Spinner)this.findViewById(R.id.lstVersion);
        } catch (Exception e) {}
        try {
            //Inicializa la lista de tipos de coordenadas, para añadir el listener de selección
            this.lstCoordenadas = (Spinner)this.findViewById(R.id.lstCoordenadas);
            this.lstCoordenadas.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    AGenerarOCAD.this.cambiarSistema();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        } catch (Exception e) {}
        try {
            //Inicializa la lista de zonas
            this.lstZona = (Spinner)this.findViewById(R.id.lstZona);
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
     * Si el usuario pulsa en aceptar, se procede a la generación del fichero OCAD.<BR>
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
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                voAdapter1.add(oApp.getString(R.string.ORI_ML00117));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00118));
                this.lstVersion.setAdapter(voAdapter1);
            } catch (Exception e) {}
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                voAdapter1.add(oApp.getString(R.string.ORI_ML00120));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00121));
                this.lstCoordenadas.setAdapter(voAdapter1);
            } catch (Exception e) {}
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (int i=1; i<=60; i++)
                    voAdapter1.add(i + "");
                this.lstZona.setAdapter(voAdapter1);
            } catch (Exception e) {}
            this.lstVersion.setSelection(0);
            this.lstCoordenadas.setSelection(0);
            this.lstZona.setSelection(28);
            this.lstZona.setEnabled(false);
        } catch (Exception e) {}
    }
    /**
     * Método que procesa la generación del mapa OCAD a partir de los parámetros configurados
     */
    private void generar () {
        try {
            int vnVersion = 6;
            int vnCoord = 0;
            String vcZona = (String)lstZona.getSelectedItem();
            int vnZona = Integer.parseInt(vcZona);
            int vnPos = lstVersion.getSelectedItemPosition();
            if (vnPos==1)
                vnVersion = 7;
            vnPos = lstCoordenadas.getSelectedItemPosition();
            if (vnPos==1)
                vnCoord = 1;
            try {
                TransfOCAD.setNEscala(Integer.parseInt(oParametro.getCEscala()));
                TransfOCAD.setNVersion(vnVersion);
                TransfOCAD.setNCoord(vnCoord);
                TransfOCAD.setNZona(vnZona);
                TransfOCAD.setCPathAplica(cPathAplica);
                TransfOCAD.setOApp(oApp);
                TransfOCAD.setORes(oRes);
            } catch (Exception e) {
            }
            boolean vbCorrecto = TransfOCAD.generarFicheroOCAD(oParametro.getCPathXML(), vRegistros);
            //Se muestra un mensaje con el resultado correcto o incorrecto del proceso.
            String vcTexto = oApp.getString(R.string.ORI_MI00007);
            if (!vbCorrecto) {
                vcTexto = oApp.getString(R.string.ORI_MI00008);
            }
            Toast.makeText(oApp.getApplicationContext(), vcTexto, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que activa o desactiva la opción de Zona UTM según el tipo
     * de sistema de coordenadas seleccionado
     */
    private void cambiarSistema () {
        try {
            int vnTipo = lstCoordenadas.getSelectedItemPosition();
            if (vnTipo==1) {
                lstZona.setEnabled(true);
            } else {
                lstZona.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
