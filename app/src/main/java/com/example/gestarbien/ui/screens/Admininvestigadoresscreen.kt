package com.example.gestarbien.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gestarbien.data.AdminRepository
import com.example.gestarbien.data.InvestigadorResumen
import com.example.gestarbien.data.RolAdmin
import kotlinx.coroutines.launch

/**
 * Solo accesible para ADMIN_TOTAL. La pantalla en sí no valida el rol
 * porque MainActivity no debe enrutar aquí si el rol es INVESTIGADOR:
 * la fila "Investigadores" del dashboard ni siquiera se muestra para
 * ese rol. Aun así, el repositorio vuelve a validar por si acaso.
 */
@Composable
fun AdminInvestigadoresScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val repo    = remember { AdminRepository(context.applicationContext) }
    val scope   = rememberCoroutineScope()

    var investigadores    by remember { mutableStateOf<List<InvestigadorResumen>>(emptyList()) }
    var cargando          by remember { mutableStateOf(true) }
    var mostrarSheet      by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf<InvestigadorResumen?>(null) }
    var eliminando        by remember { mutableStateOf(false) }

    fun recargar() {
        scope.launch {
            cargando = true
            investigadores = try { repo.listarInvestigadores() } catch (e: Exception) { emptyList() }
            cargando = false
        }
    }
    LaunchedEffect(Unit) { recargar() }

    confirmarEliminar?.let { inv ->
        Dialog(onDismissRequest = { if (!eliminando) confirmarEliminar = null }) {
            Surface(shape = RoundedCornerShape(20.dp), color = AdmBlanco) {
                Column(Modifier.padding(22.dp)) {
                    Text("¿Quitar el acceso de ${inv.nombre}?", fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = AdmOscuro)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Ya no podrá entrar al módulo de investigación. Su cuenta de correo " +
                                "sigue existiendo, pero pierde el acceso a la app.",
                        fontSize = 12.sp, color = AdmGrisTxt, lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { confirmarEliminar = null }, enabled = !eliminando) {
                            Text("Cancelar", color = AdmGrisTxt)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                eliminando = true
                                scope.launch {
                                    try { repo.eliminarInvestigador(inv.docId, RolAdmin.ADMIN_TOTAL) } catch (_: Exception) {}
                                    eliminando = false
                                    confirmarEliminar = null
                                    recargar()
                                }
                            },
                            enabled = !eliminando,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AdmRojo)
                        ) {
                            if (eliminando) {
                                CircularProgressIndicator(color = AdmBlanco, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Quitar acceso", color = AdmBlanco, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().background(AdmBlanco)) {

        AdminHeader(
            titulo      = "Investigadores",
            subtitulo   = if (cargando) "" else "${investigadores.size} con acceso de solo lectura",
            onBackClick = onBackClick,
            accion = {
                Surface(shape = RoundedCornerShape(12.dp), color = AdmMedio, modifier = Modifier.size(40.dp)) {
                    IconButton(onClick = { mostrarSheet = true }) {
                        Icon(Icons.Default.Add, "Añadir investigador", tint = AdmBlanco, modifier = Modifier.size(22.dp))
                    }
                }
            }
        )

        Spacer(Modifier.height(18.dp))

        Surface(
            shape    = RoundedCornerShape(14.dp),
            color    = AdmFondoSuave,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Text(
                "Un investigador puede ver los contadores del panel, las asignaciones y la " +
                        "caracterización de barreras con su exportador. No puede crear, editar " +
                        "ni eliminar nada.",
                fontSize = 12.sp, color = AdmOscuro, lineHeight = 17.sp,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        when {
            cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AdmSuave)
            }
            investigadores.isEmpty() -> AdminVacio(
                "Todavía no hay investigadores con acceso.\nUsa el botón + para crear el primero."
            )
            else -> LazyColumn(
                contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(investigadores, key = { it.docId }) { inv ->
                    AdminTarjeta {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(inv.nombre, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                                    color = AdmOscuro, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(3.dp))
                                Text(inv.correo, fontSize = 11.sp, color = AdmGrisTxt,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(5.dp))
                                AdminEtiqueta("Solo lectura")
                            }
                            IconButton(onClick = { confirmarEliminar = inv }) {
                                Icon(Icons.Default.Delete, "Quitar acceso",
                                    tint = Color(0xFFCBC9DC), modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    if (mostrarSheet) {
        SheetInvestigador(
            onDismiss = { mostrarSheet = false },
            onCreado  = { mostrarSheet = false; recargar() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetInvestigador(onDismiss: () -> Unit, onCreado: () -> Unit) {
    val context = LocalContext.current
    val repo    = remember { AdminRepository(context.applicationContext) }
    val scope   = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var nombre      by remember { mutableStateOf("") }
    var correo      by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var verPassword by remember { mutableStateOf(false) }
    var guardando   by remember { mutableStateOf(false) }
    var error       by remember { mutableStateOf("") }
    var creado      by remember { mutableStateOf(false) }

    val correoValido   = correo.contains("@") && correo.contains(".") && correo.length > 5
    val passwordValida = password.length >= 6
    val formularioOk   = nombre.isNotBlank() && correoValido && passwordValida

    val camposColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AdmSuave, unfocusedBorderColor = AdmBordeSuave,
        cursorColor = AdmMedio, focusedContainerColor = AdmBlanco, unfocusedContainerColor = AdmBlanco
    )

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = AdmBlanco) {

        if (creado) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = AdmVerde, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(14.dp))
                Text("Investigador creado", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AdmOscuro)
                Spacer(Modifier.height(6.dp))
                Text(
                    "$nombre ya puede entrar con su correo y contraseña. Solo verá lo que corresponde a su rol de solo lectura.",
                    fontSize = 12.sp, color = AdmGrisTxt, textAlign = TextAlign.Center, lineHeight = 17.sp
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onCreado,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdmMedio)
                ) { Text("Listo", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
            return@ModalBottomSheet
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp)) {

            Text("Nuevo investigador", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AdmOscuro)
            Spacer(Modifier.height(4.dp))
            Text(
                "Tendrá acceso de solo lectura: dashboard, asignaciones y caracterización de barreras.",
                fontSize = 12.sp, color = AdmGrisTxt, lineHeight = 17.sp
            )

            Spacer(Modifier.height(18.dp))

            Text("Nombre completo", fontSize = 11.sp, color = AdmGrisTxt)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it; error = "" },
                placeholder = { Text("Ej. Laura Gómez", fontSize = 13.sp, color = Color(0xFFA9A8BC)) },
                singleLine = true, shape = RoundedCornerShape(14.dp), colors = camposColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))
            Text("Correo institucional", fontSize = 11.sp, color = AdmGrisTxt)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = correo, onValueChange = { correo = it.trim(); error = "" },
                placeholder = { Text("investigador@ucundinamarca.edu.co", fontSize = 13.sp, color = Color(0xFFA9A8BC)) },
                singleLine = true, isError = correo.isNotBlank() && !correoValido,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp), colors = camposColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))
            Text("Contraseña temporal", fontSize = 11.sp, color = AdmGrisTxt)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; error = "" },
                placeholder = { Text("Mínimo 6 caracteres", fontSize = 13.sp, color = Color(0xFFA9A8BC)) },
                singleLine = true, isError = password.isNotBlank() && !passwordValida,
                visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { verPassword = !verPassword }) {
                        Icon(
                            if (verPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = AdmGrisTxt, modifier = Modifier.size(18.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp), colors = camposColors,
                modifier = Modifier.fillMaxWidth()
            )

            if (error.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = AdmRojoFondo, modifier = Modifier.fillMaxWidth()) {
                    Text(error, fontSize = 12.sp, color = AdmRojo, lineHeight = 17.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    guardando = true; error = ""
                    scope.launch {
                        try {
                            repo.agregarInvestigador(nombre, correo, password, RolAdmin.ADMIN_TOTAL)
                            guardando = false; creado = true
                        } catch (e: Exception) {
                            guardando = false
                            val msg = e.message ?: ""
                            error = when {
                                msg.contains("already in use", true) -> "Ya existe una cuenta con ese correo."
                                msg.contains("badly formatted", true) -> "El correo no tiene un formato válido."
                                msg.contains("Password should be", true) -> "La contraseña es demasiado débil."
                                else -> "No se pudo crear la cuenta: $msg"
                            }
                        }
                    }
                },
                enabled = formularioOk && !guardando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdmMedio, disabledContainerColor = Color(0xFFDEDCF0),
                    contentColor = AdmBlanco, disabledContentColor = AdmBlanco
                )
            ) {
                if (guardando) {
                    CircularProgressIndicator(color = AdmBlanco, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text("Crear investigador", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}