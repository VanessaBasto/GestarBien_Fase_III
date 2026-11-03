package com.example.gestarbien.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

// ─── Modelo de contenido legal ─────────────────────────────────────────────
sealed class LegalBlock {
    data class Paragraph(val text: String) : LegalBlock()
    data class BulletList(val items: List<String>) : LegalBlock()
}

// ─── Contenido consentimiento ──────────────────────────────────────────────
private val BLOQUES_CONSENTIMIENTO: List<LegalBlock> = listOf(
    LegalBlock.Paragraph(
        "GestarBien es una herramienta de acompañamiento en salud mental durante el embarazo y el posparto. Al usarla, usted acepta:"
    ),
    LegalBlock.BulletList(
        listOf(
            "Proporcionar información personal y responder cuestionarios sobre su bienestar emocional.",
            "Interactuar con herramientas digitales como contenido psicoeducativo y un chatbot de apoyo.",
            "Aceptar que sus datos sean utilizados de forma confidencial y con fines de atención en salud, investigación y monitoreo en salud mental perinatal."
        )
    ),
    LegalBlock.Paragraph(
        "Esta aplicación NO reemplaza la atención médica o psicológica de un profesional. Si presenta malestar emocional, se recomienda acudir a un profesional o a su servicio de salud."
    )
)

// ─── Contenido tratamiento de datos ───────────────────────────────────────
private val BLOQUES_TRATAMIENTO: List<LegalBlock> = listOf(
    LegalBlock.Paragraph(
        "En cumplimiento de la Ley 1581 de 2012 y el Decreto 1377 de 2013, autorizo de manera libre, previa, expresa e informada el tratamiento de mis datos personales a través de la aplicación GestarBien."
    ),
    LegalBlock.Paragraph("Los datos recolectados serán utilizados para:"),
    LegalBlock.BulletList(
        listOf(
            "Brindar acompañamiento en salud mental perinatal.",
            "Realizar análisis estadísticos y científicos.",
            "Mejorar herramientas tecnológicas en salud (incluyendo inteligencia artificial)."
        )
    ),
    LegalBlock.Paragraph("Entiendo que:"),
    LegalBlock.BulletList(
        listOf(
            "Mis datos serán tratados de forma confidencial y segura.",
            "Serán anonimizados para fines de investigación.",
            "No serán compartidos con terceros sin mi autorización, salvo obligación legal."
        )
    ),
    LegalBlock.Paragraph("Como titular de la información, conozco mis derechos a:"),
    LegalBlock.BulletList(
        listOf(
            "Acceder, actualizar y rectificar mis datos.",
            "Solicitar la eliminación de la información cuando lo desee.",
            "Revocar esta autorización en cualquier momento."
        )
    )
)

// ─── Ítem de viñeta ────────────────────────────────────────────────────────
@Composable
fun BulletItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text       = "•",
            fontSize   = 13.sp,
            color      = Color(0xFF5C5FA8),
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(end = 8.dp, top = 1.dp)
        )
        Text(
            text       = text,
            fontSize   = 13.sp,
            color      = Color(0xFF444444),
            lineHeight = 20.sp,
            textAlign  = TextAlign.Justify,
            modifier   = Modifier.weight(1f)
        )
    }
}

// ─── Diálogo legal ─────────────────────────────────────────────────────────
@Composable
fun LegalDialog(
    title: String,
    bloques: List<LegalBlock>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape          = RoundedCornerShape(20.dp),
            color          = Color.White,
            tonalElevation = 4.dp,
            modifier       = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text       = title,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF3A3D8C)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    bloques.forEach { bloque ->
                        when (bloque) {
                            is LegalBlock.Paragraph -> {
                                Text(
                                    text       = bloque.text,
                                    fontSize   = 13.sp,
                                    color      = Color(0xFF444444),
                                    lineHeight = 20.sp,
                                    textAlign  = TextAlign.Justify,
                                    modifier   = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            is LegalBlock.BulletList -> {
                                bloque.items.forEach { item -> BulletItem(text = item) }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C5FA8))
                ) {
                    Text("Entendido", color = Color.White)
                }
            }
        }
    }
}

// ─── Checkbox con hipervínculo ─────────────────────────────────────────────
@Composable
fun CheckboxWithLink(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    normalText: String,
    linkText: String,
    onLinkClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = CheckboxDefaults.colors(checkedColor = Color(0xFF5C5FA8))
        )
        Spacer(modifier = Modifier.width(4.dp))
        val annotated = buildAnnotatedString {
            append(normalText)
            append(" ")
            withStyle(
                SpanStyle(
                    color          = Color(0xFF3A3D8C),
                    textDecoration = TextDecoration.Underline,
                    fontWeight     = FontWeight.SemiBold
                )
            ) { append(linkText) }
        }
        Text(
            text     = annotated,
            fontSize = 13.sp,
            color    = Color(0xFF555555),
            modifier = Modifier.clickable { onLinkClick() }
        )
    }
}

