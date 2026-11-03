package com.example.gestarbien.data
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.example.gestarbien.data.ProfesionalesRepository
import kotlinx.coroutines.launch
// ─────────────────────────────────────────────────────────────────────────────
//  MODELOS
// ─────────────────────────────────────────────────────────────────────────────

data class NivelRiesgo(val label: String, val cls: String)

data class PuntajePsicometrico(val puntaje: Int, val max: Int)

/** Un puntaje individual con su fecha, para armar el historial comparativo. */
data class PuntajeConFecha(val puntaje: Int, val fechaMs: Long)

data class FactorPsicosocial(val nombre: String, val nivel: String)

data class PreguntaCuestionario(
    val pregunta: String,
    val respuesta: String,
    val nivelRespuesta: String   // "alto" | "moderado" | "leve" | "neutro"
)

data class SeccionCuestionario(
    val titulo: String,
    val icono: ImageVector,
    val iconoBg: Color,
    val iconoColor: Color,
    val nivelGeneral: String,    // "alto" | "moderado" | "bajo"
    val preguntas: List<PreguntaCuestionario>
)

data class Paciente(
    val id: Int,
    val uid: String,
    val nombre: String,
    val telefono: String = "",
    val semanas: Int,
    val edadAnios: Int,
    val ultimaSesion: String,
    val epds: PuntajePsicometrico?,
    val pass: PuntajePsicometrico?,
    val historialEpds: List<PuntajeConFecha> = emptyList(),
    val historialPass: List<PuntajeConFecha> = emptyList(),
    val esPostparto: Boolean,
    val factoresPsico: List<FactorPsicosocial>,
    val cuestionarioSecciones: List<SeccionCuestionario>,
    val barreras: BarrerasPacienteUI? = null,
    /** Señales de atención derivadas del cuestionario y de las barreras (ver mapearAlertas). */
    val alertas: List<AlertaClinica> = emptyList()
) {
    val tieneCuestionario: Boolean get() = cuestionarioSecciones.isNotEmpty()

    /** Nivel según EPDS y PASS únicamente (el peor de los dos). */
    fun nivelPorEscalas(): NivelRiesgo {
        if (epds == null && pass == null) return NivelRiesgo("INCOMPLETO", "incompleto")
        val e = epds?.let { nivelEPDS(it.puntaje).cls } ?: "leve"
        val p = pass?.let { nivelPASS(it.puntaje).cls } ?: "leve"
        val peor = when {
            listOf(e, p).any { it == "alto" }  -> "alto"
            listOf(e, p).any { it == "medio" } -> "medio"
            else                               -> "leve"
        }
        return NivelRiesgo(mapOf("alto" to "ALTO", "medio" to "MEDIO", "leve" to "LEVE")[peor]!!, peor)
    }

    /**
     * Nivel final que ve el profesional: parte del nivel por escalas y, si hay
     * alguna señal de atención ALTA (violencia reciente, pide ayuda emocional),
     * sube un escalón (LEVE -> MEDIO, MEDIO -> ALTO). Si aún no hay EPDS ni PASS
     * se mantiene INCOMPLETO; las señales igual se muestran en el detalle.
     */
    fun nivelGlobal(): NivelRiesgo {
        val base = nivelPorEscalas()
        if (base.cls == "incompleto" || alertas.none { it.nivel == "alto" }) return base
        return when (base.cls) {
            "leve"  -> NivelRiesgo("MEDIO", "medio")
            "medio" -> NivelRiesgo("ALTO", "alto")
            else    -> base
        }
    }

    val subioPorAlertas: Boolean get() = nivelGlobal().cls != nivelPorEscalas().cls
}

/** Señal que el profesional debe revisar. nivel: "alto" | "medio". */
data class AlertaClinica(val texto: String, val nivel: String)

data class Profesional(val id: Int, val correo: String, val nombre: String)

// ─────────────────────────────────────────────────────────────────────────────
//  LÓGICA PSICOMÉTRICA
// ─────────────────────────────────────────────────────────────────────────────

