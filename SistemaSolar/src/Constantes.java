/**
 * Clase que contiene constantes compartidas por todos los componentes del sistema solar simulado.
 * Centraliza la configuración de IPs y puertos para facilitar mantenimiento, coherencia y legibilidad.
 */
public class Constantes {

    // Dirección IP de tipo Multicast utilizada por el Sol para enviar instrucciones a todos los planetas.
    // Rango válido de multicast: 224.0.0.0 - 239.255.255.255
    // 239.1.1.1 es una IP arbitraria reservada dentro del rango permitido para multicast local.
    public static final String MULTICAST_IP = "239.1.1.1";

    // Puerto común en el que todos los planetas escucharán los mensajes multicast del Sol.
    public static final int MULTICAST_PORT = 5000;

    // Puerto TCP de la tierra
    public static final int PUERTO_TIERRA = 6001;

    // Puerto TCP en el que Marte escucha mensajes reenviados por la Tierra.
    public static final int PUERTO_MARTE = 6002;

    // Puerto TCP en el que Mercurio escucha mensajes reenviados por la Tierra.
    public static final int PUERTO_MERCURIO = 6003;

    // Puerto TCP en el que Urano escucha mensajes reenviados por la Tierra.
    public static final int PUERTO_URANO = 6004;

    // Puerto UDP que utiliza Marte para enviar su temperatura actual a Mercurio.
    // Se trata de una comunicación no crítica, por eso se elige UDP.
    public static final int PUERTO_UDP_MARTE_MERCURIO = 7001;
}
