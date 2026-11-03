package com.example.gestarbien

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.gestarbien.data.AdminRepository
import com.example.gestarbien.data.ConversacionesRepository
import com.example.gestarbien.data.Paciente
import com.example.gestarbien.data.RolAdmin
import com.example.gestarbien.ia.offline.ModeloPreferencias
import com.example.gestarbien.ia.offline.OfflineLLM
import com.example.gestarbien.ui.screens.*
import com.example.gestarbien.ui.theme.GestarBienTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Fotos asignadas a pacientes ─────────────────────────────────────────────
private val fotosPacientes = listOf(
    com.example.gestarbien.R.drawable.mujer1,
    com.example.gestarbien.R.drawable.mujer2,
    com.example.gestarbien.R.drawable.mujer3,
    com.example.gestarbien.R.drawable.mujer4
)

enum class Screen {
    SPLASH,
    VERIFICANDO,
    WELCOME,
    LOGIN,
    REGISTER,
    PROFESIONAL_LOGIN,
    PROFESIONAL_REGISTRO,
    FORGOT_PASSWORD,
    FORGOT_PASSWORD_PROFESIONAL,
    ONBOARDING_CUESTIONARIO_INTRO,
    CUESTIONARIO_PASO1,
    CUESTIONARIO_PASO2,
    CUESTIONARIO_PASO3,
    CUESTIONARIO_PASO4,
    CUESTIONARIO_BARRERAS,
    ONBOARDING_MOOD,
    ONBOARDING_PREGNANCY,
    ONBOARDING_SUMMARY,
    CHATBOT,
    AJUSTES_IA_OFFLINE,
    AYUDA_UBICACION,
    CUADERNO,
    CALENDARIO,
    ANALISIS,
    PANEL_CLINICO,
    DETALLE_PACIENTE,
    EPDS,
    EPDS_RESULTADO,
    PASS,
    PASS_RESULTADO,
    ADMIN_LOGIN,
    ADMIN_DASHBOARD,
    ADMIN_USUARIAS,
    ADMIN_MEDICOS,
    ADMIN_AGREGAR_MEDICO,
    ADMIN_ASIGNACIONES,
    ADMIN_CARACTERIZACION,
    ADMIN_INVESTIGADORES
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Precalentar la IA offline en segundo plano ──────────────────
        // Si el modelo ya está descargado en el celular, lo cargamos a memoria
        // para que el primer mensaje sin internet no tenga que esperar.
        lifecycleScope.launch {
            if (OfflineLLM.modeloDescargado(applicationContext)) {
                OfflineLLM.warmUp(applicationContext)
            }
        }

        enableEdgeToEdge()
        setContent {
            GestarBienTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GestarBienApp(context = this)
                }
            }
        }
    }
}

