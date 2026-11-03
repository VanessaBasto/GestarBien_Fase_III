package com.example.gestarbien.ia.offline

class TopicDetector {

    // ============================================================
    // MAPEO DE TEMAS CON PALABRAS CLAVE Y EMOCIONES ASOCIADAS
    // ============================================================
    private val topicPatterns = mapOf(

        // ============================================================
        // EMBARAZO Y PARTO
        // ============================================================
        "embarazo" to TopicPattern(
            keywords = listOf("embarazo", "embarazada", "gestacion", "tenerlo", "tenerla", "barriga", "panza"),
            emotion = "ansiedad",
            followUpQuestions = listOf(
                "Qué interesante que quieras hablar del embarazo. ¿Qué parte de esta etapa está ocupando más espacio en ti hoy?",
                "Cada embarazo tiene su propia historia. ¿Cómo describirías la tuya hasta ahora?",
                "¿Hay algo que te gustaría que las personas de tu entorno entendieran mejor sobre lo que estás viviendo?",
                "¿Qué emociones aparecen cuando te detienes a pensar en lo que viene?",
                "¿Hay algún momento del día en que sientas más conexión con tu bebé y con esta etapa?"
            )
        ),

        "ambivalencia embarazo" to TopicPattern(
            keywords = listOf("no se si", "dudo", "no estoy segura", "arrepiento", "no queria", "no estaba lista", "no estaba preparada", "me cuesta aceptar"),
            emotion = "confusion",
            followUpQuestions = listOf(
                "Puedes sentir dos cosas opuestas a la vez. Eso no te hace mala persona. ¿Cómo conviven esas emociones en ti?",
                "Si pudieras hablar con esa duda sin juzgarte, ¿qué crees que intenta proteger o señalar?",
                "La ambivalencia suele aparecer cuando enfrentamos cambios grandes. ¿Qué espacio necesita hoy tu historia para sentirse escuchada?",
                "No hay una sola forma 'correcta' de vivir esto. ¿Qué te ayudaría a transitar estas sensaciones sin prisa?",
                "¿Hay alguien con quien puedas hablar de esto sin sentirte juzgada?",
                "La duda no te define. Solo muestra que estás atravesando algo grande, y eso toma tiempo."
            )
        ),

        "miedo al parto" to TopicPattern(
            keywords = listOf("parto", "cesarea", "dar a luz", "contracciones", "dilatacion", "hospital", "clinica"),
            emotion = "ansiedad",
            followUpQuestions = listOf(
                "Ese miedo no te hace débil. Habla del amor enorme que ya sientes por tu bebé y por tu propia vida.",
                "Cuando piensas en el día del parto, ¿qué es lo que el miedo intenta convencerte que va a pasar?",
                "A veces el miedo al parto quiere tomar todo el control. ¿Ha habido algún momento, por pequeño que sea, donde sientas un poco de calma o confianza?",
                "¿Qué necesitarías saber o tener cerca para sentirte un poco más acompañada ese día?",
                "Tu cuerpo sabe atravesar este proceso. Y no tienes que hacerlo sola."
            )
        ),

        "miedo a morir en el parto" to TopicPattern(
            keywords = listOf("morir", "muerte", "muera", "no despierto", "no despierta", "quedar huérfano", "quedar huerfano"),
            emotion = "ansiedad",
            followUpQuestions = listOf(
                "El miedo a morir en el parto es algo que muchas mujeres sienten en silencio. Gracias por tener la valentía de decirlo.",
                "Escucho el miedo en tus palabras. Dejar a tu bebé es una de las pérdidas más difíciles de imaginar.",
                "Tienes razón. No es una exageración. Es un miedo real y válido.",
                "¿Qué es lo que más te preocupa de esa posibilidad?",
                "¿Has podido hablar de este miedo con tu médico o con alguien de confianza?"
            )
        ),

        // ============================================================
        // BEBÉ Y VÍNCULO
        // ============================================================
        "miedo por el bebé" to TopicPattern(
            keywords = listOf("bebe", "bebé", "bebito", "bebita", "algo le pase", "preocupo por mi bebe", "miedo por mi bebe", "se enferme", "le pase algo"),
            emotion = "ansiedad",
            followUpQuestions = listOf(
                "Ese miedo por tu bebé es una expresión de cuánto te importa. ¿Hay algo en particular que te preocupe de su bienestar?",
                "La preocupación por el bebé es natural. ¿Qué es lo que más te ocupa la mente cuando piensas en él o ella?",
                "El amor se manifiesta a veces como preocupación. ¿Qué parte de ti confía en que todo va a estar bien?",
                "No todos los pensamientos de miedo son predicciones. A veces solo son señales de cuánto amas."
            )
        ),

        "vinculo con el bebé" to TopicPattern(
            keywords = listOf("conectar", "vinculo", "no siento conexion", "cuesta acercarme", "no siento nada", "no conecto", "no lo quiero", "no la quiero"),
            emotion = "vinculo",
            followUpQuestions = listOf(
                "El vínculo no se mide por la intensidad, sino por la presencia. ¿Qué emoción aparece cuando piensas en acercarte a tu bebé?",
                "No tienes que forzarte a sentir algo perfecto. ¿Qué crees que está haciendo más difícil conectar?",
                "El vínculo es como un jardín: necesita tiempo. ¿Qué semilla pequeña te gustaría plantar hoy?",
                "Aunque sientas distancia, hay algo en ti que ya está conectado.",
                "El vínculo no siempre es inmediato. Se construye en los pequeños momentos."
            )
        ),

        // ============================================================
        // CUERPO
        // ============================================================
        "cambios corporales" to TopicPattern(
            keywords = listOf("cuerpo", "cambios", "transformando", "no reconozco", "flacidez", "espejo", "no me gusta mi cuerpo", "no me gusta verme"),
            emotion = "malestar",
            followUpQuestions = listOf(
                "Tu cuerpo está pasando por una transformación importante. ¿Qué parte de esos cambios te resulta más difícil de aceptar?",
                "Los cambios en el cuerpo pueden ser desafiantes. ¿Qué te gustaría que alguien entendiera de lo que estás sintiendo?",
                "¿Cómo era tu relación con tu cuerpo antes del embarazo?",
                "Hay días en que es difícil reconocerse al espejo. ¿Qué gesto pequeño de respeto o autocuidado sientes que tu cuerpo agradecería hoy?",
                "Tu cuerpo no está arruinado. Está atravesando una historia que también te pertenece."
            )
        ),

        "estrías" to TopicPattern(
            keywords = listOf("estrias", "estrías", "marcas", "piel", "rayas", "cicatrices"),
            emotion = "malestar",
            followUpQuestions = listOf(
                "Las estrías son marcas de una historia. ¿Qué sientes cuando las ves?",
                "¿Qué te gustaría que alguien entendiera de cómo te sientes con tu piel?",
                "¿Cómo te gustaría sentirte con tu cuerpo a pesar de las estrías?",
                "Las estrías no te hacen fea. Te hacen real. Son parte de tu historia como madre."
            )
        ),

        "peso" to TopicPattern(
            keywords = listOf("peso", "gorda", "gordo", "flaca", "flaco", "engordar", "bajar de peso", "kilos", "kilogramos"),
            emotion = "malestar",
            followUpQuestions = listOf(
                "El peso es una de las cosas que más preocupan durante el embarazo. ¿Qué es lo que más te preocupa de tu peso?",
                "Tu cuerpo está cambiando para crear vida. ¿Cómo te sientes con esos cambios?",
                "¿Qué te gustaría que alguien entendiera de cómo te sientes con tu peso?"
            )
        ),

        // ============================================================
        // EMOCIONES
        // ============================================================
        "ansiedad general" to TopicPattern(
            keywords = listOf("ansiedad", "ansiosa", "ansioso", "nerviosa", "nervioso", "intranquila", "intranquilo", "no dejo de pensar", "mi cabeza no para"),
            emotion = "ansiedad",
            followUpQuestions = listOf(
                "La ansiedad puede hacer que todo se sienta más urgente. ¿Qué pensamiento aparece con más fuerza cuando llega la ansiedad?",
                "¿Hay algo específico que haya encendido esta ansiedad hoy?",
                "¿Qué te ha ayudado antes, aunque sea un poquito, cuando te sientes así?",
                "Tu mente puede estar acelerada, pero aquí no hay prisa."
            )
        ),

        "tristeza" to TopicPattern(
            keywords = listOf("triste", "tristeza", "vacia", "vacio", "llorar", "lloro", "sin animo", "desanimada", "desanimado", "derrotada", "derrotado"),
            emotion = "tristeza",
            followUpQuestions = listOf(
                "Siento mucho que hoy estés así. La tristeza puede hacer que todo se sienta más pesado. ¿Qué fue lo que más te dolió hoy?",
                "Sentirte triste no significa que estés fallando. ¿Qué necesitaría escuchar esa parte triste de ti?",
                "¿Qué es lo que más te está pesando ahora?",
                "Podemos mirar esta tristeza como algo que está visitándote, no como algo que eres tú."
            )
        ),

        "culpa materna" to TopicPattern(
            keywords = listOf("culpa", "fallando", "mala madre", "no soy suficiente", "fracasada", "soy mala madre", "no soy capaz", "no valgo nada"),
            emotion = "culpa",
            followUpQuestions = listOf(
                "La Culpa Materna suele alimentarse de estándares imposibles de perfección. Cuando esa Culpa te visita, ¿de qué te está acusando exactamente?",
                "Si pudiéramos poner a la Culpa Materna frente a ti como un personaje, ¿de dónde crees que aprendió las reglas que hoy te exige cumplir?",
                "La Culpa quiere hacerte creer que no eres suficiente. ¿Qué responderías si le recordaras las cosas que sí estás sosteniendo cada día?",
                "A veces la culpa habla con la voz de la sociedad, de la familia o de las redes sociales. ¿Esa voz es realmente tuya o la aprendiste en algún lugar?",
                "La culpa no dice la verdad sobre ti. Dice la verdad sobre las expectativas que te enseñaron a cargar."
            )
        ),

        "frustración" to TopicPattern(
            keywords = listOf("frustrada", "frustrado", "harta", "harto", "sobrepasada", "sobrepasado", "colapsada", "colapsado", "saturada", "saturado", "no puedo mas", "no aguanto"),
            emotion = "frustracion",
            followUpQuestions = listOf(
                "La frustración aparece cuando has sostenido demasiado por mucho tiempo. ¿Qué sientes que ya no quieres seguir cargando?",
                "Parece que vienes acumulando presión. No tienes que ser paciente con todo el mundo todo el tiempo. ¿Qué límite te gustaría poder poner?",
                "Es válido sentirse frustrada. También es válido no saber qué hacer con esa frustración."
            )
        ),

        "confusión" to TopicPattern(
            keywords = listOf("confundida", "confundido", "confusion", "no se como sentirme", "no se que sentir", "no entiendo lo que siento", "emociones mezcladas", "perdida", "perdido"),
            emotion = "confusion",
            followUpQuestions = listOf(
                "Vamos despacio. Cuando todo se mezcla, decidir puede sentirse imposible. ¿Qué parte se siente más difícil de ordenar?",
                "La confusión no significa que estés haciendo algo mal. ¿Qué pregunta es la que más vueltas te está dando?",
                "La confusión a veces es el espacio entre lo que era y lo que será.",
                "No hace falta que tengas todas las respuestas hoy."
            )
        ),

        // ============================================================
        // APOYO Y RELACIONES
        // ============================================================
        "soledad" to TopicPattern(
            keywords = listOf("sola", "solo", "nadie me apoya", "nadie me entiende", "desamparada", "no tengo a nadie", "no tengo con quien hablar", "abandonada", "abandonado"),
            emotion = "tristeza",
            followUpQuestions = listOf(
                "¿Qué es lo que más está doliendo de sentirte sola?",
                "¿Qué te gustaría que alguien entendiera de lo que estás viviendo?",
                "¿Qué tipo de apoyo estás necesitando más en este momento?",
                "¿Hay alguien con quien puedas hablar sin sentirte juzgada?",
                "Estar sola no significa que no importes. Este espacio es tuyo."
            )
        ),

        "familia" to TopicPattern(
            keywords = listOf(
                "familia", "mama", "mamá", "papá", "padre", "madre",
                "hermana", "hermano", "pareja", "esposo", "marido",
                "novio", "cuñada", "suegra", "padres"
            ),
            emotion = "neutral",
            followUpQuestions = listOf(
                "Qué bonito que quieras hablar de tu familia. ¿Qué momentos o gestos concretos vienen a tu mente cuando piensas en ellos?",
                "Las relaciones familiares tienen su propia historia. ¿Cómo ha sido para ti sentir ese apoyo a lo largo de esta etapa?",
                "Ese apoyo que sientes, ¿qué forma toma en el día a día? ¿Alguien que te escucha, que te ayuda con cosas concretas, que está presente?",
                "Las familias no siempre son perfectas ni siempre están presentes del modo que quisiéramos. ¿Hay algo que te gustaría que fuera distinto en cómo te acompañan?",
                "Parece que la familia es importante para ti. ¿Qué te gustaría transmitirle a tu bebé sobre lo que significa estar en familia?",
                "A veces el apoyo de la familia no se mide en grandes gestos, sino en presencias pequeñas y constantes. ¿Qué gesto de los tuyos te ha sostenido últimamente?",
                "Gracias por contarme cómo es tu familia. No todas las familias acompañan igual, y lo que tú has construido con la tuya tiene valor.",
                "El apoyo familiar puede ser un sostén importante en esta etapa. Reconocerlo también es una forma de cuidarte."
            )
        ),

        "pareja" to TopicPattern(
            keywords = listOf("pareja", "esposo", "marido", "novio", "novia", "compañero", "compañera", "el papa", "la mama del bebe"),
            emotion = "neutral",
            followUpQuestions = listOf(
                "Cuando piensas en tu pareja en esta etapa, ¿qué es lo primero que sientes?",
                "Las relaciones cambian con la llegada de un bebé. ¿Cómo ha sido ese cambio para ustedes?",
                "¿Qué gestos de tu pareja te hacen sentir acompañada, aunque sean pequeños?",
                "¿Hay algo que te gustaría que tu pareja entendiera mejor sobre lo que estás viviendo?",
                "¿Qué te gustaría que fuera distinto en la forma en que transitan juntos esta etapa?",
                "¿Cómo se cuidan ustedes como pareja en medio del cansancio y los cambios?",
                "Lo que estás construyendo con tu pareja también tiene valor, incluso en los días difíciles.",
                "Las parejas no siempre van al mismo ritmo. ¿Qué te gustaría que pasara entre ustedes en las próximas semanas?"
            )
        ),

        // ============================================================
        // IDENTIDAD
        // ============================================================
        "identidad postparto" to TopicPattern(
            keywords = listOf("posparto", "postparto", "despues del parto", "no soy la misma", "ya no soy la misma", "perdi mi identidad", "no se quien soy", "no sé quién soy"),
            emotion = "culpa",
            followUpQuestions = listOf(
                "Ser mamá cambia muchas cosas, pero también sigues siendo tú. ¿Qué parte de ti sientes que se ha perdido en esta etapa?",
                "La maternidad puede hacer que una se mida con una versión imposible de perfección. ¿Qué le dirías a esa exigencia?",
                "¿Qué cosas de antes de ser mamá te gustaría recuperar o mantener?",
                "La maternidad es un capítulo nuevo, no el final de la historia que eras.",
                "No tienes que elegir entre ser tú y ser madre. Puedes ser ambas cosas."
            )
        ),

        "identidad materna" to TopicPattern(
            keywords = listOf("ser mama", "buena mama", "mala mama", "buena madre", "mala madre", "quiero hacerlo bien", "no sere suficiente", "suficiente"),
            emotion = "culpa",
            followUpQuestions = listOf(
                "Sentir que podrías fallar no significa que estés fallando. ¿Qué idea de ser mamá te está pesando más?",
                "Tu valor no depende de vivir esta etapa de forma perfecta. ¿Qué parte de ti necesita más cuidado ahora?",
                "No tienes que sentirte feliz todo el tiempo para que tu experiencia sea válida.",
                "La maternidad no es una receta única. Cada mujer escribe su propia versión."
            )
        ),

        // ============================================================
        // SÍNTOMAS Y SALUD
        // ============================================================
        "sintoma físico" to TopicPattern(
            keywords = listOf(
                "vomito", "vomitar", "nauseas", "mareo", "mareada", "mareado",
                "sangrado", "fiebre", "contracciones", "presion alta",
                "debilidad", "no puedo retener liquidos",
                "dolor de cabeza", "dolor abdominal"
            ),
            emotion = "malestar",
            followUpQuestions = listOf(
                "Si el malestar persiste, se intensifica o viene acompañado de dolor fuerte, fiebre, sangrado o debilidad, es importante buscar orientación médica.",
                "Lo que sientes también merece atención física, no solo emocional. ¿Has podido consultar con un profesional?",
                "Tu cuerpo merece atención y cuidado. ¿Hay alguien que pueda acompañarte a hacerlo?",
                "Escuchar al cuerpo es fundamental en esta etapa. ¿Puedes contarme un poco más sobre lo que sientes?"
            )
        ),

        "lactancia" to TopicPattern(
            keywords = listOf("leche", "pecho", "dar pecho", "amamantar", "lactancia", "no tengo leche", "poca leche", "pecho duele", "pezon", "bebe no toma", "bebé no toma", "amamantando"),
            emotion = "malestar",
            followUpQuestions = listOf(
                "La lactancia es un camino único para cada madre. ¿Qué es lo que más te preocupa de la lactancia?",
                "La presión por amamantar puede ser intensa. ¿Cómo te gustaría que fuera tu experiencia con la lactancia?",
                "No todas las experiencias de lactancia son iguales. ¿Hay algo específico que te esté generando dificultad?",
                "La lactancia no define tu capacidad como madre.",
                "Dar pecho no es fácil. Y no todas las experiencias son iguales."
            )
        ),

        "cansancio extremo" to TopicPattern(
            keywords = listOf("cansada", "cansado", "agotada", "agotado", "sin energia", "no puedo mas", "no aguanto", "sin fuerzas", "no tengo fuerzas", "no puedo con todo", "me pesa el cuerpo"),
            emotion = "cansancio",
            followUpQuestions = listOf(
                "Ese cansancio suena profundo. ¿Qué parte de este cansancio pesa más: el físico o el emocional?",
                "No tienes que poder con todo hoy. ¿Hay algo pequeño que puedas soltar por ahora?",
                "El cansancio a veces es una señal de que necesitas pausa. ¿Qué tipo de pausa necesitas ahora?",
                "No tienes que explicarlo ni justificarlo. El cansancio es válido.",
                "El cuerpo sabio pide descanso cuando lo necesita. No es rendición, es cuidado."
            )
        ),

        // ============================================================
        // DESAHOGO
        // ============================================================
        "desahogo" to TopicPattern(
            keywords = listOf("desahogarme", "desahogar", "solo quiero hablar", "necesito hablar", "quiero contar lo que siento", "necesito sacar esto", "no quiero consejos", "no quiero soluciones", "solo escuchame", "solo escúchame", "quiero llorar", "necesito llorar"),
            emotion = "neutral",
            followUpQuestions = listOf(
                "A veces lo que más se necesita es poder soltar lo que se lleva dentro sin tener que resolverlo todo. Estoy aquí para escucharte. ¿Qué es lo que más está pesando ahora?",
                "No necesitas tener respuestas ni resolver nada ahora. A veces el simple hecho de poner en palabras lo que pesa ya es un alivio.",
                "Estoy aquí. Puedes decir lo que necesites, sin filtros, sin orden. No voy a juzgarte.",
                "No hace falta tener todo claro para empezar a hablar. ¿Quieres empezar por lo que más te duele ahora?"
            )
        ),

        // ============================================================
        // BIENESTAR
        // ============================================================
        "bienestar" to TopicPattern(
            keywords = listOf(
                "me siento bien", "estoy bien", "me siento mejor", "hoy estoy bien",
                "estoy tranquila", "me siento tranquila", "estoy contenta",
                "me siento contenta", "estoy feliz", "me siento feliz",
                "hoy fue un buen dia", "estoy mejor", "me siento en calma",
                "estoy en calma", "todo bien"
            ),
            emotion = "neutral",
            followUpQuestions = listOf(
                "Qué bueno escuchar eso. Los momentos de bienestar también merecen ser nombrados. ¿Hay algo en particular que esté contribuyendo a que te sientas así hoy?",
                "Me alegra mucho. A veces las madres nos acostumbramos tanto a la exigencia que olvidamos registrar los ratos en que estamos bien. ¿Quieres contarme qué está haciendo posible ese bienestar?",
                "Gracias por compartirlo. Reconocer los días buenos no es un lujo: es una forma de cuidar tu salud mental. ¿Cómo te gustaría sostener ese bienestar durante la semana?",
                "Qué lindo saber que hoy estás bien. Tu bienestar también es importante para tu bebé y para ti. ¿Qué te gustaría hacer con esta energía hoy?"
            )
        )
    )

