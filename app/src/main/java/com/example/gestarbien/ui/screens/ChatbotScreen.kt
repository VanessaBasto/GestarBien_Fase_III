@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.gestarbien.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.gestarbien.R
import com.example.gestarbien.data.ConversacionesRepository
import androidx.compose.foundation.shape.CircleShape
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.AutoStories
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.ui.text.withLink
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged

// ─────────────────────────────────────────────
//  PALETA
// ─────────────────────────────────────────────
private val Lavanda      = Color(0xFFB1B4E1)
private val LavandaSuave = Color(0xFFEEEFFE)
private val MoradoOscuro = Color(0xFF3A3D8C)
private val MoradoMedio  = Color(0xFF5C5FA8)
private val GrisTxt      = Color(0xFF888888)
private val BlancoFondo  = Color(0xFFFFFFFF)
private val RojoSuave    = Color(0xFFFFEBEB)
private val RojoBorde    = Color(0xFFFFB3B3)
private val RojoTexto    = Color(0xFFCC2222)

val coloresBot = listOf(
    "Lavanda"  to Color(0xFFD4D6F0),
    "Rosa"     to Color(0xFFF2C4D8),
    "Verde"    to Color(0xFFB8E4CC),
    "Azul"     to Color(0xFFB8D9F2),
    "Durazno"  to Color(0xFFFFD9BF),
    "Lila"     to Color(0xFFCFB8E8)
)

// Cada cuántos días se le recuerda a la usuaria repetir EPDS/PASS.
private const val PERIODICIDAD_DIAS_EVALUACION = 7L

private val aliasColores = mapOf(
    "morado"    to Color(0xFFCFB8E8),
    "violeta"   to Color(0xFFCFB8E8),
    "lila"      to Color(0xFFCFB8E8),
    "rosado"    to Color(0xFFF2C4D8),
    "rosada"    to Color(0xFFF2C4D8),
    "rosa"      to Color(0xFFF2C4D8),
    "pink"      to Color(0xFFF2C4D8),
    "azul"      to Color(0xFFB8D9F2),
    "celeste"   to Color(0xFFB8D9F2),
    "blue"      to Color(0xFFB8D9F2),
    "verde"     to Color(0xFFB8E4CC),
    "green"     to Color(0xFFB8E4CC),
    "lavanda"   to Color(0xFFD4D6F0),
    "lavender"  to Color(0xFFD4D6F0),
    "durazno"   to Color(0xFFFFD9BF),
    "melocotón" to Color(0xFFFFD9BF),
    "naranja"   to Color(0xFFFFD9BF),
    "peach"     to Color(0xFFFFD9BF),
    "normal"    to Color(0xFFD4D6F0),
    "simple"    to Color(0xFFD4D6F0),
    "sencillo"  to Color(0xFFD4D6F0),
    "básico"    to Color(0xFFD4D6F0),
    "default"   to Color(0xFFD4D6F0)
)

// ─────────────────────────────────────────────
//  HELPERS FIREBASE
// ─────────────────────────────────────────────
/** Chequeo rápido y local de conectividad (sin tocar la red). */
private fun hayConexion(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

private fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}

private fun hexToColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    Color(0xFFD4D6F0)
}

private fun guardarBotConfig(nombre: String, color: Color) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseFirestore.getInstance()
        .collection("usuarios")
        .document(uid)
        .collection("bot_config")
        .document("config")
        .set(mapOf(
            "nombre"      to nombre,
            "color"       to colorToHex(color),
            "configurado" to true
        ))
}

// ─────────────────────────────────────────────
//  MODELOS
// ─────────────────────────────────────────────
data class ChatMessage(
    val id: String,
    val text: String,
    val isBot: Boolean,
    val isTyping: Boolean = false,
    val mostrarContactoProfesional: Boolean = false
)

/** icono = drawable propio; si es null, la opción se muestra solo con texto. */
data class QuickOption(
    val label: String,
    val icono: Int? = null,
    val isSOS: Boolean = false
)

// ─────────────────────────────────────────────
//  ONBOARDING
// ─────────────────────────────────────────────
enum class BotOnboardingStep {
    LOADING,
    ASK_NAME,
    ASK_COLOR,
    ANNOUNCE_CUESTIONARIOS,
    DONE
}

// ─────────────────────────────────────────────
//  RESPUESTAS
// ─────────────────────────────────────────────
private fun botResponse(userInput: String, botName: String): String = when {
    userInput.contains("bien", ignoreCase = true) ->
        "Me alegra mucho escuchar eso. Mantener ese estado de bienestar es importante. ¿Hay algo en lo que pueda ayudarte hoy?"
    userInput.contains("ansiosa", ignoreCase = true) || userInput.contains("ansiedad", ignoreCase = true) ->
        "Entiendo que la ansiedad puede ser muy difícil, especialmente en este momento tan especial. ¿Quieres que practiquemos juntas un ejercicio de respiración?"
    userInput.contains("triste", ignoreCase = true) || userInput.contains("tristeza", ignoreCase = true) ->
        "Sentirse triste a veces es completamente válido. Estoy aquí contigo. ¿Quieres contarme más sobre cómo te sientes?"
    userInput.contains("angustiada", ignoreCase = true) || userInput.contains("angustia", ignoreCase = true) ->
        "La angustia es una señal de que necesitas apoyo extra. No estás sola en esto ¿Te gustaría que te comparta algunas técnicas de manejo?"
    userInput.contains("ayuda", ignoreCase = true) || userInput.contains("sos", ignoreCase = true) ->
        "Estoy aquí para ti. Si estás en una situación de crisis, por favor comunícate con la **Línea 106** (Salud Mental - Colombia). También puedes hablar conmigo sobre lo que sientes ahora mismo. ¿Qué está pasando?"
    userInput.contains("escribir", ignoreCase = true) || userInput.contains("prefiero", ignoreCase = true) ->
        "Claro, cuéntame con tus propias palabras. Estoy escuchando con toda mi atención"
    else ->
        "Gracias por compartir eso conmigo. Recuerda que cada sentimiento es válido y estoy aquí para acompañarte. ¿Hay algo más que quieras explorar juntas?"
}

private fun capitalizarPrimeraLetra(texto: String): String {
    if (texto.isEmpty()) return texto
    return texto.replaceFirstChar { it.uppercase() }
}
private fun obtenerFraseColor(nombre: String): String = when (nombre.lowercase()) {
    "lavanda"  -> "La lavanda se asocia con la calma y el descanso. Una elección muy sanadora."
    "rosa"     -> "El rosa evoca ternura y cuidado. Un recordatorio de que mereces gentileza."
    "verde"    -> "El verde nos conecta con la naturaleza y el crecimiento. Una elección muy serena."
    "azul"     -> "El azul transmite calma y confianza. Como el cielo o el mar en un día tranquilo."
    "durazno"  -> "El durazno combina calidez y suavidad. Un color que abraza sin abrumar."
    "lila"     -> "El lila es suave y relajante. Se asocia con la calma interior y el descanso."
    else       -> "Una elección muy especial para este espacio 💜"
}

private var eyeScaleGlobal by mutableFloatStateOf(1f)

