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
import androidx.compose.ui.zIndex
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ─────────────────────────────────────────────────────────────
//  PALETA (coherente con onboarding)
// ─────────────────────────────────────────────────────────────
private val EpdsLavanda      = Color(0xFFB1B4E1)
private val EpdsLavandaSuave = Color(0xFFEEEFFE)
private val EpdsMoradoOscuro = Color(0xFF3A3D8C)
private val EpdsMoradoMedio  = Color(0xFF5C5FA8)
private val EpdsGris         = Color(0xFF888888)
private val EpdsBlanco       = Color.White
private val EpdsAlertaFondo  = Color(0xFFFFF3E0)
private val EpdsAlertaBorde  = Color(0xFFFFB74D)

// ─────────────────────────────────────────────────────────────
//  MODELO DE DATOS — 10 ítems EPDS
// ─────────────────────────────────────────────────────────────
data class EpdsItem(
    val pregunta: String,
    val opciones: List<String>,       // índice 0..3 → puntaje 0..3
    val invertida: Boolean = false    // true si a)=0, b)=1, c)=2, d)=3  (la mayoría)
)

private val epdsItems = listOf(
    EpdsItem(
        pregunta = "He sido capaz de reírme y ver el lado divertido de las cosas.",
        opciones = listOf(
            "Tanto como siempre",
            "No tanto ahora",
            "Mucho menos ahora",
            "No, nada"
        )
    ),
    EpdsItem(
        pregunta = "He disfrutado mirar hacia delante.",
        opciones = listOf(
            "Tanto como siempre",
            "Menos que antes",
            "Muchos menos que antes",
            "Casi nada"
        )
    ),
    EpdsItem(
        pregunta = "Cuando las cosas han salido mal me he culpado a mi misma innecesariamente.",
        opciones = listOf(
            "No, nunca",
            "No, con mucha frecuencia",
            "Sí, a veces",
            "Sí, la mayor parte del tiempo"
        )
    ),
    EpdsItem(
        pregunta = "He estado nerviosa o inquieta sin tener motivo.",
        opciones = listOf(
            "No, nunca",
            "Casi nunca",
            "Sí, a veces",
            "Sí, con mucha frecuencia"
        )
    ),
    EpdsItem(
        pregunta = "He sentido miedo o he estado asustadiza sin tener motivo.",
        opciones = listOf(
            "No, nunca",
            "No, no mucho",
            "Sí, a veces",
            "Sí, bastante"
        )
    ),
    EpdsItem(
        pregunta = "Las cosas me han estado abrumando.",
        opciones = listOf(
            "No, he estado haciendo las cosas tan bien como siempre",
            "No, la mayor parte del tiempo he hecho las cosas bastante bien",
            "Sí, a veces no he podido hacer las cosas tan bien como siempre",
            "Sí, la mayor parte del tiempo no he podido hacer las cosas en absoluto"
        )
    ),
    EpdsItem(
        pregunta = "Me he sentido tan desdichada que he tenido dificultades para dormir.",
        opciones = listOf(
            "No, nunca",
            "No, con mucha frecuencia",
            "Sí, a veces",
            "Sí, la mayor parte del tiempo"
        )
    ),
    EpdsItem(
        pregunta = "Me he sentido triste o desgraciada.",
        opciones = listOf(
            "No, nunca",
            "No, con mucha frecuencia",
            "Sí, a veces",
            "Sí, la mayor parte del tiempo"
        )
    ),
    EpdsItem(
        pregunta = "Me he sentido tan desdichada que he estado llorando.",
        opciones = listOf(
            "No, nunca",
            "Sólo ocasionalmente",
            "Sí, bastante a menudo",
            "Sí, la mayor parte del tiempo"
        )
    ),
    EpdsItem(
        pregunta = "Se me ha ocurrido la idea de hacerme daño.",
        opciones = listOf(
            "Nunca",
            "Casi nunca",
            "A veces",
            "Sí, bastante a menudo"
        )
    )
)