fun nivelEPDS(puntaje: Int): NivelRiesgo = when {
    puntaje >= 13 -> NivelRiesgo("ALTO", "alto")
    puntaje >= 10 -> NivelRiesgo("MEDIO", "medio")
    else          -> NivelRiesgo("LEVE", "leve")
}

fun nivelPASS(puntaje: Int): NivelRiesgo = when {
    puntaje >= 42 -> NivelRiesgo("ALTO", "alto")   // grave: 42-93
    puntaje >= 21 -> NivelRiesgo("MODERADO", "medio")
    else          -> NivelRiesgo("LEVE", "leve")
}

// ─────────────────────────────────────────────────────────────────────────────
//  MAPEADORES: documento Firestore → modelos de UI
// ─────────────────────────────────────────────────────────────────────────────

fun mapearFactoresPsico(doc: Map<String, Any>): List<FactorPsicosocial> {
    val lista = mutableListOf<FactorPsicosocial>()

    // Situación socioeconómica ← ingresosMensuales + contrato
    val ingresos = doc["ingresosMensuales"] as? String ?: ""
    val contrato = doc["tipoContrato"] as? String ?: ""
    val nivelEco = when {
        ingresos.contains("Menos de 1") || contrato == "No aplica" -> "alto"
        ingresos == "1 SMLV" || contrato in listOf("Por días", "Por horas") -> "moderado"
        else -> "leve"
    }
    lista.add(FactorPsicosocial("Situación socioeconómica", nivelEco))

    // Antecedentes de violencia ← víctima física / psicológica / abuso
    val violencias = listOf(
        doc["victimaFisico"]      as? String ?: "No",
        doc["victimaPsicologico"] as? String ?: "No",
        doc["victimaAbuso"]       as? String ?: "No"
    )
    val nivelViol = when {
        violencias.any { it == "Sí" }           -> "alto"
        violencias.any { it == "En el pasado" } -> "moderado"
        else                                    -> "leve"
    }
    lista.add(FactorPsicosocial("Antecedentes de violencia", nivelViol))

    // Vulnerabilidad contextual ← estrato + tipo vivienda
    val estrato = (doc["estrato"] as? String ?: "3").toIntOrNull() ?: 3
    val vivienda = doc["tipoVivienda"] as? String ?: ""
    val nivelVul = when {
        estrato <= 1 || vivienda == "Otra" -> "alto"
        estrato == 2                       -> "moderado"
        else                              -> "leve"
    }
    lista.add(FactorPsicosocial("Vulnerabilidad contextual", nivelVul))

    // Red de apoyo ← redDeApoyo + relaciones
    val red = doc["redDeApoyo"] as? String ?: ""
    val relFam = doc["relacionesFamiliares"] as? String ?: ""
    val fuentes = red.split(",").count { it.isNotBlank() }
    val nivelRed = when {
        fuentes == 0 || red == "Ninguna"
                || relFam in listOf("Malas", "Conflictivas") -> "alto"
        fuentes == 1
                || relFam in listOf("Regulares", "Distantes") -> "moderado"
        else -> "leve"
    }
    lista.add(FactorPsicosocial("Red de apoyo familiar/social", nivelRed))

    // Consumo de sustancias (propio y de la pareja)
    val consumoGen = doc["consumoSustancias"]  as? String ?: ""
    val alcohol    = doc["consumoAlcohol"]     as? String ?: ""
    val psicoact   = doc["consumoPsicoactivas"] as? String ?: ""
    val consParej  = doc["consumoPareja"]      as? String ?: ""
    val nivelCons = when {
        psicoact == "Sí"                                                  -> "alto"
        alcohol == "Sí" || consParej == "Sí" || consumoGen == "Sí"        -> "moderado"
        else                                                              -> "leve"
    }
    lista.add(FactorPsicosocial("Consumo de sustancias", nivelCons))

    // Ánimo en el último mes (D01 – D03)
    val tristeza   = doc["tristezaMesPasado"]    as? String ?: ""
    val pocoInt    = doc["pocoInteresMesPasado"] as? String ?: ""
    val necAyuda   = doc["necesitaAyuda"]        as? String ?: ""
    val nivelAnimo = when {
        necAyuda == "Sí"                      -> "alto"
        tristeza == "Sí" || pocoInt == "Sí"   -> "moderado"
        else                                  -> "leve"
    }
    lista.add(FactorPsicosocial("Síntomas de ánimo (último mes)", nivelAnimo))

    return lista
}