    private val emotionWords = mapOf(
        "miedo" to "ansiedad",
        "temor" to "ansiedad",
        "ansiedad" to "ansiedad",
        "ansiosa" to "ansiedad",
        "ansioso" to "ansiedad",
        "tristeza" to "tristeza",
        "triste" to "tristeza",
        "deprimida" to "tristeza",
        "deprimido" to "tristeza",
        "culpa" to "culpa",
        "culpable" to "culpa",
        "confusion" to "confusion",
        "confundida" to "confusion",
        "confundido" to "confusion",
        "cansancio" to "cansancio",
        "cansada" to "cansancio",
        "cansado" to "cansancio",
        "agotada" to "cansancio",
        "agotado" to "cansancio",
        "frustracion" to "frustracion",
        "frustrada" to "frustracion",
        "frustrado" to "frustracion",
        "soledad" to "tristeza",
        "sola" to "tristeza",
        "solo" to "tristeza",
        "preocupada" to "ansiedad",
        "preocupado" to "ansiedad",
        "nerviosa" to "ansiedad",
        "nervioso" to "ansiedad"
    )

    /**
     * Devuelve las followUpQuestions definidas para un tema (si existen).
     */
    fun preguntasPara(tema: String): List<String>? =
        topicPatterns[tema]?.followUpQuestions?.ifEmpty { null }

