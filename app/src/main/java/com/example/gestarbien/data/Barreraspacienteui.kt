package com.example.gestarbien.data

/**
 * Vista de solo lectura de las barreras de acceso de una usuaria,
 * pensada para el panel del profesional. Se arma leyendo el mismo
 * documento "barreras_acceso" que guarda CuestionarioBarreras.kt
 * durante el onboarding.
 *
 * Soporta dos formatos:
 *  · Nuevo (4 preguntas): acceso, obstaculos, experiencia, facilitadores.
 *  · Antiguo (6 categorías sí/no + impacto): se sigue leyendo para no
 *    perder los registros que ya existen.
 */
data class CategoriaBarreraUI(
    val titulo: String,
    val tiene: Boolean,
    val detalles: List<String>
)

data class BarrerasPacienteUI(
    val categorias: List<CategoriaBarreraUI>,
    val impacto: String,                 // solo formato antiguo
    val facilitadores: List<String>,
    val comentario: String,
    val acceso: String = "",             // P1 (formato nuevo)
    val experiencia: String = ""         // P3 (formato nuevo)
) {
    val tieneRespuestas: Boolean get() = categorias.isNotEmpty()
    val hayAlguna: Boolean get() = categorias.any { it.tiene }
    val requiereAlerta: Boolean
        get() = impacto == "Mucho" || impacto == "Impidió la atención" ||
                acceso == "Lo necesité, pero no busqué o no pude acceder" ||
                experiencia == "No, me sentí juzgada o ignorada" ||
                categorias.any { it.titulo == "Familia o pareja" && it.tiene }
}

// Formato nuevo: clave guardada por la app → título que ve el profesional
private val TITULOS_OBSTACULOS = linkedMapOf(
    "personal" to "Situación personal",
    "familiar" to "Familia o pareja",
    "dinero"   to "Costo y distancia",
    "tramites" to "Servicio y trámites",
    "trato"    to "Trato del servicio",
    "digital"  to "Atención virtual",
    "otro"     to "Otro"
)

// Formato antiguo
private val TITULOS_CATEGORIAS = mapOf(
    "servicio"    to "Servicio y trámites",
    "llegar"      to "Costo y distancia",
    "condiciones" to "Trabajo y afiliación",
    "personal"    to "Situación personal",
    "familiar"    to "Familia o pareja",
    "cultural"    to "Creencias y trato"
)

/**
 * Lee el documento de barreras desde el mapa crudo de Firestore.
 * Devuelve null si la usuaria no ha completado ese paso del onboarding.
 */
fun leerBarrerasDesdeDoc(doc: Map<String, Any?>?): BarrerasPacienteUI? {
    if (doc == null) return null
    return if (doc.containsKey("acceso")) leerFormatoNuevo(doc) else leerFormatoAntiguo(doc)
}

private fun leerFormatoNuevo(doc: Map<String, Any?>): BarrerasPacienteUI? {
    val acceso = doc["acceso"] as? String ?: ""
    if (acceso.isBlank()) return null   // el documento existe pero no se completó

    val obstaculos    = (doc["obstaculos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val obstaculoOtro = doc["obstaculoOtro"] as? String ?: ""

    val categorias = TITULOS_OBSTACULOS.map { (clave, titulo) ->
        CategoriaBarreraUI(
            titulo   = titulo,
            tiene    = clave in obstaculos,
            detalles = if (clave == "otro" && obstaculoOtro.isNotBlank()) listOf(obstaculoOtro) else emptyList()
        )
    }

    return BarrerasPacienteUI(
        categorias    = categorias,
        impacto       = "",
        facilitadores = (doc["facilitadores"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        comentario    = doc["comentario"] as? String ?: "",
        acceso        = acceso,
        experiencia   = doc["experiencia"] as? String ?: ""
    )
}

@Suppress("UNCHECKED_CAST")
private fun leerFormatoAntiguo(doc: Map<String, Any?>): BarrerasPacienteUI? {
    val categorias = TITULOS_CATEGORIAS.map { (clave, titulo) ->
        val bloque = doc[clave] as? Map<String, Any?>
        CategoriaBarreraUI(
            titulo    = titulo,
            tiene     = (bloque?.get("tiene") as? String) == "Sí",
            detalles  = (bloque?.get("detalles") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )
    }

    if (categorias.all { !it.tiene } &&
        (doc["impacto"] as? String).isNullOrBlank() &&
        (doc["facilitadores"] as? List<*>).isNullOrEmpty()
    ) {
        return null
    }

    return BarrerasPacienteUI(
        categorias    = categorias,
        impacto       = doc["impacto"] as? String ?: "",
        facilitadores = (doc["facilitadores"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        comentario    = doc["comentario"] as? String ?: ""
    )
}