package com.example.gestarbien.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * Geolocalización · "Necesito ayuda"
 *
 * La primera vez muestra un modo tutorial (4 pasos) con el mismo diseño del
 * cuestionario: qué hace la función, contactos de emergencia, canal de envío
 * (WhatsApp o mensaje de texto) y permiso de ubicación.
 * Después, al entrar, va directo al botón de ayuda.
 *
 * Al pulsar el botón se toma la ubicación GPS y se arma un mensaje con un
 * enlace de Google Maps y las coordenadas, listo para enviar a los contactos.
 *
 * Dependencia necesaria en build.gradle:
 *   implementation("com.google.android.gms:play-services-location:21.3.0")
 * Permisos en AndroidManifest.xml:
 *   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 *   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 */

// ── Paleta (la misma de la app) ─────────────────────────────────────────────
private val AyLavanda      = Color(0xFFB1B4E1)
private val AyLavandaSuave = Color(0xFFEEEFFE)
private val AyMoradoOscuro = Color(0xFF3A3D8C)
private val AyMoradoMedio  = Color(0xFF5C5FA8)
private val AyGris         = Color(0xFF6B6B8A)
private val AyGrisClaro    = Color(0xFF9999BB)
private val AyCoral        = Color(0xFFE8899A)   // botón de ayuda: cálido, sin ser alarmante
private val AyCoralSuave   = Color(0xFFFDEEF1)

private const val MAX_CONTACTOS = 3
private const val CANAL_WHATSAPP = "WhatsApp"
private const val CANAL_SMS      = "Mensaje de texto"

// ── Modelos ─────────────────────────────────────────────────────────────────
data class ContactoEmergencia(val nombre: String = "", val telefono: String = "") {
    val esValido: Boolean
        get() = nombre.isNotBlank() && telefono.removePrefix("+57").length == 10
}

data class ConfigAyuda(
    val contactos: List<ContactoEmergencia> = emptyList(),
    val canal: String = CANAL_WHATSAPP,
    val tutorialVisto: Boolean = false
)

private enum class PasoAyuda { INTRO, CONTACTOS, CANAL, PERMISO, LISTO }
private enum class EstadoEnvioAyuda { INACTIVO, BUSCANDO, UBICADA, ERROR_UBICACION, GPS_APAGADO }

// ── Persistencia: caché local (funciona sin internet) + Firestore ─────────
private fun claveConfig(uid: String) = "ayuda_config_$uid"

private fun ConfigAyuda.aJson(): String = JSONObject().apply {
    put("canal", canal)
    put("tutorialVisto", tutorialVisto)
    put("contactos", JSONArray().apply {
        contactos.forEach { put(JSONObject().put("nombre", it.nombre).put("telefono", it.telefono)) }
    })
}.toString()

private fun configDesdeJson(json: String?): ConfigAyuda? = runCatching {
    if (json.isNullOrBlank()) return null
    val o = JSONObject(json)
    val arr = o.optJSONArray("contactos") ?: JSONArray()
    ConfigAyuda(
        contactos = (0 until arr.length()).map {
            val c = arr.getJSONObject(it)
            ContactoEmergencia(c.optString("nombre"), c.optString("telefono"))
        },
        canal = o.optString("canal", CANAL_WHATSAPP),
        tutorialVisto = o.optBoolean("tutorialVisto", false)
    )
}.getOrNull()

private fun guardarConfigAyuda(context: Context, uid: String, config: ConfigAyuda) {
    context.getSharedPreferences("gestarbien_prefs", Context.MODE_PRIVATE)
        .edit().putString(claveConfig(uid), config.aJson()).apply()
    if (uid.isBlank()) return
    FirebaseFirestore.getInstance()
        .collection("usuarios").document(uid)
        .collection("emergencia").document("config")
        .set(
            mapOf(
                "contactos" to config.contactos.map { mapOf("nombre" to it.nombre, "telefono" to it.telefono) },
                "canal" to config.canal,
                "tutorialVisto" to config.tutorialVisto
            )
        )
}

// ── Ubicación ───────────────────────────────────────────────────────────────
private fun tienePermisoUbicacion(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun gpsEncendido(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
    return LocationManagerCompat.isLocationEnabled(lm)
}

/** Pide una lectura fresca del GPS (máx. 15 s); si no llega, usa la última conocida. */
@SuppressLint("MissingPermission")
private suspend fun obtenerUbicacion(context: Context): Pair<Double, Double>? {
    if (!tienePermisoUbicacion(context)) return null
    val cliente = LocationServices.getFusedLocationProviderClient(context)
    return try {
        val token = CancellationTokenSource()
        val fresca = withTimeoutOrNull(15_000) {
            cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token).await()
        }
        if (fresca == null) token.cancel()
        val loc = fresca ?: cliente.lastLocation.await()
        loc?.let { it.latitude to it.longitude }
    } catch (e: Exception) {
        null
    }
}

