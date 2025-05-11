/**
 * 🖍️ Clase utilitaria para formatear salida por consola con colores.
 *
 * Utiliza códigos ANSI para aplicar color al texto mostrado en terminales compatibles.
 * Su propósito es hacer más visual y clara la salida de las instrucciones y eventos del sistema solar simulado.
 */
public class FormatoConsola {

    // Código ANSI que restablece el color al valor por defecto del terminal
    private static final String RESET = "\u001B[0m";

    // Código ANSI para texto en rojo (usado para SUBIR temperatura o explosiones)
    private static final String ROJO = "\u001B[31m";

    // Código ANSI para texto en azul claro (usado para BAJAR temperatura)
    private static final String AZUL_CLARO = "\u001B[36m";

    // Código ANSI para texto en magenta (usado para RESET)
    private static final String MAGENTA = "\u001B[35m";

    // Código ANSI para texto en verde (no usado directamente pero asi se tiene otro disponible)
    private static final String VERDE = "\u001B[32m";

    /**
     * Aplica color al texto según la instrucción que se ha recibido.
     *
     * @param instruccion La instrucción (SUBIR, BAJAR, RESET)
     * @param texto El texto que se desea colorear
     * @return Texto con códigos ANSI aplicados que se verá coloreado en consola
     */
    public static String colorearPorInstruccion(Instruccion instruccion, String texto) {
        return switch (instruccion) {
            case SUBIR -> ROJO + texto + RESET;        // Rojo para SUBIR temperatura
            case BAJAR -> AZUL_CLARO + texto + RESET;  // Azul claro para BAJAR temperatura
            case RESET -> MAGENTA + texto + RESET;     // Magenta para RESET (podría ser VERDE si se desea)
        };
    }

    /**
     * Devuelve un mensaje formateado en rojo indicando que un planeta ha explotado.
     *
     * @param nombrePlaneta Nombre del planeta que explotó
     * @return Mensaje en rojo con emojis de explosión
     */
    public static String explosion(String nombrePlaneta) {
        return ROJO + "💥💥💥 ¡" + nombrePlaneta.toUpperCase() + " HA EXPLOTADO! 💥💥💥" + RESET;
    }
}