    // ============================================================
    // DETECTAR MÚLTIPLES TEMAS EN UN MENSAJE
    // ============================================================
    fun detectarMultiplesTemas(mensaje: String): List<String> {
        val texto = mensaje.lowercase().trim()
        val temasDetectados = mutableListOf<String>()

        for ((tema, pattern) in topicPatterns) {
            val regex = pattern.keywords.joinToString("|") { "\\b$it\\b" }.toRegex(RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(texto)) {
                temasDetectados.add(tema)
            }
        }
        return temasDetectados.distinct()
    }

    // ============================================================
    // DETECTAR TEMA PRINCIPAL
    // ============================================================
    fun detectarTema(
        mensaje: String,
        clasificacion: ClasificacionOffline,
        memoria: MemoriaOffline
    ): TopicDetectionResult? {
        val texto = mensaje.lowercase().trim()
        val palabras = texto.split(" ").filter { it.isNotBlank() }

        // ============================================================
        // FIX: si ya hay un tema activo con profundidad >= 2, mantenerlo.
        // Evita fragmentar la conversación cuando la usuaria menciona
        // palabras clave secundarias ("mamá", "suegra", "estrés", "triste").
        // ============================================================
        if (memoria.tieneTemaActivo() && memoria.getProfundidad() >= 2) {
            return TopicDetectionResult(
                tema = memoria.getTema()!!,
                emoción = memoria.getEmocionTema(),
                palabraClave = mensaje,
                tipo = "continuacion_profunda"
            )
        }

        // ============================================================
        // FIX: si el clasificador ya detectó sintoma_fisico, no dejar que
        // el tema "sintoma físico" (con espacio) lo pise.
        // ============================================================
        if (clasificacion.category == "sintoma_fisico") {
            return null
        }

        // ============================================================
        // PASO 1: CONTAR COINCIDENCIAS POR TEMA
        // ============================================================
        val coincidenciasPorTema = mutableMapOf<String, Int>()

        for ((tema, pattern) in topicPatterns) {
            var count = 0
            for (keyword in pattern.keywords) {
                if (texto.contains(keyword)) {
                    count++
                }
            }
            if (count > 0) {
                coincidenciasPorTema[tema] = count
            }
        }

        // ============================================================
        // PASO 2: VERIFICAR TEMAS DE ALTA PRIORIDAD PRIMERO
        // ============================================================
        val prioridad = listOf(
            "miedo a morir en el parto",
            "estrías",
            "peso",
            "cambios corporales",
            "miedo al parto",
            "miedo por el bebé",
            "culpa materna",
            "soledad",
            "ansiedad general",
            "tristeza"
        )

        for (tema in prioridad) {
            if (coincidenciasPorTema.containsKey(tema)) {
                val pattern = topicPatterns[tema]
                if (pattern != null) {
                    return TopicDetectionResult(
                        tema = tema,
                        emoción = pattern.emotion,
                        palabraClave = pattern.keywords.first { texto.contains(it) },
                        tipo = "prioridad",
                        followUpQuestions = pattern.followUpQuestions
                    )
                }
            }
        }

        // ============================================================
        // PASO 3: SI HAY MÚLTIPLES TEMAS, ELEGIR EL DE MÁS COINCIDENCIAS
        // ============================================================
        if (coincidenciasPorTema.size > 1) {
            val mejor = coincidenciasPorTema.maxByOrNull { it.value }
            if (mejor != null) {
                val pattern = topicPatterns[mejor.key]
                if (pattern != null) {
                    return TopicDetectionResult(
                        tema = mejor.key,
                        emoción = pattern.emotion,
                        palabraClave = "multiple",
                        tipo = "multiple_temas",
                        followUpQuestions = pattern.followUpQuestions
                    )
                }
            }
        }

        // ============================================================
        // PASO 4: SI HAY UN TEMA ACTIVO Y EL MENSAJE ES CORTO
        // ============================================================
        if (memoria.tieneTemaActivo() && palabras.size <= 4) {
            return TopicDetectionResult(
                tema = memoria.getTema()!!,
                emoción = memoria.getEmocionTema(),
                palabraClave = mensaje,
                tipo = "continuación_tema"
            )
        }

        // ============================================================
        // PASO 5: DETECTAR CUALQUIER TEMA RESTANTE
        // ============================================================
        for ((tema, pattern) in topicPatterns) {
            if (tema in prioridad) continue
            for (keyword in pattern.keywords) {
                if (texto.contains(keyword)) {
                    return TopicDetectionResult(
                        tema = tema,
                        emoción = pattern.emotion,
                        palabraClave = keyword,
                        tipo = "palabra_clave",
                        followUpQuestions = pattern.followUpQuestions
                    )
                }
            }
        }

        // ============================================================
        // PASO 6: RESPUESTA CORTA QUE ES UNA EMOCIÓN
        // ============================================================
        if (palabras.size <= 3) {
            val emocion = emotionWords[texto]
            if (emocion != null) {
                if (memoria.tieneTemaActivo()) {
                    return TopicDetectionResult(
                        tema = memoria.getTema()!!,
                        emoción = emocion,
                        palabraClave = texto,
                        tipo = "respuesta_emoción_en_tema"
                    )
                }
                val temaMatch = topicPatterns.entries.firstOrNull { it.value.emotion == emocion }
                return if (temaMatch != null) {
                    TopicDetectionResult(
                        tema = temaMatch.key,
                        emoción = emocion,
                        palabraClave = texto,
                        tipo = "respuesta_emoción"
                    )
                } else {
                    TopicDetectionResult(
                        tema = emocion,
                        emoción = emocion,
                        palabraClave = texto,
                        tipo = "respuesta_emoción_genérica"
                    )
                }
            }
        }

        return null
    }
}

// ============================================================
// DATA CLASSES
// ============================================================
data class TopicPattern(
    val keywords: List<String>,
    val emotion: String,
    val followUpQuestions: List<String> = emptyList()
)

data class TopicDetectionResult(
    val tema: String,
    val emoción: String,
    val palabraClave: String,
    val tipo: String,
    val followUpQuestions: List<String> = emptyList()
)