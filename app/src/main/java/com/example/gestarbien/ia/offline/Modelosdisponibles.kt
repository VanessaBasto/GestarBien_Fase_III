package com.example.gestarbien.ia.offline

data class ModeloCactus(
    val slug: String,
    val nombre: String,
    val descripcion: String,
    val tamanioAprox: String,
)

object ModelosDisponibles {
    val RESPALDO = ModeloCactus(
        slug = ModeloPreferencias.SLUG_PREDETERMINADO,
        nombre = "LFM2.5 350M",
        descripcion = "Liviano y rápido. Ayuda cuando el banco de respuestas no sabe qué decir.",
        tamanioAprox = "~220 MB",
    )

    val TODOS = listOf(RESPALDO)
    val PREDETERMINADO = RESPALDO
}