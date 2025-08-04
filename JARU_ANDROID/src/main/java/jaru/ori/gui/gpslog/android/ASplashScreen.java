package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.content.Intent;

import jaru.ori.utils.android.UtilsAndroid;

/**
 * Clase que permite visualizar una pantalla de bienvenida durante un intervalo
 * corto de tiempo. Se ha creado pero no forma parte de la ejecución normal del 
 * programa. Aún así, no se ha borrado por si se usa en el futuro.
 * @author javier.arufe
 */
public class ASplashScreen extends Activity {
    protected boolean _active = true;
    protected int _splashTime = 5000; // time to display the splash screen in ms

    /**
     * Método que se lanza la primera vez que se ejecuta la actividad.
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
        setContentView(R.layout.splash);
        // thread para visualizar la pantalla de bienvenida
        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
                        sleep(100);
                        if(_active) {
                            waited += 100;
                        }
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {
                    finish();
                    startActivity(new Intent("jaru.ori.gui.gpslog.android.APrincipal"));
                    stop();
                }
            }
        };
        splashTread.start();
    }
    /**
     * Método que se lanza cuando el usuario toca la pantalla. Sirve para que
     * continúe la carga de la aplicación.
     * @param event MotionEvent
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _active = false;
        }
        return true;
    }

}
