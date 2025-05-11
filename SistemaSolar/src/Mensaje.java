import java.io.Serializable;
import java.util.UUID;

/**
 * Representa un mensaje intercambiado entre el Sol, la Tierra y otros planetas.
 * Este mensaje es serializable, lo que permite enviarlo por red usando sockets.
 *
 * Cada mensaje contiene:
 * - Una instrucción (SUBIR, BAJAR o RESET) que indica qué acción debe realizar el receptor.
 * - El origen del mensaje (ej. "🌞 SOL" o "Tierra").
 * - Un UUID único que identifica de manera inequívoca cada instancia, útil para evitar duplicados.
 */
public class Mensaje implements Serializable {

    // Instrucción que contiene el mensaje, definida en la enumeración Instruccion
    private final Instruccion instruccion;

    // Identificador del origen que envió el mensaje (Sol o Tierra)
    private final String origen;

    // Identificador único generado automáticamente para cada mensaje
    private final UUID id;

    /**
     * Constructor que inicializa el mensaje con una instrucción y un origen.
     * Se genera automáticamente un UUID único para permitir identificar el mensaje incluso si su contenido se repite.
     */
    public Mensaje(Instruccion instruccion, String origen) {
        this.instruccion = instruccion;
        this.origen = origen;
        this.id = UUID.randomUUID();
    }

    /**
     * Devuelve la instrucción del mensaje.
     */
    public Instruccion getInstruccion() {
        return instruccion;
    }

    /**
     * Devuelve el origen del mensaje.
     */
    public String getOrigen() {
        return origen;
    }

    /**
     * Devuelve el identificador único del mensaje (UUID).
     * Este ID permite reconocer el mensaje de forma unívoca, incluso si su contenido se repite.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Representación textual del mensaje, útil para depuración y salida por consola.
     */
    @Override
    public String toString() {
        return "[ID: " + id + ", Origen: " + origen + ", Instrucción: " + instruccion + "]";
    }
}
