package com.example.gestarbien.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─────────────────────────────────────────────────────────────
//  PALETA
// ─────────────────────────────────────────────────────────────
private val AnLavanda      = Color(0xFFB1B4E1)
private val AnLavandaSuave = Color(0xFFEEEFFE)
private val AnMoradoOscuro = Color(0xFF3A3D8C)
private val AnMoradoMedio  = Color(0xFF5C5FA8)
private val AnGris         = Color(0xFF888888)
private val AnFondo        = Color(0xFFFBFBFE)
private val AnNaranja      = Color(0xFFE6A15C)

// ─────────────────────────────────────────────────────────────
//  MODELOS
// ─────────────────────────────────────────────────────────────
private data class RegistroAnimoDia(
    val fecha: String,
    val animo: String,
    val animos: List<String>,
    val intensidad: Int
)

private data class ResultadoCuestionario(
    val puntaje: Int,
    val fechaMs: Long,
    val alertaItem10: Boolean = false
)

private data class BarraDia(val etiqueta: String, val registro: RegistroAnimoDia?)

private data class InfoAnimo(val etiqueta: String, val icono: Int, val color: Color)

private val CATALOGO_ANIMOS = mapOf(
    "tranquila"  to InfoAnimo("Tranquila",  R.drawable.animo_tranquila,  Color(0xFF9FD8CB)),
    "feliz"      to InfoAnimo("Feliz",      R.drawable.animo_feliz,      Color(0xFFF3B6CE)),
    "cansada"    to InfoAnimo("Cansada",    R.drawable.animo_cansada,    Color(0xFFB6C2D9)),
    "ansiosa"    to InfoAnimo("Ansiosa",    R.drawable.animo_ansiosa,    Color(0xFFF2BE96)),
    "triste"     to InfoAnimo("Triste",     R.drawable.animo_triste,     Color(0xFF9FBCE3)),
    "molesta"    to InfoAnimo("Molesta",    R.drawable.animo_molesta,    Color(0xFFEC9A94)),
    "confundida" to InfoAnimo("Confundida", R.drawable.animo_confundida, Color(0xFFF0D68C)),
    "mixto"      to InfoAnimo("Emoción mixta", R.drawable.animo_mixto,   Color(0xFFC9B7E8))
)

private val ANIMOS_DIFICILES = setOf("triste", "ansiosa", "molesta")
private val DIAS_CORTOS = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
private val fmtClaveAn  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

private const val DIAS_ESPERA_RETOMAR      = 7
private const val DIAS_VENTANA_FRECUENCIAS = 30
private const val DIAS_VENTANA_ALERTA      = 7
private const val MINIMO_DIAS_DIFICILES    = 4

// ─────────────────────────────────────────────────────────────
//  EXPLICACIÓN DEL PUNTAJE
//  Solo se muestra si la usuaria toca el icono de información.
// ─────────────────────────────────────────────────────────────
private fun explicacionEpds(p: Int): String = when {
    p < 10 -> "Un puntaje por debajo de 10 sugiere que, en estas semanas, los síntomas de tristeza " +
            "aparecieron poco o de forma leve."
    p < 13 -> "Entre 10 y 12 se suele recomendar repetir la escala en unas semanas y comentarlo en " +
            "tu próximo control, para ver si algo cambia."
    else   -> "De 13 en adelante, lo indicado es conversarlo con tu profesional. No es un diagnóstico: " +
            "es una señal de que vale la pena mirarlo con calma y acompañada."
}

private fun explicacionPass(p: Int): String = when {
    p <= 20 -> "Hasta 20 puntos, la escala no detecta síntomas de ansiedad significativos en estas semanas."
    p <= 41 -> "Entre 21 y 41 se describen síntomas leves o moderados. Conviene comentarlo en tu " +
            "próximo control y observar cómo evoluciona."
    else    -> "De 42 en adelante, lo indicado es conversarlo con tu profesional. No es un diagnóstico: " +
            "es una señal de que vale la pena mirarlo con calma y acompañada."
}

