package com.example.gestarbien.ia.offline

import android.content.Context
import android.util.Log
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * IA offline (llama.cpp, módulo :lib) usada como RESPALDO cuando el banco no tiene respuesta.
 * NUNCA decide riesgo: eso es exclusivo de OfflineClassifier.
 *
 * Diferencias importantes con la versión de Cactus:
 * - El motor guarda la conversación por su cuenta; por eso `historial` ya no se usa.
 * - El system prompt solo se puede enviar UNA vez, justo después de cargar el modelo.
 *   Por eso el nombre del bot queda fijo al cargar: si cambia, se recarga el modelo.
 * - No hay parámetros de muestreo desde Kotlin (la temperatura está fija en ai_chat.cpp).
 * - Hay que descargar el .gguf nosotros mismos (descargarModelo).
 */
object OfflineLLM {

    private const val TAG = "OfflineLLM"
    private const val MAX_TOKENS = 60
    private const val MAX_ORACIONES = 3
    private const val NOMBRE_POR_DEFECTO = "tu acompañante"

    private const val MODELO_ARCHIVO = "LFM2.5-350M-Q4_K_M.gguf"
    private const val MODELO_URL =
        "https://huggingface.co/LiquidAI/LFM2.5-350M-GGUF/resolve/main/$MODELO_ARCHIVO"

    // El archivo real pesa ~229 MB; esto solo sirve para detectar descargas cortadas.
    private const val TAMANIO_MINIMO_BYTES = 150L * 1024L * 1024L

    /** Último error, para mostrarlo en la pantalla de ajustes. */
    @Volatile
    var ultimoError: String? = null

    private val SYSTEM_PROMPT_BASE = """
Eres una acompañante cálida y empática para mujeres embarazadas o en posparto.
Respondes siempre en español, con 1 o máximo 2 frases cortas y completas.

Reglas estrictas:
1. Primero refleja con tus palabras lo que la persona acaba de decir.
2. Luego haz UNA sola pregunta suave y curiosa.
3. Nunca te presentes con otro nombre. Usa solo el nombre que te dieron.
4. Nunca inventes información, hospitales, nombres ni situaciones que la persona no haya mencionado.
5. No des diagnósticos, no hables de medicamentos ni de tratamientos.
6. Usa un tono cálido, no directivo y cercano.
    """.trimIndent()

    // Este motor no acepta turnos de ejemplo: van como texto dentro del system prompt.
    private val EJEMPLOS = listOf(
        "Hola" to "¡Hola! Qué gusto saludarte. ¿Cómo te has sentido hoy?",
        "Estoy muy cansada y no duermo" to
                "Suena realmente agotador dormir tan poco. ¿Qué ha sido lo más pesado de estas noches?",
        "Tengo miedo del parto" to
                "Es natural sentir miedo ante algo tan grande. ¿Qué es lo que más te preocupa de ese día?",
        "Siento que soy mala madre" to
                "Escucho que esa idea de ser mala madre te está pesando mucho. ¿Qué es lo que más te hace sentir así en este momento?",
        "No disfruto a mi bebé como debería" to
                "Parece que estás siendo muy dura contigo misma. ¿Qué te dice esa voz que te juzga?",
        "Mi cuerpo es horrible, tengo mucha celulitis" to
                "Siento lo duro que es mirarte así en este momento. ¿Qué es lo que más te pesa de cómo te ves ahora?",
    )

    data class Turno(val esUsuaria: Boolean, val texto: String)

    private val mutex = Mutex()
    private var engine: InferenceEngine? = null
    private var appContext: Context? = null
    private var nombreCargado: String? = null
    private var ultimaRespuesta: String? = null

    private fun construirSystemPrompt(nombreBot: String): String = buildString {
        append(SYSTEM_PROMPT_BASE)
        append("\n\nTu nombre es ").append(nombreBot)
        append(". Úsalo solo si es natural, nunca te presentes con otro nombre.")
        append("\n\nEjemplos de tono (no los repitas tal cual):\n")
        for ((u, s) in EJEMPLOS) {
            append("\nUsuaria: ").append(u)
            append("\nTú: ").append(s)
            append("\n")
        }
    }

    // ───────────────────────── Archivo y descarga ─────────────────────────

    private fun archivoModelo(context: Context): File =
        File(File(context.filesDir, "models"), MODELO_ARCHIVO)

    fun modeloDescargado(context: Context): Boolean {
        val f = archivoModelo(context)
        return f.isFile && f.length() >= TAMANIO_MINIMO_BYTES
    }

    fun isModelReady(context: Context): Boolean = modeloDescargado(context)

