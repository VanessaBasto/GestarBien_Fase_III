package com.example.gestarbien.ia.offline

import kotlin.random.Random

data class RespuestaOffline(
    val bloques: List<String>,
    val categoria: String,
    val modulo: String?,
    val requiereAlerta: Boolean,
    val tema: String? = null,
    val esValidacion: Boolean = false,
    val profundidad: Int = 0
)

object OfflineBank {

    private val CATEGORY_TO_MODULE: Map<String, String?> = mapOf(
        "saludo" to null,
        "bienestar" to "estado_emocional",
        "malestar" to "estado_emocional",
        "tristeza" to "estado_emocional",
        "ansiedad" to "externalizacion",
        "frustracion" to "estado_emocional",
        "confusion" to "estado_emocional",
        "ambivalencia_embarazo" to "estado_emocional",
        "ambivalencia_followup" to "estado_emocional",
        "sintoma_fisico" to "estado_emocional",
        "sintoma_fisico_followup" to "estado_emocional",
        "cansancio" to "estado_emocional",
        "lactancia" to "orientacion_salud",
        "cuerpo_cambios" to "estado_emocional",
        "ansiedad_bebe" to "externalizacion",
        "identidad_postparto" to "identidad_preferida",
        "desahogo" to "estado_emocional",
        "desahogo_sin_apoyo" to "estado_emocional",
        "red_apoyo" to "redes_apoyo",
        "vinculo_bebe" to "conversaciones_bebe",
        "identidad_materna" to "identidad_preferida",
        "pedir_mas_informacion" to null,
        "pregunta_por_que" to null,
        "respuesta_corta" to null,
        "cierre" to "cierre",
        "riesgo_medio" to "estado_emocional",
        "riesgo_alto" to "estado_emocional",
        "desconocido" to null,
        "multitema_ambivalencia_sintoma_ansiedad" to "estado_emocional",
        "multitema_ambivalencia_ansiedad" to "estado_emocional",
        "multitema_sintoma_ansiedad" to "estado_emocional",
        "multitema_tristeza_ansiedad" to "estado_emocional",
    )

    private val CONTEXTUAL_CATEGORIES = listOf(
        "pedir_mas_informacion", "pregunta_por_que", "respuesta_corta", "desconocido",
    )

    private val STRUCTURED_ONLY_CATEGORIES = listOf(
        "saludo", "cierre", "respuesta_corta", "pedir_mas_informacion", "pregunta_por_que",
        "red_apoyo", "vinculo_bebe", "identidad_materna",
        "multitema_ambivalencia_sintoma_ansiedad", "multitema_ambivalencia_ansiedad",
        "multitema_sintoma_ansiedad", "multitema_tristeza_ansiedad",
    )

    private val FOLLOWUP_LIKE_CATEGORIES = listOf(
        "ambivalencia_followup", "sintoma_fisico_followup",
        "pedir_mas_informacion", "pregunta_por_que", "respuesta_corta", "desconocido",
    )

    private val ALERT_CATEGORIES = listOf(
        "ambivalencia_embarazo", "ambivalencia_followup",
        "multitema_ambivalencia_sintoma_ansiedad", "multitema_ambivalencia_ansiedad",
        "riesgo_medio", "riesgo_alto",
    )

