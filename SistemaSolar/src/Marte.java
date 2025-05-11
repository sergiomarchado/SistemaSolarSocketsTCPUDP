import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 🔴 Clase MARTE
 *
 * Funcionalidades principales:
 * - Recibe mensajes del Sol mediante Multicast (UDP).
 * - Recibe instrucciones directas desde Tierra por TCP (fiable).
 * - Cada mensaje de la Tierra solo se procesa una vez (detecta duplicados por UUID).
 * - Aplica tanto las instrucciones del Sol como las reenviadas desde Tierra.
 * - Muestra su temperatura y el nº de mensajes del Sol cada 2 segundos.
 * - Envía su temperatura a Mercurio por UDP cada 100 ms.
 * - Explota si su temperatura se sale del rango [-182, -33] °C.
 */
public class Marte {

    private static final int TEMP_RESET = -63;
    private static int temperatura = TEMP_RESET;

    private static int mensajesSol = 0;
    private static final AtomicBoolean activo = new AtomicBoolean(true);

    // Control de duplicados: solo para mensajes desde la Tierra
    private static final Set<UUID> mensajesTierraProcesados = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("------------------------------------------------------------------");
        System.out.println("🔴 Marte en funcionamiento...");
        System.out.println("------------------------------------------------------------------");

        new Thread(Marte::escucharMulticastSol).start();
        new Thread(Marte::escucharTCPDesdeTierra).start();
        new Thread(Marte::mostrarInfoCada2s).start();
        new Thread(Marte::enviarTemperaturaAMercurio).start();
    }

    /**
     * Escucha mensajes del Sol vía Multicast.
     * Cada vez que llega uno válido:
     * - Incrementa el contador de mensajes solares.
     * - Aplica la instrucción sobre la temperatura.
     */
    public static void escucharMulticastSol() {
        try (MulticastSocket socket = new MulticastSocket(Constantes.MULTICAST_PORT)) {
            socket.joinGroup(InetAddress.getByName(Constantes.MULTICAST_IP));

            while (activo.get()) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()))) {
                    Mensaje mensaje = (Mensaje) ois.readObject();

                    if (mensaje.getOrigen().toLowerCase().contains("sol")) {
                        mensajesSol++;

                        // ✅ Aplica la instrucción recibida del Sol
                        aplicarInstruccion(mensaje.getInstruccion());

                        System.out.println(FormatoConsola.colorearPorInstruccion(
                                mensaje.getInstruccion(),
                                "☀️ Instrucción del Sol aplicada. Nueva temperatura: " + temperatura + "ºC"
                        ));
                    }

                    comprobarExplosion();
                } catch (Exception e) {
                    System.err.println("❌ [Marte] Error leyendo mensaje del Sol: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            if (activo.get()) System.err.println("❌ [Marte] Error en multicast: " + e.getMessage());
        }
    }

    /**
     * Escucha mensajes TCP provenientes de la Tierra.
     * Cada mensaje tiene un UUID único y solo se procesa una vez.
     */
    public static void escucharTCPDesdeTierra() {
        try (ServerSocket serverSocket = new ServerSocket(Constantes.PUERTO_MARTE)) {
            while (activo.get()) {
                try (
                        Socket cliente = serverSocket.accept();
                        ObjectInputStream ois = new ObjectInputStream(cliente.getInputStream())
                ) {
                    Mensaje mensaje = (Mensaje) ois.readObject();

                    // ✅ Evita duplicados por UUID
                    if (!mensajesTierraProcesados.add(mensaje.getId())) {
                        continue;
                    }

                    System.out.println("\n🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔");
                    System.out.println("MENSAJE RECIBIDO de Tierra 🌍: " +
                            FormatoConsola.colorearPorInstruccion(mensaje.getInstruccion(), mensaje.toString()));

                    aplicarInstruccion(mensaje.getInstruccion());

                    System.out.println(FormatoConsola.colorearPorInstruccion(
                            mensaje.getInstruccion(),
                            "\n🌡️ Temperatura modificada a: " + temperatura + "ºC // LÍMITES: entre -33ºC y -182ºC\n"
                    ));
                    System.out.println("🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔\n");

                    comprobarExplosion();
                } catch (Exception e) {
                    System.err.println("❌ [Marte] Error procesando mensaje TCP: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("❌ [Marte] Error en socket TCP: " + e.getMessage());
        }
    }

    /**
     * Aplica una instrucción (subir, bajar, resetear) sobre la temperatura actual.
     * Usado tanto para instrucciones del Sol como de Tierra.
     */
    public static void aplicarInstruccion(Instruccion instruccion) {
        switch (instruccion) {
            case SUBIR -> temperatura += 10;
            case BAJAR -> temperatura -= 40;
            case RESET -> temperatura = TEMP_RESET;
        }
    }

    /**
     * Muestra el estado del planeta cada 2 segundos:
     * - Temperatura
     * - Nº de mensajes del Sol recibidos
     */
    public static void mostrarInfoCada2s() {
        while (activo.get()) {
            try {
                Thread.sleep(2000);
                System.out.println("📊 [Marte] Temp: " + temperatura + "ºC | Mensajes del Sol: " + mensajesSol);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Envía la temperatura de Marte a Mercurio por UDP cada 100 ms.
     * No garantiza entrega (UDP no fiable).
     */
    public static void enviarTemperaturaAMercurio() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress destino = InetAddress.getByName("localhost");

            while (activo.get()) {
                String tempMsg = String.valueOf(temperatura);
                byte[] buffer = tempMsg.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destino, Constantes.PUERTO_UDP_MARTE_MERCURIO);
                socket.send(packet);
                Thread.sleep(100);
            }
        } catch (Exception e) {
            if (activo.get()) System.err.println("❌ [Marte] Error enviando a Mercurio: " + e.getMessage());
        }
    }

    /**
     * Si la temperatura sale del rango permitido, el planeta explota.
     * Se detienen todos los hilos en ejecución.
     */
    private static void comprobarExplosion() {
        if (temperatura > -33 || temperatura < -182) {
            System.out.println("💥💥💥💥💥💥 [Marte] ¡EXPLOSIÓN! Temperatura fuera de rango: " + temperatura);
            activo.set(false);
        }
    }
}
