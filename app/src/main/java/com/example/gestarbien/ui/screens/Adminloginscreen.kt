package com.example.gestarbien.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.data.AdminRepository
import com.example.gestarbien.data.RolAdmin
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private val Indigo900 = Color(0xFF26215C)
private val Indigo700 = Color(0xFF3C3489)
private val Indigo400 = Color(0xFF7F77DD)
private val Indigo200 = Color(0xFFAFA9EC)
private val Indigo100 = Color(0xFFCECBF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onBackClick: () -> Unit,
    onAdminLoginSuccess: (RolAdmin) -> Unit
) {
    val context = LocalContext.current
    val auth    = remember { FirebaseAuth.getInstance() }
    val repo    = remember { AdminRepository(context.applicationContext) }
    val scope   = rememberCoroutineScope()

    var correo    by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf("") }

    val correoValido  = correo.contains("@") && correo.contains(".")
    val puedeIngresar = correoValido && password.length >= 6 && !isLoading

    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor        = Indigo200,
        unfocusedBorderColor      = Indigo100,
        focusedLeadingIconColor   = Indigo200,
        unfocusedLeadingIconColor = Indigo200,
        cursorColor               = Indigo200,
        focusedTextColor          = Color(0xFF333333),
        unfocusedTextColor        = Color(0xFF333333)
    )

    val ingresar: () -> Unit = {
        isLoading = true
        errorMsg  = ""

        val correoClean = correo.trim().lowercase()

        auth.signInWithEmailAndPassword(correoClean, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val correoAutenticado = auth.currentUser?.email?.lowercase() ?: ""

                    // El rol se resuelve con la misma lógica que usa el resto del
                    // módulo admin (AdminRepository.obtenerRol): primero los correos
                    // fijos de ADMIN_TOTAL, luego la colección "investigadores". Así
                    // el login y el repositorio nunca quedan desincronizados.
                    scope.launch {
                        val rol = try { repo.obtenerRol(correoAutenticado) } catch (e: Exception) { null }
                        if (rol != null) {
                            isLoading = false
                            onAdminLoginSuccess(rol)
                        } else {
                            auth.signOut()
                            isLoading = false
                            errorMsg = "Este correo no tiene permisos de administrador."
                        }
                    }
                } else {
                    isLoading = false
                    val msg = task.exception?.message ?: ""
                    errorMsg = when {
                        msg.contains("no user record") || msg.contains("user-not-found") ->
                            "Correo o contraseña incorrectos."
                        msg.contains("password is invalid") || msg.contains("wrong-password") ->
                            "Correo o contraseña incorrectos."
                        msg.contains("badly formatted")   -> "El correo no tiene un formato válido."
                        msg.contains("network")            -> "Sin conexión a internet. Intenta de nuevo."
                        msg.contains("too-many-requests")  -> "Demasiados intentos. Espera un momento."
                        else                                -> "No se pudo iniciar sesión. Verifica tus datos."
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Indigo400)
                .padding(bottom = 40.dp)
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
                                tint               = Indigo700,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text       = "",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Indigo900,
                    lineHeight = 30.sp,
                    modifier   = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    text       = "Login admin",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Indigo700
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

                Spacer(modifier = Modifier.height(14.dp))

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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick  = { if (puedeIngresar) ingresar() },
                    enabled  = puedeIngresar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = Indigo200,
                        disabledContainerColor = Indigo200,
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
                            text       = "Ingresar",
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

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text      = "Solo cuentas preautorizadas pueden ingresar aquí.",
                    fontSize  = 11.sp,
                    color     = Color(0xFF999999),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}