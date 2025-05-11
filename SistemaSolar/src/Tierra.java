import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 🌍 Clase TIERRA
 * - Recibe instrucciones del Sol vía Multicast.
 * - Aplica efecto sobre su temperatura.
 * - Reenvía CADA mensaje recibido una sola vez a otro planeta por TCP (con probabilidad).
 * - Muestra temperatura cada 3 segundos.
 * - No puede explotar.
 */
public class Tierra {

    // Temperatura base de la Tierra
    private static final int TEMP_RESET = 15;
    // Temperatura actual (segura en multihilo)
    private static final AtomicInteger temperatura = new AtomicInteger(TEMP_RESET);

    // Para decisiones aleatorias de reenvío
    private static final Random random = new Random();

    // Contadores de reenvíos para estadística
    private static final AtomicInteger enviadosAMercurio = new AtomicInteger(0);
    private static final AtomicInteger enviadosAMarte = new AtomicInteger(0);
    private static final AtomicInteger enviadosAUrano = new AtomicInteger(0);

    // Para evitar reenviar múltiples veces el mismo mensaje recibido por Multicast
    private static final Set<String> mensajesProcesados = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("------------------------------------------------------------------");
        System.out.println("🌍 Tierra en funcionamiento...");
        System.out.println("------------------------------------------------------------------\n");

        // Hilo que escucha instrucciones multicast del Sol
        new Thread(Tierra::escucharSol).start();

        // Hilo que muestra la temperatura de la Tierra cada 3 segundos
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);
                    System.out.println("\n🌍 [TIERRA] Temperatura actual: " + temperatura.get() + "ºC");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    /**
     * Escucha mensajes multicast provenientes del Sol.
     * Procesa y reenvía cada mensaje recibido solo una vez.
     */
    public static void escucharSol() {
        try (MulticastSocket socket = new MulticastSocket(Constantes.MULTICAST_PORT)) {
            InetAddress grupo = InetAddress.getByName(Constantes.MULTICAST_IP);
            socket.joinGroup(grupo); // Se une al grupo multicast

            while (true) {
                // Espera y recibe paquete UDP
                byte[] buffer = new byte[1024];
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquete);

                // Deserializa el mensaje recibido desde el buffer
                try (
                        ByteArrayInputStream bais = new ByteArrayInputStream(paquete.getData(), 0, paquete.getLength());
                        ObjectInputStream ois = new ObjectInputStream(bais)
                ) {
                    Mensaje mensaje = (Mensaje) ois.readObject();

                    // Ignora si el mensaje no viene del Sol
                    if (!mensaje.getOrigen().toLowerCase().contains("sol")) continue;

                    // Evita procesar el mismo mensaje dos veces usando su UUID como clave única
                    String idMensaje = mensaje.getId().toString();
                    if (!mensajesProcesados.add(idMensaje)) continue; // ya procesado

                    // Mostrar recepción del mensaje
                    System.out.println("\n🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔");
                    System.out.println("MENSAJE RECIBIDO!!! del Sol 🌞: " +
                            FormatoConsola.colorearPorInstruccion(mensaje.getInstruccion(), mensaje.toString()));

                    procesarInstruccion(mensaje.getInstruccion());
                    System.out.println("\n🌡️ Temperatura modificada: " + temperatura.get() + "ºC");
                    System.out.println("🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔\n");


                    // Reenvía el mensaje a un planeta al azar (solo una vez)
                    reenviarMensaje(mensaje);

                } catch (Exception e) {
                    System.err.println("❌ Error procesando mensaje recibido: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Error en Tierra (Multicast): " + e.getMessage());
        }
    }

    /**
     * Aplica el efecto de la instrucción recibida sobre la temperatura de la Tierra.
     */
    private static void procesarInstruccion(Instruccion instruccion) {
        switch (instruccion) {
            case SUBIR -> temperatura.addAndGet(5);
            case BAJAR -> temperatura.addAndGet(-5);
            case RESET -> temperatura.set(TEMP_RESET);
        }
    }

    /**
     * Reenvía el mensaje recibido a uno de los planetas con probabilidad:
     * - 60% Mercurio
     * - 10% Marte
     * - 30% Urano
     */
    private static void reenviarMensaje(Mensaje mensajeOriginal) {
        int prob = random.nextInt(100);
        String destino;
        int puertoDestino;

        // Selección del planeta destino en base a la probabilidad
        if (prob < 60) {
            destino = "Mercurio";
            puertoDestino = Constantes.PUERTO_MERCURIO;
            enviadosAMercurio.incrementAndGet();
        } else if (prob < 70) {
            destino = "Marte";
            puertoDestino = Constantes.PUERTO_MARTE;
            enviadosAMarte.incrementAndGet();
        } else {
            destino = "Urano";
            puertoDestino = Constantes.PUERTO_URANO;
            enviadosAUrano.incrementAndGet();
        }

        // Enviamos el mensaje TCP en un hilo aparte para no bloquear
        new Thread(() -> {
            try (
                    Socket socket = new Socket("localhost", puertoDestino);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())
            ) {
                // Creamos una nueva instancia de Mensaje con nuevo UUID y origen "Tierra"
                Mensaje reenviado = new Mensaje(mensajeOriginal.getInstruccion(), "Tierra");
                oos.writeObject(reenviado); // Enviamos el objeto

                // Muestra por consola el reenvío
                System.out.println("\n➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️");
                System.out.println("REENVIANDO MENSAJE A " + destino + ": " +
                        FormatoConsola.colorearPorInstruccion(reenviado.getInstruccion(), reenviado.toString()));

                // Muestra estadísticas de reenvíos acumulados
                System.out.println("\n📊 Reenvíos acumulados → Mercurio: " + enviadosAMercurio.get()
                        + ", Marte: " + enviadosAMarte.get()
                        + ", Urano: " + enviadosAUrano.get());
                System.out.println("➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️➡️\n");

            } catch (IOException e) {
                System.err.println("❌ No se pudo contactar con " + destino + ": " + e.getMessage());
            }
        }).start();
    }
}
