package com.example.gestarbien.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gestarbien.data.AdminRepository
import com.example.gestarbien.data.RolAdmin
import com.example.gestarbien.data.UsuariaResumen
import kotlinx.coroutines.launch

@Composable
fun AdminUsuariasScreen(rol: RolAdmin, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val repo    = remember { AdminRepository(context.applicationContext) }
    val scope   = rememberCoroutineScope()

    var usuarias          by remember { mutableStateOf<List<UsuariaResumen>>(emptyList()) }
    var cargando          by remember { mutableStateOf(true) }
    var busqueda          by remember { mutableStateOf("") }
    var confirmarEliminar by remember { mutableStateOf<UsuariaResumen?>(null) }
    var eliminando        by remember { mutableStateOf(false) }

    fun recargar() {
        scope.launch {
            cargando = true
            usuarias = try { repo.listarUsuarias() } catch (e: Exception) { emptyList() }
            cargando = false
        }
    }
    LaunchedEffect(Unit) { recargar() }

    val filtradas = remember(usuarias, busqueda) {
        if (busqueda.isBlank()) usuarias
        else usuarias.filter {
            it.nombre.contains(busqueda, true) || it.correo.contains(busqueda, true)
        }
    }

    // ── Confirmación de borrado ──────────────────────────────
    confirmarEliminar?.let { usuaria ->
        Dialog(onDismissRequest = { if (!eliminando) confirmarEliminar = null }) {
            Surface(shape = RoundedCornerShape(20.dp), color = AdmBlanco) {
                Column(Modifier.padding(22.dp)) {
                    Text("¿Eliminar a ${usuaria.nombre}?", fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = AdmOscuro)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Se borrarán sus datos, registros y cuestionarios. Esta acción no se " +
                                "puede deshacer y tampoco elimina su cuenta de acceso.",
                        fontSize   = 12.sp,
                        color      = AdmGrisTxt,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { confirmarEliminar = null },
                            enabled = !eliminando
                        ) { Text("Cancelar", color = AdmGrisTxt) }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                eliminando = true
                                scope.launch {
                                    try { repo.eliminarUsuaria(usuaria.uid, rol) } catch (_: Exception) {}
                                    eliminando        = false
                                    confirmarEliminar = null
                                    recargar()
                                }
                            },
                            enabled = !eliminando,
                            shape   = RoundedCornerShape(12.dp),
                            colors  = ButtonDefaults.buttonColors(containerColor = AdmRojo)
                        ) {
                            if (eliminando) {
                                CircularProgressIndicator(
                                    color = AdmBlanco, strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text("Eliminar", color = AdmBlanco, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().background(AdmBlanco)) {

        AdminHeader(
            titulo      = "Usuarias registradas",
            subtitulo   = if (cargando) "" else "${usuarias.size} en total",
            onBackClick = onBackClick
        )

        Spacer(Modifier.height(18.dp))

        Box(Modifier.padding(horizontal = 20.dp)) {
            AdminBuscador(
                valor       = busqueda,
                onCambio    = { busqueda = it },
                placeholder = "Buscar por nombre o correo"
            )
        }

        Spacer(Modifier.height(14.dp))

        when {
            cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AdmSuave)
            }

            usuarias.isEmpty() -> AdminVacio("Todavía no hay usuarias registradas en la app.")

            filtradas.isEmpty() -> AdminVacio("Ninguna usuaria coincide con “$busqueda”.")

            else -> LazyColumn(
                contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(filtradas, key = { _, u -> u.uid }) { index, usuaria ->
                    val sinAsignar = usuaria.profesionalAsignado.isBlank() ||
                            usuaria.profesionalAsignado == "Sin asignar"

                    AdminTarjeta {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            AdminAvatarImagen(
                                recurso = FOTOS_USUARIAS[index % FOTOS_USUARIAS.size],
                                tamano  = 48
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        usuaria.nombre,
                                        fontSize   = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color      = AdmOscuro,
                                        maxLines   = 1,
                                        overflow   = TextOverflow.Ellipsis,
                                        modifier   = Modifier.weight(1f, fill = false)
                                    )
                                    if (usuaria.semanas > 0) {
                                        Spacer(Modifier.width(6.dp))
                                        AdminEtiqueta("Sem. ${usuaria.semanas}")
                                    }
                                }

                                if (usuaria.correo.isNotBlank()) {
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        usuaria.correo,
                                        fontSize = 11.sp,
                                        color    = AdmGrisTxt,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(Modifier.height(5.dp))
                                AdminEtiqueta(
                                    texto = if (sinAsignar) "Sin asignar" else usuaria.profesionalAsignado,
                                    fondo = if (sinAsignar) AdmRojoFondo else AdmVerdeFondo,
                                    tinta = if (sinAsignar) AdmRojo else AdmVerde
                                )
                            }

                            if (rol.puedeEditar) {
                                IconButton(onClick = { confirmarEliminar = usuaria }) {
                                    Icon(
                                        Icons.Default.Delete, "Eliminar",
                                        tint     = Color(0xFFCBC9DC),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}