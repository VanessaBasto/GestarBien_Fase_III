package com.example.gestarbien.ia.offline

import java.text.Normalizer

/**
 * Resultado de clasificar un mensaje offline.
 */
data class ClasificacionOffline(
    val category: String,
    val secondaryCategories: List<String>,
    val detectedTopics: List<String>,
    val emotion: String,
    val riskLevel: String,
    val intensity: String,
    val action: String,
    val matched: String?,
    val normalizedText: String,
    val confidence: Double,
    val shouldCreateAlert: Boolean = false,
)

object OfflineClassifier {

    // Reemplazos de escritura informal. Se aplican por palabra completa para no
    // romper palabras que contengan la abreviatura ("bb" dentro de otra palabra, etc.).
    private val REPLACEMENTS = linkedMapOf(
        "nose" to "no se",
        "faltal" to "fatal",
        "fatla" to "fatal",
        "demaciado" to "demasiado",
        "demaciada" to "demasiada",
        "demaciadas" to "demasiadas",
        "demaciados" to "demasiados",
        "bb" to "bebe",
        "bbe" to "bebe",
        "xq" to "porque",
        "pq" to "porque",
        "q" to "que",
        "x" to "por",
        "tb" to "tambien",
        "tmb" to "tambien",
        "xfa" to "por favor",
        "toy" to "estoy",
        "ta" to "esta",
    )

    private val NON_ALLOWED_CHARS = Regex("[^a-z0-9\\s]")
    private val MULTI_SPACE = Regex("\\s+")
    private val DIACRITICS = Regex("\\p{Mn}+")

    /** Palabras que niegan lo que viene justo después. */
    private val NEGADORES = setOf("no", "nunca", "jamas", "tampoco", "ni", "sin", "nada")

    /**
     * Normaliza el texto: minúsculas, sin tildes, sin signos, con las
     * abreviaturas más comunes expandidas.
     *
     * Nota: la ñ se convierte en n (NFD la descompone), así que el texto
     * normalizado nunca contiene ñ. Por eso las palabras clave también se
     * normalizan antes de compararlas.
     */
    fun normalizeText(raw: String?): String {
        if (raw.isNullOrEmpty()) return ""

        var text = raw.lowercase().trim()

        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        text = nfd.replace(DIACRITICS, "")

        text = text.replace(NON_ALLOWED_CHARS, " ")
        text = text.replace(MULTI_SPACE, " ").trim()

        if (text.isEmpty()) return ""

        // Reemplazos por palabra completa
        text = text.split(" ").joinToString(" ") { palabra ->
            REPLACEMENTS[palabra] ?: palabra
        }

        return text.replace(MULTI_SPACE, " ").trim()
    }

    // ============================================================
    // MATCHING POR LÍMITES DE PALABRA
    // ============================================================

    private val regexCache = HashMap<String, Regex>()

    private fun regexDe(frase: String): Regex = regexCache.getOrPut(frase) {
        val normalizada = normalizeText(frase)
        Regex("(?<![a-z0-9])" + Regex.escape(normalizada) + "(?![a-z0-9])")
    }

    /**
     * Palabras demasiado genéricas para sostener una categoría por sí solas.
     * Si una categoría solo coincide por una de estas, baja su prioridad para
     * que gane otra categoría con una coincidencia más específica.
     * (Antes "me siento culpable con mi bebé" ganaba ansiedad_bebe solo por "bebe".)
     */
    private val PALABRAS_DEBILES = setOf(
        "bebe", "bebé", "mi bebe", "mi bebé", "cuerpo", "mi cuerpo",
        "cambios", "cambiando", "cambio", "transformando", "reconozco",
        "creo", "bueno", "mmm", "ok", "vale",
    )
    private const val PRIORIDAD_DEBIL = 25

