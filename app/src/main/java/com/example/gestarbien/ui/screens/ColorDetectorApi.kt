package com.example.gestarbien.ui.screens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Mapa de palabras clave → color directo (sin IA)
data class ColorResult(
    val detectado: Boolean,
    val nombre: String = "",
    val hex: String = "",
    val r: Int = 0,
    val g: Int = 0,
    val b: Int = 0,
    val confianza: Double = 0.0,
    val frase: String = ""
)
private val COLORES_EXACTOS = mapOf(
    "rojo"      to Triple("Rojo",    "#D32F2F", "El rojo habla de vitalidad y presencia."),
    "negro"     to Triple("Negro",   "#2C2C2C", "El negro evoca profundidad y silencio."),
    "blanco"    to Triple("Blanco",  "#F5F5F5", "El blanco representa un espacio limpio y abierto."),
    "verde"     to Triple("Verde",   "#388E3C", "El verde nos conecta con la naturaleza y el crecimiento."),
    "azul"      to Triple("Azul",    "#1976D2", "El azul transmite calma y confianza."),
    "amarillo"  to Triple("Amarillo","#FFEB3B", "El amarillo evoca calidez y claridad."),
    "naranja"   to Triple("Naranja", "#E64A19", "El naranja combina calidez y alegría."),
    "morado"    to Triple("Morado",  "#6A1B9A", "El morado se asocia con la intuición y espiritualidad."),
    "violeta"   to Triple("Morado",  "#6A1B9A", "El morado se asocia con la intuición y espiritualidad."),
    "rosa"      to Triple("Rosa",    "#E91E8C", "El rosa evoca ternura y cuidado."),
    "rosado"    to Triple("Rosa",    "#E91E8C", "El rosa evoca ternura y cuidado."),
    "gris"      to Triple("Gris",    "#757575", "El gris es equilibrio entre la luz y la sombra."),
    "celeste"   to Triple("Celeste", "#0288D1", "El celeste abre el espacio y calma la mente."),
    "lila"      to Triple("Lila",    "#8E24AA", "El lila es suave y relajante."),
    "turquesa"  to Triple("Turquesa","#00838F", "El turquesa equilibra calma y vitalidad."),
    "dorado"    to Triple("Dorado",  "#F9A825", "El dorado evoca calidez y valor."),
    "vino"      to Triple("Vino",    "#880E4F", "El vino es profundo y cálido."),
    "cafe"      to Triple("Café",    "#6D4C41", "El café es terroso y reconfortante."),
    "café"      to Triple("Café",    "#6D4C41", "El café es terroso y reconfortante."),
    "beige"     to Triple("Beige",   "#A1887F", "El beige es suave y discreto."),
    "lavanda"   to Triple("Lavanda", "#7986CB", "La lavanda se asocia con la calma y el descanso."),
    "lavender"  to Triple("Lavanda", "#7986CB", "La lavanda se asocia con la calma y el descanso."),
    "durazno"   to Triple("Durazno", "#FF7043", "El durazno combina calidez y suavidad."),
    "melocotón" to Triple("Durazno", "#FF7043", "El durazno combina calidez y suavidad."),
    "menta"     to Triple("Menta",   "#00897B", "La menta es fresca y ligera."),
    "coral"     to Triple("Coral",   "#FF5722", "El coral es cálido y vital."),
    "pink"      to Triple("Rosa",    "#E91E8C", "El rosa evoca ternura y cuidado.")
)

private fun hexARgb(hex: String): Triple<Int, Int, Int> {
    val h = hex.trimStart('#')
    return Triple(
        h.substring(0, 2).toInt(16),
        h.substring(2, 4).toInt(16),
        h.substring(4, 6).toInt(16)
    )
}

suspend fun detectarColorDesdeIA(mensaje: String): ColorResult = withContext(Dispatchers.IO) {

    // ── 1. Detección directa por palabra exacta (sin IA) ──────────────
    val lower = mensaje.trim().lowercase()
    for ((palabra, datos) in COLORES_EXACTOS) {
        // Busca la palabra como token completo (evita "rosa" dentro de "rosado" cruzado)
        val regex = Regex("\\b${Regex.escape(palabra)}\\b")
        if (regex.containsMatchIn(lower)) {
            val (nombre, hex, frase) = datos
            val (r, g, b) = hexARgb(hex)
            return@withContext ColorResult(
                detectado = true,
                nombre    = nombre,
                hex       = hex,
                r = r, g = g, b = b,
                confianza = 1.0,
                frase     = frase
            )
        }
    }

    // ── 2. Solo si no hay coincidencia exacta → llamar a la IA ────────
    try {
        val url  = URL("https://azaratthh-color-detectorr.hf.space/detectar-color")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput      = true
        conn.connectTimeout = 10000
        conn.readTimeout    = 15000

        val body = JSONObject().put("mensaje", mensaje).toString()
        conn.outputStream.write(body.toByteArray())

        val response = conn.inputStream.bufferedReader().readText()
        val json     = JSONObject(response)

        if (!json.getBoolean("detectado")) return@withContext ColorResult(detectado = false)

        ColorResult(
            detectado = true,
            nombre    = json.getString("nombre"),
            hex       = json.getString("hex"),
            r         = json.getInt("r"),
            g         = json.getInt("g"),
            b         = json.getInt("b"),
            confianza = json.getDouble("confianza"),
            frase     = json.getString("frase")
        )
    } catch (e: Exception) {
        ColorResult(detectado = false)
    }
}