package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
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
import android.os.Handler;
import android.os.Message;

import jaru.gps.logic.*;
import jaru.ori.logic.gpslog.xml.*;
import java.util.Vector;
import jaru.ori.logic.gpslog.*;
import jaru.ori.utils.Utilidades;
import jaru.ori.utils.android.UtilsAndroid;

/**
 * Grabación de registros que representan puntos, líneas o áreas.
 * <P>
 * Los datos descriptivos los introduce el usuario: id, descripción, tipo.
 * La georreferenciación del elemento proviene de la lectura del GPS.
 * </p>
 * Esta actividad es la base para el almacenamiento de datos vectoriales recogidos
 * del terreno de una forma rápida. Fue el primer desarrollo, el cual funcionaba
 * tanto en PC como en PDAs. Luego, se desarrolló la parte gráfica del editor de
 * trabajo de campo, con la que se puede dibujar en modo Raster pero también
 * crear y borrar elementos vectoriales. Sin embargo, esta actividad sigue 
 * teniendo toda la utilidad, ya que es una forma más rápida de hacer un barrido
 * sobre el terreno para tomar muestras, por ejemplo durante una primera fase de
 * revisión del terreno, para completar un mapa base que contenga más elementos
 * (caminos, piedras, objetos característicos) que no aparezcan en otros documentos
 * fuente.
 * @author jarufe
 * @version 1.0
 */
public class ALog extends Activity {
    private Parametro oParametro = null;
    private GpsInterno oGpsInterno = null;
    private Vector<Registro> vRegistros = null;
    private Application oApp = null;
    private Resources oRes = null;
    private boolean bCorriendo = false;
    private TickerNMEA oTicker = null;
    private TransfGeografica oTransf;
    private int nId = 1;

    private Button botNuevoId;
    private Button botAnadir;
    private Button botCentroide;
    private Button botAuto;
    private Spinner oTipos;

    final int ACTIVITY_CENTROIDE = 4;
    //Hilo propio para refrescar los datos de lectura del GPS, de forma que el
    //usuario pueda ver que sigue funcionando la conexión
    private Thread oThread;
    private int nRetardo = 500;
    protected static final int GUIUPDATEIDENTIFIER = 0x101;

