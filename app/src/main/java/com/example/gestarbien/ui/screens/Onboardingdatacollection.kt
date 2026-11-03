package com.example.gestarbien.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.example.gestarbien.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale


// ────────────────────────────────────────────────────────────────────────────
//  BARRA DE PROGRESO FIJA
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingTopBar(
    currentStep: Int,
    totalSteps: Int,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFB1B4E1))
            .padding(top = 36.dp, bottom = 16.dp, start = 16.dp, end = 24.dp)
            .zIndex(1f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF5C5FA8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Spacer(modifier = Modifier.width(52.dp))
            }
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color.White,
                trackColor = Color(0xFFD0D3F0)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "$currentStep/$totalSteps",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  MINI CALENDARIO INLINE
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun MiniCalendario(fechaSeleccionada: LocalDate?, onFechaSeleccionada: (LocalDate) -> Unit) {
    var mesActual by remember {
        mutableStateOf(fechaSeleccionada?.let { YearMonth.from(it) } ?: YearMonth.now())
    }
    var editandoAnio by remember { mutableStateOf(false) }
    var anioTexto by remember { mutableStateOf(mesActual.year.toString()) }

    val diasDelMes = mesActual.lengthOfMonth()
    val primerDiaSemana = mesActual.atDay(1).dayOfWeek.value % 7

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFFDDD0F8), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { mesActual = mesActual.minusMonths(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("‹", fontSize = 22.sp, color = Color(0xFF5C5FA8), fontWeight = FontWeight.Bold)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        editandoAnio = !editandoAnio
                        anioTexto = mesActual.year.toString()
                    }
                ) {
                    Text(
                        text = mesActual.month
                            .getDisplayName(JavaTextStyle.FULL, Locale("es"))
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3A3D8C)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (editandoAnio) {
                        BasicTextField(
                            value = anioTexto,
                            onValueChange = { nuevo ->
                                if (nuevo.length <= 4 && nuevo.all { it.isDigit() }) {
                                    anioTexto = nuevo
                                    if (nuevo.length == 4) {
                                        val anio = nuevo.toIntOrNull()
                                        if (anio != null && anio in 1900..2100) {
                                            mesActual = YearMonth.of(anio, mesActual.month)
                                            editandoAnio = false
                                        }
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val anio = anioTexto.toIntOrNull()
                                    if (anio != null && anio in 1900..2100) {
                                        mesActual = YearMonth.of(anio, mesActual.month)
                                    }
                                    editandoAnio = false
                                }
                            ),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5C5FA8),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .width(52.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEEEFFE))
                                .border(1.dp, Color(0xFFB1B4E1), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEEEFFE))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = mesActual.year.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5C5FA8)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (editandoAnio) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF9999BB),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { mesActual = mesActual.plusMonths(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("›", fontSize = 22.sp, color = Color(0xFF5C5FA8), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("D", "L", "M", "X", "J", "V", "S").forEach { dia ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(dia, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9999BB))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            val filas = ((primerDiaSemana + diasDelMes) + 6) / 7
            for (fila in 0 until filas) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val dia = fila * 7 + col - primerDiaSemana + 1
                        val esDia = dia in 1..diasDelMes
                        val fecha = if (esDia) mesActual.atDay(dia) else null
                        val seleccionado = fecha != null && fecha == fechaSeleccionada
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (seleccionado) Color(0xFFB1B4E1) else Color.Transparent)
                                .then(if (esDia) Modifier.clickable { onFechaSeleccionada(fecha!!) } else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            if (esDia) Text(
                                "$dia",
                                fontSize = 13.sp,
                                color = if (seleccionado) Color.White else Color(0xFF3A3D8C),
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  HELPERS SEMANAS
// ────────────────────────────────────────────────────────────────────────────
fun emojiPorSemanas(semanas: Int): String = when {
    semanas <= 4  -> "🌱"
    semanas <= 12 -> "🌿"
    semanas <= 24 -> "🪴"
    else          -> "🌳"
}

fun etiquetaSemanas(semanas: Int): String = if (semanas == 1) "1 semana" else "$semanas semanas"

@Composable
fun SemanasSpinBox(semanas: Int, onCambio: (Int) -> Unit, min: Int = 1, max: Int = 40) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFFB1B4E1), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (semanas > min) Color(0xFFB1B4E1) else Color(0xFFE0DFEE))
                .clickable(enabled = semanas > min) { onCambio(semanas - 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("−", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emojiPorSemanas(semanas), fontSize = 36.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                etiquetaSemanas(semanas),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3A3D8C)
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (semanas < max) Color(0xFFB1B4E1) else Color(0xFFE0DFEE))
                .clickable(enabled = semanas < max) { onCambio(semanas + 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

fun calcularRangoEdad(fechaNacimiento: String): String? {
    if (fechaNacimiento.isBlank()) return null
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaNac = LocalDate.parse(fechaNacimiento, formatter)
        val edad = Period.between(fechaNac, LocalDate.now()).years
        val inicio = (edad / 5) * 5
        "$inicio – ${inicio + 4} años"
    }.getOrNull()
}

// ────────────────────────────────────────────────────────────────────────────
//  PASO MOOD – ¿Cómo te has sentido?
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingMoodScreen(onContinue: (String) -> Unit, onBack: () -> Unit) {
    val emociones = listOf(
        "😊" to "Tranquila",
        "😰" to "Ansiosa",
        "😢" to "Triste",
        "🥰" to "Feliz",
        "😤" to "Estresada",
        "🌀" to "Mixto"
    )
    var seleccionado by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.45f).align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(Color(0xFFB1B4E1))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.55f).align(Alignment.BottomCenter)
                .background(Color.White)
        )
        OnboardingTopBar(currentStep = 1, totalSteps = 3, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Ahora personalizemos tu experiencia",
                        fontSize = 12.sp, color = Color(0xFF9999BB),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "¿Cómo te has sentido?",
                        fontSize = 26.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A3D8C),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "En las últimas semanas, emocionalmente...",
                        fontSize = 13.sp, color = Color(0xFF9999BB),
                        lineHeight = 18.sp, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    emociones.chunked(2).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            fila.forEach { (emoji, label) ->
                                MoodOptionCard(
                                    emoji = emoji, label = label,
                                    selected = seleccionado == label,
                                    onClick = { seleccionado = label },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { if (seleccionado.isNotBlank()) onContinue(seleccionado) },
                        enabled = seleccionado.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF5C5FA8),
                            disabledContainerColor = Color.White,
                            disabledContentColor = Color(0xFFB1B4E1)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (seleccionado.isNotBlank()) Color(0xFFB1B4E1) else Color(0xFFDDD0F8)
                        )
                    ) {
                        Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MoodOptionCard(
    emoji: String, label: String, selected: Boolean,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFFEEEFFE) else Color.White)
            .border(
                1.5.dp,
                if (selected) Color(0xFFB1B4E1) else Color(0xFFDDD0F8),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            color = if (selected) Color(0xFF5C5FA8) else Color(0xFF3A3D8C)
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  PASO EMBARAZO
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingPregnancyScreen(
    onContinue: (fechaParto: String?) -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    var capitulo by remember { mutableStateOf("") }
    // FIX bug "0 semanas": arranca en 1 de verdad, en vez de 0 con parche visual.
    var semanas by remember { mutableIntStateOf(1) }
    var quiereSeguimientoParto by remember { mutableStateOf(false) }
    var semanasBebePostparto by remember { mutableIntStateOf(4) }

    fun calcularFechaParto(sem: Int): String =
        LocalDate.now().plusWeeks((40 - sem).toLong())
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).align(Alignment.TopCenter)
            .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
            .background(Color(0xFFB1B4E1)))
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.55f).align(Alignment.BottomCenter)
            .background(Color.White))

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(100.dp))
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White,
                tonalElevation = 4.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {

                    Text("Momento actual · Opcional", fontSize = 12.sp, color = Color(0xFF9999BB), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("¿Qué capítulo de tu historia estás escribiendo hoy?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D8C), lineHeight = 32.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Cada historia es única, y merece ser acompañada. Cuéntanos en qué etapa estás para empezar.", fontSize = 13.sp, color = Color(0xFF9999BB), lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(18.dp))

                    // ── Selector ──
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Espero a mi bebé" to R.drawable.mama, "Mi bebé ya llegó" to R.drawable.bebe)
                            .forEach { (label, imgRes) ->
                                val seleccionado = capitulo == label
                                Column(modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(if (seleccionado) 2.dp else 1.5.dp, if (seleccionado) Color(0xFF5C5FA8) else Color(0xFFDDD0F8), RoundedCornerShape(16.dp))
                                    .clickable { capitulo = label; quiereSeguimientoParto = false }
                                    .padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(painterResource(imgRes), label, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(label, fontSize = 13.sp, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                        color = if (seleccionado) Color(0xFF3A3D8C) else Color(0xFF888888),
                                        textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 6.dp))
                                }
                            }
                    }

                    // ════════════════════════════════════════
                    //  Rama A – Espero a mi bebé
                    // ════════════════════════════════════════
                    if (capitulo == "Espero a mi bebé") {
                        Spacer(modifier = Modifier.height(22.dp))
                        Text("Actualmente tengo...", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D8C))
                        Spacer(modifier = Modifier.height(12.dp))
                        SemanasSpinBox(semanas = semanas, onCambio = { semanas = it })
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.5.dp, if (quiereSeguimientoParto) Color(0xFF5C5FA8) else Color(0xFFDDD0F8), RoundedCornerShape(14.dp))
                            .clickable { quiereSeguimientoParto = !quiereSeguimientoParto }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = quiereSeguimientoParto, onCheckedChange = { quiereSeguimientoParto = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF5C5FA8), uncheckedColor = Color(0xFFB1B4E1)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("¿Quieres ver una fecha estimada para la llegada de tu bebé?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3A3D8C))
                                Text("Si lo deseas, puedes activar esta opción para visualizar este momento en tu calendario.", fontSize = 11.sp, color = Color(0xFF9999BB), lineHeight = 16.sp)
                            }
                        }

                        if (quiereSeguimientoParto) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEEEFFE), modifier = Modifier.fillMaxWidth()) {
                                Text("Fecha estimada de parto: ${calcularFechaParto(semanas)}",
                                    fontSize = 13.sp, color = Color(0xFF3A3D8C), fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Es una estimación orientativa. La fecha exacta la confirma tu médico.",
                                fontSize = 11.sp, color = Color(0xFF999999), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    // ════════════════════════════════════════
                    //  Rama B – Mi bebé ya llegó
                    // ════════════════════════════════════════
                    if (capitulo == "Mi bebé ya llegó") {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Actualmente mi bebé tiene...", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D8C))
                        Spacer(modifier = Modifier.height(12.dp))
                        SemanasSpinBox(semanas = semanasBebePostparto, onCambio = { semanasBebePostparto = it }, min = 0, max = 52)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEEEFFE), modifier = Modifier.fillMaxWidth()) {
                            Text("Ya soy mamá de un bebé de ${etiquetaSemanas(semanasBebePostparto)}",
                                fontSize = 13.sp, color = Color(0xFF3A3D8C), fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Esta información nos ayuda a acompañarte en tu etapa de posparto.",
                            fontSize = 11.sp, color = Color(0xFF999999), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }

                    // ── Botón Continuar ──
                    Spacer(modifier = Modifier.height(20.dp))
                    val puedeAvanzar = when (capitulo) {
                        "Espero a mi bebé" -> true
                        "Mi bebé ya llegó" -> true
                        else -> false
                    }
                    OutlinedButton(
                        onClick = {
                            when (capitulo) {
                                "Espero a mi bebé" -> {
                                    // Se codifica siempre "embarazo:{semanas}:{fechaEstimadaOVacío}"
                                    // para que las semanas viajen aunque no active el seguimiento.
                                    val fechaEstimada = if (quiereSeguimientoParto) calcularFechaParto(semanas) else ""
                                    onContinue("embarazo:$semanas:$fechaEstimada")
                                }
                                "Mi bebé ya llegó" -> onContinue("bebe:$semanasBebePostparto")
                                else -> onSkip()
                            }
                        },
                        enabled = puedeAvanzar,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF5C5FA8), disabledContainerColor = Color.White, disabledContentColor = Color(0xFFB1B4E1)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (puedeAvanzar) Color(0xFFB1B4E1) else Color(0xFFE0DFEE))
                    ) {
                        Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        OnboardingTopBar(currentStep = 2, totalSteps = 3, onBack = onBack)
    }
}