    // ============================================================
    // RESPUESTAS POR TEMA — ordenadas como secuencia narrativa
    // ============================================================
    private val RESPONSES_BY_TOPIC = mapOf(

        "familia" to listOf(
            "Qué bonito que quieras hablar de tu familia. ¿Qué momentos o gestos concretos vienen a tu mente cuando piensas en ellos?",
            "Las relaciones familiares tienen su propia historia. ¿Cómo ha sido para ti sentir ese apoyo a lo largo de esta etapa?",
            "Ese apoyo que sientes, ¿qué forma toma en el día a día?",
            "Las familias no siempre son perfectas ni siempre están presentes del modo que quisiéramos. ¿Hay algo que te gustaría que fuera distinto en cómo te acompañan?",
            "Parece que la familia es importante para ti. ¿Qué te gustaría transmitirle a tu bebé sobre lo que significa estar en familia?",
            "A veces el apoyo de la familia no se mide en grandes gestos, sino en presencias pequeñas y constantes. ¿Qué gesto de los tuyos te ha sostenido últimamente?",
            "Gracias por contarme cómo es tu familia. No todas las familias acompañan igual, y lo que tú has construido con la tuya tiene valor.",
            "El apoyo familiar puede ser un sostén importante en esta etapa. Reconocerlo también es una forma de cuidarte."
        ),

        "pareja" to listOf(
            "Cuando piensas en tu pareja en esta etapa, ¿qué es lo primero que sientes?",
            "Las relaciones cambian con la llegada de un bebé. ¿Cómo ha sido ese cambio para ustedes?",
            "¿Qué gestos de tu pareja te hacen sentir acompañada, aunque sean pequeños?",
            "¿Hay algo que te gustaría que tu pareja entendiera mejor sobre lo que estás viviendo?",
            "¿Qué te gustaría que fuera distinto en la forma en que transitan juntos esta etapa?",
            "¿Cómo se cuidan ustedes como pareja en medio del cansancio y los cambios?",
            "Lo que estás construyendo con tu pareja también tiene valor, incluso en los días difíciles.",
            "Las parejas no siempre van al mismo ritmo. ¿Qué te gustaría que pasara entre ustedes en las próximas semanas?"
        ),

        "embarazo" to listOf(
            "Gracias por abrir este espacio. No hay una sola forma correcta de vivir el embarazo, y lo que sientes merece ser escuchado.",
            "El embarazo trae consigo muchas historias y expectativas. ¿Cómo ha estado llegando esta experiencia a tu día a día?",
            "A veces esta etapa viene acompañada de voces o exigencias externas. ¿Qué aspecto de todo esto te está ocupando más espacio hoy?",
            "Cada día en este proceso es distinto. Si tuvieras que ponerle un nombre a cómo te has sentido últimamente, ¿cuál sería?",
            "Hay muchas ideas sobre cómo 'debería' vivirse el embarazo. ¿Qué tanto esas ideas coinciden con lo que tú verdaderamente estás sintiendo?",
            "Cuando miras este camino que estás recorriendo, ¿qué es lo que más te hace sentido a ti, más allá de lo que digan los demás?",
            "Lo que estás viviendo es tuyo, y merece ser transitado a tu propio ritmo."
        ),

        "ambivalencia embarazo" to listOf(
            "Puedes sentir dos cosas opuestas a la vez. Eso no te hace mala madre ni mala persona. Gracias por confiarme algo tan delicado.",
            "Gracias por nombrar algo de lo que muy poco se habla abiertamente. Cuando la duda o la incertidumbre te visitan, ¿qué suelen decirte?",
            "A veces la sociedad nos dice que solo deberíamos sentir una sola cosa, pero la realidad es más compleja. ¿Cómo conviven esas emociones contradictorias en ti?",
            "Si pudieras hablar con esa duda sin juzgarte, ¿qué crees que intenta proteger o señalar en este momento?",
            "La ambivalencia suele aparecer cuando nos enfrentamos a cambios grandes. ¿Qué espacio necesita hoy tu historia para sentirse escuchada?",
            "No hay una sola forma 'correcta' de vivir esto. ¿Qué es lo que más te ayudaría a transitar estas sensaciones sin prisa?",
            "La duda no te define. Solo muestra que estás atravesando algo grande, y eso toma tiempo."
        ),

        "miedo al parto" to listOf(
            "Ese miedo no te hace débil. Habla del amor enorme que ya sientes por tu bebé y por tu propia vida.",
            "El Miedo al parto suele alimentarse de relatos ajenos o de lo desconocido. ¿Cómo se manifiesta ese miedo cuando aparece en tu día?",
            "Cuando piensas en el día del parto, ¿qué es lo que el miedo intenta convencerte que va a pasar?",
            "A veces el Miedo al parto quiere tomar todo el control. ¿Ha habido algún momento, por pequeño que sea, donde sientas un poco de calma o confianza?",
            "Nombrar el temor a morir o a que algo salga mal requiere mucha valentía. Cuando ese pensamiento tan pesado aparece, ¿con qué frecuencia te visita?",
            "Ese temor habla del valor inmenso que le das a tu vida y a la de tu bebé. ¿De qué manera ese cuidado busca protegerte hoy?",
            "Cuando los pensamientos más oscuros sobre el parto se vuelven intensos, ¿qué o quién te ayuda a sostener los pies en la tierra?",
            "Tu cuerpo sabe atravesar este proceso. Y no tienes que hacerlo sola."
        ),

        "miedo por el bebé" to listOf(
            "Estar alerta no es lo mismo que estar en peligro. Tu cuidado es real y tu bebé está bien ahora mismo.",
            "La preocupación por el bienestar del bebé a veces se vuelve una voz muy alta. ¿En qué momentos del día esa voz se hace más fuerte?",
            "Esa atención constante habla del profundo cuidado que ya le tienes. ¿Cómo logras diferenciar entre tu intuición y lo que el miedo te dicta?",
            "A pesar de esa preocupación constante, ¿qué pequeñas cosas haces en el día a día que te recuerdan que estás cuidándolo/a de la mejor manera que puedes?",
            "No todos los pensamientos de miedo son predicciones. A veces solo son señales de cuánto amas."
        ),

        "cambios corporales" to listOf(
            "Tu cuerpo está haciendo algo extraordinario. No tienes que amarlo todo el tiempo para respetarlo.",
            "Las transformaciones del cuerpo a veces traen miradas o exigencias estéticas muy duras. ¿Cómo te sientes tú con esos cambios, más allá de lo que la sociedad espera?",
            "Cuando miras los cambios en tu cuerpo hoy, ¿sientes que es una mirada tuya o la de alguien más la que se vuelve difícil de sostener?",
            "A veces toma tiempo familiarizarse con un cuerpo en transformación. ¿Qué conversación te gustaría tener con tu cuerpo hoy?",
            "Las marcas o cambios son testigos de la historia que estás viviendo. ¿Qué dirían si hablaran de todo lo que has atravesado hasta ahora?",
            "Hay días en que es difícil reconocerse al espejo. ¿Qué gesto pequeño de respeto o autocuidado sientes que tu cuerpo agradecería hoy?",
            "Tu cuerpo no está arruinado. Está atravesando una historia que también te pertenece."
        ),

        "culpa materna" to listOf(
            "Gracias por confiarme esto. Lo que sientes es más común de lo que se habla, y no te hace mala madre. La culpa materna suele alimentarse de estándares imposibles de perfección.",
            "Cuando esa culpa te visita, ¿de qué te está acusando exactamente?",
            "Si pudiéramos poner a la Culpa Materna frente a ti como un personaje, ¿de dónde crees que aprendió las reglas que hoy te exige cumplir?",
            "La culpa quiere hacerte creer que no eres suficiente. ¿Qué responderías si le recordaras las cosas que sí estás sosteniendo cada día?",
            "A veces la culpa habla con la voz de la sociedad, de la familia o de las redes sociales. ¿Esa voz es realmente tuya o la aprendiste en algún lugar?",
            "Mientras la culpa te acusa, ¿qué momentos de conexión con tu bebé recuerdas que ella no te deja ver?",
            "¿Qué le dirías a otra madre que amas si estuviera siendo acusada por la misma culpa que te visita a ti?",
            "La culpa no dice la verdad sobre ti. Dice la verdad sobre las expectativas que te enseñaron a cargar.",
            "Una madre suficiente no es la que nunca falla, sino la que se queda, se corrige y sigue presente."
        ),

        "soledad" to listOf(
            "Estar sola no significa que no importes. Este espacio es tuyo y aquí no tienes que sostenerlo todo.",
            "La Soledad puede sentirse como un peso transparente que los demás no ven. ¿En qué momentos del día se siente más presente o pesada?",
            "Cuando la Soledad se hace fuerte, ¿qué intenta convencerte sobre ti misma o sobre los demás?",
            "Incluso cuando nos sentimos aisladas, a veces mantenemos pequeños refugios o conexiones. ¿Hay algún espacio o recuerdo donde sientas un respiro de esa soledad?",
            "Qué importante es que te des la oportunidad de nombrar esto aquí. ¿Qué tipo de compañía o presencia crees que le haría bien a tu historia hoy?",
            "Mereces una red que te sostenga. Mientras llega, aquí tienes un lugar."
        ),

        "cansancio extremo" to listOf(
            "El agotamiento no es un fallo personal. Es la señal de que llevas demasiado tiempo dando sin recibir pausa.",
            "El Cansancio no solo se acumula en el cuerpo, también en la mente. ¿Qué área de tu vida siente que está cargando con el peso más grande en este momento?",
            "A veces el Cansancio nos invita a parar, pero la exigencia externa nos dice que no se puede. ¿Qué respuesta le darías a esa exigencia si decidieras hacer una pausa?",
            "No tienes que justificar ni explicar cómo te sientes. Si pudieras soltar una sola responsabilidad por el resto del día, ¿cuál elegirías?",
            "Descansar no es un premio. Es una necesidad que también mereces."
        ),

        "lactancia" to listOf(
            "Alimentar a tu bebé con amor, en cualquier forma, es un acto de cuidado completo.",
            "Alrededor de la lactancia existen muchísimos mitos y presiones sociales. ¿Qué expectativas sientes que están pesando más sobre ti en este tema?",
            "Cuando surgen dificultades con la amamantada, ¿qué historia te estás contando sobre tu papel en este proceso?",
            "La forma en que alimentas y cuidas a tu bebé no define el amor que hay. ¿Qué decisiones sobre la lactancia te darían más tranquilidad a ti?",
            "Tu bienestar también alimenta a tu bebé."
        ),

        "vinculo con el bebé" to listOf(
            "El vínculo se construye en lo cotidiano, no en la perfección. Estar presente ya es una forma de amar.",
            "El vínculo no es algo mágico e inmediato como a veces nos venden; es una relación que se construye poco a poco. ¿Cómo ha sido para ti ir conociéndolo/a?",
            "Si dejamos de lado la idea de la 'conexión perfecta', ¿qué pequeños momentos compartidos de cotidianeidad o calma han tenido últimamente?",
            "A veces la presión por 'sentir algo específico' bloquea la espontaneidad. ¿Qué pasaría si te das permiso de ir a tu propio ritmo en esta relación?",
            "No tienes que sentir todo perfecto para estar construyendo un vínculo real."
        ),

        "identidad postparto" to listOf(
            "Puedes ser madre y seguir siendo tú. La maternidad suma, no borra lo que ya eras.",
            "La llegada de la maternidad a veces reescribe por completo quiénes somos. ¿Qué partes de la mujer que eras antes sientes que extrañas o deseas reclamar?",
            "Parece que estás navegando entre quién eras y la versión que se está construyendo ahora. ¿Cómo te gustaría que convivieran esas dos facetas?",
            "Ser madre es un capítulo en tu libro, pero no es la única página. ¿Qué proyectos, gustos o espacios tuyos personales te gustaría volver a cultivar?",
            "No tienes que elegir entre ser tú y ser madre. Puedes ser ambas cosas."
        )
    )

