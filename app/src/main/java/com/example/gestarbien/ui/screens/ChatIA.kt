package com.example.gestarbien.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.gestarbien.data.AlertasRepository
import com.example.gestarbien.ia.offline.ModeloPreferencias
import kotlinx.coroutines.withTimeoutOrNull
import com.example.gestarbien.data.ProfesionalesRepository
import com.example.gestarbien.ia.offline.MemoriaOffline
import com.example.gestarbien.ia.offline.OfflineBank
import com.example.gestarbien.ia.offline.OfflineClassifier
import com.example.gestarbien.ia.offline.OfflineLLM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val TAG_CHATIA = "ChatIA"

data class MensajeHistorial(val role: String, val content: String)

enum class OrigenRespuesta { SERVIDOR, BANCO_OFFLINE, PROTOCOLO_LOCAL }

data class RespuestaChat(
    val bloques: List<String>,
    val origen: OrigenRespuesta,
    val requiereAlerta: Boolean = false,
    val categoria: String = "",
    val modulo: String = ""
) {
    val texto: String get() = bloques.joinToString("\n\n")
}

private const val CHAT_ENDPOINT = "https://azaratthh-color-detectorr.hf.space/chat"

val memoriaOffline = MemoriaOffline()

private val profesionalesRepo = ProfesionalesRepository()
private val alertasRepo = AlertasRepository()

/**
 * Chequeo rápido y local de conectividad. No toca la red.
 */