private fun mensajeAyuda(lat: Double, lng: Double): String {
    val la = String.format(Locale.US, "%.6f", lat)
    val lo = String.format(Locale.US, "%.6f", lng)
    return "Necesito ayuda. Esta es mi ubicación actual:\n" +
            "https://www.google.com/maps/search/?api=1&query=$la,$lo\n" +
            "Coordenadas: $la, $lo\n" +
            "(Enviado desde Gestar Bien)"
}

// ── Envío ───────────────────────────────────────────────────────────────────
/** Abre la app de mensajes con todos los contactos y el texto ya escrito. */
private fun abrirSms(context: Context, contactos: List<ContactoEmergencia>, mensaje: String) {
    val numeros = contactos.joinToString(";") { it.telefono }
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$numeros"))
        .putExtra("sms_body", mensaje)
    runCatching { context.startActivity(intent) }
}

/** WhatsApp solo permite un chat a la vez, así que se abre por contacto. */
private fun abrirWhatsApp(context: Context, contacto: ContactoEmergencia, mensaje: String) {
    val numero = contacto.telefono.filter { it.isDigit() }   // 57XXXXXXXXXX
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$numero?text=${Uri.encode(mensaje)}"))
    runCatching { context.startActivity(intent) }
}

private fun llamar(context: Context, numero: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero"))) }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PANTALLA
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AyudaUbicacionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val prefs = remember { context.getSharedPreferences("gestarbien_prefs", Context.MODE_PRIVATE) }

    var config by remember {
        mutableStateOf(configDesdeJson(prefs.getString(claveConfig(uid), null)) ?: ConfigAyuda())
    }
    var paso by remember {
        mutableStateOf(if (config.tutorialVisto && config.contactos.isNotEmpty()) PasoAyuda.LISTO else PasoAyuda.INTRO)
    }
    var contactos by remember {
        mutableStateOf(config.contactos.ifEmpty { listOf(ContactoEmergencia()) })
    }
    var canal by remember { mutableStateOf(config.canal) }
    var permisoOk by remember { mutableStateOf(tienePermisoUbicacion(context)) }
    var permisoNegado by remember { mutableStateOf(false) }

    // Si es un celular nuevo y no hay caché local, se recupera de Firestore.
    LaunchedEffect(uid) {
        if (uid.isBlank() || config.contactos.isNotEmpty()) return@LaunchedEffect
        runCatching {
            val doc = FirebaseFirestore.getInstance()
                .collection("usuarios").document(uid)
                .collection("emergencia").document("config")
                .get().await()
            if (doc.exists()) {
                @Suppress("UNCHECKED_CAST")
                val lista = (doc.get("contactos") as? List<Map<String, Any?>>).orEmpty().map {
                    ContactoEmergencia(it["nombre"] as? String ?: "", it["telefono"] as? String ?: "")
                }
                val remota = ConfigAyuda(
                    contactos = lista,
                    canal = doc.getString("canal") ?: CANAL_WHATSAPP,
                    tutorialVisto = doc.getBoolean("tutorialVisto") ?: false
                )
                config = remota
                contactos = lista.ifEmpty { listOf(ContactoEmergencia()) }
                canal = remota.canal
                prefs.edit().putString(claveConfig(uid), remota.aJson()).apply()
                if (remota.tutorialVisto && lista.isNotEmpty()) paso = PasoAyuda.LISTO
            }
        }
    }

    val launcherPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        permisoOk = resultado.values.any { it }
        permisoNegado = !permisoOk
    }
    fun pedirPermiso() = launcherPermiso.launch(
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    fun terminarTutorial() {
        val nueva = ConfigAyuda(contactos.filter { it.esValido }, canal, tutorialVisto = true)
        config = nueva
        guardarConfigAyuda(context, uid, nueva)
        paso = PasoAyuda.LISTO
    }

    fun atras() {
        paso = when (paso) {
            PasoAyuda.CONTACTOS -> if (config.tutorialVisto) PasoAyuda.LISTO else PasoAyuda.INTRO
            PasoAyuda.CANAL     -> PasoAyuda.CONTACTOS
            PasoAyuda.PERMISO   -> PasoAyuda.CANAL
            else -> { onBack(); return }
        }
    }
    BackHandler { atras() }

    when (paso) {
        PasoAyuda.INTRO -> PasoTutorial(numero = 1, onBack = ::atras) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BotMascota(
                    color = runCatching {
                        Color(android.graphics.Color.parseColor(prefs.getString("bot_color_$uid", "#B1B4E1")))
                    }.getOrDefault(AyLavanda),
                    size = 64.dp
                )
            }
            Spacer(Modifier.height(16.dp))
            TituloPaso("Modo tutorial · Paso 1 de 4", "Necesito ayuda")
            Text(
                "Esta función te permite avisar a las personas de tu confianza dónde estás, " +
                        "con un solo toque, cuando sientas que necesitas apoyo.",
                fontSize = 13.sp, color = AyGris, lineHeight = 18.sp
            )
            Spacer(Modifier.height(20.dp))
            FilaTutorial("1", "Añade tus contactos de emergencia", "Hasta 3 personas de tu confianza.")
            FilaTutorial("2", "Elige cómo avisarles", "Por WhatsApp o por mensaje de texto.")
            FilaTutorial("3", "Permite conocer tu ubicación", "Solo se usa cuando pulsas el botón de ayuda.")
            Spacer(Modifier.height(24.dp))
            BotonContinuar(texto = "Comenzar", habilitado = true, onClick = { paso = PasoAyuda.CONTACTOS })
        }

        PasoAyuda.CONTACTOS -> PasoTutorial(numero = 2, onBack = ::atras) {
            TituloPaso("Modo tutorial · Paso 2 de 4", "Contactos de emergencia")
            Text(
                "Acá podrás añadir los contactos de emergencia para cuando necesites ayuda.",
                fontSize = 13.sp, color = AyGris, lineHeight = 18.sp
            )
            Spacer(Modifier.height(20.dp))

            contactos.forEachIndexed { i, c ->
                PreguntaCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SeccionLabel("Contacto ${i + 1}", modifier = Modifier.weight(1f))
                        if (contactos.size > 1) {
                            Icon(
                                Icons.Default.Close, contentDescription = "Quitar contacto",
                                tint = AyGrisClaro,
                                modifier = Modifier.size(20.dp).clip(CircleShape)
                                    .clickable { contactos = contactos.filterIndexed { j, _ -> j != i } }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    CampoTexto(
                        label = "", value = c.nombre,
                        onValueChange = { v -> contactos = contactos.mapIndexed { j, x -> if (j == i) x.copy(nombre = v) else x } },
                        placeholder = "Nombre (ej. Mi mamá)"
                    )
                    Spacer(Modifier.height(8.dp))
                    val digitos = c.telefono.removePrefix("+57")
                    CampoTelefono(
                        label = "", value = c.telefono,
                        onValueChange = { v -> contactos = contactos.mapIndexed { j, x -> if (j == i) x.copy(telefono = v) else x } },
                        isError = digitos.isNotEmpty() && digitos.length != 10,
                        errorText = "Ingresa un número válido (10 dígitos)"
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            if (contactos.size < MAX_CONTACTOS) {
                OpcionChip(
                    text = "+ Añadir otro contacto", selected = false,
                    onClick = { contactos = contactos + ContactoEmergencia() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))
            SeccionPrivacidad()
            Spacer(Modifier.height(24.dp))
            BotonContinuar(
                texto = "Continuar",
                habilitado = contactos.isNotEmpty() && contactos.all { it.esValido },
                onClick = { paso = PasoAyuda.CANAL }
            )
        }

        PasoAyuda.CANAL -> PasoTutorial(numero = 3, onBack = ::atras) {
            TituloPaso("Modo tutorial · Paso 3 de 4", "¿Cómo les avisamos?")
            Text(
                "Cuando pulses el botón de ayuda, prepararemos un mensaje con tu ubicación " +
                        "para enviarlo por el medio que elijas.",
                fontSize = 13.sp, color = AyGris, lineHeight = 18.sp
            )
            Spacer(Modifier.height(20.dp))
            PreguntaCard {
                SeccionLabel("Enviar el aviso por")
                Spacer(Modifier.height(10.dp))
                FilaOpciones(
                    opciones = listOf(CANAL_WHATSAPP, CANAL_SMS),
                    seleccionado = canal,
                    onSeleccionar = { canal = it }
                )
                AyudaSeleccion(
                    if (canal == CANAL_SMS) "El mensaje de texto funciona aunque no tengas internet."
                    else "WhatsApp necesita internet. Si no tienes, podrás usar mensaje de texto."
                )
            }
            Spacer(Modifier.height(24.dp))
            BotonContinuar(texto = "Continuar", habilitado = canal.isNotBlank(), onClick = {
                paso = if (permisoOk) { terminarTutorial(); PasoAyuda.LISTO } else PasoAyuda.PERMISO
            })
        }

        PasoAyuda.PERMISO -> PasoTutorial(numero = 4, onBack = ::atras) {
            TituloPaso("Modo tutorial · Paso 4 de 4", "Tu ubicación")
            Text(
                "Ahora necesitamos permiso para saber tu ubicación. Solo la tomaremos " +
                        "cuando pulses el botón \"Necesito ayuda\", nunca en segundo plano.",
                fontSize = 13.sp, color = AyGris, lineHeight = 18.sp
            )
            Spacer(Modifier.height(20.dp))
            PreguntaCard {
                if (permisoOk) {
                    Text("✓ Permiso concedido", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AyMoradoOscuro)
                } else {
                    SeccionLabel("Toca el botón y elige \"Mientras se usa la app\".")
                    Spacer(Modifier.height(10.dp))
                    OpcionChip(
                        text = "Permitir ubicación", selected = false,
                        onClick = { pedirPermiso() }, modifier = Modifier.fillMaxWidth()
                    )
                    if (permisoNegado) {
                        Spacer(Modifier.height(10.dp))
                        AvisoApoyo(
                            "No pudimos obtener el permiso. Puedes activarlo desde los ajustes " +
                                    "del celular, en Permisos → Ubicación."
                        )
                        Spacer(Modifier.height(8.dp))
                        OpcionChip(
                            text = "Abrir ajustes del celular", selected = false,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null))
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            BotonContinuar(texto = "Terminar", habilitado = permisoOk, onClick = { terminarTutorial() })
            Spacer(Modifier.height(10.dp))
            Text(
                "Ahora no",
                fontSize = 13.sp, color = AyGrisClaro, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable { terminarTutorial() }.padding(8.dp)
            )
        }

        PasoAyuda.LISTO -> PantallaBotonAyuda(
            config = config,
            permisoOk = permisoOk,
            onPedirPermiso = { pedirPermiso() },
            onEditarContactos = {
                contactos = config.contactos.ifEmpty { listOf(ContactoEmergencia()) }
                canal = config.canal
                paso = PasoAyuda.CONTACTOS
            },
            onVerTutorial = { paso = PasoAyuda.INTRO },
            onBack = onBack,
            onRefrescarPermiso = { permisoOk = tienePermisoUbicacion(context) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  BOTÓN DE AYUDA (después del tutorial)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PantallaBotonAyuda(
    config: ConfigAyuda,
    permisoOk: Boolean,
    onPedirPermiso: () -> Unit,
    onEditarContactos: () -> Unit,
    onVerTutorial: () -> Unit,
    onBack: () -> Unit,
    onRefrescarPermiso: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var estado by remember { mutableStateOf(EstadoEnvioAyuda.INACTIVO) }
    var mensaje by remember { mutableStateOf("") }

    fun enviarPorCanalPreferido(texto: String) {
        if (config.canal == CANAL_SMS) abrirSms(context, config.contactos, texto)
        else if (config.contactos.size == 1) abrirWhatsApp(context, config.contactos.first(), texto)
        // Con varios contactos por WhatsApp se muestran los botones por persona.
    }

    fun pulsarAyuda() {
        onRefrescarPermiso()
        if (!tienePermisoUbicacion(context)) { onPedirPermiso(); return }
        if (!gpsEncendido(context)) { estado = EstadoEnvioAyuda.GPS_APAGADO; return }
        estado = EstadoEnvioAyuda.BUSCANDO
        scope.launch {
            val ubic = obtenerUbicacion(context)
            if (ubic == null) {
                estado = EstadoEnvioAyuda.ERROR_UBICACION
            } else {
                mensaje = mensajeAyuda(ubic.first, ubic.second)
                estado = EstadoEnvioAyuda.UBICADA
                enviarPorCanalPreferido(mensaje)
            }
        }
    }

    PasoContainer(currentStep = 0, totalSteps = 0, onBack = onBack) {
        Spacer(Modifier.height(40.dp))
        TarjetaPaso {
            TituloPaso("Geolocalización", "Necesito ayuda")
            Text(
                "Pulsa el botón y enviaremos tu ubicación a tus contactos de emergencia.",
                fontSize = 13.sp, color = AyGris, lineHeight = 18.sp
            )
            Spacer(Modifier.height(24.dp))

            // ── Botón principal ──
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(AyCoralSuave)
                        .padding(14.dp)
                        .clip(CircleShape)
                        .background(AyCoral)
                        .clickable(enabled = estado != EstadoEnvioAyuda.BUSCANDO) { pulsarAyuda() },
                    contentAlignment = Alignment.Center
                ) {
                    if (estado == EstadoEnvioAyuda.BUSCANDO) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Buscando tu\nubicación...", fontSize = 12.sp, color = Color.White, textAlign = TextAlign.Center)
                        }
                    } else {
                        Text(
                            "Necesito\nayuda", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = Color.White, textAlign = TextAlign.Center, lineHeight = 24.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // ── Resultado ──
            when (estado) {
                EstadoEnvioAyuda.GPS_APAGADO -> {
                    AvisoApoyo("Tu ubicación (GPS) está apagada. Actívala y vuelve a pulsar el botón.")
                    Spacer(Modifier.height(8.dp))
                    OpcionChip(
                        text = "Activar ubicación", selected = false,
                        onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                EstadoEnvioAyuda.ERROR_UBICACION -> {
                    AvisoApoyo(
                        "No pudimos obtener tu ubicación. Si estás en un lugar cerrado, intenta acercarte " +
                                "a una ventana y vuelve a pulsar el botón."
                    )
                }
                EstadoEnvioAyuda.UBICADA -> {
                    PreguntaCard {
                        SeccionLabel("Tu mensaje está listo. Envíalo a:")
                        Spacer(Modifier.height(10.dp))
                        if (config.canal == CANAL_WHATSAPP) {
                            config.contactos.forEach { c ->
                                OpcionChip(
                                    text = "WhatsApp · ${c.nombre}", selected = false,
                                    onClick = { abrirWhatsApp(context, c, mensaje) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            OpcionChip(
                                text = "Enviar por mensaje de texto a todos", selected = false,
                                onClick = { abrirSms(context, config.contactos, mensaje) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OpcionChip(
                                text = "Mensaje de texto a todos", selected = false,
                                onClick = { abrirSms(context, config.contactos, mensaje) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            config.contactos.forEach { c ->
                                OpcionChip(
                                    text = "WhatsApp · ${c.nombre}", selected = false,
                                    onClick = { abrirWhatsApp(context, c, mensaje) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        AyudaSeleccion("Se abrirá la app con el mensaje escrito; solo tienes que tocar enviar.")
                    }
                }
                else -> {
                    if (!permisoOk) {
                        AvisoApoyo("Para enviar tu ubicación necesitamos tu permiso. Te lo pediremos al pulsar el botón.")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            // ── Emergencia inmediata ──
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OpcionChip(text = "Llamar al 123\nEmergencias", selected = false,
                    onClick = { llamar(context, "123") }, modifier = Modifier.weight(1f))
                OpcionChip(text = "Llamar a la 155\nViolencia", selected = false,
                    onClick = { llamar(context, "155") }, modifier = Modifier.weight(1f))
            }

            SeparadorSeccion("Tus contactos")
            config.contactos.forEach { c ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(AyLavandaSuave),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(c.nombre.take(1).uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AyMoradoMedio)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(c.nombre, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AyMoradoOscuro)
                        Text(c.telefono, fontSize = 11.sp, color = AyGrisClaro)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OpcionChip(text = "Editar contactos", selected = false,
                    onClick = onEditarContactos, modifier = Modifier.weight(1f))
                OpcionChip(text = "Ver tutorial", selected = false,
                    onClick = onVerTutorial, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PIEZAS DE DISEÑO (reutilizan los componentes del cuestionario)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PasoTutorial(numero: Int, onBack: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    PasoContainer(currentStep = numero, totalSteps = 4, onBack = onBack) {
        Spacer(Modifier.height(40.dp))
        TarjetaPaso(contenido)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun TarjetaPaso(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp), color = Color.White,
        tonalElevation = 4.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp), content = contenido)
    }
}

@Composable
private fun TituloPaso(antetitulo: String, titulo: String) {
    Text(antetitulo, fontSize = 12.sp, color = AyGris, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(6.dp))
    Text(titulo, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AyMoradoOscuro, lineHeight = 32.sp)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun FilaTutorial(numero: String, titulo: String, descripcion: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp).clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFEAE5F5), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(numero, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AyMoradoMedio)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(titulo, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = AyMoradoOscuro)
            Text(descripcion, fontSize = 12.sp, color = AyGrisClaro, lineHeight = 16.sp)
        }
    }
}