    private val VALIDATION_RESPONSES = listOf(
        "Te escucho con absoluta atención. Puedes seguir a tu ritmo.",
        "Aquí hay un espacio seguro para tu historia. No hay prisa.",
        "Te acompaño en esto. Tómate el tiempo que necesites para expresarlo.",
        "Tus palabras son importantes aquí. Te escucho.",
        "No hay respuestas correctas ni incorrectas en este espacio.",
        "Gracias por compartir esto. Estoy aquí para leerte.",
        "Lo que traes merece ser escuchado sin juicio. Aquí tienes espacio."
    )

    private val topicDetector = TopicDetector()

    private data class RespuestaInterna(
        val responseId: String?,
        val text: String,
        val family: String?,
        val question: String?,
        val module: String?,
        val usedParts: List<String>,
        val shouldCreateAlertHint: Boolean,
        val tema: String? = null,
        val esValidacion: Boolean = false,
        val profundidad: Int = 0
    )

    // ============================================================
    // PUNTO DE ENTRADA PÚBLICO
    // ============================================================

    fun responder(mensaje: String, nombreUsuario: String, memoria: MemoriaOffline): RespuestaOffline {
        val classification = OfflineClassifier.clasificar(mensaje)
        val originalCategory = classification.category

        // ============================================================
        // PASO 0: MANTENER PROTOCOLO DE RIESGO ALTO (con salida)
        // ============================================================
        if (memoria.lastRiskLevel == "alto" && memoria.lastCategory == "riesgo_alto") {

            // ¿La usuaria está saliendo de la crisis?
            val saliendoDeCrisis = originalCategory in listOf(
                "bienestar", "cierre", "saludo"
            )
            val esMensajeNeutro = originalCategory == "respuesta_corta" ||
                    originalCategory == "desconocido"

            val seQuedaEnProtocolo = !saliendoDeCrisis &&
                    memoria.turnosEnRiesgoAlto < 3

            if (seQuedaEnProtocolo) {
                val respuesta = SAFETY_RESPONSES["riesgo_alto"]!!.random()
                memoria.turnosEnRiesgoAlto++
                memoria.actualizar(
                    category = "riesgo_alto",
                    originalCategory = originalCategory,
                    emotion = "desesperanza",
                    riskLevel = "alto",
                    intensity = "alta",
                    responseId = "safety_alto_followup",
                    question = null,
                    responseText = respuesta,
                    usedParts = listOf(respuesta),
                    newTopics = listOf("riesgo_alto")
                )
                return RespuestaOffline(
                    bloques = listOf(respuesta),
                    categoria = "riesgo_alto",
                    modulo = "seguridad",
                    requiereAlerta = true,
                    tema = null,
                    esValidacion = true,
                    profundidad = 0
                )
            } else {
                // Salimos del protocolo.
                memoria.turnosEnRiesgoAlto = 0
                memoria.lastRiskLevel = "bajo"
                memoria.lastCategory = null

                // Si solo dijo algo neutro tras la crisis, responder con
                // validación suave, no con el protocolo completo.
                if (esMensajeNeutro && !saliendoDeCrisis) {
                    val respuesta = "Gracias por quedarte conmigo en este momento. " +
                            "Estoy aquí, sin prisa. Si quieres, podemos respirar juntas un momento, " +
                            "o simplemente quedarnos en silencio."
                    memoria.actualizar(
                        category = "desconocido",
                        originalCategory = originalCategory,
                        emotion = "neutral",
                        riskLevel = "bajo",
                        intensity = "baja",
                        responseId = "post_crisis_neutral",
                        question = null,
                        responseText = respuesta,
                        usedParts = listOf(respuesta),
                        newTopics = emptyList()
                    )
                    return RespuestaOffline(
                        bloques = listOf(respuesta),
                        categoria = "desconocido",
                        modulo = "estado_emocional",
                        requiereAlerta = false,
                        tema = null,
                        esValidacion = true,
                        profundidad = 0
                    )
                }
                // Si dijo bienestar / cierre / saludo, cae al flujo normal.
            }
        }

        // ============================================================
        // PASO 1: SEGURIDAD - PRIORIDAD MÁXIMA
        // ============================================================
        if (classification.riskLevel == "alto") {
            memoria.turnosEnRiesgoAlto = 0
            val respuesta = SAFETY_RESPONSES["riesgo_alto"]!!.random()
            memoria.actualizar(
                category = "riesgo_alto",
                originalCategory = originalCategory,
                emotion = "desesperanza",
                riskLevel = "alto",
                intensity = "alta",
                responseId = "safety_alto",
                question = null,
                responseText = respuesta,
                usedParts = listOf(respuesta),
                newTopics = listOf("riesgo_alto")
            )
            return RespuestaOffline(
                bloques = listOf(respuesta),
                categoria = "riesgo_alto",
                modulo = "seguridad",
                requiereAlerta = true,
                tema = null,
                esValidacion = true,
                profundidad = 0
            )
        }

        if (classification.riskLevel == "medio") {
            val respuesta = SAFETY_RESPONSES["riesgo_medio"]!!.random()
            memoria.actualizar(
                category = "riesgo_medio",
                originalCategory = originalCategory,
                emotion = classification.emotion,
                riskLevel = "medio",
                intensity = classification.intensity,
                responseId = "safety_medio",
                question = null,
                responseText = respuesta,
                usedParts = listOf(respuesta),
                newTopics = listOf("riesgo_medio")
            )
            return RespuestaOffline(
                bloques = listOf(respuesta),
                categoria = "riesgo_medio",
                modulo = "seguridad",
                requiereAlerta = true,
                tema = null,
                esValidacion = true,
                profundidad = 0
            )
        }

        // ============================================================
        // PASO 2: DETECCIÓN DE TEMAS
        // ============================================================
        val topicResult = if (originalCategory == "saludo" || originalCategory == "cierre") {
            null
        } else {
            topicDetector.detectarTema(mensaje, classification, memoria)
        }

        if ((originalCategory == "respuesta_corta" || originalCategory == "desconocido") &&
            memoria.tieneTemaActivo() &&
            topicResult == null
        ) {
            memoria.actualizarTema(mensaje)
            return responderConTemaActivo(mensaje, nombreUsuario, memoria, classification)
        }

        if (topicResult != null) {
            if (!memoria.tieneTemaActivo() || memoria.getTema() != topicResult.tema) {
                memoria.iniciarTema(topicResult.tema, topicResult.emoción)
            }
            memoria.actualizarTema(mensaje)

            val respuesta = if (memoria.respuestasEnEsteTema <= 1) {
                responderConTemaNuevo(topicResult, mensaje, nombreUsuario, memoria)
            } else {
                responderConTemaActivo(mensaje, nombreUsuario, memoria, classification)
            }

            memoria.actualizar(
                category = "tema_${topicResult.tema}",
                originalCategory = originalCategory,
                emotion = topicResult.emoción,
                riskLevel = classification.riskLevel,
                intensity = classification.intensity,
                responseId = null,
                question = null,
                responseText = respuesta.bloques.joinToString(" "),
                usedParts = emptyList(),
                newTopics = listOf(topicResult.tema),
                lastStyle = if (respuesta.esValidacion) "afirmacion_validante" else "pregunta_directa"
            )

            return respuesta
        }

        // ============================================================
        // PASO 3: FALLBACK - SIN RIESGO Y SIN TEMA
        // ============================================================
        var category = resolveContextualCategory(originalCategory, memoria.lastCategory)
        category = resolveMultitopicCategory(category, classification.secondaryCategories)

        val respuesta = buildResponseData(
            mensaje = mensaje,
            category = category,
            classification = classification,
            userName = nombreUsuario,
            usedResponseIds = memoria.usedResponseIds,
            usedQuestions = memoria.usedQuestions,
            usedResponseParts = memoria.usedResponseParts,
            lastStyle = memoria.lastStyle,
            lastCategory = memoria.lastCategory,
            consecutiveQuestions = memoria.consecutiveQuestions,
        )

        val moduleId = respuesta.module ?: CATEGORY_TO_MODULE[category]

        val finalEmotion = contextualValue(category, classification.emotion, memoria.lastEmotion)
        val finalRisk = contextualValue(category, classification.riskLevel, memoria.lastRiskLevel)
        val finalIntensity = contextualValue(category, classification.intensity, memoria.lastIntensity)

        memoria.actualizar(
            category = category,
            originalCategory = originalCategory,
            emotion = finalEmotion,
            riskLevel = finalRisk,
            intensity = finalIntensity,
            responseId = respuesta.responseId,
            question = respuesta.question,
            responseText = respuesta.text,
            usedParts = respuesta.usedParts,
            newTopics = (listOf(category) + classification.secondaryCategories).distinct(),
            lastStyle = respuesta.family ?: "unknown",
        )

        val alert = classification.shouldCreateAlert ||
                category in ALERT_CATEGORIES ||
                respuesta.shouldCreateAlertHint

        return RespuestaOffline(
            bloques = aBloquesInterno(respuesta.text),
            categoria = category,
            modulo = moduleId,
            requiereAlerta = alert,
            tema = memoria.getTema(),
            esValidacion = respuesta.esValidacion,
            profundidad = memoria.getProfundidad()
        )
    }