private fun marcarParaSeguimiento(uid: String, motivo: String) {
    if (uid.isBlank()) return
    FirebaseFirestore.getInstance()
        .collection("usuarios").document(uid)
        .set(
            mapOf(
                "requiereSeguimiento" to true,
                "motivoSeguimiento"   to motivo,
                "fechaSeguimientoMs"  to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )
}

// ─────────────────────────────────────────────────────────────
//  PANTALLA
// ─────────────────────────────────────────────────────────────
@Composable
fun AnalysisScreen(
    botColor: Color = AnLavanda,
    onChatbotClick: () -> Unit,
    onCalendarioClick: () -> Unit,
    onRetomarEpds: () -> Unit,
    onRetomarPass: () -> Unit
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db  = remember { FirebaseFirestore.getInstance() }
    val prefs = remember { context.getSharedPreferences("gestarbien_prefs", Context.MODE_PRIVATE) }

    var cargando      by remember { mutableStateOf(true) }
    var barrasSemana  by remember { mutableStateOf<List<BarraDia>>(emptyList()) }
    var frecuencias   by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var epds          by remember { mutableStateOf<ResultadoCuestionario?>(null) }
    var pass          by remember { mutableStateOf<ResultadoCuestionario?>(null) }
    var mostrarInfo   by remember { mutableStateOf(false) }
    var infoPuntaje   by remember { mutableStateOf<String?>(null) }
    var avisoVisible  by remember { mutableStateOf(false) }
    var avisoItem10   by remember { mutableStateOf(false) }
    var ejercicio     by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        if (uid.isBlank()) { cargando = false; return@LaunchedEffect }

        val diasSemana = (6 downTo 0).map { offset ->
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
            fmtClaveAn.format(c.time) to DIAS_CORTOS[(c.get(Calendar.DAY_OF_WEEK) + 5) % 7]
        }

        val claveLimiteFrec = Calendar.getInstance()
            .apply { add(Calendar.DAY_OF_YEAR, -DIAS_VENTANA_FRECUENCIAS) }
            .let { fmtClaveAn.format(it.time) }

        try {
            val snap = db.collection("usuarios").document(uid)
                .collection("registros_diarios").get().await()

            val mapa = snap.documents.associate { doc ->
                doc.id to RegistroAnimoDia(
                    fecha      = doc.id,
                    animo      = doc.getString("animo") ?: "",
                    animos     = (doc.get("animos") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    intensidad = (doc.getLong("intensidad") ?: 0L).toInt()
                )
            }

            barrasSemana = diasSemana.map { (clave, etiqueta) ->
                BarraDia(etiqueta, mapa[clave]?.takeIf { it.animo.isNotBlank() })
            }

            val conteo = mutableMapOf<String, Int>()
            mapa.filterKeys { it >= claveLimiteFrec }.values.forEach { r ->
                if (r.animo.isNotBlank()) conteo[r.animo] = (conteo[r.animo] ?: 0) + 1
            }
            val total = conteo.values.sum().coerceAtLeast(1)
            frecuencias = conteo.entries.sortedByDescending { it.value }.take(4)
                .map { it.key to (it.value * 100 / total) }

            // ── Revisión de la última semana ─────────────────
            val claveLimiteAlerta = Calendar.getInstance()
                .apply { add(Calendar.DAY_OF_YEAR, -DIAS_VENTANA_ALERTA) }
                .let { fmtClaveAn.format(it.time) }

            val diasDificiles = mapa.filterKeys { it >= claveLimiteAlerta }
                .values.count { r ->
                    val claves = if (r.animos.isNotEmpty()) r.animos else listOf(r.animo)
                    claves.any { it in ANIMOS_DIFICILES } && r.intensidad >= 4
                }

            if (diasDificiles >= MINIMO_DIAS_DIFICILES) {
                marcarParaSeguimiento(
                    uid,
                    "$diasDificiles de los últimos $DIAS_VENTANA_ALERTA días con emociones difíciles de alta intensidad"
                )
                val clave = "aviso_seg_${uid}_$claveLimiteAlerta"
                if (!prefs.getBoolean(clave, false)) {
                    avisoVisible = true
                    prefs.edit().putBoolean(clave, true).apply()
                }
            }
        } catch (_: Exception) { }

        try {
            val docs = db.collection("usuarios").document(uid)
                .collection("cuestionarios_clinicos").get().await()
                .documents.sortedByDescending { it.getTimestamp("fecha")?.toDate()?.time ?: 0L }

            epds = docs.firstOrNull { it.getString("tipo") == "EPDS" }?.let {
                ResultadoCuestionario(
                    puntaje      = (it.getLong("puntaje") ?: 0L).toInt(),
                    fechaMs      = it.getTimestamp("fecha")?.toDate()?.time ?: 0L,
                    alertaItem10 = it.getBoolean("alertaItem10") ?: false
                )
            }
            pass = docs.firstOrNull { it.getString("tipo") == "PASS" }?.let {
                ResultadoCuestionario(
                    puntaje = (it.getLong("puntaje") ?: 0L).toInt(),
                    fechaMs = it.getTimestamp("fecha")?.toDate()?.time ?: 0L
                )
            }

            // El aviso del ítem 10 se muestra una sola vez por cuestionario
            epds?.let { e ->
                if (e.alertaItem10) {
                    marcarParaSeguimiento(uid, "Marcó el ítem 10 del EPDS")
                    val clave = "aviso_item10_${uid}_${e.fechaMs}"
                    if (!prefs.getBoolean(clave, false)) {
                        avisoItem10 = true
                        prefs.edit().putBoolean(clave, true).apply()
                    }
                }
            }
        } catch (_: Exception) { }

        cargando = false
    }

    Scaffold(
        containerColor = AnFondo,
        bottomBar = {
            BottomNavBar(
                onAnalysis = {},
                onCalendar = onCalendarioClick,
                onChat     = onChatbotClick,
                botColor   = botColor
            )
        }
    ) { inner ->

        if (cargando) {
            Box(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = AnLavanda) }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(Modifier.height(6.dp))

            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Mi bienestar emocional",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AnMoradoOscuro,
                    modifier   = Modifier.weight(1f)
                )
                IconButton(onClick = { mostrarInfo = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Sobre estos cuestionarios",
                        tint = AnMoradoMedio, modifier = Modifier.size(19.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Aviso de seguimiento (una sola vez) ──────────
            if (avisoVisible) {
                AvisoPasajero(
                    texto  = "Estos días han sido intensos. Le avisé a tu profesional para que pueda " +
                            "acompañarte mejor. Tus notas siguen siendo tuyas.",
                    fondo  = Color(0xFFEFF5EC),
                    tinta  = Color(0xFF3F5C36),
                    onCerrar = { avisoVisible = false }
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Aviso del ítem 10 (una sola vez) ─────────────
            if (avisoItem10) {
                Surface(
                    shape    = RoundedCornerShape(14.dp),
                    color    = Color(0xFFFFF6EC),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                ) {
                    Row(Modifier.padding(start = 13.dp, top = 13.dp, bottom = 13.dp, end = 4.dp),
                        verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Favorite, contentDescription = null,
                            tint = AnNaranja, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "En tu último cuestionario apareció algo que merece acompañamiento. No tienes que resolverlo sola.",
                                fontSize = 11.sp, color = Color(0xFF7A5230), lineHeight = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Puedes hablarlo con tu profesional, o llamar a la Línea 106 en cualquier momento.",
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7A5230), lineHeight = 16.sp
                            )
                        }
                        IconButton(onClick = { avisoItem10 = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar",
                                tint = Color(0xFFB08A5E), modifier = Modifier.size(15.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            TarjetaAnimoSemanal(barrasSemana)

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 14.dp)
            ) {
                TarjetaCuestionario(
                    titulo = "EPDS", subtitulo = "Escala depresión postnatal de Edimburgo",
                    emoji = "📋", totalPreguntas = 10, resultado = epds, maxPuntaje = 30,
                    onInfo = { infoPuntaje = "EPDS" },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Spacer(Modifier.width(10.dp))
                TarjetaCuestionario(
                    titulo = "PASS", subtitulo = "Escala de ansiedad perinatal",
                    emoji = "🧠", totalPreguntas = 31, resultado = pass, maxPuntaje = 93,
                    onInfo = { infoPuntaje = "PASS" },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            Spacer(Modifier.height(14.dp))

            TarjetaAvisoRetomar(epds, pass, onChatbotClick)

            Spacer(Modifier.height(14.dp))

            TarjetaFrecuencias(frecuencias)

            Spacer(Modifier.height(14.dp))

            TarjetaRecursos(onAbrir = { ejercicio = it })

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Información general ──────────────────────────────────
    if (mostrarInfo) {
        AlertDialog(
            onDismissRequest = { mostrarInfo = false },
            containerColor   = Color.White,
            title = {
                Text("Sobre estos cuestionarios", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, color = AnMoradoOscuro)
            },
            text = {
                Text(
                    text = "El EPDS y el PASS son escalas validadas que se usan en el embarazo y el " +
                            "posparto para identificar síntomas de depresión y ansiedad. No son un " +
                            "diagnóstico: son una forma de ponerle números a cómo te has sentido, para " +
                            "que tú y tu equipo de salud puedan conversarlo con más claridad.",
                    fontSize = 12.sp, color = AnMoradoOscuro, lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarInfo = false }) {
                    Text("Entendido", color = AnMoradoMedio, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ── Explicación del puntaje concreto ─────────────────────
    val cual = infoPuntaje
    if (cual != null) {
        val resultado = if (cual == "EPDS") epds else pass
        AlertDialog(
            onDismissRequest = { infoPuntaje = null },
            containerColor   = Color.White,
            title = {
                Text("Tu puntaje en $cual", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, color = AnMoradoOscuro)
            },
            text = {
                Text(
                    text = if (resultado == null) "Todavía no has completado este cuestionario."
                    else if (cual == "EPDS") explicacionEpds(resultado.puntaje)
                    else explicacionPass(resultado.puntaje),
                    fontSize = 12.sp, color = AnMoradoOscuro, lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { infoPuntaje = null }) {
                    Text("Entendido", color = AnMoradoMedio, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    when (ejercicio) {
        "respiracion" -> DialogoRespiracion(botColor) { ejercicio = null }
        "meditacion"  -> DialogoMeditacion(botColor) { ejercicio = null }
    }
}

// ─────────────────────────────────────────────────────────────
//  AVISO PASAJERO REUTILIZABLE
// ─────────────────────────────────────────────────────────────
@Composable
private fun AvisoPasajero(texto: String, fondo: Color, tinta: Color, onCerrar: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = fondo,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
    ) {
        Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)) {
            Text(texto, fontSize = 11.sp, color = tinta,
                lineHeight = 17.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onCerrar, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar",
                    tint = tinta.copy(alpha = 0.7f), modifier = Modifier.size(15.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  GRÁFICO SEMANAL
// ─────────────────────────────────────────────────────────────
@Composable
private fun TarjetaAnimoSemanal(barras: List<BarraDia>) {
    var tocada by remember { mutableStateOf<String?>(null) }

    Surface(
        shape           = RoundedCornerShape(20.dp),
        color           = Color.White,
        shadowElevation = 3.dp,
        modifier        = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("CÓMO ME SENTÍ ESTA SEMANA", fontSize = 11.sp,
                fontWeight = FontWeight.Bold, color = AnMoradoMedio)

            Spacer(Modifier.height(16.dp))

            if (barras.none { it.registro != null }) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no registraste tu ánimo esta semana.\nHazlo desde el calendario para ver tu semana acá.",
                        fontSize = 12.sp, color = AnGris,
                        textAlign = TextAlign.Center, lineHeight = 18.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    barras.forEachIndexed { i, barra ->
                        val reg     = barra.registro
                        val esHoy   = i == barras.lastIndex
                        val esMixto = reg?.animo == "mixto" && reg.animos.isNotEmpty()

                        val fraccion = when {
                            reg == null        -> 0f
                            esMixto            -> 0.7f
                            reg.intensidad > 0 -> reg.intensidad / 5f
                            else               -> 0.5f
                        }
                        val alturaBarra = if (reg != null)
                            (100 * fraccion).dp.coerceAtLeast(16.dp) else 4.dp

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            if (esMixto) {
                                Column(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(alturaBarra)
                                        .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                                ) {
                                    reg!!.animos.forEach { clave ->
                                        val info = CATALOGO_ANIMOS[clave] ?: return@forEach
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .background(info.color)
                                                .clickable { tocada = clave }
                                        )
                                    }
                                }
                            } else {
                                val info = reg?.let { CATALOGO_ANIMOS[it.animo] }
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(alturaBarra)
                                        .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                                        .background(info?.color ?: Color(0xFFEDEDF5))
                                        .clickable(enabled = reg != null) { tocada = reg?.animo }
                                )
                            }

                            Spacer(Modifier.height(6.dp))
                            Text(
                                text       = if (esHoy) "Hoy" else barra.etiqueta,
                                fontSize   = 10.sp,
                                color      = if (esHoy) AnMoradoMedio else AnGris,
                                fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                val info = tocada?.let { CATALOGO_ANIMOS[it] }
                if (info != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(info.color))
                        Spacer(Modifier.width(7.dp))
                        Text(
                            info.etiqueta,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color      = AnGris
                        )
                    }
                } else {
                    Text("Toca una barra para ver de qué emoción se trata.",
                        fontSize = 10.sp, color = AnGris)
                }
                Spacer(Modifier.height(8.dp))

                Text(
                    text       = "La altura de cada barra indica qué tan fuerte lo sentiste: desde un poco hasta demasiado. Los días con varias emociones aparecen divididos.",
                    fontSize   = 10.sp,
                    color      = AnGris,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  TARJETA DE CUESTIONARIO
//  El puntaje va acompañado de un icono opcional que lo explica.
// ─────────────────────────────────────────────────────────────
@Composable
private fun TarjetaCuestionario(
    titulo: String,
    subtitulo: String,
    emoji: String,
    totalPreguntas: Int,
    resultado: ResultadoCuestionario?,
    maxPuntaje: Int,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diasDesde = resultado?.let { ((System.currentTimeMillis() - it.fechaMs) / 86_400_000L).toInt() }

    Surface(
        shape           = RoundedCornerShape(18.dp),
        color           = Color.White,
        shadowElevation = 3.dp,
        modifier        = modifier
    ) {
        Box(Modifier.fillMaxHeight()) {

            Column(Modifier.padding(14.dp).fillMaxHeight()) {

                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AnLavandaSuave),
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 17.sp) }

                Spacer(Modifier.height(8.dp))

                Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AnMoradoOscuro)
                Text(subtitulo, fontSize = 10.sp, color = AnGris, lineHeight = 13.sp, minLines = 2)

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(8.dp))

                if (resultado == null) {
                    Surface(shape = RoundedCornerShape(10.dp), color = AnLavandaSuave) {
                        Text(
                            text = "$totalPreguntas preguntas", fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold, color = AnMoradoMedio,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Aún no lo has hecho", fontSize = 11.sp, color = AnGris)
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${resultado.puntaje}", fontSize = 24.sp,
                            fontWeight = FontWeight.Bold, color = AnMoradoOscuro)
                        Text("/$maxPuntaje", fontSize = 11.sp, color = AnGris,
                            modifier = Modifier.padding(start = 2.dp, bottom = 3.dp))
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = when (diasDesde) {
                            0    -> "Respondido hoy"
                            1    -> "Hace 1 día"
                            else -> "Hace ${diasDesde ?: 0} días"
                        },
                        fontSize = 10.sp, color = AnGris
                    )
                }
            }

            if (resultado != null) {
                IconButton(
                    onClick  = onInfo,
                    modifier = Modifier.align(Alignment.BottomEnd).size(34.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Qué significa este puntaje",
                        tint     = Color(0xFFBFC0D6),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}




// ─────────────────────────────────────────────────────────────
//  AVISO PARA RETOMAR
// ─────────────────────────────────────────────────────────────
@Composable
private fun TarjetaAvisoRetomar(
    epds: ResultadoCuestionario?,
    pass: ResultadoCuestionario?,
    onChatbotClick: () -> Unit
) {
    fun diasDesde(r: ResultadoCuestionario?): Int? =
        r?.let { ((System.currentTimeMillis() - it.fechaMs) / 86_400_000L).toInt() }

    val diasMinimo = listOfNotNull(diasDesde(epds), diasDesde(pass)).minOrNull()

    val mensaje = when {
        epds == null && pass == null ->
            "Puedes hacer el EPDS y el PASS conversando conmigo en el chat, cuando quieras."
        diasMinimo != null && diasMinimo < DIAS_ESPERA_RETOMAR -> {
            val faltan = DIAS_ESPERA_RETOMAR - diasMinimo
            "Ya hiciste tus cuestionarios. En ${if (faltan == 1) "1 día" else "$faltan días"} podrás " +
                    "volver a hacerlos: dejamos ese tiempo para que reflejen un cambio real."
        }
        else ->
            "Ya pasaron $DIAS_ESPERA_RETOMAR días desde tu último cuestionario. Si quieres, puedes retomarlos hablando conmigo en el chat."
    }

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = AnLavandaSuave,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).clickable { onChatbotClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💬", fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Text(mensaje, fontSize = 12.sp, color = AnMoradoOscuro,
                lineHeight = 17.sp, modifier = Modifier.weight(1f))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  EMOCIONES MÁS FRECUENTES
// ─────────────────────────────────────────────────────────────
@Composable
private fun TarjetaFrecuencias(frecuencias: List<Pair<String, Int>>) {
    Surface(
        shape           = RoundedCornerShape(20.dp),
        color           = Color.White,
        shadowElevation = 3.dp,
        modifier        = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("EMOCIONES MÁS FRECUENTES", fontSize = 11.sp,
                fontWeight = FontWeight.Bold, color = AnMoradoMedio)
            Spacer(Modifier.height(3.dp))
            Text("En los últimos $DIAS_VENTANA_FRECUENCIAS días", fontSize = 10.sp, color = AnGris)

            Spacer(Modifier.height(14.dp))

            if (frecuencias.isEmpty()) {
                Text("Registra tu ánimo en el calendario para ver tus patrones acá.",
                    fontSize = 12.sp, color = AnGris, lineHeight = 18.sp)
            } else {
                frecuencias.forEach { (clave, pct) ->
                    val info = CATALOGO_ANIMOS[clave]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        if (info != null) {
                            Image(
                                painter = painterResource(id = info.icono),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        } else Spacer(Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = info?.etiqueta ?: clave.replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = AnMoradoOscuro, modifier = Modifier.width(84.dp)
                        )
                        Box(
                            modifier = Modifier.weight(1f).height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEDEDF5))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct / 100f).fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(info?.color ?: AnGris)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("$pct%", fontSize = 11.sp, color = AnGris)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  RECURSOS
//  TODO: textos y tiempos pendientes de validación por el área
//  de psicología antes de la entrega final.
// ─────────────────────────────────────────────────────────────
@Composable
private fun TarjetaRecursos(onAbrir: (String) -> Unit) {
    Surface(
        shape           = RoundedCornerShape(20.dp),
        color           = Color.White,
        shadowElevation = 3.dp,
        modifier        = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("PARA CUANDO LO NECESITES", fontSize = 11.sp,
                fontWeight = FontWeight.Bold, color = AnMoradoMedio)
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Dos prácticas breves para bajar el ritmo cuando la mente se acelera. " +
                        "No hace falta esperar a estar abrumada para usarlas.",
                fontSize = 11.sp, color = AnGris, lineHeight = 16.sp
            )
            Spacer(Modifier.height(12.dp))

            FilaRecurso(
                titulo  = "Respiración guiada",
                detalle = "Sigue el ritmo del círculo, a tu propio paso",
                onClick = { onAbrir("respiracion") }
            )
            Spacer(Modifier.height(8.dp))
            FilaRecurso(
                titulo  = "Meditación guiada",
                detalle = "Un recorrido por el cuerpo, paso a paso · 8 min",
                onClick = { onAbrir("meditacion") }
            )
        }
    }
}

@Composable
private fun FilaRecurso(titulo: String, detalle: String, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEDEDF5), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier          = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(AnLavanda)
            )
            Column(Modifier.padding(start = 13.dp, end = 14.dp, top = 13.dp, bottom = 13.dp)) {
                Text(titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AnMoradoOscuro)
                Text(detalle, fontSize = 11.sp, color = AnGris, lineHeight = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  EJERCICIO: RESPIRACIÓN GUIADA
// ─────────────────────────────────────────────────────────────
@Composable
private fun DialogoRespiracion(botColor: Color, onCerrar: () -> Unit) {
    var fase   by remember { mutableStateOf(0) }
    var ciclos by remember { mutableStateOf(0) }
    val textos = listOf("Inhala…", "Sostén…", "Exhala…")

    val escala = remember { Animatable(0.55f) }

    LaunchedEffect(fase) {
        when (fase) {
            0 -> escala.animateTo(1f, tween(4000, easing = LinearEasing))
            1 -> delay(4000)
            2 -> escala.animateTo(0.55f, tween(6000, easing = LinearEasing))
        }
        if (fase == 2) ciclos += 1
        fase = (fase + 1) % 3
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor   = Color.White,
        title = {
            Text("Respiración guiada", fontSize = 15.sp,
                fontWeight = FontWeight.Bold, color = AnMoradoOscuro)
        },
        text = {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Sigue el círculo con tu respiración. Si te marea, detente.",
                    fontSize = 11.sp, color = AnGris,
                    textAlign = TextAlign.Center, lineHeight = 16.sp
                )

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier         = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2f * escala.value
                        drawCircle(botColor.copy(alpha = 0.18f), radius = r,
                            center = Offset(size.width / 2f, size.height / 2f))
                        drawCircle(botColor.copy(alpha = 0.35f), radius = r * 0.72f,
                            center = Offset(size.width / 2f, size.height / 2f))
                    }
                    BotMascota(color = botColor, hasText = false, size = 56.dp)
                }

                Spacer(Modifier.height(18.dp))

                Text(textos[fase], fontSize = 17.sp,
                    fontWeight = FontWeight.Bold, color = AnMoradoOscuro)

                Spacer(Modifier.height(6.dp))

                Text(
                    if (ciclos == 0) "Primer ciclo" else "$ciclos ciclos completados",
                    fontSize = 11.sp, color = AnGris
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text("Terminar", color = AnMoradoMedio, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────
//  EJERCICIO: MEDITACIÓN GUIADA
//  Adaptado del audio psicoeducativo del área de psicología.
// ─────────────────────────────────────────────────────────────
private data class PasoMeditacion(val titulo: String, val texto: String, val segundos: Int)

private val PASOS_MEDITACION = listOf(
    PasoMeditacion("Encuentra tu postura",
        "Espalda erguida pero sin rigidez, dejando que el pecho se abra. Que tus manos descansen sobre tus piernas.", 40),
    PasoMeditacion("Aterriza aquí",
        "Nota el silencio a tu alrededor y permite que tu cuerpo empiece a aquietarse.", 40),
    PasoMeditacion("Observa tu respiración",
        "No intentes cambiar su ritmo. Solo observa cómo el cuerpo inhala y exhala por sí mismo.", 60),
    PasoMeditacion("Cabeza y rostro",
        "Lleva la atención a la frente, el entrecejo y la mandíbula. Solemos tensarlos sin darnos cuenta. Suéltalos.", 60),
    PasoMeditacion("Cuello y hombros",
        "Con cada exhalación, imagina que ese peso se disuelve. Siente los hombros alejarse de las orejas.", 60),
    PasoMeditacion("Brazos y manos",
        "Baja por los brazos hasta la punta de los dedos. Permite que se vuelvan pesados y tranquilos.", 45),
    PasoMeditacion("Pecho y abdomen",
        "Nota cómo se expanden y se contraen. Si quieres, apoya una mano ahí y acompaña ese movimiento.", 45),
    PasoMeditacion("Espalda y pelvis",
        "Si hay algún punto de rigidez, respira hacia él. Siente el apoyo de tu cuerpo en la silla o el suelo.", 45),
    PasoMeditacion("Piernas y pies",
        "Recorre los muslos, las rodillas y los tobillos, hasta cada uno de tus dedos.", 45),
    PasoMeditacion("Regresa despacio",
        "Mueve suavemente los dedos. Toma una última respiración consciente y, a tu ritmo, abre los ojos.", 40)
)

@Composable
private fun DialogoMeditacion(botColor: Color, onCerrar: () -> Unit) {
    var indice   by remember { mutableStateOf(0) }
    var restante by remember { mutableStateOf(PASOS_MEDITACION.first().segundos) }
    var enMarcha by remember { mutableStateOf(false) }

    val paso = PASOS_MEDITACION[indice]

    LaunchedEffect(indice) { restante = PASOS_MEDITACION[indice].segundos }

    LaunchedEffect(enMarcha, indice) {
        while (enMarcha && restante > 0) {
            delay(1000)
            restante -= 1
        }
        if (enMarcha && restante <= 0) {
            if (indice < PASOS_MEDITACION.lastIndex) indice += 1 else enMarcha = false
        }
    }

    val transicion = rememberInfiniteTransition(label = "respirar")
    val escala by transicion.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor   = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Meditación guiada", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, color = AnMoradoOscuro,
                    modifier = Modifier.weight(1f))
                Text("${indice + 1}/${PASOS_MEDITACION.size}",
                    fontSize = 12.sp, color = AnMoradoMedio, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier         = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val r = size.minDimension / 2f * escala
                        drawCircle(botColor.copy(alpha = 0.16f), radius = r, center = c)
                        drawCircle(botColor.copy(alpha = 0.30f), radius = r * 0.7f, center = c)
                    }
                    BotMascota(color = botColor, hasText = false, size = 52.dp)
                }

                Spacer(Modifier.height(16.dp))

                Text(paso.titulo, fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, color = AnMoradoOscuro,
                    textAlign = TextAlign.Center)

                Spacer(Modifier.height(6.dp))

                Text(paso.texto, fontSize = 12.sp, color = Color(0xFF555566),
                    lineHeight = 18.sp, textAlign = TextAlign.Center)

                Spacer(Modifier.height(14.dp))

                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", restante / 60, restante % 60),
                    fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AnMoradoOscuro
                )

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (indice > 0) indice -= 1 },
                        enabled = indice > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Anterior",
                            tint = if (indice > 0) AnMoradoMedio else Color(0xFFD5D5E0))
                    }

                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(56.dp).clip(CircleShape).background(botColor)
                            .clickable { enMarcha = !enMarcha },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (enMarcha) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (enMarcha) "Pausar" else "Comenzar",
                            tint = Color.White, modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    IconButton(
                        onClick = { if (indice < PASOS_MEDITACION.lastIndex) indice += 1 },
                        enabled = indice < PASOS_MEDITACION.lastIndex
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Siguiente",
                            tint = if (indice < PASOS_MEDITACION.lastIndex) AnMoradoMedio else Color(0xFFD5D5E0))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text("Terminar", color = AnMoradoMedio, fontWeight = FontWeight.Bold)
            }
        }
    )
}