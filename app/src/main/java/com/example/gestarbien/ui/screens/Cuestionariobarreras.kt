package com.example.gestarbien.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Barreras y facilitadores de acceso a la atención en salud mental.
 *
 * Versión corta: 4 preguntas con lógica condicional, para que la usuaria
 * solo responda lo que realmente le aplica.
 *
 *   P1  ¿Necesitaste apoyo emocional y lograste recibirlo?   (filtro)
 *   P2  ¿Cuál fue el mayor obstáculo?                         (si P1 = con dificultades / no pudo)
 *   P3  ¿Te sentiste cómoda y escuchada?                      (si P1 = sí recibió atención)
 *   P4  ¿Qué te facilitaría recibir apoyo?                    (si necesitó apoyo)
 */

// ── Textos de P1 (se guardan tal cual en Firestore) ─────────────────────────
private const val ACCESO_SIN_PROBLEMA   = "Sí, sin problema"
private const val ACCESO_CON_DIFICULTAD = "Sí, pero tuve muchas dificultades para conseguirlo"
private const val ACCESO_NO_PUDO        = "Lo necesité, pero no busqué o no pude acceder"
private const val ACCESO_NO_NECESITO    = "No lo necesité"

// ── Textos de P3 ────────────────────────────────────────────────────────────
private const val EXP_COMPLETA = "Sí, completamente"
private const val EXP_PARCIAL  = "Más o menos"
private const val EXP_NEGATIVA = "No, me sentí juzgada o ignorada"

data class BarrerasAcceso(
    val acceso: String = "",                                   // P1
    val obstaculos: List<String> = emptyList(),                // P2: claves (personal, familiar, dinero, tramites, trato, digital, otro)
    val obstaculoOtro: String = "",                            // P2: texto libre si eligió "otro"
    val experiencia: String = "",                              // P3
    val facilitadores: List<String> = emptyList(),             // P4
    val comentario: String = ""                                // se conserva por compatibilidad; el comentario final va en el Paso 5
) {
    val pideObstaculos: Boolean
        get() = acceso == ACCESO_CON_DIFICULTAD || acceso == ACCESO_NO_PUDO

    val pideExperiencia: Boolean
        get() = acceso == ACCESO_SIN_PROBLEMA || acceso == ACCESO_CON_DIFICULTAD

    val pideFacilitadores: Boolean
        get() = acceso.isNotBlank() && acceso != ACCESO_NO_NECESITO

    /** El paso está completo cuando respondió todo lo que le aplicaba. */
    val todasRespondidas: Boolean
        get() = acceso.isNotBlank() &&
                (!pideObstaculos || (obstaculos.isNotEmpty() && ("otro" !in obstaculos || obstaculoOtro.isNotBlank()))) &&
                (!pideExperiencia || experiencia.isNotBlank())

    val hayAlguna: Boolean get() = obstaculos.isNotEmpty()

    /** Señala al profesional que conviene revisar el caso. */
    val requiereAlerta: Boolean
        get() = acceso == ACCESO_NO_PUDO || "familiar" in obstaculos || experiencia == EXP_NEGATIVA

    val motivoAlerta: String
        get() = when {
            "familiar" in obstaculos ->
                "Reporta falta de apoyo, control o impedimentos familiares para acceder a la atención"
            acceso == ACCESO_NO_PUDO ->
                "Necesitó apoyo emocional pero no logró acceder a la atención"
            experiencia == EXP_NEGATIVA ->
                "Se sintió juzgada o ignorada en la atención recibida"
            else -> ""
        }
}

private data class Obstaculo(val clave: String, val titulo: String, val descripcion: String)

