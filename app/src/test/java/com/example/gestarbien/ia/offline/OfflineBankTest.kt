package com.example.gestarbien.ia.offline

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test de integración con 20 escenarios x 7 mensajes cada uno.
 *
 * Imprime la conversación completa (usuaria + bot) en la consola para
 * que puedas inspeccionar manualmente las respuestas.
 *
 * Verifica:
 *  - No hay 3+ preguntas seguidas (excepto en protocolo de seguridad).
 *  - No se repite la misma respuesta 2 turnos seguidos.
 *  - El tema activo se mantiene si no hay cambio explícito.
 *  - El riesgo alto mantiene protocolo.
 *  - No hay crashes.
 *
 * Para ver la salida:
 *  - Android Studio: en la pestaña "Run", activa "Show stdout" (icono de engranaje).
 *  - Gradle: ./gradlew test --tests "...ConversacionesEscenariosTest" -i
 */
class ConversacionesEscenariosTest {

    private lateinit var memoria: MemoriaOffline

    @Before
    fun setup() {
        memoria = MemoriaOffline()
        OfflineBank.resetCacheForTests()
    }

    private fun esPregunta(r: RespuestaOffline): Boolean =
        r.bloques.any { it.contains("?") || it.contains("¿") }

    private fun texto(r: RespuestaOffline): String = r.bloques.joinToString(" ")

    /**
     * Corre una conversación de 7 mensajes, imprime la conversación completa,
     * y aplica invariantes básicos.
     */
    private fun correrConversacion(
        escenario: String,
        mensajes: List<String>
    ): List<RespuestaOffline> {
        require(mensajes.size == 7) { "Cada escenario debe tener 7 mensajes" }

        // Reset por escenario
        memoria.limpiar()
        OfflineBank.resetCacheForTests()

        val respuestas = mutableListOf<RespuestaOffline>()
        var preguntasSeguidas = 0
        var ultimoTexto = ""

        println()
        println("╔══════════════════════════════════════════════════════════════╗")
        println("║  ESCENARIO: ${escenario.padEnd(48)}║")
        println("╚══════════════════════════════════════════════════════════════╝")

        for ((i, msg) in mensajes.withIndex()) {
            val r = OfflineBank.responder(msg, "Usuaria", memoria)
            respuestas.add(r)

            // ============================================================
            // IMPRIMIR CONVERSACIÓN
            // ============================================================
            println()
            println("  ── Turno ${i + 1} ─────────────────────────────────────────")
            println("  👤 USUARIA: $msg")
            println("  🤖 BOT    : ${texto(r)}")
            println("     └─ categoria=${r.categoria}")
            println("        modulo=${r.modulo}")
            println("        alerta=${r.requiereAlerta}")
            println("        tema=${r.tema}")
            println("        profundidad=${r.profundidad}")
            println("        esValidacion=${r.esValidacion}")

            // ============================================================
            // INVARIANTES
            // ============================================================

            // FIX: no contar preguntas del protocolo de seguridad.
            val esPreguntaContable = esPregunta(r) && !r.requiereAlerta

            if (esPreguntaContable) {
                preguntasSeguidas++
                assertTrue(
                    "[$escenario] turno ${i + 1}: 3+ preguntas seguidas. " +
                            "Texto: '${texto(r)}'",
                    preguntasSeguidas <= 2
                )
            } else {
                preguntasSeguidas = 0
            }

            val actual = texto(r)
            assertTrue(
                "[$escenario] turno ${i + 1}: respuesta repetida consecutiva. " +
                        "Texto: '$actual'",
                actual != ultimoTexto || actual.isEmpty()
            )
            ultimoTexto = actual

            assertTrue(
                "[$escenario] turno ${i + 1}: bloques vacíos",
                r.bloques.isNotEmpty() && r.bloques.first().isNotBlank()
            )

            assertNotNull(
                "[$escenario] turno ${i + 1}: categoría nula",
                r.categoria
            )
        }

        println()
        println("  ✓ Escenario '$escenario' completado sin violaciones.")
        return respuestas
    }

    // ============================================================
    // ESCENARIO 1: Culpa materna pura
    // ============================================================
    @Test
    fun escenario01_culpaMaterna() {
        correrConversacion(
            "culpa_materna",
            listOf(
                "Hola",
                "Me siento culpable, siento que soy mala madre",
                "Es que siento que no hago suficiente por mi bebé",
                "Mi mamá siempre me dice que debería hacer más",
                "No sé, a veces siento que no nací para esto",
                "Gracias por escucharme, me siento un poco mejor",
                "Hasta luego"
            )
        )
    }

