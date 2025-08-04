package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

import java.util.Vector;

import jaru.gps.logic.GpsInterno;
import jaru.gps.logic.Parametro;
import jaru.gps.logic.PuertoSerie;
import jaru.gps.logic.SentenciaNMEA;
import jaru.ori.logic.gpslog.Registro;
import jaru.ori.logic.gpslog.TickerNMEA;
import jaru.ori.logic.gpslog.TransfGeografica;
import jaru.ori.logic.gpslog.xml.RegistrosXMLHandler;
import jaru.ori.utils.Utilidades;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Grabación de registros que representan caminos de forma dual.
 * <P>
 * Los datos descriptivos los introduce el usuario: id, descripción, tipo.
 * La georreferenciación del elemento proviene de la lectura del GPS.
 * </p>
 * Esta actividad es fundamentalmente igual que la de registro de puntos, pero para simplificar y
 * ayudar en la identificación de los caminos tanto como objetos de O-Pie como de objetos OBM.
 * El usuario, al iniciar un camino, dice qué tipo O-Pie y OBM es y luego comienza a grabar los
 * datos automáticamente desde el GPS. Esto permite tener identificados perfectamente los caminos
 * para las dos especialidades.
 * @author jarufe
 * @version 1.0
 */
public class ADual extends Activity {
    private Parametro oParametro = null;
    private GpsInterno oGpsInterno = null;
    private Vector<Registro> vRegistros = null;
    private Application oApp = null;
    private Resources oRes = null;
    private boolean bCorriendo = false;
    private TickerNMEA oTicker = null;
    private int nId = 1;
    private TransfGeografica oTransf;

    private Button botNuevoId;
    private Button botAuto;

    //Hilo propio para refrescar los datos de lectura del GPS, de forma que el
    //usuario pueda ver que sigue funcionando la conexión
    private Thread oThread;
    private int nRetardo = 500;
    protected static final int GUIUPDATEIDENTIFIER = 0x101;

