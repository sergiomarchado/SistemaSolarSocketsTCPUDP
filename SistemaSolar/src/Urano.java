import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 💠 Planeta Urano
 *
 * - Recibe solo instrucciones por TCP desde la Tierra (ignora al Sol).
 * - Procesa cada mensaje recibido una sola vez (usando su UUID).
 * - Aplica la instrucción para modificar su temperatura.
 * - Muestra su temperatura actual cada 4 segundos.
 * - EXPLOTA si su temperatura se sale del rango permitido: entre -374°C y -177°C.
 */
public class Urano {

    private static final int TEMP_RESET = -193;  // Temperatura inicial por defecto
    private static int temperatura = TEMP_RESET;

    // Bandera para controlar si Urano sigue activo (usado por todos los hilos)
    private static final AtomicBoolean activo = new AtomicBoolean(true);

    // Conjunto para almacenar los UUID de los mensajes ya procesados (evita duplicados)
    private static final Set<UUID> mensajesProcesados = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("------------------------------------------------------------------");
        System.out.println("🌀 Urano en funcionamiento...");
        System.out.println("------------------------------------------------------------------");

        // Lanzar hilo que escucha mensajes TCP desde Tierra
        new Thread(Urano::escucharTCPDesdeTierra).start();

        // Lanzar hilo que imprime la temperatura cada 4 segundos
        new Thread(Urano::mostrarTemperaturaCada4s).start();
    }

    /**
     * Escucha mensajes TCP desde la Tierra (uno a la vez).
     * Solo aplica instrucciones si el origen es "Tierra".
     * Evita duplicados mediante el UUID del mensaje.
     */
    public static void escucharTCPDesdeTierra() {
        try (ServerSocket serverSocket = new ServerSocket(Constantes.PUERTO_URANO)) {
            while (activo.get()) {
                // Esperar una conexión entrante
                Socket cliente = serverSocket.accept();

                try (ObjectInputStream ois = new ObjectInputStream(cliente.getInputStream())) {
                    // Leer el mensaje enviado
                    Mensaje mensaje = (Mensaje) ois.readObject();

                    // Verificar si viene de Tierra
                    if (mensaje.getOrigen().toLowerCase().contains("tierra")) {

                        // Si el UUID ya está procesado, lo ignoramos
                        if (!mensajesProcesados.add(mensaje.getId())) {
                            continue; // mensaje duplicado
                        }

                        // Mostrar mensaje recibido
                        System.out.println("\n🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔");
                        System.out.println("MENSAJE RECIBIDO!!! 🌍: " +
                                FormatoConsola.colorearPorInstruccion(mensaje.getInstruccion(), mensaje.toString()));

                        // Aplicar instrucción sobre temperatura
                        aplicarInstruccion(mensaje.getInstruccion());

                        // Mostrar resultado
                        System.out.println(FormatoConsola.colorearPorInstruccion(
                                mensaje.getInstruccion(), "\n🌡️ Temperatura modificada: " + temperatura + "ºC // LÍMITES: entre -177ºC y -374ºC"));
                        System.out.println("🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔🔔\n");

                        // Verificar si Urano debe explotar
                        comprobarExplosion();
                    } else {
                        System.out.println("📭 [Urano] Mensaje ignorado (no proviene de Tierra): " + mensaje);
                    }

                } catch (Exception e) {
                    System.err.println("❌ [Urano] Error procesando mensaje: " + e.getMessage());
                }

                cliente.close();
            }
        } catch (IOException e) {
            if (activo.get()) System.out.println("❌ [Urano] Error en TCP: " + e.getMessage());
        }
    }

    /**
     * Aplica la instrucción recibida para modificar la temperatura local.
     * Cada instrucción tiene un efecto distinto:
     * - SUBIR: +5°C
     * - BAJAR: -80°C
     * - RESET: se restablece a -193°C
     */
    private static void aplicarInstruccion(Instruccion instruccion) {
        switch (instruccion) {
            case SUBIR -> temperatura += 5;
            case BAJAR -> temperatura -= 80;
            case RESET -> temperatura = TEMP_RESET;
        }
    }

    /**
     * Imprime la temperatura actual de Urano cada 4 segundos.
     */
    public static void mostrarTemperaturaCada4s() {
        while (activo.get()) {
            try {
                Thread.sleep(4000);
                System.out.println("🧊 [Urano] Temp actual: " + temperatura + "°C");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Si la temperatura se sale del rango permitido, Urano "explota"
     * y se apagan todos los hilos del planeta.
     */
    private static void comprobarExplosion() {
        if (temperatura > -177 || temperatura < -374) {
            System.out.println("💥💥💥 [Urano] ¡EXPLOSIÓN! Temperatura fuera de rango: " + temperatura);
            activo.set(false);
        }
    }
}
