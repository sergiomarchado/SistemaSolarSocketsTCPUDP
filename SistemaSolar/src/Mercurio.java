import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 🟣 Clase MERCURIO
 *
 * - Escucha instrucciones del Sol por UDP Multicast.
 * - Recibe mensajes fiables desde la Tierra por TCP.
 * - Lee temperatura enviada desde Marte vía UDP (no fiable).
 * - Imprime su estado cada 2.5 segundos.
 * - EXPLOTA si su temperatura sale del rango [148ºC, 310ºC].
 */
public class Mercurio {

    private static final int TEMP_RESET = 179;
    private static int temperatura = TEMP_RESET;

    // Contador de mensajes recibidos directamente del Sol
    private static int mensajesSol = 0;

    // Última temperatura reportada por Marte (recibida por UDP)
    private static String tempMarte = "???";

    // Control de ejecución de hilos (se desactiva si Mercurio explota)
    private static final AtomicBoolean activo = new AtomicBoolean(true);

    // Conjunto de UUIDs de mensajes recibidos por TCP desde Tierra (evita duplicados)
    private static final Set<String> mensajesTierraProcesados = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("------------------------------------------------------------------\n");
        System.out.println("🟣 Mercurio en funcionamiento...");
        System.out.println("------------------------------------------------------------------\n");

        // Lanzamiento de hilos independientes para cada función
        new Thread(Mercurio::escucharMulticastSol).start();
        new Thread(Mercurio::escucharTCPDesdeTierra).start();
        new Thread(Mercurio::escucharUDPMarte).start();
        new Thread(Mercurio::mostrarEstadoCada2_5s).start();
    }

    /**
     * Escucha mensajes enviados por el Sol mediante UDP multicast.
     * Incrementa un contador si el mensaje proviene del Sol.
     */
    public static void escucharMulticastSol() {
        try (MulticastSocket socket = new MulticastSocket(Constantes.MULTICAST_PORT)) {
            socket.joinGroup(InetAddress.getByName(Constantes.MULTICAST_IP));

            while (activo.get()) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);  // Espera y recibe paquete UDP

                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()))) {
                    Mensaje mensaje = (Mensaje) ois.readObject();

                    if (mensaje.getOrigen().toLowerCase().contains("sol")) {
                        mensajesSol++;  // Solo si el mensaje proviene del Sol
                    }

                    comprobarExplosion();
                } catch (Exception e) {
                    System.out.println("❌ [Mercurio] Error leyendo mensaje multicast: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            if (activo.get()) System.out.println("❌ [Mercurio] Error en multicast: " + e.getMessage());
        }
    }

    /**
     * Escucha mensajes TCP desde la Tierra.
     * Aplica la instrucción si no es duplicada (usando UUID como clave).
     */
    public static void escucharTCPDesdeTierra() {
        try (ServerSocket serverSocket = new ServerSocket(Constantes.PUERTO_MERCURIO)) {
            while (activo.get()) {
                try (
                        Socket cliente = serverSocket.accept();
                        ObjectInputStream ois = new ObjectInputStream(cliente.getInputStream())
                ) {
                    Mensaje mensaje = (Mensaje) ois.readObject();

                    // Usamos el UUID como ID único del mensaje
                    String idMensaje = mensaje.getId().toString();
                    if (!mensajesTierraProcesados.add(idMensaje)) {
                        continue; // mensaje ya procesado
                    }

                    // Mostrar en consola el mensaje recibido
                    System.out.println("\n🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔");
                    System.out.println("MENSAJE RECIBIDO!!! de Tierra 🌍: " +
                            FormatoConsola.colorearPorInstruccion(mensaje.getInstruccion(), mensaje.toString()));

                    // Aplicar efecto sobre la temperatura
                    procesarMensajeDeTierra(mensaje.getInstruccion());

                    System.out.println(FormatoConsola.colorearPorInstruccion(
                            mensaje.getInstruccion(), "\n🌡️ Temperatura modificada a: " + temperatura + "ºC // LÍMITES: entre 310ºC y 148ºC"));
                    System.out.println("🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔\n");
                }

                comprobarExplosion();  // Se comprueba tras cada mensaje
            }
        } catch (Exception e) {
            if (activo.get()) System.out.println("❌ [Mercurio] Error en TCP: " + e.getMessage());
        }
    }

    /**
     * Aplica una instrucción a la temperatura de Mercurio.
     */
    private static void procesarMensajeDeTierra(Instruccion instruccion) {
        switch (instruccion) {
            case SUBIR -> temperatura += 50;
            case BAJAR -> temperatura -= 10;
            case RESET -> temperatura = TEMP_RESET;
        }
    }

    /**
     * Recibe por UDP la temperatura actual de Marte (cada 100ms).
     * No se garantiza la entrega (UDP no fiable).
     */
    public static void escucharUDPMarte() {
        try (DatagramSocket socket = new DatagramSocket(Constantes.PUERTO_UDP_MARTE_MERCURIO)) {
            while (activo.get()) {
                byte[] buffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                tempMarte = new String(packet.getData(), 0, packet.getLength());
            }
        } catch (Exception e) {
            if (activo.get()) System.out.println("❌ [Mercurio] Error recibiendo UDP desde Marte: " + e.getMessage());
        }
    }

    /**
     * Imprime el estado actual del planeta cada 2.5 segundos:
     * - Temperatura de Mercurio
     * - Nº de mensajes del Sol recibidos
     * - Última temperatura reportada por Marte
     */
    public static void mostrarEstadoCada2_5s() {
        while (activo.get()) {
            try {
                Thread.sleep(2500);
                System.out.println("🛰️ [Mercurio] Temp: " + temperatura + "ºC | Msgs Sol: " +
                        mensajesSol + " | Temp Marte: " + tempMarte);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Si la temperatura está fuera del rango permitido, Mercurio explota y se detienen los hilos.
     */
    private static void comprobarExplosion() {
        if (temperatura > 310 || temperatura < 148) {
            System.out.println("💥💥💥💥💥💥 [Mercurio] ¡EXPLOSIÓN! Temperatura fuera de rango: " + temperatura);
            activo.set(false);
        }
    }
}
