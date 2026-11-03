package com.example.gestarbien.ia.offline

class MemoriaOffline {

    var lastCategory: String? = null
    var lastOriginalCategory: String? = null
    var lastEmotion: String? = null
    var lastRiskLevel: String? = null
    var lastIntensity: String? = null
    var lastStyle: String? = null
    var consecutiveQuestions: Int = 0

    var lastQuestion: String? = null
    var lastResponse: String? = null

    val usedResponseIds = mutableListOf<String>()
    val usedQuestions = mutableListOf<String>()
    val usedResponseParts = mutableListOf<String>()
    val detectedTopics = mutableListOf<String>()

    var temaActual: TemaConversacional? = null
    var ultimaRespuestaUsuario: String? = null
    var respuestasEnEsteTema: Int = 0
    var profundidadMaxima: Int = 3
    val temasPendientes = mutableListOf<String>()
    val respuestasUsadasPorTema = mutableMapOf<String, MutableList<String>>()

    var esperandoRespuesta: Boolean = false
    var ultimaPregunta: String? = null
    var vecesPreguntadoMismoTema: Int = 0
    var preguntasConsecutivas: Int = 0

    // FIX: contador de turnos consecutivos en protocolo de riesgo alto.
    // Se incrementa cada vez que el bot responde con SAFETY_RESPONSES.
    // Se resetea cuando la usuaria sale del protocolo (bienestar, cierre,
    // saludo, o al pasar 3 turnos).
    var turnosEnRiesgoAlto: Int = 0

    val responseCycles = mutableMapOf<String, MutableList<BankItem>>()
    val responseCycleIndex = mutableMapOf<String, Int>()

    fun actualizar(
        category: String,
        originalCategory: String? = null,
        emotion: String,
        riskLevel: String,
        intensity: String,
        responseId: String?,
        question: String?,
        responseText: String? = null,
        usedParts: List<String>,
        newTopics: List<String>,
        lastStyle: String? = null,
    ) {
        lastCategory = category
        lastOriginalCategory = originalCategory ?: category
        lastEmotion = emotion
        lastRiskLevel = riskLevel
        lastIntensity = intensity
        lastQuestion = question
        lastResponse = responseText
        this.lastStyle = lastStyle

        if (question != null) {
            consecutiveQuestions++
            preguntasConsecutivas++
        } else {
            consecutiveQuestions = 0
            preguntasConsecutivas = 0
        }

        if (responseId != null && responseId !in usedResponseIds) {
            usedResponseIds.add(responseId)
        }
        while (usedResponseIds.size > 20) usedResponseIds.removeAt(0)

        if (question != null && question !in usedQuestions) {
            usedQuestions.add(question)
        }
        while (usedQuestions.size > 10) usedQuestions.removeAt(0)

        for (part in usedParts) {
            if (part !in usedResponseParts) usedResponseParts.add(part)
        }
        while (usedResponseParts.size > 15) usedResponseParts.removeAt(0)

        for (topic in newTopics) {
            if (topic !in detectedTopics) detectedTopics.add(topic)
        }
        while (detectedTopics.size > 10) detectedTopics.removeAt(0)

        // FIX: si estamos saliendo del protocolo de riesgo alto, resetear
        // el contador de turnos en riesgo.
        if (category != "riesgo_alto" && riskLevel != "alto") {
            turnosEnRiesgoAlto = 0
        }
    }

    fun limpiar() {
        lastCategory = null
        lastOriginalCategory = null
        lastEmotion = null
        lastRiskLevel = null
        lastIntensity = null
        lastStyle = null
        consecutiveQuestions = 0
        preguntasConsecutivas = 0
        lastQuestion = null
        lastResponse = null
        usedResponseIds.clear()
        usedQuestions.clear()
        usedResponseParts.clear()
        detectedTopics.clear()
        temaActual = null
        ultimaRespuestaUsuario = null
        respuestasEnEsteTema = 0
        temasPendientes.clear()
        respuestasUsadasPorTema.clear()
        esperandoRespuesta = false
        ultimaPregunta = null
        vecesPreguntadoMismoTema = 0
        responseCycles.clear()
        responseCycleIndex.clear()
        turnosEnRiesgoAlto = 0
    }

    fun hasUsedQuestion(question: String) = question in usedQuestions
    fun hasUsedResponse(responseId: String) = responseId in usedResponseIds

    fun iniciarTema(tema: String, emocion: String = "neutral") {
        temaActual = TemaConversacional(
            tema = tema,
            emocion = emocion,
            profundidad = 0,
            preguntasHechas = mutableListOf(),
            respuestasRecibidas = mutableListOf()
        )
        respuestasEnEsteTema = 0
        vecesPreguntadoMismoTema = 0
        preguntasConsecutivas = 0
        if (!respuestasUsadasPorTema.containsKey(tema)) {
            respuestasUsadasPorTema[tema] = mutableListOf()
        }
    }

    fun actualizarTema(mensaje: String) {
        temaActual?.let { tema ->
            tema.respuestasRecibidas.add(mensaje)
            tema.profundidad++
            respuestasEnEsteTema++
            ultimaRespuestaUsuario = mensaje
        }
        // NO resetea preguntasConsecutivas aquí.
        esperandoRespuesta = false
    }

    fun registrarPregunta(pregunta: String) {
        temaActual?.preguntasHechas?.add(pregunta)
        ultimaPregunta = pregunta
        esperandoRespuesta = true
        vecesPreguntadoMismoTema++
        preguntasConsecutivas++
        if (pregunta !in usedQuestions) {
            usedQuestions.add(pregunta)
            while (usedQuestions.size > 10) usedQuestions.removeAt(0)
        }
    }

    fun marcarRespuestaRecibida() {
        esperandoRespuesta = false
        vecesPreguntadoMismoTema = 0
        preguntasConsecutivas = 0
    }

    fun tieneTemaActivo(): Boolean = temaActual != null
    fun getTema(): String? = temaActual?.tema
    fun getProfundidad(): Int = temaActual?.profundidad ?: 0
    fun getEmocionTema(): String = temaActual?.emocion ?: "neutral"

    fun guardarRespuestaUsada(tema: String, respuesta: String) {
        if (!respuestasUsadasPorTema.containsKey(tema)) {
            respuestasUsadasPorTema[tema] = mutableListOf()
        }
        respuestasUsadasPorTema[tema]?.add(respuesta)
    }

    fun haUsadoRespuestaEnTema(tema: String, respuesta: String): Boolean {
        return respuestasUsadasPorTema[tema]?.contains(respuesta) ?: false
    }

    fun obtenerRespuestasUsadasEnTema(tema: String): List<String> {
        return respuestasUsadasPorTema[tema] ?: emptyList()
    }
}

data class TemaConversacional(
    val tema: String,
    var emocion: String,
    var profundidad: Int = 0,
    val preguntasHechas: MutableList<String> = mutableListOf(),
    val respuestasRecibidas: MutableList<String> = mutableListOf()
)