    // ============================================================
    // RESPUESTAS POR TEMA
    // ============================================================

    private fun responderConTemaNuevo(
        topicResult: TopicDetectionResult,
        mensaje: String,
        nombreUsuario: String,
        memoria: MemoriaOffline
    ): RespuestaOffline {
        val respuestas = RESPONSES_BY_TOPIC[topicResult.tema]

        return if (respuestas != null) {
            val usadas = memoria.obtenerRespuestasUsadasEnTema(topicResult.tema)

            val indiceSiguiente = usadas.size
            val seleccionada = when {
                indiceSiguiente < respuestas.size -> respuestas[indiceSiguiente]
                else -> respuestas.lastOrNull() ?: VALIDATION_RESPONSES.random()
            }

            memoria.guardarRespuestaUsada(topicResult.tema, seleccionada)

            val esPregunta = seleccionada.contains("?") || seleccionada.contains("¿")
            if (esPregunta) {
                memoria.registrarPregunta(seleccionada)
            } else {
                memoria.marcarRespuestaRecibida()
            }

            RespuestaOffline(
                bloques = listOf(seleccionada),
                categoria = "tema_${topicResult.tema}",
                modulo = "estado_emocional",
                requiereAlerta = false,
                tema = topicResult.tema,
                esValidacion = !esPregunta,
                profundidad = memoria.getProfundidad()
            )
        } else {
            val preguntas = topicDetector.preguntasPara(topicResult.tema)
            val usadas = memoria.obtenerRespuestasUsadasEnTema(topicResult.tema)
            val disponibles = preguntas?.filter { !usadas.contains(it) }

            val respuesta = when {
                !disponibles.isNullOrEmpty() -> disponibles.first()
                !preguntas.isNullOrEmpty() -> preguntas.first()
                else -> VALIDATION_RESPONSES.random()
            }
            memoria.guardarRespuestaUsada(topicResult.tema, respuesta)

            val esPregunta = respuesta.contains("?") || respuesta.contains("¿")
            if (esPregunta) memoria.registrarPregunta(respuesta)
            else memoria.marcarRespuestaRecibida()

            RespuestaOffline(
                bloques = listOf(respuesta),
                categoria = "tema_${topicResult.tema}",
                modulo = "estado_emocional",
                requiereAlerta = false,
                tema = topicResult.tema,
                esValidacion = !esPregunta,
                profundidad = memoria.getProfundidad()
            )
        }
    }

