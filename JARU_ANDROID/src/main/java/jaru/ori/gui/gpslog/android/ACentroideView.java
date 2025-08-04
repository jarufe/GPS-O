package jaru.ori.gui.gpslog.android;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.graphics.Paint;

import jaru.ori.logic.gpslog.*;
import jaru.ori.utils.*;

/**
 * Panel que se encarga del dibujado de los datos de un centroide.<BR>
 * Se utiliza en conjunto con la actividad ACentroide y se dedica únicamente
 * a la parte gráfica.
 * @author jarufe
 * @version 1.0
 *
 */
public class ACentroideView extends View {
    private TransfGeografica oTransf;
    private Context oContext;
    private String cTexto;

    /**
     * Constructor por defecto de la clase.
     */
    public ACentroideView(Context context) {
        super(context);
        oContext = context;
        oTransf = new TransfGeografica();
        cTexto = "";
    }
    /**
     * Método que establece los datos actuales de puntos.
     * @param poTransf TransfGeografica. Datos de puntos registrados.
     */
    public void setOTransf(TransfGeografica poTransf) {
        oTransf = poTransf;
    }
    /**
     * Método que devuelve el objeto con los datos de puntos registrados.
     * @return TransfGeografica. Datos de puntos registrados.
     */
    public TransfGeografica getOTransf() {
        return oTransf;
    }
    /**
     * Método que establece un texto adicional que se puede visualizar en pantalla.
     * @param pcTexto String. Texto.
     */
    public void setCTexto(String pcTexto) {
        cTexto = pcTexto;
    }
    /**
     * Método que devuelve el valor del texto adicional.
     * @return String. Texto.
     */
    public String getCTexto() {
        return cTexto;
    }

    /**
     * Realiza el repintado gráfico de los puntos recogidos y del centroide calculado.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            Paint voNormal = new Paint();
            voNormal.setColor(0xFFFF0000);
            Paint voExtra = new Paint();
            voExtra.setColor(0xFF000000);
            //Dibuja un fondo blanco en toda la pantalla
            Paint voFondo = new Paint();
            voFondo.setColor(0xFFFFFFFF);
            canvas.drawRect(0, 0, getWidth(), getHeight(), voFondo);

            long nMinX, nMaxX, nMinY, nMaxY, nValX, nValY, nTamX, nTamY;
            Integer nInt = new Integer(0);

            //Obtiene el tamaño del panel
            int width = getWidth();
            int height = getHeight();
            //Obtiene los valores mínimos y máximos de las coordenadas, para poder dibujarlas
            nMinX = oTransf.nMinX;
            nMaxX = oTransf.nMaxX;
            nMinY = oTransf.nMinY;
            nMaxY = oTransf.nMaxY;

            //Reducimos el tamaño de la pantalla para que los puntos aparezcan claramente
            nTamX = width - 30;
            nTamY = height - 30;
            if ((nMaxX-nMinX)!=0 && (nMaxY-nMinY)!=0) {
                //Para cada coordenada, calcula sus coordenadas de pantalla y dibuja un punto
                int i=0;
                int vnLimite = Utilidades.getNLecturasNMEA();
                while (i<vnLimite && oTransf.nDatos[i][0]!=-9999) {
                    nValX = (((oTransf.nDatos[i][0]-nMinX) * nTamX) / (nMaxX - nMinX)) + 15;
                    nValY = (((oTransf.nDatos[i][1]-nMinY) * nTamY) / (nMaxY - nMinY)) + 15;
                    if (oTransf.nSatelites[i]==oTransf.nMaxSatelites || oTransf.nMaxSatelites==0) {
                        canvas.drawRect((int)nValX, (int)nValY, (int)nValX+2, (int)nValY+2, voNormal);
                    }
                    else {
                        canvas.drawRect((int)nValX, (int)nValY, (int)nValX+2, (int)nValY+2, voExtra);
                    }
                    i++;
                }
                //Calcula las coordenadas de pantalla del centroide, y dibuja un punto más grueso
                if ((nMaxX-nMinX)!=0 && (nMaxY-nMinY)!=0) {
                    //Centroide de los puntos con el máximo número de satélites de la muestra
                    nValX = (((oTransf.nCentroGlobal[0][0]-nMinX) * nTamX) / (nMaxX - nMinX)) + 15;
                    nValY = (((oTransf.nCentroGlobal[0][1]-nMinY) * nTamY) / (nMaxY - nMinY)) + 15;
                    canvas.drawRect((int)nValX, (int)nValY, (int)nValX+2, (int)nValY+2, voExtra);
                    //Centroide de todos los puntos
                    nValX = (((oTransf.nCentro[0][0]-nMinX) * nTamX) / (nMaxX - nMinX)) + 15;
                    nValY = (((oTransf.nCentro[0][1]-nMinY) * nTamY) / (nMaxY - nMinY)) + 15;
                    canvas.drawRect((int)nValX, (int)nValY, (int)nValX+4, (int)nValY+4, voNormal);
                }
            }

            //Escribe las coordenadas del centroide en texto
            String r1 = oTransf.transfCoord(oTransf.obtieneCadena(oTransf.nCentro[0][0]))
                    + "; " +
                    oTransf.transfCoord(oTransf.obtieneCadena(oTransf.nCentro[0][1]));
            String r2 = oContext.getString(R.string.ORI_ML00092) + " (" + oTransf.nCont + ")";
            String r3 = oTransf.transfCoord(oTransf.obtieneCadena(oTransf.nCentroGlobal[0][0]))
                    + "; " +
                    oTransf.transfCoord(oTransf.obtieneCadena(oTransf.nCentroGlobal[0][1]));
            Paint voTexto = new Paint();
            voTexto.setColor(0xFF000000);
            voTexto.setTextSize(10);
            canvas.drawText(r2, 5, 10, voTexto);
            voTexto.setColor(0xFFFF0000);
            canvas.drawText(r3, 5, height, voTexto);
            voTexto.setColor(0xFF000000);
            canvas.drawText(r1, width/2, height, voTexto);
            voTexto.setColor(0xFF000000);
            voTexto.setTextSize(12);
            canvas.drawText(cTexto, 5, height-20, voTexto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
