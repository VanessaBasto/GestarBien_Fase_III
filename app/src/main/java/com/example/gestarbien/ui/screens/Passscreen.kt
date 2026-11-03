package com.example.gestarbien.ui.screens
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ─────────────────────────────────────────────────────────────
//  PALETA — azul claro
// ─────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────
//  PALETA — azul claro
// ─────────────────────────────────────────────────────────────
private val PassAzul         = Color(0xFFADC8E8)
private val PassAzulSuave    = Color(0xFFD6E8F7)
private val PassMoradoOscuro = Color(0xFF1B3A6B)
private val PassMoradoMedio  = Color(0xFF2D5F9E)
private val PassGris         = Color(0xFF888888)
private val PassBlanco       = Color.White
// ─────────────────────────────────────────────────────────────
//  31 ítems del PASS
// ─────────────────────────────────────────────────────────────
private val passPreguntas = listOf(
    "Preocupación por el bebé / embarazo",
    "Miedo a que le pase algo malo al bebé",
    "Sensación de que algo malo va a pasar",
    "Preocupación por muchas cosas",
    "Preocupación por el futuro",
    "Sentirme abrumada",
    "Miedos fuertes (agujas, sangre, parto, dolor, etc.)",
    "Oleadas repentinas de miedo o malestar extremo",
    "Pensamientos repetitivos difíciles de detener o controlar",
    "Dificultad para dormir aunque tenga la oportunidad",
    "Necesitar hacer las cosas de cierta manera u orden",
    "Querer que todo sea perfecto",
    "Necesitar tener control de las cosas",
    "Dificultad para dejar de revisar o hacer cosas repetidas veces",
    "Sentirme nerviosa o asustadiza fácilmente",
    "Preocupación por pensamientos repetitivos",
    "Estar 'en alerta' o necesitar vigilar las cosas",
    "Malestar por recuerdos, sueños o pesadillas repetidas",
    "Preocupación de hacer el ridículo frente a otros",
    "Miedo a que otros me juzguen negativamente",
    "Sentirme muy incómoda en multitudes",
    "Evitar actividades sociales por nerviosismo",
    "Evitar cosas que me preocupan",
    "Sentirme desconectada, como si me viera desde fuera",
    "Perder la noción del tiempo y no recordar lo que pasó",
    "Dificultad para adaptarme a cambios recientes",
    "La ansiedad me impide hacer cosas",
    "Pensamientos acelerados que dificultan concentrarme",
    "Miedo a perder el control",
    "Sentirme en pánico",
    "Sentirme agitada"
)

private val passOpciones = listOf(
    "En absoluto",
    "A veces",
    "A menudo",
    "Casi siempre"
)

// Grupos para secciones visuales
private val passSecciones = listOf(
    "Preocupación y miedos específicos" to (0..7),
    "Perfeccionismo, control y trauma"  to (8..17),
    "Ansiedad social"                   to (18..22),
    "Ansiedad aguda y ajuste"           to (23..30)
)

