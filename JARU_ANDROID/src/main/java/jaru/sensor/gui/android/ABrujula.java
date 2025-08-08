package jaru.sensor.gui.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jaru.ori.gui.gpslog.android.*;
import jaru.sensor.logic.android.*;
import jaru.ori.utils.android.UtilsAndroid;

/**
 * Actividad que muestra una brújula en pantalla. Sirve tanto para calibrar como para mostrar.
 * Calibrar la brújula supone que se muestra la brújula, con la aguja en el cero y el usuario
 * tiene que mover el dispositivo hasta que el norte coincida con el de una brújula real. Cuando
 * se ha conseguido, se pulsa un botón para fijar el desvío. Esto es necesario cuando el dispositivo
 * no dispone de sensor de orientación pero sí de un acelerómetro.
 * La otra opción, mostrar la brújula, sirve para indicar la lectura de la orientación de forma
 * visual.
 */
public class ABrujula extends Activity {
    private Thread oThread;
    private int nRetardo = 500;
    private Bitmap oLimbo = null;
    private Bitmap oAguja = null;

    private int nOpcion = 0;
    private CombiOrientacion oCombi = null;

    private ABrujulaView iPanel = null;
    protected static final int GUIUPDATEIDENTIFIER = 0x101;

    Handler iHandler = new Handler() {
        @Override
        public void handleMessage(Message poMsg) {
            switch (poMsg.what) {
                case ABrujula.GUIUPDATEIDENTIFIER:
                    repintarBrujula();
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
        try {
            // Se inicializan las variables con valores que están escritos en la actividad principal.
            // Este es un metodo sencillo de compartir valores a lo largo de la ejecución.
            nOpcion = APrincipal.getNOpcionEntrada();
            oCombi = APrincipal.getOCombi();
            oLimbo = BitmapFactory.decodeResource(this.getResources(), R.drawable.ori_glbruj);
            oAguja = BitmapFactory.decodeResource(this.getResources(), R.drawable.ori_glaguj);
        }catch(Exception e1) {
            Log.e("GPS-O", "Error inicializando parámetros", e1);
        }
        try {
            //Crea el objeto que representa al panel gráfico
            this.iPanel = new ABrujulaView(this);
            this.iPanel.setNOpcion(nOpcion);
            this.iPanel.setOLimbo(oLimbo);
            this.iPanel.setOAguja(oAguja);
            if (oCombi != null) {
                this.iPanel.setNDesvio(oCombi.getNDesvio());
                //Se establece un texto que indica qué sensor existe: brújula, rotación o ninguno
                if (oCombi.existeBrujula())
                    this.iPanel.setCSensor(this.getApplicationContext().getString(R.string.ORI_ML00135));
                else if (oCombi.existeRotacion())
                    this.iPanel.setCSensor(this.getApplicationContext().getString(R.string.ORI_ML00207));
                else
                    this.iPanel.setCSensor(this.getApplicationContext().getString(R.string.ORI_ML00206));
            } else {
                this.iPanel.setCSensor(this.getApplicationContext().getString(R.string.ORI_ML00206));
            }
            if (nOpcion == 1) {
                this.iPanel.setCTexto(this.getApplicationContext().getString(R.string.ORI_ML00204));
                this.iPanel.setNDesvio(0.0);
            }
            this.setContentView(this.iPanel);
        }catch(Exception e2) {
            Log.e("GPS-O", "Error inicializando vista de la brújula", e2);
        }
        //Realiza primera lectura de datos
        oThread = new Thread(new BrujulaRun());
        oThread.start();
    }

    /**
     * MÃ©todo que se llama cuando la aplicaciÃ³n se cierra.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * MÃ©todo que se lanza cuando se crean las opciones de menÃº
     * @param menu Menu
     * @return boolean. Devuelve true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        if (nOpcion==0)
            inflater.inflate(R.menu.cerrar, menu);
        else
            inflater.inflate(R.menu.calibrarcerrar, menu);
        return true;
    }

    /**
     * MÃ©todo que se lanza cuando se preparan las opciones de menÃº.
     * @param menu Menu
     * @return boolean. Devuelve el resultado de la ejeccuciÃ³n del mÃ©todo en super.
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        boolean vbResul = super.onPrepareOptionsMenu(menu);
        return vbResul;
    }

    /**
     * MÃ©todo que se lanza cuando el usuario selecciona una opciÃ³n de menÃº
     * <BR>
     * Si se pulsa la opciÃ³n refrescar, se ejecuta una nueva lectura de GPS.<BR>
     * Si se pulsa cancelar, se guardan los valores seleccionados en la actividad principal y se cierra
     * @param item MenuItem
     * @return boolean. Devuelve true.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.grabar4) {
            if (oCombi!=null)
                oCombi.setNDesvio(oCombi.leerGrados());
        } else if (item.getItemId()==R.id.cerrar4 ||
                item.getItemId()==R.id.cerrar5) {
            oThread.interrupt();
            this.finish();
        }
        return true;
    }

    /**
     * Fuerza el repintado de la brújula y la aguja que marca la orientación
     */
    private void repintarBrujula() {
        try {
            //Pasa los datos a la vista, para repintarlos en pantalla
            if (oCombi!=null) {
                iPanel.setNGrados(oCombi.leerGrados());
                if (oCombi.getRotacion()!=null) {
                    double vnVal1 = oCombi.getRotacion().leerX();
                    vnVal1 = new BigDecimal(vnVal1).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double vnVal2 = oCombi.getRotacion().leerY();
                    vnVal2 = new BigDecimal(vnVal2).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double vnVal3 = oCombi.getRotacion().leerZ();
                    vnVal3 = new BigDecimal(vnVal3).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    double vnVal4 = oCombi.getRotacion().leerMaxRange();
                    vnVal4 = new BigDecimal(vnVal4).setScale(2, RoundingMode.HALF_UP).doubleValue();
                    iPanel.setCVarios("X:" + vnVal1 + ";Y:" + vnVal2 + ";Z:" + vnVal3 + ";Max:" + vnVal4);
                } else {
                    iPanel.setCVarios("");
                }
            }
            iPanel.invalidate();
        } catch (Exception e) {
            Log.e("GPS-O", "Error repintando brújula", e);
        }
    }

    /**
     * Clase interna que permite gestionar el Thread que periódicamente lee la orientación
     * del sensor y muestra de forma visual la lectura en una brújula
     */
    class BrujulaRun implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = ABrujula.GUIUPDATEIDENTIFIER;
                ABrujula.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}