// ────────────────────────────────────────────────────────────────────────────
//  PASO RESUMEN – recibe TODO desde MainActivity y guarda en Firebase
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingSummaryScreen(
    sentimientoInicial: String,
    fechaParto: String?,
    datos: DatosSocioDemograficos,          // ← viene del cuestionario
    antecedentes: Antecedentes,             // ← viene del cuestionario
    infoAdicional: String,                  // ← viene del cuestionario
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val userName = FirebaseAuth.getInstance().currentUser?.displayName
        ?.takeIf { it.isNotBlank() }
        ?: datos.nombreCompleto.split(" ").firstOrNull()?.takeIf { it.isNotBlank() }
        ?: "tú"

    val rangoEdad: String? = calcularRangoEdad(datos.fechaNacimiento)

    val db  = remember { FirebaseFirestore.getInstance() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // Se guarda una sola vez cuando se pulsa "Comenzar"
    fun guardarTodo() {
        uid ?: return

        // "fechaParto" llega codificado desde OnboardingPregnancyScreen:
        //   - "bebe:{semanas}"                  → posparto
        //   - "embarazo:{semanas}:{fechaOVacío}" → gestante (con o sin fecha estimada)
        //   - null / ""                          → sin dato / se saltó el paso
        val valor = fechaParto ?: ""
        val esPostparto = valor.startsWith("bebe:")
        val esEmbarazo  = valor.startsWith("embarazo:")

        val semanasBebeGuardar: String
        val semanasEmbarazoGuardar: String
        val fechaPartoGuardar: String

        when {
            esPostparto -> {
                semanasBebeGuardar     = valor.removePrefix("bebe:")
                semanasEmbarazoGuardar = ""
                fechaPartoGuardar      = ""
            }
            esEmbarazo -> {
                val partes = valor.removePrefix("embarazo:").split(":")
                semanasEmbarazoGuardar = partes.getOrNull(0) ?: ""
                fechaPartoGuardar      = partes.getOrNull(1)?.takeIf { it.isNotBlank() } ?: ""
                semanasBebeGuardar     = ""
            }
            else -> {
                semanasBebeGuardar     = ""
                semanasEmbarazoGuardar = ""
                fechaPartoGuardar      = ""
            }
        }

        // Se guarda también como Long en el documento principal del usuario
        // ("semanas"), porque el panel admin y ProfesionalRepository leen ese campo.
        val semanasLong: Long = semanasEmbarazoGuardar.toLongOrNull() ?: 0L

        val onboardingData = hashMapOf(
            "nombreCompleto"       to datos.nombreCompleto,
            "telefono"             to datos.telefono,
            "fechaNacimiento"      to datos.fechaNacimiento,
            "sentimientoinicial"   to sentimientoInicial,
            "fechaPartoEstimada"   to fechaPartoGuardar,
            "semanasBebePostparto" to semanasBebeGuardar,
            "semanas"              to semanasLong,
            "onboardingCompleto"   to true,
            "cuestionarioCompleto" to true
        )

        // Subcolección con todos los datos del cuestionario
        val cuestionarioData = hashMapOf<String, Any>(
            // Datos sociodemográficos
            "nombreCompleto"       to datos.nombreCompleto,
            "telefono"             to datos.telefono,
            "fechaNacimiento"      to datos.fechaNacimiento,
            "lugarResidencia"      to datos.lugarResidencia,
            "estrato"              to datos.estratoSocioeconomico,
            "tipoVivienda"         to datos.tipoVivienda,
            "tipoViviendaOtra"     to datos.tipoViviendaOtra,
            "condicionesVivienda"  to datos.condicionesVivienda,
            "afiliacionSalud"      to datos.afiliacionSalud,
            "ocupacion"            to datos.ocupacion,
            "nivelEscolar"         to datos.nivelEscolar,
            "tipoContrato"         to datos.tipoContrato,
            "ingresosMensuales"    to datos.ingresosMensuales,
            "composicionFamiliar"  to datos.composicionFamiliar,
            "estadoCivil"          to datos.estadoCivil,
            "relacionPareja"       to datos.relacionPareja,
            "relacionesFamiliares" to datos.relacionesFamiliares,
            "redDeApoyo"           to datos.redDeApoyo,
            "redApoyoOtro"         to datos.redApoyoOtro,

            // Antecedentes
            "momentoActual"        to antecedentes.momentoActual,
            "semanasEmbarazo"      to semanasEmbarazoGuardar,

            // Embarazos previos (A01 – A03)
            "embarazoPrevio"       to antecedentes.embarazoPrevio,
            "embarazos"            to antecedentes.embarazos,
            "perdidaGestacional"   to antecedentes.perdidaGestacional,

            // Violencia
            "violenciaGeneral"     to antecedentes.violenciaGeneral,
            "victimaFisico"        to antecedentes.victimaFisico,
            "victimaPsicologico"   to antecedentes.victimaPsicologico,
            "victimaAbuso"         to antecedentes.victimaAbuso,
            "alertaViolencia"      to antecedentes.violenciaPositiva,

            // Consumo de sustancias
            "consumoSustancias"    to antecedentes.consumoSustancias,
            "consumoAlcohol"       to antecedentes.consumoAlcohol,
            "consumoPsicoactivas"  to antecedentes.consumoPsicoactivas,
            "consumoDetalle"       to antecedentes.consumoSustanciasDetalle,
            "consumoPareja"        to antecedentes.consumoPareja,

            // Diagnósticos y estado de ánimo
            "diagAnsiedad"         to antecedentes.diagnosticoAnsiedad,
            "diagDepresion"        to antecedentes.diagnosticoDepresion,
            "tristezaMesPasado"    to antecedentes.tristeza,
            "pocoInteresMesPasado" to antecedentes.pocoInteres,
            "necesitaAyuda"        to antecedentes.necesitaAyuda,

            // Tratamiento (solo se llena si hubo ansiedad o depresión)
            "psicoterapia"         to antecedentes.tratamientoPsicoterapia,
            "sesionesPsico"        to antecedentes.sesiones,
            "culminoPsico"         to antecedentes.culmino,
            "farmacoterapia"       to antecedentes.farmacoterapia,
            "medicamentos"         to antecedentes.medicamentos,
            "otroTratamiento"      to antecedentes.otroTratamiento,

            // Info adicional y mood
            "infoAdicional"        to infoAdicional,
            "sentimientoinicial"   to sentimientoInicial,
            "fechaPartoEstimada"   to fechaPartoGuardar,
            "semanasBebePostparto" to semanasBebeGuardar,
            "cuestionarioCompleto" to true,
            "fechaCompletado"      to com.google.firebase.Timestamp.now()
        )

        db.collection("usuarios").document(uid)
            .set(onboardingData, com.google.firebase.firestore.SetOptions.merge())

        db.collection("usuarios").document(uid)
            .collection("cuestionarios")
            .add(cuestionarioData)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.45f).align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(Color(0xFFB1B4E1))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.55f).align(Alignment.BottomCenter)
                .background(Color.White)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            Surface(
                shape = RoundedCornerShape(24.dp), color = Color.White,
                tonalElevation = 4.dp, shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Resumen Final", fontSize = 12.sp,
                        color = Color(0xFF9999BB), fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "¡Todo listo, $userName!",
                        fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A3D8C), textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tu espacio de acompañamiento emocional está listo.",
                        fontSize = 14.sp, color = Color(0xFF9999BB),
                        textAlign = TextAlign.Center, lineHeight = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "RESUMEN DE TU PERFIL",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF9E86C8), letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Rango de edad calculado desde fechaNacimiento
                    if (!rangoEdad.isNullOrBlank()) {
                        ProfileSummaryCard(image = R.drawable.pastel, text = rangoEdad)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Sentimiento
                    ProfileSummaryCard(
                        image = R.drawable.sentir,
                        text = "Me he sentido $sentimientoInicial"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    when {
                        fechaParto.isNullOrBlank() -> {
                            ProfileSummaryCard(
                                image = R.drawable.parto,
                                text  = "Sin seguimiento de embarazo"
                            )
                        }
                        fechaParto.startsWith("bebe:") -> {
                            val semBebe = fechaParto.removePrefix("bebe:").toIntOrNull() ?: 0
                            val textoSem = if (semBebe == 1) "1 semana" else "$semBebe semanas"
                            ProfileSummaryCard(
                                image = R.drawable.parto,
                                text  = "Ya soy mamá de un bebé de $textoSem"
                            )
                        }
                        fechaParto.startsWith("embarazo:") -> {
                            val partes = fechaParto.removePrefix("embarazo:").split(":")
                            val semEmb = partes.getOrNull(0)?.toIntOrNull() ?: 0
                            val fechaEst = partes.getOrNull(1)?.takeIf { it.isNotBlank() }
                            val textoSem = if (semEmb == 1) "1 semana" else "$semEmb semanas"
                            ProfileSummaryCard(
                                image = R.drawable.parto,
                                text  = if (fechaEst != null)
                                    "Estás en la semana $semEmb · Fecha estimada de parto: $fechaEst"
                                else
                                    "Estás en $textoSem de embarazo"
                            )
                        }
                        else -> {
                            // Compatibilidad con el formato antiguo
                            ProfileSummaryCard(
                                image = R.drawable.parto,
                                text  = "Fecha estimada de parto: $fechaParto"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEEEFFE))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF5C5FA8))) {
                                    append("El chatbot y todas las herramientas")
                                }
                                append(" están disponibles desde el inicio.")
                            },
                            fontSize = 13.sp, color = Color(0xFF555555), lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = {
                            guardarTodo()
                            onContinue()
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF5C5FA8)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFB1B4E1))
                    ) {
                        Text("Comenzar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        OnboardingTopBar(currentStep = 3, totalSteps = 3, onBack = onBack)
    }
}

@Composable
fun ProfileSummaryCard(image: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFFB1B4E1), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = Color(0xFF3A3D8C), fontWeight = FontWeight.Medium)
    }
}