    /**
     * Descarga el .gguf a filesDir/models. Si ya existe, no hace nada.
     * @param onProgreso valor de 0f a 1f (solo se llama si el servidor informa el tamaño).
     */
    suspend fun descargarModelo(
        context: Context,
        onProgreso: (Float) -> Unit = {},
    ): Boolean = withContext(Dispatchers.IO) {
        if (modeloDescargado(context)) return@withContext true

        val destino = archivoModelo(context)
        val temporal = File(destino.parentFile, "$MODELO_ARCHIVO.part")
        val conexion = URL(MODELO_URL).openConnection() as HttpURLConnection

        try {
            ultimoError = null
            destino.parentFile?.mkdirs()
            conexion.connectTimeout = 15_000
            conexion.readTimeout = 30_000
            conexion.instanceFollowRedirects = true

            if (conexion.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Respuesta HTTP ${conexion.responseCode}")
            }
            val total = conexion.contentLengthLong

            conexion.inputStream.use { entrada ->
                temporal.outputStream().use { salida ->
                    val buffer = ByteArray(64 * 1024)
                    var leidos = 0L
                    var ultimoPct = -1
                    while (true) {
                        val n = entrada.read(buffer)
                        if (n < 0) break
                        salida.write(buffer, 0, n)
                        leidos += n
                        ensureActive()
                        if (total > 0) {
                            val pct = (leidos * 100 / total).toInt()
                            if (pct != ultimoPct) {
                                ultimoPct = pct
                                onProgreso(leidos.toFloat() / total)
                            }
                        }
                    }
                }
            }

            if (temporal.length() < TAMANIO_MINIMO_BYTES) throw IOException("Descarga incompleta")
            if (!temporal.renameTo(destino)) throw IOException("No se pudo guardar el modelo")
            true
        } catch (e: CancellationException) {
            temporal.delete()
            throw e
        } catch (e: Throwable) {
            Log.e(TAG, "Error descargando el modelo", e)
            ultimoError = "${e::class.simpleName}: ${e.message}"
            temporal.delete()
            false
        } finally {
            conexion.disconnect()
        }
    }

    suspend fun eliminarModelo(context: Context) {
        liberar()
        withContext(Dispatchers.IO) { archivoModelo(context).delete() }
        ModeloPreferencias.marcarModeloListo(context, false)
    }

    // ───────────────────────── Carga del modelo ─────────────────────────

