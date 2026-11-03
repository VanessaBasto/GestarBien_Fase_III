package com.example.gestarbien.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.ui.layout.ContentScale
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
import android.content.Context
import com.example.gestarbien.R
import com.example.gestarbien.data.Paciente
import com.example.gestarbien.data.ProfesionalRepository
import com.example.gestarbien.data.ProfesionalesRepository
import com.example.gestarbien.data.ProfesionalPerfil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private val AzulOscuro    = Color(0xFF1B3A6B)
private val AzulMedio     = Color(0xFF2D5F9E)
private val AzulSuave     = Color(0xFFADC8E8)
private val AzulFondo     = Color(0xFFD6E8F7)
private val BlancoPuro    = Color.White
private val GrisTexto     = Color(0xFF555555)
private val RojoAlto      = Color(0xFFE74C3C)
private val RojoFondo     = Color(0xFFFFEBEA)
private val AmarilloMedio = Color(0xFFE67E22)
private val AmarilloFondo = Color(0xFFFFF3E0)
private val VerdeLeve     = Color(0xFF27AE60)
private val VerdeFondo    = Color(0xFFE8F8F0)
private val GrisNeutro    = Color(0xFF7F8C8D)
private val GrisFondo     = Color(0xFFF0F0F0)

private val fotosPacientes = listOf(
    R.drawable.mujer1,
    R.drawable.mujer2,
    R.drawable.mujer3,
    R.drawable.mujer4
)

