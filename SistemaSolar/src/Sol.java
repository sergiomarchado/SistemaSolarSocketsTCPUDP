import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Random;

/**
 * Su función principal es enviar cada 5 segundos un mensaje multicast a todos los planetas.
 * El mensaje contiene una instrucción (SUBIR, BAJAR o RESET) elegida aleatoriamente, con probabilidades concretas.
 *
 * Utiliza el protocolo UDP Multicast, que permite enviar mensajes a múltiples receptores simultáneamente
 * (en este caso: Tierra, Marte, Mercurio y Urano), sin necesidad de gestionar una conexión punto a punto con cada uno.
 */
public class Sol {

    public static void main(String[] args) {

        // Dirección IP del grupo multicast y puerto de envío
        String grupoMulticast = Constantes.MULTICAST_IP;
        int puerto = Constantes.MULTICAST_PORT;

        // Generador aleatorio para escoger instrucciones con probabilidad
        Random random = new Random();

        // Creamos el socket UDP que se usará para enviar los paquetes
        try (DatagramSocket socket = new DatagramSocket()) {

            // Convertimos la IP multicast en una InetAddress
            InetAddress grupo = InetAddress.getByName(grupoMulticast);

            System.out.println("------------------------------------------------------------------");
            System.out.println("🌞 Sol iniciado. Enviando instrucciones cada 5 segundos...");
            System.out.println("------------------------------------------------------------------");

            // Bucle infinito para enviar instrucciones cada 5 segundos
            while (true) {
                // Se elige una instrucción aleatoria con probabilidad (ver método más abajo)
                Instruccion instruccion = elegirInstruccion(random);

                // Se crea el mensaje con la instrucción y el identificador de origen
                Mensaje mensaje = new Mensaje(instruccion, "🌞 SOL");

                // Serializamos el objeto Mensaje para poder enviarlo como datos binarios
                byte[] datos = serializarMensaje(mensaje);

                // Empaquetamos los datos en un DatagramPacket con el grupo multicast y el puerto
                DatagramPacket paquete = new DatagramPacket(datos, datos.length, grupo, puerto);

                // Enviamos el paquete UDP al grupo multicast
                socket.send(paquete);

                // Mostramos por consola el mensaje enviado con color según el tipo de instrucción
                System.out.println("📤 Instrucción enviada: " + FormatoConsola.colorearPorInstruccion(instruccion, mensaje.toString()));

                // Pausa de 5 segundos antes de enviar el siguiente mensaje
                Thread.sleep(5000);
            }

        } catch (IOException | InterruptedException e) {
            // En caso de error, se imprime el mensaje y se interrumpe el hilo
            System.err.println("❌ Error en el Sol: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }


    // Convierte un objeto Mensaje a un array de bytes, necesario para su envío por UDP.
    private static byte[] serializarMensaje(Mensaje mensaje) throws IOException {

        // ByteArrayOutputStream almacena los bytes temporalmente en memoria
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             // ObjectOutputStream convierte el objeto en una secuencia de bytes
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {


            oos.writeObject(mensaje); // Se escribe el objeto en el flujo
            return baos.toByteArray(); // Se recuperan los bytes generados
        }
    }

    /**
     * Escoge aleatoriamente una instrucción con las siguientes probabilidades:
     * - SUBIR: 50%
     * - BAJAR: 30%
     * - RESET: 20%
     */
    private static Instruccion elegirInstruccion(Random rand) {
        int numero = rand.nextInt(100); // genera un número aleatorio entre 0 y 99

        if (numero < 50) return Instruccion.SUBIR;       // 0 - 49: 50%
        else if (numero < 80) return Instruccion.BAJAR;  // 50 - 79: 30%
        else return Instruccion.RESET;                   // 80 - 99: 20%
    }
}
