package jaru.sensor.gui.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.graphics.Paint;

import jaru.ori.utils.android.UtilsAndroid;
import jaru.sensor.logic.android.*;
import jaru.ori.utils.*;

/**
 * Panel que se encarga del dibujado de los datos de un centroide.<BR>
 * Se utiliza en conjunto con la actividad ACentroide y se dedica Ãºnicamente
 * a la parte grÃ¡fica.
 * @author jarufe
 * @version 1.0
 *
 */
public class ABrujulaView extends View {
    private Context oContext;
    private int nOpcion;
    private double nGrados;
    private double nDesvio;
    private String cTexto;
    private String cSensor;
    private String cVarios;
    private Bitmap oLimbo;
    private Bitmap oAguja;
    private int x0 = 0;
    private int x1 = 0;
    private int y0 = 0;
    private int y1 = 0;
    private int xc = 0;
    private int yc = 0;
    private double nProp = 1.0;

    /**
     * Constructor por defecto de la clase.
     */
    public ABrujulaView(Context context) {
        super(context);
        oContext = context;
        nOpcion = 0;
        nGrados = 0.0;
        cTexto = "";
        cSensor = "";
        cVarios = "";
        oLimbo = null;
        oAguja = null;
    }
    public void setNOpcion(int pnOpcion) {
        nOpcion = pnOpcion;
    }
    public void setNGrados(double pnGrados) {
        nGrados = pnGrados;
    }
    public void setNDesvio(double pnDesvio) {
        nDesvio = pnDesvio;
    }
    public void setCTexto (String pcTexto) {
        cTexto = pcTexto;
    }
    public void setCSensor (String pcSensor) {
        cSensor = pcSensor;
    }
    public void setCVarios (String pcVarios) {
        cVarios = pcVarios;
    }
    public void setOLimbo (Bitmap poValor) {
        oLimbo = poValor;
    }
    public void setOAguja (Bitmap poValor) {
        oAguja = poValor;
    }
    public int getNOpcion() {
        return nOpcion;
    }
    public double getNGrados() {
        return nGrados;
    }
    public double getNDesvio() {
        return nDesvio;
    }
    public String getCTexto() {
        return cTexto;
    }
    public String getCSensor() {
        return cSensor;
    }
    public String getCVarios() {
        return cVarios;
    }
    public Bitmap getOLimbo() {
        return oLimbo;
    }
    public Bitmap getOAguja() {
        return oAguja;
    }

    /**
     * Realiza el repintado grÃ¡fico de los puntos recogidos y del centroide calculado.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            int width = getWidth();
            int height = getHeight();

            // Detectar si es tablet (simple heurística)
            boolean esTablet = UtilsAndroid.esTablet(getContext());

            // Tamaños adaptativos
            float margen        = esTablet ? 40f : 30f;
            float textoSize     = Math.min(width, height) * (esTablet ? 0.050f : 0.040f);
            float textoSize2     = Math.min(width, height) * (esTablet ? 0.120f : 0.100f);

            String vcTexto = Conversor.corregirLectura(nGrados, nDesvio) + "";
            Paint voNormal = new Paint();
            voNormal.setColor(0xFFFF0000);
            Paint voExtra = new Paint();
            voExtra.setColor(0xFF000000);
            //Dibuja un fondo blanco en toda la pantalla
            Paint voFondo = new Paint();
            voFondo.setColor(0xFFFFFFFF);
            canvas.drawRect(0, 0, getWidth(), getHeight(), voFondo);

            //Dibuja el limbo de la brújula usando el 90% de la pantalla
            calcularCoordenadasImagenes(getWidth(), getHeight(), oLimbo.getWidth());
            Rect voOrig = new Rect(0, 0, oLimbo.getWidth(), oLimbo.getHeight());
            Rect voDest = new Rect(x0, y0, x1, y1);
            canvas.drawBitmap(oLimbo, voOrig, voDest, null);
            //Dibuja la aguja rotando para que señale al norte, según lectura y desvío
            double vnRotacion = 360.0 - Conversor.corregirLectura(nGrados, nDesvio);
            if (nOpcion==1) {
                vnRotacion = 0.0;
            }
            Matrix voMatrix = new Matrix();
            voMatrix.postRotate((float)vnRotacion);
            Bitmap voAguja2 = Bitmap.createBitmap(oAguja, 0, 0,
                    oAguja.getWidth(), oAguja.getHeight(), voMatrix, true);
            voOrig = new Rect(0, 0, voAguja2.getWidth(), voAguja2.getHeight());
            voDest = new Rect(xc-((int)(voAguja2.getWidth()*nProp)/2),
                    yc-((int)(voAguja2.getHeight()*nProp)/2),
                    xc+((int)(voAguja2.getWidth()*nProp)/2),
                    yc+((int)(voAguja2.getHeight()*nProp)/2));
            canvas.drawBitmap(voAguja2, voOrig, voDest, null);
            //Dibuja un texto con desvío
            Paint voTexto = new Paint();
            voTexto.setColor(0xFF000000);
            voTexto.setTextSize(textoSize); //Originalmente estaba a 24
            String vcPrimeraLinea = "";
            if (nOpcion==1) {
                vcPrimeraLinea = cTexto + "    ";
            }
            vcPrimeraLinea = vcPrimeraLinea + cSensor;
            //Muestra un texto indicando si se está usando la brújula o el acelerómetro
            canvas.drawText(vcPrimeraLinea, margen, textoSize + 5, voTexto);
            canvas.drawText("Desvio: " + nDesvio, margen, (textoSize * 2) + 5, voTexto);
            canvas.drawText(cVarios, margen, (textoSize * 3) + 5, voTexto);
            //Dibuja un texto en grande con la lectura de grados
            Paint voTexto2 = new Paint();
            voTexto2.setColor(0xFF0000FF);
            voTexto2.setTextSize(textoSize2); //Originalmente estaba a 100
            voTexto2.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);
            Rect voLim = new Rect();
            voTexto2.getTextBounds(vcTexto, 0, vcTexto.length(), voLim);
            int vnCentroX = (getWidth() - voLim.width())/2;
            int vnCentroY = (int)(getHeight()*0.95) + (int)(voLim.height()/2.0);
            canvas.drawText(vcTexto, vnCentroX, vnCentroY, voTexto2);
        } catch (Exception e) {
            Log.e("GPS-O", "Error en onDraw de la vista de la brújula", e);
        }
    }
    private void calcularCoordenadasImagenes (int pnAnchoPant, int pnAltoPant,
                                              int pnAnchoImag) {
        try {
            if (pnAnchoPant>(pnAltoPant*0.9)) {
                xc = ((int)pnAnchoPant/2);
                yc = ((int)(pnAltoPant*0.9)/2);
                x0 = xc - yc;
                y0 = 0;
                x1 = xc + yc;
                y1 = (int)(pnAltoPant*0.9);
                nProp = y1 / pnAnchoImag;
            } else {
                xc = ((int)pnAnchoPant/2);
                yc = ((int)(pnAltoPant*0.9)/2);
                x0 = 0;
                y0 = yc - xc;
                x1 = pnAnchoPant;
                y1 = yc + xc;
                nProp = x1 / pnAnchoImag;
            }
        } catch (Exception e) {
            x0 = 0;
            x1 = 0;
            y0 = 0;
            y1 = 0;
            xc = 0;
            yc = 0;
            nProp = 1.0;
        }
    }


}
