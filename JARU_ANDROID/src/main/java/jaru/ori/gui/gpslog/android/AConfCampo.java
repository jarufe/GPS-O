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
import android.view.View;
import android.content.Intent;
import android.graphics.Point;

import jaru.ori.logic.campo.*;
import jaru.ori.utils.*;
import jaru.gps.logic.*;
import jaru.ori.utils.android.UtilsAndroid;

/*
 * Edición de los parámetros de configuración del editor de trabajo de campo.
 * <P>
 * Los parámetros del editor de trabajo de campo son de dos tipos.<BR>
 * El primero se refiere a los elementos utilizados para dibujar: imagen de fondo, 
 * bocetos y sus coordenadas.<BR>
 * El segundo tipo tiene que ver con la configuración del editor gráfico la 
 * última vez que se usó.
 * </p>
 * <P>
 * Lo fundamental es elegir una imagen de fondo. Se usará como plantilla para el 
 * dibujado de los elementos gráficos. Lo ideal es que esa imagen de fondo tenga 
 * un fichero de mundo asociado, de forma que esté perfectamente georreferenciado 
 * sin necesidad de mayor intervención por parte del usuario.<BR>
 * Esto significa que la imagen vendrá con la información de coordenada inicial 
 * (esquina superior izquierda) y un factor de resolución, expresado en metros 
 * por pixel.<BR>
 * Si no existe fichero de mundo, el usuario tendrá que dar la información de 
 * georreferenciación.
 * De nuevo, lo principal es escribir el valor de la coordenada UTM que corresponde 
 * a la esquina superior izquierda. Luego, será necesario uno de dos valores: 
 * el factor de resolución o la coordenada de la esquina inferior derecha.
 * <BR>
 * Si el usuario no escribe alguno de los valores necesarios para tener 
 * georreferenciada la imagen, entonces cuando salga del cuadro de diálogo se 
 * calcularán unos valores ficticios.
 * </P>
 * <P>
 * Además de la imagen de fondo, es necesario disponer de unas imágenes que le 
 * servirán al usuario para dibujar. Se crean tres imágenes adicionales: una 
 * para los puntos y líneas; otra para las áreas; la última para dibujar en sucio.<BR>
 * Si el usuario ya tenía ese tipo de imágenes asociadas a una de fondo, puede 
 * seleccionarlas de forma explícita. Si es la primera vez que va a actuar sobre 
 * una imagen de fondo, o si quiere crear unas nuevas imágenes asociadas, dejará 
 * el campo de boceto en blanco.<BR>
 * Las imágenes de bocetos se considera que tienen la misma georreferenciación 
 * que la imagen de fondo.<BR>
 * Si el usuario cambia de imagen de fondo, podría usar las mismas imágenes de 
 * bocetos con otra, siempre y cuando la nueva imagen de fondo tenga la misma 
 * georreferenciación que la anterior.<BR>
 * Si el usuario no selecciona ninguna imagen de boceto, al salir del cuadro de 
 * diálogo se crearán automáticamente las tres necesarias para el buen funcionamiento 
 * del sistema. Se llamarán: boceto_puntos.jpg, boceto_areas.jpg y boceto_sucio.jpg, 
 * respectivamente.<BR>
 * Para seleccionar una imagen de boceto en el cuadro de configuración, basta con 
 * elegir una de ellas. Simplemente habrá que seguir ese mismo tipo de notación: 
 * nombre_puntos.jpg, nombre_areas.jpg y nombre_sucio.jpg.
 * </P>
 * @author jarufe
 * @version 1.0
 */
public class AConfCampo extends Activity {
    private Parametro oParametro = null;
    private ConfCampo oConfCampo = null;
    private int nZoom = 0;
    private String cCXCentral = "";
    private String cCYCentral = "";
    //Dimensiones de la imagen de fondo
    private Point oTam = new Point(0, 0);
    private Application oApp = null;
    private Resources oRes = null;

    private Button botExaminarFondo;
    private Button botExaminarBoceto;
    private EditText txtCX;
    private EditText txtCY;
    private EditText txtFactorX;
    private EditText txtFactorY;
    private EditText txtCX2;
    private EditText txtCY2;
    private EditText txtZona;