    Handler iHandler = new Handler() {
        @Override
        public void handleMessage(Message poMsg) {
            switch (poMsg.what) {
                case ALog.GUIUPDATEIDENTIFIER:
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
            SentenciaNMEA voSentencia = new SentenciaNMEA();
            try {
                if (oParametro.getCGpsInterno().equals("0")) {
                    voSentencia = PuertoSerie.getOSentencia().copia();
                    //Como la sentencia viene de un GPS externo con NMEA, ajusta la hora según el desfase UTC
                    int vnDesfase = Utilidades.obtenerDesfaseHorarioMinutos();
                    voSentencia.ajustarHora(vnDesfase);
                } else {
                    voSentencia = oGpsInterno.getOSentencia().copia();
                }
                String vcTexto = "---";
                if (voSentencia != null) {
                    String vcLong = oTransf.transfCoordAGrados(oTransf.obtieneCadena(oTransf.obtieneLong(voSentencia.getCLongitud())));
                    if (voSentencia.getCMeridiano().equalsIgnoreCase("W")) {
                        vcTexto = "-" + vcLong;
                    } else {
                        vcTexto = vcLong;
                    }
                    vcTexto += "; ";
                    String vcLat = oTransf.transfCoordAGrados(oTransf.obtieneCadena(oTransf.obtieneLong(voSentencia.getCLatitud())));
                    if (voSentencia.getCHemisferio().equalsIgnoreCase("S")) {
                        vcTexto += "-" + vcLat;
                    } else {
                        vcTexto += vcLat;
                    }
                    vcTexto += "; Sat: " + voSentencia.getCSatelites();
                }
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
        setContentView(R.layout.log);
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
        } catch (Exception e) {
            Log.e("GPS-O", "Error inicializando parámetros para el funcionamiento de la actividad", e);
        }
        try {
            //Inicializa el botón para crear un nuevo ID
            this.botNuevoId = (Button)this.findViewById(R.id.botNuevo);
            this.botNuevoId.setOnClickListener(new android.view.View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    //Obtiene el siguiente ID asignable
                    nId = nId + 1;
                    ((EditText)findViewById(R.id.txtId)).setText(nId + "", TextView.BufferType.EDITABLE);
                }
            });
        } catch (Exception e) {
            Log.e("GPS-O", "Error inicializando botón para incrementar Id", e);
        }
        try {
            //Inicializa el botón para añadir un registro
            this.botAnadir = (Button)this.findViewById(R.id.botLogAnadir);
            this.botAnadir.setOnClickListener(new android.view.View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    anadirRegistro();
                }
            });
        } catch (Exception e) {
            Log.e("GPS-O", "Error inicializando botón para nuevos registros", e);
        }
        try {
            //Inicializa el botón para calcular un centroide
            this.botCentroide = (Button)this.findViewById(R.id.botLogCentroide);
            this.botCentroide.setOnClickListener(new android.view.View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    calcularCentroide();
                }
            });
        } catch (Exception e) {
            Log.e("GPS-O", "Error inicializando botón para centroide", e);
        }
        try {
            //Inicializa el botón para grabar registros de forma automática
            this.botAuto = (Button)this.findViewById(R.id.botLogAuto);
            this.botAuto.setOnClickListener(new android.view.View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    cambiarGrabarAuto();
                }
            });
        } catch (Exception e) {
            Log.e("GPS-O", "Error inicializando botón para grabación automática", e);
        }
        try {
            //Inicializa la lista de tipos, para añadir el listener de selección
            this.oTipos = (Spinner)this.findViewById(R.id.lstTipo);
            this.oTipos.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    ALog.this.actualizarListaTiposOCAD();
                }
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        } catch (Exception e) {
            Log.e("GPS-O", "Error inicializando lista de tipos de objetos", e);
        }
        //Inicializa los demás elementos GUI
        this.limpiarDatos(true);
        oThread = new Thread(new LogRun());
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
     * Método que se lanza cuando se regresa de la actividad que se puede llamar
     * desde ésta. En concreto, el usuario puede llamar a la actividad que calcula
     * y presenta en pantalla un centroide con datos que se van leyendo de forma
     * sucesiva desde el GPS.
     * @param requestCode int
     * @param resultCode int
     * @param data Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_CENTROIDE:
                //Cuando vuelve del cuadro de diálogo de cálculo de centroide
                //actualiza el valor de la coordenada para el registro actual.
                actualizarCoordenada();
                break;
        }
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
        } catch (Exception e) {
            Log.e("GPS-O", "Error grabando a fichero", e);
        }
    }
    /**
     * Método que limpia los datos en pantalla
     */
    private void limpiarDatos (boolean pbTodos) {
        try {
            //Inicialización de la lista desplegable de Tipos
            if (pbTodos) {
                try {
                    ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                    voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00094));
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00095));
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00096));
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00111));
                    voAdapter1.add(oApp.getString(R.string.ORI_ML00112));
                    this.oTipos.setAdapter(voAdapter1);
                } catch (Exception e) {}
                ((EditText)findViewById(R.id.txtId)).setText(nId + "", TextView.BufferType.EDITABLE);
                this.oTipos.setSelection(0);
                actualizarListaTiposOCAD();
            }
            ((EditText)findViewById(R.id.txtDescripcion)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtLongitud)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtLatitud)).setText("", TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtAltitud)).setText("", TextView.BufferType.EDITABLE);
        } catch (Exception e) {
            Log.e("GPS-O", "Error limpiando datos de pantalla", e);
        }
    }
    /**
     * Dada una selección de tipo de punto, actualiza el listado de tipos OCAD 
     * que corresponden a esa selección.
     */
    private void actualizarListaTiposOCAD () {
        try {
            //Dado el tipo de objeto seleccionado, calcula el valor de comienzo 
            //y de fin de los mensajes que corresponden a objetos del tipo relacionado.
            int vnTipo = ((Spinner)findViewById(R.id.lstTipo)).getSelectedItemPosition();
            int vnComienzo = 1000;
            int vnFinal = 0;
            if (vnTipo == 0) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00994));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00995));
            }
            else if (vnTipo == 1) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00996));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00997));
            }
            else if (vnTipo == 2) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00998));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00999));
            }
            else if (vnTipo == 3) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00990));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00991));
            }
            else if (vnTipo == 4) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00992));
                vnFinal = Integer.parseInt(oApp.getString(R.string.ORI_ML00993));
            }
            //Inicialización de la lista desplegable de Tipos OCAD para un tipo dado
            Spinner voSpinner1 = (Spinner) findViewById(R.id.lstTipoOCAD);
            ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
            voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //Rellena los nuevos datos de la lista de tipos OCAD
            for (int i=vnComienzo; i<=vnFinal; i++) {
                String resourceName = "ORI_ML0" + String.format("%04d", i);
                int vnId = oRes.getIdentifier(resourceName, "string", getPackageName());

                if (vnId != 0) {
                    String vcTexto = oApp.getString(vnId);
                    voAdapter1.add(vcTexto);
                } else {
                    Log.w("GPS-O", "No se encontró el recurso: " + resourceName);
                }
            }
            voSpinner1.setAdapter(voAdapter1);
            voSpinner1.setSelection(0);
        } catch (Exception e) {
            Log.e("GPS-O", "Error actualizando lista de objetos OCAD a partir del tipo seleccionado", e);
        }
    }
    /**
     * Lee los tipos seleccionados de las listas desplegables y recupera a partir 
     * de ellos el ID OCAD del objeto.
     */
    private String obtenerTipoOCADSeleccionado () {
        String vcResul = "101.0";
        try {
            //Dado el tipo de objeto seleccionado, calcula el valor de comienzo 
            //y de fin de los mensajes que corresponden a objetos del tipo relacionado.
            int vnTipo = ((Spinner)findViewById(R.id.lstTipo)).getSelectedItemPosition();
            int vnComienzo = 1000;
            if (vnTipo == 0) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00994));
            }
            else if (vnTipo == 1) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00996));
            }
            else if (vnTipo == 2) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00998));
            }
            else if (vnTipo == 3) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00990));
            }
            else if (vnTipo == 4) {
                vnComienzo = Integer.parseInt(oApp.getString(R.string.ORI_ML00992));
            }
            int vnElemento = vnComienzo + ((Spinner)findViewById(R.id.lstTipoOCAD)).getSelectedItemPosition();
            //Recupera el texto del elemento seleccionado y se queda sólo con la 
            //parte que representa al código OCAD
            String resourceName = "ORI_ML0" + String.format("%04d", vnElemento);
            int vnId = oRes.getIdentifier(resourceName, "string", getPackageName());
            if (vnId != 0) {
                vcResul = oApp.getString(vnId);
                int vnPos = vcResul.indexOf(" ");
                if (vnPos>0)
                    vcResul = vcResul.substring(0, vnPos);
            } else {
                Log.w("GPS-O", "No se encontró el recurso: " + resourceName);
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error obteniendo tipo de OCAD seleccionado", e);
        }
        return vcResul;
    }
    /**
     * Cuando el usuario pulsa el botón de Añadir, se crea un nuevo registro con 
     * los datos en pantalla.
     */
    private void anadirRegistro () {
        try {
            //Sólo responde si no se está realizando la toma automática de datos
            if (!bCorriendo) {
                //Comprueba que se ha escrito una coordenada correcta
                String vcLong = ((EditText)findViewById(R.id.txtLongitud)).getText().toString();
                String vcLat = ((EditText)findViewById(R.id.txtLatitud)).getText().toString();
                vcLong = vcLong.replace(',', '.').trim();
                vcLat = vcLat.replace(',', '.').trim();
                if (!vcLong.isEmpty() && !vcLat.isEmpty() &&
                        Utilidades.esLongitudValida(vcLong) && Utilidades.esLatitudValida(vcLat)) {
                    //Crea un nuevo registro.
                    Registro voRegistro = new Registro();
                    voRegistro.setCID(((EditText) findViewById(R.id.txtId)).getText().toString());
                    voRegistro.setNTipo(((Spinner) findViewById(R.id.lstTipo)).getSelectedItemPosition());
                    //Tipos 3 y 4 corresponden a líneas O-Pie y líneas O-BM => tipo 1
                    if (voRegistro.getNTipo() > 2)
                        voRegistro.setNTipo(1);
                    voRegistro.setCTipoOCAD(obtenerTipoOCADSeleccionado());
                    voRegistro.setCTipoOBM("");
                    voRegistro.setCDesc(((EditText) findViewById(R.id.txtDescripcion)).getText().toString());
                    voRegistro.setCCX(vcLong);
                    voRegistro.setCCY(vcLat);
                    voRegistro.setCElev(((EditText) findViewById(R.id.txtAltitud)).getText().toString());
                    voRegistro.setCFecha(Utilidades.obtenerFechaHoraParaGpx());
                    //Añade el nuevo registro al conjunto de datos
                    vRegistros.add((Registro) voRegistro);
                    //Muestra en una etiqueta de texto el número de registros existente.
                    ((TextView) findViewById(R.id.lblRegistros)).setText(oApp.getString(R.string.ORI_ML00101) + " " + vRegistros.size());
                    //Limpia el contenido del componente, pero deja seleccionados los valores de ID, Tipo y Tipo OCAD, por si el siguiente punto pertenece al mismo objeto.
                    limpiarDatos(false);
                } else {
                    String vcMensaje = this.getString(R.string.ORI_MI00020);
                    Toast.makeText(this.getApplicationContext(), vcMensaje, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error añadiendo registro de datos", e);
        }
    }
    /**
     * Activa la pantalla de cálculo de centroide, para poder obtener una nueva 
     * coordenada ponderada en varias lecturas.
     */
    private void calcularCentroide () {
        try {
            //Sólo responde si no se está realizando la toma automática de datos
            if (!bCorriendo) {
                APrincipal.setOTransf(new TransfGeografica());
                Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ACentroide.class);
                startActivityForResult(viIntent, ACTIVITY_CENTROIDE);
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error llamando a actividad de Centroide", e);
        }
    }
    /**
     * Método que actualiza los datos de coordenadas a partir de la lectura de centroide.
     */
    private void actualizarCoordenada () {
        try {
            TransfGeografica voTransf = APrincipal.getOTransf();
            String cLongitud = voTransf.transfCoordAGrados(voTransf.obtieneCadena(voTransf.nCentro[0][0]));
            if (APrincipal.getCMeridiano().equals("W"))
                cLongitud = "-" + cLongitud;
            ((EditText)findViewById(R.id.txtLongitud)).setText(cLongitud, TextView.BufferType.EDITABLE);
            String cLatitud = voTransf.transfCoordAGrados(voTransf.obtieneCadena(voTransf.nCentro[0][1]));
            if (APrincipal.getCHemisferio().equals("S"))
                cLatitud = "-" + cLatitud;
            ((EditText)findViewById(R.id.txtLatitud)).setText(cLatitud, TextView.BufferType.EDITABLE);
            String cAltitud = voTransf.nCentro[0][2] + "";
            ((EditText)findViewById(R.id.txtAltitud)).setText(cAltitud, TextView.BufferType.EDITABLE);
        } catch (Exception e) {
            Log.e("GPS-O", "Error actualizando datos de coordenadas a partir de Centroide", e);
        }
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
                voRegistro.setNTipo(((Spinner)findViewById(R.id.lstTipo)).getSelectedItemPosition());
                //Tipos 3 y 4 corresponden a líneas O-Pie y líneas O-BM => tipo 1
                if (voRegistro.getNTipo()>2)
                    voRegistro.setNTipo(1);
                voRegistro.setCTipoOCAD(obtenerTipoOCADSeleccionado());
                voRegistro.setCTipoOBM("");
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
                //valores de ID, Tipo y Tipo OCAD, por si el siguiente punto pertenece al mismo objeto.
                limpiarDatos(false);
                botAuto.setText(oApp.getString(R.string.ORI_ML00106), TextView.BufferType.EDITABLE);
            }
        } catch (Exception e) {
            Log.e("GPS-O", "Error cambiando grabación auto", e);
        }
    }

    /**
     * Clase interna que permite gestionar el Thread que periódicamente lee
     * un punto del GPS y lo visualiza en el panel.
     */
    class LogRun implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = ACentroide.GUIUPDATEIDENTIFIER;
                ALog.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
