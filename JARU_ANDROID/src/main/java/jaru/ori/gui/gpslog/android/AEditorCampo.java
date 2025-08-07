package jaru.ori.gui.gpslog.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;
import android.app.Application;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.Log;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;

import jaru.gps.logic.*;
import java.util.Vector;
import jaru.ori.logic.gpslog.*;
import jaru.ori.logic.campo.*;
import jaru.ori.utils.Utilidades;
import jaru.ori.utils.android.UtilsAndroid;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.graphics.Rect;


/*
 * Editor de trabajo de campo.
 * <P>
 * Se utiliza una imagen de fondo y tres bocetos. La imagen de fondo está georreferenciada.
 * Los bocetos se utilizan para dibujar distintos tipos de elementos: puntos, líneas y
 * trabajo en sucio. Cada boceto se puede visualizar u ocultar en cualquier momento.
 * Los bocetos se dibujan con fondo transparente para que se pueda ver la plantilla que
 * hay por debajo.<BR>
 * Se incorporan funciones para realizar el desplazamiento por la imagen, acercar y 
 * alejar.<BR>
 * También hay funciones para dibujar y borrar lo previamente dibujado.<BR>
 * Se añaden utilidades para establecer el color del lápiz (negro, verde, rojo, azul, gris) y
 * su grosor (fino, grueso)<BR>
 * También existe una función que permite, habiéndose situado en una coordenada, 
 * dibujar una línea con una distancia (metros) y un rumbo (grados) determinados.<BR>
 * Se puede dibujar y borrar en modo raster o vectorial. En modo raster se dibujan píxeles
 * en el boceto seleccionado. En modo vectorial se dibujan objetos de tipo punto, línea o 
 * área que quedan almacenados como registros.
 * </p>
 * @author jarufe
 * @version 1.0
 */
public class AEditorCampo extends Activity {
    public Parametro oParametro = null;
    public GpsInterno oGpsInterno = null;
    public Vector<Registro> vRegistros = null;
    public Vector<Registro> vNuevos = new Vector<Registro>();
    public ConfCampo oConfCampo = null;
    public Application oApp = null;
    public Resources oRes = null;
    //Datos para gestión de GPS
    public Thread oThread;
    public int nRetardo = 50;
    //Objeto que representa la vista de las imágenes
    public Spinner lstEditorCapas;
    public CheckBox chkEditorPuntos;
    public CheckBox chkEditorAreas;
    public CheckBox chkEditorSucio;
    public CheckBox chkEditorFondo;
    public CheckBox chkEditorRegistros;
    public Spinner lstEditorColor;
    public Spinner lstEditorGrosor;
    public Spinner lstEditorModo;
    public ImageButton botDibujar;
    public ImageButton botBorrar;
    public ImageButton botSeleccionar;
    public ImageButton botMover;
    public ImageButton botAcercar;
    public ImageButton botAlejar;
    public ImageButton botVerTodo;
    public ImageButton botSatelite;
    public AEditorCampoView oPanel;
    public TextView lblEditorCoordX;
    public TextView lblEditorCoordY;
    public EditText txtEditorDistancia;
    public EditText txtEditorRumbo;
    public Button botEditorTrazar;

    protected static final int GUIUPDATEIDENTIFIER2 = 0x102;
    final int ACTIVITY_ELIMINAR = 20;
    final int ACTIVITY_SELOCAD = 21;

