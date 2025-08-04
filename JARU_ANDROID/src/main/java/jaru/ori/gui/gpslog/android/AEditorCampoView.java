package jaru.ori.gui.gpslog.android;

import android.graphics.Point;

import java.util.Vector;
import jaru.ori.logic.gpslog.*;
import jaru.ori.utils.*;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.graphics.Paint;
import android.util.AttributeSet;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.MotionEvent;

/**
 * Panel que se encarga del dibujado y manipulación de las imágenes en el editor
 * de trabajo de campo<BR>
 * Esta clase contiene toda la funcionalidad gráfica, como complemento de la 
 * clase AEditorCampo, que es la actividad que engloba todas las tareas 
 * relacionadas con el editor de trabajo de campo.
 * @author jarufe
 * @version 1.0
 *
 */
public class AEditorCampoView extends View {
    //Imagen de fondo y tres bocetos
    //En Swing es BufferedImage
    public Bitmap voImg = null;
    public Bitmap[] vaBoceto = new Bitmap[3];
    //
    public Matrix voMatriz = new Matrix();
    //
    private int nZoom = 0;
    private Point preferredSize = new Point(100, 100);
    private Point oCentro = new Point(0,0);
    private Point oAnchor = new Point(0,0);
    private Point oPosGps = new Point(0,0);
    private String cModo = "Mover";
    private boolean bGpsActivado = false;
    private Point oTraslacion = new Point(0,0);

    private AEditorCampo oPadre = null;

    /**
     * Esablece la propiedad que se refiere a la clase padre (AEditorCampo)
     * @param poPadre AEditorCampo
     */
    public void setOPadre (AEditorCampo poPadre) {
        oPadre = poPadre;
    }
    /**
     * Devuelve el objeto que se refiere a la clase padre
     * @return AEditorCampo
     */
    public AEditorCampo getOPadre() {
        return oPadre;
    }
    /**
     * Devuelve el objeto que contiene las coordenadas del centro de imagen
     * @return Point
     */
    public Point getOCentro () {
        return oCentro;
    }
    /**
     * Establece el objeto que contiene las coordenadas del centro de imagen
     * @param poCentro Point
     */
    public void setOCentro (Point poCentro) {
        oCentro = poCentro;
    }
    /**
     * Devuelve el objeto que contiene las coordenadas actuales de GPS
     * @return Point
     */
    public Point getOPosGps () {
        return oPosGps;
    }
    /**
     * Establece el objeto que contiene las coordenadas actuales de GPS
     * @param poPosGps Point
     */
    public void setOPosGps (Point poPosGps) {
        oPosGps = poPosGps;
    }
    /**
     * Devuelve el valor de nivel de zoom actual
     * @return int
     */
    public int getNZoom () {
        return nZoom;
    }
    /**
     * Devuelve el valor de la escala actual. Es igual al nivel de zoom / 10, + 1
     * @return double
     */
    public double getNScale() {
        return ((nZoom/10.0)+1);
    }
    /**
     * Establece el nivel de zoom actual
     * @param pnZoom int
     */
    public void setNZoom (int pnZoom) {
        nZoom = pnZoom;
        updatePreferredSize();
    }
    /**
     * Devuelve una cadena de texto que indica el modo de edición, lo cual sirve
     * para distinguir qué tarea realizar cuando el usuario se mueve o toca algo
     * en pantalla. Puede ser: Situar (para leer la coordenada del punto tocado en pantalla)
     * , Dibujar (para realizar el dibujado de elementos gráficos)
     * , Borrar (para borrar elementos dibujados)
     * , Mover (para realizar el desplazamiento de las imágenes)
     * @return String
     */
    public String getCModo () {
        return cModo;
    }
    /**
     * Establece el modo de edición: Situar, Dibujar, Borrar, Mover
     * @param pcModo
     */
    public void setCModo (String pcModo) {
        cModo = pcModo;
    }
    /**
     * Devuelve si la lectura de GPS está activada o no
     * @return boolean
     */
    public boolean getBGpsActivado () {
        return bGpsActivado;
    }
    /**
     * Establece si la lectura de GPS está activada o no
     * @param pbGpsActivado boolean
     */
    public void setBGpsActivado (boolean pbGpsActivado) {
        bGpsActivado = pbGpsActivado;
    }
    /**
     * Devuelve el desplazamiento que hay que realizar sobre la imagen para
     * situarla en el punto de vista correcto, con respecto al desplazamiento
     * ordenado por el usuario
     * @return Point
     */
    public Point getOTraslacion () {
        return oTraslacion;
    }
    /**
     * Establece el desplazamiento que hay que realizar sobre la imagen para
     * situarla en el punto correcto.
     * @param poTraslacion Point
     */
    public void setOTraslacion (Point poTraslacion) {
        oTraslacion = poTraslacion;
    }

