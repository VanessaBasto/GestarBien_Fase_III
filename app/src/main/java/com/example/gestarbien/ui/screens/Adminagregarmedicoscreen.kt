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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R
import com.example.gestarbien.data.AdminRepository
import com.example.gestarbien.data.RolAdmin
import kotlinx.coroutines.launch

private val PROFESIONES = listOf(
    "Psicología",
    "Psiquiatría",
    "Medicina general",
    "Ginecología y obstetricia",
    "Enfermería",
    "Trabajo social"
)

@Composable
fun AdminAgregarMedicoScreen(
    rol: RolAdmin,
    onBackClick: () -> Unit,
    onMedicoCreado: () -> Unit
) {
    val context = LocalContext.current
    val repo    = remember { AdminRepository(context.applicationContext) }
    val scope   = rememberCoroutineScope()

    var nombre      by remember { mutableStateOf("") }
    var profesion   by remember { mutableStateOf(PROFESIONES.first()) }
    var menuAbierto by remember { mutableStateOf(false) }
    var whatsapp    by remember { mutableStateOf("") }
    var correo      by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var verPassword by remember { mutableStateOf(false) }

    var guardando by remember { mutableStateOf(false) }
    var error     by remember { mutableStateOf("") }
    var creado    by remember { mutableStateOf(false) }

    val correoValido   = correo.contains("@") && correo.contains(".") && correo.length > 5
    val passwordValida = password.length >= 6
    val whatsappValido = whatsapp.isBlank() || whatsapp.filter { it.isDigit() }.length >= 10
    val formularioOk   = nombre.isNotBlank() && correoValido && passwordValida && whatsappValido

    val camposColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor      = AdmSuave,
        unfocusedBorderColor    = AdmBordeSuave,
        cursorColor             = AdmMedio,
        focusedContainerColor   = AdmBlanco,
        unfocusedContainerColor = AdmBlanco
    )

    // ── Confirmación ─────────────────────────────────────────
    if (creado) {
        Column(
            modifier            = Modifier.fillMaxSize().background(AdmBlanco).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(96.dp).clip(CircleShape).background(AdmVerdeFondo),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(id = R.drawable.animo_feliz),
                    contentDescription = null,
                    modifier           = Modifier.size(64.dp),
                    contentScale       = ContentScale.Fit
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("Profesional registrado", fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = AdmOscuro)
            Spacer(Modifier.height(8.dp))
            Text(
                "$nombre ya puede ingresar al panel clínico con el correo y la contraseña que acabas de crear.",
                fontSize   = 13.sp,
                color      = AdmGrisTxt,
                textAlign  = TextAlign.Center,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick  = onMedicoCreado,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AdmMedio)
            ) { Text("Volver al panel", fontSize = 15.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(10.dp))
            TextButton(onClick = {
                nombre = ""; whatsapp = ""; correo = ""; password = ""
                profesion = PROFESIONES.first(); creado = false; error = ""
            }) {
                Text("Registrar otro profesional", color = AdmMedio, fontSize = 13.sp)
            }
        }
        return
    }

    // ── Formulario ───────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdmBlanco)
            .verticalScroll(rememberScrollState())
    ) {

        AdminHeader(
            titulo      = "Añadir profesional",
            subtitulo   = "Se creará su cuenta de acceso",
            onBackClick = onBackClick
        )

        Spacer(Modifier.height(18.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            AdminTarjeta {
                Text("Datos del profesional", fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = AdmOscuro)

                Spacer(Modifier.height(14.dp))

                Text("Nombre completo", fontSize = 11.sp, color = AdmGrisTxt)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it; error = "" },
                    placeholder   = { Text("Ana María Rodríguez", fontSize = 13.sp, color = Color(0xFFA9A8BC)) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    colors        = camposColors,
                    modifier      = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                Text("Profesión", fontSize = 11.sp, color = AdmGrisTxt)
                Spacer(Modifier.height(6.dp))
                Box {
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = AdmBlanco,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AdmBordeSuave, RoundedCornerShape(14.dp))
                            .clickable { menuAbierto = true }
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(profesion, fontSize = 14.sp, color = AdmOscuro)
                            Text("▾", fontSize = 14.sp, color = AdmGrisTxt)
                        }
                    }
                    DropdownMenu(
                        expanded         = menuAbierto,
                        onDismissRequest = { menuAbierto = false },
                        modifier         = Modifier.background(AdmBlanco)
                    ) {
                        PROFESIONES.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        p, fontSize = 14.sp,
                                        color      = if (p == profesion) AdmMedio else AdmGrisTxt,
                                        fontWeight = if (p == profesion) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = { profesion = p; menuAbierto = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text("WhatsApp (opcional)", fontSize = 11.sp, color = AdmGrisTxt)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value           = whatsapp,
                    onValueChange   = { whatsapp = it.filter { c -> c.isDigit() || c == '+' } },
                    placeholder     = { Text("3001234567", fontSize = 13.sp, color = Color(0xFFA9A8BC)) },
                    singleLine      = true,
                    isError         = !whatsappValido,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape           = RoundedCornerShape(14.dp),
                    colors          = camposColors,
                    modifier        = Modifier.fillMaxWidth()
                )
                if (!whatsappValido) {
                    Text("Debe tener al menos 10 dígitos", fontSize = 10.sp,
                        color = AdmRojo, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            AdminTarjeta {
                Text("Credenciales de acceso", fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = AdmOscuro)
                Spacer(Modifier.height(3.dp))
                Text(
                    "Con estos datos ingresará al panel clínico. Compárteselos por un medio seguro.",
                    fontSize = 11.sp, color = AdmGrisTxt, lineHeight = 15.sp
                )

                Spacer(Modifier.height(14.dp))

                Text("Correo electrónico", fontSize = 11.sp, color = AdmGrisTxt)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value           = correo,
                    onValueChange   = { correo = it.trim(); error = "" },
                    placeholder     = { Text("medico@ucundinamarca.edu.co", fontSize = 13.sp, color = Color(0xFFA9A8BC)) },
                    singleLine      = true,
                    isError         = correo.isNotBlank() && !correoValido,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape           = RoundedCornerShape(14.dp),
                    colors          = camposColors,
                    modifier        = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                Text("Contraseña temporal", fontSize = 11.sp, color = AdmGrisTxt)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; error = "" },
                    placeholder          = { Text("Mínimo 6 caracteres", fontSize = 13.sp, color = Color(0xFFA9A8BC)) },
                    singleLine           = true,
                    isError              = password.isNotBlank() && !passwordValida,
                    visualTransformation = if (verPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verPassword = !verPassword }) {
                            Icon(
                                if (verPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (verPassword) "Ocultar" else "Mostrar",
                                tint     = AdmGrisTxt,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    shape    = RoundedCornerShape(14.dp),
                    colors   = camposColors,
                    modifier = Modifier.fillMaxWidth()
                )
                if (password.isNotBlank() && !passwordValida) {
                    Text("La contraseña debe tener al menos 6 caracteres", fontSize = 10.sp,
                        color = AdmRojo, modifier = Modifier.padding(top = 4.dp))
                }
            }

            if (error.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape    = RoundedCornerShape(14.dp),
                    color    = AdmRojoFondo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        error,
                        fontSize   = 12.sp,
                        color      = AdmRojo,
                        lineHeight = 17.sp,
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    guardando = true
                    error     = ""
                    scope.launch {
                        try {
                            repo.agregarMedico(
                                nombre    = nombre,
                                profesion = profesion,
                                whatsapp  = whatsapp,
                                correo    = correo,
                                password  = password,
                                rol       = rol
                            )
                            guardando = false
                            creado    = true
                        } catch (e: Exception) {
                            guardando = false
                            val msg = e.message ?: ""
                            error = when {
                                msg.contains("already in use", true) ->
                                    "Ya existe una cuenta con ese correo."
                                msg.contains("badly formatted", true) ->
                                    "El correo no tiene un formato válido."
                                msg.contains("Password should be", true) ->
                                    "La contraseña es demasiado débil."
                                msg.contains("network", true) ->
                                    "No hay conexión. Revisa tu internet e intenta de nuevo."
                                else -> "No se pudo crear la cuenta: $msg"
                            }
                        }
                    }
                },
                enabled  = formularioOk && !guardando,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = AdmMedio,
                    disabledContainerColor = Color(0xFFDEDCF0),
                    contentColor           = AdmBlanco,
                    disabledContentColor   = AdmBlanco
                )
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        color = AdmBlanco, strokeWidth = 2.dp, modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Crear cuenta del profesional", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (!formularioOk && !guardando) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Completa el nombre, el correo y una contraseña de al menos 6 caracteres.",
                    fontSize  = 11.sp,
                    color     = AdmGrisTxt,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}