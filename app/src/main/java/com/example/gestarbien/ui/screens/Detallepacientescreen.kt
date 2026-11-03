package com.example.gestarbien.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R
import com.example.gestarbien.data.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────────
//  COLORES COMPARTIDOS (azul clínico)
// ─────────────────────────────────────────────────────────────────────────────
private val AzulOscuro    = Color(0xFF1B3A6B)
private val AzulMedio     = Color(0xFF2D5F9E)
private val AzulFondo     = Color(0xFFD6E8F7)
private val BlancoPuro    = Color.White
private val GrisTexto     = Color(0xFF555555)
private val RojoAlto      = Color(0xFFE74C3C)
private val RojoFondo     = Color(0xFFFFEBEA)
private val NaranjaAlto   = Color(0xFFE67E22)
private val NaranjaFondo  = Color(0xFFFFF3E0)
private val VerdeColor    = Color(0xFF27AE60)
private val VerdeFondo    = Color(0xFFE8F8F0)
private val GrisNeutro    = Color(0xFF7F8C8D)
private val GrisFondo     = Color(0xFFF2F3F4)
private val VerdeWhatsapp = Color(0xFF25D366)

private val fmtFecha = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "CO"))

// Periodicidad recomendada entre evaluaciones (EPDS/PASS), en días.
private const val PERIODICIDAD_DIAS_EVALUACION = 7L

/**
 * Arma el texto "Realizado hace X días · faltan Y días para el siguiente"
 * a partir de la fecha (ms) de la evaluación más reciente. Devuelve null
 * si no hay una fecha válida para calcular (fechaMs <= 0).
 */
private fun textoIntervaloEvaluacion(fechaUltimaMs: Long): String? {
    if (fechaUltimaMs <= 0L) return null

    val diasDesde = ((System.currentTimeMillis() - fechaUltimaMs) / (1000L * 60 * 60 * 24))
        .coerceAtLeast(0L)
    val faltan = PERIODICIDAD_DIAS_EVALUACION - diasDesde

    val textoDesde = when (diasDesde) {
        0L   -> "Realizado hoy"
        1L   -> "Realizado ayer"
        else -> "Realizado hace $diasDesde días"
    }

    val textoFaltan = when {
        faltan > 1L  -> "faltan $faltan días para el siguiente"
        faltan == 1L -> "falta 1 día para el siguiente"
        else         -> "ya puede realizar el siguiente"
    }

    return "$textoDesde · $textoFaltan"
}