    private fun responderConTemaActivo(
        mensaje: String,
        nombreUsuario: String,
        memoria: MemoriaOffline,
        classification: ClasificacionOffline
    ): RespuestaOffline {
        val tema = memoria.getTema()!!
        val respuestas = RESPONSES_BY_TOPIC[tema]
        val profundidad = memoria.getProfundidad()

        if (respuestas != null) {
            val usadas = memoria.obtenerRespuestasUsadasEnTema(tema)
            val forzarAfirmacion = memoria.preguntasConsecutivas >= 2
            val indiceSiguiente = usadas.size

            val seleccionada = when {
                forzarAfirmacion -> respuestas.firstOrNull {
                    !usadas.contains(it) &&
                            !it.contains("?") && !it.contains("¿")
                } ?: VALIDATION_RESPONSES.random()

                indiceSiguiente < respuestas.size &&
                        !usadas.contains(respuestas[indiceSiguiente]) ->
                    respuestas[indiceSiguiente]

                else -> respuestas.firstOrNull { !usadas.contains(it) }
                    ?: VALIDATION_RESPONSES.random()
            }

            memoria.guardarRespuestaUsada(tema, seleccionada)

            val esPregunta = seleccionada.contains("?") || seleccionada.contains("¿")
            if (esPregunta) {
                memoria.registrarPregunta(seleccionada)
            } else {
                memoria.marcarRespuestaRecibida()
            }

            return RespuestaOffline(
                bloques = listOf(seleccionada),
                categoria = "tema_$tema",
                modulo = "estado_emocional",
                requiereAlerta = false,
                tema = tema,
                esValidacion = !esPregunta,
                profundidad = profundidad
            )
        }

        val preguntas = topicDetector.preguntasPara(tema)
        val usadas = memoria.obtenerRespuestasUsadasEnTema(tema)
        val disponibles = preguntas?.filter { !usadas.contains(it) }
        val forzarAfirmacion = memoria.preguntasConsecutivas >= 2

        val respuesta = when {
            forzarAfirmacion -> VALIDATION_RESPONSES.random()
            !disponibles.isNullOrEmpty() -> disponibles.first()
            !preguntas.isNullOrEmpty() -> preguntas.first()
            else -> VALIDATION_RESPONSES.random()
        }
        memoria.guardarRespuestaUsada(tema, respuesta)
        val esPregunta = respuesta.contains("?") || respuesta.contains("¿")
        if (esPregunta) memoria.registrarPregunta(respuesta)
        else memoria.marcarRespuestaRecibida()

        return RespuestaOffline(
            bloques = listOf(respuesta),
            categoria = "tema_$tema",
            modulo = "estado_emocional",
            requiereAlerta = false,
            tema = tema,
            esValidacion = !esPregunta,
            profundidad = profundidad
        )
    }

