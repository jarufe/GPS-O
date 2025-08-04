package jaru.ori.gui.gpslog.android;
import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

import jaru.ori.utils.android.UtilsAndroid;
import jaru.red.logic.*;

public class ATestConn extends Activity {
    private Thread oThread;
    private int nRetardo = 500;
    private HiloTransmisiones oTrans = null;
    private Button botProbar;
    public Application oApp = null;
    public Resources oRes = null;

    protected static final int GUIUPDATEIDENTIFIER = 0x101;

    public HiloTransmisiones getoTrans() {
        return oTrans;
    }
    public void setoTrans(HiloTransmisiones oTrans) {
        this.oTrans = oTrans;
    }

    Handler iHandler = new Handler() {
        @Override
        public void handleMessage(Message poMsg) {
            switch (poMsg.what) {
                case ATestConn.GUIUPDATEIDENTIFIER:
                    if (oTrans!=null) {
                        if (oTrans.isbResulPreparado()) {
                            UploadRequestResponse oRespuesta = oTrans.getoRespuesta();
                            List<String>vlData = oRespuesta.getlData();
                            String vcResul = vlData.get(0);
                            ((TextView)findViewById(R.id.lblMensaje)).setText("Result. " + vcResul);
                            oTrans.setbResulPreparado(false);
                            oTrans.stop();
                        }
                    }
                    break;
            }
            super.handleMessage(poMsg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testcon);
        try {
            oApp = APrincipal.getOApp();
            oRes = APrincipal.getORes();
            GestionTransmisiones.setcServidor(APrincipal.getoConfLocaliza().getcServidor());
            GestionTransmisiones.setnPuerto(APrincipal.getoConfLocaliza().getnPuerto());
            GestionTransmisiones.setcServlet(APrincipal.getoConfLocaliza().getcServlet());
            oTrans = new HiloTransmisiones();
            try {
                //Inicializa el botón para probar la conexión
                this.botProbar = (Button)this.findViewById(R.id.botProbar);
                this.botProbar.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        UploadRequestResponse voEnvio = new UploadRequestResponse();
                        voEnvio.setcOrder("CheckConnectivity");
                        List<String> vlData = new ArrayList<>();
                        vlData.add("");
                        voEnvio.setlData(vlData);
                        oTrans.setoEnvio(voEnvio);
                        oTrans.start();
                        ((TextView)findViewById(R.id.lblMensaje)).setText(oApp.getString(R.string.ORI_ML00218));
                    }
                });
            } catch (Exception e) {}
            //Crea el nuevo hilo de ejecución y lo pone a correr para refrescar la gestión de transmisiones
            oThread = new Thread(new TestConnRun());
            oThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(oThread!=null)
            oThread.interrupt();
        oThread = new Thread(new TestConnRun());
        oThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(oThread!=null)
            oThread.interrupt();
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
     * @return boolean. Devuelve el resultado del método en super.
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        boolean vbResul = super.onPrepareOptionsMenu(menu);
        return vbResul;
    }
    /**
     * Método que se lanza cuando el usuario selecciona una opción de menú.<BR>
     * Si se pulsa aceptar, se pasan los registros seleccionados a la actividad llamante.<BR>
     * Si se pulsa cancelar, se sale de la pantalla.<BR>
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.aceptar1) {
            this.finish();
        } else if (item.getItemId()==R.id.cancelar1) {
            this.finish();
        }
        return true;
    }

    /**
     * Clase interna que permite gestionar el Thread que periódicamente comprubea la comunicación
     */
    class TestConnRun implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = ATestConn.GUIUPDATEIDENTIFIER;
                ATestConn.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