// ─────────────────────────────────────────────────────────────
//  PANTALLA EPDS
// ─────────────────────────────────────────────────────────────
@Composable
fun EpdsScreen(
    onBackClick: () -> Unit,
    onCompleted: (Int) -> Unit   // devuelve el puntaje total
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db  = FirebaseFirestore.getInstance()

    // Respuestas: índice de opción seleccionada (-1 = sin responder)
    val respuestas = remember { mutableStateListOf(*Array(epdsItems.size) { -1 }) }

    var guardando   by remember { mutableStateOf(false) }
    var errorGuardar by remember { mutableStateOf("") }

    val total       = epdsItems.size
    val respondidas = respuestas.count { it >= 0 }
    val completo    = respondidas == total

    // Puntaje: suma directa de índices (0-3)
    val puntaje     = respuestas.sumOf { if (it >= 0) it else 0 }

    // Alerta ítem 10 (índice 9) — cualquier puntaje > 0 requiere evaluación
    val alertaItem10 = respuestas[9] > 0

    fun guardarYContinuar() {
        if (!completo || guardando) return
        guardando = true

        val data = mapOf(
            "tipo"         to "EPDS",
            "puntaje"      to puntaje,
            "respuestas"   to respuestas.toList(),
            "alertaItem10" to alertaItem10,
            "fecha"        to com.google.firebase.Timestamp.now()
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

        // Fondo superior lavanda
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.32f)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(EpdsLavanda)
        )

        // Fondo inferior blanco
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.BottomCenter)
                .background(EpdsBlanco)
        )

        // Contenido con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(110.dp))   // espacio para TopBar

            // ── Tarjeta intro ──────────────────────────────────────────
            Surface(
                shape           = RoundedCornerShape(24.dp),
                color           = EpdsBlanco,
                tonalElevation  = 4.dp,
                shadowElevation = 8.dp,
                modifier        = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp)) {
                    Text(
                        text       = "📋  EPDS",
                        fontSize   = 13.sp,
                        color      = EpdsGris,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text       = "Escala de Depresión de Edimburgo",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = EpdsMoradoOscuro
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text       = "Marca la respuesta que más se acerca a cómo te has sentido en los últimos 7 días.",
                        fontSize   = 13.sp,
                        color      = EpdsGris,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progreso
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress    = { respondidas.toFloat() / total },
                            modifier    = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color       = EpdsLavanda,
                            trackColor  = EpdsLavandaSuave
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text       = "$respondidas/$total",
                            fontSize   = 12.sp,
                            color      = EpdsMoradoMedio,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Preguntas ──────────────────────────────────────────────
            epdsItems.forEachIndexed { idx, item ->
                AnimatedVisibility(
                    visible = true,
                    enter   = fadeIn() + slideInVertically { it / 3 }
                ) {
                    EpdsPreguntaCard(
                        numero     = idx + 1,
                        item       = item,
                        seleccion  = respuestas[idx],
                        onSelect   = { respuestas[idx] = it },
                        esUltima   = idx == epdsItems.lastIndex
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                    containerColor         = EpdsLavanda,
                    disabledContainerColor = EpdsLavanda.copy(alpha = 0.4f),
                    contentColor           = EpdsBlanco,
                    disabledContentColor   = EpdsBlanco.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        color       = EpdsBlanco,
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text       = if (completo) "Guardar y continuar" else "Responde todas las preguntas",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // ── Barra superior ─────────────────────────────────────────────
        OnboardingTopBar(
            currentStep = 1,
            totalSteps  = 2,
            onBack      = onBackClick
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  TARJETA DE PREGUNTA EPDS
// ─────────────────────────────────────────────────────────────
@Composable
fun EpdsPreguntaCard(
    numero: Int,
    item: EpdsItem,
    seleccion: Int,
    onSelect: (Int) -> Unit,
    esUltima: Boolean = false
) {
    val bordeColor = if (seleccion >= 0) EpdsLavanda else Color(0xFFE0E0E0)

    Surface(
        shape           = RoundedCornerShape(18.dp),
        color           = EpdsBlanco,
        tonalElevation  = 2.dp,
        shadowElevation = 4.dp,
        modifier        = Modifier
            .fillMaxWidth()
            .border(1.5.dp, bordeColor, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {

            // Número + pregunta
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier         = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (seleccion >= 0) EpdsLavanda else Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "$numero",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (seleccion >= 0) EpdsBlanco else EpdsGris
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text       = item.pregunta,
                    fontSize   = 14.sp,
                    color      = EpdsMoradoOscuro,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 21.sp,
                    modifier   = Modifier.weight(1f)
                )
            }


            Spacer(modifier = Modifier.height(14.dp))

            // Opciones
            item.opciones.forEachIndexed { optIdx, opcion ->
                val seleccionado = seleccion == optIdx
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (seleccionado) EpdsLavandaSuave else EpdsBlanco
                        )
                        .border(
                            1.5.dp,
                            if (seleccionado) EpdsLavanda else Color(0xFFEEEEEE),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(optIdx) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier         = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (seleccionado) EpdsLavanda else Color(0xFFEEEEEE))
                            .border(
                                1.5.dp,
                                if (seleccionado) EpdsMoradoMedio else Color(0xFFCCCCCC),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (seleccionado) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EpdsBlanco)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text       = opcion,
                        fontSize   = 13.sp,
                        color      = if (seleccionado) EpdsMoradoOscuro else EpdsGris,
                        fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = 18.sp
                    )
                }

                if (optIdx < item.opciones.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  PANTALLA DE RESULTADO EPDS
// ─────────────────────────────────────────────────────────────
@Composable
fun EpdsResultadoScreen(
    puntaje: Int,
    onContinuar: () -> Unit  // navega hacia PASS
) {
    val (nivel, descripcion, colorNivel) = when {
        puntaje < 10 -> Triple(
            "Un presente en equilibrio",
            "Tus respuestas sugieren que estás transitando este momento con estabilidad. Sigue cuidándote con la misma gentileza.",
            Color(0xFF6B8E23)
        )
        puntaje < 13 -> Triple(
            "Días con mayor sensibilidad",
            "Estás cargando un poco más de peso emocional de lo usual. Sería valioso mencionárselo a tu profesional de salud en tu próxima consulta.",
            Color(0xFFE6A15C)
        )
        else -> Triple(
            "Un momento que merece compañía",
            "Tus respuestas reflejan una carga emocional intensa. No tienes que sostener esto sola — mereces apoyo. Por favor, habla con tu profesional de salud pronto.",
            Color(0xFFCD5C5C)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(EpdsLavanda)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .background(EpdsBlanco)
        )

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape           = RoundedCornerShape(24.dp),
                color           = EpdsBlanco,
                tonalElevation  = 4.dp,
                shadowElevation = 8.dp,
                modifier        = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = "Resultado EPDS",
                        fontSize   = 13.sp,
                        color      = EpdsGris,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Puntaje
                    Box(
                        modifier         = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(colorNivel.copy(alpha = 0.12f))
                            .border(2.dp, colorNivel, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text       = "$puntaje",
                                fontSize   = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color      = colorNivel
                            )
                            Text(text = "/ 30", fontSize = 11.sp, color = EpdsGris)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text       = nivel,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = colorNivel,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text       = descripcion,
                        fontSize   = 13.sp,
                        color      = EpdsGris,
                        lineHeight = 20.sp,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EpdsLavandaSuave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text      = "Sobre el EPDS: Un puntaje de 13 o más en el embarazo, o de 10 o más en el posparto, nos indica que vale la pena buscar acompañamiento. Si respondiste algo distinto de cero en la pregunta 10, por favor habla con tu profesional de salud en las próximas 24 horas.",
                            fontSize  = 11.sp,
                            color     = EpdsMoradoMedio,
                            lineHeight = 17.sp,
                            modifier  = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick  = onContinuar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EpdsLavanda,
                            contentColor   = EpdsBlanco
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text       = "Continuar al cuestionario PASS →",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}