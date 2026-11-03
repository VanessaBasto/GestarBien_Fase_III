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
import androidx.compose.material.icons.filled.Lock
import com.example.gestarbien.data.ProfesionalesRepository
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onProfesionalClick: () -> Unit,
    successMessage: String = ""
) {
    val auth = remember { FirebaseAuth.getInstance() }
    var mostrarBanner by remember { mutableStateOf(successMessage.isNotBlank()) }

    var correo    by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf("") }

    val correoValido = correo.contains("@") && correo.contains(".")
    val puedeLogin   = correoValido && password.length >= 6 && !isLoading

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

    val repoProfesionales = remember { ProfesionalesRepository() }
    val scope = rememberCoroutineScope()

    val iniciarSesion: () -> Unit = {
        isLoading = true
        errorMsg  = ""
        auth.signInWithEmailAndPassword(correo.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    scope.launch {
                        val uid = auth.currentUser?.uid

                        // Bloquear si esta cuenta ya es un profesional
                        if (uid != null && repoProfesionales.esProfesionalExistente(uid)) {
                            auth.signOut()
                            isLoading = false
                            errorMsg = "Esta cuenta ya está registrada como profesional. Inicia sesión desde el portal de profesionales."
                            return@launch
                        }

                        isLoading = false
                        onLoginSuccess()
                    }
                } else {
                    isLoading = false
                    val msg = task.exception?.message ?: ""
                    errorMsg = when {
                        msg.contains("no user record")
                                || msg.contains("user-not-found")  -> "No existe una cuenta con este correo."
                        msg.contains("password is invalid")
                                || msg.contains("wrong-password")  -> "Contraseña incorrecta."
                        msg.contains("badly formatted")            -> "El correo no tiene un formato válido."
                        msg.contains("network")                    -> "Sin conexión a internet. Intenta de nuevo."
                        msg.contains("too-many-requests")          -> "Demasiados intentos. Espera un momento."
                        else                                       -> "Error al iniciar sesión. Verifica tus datos."
                    }
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
                        text       = "¡Nos alegra\ntenerte con\nnosotros!",
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
                Text(
                    text       = "Login",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF3A3D8C)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Campo correo ─────────────────────────────────────────
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

                Spacer(modifier = Modifier.height(14.dp))

                // ── Campo contraseña ─────────────────────────────────────
                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; errorMsg = "" },
                    placeholder          = { Text("Contraseña", color = Color(0xFFAAAAAA)) },
                    leadingIcon          = {
                        Icon(
                            imageVector        = Icons.Default.Lock,
                            contentDescription = null,
                            modifier           = Modifier.size(24.dp)
                        )
                    },
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier             = Modifier.fillMaxWidth(),
                    shape                = fieldShape,
                    colors               = fieldColors
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ── ¿Olvidaste contraseña? (derecha) ────────────────────
                Text(
                    text           = "¿Olvidaste tu contraseña?",
                    fontSize       = 13.sp,
                    color          = Color(0xFF5C5FA8),
                    textDecoration = TextDecoration.Underline,
                    modifier       = Modifier
                        .align(Alignment.End)
                        .clickable { onForgotPasswordClick() }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // ── Soy profesional (derecha, debajo) ────────────────────
                Text(
                    text           = "Soy profesional",
                    fontSize       = 13.sp,
                    color          = Color(0xFF5C5FA8),
                    textDecoration = TextDecoration.Underline,
                    fontWeight     = FontWeight.Normal,
                    modifier       = Modifier
                        .align(Alignment.End)
                        .clickable { onProfesionalClick() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick  = { if (puedeLogin) iniciarSesion() },
                    enabled  = puedeLogin,
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
                            text       = "Iniciar sesión",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }

                // ── Banner registro exitoso ──────────────────────────────
                if (mostrarBanner) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = Color(0xFFEEEFFE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text       = successMessage,
                                color      = Color(0xFF3A3D8C),
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 19.sp,
                                textAlign  = TextAlign.Center,
                                modifier   = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ── Error Firebase ────────────────────────────────────────
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
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}