    final int ACTIVITY_FILECHOOSER1 = 80;
    final int ACTIVITY_FILECHOOSER2 = 81;

    /**
     * Método que se lanza la primera vez que se crea la actividad. 
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
        setContentView(R.layout.confcampo);
        try {
            //Recoge de la clase principal los elementos básicos que intervienen en el proceso
            oParametro = APrincipal.getOParametro();
            oConfCampo = APrincipal.getOConfCampo();
            //Resto de objetos
            oApp = APrincipal.getOApp();
            oRes = APrincipal.getORes();
            //Establece el valor del directorio inicial donde realizar la selección de fichero
            Utilidades.setCDirActual(APrincipal.getOParametro().getCPathXML());
            Utilidades.setCFicheroSel("");
            Utilidades.setCFicheroSelNombre("");
            //Establece los objetos que representan a los widgets en pantalla
            txtCX = ((EditText)findViewById(R.id.txtCX));
            txtCX.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
                public void onFocusChange(View arg0, boolean arg1) {
                    AConfCampo.this.calcularValores(1);
                }
            });
            txtCY = ((EditText)findViewById(R.id.txtCY));
            txtCY.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
                public void onFocusChange(View arg0, boolean arg1) {
                    AConfCampo.this.calcularValores(2);
                }
            });
            txtFactorX = ((EditText)findViewById(R.id.txtFactorX));
            txtFactorX.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
                public void onFocusChange(View arg0, boolean arg1) {
                    AConfCampo.this.calcularValores(5);
                }
            });
            txtFactorY = ((EditText)findViewById(R.id.txtFactorY));
            txtFactorY.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
                public void onFocusChange(View arg0, boolean arg1) {
                    AConfCampo.this.calcularValores(6);
                }
            });
            txtCX2 = ((EditText)findViewById(R.id.txtCX2));
            txtCX2.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
                public void onFocusChange(View arg0, boolean arg1) {
                    AConfCampo.this.calcularValores(3);
                }
            });
            txtCY2 = ((EditText)findViewById(R.id.txtCY2));
            txtCY2.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
                public void onFocusChange(View arg0, boolean arg1) {
                    AConfCampo.this.calcularValores(4);
                }
            });
            txtZona = ((EditText)findViewById(R.id.txtZona));
        } catch (Exception e) {}
        try {
            //Inicializa el botón para seleccionar un archivo de fondo
            this.botExaminarFondo = (Button)this.findViewById(R.id.botExaminarFondo);
            this.botExaminarFondo.setOnClickListener(new android.view.View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    AConfCampo.this.abrirFondo();
                }
            });
        } catch (Exception e) {}
        try {
            //Inicializa el botón para seleccionar un archivo de boceto
            this.botExaminarBoceto = (Button)this.findViewById(R.id.botExaminarBoceto);
            this.botExaminarBoceto.setOnClickListener(new android.view.View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    AConfCampo.this.abrirBoceto();
                }
            });
        } catch (Exception e) {}
        //Inicializa los demás elementos GUI
        this.inicializarDatos();
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
     * Método que se lanza cuando se preparan las opciones de menú
     * @param menu Menu
     * @return boolean. Devuelve el resultado de la ejecución del método en super.
     */
    @Override
    public boolean  onPrepareOptionsMenu (Menu menu) {
        boolean vbResul = super.onPrepareOptionsMenu(menu);
        return vbResul;
    }

