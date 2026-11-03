package com.example.gestarbien.data

import com.example.gestarbien.ui.screens.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale

/**
 * Una conversación completa del cuaderno.
 *
 * Ruta en Firestore: usuarios/{uid}/conversaciones/{id}
 */
data class Conversacion(
    val id: String = "",
    val titulo: String = "",
    val inicioMs: Long = 0L,
    val ultimoMs: Long = 0L,
    val mensajes: List<ChatMessage> = emptyList()
) {
    /** Primer mensaje de la usuaria; sirve de vista previa en la lista. */
    val vistaPrevia: String
        get() = mensajes.firstOrNull { !it.isBot }?.text
            ?: mensajes.firstOrNull()?.text
            ?: ""

    val cantidadMensajes: Int get() = mensajes.size
}

object ConversacionesRepository {

    private val MESES = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    private fun uid(): String? = FirebaseAuth.getInstance().currentUser?.uid

    private fun coleccion() = uid()?.let {
        FirebaseFirestore.getInstance()
            .collection("usuarios").document(it)
            .collection("conversaciones")
    }

    fun nuevoId(): String = System.currentTimeMillis().toString()

    /**
     * Id fijo para la conversación de hoy. Usarlo al iniciar sesión hace
     * que, mientras sea el mismo día, volver al chat (desde Análisis,
     * Calendario, o tras cerrar y volver a abrir la app) siempre continúe
     * la misma conversación en vez de empezar una nueva. Al cambiar de
     * día, el id cambia solo y se abre naturalmente una conversación nueva.
     */
    fun idDeHoy(): String = "dia_${claveDia(System.currentTimeMillis())}"

    /** "4 de agosto de 2026" */
    fun fechaLegible(ms: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = ms }
        return "${c.get(Calendar.DAY_OF_MONTH)} de ${MESES[c.get(Calendar.MONTH)]} de ${c.get(Calendar.YEAR)}"
    }

    /** "yyyy-MM-dd", para comparar si una conversación es de hoy. */
    fun claveDia(ms: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = ms }
        return String.format(
            Locale.getDefault(), "%04d-%02d-%02d",
            c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Título por defecto: la primera frase que escribió la usuaria, recortada.
     * Si aún no escribió nada, se usa un título narrativo genérico.
     */
    fun tituloAutomatico(mensajes: List<ChatMessage>, inicioMs: Long): String {
        val primera = mensajes.firstOrNull { !it.isBot }?.text?.trim()
        return if (primera.isNullOrBlank()) {
            // Sin mensaje de ella todavía: título narrativo genérico,
            // en vez de uno administrativo tipo "Conversación del...".
            "Un capítulo del ${fechaLegible(inicioMs)}"
        } else if (primera.length <= 42) {
            // Sus propias palabras como título — coherente con respetar
            // su lenguaje en vez de traducirlo a jerga o resumirlo por ella.
            primera
        } else {
            primera.take(42).trimEnd() + "…"
        }
    }

    // ─────────────────────────────────────────────
    //  LECTURA
    // ─────────────────────────────────────────────
    suspend fun listar(): List<Conversacion> {
        val col = coleccion() ?: return emptyList()
        return try {
            col.get().await().documents.map { doc ->
                Conversacion(
                    id       = doc.id,
                    titulo   = doc.getString("titulo") ?: "",
                    inicioMs = doc.getLong("inicioMs") ?: 0L,
                    ultimoMs = doc.getLong("ultimoMs") ?: 0L,
                    mensajes = leerMensajes(doc.get("mensajes"))
                )
            }.sortedByDescending { it.ultimoMs }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun cargar(id: String): Conversacion? {
        val col = coleccion() ?: return null
        return try {
            val doc = col.document(id).get().await()
            if (!doc.exists()) return null
            Conversacion(
                id       = doc.id,
                titulo   = doc.getString("titulo") ?: "",
                inicioMs = doc.getLong("inicioMs") ?: 0L,
                ultimoMs = doc.getLong("ultimoMs") ?: 0L,
                mensajes = leerMensajes(doc.get("mensajes"))
            )
        } catch (e: Exception) {
            null
        }
    }

    /** La más reciente. Sirve para saber si hay que abrir una nueva por cambio de día. */
    suspend fun ultima(): Conversacion? = listar().firstOrNull()

    @Suppress("UNCHECKED_CAST")
    private fun leerMensajes(bruto: Any?): List<ChatMessage> {
        val lista = bruto as? List<*> ?: return emptyList()
        return lista.mapNotNull { item ->
            val m = item as? Map<String, Any?> ?: return@mapNotNull null
            ChatMessage(
                id    = m["id"] as? String ?: System.nanoTime().toString(),
                text  = m["text"] as? String ?: return@mapNotNull null,
                isBot = m["isBot"] as? Boolean ?: true,
                mostrarContactoProfesional = m["mostrarContactoProfesional"] as? Boolean ?: false
            )
        }
    }

    // ─────────────────────────────────────────────
    //  ESCRITURA
    // ─────────────────────────────────────────────
    /**
     * Guarda o actualiza la conversación. No hace nada si no hay mensajes
     * de la usuaria: evita llenar el cuaderno de chats vacíos.
     */
    fun guardar(
        id: String,
        mensajes: List<ChatMessage>,
        inicioMs: Long,
        titulo: String = "",
        onDone: (Boolean) -> Unit = {}
    ) {
        val col = coleccion()
        if (col == null || mensajes.none { !it.isBot }) { onDone(false); return }

        val datos = mapOf(
            "titulo"   to (titulo.ifBlank { tituloAutomatico(mensajes, inicioMs) }),
            "inicioMs" to inicioMs,
            "ultimoMs" to System.currentTimeMillis(),
            "mensajes" to mensajes.map {
                mapOf(
                    "id" to it.id,
                    "text" to it.text,
                    "isBot" to it.isBot,
                    "mostrarContactoProfesional" to it.mostrarContactoProfesional
                )
            }
        )

        col.document(id).set(datos)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun renombrar(id: String, titulo: String, onDone: (Boolean) -> Unit = {}) {
        val col = coleccion()
        if (col == null) { onDone(false); return }
        col.document(id).update("titulo", titulo)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun borrar(id: String, onDone: (Boolean) -> Unit = {}) {
        val col = coleccion()
        if (col == null) { onDone(false); return }
        col.document(id).delete()
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }
}