    // ============================================================
    // MÉTODOS EXISTENTES
    // ============================================================

    private fun aBloquesInterno(texto: String): List<String> {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return emptyList()
        val bloques = limpio.split(Regex("\\n\\s*\\n")).map { it.trim() }.filter { it.isNotBlank() }
        return if (bloques.size > 3) bloques.take(2) + bloques.drop(2).joinToString("\n\n") else bloques
    }

    private fun contextualValue(category: String, currentValue: String, lastValue: String?): String {
        if (category in FOLLOWUP_LIKE_CATEGORIES && !lastValue.isNullOrEmpty()) return lastValue
        return currentValue
    }

    private fun resolveContextualCategory(originalCategory: String, lastCategory: String?): String {
        if (lastCategory == "cierre") return "cierre"
        if (originalCategory !in CONTEXTUAL_CATEGORIES) return originalCategory

        return when (lastCategory) {
            "ambivalencia_embarazo" -> "ambivalencia_followup"
            "ambivalencia_followup" -> "ambivalencia_followup"
            "sintoma_fisico" -> "sintoma_fisico_followup"
            "sintoma_fisico_followup" -> "sintoma_fisico_followup"
            "malestar", "tristeza", "ansiedad", "frustracion", "confusion" ->
                if (originalCategory == "pregunta_por_que") "pregunta_por_que" else "pedir_mas_informacion"
            else -> originalCategory
        }
    }

    private fun resolveMultitopicCategory(category: String, secondary: List<String>): String {
        if (category == "ambivalencia_embarazo") {
            if ("sintoma_fisico" in secondary) return "multitema_ambivalencia_sintoma_ansiedad"
            if ("ansiedad" in secondary) return "multitema_ambivalencia_ansiedad"
            return "ambivalencia_embarazo"
        }
        if (category == "sintoma_fisico" && "ansiedad" in secondary) return "multitema_sintoma_ansiedad"
        if (category == "tristeza" && "ansiedad" in secondary) return "multitema_tristeza_ansiedad"
        if (category == "ansiedad" && "tristeza" in secondary) return "multitema_tristeza_ansiedad"
        return category
    }

    private fun buildResponseData(
        mensaje: String,
        category: String,
        classification: ClasificacionOffline,
        userName: String,
        usedResponseIds: List<String>,
        usedQuestions: List<String>,
        usedResponseParts: List<String>,
        lastStyle: String?,
        lastCategory: String?,
        consecutiveQuestions: Int = 0,
    ): RespuestaInterna {
        if (category in STRUCTURED_ONLY_CATEGORIES || category == "saludo" || category == "cierre") {
            val selected = selectResponseWithoutRepetition(
                mensaje = mensaje,
                category = category,
                userName = userName,
                usedResponseIds = usedResponseIds,
                usedQuestions = usedQuestions,
                usedResponseParts = usedResponseParts,
                memoria = null
            )
            return RespuestaInterna(
                responseId = selected.id,
                text = selected.text,
                family = selected.family,
                question = selected.question,
                module = selected.module ?: CATEGORY_TO_MODULE[category],
                usedParts = emptyList(),
                shouldCreateAlertHint = category in ALERT_CATEGORIES || category == "sintoma_fisico",
                esValidacion = selected.family?.contains("validacion") == true
            )
        }

        val composed = composeHumanizedResponse(
            mensaje = mensaje,
            category = category,
            riskLevel = classification.riskLevel,
            intensity = classification.intensity,
            usedQuestions = usedQuestions,
            usedResponseParts = usedResponseParts,
            lastStyle = lastStyle,
            lastCategory = lastCategory,
            consecutiveQuestions = consecutiveQuestions,
        )

        return RespuestaInterna(
            responseId = composed.responseId,
            text = composed.text,
            family = composed.family,
            question = composed.question,
            module = CATEGORY_TO_MODULE[category],
            usedParts = composed.usedParts,
            shouldCreateAlertHint = composed.shouldCreateAlert,
            esValidacion = composed.family?.contains("validacion") == true
        )
    }

    // ============================================================
    // MÉTODOS DE SELECCIÓN DE RESPUESTAS
    // ============================================================

    private fun normalizeBankCategory(category: String?): String {
        if (category.isNullOrEmpty()) return "desconocido"
        return CATEGORY_ALIASES[category] ?: category
    }

    private fun getAllResponsesForCategory(category: String): List<BankItem> {
        val normalized = normalizeBankCategory(category)
        return STRUCTURED_RESPONSES[normalized] ?: STRUCTURED_RESPONSES.getValue("desconocido")
    }

    // ============================================================
    // DETECCIÓN DE TAGS
    // ============================================================

    private val tagPatternCache = mutableMapOf<String, Regex>()

    private fun normalizarTexto(texto: String): String {
        val nfd = java.text.Normalizer.normalize(texto.lowercase(), java.text.Normalizer.Form.NFD)
        return nfd.replace(Regex("\\p{Mn}+"), "")
    }