// ─────────────────────────────────────────────────────────────────────────────
//  PANTALLA PRINCIPAL: DETALLE DE PACIENTE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DetallePacienteScreen(
    paciente    : Paciente,
    fotoRes     : Int? = null,          // nullable: si es null se muestra avatar emoji
    onBackClick : () -> Unit
) {
    var verCuestionario by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (verCuestionario) {
        CuestionarioPsicosocialDetalleScreen(
            paciente    = paciente,
            onBackClick = { verCuestionario = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlancoPuro)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Header ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(AzulFondo)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                // Botón atrás
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BlancoPuro)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint               = AzulOscuro,
                        modifier           = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = paciente.nombre,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AzulOscuro
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = buildString {
                            append("${paciente.edadAnios} años · ")
                            if (paciente.esPostparto) {
                                append("Posparto · bebé de ${paciente.semanas} sem.")
                            } else {
                                append("Gestante · ${paciente.semanas} sem.")
                            }
                        },
                        fontSize = 13.sp,
                        color    = AzulMedio
                    )
                    if (paciente.telefono.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text           = "Escribir por WhatsApp",
                            fontSize       = 13.sp,
                            fontWeight     = FontWeight.Bold,
                            color          = VerdeWhatsapp,
                            textDecoration = TextDecoration.Underline,
                            modifier       = Modifier.clickable {
                                val numero = paciente.telefono.filter { it.isDigit() }
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/$numero")
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Título sección ────────────────────────────────────────────────
        Text(
            text       = "Panel clínico",
            fontSize   = 17.sp,
            fontWeight = FontWeight.Bold,
            color      = AzulOscuro,
            modifier   = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ── Estado de riesgo (escalas + señales de atención) ─────────────
        TarjetaEstadoRiesgo(paciente = paciente)

        Spacer(modifier = Modifier.height(10.dp))

        // ── Tarjeta EPDS ──────────────────────────────────────────────────
        TarjetaPsicometrica(
            titulo    = "Depresión Perinatal · EPDS",
            subtitulo = "Edinburgh Postnatal Depression Scale",
            iconoRes  = R.drawable.parto,
            puntos    = paciente.epds,
            historial = paciente.historialEpds,
            nivelFn   = { nivelEPDS(it) },
            altoColor = RojoAlto,
            altoFondo = RojoFondo,
            medioColor= NaranjaAlto,
            medioFondo= NaranjaFondo,
            leveColor = VerdeColor,
            leveFondo = VerdeFondo
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ── Tarjeta PASS ──────────────────────────────────────────────────
        TarjetaPsicometrica(
            titulo    = "Ansiedad Perinatal · PASS",
            subtitulo = "Perinatal Anxiety Screening Scale",
            iconoRes  = R.drawable.animo_ansiosa,
            puntos    = paciente.pass,
            historial = paciente.historialPass,
            nivelFn   = { nivelPASS(it) },
            altoColor = RojoAlto,
            altoFondo = RojoFondo,
            medioColor= NaranjaAlto,
            medioFondo= NaranjaFondo,
            leveColor = VerdeColor,
            leveFondo = VerdeFondo
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ── Tarjeta Factores Psicosociales ────────────────────────────────
        TarjetaFactoresPsicosociales(
            factores        = paciente.factoresPsico,
            tieneCuestionario = paciente.tieneCuestionario,
            onVerRespuestas = { verCuestionario = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ── Tarjeta Barreras de Acceso ───────────────────────────────────
        TarjetaBarrerasAcceso(barreras = paciente.barreras)

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TARJETA PSICOMÉTRICA (EPDS / PASS) — con botón de comparación
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaPsicometrica(
    titulo    : String,
    subtitulo : String,
    iconoRes  : Int,
    puntos    : PuntajePsicometrico?,
    historial : List<PuntajeConFecha>,
    nivelFn   : (Int) -> NivelRiesgo,
    altoColor : Color,
    altoFondo : Color,
    medioColor: Color,
    medioFondo: Color,
    leveColor : Color,
    leveFondo : Color
) {
    var mostrarComparativa by remember { mutableStateOf(false) }

    if (mostrarComparativa) {
        DialogoComparativa(
            titulo = titulo,
            historial = historial,
            nivelFn = nivelFn,
            altoColor = altoColor, altoFondo = altoFondo,
            medioColor = medioColor, medioFondo = medioFondo,
            leveColor = leveColor, leveFondo = leveFondo,
            onDismiss = { mostrarComparativa = false }
        )
    }

    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape           = RoundedCornerShape(16.dp),
        color           = BlancoPuro,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                // FIX: sin caja de color de fondo — solo el ícono PNG, a su
                // color propio (antes era un emoji sobre un Box con .background()).
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter            = painterResource(id = iconoRes),
                        contentDescription = null,
                        modifier           = Modifier.size(38.dp),
                        contentScale       = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = titulo,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AzulOscuro
                    )
                    Text(
                        text     = subtitulo,
                        fontSize = 11.sp,
                        color    = GrisTexto
                    )
                }
                // Botón de comparación — solo aparece si hay 2+ evaluaciones
                if (historial.size >= 2) {
                    IconButton(
                        onClick = { mostrarComparativa = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ver comparativa de evaluaciones",
                            tint = AzulMedio,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (puntos == null) {
                // Sin datos
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GrisFondo)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text     = "Sin datos — evaluación pendiente",
                        fontSize = 13.sp,
                        color    = GrisNeutro
                    )
                }
                return@Column
            }

            val nivel      = nivelFn(puntos.puntaje)
            val etqColor   = when (nivel.cls) {
                "alto"  -> altoColor
                "medio" -> medioColor
                else    -> leveColor
            }
            val etqFondo   = when (nivel.cls) {
                "alto"  -> altoFondo
                "medio" -> medioFondo
                else    -> leveFondo
            }
            val barraColor = etqColor
            val progreso   = puntos.puntaje.toFloat() / puntos.max.toFloat()

            Spacer(modifier = Modifier.height(10.dp))

            // Puntaje + etiqueta
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "${puntos.puntaje}",
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AzulOscuro
                    )
                    Text(
                        text     = "Puntos de ${puntos.max}",
                        fontSize = 11.sp,
                        color    = GrisTexto
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(etqFondo)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = nivel.label,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = etqColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progreso.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(barraColor)
                )
            }

            val textoIntervalo = textoIntervaloEvaluacion(historial.lastOrNull()?.fechaMs ?: 0L)
            if (textoIntervalo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = textoIntervalo,
                    fontSize = 11.sp,
                    color = GrisNeutro
                )
            }

            if (historial.size >= 2) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tiene ${historial.size} evaluaciones — toca el ícono ⓘ para comparar",
                    fontSize = 11.sp,
                    color = AzulMedio,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DIÁLOGO: COMPARATIVA ENTRE PRIMERA EVALUACIÓN Y LA MÁS RECIENTE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DialogoComparativa(
    titulo    : String,
    historial : List<PuntajeConFecha>,
    nivelFn   : (Int) -> NivelRiesgo,
    altoColor : Color, altoFondo : Color,
    medioColor: Color, medioFondo: Color,
    leveColor : Color, leveFondo : Color,
    onDismiss : () -> Unit
) {
    val primera = historial.first()
    val ultima  = historial.last()
    val diasEntre = if (primera.fechaMs > 0 && ultima.fechaMs > 0)
        abs(ultima.fechaMs - primera.fechaMs) / (1000L * 60 * 60 * 24)
    else null

    val delta = ultima.puntaje - primera.puntaje
    val mejoro = delta < 0   // menos puntaje = menos síntomas = mejoró

    fun colorNivel(puntaje: Int): Color = when (nivelFn(puntaje).cls) {
        "alto" -> altoColor
        "medio" -> medioColor
        else -> leveColor
    }
    fun fondoNivel(puntaje: Int): Color = when (nivelFn(puntaje).cls) {
        "alto" -> altoFondo
        "medio" -> medioFondo
        else -> leveFondo
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BlancoPuro,
        title = {
            Text("Comparativa · $titulo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
        },
        text = {
            Column {
                if (diasEntre != null) {
                    val etiquetaIntervalo = when {
                        diasEntre in 10..18 -> "Aprox. seguimiento a 14 días"
                        else -> "$diasEntre días entre evaluaciones"
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AzulFondo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            etiquetaIntervalo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulOscuro,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ColumnaComparativa(
                        etiqueta = "Primera evaluación",
                        fecha = if (primera.fechaMs > 0) fmtFecha.format(Date(primera.fechaMs)) else "Sin fecha",
                        puntaje = primera.puntaje,
                        nivelLabel = nivelFn(primera.puntaje).label,
                        color = colorNivel(primera.puntaje),
                        fondo = fondoNivel(primera.puntaje)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = GrisNeutro,
                        modifier = Modifier.size(20.dp).align(Alignment.CenterVertically)
                    )
                    ColumnaComparativa(
                        etiqueta = "Más reciente",
                        fecha = if (ultima.fechaMs > 0) fmtFecha.format(Date(ultima.fechaMs)) else "Sin fecha",
                        puntaje = ultima.puntaje,
                        nivelLabel = nivelFn(ultima.puntaje).label,
                        color = colorNivel(ultima.puntaje),
                        fondo = fondoNivel(ultima.puntaje)
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(Modifier.height(12.dp))

                val textoDelta = when {
                    delta == 0 -> "Sin cambio en el puntaje."
                    mejoro -> "Bajó ${-delta} puntos — posible mejoría."
                    else -> "Subió $delta puntos — posible empeoramiento."
                }
                Text(
                    textoDelta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (delta == 0) GrisTexto else if (mejoro) VerdeColor else RojoAlto
                )

                if (historial.size > 2) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Hay ${historial.size} evaluaciones en total. Se muestra la primera y la más reciente.",
                        fontSize = 11.sp,
                        color = GrisNeutro
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = AzulMedio, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun ColumnaComparativa(
    etiqueta: String,
    fecha: String,
    puntaje: Int,
    nivelLabel: String,
    color: Color,
    fondo: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(110.dp)) {
        Text(etiqueta, fontSize = 11.sp, color = GrisTexto, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(fecha, fontSize = 10.sp, color = GrisNeutro, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("$puntaje", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(fondo)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(nivelLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TARJETA FACTORES PSICOSOCIALES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaFactoresPsicosociales(
    factores          : List<FactorPsicosocial>,
    tieneCuestionario : Boolean,
    onVerRespuestas   : () -> Unit
) {
    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape           = RoundedCornerShape(16.dp),
        color           = BlancoPuro,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado
            Row(verticalAlignment = Alignment.CenterVertically) {
                // FIX: sin caja de color de fondo — antes tenía un Box con
                // .background(Color(0xFFF3E8FF)) detrás del emoji 🔍.
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter            = painterResource(id = R.drawable.analisis),
                        contentDescription = null,
                        modifier           = Modifier.size(38.dp),
                        contentScale       = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text       = "Factores Psicosociales",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AzulOscuro
                    )
                    Text(
                        text     = "Resumen",
                        fontSize = 11.sp,
                        color    = GrisTexto
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!tieneCuestionario || factores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GrisFondo)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text     = "Sin datos — cuestionario psicosocial pendiente",
                        fontSize = 13.sp,
                        color    = GrisNeutro
                    )
                }
                return@Column
            }

            // Lista de factores
            factores.forEach { factor ->
                val punto = when (factor.nivel) {
                    "alto"     -> Color(0xFFE74C3C)
                    "moderado" -> Color(0xFFE67E22)
                    else       -> Color(0xFF27AE60)
                }
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text     = factor.nombre,
                        fontSize = 13.sp,
                        color    = GrisTexto,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(punto)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Leyenda
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                listOf(
                    "Alto"     to Color(0xFFE74C3C),
                    "Moderado" to Color(0xFFE67E22),
                    "Bajo"     to Color(0xFF27AE60)
                ).forEach { (label, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = label, fontSize = 10.sp, color = GrisTexto)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Link "Ver respuestas completas"
                Row(
                    modifier          = Modifier.clickable { onVerRespuestas() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text     = "Ver respuestas completas",
                        fontSize = 11.sp,
                        color    = AzulMedio,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint               = AzulMedio,
                        modifier           = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PANTALLA: CUESTIONARIO PSICOSOCIAL — RESPUESTAS COMPLETAS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CuestionarioPsicosocialDetalleScreen(
    paciente    : Paciente,
    onBackClick : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlancoPuro)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Header ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(AzulFondo)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BlancoPuro)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint               = AzulOscuro,
                        modifier           = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text       = "Cuestionario Psicosocial",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AzulOscuro
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text     = "Respuestas completas · ${paciente.nombre}",
                        fontSize = 12.sp,
                        color    = AzulMedio
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!paciente.tieneCuestionario || paciente.cuestionarioSecciones.isEmpty()) {
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📋", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text      = "La usuaria aún no ha completado\nel cuestionario psicosocial.",
                        fontSize  = 14.sp,
                        color     = GrisNeutro,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@Column
        }

        // Secciones del cuestionario
        paciente.cuestionarioSecciones.forEach { seccion ->
            SeccionCuestionarioCard(seccion = seccion)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TARJETA DE SECCIÓN DEL CUESTIONARIO
//  NOTA: seccion.icono / seccion.iconoBg / seccion.iconoColor se reciben ya
//  armados desde donde se construyen los SeccionCuestionario (no en este
//  archivo). Para ajustar el color de "Situación socioeconómica" (mochila
//  naranja) y de "Antecedentes de salud mental" hay que editar ese archivo,
//  no este.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SeccionCuestionarioCard(seccion: SeccionCuestionario) {

    val (nivelLabel, nivelColor, nivelFondo, barraColor, barraAncho) = remember(seccion.nivelGeneral) {
        when (seccion.nivelGeneral) {
            "alto"     -> NivelUI("Alto",     RojoAlto,    RojoFondo,    RojoAlto,    0.85f)
            "moderado" -> NivelUI("Moderado", NaranjaAlto, NaranjaFondo, NaranjaAlto, 0.55f)
            else       -> NivelUI("Bajo",     VerdeColor,  VerdeFondo,   VerdeColor,  0.25f)
        }
    }

    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape           = RoundedCornerShape(16.dp),
        color           = BlancoPuro,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Cabecera sección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(seccion.iconoBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = seccion.icono,
                        contentDescription = null,
                        tint               = seccion.iconoColor,
                        modifier           = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = seccion.titulo,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AzulOscuro
                    )
                    Text(
                        text     = "${seccion.preguntas.size} preguntas respondidas",
                        fontSize = 11.sp,
                        color    = GrisTexto
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(nivelFondo)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = nivelLabel,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = nivelColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de nivel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barraAncho)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(barraColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de preguntas/respuestas
            seccion.preguntas.forEachIndexed { idx, pregunta ->
                if (idx > 0) {
                    HorizontalDivider(
                        color     = Color(0xFFF0F0F0),
                        thickness = 1.dp,
                        modifier  = Modifier.padding(vertical = 6.dp)
                    )
                }
                FilaPregunta(pregunta = pregunta)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FILA: PREGUNTA + RESPUESTA + NIVEL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FilaPregunta(pregunta: PreguntaCuestionario) {

    // Normalizar la respuesta para mostrar un texto más legible
    val respuestaMostrada = normalizarRespuesta(pregunta.respuesta)
    val respuestaColor = GrisNeutro

    // Las respuestas largas (listas de varias opciones) se apilan debajo
    // de la pregunta en vez de al lado, para no dejar un hueco vertical
    // cuando el texto ocupa varias líneas.
    val esRespuestaLarga = respuestaMostrada.length > 28

    if (esRespuestaLarga) {
        var expandido by remember(pregunta.pregunta) { mutableStateOf(false) }
        val excedeLimite = respuestaMostrada.length > 90

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text       = pregunta.pregunta,
                fontSize   = 13.sp,
                color      = GrisTexto,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text       = respuestaMostrada,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = respuestaColor,
                lineHeight = 18.sp,
                maxLines   = if (expandido || !excedeLimite) Int.MAX_VALUE else 2,
                overflow   = TextOverflow.Ellipsis
            )
            if (excedeLimite) {
                Text(
                    text       = if (expandido) "Ver menos" else "Ver más",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = AzulMedio,
                    modifier   = Modifier
                        .padding(top = 2.dp)
                        .clickable { expandido = !expandido }
                )
            }
        }
    } else {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = pregunta.pregunta,
                fontSize   = 13.sp,
                color      = GrisTexto,
                modifier   = Modifier.weight(1f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text       = respuestaMostrada,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = respuestaColor,
                textAlign  = TextAlign.End,
                maxLines   = 2
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HELPER: NORMALIZAR RESPUESTAS (vacíos, "nada", listas, etc.)
// ─────────────────────────────────────────────────────────────────────────────

private fun normalizarRespuesta(respuesta: String): String {
    val r = respuesta.trim()
    return when {
        r.isBlank()
                || r.equals("sin dato", ignoreCase = true)
                || r.equals("na", ignoreCase = true)
                || r.equals("n/a", ignoreCase = true)
                || r.equals("-", ignoreCase = true)
                || r.equals("prefiero no decir", ignoreCase = true) -> "No respondió"

        r.equals("no aplica", ignoreCase = true) -> "No aplica"
        r.equals("ninguna", ignoreCase = true)   -> "Ninguna"
        r.equals("ninguno", ignoreCase = true)   -> "Ninguno"
        r.equals("nada", ignoreCase = true)      -> "Nada"

        r.equals("no", ignoreCase = true)        -> "No"
        r.equals("sí", ignoreCase = true)
                || r.equals("si", ignoreCase = true) -> "Sí"

        else -> r
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DATA CLASS AUXILIAR (solo para desestructuración interna)
// ─────────────────────────────────────────────────────────────────────────────

private data class NivelUI(
    val label     : String,
    val color     : Color,
    val fondo     : Color,
    val barraColor: Color,
    val barraAncho: Float
)

// ─────────────────────────────────────────────────────────────────────────────
//  TARJETA: BARRERAS DE ACCESO
//  Muestra, por categoría, si la usuaria reportó dificultades y cuáles.
//  Sirve para que el profesional entienda por qué alguien falta a
//  controles, antes de leerlo como desinterés.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaBarrerasAcceso(barreras: BarrerasPacienteUI?) {

    Surface(
        modifier        = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape           = RoundedCornerShape(16.dp),
        color           = BlancoPuro,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                // FIX: sin caja de color de fondo — antes tenía un Box con
                // .background(Color(0xFFF3ECFB)) detrás del emoji 🚧.
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter            = painterResource(id = R.drawable.candado),
                        contentDescription = null,
                        modifier           = Modifier.size(38.dp),
                        contentScale       = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Barreras de acceso", fontSize = 14.sp,
                        fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("Del cuestionario psicosocial", fontSize = 11.sp, color = GrisTexto)
                }
                if (barreras != null && barreras.requiereAlerta) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(RojoFondo)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Revisar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RojoAlto)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (barreras == null || !barreras.tieneRespuestas) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GrisFondo)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text     = "Sin datos — módulo de barreras pendiente",
                        fontSize = 13.sp,
                        color    = GrisNeutro
                    )
                }
                return@Column
            }

            if (barreras.acceso.isNotBlank()) {
                Text("Necesidad de apoyo emocional y acceso", fontSize = 12.sp, color = GrisTexto)
                Text(
                    barreras.acceso, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp,
                    color = if (barreras.acceso.startsWith("Lo necesité")) RojoAlto else AzulMedio
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (barreras.experiencia.isNotBlank()) {
                Text("Experiencia con la atención recibida", fontSize = 12.sp, color = GrisTexto)
                Text(
                    barreras.experiencia, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp,
                    color = if (barreras.experiencia.startsWith("No,")) RojoAlto else GrisTexto
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!barreras.hayAlguna) {
                Text(
                    "No reportó dificultades para acceder a la atención.",
                    fontSize = 13.sp, color = GrisTexto, lineHeight = 18.sp
                )
            } else {
                barreras.categorias.filter { it.tiene }.forEach { cat ->
                    Column(modifier = Modifier.padding(vertical = 5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(7.dp).clip(CircleShape).background(NaranjaAlto)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(cat.titulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GrisTexto)
                        }
                        if (cat.detalles.isNotEmpty()) {
                            Text(
                                cat.detalles.joinToString(" · "),
                                fontSize = 12.sp, color = GrisNeutro,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(start = 14.dp, top = 1.dp)
                            )
                        }
                    }
                }

                if (barreras.impacto.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Impacto reportado", fontSize = 12.sp, color = GrisTexto,
                            modifier = Modifier.weight(1f))
                        Text(barreras.impacto, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = if (barreras.requiereAlerta) RojoAlto else AzulMedio)
                    }
                }

                if (barreras.facilitadores.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Lo que facilitaría su acceso: " + barreras.facilitadores.joinToString(", "),
                        fontSize = 12.sp, color = VerdeColor, lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TARJETA: ESTADO DE RIESGO
//  Nivel = peor resultado entre EPDS y PASS; sube un escalón si hay una señal
//  de atención alta (violencia reciente, pide ayuda emocional). Debajo se
//  listan las señales para que el profesional vea el porqué.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaEstadoRiesgo(paciente: Paciente) {
    val nivel = paciente.nivelGlobal()
    val (color, fondo) = when (nivel.cls) {
        "alto"  -> RojoAlto to RojoFondo
        "medio" -> NaranjaAlto to NaranjaFondo
        "leve"  -> VerdeColor to VerdeFondo
        else    -> GrisNeutro to GrisFondo
    }
    val detalle = when {
        nivel.cls == "incompleto" -> "Aún no hay resultados de EPDS ni PASS."
        paciente.subioPorAlertas  ->
            "Según EPDS/PASS: ${paciente.nivelPorEscalas().label}. Subió un escalón por señales de atención."
        else -> "Calculado con el peor resultado entre EPDS y PASS."
    }

    Surface(
        modifier        = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape           = RoundedCornerShape(16.dp),
        color           = BlancoPuro,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Estado de riesgo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text(detalle, fontSize = 11.sp, color = GrisTexto, lineHeight = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(fondo)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(nivel.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                }
            }

            if (paciente.alertas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Señales de atención", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GrisTexto)
                Spacer(modifier = Modifier.height(4.dp))
                paciente.alertas.forEach { alerta ->
                    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (alerta.nivel == "alto") RojoAlto else NaranjaAlto)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(alerta.texto, fontSize = 12.sp, color = GrisTexto, lineHeight = 17.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Herramienta de apoyo: no reemplaza el juicio clínico.",
                fontSize = 10.sp, color = GrisNeutro
            )
        }
    }
}