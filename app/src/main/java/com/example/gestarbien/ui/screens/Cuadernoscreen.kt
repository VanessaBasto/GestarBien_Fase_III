package com.example.gestarbien.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.data.Conversacion
import com.example.gestarbien.data.ConversacionesRepository

private val CuLavanda      = Color(0xFFB1B4E1)
private val CuLavandaSuave = Color(0xFFEEEFFE)
private val CuMoradoOscuro = Color(0xFF3A3D8C)
private val CuMoradoMedio  = Color(0xFF5C5FA8)
private val CuGrisTxt      = Color(0xFF888888)
private val CuBorde        = Color(0xFFE4E5F5)
private val CuFondo        = Color(0xFFFBFBFE)

@Composable
fun CuadernoScreen(
    botColor: Color = CuLavanda,
    onBackClick: () -> Unit,
    onNuevaConversacion: () -> Unit,
    onAbrirConversacion: (String) -> Unit
) {
    var conversaciones by remember { mutableStateOf<List<Conversacion>>(emptyList()) }
    var cargando       by remember { mutableStateOf(true) }
    var aBorrar        by remember { mutableStateOf<Conversacion?>(null) }

    LaunchedEffect(Unit) {
        conversaciones = ConversacionesRepository.listar()
        cargando = false
    }

    Scaffold(containerColor = CuFondo) { inner ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {

            // ── Barra superior ───────────────────────────────
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
                        tint = CuMoradoOscuro
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mi cuaderno", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CuMoradoOscuro)
                    Text(
                        if (conversaciones.isEmpty()) "Aquí quedan nuestras conversaciones"
                        else "${conversaciones.size} conversación${if (conversaciones.size == 1) "" else "es"} guardada${if (conversaciones.size == 1) "" else "s"}",
                        fontSize = 11.sp,
                        color    = CuGrisTxt
                    )
                }
            }

            // ── Botón de nueva conversación ──────────────────
            Surface(
                shape    = RoundedCornerShape(16.dp),
                color    = CuLavandaSuave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .border(1.5.dp, CuLavanda, RoundedCornerShape(16.dp))
                    .clickable { onNuevaConversacion() }
            ) {
                Row(
                    modifier              = Modifier.padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, null, tint = CuMoradoOscuro, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nueva conversación", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CuMoradoOscuro)
                }
            }

            when {
                cargando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CuLavanda)
                    }
                }

                // ── Estado vacío: la mascota explica para qué sirve ──
                conversaciones.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        BotMascota(color = botColor, hasText = false, size = 76.dp)

                        Spacer(Modifier.height(18.dp))

                        Surface(
                            shape           = RoundedCornerShape(18.dp),
                            color           = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    "Todavía no hay nada aquí.",
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = CuMoradoOscuro,
                                    textAlign  = TextAlign.Center,
                                    modifier   = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Este es tu cuaderno. Cada vez que hablamos, guardo la conversación acá para que puedas volver a leerla cuando quieras.",
                                    fontSize   = 13.sp,
                                    color      = Color(0xFF555566),
                                    lineHeight = 20.sp,
                                    textAlign  = TextAlign.Center
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Releer lo que escribiste hace semanas ayuda a notar cosas que en el momento no se ven. Ese es el sentido de tenerlo todo junto.",
                                    fontSize   = 12.sp,
                                    color      = CuGrisTxt,
                                    lineHeight = 18.sp,
                                    textAlign  = TextAlign.Center
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "Empieza tocando “Nueva conversación”.",
                            fontSize = 12.sp,
                            color    = CuMoradoMedio
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(conversaciones, key = { it.id }) { conv ->
                            TarjetaConversacion(
                                conv      = conv,
                                botColor  = botColor,
                                onAbrir   = { onAbrirConversacion(conv.id) },
                                onBorrar  = { aBorrar = conv }
                            )
                        }
                        item { Spacer(Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }

    // ── Confirmación de borrado ──────────────────────────────
    val objetivo = aBorrar
    if (objetivo != null) {
        AlertDialog(
            onDismissRequest = { aBorrar = null },
            containerColor   = Color.White,
            title = {
                Text("¿Borrar esta conversación?", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, color = CuMoradoOscuro)
            },
            text = {
                Text(
                    "Se eliminará “${objetivo.titulo}” y no podrás recuperarla.",
                    fontSize = 13.sp, color = Color(0xFF555566), lineHeight = 19.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    ConversacionesRepository.borrar(objetivo.id)
                    conversaciones = conversaciones.filter { it.id != objetivo.id }
                    aBorrar = null
                }) {
                    Text("Borrar", color = Color(0xFFCC3333), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { aBorrar = null }) {
                    Text("Cancelar", color = CuGrisTxt)
                }
            }
        )
    }
}

@Composable
private fun TarjetaConversacion(
    conv: Conversacion,
    botColor: Color,
    onAbrir: () -> Unit,
    onBorrar: () -> Unit
) {
    Surface(
        shape           = RoundedCornerShape(18.dp),
        color           = Color.White,
        shadowElevation = 3.dp,
        modifier        = Modifier
            .fillMaxWidth()
            .clickable { onAbrir() }
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier.size(38.dp).clip(CircleShape).background(botColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(14.dp).clip(CircleShape).background(botColor))
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    conv.titulo,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = CuMoradoOscuro,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    conv.vistaPrevia,
                    fontSize = 12.sp,
                    color    = Color(0xFF666677),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${ConversacionesRepository.fechaLegible(conv.ultimoMs)} · ${conv.cantidadMensajes} mensajes",
                    fontSize = 10.sp,
                    color    = CuGrisTxt
                )
            }

            IconButton(onClick = onBorrar) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Borrar conversación",
                    tint     = Color(0xFFC2C3DC),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
