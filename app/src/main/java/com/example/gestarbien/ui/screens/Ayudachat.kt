package com.example.gestarbien.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.gestarbien.ia.offline.OfflineClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// ============================================================
//  1) PREGUNTAS POR SU PSICÓLOGO / PROFESIONAL ASIGNADO
// ============================================================

private val PALABRAS_PROFESIONAL = listOf(
    "psicologo", "psicologa", "psicologos", "psicologas", "terapeuta",
    "profesional", "psiquiatra", "mi doctor", "mi doctora", "quien me atiende",
)
private val PALABRAS_DATO = listOf(
    "contacto", "numero", "whatsapp", "wasap", "celular", "telefono",
    "hablar", "quien", "como se llama", "nombre", "informacion", "info",
    "datos", "llamar", "escribir", "escribirle", "comunicarme", "asignado", "asignada",
)

/**
 * true si la usuaria pregunta por su psicólogo/profesional ("¿quién es mi psicólogo?",
 * "dame el número de la psicóloga", "quiero hablar con un profesional").
 * Nunca devuelve true si el mensaje es de riesgo alto: ese caso lo maneja el protocolo.
 */
fun esPreguntaPorProfesional(mensaje: String): Boolean {
    val t = OfflineClassifier.normalizeText(mensaje)
    if (t.isBlank()) return false
    if (OfflineClassifier.clasificar(mensaje).riskLevel == "alto") return false
    val mencionaProfesional = PALABRAS_PROFESIONAL.any { t.contains(it) }
    val pideDato = PALABRAS_DATO.any { t.contains(it) }
    val esCortaPregunta = t.split(" ").size <= 4   // "¿mi psicóloga?", "psicólogo"
    return mencionaProfesional && (pideDato || esCortaPregunta)
}

/** Texto con los datos del profesional asignado (o qué hacer si aún no hay). */
fun textoInfoProfesional(nombre: String?, whatsapp: String?): String = when {
    !nombre.isNullOrBlank() && !whatsapp.isNullOrBlank() ->
        "La persona que te acompaña desde psicología es **$nombre**. Puedes escribirle por WhatsApp con el botón de abajo cuando lo necesites."
    !nombre.isNullOrBlank() ->
        "La persona que te acompaña desde psicología es **$nombre**. Por ahora no tengo su número registrado, pero puedes pedirlo en tu centro de salud. Mientras tanto, aquí sigo contigo."
    else ->
        "Todavía no tengo un profesional asignado para ti, o no pude consultarlo porque no hay conexión. Cuando tengas internet vuelve a preguntarme.\n\nSi necesitas hablar con alguien ahora, puedes llamar a la [[tel:106:Línea 106]], que atiende salud mental."
}

// ============================================================
//  2) DICTADO POR VOZ
// ============================================================

/**
 * Devuelve una función que abre el reconocimiento de voz del teléfono (Google)
 * y entrega el texto reconocido. No necesita permiso de micrófono: lo pide la
 * app de Google. Si el teléfono tiene descargado el español, funciona sin internet.
 */
@Composable
fun rememberDictado(onTexto: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            val texto = resultado.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
            if (!texto.isNullOrBlank()) onTexto(texto)
        }
    }
    return remember(launcher) {
        {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-CO")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Cuéntame cómo te sientes")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            try {
                launcher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "Este teléfono no tiene reconocimiento de voz. Instala o actualiza la app de Google.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

private val INICIOS_PREGUNTA = listOf(
    "que", "qué", "como", "cómo", "cuando", "cuándo", "donde", "dónde", "por que", "por qué",
    "quien", "quién", "cual", "cuál", "cuanto", "cuánto", "es normal", "puedo", "debo",
    "sera", "será", "me puedes", "podrias", "podrías", "tienes", "hay",
)

/** Arreglo local (sin internet): mayúscula inicial, "¿?" en preguntas y punto final. */
fun pulirDictadoLocal(texto: String): String {
    var t = texto.trim().replace(Regex("\\s+"), " ")
    if (t.isEmpty()) return t
    t = t.replace(Regex("(?<![\\p{L}])i(?![\\p{L}])"), "y")   // "i" suelta → "y"
    val minus = t.lowercase()
    val esPregunta = INICIOS_PREGUNTA.any { minus == it || minus.startsWith("$it ") }
    t = t.replaceFirstChar { it.uppercase() }
    if (t.last() !in ".?!…") {
        t = if (esPregunta) "¿$t?" else "$t."
    }
    return t
}

private const val CORREGIR_ENDPOINT = "https://azaratthh-color-detectorr.hf.space/corregir"

/**
 * Corrige ortografía, tildes y puntuación con el servidor (sin cambiar lo que dijo).
 * Si no hay internet o tarda, devuelve el arreglo local. Nunca lanza excepción.
 */
suspend fun corregirDictado(texto: String): String = withContext(Dispatchers.IO) {
    val local = pulirDictadoLocal(texto)
    var conn: HttpURLConnection? = null
    try {
        conn = (URL(CORREGIR_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            doOutput = true
            connectTimeout = 4_000
            readTimeout = 8_000
        }
        val body = JSONObject().put("texto", texto).toString()
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        if (conn.responseCode !in 200..299) return@withContext local
        val json = JSONObject(conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
        val corregido = json.optString("texto", "").trim()
        // Si el servidor devolvió algo muy distinto en largo, mejor quedarse con lo local.
        if (corregido.isBlank() || corregido.length > texto.length * 2 + 20) local else corregido
    } catch (e: Exception) {
        Log.d("Dictado", "Sin corrección del servidor: ${e.message}")
        local
    } finally {
        conn?.disconnect()
    }
}