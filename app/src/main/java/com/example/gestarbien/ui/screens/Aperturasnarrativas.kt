package com.example.gestarbien.ui.screens

/**
 * Aperturas de conversación que usa el chat justo después de que la usuaria
 * termina el EPDS y el PASS.
 *
 * Principios que sostienen estos textos:
 *
 *  1. El bot NUNCA menciona el puntaje ni la categoría. El número decide qué
 *     pregunta hacer, no qué decirle a ella. La interpretación es del profesional.
 *  2. Las preguntas son externalizantes: separan a la persona del problema
 *     ("eso que ha estado rondando") en lugar de definirla por él ("tu ansiedad").
 *  3. En puntajes bajos no se felicita: se buscan excepciones, momentos en que
 *     las cosas funcionaron, que es material para el relato alternativo.
 *  4. Si marcó el ítem 10 del EPDS, la prioridad deja de ser explorar y pasa a
 *     ser acompañar y ofrecer un recurso real antes de cualquier pregunta.
 *
 * TODO: textos pendientes de revisión por el área de psicología del proyecto.
 */

/** Franja de resultado. No se le muestra nunca a la usuaria. */
enum class FranjaResultado { BAJA, MEDIA, ALTA, ALERTA }

fun franjaDeResultado(
    puntajeEpds: Int?,
    puntajePass: Int?,
    alertaItem10: Boolean
): FranjaResultado = when {
    alertaItem10 -> FranjaResultado.ALERTA
    (puntajeEpds ?: 0) >= 13 || (puntajePass ?: 0) >= 42 -> FranjaResultado.ALTA
    (puntajeEpds ?: 0) >= 10 || (puntajePass ?: 0) >= 21 -> FranjaResultado.MEDIA
    else -> FranjaResultado.BAJA
}

// ─────────────────────────────────────────────────────────────
//  BANCO DE APERTURAS
//  Cada entrada es una conversación completa: una o varias
//  burbujas que el bot envía seguidas.
//
//  Los marcadores {PROF} y {PROF_CAP} se reemplazan en tiempo de
//  ejecución con el nombre del profesional asignado, si se conoce
//  (ver aperturaSegunResultados). {PROF} va a mitad de frase en
//  minúscula, {PROF_CAP} al inicio de frase con mayúscula.
// ─────────────────────────────────────────────────────────────

/** Puntajes bajos: se buscan excepciones, no se felicita. */
private val APERTURAS_BAJA = listOf(
    listOf(
        "Gracias por tomarte el tiempo de responder. Sé que no siempre es fácil ponerle palabras a lo que una siente.",
        "En estas semanas hubo días que estuvieron bien. ¿Te acuerdas de alguno en particular? Me gustaría saber qué tenía ese día."
    ),
    listOf(
        "Listo, ya quedaron guardados. Gracias por la honestidad.",
        "Cuéntame algo: de estas últimas semanas, ¿hubo algún momento en que te sentiste más tú misma? No tiene que ser algo grande."
    ),
    listOf(
        "Terminamos. Lo que respondiste queda en tu historia, y tu profesional lo va a revisar contigo.",
        "Mientras tanto, tengo curiosidad por algo: ¿qué es lo que mejor te ha funcionado estos días cuando la cosa se pone cuesta arriba?"
    )
)

/** Puntajes medios: externalización del malestar. */
private val APERTURAS_MEDIA = listOf(
    listOf(
        "Gracias por responder con tanta honestidad. No es poca cosa.",
        "Por lo que me contaste, hay algo que ha estado rondando estas semanas. Si eso tuviera un nombre, ¿cuál le pondrías?"
    ),
    listOf(
        "Ya quedó todo guardado. Sé que responder estas preguntas a veces remueve cosas.",
        "Quiero preguntarte algo distinto: eso que has venido cargando, ¿a qué hora del día aparece con más fuerza? ¿Y hay momentos en que se calla?"
    ),
    listOf(
        "Listo. Gracias por dedicarle este rato a ti misma.",
        "De lo que respondiste, ¿qué fue lo que más te costó marcar? A veces la pregunta que más incomoda es la que más tiene para decirnos."
    )
)

/** Puntajes altos: primero orientar al profesional, después acompañar. */
private val APERTURAS_ALTA = listOf(
    listOf(
        "Gracias por responder. Por lo que me cuentas, estas semanas han pesado bastante.",
        "Quiero que sepas algo: no tienes que sostener esto sola. Buscar compañía en un momento así puede ayudar mucho.",
        "Si quieres, mientras tanto puedes contarme qué ha sido lo más difícil de estos días."
    ),
    listOf(
        "Ya terminamos con las preguntas. Gracias por tu honestidad, sobre todo porque veo que no ha sido una época fácil.",
        "Buscar acompañamiento en un momento así no es debilidad, es de las cosas más valientes que se pueden hacer.",
        "¿Qué es lo que más te ha estado costando últimamente?"
    )
)

