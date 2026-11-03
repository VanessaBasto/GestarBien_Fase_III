package com.example.gestarbien.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Caracterización agregada de barreras de acceso, para el objetivo de
 * investigación del proyecto (no para consulta clínica individual, que
 * ya se muestra en el detalle de cada paciente del panel profesional).
 *
 * Esta pantalla resume el conjunto de usuarias; el análisis estadístico
 * en profundidad se hace fuera de la app con el CSV exportado.
 */
private data class RegistroBarreras(
    val nombre: String,
    val municipio: String,
    val servicio: Boolean, val llegar: Boolean, val condiciones: Boolean,
    val personal: Boolean, val familiar: Boolean, val cultural: Boolean,
    val impacto: String,
    val facilitadores: List<String>
)

private val CATEGORIAS_LABEL = listOf(
    "servicio" to "Servicio y trámites",
    "llegar" to "Costo y distancia",
    "condiciones" to "Trabajo y afiliación",
    "personal" to "Situación personal",
    "familiar" to "Familia o pareja",
    "cultural" to "Creencias y trato"
)

@Composable
fun AdminCaracterizacionScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var registros by remember { mutableStateOf<List<RegistroBarreras>>(emptyList()) }
    var cargando  by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            registros = cargarRegistros()
            cargando = false
        }
    }

    val total = registros.size
    val frecuencias = CATEGORIAS_LABEL.map { (clave, label) ->
        val count = registros.count {
            when (clave) {
                "servicio" -> it.servicio; "llegar" -> it.llegar
                "condiciones" -> it.condiciones; "personal" -> it.personal
                "familiar" -> it.familiar; else -> it.cultural
            }
        }
        Triple(label, count, if (total > 0) count * 100 / total else 0)
    }.sortedByDescending { it.second }

    val impactoDist = registros
        .filter { it.impacto.isNotBlank() }
        .groupingBy { it.impacto }
        .eachCount()

    val facilitadoresTop = registros
        .flatMap { it.facilitadores }
        .groupingBy { it }
        .eachCount()
        .entries.sortedByDescending { it.value }
        .take(5)

    val porMunicipio = registros
        .filter { it.municipio.isNotBlank() }
        .groupingBy { it.municipio }
        .eachCount()
        .entries.sortedByDescending { it.value }

    Column(Modifier.fillMaxSize().background(AdmBlanco)) {

        AdminHeader(
            titulo      = "Caracterización de barreras",
            subtitulo   = if (cargando) "" else "$total usuarias con datos",
            onBackClick = onBackClick,
            accion = {
                if (!cargando && total > 0) {
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = AdmMedio,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = { exportarCsv(context, registros) }) {
                            Icon(Icons.Default.Share, "Exportar CSV",
                                tint = AdmBlanco, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        )

        Spacer(Modifier.height(18.dp))

        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AdmSuave)
            }
            return@Column
        }

        if (total == 0) {
            AdminVacio("Todavía ninguna usuaria completó el módulo de barreras de acceso.")
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            // ── Nota metodológica ─────────────────────────────
            Surface(shape = RoundedCornerShape(14.dp), color = AdmFondoSuave) {
                Text(
                    "Este resumen es para consulta rápida. El análisis estadístico completo " +
                            "se hace con el CSV exportado, que trae una fila por usuaria y una " +
                            "columna por variable.",
                    fontSize   = 12.sp,
                    color      = AdmOscuro,
                    lineHeight = 17.sp,
                    modifier   = Modifier.padding(14.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Frecuencia por categoría ──────────────────────
            Text("BARRERAS MÁS REPORTADAS", fontSize = 11.sp,
                fontWeight = FontWeight.Bold, color = AdmMedio)
            Spacer(Modifier.height(10.dp))

            AdminTarjeta {
                frecuencias.forEach { (label, count, pct) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp)
                    ) {
                        Text(label, fontSize = 12.sp, color = AdmOscuro,
                            modifier = Modifier.width(140.dp))
                        Box(
                            modifier = Modifier.weight(1f).height(8.dp)
                                .background(Color(0xFFEDEDF5), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct / 100f).fillMaxHeight()
                                    .background(AdmMedio, RoundedCornerShape(4.dp))
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdmOscuro)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Impacto ────────────────────────────────────────
            if (impactoDist.isNotEmpty()) {
                Text("IMPACTO REPORTADO", fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, color = AdmMedio)
                Spacer(Modifier.height(10.dp))
                AdminTarjeta {
                    listOf("No me afectó", "Poco", "Moderadamente", "Mucho", "Impidió la atención")
                        .forEach { nivel ->
                            val c = impactoDist[nivel] ?: 0
                            if (c > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                                ) {
                                    val severo = nivel == "Mucho" || nivel == "Impidió la atención"
                                    AdminEtiqueta(
                                        nivel,
                                        fondo = if (severo) AdmRojoFondo else AdmFondoSuave,
                                        tinta = if (severo) AdmRojo else AdmMedio
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text("$c", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdmOscuro)
                                }
                            }
                        }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Facilitadores ──────────────────────────────────
            if (facilitadoresTop.isNotEmpty()) {
                Text("FACILITADORES MÁS SEÑALADOS", fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, color = AdmMedio)
                Spacer(Modifier.height(10.dp))
                AdminTarjeta {
                    facilitadoresTop.forEach { (nombre, count) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                        ) {
                            Text(nombre, fontSize = 12.sp, color = AdmOscuro, modifier = Modifier.weight(1f))
                            AdminEtiqueta("$count", fondo = AdmVerdeFondo, tinta = AdmVerde)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Por municipio ──────────────────────────────────
            if (porMunicipio.isNotEmpty()) {
                Text("POR MUNICIPIO", fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, color = AdmMedio)
                Spacer(Modifier.height(10.dp))
                AdminTarjeta {
                    porMunicipio.forEach { (mun, count) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                        ) {
                            Text(mun, fontSize = 12.sp, color = AdmOscuro, modifier = Modifier.weight(1f))
                            Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdmOscuro)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  CARGA DE DATOS
// ─────────────────────────────────────────────────────────────
private suspend fun cargarRegistros(): List<RegistroBarreras> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val usuarios = db.collection("usuarios").get().await()

        usuarios.documents.mapNotNull { usuarioDoc ->
            val barrerasDoc = try {
                db.collection("usuarios").document(usuarioDoc.id)
                    .collection("caracterizacion").document("barreras_acceso")
                    .get().await()
            } catch (e: Exception) { null } ?: return@mapNotNull null

            if (!barrerasDoc.exists()) return@mapNotNull null

            fun tiene(clave: String): Boolean {
                val bloque = barrerasDoc.get(clave) as? Map<*, *>
                return (bloque?.get("tiene") as? String) == "Sí"
            }

            RegistroBarreras(
                nombre        = usuarioDoc.getString("nombreCompleto") ?: "Sin nombre",
                municipio     = usuarioDoc.getString("lugarResidencia") ?: "",
                servicio      = tiene("servicio"),
                llegar        = tiene("llegar"),
                condiciones   = tiene("condiciones"),
                personal      = tiene("personal"),
                familiar      = tiene("familiar"),
                cultural      = tiene("cultural"),
                impacto       = barrerasDoc.getString("impacto") ?: "",
                facilitadores = (barrerasDoc.get("facilitadores") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

// ─────────────────────────────────────────────────────────────
//  EXPORTACIÓN CSV
//  Una fila por usuaria, una columna por variable. Formato pensado
//  para abrirse directo en Excel o SPSS por el equipo de psicología.
// ─────────────────────────────────────────────────────────────
private fun exportarCsv(context: Context, registros: List<RegistroBarreras>) {
    val encabezado = listOf(
        "nombre", "municipio",
        "servicio_tramites", "costo_distancia", "trabajo_afiliacion",
        "situacion_personal", "familia_pareja", "creencias_trato",
        "impacto", "facilitadores"
    ).joinToString(";")

    fun csv(v: String) = "\"${v.replace("\"", "'")}\""
    fun sn(b: Boolean) = if (b) "Si" else "No"

    val filas = registros.joinToString("\n") { r ->
        listOf(
            csv(r.nombre), csv(r.municipio),
            sn(r.servicio), sn(r.llegar), sn(r.condiciones),
            sn(r.personal), sn(r.familiar), sn(r.cultural),
            csv(r.impacto), csv(r.facilitadores.joinToString(", "))
        ).joinToString(";")
    }

    val contenido = "$encabezado\n$filas"

    try {
        val archivo = File(context.cacheDir, "barreras_acceso_gestarbien.csv")
        archivo.writeText(contenido, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", archivo
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir CSV de barreras de acceso"))
    } catch (e: Exception) {
        // Antes esto fallaba en silencio. Si ves este Toast, casi seguro es
        // que falta declarar el FileProvider en AndroidManifest.xml, o falta
        // res/xml/file_paths.xml (ver detalle abajo).
        Log.e("AdminCaracterizacion", "Error exportando CSV", e)
        Toast.makeText(context, "No se pudo exportar el CSV: ${e.message}", Toast.LENGTH_LONG).show()
    }
}