    /**
     * Antes esto era `text.contains(it)`, que hacía que "sola" coincidiera con
     * "consola" y "solo" con "solos". Ahora exige palabra completa y, entre
     * varias coincidencias, devuelve la MÁS LARGA (la más específica), no la
     * primera de la lista.
     */
    private fun encontrar(text: String, words: List<String>): String? {
        val coincidencias = words.filter { palabra ->
            val n = normalizeText(palabra)
            n.isNotEmpty() && regexDe(palabra).containsMatchIn(text)
        }
        if (coincidencias.isEmpty()) return null

        // Se prefiere la coincidencia más larga que NO sea una palabra genérica.
        val especificas = coincidencias.filterNot { esDebil(it) }
        val candidatas = especificas.ifEmpty { coincidencias }
        return candidatas.maxByOrNull { normalizeText(it).length }
    }

    private fun esDebil(palabra: String): Boolean = normalizeText(palabra) in debilesNormalizadas

    private val debilesNormalizadas: Set<String> by lazy {
        PALABRAS_DEBILES.map { normalizeText(it) }.toSet()
    }

    fun hasAny(text: String, words: List<String>): Boolean = encontrar(text, words) != null

    private fun hasAllGroups(text: String, groups: List<List<String>>): Boolean =
        groups.all { hasAny(text, it) }

    /**
     * Detecta si la coincidencia está negada ("no estoy triste", "ya no me siento sola").
     * Solo se usa en categorías emocionales: en riesgo NUNCA se aplica, porque un
     * falso negativo ahí es mucho más caro que un falso positivo.
     */
    private fun estaNegado(text: String, keyword: String): Boolean {
        val n = normalizeText(keyword)
        if (n.isEmpty()) return false
        // Si la propia palabra clave contiene un negador ("no puedo mas"), no aplica.
        val tokensKeyword = n.split(" ")
        if (tokensKeyword.any { it in NEGADORES }) return false

        val match = regexDe(keyword).find(text) ?: return false
        val previo = text.substring(0, match.range.first).trim()
        if (previo.isEmpty()) return false

        val tokensPrevios = previo.split(" ")
        val ventana = tokensPrevios.takeLast(3)
        return ventana.any { it in NEGADORES }
    }

    private data class DetectedEntry(val matched: String, val priority: Int)


    /** Categorías donde la negación puede invertir el sentido. */
    private val CATEGORIAS_NEGABLES = setOf(
        "tristeza", "ansiedad", "frustracion", "confusion", "malestar",
        "cansancio", "ansiedad_bebe", "identidad_materna", "identidad_postparto",
        "vinculo_bebe", "red_apoyo", "desahogo",
    )

