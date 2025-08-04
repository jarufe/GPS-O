package jaru.red.logic;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Clase que implementa métodos para gestionar la realización de comunicaciones.
 * Permite establecer los parámetros de conexión a un Servlet (servidor, puerto, nombre del servlet)
 * Contiene un método para realizar una conexión y enviarle una orden con unos datos asociados
 * @author javier.arufe
 */
public class GestionTransmisiones {
    private static String cServidor = "";
    private static int nPuerto = -1;
    private static String cServlet = "";

    public static String getcServidor() {
        return cServidor;
    }

    public static void setcServidor(String cServidor) {
        GestionTransmisiones.cServidor = cServidor;
    }

    public static int getnPuerto() {
        return nPuerto;
    }

    public static void setnPuerto(int nPuerto) {
        GestionTransmisiones.nPuerto = nPuerto;
    }

    public static String getcServlet() {
        return cServlet;
    }

    public static void setcServlet(String cServlet) {
        GestionTransmisiones.cServlet = cServlet;
    }

    /**
     * Método que conecta con el servidor web y le envía una orden acompañada de unos datos,
     * todo ello dentro de un vector
     * @param poEnvio UploadRequestResponse. Orden + datos a transmitir
     * @return UploadRequestResponse Un objeto cuyos datos son una ristra de cadenas con la respuesta del servidor
     */
    public static UploadRequestResponse transmitirOrden (UploadRequestResponse poEnvio) {
        UploadRequestResponse voRespuesta = null;
        HttpURLConnection voConHttp = null;
        HttpsURLConnection voConHttps = null;
        boolean vbSecure = true;
        try {
            if (poEnvio!=null) {
                //Convierte el objeto a transmitir en una cadena en formato JSON con Jackson
                ObjectMapper voMapper = new ObjectMapper();
                //voMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
                String vcJson = voMapper.writerWithDefaultPrettyPrinter().writeValueAsString(poEnvio);
                //Compone la cadena del servlet de destino y envía el objeto
                //Si el servidor termina con /, se lo quita. Si el servlet empieza con /, se lo quita
                if (cServidor.endsWith("/"))
                    cServidor = cServidor.substring(0, cServidor.length()-1);
                if (cServlet.startsWith("/"))
                    cServlet = cServlet.substring(1);
                String vcUri = "https://" + cServidor + ":" +
                        nPuerto + "/" + cServlet;
                if (nPuerto == 80 || nPuerto == 8080 || nPuerto == 8081) {
                    vcUri = "http://" + cServidor + "/" + cServlet;
                    vbSecure = false;
                }
                //Crea la conexión, con un objeto distinto según sea HTTP o HTTPS
                //y obtiene un canal de salida
                java.net.URL voUrl = new java.net.URL(vcUri);
                OutputStream voOut = null;
                if (vbSecure) {
                    voConHttps = (HttpsURLConnection) voUrl.openConnection();
                    voConHttps.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    voConHttps.setRequestProperty("Accept", "application/json");
                    voConHttps.setRequestMethod("POST");
                    //voConHttps.setRequestProperty("User-Agent", "Mozilla/5.0");
                    setJellyBeanAuth(voConHttps);
                    voConHttps.setDoInput(true);
                    voConHttps.setDoOutput(true);
                    voOut = voConHttps.getOutputStream();
                } else {
                    voConHttp = (HttpURLConnection) voUrl.openConnection();
                    voConHttp.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    voConHttp.setRequestProperty("Accept", "application/json");
                    voConHttp.setRequestMethod("POST");
                    //voConHttp.setRequestProperty("User-Agent", "Mozilla/5.0");
                    setJellyBeanAuth(voConHttp);
                    voConHttp.setDoInput(true);
                    voConHttp.setDoOutput(true);
                    voOut = voConHttp.getOutputStream();
                }
                voOut.write(vcJson.getBytes("UTF-8"));
                voOut.flush();
                voOut.close();
                int vnRespCode = (vbSecure?voConHttps.getResponseCode():voConHttp.getResponseCode());
                if (vnRespCode == HttpURLConnection.HTTP_OK) { //success
                    BufferedReader voIn = new BufferedReader(new InputStreamReader((vbSecure?voConHttps.getInputStream():voConHttp.getInputStream()), "UTF-8"));
                    String vcLinea;
                    StringBuffer vcRespuesta = new StringBuffer();
                    while ((vcLinea = voIn.readLine()) != null) {
                        vcRespuesta.append(vcLinea);
                    }
                    voIn.close();
                    //Ahora convierte la cadena JSON de entrada en un objeto de la clase para la respuesta
                    vcJson = vcRespuesta.toString();
                    //Convierte la cadena recibida de JSON a un objeto Java
                    voRespuesta = voMapper.readValue(vcJson, UploadRequestResponse.class);
                } else {
                    voRespuesta = new UploadRequestResponse();
                    List<String>vlDatos = new ArrayList<>();
                    vlDatos.add("Error: " + vnRespCode);
                    voRespuesta.setlData(vlDatos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            voRespuesta = new UploadRequestResponse();
            List<String>vlDatos = new ArrayList<>();
            vlDatos.add("Error: " + e.getMessage());
            voRespuesta.setlData(vlDatos);
        } finally {
            if (vbSecure) {
                if (voConHttps!=null) {
                    voConHttps.disconnect();
                }
            } else {
                if (voConHttp!=null) {
                    voConHttp.disconnect();
                }
            }
        }
        return voRespuesta;
    }

    //    @TargetApi(Build.VERSION_CODES.FROYO)
    private static void setJellyBeanAuth(URLConnection httpConn) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        byte[] auth = ("jarufe" + ":" + "Rascayu0").getBytes();
        String basic = ""; //android.util.Base64.encodeToString(auth, Base64.NO_WRAP);
        httpConn.setRequestProperty("Authorization", "Basic " + basic);
//        }
    }
}