    private fun patronParaFrase(frase: String): Regex {
        val normalizada = normalizarTexto(frase)
        return tagPatternCache.getOrPut(normalizada) {
            Regex("(?<![a-z0-9])" + Regex.escape(normalizada) + "(?![a-z0-9])")
        }
    }

    private fun detectTags(mensaje: String): Set<String> {
        val normalizado = normalizarTexto(mensaje)
        return TAG_KEYWORDS.filterValues { frases ->
            frases.any { frase -> patronParaFrase(frase).containsMatchIn(normalizado) }
        }.keys
    }

    private fun filtrarPorTagsPrioritarios(items: List<BankItem>, mensaje: String): List<BankItem> {
        val tags = detectTags(mensaje)
        val conMatch = items.filter {
            it.requiredTags.isNotEmpty() && it.requiredTags.all { tag -> tag in tags }
        }
        return conMatch.ifEmpty {
            items.filter { it.requiredTags.isEmpty() }
        }.ifEmpty { items }
    }

    private fun formatUserName(userName: String): String {
        val name = userName.trim()
        val ignored = setOf("", "usuaria", "usuario", "user", "test", "none", "null")
        if (name.lowercase() in ignored) return ""
        return " $name"
    }

    private data class SelectedBankResponse(
        val id: String?,
        val text: String,
        val family: String?,
        val question: String?,
        val module: String?,
    )

    private val staticCycles = mutableMapOf<String, MutableList<BankItem>>()
    private val staticIndex = mutableMapOf<String, Int>()

    internal fun resetCacheForTests() {
        staticCycles.clear()
        staticIndex.clear()
    }

    private fun selectResponseWithoutRepetition(
        mensaje: String,
        category: String,
        userName: String,
        usedResponseIds: List<String>,
        usedQuestions: List<String>,
        usedResponseParts: List<String>,
        memoria: MemoriaOffline? = null
    ): SelectedBankResponse {

        val normalized = normalizeBankCategory(category)

        val responsesAll = getAllResponsesForCategory(normalized)
        val responses = filtrarPorTagsPrioritarios(responsesAll, mensaje)

        val available = responses.filter { item ->
            item.id !in usedResponseIds &&
                    (item.question == null || item.question !in usedQuestions) &&
                    item.id !in usedResponseParts
        }
        val availableResponses = available.ifEmpty { responses }

        val cycles: MutableMap<String, MutableList<BankItem>>
        val index: MutableMap<String, Int>

        if (memoria != null) {
            cycles = memoria.responseCycles
            index = memoria.responseCycleIndex
        } else {
            cycles = staticCycles
            index = staticIndex
        }

        if (!cycles.containsKey(normalized) || cycles.getValue(normalized).isEmpty()) {
            cycles[normalized] = availableResponses.toMutableList()
            index[normalized] = 0
        } else {
            val actual = cycles.getValue(normalized)
            val idsActuales = actual.map { it.id }.toSet()
            val idsNuevos = availableResponses.map { it.id }.toSet()
            if (idsActuales != idsNuevos) {
                cycles[normalized] = availableResponses.toMutableList()
                index[normalized] = 0
            }
        }

        val cycleList = cycles.getValue(normalized)
        var idx = index[normalized] ?: 0
        if (idx >= cycleList.size) idx = 0
        val selected = cycleList[idx]
        index[normalized] = idx + 1

        var text = selected.text.replace("{name}", formatUserName(userName))
        text = text.split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ").trim()

        return SelectedBankResponse(
            id = selected.id,
            text = text,
            family = selected.family,
            question = selected.question,
            module = selected.module,
        )
    }

    private data class ComposedResponse(
        val responseId: String?,
        val text: String,
        val family: String?,
        val question: String?,
        val usedParts: List<String>,
        val shouldCreateAlert: Boolean,
    )

    private fun composerNormalizeCategory(category: String?): String {
        if (category.isNullOrEmpty()) return "desconocido"
        val map = mapOf(
            "desconocido" to "desconocido",
            "respuesta_corta" to "respuesta_corta",
            "pedir_mas_informacion" to "pedir_mas_informacion",
            "pregunta_por_que" to "pregunta_por_que",
            "estado_emocional" to "malestar",
            "sintoma_fisico_followup" to "sintoma_fisico",
            "ambivalencia_followup" to "ambivalencia_embarazo",
            "multitema_ambivalencia_ansiedad" to "ambivalencia_embarazo",
            "multitema_ambivalencia_sintoma_ansiedad" to "sintoma_fisico",
            "multitema_sintoma_ansiedad" to "sintoma_fisico",
            "multitema_tristeza_ansiedad" to "ansiedad",
        )
        return map[category] ?: category
    }

    private fun extractQuestion(text: String): String? {
        if ("¿" !in text) return null
        val start = text.lastIndexOf('¿')
        return text.substring(start).trim()
    }

    private fun determineStyle(
        category: String,
        usedQuestions: List<String>,
        lastStyle: String?,
        lastCategory: String?,
        intensity: String,
        riskLevel: String
    ): String {
        if (riskLevel == "alto" || riskLevel == "medio") {
            return "afirmacion_validante"
        }

        if (intensity == "alta") {
            return if (Random.nextBoolean()) "afirmacion_validante" else "invitacion_suave"
        }

        val recentQuestions = usedQuestions.takeLast(3)

        if (recentQuestions.size >= 2) {
            return if (Random.nextBoolean()) "invitacion_suave" else "afirmacion_validante"
        }

        if (lastStyle?.contains("pregunta") == true || lastStyle?.contains("question") == true) {
            return if (Random.nextBoolean()) "invitacion_suave" else "afirmacion_validante"
        }

        if (lastStyle?.contains("invitacion") == true) {
            return if (Random.nextBoolean()) "pregunta_directa" else "afirmacion_validante"
        }

        if (lastStyle?.contains("afirmacion") == true) {
            return if (Random.nextBoolean()) "pregunta_directa" else "invitacion_suave"
        }

        if (usedQuestions.isEmpty()) {
            return "pregunta_directa"
        }

        return when (Random.nextInt(3)) {
            0 -> "pregunta_directa"
            1 -> "invitacion_suave"
            else -> "afirmacion_validante"
        }
    }

