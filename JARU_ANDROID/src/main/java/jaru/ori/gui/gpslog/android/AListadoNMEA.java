package jaru.ori.gui.gpslog.android;

import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import android.view.*;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import jaru.gps.logic.*;
import jaru.ori.utils.Utilidades;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Muestra lecturas contínuas que llegan por el interface de comunicaciones desde el GPS.
 * <P>
 * Esta clase es un listado que va mostrando periódicamente las lecturas que se 
 * van realizando desde el GPS. Para ello se lee un contenido amplio desde el 
 * puerto serie y se queda a la espera de lo que decida el usuario.
 * Este puede refrescar para volver a mostrar otra lectura.
 * </p>
 * Esta actividad se tiene que utilizar obligatoriamente en primer lugar, 
 * siempre que se quiera manejar un dispositivo GPS, ya que se encarga de abrir
 * el puerto de comunicaciones.
 * @author jarufe
 * @version 1.0
 */
public class AListadoNMEA extends AppCompatActivity {
    private TextView txtVacio;

    private RecyclerView recyclerView;
    private NMEAAdapter adapter;
    private ArrayList<String> oLista = new ArrayList<>();

    private Parametro oParametro = null;
    private GpsInterno oGpsInterno = null;

    /**
     * Método que se llama cuando la actividad se ejecuta por primera vez.
     * @param icicle Bundle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // ToDo add your GUI initialization code here        
        setContentView(R.layout.listadonmea);

        txtVacio = findViewById(R.id.lblVacio);
        recyclerView = findViewById(R.id.recyclerNMEA);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Recoge objetos de la actividad principal
        oParametro = APrincipal.getOParametro();
        oGpsInterno = APrincipal.getOGpsInterno();
        //Abre el puerto de comunicaciones y lee datos
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
     * Método que se llama cuando se crean las opciones de menú.
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
     * Método que se llama cuando se preparan las opciones de menú.
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
     * Si el usuario pulsa la opción refrescar, se vuelve a realizar una lectura
     * de datos del GPS y se vuelca en pantalla.<BR>
     * Si el usuario pulsa cancelar, simplemente se sale de la actividad.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.refrescar7) {
            this.realizarLectura();
        } else if (item.getItemId()==R.id.cancelar7) {
            APrincipal.setOGpsInterno(oGpsInterno);
            this.finish();
        }
        return true;
    }

    /**
     * Realiza una lectura de GPS y llama al método que refresca los datos en el 
     * componente.
     */
    private void realizarLectura () {
        String vcTextoParcial = "";
        try {
            //Si se usa GPS externo, se abre el puerto y se leen datos
            if (oParametro.getCGpsInterno().equals("0")) {
                if (!PuertoSerie.getBAbierto())
                    PuertoSerie.abrir(0);
                PuertoSerie.getOTicker().stop();
                String vcTexto = PuertoSerie.recibir(500);
                PuertoSerie.getOTicker().start();
                while (vcTexto.length()>0) {
                    int vnPos = vcTexto.indexOf('\n');
                    if (vnPos>0) {
                        vcTextoParcial = vcTexto.substring(0, vnPos-1);
                        vcTexto = vcTexto.substring(vnPos+1);
                    }
                    else {
                        vcTextoParcial = vcTexto;
                        vcTexto = "";
                    }
                    oLista.add(vcTextoParcial);
                }
            } else {
                //Si se usa GPS interno, se crea una nueva instancia y se lee
                if (oGpsInterno==null)
                    oGpsInterno = new GpsInterno(this);
                if (!oGpsInterno.getBPuedoObtenerPosicion()) {
                    oGpsInterno.mostrarAlertaConfiguracion();
                } else {
                    vcTextoParcial = Utilidades.obtenerHoraNMEADesdeMilisecs(oGpsInterno.getNHora()) + ": " +
                            oGpsInterno.getNLatitud() + ";" + oGpsInterno.getNLongitud();
                    oLista.add(vcTextoParcial);
                }
            }

            if (oLista.isEmpty()) {
                txtVacio.setVisibility(View.VISIBLE);
            } else {
                txtVacio.setVisibility(View.GONE);
                adapter = new NMEAAdapter(oLista, (item, position) -> {
                    // Equivalente a tu antiguo onListItemClick
                    String vcTexto = item;
                    Log.d("GPS-O", "Listado NMEA. Elemento clicado: " + vcTexto);
                    // Aquí puedes hacer lo que necesites con el texto
                });
                recyclerView.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