    private fun detectAllCategories(text: String): LinkedHashMap<String, DetectedEntry> {
        val detected = LinkedHashMap<String, DetectedEntry>()

        val riesgo = encontrar(text, KEYWORD_GROUPS.getValue("riesgo_alto"))
        if (riesgo != null) {
            detected["riesgo_alto"] = DetectedEntry(riesgo, CATEGORY_PRIORITY.getValue("riesgo_alto"))
        }

        if (hasAllGroups(text, listOf(KEYWORD_GROUPS.getValue("negacion_duda"), KEYWORD_GROUPS.getValue("embarazo_bebe")))) {
            detected["ambivalencia_embarazo"] = DetectedEntry(
                "negacion_duda + embarazo_bebe",
                CATEGORY_PRIORITY.getValue("ambivalencia_embarazo"),
            )
        }

        if (
            hasAny(text, listOf("y si no", "que pasa si no", "si no tengo", "no se si")) &&
            hasAny(text, KEYWORD_GROUPS.getValue("embarazo_bebe"))
        ) {
            detected["ambivalencia_embarazo"] = DetectedEntry(
                "duda_hipotetica + embarazo_bebe",
                CATEGORY_PRIORITY.getValue("ambivalencia_embarazo"),
            )
        }

        // Se añadieron las categorías que existían en KEYWORD_GROUPS,
        // CATEGORY_PRIORITY y STRUCTURED_RESPONSES pero que nunca se comprobaban:
        // lactancia, cansancio, cuerpo_cambios, ansiedad_bebe, identidad_postparto,

        val categoryChecks = listOf(
            "riesgo_medio", "sintoma_fisico", "lactancia", "cuerpo_cambios",
            "ambivalencia_embarazo", "ansiedad_bebe", "ansiedad", "tristeza",
            "cansancio", "frustracion", "confusion", "malestar", "red_apoyo",
            "identidad_postparto", "identidad_materna", "vinculo_bebe", "desahogo",
            "bienestar",   // ← NUEVO
            "cierre", "pedir_mas_informacion", "pregunta_por_que", "respuesta_corta", "saludo",
        )

        for (category in categoryChecks) {
            val grupo = KEYWORD_GROUPS[category] ?: continue
            val prioridad = CATEGORY_PRIORITY[category] ?: continue

            if (category == "saludo") {
                if (text in grupo.map { normalizeText(it) } || text.startsWith("hola ")) {
                    detected[category] = DetectedEntry("saludo", prioridad)
                }
                continue
            }

            val match = encontrar(text, grupo) ?: continue
            if (category in CATEGORIAS_NEGABLES && estaNegado(text, match)) continue

            // No pisar una ambivalencia ya detectada por reglas combinadas.
            if (category == "ambivalencia_embarazo" && detected.containsKey(category)) continue

            val prioridadFinal = if (esDebil(match)) minOf(prioridad, PRIORIDAD_DEBIL) else prioridad

            detected[category] = DetectedEntry(match, prioridadFinal)
        }

        // "desahogo" sin red de apoyo tiene su propio banco de respuestas.
        if ("desahogo" in detected && "red_apoyo" in detected) {
            detected.remove("desahogo")
            detected["desahogo_sin_apoyo"] = DetectedEntry(
                "desahogo + red_apoyo",
                CATEGORY_PRIORITY.getValue("desahogo_sin_apoyo"),
            )
        }

        return detected
    }

    private fun chooseMainCategory(detected: Map<String, DetectedEntry>): String =
        detected.entries.maxByOrNull { it.value.priority }!!.key

    private fun inferEmotion(mainCategory: String, secondaryCategories: List<String>): String {
        if (mainCategory == "riesgo_alto") return "desesperanza"
        if (mainCategory == "ambivalencia_embarazo" || mainCategory == "confusion") return "confusion"
        if (mainCategory == "riesgo_medio") return "malestar"

        if (mainCategory == "sintoma_fisico") {
            if ("ansiedad" in secondaryCategories) return "ansiedad"
            if ("tristeza" in secondaryCategories) return "tristeza"
            return "malestar"
        }

        return EMOTION_BY_CATEGORY[mainCategory] ?: "neutral"
    }

    private fun inferRiskLevel(mainCategory: String, secondaryCategories: List<String>): String {
        if (mainCategory == "riesgo_alto" || "riesgo_alto" in secondaryCategories) return "alto"
        if (mainCategory == "riesgo_medio" || mainCategory == "ambivalencia_embarazo") return "medio"
        if ("riesgo_medio" in secondaryCategories || "ambivalencia_embarazo" in secondaryCategories) return "medio"
        return "bajo"
    }

    private fun inferAction(mainCategory: String, riskLevel: String): String {
        if (riskLevel == "alto") return "risk_protocol"
        if (riskLevel == "medio" && mainCategory in listOf("riesgo_alto", "riesgo_medio")) return "risk_protocol"
        if (mainCategory in listOf("sintoma_fisico", "lactancia")) return "health_orientation"
        if (mainCategory in listOf(
                "tristeza", "ansiedad", "ansiedad_bebe", "frustracion", "confusion",
                "malestar", "cansancio", "cuerpo_cambios", "ambivalencia_embarazo",
                "desahogo", "desahogo_sin_apoyo",
            )
        ) {
            return "emotional_pause"
        }
        if (mainCategory in listOf("red_apoyo", "vinculo_bebe", "identidad_materna", "identidad_postparto")) {
            return "module_response"
        }
        if (mainCategory == "cierre") return "closing"
        if (mainCategory == "saludo") return "greeting"
        if (mainCategory in listOf("pedir_mas_informacion", "pregunta_por_que", "respuesta_corta")) return "followup"
        return "clarify"
    }

