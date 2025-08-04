package jaru.ori.logic.sportident;

/**
 * Clase que contiene métodos estáticos necesarios para el cálculo y el manejo
 * del CRC que se utiliza en Sportident.
 * @author javier.arufe
 */
public class CRCCalculator {
    private CRCCalculator() {}

    private static final int POLY = 0x8005;
    private static final int BITF = 0x8000;

    /**
     * Método que calcula el CRC de un array de bytes, según el esquema propuesto por Sportident.
     * @param buffer byte[]. Array de bytes con el comando que se quiere calcular.
     * @return int. Valor del CRC.
     */
    static public int crc(byte[] buffer) {
        int   count = buffer.length;
        int   i, j;
        int tmp, val;    // 16 Bit
        int   ptr = 0;

        tmp = (short) (buffer[ptr++] << 8 | (buffer[ptr++] & 0xFF));

        if(count > 2) {
            // only even counts !!! and more than 4
            for (i = count / 2; i > 0; i--) {
                if (i > 1) {
                    val = (int) (buffer[ptr++] << 8 | (buffer[ptr++] & 0xFF));
                }
                else {
                    if(count%2==1) {
                        val = buffer[count-1] << 8;
                    }
                    else {
                        val = 0;	  // last value with 0   // last 16 bit value
                    }
                }

                for (j = 0; j < 16; j++) {
                    if ((tmp & BITF) != 0) {
                        tmp <<= 1;
                        if ((val & BITF) != 0) {
                            tmp++;    // rotate carry
                        }
                        tmp ^= POLY;
                    }
                    else {
                        tmp <<= 1;
                        if ((val & BITF) != 0) {
                            tmp++;    // rotate carry
                        }
                    }
                    val <<= 1;
                }
            }
        }
        return (tmp & 0xFFFF);
    }

    /**
     * Convierte un valor de tipo int en un array de bytes.
     * Java utiliza 4 bytes para almacenar un int, pero en Sportident sólo se utilizan enteros de 2 bytes.
     * Los elementos importantes del array son el 2 (byte más significativo) y el 3 (byte menos significativo).
     * @param value int. Valor que se quiere convertir.
     * @return byte[]. Valor convertido a ristra de bytes.
     */
    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)(value & 0xFF)};
    }

    /**
     * Convierte un valor entero que está en forma de bytes a un número de tipo int.
     * Java utiliza 4 bytes para almacenar un int, pero en Sportident sólo se utilizan enteros de 2 bytes.
     * Los elementos importantes del array son el 2 (byte más significativo) y el 3 (byte menos significativo).
     * @param b byte[]. Array de bytes que almacenan un número entero.
     * @return int. Valor entero en forma de número.
     */
    public static final int byteArrayToInt(byte [] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    /**
     * Realiza una comprobación acerca de la corrección de una sentencia devuelta por una
     * estación de descarga Sportident.
     * Se trata solamente de ver si es una posible sentencia: comienza por STX, termina por ETX y el CRC es válido.
     * @param paDatos byte[]. Array de bytes que contiene una supuesta sentencia Sportident.
     * @return int. Devuelve el valor del comando, en caso de que la sentencia sea correcta. Sino, devuelve -1.
     */
    public static int comprobarSentenciaCorrecta (byte[] paDatos) {
        int vnResul = -1;
        try {
            if (paDatos!=null){
                //Comprueba si se trata de un NAK
                if (paDatos.length==1 && paDatos[0]==((byte)0x15))
                    vnResul = (byte)0x15;
                if (paDatos.length>2) {
                    //Comprueba si comienza por STX y termina por ETX
                    if (paDatos[0]==((byte)0x02) && paDatos[paDatos.length-1]==((byte)0x03)) {
                        //Si la instrucción forma parte del protocolo extendido, comprueba el CRC
                        //Protocolo extendido -> comando >= 0x80 y no es 0xC4
                        if (paDatos[1]>=((byte)0x80) && paDatos[1]!=((byte)0xC4)) {
                            //Compone el valor del CRC tal y como está en la supuesta sentencia
                            byte[] vaCRC = {(byte)0x00, (byte)0x00, paDatos[paDatos.length-3], paDatos[paDatos.length-2]};
                            //y lo convierte a un valor entero
                            int vnCRC = CRCCalculator.byteArrayToInt(vaCRC);
                            //Compone la parte de la sentencia para la que se tiene que calcular el CRC
                            //desde la posición 1 de la sentencia hasta la posición (sentencia-4)
                            byte[] vaDatos = new byte[paDatos.length-4];
                            for (int i=0; i<vaDatos.length; i++) {
                                vaDatos[i] = paDatos[i+1];
                            }
                            //Calula el valor que tendría que tener el CRC de esa parte de la sentencia
                            int vnCRCCalculado = CRCCalculator.crc(vaDatos);
                            //Comprueba si el CRC que viene insertado en la sentencia es el mismo que tendría que tener.
                            if (vnCRC==vnCRCCalculado)
                                vnResul = paDatos[1];
                        }
                    }
                }
            }
        } catch (Exception e) {
            vnResul = -1;
        }
        return vnResul;
    }

    public static void main(String[] args) {
        byte[] test_data = new byte[] {

                // Example Test Message:

	    /*(byte)0x02, */
                (byte) 0x53, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x0F,
                (byte) 0xB5, (byte) 0x00, (byte) 0x00, (byte) 0x1E, (byte) 0x08
		/*, (byte)0x2C, (byte)0x12, (byte)0x03 */
        };
        byte[] sentencia = new byte[] { (byte)0x02,
                (byte) 0x53, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x0F,
                (byte) 0xB5, (byte) 0x00, (byte) 0x00, (byte) 0x1E, (byte) 0x08
                , (byte)0x2C, (byte)0x12, (byte)0x03
        };

        int valor = CRCCalculator.crc(test_data);
        System.out.println("Valor entero resultado: " + valor);
        // Should give 2C12 (see message above)
        System.out.println("Valor hexadecimal: " + Integer.toString(valor, 16));
        byte[] bytes = intToByteArray(valor);
        System.out.println("Byte 0: " + Integer.toString(bytes[0],16) +
                "; Byte 1: " + Integer.toString(bytes[1],16) +
                "; Byte 2: " + Integer.toString(bytes[2],16) +
                "; Byte 3: " + Integer.toString(bytes[3],16));
        System.out.println("Valor reconvertido a entero: " + byteArrayToInt(bytes));
        System.out.println("--------------------------------");
        String vcTest = Integer.toString(CRCCalculator.comprobarSentenciaCorrecta(sentencia), 16);
        System.out.println("Sentencia correcta?: 0x" + vcTest);
    }

}