// ─────────────────────────────────────────────
//  BOT MASCOTA
// ─────────────────────────────────────────────
@Composable
fun BotMascota(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFB1B4E1),
    hasText: Boolean = false,
    size: Dp = 44.dp,
    parpadea: Boolean = true
) {
    val eyeXAnim by animateFloatAsState(
        targetValue  = if (hasText) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "eyeX"
    )

    LaunchedEffect(parpadea) {
        if (!parpadea) {
            eyeScaleGlobal = 1f
            return@LaunchedEffect
        }
        val cycle = 6000L
        while (true) {
            delay(cycle - 224L)
            val steps = 8
            repeat(steps) { i -> eyeScaleGlobal = 1f - (i + 1f) / steps; delay(14) }
            repeat(steps) { i -> eyeScaleGlobal = (i + 1f) / steps;       delay(14) }
            eyeScaleGlobal = 1f
        }
    }

    Canvas(modifier = modifier.size(size)) {
        val w  = this.size.width
        val h  = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r  = w / 2f * 0.97f

        val sc    = w / 240f
        val erx   = 46f * sc
        val ery   = 52f * sc
        val gap   = 8f  * sc
        val shft  = 14f * sc
        val baseY = cy + (10f * sc)

        val lx   = cx - gap / 2f - erx + eyeXAnim * shft
        val rx   = cx + gap / 2f + erx + eyeXAnim * shft
        val visH = ery * eyeScaleGlobal

        drawCircle(color = color, radius = r, center = Offset(cx, cy))

        val bodyPath = Path().apply {
            addOval(Rect(center = Offset(cx, cy), radius = r))
        }

        clipPath(bodyPath) {
            if (visH > 0.5f) {
                clipRect(lx - erx - 1f, baseY - ery - 2f, lx + erx + 1f, baseY + 4f) {
                    drawOval(Color.White, Offset(lx - erx, baseY - visH), Size(erx * 2f, visH * 2f))
                }
                clipRect(rx - erx - 1f, baseY - ery - 2f, rx + erx + 1f, baseY + 4f) {
                    drawOval(Color.White, Offset(rx - erx, baseY - visH), Size(erx * 2f, visH * 2f))
                }
            } else {
                drawLine(Color.White, Offset(lx - erx, baseY), Offset(lx + erx, baseY), 3f * sc)
                drawLine(Color.White, Offset(rx - erx, baseY), Offset(rx + erx, baseY), 3f * sc)
            }
        }
    }
}

