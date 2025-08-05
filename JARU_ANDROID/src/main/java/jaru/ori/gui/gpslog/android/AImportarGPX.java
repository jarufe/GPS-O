package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;
import android.app.Application;
import android.content.res.Resources;
import android.view.View;
import android.content.Intent;

import jaru.ori.logic.gpslog.*;
import jaru.ori.logic.gpslog.xml.*;
import jaru.ori.utils.*;
import jaru.ori.utils.android.UtilsAndroid;

import java.util.Vector;

/**
 * Importación de datos procedentes de GPS a través del formato GPX
 * <P>
 * Con esta clase se configura y procesa la importación de puntos y líneas que se han
 * grabado en un GPS y se intercambian a través del formato de archivo GPX.<BR>
 * Se puede decidir sobreescribir los registros existentes o bien añadirlos al final.<BR>
 * Se puede establecer el tipo OCAD por defecto para puntos y para líneas.<BR>
 * Si los datos se han grabado en el GPS indicando el tipo OCAD en algún campo (name o desc),
 * se puede seleccionar el campo adecuado para que ya queden caracterizados cuando se importen.
 * </p>
 * Trabajando de una forma un poco metódica se pueden obtener resultados óptimos con
 * poco esfuerzo posterior. Muchos dispositivos GPS permiten escribir un texto asociado al 
 * nombre y descripción del waypoint o track que están recogiendo.<BR>
 * Cuando una persona se encuentra en el campo tomando muestras durante algunas horas, 
 * es imposible que recuerde la caracterización de todos y cada uno de los elementos
 * que ha grabado. Sin embargo, sería muy interesante que desde los datos almacenados en 
 * formato GPX se pudiera obtener el objeto OCAD correspondiente a la caracterización
 * del elemento.<BR>
 * Si el usuario almacena un waypoint o track especificando el tipo OCAD al que corresponde, 
 * en el campo nombre o descripción, mediante esta actividad se puede tratar de
 * realizar una conversión correcta. Para eso están los controles que permiten 
 * especificar dónde se encuentra descrito el tipo OCAD. En caso contrario, también 
 * se le puede decir a esta actividad que todo registro que no sea capaz de identificar
 * correctamente, sea importado como un tipo OCAD por defecto.
 * @author jarufe
 * @version 1.0
 */
public class AImportarGPX extends Activity {
    private Vector<Registro> vRegistros = null;
    private Application oApp = null;
    private Resources oRes = null;

    private Button botExaminar;
    private Spinner lstTipoOcadEn;
    private Spinner lstPuntos;
    private Spinner lstLineas;
    private Spinner lstSobreescribir;

    final int ACTIVITY_FILECHOOSER = 8;

