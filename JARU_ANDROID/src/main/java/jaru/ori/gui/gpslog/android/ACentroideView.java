package jaru.ori.gui.gpslog.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.graphics.Paint;

import jaru.ori.logic.gpslog.*;
import jaru.ori.utils.*;
import jaru.ori.utils.android.UtilsAndroid;

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

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            int width = getWidth();
            int height = getHeight();

            // Detectar si es tablet (simple heurística)
            boolean esTablet = UtilsAndroid.esTablet(getContext());

            // Tamaños adaptativos
            float puntoPequeno = Math.max(width, height) * (esTablet ? 0.014f : 0.007f);
            float puntoGrande  = Math.max(width, height) * (esTablet ? 0.021f : 0.014f);
            float margen        = esTablet ? 40f : 30f;
            float textoSize     = Math.min(width, height) * (esTablet ? 0.037f : 0.030f);

            // Pinturas
            Paint voNormal = new Paint();
            voNormal.setColor(0xFFFF0000);
            Paint voExtra = new Paint();
            voExtra.setColor(0xFF000000);
            Paint voFondo = new Paint();
            voFondo.setColor(0xFFFFFFFF);
            canvas.drawRect(0, 0, width, height, voFondo);

            long nMinX = oTransf.nMinX;
            long nMaxX = oTransf.nMaxX;
            long nMinY = oTransf.nMinY;
            long nMaxY = oTransf.nMaxY;
            long nTamX = width - (int)(2 * margen);
            long nTamY = height - (int)(2 * margen);

            if ((nMaxX - nMinX) != 0 && (nMaxY - nMinY) != 0) {
                int vnLimite = Utilidades.getNLecturasNMEA();
                for (int i = 0; i < vnLimite && oTransf.nDatos[i][0] != -9999; i++) {
                    long nValX = (((oTransf.nDatos[i][0] - nMinX) * nTamX) / (nMaxX - nMinX)) + (int)margen;
                    long nValY = (((oTransf.nDatos[i][1] - nMinY) * nTamY) / (nMaxY - nMinY)) + (int)margen;
                    Paint paint = (oTransf.nSatelites[i] == oTransf.nMaxSatelites || oTransf.nMaxSatelites == 0) ? voNormal : voExtra;
                    canvas.drawCircle(nValX, nValY, puntoPequeno, paint);
                }

                // Centroide global
                long nValX = (((oTransf.nCentroGlobal[0][0] - nMinX) * nTamX) / (nMaxX - nMinX)) + (int)margen;
                long nValY = (((oTransf.nCentroGlobal[0][1] - nMinY) * nTamY) / (nMaxY - nMinY)) + (int)margen;
                canvas.drawCircle(nValX, nValY, puntoPequeno, voExtra);

                // Centroide total
                nValX = (((oTransf.nCentro[0][0] - nMinX) * nTamX) / (nMaxX - nMinX)) + (int)margen;
                nValY = (((oTransf.nCentro[0][1] - nMinY) * nTamY) / (nMaxY - nMinY)) + (int)margen;
                canvas.drawCircle(nValX, nValY, puntoGrande, voNormal);
            }

            // Texto
            Paint voTexto = new Paint();
            voTexto.setColor(0xFF000000);
            voTexto.setTextSize(textoSize);

            String r1 = oTransf.transfCoordAGrados(oTransf.obtieneCadena(oTransf.nCentro[0][0])) + "; " +
                    oTransf.transfCoordAGrados(oTransf.obtieneCadena(oTransf.nCentro[0][1]));
            String r2 = oContext.getString(R.string.ORI_ML00092) + " (" + oTransf.nCont + ")";
            String r3 = oTransf.transfCoordAGrados(oTransf.obtieneCadena(oTransf.nCentroGlobal[0][0])) + "; " +
                    oTransf.transfCoordAGrados(oTransf.obtieneCadena(oTransf.nCentroGlobal[0][1]));

            canvas.drawText(r2, margen, textoSize + 5, voTexto);
            voTexto.setColor(0xFFFF0000);
            canvas.drawText(r3, margen, height - textoSize * 2, voTexto);
            voTexto.setColor(0xFF000000);
            canvas.drawText(r1, width / 2f, height - textoSize * 2, voTexto);
            //voTexto.setTextSize(textoSize * 1.2f);
            canvas.drawText(cTexto, margen, height - textoSize, voTexto);

        } catch (Exception e) {
            Log.e("GPS-O", "Error dibujando centroide", e);
        }
    }


}