// ─────────────────────────────────────────────
//  PANTALLA PRINCIPAL
// ─────────────────────────────────────────────
@Composable
fun ChatbotScreen(
    aperturaCuestionarios: Boolean = false,
    onAperturaConsumida: () -> Unit = {},
    conversacionId: String = "",
    conversacionInicioMs: Long = 0L,
    conversacionACargar: String? = null,
    onConversacionCargada: () -> Unit = {},
    onAbrirCuaderno: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToEpds: () -> Unit = {},
    onNavigateToPass: () -> Unit = {},
    onNavigateToIAOffline: () -> Unit = {},
    onNavigateToAyuda: () -> Unit = {},
    onLogout: () -> Unit = {},
    emocionInicial: String? = null,
    onEmocionConsumida: () -> Unit = {}
) {
    val context = LocalContext.current
    val userName = FirebaseAuth.getInstance().currentUser?.displayName
        ?.takeIf { it.isNotBlank() } ?: "Sofía"
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val prefs = remember { context.getSharedPreferences("gestarbien_prefs", Context.MODE_PRIVATE) }

    // Se captura al entrar: si viene con id, esta pantalla abre una
    // conversación guardada y no debe generar mensajes de bienvenida.
    val abrirGuardada = remember { conversacionACargar }
    var cargaInicialLista by remember { mutableStateOf(false) }
    var showColorConfirm        by remember { mutableStateOf(false) }
    var onboardingStep          by remember { mutableStateOf(BotOnboardingStep.LOADING) }
    var botName by remember {
        mutableStateOf(prefs.getString("bot_nombre_$uid", "") ?: "")
    }
    var botColor by remember {
        mutableStateOf(hexToColor(prefs.getString("bot_color_$uid", "#B1B4E1") ?: "#B1B4E1"))
    }
    var botColorNombre          by remember { mutableStateOf("Lavanda") }
    var messages                by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText               by remember { mutableStateOf("") }
    var showOptions             by remember { mutableStateOf(true) }
    var isTyping                by remember { mutableStateOf(false) }
    var showCuestionarioButtons by remember { mutableStateOf(false) }
    var puntajeEpdsUlt by remember { mutableStateOf<Int?>(null) }
    var puntajePassUlt by remember { mutableStateOf<Int?>(null) }
    var item10Ult      by remember { mutableStateOf(false) }
    // Se arranca con lo guardado en el teléfono para poder responder también sin internet.
    var nombreProfesionalAsignado by remember { mutableStateOf(prefs.getString("prof_nombre_$uid", null)) }
    var whatsappProfesionalAsignado by remember { mutableStateOf(prefs.getString("prof_wa_$uid", null)) }
    // FIX: true mientras el bot presenta la info inicial (profesional + cuestionarios).
    // Oculta los botones de color para que no se puedan tocar a mitad de la presentación.
    var secuenciaEnCurso by remember { mutableStateOf(false) }
    // Dictado por voz: true mientras se corrige la ortografía del texto dictado.
    var corrigiendoVoz by remember { mutableStateOf(false) }
    // Recordatorio de repetir EPDS/PASS cuando ya pasó la periodicidad recomendada.
    var mostrarBotonesReintento by remember { mutableStateOf(false) }

    var mostrarMascota by remember {
        mutableStateOf(prefs.getBoolean("mostrar_mascota_$uid", true))
    }
    var recoloreando by remember { mutableStateOf(false) }

    val quickOptions = listOf(
        QuickOption("Me siento bien",       R.drawable.animo_feliz),
        QuickOption("Estoy ansiosa",        R.drawable.animo_ansiosa),
        QuickOption("Me siento triste",     R.drawable.animo_triste),
        QuickOption("Estoy muy angustiada", R.drawable.animo_confundida),
        QuickOption("Necesito ayuda", isSOS = true),
        QuickOption("Prefiero escribir cómo me siento")
    )

    // ── Ritmo de conversación coherente con la longitud del texto ────
    // tiempoTecleo: cuánto dura el indicador de "escribiendo..." antes de
    //   que aparezca el mensaje (simula que el bot "tarda" en función de
    //   lo largo que es lo que va a decir).
    // tiempoLectura: pausa DESPUÉS de publicar un mensaje, antes de que el
    //   bot empiece a "escribir" el siguiente, para darle tiempo real de
    //   leer lo que acaba de aparecer.
    fun tiempoTecleo(texto: String): Long =
        (texto.length * 22L).coerceIn(700L, 2200L)

    fun tiempoLectura(texto: String): Long =
        (texto.length * 35L).coerceIn(1200L, 3200L)

    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()

    // ── Cargar configuración y conversación desde Firebase ───────────
    LaunchedEffect(uid) {
        // "Ping" de calentamiento del backend: en un hilo de IO y con timeout
        // corto, para que jamás retrase ni bloquee la carga de la pantalla
        // (antes podía quedarse esperando varios minutos sin wifi).
        launch(Dispatchers.IO) {
            try {
                val connection = java.net.URL("https://azaratthh-color-detectorr.hf.space/ping")
                    .openConnection() as java.net.HttpURLConnection
                // Antes 3000/3000: casi nunca alcanzaba a completar el
                // arranque del Space dormido, así que el "calentamiento"
                // no calentaba nada. Corre en background sin bloquear la
                // pantalla, así que no hay costo en darle más margen.
                connection.connectTimeout = 15000
                connection.readTimeout = 55000
                connection.inputStream.close()
            } catch (_: Exception) {}
        }

        val idAAbrir = abrirGuardada ?: conversacionId

        // ── Sin conexión: usar directamente lo que ya tenemos en caché ──
        // Evita quedarnos esperando a que Firestore intente conectarse y
        // falle (eso puede tardar bastante sin wifi). Si ya sabemos el
        // nombre/color del bot (sesión anterior), arrancamos con eso de
        // inmediato; si nunca se configuró, no hay nada que cachear y
        // pasamos igual al onboarding (no requiere red hasta el final).
        if (!hayConexion(context)) {
            if (botName.isNotBlank()) {
                val epdsHechoCache = prefs.getBoolean("epds_hecho_$uid", false)
                val passHechoCache = prefs.getBoolean("pass_hecho_$uid", false)

                onboardingStep = if (epdsHechoCache && passHechoCache) {
                    BotOnboardingStep.DONE
                } else {
                    BotOnboardingStep.ANNOUNCE_CUESTIONARIOS
                }

                if (abrirGuardada == null) {
                    messages = listOf(
                        ChatMessage(
                            id    = "bienvenida",
                            text  = when {
                                epdsHechoCache && passHechoCache ->
                                    "¡Hola de nuevo, **$userName**! Qué bueno verte. ¿Cómo te sientes hoy?"
                                else ->
                                    "¡Hola de nuevo, **$userName**! Aún tienes pendientes algunos cuestionarios. Cuando tengas internet, complétalos para que pueda acompañarte mejor."
                            },
                            isBot = true
                        )
                    )
                    showOptions             = epdsHechoCache && passHechoCache
                    showCuestionarioButtons = !(epdsHechoCache && passHechoCache)
                }
            } else {
                onboardingStep = BotOnboardingStep.ASK_NAME
                delay(200)
                messages = listOf(
                    ChatMessage(
                        id    = "onboarding_0",
                        text  = "¡Hola, **$userName**! Qué alegría que estés aquí. Este es un espacio seguro para hablar con calma, sin juicios y a tu ritmo. Antes de comenzar... ¿cómo te gustaría llamarme?",
                        isBot = true
                    )
                )
            }
            cargaInicialLista = true
            onConversacionCargada()
            return@LaunchedEffect
        }

        // Profesional asignado — se consulta siempre (nueva o ya
        // configurada), porque ahora se presenta también en el onboarding.
        try {
            val profesionalesRepo = com.example.gestarbien.data.ProfesionalesRepository()
            val correoAsignado = profesionalesRepo.obtenerOAsignarProfesional(uid)
            if (correoAsignado != null) {
                val perfil = profesionalesRepo.listaProfesionalesActivos()
                    .find { it.correo == correoAsignado }
                nombreProfesionalAsignado   = perfil?.nombre
                whatsappProfesionalAsignado = perfil?.whatsapp
                prefs.edit()
                    .putString("prof_nombre_$uid", perfil?.nombre)
                    .putString("prof_wa_$uid", perfil?.whatsapp)
                    .apply()
            }
        } catch (e: Exception) { /* sigue sin datos del profesional, no bloquea el chat */ }

        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("usuarios").document(uid)
                .collection("bot_config").document("config")
                .get().await()

            if (doc.exists() && doc.getBoolean("configurado") == true) {
                botName  = doc.getString("nombre") ?: ""
                botColor = hexToColor(doc.getString("color") ?: "#D4D6F0")

                val cuestionarios = FirebaseFirestore.getInstance()
                    .collection("usuarios").document(uid)
                    .collection("cuestionarios_clinicos")
                    .get().await()

                val tiposCompletados = cuestionarios.documents
                    .mapNotNull { it.getString("tipo") }
                    .toSet()

                val epdsHecho = "EPDS" in tiposCompletados
                val passHecho = "PASS" in tiposCompletados

                onboardingStep = if (epdsHecho && passHecho) {
                    BotOnboardingStep.DONE
                } else {
                    BotOnboardingStep.ANNOUNCE_CUESTIONARIOS
                }

                prefs.edit()
                    .putString("bot_nombre_$uid", botName)
                    .putString("bot_color_$uid", doc.getString("color") ?: "#D4D6F0")
                    .putBoolean("epds_hecho_$uid", epdsHecho)
                    .putBoolean("pass_hecho_$uid", passHecho)
                    .apply()

                val docsOrdenados = cuestionarios.documents.sortedByDescending {
                    it.getTimestamp("fecha")?.toDate()?.time ?: 0L
                }
                var fechaUltimaEpdsMs = 0L
                var fechaUltimaPassMs = 0L
                docsOrdenados.firstOrNull { it.getString("tipo") == "EPDS" }?.let {
                    puntajeEpdsUlt   = (it.getLong("puntaje") ?: 0L).toInt()
                    item10Ult        = it.getBoolean("alertaItem10") ?: false
                    fechaUltimaEpdsMs = it.getTimestamp("fecha")?.toDate()?.time ?: 0L
                }
                docsOrdenados.firstOrNull { it.getString("tipo") == "PASS" }?.let {
                    puntajePassUlt   = (it.getLong("puntaje") ?: 0L).toInt()
                    fechaUltimaPassMs = it.getTimestamp("fecha")?.toDate()?.time ?: 0L
                }

                // ¿Ya pasó la periodicidad recomendada desde la última evaluación?
                fun diasDesde(ms: Long): Long =
                    if (ms <= 0L) Long.MAX_VALUE
                    else (System.currentTimeMillis() - ms) / (1000L * 60 * 60 * 24)

                val epdsVencido = epdsHecho && diasDesde(fechaUltimaEpdsMs) >= PERIODICIDAD_DIAS_EVALUACION
                val passVencido = passHecho && diasDesde(fechaUltimaPassMs) >= PERIODICIDAD_DIAS_EVALUACION

                val textoRecordatorioReintento = when {
                    epdsVencido && passVencido ->
                        "Recuerda que ya puedes volver a completar el **EPDS** y el **PASS**. Ayudan a que pueda acompañarte mejor esta semana."
                    epdsVencido ->
                        "Recuerda que ya puedes volver a completar el **EPDS** cuando quieras."
                    passVencido ->
                        "Recuerda que ya puedes volver a completar el **PASS** cuando quieras."
                    else -> null
                }

                // ── Intentar cargar la conversación de hoy (o la pedida desde el cuaderno) ──
                val conv = ConversacionesRepository.cargar(idAAbrir)
                if (conv != null && conv.mensajes.isNotEmpty()) {
                    messages = conv.mensajes
                    showOptions = false
                    showCuestionarioButtons = onboardingStep != BotOnboardingStep.DONE
                    mostrarBotonesReintento = false
                } else if (abrirGuardada == null) {
                    // Es la primera vez hoy: bienvenida normal
                    messages = listOf(
                        ChatMessage(
                            id    = "bienvenida",
                            text  = when {
                                epdsHecho && passHecho ->
                                    "¡Hola de nuevo, **$userName**! Qué bueno verte. ¿Cómo te sientes hoy?"
                                !epdsHecho && !passHecho ->
                                    "¡Hola de nuevo, **$userName**! Aún tienes pendientes el **EPDS** y el **PASS**. Son solo unos minutos y me ayudarán a acompañarte mejor"
                                epdsHecho && !passHecho ->
                                    "¡Hola de nuevo, **$userName**! Ya completaste el EPDS, solo falta el **PASS**. ¿Lo completamos ahora?"
                                else ->
                                    "¡Hola de nuevo, **$userName**! Ya completaste el PASS, solo falta el **EPDS**. ¿Lo completamos ahora?"
                            },
                            isBot = true
                        )
                    )

                    if (epdsHecho && passHecho) {
                        if (textoRecordatorioReintento != null) {
                            messages = messages + ChatMessage(
                                id    = "recordatorio_reintento",
                                text  = textoRecordatorioReintento,
                                isBot = true
                            )
                            mostrarBotonesReintento = true
                            showOptions             = false
                        } else {
                            showOptions = true
                        }
                    } else {
                        showCuestionarioButtons = true
                    }
                }

            } else {
                onboardingStep = BotOnboardingStep.ASK_NAME
                delay(400)
                messages = listOf(
                    ChatMessage(
                        id    = "onboarding_0",
                        text  = "¡Hola, **$userName**! Qué alegría que estés aquí. Este es un espacio seguro para hablar con calma, sin juicios y a tu ritmo. Antes de comenzar... ¿cómo te gustaría llamarme?",
                        isBot = true
                    )
                )
            }
        } catch (e: Exception) {
            onboardingStep = BotOnboardingStep.ASK_NAME
            delay(400)
            messages = listOf(
                ChatMessage(
                    id    = "onboarding_0",
                    text  = "¡Hola, **$userName**! Qué alegría que estés aquí. Este es un espacio seguro para hablar con calma, sin juicios y a tu ritmo. Antes de comenzar... ¿cómo te gustaría llamarme?",
                    isBot = true
                )
            )
        }

        cargaInicialLista = true
        onConversacionCargada()
    }
    // ── Guardado automático de la conversación ───────────────────────
    // Espera un momento tras el último mensaje para no escribir en
    // Firestore en cada tecleo del bot.
    LaunchedEffect(messages) {
        if (messages.none { !it.isBot }) return@LaunchedEffect
        if (conversacionId.isBlank()) return@LaunchedEffect
        delay(1200)
        ConversacionesRepository.guardar(
            id       = conversacionId,
            mensajes = messages,
            inicioMs = if (conversacionInicioMs > 0) conversacionInicioMs else System.currentTimeMillis()
        )
    }

    // ── Emoción que llega desde el calendario ────────────────────────
    LaunchedEffect(emocionInicial, onboardingStep) {
        val emo = emocionInicial ?: return@LaunchedEffect
        if (onboardingStep == BotOnboardingStep.LOADING) return@LaunchedEffect

        val etiqueta = when (emo) {
            "mixto"      -> "con muchas emociones a la vez"
            "tranquila"  -> "tranquila"
            "feliz"      -> "feliz"
            "cansada"    -> "cansada"
            "ansiosa"    -> "ansiosa"
            "triste"     -> "triste"
            "molesta"    -> "molesta"
            else         -> "confundida"
        }

        val texto1 = "Vi que hoy te sentiste $etiqueta. ¿Quieres que hablemos de eso?"
        isTyping = true
        delay(tiempoTecleo(texto1))
        isTyping = false

        messages = messages + ChatMessage(
            id = System.currentTimeMillis().toString(),
            text = texto1,
            isBot = true
        )
        delay(tiempoLectura(texto1))

        if (onboardingStep != BotOnboardingStep.DONE) {
            val texto2 = "Antes te pediría completar los cuestionarios, así entiendo mejor lo que estás sintiendo."
            isTyping = true
            delay(tiempoTecleo(texto2))
            isTyping = false
            messages = messages + ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                text = texto2,
                isBot = true
            )
            showCuestionarioButtons = true
        } else {
            val texto3 = "Si eso que sentiste hoy tuviera un nombre, ¿cuál le pondrías? ¿En qué momento del día apareció?"
            isTyping = true
            delay(tiempoTecleo(texto3))
            isTyping = false
            messages = messages + ChatMessage(
                id = (System.currentTimeMillis() + 2).toString(),
                text = texto3,
                isBot = true
            )
        }
        onEmocionConsumida()
    }

    LaunchedEffect(aperturaCuestionarios, onboardingStep, cargaInicialLista) {
        if (!aperturaCuestionarios) return@LaunchedEffect
        if (onboardingStep == BotOnboardingStep.LOADING) return@LaunchedEffect
        if (!cargaInicialLista) return@LaunchedEffect

        // Burbuja de la usuaria, como si ella misma avisara que terminó
        messages = messages + ChatMessage(
            id = System.currentTimeMillis().toString(),
            text = "Ya completé los cuestionarios",
            isBot = false
        )
        isTyping = true
        delay(1000)
        isTyping = false

        val franjaResultado = franjaDeResultado(puntajeEpdsUlt, puntajePassUlt, item10Ult)

        val burbujas = aperturaSegunResultados(
            puntajeEpdsUlt, puntajePassUlt, item10Ult,
            nombreProfesional = nombreProfesionalAsignado
        )

        burbujas.forEachIndexed { i, texto ->
            isTyping = true
            delay(tiempoTecleo(texto))
            isTyping = false
            messages = messages + ChatMessage(
                id    = (System.currentTimeMillis() + i).toString(),
                text  = texto,
                isBot = true,
                mostrarContactoProfesional = (
                        i == burbujas.lastIndex &&
                                (franjaResultado == FranjaResultado.ALERTA || franjaResultado == FranjaResultado.ALTA)
                        )
            )
            // Pausa de lectura antes de la siguiente burbuja.
            if (i < burbujas.lastIndex) delay(tiempoLectura(texto))
        }

        // El nombre y número del profesional ya se presentaron una sola vez
        // durante el onboarding (mensajeInfoProfesional), así que aquí no se
        // repite. Si hay alerta real, el botón de WhatsApp (mostrado en la
        // última burbuja de arriba) es el único paso adicional necesario.
        showOptions = false
        onAperturaConsumida()
    }
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        if (secuenciaEnCurso) return   // FIX: espera a que termine la presentación inicial
        messages  = messages + ChatMessage(id = System.currentTimeMillis().toString(), text = text, isBot = false)
        inputText = ""
        isTyping  = true

        scope.launch {
            when (onboardingStep) {
                BotOnboardingStep.LOADING -> { isTyping = false }

                BotOnboardingStep.ASK_NAME -> {
                    botName = text.trim().replaceFirstChar { it.uppercase() }
                    val texto1 = "¡Qué bonito nombre! Mucho gusto, **$userName**. Soy **$botName**."
                    delay(tiempoTecleo(texto1))
                    isTyping = false
                    messages = messages + ChatMessage(
                        id    = (System.currentTimeMillis() + 1).toString(),
                        text  = texto1,
                        isBot = true
                    )
                    delay(tiempoLectura(texto1))

                    val texto2 = "A veces los colores también cuentan historias.\n\n¿Qué color le darías a nuestro espacio?\n\nPuedes elegir un botón, escribir un color, una emoción o una palabra que conecte contigo."
                    isTyping = true
                    delay(tiempoTecleo(texto2))
                    isTyping = false
                    messages = messages + ChatMessage(
                        id    = (System.currentTimeMillis() + 2).toString(),
                        text = texto2,
                        isBot = true
                    )
                    onboardingStep = BotOnboardingStep.ASK_COLOR
                }

                BotOnboardingStep.ASK_COLOR -> {
                    // El indicador de "escribiendo" dura lo que tarda la
                    // detección real del color; no hace falta un delay extra.
                    val resultado = detectarColorDesdeIA(text)
                    isTyping = false

                    if (!resultado.detectado) {
                        messages = messages + ChatMessage(
                            id    = (System.currentTimeMillis() + 1).toString(),
                            text  = "A veces el color que necesitamos no tiene nombre exacto... Quizás uno de estos se acerca a lo que sientes hoy: **Rosa**, **Azul**, **Verde**, **Lavanda**, **Durazno** o **Lila**.",
                            isBot = true
                        )
                        return@launch
                    }

                    botColor       = Color(resultado.r, resultado.g, resultado.b)
                    botColorNombre = resultado.nombre

                    messages = messages + ChatMessage(
                        id    = (System.currentTimeMillis() + 1).toString(),
                        text  = "He elegido el color **${resultado.nombre}** para nuestro espacio.\n\n${resultado.frase}\n\n¿Te gusta?",
                        isBot = true
                    )
                    showColorConfirm = true
                }

                BotOnboardingStep.ANNOUNCE_CUESTIONARIOS -> {
                    val texto = "Usa los botones de abajo para iniciar cada cuestionario"
                    delay(tiempoTecleo(texto))
                    isTyping = false
                    messages = messages + ChatMessage(
                        id    = (System.currentTimeMillis() + 1).toString(),
                        text  = texto,
                        isBot = true
                    )
                }

                BotOnboardingStep.DONE -> {
                    showOptions = false
                    mostrarBotonesReintento = false

                    // NUEVO: "¿quién es mi psicólogo?", "dame el número de la psicóloga"...
                    // Se responde con los datos reales del profesional asignado.
                    if (esPreguntaPorProfesional(text)) {
                        val textoProf = textoInfoProfesional(nombreProfesionalAsignado, whatsappProfesionalAsignado)
                        delay(tiempoTecleo(textoProf))
                        isTyping = false
                        messages = messages + ChatMessage(
                            id    = (System.currentTimeMillis() + 1).toString(),
                            text  = textoProf,
                            isBot = true,
                            mostrarContactoProfesional = !whatsappProfesionalAsignado.isNullOrBlank()
                        )
                        return@launch
                    }
                    val historialIA = messages.takeLast(10).map {
                        MensajeHistorial(role = if (it.isBot) "assistant" else "user", content = it.text)
                    }
                    val respuestaChat = responderChat(
                        mensaje = text,
                        botName = botName,
                        nombreUsuario = "",
                        usuarioUid = uid,
                        historial = historialIA,
                        appContext = context      // FIX: pasa el contexto para chequear conectividad
                    )
                    Log.d(
                        "ChatbotScreen",
                        "respuestaChat: cat=${respuestaChat.categoria} alerta=${respuestaChat.requiereAlerta} " +
                                "origen=${respuestaChat.origen}"
                    )
                    // Antes: delay(tiempoTecleo(respuestaChat.texto)), hasta 2.2s
                    // MÁS ENCIMA del tiempo que ya tomó la llamada de red. Se pidió
                    // que la espera total no pase de ~2s, así que acá el "escribiendo"
                    // es un pestañeo fijo, no proporcional al largo del texto.
                    delay(300)
                    isTyping = false
                    messages = messages + ChatMessage(
                        id    = (System.currentTimeMillis() + 1).toString(),
                        text  = respuestaChat.texto,
                        isBot = true,
                        mostrarContactoProfesional = respuestaChat.requiereAlerta
                    )
                }
            }
        }
    }

    fun seleccionarColor(nombre: String, color: Color) {
        // FIX: si ya se eligió el color o el bot está presentando la info, no hace nada.
        if (secuenciaEnCurso || onboardingStep != BotOnboardingStep.ASK_COLOR || showColorConfirm) return
        botColor = color; botColorNombre = nombre
        messages = messages + ChatMessage(id = System.currentTimeMillis().toString(), text = nombre, isBot = false)
        isTyping = true
        scope.launch {
            delay(900); isTyping = false
            messages = messages + ChatMessage(
                id    = (System.currentTimeMillis() + 1).toString(),
                text  = "He elegido el color **$nombre**\n\n${obtenerFraseColor(nombre)}\n\n¿Te gusta?",
                isBot = true
            )
            showColorConfirm = true
        }
    }

    /** Reabre el flujo de color desde ajustes, con marco narrativo. */
    fun iniciarCambioDeColor() {
        recoloreando     = true
        showColorConfirm = false
        showOptions      = false
        onboardingStep   = BotOnboardingStep.ASK_COLOR
        scope.launch {
            isTyping = true; delay(900); isTyping = false
            messages = messages + ChatMessage(
                id    = System.currentTimeMillis().toString(),
                text  = "Las personas cambiamos, y los espacios que habitamos también pueden hacerlo.\n\nSi hoy tuvieras que darle otro color a este lugar, ¿cuál sería? Escríbelo o elige uno de los botones.",
                isBot = true
            )
        }
    }

    // ── Teclado abierto: la caja de texto sube y la lista baja al último mensaje ──
    // FIX lag: antes se leía la altura del teclado en cada cuadro de su animación y se
    // redibujaba TODA la pantalla ~60 veces por segundo. isImeVisible solo cambia
    // una vez (al abrir y al cerrar).
    val tecladoAbierto = WindowInsets.isImeVisible
    val densidad = LocalDensity.current
    var altoBarraNav by remember { mutableStateOf(0.dp) }
    LaunchedEffect(tecladoAbierto) {
        // scrollToItem (sin animación) para que no se vea "lag" mientras sube el teclado.
        if (tecladoAbierto && messages.isNotEmpty()) listState.scrollToItem(messages.size - 1)
    }

    // ── Dictado por voz ──
    val abrirDictado = rememberDictado { dictado ->
        corrigiendoVoz = true
        scope.launch {
            val corregido = corregirDictado(dictado)
            inputText = if (inputText.isBlank()) corregido else "${inputText.trimEnd()} $corregido"
            corrigiendoVoz = false
        }
    }

    // ── UI ────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = BlancoFondo,
        topBar = {
            ChatTopBar(
                botName          = if (botName.isBlank()) "Tu acompañante" else botName,
                botColor         = botColor,
                hasText          = inputText.isNotEmpty(),
                onboardingStep   = onboardingStep,
                mostrarMascota   = mostrarMascota,
                onAbrirCuaderno  = onAbrirCuaderno,
                onCambiarColor   = { iniciarCambioDeColor() },
                onToggleMascota  = {
                    mostrarMascota = !mostrarMascota
                    prefs.edit().putBoolean("mostrar_mascota_$uid", mostrarMascota).apply()
                },
                onNavigateToIAOffline = onNavigateToIAOffline,
                onNavigateToAyuda     = onNavigateToAyuda,
                onLogout         = onLogout
            )
        },

        bottomBar = {
            // El teclado TAPA la barra de navegación (como WhatsApp): la columna solo sube
            // lo que el teclado mide de más sobre esa barra. Así la caja de texto queda
            // pegada al teclado, la barra "desaparece" debajo y no hay saltos.
            // Requiere android:windowSoftInputMode="adjustResize".
            Column(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.ime.exclude(WindowInsets(bottom = altoBarraNav))
                )
            ) {
                AnimatedVisibility(
                    visible = showCuestionarioButtons &&
                            onboardingStep == BotOnboardingStep.ANNOUNCE_CUESTIONARIOS && !isTyping,
                    enter = fadeIn() + slideInVertically { it },
                    exit  = fadeOut() + slideOutVertically { it }
                ) {
                    CuestionariosBar(botColor = botColor, onEpdsClick = onNavigateToEpds, onPassClick = onNavigateToPass)
                }

                AnimatedVisibility(
                    visible = showOptions && onboardingStep == BotOnboardingStep.DONE && !isTyping,
                    enter = fadeIn() + slideInVertically { it },
                    exit  = fadeOut() + slideOutVertically { it }
                ) {
                    QuickOptionsBar(options = quickOptions, botColor = botColor, onSelect = { sendMessage(it) })
                }

                // Recordatorio de retomar EPDS/PASS pasada la periodicidad recomendada.
                AnimatedVisibility(
                    visible = mostrarBotonesReintento && onboardingStep == BotOnboardingStep.DONE && !isTyping,
                    enter = fadeIn() + slideInVertically { it },
                    exit  = fadeOut() + slideOutVertically { it }
                ) {
                    CuestionariosBar(botColor = botColor, onEpdsClick = onNavigateToEpds, onPassClick = onNavigateToPass)
                }

                AnimatedVisibility(
                    visible = showColorConfirm && !isTyping,
                    enter = fadeIn() + slideInVertically { it },
                    exit  = fadeOut()
                ) {
                    Surface(color = Color.White, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 10.dp)) {
                            Text(
                                "¿Te gusta este color?",
                                fontSize = 11.sp, color = GrisTxt,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // ── Sí, me gusta ──
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(botColor)
                                        .clickable {
                                            showColorConfirm = false
                                            guardarBotConfig(botName, botColor)
                                            prefs.edit()
                                                .putString("bot_nombre_$uid", botName)
                                                .putString("bot_color_$uid", colorToHex(botColor))
                                                .apply()

                                            if (recoloreando) {
                                                recoloreando   = false
                                                onboardingStep = BotOnboardingStep.DONE
                                                scope.launch {
                                                    isTyping = true; delay(800); isTyping = false
                                                    messages = messages + ChatMessage(
                                                        id    = System.currentTimeMillis().toString(),
                                                        text  = "Listo, nuestro espacio ya tiene su color nuevo. Gracias por dejarme acompañarte también en los cambios pequeños.",
                                                        isBot = true
                                                    )
                                                }
                                            } else {
                                                // FIX: se sale de ASK_COLOR de inmediato, así los botones de
                                                // color desaparecen y no interrumpen la presentación.
                                                secuenciaEnCurso = true
                                                onboardingStep   = BotOnboardingStep.ANNOUNCE_CUESTIONARIOS
                                                scope.launch {
                                                    val textoInfoProfesional = mensajeInfoProfesional(nombreProfesionalAsignado, whatsappProfesionalAsignado)
                                                    val textoInvitacionCuestionarios = "Quiero pedirte algo antes de continuar.\n\nVoy a compartirte dos cuestionarios que me ayudarán a entenderte mejor."
                                                    val textoExplicacionCuestionarios = "El **EPDS** — Escala de Depresión Postnatal de Edimburgo — es una forma de explorar cómo has estado cargando tus emociones estas semanas.\n\nEl **PASS** — Escala de Ansiedad Perinatal — es un espacio para nombrar esas preocupaciones que a veces no encuentran palabras.\n\nNo hay respuestas correctas. Solo las tuyas."

                                                    isTyping = true; delay(tiempoTecleo(textoInfoProfesional)); isTyping = false
                                                    messages = messages + ChatMessage(
                                                        id    = System.currentTimeMillis().toString(),
                                                        text  = textoInfoProfesional,
                                                        isBot = true
                                                    )
                                                    delay(tiempoLectura(textoInfoProfesional))

                                                    isTyping = true; delay(tiempoTecleo(textoInvitacionCuestionarios)); isTyping = false
                                                    messages = messages + ChatMessage(
                                                        id    = (System.currentTimeMillis() + 1).toString(),
                                                        text  = textoInvitacionCuestionarios,
                                                        isBot = true
                                                    )
                                                    delay(tiempoLectura(textoInvitacionCuestionarios))

                                                    isTyping = true; delay(tiempoTecleo(textoExplicacionCuestionarios)); isTyping = false
                                                    messages = messages + ChatMessage(
                                                        id    = (System.currentTimeMillis() + 2).toString(),
                                                        text  = textoExplicacionCuestionarios,
                                                        isBot = true
                                                    )
                                                    delay(tiempoLectura(textoExplicacionCuestionarios))
                                                    onboardingStep          = BotOnboardingStep.ANNOUNCE_CUESTIONARIOS
                                                    showCuestionarioButtons = true
                                                    secuenciaEnCurso        = false
                                                }
                                            }
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Text("Sí, me gusta", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }

                                // ── No, cambiar ──
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                        // CAMBIO 1: El borde ahora es un gris neutro suave
                                        .border(1.5.dp, Color(0xFF757575).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .clickable {
                                            showColorConfirm = false
                                            onboardingStep   = BotOnboardingStep.ASK_COLOR
                                            scope.launch {
                                                isTyping = true; delay(800); isTyping = false
                                                messages = messages + ChatMessage(
                                                    id    = System.currentTimeMillis().toString(),
                                                    text  = "¡Sin problema! ¿Qué color te gustaría entonces? Puedes elegir de los botones o escribirlo",
                                                    isBot = true
                                                )
                                            }
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    // CAMBIO 2: El texto reemplaza MoradoOscuro por un gris muy oscuro / negro
                                    Text("No, cambiar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C2C2C))
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = onboardingStep == BotOnboardingStep.ASK_COLOR && !isTyping &&
                            !showColorConfirm && !secuenciaEnCurso,
                    enter = fadeIn() + slideInVertically { it },
                    exit  = fadeOut()
                ) {
                    ColorPickerBar(colores = coloresBot, onSelect = { n, c -> seleccionarColor(n, c) })
                }

                if (onboardingStep != BotOnboardingStep.LOADING) {
                    ChatInputBar(
                        value    = inputText,
                        onChange = { inputText = it },
                        onSend   = { sendMessage(inputText) },
                        botColor = botColor,
                        onMic    = if (onboardingStep == BotOnboardingStep.DONE) abrirDictado else null,
                        corrigiendoVoz = corrigiendoVoz,
                        hint     = when (onboardingStep) {
                            BotOnboardingStep.ASK_NAME               -> "Escribe un nombre para mí..."
                            BotOnboardingStep.ASK_COLOR              -> "Escribe un color..."
                            BotOnboardingStep.ANNOUNCE_CUESTIONARIOS -> "Escribe algo o usa los botones de arriba..."
                            else                                     -> "Escribe cómo te sientes..."
                        }
                    )
                }

                // Se mide una vez para saber cuánto tapa el teclado.
                Box(modifier = Modifier.onSizeChanged { altoBarraNav = with(densidad) { it.height.toDp() } }) {
                    BottomNavBar(
                        onAnalysis = onNavigateToAnalysis,
                        onCalendar = onNavigateToCalendar,
                        botColor   = botColor
                    )
                }
            }
        }

    ) { paddingValues ->

        if (onboardingStep == BotOnboardingStep.LOADING) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Lavanda) }
            return@Scaffold
        }

        LazyColumn(
            state               = listState,
            modifier            = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding      = PaddingValues(vertical = 16.dp)
        ) {
            itemsIndexed(messages, key = { _, msg -> msg.id }) { index, msg ->
                AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Column {
                        if (msg.isBot) {
                            val isLastInGroup = index == messages.lastIndex || !messages[index + 1].isBot
                            BotMessageBubble(
                                text           = msg.text,
                                botColor       = botColor,
                                showAvatar     = isLastInGroup,
                                mostrarMascota = mostrarMascota
                            )
                        } else {
                            UserMessageBubble(text = msg.text, botColor = botColor)
                        }
                        if (msg.mostrarContactoProfesional) {
                            Spacer(Modifier.height(8.dp))
                            BotonContactoProfesional(
                                nombreProfesional   = nombreProfesionalAsignado,
                                whatsappProfesional = whatsappProfesionalAsignado,
                                botColor            = botColor,
                                mostrarMascota       = mostrarMascota
                            )
                        }
                    }
                }
            }
            if (isTyping) { item { TypingIndicator(botColor = botColor, mostrarMascota = mostrarMascota) } }
        }
    }
}

