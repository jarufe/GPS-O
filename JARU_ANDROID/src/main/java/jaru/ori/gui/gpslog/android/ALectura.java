package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;

import jaru.ori.logic.gpslog.*;
import jaru.gps.logic.*;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Lectura de una coordenada procedente del GPS.
 * <P>
 * Muestra los datos básicos leídos del GPS, transformados para su correcta
 * presentación en pantalla a partir del formato NMEA.
 * </p>
 * @author jarufe
 * @version 1.0
 */
public class ALectura extends Activity {
    private Parametro oParametro = null;
    private TransfGeografica cTransf;
    private GpsInterno oGpsInterno = null;

    /**
     * Método que se llama la primera vez que se ejecuta la actividad.
     * @param icicle Bundle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // ToDo add your GUI initialization code here        
        setContentView(R.layout.lectura);
        cTransf = new TransfGeografica();
        oParametro = APrincipal.getOParametro();
        oGpsInterno = APrincipal.getOGpsInterno();
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
     * Método que se llama cuando se crean las opciones de menú
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
     * Si el usuario pulsa refrescar, se vuelve a leer la posición del GPS y se
     * escribe en pantalla.<BR>
     * Si el usuario pulsa cancelar, simplemente se cierra la actividad.
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.refrescar7) {
            this.realizarLectura();
        } else if (item.getItemId()==R.id.cancelar7) {
            this.finish();
        }
        return true;
    }

    /**
     * Método que establece los datos actuales de configuración.
     * @param poParametro Parametro. Objeto con los parámetros actuales.
     */
    public void setOParametro(Parametro poParametro) {
        oParametro = poParametro;
    }
    /**
     * Método que devuelve el objeto con los datos de configuración.
     * @return Parametro. Objeto con los nuevos parámetros configurados.
     */
    public Parametro getOParametro() {
        return oParametro;
    }
    /**
     * Método que limpia los datos en pantalla
     */
    private void limpiarDatos () {
        try {
            ((EditText)findViewById(R.id.txtLongitud)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtLatitud)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtAltura)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtDatum)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtHora)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtFix)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtSatelites)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtHdop)).setText("", TextView.BufferType.EDITABLE);
        } catch (Exception e) {
            Log.e ("GPS-O", "Error limpiando datos", e);
        }
    }
    /**
     * Realiza una lectura de GPS y llama al método que refresca los datos en el componente.
     */
    private void realizarLectura () {
        try {
            ((EditText)findViewById(R.id.txtLongitud)).setText(R.string.ORI_ML00084, TextView.BufferType.EDITABLE);
            cTransf.reinicia();
            SentenciaNMEA cSentencia = new SentenciaNMEA();
            //Línea para probar el funcionamiento de la actividad
            //PuertoSerie.getOSentencia().procesaSentencia("$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47");
            //Recoge los datos de posicionamiento del GPS interno o externo según la configuración establecida
            if (oParametro.getCGpsInterno().equals("0"))
                cSentencia = PuertoSerie.getOSentencia().copia();
            else
                cSentencia = oGpsInterno.getOSentencia().copia();
            mostrarDatosCompletos (cSentencia);
        } catch (Exception e) {
            Log.e ("GPS-O", "Error realizando lectura", e);
        }
    }
    /**
     * Muestra los datos que se han recogido tras una lectura del puerto serie.
     * @param cSentencia SentenciaNMEA. Lectura recogida.
     */
    private void mostrarDatosCompletos(SentenciaNMEA cSentencia) {
        String cTexto = new String("");

        limpiarDatos();
        try {
            if (cSentencia.nOk==3) {
                //String vcLong = cTransf.transfCoord(cTransf.obtieneCadena(cTransf.obtieneLong(cSentencia.cLongitud)));
                //cTexto = vcLong + " " + cSentencia.cMeridiano;
                String vcLong = cTransf.transfCoordAGrados(cTransf.obtieneCadena(cTransf.obtieneLong(cSentencia.cLongitud)));
                if (cSentencia.getCMeridiano().equalsIgnoreCase("W")) {
                    cTexto = "-" + vcLong;
                } else {
                    cTexto = vcLong;
                }
                ((EditText)findViewById(R.id.txtLongitud)).setText(cTexto, TextView.BufferType.EDITABLE);
                //String vcLat = cTransf.transfCoord(cTransf.obtieneCadena(cTransf.obtieneLong(cSentencia.cLatitud)));
                //cTexto = vcLat + " " + cSentencia.cHemisferio;
                String vcLat = cTransf.transfCoordAGrados(cTransf.obtieneCadena(cTransf.obtieneLong(cSentencia.cLatitud)));
                if (cSentencia.getCHemisferio().equalsIgnoreCase("S")) {
                    cTexto = "-" + vcLat;
                } else {
                    cTexto = vcLat;
                }
                ((EditText)findViewById(R.id.txtLatitud)).setText(cTexto, TextView.BufferType.EDITABLE);
                ((EditText)findViewById(R.id.txtAltura)).setText(cSentencia.cAltura, TextView.BufferType.EDITABLE);
                try {
                    cTexto = cSentencia.cHora.substring(0, 2) + ":" +
                            cSentencia.cHora.substring(2, 4) + ":" +
                            cSentencia.cHora.substring(4, 6);
                } catch (Exception e) {
                    cTexto = "";
                }
                ((EditText)findViewById(R.id.txtHora)).setText(cTexto, TextView.BufferType.EDITABLE);
                ((EditText)findViewById(R.id.txtDatum)).setText(cSentencia.cDatum, TextView.BufferType.EDITABLE);
                String vcFix = this.getApplicationContext().getString(R.string.ORI_ML00001);
                if (cSentencia.getCFix().equals("0"))
                    vcFix = this.getApplicationContext().getString(R.string.ORI_ML00002);
                ((EditText)findViewById(R.id.txtFix)).setText(vcFix, TextView.BufferType.EDITABLE);
                ((EditText)findViewById(R.id.txtSatelites)).setText(cSentencia.cSatelites, TextView.BufferType.EDITABLE);
                ((EditText)findViewById(R.id.txtHdop)).setText(cSentencia.cHdop, TextView.BufferType.EDITABLE);
            }
            else
                ((EditText)findViewById(R.id.txtLongitud)).setText("00:00:00", TextView.BufferType.EDITABLE);
        } catch (Exception e) {
            Log.e ("GPS-O", "Error mostrando lectura", e);
            ((EditText)findViewById(R.id.txtLongitud)).setText("ERROR CONVERSION", TextView.BufferType.EDITABLE);
        }
    }


}