private fun hayConexion(context: Context?): Boolean {
    if (context == null) return false
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

/**
 * Envía la alerta al profesional. Se llama SIEMPRE desde background,
 * nunca bloquea la respuesta al usuario.
 */
private suspend fun enviarAlertaSiAplica(
    respuesta: RespuestaChat,
    usuarioUid: String,
    nombreUsuario: String,
    mensajeOriginal: String
) {
    if (!respuesta.requiereAlerta) return
    if (usuarioUid.isBlank()) return
    try {
        val correoAsignado = profesionalesRepo.obtenerOAsignarProfesional(usuarioUid)
        alertasRepo.registrarAlerta(
            usuarioUid = usuarioUid,
            usuarioNombre = nombreUsuario.ifBlank { "Usuaria" },
            categoria = respuesta.categoria.ifBlank { "riesgo_alto" },
            resumenMensaje = mensajeOriginal,
            profesionalCorreo = correoAsignado
        )
        Log.d(TAG_CHATIA, "Alerta enviada al profesional: $correoAsignado")
    } catch (e: Exception) {
        Log.e(TAG_CHATIA, "Fallo al enviar alerta: ${e.message}", e)
    }
}

/**
 * FIX CLAVE: dispara la alerta en segundo plano, sin esperar a Firestore.
 * Así la respuesta al usuario es inmediata aunque no haya conexión.
 */
private fun dispararAlertaEnSegundoPlano(
    respuesta: RespuestaChat,
    usuarioUid: String,
    nombreUsuario: String,
    mensajeOriginal: String
) {
    if (!respuesta.requiereAlerta) return
    if (usuarioUid.isBlank()) return
    GlobalScope.launch(Dispatchers.IO) {
        enviarAlertaSiAplica(respuesta, usuarioUid, nombreUsuario, mensajeOriginal)
    }
}

private suspend fun protocoloLocalSiAplica(
    mensaje: String,
    usuarioUid: String,
    nombreUsuario: String
): RespuestaChat? {
    val clasificacion = OfflineClassifier.clasificar(mensaje)

    Log.d(
        TAG_CHATIA,
        "Clasificación local: category=${clasificacion.category} risk=${clasificacion.riskLevel} " +
                "matched=${clasificacion.matched}"
    )

    if (clasificacion.riskLevel == "alto" || clasificacion.category == "riesgo_alto") {
        val respuestaOffline = OfflineBank.responder(
            mensaje = mensaje,
            nombreUsuario = "",
            memoria = memoriaOffline
        )
        val resultado = RespuestaChat(
            bloques = respuestaOffline.bloques,
            origen = OrigenRespuesta.PROTOCOLO_LOCAL,
            requiereAlerta = true,
            categoria = respuestaOffline.categoria,
            modulo = respuestaOffline.modulo ?: ""
        )
        Log.d(TAG_CHATIA, "PROTOCOLO LOCAL activado. bloques=${respuestaOffline.bloques.size}")

        // Alerta en background, sin bloquear.
        dispararAlertaEnSegundoPlano(resultado, usuarioUid, nombreUsuario, mensaje)

        return resultado
    }
    return null
}

private fun aBloques(texto: String): List<String> {
    val limpio = texto.trim()
    if (limpio.isEmpty()) return emptyList()
    val bloques = limpio.split(Regex("\\n\\s*\\n"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
    return if (bloques.size > 3) {
        bloques.take(2) + bloques.drop(2).joinToString("\n\n")
    } else {
        bloques
    }
}

private fun respuestaValida(texto: String?): Boolean {
    if (texto.isNullOrBlank()) return false
    val t = texto.trim()
    if (t.length < 3) return false
    if (t.equals("null", ignoreCase = true) ||
        t.equals("undefined", ignoreCase = true) ||
        t.equals("{}", ignoreCase = true)
    ) return false
    if (t.replace(Regex("[\\p{Punct}\\s]"), "").isEmpty()) return false
    return true
}

/**
 * Punto de entrada principal.
 */
suspend fun responderChat(
    mensaje: String,
    botName: String,
    nombreUsuario: String = "",
    usuarioUid: String = "",
    historial: List<MensajeHistorial> = emptyList(),
    appContext: Context? = null
): RespuestaChat = withContext(Dispatchers.IO) {

    if (mensaje.isBlank()) {
        return@withContext RespuestaChat(
            bloques = listOf("Cuéntame con calma, aquí estoy."),
            origen = OrigenRespuesta.PROTOCOLO_LOCAL
        )
    }

    // 1) PRIMERO: protocolo local (siempre, sin importar red).
    protocoloLocalSiAplica(mensaje, usuarioUid, nombreUsuario)?.let {
        Log.d(TAG_CHATIA, "Devolviendo respuesta del protocolo local")
        return@withContext it
    }

    // 2) SEGUNDO: si no hay conexión, ir directo al banco offline.
    if (!hayConexion(appContext)) {
        Log.d(TAG_CHATIA, "Sin conexión, usando banco offline directamente")
        return@withContext respuestaOffline(
            mensaje = mensaje,
            nombreUsuario = nombreUsuario,
            usuarioUid = usuarioUid,
            historial = historial,
            appContext = appContext,
            botName = botName
        )
    }

    // 3) TERCERO: intentar red. Si falla, caer al banco offline.
    var conn: HttpURLConnection? = null
    try {
        conn = (URL(CHAT_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Gestarbien-Android")
            doOutput = true
            // Espacios gratuitos de Hugging Face se "duermen" por inactividad.
            // Cuando el contenedor está dormido, la primera petición después
            // de eso puede tardar 30-60s en levantar antes de responder.
            connectTimeout = 15_000
            readTimeout = 55_000
        }

        val historialJson = JSONArray()
        historial.takeLast(10)
            .filter { it.content.isNotBlank() }
            .forEach {
                historialJson.put(
                    JSONObject().apply {
                        put("role", it.role)
                        put("content", it.content)
                    }
                )
            }

        val body = JSONObject().apply {
            put("mensaje", mensaje)
            put("botName", botName.ifBlank { "tu acompañante" })
            put("nombreUsuario", nombreUsuario.ifBlank { "usuaria" })
            put("historial", historialJson)
        }.toString()

        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val responseCode = conn.responseCode
        if (responseCode !in 200..299) {
            Log.d(TAG_CHATIA, "Servidor respondió $responseCode, usando offline")
            return@withContext respuestaOffline(
                mensaje, nombreUsuario, usuarioUid, historial, appContext, botName
            )
        }

        val cuerpo = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        if (cuerpo.isBlank()) {
            Log.d(TAG_CHATIA, "Servidor devolvió vacío, usando offline")
            return@withContext respuestaOffline(
                mensaje, nombreUsuario, usuarioUid, historial, appContext, botName
            )
        }

        val json = JSONObject(cuerpo)
        val texto = listOf("respuesta", "response", "respuesta_chatbot", "text", "mensaje")
            .asSequence()
            .map { clave ->
                val valor = json.optString(clave, "")
                if (respuestaValida(valor)) valor else null
            }
            .firstOrNull()

        if (texto == null) {
            Log.d(TAG_CHATIA, "Servidor devolvió texto inválido, usando offline")
            return@withContext respuestaOffline(
                mensaje, nombreUsuario, usuarioUid, historial, appContext, botName
            )
        }

        val bloques = aBloques(texto)
        if (bloques.isEmpty()) {
            return@withContext respuestaOffline(
                mensaje, nombreUsuario, usuarioUid, historial, appContext, botName
            )
        }

        val resultado = RespuestaChat(
            bloques = bloques,
            origen = OrigenRespuesta.SERVIDOR,
            requiereAlerta = json.optBoolean("should_create_alert", false),
            categoria = json.optString("categoria", ""),
            modulo = json.optString("modulo", "")
        )
        // Alerta en background.
        dispararAlertaEnSegundoPlano(resultado, usuarioUid, nombreUsuario, mensaje)
        resultado

    } catch (e: Exception) {
        Log.d(TAG_CHATIA, "Error de red (${e.message}), usando offline")
        respuestaOffline(mensaje, nombreUsuario, usuarioUid, historial, appContext, botName)
    } finally {
        conn?.disconnect()
    }
}

private const val TIMEOUT_IA_OFFLINE_MS = 30_000L

private suspend fun respuestaOffline(
    mensaje: String,
    nombreUsuario: String,
    usuarioUid: String,
    historial: List<MensajeHistorial> = emptyList(),
    appContext: Context? = null,
    botName: String = "tu acompañante"
): RespuestaChat {
    val respuesta = OfflineBank.responder(
        mensaje = mensaje,
        nombreUsuario = nombreUsuario,
        memoria = memoriaOffline
    )

    var bloquesFinal = respuesta.bloques
    var usoIA = false

    when {
        // Riesgo alto/medio: SIEMPRE respuesta fija del banco.
        respuesta.requiereAlerta ->
            Log.d(TAG_CHATIA, "IA offline omitida: requiere alerta (cat=${respuesta.categoria})")

        appContext == null ->
            Log.w(TAG_CHATIA, "IA offline omitida: appContext es null")

        !OfflineLLM.isModelReady(appContext) ->
            Log.d(TAG_CHATIA, "IA offline omitida: no hay modelo descargado")

        else -> {
            // Si el historial ya incluye el mensaje actual, no lo duplicamos.
            val previos = historial.let {
                if (it.lastOrNull()?.let { u -> u.role == "user" && u.content == mensaje } == true)
                    it.dropLast(1) else it
            }
            val turnos = previos.takeLast(4).map {
                OfflineLLM.Turno(esUsuaria = it.role == "user", texto = it.content)
            }

            // Carga perezosa + generación, con tope de tiempo para no dejar colgada la UI.
            val textoModelo = withTimeoutOrNull(TIMEOUT_IA_OFFLINE_MS) {
                if (OfflineLLM.warmUp(appContext)) {
                    OfflineLLM.generarRespuesta(
                        turnos,
                        mensaje,
                        nombreBot = botName.ifBlank { "tu acompañante" }
                    )
                } else null
            }

            if (textoModelo.isNullOrBlank()) {
                Log.w(TAG_CHATIA, "IA offline sin resultado útil; se usa el banco offline")
            } else {
                val bloquesModelo = aBloques(textoModelo)
                if (bloquesModelo.isNotEmpty()) {
                    bloquesFinal = bloquesModelo
                    usoIA = true
                }
            }
        }
    }

    val resultado = RespuestaChat(
        bloques = bloquesFinal,
        origen = OrigenRespuesta.BANCO_OFFLINE,
        requiereAlerta = respuesta.requiereAlerta,
        categoria = respuesta.categoria,
        modulo = respuesta.modulo ?: ""
    )
    Log.d(
        TAG_CHATIA,
        "Respuesta offline: cat=${respuesta.categoria} alerta=${respuesta.requiereAlerta} " +
                "bloques=${bloquesFinal.size} usoIA=$usoIA"
    )
    dispararAlertaEnSegundoPlano(resultado, usuarioUid, nombreUsuario, mensaje)
    return resultado
}