@Composable
fun GestarBienApp(context: Context) {

    var currentScreen       by remember { mutableStateOf(Screen.SPLASH) }
    var showRegisterSuccess by remember { mutableStateOf(false) }

    var datos          by remember { mutableStateOf(DatosSocioDemograficos()) }
    var antecedentes   by remember { mutableStateOf(Antecedentes()) }
    var infoAdicional  by remember { mutableStateOf("") }
    var barreras       by remember { mutableStateOf(BarrerasAcceso()) }

    var sentimientoInicial  by remember { mutableStateOf("") }
    var fechaPartoEstimada  by remember { mutableStateOf<String?>(null) }

    var pacienteSeleccionado by remember { mutableStateOf<Paciente?>(null) }
    var indexPaciente        by remember { mutableStateOf(0) }

    var puntajeEpds by remember { mutableStateOf(0) }
    var puntajePass by remember { mutableStateOf(0) }

    var aperturaCuestionarios by remember { mutableStateOf(false) }

    var emocionPendiente by remember { mutableStateOf<String?>(null) }

    var conversacionId      by remember { mutableStateOf(ConversacionesRepository.nuevoId()) }
    var conversacionInicio  by remember { mutableStateOf(System.currentTimeMillis()) }
    var conversacionACargar by remember { mutableStateOf<String?>(null) }

    var passOrigen by remember { mutableStateOf(Screen.CHATBOT) }
    var epdsOrigen by remember { mutableStateOf(Screen.CHATBOT) }

    var rolAdmin by remember { mutableStateOf<RolAdmin?>(null) }

    fun reiniciarEstadoUsuario() {
        datos                  = DatosSocioDemograficos()
        antecedentes            = Antecedentes()
        infoAdicional           = ""
        barreras                = BarrerasAcceso()
        sentimientoInicial      = ""
        fechaPartoEstimada      = null
        puntajeEpds             = 0
        puntajePass             = 0
        aperturaCuestionarios   = false
        emocionPendiente        = null
        conversacionACargar     = null
        pacienteSeleccionado    = null
        indexPaciente           = 0
        showRegisterSuccess     = false
        rolAdmin                = null
    }

    when (currentScreen) {

        Screen.SPLASH -> SplashScreen(
            onFinished = {
                currentScreen = if (FirebaseAuth.getInstance().currentUser != null)
                    Screen.VERIFICANDO
                else
                    Screen.WELCOME
            }
        )

        Screen.VERIFICANDO -> RutaInicio(
            context          = context,
            onRolAdmin       = { rol -> rolAdmin = rol },
            onDestino        = { destino -> currentScreen = destino }
        )

        Screen.WELCOME -> WelcomeScreen(
            onLoginClick    = { currentScreen = Screen.LOGIN },
            onRegisterClick = { currentScreen = Screen.REGISTER },
            onAdminAccess   = { currentScreen = Screen.ADMIN_LOGIN }
        )

        Screen.LOGIN -> LoginScreen(
            onBackClick    = { currentScreen = Screen.WELCOME },
            onLoginSuccess = {
                conversacionId      = ConversacionesRepository.nuevoId()
                conversacionInicio  = System.currentTimeMillis()
                conversacionACargar = null
                currentScreen       = Screen.VERIFICANDO
            },
            onForgotPasswordClick = { currentScreen = Screen.FORGOT_PASSWORD },
            onProfesionalClick    = { currentScreen = Screen.PROFESIONAL_LOGIN },
            successMessage        = if (showRegisterSuccess)
                "¡Registrada correctamente, ahora inicia sesión con tus datos!"
            else ""
        )

        Screen.PROFESIONAL_LOGIN -> ProfesionalLoginScreen(
            onBackClick           = { currentScreen = Screen.LOGIN },
            onLoginSuccess        = { registroCompleto ->
                currentScreen = if (registroCompleto) Screen.PANEL_CLINICO else Screen.PROFESIONAL_REGISTRO
            },
            onForgotPasswordClick = { currentScreen = Screen.FORGOT_PASSWORD_PROFESIONAL }
        )

        Screen.PROFESIONAL_REGISTRO -> ProfesionalRegistroScreen(
            onRegistroCompleto = { currentScreen = Screen.PANEL_CLINICO }
        )

        Screen.FORGOT_PASSWORD -> ForgotPasswordScreen(
            onBackClick = { currentScreen = Screen.LOGIN }
        )

        Screen.FORGOT_PASSWORD_PROFESIONAL -> ForgotPasswordProfesionalScreen(
            onBackClick = { currentScreen = Screen.PROFESIONAL_LOGIN }
        )

        Screen.REGISTER -> RegisterScreen(
            onBackClick       = { currentScreen = Screen.WELCOME },
            onRegisterSuccess = { showRegisterSuccess = true; currentScreen = Screen.LOGIN },
            onLoginClick      = { currentScreen = Screen.LOGIN }
        )

        Screen.ADMIN_LOGIN -> AdminLoginScreen(
            onBackClick          = { currentScreen = Screen.WELCOME },
            onAdminLoginSuccess = { rol ->
                rolAdmin      = rol
                currentScreen = Screen.ADMIN_DASHBOARD
            }
        )

        Screen.ADMIN_DASHBOARD -> {
            val rol = rolAdmin
            if (rol != null) {
                AdminDashboardScreen(
                    rol                  = rol,
                    onVerUsuarias        = { currentScreen = Screen.ADMIN_USUARIAS },
                    onVerMedicos         = { currentScreen = Screen.ADMIN_MEDICOS },
                    onAgregarMedico      = { currentScreen = Screen.ADMIN_AGREGAR_MEDICO },
                    onVerAsignaciones    = { currentScreen = Screen.ADMIN_ASIGNACIONES },
                    onVerCaracterizacion = { currentScreen = Screen.ADMIN_CARACTERIZACION },
                    onVerInvestigadores  = { currentScreen = Screen.ADMIN_INVESTIGADORES },
                    onCerrarSesion       = {
                        FirebaseAuth.getInstance().signOut()
                        reiniciarEstadoUsuario()
                        currentScreen = Screen.WELCOME
                    }
                )
            } else {
                currentScreen = Screen.ADMIN_LOGIN
            }
        }

        Screen.ADMIN_USUARIAS -> {
            val rol = rolAdmin
            if (rol != null) {
                AdminUsuariasScreen(
                    rol         = rol,
                    onBackClick = { currentScreen = Screen.ADMIN_DASHBOARD }
                )
            } else {
                currentScreen = Screen.ADMIN_LOGIN
            }
        }

        Screen.ADMIN_MEDICOS -> {
            val rol = rolAdmin
            if (rol != null) {
                AdminMedicosScreen(
                    rol             = rol,
                    onBackClick     = { currentScreen = Screen.ADMIN_DASHBOARD },
                    onAgregarMedico = { currentScreen = Screen.ADMIN_AGREGAR_MEDICO }
                )
            } else {
                currentScreen = Screen.ADMIN_LOGIN
            }
        }

        Screen.ADMIN_AGREGAR_MEDICO -> {
            val rol = rolAdmin
            if (rol != null) {
                AdminAgregarMedicoScreen(
                    rol            = rol,
                    onBackClick    = { currentScreen = Screen.ADMIN_MEDICOS },
                    onMedicoCreado = { currentScreen = Screen.ADMIN_MEDICOS }
                )
            } else {
                currentScreen = Screen.ADMIN_LOGIN
            }
        }

        Screen.ADMIN_ASIGNACIONES -> AdminAsignacionesScreen(
            onBackClick = { currentScreen = Screen.ADMIN_DASHBOARD }
        )

        Screen.ADMIN_CARACTERIZACION -> AdminCaracterizacionScreen(
            onBackClick = { currentScreen = Screen.ADMIN_DASHBOARD }
        )

        Screen.ADMIN_INVESTIGADORES -> AdminInvestigadoresScreen(
            onBackClick = { currentScreen = Screen.ADMIN_DASHBOARD }
        )

        Screen.ONBOARDING_CUESTIONARIO_INTRO -> OnboardingCuestionarioIntroScreen(
            onContinue = { currentScreen = Screen.CUESTIONARIO_PASO1 }
        )

        Screen.CUESTIONARIO_PASO1 -> CuestionarioPaso1(
            datos      = datos,
            onUpdate   = { datos = it },
            onContinue = { currentScreen = Screen.CUESTIONARIO_PASO2 },
            onBack     = { currentScreen = Screen.ONBOARDING_CUESTIONARIO_INTRO }
        )

        Screen.CUESTIONARIO_PASO2 -> CuestionarioPaso2(
            datos      = datos,
            onUpdate   = { datos = it },
            onContinue = { currentScreen = Screen.CUESTIONARIO_PASO3 },
            onBack     = { currentScreen = Screen.CUESTIONARIO_PASO1 }
        )

        Screen.CUESTIONARIO_PASO3 -> CuestionarioPaso3(
            antecedentes = antecedentes,
            onUpdate     = { antecedentes = it },
            onContinue   = { currentScreen = Screen.CUESTIONARIO_BARRERAS },
            onBack       = { currentScreen = Screen.CUESTIONARIO_PASO2 }
        )

        Screen.CUESTIONARIO_PASO4 -> CuestionarioPaso4(
            infoAdicional = infoAdicional,
            onUpdateInfo  = { infoAdicional = it },
            onContinue    = { currentScreen = Screen.ONBOARDING_MOOD },
            onBack        = { currentScreen = Screen.CUESTIONARIO_BARRERAS }
        )

        Screen.CUESTIONARIO_BARRERAS -> CuestionarioBarreras(
            barreras   = barreras,
            onUpdate   = { barreras = it },
            onContinue = {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    FirebaseFirestore.getInstance()
                        .collection("usuarios").document(uid)
                        .collection("caracterizacion").document("barreras_acceso")
                        .set(barreras.aMapaFirestore())

                    if (barreras.requiereAlerta) {
                        FirebaseFirestore.getInstance()
                            .collection("usuarios").document(uid)
                            .set(
                                mapOf(
                                    "requiereSeguimiento" to true,
                                    "motivoSeguimiento"   to barreras.motivoAlerta,
                                    "fechaSeguimientoMs"  to System.currentTimeMillis()
                                ),
                                com.google.firebase.firestore.SetOptions.merge()
                            )
                    }
                }
                currentScreen = Screen.CUESTIONARIO_PASO4
            },
            onBack = { currentScreen = Screen.CUESTIONARIO_PASO3 }
        )

        Screen.ONBOARDING_MOOD -> OnboardingMoodScreen(
            onContinue = { mood ->
                sentimientoInicial = mood
                currentScreen = Screen.ONBOARDING_PREGNANCY
            },
            onBack = { currentScreen = Screen.CUESTIONARIO_PASO4 }
        )

        Screen.ONBOARDING_PREGNANCY -> OnboardingPregnancyScreen(
            onContinue = { parto ->
                fechaPartoEstimada = parto
                currentScreen = Screen.ONBOARDING_SUMMARY
            },
            onSkip = {
                fechaPartoEstimada = null
                currentScreen = Screen.ONBOARDING_SUMMARY
            },
            onBack = { currentScreen = Screen.ONBOARDING_MOOD }
        )

        Screen.ONBOARDING_SUMMARY -> OnboardingSummaryScreen(
            sentimientoInicial = sentimientoInicial,
            fechaParto         = fechaPartoEstimada,
            datos              = datos,
            antecedentes       = antecedentes,
            infoAdicional      = infoAdicional,
            onContinue         = {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                context.getSharedPreferences("gestarbien_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("onboarding_completado_$uid", true)
                    .apply()
                currentScreen = Screen.CHATBOT
            },
            onBack = { currentScreen = Screen.ONBOARDING_PREGNANCY }
        )

        Screen.CHATBOT -> ChatbotScreen(
            conversacionId        = conversacionId,
            conversacionInicioMs  = conversacionInicio,
            conversacionACargar   = conversacionACargar,
            onConversacionCargada = { conversacionACargar = null },
            onAbrirCuaderno       = { currentScreen = Screen.CUADERNO },
            aperturaCuestionarios = aperturaCuestionarios,
            onAperturaConsumida   = { aperturaCuestionarios = false },
            emocionInicial        = emocionPendiente,
            onEmocionConsumida    = { emocionPendiente = null },
            onNavigateToCalendar  = { currentScreen = Screen.CALENDARIO },
            onNavigateToAnalysis  = { currentScreen = Screen.ANALISIS },

            onBackClick           = {
                FirebaseAuth.getInstance().signOut()
                reiniciarEstadoUsuario()
                currentScreen = Screen.WELCOME
            },
            onNavigateToEpds      = {
                epdsOrigen = Screen.CHATBOT
                currentScreen = Screen.EPDS
            },
            onNavigateToPass      = {
                passOrigen = Screen.CHATBOT
                currentScreen = Screen.PASS
            },
            onNavigateToIAOffline = { currentScreen = Screen.AJUSTES_IA_OFFLINE },
            onNavigateToAyuda     = { currentScreen = Screen.AYUDA_UBICACION },
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                reiniciarEstadoUsuario()
                currentScreen = Screen.LOGIN
            }
        )

        Screen.AJUSTES_IA_OFFLINE -> AjustesIAOfflineScreen(
            onBackClick = { currentScreen = Screen.CHATBOT }
        )

        Screen.AYUDA_UBICACION -> AyudaUbicacionScreen(
            onBack = { currentScreen = Screen.CHATBOT }
        )

        Screen.CUADERNO -> CuadernoScreen(
            onBackClick = { currentScreen = Screen.CHATBOT },
            onNuevaConversacion = {
                conversacionId      = ConversacionesRepository.nuevoId()
                conversacionInicio  = System.currentTimeMillis()
                conversacionACargar = null
                currentScreen       = Screen.CHATBOT
            },
            onAbrirConversacion = { id ->
                conversacionId      = id
                conversacionACargar = id
                currentScreen       = Screen.CHATBOT
            }
        )

        Screen.CALENDARIO -> CalendarioScreen(
            onBackClick          = { currentScreen = Screen.CHATBOT },
            onNavigateToChat     = { currentScreen = Screen.CHATBOT },
            onNavigateToAnalysis = { currentScreen = Screen.ANALISIS },
            onHablarDeEsto       = { emocion -> emocionPendiente = emocion }
        )

        Screen.ANALISIS -> AnalysisScreen(
            onChatbotClick    = { currentScreen = Screen.CHATBOT },
            onCalendarioClick = { currentScreen = Screen.CALENDARIO },
            onRetomarEpds     = {
                epdsOrigen = Screen.ANALISIS
                currentScreen = Screen.EPDS
            },
            onRetomarPass     = {
                passOrigen = Screen.ANALISIS
                currentScreen = Screen.PASS
            }
        )

        Screen.EPDS -> EpdsScreen(
            onBackClick = { currentScreen = epdsOrigen },
            onCompleted = { puntaje ->
                puntajeEpds = puntaje
                currentScreen = Screen.EPDS_RESULTADO
            }
        )

        Screen.EPDS_RESULTADO -> EpdsResultadoScreen(
            puntaje     = puntajeEpds,
            onContinuar = {
                passOrigen = Screen.EPDS_RESULTADO
                currentScreen = Screen.PASS
            }
        )

        Screen.PASS -> PassScreen(
            onBackClick = { currentScreen = passOrigen },
            onCompleted = { puntaje ->
                puntajePass = puntaje
                currentScreen = Screen.PASS_RESULTADO
            }
        )

        Screen.PASS_RESULTADO -> PassResultadoScreen(
            puntajeEpds    = puntajeEpds,
            puntajePass    = puntajePass,
            onVolverAlChat = {
                aperturaCuestionarios = true
                currentScreen = Screen.CHATBOT
            }
        )

        Screen.PANEL_CLINICO -> PanelClinicoScreen(
            onBackClick     = { currentScreen = Screen.PROFESIONAL_LOGIN },
            onPacienteClick = { paciente, index ->
                pacienteSeleccionado = paciente
                indexPaciente        = index
                currentScreen        = Screen.DETALLE_PACIENTE
            }
        )

        Screen.DETALLE_PACIENTE -> {
            val p = pacienteSeleccionado
            if (p != null) {
                DetallePacienteScreen(
                    paciente    = p,
                    fotoRes     = fotosPacientes[indexPaciente % fotosPacientes.size],
                    onBackClick = { currentScreen = Screen.PANEL_CLINICO }
                )
            } else {
                currentScreen = Screen.PANEL_CLINICO
            }
        }
    }
}