    Handler iHandler = new Handler() {
        @Override
        public void handleMessage(Message poMsg) {
            switch (poMsg.what) {
                case AEditorCampo.GUIUPDATEIDENTIFIER2:
                    //((TextView)AEditorCampo.this.findViewById(R.id.lblCapas)).setText("Llego 0");
                    if ((oParametro.getCGpsInterno().equals("0") && PuertoSerie.getBAbierto()) ||
                            (oParametro.getCGpsInterno().equals("1") && oGpsInterno!=null)) {
                        //((TextView)AEditorCampo.this.findViewById(R.id.lblCapas)).setText("Llego 1");
                        calcularPosicionGps();
                        oPanel.invalidate();
                    }
                    break;
            }
            super.handleMessage(poMsg);
        }
    };
    /**
     * Procesa el resultado obtenido al regresar de las actividades que se pueden
     * ejecutar desde este editor. Hay dos actividades:<BR>
     * 1.- Eliminar elementos vectoriales. Únicamente hay que refrescar el contenido en pantalla
     * 2.- Seleccionar tipo de objeto OCAD. Se pasan los datos seleccionados para
     * grabar el nuevo elemento OCAD en el conjunto de registros vectoriales.
     * @param requestCode int
     * @param resultCode int
     * @param data Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_ELIMINAR:
                //Cuando vuelve del cuadro de diálogo de eliminación de registros
                //actualiza el contenido en pantalla
                oPanel.invalidate();
                break;
            case ACTIVITY_SELOCAD:
                //Cuando vuelve del cuadro de diálogo de selección de tipo OCAD
                //añade los registros pendientes y actualiza el contenido en pantalla
                int vnTipo = APrincipal.getNTipo();
                String vcTipoOCAD = APrincipal.getCTipoOCAD();
                String vcDesc = APrincipal.getCDesc();
                boolean vbContinuar = APrincipal.getBAceptar();
                if (vbContinuar) {
                    //Obtiene el nuevo identificador, tipo de elemento y tipo OCAD
                    int vnId = Registro.getIdAsignable(vRegistros);
                    //Recorre los registros temporales uno a uno para insertarlos en el Vector de registros de la aplicación
                    for (int i=0; i<vNuevos.size(); i++) {
                        try {
                            //Obtiene el siguiente elemento
                            Registro voRegistro = (Registro)vNuevos.elementAt(i);
                            vRegistros.addElement(new Registro(vnId+"", vnTipo, vcTipoOCAD,
                                    vcDesc, voRegistro.getCCX(), voRegistro.getCCY(), voRegistro.getCElev(), voRegistro.getCFecha()));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                oPanel.invalidate();
                break;
        }
    }

    /*
     * Método que se llama cuando se pulsa el botón para volver atrás.
     * Pregunta al usuario si quiere grabar los cambios realizados sobre las
     * imágenes.
     */
    @Override
    public void onBackPressed() {
        try {
            if (oPanel.voImg!=null) {
                AlertDialog viConfirma = new AlertDialog.Builder(AEditorCampo.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.ORI_MI00014)
                        .setPositiveButton(R.string.ORI_ML00001, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Al salir, se guardan los valores de configuración de nivel de zoom y coordenada central.
                                //También se almacenan las imágnes editadas
                                //Si existe hilo de ejecución para señal de satélite, se finaliza
                                if (oThread!=null)
                                    oThread.interrupt();
                                AEditorCampo.this.actualizarParametro();
                                AEditorCampo.super.onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.ORI_ML00002, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (oThread!=null)
                                    oThread.interrupt();
                                AEditorCampo.super.onBackPressed();
                            }
                        })
                        .create();
                viConfirma.show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    /**
     * Llamado cuando se crea la actividad por primera vez.
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
        setContentView(R.layout.editorcampo);
        try {
            //Recoge de la clase principal los elementos básicos que intervienen en el proceso
            oParametro = APrincipal.getOParametro();
            oGpsInterno = APrincipal.getOGpsInterno();
            vRegistros = APrincipal.getVRegistros();
            oConfCampo = APrincipal.getOConfCampo();
            //Resto de objetos
            oApp = APrincipal.getOApp();
            oRes = APrincipal.getORes();
            //Inicialización
            this.oPanel = (AEditorCampoView)this.findViewById(R.id.imgPlantilla);
            this.oPanel.setOPadre(this);
            this.lblEditorCoordX = (TextView)this.findViewById(R.id.lblEditorCoordX);
            this.lblEditorCoordY = (TextView)this.findViewById(R.id.lblEditorCoordY);
            this.txtEditorDistancia = (EditText)this.findViewById(R.id.txtEditorDistancia);
            this.txtEditorRumbo = (EditText)this.findViewById(R.id.txtEditorRumbo);
            try {
                //Inicializa la lista de capas, para añadir el listener de selección
                this.lstEditorCapas = (Spinner)this.findViewById(R.id.lstEditorCapas);
                this.lstEditorCapas.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    }
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            } catch (Exception e1) {}
            try {
                //Inicializa los checkboxes para visualizar/ocultar bocetos
                this.chkEditorPuntos = (CheckBox)this.findViewById(R.id.chkEditorPuntos);
                this.chkEditorPuntos.setOnClickListener(new android.widget.CheckBox.OnClickListener() {
                    public void onClick(View v) {
                        if (oPanel!=null)
                            oPanel.invalidate();
                    }
                });
                this.chkEditorAreas = (CheckBox)this.findViewById(R.id.chkEditorAreas);
                this.chkEditorAreas.setOnClickListener(new android.widget.CheckBox.OnClickListener() {
                    public void onClick(View v) {
                        if (oPanel!=null)
                            oPanel.invalidate();
                    }
                });
                this.chkEditorSucio = (CheckBox)this.findViewById(R.id.chkEditorSucio);
                this.chkEditorSucio.setOnClickListener(new android.widget.CheckBox.OnClickListener() {
                    public void onClick(View v) {
                        if (oPanel!=null)
                            oPanel.invalidate();
                    }
                });
                this.chkEditorFondo = (CheckBox)this.findViewById(R.id.chkEditorFondo);
                this.chkEditorFondo.setOnClickListener(new android.widget.CheckBox.OnClickListener() {
                    public void onClick(View v) {
                        if (oPanel!=null)
                            oPanel.invalidate();
                    }
                });
                this.chkEditorFondo.setChecked(true);
                this.chkEditorRegistros = (CheckBox)this.findViewById(R.id.chkEditorRegistros);
                this.chkEditorRegistros.setOnClickListener(new android.widget.CheckBox.OnClickListener() {
                    public void onClick(View v) {
                        if (oPanel!=null)
                            oPanel.invalidate();
                    }
                });
            } catch (Exception e1) {}
            try {
                //Inicializa las listas de colores, grosor y modo, para añadir el listener de selección
                this.lstEditorColor = (Spinner)this.findViewById(R.id.lstEditorColor);
                this.lstEditorColor.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    }
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
                this.lstEditorGrosor = (Spinner)this.findViewById(R.id.lstEditorGrosor);
                this.lstEditorGrosor.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    }
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
                this.lstEditorModo = (Spinner)this.findViewById(R.id.lstEditorModo);
                this.lstEditorModo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    }
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            } catch (Exception e1) {}
            //Inicializa los botones de la barra de herramientas
            try {
                this.botDibujar = (ImageButton)this.findViewById(R.id.botDibujar);
                this.botDibujar.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        /*
                        if (oLapiz!=null)
                            oPanel.setCursor(oLapiz);
                        else
                            oPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                         * 
                         */
                        oPanel.setCModo("Dibujar");
                    }
                });
                this.botBorrar = (ImageButton)this.findViewById(R.id.botBorrar);
                this.botBorrar.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        /*
                        if (oGoma!=null)
                            oPanel.setCursor(oGoma);
                        else
                            oPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                         * 
                         */
                        oPanel.setCModo("Borrar");
                    }
                });
                this.botSeleccionar = (ImageButton)this.findViewById(R.id.botSeleccionar);
                this.botSeleccionar.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        //oPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        oPanel.setCModo("Situar");
                    }
                });
                this.botMover = (ImageButton)this.findViewById(R.id.botMover);
                this.botMover.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        //oPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        oPanel.setCModo("Mover");

                    }
                });
                this.botAcercar = (ImageButton)this.findViewById(R.id.botAcercar);
                this.botAcercar.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        //                    oPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        int vnZoom = oConfCampo.getNZoom();
                        vnZoom = vnZoom+1;
                        AEditorCampo.this.cambiarEscala (vnZoom);
                    }
                });
                this.botAlejar = (ImageButton)this.findViewById(R.id.botAlejar);
                this.botAlejar.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        /*
                        oPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                         * 
                         */
                        int vnZoom = oConfCampo.getNZoom();
                        if ((vnZoom-1)>-10)
                            vnZoom = vnZoom-1;
                        AEditorCampo.this.cambiarEscala (vnZoom);
                    }
                });
                this.botVerTodo = (ImageButton)this.findViewById(R.id.botVerTodo);
                this.botVerTodo.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        AEditorCampo.this.cambiarEscala (0);
                    }
                });
                this.botSatelite = (ImageButton)this.findViewById(R.id.botSatelite);
                this.botSatelite.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        AEditorCampo.this.cambiarGrabarAuto();
                    }
                });
                this.botEditorTrazar = (Button)this.findViewById(R.id.botEditorTrazar);
                this.botEditorTrazar.setOnClickListener(new android.view.View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        if (oPanel!=null)
                            oPanel.dibujarLinea(lblEditorCoordX.getText().toString(),
                                    lblEditorCoordY.getText().toString(),
                                    txtEditorDistancia.getText().toString(),
                                    txtEditorRumbo.getText().toString());
                    }
                });
            } catch (Exception e) {
            }
            //Finaliza la inicialización, cargando datos e imágenes en pantalla
            initialize();
        } catch (Exception e) {}
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
     * @return boolean. Devuelve true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log, menu);
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
     * Método que se llama cuando el usuario selecciona una opción de menú.<BR>
     * Si se pulsa el botón grabar, se guardan los parámetros actuales de configuración
     * y las imágenes que se están editando.<BR>
     * Si se pulsa el botón cerrar, se para el hilo de ejecución que recoge periódicamente
     * la señal de satélite, se guardan los parámetros de configuración y las 
     * imágenes editadas.
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle item selection
        if (item.getItemId()==R.id.grabar6) {
            //Se almacenan las imágenes y parámetros actuales, pero sin salir
            String vcMensaje = oApp.getString(R.string.ORI_MI00004);
            if (!this.actualizarParametro())
                vcMensaje = oApp.getString(R.string.ORI_MI00005);
            Toast.makeText(oApp.getApplicationContext(), vcMensaje, Toast.LENGTH_LONG).show();
        } else if (item.getItemId()==R.id.cerrar6) {
            //Al salir, se guardan los valores de configuración de nivel de zoom y coordenada central.
            if (oThread!=null)
                oThread.interrupt();
            this.actualizarParametro();
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
     * Método que establece los datos actuales de configuración del editor de trabajo de campo.
     * @param poConfCampo ConfCampo. Objeto con los parámetros actuales del editor de trabajo de campo.
     */
    public void setOConfCampo(ConfCampo poConfCampo) {
        oConfCampo = poConfCampo;
    }
    /**
     * Método que devuelve el objeto con los datos de configuración del editor de trabajo de campo.
     * @return ConfCampo. Objeto con los nuevos parámetros configurados del editor de trabajo de campo.
     */
    public ConfCampo getOConfCampo() {
        return oConfCampo;
    }
    /**
     * Método que establece los datos de registros ya creados.
     * @param pvRegistros Vector. Conjunto de objetos de la clase Registro.
     */
    public void setVRegistros(Vector<Registro> pvRegistros) {
        vRegistros = pvRegistros;
    }
    /**
     * Método que devuelve el objeto con los datos de registros ya creados.
     * @return Vector. Conjunto de objetos de la clase Registro.
     */
    public Vector<Registro> getVRegistros() {
        return vRegistros;
    }


    /**
     * Inicialización de la clase. Establece el interface gráfico. 
     * Se cargan las imágenes y bocetos.
     * @return void
     */
    private void initialize() {
        try {
            limpiarDatos();
            //Sólo muestra el editor si es capaz de cargar todas las imágenes que se tienen que usar
            //Esto es: plantilla + 3 bocetos (puntos/líneas, áreas y sucio)
            if (!this.cargarImagenes()) {
                //Se muestra un mensaje con el resultado incorrecto del proceso.
                String vcMensaje = oApp.getString(R.string.ORI_MI00013);
                Toast.makeText(oApp.getApplicationContext(), vcMensaje, Toast.LENGTH_LONG).show();
                //Y sale de la actividad
                this.finish();
            }
            /*
            //Carga los cursores personalizados para dibujar y borrar
            Image voImg1 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/jaru/ori/library/images/ORI_GLLAPI.gif"));
            Image voImg2 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/jaru/ori/library/images/ORI_GLGOMA.png"));
            //Coordenada del cursor que se usa como referencia. En este caso, la esquina sup-izq
            Point voHotSpot1 = new Point(0,0);
            Point voHotSpot2 = new Point(2,11);
            oLapiz = Toolkit.getDefaultToolkit().createCustomCursor(voImg1, voHotSpot1, "Lapiz");
            oGoma = Toolkit.getDefaultToolkit().createCustomCursor(voImg2, voHotSpot2, "Goma");
            //Establece el cursor inicial para la imagen -> desplazamiento
            oScr.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
             * 
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que lee los datos de pantalla y los asigna al objeto de la clase ConfCampo
     */
    private boolean actualizarParametro () {
        boolean vbResul = true;
        try {
            //Guarda la configuración del nivel de zoom y coordenada central de la imagen
            oConfCampo.setNZoom(oPanel.getNZoom());
            oConfCampo.setCCXCentral(lblEditorCoordX.getText().toString());
            oConfCampo.setCCYCentral(lblEditorCoordY.getText().toString());
            //Graba los bocetos a disco
            vbResul = oPanel.grabarImagenes(oConfCampo.getCBoceto());
        } catch (Exception e) {
            e.printStackTrace();
            vbResul = false;
        }
        return vbResul;
    }
    /**
     * Método que limpia los datos en pantalla
     */
    private void limpiarDatos () {
        try {
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                voAdapter1.add(oApp.getString(R.string.ORI_ML00147));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00148));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00149));
                this.lstEditorCapas.setAdapter(voAdapter1);
            } catch (Exception e) {}
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                voAdapter1.add(oApp.getString(R.string.ORI_ML00151));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00152));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00153));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00154));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00155));
                this.lstEditorColor.setAdapter(voAdapter1);
            } catch (Exception e) {}
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                voAdapter1.add(oApp.getString(R.string.ORI_ML00169));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00170));
                this.lstEditorGrosor.setAdapter(voAdapter1);
            } catch (Exception e) {}
            try {
                ArrayAdapter<CharSequence> voAdapter1 = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
                voAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                voAdapter1.add(oApp.getString(R.string.ORI_ML00173));
                voAdapter1.add(oApp.getString(R.string.ORI_ML00174));
                this.lstEditorModo.setAdapter(voAdapter1);
            } catch (Exception e) {}
            this.lstEditorCapas.setSelection(0);
            this.lstEditorColor.setSelection(0);
            this.lstEditorGrosor.setSelection(1);
            this.lstEditorModo.setSelection(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que carga y visualiza la imagen de fondo (si existe)
     * Este Método se llama desde la pantalla principal, como punto de comienzo
     * de la edición de trabajo de campo
     * @return boolean Devuelve si la carga ha sido correcta o no.
     */
    private boolean cargarImagenes() {
        boolean vbResul = false;
        try {
            if (oPanel==null) {
                oPanel = (AEditorCampoView)this.findViewById(R.id.imgPlantilla);
                oPanel.setOPadre(this);
            }
            //Si existen todos los ficheros, trata de cargarlos
            if (oConfCampo.existenTodosLosFicheros()) {
                //oPanel.setLayout(null);
                if (oPanel.cargarImagenes(oConfCampo.getCPlantilla(), oConfCampo.getCBoceto()))
                    vbResul = true;
                //Establece la escala y posición que está almacenada en los parámetros de configuración.
                try {
                    cambiarEscala(oConfCampo.getNZoom());
                } catch (Exception e) {
                    vbResul = false;
                    e.printStackTrace();
                }
                try {
                    if (!oConfCampo.getCCXCentral().equals("") && !oConfCampo.getCCYCentral().equals("")) {
                        //Calcula la coordenada central en función de los valores almacenados
                        int vnOrigX = (int)Double.parseDouble(oConfCampo.getCCX());
                        int vnOrigY = (int)Double.parseDouble(oConfCampo.getCCY());
                        int vnCentroX = (int)Double.parseDouble(oConfCampo.getCCXCentral()) - vnOrigX;
                        int vnCentroY = vnOrigY - (int)Double.parseDouble(oConfCampo.getCCYCentral());
                        //Dependiendo de en qué sentido se incrementen las coordenadas,
                        //habrá que modificar el signo de la resta para que quede positivo
                        if (vnCentroX<0)
                            vnCentroX = vnCentroX * -1;
                        if (vnCentroY<0)
                            vnCentroY = vnCentroY * -1;
                        //Llama al método que posiciona las barras de desplazamiento
                        this.establecerNuevaPosicionScrollBar(new Point(vnCentroX, vnCentroY));
                    }
                } catch (Exception e) {
                    vbResul = false;
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            vbResul = false;
            e.printStackTrace();
        }
        return vbResul;
    }

    /**
     * Método que permite cambiar la escala de visualización de la imagen
     * @param pnEscala int. Valor de escala (-10..0..x)
     */
    private void cambiarEscala (int pnEscala) {
        //Lee el zoom que había antes del nuevo cambio de escala
        int vnZoomAnt = oConfCampo.getNZoom();
        //Actualiza el nivel de zoom
        oConfCampo.setNZoom(pnEscala);
        //Obtiene la coordenada del centro de la imagen en pantalla, según escala anterior
        Rect voRect = new Rect (0, 0, oPanel.voImg.getWidth(), oPanel.voImg.getHeight());
        Point voCentro = new Point((int)(voRect.left+(voRect.width()/2)), (int)(voRect.top+(voRect.height()/2)));
        //Recalcula ese mismo centro, pero según el tamaño original de la imagen
        int vnCentroXOrig = (int)((voCentro.x / ((vnZoomAnt/10.0)+1)) * Math.abs(Double.parseDouble(oConfCampo.getCFactorX())));
        int vnCentroYOrig = (int)((voCentro.y / ((vnZoomAnt/10.0)+1)) * Math.abs(Double.parseDouble(oConfCampo.getCFactorY())));
        voCentro.set(vnCentroXOrig, vnCentroYOrig);
        //Guarda la posición del nuevo centro de pantalla
        oPanel.setOCentro (voCentro);
        //Llama al método que se encarga de aplicar y visualizar el nuevo zoom
        oPanel.setNZoom(oConfCampo.getNZoom());
        //Sitúa el origen de la pantalla de manera que el centro siga siendo el mismo punto
        //this.establecerNuevaPosicionScrollBar(voCentro);
    }

    /**
     * Después de un cambio de escala, actualiza la posición de las barras de desplazamiento
     * para seguir visualizando la imagen en la posición correcta.
     * @param poCentro
     */
    private void establecerNuevaPosicionScrollBar(Point poCentro) {
        //Calcula la coordenada del centro, según la nueva escala
        int vnNuevoX = (int)(((poCentro.x * oPanel.getNScale())) / Math.abs(Double.parseDouble(oConfCampo.getCFactorX())));
        int vnNuevoY = (int)(((poCentro.y * oPanel.getNScale())) / Math.abs(Double.parseDouble(oConfCampo.getCFactorY())));
        //Guarda la posición del nuevo centro de pantalla
        oPanel.setOCentro (new Point(vnNuevoX, vnNuevoY));
        //Fuerza el repintado de la pantalla
        oPanel.invalidate();
    }

    /**
     * Método que alterna la activación/ocultación de la visualización de la posición del GPS
     */
    private void cambiarGrabarAuto () {
        try {
            //Si estaba parado, pone a correr el proceso de recogida automática de datos.
            if (!oPanel.getBGpsActivado()) {
                //Crea el nuevo hilo de ejecución y lo pone a correr
                oThread = new Thread(new EditorCampoRun());
                oThread.start();
                oPanel.setBGpsActivado(true);
            }
            else {
                if(oThread!=null)
                    oThread.interrupt();
                oPanel.setBGpsActivado(false);
                oPanel.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Calcula la coordenada UTM correspondiente a la posición del GPS
     */
    private void calcularPosicionGps() {
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
            } catch (Exception e) {
            }
//((TextView)AEditorCampo.this.findViewById(R.id.lblCapas)).setText(voSentencia.cLongitud);
            //Convierte la sentencia NMEA a un valor de coordenada en UTM
            if (voSentencia.cLongitud.length()>0 && voSentencia.cLatitud.length()>0) {
                //Obtiene los valores transformados de la coordenada leída
                TransfGeografica voTransf = new TransfGeografica();
                String vcLongitud = voTransf.transfCoord(voTransf.obtieneCadena(voTransf.obtieneLong(voSentencia.cLongitud)));
                if (voSentencia.cMeridiano.equals("W"))
                    vcLongitud = "-" + vcLongitud;
                String vcLatitud = voTransf.transfCoord(voTransf.obtieneCadena(voTransf.obtieneLong(voSentencia.cLatitud)));
                if (voSentencia.cHemisferio.equals("S"))
                    vcLongitud = "-" + vcLongitud;
                //Convierte la coordenada de grados a UTM
                double[] vaCoord = Utilidades.convertirLatLongToUTM(vcLongitud, vcLatitud);
                //Establece el valor del punto que representa la posición del GPS
                if (vaCoord!=null && vaCoord.length>=3) {
                    double vnGpsX = vaCoord[1];
                    double vnGpsY = vaCoord[2];
                    oPanel.setOPosGps(new Point((int)vnGpsX, (int)vnGpsY));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que se encarga de mostrar los registros vectoriales y le permite
     * al usuario eliminar uno o varios.
     */
    public void borrarRegistros () {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.AEliminarRegistros.class);
            startActivityForResult(viIntent, ACTIVITY_ELIMINAR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que se encarga de grabar los registros temporales en el vector de registros general
     * de la aplicación.
     */
    public void grabarRegistros () {
        try {
            //Si hay registros temporales
            if (vNuevos.size()>0) {
                //Por defecto se trata como de tipo punto. Si hay más de un elemento, 
                //será una línea o un área en función del boceto que esté seleccionado
                int vnTipo = 0;
                //Si está seleccionada la capa de puntos/líneas o de sucio
                if (vNuevos.size()>1 &&
                        (lstEditorCapas.getSelectedItemPosition()==0 ||
                                lstEditorCapas.getSelectedItemPosition()==2))
                    vnTipo = 1;
                //Si está seleccionada la capa de áreas
                if (vNuevos.size()>1 && lstEditorCapas.getSelectedItemPosition()==1)
                    vnTipo = 2;
                //Guarda el tipo como variable global, para que lo recoja el cuadro de diálogo
                APrincipal.setNTipo(vnTipo);
                //Llama a un cuadro de diálogo que le permite al usuario establecer el tipo OCAD del objeto
                Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.gui.gpslog.android.ASelOCAD.class);
                startActivityForResult(viIntent, ACTIVITY_SELOCAD);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Clase interna que permite gestionar el Thread que periódicamente lee
     * un punto del GPS y lo visualiza en el panel.
     */
    class EditorCampoRun implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = new Message();
                message.what = AEditorCampo.GUIUPDATEIDENTIFIER2;
                AEditorCampo.this.iHandler.sendMessage(message);
                try {
                    Thread.sleep(nRetardo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }




}
