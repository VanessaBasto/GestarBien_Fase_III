package com.example.gestarbien.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ProfesionalPerfil(
    val uid: String = "",
    val correo: String = "",
    val nombre: String = "",
    val profesion: String = "",   // "Psicólogo(a)", "Psiquiatra", "Trabajador(a) Social"...
    val whatsapp: String = "",    // formato E.164, ej: 573001234567
    val registroCompleto: Boolean = false
)

class ProfesionalesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun coleccion() = db.collection("profesionales")

    /**
     * true si ya existe una cuenta de usuaria (paciente) con este uid.
     * Firebase Authentication es una sola base compartida entre los tres
     * roles, así que un mismo correo/uid puede iniciar sesión en el
     * portal de profesional aunque ya esté registrado como usuaria.
     */
    suspend fun esUsuariaExistente(uid: String): Boolean {
        return db.collection("usuarios").document(uid).get().await().exists()
    }

    /**
     * true si ya existe una cuenta de profesional con este uid.
     * Complemento simétrico de esUsuariaExistente(): protege el portal
     * de pacientes para que un profesional no entre ahí con su misma cuenta.
     */
    suspend fun esProfesionalExistente(uid: String): Boolean {
        return coleccion().document(uid).get().await().exists()
    }

    /** Guarda o actualiza el perfil del profesional autenticado actualmente. */
    suspend fun registrarPerfil(nombre: String, profesion: String, whatsapp: String) {
        val user = auth.currentUser ?: throw IllegalStateException("No hay sesión activa")

        if (esUsuariaExistente(user.uid)) {
            throw IllegalStateException(
                "Este correo ya está registrado como usuaria. No se puede usar la misma cuenta para el portal de profesionales."
            )
        }

        val datos = mapOf(
            "uid" to user.uid,
            "correo" to (user.email ?: ""),
            "nombre" to nombre.trim(),
            "profesion" to profesion,
            "whatsapp" to whatsapp.trim(),
            "registroCompleto" to true
        )
        coleccion().document(user.uid).set(datos).await()
    }

    /** Perfil del profesional que tiene sesión iniciada ahora mismo. Null si no ha registrado datos. */
    suspend fun obtenerPerfilPropio(): ProfesionalPerfil? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = coleccion().document(uid).get().await()
        if (!doc.exists()) return null
        return ProfesionalPerfil(
            uid = doc.id,
            correo = doc.getString("correo") ?: "",
            nombre = doc.getString("nombre") ?: "",
            profesion = doc.getString("profesion") ?: "",
            whatsapp = doc.getString("whatsapp") ?: "",
            registroCompleto = doc.getBoolean("registroCompleto") ?: false
        )
    }

    /** Todos los profesionales que ya completaron su registro (candidatos para asignación). */
    suspend fun listaProfesionalesActivos(): List<ProfesionalPerfil> {
        val snap = coleccion().whereEqualTo("registroCompleto", true).get().await()
        return snap.documents.map { doc ->
            ProfesionalPerfil(
                uid = doc.id,
                correo = doc.getString("correo") ?: "",
                nombre = doc.getString("nombre") ?: "",
                profesion = doc.getString("profesion") ?: "",
                whatsapp = doc.getString("whatsapp") ?: "",
                registroCompleto = true
            )
        }
    }

    fun obtenerPerfilPorCorreo(correo: String, onResult: (ProfesionalPerfil?) -> Unit) {
        coleccion().whereEqualTo("correo", correo).limit(1).get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                if (doc == null) { onResult(null); return@addOnSuccessListener }
                onResult(
                    ProfesionalPerfil(
                        uid = doc.id,
                        correo = doc.getString("correo") ?: "",
                        nombre = doc.getString("nombre") ?: "",
                        profesion = doc.getString("profesion") ?: "",
                        whatsapp = doc.getString("whatsapp") ?: "",
                        registroCompleto = doc.getBoolean("registroCompleto") ?: false
                    )
                )
            }
            .addOnFailureListener { onResult(null) }
    }

    /**
     * Si la usuaria ya tiene profesional asignado, lo devuelve. Si no,
     * elige uno al azar entre los activos, lo guarda en su documento
     * (para que quede fijo de ahí en adelante) y lo devuelve.
     */
    suspend fun obtenerOAsignarProfesional(usuarioUid: String): String? {
        val usuarioRef = db.collection("usuarios").document(usuarioUid)
        val usuarioDoc = usuarioRef.get().await()
        val existente = usuarioDoc.getString("profesionalAsignado")
        if (!existente.isNullOrBlank()) return existente

        val activos = listaProfesionalesActivos()
        if (activos.isEmpty()) return null

        val elegido = activos.random().correo
        usuarioRef.set(mapOf("profesionalAsignado" to elegido), com.google.firebase.firestore.SetOptions.merge()).await()
        return elegido
    }
}