// ─────────────────────────────────────────────
//  TOP BAR
// ─────────────────────────────────────────────
@Composable
fun ChatTopBar(
    botName: String,
    botColor: Color,
    hasText: Boolean,
    onboardingStep: BotOnboardingStep,
    mostrarMascota: Boolean = true,
    onAbrirCuaderno: () -> Unit = {},
    onCambiarColor: () -> Unit = {},
    onToggleMascota: () -> Unit = {},
    onNavigateToIAOffline: () -> Unit = {},
    onNavigateToAyuda: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotMascota(
                color    = botColor,
                hasText  = hasText,
                size     = 44.dp,
                parpadea = mostrarMascota
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = botName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MoradoOscuro)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (onboardingStep) {
                                    BotOnboardingStep.DONE -> Color(0xFF4CAF50)
                                    else                   -> botColor
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when (onboardingStep) {
                            BotOnboardingStep.DONE                   -> "En línea"
                            BotOnboardingStep.ANNOUNCE_CUESTIONARIOS -> "Cuestionarios pendientes"
                            else                                     -> "Configurando..."
                        },
                        fontSize = 12.sp,
                        color    = GrisTxt
                    )
                }
            }

            // ── Cuaderno de conversaciones ──
            IconButton(onClick = onAbrirCuaderno) {
                Icon(
                    imageVector        = Icons.Default.AutoStories,
                    contentDescription = "Mis conversaciones",
                    tint               = GrisTxt,
                    modifier           = Modifier.size(22.dp)
                )
            }

            // ── Menú de ajustes ──
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector        = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint               = GrisTxt,
                        modifier           = Modifier.size(22.dp)
                    )
                }
                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier         = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text    = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, contentDescription = null,
                                    tint = MoradoMedio, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Cambiar color", color = MoradoOscuro, fontSize = 14.sp)
                            }
                        },
                        onClick = { showMenu = false; onCambiarColor() }
                    )

                    DropdownMenuItem(
                        text    = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (mostrarMascota) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MoradoMedio, modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (mostrarMascota) "Ocultar acompañante" else "Mostrar acompañante",
                                    color = MoradoOscuro, fontSize = 14.sp
                                )
                            }
                        },
                        onClick = { showMenu = false; onToggleMascota() }
                    )

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    // ── Geolocalización: Necesito ayuda ──
                    DropdownMenuItem(
                        text    = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null,
                                    tint = MoradoMedio, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Necesito ayuda", color = MoradoOscuro, fontSize = 14.sp)
                                    Text("Geolocalización", color = GrisTxt, fontSize = 11.sp)
                                }
                            }
                        },
                        onClick = { showMenu = false; onNavigateToAyuda() }
                    )

                    DropdownMenuItem(
                        text    = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudOff, contentDescription = null,
                                    tint = MoradoMedio, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("IA offline", color = MoradoOscuro, fontSize = 14.sp)
                            }
                        },
                        onClick = { showMenu = false; onNavigateToIAOffline() }
                    )

                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    DropdownMenuItem(
                        text    = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null,
                                    tint = RojoTexto, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Cerrar sesión", color = RojoTexto, fontSize = 14.sp)
                            }
                        },
                        onClick = {
                            showMenu = false
                            FirebaseAuth.getInstance().signOut()
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  BARRA DE CUESTIONARIOS
// ─────────────────────────────────────────────
@Composable
fun CuestionariosBar(botColor: Color, onEpdsClick: () -> Unit, onPassClick: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text("Cuestionarios pendientes", fontSize = 11.sp, color = GrisTxt,
                fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape           = RoundedCornerShape(14.dp),
                    color           = Color.White,
                    shadowElevation = 4.dp,
                    modifier        = Modifier
                        .weight(1f)
                        .border(1.5.dp, MoradoMedio.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { onEpdsClick() }
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 28.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("EPDS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MoradoOscuro)
                        Text("Escala depresión\npostnatal de Edimburgo", fontSize = 11.sp, color = GrisTxt, textAlign = TextAlign.Center, lineHeight = 15.sp)
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(50.dp), color = LavandaSuave,
                            modifier = Modifier.border(1.dp, MoradoMedio.copy(alpha = 0.3f), RoundedCornerShape(50.dp))) {
                            Text("10 preguntas", fontSize = 11.sp, color = MoradoMedio, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                }
                Surface(
                    shape           = RoundedCornerShape(14.dp),
                    color           = Color.White,
                    shadowElevation = 4.dp,
                    modifier        = Modifier
                        .weight(1f)
                        .border(1.5.dp, Color(0xFFE8A0BF).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .clickable { onPassClick() }
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧠", fontSize = 28.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("PASS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB03060))
                        Text("Escala de ansiedad\nperinatal", fontSize = 11.sp, color = GrisTxt, textAlign = TextAlign.Center, lineHeight = 15.sp)
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(50.dp), color = Color(0xFFFFF0F8),
                            modifier = Modifier.border(1.dp, Color(0xFFE8A0BF).copy(alpha = 0.5f), RoundedCornerShape(50.dp))) {
                            Text("31 preguntas", fontSize = 11.sp, color = Color(0xFFB03060), fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  BURBUJA BOT
// ─────────────────────────────────────────────
/**
 * Convierte el marcado propio del chat en un AnnotatedString con
 * enlaces reales.
 *
 *   **negrita**                    → texto en negrita, color del bot
 *   [[tel:106:Línea 106]]          → subrayado, clic abre el marcador
 *   [[wa:3001234567:Nombre]]       → subrayado, clic abre WhatsApp
 *
 * Corre DESPUÉS de personalizar() en AperturasNarrativas.kt, así que
 * para cuando llega aquí {PROF_CAP} ya es el nombre real del profesional.
 */
private val REGEX_ENLACE  = "\\[\\[(tel|wa):([^:\\]]+):([^\\]]+)\\]\\]"
private val REGEX_NEGRITA = "\\*\\*(.+?)\\*\\*"

private fun textoConEnlaces(
    texto: String,
    botColor: Color,
    context: android.content.Context
): AnnotatedString = buildAnnotatedString {

    var cursor = 0
    val combinada = Regex("$REGEX_ENLACE|$REGEX_NEGRITA")

    combinada.findAll(texto).forEach { m ->
        if (m.range.first > cursor) append(texto.substring(cursor, m.range.first))

        val esEnlace = m.groupValues[1].isNotEmpty()
        if (esEnlace) {
            val tipo  = m.groupValues[1]   // "tel" o "wa"
            val valor = m.groupValues[2]   // número
            val label = m.groupValues[3]   // texto visible

            val accion = LinkAnnotation.Clickable(
                tag = "$tipo:$valor",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = botColor,
                        fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                ),
                linkInteractionListener = {
                    try {
                        val intent = if (tipo == "tel") {
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$valor"))
                        } else {
                            val numero = valor.filter { it.isDigit() }
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$numero"))
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) { /* sin app de teléfono o WhatsApp disponible */ }
                }
            )
            withLink(accion) { append(label) }
        } else {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = botColor)) {
                append(m.groupValues[4])
            }
        }
        cursor = m.range.last + 1
    }
    if (cursor < texto.length) append(texto.substring(cursor))
}

@Composable
fun BotMessageBubble(
    text: String,
    botColor: Color,
    showAvatar: Boolean = true,
    mostrarMascota: Boolean = true
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val annotated = remember(text, botColor) { textoConEnlaces(text, botColor, context) }

    val forma = if (mostrarMascota)
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    else
        RoundedCornerShape(18.dp)

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment     = Alignment.Bottom
    ) {
        if (mostrarMascota) {
            if (showAvatar) BotMascota(color = botColor, hasText = false, size = 32.dp)
            else Spacer(modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape           = forma,
            color           = Color.White,
            modifier        = Modifier
                .widthIn(max = if (mostrarMascota) 280.dp else 300.dp)
                .border(1.dp, botColor.copy(alpha = 0.25f), forma),
            shadowElevation = 2.dp
        ) {
            Text(text = annotated, fontSize = 15.sp, color = Color(0xFF333355), lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  BURBUJA USUARIO
// ─────────────────────────────────────────────
@Composable
fun UserMessageBubble(text: String, botColor: Color) {
    val forma = RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            shape           = forma,
            color           = Color(0xFFF7F7F9),
            modifier        = Modifier
                .widthIn(max = 280.dp)
                .border(1.dp, Color(0xFFE4E4EA), forma),
            shadowElevation = 1.dp
        ) {
            Text(text = text, fontSize = 15.sp, color = Color(0xFF333333), lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  TYPING INDICATOR
// ─────────────────────────────────────────────
@Composable
fun TypingIndicator(botColor: Color, mostrarMascota: Boolean = true) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Bottom) {
        if (mostrarMascota) {
            BotMascota(color = botColor, hasText = false, size = 32.dp)
            Spacer(Modifier.width(8.dp))
        }
        Surface(
            shape = if (mostrarMascota)
                RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
            else RoundedCornerShape(18.dp),
            color = Color.White, shadowElevation = 2.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue  = 0.3f,
                        targetValue   = 1f,
                        animationSpec = infiniteRepeatable(
                            animation          = tween(600, easing = EaseInOut),
                            repeatMode         = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(index * 150)
                        ),
                        label = "dot_$index"
                    )
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(botColor.copy(alpha = alpha)))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  OPCIONES RÁPIDAS
// ─────────────────────────────────────────────
@Composable
fun QuickOptionsBar(options: List<QuickOption>, botColor: Color, onSelect: (String) -> Unit) {
    Surface(color = Color.White, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Text("¿Cómo te sientes?", fontSize = 11.sp, color = GrisTxt, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(options) { opt ->
                    val bgColor     = if (opt.isSOS) RojoSuave else Color.White
                    val borderColor = if (opt.isSOS) RojoBorde else Color(0xFFDDDDDD)
                    val textColor   = if (opt.isSOS) RojoTexto else MoradoOscuro
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(bgColor)
                            .border(1.5.dp, borderColor, RoundedCornerShape(50.dp))
                            .clickable { onSelect(opt.label) }
                            .padding(
                                start  = if (opt.icono != null) 8.dp else 16.dp,
                                end    = 16.dp,
                                top    = if (opt.icono != null) 6.dp else 10.dp,
                                bottom = if (opt.icono != null) 6.dp else 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (opt.icono != null) {
                            Image(
                                painter            = painterResource(id = opt.icono),
                                contentDescription = null,
                                modifier           = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(text = opt.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  COLOR PICKER
// ─────────────────────────────────────────────
@Composable
fun ColorPickerBar(colores: List<Pair<String, Color>>, onSelect: (String, Color) -> Unit) {
    Surface(color = Color.White, shadowElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text("Elige un color para nuestro chat", fontSize = 11.sp, color = GrisTxt,
                fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                colores.forEach { (nombre, color) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelect(nombre, color) }) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color).border(2.dp, Color.White, CircleShape))
                        Spacer(Modifier.height(4.dp))
                        Text(text = nombre, fontSize = 10.sp, color = GrisTxt, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  INPUT BAR
// ─────────────────────────────────────────────
@Composable
fun ChatInputBar(
    value: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
    botColor: Color,
    hint: String = "Escribe cómo te sientes...",
    onMic: (() -> Unit)? = null,
    corrigiendoVoz: Boolean = false
) {
    Surface(color = Color.White, shadowElevation = 0.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(28.dp))
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) Text(
                    text = if (corrigiendoVoz) "Escribiendo lo que dijiste..." else hint,
                    fontSize = 14.sp, color = Color(0xFFB0B0C8)
                )
                BasicTextField(
                    value = value,
                    onValueChange = { nuevoTexto -> onChange(capitalizarPrimeraLetra(nuevoTexto)) },
                    textStyle = TextStyle(fontSize = 14.sp, color = MoradoOscuro),
                    cursorBrush = SolidColor(botColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // ── Botón de voz ──
            if (onMic != null) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, botColor, CircleShape)
                        .clickable(enabled = !corrigiendoVoz) { onMic() },
                    contentAlignment = Alignment.Center
                ) {
                    if (corrigiendoVoz) {
                        CircularProgressIndicator(color = botColor, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Hablar en vez de escribir",
                            tint = botColor, modifier = Modifier.size(22.dp))
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(botColor).clickable { onSend() },
                contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar",
                    tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
//  BOTTOM NAV BAR
// ─────────────────────────────────────────────
@Composable
fun BottomNavBar(
    onAnalysis: () -> Unit,
    onCalendar: () -> Unit,
    onChat: () -> Unit = {},
    botColor: Color = Color(0xFF3A3D8C)
) {
    Surface(color = Color.White, shadowElevation = 12.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            NavItem(icono = R.drawable.analisis, label = "Análisis", selected = false, onClick = onAnalysis)
            Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(botColor).clickable { onChat() },
                contentAlignment = Alignment.Center) { Text(text = "💬", fontSize = 24.sp) }
            NavItem(icono = R.drawable.calendario, label = "Calendario", selected = false, onClick = onCalendar)
        }
    }
}

@Composable
fun NavItem(icono: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter            = painterResource(id = icono),
            contentDescription = label,
            modifier           = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp,
            color = if (selected) MoradoMedio else GrisTxt,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}