    /**
     * Método que se lanza cuando el usuario selecciona una opción de menú.<BR>
     * Si se pulsa aceptar, se guardan los parámetros de configuración.
     * @param item MenuItem
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
     * Método que se lanza cuando se regresa de la actividad a la que se llama desde ésta.
     * En concreto, esta actividad tiene varios botones para seleccionar un archivo dentro
     * del sistema de archivos.
     * @param requestCode int
     * @param resultCode int
     * @param data  Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean bDim = false;
        switch(requestCode) {
            case ACTIVITY_FILECHOOSER1:
                String vcFichero = Utilidades.getCFicheroSel();
                //Obtiene las dimensiones en pixels de la imagen
                oTam = Utilidades.obtenerTamanoImagen(vcFichero);
                //Comprueba si tiene fichero de mundo y extrae los datos de coordenadas
                String[] vaGeo = Utilidades.obtenerGeorreferencia(vcFichero);
                if (vaGeo!=null) {
                    if (vaGeo.length>=6) {
                        txtFactorX.setText(vaGeo[0], TextView.BufferType.EDITABLE);
                        txtFactorY.setText(vaGeo[3], TextView.BufferType.EDITABLE);
                        txtCX.setText(vaGeo[4], TextView.BufferType.EDITABLE);
                        txtCY.setText(vaGeo[5], TextView.BufferType.EDITABLE);
                        //Aunque no nos hace falta, calculamos la coordenada final
                        //a partir de: coordenada inicial, factor de resolución, tamaño en pixels de la imagen
                        String vcValor1 = Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(vaGeo[4], vaGeo[0], oTam.x);
                        String vcValor2 = Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(vaGeo[5], vaGeo[3], oTam.y);
                        txtCX2.setText(vcValor1, TextView.BufferType.EDITABLE);
                        txtCY2.setText(vcValor2, TextView.BufferType.EDITABLE);
                        bDim = true;
                    }
                }
                //Si no hay georreferenciación, se calculan los valores (si existe coordenada inicial)
                if (!bDim)
                    calcularValores (0);
                ((EditText)findViewById(R.id.txtPlantilla)).setText(vcFichero, TextView.BufferType.EDITABLE);
                break;
            case ACTIVITY_FILECHOOSER2:
                ((EditText)findViewById(R.id.txtBoceto)).setText(Utilidades.getCFicheroSel(), TextView.BufferType.EDITABLE);
                break;
        }
    }

    /**
     * Método que escribe en pantalla los datos que están almacenados en el
     * archivo de configuración.
     */
    private void inicializarDatos () {
        try {
            ((EditText)findViewById(R.id.txtPlantilla)).setText(oConfCampo.getCPlantilla(), TextView.BufferType.EDITABLE);
            ((EditText)findViewById(R.id.txtBoceto)).setText(oConfCampo.getCBoceto(), TextView.BufferType.EDITABLE);
            txtCX.setText(oConfCampo.getCCX(), TextView.BufferType.EDITABLE);
            txtCY.setText(oConfCampo.getCCY(), TextView.BufferType.EDITABLE);
            txtCX2.setText(oConfCampo.getCCX2(), TextView.BufferType.EDITABLE);
            txtCY2.setText(oConfCampo.getCCY2(), TextView.BufferType.EDITABLE);
            txtZona.setText(oConfCampo.getNZona()+"", TextView.BufferType.EDITABLE);
            txtFactorX.setText(oConfCampo.getCFactorX(), TextView.BufferType.EDITABLE);
            txtFactorY.setText(oConfCampo.getCFactorY(), TextView.BufferType.EDITABLE);
            nZoom = oConfCampo.getNZoom();
            cCXCentral = oConfCampo.getCCXCentral();
            cCYCentral = oConfCampo.getCCYCentral();
            ((CheckBox)findViewById(R.id.chkCalidad)).setChecked(oConfCampo.getBCalidad());
        } catch (Exception e) {}
    }
    /**
     * Método que lee los datos de pantalla y los asigna al objeto de la clase ConfCampo
     */
    private void actualizarParametro () {
        try {
            int pnZona = 29;
            //Antes de salir, comprueba si se ha dejado de inicializar la configuración de coordenadas
            //En ese caso, se dan de alta unos valores ficticios
            if (txtCX.getText().toString().equals(""))
                txtCX.setText("500000", TextView.BufferType.EDITABLE);
            if (txtCY.getText().toString().equals(""))
                txtCY.setText("4789000", TextView.BufferType.EDITABLE);
            if (txtZona.getText().toString().equals(""))
                txtZona.setText("29", TextView.BufferType.EDITABLE);
            if (txtFactorX.getText().toString().equals("") &&
                    txtCX2.getText().toString().equals(""))
                txtFactorX.setText("1.0", TextView.BufferType.EDITABLE);
            if (txtFactorY.getText().toString().equals("") &&
                    txtCY2.getText().toString().equals(""))
                txtFactorY.setText("1.0", TextView.BufferType.EDITABLE);
            calcularValores(0);
            //Obtiene los valores finales y actualiza el objeto con los parámetros de configuración
            String pcPlantilla = ((EditText)findViewById(R.id.txtPlantilla)).getText().toString();
            String pcCX = txtCX.getText().toString();
            String pcCY = txtCY.getText().toString();
            String pcCX2 = txtCX2.getText().toString();
            String pcCY2 = txtCY2.getText().toString();
            try {
                pnZona = Integer.parseInt(txtZona.getText().toString());
            } catch (Exception e) {e.printStackTrace();}
            String pcFactorX = txtFactorX.getText().toString();
            String pcFactorY = txtFactorY.getText().toString();
            String pcBoceto = ((EditText)findViewById(R.id.txtBoceto)).getText().toString();
            boolean pbCalidad = ((CheckBox)findViewById(R.id.chkCalidad)).isChecked();
            nZoom = 0;
            cCXCentral = "";
            cCYCentral = "";
            oConfCampo = new ConfCampo(pcPlantilla, pcCX, pcCY, pcCX2, pcCY2, pnZona,
                    pcFactorX, pcFactorY, pcBoceto, nZoom, cCXCentral, cCYCentral, pbCalidad);
            APrincipal.setOConfCampo(oConfCampo);
            //Ahora se comprueba que existen imágenes de bocetos. Sino, las crea con un nombre por defecto.
            oConfCampo.crearImagenesDeBocetos(oParametro.getCPathXML());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que limpia los datos en pantalla
     */
    private void limpiarDatos () {
        try {
            ((EditText)findViewById(R.id.txtPlantilla)).setText("", TextView.BufferType.EDITABLE);
            txtCX.setText("", TextView.BufferType.EDITABLE);
            txtCY.setText("", TextView.BufferType.EDITABLE);
            txtCX2.setText("", TextView.BufferType.EDITABLE);
            txtCY2.setText("", TextView.BufferType.EDITABLE);
            txtZona.setText("", TextView.BufferType.EDITABLE);
            txtFactorX.setText("", TextView.BufferType.EDITABLE);
            txtFactorY.setText("", TextView.BufferType.EDITABLE);
            ((CheckBox)findViewById(R.id.chkCalidad)).setChecked(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * En función del cambio de valores que haga el usuario en el cuadro de diálogo,
     * este método se encarga de calcular el resto. Los valores son, tanto para las
     * coordenadas X como para las Y: coordenada inicial, coordenada final, factor de resolución.
     * @param pnOpcion int. Tipo de cálculo a realizar. 0=se elige imagen; 1=inicial X; 2=inicial Y; 3=final X; 4=final Y; 5=Factor X; 6=Factor Y.
     */
    private void calcularValores (int pnOpcion) {
        try {
            if (oTam.x<=0 || oTam.y<=0)
                oTam = Utilidades.obtenerTamanoImagen(((EditText)findViewById(R.id.txtPlantilla)).getText().toString());
            //Cálculo cuando se elige una nueva imagen de fondo
            if (pnOpcion==0) {
                if (!txtCX.getText().toString().equals("")) {
                    if (!txtFactorX.getText().toString().equals("")) {
                        txtCX2.setText(Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(txtCX.getText().toString(), txtFactorX.getText().toString(), oTam.x), TextView.BufferType.EDITABLE);
                    } else if (!txtCX2.getText().toString().equals("")) {
                        txtFactorX.setText(Utilidades.calcularFactorDesdeCoordenadaInicialMasFinal(txtCX.getText().toString(), txtCX2.getText().toString(), oTam.x), TextView.BufferType.EDITABLE);
                    }
                }
                if (!txtCY.getText().toString().equals("")) {
                    if (!txtFactorY.getText().toString().equals("")) {
                        txtCY2.setText(Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(txtCY.getText().toString(), txtFactorY.getText().toString(), oTam.y), TextView.BufferType.EDITABLE);
                    } else if (!txtCY2.getText().toString().equals("")) {
                        txtFactorY.setText(Utilidades.calcularFactorDesdeCoordenadaInicialMasFinal(txtCY.getText().toString(), txtCY2.getText().toString(), oTam.y), TextView.BufferType.EDITABLE);
                    }
                }
            }
            //Cálculo cuando se cambia la coordenada X inicial
            if (pnOpcion==1) {
                if (!txtCX.getText().toString().equals("")) {
                    if (!txtFactorX.getText().toString().equals("")) {
                        txtCX2.setText(Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(txtCX.getText().toString(), txtFactorX.getText().toString(), oTam.x), TextView.BufferType.EDITABLE);
                    } else if (!txtCX2.getText().toString().equals("")) {
                        txtFactorX.setText(Utilidades.calcularFactorDesdeCoordenadaInicialMasFinal(txtCX.getText().toString(), txtCX2.getText().toString(), oTam.x), TextView.BufferType.EDITABLE);
                    }
                } else {
                    txtCX2.setText("", TextView.BufferType.EDITABLE);
                    txtFactorX.setText("", TextView.BufferType.EDITABLE);
                }
            }
            //Cálculo cuando se cambia la coordenada Y inicial
            if (pnOpcion==2) {
                if (!txtCY.getText().toString().equals("")) {
                    if (!txtFactorY.getText().equals("")) {
                        txtCY2.setText(Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(txtCY.getText().toString(), txtFactorY.getText().toString(), oTam.y), TextView.BufferType.EDITABLE);
                    } else if (!txtCY2.getText().toString().equals("")) {
                        txtFactorY.setText(Utilidades.calcularFactorDesdeCoordenadaInicialMasFinal(txtCY.getText().toString(), txtCY2.getText().toString(), oTam.y), TextView.BufferType.EDITABLE);
                    }
                } else {
                    txtCY2.setText("", TextView.BufferType.EDITABLE);
                    txtFactorY.setText("", TextView.BufferType.EDITABLE);
                }
            }
            //Cálculo cuando se cambia la coordenada X final
            if (pnOpcion==3) {
                if (!txtCX2.getText().toString().equals("")) {
                    if (!txtCX.getText().toString().equals("")) {
                        txtFactorX.setText(Utilidades.calcularFactorDesdeCoordenadaInicialMasFinal(txtCX.getText().toString(), txtCX2.getText().toString(), oTam.x), TextView.BufferType.EDITABLE);
                    }
                } else {
                    txtFactorX.setText("", TextView.BufferType.EDITABLE);
                }
            }
            //Cálculo cuando se cambia la coordenada Y final
            if (pnOpcion==4) {
                if (!txtCY2.getText().toString().equals("")) {
                    if (!txtCY.getText().toString().equals("")) {
                        txtFactorY.setText(Utilidades.calcularFactorDesdeCoordenadaInicialMasFinal(txtCY.getText().toString(), txtCY2.getText().toString(), oTam.y), TextView.BufferType.EDITABLE);
                    }
                } else {
                    txtFactorY.setText("", TextView.BufferType.EDITABLE);
                }
            }
            //Cálculo cuando se cambia el Factor X
            if (pnOpcion==5) {
                if (!txtFactorX.getText().toString().equals("")) {
                    if (!txtCX.getText().toString().equals("")) {
                        txtCX2.setText(Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(txtCX.getText().toString(), txtFactorX.getText().toString(), oTam.x), TextView.BufferType.EDITABLE);
                    }
                } else {
                    txtCX2.setText("", TextView.BufferType.EDITABLE);
                }
            }
            //Cálculo cuando se cambia el Factor Y
            if (pnOpcion==6) {
                if (!txtFactorY.getText().toString().equals("")) {
                    if (!txtCY.getText().toString().equals("")) {
                        txtCY2.setText(Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(txtCY.getText().toString(), txtFactorY.getText().toString(), oTam.y), TextView.BufferType.EDITABLE);
                    }
                } else {
                    txtCY2.setText("", TextView.BufferType.EDITABLE);
                }
            }
        } catch (Exception e) {
        }
    }
    /**
     * Método que permite seleccionar un archivo de fondo a partir de un cuadro de diálogo.
     */
    private void abrirFondo () {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.utils.android.FileChooser.class);
            startActivityForResult(viIntent, ACTIVITY_FILECHOOSER1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que permite seleccionar un archivo de boceto a partir de un cuadro de diálogo.
     */
    private void abrirBoceto () {
        try {
            Intent viIntent = new Intent(this.getApplicationContext(), jaru.ori.utils.android.FileChooser.class);
            startActivityForResult(viIntent, ACTIVITY_FILECHOOSER2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