fun mapearSeccionesCuestionario(doc: Map<String, Any>): List<SeccionCuestionario> {
    val secciones = mutableListOf<SeccionCuestionario>()

    // ── 1. Situación socioeconómica ───────────────────────────────────────
    val ingresos    = doc["ingresosMensuales"]  as? String ?: "Sin dato"
    val vivienda    = doc["tipoVivienda"]       as? String ?: "Sin dato"
    val estrato     = doc["estrato"]            as? String ?: "Sin dato"
    val condiciones = (doc["condicionesVivienda"] as? List<*>)?.joinToString(", ") ?: "Sin dato"
    val ocupacion   = doc["ocupacion"]          as? String ?: "Sin dato"
    val contrato    = doc["tipoContrato"]       as? String ?: "Sin dato"

    val nivelEco = when {
        ingresos.contains("Menos de 1") || contrato == "No aplica" -> "alto"
        ingresos == "1 SMLV" -> "moderado"
        else -> "bajo"
    }
    secciones.add(SeccionCuestionario(
        titulo       = "Situación socioeconómica",
        icono        = Icons.Default.Work,
        // FIX: antes era naranja (iconoBg 0xFFFEF6E4 / iconoColor 0xFFE67E22)
        // y chocaba visualmente con otros elementos naranjas de la pantalla
        // (p.ej. "Riesgo alto" del panel). Ahora en tono morado.
        iconoBg      = Color(0xFFF3E8FF),
        iconoColor   = Color(0xFF8E44AD),
        nivelGeneral = nivelEco,
        preguntas    = listOf(
            PreguntaCuestionario("Ingresos mensuales",       ingresos,    nivelIngreso(ingresos)),
            PreguntaCuestionario("Tipo de contrato",         contrato,    nivelContrato(contrato)),
            PreguntaCuestionario("Tipo de vivienda",         vivienda,    "neutro"),
            PreguntaCuestionario("Estrato socioeconómico",   estrato,     nivelEstrato(estrato)),
            PreguntaCuestionario("Servicios disponibles",    condiciones, "neutro"),
            PreguntaCuestionario("Ocupación",                ocupacion,   "neutro"),
        )
    ))

    // ── 2. Antecedentes de violencia ──────────────────────────────────────
    val violGeneral = doc["violenciaGeneral"]  as? String ?: ""
    val vicFisico   = doc["victimaFisico"]      as? String ?: "Sin dato"
    val vicPsico    = doc["victimaPsicologico"] as? String ?: "Sin dato"
    val vicAbuso    = doc["victimaAbuso"]       as? String ?: "Sin dato"
    val violencias  = listOf(vicFisico, vicPsico, vicAbuso)
    val hayViolenciaSi = violencias.any { it == "Sí" }
    val nivelViol = when {
        hayViolenciaSi                          -> "alto"
        violencias.any { it == "En el pasado" } -> "moderado"
        else -> "bajo"
    }
    secciones.add(SeccionCuestionario(
        titulo       = "Antecedentes de violencia",
        icono        = Icons.Default.Shield,
        iconoBg      = Color(0xFFEAFAF1),
        iconoColor   = Color(0xFF27AE60),
        nivelGeneral = nivelViol,
        preguntas    = buildList {
            if (violGeneral.isNotBlank())
                add(PreguntaCuestionario("Violencia física, verbal, psicológica o emocional", violGeneral, if (violGeneral == "Sí") "alto" else "neutro"))
            add(PreguntaCuestionario("Violencia psicológica (último año)", vicPsico,  nivelViolencia(vicPsico)))
            add(PreguntaCuestionario("Violencia física (último año)",      vicFisico, nivelViolencia(vicFisico)))
            add(PreguntaCuestionario("Violencia sexual (último año)",      vicAbuso,  nivelViolencia(vicAbuso)))
            if (hayViolenciaSi)
                add(PreguntaCuestionario("Derivación sugerida", "Ruta Integral de Atención en Salud · Línea 155", "alto"))
        }
    ))

    // ── 2b. Consumo de sustancias ─────────────────────────────────────────
    val consumo     = doc["consumoSustancias"]   as? String ?: ""
    val consAlcohol = doc["consumoAlcohol"]      as? String ?: ""
    val consPsico   = doc["consumoPsicoactivas"] as? String ?: ""
    val consDetalle = doc["consumoDetalle"]      as? String ?: ""
    val consPareja  = doc["consumoPareja"]       as? String ?: ""
    if (consumo.isNotBlank()) {
        val nivelCons = when {
            consPsico == "Sí" -> "alto"
            consAlcohol == "Sí" || consPareja == "Sí" || (consumo == "Sí" && consAlcohol.isBlank()) -> "moderado"
            else -> "bajo"
        }
        secciones.add(SeccionCuestionario(
            titulo       = "Consumo de sustancias",
            icono        = Icons.Default.Warning,
            iconoBg      = Color(0xFFFFF4E0),
            iconoColor   = Color(0xFFD68910),
            nivelGeneral = nivelCons,
            preguntas    = buildList {
                add(PreguntaCuestionario("¿Ella o su pareja han consumido alguna sustancia?", consumo, if (consumo == "Sí") "moderado" else "leve"))
                if (consAlcohol.isNotBlank())
                    add(PreguntaCuestionario("Alcohol en los últimos meses", consAlcohol, if (consAlcohol == "Sí") "moderado" else "leve"))
                if (consPsico.isNotBlank())
                    add(PreguntaCuestionario("Sustancias psicoactivas (distintas del alcohol)", consPsico, if (consPsico == "Sí") "alto" else "leve"))
                if (consDetalle.isNotBlank())
                    add(PreguntaCuestionario("¿Cuál o cuáles?", consDetalle, "neutro"))
                if (consPareja.isNotBlank())
                    add(PreguntaCuestionario("Consumo de la pareja afecta su bienestar", consPareja, if (consPareja == "Sí") "moderado" else "leve"))
            }
        ))
    }

    // ── 2c. Antecedentes obstétricos ──────────────────────────────────────
    val embPrevio   = doc["embarazoPrevio"]     as? String ?: ""
    val embNumero   = doc["embarazos"]          as? String ?: ""
    val perdida     = doc["perdidaGestacional"] as? String ?: ""
    val abortosAnt  = doc["abortos"]            as? String ?: ""   // registros anteriores al cambio de cuestionario
    if (embPrevio.isNotBlank() || perdida.isNotBlank() || embNumero.isNotBlank() || abortosAnt.isNotBlank()) {
        secciones.add(SeccionCuestionario(
            titulo       = "Antecedentes obstétricos",
            icono        = Icons.Default.Favorite,
            iconoBg      = Color(0xFFFDEBF3),
            iconoColor   = Color(0xFFD6336C),
            nivelGeneral = if (perdida == "Sí") "moderado" else "bajo",
            preguntas    = buildList {
                if (embPrevio.isNotBlank())
                    add(PreguntaCuestionario("Embarazo previo a este embarazo o parto", embPrevio, "neutro"))
                if (embNumero.isNotBlank())
                    add(PreguntaCuestionario("Embarazos anteriores", embNumero, "neutro"))
                if (perdida.isNotBlank())
                    add(PreguntaCuestionario("Pérdida gestacional o perinatal", perdida, if (perdida == "Sí") "moderado" else "leve"))
                if (abortosAnt.isNotBlank())
                    add(PreguntaCuestionario("Abortos (registro anterior)", abortosAnt, "neutro"))
            }
        ))
    }

    // ── 3. Red de apoyo y relaciones ──────────────────────────────────────
    val red        = doc["redDeApoyo"]          as? String ?: "Sin dato"
    val relPareja  = doc["relacionPareja"]       as? String ?: "Sin dato"
    val relFam     = doc["relacionesFamiliares"] as? String ?: "Sin dato"
    val estadoCivil= doc["estadoCivil"]          as? String ?: "Sin dato"
    val fuentes    = red.split(",").count { it.isNotBlank() }
    val nivelRed   = when {
        fuentes == 0 || red == "Ninguna"
                || relFam in listOf("Malas","Conflictivas") -> "alto"
        fuentes == 1
                || relFam in listOf("Regulares","Distantes") -> "moderado"
        else -> "bajo"
    }
    secciones.add(SeccionCuestionario(
        titulo       = "Red de apoyo y relaciones",
        icono        = Icons.Default.FavoriteBorder,
        iconoBg      = Color(0xFFE3F0FB),
        iconoColor   = Color(0xFF2D5F9E),
        nivelGeneral = nivelRed,
        preguntas    = listOf(
            PreguntaCuestionario("Red de apoyo",          red,         nivelRed(red)),
            PreguntaCuestionario("Relación de pareja",    relPareja,   nivelRelacion(relPareja)),
            PreguntaCuestionario("Relaciones familiares", relFam,      nivelRelacion(relFam)),
            PreguntaCuestionario("Estado civil",          estadoCivil, "neutro"),
        )
    ))

    // ── 4. Antecedentes de salud mental ───────────────────────────────────
    val diagAns   = doc["diagAnsiedad"]   as? String ?: "Sin dato"
    val diagDep   = doc["diagDepresion"]  as? String ?: "Sin dato"
    val tristeza  = doc["tristezaMesPasado"]    as? String ?: ""
    val pocoInt   = doc["pocoInteresMesPasado"] as? String ?: ""
    val necAyuda  = doc["necesitaAyuda"]        as? String ?: ""
    // El tratamiento solo se pregunta si hubo diagnóstico: si está vacío, no se muestra.
    val psico     = doc["psicoterapia"]   as? String ?: ""
    val sesiones  = doc["sesionesPsico"]  as? String ?: ""
    val culmino   = doc["culminoPsico"]   as? String ?: ""
    val farmaco   = doc["farmacoterapia"] as? String ?: ""
    val medicamentos = doc["medicamentos"] as? String ?: ""
    val otroTrat  = doc["otroTratamiento"] as? String ?: ""
    val nivelSalud = when {
        necAyuda == "Sí" || (diagAns == "Sí" && diagDep == "Sí") -> "alto"
        diagAns == "Sí" || diagDep == "Sí" || tristeza == "Sí" || pocoInt == "Sí" -> "moderado"
        else -> "bajo"
    }
    secciones.add(SeccionCuestionario(
        titulo       = "Antecedentes de salud mental",
        icono        = Icons.Default.Psychology,
        iconoBg      = Color(0xFFE0F5F3),
        iconoColor   = Color(0xFF16A085),
        nivelGeneral = nivelSalud,
        preguntas    = buildList {
            add(PreguntaCuestionario("Diagnóstico de ansiedad",  diagAns,  if (diagAns  == "Sí") "alto" else "leve"))
            add(PreguntaCuestionario("Diagnóstico de depresión", diagDep,  if (diagDep  == "Sí") "alto" else "leve"))
            if (tristeza.isNotBlank())
                add(PreguntaCuestionario("Triste o sin esperanza (último mes)", tristeza, if (tristeza == "Sí") "moderado" else "leve"))
            if (pocoInt.isNotBlank())
                add(PreguntaCuestionario("Poco interés o placer (último mes)", pocoInt, if (pocoInt == "Sí") "moderado" else "leve"))
            if (necAyuda.isNotBlank())
                add(PreguntaCuestionario("Siente que necesita ayuda", necAyuda, if (necAyuda == "Sí") "alto" else "leve"))
            if (psico.isNotBlank())
                add(PreguntaCuestionario("Psicoterapia recibida", psico, "neutro"))
            if (sesiones.isNotBlank())
                add(PreguntaCuestionario("Sesiones de psicoterapia", sesiones, "neutro"))
            if (culmino.isNotBlank())
                add(PreguntaCuestionario("¿Culminó el tratamiento?", culmino, "neutro"))
            if (farmaco.isNotBlank())
                add(PreguntaCuestionario("Farmacoterapia", farmaco, "neutro"))
            if (medicamentos.isNotBlank())
                add(PreguntaCuestionario("Medicamentos", medicamentos, "neutro"))
            if (otroTrat.isNotBlank())
                add(PreguntaCuestionario("Otro tratamiento", otroTrat, "neutro"))
        }
    ))

    return secciones
}

