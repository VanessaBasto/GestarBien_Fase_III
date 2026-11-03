package com.example.gestarbien.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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

// ─────────────────────────────────────────────────────────────────────────────
//  MÓDULO PROFESIONALES — LOGIN
// ─────────────────────────────────────────────────────────────────────────────

private val AzulOscuro   = Color(0xFF1B3A6B)
private val AzulMedio    = Color(0xFF2D5F9E)
private val AzulSuave    = Color(0xFFADC8E8)
private val AzulFondo    = Color(0xFFD6E8F7)
private val BlancoPuro   = Color.White
private val GrisTexto    = Color(0xFF555555)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesionalLoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: (registroCompleto: Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit = {}
) {
    val auth = remember { FirebaseAuth.getInstance() }

    var correo   by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf("") }

    val correoValido = correo.contains("@") && correo.contains(".")
    val puedeLogin   = correoValido && password.length >= 6 && !isLoading

    val fieldShape  = RoundedCornerShape(14.dp)
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

    val repoProfesionales = remember { com.example.gestarbien.data.ProfesionalesRepository() }
    val scope = rememberCoroutineScope()

    val iniciarSesion: () -> Unit = {
        isLoading = true
        errorMsg  = ""
        auth.signInWithEmailAndPassword(correo.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    scope.launch {
                        val uid = auth.currentUser?.uid

                        // Nuevo: bloquear si esta cuenta ya es una usuaria (paciente)
                        if (uid != null && repoProfesionales.esUsuariaExistente(uid)) {
                            auth.signOut()
                            isLoading = false
                            errorMsg = "Esta cuenta ya está registrada como usuaria. Inicia sesión desde el portal de pacientes."
                            return@launch
                        }

                        val perfil = repoProfesionales.obtenerPerfilPropio()
                        isLoading = false
                        onLoginSuccess(perfil?.registroCompleto == true)
                    }
                } else {
                    isLoading = false
                    val msg = task.exception?.message ?: ""
                    errorMsg = when {
                        msg.contains("no user record") || msg.contains("user-not-found")      -> "No existe una cuenta con este correo."
                        msg.contains("password is invalid") || msg.contains("wrong-password") -> "Contraseña incorrecta."
                        msg.contains("badly formatted")                                       -> "El correo no tiene un formato válido."
                        msg.contains("network")                                               -> "Sin conexión a internet."
                        msg.contains("too-many-requests")                                     -> "Demasiados intentos. Espera un momento."
                        else                                                                  -> "Error al iniciar sesión. Verifica tus datos."
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlancoPuro)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Sección superior azul ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(AzulFondo)
                .padding(bottom = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(36.dp))

                // Flecha atrás
                IconButton(
                    onClick  = onBackClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Surface(
                        shape    = RoundedCornerShape(50),
                        color    = BlancoPuro,
                        modifier = Modifier.size(180.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint               = AzulOscuro,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Imagen a la izquierda + texto "Módulo Profesionales" centrado
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter            = painterResource(id = R.drawable.profesional),
                        contentDescription = "Profesional de salud",
                        modifier           = Modifier.size(140.dp)
                    )
                    Text(
                        text       = "Módulo\nProfesionales",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AzulMedio,
                        textAlign  = TextAlign.Center,
                        lineHeight = 30.sp,
                        modifier   = Modifier.weight(1f)
                    )
                }
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
                Text(
                    text       = "Login",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AzulOscuro
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text      = "Acceso exclusivo para profesionales de la salud",
                    fontSize  = 12.sp,
                    color     = GrisTexto,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Campo correo ─────────────────────────────────────────
                OutlinedTextField(
                    value         = correo,
                    onValueChange = { correo = it; errorMsg = "" },
                    placeholder   = { Text("Correo electrónico", color = Color(0xFFAAAAAA)) },
                    leadingIcon   = {
                        Icon(
                            painter            = painterResource(id = android.R.drawable.ic_dialog_info),
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
                            tint               = AzulSuave,
                            modifier           = Modifier.size(22.dp)
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

                Text(
                    text           = "¿Olvidaste tu contraseña?",
                    fontSize       = 13.sp,
                    color          = AzulMedio,
                    textDecoration = TextDecoration.Underline,
                    modifier       = Modifier
                        .align(Alignment.End)
                        .clickable { onForgotPasswordClick() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Botón Iniciar Sesión ──────────────────────────────────
                Button(
                    onClick  = { if (puedeLogin) iniciarSesion() },
                    enabled  = puedeLogin,
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
                            text       = "Iniciar Sesión",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = BlancoPuro
                        )
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