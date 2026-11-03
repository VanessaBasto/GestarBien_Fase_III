package com.example.gestarbien.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.gestarbien.R
import com.example.gestarbien.data.AdminRepository
import com.example.gestarbien.data.MedicoResumen
import com.example.gestarbien.data.RolAdmin
import kotlinx.coroutines.launch

@Composable
fun AdminMedicosScreen(
    rol: RolAdmin,
    onBackClick: () -> Unit,
    onAgregarMedico: () -> Unit = {}
) {
    val context = LocalContext.current
    val repo    = remember { AdminRepository(context.applicationContext) }
    val scope   = rememberCoroutineScope()

    var medicos           by remember { mutableStateOf<List<MedicoResumen>>(emptyList()) }
    var cargando          by remember { mutableStateOf(true) }
    var busqueda          by remember { mutableStateOf("") }
    var confirmarEliminar by remember { mutableStateOf<MedicoResumen?>(null) }
    var eliminando        by remember { mutableStateOf(false) }
    var avisoReasignacion by remember { mutableStateOf("") }

    fun recargar() {
        scope.launch {
            cargando = true
            medicos  = try { repo.listarMedicos() } catch (e: Exception) { emptyList() }
            cargando = false
        }
    }
    LaunchedEffect(Unit) { recargar() }

    val filtrados = remember(medicos, busqueda) {
        if (busqueda.isBlank()) medicos
        else medicos.filter {
            it.nombre.contains(busqueda, true) ||
                    it.correo.contains(busqueda, true) ||
                    it.profesion.contains(busqueda, true)
        }
    }

    // ── Confirmación de borrado ──────────────────────────────
    confirmarEliminar?.let { medico ->
        val hayOtros = medicos.any { it.docId != medico.docId }

        Dialog(onDismissRequest = { if (!eliminando) confirmarEliminar = null }) {
            Surface(shape = RoundedCornerShape(20.dp), color = AdmBlanco) {
                Column(Modifier.padding(22.dp)) {
                    Text("¿Eliminar a ${medico.nombre}?", fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = AdmOscuro)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (hayOtros)
                            "Sus usuarias se reasignarán automáticamente al siguiente profesional " +
                                    "de la lista, para que ninguna quede sin acompañamiento."
                        else
                            "Es el único profesional registrado, así que sus usuarias quedarán sin " +
                                    "asignar hasta que crees otro médico.",
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
                                    try {
                                        val r = repo.eliminarMedico(medico.docId, rol)
                                        avisoReasignacion = when {
                                            r.reasignadas == 0 -> "Médico eliminado."
                                            r.nuevoProfesional != null ->
                                                "${r.reasignadas} usuaria${if (r.reasignadas == 1) "" else "s"} " +
                                                        "se reasignó a ${r.nuevoProfesional}."
                                            else ->
                                                "${r.reasignadas} usuaria${if (r.reasignadas == 1) "" else "s"} " +
                                                        "quedó sin asignar. Crea otro médico pronto."
                                        }
                                    } catch (e: Exception) {
                                        avisoReasignacion = "No se pudo eliminar: ${e.message}"
                                    }
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
            titulo      = "Médicos registrados",
            subtitulo   = if (cargando) "" else "${medicos.size} en total",
            onBackClick = onBackClick,
            accion = {
                if (rol.puedeEditar) {
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = AdmMedio,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onAgregarMedico) {
                            Icon(Icons.Default.Add, "Añadir profesional",
                                tint = AdmBlanco, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        )

        Spacer(Modifier.height(18.dp))

        Box(Modifier.padding(horizontal = 20.dp)) {
            AdminBuscador(
                valor       = busqueda,
                onCambio    = { busqueda = it },
                placeholder = "Buscar por nombre, correo o profesión"
            )
        }

        // Aviso de reasignación tras eliminar
        if (avisoReasignacion.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = AdmFondoSuave,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        avisoReasignacion,
                        fontSize   = 12.sp,
                        color      = AdmOscuro,
                        lineHeight = 17.sp,
                        modifier   = Modifier.weight(1f)
                    )
                    TextButton(onClick = { avisoReasignacion = "" }) {
                        Text("OK", fontSize = 12.sp, color = AdmMedio, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        when {
            cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AdmSuave)
            }

            medicos.isEmpty() -> AdminVacio(
                "Todavía no hay profesionales registrados.\nUsa el botón + para crear el primero."
            )

            filtrados.isEmpty() -> AdminVacio("Ningún profesional coincide con “$busqueda”.")

            else -> LazyColumn(
                contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtrados, key = { it.docId }) { medico ->
                    AdminTarjeta {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            AdminAvatarImagen(
                                recurso = R.drawable.doctor,
                                tamano  = 48,
                                fondo   = Color(0xFFE6EFFB)
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(Modifier.weight(1f)) {
                                // Nombre y cargo en la misma línea
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        medico.nombre,
                                        fontSize   = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color      = AdmOscuro,
                                        maxLines   = 1,
                                        overflow   = TextOverflow.Ellipsis,
                                        modifier   = Modifier.weight(1f, fill = false)
                                    )
                                    if (medico.profesion.isNotBlank()) {
                                        Spacer(Modifier.width(6.dp))
                                        AdminEtiqueta(medico.profesion)
                                    }
                                }

                                Spacer(Modifier.height(4.dp))

                                // Correo y teléfono en una sola línea
                                Text(
                                    buildString {
                                        append(medico.correo)
                                        if (medico.whatsapp.isNotBlank()) {
                                            append("  ·  ")
                                            append(medico.whatsapp)
                                        }
                                    },
                                    fontSize = 11.sp,
                                    color    = AdmGrisTxt,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (rol.puedeEditar) {
                                IconButton(onClick = { confirmarEliminar = medico }) {
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