// ─────────────────────────────────────────────────────────────────────────────
//  SEÑALES DE ATENCIÓN (alimentan el estado de riesgo y el banner del detalle)
// ─────────────────────────────────────────────────────────────────────────────
fun mapearAlertas(doc: Map<String, Any>?, barreras: BarrerasPacienteUI?): List<AlertaClinica> {
    val alertas = mutableListOf<AlertaClinica>()
    fun s(clave: String): String = doc?.get(clave) as? String ?: ""

    if (doc != null) {
        // Violencia: una respuesta positiva deriva a la Ruta Integral de Atención en Salud
        val tipos = buildList {
            if (s("victimaPsicologico") == "Sí") add("psicológica")
            if (s("victimaFisico") == "Sí")      add("física")
            if (s("victimaAbuso") == "Sí")       add("sexual")
        }
        if (tipos.isNotEmpty()) {
            alertas.add(AlertaClinica(
                "Reporta violencia ${tipos.joinToString(", ")} en el último año. " +
                        "Derivar a la Ruta Integral de Atención en Salud (Línea 155).", "alto"))
        } else if (listOf(s("victimaPsicologico"), s("victimaFisico"), s("victimaAbuso")).any { it == "En el pasado" }) {
            alertas.add(AlertaClinica("Antecedente de violencia en el pasado.", "medio"))
        }

        // Ánimo del último mes
        if (s("necesitaAyuda") == "Sí") {
            alertas.add(AlertaClinica(
                "Refiere tristeza o falta de interés en el último mes y siente que necesita ayuda (se le sugirió la Línea 106).", "alto"))
        } else if (s("tristezaMesPasado") == "Sí" || s("pocoInteresMesPasado") == "Sí") {
            alertas.add(AlertaClinica("Síntomas de ánimo durante el último mes.", "medio"))
        }

        // Diagnósticos previos
        val dx = buildList {
            if (s("diagAnsiedad") == "Sí")  add("ansiedad")
            if (s("diagDepresion") == "Sí") add("depresión")
        }
        if (dx.isNotEmpty())
            alertas.add(AlertaClinica("Diagnóstico previo de ${dx.joinToString(" y ")}.", "medio"))

        // Consumo
        val propio = buildList {
            if (s("consumoAlcohol") == "Sí")      add("alcohol")
            if (s("consumoPsicoactivas") == "Sí") add("sustancias psicoactivas")
        }
        val partes = mutableListOf<String>()
        if (propio.isNotEmpty()) partes.add("consumo propio de ${propio.joinToString(" y ")}")
        if (s("consumoPareja") == "Sí") partes.add("el consumo de su pareja afecta su bienestar")
        if (partes.isEmpty() && s("consumoSustancias") == "Sí" && s("consumoAlcohol").isBlank())
            partes.add("reporta consumo de sustancias")   // registros anteriores
        if (partes.isNotEmpty())
            alertas.add(AlertaClinica(partes.joinToString("; ").replaceFirstChar { it.uppercase() } + ".", "medio"))

        // Pérdida gestacional
        if (s("perdidaGestacional") == "Sí")
            alertas.add(AlertaClinica("Antecedente de pérdida gestacional o perinatal.", "medio"))
    }

    if (barreras != null && barreras.requiereAlerta)
        alertas.add(AlertaClinica("Barreras de acceso que requieren revisión (ver tarjeta de barreras).", "medio"))

    // Primero las altas
    return alertas.sortedBy { if (it.nivel == "alto") 0 else 1 }
}

