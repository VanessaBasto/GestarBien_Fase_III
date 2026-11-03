package com.example.gestarbien.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Abre WhatsApp con un mensaje precargado hacia el número dado.
 * numero debe venir en formato E.164 sin '+' (ej: 573001234567),
 * que es como se guarda en ProfesionalPerfil.whatsapp.
 *
 * IMPORTANTE: esto abre la app de WhatsApp con el chat ya armado,
 * pero es la usuaria quien decide tocar "Enviar". No es un envío
 * automático ni silencioso — eso requeriría WhatsApp Business API
 * (Twilio/Meta), que es infraestructura de servidor aparte.
 */
fun abrirWhatsApp(context: Context, numero: String, mensaje: String) {
    val numeroLimpio = numero.filter { it.isDigit() }
    val mensajeCodificado = Uri.encode(mensaje)
    val uri = Uri.parse("https://wa.me/$numeroLimpio?text=$mensajeCodificado")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}