private val OBSTACULOS = listOf(
    Obstaculo("personal", "Personales o emocionales",
        "Me daba pena, temía que me juzgaran o creí que podía sola"),
    Obstaculo("familiar", "Familiares o de cuidado",
        "Falta de apoyo en casa, nadie que cuidara al bebé, o alguien me lo prohibía o controlaba"),
    Obstaculo("dinero", "Dinero o transporte",
        "Costos altos, distancia o falta de transporte"),
    Obstaculo("tramites", "Trámites o citas (EPS)",
        "Demoras largas, citas canceladas o papeleo difícil"),
    Obstaculo("trato", "Trato del servicio",
        "Sentí maltrato, desinterés o falta de respeto del personal de salud"),
    Obstaculo("digital", "Atención virtual",
        "Problemas con internet, señal o no saber usar la plataforma")
)

private val OPCIONES_ACCESO = listOf(
    ACCESO_SIN_PROBLEMA, ACCESO_CON_DIFICULTAD, ACCESO_NO_PUDO, ACCESO_NO_NECESITO
)

private val OPCIONES_EXPERIENCIA = listOf(EXP_COMPLETA, EXP_PARCIAL, EXP_NEGATIVA)

private val OPCIONES_FACILITADORES = listOf(
    "Horarios más flexibles o virtuales",
    "Atención gratuita o económica",
    "Tener con quién dejar a los niños",
    "Información clara de a dónde acudir",
    "Un trato más cálido y humano"
)

