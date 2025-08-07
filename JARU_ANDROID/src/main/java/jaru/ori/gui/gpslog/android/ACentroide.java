package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import jaru.ori.logic.gpslog.*;
import jaru.gps.logic.*;
import jaru.ori.utils.*;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Lectura repetida de coordenadas procedentes del GPS para calcular un centroide.
 * <P>
 * Los datos que proceden del GPS se leen en secuencia periódica. Con todas las
 * lecturas se va recalculando un centroide que da una lectura corregida.
 * </p>
 * @author jarufe
 * @version 1.0
 */
public class ACentroide extends Activity {
    private Thread oThread;
    private int nRetardo = 500;

    private Parametro oParametro = null;
    private GpsInterno oGpsInterno = null;
    private TransfGeografica oTransf;
    private String cHemisferio = "N";
    private String cMeridiano = "W";

    private ACentroideView iPanel = null;
    protected static final int GUIUPDATEIDENTIFIER = 0x101;

    Handler iHandler = new Handler() {
        @Override
        public void handleMessage(Message poMsg) {
            switch (poMsg.what) {
                case ACentroide.GUIUPDATEIDENTIFIER:
                    if ((oParametro.getCGpsInterno().equals("0") && PuertoSerie.getBAbierto()) ||
                            (oParametro.getCGpsInterno().equals("1") && oGpsInterno!=null)) {
                        anadirPunto();
                    }
                    break;
            }
            super.handleMessage(poMsg);
        }
    };