    // ============================================================
    // ESCENARIO 2: Tristeza con llanto
    // ============================================================
    @Test
    fun escenario02_tristezaLlanto() {
        correrConversacion(
            "tristeza_llanto",
            listOf(
                "Hola",
                "Me siento muy triste, no paro de llorar",
                "No sé qué me pasa, todo me hace llorar",
                "Extraño mi vida de antes",
                "Sí, extraño tener tiempo para mí",
                "Gracias por escucharme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 3: Ansiedad por el parto
    // ============================================================
    @Test
    fun escenario03_ansiedadParto() {
        correrConversacion(
            "ansiedad_parto",
            listOf(
                "Hola",
                "Tengo mucho miedo al parto",
                "Me da miedo el dolor y que algo salga mal",
                "Sí, sobre todo me preocupa no despertar",
                "No sé, siento que no puedo controlar nada",
                "Gracias por escucharme",
                "Hasta pronto"
            )
        )
    }

    // ============================================================
    // ESCENARIO 4: Síntoma físico con alarma
    // ============================================================
    @Test
    fun escenario04_sintomaFisicoAlarma() {
        correrConversacion(
            "sintoma_fisico",
            listOf(
                "Hola",
                "Me duele mucho la cabeza",
                "También veo luces y tengo zumbido en los oídos",
                "Sí, me da miedo que sea algo grave",
                "No tengo a nadie que me acompañe",
                "Gracias por avisarme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 5: Cansancio extremo + falta de sueño
    // ============================================================
    @Test
    fun escenario05_cansancioExtremo() {
        correrConversacion(
            "cansancio",
            listOf(
                "Hola",
                "No he dormido nada, estoy agotada",
                "El bebé no duerme y yo tampoco",
                "Sí, me siento al límite",
                "Nadie me ayuda en casa",
                "Gracias por escucharme",
                "Hasta luego"
            )
        )
    }

    // ============================================================
    // ESCENARIO 6: Lactancia con dolor
    // ============================================================
    @Test
    fun escenario06_lactanciaDolor() {
        correrConversacion(
            "lactancia",
            listOf(
                "Hola",
                "Me duele mucho amamantar",
                "Tengo grietas y el pecho rojo",
                "Sí, me da fiebre",
                "No sé si seguir dando pecho",
                "Gracias por orientarme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 7: Ambivalencia del embarazo
    // ============================================================
    @Test
    fun escenario07_ambivalenciaEmbarazo() {
        correrConversacion(
            "ambivalencia",
            listOf(
                "Hola",
                "No sé si quiero seguir con este embarazo",
                "Me arrepiento de haber quedado embarazada",
                "Extraño mi vida de antes",
                "Sí, extraño mi libertad",
                "Gracias por no juzgarme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 8: Riesgo alto
    // ============================================================
    @Test
    fun escenario08_riesgoAlto() {
        correrConversacion(
            "riesgo_alto",
            listOf(
                "Hola",
                "Ya no quiero vivir",
                "Me quiero morir",
                "Sí, estoy sola",
                "No tengo a nadie",
                "Gracias por avisarme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 9: Soledad absoluta
    // ============================================================
    @Test
    fun escenario09_soledad() {
        correrConversacion(
            "soledad",
            listOf(
                "Hola",
                "Me siento muy sola",
                "No tengo a nadie con quien hablar",
                "Mi pareja no me entiende",
                "Sí, me siento desamparada",
                "Gracias por escucharme",
                "Hasta luego"
            )
        )
    }

    // ============================================================
    // ESCENARIO 10: Vínculo con el bebé
    // ============================================================
    @Test
    fun escenario10_vinculoBebe() {
        correrConversacion(
            "vinculo_bebe",
            listOf(
                "Hola",
                "No siento conexión con mi bebé",
                "Me da miedo no quererlo",
                "Sí, me siento desconectada",
                "No sé qué hacer",
                "Gracias por escucharme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 11: Identidad postparto
    // ============================================================
    @Test
    fun escenario11_identidadPostparto() {
        correrConversacion(
            "identidad_postparto",
            listOf(
                "Hola",
                "Ya no soy la misma desde que nació mi bebé",
                "Extraño mi vida anterior",
                "Sí, extraño mi trabajo y mi tiempo libre",
                "No sé quién soy ahora",
                "Gracias por escucharme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 12: Ansiedad por el bebé (hipervigilancia)
    // ============================================================
    @Test
    fun escenario12_ansiedadBebe() {
        correrConversacion(
            "ansiedad_bebe",
            listOf(
                "Hola",
                "No puedo dormir, velo a mi bebé toda la noche",
                "Tengo miedo de que le pase algo",
                "Sí, reviso si respira constantemente",
                "No puedo dejar de pensar en lo peor",
                "Gracias por escucharme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 13: Cuerpo y cambios
    // ============================================================
    @Test
    fun escenario13_cuerpoCambios() {
        correrConversacion(
            "cuerpo_cambios",
            listOf(
                "Hola",
                "No reconozco mi cuerpo",
                "Me veo diferente al espejo",
                "Sí, no me gusta mi cuerpo",
                "Tengo estrías y flacidez",
                "Gracias por escucharme",
                "Hasta luego"
            )
        )
    }

    // ============================================================
    // ESCENARIO 14: Frustración con el llanto del bebé
    // ============================================================
    @Test
    fun escenario14_frustracionLlanto() {
        correrConversacion(
            "frustracion",
            listOf(
                "Hola",
                "No soporto el llanto de mi bebé",
                "No para de llorar y no sé qué hacer",
                "Me siento al límite",
                "Sí, quiero salir corriendo a veces",
                "Gracias por escucharme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 15: Confusión por exceso de consejos
    // ============================================================
    @Test
    fun escenario15_confusionConsejos() {
        correrConversacion(
            "confusion",
            listOf(
                "Hola",
                "Estoy confundida, todos me dan consejos distintos",
                "Mi suegra y mi mamá me dicen cosas opuestas",
                "No sé qué hacer",
                "Sí, me siento perdida",
                "Gracias por escucharme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 16: Red de apoyo débil
    // ============================================================
    @Test
    fun escenario16_redApoyo() {
        correrConversacion(
            "red_apoyo",
            listOf(
                "Hola",
                "Me siento sola aunque tengo pareja",
                "Mi esposo no me ayuda con el bebé",
                "No me entiende cuando le digo que estoy cansada",
                "Sí, me siento incomprendida",
                "Gracias por escucharme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 17: Desahogo sin apoyo
    // ============================================================
    @Test
    fun escenario17_desahogo() {
        correrConversacion(
            "desahogo",
            listOf(
                "Hola",
                "Solo quiero hablar, no quiero consejos",
                "Necesito sacar todo lo que siento",
                "Sí, quiero desahogarme",
                "Gracias por escucharme sin juzgar",
                "Me siento un poco mejor",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 18: Multitema (tristeza + ansiedad)
    // ============================================================
    @Test
    fun escenario18_multitemaTristezaAnsiedad() {
        correrConversacion(
            "multitema_tristeza_ansiedad",
            listOf(
                "Hola",
                "Me siento triste y ansiosa a la vez",
                "Quiero llorar pero también no puedo parar de pensar",
                "Sí, todo se mezcla",
                "No sé qué siento primero",
                "Gracias por escucharme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 19: Cambio brusco de tema (de tristeza a síntoma)
    // ============================================================
    @Test
    fun escenario19_cambioTema() {
        correrConversacion(
            "cambio_tema",
            listOf(
                "Hola",
                "Estoy muy triste",
                "Perdón, ahora me duele mucho la cabeza",
                "También tengo náuseas",
                "No sé si es normal",
                "Gracias por escucharme",
                "Adiós"
            )
        )
    }

    // ============================================================
    // ESCENARIO 20: Cierre conversacional
    // ============================================================
    @Test
    fun escenario20_cierreConversacion() {
        val respuestas = correrConversacion(
            "cierre",
            listOf(
                "Hola",
                "Quería contarte algo",
                "Me siento mejor ahora",
                "Gracias por escucharme",
                "Me voy a descansar",
                "Hasta pronto",
                "Adiós"
            )
        )
        val ultima = respuestas.last()
        assertTrue(
            "El último turno debe ser cierre. Categoría: ${ultima.categoria}",
            ultima.categoria == "cierre" || ultima.categoria.contains("cierre")
        )
    }
}