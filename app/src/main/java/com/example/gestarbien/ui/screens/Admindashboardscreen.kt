package com.example.gestarbien.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R
import com.example.gestarbien.data.AdminRepository
import com.example.gestarbien.data.RolAdmin
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    rol: RolAdmin,
    onVerUsuarias: () -> Unit,
    onVerMedicos: () -> Unit,
    onAgregarMedico: () -> Unit,
    onVerAsignaciones: () -> Unit,
    onVerCaracterizacion: () -> Unit,
    onVerInvestigadores: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val repo    = remember { AdminRepository(context.applicationContext) }
    val scope   = rememberCoroutineScope()

    val nombreAdmin = remember { nombreDelAdmin() }
    val esTotal = rol.puedeEditar

    var totalUsuarias by remember { mutableStateOf(0) }
    var totalMedicos  by remember { mutableStateOf(0) }
    var sinAsignar    by remember { mutableStateOf(0) }
    var conAsignacion by remember { mutableStateOf(0) }
    var cargando      by remember { mutableStateOf(true) }
    var errorMsg      by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val usuarias = repo.listarUsuarias()
                totalUsuarias = usuarias.size
                sinAsignar    = usuarias.count {
                    it.profesionalAsignado.isBlank() || it.profesionalAsignado == "Sin asignar"
                }
                conAsignacion = totalUsuarias - sinAsignar
                totalMedicos  = repo.listarMedicos().size
            } catch (e: Exception) {
                errorMsg = "No se pudieron cargar los datos: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdmBlanco)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Header ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(AdmFondoSuave)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text       = "Hola, $nombreAdmin",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AdmOscuro
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = if (cargando) "Cargando información…"
                        else "$totalUsuarias usuarias y $totalMedicos profesionales registrados",
                        fontSize = 14.sp,
                        color    = AdmMedio
                    )
                    if (!esTotal) {
                        Spacer(Modifier.height(6.dp))
                        AdminEtiqueta("Solo lectura", fondo = AdmFondoCard, tinta = AdmMedio)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Image(
                    painter            = painterResource(id = R.drawable.iconp),
                    contentDescription = "Administrador",
                    modifier           = Modifier.size(76.dp).clip(CircleShape),
                    contentScale       = ContentScale.Fit
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // ── Estadísticas, fila 1 ─────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaAdminStat(
                modifier    = Modifier.weight(1f),
                numero      = totalUsuarias,
                etiqueta    = "Usuarias",
                recurso     = R.drawable.mujer1,
                iconoBg     = Color.White,
                iconoBorder = Color(0xFFEAE8F7)
            )
            TarjetaAdminStat(
                modifier    = Modifier.weight(1f),
                numero      = totalMedicos,
                etiqueta    = "Médicos",
                recurso     = R.drawable.doctor,
                iconoBg     = Color(0xFFD6E8F7),
                iconoBorder = Color(0xFFADC8E8)
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Estadísticas, fila 2 ─────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaAdminStat(
                modifier    = Modifier.weight(1f),
                numero      = conAsignacion,
                etiqueta    = "Con psicólogo",
                recurso     = R.drawable.animo_feliz,
                iconoBg     = Color.White,
                iconoBorder = Color(0xFFEAE8F7)
            )
            TarjetaAdminStat(
                modifier    = Modifier.weight(1f),
                numero      = sinAsignar,
                etiqueta    = "Sin asignar",
                recurso     = R.drawable.animo_mixto,
                iconoBg     = Color.White,
                iconoBorder = if (sinAsignar > 0) Color(0xFFF0B5AF) else Color(0xFFEAE8F7),
                resaltado   = sinAsignar > 0
            )
        }

        Spacer(Modifier.height(26.dp))

        if (errorMsg.isNotBlank()) {
            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = AdmRojoFondo,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Text(
                    errorMsg,
                    fontSize   = 12.sp,
                    color      = AdmRojo,
                    lineHeight = 17.sp,
                    modifier   = Modifier.padding(14.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
        }

        // ── Gestión ──────────────────────────────────────────
        Text(
            text       = "Gestión",
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = AdmOscuro,
            modifier   = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Acción principal: crear médico. Solo administrador total.
        if (esTotal) {
            Surface(
                shape           = RoundedCornerShape(18.dp),
                color           = AdmMedio,
                shadowElevation = 4.dp,
                modifier        = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onAgregarMedico() }
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AdmBlanco.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = AdmBlanco, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Añadir profesional", fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, color = AdmBlanco)
                        Text(
                            "Crea su cuenta y queda listo para ingresar",
                            fontSize = 12.sp,
                            color    = AdmBlanco.copy(alpha = 0.85f)
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                        tint = AdmBlanco.copy(alpha = 0.75f)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        // Usuarias y médicos: visibles para los dos roles, pero el
        // investigador entra en modo solo lectura (sin botón de borrar).
        FilaGestion("Usuarias registradas", "Ver, buscar y eliminar", totalUsuarias, onVerUsuarias)
        Spacer(Modifier.height(10.dp))
        FilaGestion("Profesionales registrados", "Ver y eliminar profesionales", totalMedicos, onVerMedicos)
        Spacer(Modifier.height(10.dp))
        FilaGestion("Asignaciones", "Qué psicólogo acompaña a cada usuaria", null, onVerAsignaciones)
        Spacer(Modifier.height(10.dp))
        FilaGestion("Caracterización de barreras", "Frecuencias y exportar CSV", null, onVerCaracterizacion)

        // Gestión de investigadores: solo la ve quien puede crearlos.
        if (esTotal) {
            Spacer(Modifier.height(10.dp))
            FilaGestion("Investigadores", "Cuentas de solo lectura para el equipo", null, onVerInvestigadores)
        }

        Spacer(Modifier.height(28.dp))

        OutlinedButton(
            onClick  = onCerrarSesion,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = AdmRojo),
            border   = BorderStroke(1.5.dp, AdmRojo)
        ) {
            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(48.dp))
    }
}

// ─────────────────────────────────────────────────────────────
//  TARJETA DE ESTADÍSTICA
// ─────────────────────────────────────────────────────────────
@Composable
private fun TarjetaAdminStat(
    modifier: Modifier,
    numero: Int,
    etiqueta: String,
    recurso: Int,
    iconoBg: Color,
    iconoBorder: Color,
    resaltado: Boolean = false
) {
    Surface(
        modifier        = modifier,
        shape           = RoundedCornerShape(16.dp),
        color           = AdmFondoCard,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconoBg)
                    .border(1.5.dp, iconoBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(id = recurso),
                    contentDescription = null,
                    modifier           = Modifier.size(34.dp),
                    contentScale       = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text       = numero.toString(),
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (resaltado) AdmRojo else AdmOscuro
                )
                Text(
                    text       = etiqueta,
                    fontSize   = 12.sp,
                    color      = AdmGrisTxt,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  FILA DE GESTIÓN
// ─────────────────────────────────────────────────────────────
@Composable
private fun FilaGestion(
    titulo: String,
    subtitulo: String,
    conteo: Int?,
    onClick: () -> Unit
) {
    Surface(
        shape           = RoundedCornerShape(16.dp),
        color           = AdmBlanco,
        shadowElevation = 2.dp,
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, AdmBordeSuave, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(titulo, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, color = AdmOscuro)
                Spacer(Modifier.height(2.dp))
                Text(subtitulo, fontSize = 12.sp, color = AdmGrisTxt)
            }
            if (conteo != null) {
                AdminEtiqueta(conteo.toString())
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                tint     = Color(0xFFC5C4D6),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}