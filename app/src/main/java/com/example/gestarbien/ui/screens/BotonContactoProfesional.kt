package com.example.gestarbien.ui.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.util.abrirWhatsApp

/**
 * Chequeo de conectividad en el momento en que se muestra el botón (no al
 * cargar el chat). whatsappProfesionalAsignado en ChatbotScreen.kt se carga
 * una sola vez, con la conexión que había al abrir la pantalla, y queda
 * cacheado en memoria durante toda la sesión. Sin este chequeo, si la
 * usuaria se queda sin conexión después de eso, este botón seguiría
 * ofreciendo "Escribir por WhatsApp" aunque no se pueda abrir sin internet.
 */
private fun hayConexion(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

private val BordeRojoContacto = Color(0xFFFFB3B3)
private val TituloContacto    = Color(0xFF333355)
private val SubtextoContacto  = Color(0xFF888888)

private val VerdeSuaveFondo = Color(0xFFE3F6EA)
private val VerdeSuaveBorde = Color(0xFFB6E3C6)
private val VerdeSuaveTexto = Color(0xFF1F7A4D)

/**
 * Se muestra como una burbuja más del chat (con el muñequito, como si fuera
 * un mensaje del bot). El único indicio visual de que pide atención especial
 * es el borde rojo.
 *
 * Lógica:
 *  - Hay profesional asignado Y hay conexión ahora mismo → ofrece escribirle por WhatsApp.
 *  - Hay profesional asignado pero SIN conexión ahora mismo → explica que no se
 *    puede abrir WhatsApp sin internet y ofrece Línea 106.
 *  - No hay profesional asignado → ofrece Línea 106 directamente.
 *
 * Nunca reemplaza el protocolo de seguridad (Línea 106 / 123).
 */
@Composable
fun BotonContactoProfesional(
    nombreProfesional: String?,
    whatsappProfesional: String?,
    botColor: Color,
    mostrarMascota: Boolean = true
) {
    val context = LocalContext.current

    val forma = if (mostrarMascota)
        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    else
        RoundedCornerShape(18.dp)

    val tieneProfesionalAsignado = !whatsappProfesional.isNullOrBlank()
    val conectado    = hayConexion(context)
    val hayWhatsApp  = tieneProfesionalAsignado && conectado
    // Tiene profesional, pero no se le puede escribir ahora porque no hay
    // internet para abrir WhatsApp: caso distinto de "no tiene profesional".
    val sinConexionConProfesional = tieneProfesionalAsignado && !conectado

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment     = Alignment.Bottom
    ) {
        if (mostrarMascota) {
            BotMascota(color = botColor, hasText = false, size = 32.dp)
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape           = forma,
            color           = Color.White,
            modifier        = Modifier
                .widthIn(max = if (mostrarMascota) 280.dp else 300.dp)
                .border(1.5.dp, BordeRojoContacto, forma),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                if (hayWhatsApp) {
                    // ── MODO CON WHATSAPP DEL PROFESIONAL ──
                    Text(
                        text       = "¿Quieres escribirle a tu psicólogo asignado?",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TituloContacto,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "Si prefieres no hacerlo ahora, no hay problema, seguiré acompañándote aquí igual.",
                        fontSize   = 12.sp,
                        color      = SubtextoContacto,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VerdeSuaveFondo)
                            .border(1.dp, VerdeSuaveBorde, RoundedCornerShape(12.dp))
                            .clickable {
                                abrirWhatsApp(
                                    context,
                                    whatsappProfesional!!,
                                    "Hola, soy una usuaria de GestarBien y me gustaría hablar contigo."
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = VerdeSuaveTexto, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Escribir por WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VerdeSuaveTexto)
                    }

                } else if (sinConexionConProfesional) {
                    // ── MODO SIN CONEXIÓN, CON PROFESIONAL ASIGNADO ──
                    // Tiene a quién escribirle, pero no hay internet para
                    // abrir WhatsApp ahora mismo — se lo decimos explícito
                    // en vez de ofrecer un botón que no va a funcionar.
                    Text(
                        text       = "No tienes conexión en este momento",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TituloContacto,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "Sin internet no podemos abrirte WhatsApp para contactar a tu profesional. Te sugerimos llamar a la Línea 106 de Salud Mental en Colombia. Es gratuita, confidencial y está disponible 24/7.",
                        fontSize   = 12.sp,
                        color      = SubtextoContacto,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    BotonLlamarLinea106(context)
                } else {
                    // ── MODO SIN PROFESIONAL ASIGNADO ──
                    Text(
                        text       = "Si en este momento no tienes conexión",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TituloContacto,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "Puedes llamar a la Línea 106 de Salud Mental en Colombia. Es gratuita, confidencial y está disponible 24/7.",
                        fontSize   = 12.sp,
                        color      = SubtextoContacto,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    BotonLlamarLinea106(context)
                }
            }
        }
    }
}

/** Botón "Llamar a la Línea 106", compartido por los dos modos sin WhatsApp. */
@Composable
private fun BotonLlamarLinea106(context: android.content.Context) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(VerdeSuaveFondo)
            .border(1.dp, VerdeSuaveBorde, RoundedCornerShape(12.dp))
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:106"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Call, contentDescription = null, tint = VerdeSuaveTexto, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Llamar a la Línea 106", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VerdeSuaveTexto)
    }
}