package com.example.gestarbien.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gestarbien.ia.offline.ModeloPreferencias
import com.example.gestarbien.ia.offline.OfflineLLM
import kotlinx.coroutines.launch

private const val PESO_APROX_MB = 229

@Composable
fun AjustesIAOfflineScreen(modifier: Modifier = Modifier, onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // La fuente de verdad ahora es el archivo en disco, no el flag de preferencias.
    var modeloDescargado by remember {
        mutableStateOf(
            OfflineLLM.modeloDescargado(context).also {
                ModeloPreferencias.marcarModeloListo(context, it)
            }
        )
    }
    var descargando by remember { mutableStateOf(false) }
    var progreso by remember { mutableStateOf<Float?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(Modifier.width(4.dp))
            Text("Conversación ampliada", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Descarga un pequeño modelo de inteligencia artificial que corre directamente en tu " +
                    "celular. Se usa cuando las respuestas guardadas no cubren lo que quieres contar, " +
                    "para conversar de forma más natural sin internet. " +
                    "Ningún mensaje sale de tu dispositivo.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(24.dp))

        when {
            modeloDescargado -> {
                Text("✓ Conversación ampliada activa")
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    scope.launch {
                        OfflineLLM.eliminarModelo(context)
                        modeloDescargado = false
                    }
                }) { Text("Eliminar modelo descargado") }
            }

            descargando -> {
                val p = progreso
                if (p == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (p == null) "Descargando y preparando el modelo... puede tardar unos minutos. No cierres la app."
                    else "Descargando... ${(p * 100).toInt()}%. No cierres la app."
                )
            }

            else -> {
                Button(onClick = { mostrarDialogo = true }) {
                    Text("Descargar (~$PESO_APROX_MB MB)")
                }
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("No se pudo preparar el modelo: $it", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("¿Descargar el modelo de IA?") },
            text = {
                Text(
                    "Se descargará una sola vez (~$PESO_APROX_MB MB) y " +
                            "después funcionará sin conexión. Recomendamos hacerlo con wifi."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    error = null
                    progreso = null
                    descargando = true
                    scope.launch {
                        // 1) bajar el archivo (si ya está, salta directo)  2) cargarlo para comprobar que funciona
                        var ok = OfflineLLM.descargarModelo(context) { progreso = it }
                        if (ok) ok = OfflineLLM.warmUp(context)

                        if (ok) {
                            ModeloPreferencias.marcarModeloListo(context, true)
                            modeloDescargado = true
                        } else {
                            error = OfflineLLM.ultimoError ?: "Error desconocido"
                        }
                        descargando = false
                    }
                }) { Text("Sí, descargar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Ahora no") }
            },
        )
    }
}