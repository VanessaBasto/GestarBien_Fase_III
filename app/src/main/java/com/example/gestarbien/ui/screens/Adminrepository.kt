package com.example.gestarbien.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────
//  ROLES
//
//  Dos roles fijos, definidos en código y no editables desde la
//  app: menos superficie de error que un sistema de permisos por
//  casilla, y más fácil de defender ante el jurado.
//
//  ADMIN_TOTAL   → Víctor y Vanessa. Crea médicos, elimina usuarias,
//                  ve asignaciones y caracterización, y es el único
//                  que puede dar de alta a un investigador.
//  INVESTIGADOR  → solo lectura. Ve los contadores del dashboard,
//                  asignaciones y caracterización con su exportador
//                  CSV. No ve el botón de añadir médico, ni el de
//                  eliminar en ninguna lista.
//
//  IMPORTANTE — esto es control de acceso a nivel de APP, no de
//  servidor. Oculta los botones según el rol, pero si algún día se
//  manejan datos reales de gestantes con más personas, hay que
//  replicar esta misma regla en las reglas de seguridad de
//  Firestore, porque un cliente que hable directo con la base de
//  datos podría saltarse lo que la interfaz esconde.
// ─────────────────────────────────────────────────────────────
enum class RolAdmin {
    ADMIN_TOTAL,
    INVESTIGADOR;

    val puedeEditar: Boolean get() = this == ADMIN_TOTAL
}

/** Correos con acceso total, fijados en código porque son solo dos. */
private val ADMIN_TOTAL_EMAILS = setOf(
    "vabasto@ucundinamarca.edu.co",
    "vcarreno@ucundinamarca.edu.co"
)

data class UsuariaResumen(
    val uid: String,
    val nombre: String,
    val correo: String,
    val semanas: Int,
    val profesionalAsignado: String
)

data class MedicoResumen(
    val docId: String,
    val uid: String,
    val nombre: String,
    val profesion: String,
    val whatsapp: String,
    val correo: String
)

data class InvestigadorResumen(
    val docId: String,
    val uid: String,
    val nombre: String,
    val correo: String
)

data class ResultadoEliminarMedico(
    val reasignadas: Int,
    val nuevoProfesional: String?
)

class AdminRepository(private val appContext: Context) {

    private val db = FirebaseFirestore.getInstance()

    // ── Rol de la sesión actual ─────────────────────────────────────────

    /**
     * Determina el rol del correo dado. Primero revisa la lista fija de
     * administradores totales; si no está ahí, busca en la colección
     * "investigadores" de Firestore. Devuelve null si no tiene acceso.
     */
    suspend fun obtenerRol(correo: String): RolAdmin? {
        val c = correo.trim().lowercase()
        if (ADMIN_TOTAL_EMAILS.contains(c)) return RolAdmin.ADMIN_TOTAL

        return try {
            val doc = db.collection("investigadores").document(c).get().await()
            if (doc.exists()) RolAdmin.INVESTIGADOR else null
        } catch (e: Exception) {
            null
        }
    }

    fun esCorreoAdministrador(correo: String): Boolean =
        ADMIN_TOTAL_EMAILS.contains(correo.trim().lowercase())

    // ── Usuarias ─────────────────────────────────────────────────────────

    suspend fun listarUsuarias(): List<UsuariaResumen> {
        val snap = db.collection("usuarios").get().await()
        return snap.documents.map { doc ->
            val data = doc.data ?: emptyMap()
            UsuariaResumen(
                uid = doc.id,
                nombre = data["nombreCompleto"] as? String ?: "Sin nombre",
                correo = data["correo"] as? String ?: "",
                semanas = (data["semanas"] as? Long)?.toInt() ?: 0,
                profesionalAsignado = data["profesionalAsignado"] as? String ?: "Sin asignar"
            )
        }
    }

    /**
     * Borra el documento de la usuaria y sus subcolecciones.
     * Reservado a ADMIN_TOTAL: la pantalla que llama esto no debe
     * mostrarse ni el botón debe existir para un investigador, pero
     * además se valida aquí por si acaso.
     */
    suspend fun eliminarUsuaria(uid: String, rol: RolAdmin) {
        require(rol.puedeEditar) { "El rol actual no tiene permiso para eliminar usuarias." }
        val ref = db.collection("usuarios").document(uid)
        borrarSubcoleccion(ref, "cuestionarios")
        borrarSubcoleccion(ref, "cuestionarios_clinicos")
        borrarSubcoleccion(ref, "caracterizacion")
        ref.delete().await()
    }

    // ── Médicos / profesionales ──────────────────────────────────────────

    suspend fun listarMedicos(): List<MedicoResumen> {
        val snap = db.collection("profesionales").get().await()
        return snap.documents.map { doc ->
            val data = doc.data ?: emptyMap()
            MedicoResumen(
                docId = doc.id,
                uid = data["uid"] as? String ?: doc.id,
                nombre = data["nombre"] as? String ?: "Sin nombre",
                profesion = data["profesion"] as? String ?: "",
                whatsapp = data["whatsapp"] as? String ?: "",
                correo = data["correo"] as? String ?: ""
            )
        }
    }