@Composable
fun PanelClinicoScreen(
    onBackClick: () -> Unit,
    onPacienteClick: (Paciente, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val repo         = remember { ProfesionalRepository() }
    val perfilRepo   = remember { ProfesionalesRepository() }
    val scope  = rememberCoroutineScope()
    val auth   = FirebaseAuth.getInstance()
    val correo = auth.currentUser?.email ?: ""

    var pacientes by remember { mutableStateOf<List<Paciente>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg  by remember { mutableStateOf("") }

    var profesional by remember { mutableStateOf<ProfesionalPerfil?>(null) }
    val nombreProf = profesional?.nombre ?: "Profesional"

    LaunchedEffect(correo) {
        profesional = perfilRepo.obtenerPerfilPropio()
    }

    var alertas by remember { mutableStateOf<List<com.example.gestarbien.data.AlertaRiesgo>>(emptyList()) }
    val alertasRepo = remember { com.example.gestarbien.data.AlertasRepository() }

    DisposableEffect(correo) {
        val listener = alertasRepo.escucharAlertasPendientes(correo) { nuevas ->
            alertas = nuevas
        }
        onDispose { listener.remove() }
    }
    LaunchedEffect(correo) {
        scope.launch {
            try {
                pacientes = repo.getPacientesAsignados(correo)
            } catch (e: Exception) {
                errorMsg = "Error al cargar pacientes: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    val totalUsuarias   = pacientes.size
    val riesgoAlto      = pacientes.count { it.nivelGlobal().cls == "alto" }
    val conCuestionario = pacientes.count { it.tieneCuestionario }
    val sinRiesgoLeve   = pacientes.count { it.nivelGlobal().cls == "leve" }

    // ── Tutorial de bienvenida (una sola vez por profesional) ─────────────
    val prefs = remember { context.getSharedPreferences("gestarbien_prefs", Context.MODE_PRIVATE) }
    val claveTutorial = "tutorial_panel_clinico_$correo"
    var pasoTutorial by remember {
        mutableStateOf(if (prefs.getBoolean(claveTutorial, false)) -1 else 0)
    }
    val zonasTutorial = remember { mutableStateMapOf<Int, Rect>() }
    val scrollEstado = rememberScrollState()
    val density = LocalDensity.current
    val alturaPantallaDp = LocalConfiguration.current.screenHeightDp

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlancoPuro)
                .verticalScroll(scrollEstado)
        ) {

            // ── Header ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(AzulFondo)
                    .padding(horizontal = 24.dp, vertical = 28.dp)
                    .onGloballyPositioned { zonasTutorial[0] = it.boundsInRoot() }
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(
                            text       = "Hola, $nombreProf",
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color      = AzulOscuro
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text     = "Tienes $totalUsuarias usuarias asignadas",
                            fontSize = 14.sp,
                            color    = AzulMedio
                        )
                    }
                    Image(
                        painter            = painterResource(id = R.drawable.doctor),
                        contentDescription = "Doctor",
                        modifier           = Modifier.size(80.dp).clip(CircleShape),
                        contentScale       = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Fila 1 de estadísticas ────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    .onGloballyPositioned { zonasTutorial[1] = it.boundsInRoot() },
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaEstadistica(
                    modifier   = Modifier.weight(1f),
                    icono      = R.drawable.mujer1,
                    esIconoImg = true,
                    numero     = totalUsuarias,
                    etiqueta   = "Usuarias",
                    fondoColor = Color(0xFFEEF4FF),
                    iconoBg    = Color(0xFFF3E8FF)
                )
                TarjetaEstadisticaIcono(
                    modifier    = Modifier.weight(1f),
                    numero      = riesgoAlto,
                    etiqueta    = "Riesgo alto",
                    fondoColor  = Color(0xFFEEF4FF),
                    iconoBg     = Color(0xFFFFEBEA),
                    iconoBorder = Color(0xFFF09595),
                    icono       = "⚠️"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Fila 2 de estadísticas ────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaEstadisticaIcono(
                    modifier    = Modifier.weight(1f),
                    numero      = conCuestionario,
                    etiqueta    = "Cuestionarios",
                    fondoColor  = Color(0xFFEEF4FF),
                    iconoBg     = Color(0xFFD6E8F7),
                    iconoBorder = Color(0xFFADC8E8),
                    icono       = "📋"
                )
                TarjetaEstadisticaIcono(
                    modifier    = Modifier.weight(1f),
                    numero      = sinRiesgoLeve,
                    etiqueta    = "Sin riesgo/Leve",
                    fondoColor  = Color(0xFFEEF4FF),
                    iconoBg     = Color(0xFFE8F8F0),
                    iconoBorder = Color(0xFFA8D8A8),
                    icono       = "✅"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Lista de pacientes ────────────────────────────────────────────
            Text(
                text       = "Mis usuarias",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = AzulOscuro,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.onGloballyPositioned { zonasTutorial[2] = it.boundsInRoot() }) {
                when {
                    isLoading -> {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AzulMedio)
                        }
                    }
                    errorMsg.isNotBlank() -> {
                        Text(
                            text     = errorMsg,
                            color    = Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    pacientes.isEmpty() -> {
                        Text(
                            text     = "No tienes usuarias asignadas aún.",
                            color    = GrisTexto,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    else -> {
                        pacientes.forEachIndexed { index, paciente ->
                            TarjetaPaciente(
                                paciente = paciente,
                                fotoRes  = fotosPacientes[index % fotosPacientes.size],
                                onClick  = { onPacienteClick(paciente, index) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE74C3C)),
                border = BorderStroke(1.5.dp, Color(0xFFE74C3C))
            ) {
                Icon(
                    imageVector        = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── Overlay del tutorial ────────────────────────────────────────────
        if (pasoTutorial in PASOS_TUTORIAL_PANEL.indices) {
            val zonaPaso: Rect? = when (pasoTutorial) {
                0 -> null
                1 -> zonasTutorial[0]
                2 -> zonasTutorial[1]
                else -> zonasTutorial[2]
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

            TutorialOverlayPanel(
                paso = pasoTutorial,
                zona = zonaPaso,
                onSiguiente = {
                    if (pasoTutorial >= PASOS_TUTORIAL_PANEL.lastIndex) {
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

// ─────────────────────────────────────────────────────────────────────────────
//  TUTORIAL DEL PANEL CLÍNICO
// ─────────────────────────────────────────────────────────────────────────────

private val PASOS_TUTORIAL_PANEL = listOf(
    "¡Hola! Este es tu panel clínico. Aquí acompañas a las usuarias que te fueron asignadas al azar.",
    "Esta es tu información y cuántas usuarias tienes asignadas en este momento.",
    "Un resumen rápido: total de usuarias, cuántas están en riesgo alto, cuántas ya completaron su cuestionario, y cuántas están en riesgo leve o sin riesgo.",
    "Estas son tus usuarias. Tócalas para ver su cuestionario psicosocial completo y sus puntajes EPDS y PASS actualizados. Si una usuaria repitió la evaluación, verás un ícono ⓘ junto al puntaje para comparar los resultados en el tiempo."
)

@Composable
private fun TutorialOverlayPanel(
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
                BotMascota(color = AzulSuave, hasText = false, size = 72.dp)
                Spacer(Modifier.height(14.dp))
            }

            Row(verticalAlignment = Alignment.Bottom) {

                if (zona != null) {
                    BotMascota(color = AzulSuave, hasText = false, size = 44.dp)
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
                            PASOS_TUTORIAL_PANEL[paso],
                            fontSize   = 13.sp,
                            color      = AzulOscuro,
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
                                PASOS_TUTORIAL_PANEL.indices.forEach { i ->
                                    Box(
                                        Modifier
                                            .size(width = if (i == paso) 16.dp else 6.dp, height = 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (i == paso) AzulSuave else Color(0xFFDDDEEF))
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Saltar",
                                    fontSize = 12.sp,
                                    color    = GrisTexto,
                                    modifier = Modifier.clickable { onSaltar() }.padding(8.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Button(
                                    onClick = onSiguiente,
                                    shape   = RoundedCornerShape(14.dp),
                                    colors  = ButtonDefaults.buttonColors(
                                        containerColor = AzulSuave, contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                                    elevation      = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Text(
                                        if (paso == PASOS_TUTORIAL_PANEL.lastIndex) "Listo" else "Siguiente",
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

// ─────────────────────────────────────────────────────────────────────────────
//  TARJETA ESTADÍSTICA (con imagen)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaEstadistica(
    modifier   : Modifier,
    icono      : Int,
    esIconoImg : Boolean,
    numero     : Int,
    etiqueta   : String,
    fondoColor : Color,
    iconoBg    : Color
) {
    Surface(
        modifier        = modifier,
        shape           = RoundedCornerShape(16.dp),
        color           = Color(0xFFEEF4FF),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3E8FF))
                    .border(1.5.dp, Color(0xFFE8D5F0), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(id = icono),
                    contentDescription = null,
                    modifier           = Modifier.size(44.dp),
                    contentScale       = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text       = numero.toString(),
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AzulOscuro
                )
                Text(
                    text     = etiqueta,
                    fontSize = 12.sp,
                    color    = GrisTexto
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TARJETA ESTADÍSTICA (con emoji)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaEstadisticaIcono(
    modifier    : Modifier,
    numero      : Int,
    etiqueta    : String,
    fondoColor  : Color,
    iconoBg     : Color,
    iconoBorder : Color,
    icono       : String
) {
    Surface(
        modifier        = modifier,
        shape           = RoundedCornerShape(16.dp),
        color           = Color(0xFFEEF4FF),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconoBg)
                    .border(1.5.dp, iconoBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icono, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text       = numero.toString(),
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AzulOscuro
                )
                Text(
                    text       = etiqueta,
                    fontSize   = 12.sp,
                    color      = GrisTexto,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TARJETA DE PACIENTE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarjetaPaciente(
    paciente : Paciente,
    fotoRes  : Int,
    onClick  : () -> Unit
) {
    val nivel      = paciente.nivelGlobal()
    val nivelColor = when (nivel.cls) {
        "alto"  -> RojoAlto
        "medio" -> AmarilloMedio
        "leve"  -> VerdeLeve
        else    -> GrisNeutro
    }
    val nivelFondo = when (nivel.cls) {
        "alto"  -> RojoFondo
        "medio" -> AmarilloFondo
        "leve"  -> VerdeFondo
        else    -> GrisFondo
    }
    val nivelLabel = when (nivel.cls) {
        "alto"  -> "ALTO"
        "medio" -> "MEDIO"
        "leve"  -> "LEVE"
        else    -> "INCOMPLETO"
    }

    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape           = RoundedCornerShape(16.dp),
        color           = BlancoPuro,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter            = painterResource(id = fotoRes),
                contentDescription = paciente.nombre,
                modifier           = Modifier.size(52.dp).clip(CircleShape),
                contentScale       = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = paciente.nombre,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = AzulOscuro
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text     = "${paciente.edadAnios} años · Última sesión: ${paciente.ultimaSesion}",
                    fontSize = 12.sp,
                    color    = GrisTexto
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(nivelFondo)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = nivelLabel,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = nivelColor
                )
            }
        }
    }
}