    private fun composeHumanizedResponse(
        mensaje: String,
        category: String,
        riskLevel: String,
        intensity: String,
        usedQuestions: List<String>,
        usedResponseParts: List<String>,
        lastStyle: String?,
        lastCategory: String?,
        consecutiveQuestions: Int = 0,
    ): ComposedResponse {
        if (riskLevel == "alto") return composeSafetyResponse("riesgo_alto")
        if (category == "ambivalencia_embarazo") return composeSensitiveResponse(mensaje, category, usedQuestions, usedResponseParts)
        if (riskLevel == "medio" || category == "riesgo_medio") return composeSafetyResponse("riesgo_medio")
        if (category == "sintoma_fisico") return composeSafetyResponseWithTags(mensaje, "sintoma_fisico", usedQuestions, usedResponseParts)
        if (category == "saludo") return composeDirectResponse("saludo")
        if (category == "cierre") return composeDirectResponse("cierre")

        var normalizedCategory = composerNormalizeCategory(category)
        if (normalizedCategory !in STRUCTURED_RESPONSES.keys) normalizedCategory = "desconocido"

        val targetFamily = if (consecutiveQuestions >= 2) {
            if (Random.nextBoolean()) "afirmacion_validante" else "reflexion"
        } else {
            determineStyle(
                category = normalizedCategory,
                usedQuestions = usedQuestions,
                lastStyle = lastStyle,
                lastCategory = lastCategory,
                intensity = intensity,
                riskLevel = riskLevel
            )
        }

        val bankResponsesAll = STRUCTURED_RESPONSES[normalizedCategory] ?: STRUCTURED_RESPONSES.getValue("desconocido")
        val bankResponses = filtrarPorTagsPrioritarios(bankResponsesAll, mensaje)

        val porEstilo = bankResponses.filter { it.family == targetFamily }
        val disponibles = porEstilo.filter {
            it.id !in usedResponseParts && (it.question == null || it.question !in usedQuestions)
        }

        val sinPregunta = bankResponses.filter {
            it.question == null && it.id !in usedResponseParts
        }

        val pool = if (consecutiveQuestions >= 2) {
            when {
                disponibles.isNotEmpty() -> disponibles
                porEstilo.isNotEmpty() -> porEstilo
                sinPregunta.isNotEmpty() -> sinPregunta
                else -> bankResponses.filter { it.question == null }.ifEmpty { bankResponses }
            }
        } else {
            disponibles.ifEmpty { porEstilo.ifEmpty { bankResponses } }
        }

        val selected = pool[Random.nextInt(pool.size)]
        val text = selected.text.replace("{name}", "")

        return ComposedResponse(
            responseId = selected.id,
            text = text,
            family = selected.family,
            question = selected.question,
            usedParts = listOf(selected.id),
            shouldCreateAlert = false
        )
    }

    private fun composeDirectResponse(category: String): ComposedResponse {
        val options = DIRECT_RESPONSES.getValue(category)
        val response = options[Random.nextInt(options.size)]
        return ComposedResponse(
            responseId = "direct_$category",
            text = response,
            family = "direct_local",
            question = extractQuestion(response),
            usedParts = listOf(response),
            shouldCreateAlert = false,
        )
    }

    private fun composeSafetyResponse(category: String): ComposedResponse {
        val options = SAFETY_RESPONSES[category] ?: SAFETY_RESPONSES.getValue("riesgo_medio")
        val response = options[Random.nextInt(options.size)]
        return ComposedResponse(
            responseId = "safety_$category",
            text = response,
            family = "safety_local",
            question = extractQuestion(response),
            usedParts = listOf(response),
            shouldCreateAlert = true,
        )
    }

    private fun composeSafetyResponseWithTags(
        mensaje: String,
        category: String,
        usedQuestions: List<String>,
        usedResponseParts: List<String>,
    ): ComposedResponse {
        val bankResponsesAll = STRUCTURED_RESPONSES[category] ?: STRUCTURED_RESPONSES.getValue("desconocido")
        val filtradas = filtrarPorTagsPrioritarios(bankResponsesAll, mensaje)
        val disponibles = filtradas.filter {
            it.id !in usedResponseParts && (it.question == null || it.question !in usedQuestions)
        }
        val pool = disponibles.ifEmpty { filtradas }
        val selected = pool[Random.nextInt(pool.size)]

        return ComposedResponse(
            responseId = selected.id,
            text = selected.text,
            family = selected.family,
            question = selected.question,
            usedParts = listOf(selected.id),
            shouldCreateAlert = true,
        )
    }

    private fun composeSensitiveResponse(
        mensaje: String,
        category: String,
        usedQuestions: List<String>,
        usedResponseParts: List<String>,
    ): ComposedResponse {
        val bankResponsesAll = STRUCTURED_RESPONSES[category] ?: STRUCTURED_RESPONSES.getValue("desconocido")
        val filtradas = filtrarPorTagsPrioritarios(bankResponsesAll, mensaje)
        val available = filtradas.filter {
            it.id !in usedResponseParts && (it.question == null || it.question !in usedQuestions)
        }
        val pool = available.ifEmpty { filtradas }
        val selected = pool[Random.nextInt(pool.size)]

        return ComposedResponse(
            responseId = selected.id,
            text = selected.text,
            family = selected.family,
            question = selected.question,
            usedParts = listOf(selected.id),
            shouldCreateAlert = true
        )
    }
}