package com.example.gestarbien.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
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
import com.google.firebase.auth.FirebaseAuth

// Mismos colores que ProfesionalLoginScreen, para que quede a juego.
private val AzulOscuro = Color(0xFF1B3A6B)
private val AzulMedio  = Color(0xFF2D5F9E)
private val AzulSuave  = Color(0xFFADC8E8)
private val AzulFondo  = Color(0xFFD6E8F7)
private val BlancoPuro = Color.White
private val GrisTexto  = Color(0xFF555555)

/**
 * Versión simple para el módulo de profesionales: solo pide el correo
 * y confirma que se envió el mensaje con las indicaciones. Mismo
 * mecanismo gratis (Firebase Auth nativo) que la pantalla de usuarias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordProfesionalScreen(
    onBackClick: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }

    var correo        by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(false) }
    var errorMsg       by remember { mutableStateOf("") }
    var correoEnviado  by remember { mutableStateOf(false) }

    val correoValido = correo.contains("@") && correo.contains(".")
    val puedeEnviar  = correoValido && !isLoading

    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor        = AzulMedio,
        unfocusedBorderColor      = AzulSuave,
        focusedLeadingIconColor   = AzulMedio,
        unfocusedLeadingIconColor = AzulSuave,
        cursorColor               = AzulMedio,
        focusedTextColor          = AzulOscuro,
        unfocusedTextColor        = GrisTexto,
        focusedContainerColor     = BlancoPuro,
        unfocusedContainerColor   = BlancoPuro
    )

    val enviarCorreo: () -> Unit = {
        isLoading = true
        errorMsg  = ""
        auth.sendPasswordResetEmail(correo.trim())
            .addOnCompleteListener { task ->
                isLoading = false
                val msg = task.exception?.message ?: ""
                when {
                    task.isSuccessful || msg.contains("no user record")
                            || msg.contains("user-not-found") -> {
                        correoEnviado = true
                    }
                    msg.contains("badly formatted") ->
                        errorMsg = "Ese correo no tiene un formato válido."
                    msg.contains("network") ->
                        errorMsg = "Sin conexión a internet."
                    msg.contains("too-many-requests") ->
                        errorMsg = "Demasiados intentos. Espera un momento."
                    else ->
                        errorMsg = "No pudimos enviar el correo. Intenta de nuevo."
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlancoPuro)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Encabezado azul simple ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(AzulFondo)
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(36.dp))
                IconButton(
                    onClick  = onBackClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Surface(
                        shape    = RoundedCornerShape(50),
                        color    = BlancoPuro,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint               = AzulOscuro,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text       = "Recuperar acceso",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AzulOscuro,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Tarjeta formulario ───────────────────────────────────────────
        Surface(
            shape           = RoundedCornerShape(24.dp),
            color           = BlancoPuro,
            tonalElevation  = 4.dp,
            shadowElevation = 10.dp,
            modifier        = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier            = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!correoEnviado) {
                    Text(
                        text      = "Escribe tu correo profesional y te enviaremos las indicaciones para restablecer tu contraseña.",
                        fontSize  = 13.sp,
                        color     = GrisTexto,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value         = correo,
                        onValueChange = { correo = it; errorMsg = "" },
                        placeholder   = { Text("Correo electrónico", color = Color(0xFFAAAAAA)) },
                        leadingIcon   = {
                            Icon(
                                painter            = painterResource(id = android.R.drawable.ic_dialog_email),
                                contentDescription = null,
                                tint               = AzulSuave,
                                modifier           = Modifier.size(22.dp)
                            )
                        },
                        isError         = correo.isNotBlank() && !correoValido,
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier        = Modifier.fillMaxWidth(),
                        shape           = fieldShape,
                        colors          = fieldColors
                    )
                    if (correo.isNotBlank() && !correoValido) {
                        Text(
                            text     = "Ingresa un correo válido",
                            color    = Color.Red,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick  = { if (puedeEnviar) enviarCorreo() },
                        enabled  = puedeEnviar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = AzulSuave,
                            disabledContainerColor = AzulSuave,
                            contentColor           = BlancoPuro,
                            disabledContentColor   = BlancoPuro
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation  = 0.dp,
                            pressedElevation  = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = BlancoPuro,
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text       = "Enviar correo",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = BlancoPuro
                            )
                        }
                    }

                    if (errorMsg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF0F0)
                        ) {
                            Text(
                                text     = "⚠ $errorMsg",
                                color    = Color(0xFFCC3333),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AzulFondo,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector        = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint               = AzulMedio,
                                modifier           = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text       = "Listo, te enviamos un correo con las indicaciones.",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = AzulOscuro,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text       = "Revisa tu bandeja de entrada (y spam, por si acaso).",
                        fontSize   = 12.sp,
                        color      = GrisTexto,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick  = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulSuave,
                            contentColor   = BlancoPuro
                        )
                    ) {
                        Text("Volver a iniciar sesión", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}