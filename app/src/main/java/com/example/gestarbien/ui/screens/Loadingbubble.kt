package com.example.gestarbien.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Mensajes que rotan mientras espera la respuesta. Mismo tono cálido y
// sobrio del bot: nada de "procesando", nada robótico.
private val MENSAJES_ESPERA = listOf(
    "Pensando cómo acompañarte...",
    "Leyendo con calma lo que me contaste...",
    "Buscando las palabras...",
    "Un momento, ya casi...",
    "Aquí sigo, dame un segundo...",
)

/**
 * Burbuja de "escribiendo..." con puntos animados y un mensaje que va
 * rotando cada cierto tiempo, en vez de dejar la pantalla en blanco
 * mientras se espera la respuesta del backend.
 *
 * Uso: mostrar este composable en la lista de mensajes mientras
 * `responderChat()` está en curso (por ejemplo, con un estado
 * `isLoading: Boolean` en tu ViewModel).
 */
@Composable
fun BurbujaEscribiendo(
    modifier: Modifier = Modifier,
    colorFondo: Color = Color(0xFFF0F0F0),
    colorTexto: Color = Color(0xFF757575),
) {
    var indiceMensaje by remember { mutableIntStateOf(0) }

    // Rota el mensaje cada 3.5s. Empieza en un índice aleatorio para que
    // no siempre arranque igual si el usuario manda varios mensajes seguidos.
    LaunchedEffect(Unit) {
        indiceMensaje = MENSAJES_ESPERA.indices.random()
        while (true) {
            delay(3500)
            indiceMensaje = (indiceMensaje + 1) % MENSAJES_ESPERA.size
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colorFondo)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PuntosAnimados(color = colorTexto)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = MENSAJES_ESPERA[indiceMensaje],
            color = colorTexto,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Los tres puntitos clásicos de "escribiendo...", con animación de opacidad. */
@Composable
private fun PuntosAnimados(color: Color) {
    val transicion = rememberInfiniteTransition(label = "puntos")

    Row {
        repeat(3) { i ->
            val alpha by transicion.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = i * 150),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "punto_$i",
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color.copy(alpha = alpha))
            )
        }
    }
}