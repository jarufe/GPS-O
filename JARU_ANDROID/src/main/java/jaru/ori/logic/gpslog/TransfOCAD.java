package jaru.ori.logic.gpslog;

import android.content.res.Resources;
import android.app.Application;
import java.util.Vector;
import java.io.*;
import jaru.ori.utils.*;
import jaru.ori.gui.gpslog.android.R;

/**
 * Clase que encapsula la operativa para poder generar un archivo OCAD a partir 
 * de datos registrados.<BR>
 * Se pueden generar ficheros en versión 6 ó 7 de OCAD.<BR> 
 * Se puede establecer la escala del mapa.<BR>
 * También se puede elegir entre un sistema de coordenadas de papel o de mundo real. 
 * En el caso de seleccionar coordenadas de mundo real, sólo se soporta el 
 * sistema UTM y se presume que las coordenadas de GPS están en WGS84.
 * @author jarufe
 */
public class TransfOCAD {
    private static RandomAccessFile oRaf = null;
    private static long nPosIndice = 0;
    private static long nPosObjeto = 0;
    private static long nContObjeto = 0;
    private static int nCoords = 0;
    private static long nTamObjeto = 0;
    private static int nMenX;
    private static int nMenY;
    private static int nMayX;
    private static int nMayY;
    private static double nCXOrig = 0;
    private static double nCYOrig = 0;
    private static int nZonaOrig = 0;

    private static Cabecera oCabecera = null;
    private static BloqueIndice oBloque = null;
    private static Objeto oObjeto = null;
    private static TCord oCoord = null;
    private static Configuracion oConfig = null;

    private static int nEscala = 5000;
    private static int nVersion = 6;
    private static int nCoord = 0;
    private static int nZona = 29;
    private static Vector vRegistros = new Vector();
    private static String cPathAplica = "/mnt/sdcard/JARU/";

    private static Resources oRes = null;
    private static Application oApp = null;


    /**
     * Constructor por defecto de la clase. Inicializa las estructuras de datos que
     * permiten realizar los cálculos y transformaciones.
     *
     */
    public TransfOCAD () {
        nEscala = 5000;
        nVersion = 6;
        nCoord = 0;
        nZona = 29;
    }

