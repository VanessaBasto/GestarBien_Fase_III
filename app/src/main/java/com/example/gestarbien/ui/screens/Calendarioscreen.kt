package com.example.gestarbien.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.gestarbien.R
import com.example.gestarbien.notificaciones.RecordatoriosScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─────────────────────────────────────────────
//  PALETA
// ─────────────────────────────────────────────
private val CalLavanda      = Color(0xFFB1B4E1)
private val CalLavandaSuave = Color(0xFFEEEFFE)
private val CalMoradoOscuro = Color(0xFF3A3D8C)
private val CalMoradoMedio  = Color(0xFF5C5FA8)
private val CalGrisTxt      = Color(0xFF888888)
private val CalGrisBorde    = Color(0xFFDDDDE6)
private val CalBorde        = Color(0xFFE4E5F5)
private val CalNaranja      = Color(0xFFE8A33D)
private val CalFondo        = Color(0xFFFBFBFE)
private val CalEstrella     = Color(0xFFE8B33D)

// ─────────────────────────────────────────────
//  CATÁLOGOS
// ─────────────────────────────────────────────
data class OpcionAnimo(val clave: String, val etiqueta: String, val icono: Int)

private val ANIMOS = listOf(
    OpcionAnimo("tranquila",  "Tranquila",  R.drawable.animo_tranquila),
    OpcionAnimo("feliz",      "Feliz",      R.drawable.animo_feliz),
    OpcionAnimo("cansada",    "Cansada",    R.drawable.animo_cansada),
    OpcionAnimo("ansiosa",    "Ansiosa",    R.drawable.animo_ansiosa),
    OpcionAnimo("triste",     "Triste",     R.drawable.animo_triste),
    OpcionAnimo("molesta",    "Molesta",    R.drawable.animo_molesta),
    OpcionAnimo("confundida", "Confundida", R.drawable.animo_confundida)
)

private const val CLAVE_MIXTO = "mixto"

/** Gradación del 1 al 5 que se muestra en el aviso del día. */
private val GRADOS = listOf("un poco", "algo", "bastante", "muy", "demasiado")

data class OpcionAntelacion(val etiqueta: String, val minutos: Int)

private val ANTELACIONES = listOf(
    OpcionAntelacion("En el momento exacto", 0),
    OpcionAntelacion("30 min antes",         30),
    OpcionAntelacion("1 hora antes",         60),
    OpcionAntelacion("2 horas antes",        120)
)

private val TIPOS_RECORDATORIO = listOf(
    "Control prenatal",
    "Ecografía",
    "Examen",
    "Vacuna",
    "Cita con el bebé",
    "Otro"
)

private val MESES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

private val DIAS_SEMANA = listOf("L", "M", "X", "J", "V", "S", "D")

// ─────────────────────────────────────────────
//  MODELOS
// ─────────────────────────────────────────────
data class RegistroDia(
    val fecha: String = "",
    val animo: String = "",              // clave única o "mixto"
    val animos: List<String> = emptyList(),
    val intensidad: Int = 0,             // 1..5, solo con ánimo único
    val nota: String = ""
) {
    val vacio: Boolean get() = animo.isBlank() && nota.isBlank()
}

data class Recordatorio(
    val id: String = "",
    val titulo: String = "",
    val lugar: String = "",
    val fechaHora: Long = 0L,
    val antelacionMin: Int = 0,
    val avisoActivo: Boolean = true
) {
    val alarmaId: Int get() = id.hashCode()
    val momentoAviso: Long get() = fechaHora - antelacionMin * 60_000L
}

// ─────────────────────────────────────────────
//  FECHAS
// ─────────────────────────────────────────────
private val fmtClave = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

private fun horaTexto(ms: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ms }
    var h = c.get(Calendar.HOUR)
    if (h == 0) h = 12
    val min = c.get(Calendar.MINUTE)
    val sufijo = if (c.get(Calendar.AM_PM) == Calendar.AM) "a. m." else "p. m."
    return String.format(Locale.getDefault(), "%d:%02d %s", h, min, sufijo)
}

private fun fechaTexto(ms: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ms }
    return "${c.get(Calendar.DAY_OF_MONTH)} de ${MESES[c.get(Calendar.MONTH)]} de ${c.get(Calendar.YEAR)}"
}

private fun claveFecha(y: Int, m: Int, d: Int): String =
    String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)

private fun hoyClave(): String = fmtClave.format(Calendar.getInstance().time)

private fun offsetPrimerDia(y: Int, m: Int): Int {
    val c = Calendar.getInstance().apply { set(y, m, 1) }
    return (c.get(Calendar.DAY_OF_WEEK) + 5) % 7
}