    private fun inferConfidence(
        text: String,
        mainCategory: String,
        secondaryCategories: List<String>,
    ): Double {
        if (mainCategory in listOf("riesgo_alto", "ambivalencia_embarazo", "riesgo_medio")) return 0.95
        if (mainCategory == "desconocido") return 0.25

        var base = 0.75
        if (secondaryCategories.isEmpty()) base += 0.10
        if (secondaryCategories.size >= 2) base -= 0.10
        if (text.split(" ").filter { it.isNotBlank() }.size <= 2) base -= 0.10
        if (hasAny(text, listOf("demasiado", "muy", "mucho", "mucha", "fatal", "horrible"))) base += 0.05

        return base.coerceIn(0.30, 0.95).let { Math.round(it * 100.0) / 100.0 }
    }

    private fun shouldCreateAlertFromCategories(mainCategory: String, secondaryCategories: List<String>): Boolean {
        val alertCategories = listOf("riesgo_alto", "riesgo_medio", "ambivalencia_embarazo")
        if (mainCategory in alertCategories) return true
        return secondaryCategories.any { it in alertCategories }
    }

    private fun inferIntensity(category: String, riskLevel: String, text: String): String {
        if (riskLevel == "alto" || riskLevel == "medio") return "alta"

        val highIntensityWords = listOf(
            "demasiado", "demasiada", "mucho", "mucha", "muy", "no puedo", "no aguanto",
            "fatal", "horrible", "me supera", "todo me pesa", "todo me afecta",
        )
        if (hasAny(text, highIntensityWords)) return "alta"

        return INTENSITY_BY_CATEGORY[category] ?: "baja"
    }

    fun clasificar(mensaje: String?): ClasificacionOffline {
        val text = normalizeText(mensaje)

        if (text.isEmpty()) {
            return ClasificacionOffline(
                category = "desconocido",
                secondaryCategories = emptyList(),
                detectedTopics = emptyList(),
                emotion = "neutral",
                riskLevel = "bajo",
                intensity = "baja",
                action = "clarify",
                matched = "mensaje_vacio",
                normalizedText = text,
                confidence = 0.20,
                shouldCreateAlert = false,
            )
        }

        val detected = detectAllCategories(text)

        if (detected.isEmpty()) {
            return ClasificacionOffline(
                category = "desconocido",
                secondaryCategories = emptyList(),
                detectedTopics = emptyList(),
                emotion = "neutral",
                riskLevel = "bajo",
                intensity = "baja",
                action = "clarify",
                matched = null,
                normalizedText = text,
                confidence = 0.25,
                shouldCreateAlert = false,
            )
        }

        val mainCategory = chooseMainCategory(detected)
        val secondaryCategories = detected.keys.filter { it != mainCategory }

        val emotion = inferEmotion(mainCategory, secondaryCategories)
        val riskLevel = inferRiskLevel(mainCategory, secondaryCategories)
        val action = inferAction(mainCategory, riskLevel)
        val confidence = inferConfidence(text, mainCategory, secondaryCategories)
        val shouldAlert = shouldCreateAlertFromCategories(mainCategory, secondaryCategories)
        val intensity = inferIntensity(mainCategory, riskLevel, text)

        return ClasificacionOffline(
            category = mainCategory,
            secondaryCategories = secondaryCategories,
            detectedTopics = detected.keys.toList(),
            emotion = emotion,
            riskLevel = riskLevel,
            intensity = intensity,
            action = action,
            matched = detected[mainCategory]?.matched,
            normalizedText = text,
            confidence = confidence,
            shouldCreateAlert = shouldAlert,
        )
    }
}