    /**
     * Constructor por defecto de la clase.
     */
    public AEditorCampoView(Context context) {
        super(context);
    }
    /**
     * Otro constructor de la clase.
     */
    public AEditorCampoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Establece las dimensiones medidas para la vista, calculando tanto el 
     * ancho como el alto, en función de lo que pida el SO.<BR>
     * La anchura de esta parte gráfica se establece como el 75% de la anchura que
     * tenga la pantalla del dispositivo.<BR>
     * La altura se establece como la del dispositivo - 400 puntos.<BR>
     * De esta forma, lo que se intenta es reservar la mayor parte del ancho 
     * de la pantalla del dispositivo para la vista gráfica del editor y ajustar
     * al máximo la altura, dejando en ambos casos espacio suficiente para alojar
     * el resto de botones y controles de edición del componente.
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }
    /**
     * Determina la anchura de esta vista
     * @param measureSpec int Tipo de especificación de medida
     * @return int Medida de anchura de la vista
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Ejemplo de medida. Valor puesto aleatoriamente
            result = (int) 250;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                //result = Math.min(result, specSize);
                result = (int)(specSize*0.75);
            }
        }

        return result;
    }

    /**
     * Determina la altura de esta vista
     * @param measureSpec int Tipo de especificación de medida
     * @return int Medida de altura de la vista
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Ejemplo de medida. Valor puesto aleatoriamente
            result = 150;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                //result = Math.min(result, specSize);
                result = (int)(specSize-400);
            }
        }
        return result;
    }

    /**
     * Método que se lanza cuando el usuario toca la pantalla.<BR>
     * Cuando se toca la pantalla, si se encuentra en modo "Situar", se recalcula
     * la coordenada de la imagen en el punto tocado. Si el modo es "Dibujar" y
     * se está en modo vectorial, se inicia un nuevo objeto para almacenar las
     * coordenadas de dibujo.<BR>
     * Cuando se realiza el desplazamiento por la pantalla, si el modo es "Mover"
     * Se realiza el desplazamiento de la imagen. Si el modo es "Dibujar" o "Borrar"
     * se procesa el movimiento del cursor para realizar el dibujado o el borrado
     * de elementos gráficos del boceto actual.<BR>
     * Cuando se deja de tocar la pantalla, si el modo es "Dibujar" y se encuentra
     * en modo vectorial, se realiza la grabación de un nuevo elemento vectorial.
     * Por el contrario, si el modo es "Borrar" y se encuentra en modo vectorial, se
     * lanza la actividad que permite eliminar registros vectoriales.
     * @param event MotionEvent Contiene información del evento lanzado
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //Obtiene la posición del cursor, para control posterior del arrastre
            oAnchor.set((int)event.getX(), (int)event.getY());
            //Escribe las coordenadas de la posición del cursor en las etiquetas correspondientes
            if (cModo.equals("Situar"))
                recalcularCoordenadaImagen (false);
                //Si estamos en modo de dibujo vectorial, reiniciamos el vector de datos de nuevos registros
            else if (cModo.equals("Dibujar") && getOPadre().lstEditorModo.getSelectedItemPosition()>0) {
                getOPadre().vNuevos = new Vector<Registro>();
                //Añade el punto correspondiente a la coordenada actual del ratón como Registro temporal
                anadirPuntoComoRegistro();
            }
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            // Al levantar el dedo o ratón, si se está en modo de dibujo vectorial hay que
            // almacenar los registros correspondientes a todas las posiciones del ratón.
            // Hay que pedir al usuario que seleccione el tipo OCAD de que se trata.
            if (cModo.equals("Dibujar") && getOPadre().lstEditorModo.getSelectedItemPosition()>0) {
                //Llama al método que graba los registros
                grabarRegistros();
            }
            if (cModo.equals("Borrar") && getOPadre().lstEditorModo.getSelectedItemPosition()>0) {
                //Llama al método que permite eliminar registros vectoriales
                borrarRegistros();
            }
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            //El arrastre del dedo/ratón significa diferentes cosas en función del modo en
            //que estemos trabajando.
            //En modo "Mover", hay que desplazar la imagen a la par del desplazamiento del ratón.
            //En modo "Borrar", hay que borrar lo dibujado en la capa activa.
            //En modo "Dibujar", hay que dibujar a mano alzada en la capa activa.
            if (cModo.equals("Mover")) {
                moverCursor(event);
            } else if (cModo.equals("Borrar") || cModo.equals("Dibujar")) {
                dibujarCursor(event);
            }
            //Si estamos en modo de dibujo vectorial, añadimos un nuevo registro temporal
            if (cModo.equals("Dibujar") && getOPadre().lstEditorModo.getSelectedItemPosition()>0) {
                //Añade el punto correspondiente a la coordenada actual del ratón como Registro temporal
                anadirPuntoComoRegistro();
            }
        }
        return true;
    }

    /**
     * Método que gestiona el arrastre de la imagen cuando se está en modo de desplazamiento
     * @param poEvento MotionEvent. Evento de movimiento
     */
    private void moverCursor(MotionEvent poEvento) {
        try{
            Point newPosition = new Point((int)poEvento.getX(), (int)poEvento.getY());
            int dx, dy;
            dx = oAnchor.x - newPosition.x;
            dy = oAnchor.y - newPosition.y;
            // update pan anchor
            oAnchor.x = newPosition.x;
            oAnchor.y = newPosition.y;
            int x, y;
            oTraslacion.x = oTraslacion.x - dx;
            oTraslacion.y = oTraslacion.y - dy;
            /*
            x = viewport.getViewPosition().x + dx;
            y = viewport.getViewPosition().y + dy;
            if ( x < 1 || this.getSize().getWidth() < oScr.getVisibleRect().width + jsbY.getWidth())
                x = 1;
            else if( x > (this.getSize().getWidth() - oScr.getVisibleRect().width + jsbY.getWidth()))
                x = (int)this.getSize().getWidth() - oScr.getVisibleRect().width + jsbY.getWidth();
            if ( y < 1 || this.getSize().getHeight() < oScr.getVisibleRect().height + jsbX.getHeight())
                y = 1;
            else if( y > (this.getSize().getHeight() - oScr.getVisibleRect().height + jsbX.getHeight()))
                y = (int)this.getSize().getHeight() - oScr.getVisibleRect().height + jsbX.getHeight();
            viewport.setViewPosition(new Point(x,y));
             * 
             */
        } catch(Exception e2){}
        this.invalidate();
    }
    /**
     * Método que gestiona el pintado sobre la superficie de una de las imágenes de boceto.
     * @param poEvento MotionEvent. Evento del ratón.
     */
    private void dibujarCursor(MotionEvent poEvento) {
        try{
            //Obtiene la coordenada de la posición del ratón, según escala actual
            Point voFin = new Point((int)poEvento.getX(), (int)poEvento.getY());
            Point voIni = new Point(oAnchor.x, oAnchor.y);
            oAnchor.set(voFin.x, voFin.y);
            //Recalcula ese mismo punto, pero según el tamaño original de la imagen
            voFin.set((int)((voFin.x-oTraslacion.x) / getNScale()),
                    (int)((voFin.y-oTraslacion.y) / getNScale()));
            voIni.set((int)((voIni.x-oTraslacion.x) / getNScale()),
                    (int)((voIni.y-oTraslacion.y) / getNScale()));
            //Dibuja una línea entre las coordenadas inicial y final
            dibujarLinea(voIni, voFin);
        } catch(Exception e2){
        }
        this.invalidate();
    }
    /**
     * Método que traza una línea en el boceto activo, con el color activo, a partir
     * de una coordenada, una distancia (en metros) y un rumbo
     * @param pcCx String Coordenada UTM X del punto de origen
     * @param pcCy String Coordenada UTM Y del punto de origen
     * @param pcDist String Distancia en metros
     * @param pcRumbo String Rumbo en grados
     */
    public void dibujarLinea (String pcCx, String pcCy, String pcDist, String pcRumbo) {
        Point voIni = new Point(0, 0);
        Point voFin = new Point(0, 0);
        //Calcula la coordenada central en función de los valores almacenados
        int vnOrigX = (int)Double.parseDouble(getOPadre().oConfCampo.getCCX());
        int vnOrigY = (int)Double.parseDouble(getOPadre().oConfCampo.getCCY());
        if (Double.parseDouble(getOPadre().oConfCampo.getCFactorX())>0)
            voIni.x = (int)Double.parseDouble(pcCx)-vnOrigX;
        else
            voIni.x = vnOrigX - (int)Double.parseDouble(pcCx);
        if (Double.parseDouble(getOPadre().oConfCampo.getCFactorY())>0)
            voIni.y = (int)Double.parseDouble(pcCy) - vnOrigY;
        else
            voIni.y = vnOrigY - (int)Double.parseDouble(pcCy);
        int vnDist = (int)Double.parseDouble(pcDist);
        double vnAng = Double.parseDouble(pcRumbo);
        //Traslada el ángulo, ya que el de la brújula y el de pantalla no es el mismo
        vnAng = 360 - (vnAng - 90);
        //Calcula la coordenada final en función de la inicial, la distancia y el ángulo
        voFin.x = voIni.x + (int)(vnDist * Math.cos(Math.toRadians(vnAng)));
        voFin.y = voIni.y - (int)(vnDist * Math.sin(Math.toRadians(vnAng)));
        //Calcula las coordenadas inicial y final. No se aplica factor de escala, ya que estamos
        //con coordenadas de imagen. Sí que se aplica el factor de resolución.
        voIni.x = (int)((voIni.x) / Math.abs(Double.parseDouble(getOPadre().oConfCampo.getCFactorX())));
        voIni.y = (int)((voIni.y) / Math.abs(Double.parseDouble(getOPadre().oConfCampo.getCFactorY())));
        voFin.x = (int)((voFin.x) / Math.abs(Double.parseDouble(getOPadre().oConfCampo.getCFactorX())));
        voFin.y = (int)((voFin.y) / Math.abs(Double.parseDouble(getOPadre().oConfCampo.getCFactorY())));
        //Ahora dibuja una línea, en el boceto y con el color que están activos.
        dibujarLinea (voIni, voFin);
    }

    /**
     * Método que gestiona el pintado sobre la superficie de una de las imágenes de boceto.
     * @param poIni Point. Punto inicial, en coordenadas de la escala de visualización.
     * @param poFin Point. Punto final, en coordenadas de la escala de visualización.
     */
    public void dibujarLinea(Point poIni, Point poFin) {
        Bitmap voImgDest = null;
        int vnRgb = 0xFF323232; // negro
        try{
            //Establece en qué boceto hay que dibujar
            if (getOPadre().lstEditorCapas.getSelectedItemPosition()==0)
                voImgDest = vaBoceto[0];
            else if (getOPadre().lstEditorCapas.getSelectedItemPosition()==1)
                voImgDest = vaBoceto[1];
            else if (getOPadre().lstEditorCapas.getSelectedItemPosition()==2)
                voImgDest = vaBoceto[2];
            //Establece el color del pincel
            if (getOPadre().lstEditorColor.getSelectedItemPosition()==0)
                vnRgb = 0xFF323232;
            else if (getOPadre().lstEditorColor.getSelectedItemPosition()==3)
                vnRgb = 0xFF0000FF;
            else if (getOPadre().lstEditorColor.getSelectedItemPosition()==4)
                vnRgb = 0xFF888888;
            else if (getOPadre().lstEditorColor.getSelectedItemPosition()==2)
                vnRgb = 0xFFFF0000;
            else if (getOPadre().lstEditorColor.getSelectedItemPosition()==1)
                vnRgb = 0xFF00FF00;
            //Si estamos en modo de borrado, se trata de poner transparente
            if (cModo.equals("Borrar"))
                vnRgb = 0x00FFFFFF;
            //Sólo pinta si alguno de los destinatarios está seleccionado
            if (voImgDest!=null) {
                //Pinta los pixels adyacentes a la coordenada
                int x1 = poIni.x;
                int y1 = poIni.y;
                int x2 = poFin.x;
                int y2 = poFin.y;
                if (Math.abs(x1-x2)>Math.abs(y1-y2)) {
                    if (poIni.x > poFin.x) {
                        x1 = poFin.x;
                        y1 = poFin.y;
                        x2 = poIni.x;
                        y2 = poIni.y;
                    }
                    for (int x=x1; x<=x2; x++) {
                        double y = y1 + ( ((x-x1)/(double)(x2-x1)) * (y2-y1) );
                        voImgDest.setPixel(x, (int)y, vnRgb);
                        if (getOPadre().lstEditorGrosor.getSelectedItemPosition()>0) {
                            voImgDest.setPixel(x-1, (int)y, vnRgb);
                            voImgDest.setPixel(x+1, (int)y, vnRgb);
                            voImgDest.setPixel(x, (int)y-1, vnRgb);
                            voImgDest.setPixel(x, (int)y+1, vnRgb);
                        }
                        //Si estamos en modo de borrado, se pinta más grueso, pero también dependiendo del grosor seleccionado
                        if (cModo.equals("Borrar")) {
                            int vnLim = 3;
                            if (getOPadre().lstEditorGrosor.getSelectedItemPosition()>0)
                                vnLim = 5;
                            for (int i=(int)x-vnLim; i<(int)x+vnLim; i++)
                                for (int j=(int)y-vnLim; j<(int)y+vnLim; j++)
                                    voImgDest.setPixel(i, j, vnRgb);
                        }
                    }
                } else {
                    if (poIni.y > poFin.y) {
                        x1 = poFin.x;
                        y1 = poFin.y;
                        x2 = poIni.x;
                        y2 = poIni.y;
                    }
                    for (int y=y1; y<=y2; y++) {
                        double x = x1 + ( ((y-y1)/(double)(y2-y1)) * (x2-x1) );
                        voImgDest.setPixel((int)x, (int)y, vnRgb);
                        if (getOPadre().lstEditorGrosor.getSelectedItemPosition()>0) {
                            voImgDest.setPixel((int)x-1, (int)y, vnRgb);
                            voImgDest.setPixel((int)x+1, (int)y, vnRgb);
                            voImgDest.setPixel((int)x, (int)y-1, vnRgb);
                            voImgDest.setPixel((int)x, (int)y+1, vnRgb);
                        }
                        //Si estamos en modo de borrado, se pinta más grueso, pero también dependiendo del grosor seleccionado
                        if (cModo.equals("Borrar")) {
                            int vnLim = 3;
                            if (getOPadre().lstEditorGrosor.getSelectedItemPosition()>0)
                                vnLim = 5;
                            for (int i=(int)x-vnLim; i<(int)x+vnLim; i++)
                                for (int j=(int)y-vnLim; j<(int)y+vnLim; j++)
                                    voImgDest.setPixel(i, j, vnRgb);
                        }
                    }
                }
            }
        } catch(Exception e2){}
        this.invalidate();
    }


    /**
     * Realiza las tareas de repintado de los elementos gráficos, tanto Raster como
     * Vector, así como la posición del GPS y de los datos de localización del centro
     * de imagen (o punto en que se encuentra el cursor en un momento dado)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
            //Personalización
            Paint voNormal = new Paint();
            voNormal.setColor(0xFFFF0000);
            Paint voExtra = new Paint();
            voExtra.setColor(0xFF000000);
            //Dibuja un fondo blanco en toda la pantalla
            //Paint voFondo = new Paint();
            //voFondo.setColor(0xFFFFFFFF);
            //canvas.drawRect(0, 0, getWidth(), getHeight(), voFondo);
            //canvas.drawLine(0, 0, getWidth(), getHeight(), voNormal);
            //
            if (voImg!=null) {
            /*
            RenderingHints renderHints = null;
            //El usuario puede configurar el modo de renderizado, entre calidad y rendimiento
            if (!oConfCampo.getBCalidad()) {
                renderHints =  new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_OFF);
                renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            } else {
                renderHints = new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                     RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
            voG2d.setRenderingHints(renderHints);
             * 
             */
                //Si la traslación era 0 (primera vez), calcula con respecto a lo grabado en fichero
                //Se hace así porque desde la clase padre no se dispone del valor del tamaño del panel
                if (oTraslacion.x==0 && oTraslacion.y==0) {
                    int vnOrigX = (this.getWidth()/2) - oCentro.x;
                    int vnOrigY = (this.getHeight()/2) - oCentro.y;
                    //Establece la nueva posición de las barras de desplazamiento
                    this.setOTraslacion(new Point(vnOrigX, vnOrigY));
                }
                //Aplica un desplazamiento
                canvas.translate(oTraslacion.x, oTraslacion.y);
                //Aplica un zoom
                voMatriz.setScale((float)getNScale(), (float)getNScale());
                //Dibuja los bitmaps
                if (getOPadre().chkEditorFondo.isChecked())
                    canvas.drawBitmap(voImg, voMatriz, voExtra);
                if (getOPadre().chkEditorAreas.isChecked())
                    canvas.drawBitmap(vaBoceto[1], voMatriz, voExtra);
                if (getOPadre().chkEditorPuntos.isChecked())
                    canvas.drawBitmap(vaBoceto[0], voMatriz, voExtra);
                if (getOPadre().chkEditorSucio.isChecked())
                    canvas.drawBitmap(vaBoceto[2], voMatriz, voExtra);
                //Escribe las coordenadas del centro del panel en las etiquetas correspondientes
                if (!cModo.equals("Situar"))
                    recalcularCoordenadaImagen (true);
                //Dibuja los datos vectoriales, si el usuario ha marcado la opción correspondiente
                if (getOPadre().chkEditorRegistros.isChecked())
                    volcarDatosVectoriales(canvas);
                //Si está activado el botón para visualizar la posición del GPS, dibuja una cruz en ese lugar
                if (bGpsActivado) {
                    dibujarPosicionGps (canvas);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que dibuja una cruz en la posición indicada por el GPS
     * @param poG2D Canvas Contexto gráfico
     */
    private void dibujarPosicionGps (Canvas poG2D) {
//((android.widget.TextView)getOPadre().findViewById(R.id.lblCapas)).setText("Llego 5");
        int vnCentroX = 0;
        int vnCentroY = 0;
        //Calcula la coordenada central en función de los valores almacenados
        int vnOrigX = (int)Double.parseDouble(getOPadre().oConfCampo.getCCX());
        int vnOrigY = (int)Double.parseDouble(getOPadre().oConfCampo.getCCY());
        if (Double.parseDouble(getOPadre().oConfCampo.getCFactorX())>0)
            vnCentroX = (int)oPosGps.x - vnOrigX;
        else
            vnCentroX = vnOrigX - (int)oPosGps.x;
        if (Double.parseDouble(getOPadre().oConfCampo.getCFactorY())>0)
            vnCentroY = (int)oPosGps.y - vnOrigY;
        else
            vnCentroY = vnOrigY - (int)oPosGps.y;
        //Calcula la coordenada del centro, según la nueva escala
        vnCentroX = (int)((vnCentroX * getNScale()) /
                Math.abs(Double.parseDouble(getOPadre().oConfCampo.getCFactorX())));
        vnCentroY = (int)((vnCentroY * getNScale()) /
                Math.abs(Double.parseDouble(getOPadre().oConfCampo.getCFactorY())));
//((android.widget.TextView)getOPadre().findViewById(R.id.lblCapas)).setText("Llego 6");
        //Sólo dibuja si el valor está dentro de los límites de la imagen
//        if (vnCentroX>=0 && vnCentroY>=0 && vnCentroX<=voImg.getWidth() && vnCentroY<=voImg.getHeight()) {
        Paint voCruz = new Paint();
        voCruz.setColor(0xFFFA853A); //Marca de color naranja
        voCruz.setStrokeWidth(3.0f);
        poG2D.drawLine(vnCentroX-5, vnCentroY, vnCentroX+5, vnCentroY, voCruz);
        poG2D.drawLine(vnCentroX, vnCentroY-5, vnCentroX, vnCentroY+5, voCruz);
//((android.widget.TextView)getOPadre().findViewById(R.id.lblCapas)).setText("Llego 7");
//        }
    }

    /**
     * Método que carga la imagen de fondo y los bocetos
     * @param pcFichero String Fichero que corresponde a la imagen de fondo
     * @param pcBoceto String Fichero que representa a los 3 bocetos
     * @return boolean Devuelve si la carga ha resultado correcta o no.
     */
    public boolean cargarImagenes(String pcFichero, String pcBoceto) {
        boolean vbResul = false;
        try {
            FileInputStream voIn;
            BufferedInputStream voBuf;
            voIn = new FileInputStream(pcFichero);
            voBuf = new BufferedInputStream(voIn);
            voImg = BitmapFactory.decodeStream(voBuf);
            if (voIn != null) {
                voIn.close();
            }
            if (voBuf != null) {
                voBuf.close();
            }

            //Carga los bocetos
            int vnPosGuion = pcBoceto.lastIndexOf('_');
            if (vnPosGuion != -1) {
                String vcComun = pcBoceto.substring(0, vnPosGuion);
                //También extrae la extensión del archivo
                String vcExt = Utilidades.obtenerSufijoFichero(pcBoceto);
                //Carga el boceto 0, imagen de puntos/líneas
                voIn = new FileInputStream(vcComun + "_Puntos." + vcExt);
                voBuf = new BufferedInputStream(voIn);
                //vaBoceto[0] = BitmapFactory.decodeStream(voBuf);
                //Esto crea un bitmap mutable, en el cual se puede escribir
                vaBoceto[0] = BitmapFactory.decodeStream(voBuf).copy(Bitmap.Config.ARGB_8888, true);
                if (voIn != null) {
                    voIn.close();
                }
                if (voBuf != null) {
                    voBuf.close();
                }
                //Carga el boceto 1, imagen de áreas
                voIn = new FileInputStream(vcComun + "_Areas." + vcExt);
                voBuf = new BufferedInputStream(voIn);
                //vaBoceto[1] = BitmapFactory.decodeStream(voBuf);
                //Esto crea un bitmap mutable, en el cual se puede escribir
                vaBoceto[1] = BitmapFactory.decodeStream(voBuf).copy(Bitmap.Config.ARGB_8888, true);
                if (voIn != null) {
                    voIn.close();
                }
                if (voBuf != null) {
                    voBuf.close();
                }
                //Carga el boceto 2, imagen de sucio
                voIn = new FileInputStream(vcComun + "_Sucio." + vcExt);
                voBuf = new BufferedInputStream(voIn);
                //vaBoceto[2] = BitmapFactory.decodeStream(voBuf);
                //Esto crea un bitmap mutable, en el cual se puede escribir
                vaBoceto[2] = BitmapFactory.decodeStream(voBuf).copy(Bitmap.Config.ARGB_8888, true);
                if (voIn != null) {
                    voIn.close();
                }
                if (voBuf != null) {
                    voBuf.close();
                }
                vbResul = true;
            }
            //Establece el tamaño de la imagen
            int vnWidth = (int)(getNScale() * voImg.getWidth());
            int vnHeight = (int)(getNScale() * voImg.getHeight());
            preferredSize.set(vnWidth, vnHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vbResul;
    }

    /**
     * Método que graba las imágenes de los bocetos de nuevo a disco
     * @param pcBoceto String Fichero que representa a los 3 bocetos
     * @return boolean Devuelve si la grabación ha resultado correcta o no.
     */
    public boolean grabarImagenes(String pcBoceto) {
        boolean vbResul = false;
        try {
            int vnPosGuion = pcBoceto.lastIndexOf('_');
            if (vnPosGuion != -1) {
                String vcComun = pcBoceto.substring(0, vnPosGuion);
                //También extrae la extensión del archivo
                String vcExt = Utilidades.obtenerSufijoFichero(pcBoceto);
                if (vaBoceto[0]!=null)
                    Utilidades.crearArchivoBoceto(vcComun + "_Puntos." + vcExt, vaBoceto[0]);
                if (vaBoceto[1]!=null)
                    Utilidades.crearArchivoBoceto(vcComun + "_Areas." + vcExt, vaBoceto[1]);
                if (vaBoceto[2]!=null)
                    Utilidades.crearArchivoBoceto(vcComun + "_Sucio." + vcExt, vaBoceto[2]);
                vbResul = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vbResul;
    }
    /**
     * Devuelve el tamaño actual del panel
     * @return Point
     */
    public Point getPreferredSize() {
        return preferredSize;
    }
    /**
     * Actualiza las dimensiones del panel que contiene la imagen, en función 
     * del zoom aplicado
     */
    private void updatePreferredSize() {
        int w = (int) (voImg.getWidth() * getNScale());
        int h = (int) (voImg.getHeight() * getNScale());
        preferredSize.set(w, h);
        this.invalidate();
    }
    /**
     * Recalcula y escribe en pantalla el valor de una coordenada. 
     * Si el valor del parámetro es verdadero, calcula la coordenada del centro 
     * de la imagen que se está visualizando. Si es falso, calcula
     * la coordenada correspondiente a la posición del cursor.
     * @param pbCentro boolean. Calcular la coordenada del centro o del cursor.
     */
    private void recalcularCoordenadaImagen (boolean pbCentro) {
        try {
            Point voPosicion = null;
            if (!pbCentro)
                //Obtiene la coordenada de la posición del ratón, según escala actual
                voPosicion = new Point((int)(oAnchor.x), (int)(oAnchor.y));
            else
                //Obtiene la coordenada del centro de la imagen en pantalla, según escala actual
                voPosicion = new Point((int)(this.getWidth()/2), (int)(this.getHeight()/2));
            //Recalcula ese mismo punto, pero según el tamaño original de la imagen
            int vnCentroXOrig = (int)((voPosicion.x-oTraslacion.x) / getNScale());
            int vnCentroYOrig = (int)((voPosicion.y-oTraslacion.y) / getNScale());

            voPosicion.set(vnCentroXOrig, vnCentroYOrig);
            //Guarda la posición del centro de pantalla
            this.setOCentro (voPosicion);
            //Ahora, convierte la posición a una coordenada UTM y la escribe en pantalla
            getOPadre().lblEditorCoordX.setText(
                    Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(getOPadre().oConfCampo.getCCX(),
                            getOPadre().oConfCampo.getCFactorX(), (int)voPosicion.x));
            getOPadre().lblEditorCoordY.setText(
                    Utilidades.calcularCoordenadaFinalDesdeInicialMasFactor(getOPadre().oConfCampo.getCCY(),
                            getOPadre().oConfCampo.getCFactorY(), (int)voPosicion.y));
        } catch (Exception e) {
            getOPadre().lblEditorCoordX.setText("Error");
            getOPadre().lblEditorCoordY.setText("Error");
            e.printStackTrace();
        }
    }

    /**
     * Método que dibuja un punto correspondiente a la posición de un registro 
     * de datos vectoriales
     * @param pcCX String. Coordenada UTM (Easting) del punto
     * @param pcCY String. Coordenada UTM (Northing) del punto
     * @param poG2D Canvas. Contexto gráfico
     */
    private void dibujarPuntoVectorial (String pcCX, String pcCY, Canvas poG2D) {
        Point voPunto = Utilidades.calcularCoordenadaDePantallaDesdeLatLon(pcCX, pcCY,
                getOPadre().oConfCampo.getCCX(), getOPadre().oConfCampo.getCCY(),
                getOPadre().oConfCampo.getCFactorX(),
                getOPadre().oConfCampo.getCFactorY(), getNScale());
        //Sólo dibuja si el valor está dentro de los límites de la imagen
        //if (voPunto.x>=0 && voPunto.y>=0 && voPunto.x<=voImg.getWidth() && voPunto.y<=voImg.getHeight()) {
        Paint voCruz = new Paint();
        voCruz.setColor(android.graphics.Color.LTGRAY);
        voCruz.setStrokeWidth(2.0f);
        poG2D.drawLine(voPunto.x-5, voPunto.y, voPunto.x+5, voPunto.y, voCruz);
        poG2D.drawLine(voPunto.x, voPunto.y-5, voPunto.x, voPunto.y+5, voCruz);
        //}
    }
    /**
     * Método que dibuja una línea entre dos puntos correspondientes a un mismo 
     * elemento de los registros de datos vectoriales
     * @param pcCX1 String. Coordenada UTM (Easting) del primer punto
     * @param pcCY1 String. Coordenada UTM (Northing) del primer punto
     * @param pcCX2 String. Coordenada UTM (Easting) del segundo punto
     * @param pcCY2 String. Coordenada UTM (Northing) del segundo punto
     * @param poG2D Canvas. Contexto gráfico
     */
    private void dibujarLineaVectorial (String pcCX1, String pcCY1,
                                        String pcCX2, String pcCY2, Canvas poG2D) {
        Point voPunto1 = Utilidades.calcularCoordenadaDePantallaDesdeLatLon(pcCX1, pcCY1,
                getOPadre().oConfCampo.getCCX(), getOPadre().oConfCampo.getCCY(),
                getOPadre().oConfCampo.getCFactorX(),
                getOPadre().oConfCampo.getCFactorY(), getNScale());
        Point voPunto2 = Utilidades.calcularCoordenadaDePantallaDesdeLatLon(pcCX2, pcCY2,
                getOPadre().oConfCampo.getCCX(), getOPadre().oConfCampo.getCCY(),
                getOPadre().oConfCampo.getCFactorX(),
                getOPadre().oConfCampo.getCFactorY(), getNScale());
        Paint voCruz = new Paint();
        voCruz.setColor(android.graphics.Color.LTGRAY);
        voCruz.setStrokeWidth(1.0f);
        poG2D.drawLine(voPunto1.x, voPunto1.y, voPunto2.x, voPunto2.y, voCruz);
    }
    /**
     * Dibuja en pantalla los datos vectoriales.
     * @param poG2d Canvas. Contexto gráfico.
     */
    private void volcarDatosVectoriales (Canvas poG2d) {
        String vcTipoActual = "";
        String vcIdActual = "";
        String pcPrimeroX = "";
        String pcPrimeroY = "";
        String pcCX1 = "";
        String pcCY1 = "";
        try {
            if (getOPadre().vRegistros!=null) {
                if (getOPadre().vRegistros.size()>0) {
                    //Recorre todos los registros de datos.
                    for (int i=0; i<getOPadre().vRegistros.size(); i++) {
                        //Lee el siguiente registro.
                        Registro voRegistro = (Registro)getOPadre().vRegistros.elementAt(i);
                        //Si se trata de un punto, lo dibuja inmediatamente
                        if (voRegistro.getNTipo()==0) {
                            dibujarPuntoVectorial(voRegistro.getCCX(), voRegistro.getCCY(), poG2d);
                        } else {
                            //Si continúa el mismo objeto, añade una línea entre el punto anterior el actual
                            if (voRegistro.getCID().equals(vcIdActual)) {
                                dibujarLineaVectorial(pcCX1, pcCY1, voRegistro.getCCX(), voRegistro.getCCY(), poG2d);
                                pcCX1 = voRegistro.getCCX();
                                pcCY1 = voRegistro.getCCY();
                            }
                            else {
                                //Si el último objeto fue de tipo área, enlaza la última coordenada con la primera              
                                if (vcTipoActual.equals("2") && i>0)
                                    dibujarLineaVectorial(pcCX1, pcCY1, pcPrimeroX, pcPrimeroY, poG2d);
                                pcPrimeroX = voRegistro.getCCX();
                                pcPrimeroY = voRegistro.getCCY();
                                pcCX1 = voRegistro.getCCX();
                                pcCY1 = voRegistro.getCCY();
                                //Actualiza los valores de Id y tipo actual.
                                vcIdActual = voRegistro.getCID();
                                vcTipoActual = voRegistro.getNTipo()+"";
                            }
                        }
                    }
                    //Si el último objeto fue de tipo área, enlaza la última coordenada con la primera              
                    if (vcTipoActual.equals("2"))
                        dibujarLineaVectorial(pcCX1, pcCY1, pcPrimeroX, pcPrimeroY, poG2d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dada una posición del ratón sobre la imagen en pantalla, este método se encarga
     * de obtener la coordenada UTM que le corresponde y crea un Registro temporal para introducir
     * los datos de esa coordenada, después de haberla convertido en formato 
     * Lat-Lon (en grados:minutos:segundos)
     */
    private void anadirPuntoComoRegistro () {
        try {
            //Crea un nuevo Registro temporal
            Registro voRegistro = new Registro();
            //Calcula la coordenada de la posición del cursor
            recalcularCoordenadaImagen (false);
            //Guarda los datos de la coordenada en un nuevo Registro y lo añade al vector
            String[] vaResul = Utilidades.convertirUTMToLatLong(getOPadre().lblEditorCoordX.getText().toString(),
                    getOPadre().lblEditorCoordY.getText().toString(),
                    getOPadre().oConfCampo.getNZona());
            if (vaResul.length>0) {
                voRegistro.setCCX(vaResul[0]);
                voRegistro.setCCY(vaResul[1]);
                voRegistro.setCFecha(Utilidades.obtenerFechaHoraParaGpx());
            }
            getOPadre().vNuevos.addElement(voRegistro);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que se encarga de grabar los registros temporales en el vector de registros general
     * de la aplicación.
     */
    private void grabarRegistros () {
        try {
            getOPadre().grabarRegistros();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Método que se encarga de mostrar los registros vectoriales y le permite
     * al usuario eliminar uno o varios.
     */
    private void borrarRegistros () {
        try {
            getOPadre().borrarRegistros();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
