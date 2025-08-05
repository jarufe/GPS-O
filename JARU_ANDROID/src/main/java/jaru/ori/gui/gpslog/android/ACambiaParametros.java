package jaru.ori.gui.gpslog.android;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;

import jaru.gps.logic.*;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Edición de los parámetros de configuración de la aplicación.
 * <P>
 * Los parámetros de la aplicación son de dos tipos. El primero es la especificación 
 * del path donde se encuentra instalada la aplicación. Esto permite recuperar 
 * los parámetros iniciales de configuración. El segundo tipo de parámetros conforma 
 * la descripción del puerto de comunicaciones donde se va a conectar el GPS.
 * </p>
 * @author jarufe
 * @version 1.0
 */
public class ACambiaParametros extends Activity {
    private Parametro oParametro = null;
    public CheckBox chkGpsInterno;

    /**
     * Llamado cuando la actividad se crea por primera vez. 
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
        setContentView(R.layout.cambiaparametros);
        this.chkGpsInterno = (CheckBox)this.findViewById(R.id.chkGpsInterno);
        oParametro = APrincipal.getOParametro();
        this.setOParametro(oParametro);
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
            this.actualizarParametro();
            this.finish();
        } else if (item.getItemId()==R.id.cancelar1) {
            this.finish();
        }
        return true;
    }

    /**
     * Método que lee los datos de pantalla y los asigna al objeto de la clase Parametro
     */
    private void actualizarParametro () {
        try {
            oParametro.setCPuerto(((Spinner)findViewById(R.id.spnPuerto)).getSelectedItem().toString().replace(" (NOT FOUND)", ""));
            if (this.chkGpsInterno.isChecked())
                oParametro.setCGpsInterno("1");
            else
                oParametro.setCGpsInterno("0");
            oParametro.setCBaudios((String)(((Spinner)findViewById(R.id.lstBaudios)).getSelectedItem()));
            oParametro.setCBitsPalabra((String)(((Spinner)findViewById(R.id.lstBitsPalabra)).getSelectedItem()));
            oParametro.setCBitsStop((String)(((Spinner)findViewById(R.id.lstBitsStop)).getSelectedItem()));
            oParametro.setCParidad((String)(((Spinner)findViewById(R.id.lstParidad)).getSelectedItem()));
            String vcPath = "";
            try {
                vcPath = ((EditText)findViewById(R.id.txtPath)).getText().toString();
                if (!vcPath.endsWith("/"))
                    vcPath = vcPath + "/";
            } catch (Exception e) {}
            oParametro.setCPathXML(vcPath);
            oParametro.setCEscala((String)(((Spinner)findViewById(R.id.lstEscala)).getSelectedItem()));
            oParametro.setCTick((String)(((Spinner)findViewById(R.id.lstTick)).getSelectedItem()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que establece los datos actuales de configuración.<BR>
     * Se aprovecha para inicializar las listas de datos que aparecen en pantalla.
     * @param poParametro Parametro. Objeto con los parámetros actuales.
     */
    public void setOParametro(Parametro poParametro) {
        oParametro = poParametro;
        //----------COMIENZO DE LA INICIALIZACIÓN DE LA LISTA DE DISPOSITIVOS BLUETOOTH
        Spinner spnPuerto = findViewById(R.id.spnPuerto);
        ArrayList<String> listaDispositivos = new ArrayList<>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                String vcMensaje = this.getString(R.string.ORI_MI00019);
                Toast.makeText(this.getApplicationContext(), vcMensaje, Toast.LENGTH_LONG).show();
            } else {
                Set<BluetoothDevice> dispositivosEmparejados = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : dispositivosEmparejados) {
                    listaDispositivos.add(device.getName());
                }
            }
        }

        // Añadir el nombre del puerto guardado si no está en la lista
        String puertoGuardado = oParametro.getCPuerto();
        if (!listaDispositivos.contains(puertoGuardado)) {
            listaDispositivos.add(puertoGuardado + " (NOT FOUND)");
        }

        // Adaptador personalizado para marcar en rojo si no está disponible
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaDispositivos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (getItem(position).contains("(NOT FOUND)")) {
                    textView.setTextColor(Color.RED);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (getItem(position).contains("(NOT FOUND)")) {
                    textView.setTextColor(Color.RED);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPuerto.setAdapter(adapter);
        // Seleccionar el valor guardado
        for (int i = 0; i < listaDispositivos.size(); i++) {
            if (listaDispositivos.get(i).contains(puertoGuardado)) {
                spnPuerto.setSelection(i);
                break;
            }
        }
        //----------FIN DE LA INICIALIZACIÓN DE LA LISTA DE DISPOSITIVOS BLUETOOTH

        //Inicialización del check que indica si se usa GPS interno
        if (oParametro.getCGpsInterno().equals("0"))
            this.chkGpsInterno.setChecked(false);
        else
            this.chkGpsInterno.setChecked(true);
        //Inicialización de la lista desplegable de Baudios
        try {
            Spinner voSpinner1 = (Spinner) findViewById(R.id.lstBaudios);
            ArrayAdapter<CharSequence> voAdapter1 = ArrayAdapter.createFromResource(this, R.array.ORI_ML00180, android.R.layout.simple_spinner_item);
            voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            voSpinner1.setAdapter(voAdapter1);
            int vnPos = voAdapter1.getPosition(oParametro.getCBaudios());
            voSpinner1.setSelection(vnPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Inicialización de la lista desplegable de Bits por palabra
        try {
            Spinner voSpinner2 = (Spinner) findViewById(R.id.lstBitsPalabra);
            ArrayAdapter<CharSequence> voAdapter2 = ArrayAdapter.createFromResource(this, R.array.ORI_ML00182, android.R.layout.simple_spinner_item);
            voAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            voSpinner2.setAdapter(voAdapter2);
            int vnPos = voAdapter2.getPosition(oParametro.getCBitsPalabra());
            voSpinner2.setSelection(vnPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Inicialización de la lista desplegable de Bits de stop
        try {
            Spinner voSpinner3 = (Spinner) findViewById(R.id.lstBitsStop);
            ArrayAdapter<CharSequence> voAdapter3 = ArrayAdapter.createFromResource(this, R.array.ORI_ML00184, android.R.layout.simple_spinner_item);
            voAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            voSpinner3.setAdapter(voAdapter3);
            int vnPos = voAdapter3.getPosition(oParametro.getCBitsStop());
            voSpinner3.setSelection(vnPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Inicialización de la lista desplegable de paridad
        try {
            Spinner voSpinner4 = (Spinner) findViewById(R.id.lstParidad);
            ArrayAdapter<CharSequence> voAdapter4 = ArrayAdapter.createFromResource(this, R.array.ORI_ML00186, android.R.layout.simple_spinner_item);
            voAdapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            voSpinner4.setAdapter(voAdapter4);
            int vnPos = voAdapter4.getPosition(oParametro.getCParidad());
            voSpinner4.setSelection(vnPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Inicialización del cuadro de texto del Path XML
        ((EditText)findViewById(R.id.txtPath)).setText(oParametro.getCPathXML(), TextView.BufferType.EDITABLE);
        //Inicialización de la lista desplegable de escala
        try {
            Spinner voSpinner5 = (Spinner) findViewById(R.id.lstEscala);
            ArrayAdapter<CharSequence> voAdapter5 = ArrayAdapter.createFromResource(this, R.array.ORI_ML00188, android.R.layout.simple_spinner_item);
            voAdapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            voSpinner5.setAdapter(voAdapter5);
            int vnPos = voAdapter5.getPosition(oParametro.getCEscala());
            voSpinner5.setSelection(vnPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Inicialización de la lista desplegable de tick
        try {
            Spinner voSpinner6 = (Spinner) findViewById(R.id.lstTick);
            ArrayAdapter<CharSequence> voAdapter6 = ArrayAdapter.createFromResource(this, R.array.ORI_ML00190, android.R.layout.simple_spinner_item);
            voAdapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            voSpinner6.setAdapter(voAdapter6);
            int vnPos = voAdapter6.getPosition(oParametro.getCTick());
            voSpinner6.setSelection(vnPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
