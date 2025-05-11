/**
 * Enumeración que representa los tres tipos de instrucciones posibles que el Sol puede enviar a los planetas.
 *
 * Se utiliza aquí para asegurar que las instrucciones sean seguras, legibles y fácilmente mantenibles,
 * en lugar de usar cadenas de texto.
 *
 * Estas instrucciones se transmiten a través de multicast UDP desde el Sol, y son interpretadas por cada planeta
 * según su propia lógica.
 */
public enum Instruccion {
    SUBIR,
    BAJAR,
    RESET
}