@Composable
fun RutaInicio(context: Context, onRolAdmin: (RolAdmin) -> Unit, onDestino: (Screen) -> Unit) {
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        val email = user?.email

        if (uid == null) {
            onDestino(Screen.WELCOME)
            return@LaunchedEffect
        }

        val db = FirebaseFirestore.getInstance()

        if (email != null) {
            val rol = try {
                AdminRepository(context.applicationContext).obtenerRol(email)
            } catch (e: Exception) { null }

            if (rol != null) {
                onRolAdmin(rol)
                onDestino(Screen.ADMIN_DASHBOARD)
                return@LaunchedEffect
            }
        }

        val profDoc = try {
            db.collection("profesionales").document(uid).get().await()
        } catch (e: Exception) { null }

        if (profDoc != null && profDoc.exists()) {
            onDestino(Screen.PANEL_CLINICO)
            return@LaunchedEffect
        }

        val prefs = context.getSharedPreferences("gestarbien_prefs", Context.MODE_PRIVATE)

        if (prefs.getBoolean("onboarding_completado_$uid", false)) {
            onDestino(Screen.CHATBOT)
            return@LaunchedEffect
        }

        val completo = try {
            db.collection("usuarios").document(uid)
                .get().await()
                .getBoolean("cuestionarioCompleto") ?: false
        } catch (e: Exception) {
            false
        }

        if (completo) {
            prefs.edit().putBoolean("onboarding_completado_$uid", true).apply()
        }

        onDestino(if (completo) Screen.CHATBOT else Screen.ONBOARDING_CUESTIONARIO_INTRO)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}