    /**
     * Carga el modelo y le manda el system prompt.
     * `modelSlug` se conserva solo para no romper las llamadas existentes; se ignora.
     */
    suspend fun warmUp(
        context: Context,
        @Suppress("UNUSED_PARAMETER") modelSlug: String = "",
        nombreBot: String = NOMBRE_POR_DEFECTO,
    ): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            appContext = context.applicationContext
            cargarLocked(context.applicationContext, nombreBot)
        }
    }

    /** Llamar solo con el mutex tomado. */
    private suspend fun cargarLocked(context: Context, nombreBot: String): Boolean {
        if (engine != null && nombreCargado == nombreBot) return true
        return try {
            ultimoError = null
            if (!modeloDescargado(context)) {
                ultimoError = "El modelo no está descargado"
                return false
            }

            val motor = engine ?: AiChat.getInferenceEngine(context).also { engine = it }

            // Deja el motor en "Initialized": descarga el modelo anterior o limpia un error.
            // Si no había nada que limpiar lanza una excepción, que ignoramos.
            runCatching { motor.cleanUp() }
            nombreCargado = null

            val inicializado = withTimeoutOrNull(20_000L) {
                motor.state.first { it is InferenceEngine.State.Initialized }
            }
            check(inicializado != null) { "El motor no terminó de inicializar" }

            motor.loadModel(archivoModelo(context).path)
            motor.setSystemPrompt(construirSystemPrompt(nombreBot))

            nombreCargado = nombreBot
            ultimaRespuesta = null
            Log.i(TAG, "Modelo llama.cpp cargado")
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.e(TAG, "Error cargando el modelo", e)
            ultimoError = "${e::class.simpleName}: ${e.message}"
            runCatching { engine?.cleanUp() }
            nombreCargado = null
            false
        }
    }

    /**
     * Borra la conversación que el motor lleva en memoria (recarga el modelo).
     * Llámalo donde antes llamabas a MemoriaOffline.limpiar().
     */
    suspend fun reiniciarConversacion(): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            val ctx = appContext ?: return@withLock false
            val nombre = nombreCargado ?: return@withLock false
            nombreCargado = null
            cargarLocked(ctx, nombre)
        }
    }

    // ───────────────────────── Generación ─────────────────────────

    /**
     * Devuelve null si el modelo no está cargado, falla, o la salida no pasa
     * los filtros de calidad. El llamador debe caer al banco offline.
     *
     * @param historial ya no se usa: el motor guarda la conversación solo.
     * @param nombreBot Nombre que la usuaria le puso al acompañante. Si cambia, se recarga el modelo.
     */
    suspend fun generarRespuesta(
        @Suppress("UNUSED_PARAMETER") historial: List<Turno>,
        mensajeNuevo: String,
        nombreBot: String = NOMBRE_POR_DEFECTO,
    ): String? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (mensajeNuevo.isBlank()) return@withLock null
            val ctx = appContext ?: return@withLock null
            if (!cargarLocked(ctx, nombreBot)) return@withLock null
            val motor = engine ?: return@withLock null

            try {
                val cruda = StringBuilder()
                motor.sendUserPrompt(mensajeNuevo, MAX_TOKENS).collect { cruda.append(it) }
                Log.d(TAG, "RAW: $cruda")

                val limpio = recortarAOraciones(limpiarRespuesta(cruda.toString()))
                val motivo = motivoDescarte(limpio)
                if (motivo != null) {
                    Log.w(TAG, "Descartada ($motivo): ${limpio.take(100)}")
                    null
                } else {
                    ultimaRespuesta = limpio
                    limpio
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.e(TAG, "Error generando respuesta", e)
                // Si el motor quedó en estado de error, la próxima llamada lo recarga.
                runCatching { motor.cleanUp() }
                nombreCargado = null
                null
            }
        }
    }

    // ───────────────────────── Validación ─────────────────────────

    private val PALABRAS_EN = setOf(
        "the", "user", "i", "should", "is", "and", "to", "that", "this", "need",
        "response", "okay", "since", "with", "they", "first", "now", "so", "you", "your"
    )
    private val PALABRAS_ES = setOf(
        "que", "de", "la", "el", "y", "en", "un", "una", "es", "te", "me", "tu",
        "lo", "con", "por", "para", "cómo", "qué", "hola", "muy", "más", "se",
        "estás", "estoy", "eso", "cuando", "algo", "has", "sientes", "suena"
    )
    private val GENERICAS = listOf(
        "gracias por tu comentario", "gracias por compartir", "entiendo tu punto",
        "como modelo de lenguaje", "como ia"
    )
    private val PROHIBIDAS = Regex(
        "\\b(paracetamol|ibuprofeno|aspirina|medicament\\w*|pastilla\\w*|dosis|\\d+\\s?mg|" +
                "diagn[oó]stic\\w*|tienes (depresi[oó]n|ansiedad)|sufres de|infecci[oó]n|contagi\\w*)\\b",
        RegexOption.IGNORE_CASE
    )

    private fun motivoDescarte(t: String): String? {
        if (t.length < 12) return "muy corta o incompleta"
        if (t.length > 300) return "muy larga"
        val minus = t.lowercase()
        if (minus.contains("<think") || minus.contains("</think")) return "razonamiento"
        val arranques = listOf("okay", "alright", "let me", "first,", "hmm", "the user", "so,")
        if (arranques.any { minus.startsWith(it) }) return "razonamiento"

        // Detectar presentaciones falsas
        if (minus.contains("me llamo") || minus.contains("mi nombre es") ||
            minus.contains("soy ") && (minus.contains("alex") || minus.contains("assistant"))
        ) {
            return "nombre inventado"
        }

        val palabras = minus.split(Regex("[^\\p{L}']+")).filter { it.isNotBlank() }
        val en = palabras.count { it in PALABRAS_EN }
        val es = palabras.count { it in PALABRAS_ES }
        if (en >= 2 && en >= es) return "inglés"
        if (es < 2) return "no parece español"

        if (GENERICAS.any { minus.contains(it) }) return "genérica"
        if (PROHIBIDAS.containsMatchIn(t)) return "contenido no permitido"
        if (t.equals(ultimaRespuesta, ignoreCase = true)) return "repetida"
        return null
    }

    // ───────────────────────── Limpieza ─────────────────────────

    private fun limpiarRespuesta(texto: String): String {
        var limpio = texto

        limpio = limpio.replace(Regex("<think(ing)?>[\\s\\S]*?</think(ing)?>", RegexOption.IGNORE_CASE), "")
        limpio = limpio.replace(Regex("<think(ing)?>[\\s\\S]*", RegexOption.IGNORE_CASE), "")
        limpio = limpio.replace(Regex("</think(ing)?>", RegexOption.IGNORE_CASE), "")

        limpio = limpio.replace(Regex("<\\|[a-z_]+\\|>"), "")
        limpio = limpio.replace(Regex("<(start|end)_of_turn>"), "")

        limpio = limpio.trim()
        for (prefijo in listOf("Sun:", "A:", "Asistente:", "Assistant:", "AI:", "Model:", "Tú:", "Tu:")) {
            if (limpio.startsWith(prefijo)) limpio = limpio.removePrefix(prefijo).trim()
        }

        limpio = limpio.substringBefore("\nUsuaria:").substringBefore("\nU:").substringBefore("\nUser:")
        limpio = limpio.replace(Regex("\\s*\\n+\\s*"), " ").replace(Regex(" {2,}"), " ")
        return limpio.trim()
    }

    /**
     * Se queda con las primeras oraciones COMPLETAS. Si el modelo se cortó a
     * mitad de frase, ese fragmento final se elimina. Si no hay ninguna
     * oración completa devuelve "" (y será descartada por corta).
     */
    private fun recortarAOraciones(texto: String): String {
        val oraciones = Regex("[^.!?]*[.!?]+").findAll(texto).map { it.value.trim() }
            .filter { it.isNotBlank() }.toList()
        return oraciones.take(MAX_ORACIONES).joinToString(" ")
    }

    suspend fun liberar() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                runCatching { engine?.cleanUp() }
                nombreCargado = null
                ultimaRespuesta = null
            }
        }
    }
}