    suspend fun eliminarMedico(docId: String, rol: RolAdmin): ResultadoEliminarMedico {
        require(rol.puedeEditar) { "El rol actual no tiene permiso para eliminar médicos." }

        val medicos = listarMedicos()
        val medico  = medicos.firstOrNull { it.docId == docId }
        val correoMedico = medico?.correo ?: ""
        val sustituto = medicos.firstOrNull { it.docId != docId }

        var reasignadas = 0
        if (correoMedico.isNotBlank()) {
            val usuarias = db.collection("usuarios")
                .whereEqualTo("profesionalAsignado", correoMedico)
                .get().await()
            for (doc in usuarias.documents) {
                doc.reference.update(
                    "profesionalAsignado",
                    sustituto?.correo ?: "Sin asignar"
                ).await()
                reasignadas++
            }
        }

        db.collection("profesionales").document(docId).delete().await()

        return ResultadoEliminarMedico(
            reasignadas      = reasignadas,
            nuevoProfesional = sustituto?.nombre
        )
    }

    suspend fun agregarMedico(
        nombre: String,
        profesion: String,
        whatsapp: String,
        correo: String,
        password: String,
        rol: RolAdmin
    ) {
        require(rol.puedeEditar) { "El rol actual no tiene permiso para crear médicos." }

        val secundaria = obtenerAuthSecundaria()
        val resultado = secundaria.createUserWithEmailAndPassword(correo.trim(), password).await()
        val nuevoUid = resultado.user?.uid ?: error("No se pudo crear la cuenta del médico")

        db.collection("profesionales").document(nuevoUid).set(
            mapOf(
                "uid" to nuevoUid,
                "nombre" to nombre.trim(),
                "profesion" to profesion,
                "whatsapp" to whatsapp.trim(),
                "correo" to correo.trim(),
                "activo" to true
            )
        ).await()

        secundaria.signOut()
    }

    // ── Investigadores ────────────────────────────────────────────────────
    // Solo ADMIN_TOTAL puede crear o eliminar. El propio investigador no
    // puede autopromoverse ni crear a otro, porque su rol nunca pasa por
    // aquí: la pantalla de creación ni siquiera se le muestra.

    suspend fun listarInvestigadores(): List<InvestigadorResumen> {
        val snap = db.collection("investigadores").get().await()
        return snap.documents.map { doc ->
            val data = doc.data ?: emptyMap()
            InvestigadorResumen(
                docId  = doc.id,
                uid    = data["uid"] as? String ?: doc.id,
                nombre = data["nombre"] as? String ?: "Sin nombre",
                correo = data["correo"] as? String ?: doc.id
            )
        }
    }

    suspend fun agregarInvestigador(
        nombre: String,
        correo: String,
        password: String,
        rol: RolAdmin
    ) {
        require(rol.puedeEditar) { "El rol actual no tiene permiso para crear investigadores." }

        val correoLimpio = correo.trim().lowercase()
        val secundaria = obtenerAuthSecundaria()
        val resultado = secundaria.createUserWithEmailAndPassword(correoLimpio, password).await()
        val nuevoUid = resultado.user?.uid ?: error("No se pudo crear la cuenta del investigador")

        // El id del documento es el correo: es lo que consulta obtenerRol()
        // al iniciar sesión, para no depender de tener el uid de antemano.
        db.collection("investigadores").document(correoLimpio).set(
            mapOf(
                "uid" to nuevoUid,
                "nombre" to nombre.trim(),
                "correo" to correoLimpio
            )
        ).await()

        secundaria.signOut()
    }

    suspend fun eliminarInvestigador(docId: String, rol: RolAdmin) {
        require(rol.puedeEditar) { "El rol actual no tiene permiso para eliminar investigadores." }
        db.collection("investigadores").document(docId).delete().await()
        // No elimina la cuenta de Firebase Auth: mismo alcance y misma
        // limitación que eliminarUsuaria() y eliminarMedico().
    }

    // ── Asignaciones ─────────────────────────────────────────────────────

    suspend fun listarAsignaciones(): List<Pair<String, String>> =
        listarUsuarias().map { it.nombre to it.profesionalAsignado }

    // ── Helpers ──────────────────────────────────────────────────────────

    private suspend fun borrarSubcoleccion(ref: DocumentReference, nombre: String) {
        val docs = ref.collection(nombre).get().await()
        for (d in docs.documents) d.reference.delete().await()
    }

    private fun obtenerAuthSecundaria(): FirebaseAuth {
        val app = try {
            FirebaseApp.getInstance("adminSecondary")
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(appContext, FirebaseApp.getInstance().options, "adminSecondary")
        }
        return FirebaseAuth.getInstance(app!!)
    }
}