private fun diasDelMes(y: Int, m: Int): Int =
    Calendar.getInstance().apply { set(y, m, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)

private fun msDeClave(clave: String): Long {
    val p = clave.split("-")
    return Calendar.getInstance().apply {
        if (p.size == 3) {
            set(Calendar.YEAR, p[0].toInt())
            set(Calendar.MONTH, p[1].toInt() - 1)
            set(Calendar.DAY_OF_MONTH, p[2].toInt())
        }
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

// ─────────────────────────────────────────────
//  FIRESTORE
// ─────────────────────────────────────────────
private fun uidActual(): String? = FirebaseAuth.getInstance().currentUser?.uid

private suspend fun cargarNombreBot(): String {
    val u = uidActual() ?: return ""
    return try {
        FirebaseFirestore.getInstance()
            .collection("usuarios").document(u)
            .collection("bot_config").document("config")
            .get().await().getString("nombre") ?: ""
    } catch (e: Exception) { "" }
}

private suspend fun cargarRegistros(prefijoMes: String): Map<String, RegistroDia> {
    val u = uidActual() ?: return emptyMap()
    return try {
        FirebaseFirestore.getInstance()
            .collection("usuarios").document(u)
            .collection("registros_diarios").get().await()
            .documents.filter { it.id.startsWith(prefijoMes) }
            .associate {
                it.id to RegistroDia(
                    fecha      = it.id,
                    animo      = it.getString("animo") ?: "",
                    animos     = (it.get("animos") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    intensidad = (it.getLong("intensidad") ?: 0L).toInt(),
                    nota       = it.getString("nota") ?: ""
                )
            }
    } catch (e: Exception) { emptyMap() }
}

private fun guardarRegistro(r: RegistroDia, onDone: (Boolean, String) -> Unit) {
    val u = uidActual()
    if (u == null) { onDone(false, "No hay sesión activa."); return }
    FirebaseFirestore.getInstance()
        .collection("usuarios").document(u)
        .collection("registros_diarios").document(r.fecha)
        .set(
            mapOf(
                "animo"       to r.animo,
                "animos"      to r.animos,
                "intensidad"  to r.intensidad,
                "nota"        to r.nota,
                "actualizado" to System.currentTimeMillis()
            )
        )
        .addOnSuccessListener { onDone(true, "") }
        .addOnFailureListener { e -> onDone(false, e.message ?: "Error desconocido") }
}

private suspend fun cargarRecordatorios(): List<Recordatorio> {
    val u = uidActual() ?: return emptyList()
    return try {
        FirebaseFirestore.getInstance()
            .collection("usuarios").document(u)
            .collection("recordatorios").get().await()
            .documents.map {
                Recordatorio(
                    id            = it.id,
                    titulo        = it.getString("titulo") ?: "",
                    lugar         = it.getString("lugar") ?: "",
                    fechaHora     = it.getLong("fechaHora") ?: 0L,
                    antelacionMin = (it.getLong("antelacionMin") ?: 0L).toInt(),
                    avisoActivo   = it.getBoolean("avisoActivo") ?: true
                )
            }.sortedBy { it.fechaHora }
    } catch (e: Exception) { emptyList() }
}

private fun guardarRecordatorio(r: Recordatorio, onDone: (Boolean, String) -> Unit) {
    val u = uidActual()
    if (u == null) { onDone(false, "No hay sesión activa."); return }
    FirebaseFirestore.getInstance()
        .collection("usuarios").document(u)
        .collection("recordatorios").document(r.id)
        .set(
            mapOf(
                "titulo"        to r.titulo,
                "lugar"         to r.lugar,
                "fechaHora"     to r.fechaHora,
                "antelacionMin" to r.antelacionMin,
                "avisoActivo"   to r.avisoActivo
            )
        )
        .addOnSuccessListener { onDone(true, "") }
        .addOnFailureListener { e -> onDone(false, e.message ?: "Error desconocido") }
}

private fun borrarRecordatorio(id: String) {
    val u = uidActual() ?: return
    FirebaseFirestore.getInstance()
        .collection("usuarios").document(u)
        .collection("recordatorios").document(id).delete()
}

// ─────────────────────────────────────────────
//  PANTALLA
// ─────────────────────────────────────────────
@Composable
fun CalendarioScreen(
    onBackClick: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToAnalysis: () -> Unit = {},
    onHablarDeEsto: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val hoy = remember { Calendar.getInstance() }

    var mes       by remember { mutableStateOf(hoy.get(Calendar.MONTH)) }
    var anio      by remember { mutableStateOf(hoy.get(Calendar.YEAR)) }
    var expandido by remember { mutableStateOf(false) }

    var fechaSel      by remember { mutableStateOf(hoyClave()) }
    var registros     by remember { mutableStateOf<Map<String, RegistroDia>>(emptyMap()) }
    var recordatorios by remember { mutableStateOf<List<Recordatorio>>(emptyList()) }

    // Estado del registro del día
    var animosSel  by remember { mutableStateOf(setOf<String>()) }
    var mixto      by remember { mutableStateOf(false) }
    var intensidad by remember { mutableStateOf(0) }
    var nota       by remember { mutableStateOf("") }
    var botName    by remember { mutableStateOf("") }

    var guardando          by remember { mutableStateOf(false) }
    var mensajeRegistro    by remember { mutableStateOf("") }
    var mensajeRecordatorio by remember { mutableStateOf("") }
    var mostrarSheet       by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("gestarbien_prefs", Context.MODE_PRIVATE) }
    val claveTutorial = "tutorial_calendario_${uidActual()}"
    var pasoTutorial by remember {
        mutableStateOf(if (prefs.getBoolean(claveTutorial, false)) -1 else 0)
    }
    val zonas = remember { mutableStateMapOf<Int, Rect>() }
    val scrollEstado = rememberScrollState()
    val density = LocalDensity.current
    val alturaPantallaDp = LocalConfiguration.current.screenHeightDp

    fun claveBorrador(fecha: String) = "borrador_${uidActual()}_$fecha"

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (!concedido) mensajeRecordatorio = "Sin permiso de notificaciones no podré avisarte."
    }

    LaunchedEffect(Unit) {
        botName = cargarNombreBot()
        RecordatoriosScheduler.crearCanal(context)
        pedirPermisoSiHaceFalta(context) { pedirPermiso.launch(it) }
        if (!RecordatoriosScheduler.puedeAlarmasExactas(context)) {
            RecordatoriosScheduler.solicitarPermisoAlarmaExacta(context)
        }
        recordatorios = cargarRecordatorios()
    }

    val prefijoMes = String.format(Locale.getDefault(), "%04d-%02d", anio, mes + 1)

    LaunchedEffect(prefijoMes) { registros = cargarRegistros(prefijoMes) }

    // Carga el registro guardado; si no hay, recupera el borrador local
    LaunchedEffect(fechaSel, registros) {
        val r = registros[fechaSel]
        if (r != null) {
            mixto      = r.animo == CLAVE_MIXTO
            animosSel  = if (r.animos.isNotEmpty()) r.animos.toSet()
            else if (r.animo.isNotBlank() && r.animo != CLAVE_MIXTO) setOf(r.animo)
            else emptySet()
            intensidad = r.intensidad
            nota       = r.nota
        } else {
            mixto      = false
            animosSel  = emptySet()
            intensidad = 0
            nota       = prefs.getString(claveBorrador(fechaSel), "") ?: ""
        }
        mensajeRegistro = ""
    }

    // Guarda el borrador de la nota en cuanto se escribe
    LaunchedEffect(nota, fechaSel) {
        if (registros[fechaSel] == null && nota.isNotBlank()) {
            prefs.edit().putString(claveBorrador(fechaSel), nota).apply()
        }
    }

    val diasConCita = remember(recordatorios, prefijoMes) {
        recordatorios
            .map { fmtClave.format(it.fechaHora) }
            .filter { it.startsWith(prefijoMes) }
            .mapNotNull { it.takeLast(2).toIntOrNull() }
            .toSet()
    }

    val recordatoriosDelDia = recordatorios.filter { fmtClave.format(it.fechaHora) == fechaSel }

    val esFuturo   = fechaSel > hoyClave()
    val esHoyMismo = fechaSel == hoyClave()

    fun aplicarAlarma(rec: Recordatorio): String {
        return if (rec.avisoActivo) {
            val cuando = "el ${fechaTexto(rec.fechaHora)} a las ${horaTexto(rec.fechaHora)}"
            val titulo = if (botName.isBlank()) rec.titulo else botName
            val texto  = if (botName.isBlank())
                "Paso a recordarte que tienes $cuando: ${rec.titulo}."
            else
                "¡Hola! Solo pasaba a recordarte que tienes $cuando: ${rec.titulo}."

            val ok = RecordatoriosScheduler.programar(
                context, rec.alarmaId, titulo, texto, rec.momentoAviso
            )
            when {
                !ok -> "Guardado, pero el aviso ya pasó. Elige una antelación menor."
                !RecordatoriosScheduler.puedeAlarmasExactas(context) ->
                    "Guardado. El aviso puede llegar con unos minutos de diferencia."
                else -> "Recordatorio guardado. Te avisaré a tiempo."
            }
        } else {
            RecordatoriosScheduler.cancelar(context, rec.alarmaId)
            "Aviso desactivado."
        }
    }

    val hayAlgoQueGuardar = animosSel.isNotEmpty() || nota.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            containerColor = CalFondo,
            bottomBar = {
                BottomNavBar(
                    onAnalysis = onNavigateToAnalysis,
                    onCalendar = { },
                    onChat     = onNavigateToChat,
                    botColor   = CalMoradoOscuro
                )
            }
        ) { inner ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .verticalScroll(scrollEstado)
            ) {

                // ── Barra superior ───────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = CalMoradoOscuro
                        )
                    }
                    Text(
                        text       = "Mi calendario",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = CalMoradoOscuro,
                        modifier   = Modifier.weight(1f)
                    )
                    IconButton(onClick = { mostrarSheet = true }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Añadir recordatorio",
                            tint = CalMoradoMedio
                        )
                    }
                }

                // ── 1. Calendario ────────────────────────────
                TarjetaSeccion(
                    modifier = Modifier.onGloballyPositioned { zonas[0] = it.boundsInRoot() }
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            if (mes == 0) { mes = 11; anio -= 1 } else mes -= 1
                        }) {
                            Icon(Icons.Default.KeyboardArrowLeft, "Mes anterior", tint = CalMoradoMedio)
                        }
                        Text(
                            "${MESES[mes].replaceFirstChar { it.uppercase() }} $anio",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = CalMoradoOscuro
                        )
                        IconButton(onClick = {
                            if (mes == 11) { mes = 0; anio += 1 } else mes += 1
                        }) {
                            Icon(Icons.Default.KeyboardArrowRight, "Mes siguiente", tint = CalMoradoMedio)
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(Modifier.fillMaxWidth()) {
                        DIAS_SEMANA.forEach {
                            Text(
                                it,
                                fontSize  = 10.sp,
                                color     = CalGrisTxt,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    val offset       = offsetPrimerDia(anio, mes)
                    val total        = diasDelMes(anio, mes)
                    val filasTotales = (offset + total + 6) / 7

                    val diaSel  = fechaSel.takeLast(2).toIntOrNull() ?: 1
                    val filaSel = (offset + diaSel - 1) / 7

                    val inicio = if (expandido) 0
                    else (filaSel - 1).coerceIn(0, (filasTotales - 3).coerceAtLeast(0))
                    val fin = if (expandido) filasTotales else (inicio + 3).coerceAtMost(filasTotales)

                    // ── Días de ejemplo para el tutorial (paso de los puntos morado/naranja) ──
                    val esPasoDePuntos = pasoTutorial == 1
                    val diaEjemploRegistro = remember(anio, mes) {
                        val t = diasDelMes(anio, mes)
                        ((hoy.get(Calendar.DAY_OF_MONTH) + 1) - 1) % t + 1
                    }
                    val diaEjemploCita = remember(anio, mes) {
                        val t = diasDelMes(anio, mes)
                        ((hoy.get(Calendar.DAY_OF_MONTH) + 3) - 1) % t + 1
                    }

















                    for (fila in inicio until fin) {
                        Row(Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val dia = fila * 7 + col - offset + 1
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    if (dia in 1..total) {
                                        val clave = claveFecha(anio, mes, dia)
                                        CeldaDia(
                                            dia           = dia,
                                            seleccionado  = clave == fechaSel,
                                            esHoy         = clave == hoyClave(),
                                            tieneRegistro = registros[clave]?.vacio == false || (esPasoDePuntos && dia == diaEjemploRegistro),
                                            tieneCita     = dia in diasConCita || (esPasoDePuntos && dia == diaEjemploCita),
                                            onClick       = { fechaSel = clave }
                                        )
                                    } else {
                                        Spacer(Modifier.size(38.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (filasTotales > 3) {
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F8))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandido = !expandido }
                                .padding(vertical = 8.dp)
                                .onGloballyPositioned { zonas[4] = it.boundsInRoot() },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                if (expandido) "Ver menos" else "Ver mes completo",
                                fontSize = 12.sp,
                                color    = CalMoradoMedio
                            )
                            Icon(
                                if (expandido) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint     = CalMoradoMedio,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // ── Aviso contextual del día ─────────────────
                AvisoDelDia(
                    fecha    = fechaSel,
                    esFuturo = esFuturo,
                    esHoy    = esHoyMismo,
                    registro = registros[fechaSel],
                    onAccion = { mostrarSheet = true }
                )

                if (!esFuturo) {

                    // ── 2. Ánimo ─────────────────────────────
                    TarjetaSeccion(
                        modifier = Modifier.onGloballyPositioned { zonas[1] = it.boundsInRoot() }
                    ) {
                        Text(
                            if (esHoyMismo) "¿Cómo te has sentido?" else "¿Cómo te sentiste?",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = CalMoradoOscuro
                        )
                        if (mixto) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Marca todas las que sentiste hoy.",
                                fontSize = 11.sp,
                                color    = CalMoradoMedio
                            )
                        }
                        Spacer(Modifier.height(14.dp))

                        // 7 ánimos + la bolita "Mixto" = 2 filas de 4
                        val celdas = ANIMOS.map { it.clave } + CLAVE_MIXTO
                        celdas.chunked(4).forEach { fila ->
                            Row(Modifier.fillMaxWidth()) {
                                fila.forEach { clave ->
                                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        if (clave == CLAVE_MIXTO) {
                                            ItemMixto(
                                                activo  = mixto,
                                                onClick = {
                                                    mixto = !mixto
                                                    if (mixto) intensidad = 0
                                                    else if (animosSel.size > 1)
                                                        animosSel = setOf(animosSel.first())
                                                }
                                            )
                                        } else {
                                            val op = ANIMOS.first { it.clave == clave }
                                            ItemAnimo(
                                                opcion  = op,
                                                activo  = clave in animosSel,
                                                onClick = {
                                                    animosSel = when {
                                                        mixto && clave in animosSel -> animosSel - clave
                                                        mixto                       -> animosSel + clave
                                                        clave in animosSel          -> { intensidad = 0; emptySet() }
                                                        else                        -> setOf(clave)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                repeat(4 - fila.size) { Spacer(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        // ── Estrellas de intensidad ──────────
                        if (!mixto && animosSel.size == 1) {
                            val etq = ANIMOS.first { it.clave == animosSel.first() }
                                .etiqueta.lowercase(Locale.getDefault())
                            HorizontalDivider(color = Color(0xFFF0F0F8))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "¿Qué tan $etq te sentiste?",
                                fontSize = 12.sp,
                                color    = CalGrisTxt
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                (1..5).forEach { n ->
                                    IconButton(
                                        onClick  = { intensidad = if (intensidad == n) 0 else n },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            if (n <= intensidad) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Nivel $n",
                                            tint     = if (n <= intensidad) CalEstrella else CalGrisBorde,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                if (intensidad > 0) {
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        GRADOS[intensidad - 1],
                                        fontSize   = 12.sp,
                                        color      = CalMoradoMedio,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // ── 3. Nota ──────────────────────────────
                    TarjetaSeccion(
                        modifier = Modifier.onGloballyPositioned { zonas[2] = it.boundsInRoot() }
                    ) {
                        Text("Nota del día", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CalMoradoOscuro)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value         = nota,
                            onValueChange = { nota = it },
                            placeholder   = {
                                Text("Cuéntame cómo estuvo tu día…", fontSize = 13.sp, color = Color(0xFFAAAAAA))
                            },
                            minLines = 3,
                            shape    = RoundedCornerShape(14.dp),
                            colors   = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = CalLavanda,
                                unfocusedBorderColor    = CalBorde,
                                cursorColor             = CalMoradoMedio,
                                focusedContainerColor   = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── Botones, debajo de la nota ───────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .onGloballyPositioned { zonas[5] = it.boundsInRoot() },
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BotonSuave(
                            texto    = if (guardando) "Guardando…" else "Guardar",
                            activo   = hayAlgoQueGuardar && !guardando,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!hayAlgoQueGuardar || guardando) return@BotonSuave
                            guardando = true
                            val r = RegistroDia(
                                fecha      = fechaSel,
                                animo      = if (mixto) CLAVE_MIXTO else animosSel.firstOrNull() ?: "",
                                animos     = animosSel.toList(),
                                intensidad = intensidad,
                                nota       = nota
                            )
                            guardarRegistro(r) { ok, err ->
                                guardando = false
                                if (ok) {
                                    registros = registros + (fechaSel to r)
                                    prefs.edit().remove(claveBorrador(fechaSel)).apply()
                                    mensajeRegistro = "Registro guardado"
                                } else {
                                    mensajeRegistro = "No se pudo guardar: $err"
                                }
                            }
                        }

                        BotonSuave(
                            texto    = "Hablar de esto",
                            activo   = animosSel.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (animosSel.isEmpty()) return@BotonSuave
                            val payload = if (mixto || animosSel.size > 1) CLAVE_MIXTO else animosSel.first()
                            onHablarDeEsto(payload)
                            onNavigateToChat()
                        }
                    }

                    if (mensajeRegistro.isNotBlank()) {
                        Text(
                            mensajeRegistro,
                            fontSize = 11.sp,
                            color    = CalMoradoMedio,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                        )
                    }
                }

                // ── 4. Recordatorios ─────────────────────────
                TarjetaSeccion(
                    modifier = Modifier.onGloballyPositioned { zonas[3] = it.boundsInRoot() }
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recordatorios", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CalMoradoOscuro)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier.clickable { mostrarSheet = true }
                        ) {
                            Icon(Icons.Default.Add, null, tint = CalMoradoMedio, modifier = Modifier.size(16.dp))
                            Text(" Añadir", fontSize = 12.sp, color = CalMoradoMedio)
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    if (recordatoriosDelDia.isEmpty()) {
                        Text(
                            "No tienes citas agendadas para este día.",
                            fontSize = 12.sp,
                            color    = CalGrisTxt,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        recordatoriosDelDia.forEach { rec ->
                            FilaRecordatorio(
                                rec = rec,
                                onToggleAviso = {
                                    val nuevo = rec.copy(avisoActivo = !rec.avisoActivo)
                                    guardarRecordatorio(nuevo) { ok, err ->
                                        if (!ok) mensajeRecordatorio = err
                                    }
                                    mensajeRecordatorio = aplicarAlarma(nuevo)
                                    recordatorios = recordatorios.map { if (it.id == rec.id) nuevo else it }
                                },
                                onBorrar = {
                                    RecordatoriosScheduler.cancelar(context, rec.alarmaId)
                                    borrarRecordatorio(rec.id)
                                    recordatorios = recordatorios.filter { it.id != rec.id }
                                    mensajeRecordatorio = "Recordatorio eliminado."
                                }
                            )
                        }
                    }

                    // El mensaje del recordatorio vive aquí, no junto a Guardar
                    if (mensajeRecordatorio.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape    = RoundedCornerShape(12.dp),
                            color    = CalLavandaSuave,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                mensajeRecordatorio,
                                fontSize   = 11.sp,
                                color      = CalMoradoOscuro,
                                lineHeight = 16.sp,
                                modifier   = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }

        // ── Hoja: nuevo recordatorio ─────────────────────────
        if (mostrarSheet) {
            SheetRecordatorio(
                fechaBase = fechaSel,
                onDismiss = { mostrarSheet = false },
                onGuardar = { nuevo ->
                    guardarRecordatorio(nuevo) { ok, err ->
                        mensajeRecordatorio = if (ok) {
                            recordatorios = (recordatorios + nuevo).sortedBy { it.fechaHora }
                            aplicarAlarma(nuevo)
                        } else {
                            "No se pudo guardar el recordatorio: $err"
                        }
                    }
                    mostrarSheet = false
                }
            )
        }

        // ── Tutorial ─────────────────────────────────────────
        if (pasoTutorial in PASOS_TUTORIAL.indices) {
            val zonaPaso: Rect? = when (pasoTutorial) {
                0    -> null
                1    -> zonas[0]
                2    -> zonas[4]
                3    -> zonas[1]
                4    -> zonas[2]
                5    -> zonas[5]
                else -> zonas[3]
            }

            LaunchedEffect(pasoTutorial) {
                if (zonaPaso != null) {
                    val alturaVisiblePx = with(density) { alturaPantallaDp.dp.toPx() }
                    val margenSuperior  = with(density) { 100.dp.toPx() }
                    val margenInferior  = with(density) { 170.dp.toPx() }
                    val espacio = alturaVisiblePx - margenSuperior - margenInferior

                    val destino = if (zonaPaso.height <= espacio)
                        zonaPaso.top + scrollEstado.value - margenSuperior
                    else
                        zonaPaso.top + scrollEstado.value - 40f

                    scrollEstado.animateScrollTo(destino.coerceAtLeast(0f).toInt())
                } else {
                    scrollEstado.animateScrollTo(0)
                }
            }

            TutorialOverlay(
                paso = pasoTutorial,
                zona = zonaPaso,
                onSiguiente = {
                    if (pasoTutorial >= PASOS_TUTORIAL.lastIndex) {
                        prefs.edit().putBoolean(claveTutorial, true).apply()
                        pasoTutorial = -1
                    } else pasoTutorial += 1
                },
                onSaltar = {
                    prefs.edit().putBoolean(claveTutorial, true).apply()
                    pasoTutorial = -1
                }
            )
        }
    }
}

private fun pedirPermisoSiHaceFalta(context: Context, lanzar: (String) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        lanzar(Manifest.permission.POST_NOTIFICATIONS)
    }
}

// ─────────────────────────────────────────────
//  AVISO CONTEXTUAL AL TOCAR UN DÍA
// ─────────────────────────────────────────────
@Composable
private fun AvisoDelDia(
    fecha: String,
    esFuturo: Boolean,
    esHoy: Boolean,
    registro: RegistroDia?,
    onAccion: () -> Unit
) {
    val tieneRegistro = registro?.vacio == false

    val texto = when {
        esFuturo -> "Este día aún no llega. Puedes dejar agendado un control o una cita."

        tieneRegistro && registro!!.animo == CLAVE_MIXTO ->
            if (esHoy) "Hoy sentiste muchas emociones a la vez."
            else "Ese día sentiste muchas emociones a la vez."

        tieneRegistro && registro!!.animo.isNotBlank() -> {
            val etq = ANIMOS.find { it.clave == registro.animo }
                ?.etiqueta?.lowercase(Locale.getDefault()) ?: registro.animo
            val grado = if (registro.intensidad in 1..5) "${GRADOS[registro.intensidad - 1]} " else ""
            if (esHoy) "El día de hoy te sentiste $grado$etq."
            else "Ese día te sentiste $grado$etq."
        }

        tieneRegistro -> "Dejaste una nota este día."
        esHoy         -> "Hoy todavía no has registrado cómo te sientes."
        else          -> "No registraste este día. Puedes completarlo ahora."
    }

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = CalLavandaSuave,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (esFuturo) Icons.Default.Notifications else Icons.Default.Edit,
                contentDescription = null,
                tint     = CalMoradoMedio,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    fechaTexto(msDeClave(fecha)),
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = CalMoradoOscuro
                )
                Text(texto, fontSize = 11.sp, color = CalGrisTxt, lineHeight = 16.sp)
            }
            if (esFuturo) {
                Text(
                    "Añadir",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = CalMoradoMedio,
                    modifier   = Modifier.clickable { onAccion() }.padding(6.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  COMPONENTES
// ─────────────────────────────────────────────
@Composable

private fun BotonSuave(
    texto: String,
    activo: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(50.dp),
        color    = if (activo) CalLavanda else Color.White,
        modifier = modifier
            .height(46.dp)
            .border(
                width = if (activo) 0.dp else 1.dp,
                color = if (activo) Color.Transparent else CalGrisBorde,
                shape = RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                texto,
                fontSize   = 14.sp,
                color      = if (activo) Color.White else Color(0xFFAAAAAA),
                fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun TarjetaSeccion(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape           = RoundedCornerShape(20.dp),
        color           = color,
        shadowElevation = 3.dp,
        modifier        = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            content  = contenido
        )
    }
}

@Composable
private fun CeldaDia(
    dia: Int,
    seleccionado: Boolean,
    esHoy: Boolean,
    tieneRegistro: Boolean,
    tieneCita: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier         = Modifier.padding(2.dp).size(38.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (seleccionado) CalMoradoOscuro else Color.Transparent)
                .then(
                    if (esHoy && !seleccionado) Modifier.border(1.5.dp, CalLavanda, CircleShape)
                    else Modifier
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                dia.toString(),
                fontSize   = 13.sp,
                color      = if (seleccionado) Color.White else CalMoradoOscuro,
                fontWeight = if (seleccionado || esHoy) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (!seleccionado && (tieneRegistro || tieneCita)) {
            Row(
                modifier              = Modifier.align(Alignment.BottomCenter).padding(bottom = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (tieneRegistro) Box(Modifier.size(4.dp).clip(CircleShape).background(CalMoradoMedio))
                if (tieneCita)     Box(Modifier.size(4.dp).clip(CircleShape).background(CalNaranja))
            }
        }
    }
}

@Composable
private fun ItemAnimo(opcion: OpcionAnimo, activo: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF9FF))
                .then(
                    if (activo) Modifier.border(2.dp, CalLavanda, CircleShape)
                    else Modifier.border(1.dp, Color(0xFFF0F0F8), CircleShape)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter            = painterResource(id = opcion.icono),
                contentDescription = opcion.etiqueta,
                modifier           = Modifier.size(42.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            opcion.etiqueta,
            fontSize   = 10.sp,
            color      = if (activo) CalMoradoOscuro else CalGrisTxt,
            fontWeight = if (activo) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/** Bolita "Mixto": activa la selección múltiple. */
@Composable
private fun ItemMixto(activo: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF9FF))
                .then(
                    if (activo) Modifier.border(2.dp, CalLavanda, CircleShape)
                    else Modifier.border(1.dp, Color(0xFFF0F0F8), CircleShape)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter            = painterResource(id = R.drawable.animo_mixto),
                contentDescription = "Mixto",
                modifier           = Modifier.size(42.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Mixto",
            fontSize   = 10.sp,
            color      = if (activo) CalMoradoOscuro else CalGrisTxt,
            fontWeight = if (activo) FontWeight.Medium else FontWeight.Normal
        )
    }
}
@Composable
private fun ChipTexto(
    texto: String,
    activo: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = if (activo) CalLavandaSuave else Color.White,
        modifier = modifier
            .border(1.dp, if (activo) CalLavanda else CalBorde, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Text(
            texto,
            fontSize   = 12.sp,
            textAlign  = TextAlign.Center,
            color      = if (activo) CalMoradoOscuro else CalGrisTxt,
            fontWeight = if (activo) FontWeight.Medium else FontWeight.Normal,
            modifier   = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp)
        )
    }
}

@Composable
private fun FilaRecordatorio(
    rec: Recordatorio,
    onToggleAviso: () -> Unit,
    onBorrar: () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier.size(34.dp).clip(CircleShape).background(CalLavandaSuave),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint     = CalMoradoMedio,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(rec.titulo, fontSize = 13.sp, color = CalMoradoOscuro, fontWeight = FontWeight.Medium)
            Text(
                horaTexto(rec.fechaHora) + if (rec.lugar.isNotBlank()) " · ${rec.lugar}" else "",
                fontSize = 11.sp,
                color    = CalGrisTxt
            )
            if (rec.avisoActivo) {
                Text(
                    "Aviso: ${ANTELACIONES.find { it.minutos == rec.antelacionMin }?.etiqueta ?: ""}",
                    fontSize = 10.sp,
                    color    = CalMoradoMedio
                )
            }
        }

        IconButton(onClick = onToggleAviso) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = if (rec.avisoActivo) "Desactivar aviso" else "Activar aviso",
                tint     = if (rec.avisoActivo) CalMoradoMedio else Color(0xFFC2C3DC),
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onBorrar) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint     = Color(0xFFC2C3DC),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
//  HOJA: NUEVO RECORDATORIO
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetRecordatorio(
    fechaBase: String,
    onDismiss: () -> Unit,
    onGuardar: (Recordatorio) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Arranca en el día seleccionado con la hora actual; si es hoy, una hora
    // más adelante para que ninguna antelación quede vencida de entrada.
    val inicial = remember(fechaBase) {
        val ahora = Calendar.getInstance()
        if (fechaBase > hoyClave()) {
            Calendar.getInstance().apply {
                timeInMillis = msDeClave(fechaBase)
                set(Calendar.HOUR_OF_DAY, ahora.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, ahora.get(Calendar.MINUTE))
            }.timeInMillis
        } else {
            ahora.timeInMillis + 60 * 60 * 1000L
        }
    }

    var tipoSel     by remember { mutableStateOf(TIPOS_RECORDATORIO.first()) }
    var menuAbierto by remember { mutableStateOf(false) }
    var tituloOtro  by remember { mutableStateOf("") }
    var lugar       by remember { mutableStateOf("") }
    var fechaMs     by remember { mutableStateOf(inicial) }
    var antelacion  by remember { mutableStateOf(ANTELACIONES[0]) }

    val esOtro      = tipoSel == "Otro"
    val tituloFinal = if (esOtro) tituloOtro.trim() else tipoSel
    val avisoYaPaso = (fechaMs - antelacion.minutos * 60_000L) <= System.currentTimeMillis()

    val camposColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor      = CalLavanda,
        unfocusedBorderColor    = CalBorde,
        cursorColor             = CalMoradoMedio,
        focusedContainerColor   = Color.White,
        unfocusedContainerColor = Color.White
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.White
    ) {

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {

            Text("Nuevo recordatorio", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CalMoradoOscuro)
            Spacer(Modifier.height(16.dp))

            Text("¿Qué quieres recordar?", fontSize = 12.sp, color = CalGrisTxt)
            Spacer(Modifier.height(8.dp))

            Box {
                CampoSeleccion(texto = tipoSel) { menuAbierto = true }
                DropdownMenu(
                    expanded         = menuAbierto,
                    onDismissRequest = { menuAbierto = false },
                    modifier         = Modifier.background(Color.White)
                ) {
                    TIPOS_RECORDATORIO.forEach { t ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    t,
                                    fontSize   = 14.sp,
                                    color      = if (t == tipoSel) CalMoradoOscuro else CalGrisTxt,
                                    fontWeight = if (t == tipoSel) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = { tipoSel = t; menuAbierto = false }
                        )
                    }
                }
            }

            if (esOtro) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value         = tituloOtro,
                    onValueChange = { tituloOtro = it },
                    placeholder   = { Text("Escribe de qué se trata", fontSize = 13.sp, color = Color(0xFFAAAAAA)) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = camposColors,
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Fecha", fontSize = 12.sp, color = CalGrisTxt)
            Spacer(Modifier.height(8.dp))

            val c = Calendar.getInstance().apply { timeInMillis = fechaMs }

            CampoSeleccion(texto = fechaTexto(fechaMs)) {
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        fechaMs = Calendar.getInstance().apply {
                            timeInMillis = fechaMs
                            set(Calendar.YEAR, y); set(Calendar.MONTH, m); set(Calendar.DAY_OF_MONTH, d)
                        }.timeInMillis
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            Spacer(Modifier.height(12.dp))
            Text("Hora", fontSize = 12.sp, color = CalGrisTxt)
            Spacer(Modifier.height(8.dp))

            CampoSeleccion(texto = horaTexto(fechaMs)) {
                TimePickerDialog(
                    context,
                    { _, h, min ->
                        fechaMs = Calendar.getInstance().apply {
                            timeInMillis = fechaMs
                            set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, min)
                        }.timeInMillis
                    },
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false
                ).show()
            }

            Spacer(Modifier.height(16.dp))
            Text("Avisarme", fontSize = 12.sp, color = CalGrisTxt)
            Spacer(Modifier.height(8.dp))

            ANTELACIONES.chunked(2).forEach { fila ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fila.forEach { a ->
                        ChipTexto(
                            texto    = a.etiqueta,
                            activo   = antelacion.minutos == a.minutos,
                            modifier = Modifier.weight(1f)
                        ) { antelacion = a }
                    }
                    repeat(2 - fila.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (avisoYaPaso) {
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = CalLavandaSuave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Con esta antelación el aviso ya pasó, así que no sonará. Elige una más corta o cambia la fecha.",
                        fontSize   = 11.sp,
                        color      = CalMoradoOscuro,
                        lineHeight = 16.sp,
                        modifier   = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value         = lugar,
                onValueChange = { lugar = it },
                placeholder   = { Text("Lugar (opcional)", fontSize = 13.sp, color = Color(0xFFAAAAAA)) },
                singleLine    = true,
                shape         = RoundedCornerShape(14.dp),
                colors        = camposColors,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    onGuardar(
                        Recordatorio(
                            id            = System.currentTimeMillis().toString(),
                            titulo        = tituloFinal,
                            lugar         = lugar.trim(),
                            fechaHora     = fechaMs,
                            antelacionMin = antelacion.minutos,
                            avisoActivo   = true
                        )
                    )
                },
                enabled  = tituloFinal.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = CalLavanda,
                    disabledContainerColor = Color(0xFFDDDEEF),
                    contentColor           = Color.White,
                    disabledContentColor   = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Guardar recordatorio", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            if (esOtro && tituloOtro.isBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Escribe de qué se trata para poder guardarlo.", fontSize = 11.sp, color = CalGrisTxt)
            }
        }
    }
}

@Composable
private fun CampoSeleccion(texto: String, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CalBorde, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(texto, fontSize = 14.sp, color = CalMoradoOscuro)
            Icon(Icons.Default.KeyboardArrowDown, null, tint = CalGrisTxt, modifier = Modifier.size(18.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  TUTORIAL
// ─────────────────────────────────────────────
private val PASOS_TUTORIAL = listOf(
    "¡Hola! Este es tu calendario para registrar cómo te sientes día a día y donde puedes agendar tus citas.",
    "Los puntos morados marcan tus días registrados y los naranjas tus próximas citas.",
    "Toca aquí para desplegar el mes completo o volver a la vista rápida.",
    "Elige la carita que mejor describa tu día y márcame con estrellas qué tan fuerte lo sentiste. Si fue un día de muchas emociones, usa la bolita de Mixto.",
    "Escribe cualquier pensamiento o detalle que quieras recordar.",
    "Toca Guardar para que quede registrado, o Hablar de esto si prefieres que conversemos sobre cómo te sentiste.",
    "Agrega tus controles médicos y te avisaremos a tiempo."
)

@Composable
private fun TutorialOverlay(
    paso: Int,
    zona: Rect?,
    onSiguiente: () -> Unit,
    onSaltar: () -> Unit
) {
    val density = LocalDensity.current
    val altoDp  = LocalConfiguration.current.screenHeightDp.dp
    val centroZonaDp = zona?.let { with(density) { ((it.top + it.bottom) / 2f).toDp() } }

    val alineacion = when {
        zona == null                   -> Alignment.Center
        centroZonaDp!! > altoDp * 0.5f -> Alignment.TopCenter
        else                           -> Alignment.BottomCenter
    }
    val paddingVertical = when (alineacion) {
        Alignment.TopCenter    -> PaddingValues(top = 90.dp)
        Alignment.BottomCenter -> PaddingValues(bottom = 130.dp)
        else                   -> PaddingValues(0.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(paso) { detectTapGestures { } }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val scrim = Color(0xA31A1A3C)
            if (zona != null && zona.width > 0f) {
                val hueco = Rect(zona.left - 4f, zona.top - 4f, zona.right + 4f, zona.bottom + 4f)
                val path = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addRoundRect(RoundRect(hueco, CornerRadius(52f, 52f)))
                    fillType = PathFillType.EvenOdd
                }
                drawPath(path, scrim)
            } else {
                drawRect(scrim)
            }
        }

        Column(
            modifier = Modifier
                .align(alineacion)
                .padding(paddingVertical)
                .padding(horizontal = 20.dp),
            horizontalAlignment = if (zona == null) Alignment.CenterHorizontally else Alignment.Start
        ) {

            if (zona == null) {
                BotMascota(color = CalLavanda, hasText = false, size = 72.dp)
                Spacer(Modifier.height(14.dp))
            }

            Row(verticalAlignment = Alignment.Bottom) {

                if (zona != null) {
                    BotMascota(color = CalLavanda, hasText = false, size = 44.dp)
                    Spacer(Modifier.width(10.dp))
                }

                Surface(
                    shape           = RoundedCornerShape(18.dp),
                    color           = Color.White,
                    shadowElevation = 8.dp,
                    modifier        = Modifier.weight(1f, fill = zona != null)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            PASOS_TUTORIAL[paso],
                            fontSize   = 13.sp,
                            color      = CalMoradoOscuro,
                            lineHeight = 20.sp,
                            textAlign  = if (zona == null) TextAlign.Center else TextAlign.Start,
                            modifier   = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                PASOS_TUTORIAL.indices.forEach { i ->
                                    Box(
                                        Modifier
                                            .size(width = if (i == paso) 16.dp else 6.dp, height = 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (i == paso) CalLavanda else Color(0xFFDDDEEF))
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Saltar",
                                    fontSize = 12.sp,
                                    color    = CalGrisTxt,
                                    modifier = Modifier.clickable { onSaltar() }.padding(8.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Button(
                                    onClick = onSiguiente,
                                    shape   = RoundedCornerShape(14.dp),
                                    colors  = ButtonDefaults.buttonColors(
                                        containerColor = CalLavanda, contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                                    elevation      = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Text(
                                        if (paso == PASOS_TUTORIAL.lastIndex) "Listo" else "Siguiente",
                                        fontSize   = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}