/** Ítem 10 marcado: acompañar y ofrecer recurso antes de preguntar. */
private val APERTURA_ALERTA = listOf(
    "Gracias por responder con tanta sinceridad. Sé que hay preguntas de esas que cuesta mucho marcar.",
    "Quiero decirte algo con calma: lo que estás sintiendo merece acompañamiento, y no tienes que atravesarlo sola. " +
            "{PROF_CAP} es tu psicólogo asignado, y si en algún momento sientes que no puedes más, puedes llamar " +
            "a la [[tel:106:Línea 106]] a cualquier hora del día. No hace falta estar en una crisis para llamar.",
    "Y si ahora no te sientes lista para eso, está bien. De todas formas voy a seguir aquí, acompañándote.",
    "Estoy acá si quieres contarme cómo han sido estos días."
)

// ─────────────────────────────────────────────────────────────
//  PERSONALIZACIÓN DEL NOMBRE DEL PROFESIONAL
// ─────────────────────────────────────────────────────────────

private fun fraseProfesional(nombre: String?): String =
    if (nombre.isNullOrBlank()) "tu profesional" else "$nombre, tu profesional,"

private fun fraseProfesionalCap(nombre: String?): String =
    if (nombre.isNullOrBlank()) "Tu profesional" else "$nombre, tu profesional,"

private fun personalizar(texto: String, nombreProfesional: String?): String =
    texto
        .replace("{PROF_CAP}", fraseProfesionalCap(nombreProfesional))
        .replace("{PROF}", fraseProfesional(nombreProfesional))


// ─────────────────────────────────────────────────────────────
//  MENSAJE INFORMATIVO SOBRE EL PROFESIONAL ASIGNADO
//  Se envía una sola vez, justo después de EPDS + PASS, sin importar
//  la franja de resultado — es transparencia general de la app, no
//  una respuesta a una señal de riesgo puntual (eso lo maneja aparte
//  BotonContactoProfesional, el botón de WhatsApp).
// ─────────────────────────────────────────────────────────────

private fun formatearTelefono(numero: String?): String {
    val limpio = numero?.filter { it.isDigit() } ?: ""
    return if (limpio.isNotEmpty()) "+$limpio" else ""
}

fun mensajeInfoProfesional(
    nombreProfesional: String?,
    whatsappProfesional: String?,
    revelarNumero: Boolean = true
): String {
    if (nombreProfesional.isNullOrBlank()) {
        return "Quiero contarte algo: esta app te asigna un psicólogo al que puedes contactar si en algún momento te sientes muy mal. Todavía estamos por confirmarte quién es — en cuanto lo tenga, te aviso."
    }

    val telefono = formatearTelefono(whatsappProfesional)

    if (!revelarNumero || telefono.isBlank()) {
        return "Quiero contarte algo: esta app te asigna un psicólogo al que puedes contactar si en algún momento te sientes muy mal.\n\nEn tu caso, tu psicólogo asignado es **$nombreProfesional**. Si más adelante quieres escribirle, aquí puedo darte sus datos."
    }

    return "Quiero contarte algo, esta app te asigna un psicólogo al que puedes contactar si en algún momento te sientes muy mal.\n\nEn tu caso, tu psicólogo asignado es **$nombreProfesional** y su número de contacto es **$telefono**."
}
/**
 * Devuelve las burbujas que el bot debe enviar tras los cuestionarios.
 * Las variantes rotan al azar para que no se sienta un guion repetido.
 *
 * @param nombreProfesional nombre del profesional asignado a la usuaria,
 *        si ya se conoce (ver ProfesionalesRepository.obtenerOAsignarProfesional
 *        + obtenerPerfilPorCorreo). Si es null o vacío, se usa "tu profesional"
 *        genérico, igual que antes.
 */
fun aperturaSegunResultados(
    puntajeEpds: Int?,
    puntajePass: Int?,
    alertaItem10: Boolean,
    nombreProfesional: String? = null
): List<String> {
    val burbujas = when (franjaDeResultado(puntajeEpds, puntajePass, alertaItem10)) {
        FranjaResultado.ALERTA -> APERTURA_ALERTA
        FranjaResultado.ALTA   -> APERTURAS_ALTA.random()
        FranjaResultado.MEDIA  -> APERTURAS_MEDIA.random()
        FranjaResultado.BAJA   -> APERTURAS_BAJA.random()
    }
    return burbujas.map { personalizar(it, nombreProfesional) }
}