// ─── Campo de texto plano (solo línea inferior) ────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorText: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value                = value,
            onValueChange        = onValueChange,
            label                = { Text(label, fontSize = 14.sp) },
            singleLine           = true,
            isError              = isError,
            visualTransformation = if (isPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions      = KeyboardOptions(keyboardType = keyboardType),
            modifier             = Modifier.fillMaxWidth(),
            colors               = TextFieldDefaults.colors(
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor     = Color.Transparent,
                focusedIndicatorColor   = Color(0xFF5C5FA8),
                unfocusedIndicatorColor = Color(0xFFD8DAF0),
                errorIndicatorColor     = Color(0xFF5C5FA8),   // ← antes Color.Red
                focusedLabelColor       = Color(0xFF5C5FA8),
                unfocusedLabelColor     = Color(0xFF9999BB),
                errorLabelColor         = Color(0xFF3A3D8C),   // ← nombre del campo en error: morado oscuro
                cursorColor             = Color(0xFF5C5FA8),
                focusedTextColor        = Color(0xFF333333),
                unfocusedTextColor      = Color(0xFF333333)
            )
        )
        if (isError && errorText.isNotBlank()) {
            Text(
                text     = errorText,
                color    = Color(0xFF5C5FA8),               // ← antes Color.Red
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─── Pantalla de Registro ──────────────────────────────────────────────────
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    // ── Firebase (remember evita recrear la instancia en cada recomposición) ─
    val auth = remember { FirebaseAuth.getInstance() }

    // ── Estado del formulario ───────────────────────────────────────────────
    var nombre      by remember { mutableStateOf("") }
    var correo      by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var acepConsentim  by remember { mutableStateOf(false) }
    var acepTratamien  by remember { mutableStateOf(false) }

    // ── Diálogos legales ────────────────────────────────────────────────────
    var showConsentDialog   by remember { mutableStateOf(false) }
    var showTratamienDialog by remember { mutableStateOf(false) }

    // ── Estado de carga y errores ───────────────────────────────────────────
    var isLoading            by remember { mutableStateOf(false) }
    var errorFirebase        by remember { mutableStateOf("") }
    var mostrarErrorTerminos by remember { mutableStateOf(false) }
    // Mensaje que explica qué falta al presionar "Crear cuenta"
    var errorValidacion      by remember { mutableStateOf("") }

    // ── Validaciones en tiempo real ─────────────────────────────────────────
    val correoValido = android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()

    val passSegura = password.length >= 8 &&
            password.any { it.isUpperCase() } &&
            password.any { it.isDigit() }

    val passCorta      = password.isNotBlank() && !passSegura
    val passNoCoincide = confirmPass.isNotBlank() && password != confirmPass

    val canRegister = nombre.isNotBlank()
            && correoValido
            && passSegura
            && password == confirmPass
            && acepConsentim
            && acepTratamien

    // ── Lambda de registro Firebase ─────────────────────────────────────────
    // Usamos val lambda (no fun local) para que Kotlin pueda capturar
    // correctamente las variables de estado del Composable sin errores
    // de "Unresolved reference".
    val registrar: () -> Unit = {
        isLoading     = true
        errorFirebase = ""
        auth.createUserWithEmailAndPassword(correo.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Guardar nombre en el perfil de Firebase Auth
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre.trim())
                        .build()
                    auth.currentUser?.updateProfile(profileUpdates)

                    // Crear el documento usuarios/{uid} de inmediato. Sin esto,
                    // esUsuariaExistente() (usado por el login del portal de
                    // profesionales para bloquear cuentas de pacientes) no
                    // encuentra nada hasta que se complete el onboarding, y
                    // una usuaria recién registrada puede colarse al portal
                    // de profesionales con la misma cuenta.
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("usuarios").document(uid)
                            .set(mapOf("nombreCompleto" to nombre.trim()), com.google.firebase.firestore.SetOptions.merge())
                            .addOnCompleteListener {
                                isLoading = false
                                onRegisterSuccess()
                            }
                    } else {
                        isLoading = false
                        onRegisterSuccess()
                    }
                } else {
                    isLoading = false
                    val msg = task.exception?.message ?: ""
                    errorFirebase = when {
                        msg.contains("email address is already in use") ->
                            "Este correo ya está registrado."
                        msg.contains("badly formatted") ->
                            "El correo no tiene un formato válido."
                        msg.contains("network") ->
                            "Sin conexión a internet. Intenta de nuevo."
                        else ->
                            "Error al registrar. Intenta de nuevo."
                    }
                }
            }
    }

    // ── Diálogos legales ────────────────────────────────────────────────────
    if (showConsentDialog) {
        LegalDialog(
            title     = "Consentimiento informado",
            bloques   = BLOQUES_CONSENTIMIENTO,
            onDismiss = { showConsentDialog = false }
        )
    }
    if (showTratamienDialog) {
        LegalDialog(
            title     = "Tratamiento de datos personales",
            bloques   = BLOQUES_TRATAMIENTO,
            onDismiss = { showTratamienDialog = false }
        )
    }

    // ── UI ──────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo superior morado con borde curvo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(Color(0xFFB1B4E1))
        )

        // Botón atrás
        IconButton(
            onClick  = onBackClick,
            modifier = Modifier
                .padding(start = 12.dp, top = 36.dp)
                .align(Alignment.TopStart)
                .zIndex(1f)
        ) {
            Icon(
                imageVector        = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint               = Color.White
            )
        }

        // Contenido principal con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // ── Tarjeta principal ───────────────────────────────────────────
            Surface(
                shape           = RoundedCornerShape(24.dp),
                color           = Color.White,
                tonalElevation  = 4.dp,
                shadowElevation = 8.dp,
                modifier        = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text       = "Registro",
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF3A3D8C)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Nombre
                    PlainTextField(
                        value         = nombre,
                        onValueChange = { nombre = it; errorValidacion = "" },
                        label         = "Nombre:"
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Correo — valida formato en tiempo real
                    PlainTextField(
                        value         = correo,
                        onValueChange = { correo = it; errorFirebase = ""; errorValidacion = "" },
                        label         = "Correo:",
                        keyboardType  = KeyboardType.Email,
                        isError       = correo.isNotBlank() && !correoValido,
                        errorText     = "⚠ Ingresa un correo válido"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Contraseña — mínimo 8 caracteres, una mayúscula y un número
                    PlainTextField(
                        value         = password,
                        onValueChange = { password = it; errorValidacion = "" },
                        label         = "Contraseña:",
                        isPassword    = true,
                        keyboardType  = KeyboardType.Password,
                        isError       = passCorta,
                        errorText     = "⚠ Mínimo 8 caracteres, una mayúscula y un número"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Confirmar contraseña
                    PlainTextField(
                        value         = confirmPass,
                        onValueChange = { confirmPass = it; errorValidacion = "" },
                        label         = "Confirmar contraseña:",
                        isPassword    = true,
                        keyboardType  = KeyboardType.Password,
                        isError       = passNoCoincide,
                        errorText     = "⚠ Las contraseñas no coinciden"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Checkboxes legales ──────────────────────────────────
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = if (mostrarErrorTerminos) Color(0xFFFFF0F0)
                        else Color(0xFFEEEFFE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            CheckboxWithLink(
                                checked         = acepConsentim,
                                onCheckedChange = {
                                    acepConsentim = it
                                    if (it) {
                                        mostrarErrorTerminos = false
                                        errorValidacion = ""
                                    }
                                },
                                normalText  = "Acepto el",
                                linkText    = "consentimiento informado",
                                onLinkClick = { showConsentDialog = true }
                            )
                            CheckboxWithLink(
                                checked         = acepTratamien,
                                onCheckedChange = {
                                    acepTratamien = it
                                    if (it) {
                                        mostrarErrorTerminos = false
                                        errorValidacion = ""
                                    }
                                },
                                normalText  = "Autorizo el",
                                linkText    = "tratamiento de datos personales",
                                onLinkClick = { showTratamienDialog = true }
                            )

                            // ⚠ Aviso si intenta registrarse sin aceptar términos
                            if (mostrarErrorTerminos) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text     = "⚠ Debes aceptar ambos términos para usar la app.",
                                    color    = Color(0xFFCC3333),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Botón Crear cuenta ──────────────────────────────────
                    Button(
                        onClick = {
                            // Revisa campo por campo y muestra el primer problema
                            errorValidacion = when {
                                nombre.isBlank()        -> "Ingresa tu nombre."
                                correo.isBlank()        -> "Ingresa tu correo."
                                !correoValido           -> "El correo no tiene un formato válido."
                                password.isBlank()      -> "Ingresa una contraseña."
                                !passSegura             -> "La contraseña debe tener mínimo 8 caracteres, una mayúscula y un número."
                                confirmPass.isBlank()   -> "Confirma tu contraseña."
                                password != confirmPass -> "Las contraseñas no coinciden."
                                else -> ""
                            }

                            // Resalta el bloque de checkboxes si faltan los términos
                            mostrarErrorTerminos = !acepConsentim || !acepTratamien

                            if (errorValidacion.isBlank() && canRegister) {
                                registrar()
                            }
                        },
                        enabled  = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = Color(0xFFB1B4E1),
                            disabledContainerColor = Color(0xFFD8DAF0)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text("Crear cuenta", fontSize = 16.sp, color = Color.White)
                        }
                    }

                    // Aviso de validación: dice exactamente qué falta
                    if (errorValidacion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF0F0)
                        ) {
                            Text(
                                text     = "⚠ $errorValidacion",
                                color    = Color(0xFFCC3333),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Error de Firebase (correo duplicado, red, etc.)
                    if (errorFirebase.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF0F0)
                        ) {
                            Text(
                                text     = "⚠ $errorFirebase",
                                color    = Color(0xFFCC3333),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Botón Volver ────────────────────────────────────────
                    OutlinedButton(
                        onClick  = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF5C5FA8)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFFB1B4E1))
                    ) {
                        Text("Volver", fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}