// ─────────────────────────────────────────────────────────────
//  PANTALLA PASS
// ─────────────────────────────────────────────────────────────
@Composable
fun PassScreen(
    onBackClick: () -> Unit,
    onCompleted: (Int) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db  = FirebaseFirestore.getInstance()

    val respuestas  = remember { mutableStateListOf(*Array(passPreguntas.size) { -1 }) }
    var guardando    by remember { mutableStateOf(false) }
    var errorGuardar by remember { mutableStateOf("") }

    val total       = passPreguntas.size
    val respondidas = respuestas.count { it >= 0 }
    val completo    = respondidas == total
    val puntaje     = respuestas.sumOf { if (it >= 0) it else 0 }

    fun guardarYContinuar() {
        if (!completo || guardando) return
        guardando = true

        val data = mapOf(
            "tipo"       to "PASS",
            "puntaje"    to puntaje,
            "respuestas" to respuestas.toList(),
            "fecha"      to com.google.firebase.Timestamp.now()
        )

        db.collection("usuarios").document(uid)
            .collection("cuestionarios_clinicos")
            .add(data)
            .addOnSuccessListener {
                guardando = false
                onCompleted(puntaje)
            }
            .addOnFailureListener {
                guardando = false
                errorGuardar = "No se pudo guardar. Intenta de nuevo."
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo superior
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(110.dp))

            // ── Tarjeta intro ──────────────────────────────────────────
            Surface(
                shape           = RoundedCornerShape(24.dp),
                color           = PassBlanco,
                tonalElevation  = 4.dp,
                shadowElevation = 8.dp,
                modifier        = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp)) {
                    Text(
                        text       = "🧠  PASS",
                        fontSize   = 13.sp,
                        color      = PassGris,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text       = "Escala de Ansiedad Perinatal",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = PassMoradoOscuro
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text       = "Indica con qué frecuencia has experimentado cada situación durante el último mes.",
                        fontSize   = 13.sp,
                        color      = PassGris,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Progreso
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress   = { respondidas.toFloat() / total },
                            modifier   = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color      = PassAzul,
                            trackColor = PassAzulSuave
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text       = "$respondidas/$total",
                            fontSize   = 12.sp,
                            color = PassMoradoMedio, // era Color(0xFFB03060),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Preguntas por secciones ────────────────────────────────
            passSecciones.forEach { (titulo, rango) ->
                PassSeccionHeader(titulo = titulo)
                Spacer(modifier = Modifier.height(8.dp))

                rango.forEach { idx ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn() + slideInVertically { it / 3 }
                    ) {
                        PassPreguntaCard(
                            numero    = idx + 1,
                            pregunta  = passPreguntas[idx],
                            seleccion = respuestas[idx],
                            onSelect  = { respuestas[idx] = it }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Error ──────────────────────────────────────────────────
            if (errorGuardar.isNotBlank()) {
                Text(
                    text     = errorGuardar,
                    color    = Color(0xFFCC3333),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // ── Botón Finalizar ────────────────────────────────────────
            Button(
                onClick  = { guardarYContinuar() },
                enabled  = completo && !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = PassAzul,
                    disabledContainerColor = PassAzul.copy(alpha = 0.4f),
                    contentColor           = PassBlanco,
                    disabledContentColor   = PassBlanco.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        color       = PassBlanco,
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text       = if (completo) "Guardar y finalizar" else "Responde todas las preguntas",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // ── Barra superior (paso 2/2) ──────────────────────────────────
        OnboardingTopBar(
            currentStep = 2,
            totalSteps  = 2,
            onBack      = onBackClick
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  ENCABEZADO DE SECCIÓN
// ─────────────────────────────────────────────────────────────
@Composable
fun PassSeccionHeader(titulo: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(PassAzul)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text       = titulo.uppercase(),
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = PassMoradoMedio,
            letterSpacing = 0.8.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  TARJETA DE PREGUNTA PASS — versión compacta con opciones
//  horizontales (chips) para facilitar 31 respuestas
// ─────────────────────────────────────────────────────────────
@Composable
fun PassPreguntaCard(
    numero: Int,
    pregunta: String,
    seleccion: Int,
    onSelect: (Int) -> Unit
) {
    val bordeColor = if (seleccion >= 0) PassAzul else Color(0xFFE0E0E0)

    Surface(
        shape           = RoundedCornerShape(16.dp),
        color           = PassBlanco,
        tonalElevation  = 2.dp,
        shadowElevation = 3.dp,
        modifier        = Modifier
            .fillMaxWidth()
            .border(1.5.dp, bordeColor, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {

            // Número + pregunta
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier         = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (seleccion >= 0) PassAzul else Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "$numero",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (seleccion >= 0) PassBlanco else PassGris
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text       = pregunta,
                    fontSize   = 13.sp,
                    color      = PassMoradoOscuro,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    modifier   = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Opciones en grid 2×2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                passOpciones.subList(0, 2).forEachIndexed { i, texto ->
                    PassOpcionChip(
                        texto      = texto,
                        valor      = i,
                        seleccion  = seleccion,
                        onSelect   = onSelect,
                        modifier   = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                passOpciones.subList(2, 4).forEachIndexed { i, texto ->
                    PassOpcionChip(
                        texto      = texto,
                        valor      = i + 2,
                        seleccion  = seleccion,
                        onSelect   = onSelect,
                        modifier   = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PassOpcionChip(
    texto: String,
    valor: Int,
    seleccion: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val seleccionado = seleccion == valor

    Box(
        modifier          = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (seleccionado) PassAzulSuave else Color(0xFFF8F8F8))
            .border(
                1.5.dp,
                if (seleccionado) PassAzul else Color(0xFFEEEEEE),
                RoundedCornerShape(10.dp)
            )
            .clickable { onSelect(valor) }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment  = Alignment.Center
    ) {
        Text(
            text       = texto,
            fontSize   = 12.sp,
            fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (seleccionado) PassMoradoOscuro else PassGris,  // era Color(0xFFB03060)
            textAlign  = TextAlign.Center,
            lineHeight = 17.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  PANTALLA DE RESULTADO PASS
// ─────────────────────────────────────────────────────────────
@Composable
fun PassResultadoScreen(
    puntajeEpds: Int,
    puntajePass: Int,
    onVolverAlChat: () -> Unit
) {
    val (nivelPass, descPass, colorPass) = when {
        puntajePass <= 20 -> Triple(
            "Sin señales de alerta",
            "Por ahora la ansiedad no parece estar afectando tu día a día de forma significativa. Aun así, si algo cambia o simplemente quieres hablar, aquí estare.",
            Color(0xFF4CAF50)
        )
        puntajePass <= 41 -> Triple(
            "Algo te está afectando",
            "Tus respuestas sugieren que hay preocupaciones o tensiones que merecen atención. No es urgente, pero sí vale la pena mencionárselo a tu profesional en la próxima consulta.",
            Color(0xFFFFA726)
        )
        else -> Triple(
            "Estás cargando demasiado",
            "Lo que describes indica un nivel de ansiedad que no deberías manejar sola. Te recomendamos hablar con tu profesional de salud pronto. Si no puedes esperar, la Línea 106 está disponible las 24 horas.",
            Color(0xFF4A4E9B)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.42f)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(PassAzul)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
                .align(Alignment.BottomCenter)
                .background(PassBlanco)
        )

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Surface(
                shape           = RoundedCornerShape(24.dp),
                color           = PassBlanco,
                tonalElevation  = 4.dp,
                shadowElevation = 8.dp,
                modifier        = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text       = "¡Cuestionarios completados!",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = PassMoradoOscuro,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text      = "He guardado tus respuestas. Este es solo un momento de tu historia, y aquí estaré para acompañar cada página que decidas escribir.",
                        fontSize  = 13.sp,
                        color     = PassGris,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ResultadoResumenCard(
                        emoji     = "🌸", // Un emoji más suave que un portapapeles clínico
                        titulo    = "Tu reflejo emocional (EPDS)",
                        puntaje   = puntajeEpds,
                        maxPuntaje = 30,
                        nivel     = when {
                            puntajeEpds < 10 -> "Un presente con estabilidad"
                            puntajeEpds < 13 -> "Días con mayor sensibilidad"
                            else             -> "Un momento que merece compañía profesional"
                        },
                        color     = when {
                            puntajeEpds < 10 -> Color(0xFF6B8E23) // Un verde oliva suave, más natural
                            puntajeEpds < 13 -> Color(0xFFE6A15C) // Un durazno/cálido, no un naranja de tránsito
                            else             -> Color(0xFFCD5C5C) // Un rosa viejo/coral profundo, que alerta pero con suavidad
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Resumen PASS
                    ResultadoResumenCard(
                        emoji     = "🧠",
                        titulo    = "PASS — Ansiedad",
                        puntaje   = puntajePass,
                        maxPuntaje = 93,
                        nivel     = nivelPass,
                        color     = colorPass
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PassAzulSuave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text      = "Sobre el PASS: Un puntaje a partir de 26 nos indica que las preocupaciones están pesando más de lo usual.\n0–20: Un presente en calma · 21–41: Inquietudes leves a moderadas · 42–93: Días con una carga emocional intensa.",
                            fontSize  = 11.sp,
                            color     = Color(0xFF1E3A8A), // Un azul oscuro profundo que contrasta lindo y transmite calma
                            lineHeight = 17.sp,
                            modifier  = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick  = onVolverAlChat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PassAzul,
                            contentColor   = PassBlanco
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text       = "Volver al chat",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ResultadoResumenCard(
    emoji: String,
    titulo: String,
    puntaje: Int,
    maxPuntaje: Int,
    nivel: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.07f))
            .border(1.5.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = titulo,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = PassMoradoOscuro
            )
            Text(
                text      = nivel,
                fontSize  = 12.sp,
                color     = color,
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier         = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
                .border(1.5.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "$puntaje",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = color
                )
                Text(text = "/$maxPuntaje", fontSize = 9.sp, color = PassGris)
            }
        }
    }
}