    /**
     * Establece el valor del parámetro que define la escala del mapa.
     * @param pnEscala int. Escala del mapa.
     */
    public static void setNEscala (int pnEscala) {
        nEscala = pnEscala;
    }
    /**
     * Devuelve el valor del parámetro que define la escala del mapa.
     * @return int. Escala.
     */
    public static int getNEscala () {
        return nEscala;
    }
    /**
     * Establece el valor del parámetro que define la versión de OCAD escogida.
     * @param pnVersion int. 6 Ã³ 7.
     */
    public static void setNVersion (int pnVersion) {
        nVersion = pnVersion;
    }
    /**
     * Devuelve el valor del parámetro que define la versión de OCAD escogida.
     * @return int. versión de OCAD.
     */
    public static int getNVersion () {
        return nVersion;
    }
    /**
     * Establece el valor del parámetro que define el sistema de coordenadas.
     * 0 = coordenadas de papel; 1 = coordenadas de mundo real (UTM)
     * @param pnCoord int. 0 (papel) ó 1 (mundo real)
     */
    public static void setNCoord (int pnCoord) {
        nCoord = pnCoord;
    }
    /**
     * Devuelve el valor del parámetro que define el sistema de coordenadas.
     * 0 = coordenadas de papel; 1 = coordenadas de mundo real (UTM)
     * @return int. Sistema de coordenadas.
     */
    public static int getNCoord () {
        return nCoord;
    }
    /**
     * Establece el valor que define la zona UTM (si se ha escogido el sistema de
     * coordenadas de mundo real)
     * @param pnZona int. Número de la zona UTM donde se encuentran situadas las coordenadas de los puntos que forman el mapa.
     */
    public static void setNZona (int pnZona) {
        nZona = pnZona;
    }
    /**
     * Devuelve el valor del parámetro que define la zona UTM, para un sistema de
     * coordenadas de mundo real.
     * @return int. Zona UTM.
     */
    public static int getNZona () {
        return nZona;
    }
    /**
     * Método que establece el valor del path donde está instalada la aplicación.
     * @param pcValor String. Path completo.
     */
    public static void setCPathAplica(String pcValor) {
        cPathAplica = pcValor;
    }
    /**
     * Método que devuelve el valor del path donde está instalada la aplicación.
     * @return String. Path completo.
     */
    public static String getCPathAplica() {
        return cPathAplica;
    }
    /**
     * Método que devuelve el objeto que representa a los recursos de la aplicación.
     * @return Resources
     */
    public static Resources getORes() {
        return oRes;
    }
    /**
     * Establece el objeto que representa a los recursos de la aplicación.
     * @param poRes Resources
     */
    public static void setORes (Resources poRes) {
        oRes = poRes;
    }
    /**
     * Método que devuelve el objeto que representa a la aplicación.
     * @return Application
     */
    public static Application getOApp() {
        return oApp;
    }
    /**
     * Establece el objeto que representa a la aplicación.
     * @param poApp Application
     */
    public static void setOApp (Application poApp) {
        oApp = poApp;
    }
    /**
     * Dado un conjunto de datos, genera un archivo OCAD, a partir de una plantilla,
     * con la representación en ese tipo de archivo de tales conjuntos de datos.
     * @param pcPath String. Path donde se almacena el archivo resultante.
     * @param pvRegistros Vector. Conjunto de objetos de la clase Registro.
     * @return boolean. Indica si el proceso ha terminado correctamente.
     */
    public static boolean generarFicheroOCAD (String pcPath, Vector pvRegistros) {
        boolean vbResul = true;
        String vcNombre = "";
        InputStream voOrigen = null;
        try {
            nZonaOrig = 0;
            nCXOrig = 0;
            nCYOrig = 0;
            vRegistros = pvRegistros;
            if (nVersion==6) {
                vcNombre = oApp.getString(R.string.ORI_ML00115);
                voOrigen = oRes.openRawResource(R.raw.plantillav6);
            } else if (nVersion==7) {
                vcNombre = oApp.getString(R.string.ORI_ML00103);
                voOrigen = oRes.openRawResource(R.raw.plantillav7);
            }
            String vcFicheroDest = pcPath + generarNombreFichero();
            if (Utilidades.copiarFichero (voOrigen, vcFicheroDest)) {
                abrirFichero(vcFicheroDest);
                exportarDatos();
                oRaf.close();
            }
            else
                vbResul = false;
        } catch (Exception e) {
            e.printStackTrace();
            vbResul = false;
        }
        return vbResul;
    }
    /**
     * El nombre del fichero resultante se establece de forma automática.
     * Se identifica por la fecha y hora de creación más un sufijo predeterminado.
     * @return String. Nombre del fichero de destino.
     */
    private static String generarNombreFichero () {
        String vcResul = oApp.getString(R.string.ORI_ML00102);
        try {
            java.util.Date vdActual = Utilidades.getCurDate();
            String vcFecha = Utilidades.format(vdActual, oApp.getString(R.string.ORI_ML00016));
            vcResul = vcFecha + "_" + vcResul;
        } catch (Exception e) {
        }
        return vcResul;
    }
    /**
     * Apertura del fichero de destino, donde ya se empiezan a modificar algunos valores.
     * @param pcFicheroDest String. Fichero que se ha de abrir para modificar sus valores.
     * @return boolean. Indica si el proceso ha terminado correctamente.
     */
    private static boolean abrirFichero (String pcFicheroDest) {
        boolean vbResul = true;
        try {
            File voFile = new File(pcFicheroDest);
            oRaf = new RandomAccessFile(voFile, "rw");
            long vnTam = oRaf.length();
            oRaf.seek(0);
            oCabecera = new Cabecera();
            oCabecera.leerRegistro();
            oCabecera.FirstIdxBlk = (int)vnTam;
            oRaf.seek(0);
            oCabecera.escribirRegistro();
            //Modifica los datos de Escalas
            oRaf.seek(oCabecera.SetupPos);
            oConfig = new Configuracion();
            oConfig.leerRegistro();
            oConfig.nEscala = nEscala;
            oConfig.nEscalaDX = nEscala;
            oConfig.nEscalaDY = nEscala;
            if (nCoord==1)
                establecerOrigenDeCoordenadas ();
            //Si el sistema de coordenadas es de papel, el valor es (0,0)
            //Si es de mundo real, nCXOrig y nCYOrig contienen las coordenadas del primer punto
            oConfig.RealWorldOffX = nCXOrig;
            oConfig.RealWorldOffY = nCYOrig;
            oRaf.seek(oCabecera.SetupPos);
            oConfig.escribirRegistro();
        } catch (Exception e) {
            vbResul = false;
            e.printStackTrace();
        }
        return vbResul;
    }
    /**
     * Método que se llama para establecer el origen de coordenadas del mapa
     * Sólo se ejecuta si el sistema de coordenadas escogido es de mundo real.
     * En caso de escoger coordenadas de papel, el origen permanece como (0,0)
     */
    private static void establecerOrigenDeCoordenadas() {
        try {
            if (vRegistros!=null) {
                if (vRegistros.size()>0) {
                    //Lee el primer registro.
                    Registro voRegistro = (Registro)vRegistros.elementAt(0);
                    //Se calculan las coordenadas como si se fuera a insertar el punto,
                    //pero pone el parámetro grabar a falso
                    //Al ser el primer elemento, lo único que hace es establecer el
                    //origen de coordenadas
                    insertarPunto(voRegistro.getCCX(), voRegistro.getCCY(), false);
                }
            }
        } catch (Exception e) {
        }
    }
    /**
     * Procedimiento que aglutina las acciones para exportar los datos registrados
     * en el programa.
     * @return boolean. Indica si el proceso ha terminado correctamente.
     */
    private static boolean exportarDatos () {
        boolean vbResul = true;
        String vcTipoOCADActual = "";
        String vcTipoActual = "";
        String vcIdActual = "";
        //Variables para repetir un bloque de datos cuando se utiliza un valor dual de tipo OBM
        //Se genera el objeto principal y luego se repite para generar el objeto dual
        boolean vbRepetirDual = false;
        int vnInicio = 0;
        int vnFinal = 0;
        try {
            iniciarBloqueIndice();
            if (vRegistros!=null) {
                if (vRegistros.size()>0) {
                    iniciarObjeto();
                    //Recorre todos los registros de datos.
                    for (int i=0; i<vRegistros.size(); i++) {
                        //Lee el siguiente registro.
                        Registro voRegistro = (Registro)vRegistros.elementAt(i);
                        if (voRegistro.getCID().equals(vcIdActual)) {
                            //Para un punto cualquiera que corresponda al mismo Id
                            //que su anterior, inserta el punto
                            insertarPunto(voRegistro.getCCX(), voRegistro.getCCY(), true);
                        }
                        else {
                            //El primer elemento no tiene que insertarse como objeto todavía.
                            //Esto es sólo para los posteriores.
                            if (i>0) {
                                //Inserta el objeto e inicia uno nuevo.
                                insertarObjeto(vcTipoOCADActual, vcTipoActual);
                                //Al acabar el objeto, si era uno dual para crear también como OBM, lo genera
                                if (vbRepetirDual) {
                                    vnFinal = i;
                                    insertarBloqueDual (vnInicio, vnFinal);
                                    vbRepetirDual = false;
                                    vnInicio = 0;
                                    vnFinal = 0;
                                }
                                //Luego inicia el siguiente objeto que va a ser tratado
                                iniciarObjeto();
                            }
                            //Inserta el punto
                            insertarPunto(voRegistro.getCCX(), voRegistro.getCCY(), true);
                            //Actualiza los valores de Id y tipo actual.
                            vcIdActual = voRegistro.getCID();
                            vcTipoOCADActual = voRegistro.getCTipoOCAD();
                            vcTipoActual = voRegistro.getNTipo()+"";
                            //Valores para repetir el bloque si se trata de un registro dual
                            if (!voRegistro.getCTipoOBM().equals("") && voRegistro.getNTipo()==1) {
                                vbRepetirDual = true;
                                vnInicio = i;
                                vnFinal = 0;
                            } else {
                                vbRepetirDual = false;
                                vnInicio = 0;
                                vnFinal = 0;
                            }
                        }
                    }
                    //Inserta el Ãºltimo objeto que fue tratado.
                    insertarObjeto(vcTipoOCADActual, vcTipoActual);
                    //Al acabar el objeto, si era uno dual para crear también como OBM, lo genera
                    if (vbRepetirDual) {
                        vnFinal = vRegistros.size();
                        insertarBloqueDual (vnInicio, vnFinal);
                        vbRepetirDual = false;
                        vnInicio = 0;
                        vnFinal = 0;
                    }
                    //Graba el bloque
                    oRaf.seek(nPosIndice);
                    oBloque.escribirRegistro();
                }
            }
        } catch (Exception e) {
            vbResul = false;
            e.printStackTrace();
        }
        return vbResul;
    }
    /**
     * Procedimiento que repite la generación de un objeto para cuando se trata de uno dual OBM.
     * @param pnInicio int. Número de elemento de origen en el vector de registros
     * @param pnFinal int. Número de elemento final en el vector de registros
     * @return boolean. Indica si el proceso ha terminado correctamente.
     */
    private static boolean insertarBloqueDual (int pnInicio, int pnFinal) {
        boolean vbResul = true;
        String vcTipoOBM = "";
        String vcTipo = "1";
        int i = 0;
        try {
            if (pnFinal>pnInicio && pnFinal>0) {
                iniciarObjeto();
                //Recorre todos los registros de datos del bloque dual.
                i = pnInicio;
                while (i<pnFinal) {
                    //Lee el siguiente registro.
                    Registro voRegistro = (Registro)vRegistros.elementAt(i);
                    if (i==pnInicio) {
                        vcTipoOBM = voRegistro.getCTipoOBM();
                    }
                    insertarPunto(voRegistro.getCCX(), voRegistro.getCCY(), true);
                    i++;
                }
                insertarObjeto(vcTipoOBM, vcTipo);
            }
        } catch (Exception e) {
            vbResul = false;
            e.printStackTrace();
        }
        return vbResul;
    }
    /**
     * Comienza un nuevo bloque índice
     */
    private static void iniciarBloqueIndice () {
        try {
            oBloque = new BloqueIndice();
            nPosIndice = oRaf.length();
            oBloque.nPosSig = 0;
            for (int i=0; i<256; i++) {
                oBloque.aIndex[i] = new Indice();
                oBloque.aIndex[i].nLen = 0;
                oBloque.aIndex[i].nPos = 0;
                oBloque.aIndex[i].nSym = 0;
                oBloque.aIndex[i].nCoord1.nX = 0;
                oBloque.aIndex[i].nCoord1.nY = 0;
                oBloque.aIndex[i].nCoord2.nX = 0;
                oBloque.aIndex[i].nCoord2.nY = 0;
            }
            oRaf.seek(nPosIndice);
            oBloque.escribirRegistro();
            nPosObjeto = oRaf.length();
            nContObjeto = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Comienza un nuevo objeto
     */
    private static void iniciarObjeto () {
        try {
            oObjeto = new Objeto();
            oObjeto.nSym = 0;
            oObjeto.nOtp = 1;
            oObjeto.nUnicode = 0;
            oObjeto.nItem = 1;
            oObjeto.nText = 0;
            oObjeto.nAng = 0;
            oObjeto.nRes1 = 0;
            oObjeto.nRes2 = 0;

            oCoord = new TCord();
            oCoord.nX = 0;
            oCoord.nY = 0;

            nCoords = 0;

            oRaf.seek(nPosObjeto);
            oObjeto.escribirRegistro();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Realiza recálculo de los extremos del mapa
     */
    private static void calcularExtremos () {
        try {
            if (nCoords == 1) {
                nMenX = oCoord.nX;
                nMenY = oCoord.nY;
                nMayX = oCoord.nX;
                nMayY = oCoord.nY;
            }
            else {
                if (oCoord.nX < nMenX)
                    nMenX = oCoord.nX;
                if (oCoord.nX > nMayX)
                    nMayX = oCoord.nX;
                if (oCoord.nY < nMenY)
                    nMenY = oCoord.nY;
                if (oCoord.nY > nMayY)
                    nMayY = oCoord.nY;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Inserta un nuevo objeto
     */
    private static void insertarObjeto (String pcSub, String pcTip) {
        try {
            oObjeto.nItem = (short)nCoords;
            oObjeto.nAng = 0;
            oObjeto.nOtp = (byte)(Integer.parseInt(pcTip) + 1);
            oObjeto.nUnicode = 0;
            oObjeto.nSym = (short)(Double.parseDouble(pcSub) * 10);
            oBloque.aIndex[(int)nContObjeto].nCoord1.nX = (int)nMenX;
            oBloque.aIndex[(int)nContObjeto].nCoord1.nY = (int)nMenY;
            oBloque.aIndex[(int)nContObjeto].nCoord2.nX = (int)nMayX;
            oBloque.aIndex[(int)nContObjeto].nCoord2.nY = (int)nMayY;
            oBloque.aIndex[(int)nContObjeto].nLen = (short)(32 + (8 * nCoords));  //32=>Tamaño en bytes de los datos de la clase Objeto
            oBloque.aIndex[(int)nContObjeto].nPos = (int)nPosObjeto;
            oBloque.aIndex[(int)nContObjeto].nSym = (short)(Double.parseDouble(pcSub) * 10);
            oRaf.seek(nPosObjeto);
            oObjeto.escribirRegistro();
            nContObjeto = nContObjeto + 1;
            if (nContObjeto > 255) {
                nContObjeto = 0;
                oBloque.nPosSig = (int)oRaf.length();
                oRaf.seek(nPosIndice);
                oBloque.escribirRegistro();
                iniciarBloqueIndice();
            }
            nPosObjeto = oRaf.length();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Inserta un nuevo punto
     */
    private static void insertarPunto (String pcCX, String pcCY, boolean pbGrabar) {
        try {
            int vnZona = 0;
            double nX = 0;
            double nY = 0;
            double nDesplzX = 0;
            double nDesplzY = 0;
            double nDesplzXReal = 0;
            double nDesplzYReal = 0;

            nCoords = nCoords + 1;
            double[] vaResul = Utilidades.convertirLatLongToUTM(pcCX, pcCY);
            vnZona = (int)vaResul[0];
            nX = vaResul[1];
            nY = vaResul[2];
            //Asignaciones que sólo se van a producir durante el procesamiento del primer registro
            //Se inicia el origen de coordenadas y la zona UTM
            if (nZonaOrig == 0) {
                nZonaOrig = vnZona;
                //Si se ha escogido coordenada de papel, la zona por defecto es la misma que la del primer punto
                if (nCoord == 0)
                    nZona = nZonaOrig;
            }
            if (nCXOrig == 0)
                nCXOrig = nX;
            if (nCYOrig == 0)
                nCYOrig = nY;
            //Si la zona UTM de destino es distinta que la del punto actual, se transforma la coordenada entre zonas UTM
            if (vnZona!=nZona) {
                //Llama al método de conversión de coordenadas, pero con los parámetros que le indican que
                //hay que forzar la conversión para referir los valores a una zona en concreto.
                vaResul = Utilidades.convertirLatLongToUTM(pcCX, pcCY, true, nZona);
                vnZona = (int)vaResul[0];
                nX = vaResul[1];
                nY = vaResul[2];
            }
            //Calcula la posición desplazada del punto actual, en función del origen (primer punto) y de la escala
            nDesplzX = (nX - nCXOrig);
            nDesplzY = (nY - nCYOrig);

            nDesplzXReal = (((nDesplzX * 1000) / (nEscala * 1.0)) * 100);
            nDesplzYReal = (((nDesplzY * 1000) / (nEscala * 1.0)) * 100);

            nDesplzXReal = nDesplzXReal * 256;
            nDesplzYReal = nDesplzYReal * 256;
            System.out.println (pcCX + " = " + nX + " => " + (int)nDesplzXReal);
            System.out.println (pcCY + " = " + nY + " => " + (int)nDesplzYReal);
            System.out.println ("");
            //Se graba en fichero cuando se trata del procesamiento normal de los registros
            //En cambio, no se graba cuando se trata de procesar el primer punto para establecer el origen de coordenadas
            if (pbGrabar) {
                if (oCoord==null)
                    oCoord = new TCord();
                oCoord.nX = (int)nDesplzXReal;
                oCoord.nY = (int)nDesplzYReal;

                oCoord.escribirRegistro();

                calcularExtremos();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private static byte readByte() {
        byte vnResul = 0;
        try {
            vnResul = oRaf.readByte();
        } catch (Exception e) {
        }
        return vnResul;
    }
    private static short readShort() {
        short vnResul = 0;
        try {
            byte vnByte1 = oRaf.readByte();
            byte vnByte2 = oRaf.readByte();
            vnResul = (short)(((vnByte2&0xff)<<8) | (vnByte1 & 0xff));
        } catch (Exception e) {
        }
        return vnResul;
    }
    private static int readInt() {
        int vnResul = 0;
        try {
            byte vnByte1 = oRaf.readByte();
            byte vnByte2 = oRaf.readByte();
            byte vnByte3 = oRaf.readByte();
            byte vnByte4 = oRaf.readByte();
            vnResul = (int)(((vnByte4&0xff)<<24) |((vnByte3&0xff)<<16) | ((vnByte2&0xff)<<8) | (vnByte1 & 0xff));
        } catch (Exception e) {
        }
        return vnResul;
    }
    private static long readLong() {
        long vnResul = 0;
        try {
            for ( int shiftBy=0; shiftBy<64; shiftBy+=8 ) {
                vnResul |= (long)( oRaf.readByte() & 0xff ) << shiftBy;
            }
        } catch (Exception e) {
        }
        return vnResul;
    }
    private static double readDouble() {
        double vnResul = 0;
        try {
            long accum = 0;
            for ( int shiftBy=0; shiftBy<64; shiftBy+=8 ) {
                accum |= ( (long)( oRaf.readByte() & 0xff ) ) << shiftBy;
            }
            vnResul = Double.longBitsToDouble( accum );
        } catch (Exception e) {
        }
        return vnResul;
    }
    private static void writeByte(byte pnValor) {
        try {
            oRaf.writeByte(pnValor);
        } catch (Exception e) {
        }
    }
    private static void writeShort(short pnValor) {
        try {
            byte vnByte1 = (byte)((pnValor & 0x00ff));
            byte vnByte2 = (byte)((pnValor & 0xff00)>>8);
            oRaf.writeByte(vnByte1);
            oRaf.writeByte(vnByte2);
        } catch (Exception e) {
        }
    }
    private static void writeInt(int pnValor) {
        try {
            byte vnByte1 = (byte)((pnValor & 0x000000ff));
            byte vnByte2 = (byte)((pnValor & 0x0000ff00)>>8);
            byte vnByte3 = (byte)((pnValor & 0x00ff0000)>>16);
            byte vnByte4 = (byte)((pnValor & 0xff000000)>>24);
            oRaf.writeByte(vnByte1);
            oRaf.writeByte(vnByte2);
            oRaf.writeByte(vnByte3);
            oRaf.writeByte(vnByte4);
        } catch (Exception e) {
        }
    }
    private static void writeIntCoordenada(int pnValor) {
        try {
            byte vnByte1 = (byte)((pnValor & 0x000000ff));
            byte vnByte2 = (byte)((pnValor & 0x0000ff00)>>8);
            byte vnByte3 = (byte)((pnValor & 0x00ff0000)>>16);
            byte vnByte4 = (byte)((pnValor & 0xff000000)>>24);
            oRaf.writeByte(0);
            oRaf.writeByte(vnByte2);
            oRaf.writeByte(vnByte3);
            oRaf.writeByte(vnByte4);
        } catch (Exception e) {
        }
    }
    private static void writeDouble(double pnValor) {
        try {
            //oRaf.writeDouble(pnValor);
            long vnValor = Double.doubleToLongBits(pnValor);
            byte vnByte1 = (byte)((vnValor & 0x00000000000000ff));
            byte vnByte2 = (byte)((vnValor & 0x000000000000ff00)>>8);
            byte vnByte3 = (byte)((vnValor & 0x0000000000ff0000)>>16);
            byte vnByte4 = (byte)((vnValor & 0x00000000ff000000)>>24);
            byte vnByte5 = (byte)((vnValor >> 32) & 0x000000ff);
            byte vnByte6 = (byte)((vnValor >> 40) & 0x000000ff);
            byte vnByte7 = (byte)((vnValor >> 48) & 0x000000ff);
            byte vnByte8 = (byte)((vnValor >> 56) & 0x000000ff);
            oRaf.writeByte(vnByte1);
            oRaf.writeByte(vnByte2);
            oRaf.writeByte(vnByte3);
            oRaf.writeByte(vnByte4);
            oRaf.writeByte(vnByte5);
            oRaf.writeByte(vnByte6);
            oRaf.writeByte(vnByte7);
            oRaf.writeByte(vnByte8);
        } catch (Exception e) {
        }
    }

    public static class Cabecera {
        short OCADMark;    //{3245 (hex 0cad)}
        short SectionMark; //{OCAD 6: 0
        //' OCAD 7: 7
        //' OCAD 8: 2 for normal files
        //'         3 for course setting files}
        short Version;     //'{6 for OCAD 6, 7 for OCAD 7, 8 for OCAD 8}
        short Subversion;  //'{number of subversion (0 for 6.00, 1 for 6.01 etc.)}
        int FirstSymBlk;    //'{file position of the first symbol block}
        int FirstIdxBlk;    //'{file position of the first index block}
        int SetupPos;       //'{file position of the setup record }
        int SetupSize;      //'{size (in bytes) of the setup record}
        int InfoPos;        //'{file position of the file information.
        int InfoSize;       //'{size (in bytes) of the file information}
        int FirstShIndexBlk; //'{OCAD 8 only. This description does not yet exist}
        int Reserved2;
        int Reserved3;
        int Reserved4;
        public Cabecera () {
        }
        public boolean leerRegistro () {
            boolean vbResul = true;
            try {
                OCADMark = TransfOCAD.readShort();
                SectionMark = TransfOCAD.readShort();
                Version = TransfOCAD.readShort();
                Subversion = TransfOCAD.readShort();
                FirstSymBlk = TransfOCAD.readInt();
                FirstIdxBlk = TransfOCAD.readInt();
                SetupPos = TransfOCAD.readInt();
                SetupSize = TransfOCAD.readInt();
                InfoPos = TransfOCAD.readInt();
                InfoSize = TransfOCAD.readInt();
                FirstShIndexBlk = TransfOCAD.readInt();
                Reserved2 = TransfOCAD.readInt();
                Reserved3 = TransfOCAD.readInt();
                Reserved4 = TransfOCAD.readInt();
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
        public boolean escribirRegistro () {
            boolean vbResul = true;
            try {
                TransfOCAD.writeShort(OCADMark);
                TransfOCAD.writeShort(SectionMark);
                TransfOCAD.writeShort(Version);
                TransfOCAD.writeShort(Subversion);
                TransfOCAD.writeInt(FirstSymBlk);
                TransfOCAD.writeInt(FirstIdxBlk);
                TransfOCAD.writeInt(SetupPos);
                TransfOCAD.writeInt(SetupSize);
                TransfOCAD.writeInt(InfoPos);
                TransfOCAD.writeInt(InfoSize);
                TransfOCAD.writeInt(FirstShIndexBlk);
                TransfOCAD.writeInt(Reserved2);
                TransfOCAD.writeInt(Reserved3);
                TransfOCAD.writeInt(Reserved4);
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }

    }
    public static class TCord {
        int nX;
        int nY;
        public TCord () {
        }
        public boolean leerRegistro () {
            boolean vbResul = true;
            try {
                nX = TransfOCAD.readInt();
                nY = TransfOCAD.readInt();
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
        public boolean escribirRegistro () {
            boolean vbResul = true;
            try {
                TransfOCAD.writeIntCoordenada(nX);
                TransfOCAD.writeIntCoordenada(nY);
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
    }
    public static class Indice {
        TCord nCoord1;
        TCord nCoord2;
        int nPos;
        short nLen;
        short nSym;
        public Indice () {
            nCoord1 = new TCord();
            nCoord2 = new TCord();
        }
        public boolean leerRegistro () {
            boolean vbResul = true;
            try {
                nCoord1 = new TCord();
                nCoord1.leerRegistro();
                nCoord1 = new TCord();
                nCoord2.leerRegistro();
                nPos = TransfOCAD.readInt();
                nLen = TransfOCAD.readShort();
                nSym = TransfOCAD.readShort();
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
        public boolean escribirRegistro () {
            boolean vbResul = true;
            try {
                nCoord1.escribirRegistro();
                nCoord2.escribirRegistro();
                TransfOCAD.writeInt(nPos);
                TransfOCAD.writeShort(nLen);
                TransfOCAD.writeShort(nSym);
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
    }
    public static class BloqueIndice {
        int nPosSig;
        Indice[] aIndex;
        public BloqueIndice () {
            aIndex = new Indice[256];
        }
        public boolean leerRegistro () {
            boolean vbResul = true;
            try {
                nPosSig = TransfOCAD.readInt();
                for (int i=0; i<256; i++) {
                    aIndex[i] = new Indice();
                    aIndex[i].leerRegistro();
                }
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
        public boolean escribirRegistro () {
            boolean vbResul = true;
            try {
                TransfOCAD.writeInt(nPosSig);
                for (int i=0; i<256; i++) {
                    aIndex[i].escribirRegistro();
                }
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
    }
    public static class Objeto {
        short nSym;
        byte nOtp;
        byte nUnicode;
        short nItem;
        short nText;
        short nAng;
        short nRes1;
        int nRes2;
        byte[] nRes3;
        public Objeto () {
            nRes3 = new byte[16];
        }
        public boolean leerRegistro () {
            boolean vbResul = true;
            try {
                nSym = TransfOCAD.readShort();
                nOtp = TransfOCAD.readByte();
                nUnicode = TransfOCAD.readByte();
                nItem = TransfOCAD.readShort();
                nText = TransfOCAD.readShort();
                nAng = TransfOCAD.readShort();
                nRes1 = TransfOCAD.readShort();
                nRes2 = TransfOCAD.readInt();
                for (int i=0; i<16; i++) {
                    nRes3[i] = oRaf.readByte();
                }
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
        public boolean escribirRegistro () {
            boolean vbResul = true;
            try {
                TransfOCAD.writeShort(nSym);
                TransfOCAD.writeByte(nOtp);
                TransfOCAD.writeByte(nUnicode);
                TransfOCAD.writeShort(nItem);
                TransfOCAD.writeShort(nText);
                TransfOCAD.writeShort(nAng);
                TransfOCAD.writeShort(nRes1);
                TransfOCAD.writeInt(nRes2);
                for (int i=0; i<16; i++) {
                    oRaf.writeByte(nRes3[i]);
                }
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
    }
    public static class GPSAdjust {
        TCord Coord;
        double nX;
        double nY;
        byte[] cDesc;
        public GPSAdjust () {
            cDesc = new byte[16];
        }
        public boolean leerRegistro () {
            boolean vbResul = true;
            try {
                Coord = new TCord();
                Coord.leerRegistro();
                nX = TransfOCAD.readDouble();
                nY = TransfOCAD.readDouble();
                for (int i=0; i<16; i++) {
                    cDesc[i] = oRaf.readByte();
                }
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
        public boolean escribirRegistro () {
            boolean vbResul = true;
            try {
                Coord.escribirRegistro();
                TransfOCAD.writeDouble(nX);
                TransfOCAD.writeDouble(nY);
                for (int i=0; i<16; i++) {
                    oRaf.writeByte(cDesc[i]);
                }
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
    }
    public static class Configuracion {
        TCord Offset;
        double GridDist;
        short WorkMode;
        short LineMode;
        short EditMode;
        short ActSym;
        double nEscala;
        double RealWorldOffX;
        double RealWorldOffY;
        double RealWorldAngle;
        double RealWorldGrid;
        double GPSAngle;
        GPSAdjust[] oGPSAdjust;
        int nGPSAdjust;
        double nEscalaDX;
        double nEscalaDY;
        public Configuracion () {
            Offset = new TCord();
            oGPSAdjust = new GPSAdjust[12];
        }
        public boolean leerRegistro () {
            boolean vbResul = true;
            try {
                Offset = new TCord();
                Offset.leerRegistro();
                GridDist = TransfOCAD.readDouble();
                WorkMode = TransfOCAD.readShort();
                LineMode = TransfOCAD.readShort();
                EditMode = TransfOCAD.readShort();
                ActSym = TransfOCAD.readShort();
                nEscala = TransfOCAD.readDouble();
                RealWorldOffX = TransfOCAD.readDouble();
                RealWorldOffY = TransfOCAD.readDouble();
                RealWorldAngle = TransfOCAD.readDouble();
                RealWorldGrid = TransfOCAD.readDouble();
                GPSAngle = TransfOCAD.readDouble();
                for (int i=0; i<12; i++) {
                    oGPSAdjust[i] = new GPSAdjust();
                    oGPSAdjust[i].leerRegistro();
                }
                nGPSAdjust = TransfOCAD.readInt();
                nEscalaDX = TransfOCAD.readDouble();
                nEscalaDY = TransfOCAD.readDouble();
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
        public boolean escribirRegistro () {
            boolean vbResul = true;
            try {
                Offset.escribirRegistro();
                TransfOCAD.writeDouble(GridDist);
                TransfOCAD.writeShort(WorkMode);
                TransfOCAD.writeShort(LineMode);
                TransfOCAD.writeShort(EditMode);
                TransfOCAD.writeShort(ActSym);
                TransfOCAD.writeDouble(nEscala);
                TransfOCAD.writeDouble(RealWorldOffX);
                TransfOCAD.writeDouble(RealWorldOffY);
                TransfOCAD.writeDouble(RealWorldAngle);
                TransfOCAD.writeDouble(RealWorldGrid);
                TransfOCAD.writeDouble(GPSAngle);
                for (int i=0; i<12; i++) {
                    oGPSAdjust[i].escribirRegistro();
                }
                TransfOCAD.writeInt(nGPSAdjust);
                TransfOCAD.writeDouble(nEscalaDX);
                TransfOCAD.writeDouble(nEscalaDY);
            } catch (Exception e) {
                vbResul = false;
            }
            return vbResul;
        }
    }


}