// ─────────────────────────────────────────────────────────────
//  PANTALLA
// ─────────────────────────────────────────────────────────────
@Composable
fun CuestionarioBarreras(
    barreras: BarrerasAcceso,
    onUpdate: (BarrerasAcceso) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    /** Al cambiar P1 se limpia lo que deja de aplicar, para no guardar respuestas viejas. */
    fun cambiarAcceso(nuevo: String) {
        val base = barreras.copy(acceso = nuevo)
        onUpdate(
            base.copy(
                obstaculos    = if (base.pideObstaculos) base.obstaculos else emptyList(),
                obstaculoOtro = if (base.pideObstaculos) base.obstaculoOtro else "",
                experiencia   = if (base.pideExperiencia) base.experiencia else "",
                facilitadores = if (base.pideFacilitadores) base.facilitadores else emptyList()
            )
        )
    }

    PasoContainer(currentStep = 4, totalSteps = 5, onBack = onBack) {
        Spacer(modifier = Modifier.height(40.dp))
        Surface(
            shape = RoundedCornerShape(24.dp), color = Color.White,
            tonalElevation = 4.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {

                Text("Paso 4 de 5", fontSize = 12.sp,
                    color = Color(0xFF6B6B8A), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Barreras de acceso", fontSize = 26.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFF3A3D8C))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Unas pocas preguntas sobre cómo ha sido recibir apoyo emocional. " +
                            "Solo responderás las que te apliquen.",
                    fontSize = 13.sp, color = Color(0xFF6B6B8A), lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── P1 · Necesidad y acceso (filtro) ─────────
                PreguntaCard {
                    SeccionLabel(
                        text = "Durante este embarazo o desde el nacimiento de tu bebé, " +
                                "¿sentiste la necesidad de apoyo emocional y lograste recibirlo?"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OPCIONES_ACCESO.forEachIndexed { i, opcion ->
                        OpcionDetalle(
                            titulo = opcion, descripcion = "",
                            selected = barreras.acceso == opcion,
                            onClick = { cambiarAcceso(opcion) }
                        )
                        if (i < OPCIONES_ACCESO.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // ── P2 · Obstáculos ──────────────────────────
                if (barreras.pideObstaculos) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        SeccionLabel(text = "¿Cuál fue el mayor obstáculo para recibir la ayuda que necesitabas?")
                        Spacer(modifier = Modifier.height(4.dp))
                        AyudaSeleccion(text = "(Puedes escoger más de uno)")
                        Spacer(modifier = Modifier.height(10.dp))

                        OBSTACULOS.forEach { o ->
                            val activo = o.clave in barreras.obstaculos
                            OpcionDetalle(
                                titulo = o.titulo, descripcion = o.descripcion, selected = activo,
                                onClick = {
                                    val nueva = if (activo) barreras.obstaculos - o.clave
                                    else barreras.obstaculos + o.clave
                                    onUpdate(barreras.copy(obstaculos = nueva))
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        val otroActivo = "otro" in barreras.obstaculos
                        OpcionChip(
                            text = "Otro", selected = otroActivo,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (otroActivo) {
                                    onUpdate(barreras.copy(obstaculos = barreras.obstaculos - "otro", obstaculoOtro = ""))
                                } else {
                                    onUpdate(barreras.copy(obstaculos = barreras.obstaculos + "otro"))
                                }
                            }
                        )
                        if (otroActivo) {
                            Spacer(modifier = Modifier.height(8.dp))
                            CampoTexto(
                                label = "", value = barreras.obstaculoOtro,
                                onValueChange = { onUpdate(barreras.copy(obstaculoOtro = it)) },
                                placeholder = "Escribe aquí...", singleLine = false, maxLines = 2
                            )
                        }

                        // Si marcó control o prohibición familiar, es una señal de seguridad
                        if ("familiar" in barreras.obstaculos) {
                            Spacer(modifier = Modifier.height(4.dp))
                            AvisoApoyo(
                                "Si en algún momento sientes que alguien te controla o te impide buscar " +
                                        "ayuda, puedes llamar a la Línea 155. Es gratuita y confidencial."
                            )
                        }
                    }
                }

                // ── P3 · Experiencia con la atención recibida ─
                if (barreras.pideExperiencia) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        SeccionLabel(text = "Cuando recibiste la atención, ¿te sentiste cómoda y escuchada?")
                        Spacer(modifier = Modifier.height(10.dp))
                        FilaOpciones(
                            opciones = OPCIONES_EXPERIENCIA,
                            seleccionado = barreras.experiencia,
                            onSeleccionar = { onUpdate(barreras.copy(experiencia = it)) }
                        )
                    }
                }

                // ── P4 · Facilitadores ───────────────────────
                if (barreras.pideFacilitadores) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        SeccionLabel(text = "¿Qué te facilitaría recibir apoyo en salud mental?")
                        Spacer(modifier = Modifier.height(4.dp))
                        AyudaSeleccion(text = "(Puedes escoger más de uno)")
                        Spacer(modifier = Modifier.height(10.dp))
                        CheckboxMultiple(
                            opciones = OPCIONES_FACILITADORES,
                            seleccionados = barreras.facilitadores,
                            onToggle = { opcion ->
                                val nueva = if (opcion in barreras.facilitadores) barreras.facilitadores - opcion
                                else barreras.facilitadores + opcion
                                onUpdate(barreras.copy(facilitadores = nueva))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                BotonContinuar(
                    texto      = "Continuar",
                    habilitado = barreras.todasRespondidas,
                    onClick    = onContinue
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Opción de ancho completo para respuestas largas o con explicación.
 * Usa exactamente el mismo estilo que OpcionChip (fondo lavanda suave al
 * seleccionar, borde gris fino sin seleccionar, 13sp) para que esta pantalla
 * se vea igual que los pasos 1 a 3.
 */
@Composable
private fun OpcionDetalle(
    titulo: String,
    descripcion: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFB1B4E1).copy(alpha = 0.2f) else Color.White)
            .border(
                width = if (selected) 0.dp else 0.5.dp,
                color = if (selected) Color.Transparent else Color(0xFFC4C4C4),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titulo, fontSize = 13.sp, lineHeight = 16.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = Color(0xFF2D2D2D)
        )
        if (descripcion.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = descripcion, fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF9999BB))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  GUARDADO EN FIRESTORE
//  Se guarda como documento propio, separado del cuestionario
//  psicosocial, para que el profesional y el módulo admin puedan
//  leerlo sin cargar todo el resto de la caracterización.
// ─────────────────────────────────────────────────────────────
fun BarrerasAcceso.aMapaFirestore(): Map<String, Any?> = mapOf(
    "acceso"        to acceso,
    "obstaculos"    to obstaculos,
    "obstaculoOtro" to obstaculoOtro,
    "experiencia"   to experiencia,
    "facilitadores" to facilitadores,
    "comentario"    to comentario,
    "fechaMs"       to System.currentTimeMillis()
)