// ── Helpers nivel por campo ───────────────────────────────────────────────────
private fun nivelIngreso(v: String)  = when { v.contains("Menos de 1") -> "alto"; v == "1 SMLV" -> "moderado"; else -> "leve" }
private fun nivelContrato(v: String) = when (v) { "Por días", "Por horas" -> "moderado"; "No aplica" -> "alto"; else -> "leve" }
private fun nivelEstrato(v: String)  = when ((v.toIntOrNull() ?: 3)) { 1 -> "alto"; 2 -> "moderado"; else -> "leve" }
private fun nivelViolencia(v: String)= when (v) { "Sí" -> "alto"; "En el pasado" -> "moderado"; else -> "leve" }
private fun nivelRed(v: String): String { val n = v.split(",").count { it.isNotBlank() }; return if (n == 0 || v == "Ninguna") "alto" else if (n == 1) "moderado" else "leve" }
private fun nivelRelacion(v: String) = when (v) { "Mala","Conflictiva","Malas","Conflictivas" -> "alto"; "Regular","Regulares","Inestable","Distantes" -> "moderado"; else -> "leve" }

// ─────────────────────────────────────────────────────────────────────────────
//  REPOSITORIO
// ─────────────────────────────────────────────────────────────────────────────

class ProfesionalRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Lee de Firestore solo los usuarios cuyo campo `profesionalAsignado`
     * coincide con el correo del profesional indicado, y construye los
     * objetos Paciente con los datos reales del cuestionario.
     *
     * El campo `profesionalAsignado` se asigna una sola vez por paciente
     * en ProfesionalesRepository.obtenerOAsignarProfesional() y queda fijo
     * desde entonces — por eso es la fuente de verdad para saber a quién
     * pertenece cada paciente (ya no se calcula por posición/round-robin).
     *
     * Estructura Firestore esperada:
     *   usuarios/{uid}
     *     ├── nombreCompleto: String
     *     ├── fechaNacimiento: String  (dd/MM/yyyy)
     *     ├── semanas: Long
     *     ├── ultimaSesion: String     (dd/MM/yyyy) — opcional
     *     ├── epds_puntaje: Long       — opcional, se guarda al aplicar EPDS
     *     ├── pass_puntaje: Long       — opcional, se guarda al aplicar PASS
     *     └── cuestionarios/{docId}
     *           ├── cuestionarioCompleto: true
     *           ├── fechaCompletado: Timestamp
     *           └── ... todos los campos del CuestionarioPsicosocialFlow
     */
    suspend fun getPacientesAsignados(correo: String): List<Paciente> {
        val usuariosSnap: QuerySnapshot = db.collection("usuarios")
            .whereEqualTo("profesionalAsignado", correo)
            .get().await()

        val todas = mutableListOf<Paciente>()

        usuariosSnap.documents.forEachIndexed { index, usuarioDoc ->
            val uid    = usuarioDoc.id
            val perfil = usuarioDoc.data ?: return@forEachIndexed

            // Cuestionario más reciente
            val cuestionariosSnap = db
                .collection("usuarios").document(uid)
                .collection("cuestionarios")
                .whereEqualTo("cuestionarioCompleto", true)
                .orderBy("fechaCompletado", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            val cuestionarioDoc = cuestionariosSnap.documents.firstOrNull()?.data

            val nombre = cuestionarioDoc?.get("nombreCompleto") as? String
                ?: perfil["nombreCompleto"] as? String
                ?: "Usuaria ${index + 1}"

            val telefono = cuestionarioDoc?.get("telefono") as? String
                ?: perfil["telefono"] as? String
                ?: ""

// En getPacientesAsignados, reemplaza el bloque de semanas por esto:
            val momentoActual = cuestionarioDoc?.get("momentoActual") as? String ?: ""
            val esPostparto = momentoActual == "Mi bebé ya llegó"
                    || (perfil["semanasBebePostparto"] as? String)?.isNotBlank() == true

            val semanas = if (esPostparto) {
                (cuestionarioDoc?.get("semanasBebePostparto") as? String)?.toIntOrNull()
                    ?: (perfil["semanasBebePostparto"] as? String)?.toIntOrNull()
                    ?: 0
            } else {
                (cuestionarioDoc?.get("semanasEmbarazo") as? String)?.toIntOrNull()
                    ?: (perfil["semanas"] as? Long)?.toInt()
                    ?: 0
            }

            val edad = calcularEdad(
                cuestionarioDoc?.get("fechaNacimiento") as? String
                    ?: perfil["fechaNacimiento"] as? String
                    ?: ""
            )
            val ultima = perfil["ultimaSesion"]?.let {
                formatearFechaRelativa(it as? String)
            } ?: cuestionarioDoc?.let {
                val ts = it["fechaCompletado"] as? com.google.firebase.Timestamp
                ts?.toDate()?.let { date ->
                    val f = date.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    formatearFechaRelativa(
                        f.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )
                }
            } ?: "sin registro"

// Leer cuestionarios clínicos (EPDS y PASS) — se guarda el historial
// completo, no solo el último, para poder comparar evaluaciones en el tiempo.
            val clinicosSnap = db
                .collection("usuarios").document(uid)
                .collection("cuestionarios_clinicos")
                .get().await()

            val historialEpdsMutable = mutableListOf<PuntajeConFecha>()
            val historialPassMutable = mutableListOf<PuntajeConFecha>()

            clinicosSnap.documents.forEach { clinDoc ->
                val tipo    = clinDoc.getString("tipo") ?: return@forEach
                val puntaje = (clinDoc.getLong("puntaje") ?: return@forEach).toInt()
                val fechaMs = clinDoc.getTimestamp("fecha")?.toDate()?.time ?: 0L
                when (tipo) {
                    "EPDS" -> historialEpdsMutable.add(PuntajeConFecha(puntaje, fechaMs))
                    "PASS" -> historialPassMutable.add(PuntajeConFecha(puntaje, fechaMs))
                }
            }

            // Orden cronológico: la más antigua primero, la más reciente al final.
            val historialEpds = historialEpdsMutable.sortedBy { it.fechaMs }
            val historialPass = historialPassMutable.sortedBy { it.fechaMs }

            val epds: PuntajePsicometrico? = historialEpds.lastOrNull()?.let { PuntajePsicometrico(it.puntaje, 30) }
            val pass: PuntajePsicometrico? = historialPass.lastOrNull()?.let { PuntajePsicometrico(it.puntaje, 93) }

            val factores  = cuestionarioDoc?.let { mapearFactoresPsico(it) }  ?: emptyList()
            val secciones = cuestionarioDoc?.let { mapearSeccionesCuestionario(it) } ?: emptyList()

            // Documento independiente guardado por CuestionarioBarreras.kt
            val barrerasDoc = try {
                db.collection("usuarios").document(uid)
                    .collection("caracterizacion").document("barreras_acceso")
                    .get().await().data
            } catch (e: Exception) { null }
            val barrerasUI = leerBarrerasDesdeDoc(barrerasDoc)

            todas.add(Paciente(
                id                    = index,
                uid                   = uid,
                nombre                = nombre,
                telefono              = telefono,
                semanas               = semanas,
                edadAnios             = edad,
                ultimaSesion          = ultima,
                epds                  = epds,
                pass                  = pass,
                historialEpds         = historialEpds,
                historialPass         = historialPass,
                factoresPsico         = factores,
                esPostparto = esPostparto,
                cuestionarioSecciones = secciones,
                barreras              = barrerasUI,
                alertas               = mapearAlertas(cuestionarioDoc, barrerasUI)
            ))
        }

        return todas
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun calcularEdad(fechaNac: String): Int = try {
        val f = LocalDate.parse(fechaNac, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        Period.between(f, LocalDate.now()).years
    } catch (e: Exception) { 0 }

    private fun formatearFechaRelativa(fecha: String?): String {
        if (fecha == null) return "sin registro"
        return try {
            val f   = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val dias = ChronoUnit.DAYS.between(f, LocalDate.now())
            when {
                dias == 0L -> "hoy"
                dias == 1L -> "ayer"
                dias < 7   -> "hace $dias días"
                else       -> "hace ${dias / 7} sem."
            }
        } catch (e: Exception) { "sin registro" }
    }
}