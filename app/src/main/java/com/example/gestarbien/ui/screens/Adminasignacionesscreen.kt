package com.example.gestarbien.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.gestarbien.R
import com.example.gestarbien.data.AdminRepository
import com.example.gestarbien.data.MedicoResumen
import com.example.gestarbien.data.UsuariaResumen
import kotlinx.coroutines.launch

@Composable
fun AdminAsignacionesScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val repo    = remember { AdminRepository(context.applicationContext) }
    val scope   = rememberCoroutineScope()

    var usuarias by remember { mutableStateOf<List<UsuariaResumen>>(emptyList()) }
    var medicos  by remember { mutableStateOf<List<MedicoResumen>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                usuarias = repo.listarUsuarias()
                medicos  = repo.listarMedicos()
            } catch (_: Exception) { }
            cargando = false
        }
    }

    // Agrupa por profesional para ver la carga de cada uno.
    // "Sin asignar" va primero porque es lo que requiere acción.
    val agrupadas = remember(usuarias) {
        usuarias.groupBy { u ->
            if (u.profesionalAsignado.isBlank()) "Sin asignar" else u.profesionalAsignado
        }.toList().sortedWith(
            compareBy({ it.first != "Sin asignar" }, { it.first })
        )
    }

    /** Convierte el correo guardado en el nombre real del profesional. */
    fun nombreProfesional(clave: String): String =
        medicos.firstOrNull { it.correo.equals(clave, true) }?.nombre ?: clave

    Column(Modifier.fillMaxSize().background(AdmBlanco)) {

        AdminHeader(
            titulo      = "Asignaciones",
            subtitulo   = if (cargando) ""
            else "${usuarias.size} usuarias · ${agrupadas.size} grupos",
            onBackClick = onBackClick
        )

        Spacer(Modifier.height(18.dp))

        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AdmSuave)
            }
            return@Column
        }

        if (usuarias.isEmpty()) {
            AdminVacio("Todavía no hay usuarias registradas.")
            return@Column
        }

        LazyColumn(
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(agrupadas.size) { i ->
                val (clave, lista) = agrupadas[i]
                val esSinAsignar   = clave == "Sin asignar"
                val titulo         = if (esSinAsignar) clave else nombreProfesional(clave)

                AdminTarjeta {
                    // ── Cabecera del grupo ───────────────────
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        AdminAvatarImagen(
                            recurso = if (esSinAsignar) R.drawable.animo_mixto else R.drawable.doctor,
                            tamano  = 42,
                            fondo   = if (esSinAsignar) AdmRojoFondo else Color(0xFFE6EFFB)
                        )

                        Spacer(Modifier.width(11.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                titulo,
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (esSinAsignar) AdmRojo else AdmOscuro,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                            Text(
                                if (esSinAsignar) "Necesitan un profesional"
                                else "${lista.size} usuaria${if (lista.size == 1) "" else "s"} a cargo",
                                fontSize = 11.sp,
                                color    = AdmGrisTxt
                            )
                        }

                        AdminEtiqueta(
                            texto = "${lista.size}",
                            fondo = if (esSinAsignar) AdmRojoFondo else AdmVerdeFondo,
                            tinta = if (esSinAsignar) AdmRojo else AdmVerde
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = AdmBordeSuave)
                    Spacer(Modifier.height(4.dp))

                    // ── Usuarias del grupo ───────────────────
                    lista.forEachIndexed { idx, u ->
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AdminAvatarImagen(
                                recurso = FOTOS_USUARIAS[idx % FOTOS_USUARIAS.size],
                                tamano  = 32
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                u.nombre,
                                fontSize = 13.sp,
                                color    = AdmOscuro,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (u.semanas > 0) {
                                AdminEtiqueta("Sem. ${u.semanas}")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}