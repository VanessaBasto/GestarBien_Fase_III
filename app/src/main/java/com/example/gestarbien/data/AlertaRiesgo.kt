package com.example.gestarbien.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class AlertaRiesgo(
    val id: String = "",
    val usuarioUid: String = "",
    val usuarioNombre: String = "",
    val categoria: String = "",       // "riesgo_alto" | "riesgo_medio"
    val resumenMensaje: String = "",  // qué disparó la alerta, para contexto del profesional
    val profesionalCorreo: String = "",
    val fecha: Timestamp? = null,
    val atendida: Boolean = false
)

class AlertasRepository {

    private val db = FirebaseFirestore.getInstance()
    private fun coleccion() = db.collection("alertas_riesgo")

    /**
     * Registra la alerta. Se llama justo cuando se detecta riesgo alto o
     * medio en el chat, ANTES o EN PARALELO al protocolo de seguridad que
     * ya se le muestra a la usuaria (Línea 106, etc.) — esto no reemplaza
     * eso, solo notifica al profesional asignado.
     */
    suspend fun registrarAlerta(
        usuarioUid: String,
        usuarioNombre: String,
        categoria: String,
        resumenMensaje: String,
        profesionalCorreo: String?
    ) {
        if (profesionalCorreo.isNullOrBlank()) return // sin profesional asignado, no hay a quién avisar
        val datos = mapOf(
            "usuarioUid" to usuarioUid,
            "usuarioNombre" to usuarioNombre,
            "categoria" to categoria,
            "resumenMensaje" to resumenMensaje.take(300), // evita guardar mensajes larguísimos
            "profesionalCorreo" to profesionalCorreo,
            "fecha" to Timestamp.now(),
            "atendida" to false
        )
        coleccion().add(datos).await()
    }

    /**
     * Escucha en tiempo real las alertas pendientes de un profesional.
     * Devuelve el ListenerRegistration para que quien lo use pueda
     * cancelarlo en DisposableEffect (evita fugas de memoria).
     */
    fun escucharAlertasPendientes(
        correoProfesional: String,
        onUpdate: (List<AlertaRiesgo>) -> Unit
    ): ListenerRegistration {
        return coleccion()
            .whereEqualTo("profesionalCorreo", correoProfesional)
            .whereEqualTo("atendida", false)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val lista = snap.documents.map { doc ->
                    AlertaRiesgo(
                        id = doc.id,
                        usuarioUid = doc.getString("usuarioUid") ?: "",
                        usuarioNombre = doc.getString("usuarioNombre") ?: "",
                        categoria = doc.getString("categoria") ?: "",
                        resumenMensaje = doc.getString("resumenMensaje") ?: "",
                        profesionalCorreo = doc.getString("profesionalCorreo") ?: "",
                        fecha = doc.getTimestamp("fecha"),
                        atendida = doc.getBoolean("atendida") ?: false
                    )
                }
                onUpdate(lista)
            }
    }

    suspend fun marcarAtendida(alertaId: String) {
        coleccion().document(alertaId).update("atendida", true).await()
    }
}