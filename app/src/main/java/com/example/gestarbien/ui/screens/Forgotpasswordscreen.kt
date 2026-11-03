package com.example.gestarbien.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.gestarbien.R
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla "¿Olvidaste tu contraseña?" para usuarias.
 * Usa el reseteo nativo de Firebase Auth (gratis, plan Spark):
 * el correo con el link de recuperación lo envía y diseña Firebase,
 * pero esta pantalla de "antes" y "después" sí la controlamos nosotras
 * por completo — con un tono cálido, acorde al resto de la app.
 *
 * Nota de seguridad: por diseño, siempre mostramos el mismo mensaje de
 * éxito exista o no una cuenta con ese correo (así evitamos que alguien
 * use este formulario para averiguar qué correos están registrados).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }

    var correo      by remember { mutableStateOf("") }
    var isLoading   by remember { mutableStateOf(false) }
    var errorMsg    by remember { mutableStateOf("") }
    var correoEnviado by remember { mutableStateOf(false) }

    val correoValido = correo.contains("@") && correo.contains(".")
    val puedeEnviar  = correoValido && !isLoading

    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor        = Color(0xFFB1B4E1),
        unfocusedBorderColor      = Color(0xFFD0D3F0),
        focusedLeadingIconColor   = Color(0xFFB1B4E1),
        unfocusedLeadingIconColor = Color(0xFFB1B4E1),
        cursorColor               = Color(0xFFB1B4E1),
        focusedTextColor          = Color(0xFF333333),
        unfocusedTextColor        = Color(0xFF333333)
    )

    val enviarCorreo: () -> Unit = {
        isLoading = true
        errorMsg  = ""
        auth.sendPasswordResetEmail(correo.trim())
            .addOnCompleteListener { task ->
                isLoading = false
                val msg = task.exception?.message ?: ""
                when {
                    // Éxito real, o "no existe" — en ambos casos mostramos
                    // el mismo mensaje de éxito por seguridad.
                    task.isSuccessful || msg.contains("no user record")
                            || msg.contains("user-not-found") -> {
                        correoEnviado = true
                    }
                    msg.contains("badly formatted") ->
                        errorMsg = "Ese correo no tiene un formato válido."
                    msg.contains("network") ->
                        errorMsg = "Sin conexión a internet. Intenta de nuevo."
                    msg.contains("too-many-requests") ->
                        errorMsg = "Demasiados intentos. Espera un momento antes de volver a intentar."
                    else ->
                        errorMsg = "No pudimos enviar el correo. Intenta de nuevo en un momento."
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Sección superior morada ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(Color(0xFFB1B4E1))
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(36.dp))
                IconButton(
                    onClick  = onBackClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Surface(
                        shape    = RoundedCornerShape(50),
                        color    = Color.White,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint               = Color(0xFF3A3D8C),
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = "No te\npreocupes,\nte ayudamos",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF3A3D8C),
                        textAlign  = TextAlign.Center,
                        lineHeight = 30.sp,
                        modifier   = Modifier.weight(1f)
                    )
                    Image(
                        painter            = painterResource(id = R.drawable.icon2),
                        contentDescription = "Ilustración GestarBien",
                        modifier           = Modifier.size(140.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Tarjeta del formulario ───────────────────────────────────────
        Surface(
            shape           = RoundedCornerShape(24.dp),
            color           = Color.White,
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
                        text       = "Recuperar contraseña",
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF3A3D8C),
                        textAlign  = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text      = "Escribe el correo con el que te registraste y te enviaremos las instrucciones para volver a entrar.",
                        fontSize  = 13.sp,
                        color     = Color(0xFF888888),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value         = correo,
                        onValueChange = { correo = it; errorMsg = "" },
                        placeholder   = { Text("Correo electrónico", color = Color(0xFFAAAAAA)) },
                        leadingIcon   = {
                            Icon(
                                painter            = painterResource(id = android.R.drawable.ic_dialog_email),
                                contentDescription = null,
                                modifier           = Modifier.size(24.dp)
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
                            containerColor         = Color(0xFFB1B4E1),
                            disabledContainerColor = Color(0xFFB1B4E1),
                            contentColor           = Color.White,
                            disabledContentColor   = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation  = 0.dp,
                            pressedElevation  = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text       = "Enviar instrucciones",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
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
                    // ── Estado de éxito ───────────────────────────────────
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFEEEFFE),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector        = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint               = Color(0xFF5C5FA8),
                                modifier           = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text       = "Revisa tu correo",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF3A3D8C),
                        textAlign  = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Si tu correo está registrado, te enviamos un mensaje con los pasos para crear tu nueva contraseña. Puede tardar un par de minutos en llegar (revisa también la carpeta de spam)\n\nTómate tu tiempo. Aquí seguimos.",
                        fontSize   = 13.sp,
                        color      = Color(0xFF666666),
                        textAlign  = TextAlign.Center,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick  = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB1B4E1),
                            contentColor   = Color.White
                        )
                    ) {
                        Text(
                            text       = "Volver a iniciar sesión",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}