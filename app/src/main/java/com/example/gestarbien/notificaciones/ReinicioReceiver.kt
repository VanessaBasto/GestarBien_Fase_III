package com.example.gestarbien.notificaciones


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReinicioReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("usuarios").document(uid)
            .collection("recordatorios")
            .whereEqualTo("avisoActivo", true)
            .get()
            .addOnSuccessListener { docs ->
                docs.documents.forEach { doc ->
                    val id = doc.id
                    val titulo = doc.getString("titulo") ?: return@forEach
                    val fechaHora = doc.getLong("fechaHora") ?: return@forEach
                    val antelacionMin = (doc.getLong("antelacionMin") ?: 0L)
                    val momentoAviso = fechaHora - antelacionMin * 60_000L

                    RecordatoriosScheduler.programar(
                        context,
                        id.hashCode(),
                        titulo,
                        titulo,
                        momentoAviso
                    )
                }
                Log.d("ReinicioReceiver", "Recordatorios reprogramados tras reinicio: ${docs.size()}")
            }
    }
}