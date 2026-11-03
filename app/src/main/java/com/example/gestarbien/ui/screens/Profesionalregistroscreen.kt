package com.example.gestarbien.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R
import com.example.gestarbien.data.ProfesionalesRepository
import kotlinx.coroutines.launch

// Misma paleta azul del resto del módulo de profesionales
private val AzulOscuro = Color(0xFF1B3A6B)
private val AzulMedio  = Color(0xFF2D5F9E)
private val AzulSuave  = Color(0xFFADC8E8)
private val AzulFondo  = Color(0xFFD6E8F7)
private val AzulClaro  = Color(0xFFEEF4FF)
private val BlancoPuro = Color.White
private val GrisTexto  = Color(0xFF555555)

private val PROFESIONES = listOf(
    "Psicólogo(a)",
    "Psiquiatra",
    "Trabajador(a) Social",
    "Enfermero(a) especializado(a)",
    "Otro profesional de salud mental"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalRegistroScreen(
    onRegistroCompleto: () -> Unit
) {
    val repo = remember { ProfesionalesRepository() }
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var profesionSel by remember { mutableStateOf(PROFESIONES.first()) }
    var menuAbierto by remember { mutableStateOf(false) }
    var whatsapp by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val whatsappValido = whatsapp.filter { it.isDigit() }.length in 10..13
    val puedeGuardar = nombre.trim().length >= 3 && whatsappValido && !guardando

    val fieldShape = RoundedCornerShape(12.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        cursorColor = AzulMedio,
        focusedTextColor = AzulOscuro,
        unfocusedTextColor = GrisTexto,
        focusedContainerColor = BlancoPuro,
        unfocusedContainerColor = BlancoPuro
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Mitad superior azul (mismo patrón que OnboardingCuestionarioIntroScreen) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.32f)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(AzulFondo)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.BottomCenter)
                .background(BlancoPuro)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BlancoPuro,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AzulClaro),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.profesional),
                            contentDescription = null,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "MÓDULO PROFESIONALES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9AAFCB),
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Datos\nprofesionales",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulOscuro,
                        textAlign = TextAlign.Center,
                        lineHeight = 33.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Estos datos aparecerán en tu panel y son los que verán tus usuarias asignadas para poder contactarte cuando lo necesiten.",
                        fontSize = 13.sp,
                        color = GrisTexto,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(22.dp))
                    HorizontalDivider(color = Color(0xFFE9F0FA), thickness = 1.dp)
                    Spacer(Modifier.height(22.dp))

                    // ── Nombre ─────────────────────────────────────────
                    Text("Nombre completo", fontSize = 12.sp, color = Color(0xFF6B7A99), fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it; error = "" },
                        placeholder = { Text("Ej: María Fernanda Ríos", color = Color(0xFFB0B0C8)) },
                        singleLine = true,
                        shape = fieldShape,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color(0xFFC4D4E8), fieldShape)
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Profesión ──────────────────────────────────────
                    Text("Profesión", fontSize = 12.sp, color = Color(0xFF6B7A99), fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = fieldShape,
                            color = BlancoPuro,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color(0xFFC4D4E8), fieldShape)
                                .clickable { menuAbierto = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 15.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(profesionSel, fontSize = 14.sp, color = AzulOscuro)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = GrisTexto)
                            }
                        }
                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false },
                            modifier = Modifier.background(BlancoPuro)
                        ) {
                            PROFESIONES.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p, color = if (p == profesionSel) AzulOscuro else GrisTexto) },
                                    onClick = { profesionSel = p; menuAbierto = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── WhatsApp ───────────────────────────────────────
                    Text("WhatsApp de contacto", fontSize = 12.sp, color = Color(0xFF6B7A99), fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Con indicativo de país, sin espacios. Ej: 573001234567",
                        fontSize = 11.sp,
                        color = Color(0xFFAAAAAA),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it.filter { c -> c.isDigit() }; error = "" },
                        placeholder = { Text("573001234567", color = Color(0xFFB0B0C8)) },
                        singleLine = true,
                        isError = whatsapp.isNotBlank() && !whatsappValido,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = fieldShape,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color(0xFFC4D4E8), fieldShape)
                    )
                    if (whatsapp.isNotBlank() && !whatsappValido) {
                        Text(
                            "Verifica el número, con indicativo de país",
                            color = Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (!puedeGuardar) return@Button
                            guardando = true
                            scope.launch {
                                try {
                                    repo.registrarPerfil(nombre.trim(), profesionSel, whatsapp.trim())
                                    onRegistroCompleto()
                                } catch (e: Exception) {
                                    error = "No se pudo guardar: ${e.message}"
                                } finally {
                                    guardando = false
                                }
                            }
                        },
                        enabled = puedeGuardar,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulSuave,
                            disabledContainerColor = AzulSuave.copy(alpha = 0.4f),
                            contentColor = BlancoPuro,
                            disabledContentColor = BlancoPuro
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(color = BlancoPuro, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        } else {
                            Text("Guardar y continuar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (error.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(error, color = Color.Red, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}