    Handler iHandler = new Handler() {
        @Override
        public void handleMessage(Message poMsg) {
            switch (poMsg.what) {
                case ADual.GUIUPDATEIDENTIFIER:
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
     * Refresca los datos de lectura del GPS
     */
    private void anadirPunto() {
        try {
            SentenciaNMEA cSentencia = new SentenciaNMEA();
            try {
                if (oParametro.getCGpsInterno().equals("0"))
                    cSentencia = PuertoSerie.getOSentencia().copia();
                else
                    cSentencia = oGpsInterno.getOSentencia().copia();
                String vcTexto = "---";
                if (cSentencia != null)
                    vcTexto = oTransf.transfCoord(cSentencia.getCLongitud()) + " "
                            + cSentencia.getCMeridiano() + ", " +
                            oTransf.transfCoord(cSentencia.getCLatitud()) + " "
                            + cSentencia.getCHemisferio() +
                            ", Sat: " + cSentencia.getCSatelites();
                ((TextView)findViewById(R.id.lblSatelites)).setText(vcTexto);
                ((TextView)findViewById(R.id.lblRegistros)).setText(oApp.getString(R.string.ORI_ML00101) + " " + vRegistros.size());
            } catch (Exception e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        setContentView(R.layout.dual);
        try {
            //Recoge de la clase principal los elementos básicos que intervienen en el proceso
            oParametro = APrincipal.getOParametro();
            vRegistros = APrincipal.getVRegistros();
            oGpsInterno = APrincipal.getOGpsInterno();
            oTransf = new TransfGeografica();
            //Resto de objetos
            oApp = APrincipal.getOApp();
            oRes = APrincipal.getORes();
            APrincipal.setOTransf(oTransf);
            APrincipal.setCHemisferio("N");
            APrincipal.setCMeridiano("W");
            //Obtiene el siguiente ID asignable, por si lo quiere usar el usuario
            nId = Registro.getIdAsignable(vRegistros);
            ((EditText)findViewById(R.id.txtId)).setText(nId + "", TextView.BufferType.EDITABLE);
            //Escribe el número de registros almacenados hasta el momento.
            ((TextView)findViewById(R.id.lblRegistros)).setText(oApp.getString(R.string.ORI_ML00101) + " " + vRegistros.size());
        } catch (Exception e) {}
        try {
            //Inicializa el botón para crear un nuevo ID
            this.botNuevoId = (Button)this.findViewById(R.id.botNuevo);
            this.botNuevoId.setOnClickListener(new View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    //Obtiene el siguiente ID asignable
                    nId = nId + 1;
                    ((EditText)findViewById(R.id.txtId)).setText(nId + "", TextView.BufferType.EDITABLE);
                }
            });
        } catch (Exception e) {}
        try {
            //Inicializa el botón para grabar registros de forma automática
            this.botAuto = (Button)this.findViewById(R.id.botLogAuto);
            this.botAuto.setOnClickListener(new View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    ADual.this.cambiarGrabarAuto();
                }
            });
        } catch (Exception e) {}
        //Inicializa los demás elementos GUI
        this.limpiarDatos(true);
        oThread = new Thread(new DualRun());
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
     * @return boolean Devuelve true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log, menu);
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
     * Método que se lanza cuando el usuario pulsa una opción de menú.<BR>
     * Si el usuario pulsa la opción cerrar, entonces se para el hilo de ejecución
     * que toma datos de forma automática (si estaba corriendo) y se sale
     * de la actividad.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.grabar6) {
            this.grabarXML(true);
        } else if (item.getItemId()==R.id.cerrar6) {
            if (bCorriendo)
                this.cambiarGrabarAuto();
            oThread.interrupt();
            this.finish();
        }
        return true;
    }

    /**
     * Método que realiza las acciones concretas para almacenar los datos en XML.
     * @param pbAvisar boolean Flag que indica si se ha de avisar al usuario.
     */
    private void grabarXML(boolean pbAvisar) {
        try {
            String vcMensaje = "";
            String vcPathDatos = "";
            boolean vbCorrecto = true;
            try {
                if (oParametro!=null)
                    vcPathDatos = oParametro.getCPathXML();
                if (vRegistros!=null)
                    RegistrosXMLHandler.escribirXML(this, vRegistros, vcPathDatos, "Registros.xml");
            } catch (Exception e) {
                e.printStackTrace();
                vbCorrecto = false;
            }
            if (vbCorrecto)
                vcMensaje = this.getString(R.string.ORI_MI00004);
            else
                vcMensaje = this.getString(R.string.ORI_MI00005);
            if (pbAvisar) {
                //Se muestra un mensaje con el resultado correcto o incorrecto del proceso.
                Toast.makeText(this.getApplicationContext(), vcMensaje, Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que limpia los datos en pantalla
     */
    private void limpiarDatos (boolean pbTodos) {
        int vnComienzo = 1000;
        int vnFinal = 0;
        try {
            if (pbTodos) {
                //Inicialización de la lista desplegable de Tipos O-Pie
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00990));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00991));
                Spinner voSpinner1 = (Spinner) findViewById(R.id.lstTipoOCAD);
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Rellena los nuevos datos de la lista de tipos O-Pie
                for (int i=vnComienzo; i<=vnFinal; i++) {
                    int vnId = oRes.getIdentifier("ORI_ML0" + i, "string", "jaru.ori.gui.gpslog.android");
                    String vcTexto = oApp.getString(vnId);
                    voAdapter1.add(vcTexto);
                }
                voSpinner1.setAdapter(voAdapter1);
                voSpinner1.setSelection(0);

                //Inicialización de la lista desplegable de Tipos OBM
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00992));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00993));
                Spinner voSpinner2 = (Spinner) findViewById(R.id.lstTipoOBM);
                ArrayAdapter<CharSequence> voAdapter2 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                voAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Para el tipo dual OBM, se añade un elemento inicial vacío, por si no se quiere usar
                voAdapter2.add("");
                //Rellena los nuevos datos de la lista de tipos OBM
                for (int i=vnComienzo; i<=vnFinal; i++) {
                    int vnId = oRes.getIdentifier("ORI_ML0" + i, "string", "jaru.ori.gui.gpslog.android");
                    String vcTexto = oApp.getString(vnId);
                    voAdapter2.add(vcTexto);
                }
                voSpinner2.setAdapter(voAdapter2);
                voSpinner2.setSelection(0);
                //Inicializa los elementos adicionales: ID y descripción
                ((EditText)findViewById(R.id.txtId)).setText(nId + "", TextView.BufferType.EDITABLE);
            }
            ((EditText)findViewById(R.id.txtDescripcion)).setText("", TextView.BufferType.EDITABLE);
        } catch (Exception e) {}
    }
    /**
     * Lee el tipo seleccionado de la lista desplegable correspondiente y recupera a partir
     * de él el ID OCAD del objeto.
     * @param pnTipo int. 3 => O-Pie; 4 => OBM
     * @return String. Valor del identificador del objeto OCAD
     */
    private String obtenerTipoOCADSeleccionado (int pnTipo) {
        String vcResul = "";
        int vnElemento = 0;
        int vnPos = 0;
        try {
            //Dado el tipo de objeto seleccionado, calcula el valor de comienzo
            //y de fin de los mensajes que corresponden a objetos del tipo relacionado.
            int vnComienzo = 1000;
            if (pnTipo == 3) {
                vnPos = ((Spinner)findViewById(R.id.lstTipoOCAD)).getSelectedItemPosition();
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00990));
                vnElemento = vnComienzo + vnPos;
            }
            else if (pnTipo == 4) {
                vnPos = ((Spinner)findViewById(R.id.lstTipoOBM)).getSelectedItemPosition();
                //Si en el tipo OBM se ha seleccionado el primer elemento, significa que es vacío
                if (vnPos>0) {
                    vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00992));
                    vnElemento = vnComienzo + vnPos - 1;
                }
            }
            //Recupera el texto del elemento seleccionado y se queda sólo con la
            //parte que representa al código OCAD
            if (vnElemento>0) {
                int vnId = oRes.getIdentifier("ORI_ML0" + vnElemento, "string", "jaru.ori.gui.gpslog.android");
                vcResul = oApp.getString(vnId);
                vnPos = vcResul.indexOf(" ");
                if (vnPos>0)
                    vcResul = vcResul.substring(0, vnPos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcResul;
    }
    /**
     * Método que alterna entre la activación de grabación automática de datos y 
     * la vuelta al estado de reposo.
     */
    private void cambiarGrabarAuto () {
        try {
            //Si estaba parado, pone a correr el proceso de recogida automática de datos.
            if (!bCorriendo) {
                Registro voRegistro = new Registro();
                voRegistro.setCID(((EditText)findViewById(R.id.txtId)).getText().toString());
                voRegistro.setNTipo(1);
                voRegistro.setCTipoOCAD(obtenerTipoOCADSeleccionado(3));
                voRegistro.setCTipoOBM(obtenerTipoOCADSeleccionado(4));
                voRegistro.setCDesc(((EditText)findViewById(R.id.txtDescripcion)).getText().toString());
                voRegistro.setCCX("");
                voRegistro.setCCY("");
                voRegistro.setCElev("");
                //Crea el nuevo hilo de ejecución y lo pone a correr
                oTicker = new TickerNMEA(Integer.parseInt(oParametro.getCTick()), voRegistro, vRegistros, true);
                oTicker.setOParametro(oParametro);
                oTicker.setOGpsInterno(oGpsInterno);
                oTicker.start();
                bCorriendo = true;
                botAuto.setText(oApp.getString(R.string.ORI_ML00107), TextView.BufferType.EDITABLE);
            }
            else {
                oTicker.stop();
                bCorriendo = false;
                vRegistros = oTicker.getVRegistros();
                //Muestra en una etiqueta de texto el número de registros existente.
                ((TextView)findViewById(R.id.lblRegistros)).setText(oApp.getString(R.string.ORI_ML00101) + " " + vRegistros.size());
                //Limpia el contenido del componente, pero deja seleccionados los 
                //valores de ID, Tipo O-Pie y Tipo OBM, por si el siguiente punto pertenece al mismo objeto.
                limpiarDatos(false);
                botAuto.setText(oApp.getString(R.string.ORI_ML00106), TextView.BufferType.EDITABLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clase interna que permite gestionar el Thread que periódicamente lee
     * un punto del GPS y lo visualiza en el panel.
     */
    class DualRun implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = ACentroide.GUIUPDATEIDENTIFIER;
                ADual.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