    /**
     * Método que se llama la primera vez que se ejecuta la actividad.
     * @param icicle Bundle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // ToDo add your GUI initialization code here        
        setContentView(R.layout.importargpx);
        try {
            //Recoge de la clase principal los elementos básicos que intervienen en el proceso
            vRegistros = APrincipal.getVRegistros();
            //Resto de objetos
            oApp = APrincipal.getOApp();
            oRes = APrincipal.getORes();
            //Establece el valor del directorio inicial donde realizar la selección de fichero
            Utilidades.setCDirActual(APrincipal.getOParametro().getCPathXML());
            Utilidades.setCFicheroSel("");
            Utilidades.setCFicheroSelNombre("");
        } catch (Exception e) {
            Log.e("GPS-O", "Error inicializando actividad AImportarGPX", e);
        }
        try {
            //Inicializa el botón para seleccionar un archivo
            this.botExaminar = this.findViewById(R.id.botFichero);
            this.botExaminar.setOnClickListener(new android.view.View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    AImportarGPX.this.abrirFichero();
                }
            });
        } catch (Exception e) {
            Log.e("GPS-O", "Error inicializando botón de selección de archivo", e);
        }
        try {
            //Inicializa las listas
            this.lstTipoOcadEn = this.findViewById(R.id.lstTipoOcadEn);
            this.lstPuntos = this.findViewById(R.id.lstPuntos);
            this.lstLineas = this.findViewById(R.id.lstLineas);
            this.lstSobreescribir = this.findViewById(R.id.lstSobreescribir);
        } catch (Exception e) {
            Log.e("GPS-O", "Error inicializando listas", e);
        }
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
     * Método que se llama cuando se crean las opciones de menú.
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
     * Método que se llama cuando se preparan las opciones de menú.
     * @param menu Menu
     * @return boolean Devuelve el resultado del método en super.
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    /**
     * Método que se lanza cuando el usuario selecciona una opción de menú.<BR>
     * Si el usuario pulsa aceptar, entonces se procede a realizar la importación
     * del fichero seleccionado por el usuario.<BR>
     * Si se pulsa cancelar, simplemente se sale de esta pantalla.
     * @param item MenuItem Elemento de menú
     * @return boolean Flag para indicar si se ha seleccionado un elemento de menú
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.aceptar1) {
            this.importar();
            this.finish();
        } else if (item.getItemId()==R.id.cancelar1) {
            this.finish();
        }
        return true;
    }
    /**
     * Método que se lanza cuando se regresa de la actividad que se puede llamar
     * desde ésta. En este caso, se trata de dejar al usuario que seleccione un
     * archivo (que será el que se importe posteriormente).
     * @param requestCode int
     * @param resultCode int
     * @param data Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_FILECHOOSER:
                ((EditText)findViewById(R.id.txtFichero)).setText(Utilidades.getCFicheroSel(), TextView.BufferType.EDITABLE);
                break;
        }
    }

    /**
     * Método que limpia los datos en pantalla
     */
    private void limpiarDatos () {
        try {
            //Inicialización de las listas
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                voAdapter1.add(oApp.getString(R.string.ORI_ML00126));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00127));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00128));
                this.lstTipoOcadEn.setAdapter(voAdapter1);
            } catch (Exception e) {
                Log.e("GPS-O", "Error limpinado lista de tipo OCAD", e);
            }
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                voAdapter1.add(oApp.getString(R.string.ORI_ML00001));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00002));
                this.lstSobreescribir.setAdapter(voAdapter1);
            } catch (Exception e) {
                Log.e("GPS-O", "Error limpiando lista de sobreescribir", e);
            }
            //Inicializa las listas desplegables de objetos puntuales y lineales
            int vnComienzo1 = Integer.parseInt(oApp.getString(R.string.ORI_ML00994));
            int vnFinal1 = Integer.parseInt(oApp.getString(R.string.ORI_ML00995));
            int vnComienzo2 = Integer.parseInt(oApp.getString(R.string.ORI_ML00996));
            int vnFinal2 = Integer.parseInt(oApp.getString(R.string.ORI_ML00997));
            //Rellena los nuevos datos de las listas de tipos OCAD
            //Inicialización de la lista desplegable de Tipos OCAD puntuales
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Rellena los nuevos datos de la lista de tipos OCAD
                for (int i=vnComienzo1; i<=vnFinal1; i++) {
                    int vnId = oRes.getIdentifier("ORI_ML0" + i, "string", "jaru.ori.gui.gpslog.android");
                    String vcTexto = oApp.getString(vnId);
                    voAdapter1.add(vcTexto);
                }
                lstPuntos.setAdapter(voAdapter1);
            } catch (Exception e) {
                Log.e("GPS-O", "Error limpiando lista de puntos", e);
            }
            //Inicialización de la lista desplegable de Tipos OCAD lineales
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Rellena los nuevos datos de la lista de tipos OCAD
                for (int i=vnComienzo2; i<=vnFinal2; i++) {
                    int vnId = oRes.getIdentifier("ORI_ML0" + i, "string", "jaru.ori.gui.gpslog.android");
                    String vcTexto = oApp.getString(vnId);
                    voAdapter1.add(vcTexto);
                }
                lstLineas.setAdapter(voAdapter1);
            } catch (Exception e) {
                Log.e("GPS-O", "Error limpiando lista de líneas", e);
            }
            //Selecciona los elementos por defecto
            try {
                this.lstTipoOcadEn.setSelection(2);
                this.lstPuntos.setSelection(35);
                this.lstLineas.setSelection(31);
                this.lstSobreescribir.setSelection(1);
                ((EditText)findViewById(R.id.txtFichero)).setText("", TextView.BufferType.EDITABLE);
            } catch (Exception e) {
                Log.e("GPS-O", "Error realizando selección de listas por defecto", e);
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error general en la limpieza de datos", e);
        }
    }
    /**
     * Método que procesa la importación de un fichero GPX con la parametrización
     * establecida en el cuadro de diálogo.
     */
    private void importar () {
        boolean vbSobreescribir = false;
        String vcCampo = "2";
        String vcPuntos = "";
        String vcLineas = "";
        try {
            //Obtiene los valores de los parámetros seleccionables por el usuario
            int vnTipo = lstTipoOcadEn.getSelectedItemPosition();
            int vnSobreescribir = lstSobreescribir.getSelectedItemPosition();
            if (vnSobreescribir==0)
                vbSobreescribir = true;
            if (vnTipo==0)
                vcCampo = "0";
            else if (vnTipo==1)
                vcCampo = "1";
            vcPuntos = lstPuntos.getSelectedItem().toString().substring(0, 5);
            vcLineas = lstLineas.getSelectedItem().toString().substring(0, 5);
            //Si existe el fichero de datos, lo carga.
            String vcFichero = ((EditText)findViewById(R.id.txtFichero)).getText().toString();
            if (UtilsAndroid.existeFicheroPublico(this, APrincipal.getOParametro().getCPathXML(), vcFichero)) {
                //Recupera los datos que se encuentran en los ficheros XML.
                vRegistros = RegistrosGpxXMLHandler.obtenerDatosXML(oApp.getApplicationContext(),
                        APrincipal.getOParametro().getCPathXML(), vcFichero, vRegistros, vbSobreescribir,
                        vcCampo, vcPuntos, vcLineas);
            }
            boolean vbCorrecto = !vRegistros.isEmpty();
            //Se muestra un mensaje con el resultado correcto o incorrecto del proceso.
            String vcTexto = oApp.getString(R.string.ORI_MI00004);
            if (!vbCorrecto) {
                vcTexto = oApp.getString(R.string.ORI_MI00005);
            }
            Toast.makeText(oApp.getApplicationContext(), vcTexto, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("GPS-O", "Error importando", e);
        }
    }
    /**
     * Método que permite seleccionar un archivo a partir de un cuadro de diálogo.
     */
    private void abrirFichero () {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.utils.android.FileChooser.class);
            startActivityForResult(viIntent, ACTIVITY_FILECHOOSER);
        } catch (Exception e) {
            Log.e("GPS-O", "Error llamando a actividad para abrir fichero", e);
        }
    }


}