    /**
     * Método que se lanza cuando la actividad se crea por primera vez.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Se inicializan las variables con valores que están escritos en la actividad principal.
        // Este es un metodo sencillo de compartir valores a lo largo de la ejecución.
        oTransf = APrincipal.getOTransf();
        cMeridiano = APrincipal.getCMeridiano();
        cHemisferio = APrincipal.getCHemisferio();
        oParametro = APrincipal.getOParametro();
        oGpsInterno = APrincipal.getOGpsInterno();
        //Crea el objeto que representa al panel gráfico
        this.iPanel = new ACentroideView(this);
        this.setContentView(this.iPanel);
        //Realiza primera lectura de datos
        this.realizarLecturaInicial();
        oThread = new Thread(new CentroideRun());
        oThread.start();
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
     * @return boolean. Devuelve true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refrescarcancelar, menu);
        return true;
    }

    /**
     * Método que se lanza cuando se preparan las opciones de menú.
     * @param menu Menu
     * @return boolean. Devuelve el resultado de la ejeccución del método en super.
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        boolean vbResul = super.onPrepareOptionsMenu(menu);
        return vbResul;
    }

    /**
     * Método que se lanza cuando el usuario selecciona una opción de menú
     * <BR>
     * Si se pulsa la opción refrescar, se ejecuta una nueva lectura de GPS.<BR>
     * Si se pulsa cancelar, se guardan los valores seleccionados en la actividad principal y se cierra
     * @param item MenuItem
     * @return boolean. Devuelve true.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.refrescar7) {
            this.anadirPunto();
        } else if (item.getItemId()==R.id.cancelar7) {
            oTransf = iPanel.getOTransf();
            APrincipal.setOTransf(oTransf);
            APrincipal.setCHemisferio(cHemisferio);
            APrincipal.setCMeridiano(cMeridiano);
            oThread.interrupt();
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
     * Método que establece los datos actuales de puntos.
     * @param poTransf TransfGeografica. Datos de puntos registrados.
     */
    public void setOTransf(TransfGeografica poTransf) {
        oTransf = poTransf;
    }
    /**
     * Método que devuelve el objeto con los datos de puntos registrados.
     * @return TransfGeografica. Datos de puntos registrados.
     */
    public TransfGeografica getOTransf() {
        return oTransf;
    }
    /**
     * Método que devuelve el hemisferio en el que se encuentran las coordenadas.
     * @return String. Valores "N" o "S".
     */
    public String getCHemisferio() {
        return cHemisferio;
    }
    /**
     * Método que devuelve el meridiano en el que se encuentran las coordenadas.
     * @return String. Valores "W" o "E".
     */
    public String getCMeridiano() {
        return cMeridiano;
    }
    /**
     * Realiza una lectura de GPS y llama al método que añade un nuevo punto.
     */
    private void realizarLecturaInicial () {
        try {
            oTransf.reinicia();
            anadirPunto();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Añade un punto al centroide y vuelve a repintar el gráfico
     */
    private void anadirPunto() {
        try {
            SentenciaNMEA cSentencia = new SentenciaNMEA();
            Log.d("GPS-O", "Comienzo proceso para añadir punto al centroide");
            try {
                if (oParametro.getCGpsInterno().equals("0")) {
                    cSentencia = PuertoSerie.getOSentencia().copia();
                    //Como la sentencia viene de un GPS externo con NMEA, ajusta la hora según el desfase UTC
                    int vnDesfase = Utilidades.obtenerDesfaseHorarioMinutos();
                    cSentencia.ajustarHora(vnDesfase);
                } else {
                    cSentencia = oGpsInterno.getOSentencia().copia();
                }
            } catch (Exception e) {
                Log.e("GPS-O", "Error manejando GPS interno o externo", e);
            }
            Log.d("GPS-O", "Compruebo si tengo coordenadas");
            if (cSentencia.cLongitud.length()>0 && cSentencia.cLatitud.length()>0 && oTransf.nCont<Utilidades.getNLecturasNMEA()) {
                Log.d("GPS-O", "Hay nueva coordenada");
                oTransf.nDatos[(int)oTransf.nCont][0] = oTransf.obtieneLong(cSentencia.cLongitud);
                oTransf.nDatos[(int)oTransf.nCont][1] = oTransf.obtieneLong(cSentencia.cLatitud);
                int vnAltura = (int)Double.parseDouble(cSentencia.getCAltura());
                if (vnAltura<=0)
                    vnAltura = 0;
                oTransf.nDatos[(int)oTransf.nCont][2] = vnAltura;
                oTransf.nSatelites[(int)oTransf.nCont] = Integer.parseInt(cSentencia.cSatelites);
                //Actualiza los valores extremos, que se usan posteriormente para el dibujado gráfico
                oTransf.actualizarExtremos(oTransf.nDatos[(int)oTransf.nCont][0], oTransf.nDatos[(int)oTransf.nCont][1], oTransf.nSatelites[(int)oTransf.nCont]);
                oTransf.nCont++;
                cHemisferio = cSentencia.cHemisferio;
                cMeridiano = cSentencia.cMeridiano;
            }
            Log.d("GPS-O", "Añado info de Fix y número de satélites");
            String vcFix = this.getApplicationContext().getString(R.string.ORI_ML00001);
            if (cSentencia.getCFix().equals("0"))
                vcFix = this.getApplicationContext().getString(R.string.ORI_ML00002);
            String vcTexto = this.getApplicationContext().getString(R.string.ORI_ML00108) + ": " + vcFix;
            vcTexto = vcTexto + "  " + this.getApplicationContext().getString(R.string.ORI_ML00109) + ": " + cSentencia.getCSatelites();
            Log.d("GPS-O", "Calcula el nuevo centroide");
            //Calcula el nuevo centroide
            oTransf.centroide();
            //Pasa los datos a la vista, para repintarlos en pantalla
            Log.d("GPS-O", "Pasa los datos a la vista para repintarlos");
            iPanel.setOTransf(oTransf);
            iPanel.setCTexto(vcTexto);
            iPanel.setcHemisferio(cHemisferio);
            iPanel.setcMeridiano(cMeridiano);
            iPanel.invalidate();
        } catch (Exception e) {
            Log.e("GPS-O", "Error añadiendo punto al centroide", e);
        }
    }

    /**
     * Clase interna que permite gestionar el Thread que periódicamente lee
     * un punto del GPS y lo visualiza en el panel.
     */
    class CentroideRun implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = ACentroide.GUIUPDATEIDENTIFIER;
                ACentroide.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}