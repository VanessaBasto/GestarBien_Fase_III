package com.example.gestarbien.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.gestarbien.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter


// ── MODELOS ──────────────────────────────────────────────────────────────────

data class DatosSocioDemograficos(
    val nombreCompleto: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val lugarResidencia: String = "",
    val estratoSocioeconomico: String = "",
    val tipoVivienda: String = "",
    val tipoViviendaOtra: String = "",
    val condicionesVivienda: List<String> = emptyList(),
    val afiliacionSalud: String = "",
    val ocupacion: String = "",
    val nivelEscolar: String = "",
    val tipoContrato: String = "",
    val ingresosMensuales: String = "",
    val composicionFamiliar: String = "",
    val estadoCivil: String = "",
    val relacionPareja: String = "",
    val relacionesFamiliares: String = "",
    val redDeApoyo: String = "",
    val redApoyoOtro: String = "",              // texto libre cuando elige "Otro" en red de apoyo
    val mostrarCampoRelacionPareja: Boolean = false,
    val mostrarCampoRelacionesFamiliares: Boolean = false,
    val mostrarCampoRedApoyo: Boolean = false  // true = "Otro" está marcado
)

data class Antecedentes(
    val momentoActual: String = "",

    // Embarazos previos (A01 – A03)
    val embarazoPrevio: String = "",           // A01: "Sí" | "No" | "Prefiero no responder"
    val embarazos: String = "",                // A02: número, solo si A01 == "Sí"
    val perdidaGestacional: String = "",       // A03: "Sí" | "No" | "Prefiero no responder"
    val semanasEmbarazo: String = "",

    // Violencia
    val violenciaGeneral: String = "",         // pregunta general: "Sí" | "No" | "Prefiero no decir"
    val victimaPsicologico: String = "",       // "Sí" | "No" | "En el pasado" | "Prefiero no decir"
    val victimaFisico: String = "",
    val victimaAbuso: String = "",             // violencia sexual

    // Consumo de sustancias
    val consumoSustancias: String = "",        // "¿Tú o tu pareja...?" "Sí" | "No" | ""
    val consumoAlcohol: String = "",
    val consumoPsicoactivas: String = "",
    val consumoSustanciasDetalle: String = "", // ¿Cuál o cuáles? (psicoactivas)
    val consumoPareja: String = "",

    // Diagnósticos y tamizaje de ánimo
    val diagnosticoAnsiedad: String = "",
    val diagnosticoDepresion: String = "",
    val tristeza: String = "",                 // D01
    val pocoInteres: String = "",              // D02
    val necesitaAyuda: String = "",            // D03 (solo si D01 o D02 == "Sí")
    val otroTrastorno: String = "",

    // Tratamiento (solo si hay diagnóstico de ansiedad o depresión)
    val tratamientoPsicoterapia: String = "",
    val sesiones: String = "",
    val culmino: String = "",
    val farmacoterapia: String = "",
    val medicamentos: String = "",
    val otroTratamiento: String = ""
) {
    /** Resolución de violencia: cualquier respuesta positiva deriva a la Ruta Integral. */
    val violenciaPositiva: Boolean
        get() = victimaPsicologico == "Sí" || victimaFisico == "Sí" || victimaAbuso == "Sí"

    val consumoPositivo: Boolean
        get() = consumoAlcohol == "Sí" || consumoPsicoactivas == "Sí" || consumoPareja == "Sí"

    val sintomasAnimo: Boolean
        get() = tristeza == "Sí" || pocoInteres == "Sí"

    val requiereTratamiento: Boolean
        get() = diagnosticoAnsiedad == "Sí" || diagnosticoDepresion == "Sí"
}

// ── ESTILO DE RESPUESTAS ─────────────────────────────────────────────────────

/**
 * Estilo único para todo lo que la usuaria escribe o elige en un campo.
 * Va al mismo tamaño que los chips (13sp) para que el formulario se vea parejo;
 * sin esto, los TextField de Material usan 16sp por defecto y se ven enormes.
 */
val EstiloRespuesta = TextStyle(
    fontSize = 13.sp,
    lineHeight = 18.sp,
    color = Color(0xFF2D2D2D)
)

// ── VALIDACIONES ─────────────────────────────────────────────────────────────

/** Verifica que la fecha exista, no sea futura y corresponda a una persona mayor de edad. */
fun esFechaNacimientoValida(fecha: LocalDate?): Boolean {
    if (fecha == null) return false
    val hoy = LocalDate.now()
    if (fecha.isAfter(hoy)) return false
    val edad = Period.between(fecha, hoy).years
    return edad >= 18
}

// ── COMPONENTES ───────────────────────────────────────────────────────────────

@Composable
fun PasoContainer(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val mostrarTopBar by remember { derivedStateOf { scrollState.value < 50 } }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.35f).align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(Color(0xFFB1B4E1))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.65f).align(Alignment.BottomCenter)
                .background(Color.White)
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 60.dp)
                .verticalScroll(scrollState).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
        AnimatedVisibility(
            visible = mostrarTopBar,
            enter = fadeIn(), exit = fadeOut()
        ) {
            OnboardingTopBarr(currentStep = currentStep, totalSteps = totalSteps, onBack = onBack)
        }
    }
}
@Composable
fun SeccionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium,
        color = Color(0xFF6B6B8A), lineHeight = 18.sp, modifier = modifier
    )
}

/** Texto auxiliar pequeño bajo una pregunta, p. ej. "(puedes escoger más de uno)". */
@Composable
fun AyudaSeleccion(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text, fontSize = 11.sp, fontWeight = FontWeight.Normal,
        color = Color(0xFFB0B0C8), modifier = modifier
    )
}

/** Aviso suave de apoyo (líneas 106, 155, etc.) que aparece bajo una pregunta. */
@Composable
fun AvisoApoyo(texto: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp), color = Color(0xFFEEEFFE),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = texto, fontSize = 12.sp, color = Color(0xFF3A3D8C), lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

/** Mensaje previo a las preguntas sensibles (Antecedentes). */
@Composable
fun AvisoSensible() {
    Surface(
        shape = RoundedCornerShape(12.dp), color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD0F8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(id = R.drawable.candado),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = buildAnnotatedString {
                    append("Las siguientes preguntas son ")
                    withStyle(SpanStyle(color = Color(0xFF5A2D9E), fontWeight = FontWeight.Bold)) {
                        append("sensibles")
                    }
                    append(". Respóndelas solo si te encuentras en un lugar donde puedas hacerlo con privacidad.")
                },
                fontSize = 12.sp, color = Color(0xFF555555), lineHeight = 18.sp
            )
        }
    }
}

// CampoTexto con capitalización automática al inicio
@Composable
fun CampoTexto(
    label: String, value: String, onValueChange: (String) -> Unit,
    placeholder: String = "", singleLine: Boolean = true,
    maxLines: Int = 1, modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            SeccionLabel(text = label)
            Spacer(modifier = Modifier.height(6.dp))
        }
        var focused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            textStyle = EstiloRespuesta,
            placeholder = if (placeholder.isNotBlank()) {
                { Text(text = placeholder, fontSize = 13.sp, color = Color(0xFFB0B0C8)) }
            } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = keyboardType,
                imeAction = if (singleLine) ImeAction.Done else ImeAction.Default
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(
                    width = if (focused) 1.dp else 0.5.dp,
                    color = if (focused) Color(0xFFB1B4E1) else Color(0xFFC4C4C4),
                    shape = RoundedCornerShape(12.dp)
                )
                .onFocusChanged { focused = it.isFocused },
            singleLine = singleLine, maxLines = maxLines,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
fun CampoNumero(
    label: String, value: String, onValueChange: (String) -> Unit,
    placeholder: String = "", modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            SeccionLabel(text = label)
            Spacer(modifier = Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            textStyle = EstiloRespuesta,
            singleLine = true,
            placeholder = { Text(text = placeholder, fontSize = 13.sp, color = Color(0xFFB0B0C8)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color(0xFFC4C4C4), shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

/**
 * Campo de teléfono con el indicativo +57 fijo. Lo que el usuario escribe
 * (solo dígitos, máximo 10) se guarda ya con el prefijo pegado, así el valor
 * que llega a Firestore siempre queda en formato internacional completo.
 */
@Composable
fun CampoTelefono(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "3001234567",
    isError: Boolean = false,
    errorText: String = ""
) {
    val digitos = value.removePrefix("+57")
    var focused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        if (label.isNotBlank()) {
            SeccionLabel(text = label)
            Spacer(modifier = Modifier.height(6.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(
                    width = if (isError || focused) 1.dp else 0.5.dp,
                    color = when {
                        isError -> Color(0xFF5C5FA8)
                        focused -> Color(0xFFB1B4E1)
                        else    -> Color(0xFFC4C4C4)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(start = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "+57", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = Color(0xFF6B6B8A)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .width(1.dp).height(20.dp)
                    .background(Color(0xFFD8D8E8))
            )
            TextField(
                value = digitos,
                textStyle = EstiloRespuesta,
                onValueChange = { nuevo ->
                    val soloDigitos = nuevo.filter { it.isDigit() }.take(10)
                    onValueChange("+57$soloDigitos")
                },
                placeholder = { Text(text = placeholder, fontSize = 13.sp, color = Color(0xFFB0B0C8)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focused = it.isFocused },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor        = Color(0xFF333333),
                    unfocusedTextColor      = Color(0xFF333333)
                )
            )
        }
        if (isError && errorText.isNotBlank()) {
            Text(
                text     = errorText,
                color    = Color(0xFF5C5FA8),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

/**
 * Campo desplegable con flecha: muestra una lista de opciones predefinidas.
 * Si se elige "Otro", aparece un campo de texto libre debajo para especificar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoDesplegable(
    label: String,
    opciones: List<String>,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Selecciona una opción",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var modoOtro by remember { mutableStateOf(value.isNotBlank() && value !in opciones) }
    var otroTexto by remember { mutableStateOf(if (modoOtro) value else "") }

    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            SeccionLabel(text = label)
            Spacer(modifier = Modifier.height(6.dp))
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = if (modoOtro) "Otro" else value,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                textStyle = EstiloRespuesta,
                placeholder = { Text(text = placeholder, fontSize = 13.sp, color = Color(0xFFB0B0C8)) },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null, tint = Color(0xFF9999BB)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = Color(0xFFB1B4E1),
                    unfocusedBorderColor    = Color(0xFFC4C4C4),
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White
            ) {
                opciones.forEachIndexed { index, opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion, style = EstiloRespuesta) },
                        onClick = {
                            expanded = false
                            if (opcion == "Otro") {
                                modoOtro = true
                                onValueChange(otroTexto)
                            } else {
                                modoOtro = false
                                otroTexto = ""
                                onValueChange(opcion)
                            }
                        }
                    )
                    if (index < opciones.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF0EBF8), thickness = 1.dp)
                    }
                }
            }
        }
        if (modoOtro) {
            Spacer(modifier = Modifier.height(8.dp))
            CampoTexto(
                label = "",
                value = otroTexto,
                onValueChange = {
                    otroTexto = it
                    onValueChange(it)
                },
                placeholder = "Escribe aquí..."
            )
        }
    }
}

@Composable
fun StepperSemanas(value: String, onValueChange: (String) -> Unit) {
    val numero = value.toIntOrNull() ?: 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(0.5.dp, Color(0xFFC4C4C4), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { if (numero > 0) onValueChange((numero - 1).toString()) }) {
            Text(
                "−", fontSize = 22.sp, fontWeight = FontWeight.Light,
                color = if (numero > 0) Color(0xFF5C5FA8) else Color(0xFFCCCCCC)
            )
        }
        Text(
            text = if (numero == 0) "Semanas" else "$numero semanas",
            fontSize = 13.sp,
            fontWeight = if (numero == 0) FontWeight.Normal else FontWeight.Medium,
            color = if (numero == 0) Color(0xFFB0B0C8) else Color(0xFF3A3D8C)
        )
        IconButton(onClick = { if (numero < 42) onValueChange((numero + 1).toString()) }) {
            Text(
                "+", fontSize = 22.sp, fontWeight = FontWeight.Light,
                color = if (numero < 42) Color(0xFF5C5FA8) else Color(0xFFCCCCCC)
            )
        }
    }
}

/**
 * Chip de opción. Altura mínima 44dp pero crece si el texto ocupa dos líneas,
 * así textos largos como "Compañeros de trabajo" no quedan apretados ni con
 * espacios raros alrededor.
 */
@Composable
fun OpcionChip(
    text: String, selected: Boolean, onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFB1B4E1).copy(alpha = 0.2f) else Color.White)
            .border(
                width = if (selected) 0.dp else 0.5.dp,
                color = if (selected) Color.Transparent else Color(0xFFC4C4C4),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, fontSize = 13.sp, lineHeight = 16.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = Color(0xFF2D2D2D), textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun FilaOpciones(opciones: List<String>, seleccionado: String, onSeleccionar: (String) -> Unit) {
    opciones.chunked(2).forEach { fila ->
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            fila.forEach { op ->
                OpcionChip(
                    text = op, selected = seleccionado == op,
                    onClick = { onSeleccionar(op) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CheckboxMultiple(opciones: List<String>, seleccionados: List<String>, onToggle: (String) -> Unit) {
    opciones.chunked(2).forEach { fila ->
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            fila.forEach { op ->
                OpcionChip(
                    text = op, selected = seleccionados.contains(op),
                    onClick = { onToggle(op) },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** Pregunta Sí / No con dos chips del mismo ancho. Va dentro de un PreguntaCard. */
@Composable
fun PreguntaSiNo(label: String, valor: String, onSeleccionar: (String) -> Unit) {
    SeccionLabel(text = label)
    Spacer(modifier = Modifier.height(10.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf("Sí", "No").forEach { op ->
            OpcionChip(
                text = op, selected = valor == op,
                onClick = { onSeleccionar(op) }, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SeparadorSeccion(titulo: String = "") {
    Spacer(modifier = Modifier.height(20.dp))
    if (titulo.isBlank()) {
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFF0EBF8), thickness = 1.dp)
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFF0EBF8), thickness = 1.dp)
            Text(
                "  $titulo  ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3A3D8C), letterSpacing = 0.3.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFF0EBF8), thickness = 1.dp)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun PreguntaCard(contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp), color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEDE8F8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), content = contenido)
    }
}

// Botón igual al EPDS: habilitado solo cuando se cumple condición
@Composable
fun BotonContinuar(texto: String, habilitado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = habilitado,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = Color(0xFFB1B4E1),
            disabledContainerColor = Color(0xFFB1B4E1).copy(alpha = 0.4f),
            contentColor           = Color.White,
            disabledContentColor   = Color.White.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp, pressedElevation = 0.dp, disabledElevation = 0.dp
        )
    ) {
        Text(
            text = if (!habilitado && texto == "Continuar") "Completa los datos" else texto,
            fontSize = 15.sp, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SeccionPrivacidad() {
    Surface(
        shape = RoundedCornerShape(12.dp), color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD0F8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(id = R.drawable.candado),
                contentDescription = "Candado de privacidad",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = buildAnnotatedString {
                    append("Tu información es ")
                    withStyle(SpanStyle(color = Color(0xFF5A2D9E), fontWeight = FontWeight.Bold)) {
                        append("privada y confidencial")
                    }
                    append(". Solo la verá el equipo profesional que te acompaña.")
                },
                fontSize = 12.sp, color = Color(0xFF555555), lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun OnboardingTopBarr(currentStep: Int, totalSteps: Int, onBack: (() -> Unit)?) {
    Box(modifier = Modifier.fillMaxWidth().zIndex(10f)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 36.dp, bottom = 16.dp, start = 16.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF5C5FA8), modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(52.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(totalSteps) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index + 1 == currentStep) 10.dp else 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index + 1 == currentStep) Color.White
                                else Color.White.copy(alpha = 0.45f)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
fun CampoFechaCalendario(
    label: String,
    fechaSeleccionada: LocalDate?,
    onFechaSeleccionada: (LocalDate) -> Unit,
    isError: Boolean = false,
    errorText: String = ""
) {
    var mostrarCal by remember { mutableStateOf(false) }
    Column {
        if (label.isNotBlank()) {
            SeccionLabel(text = label)
            Spacer(modifier = Modifier.height(6.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(
                    width = if (isError || mostrarCal) 1.dp else 0.5.dp,
                    color = when {
                        isError    -> Color(0xFF5C5FA8)
                        mostrarCal -> Color(0xFFB1B4E1)
                        else       -> Color(0xFFC4C4C4)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { mostrarCal = !mostrarCal }
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fechaSeleccionada?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        ?: "Selecciona una fecha",
                    fontSize = 13.sp,
                    color = if (fechaSeleccionada != null) Color(0xFF2D2D2D) else Color(0xFFB0B0C8)
                )
                Icon(
                    imageVector = if (mostrarCal) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = Color(0xFF9999BB),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (isError && errorText.isNotBlank()) {
            Text(
                text     = errorText,
                color    = Color(0xFF5C5FA8),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
        if (mostrarCal) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp), color = Color.White,
                shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()
            ) {
                MiniCalendario(
                    fechaSeleccionada = fechaSeleccionada,
                    onFechaSeleccionada = { onFechaSeleccionada(it); mostrarCal = false }
                )
            }
        }
    }
}

fun calcularRangoEdadd(fechaNacimiento: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val nacimiento = LocalDate.parse(fechaNacimiento, formatter)
        val edad = Period.between(nacimiento, LocalDate.now()).years
        val inicio = (edad / 5) * 5
        "$inicio – ${inicio + 4} años"
    } catch (e: Exception) { "" }
}

// ── INTRO ─────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingCuestionarioIntroScreen(onContinue: () -> Unit) {

    data class SeccionInfo(
        val titulo: String,
        val descripcion: String,
        val imagen: Int
    )

    val secciones = listOf(
        SeccionInfo("Datos personales y vivienda", "Información básica sobre ti y tu entorno", R.drawable.casa),
        SeccionInfo("Situación laboral y familiar", "Trabajo, ingresos y red de apoyo", R.drawable.familia),
        SeccionInfo("Antecedentes de salud", "Historial médico y tratamientos previos", R.drawable.salud),
        SeccionInfo("Información adicional", "Lo que tú consideres relevante compartir", R.drawable.sentir)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.45f).align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(Color(0xFFB1B4E1))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.55f).align(Alignment.BottomCenter)
                .background(Color.White)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Surface(
                shape = RoundedCornerShape(24.dp), color = Color.White,
                tonalElevation = 4.dp, shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp).clip(CircleShape)
                            .background(Color(0xFFEEEDFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.florr),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "CUESTIONARIO PSICOSOCIAL",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF9999BB), letterSpacing = 0.8.sp,
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cuéntanos\nsobre ti",
                        fontSize = 27.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A3D8C), textAlign = TextAlign.Center, lineHeight = 33.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Para acompañarte mejor, necesitamos conocer tu contexto. No hay respuestas correctas, solo tu propia historia.",
                        fontSize = 13.sp, color = Color(0xFF6B6B8A),
                        textAlign = TextAlign.Center, lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))
                    HorizontalDivider(color = Color(0xFFF0EBF8), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "EL CUESTIONARIO INCLUYE",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF9999BB), letterSpacing = 0.8.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    secciones.forEachIndexed { index, seccion ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp).clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFEAE5F5), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(seccion.imagen),
                                    contentDescription = seccion.titulo,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(seccion.titulo, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3A3D8C))
                                Text(seccion.descripcion, fontSize = 12.sp, color = Color(0xFF9999BB), lineHeight = 16.sp)
                            }
                        }
                        if (index < secciones.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 58.dp),
                                color = Color(0xFFF5F0FC), thickness = 1.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))
                    BotonContinuar(texto = "Comenzar cuestionario", habilitado = true, onClick = onContinue)
                }  // fin Column (Surface)
            }  // fin Surface

            Spacer(modifier = Modifier.height(50.dp))
        }  // fin Column
    }  // fin Box
}

// ── PASO 1 ────────────────────────────────────────────────────────────────────

private val MUNICIPIOS_SUGERIDOS = listOf("Facatativá", "Chía", "Soacha")

@Composable
fun CuestionarioPaso1(
    datos: DatosSocioDemograficos,
    onUpdate: (DatosSocioDemograficos) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val formatter  = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val fechaNacLD = runCatching { LocalDate.parse(datos.fechaNacimiento, formatter) }.getOrNull()
    val fechaValida = esFechaNacimientoValida(fechaNacLD)
    val telefonoDigitos = datos.telefono.removePrefix("+57")
    val telefonoValido  = telefonoDigitos.length == 10

    val habilitado = datos.nombreCompleto.isNotBlank() &&
            telefonoValido &&
            datos.fechaNacimiento.isNotBlank() && fechaValida &&
            datos.lugarResidencia.isNotBlank() &&
            datos.estratoSocioeconomico.isNotBlank()

    PasoContainer(currentStep = 1, totalSteps = 5, onBack = onBack) {
        Spacer(modifier = Modifier.height(40.dp))
        Surface(
            shape = RoundedCornerShape(24.dp), color = Color.White,
            tonalElevation = 4.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                Text("Paso 1 de 5", fontSize = 12.sp, color = Color(0xFF6B6B8A), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Datos personales", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D8C))
                Text("Cuestionario psicosocial", fontSize = 13.sp, color = Color(0xFF6B6B8A), lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(20.dp))

                PreguntaCard {
                    CampoTexto(
                        label = "¿Cómo te llamas?", value = datos.nombreCompleto,
                        onValueChange = { onUpdate(datos.copy(nombreCompleto = it)) },
                        placeholder = "Escribe tu nombre completo"
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    CampoTelefono(
                        label = "¿Cuál es tu número de teléfono?", value = datos.telefono,
                        onValueChange = { onUpdate(datos.copy(telefono = it)) },
                        isError = telefonoDigitos.isNotEmpty() && !telefonoValido,
                        errorText = "Ingresa un número de teléfono válido (10 dígitos)"
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    CampoFechaCalendario(
                        label = "¿Cuándo naciste?",
                        fechaSeleccionada = fechaNacLD,
                        onFechaSeleccionada = { onUpdate(datos.copy(fechaNacimiento = it.format(formatter))) },
                        isError = datos.fechaNacimiento.isNotBlank() && !fechaValida,
                        errorText = "Ingresa una fecha válida (debes ser mayor de edad)"
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    CampoDesplegable(
                        label = "¿En qué ciudad o municipio vives actualmente?",
                        opciones = MUNICIPIOS_SUGERIDOS,
                        value = datos.lugarResidencia,
                        onValueChange = { onUpdate(datos.copy(lugarResidencia = it)) },
                        placeholder = "Selecciona tu municipio"
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    SeccionLabel(text = "¿Cuál es tu estrato socioeconómico?")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = listOf("1", "2", "3", "4", "5", "6"),
                        seleccionado = datos.estratoSocioeconomico,
                        onSeleccionar = { onUpdate(datos.copy(estratoSocioeconomico = it)) }
                    )
                }

                SeparadorSeccion("Vivienda")

                PreguntaCard {
                    SeccionLabel(text = "¿En qué tipo de vivienda vives?")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = listOf("Arriendo", "Propia", "Familiar", "Otra"),
                        seleccionado = datos.tipoVivienda,
                        onSeleccionar = { onUpdate(datos.copy(tipoVivienda = it)) }
                    )
                    if (datos.tipoVivienda == "Otra") {
                        Spacer(modifier = Modifier.height(10.dp))
                        CampoTexto(
                            label = "¿Cuál?", value = datos.tipoViviendaOtra,
                            onValueChange = { onUpdate(datos.copy(tipoViviendaOtra = it)) },
                            placeholder = "Ej. Casa de paso, albergue, finca"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    SeccionLabel(text = "¿Con qué servicios cuenta tu vivienda?")
                    Spacer(modifier = Modifier.height(4.dp))
                    AyudaSeleccion(text = "(Puedes escoger más de uno)")
                    Spacer(modifier = Modifier.height(10.dp))
                    CheckboxMultiple(
                        opciones = listOf("Gas", "Luz", "Agua", "Internet"),
                        seleccionados = datos.condicionesVivienda,
                        onToggle = { item ->
                            val nueva = if (datos.condicionesVivienda.contains(item))
                                datos.condicionesVivienda - item else datos.condicionesVivienda + item
                            onUpdate(datos.copy(condicionesVivienda = nueva))
                        }
                    )
                }

                SeparadorSeccion("Educación")

                PreguntaCard {
                    SeccionLabel(text = "¿Hasta qué nivel llegaste en tus estudios?")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = listOf("Primaria", "Bachillerato", "Técnico", "Tecnólogo", "Pregrado", "Posgrado"),
                        seleccionado = datos.nivelEscolar,
                        onSeleccionar = { onUpdate(datos.copy(nivelEscolar = it)) }
                    )
                    OpcionChip(
                        text = "Ninguno", selected = datos.nivelEscolar == "Ninguno",
                        onClick = { onUpdate(datos.copy(nivelEscolar = "Ninguno")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                BotonContinuar(texto = "Continuar", habilitado = habilitado, onClick = onContinue)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── PASO 2 ────────────────────────────────────────────────────────────────────

private val OCUPACIONES_SUGERIDAS = listOf(
    "Docente", "Estudiante", "Vendedora", "Ama de casa",
    "Independiente", "Empleada", "Desempleada", "Otro"
)

@Composable
fun CuestionarioPaso2(
    datos: DatosSocioDemograficos,
    onUpdate: (DatosSocioDemograficos) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val opcionesRelacionPareja = listOf("Buena", "Regular", "Mala", "Conflictiva", "Estable", "Inestable")
    val opcionesRelFamiliar    = listOf("Buenas", "Regulares", "Malas", "Distantes", "Cercanas", "Conflictivas")
    val opcionesRedApoyo       = listOf("Familia", "Amigos", "Pareja", "Comunidad", "Grupo religioso", "Compañeros de trabajo")

    val habilitado = datos.afiliacionSalud.isNotBlank() &&
            datos.ocupacion.isNotBlank() &&
            datos.tipoContrato.isNotBlank() &&
            datos.ingresosMensuales.isNotBlank() &&
            datos.composicionFamiliar.isNotBlank() &&
            datos.estadoCivil.isNotBlank() &&
            datos.relacionPareja.isNotBlank() &&
            datos.relacionesFamiliares.isNotBlank() &&
            datos.redDeApoyo.isNotBlank()

    PasoContainer(currentStep = 2, totalSteps = 5, onBack = onBack) {
        Spacer(modifier = Modifier.height(40.dp))
        Surface(
            shape = RoundedCornerShape(24.dp), color = Color.White,
            tonalElevation = 4.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                Text("Paso 2 de 5", fontSize = 12.sp, color = Color(0xFF6B6B8A), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Situación laboral\ny familiar", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D8C), lineHeight = 32.sp)
                Spacer(modifier = Modifier.height(20.dp))

                PreguntaCard {
                    SeccionLabel(text = "¿Cómo estás afiliada al sistema de salud?")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = listOf("Contributivo", "Subsidiado", "No estoy afiliada", "No sé"),
                        seleccionado = datos.afiliacionSalud,
                        onSeleccionar = { onUpdate(datos.copy(afiliacionSalud = it)) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    CampoDesplegable(
                        label = "¿A qué te dedicas hoy en día?",
                        opciones = OCUPACIONES_SUGERIDAS,
                        value = datos.ocupacion,
                        onValueChange = { onUpdate(datos.copy(ocupacion = it)) },
                        placeholder = "Selecciona una opción"
                    )
                }

                SeparadorSeccion("Situación laboral")

                PreguntaCard {
                    SeccionLabel(text = "Tipo de contrato")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = listOf("Término fijo", "Término indefinido", "Por días", "Por horas", "No aplica"),
                        seleccionado = datos.tipoContrato,
                        onSeleccionar = { onUpdate(datos.copy(tipoContrato = it)) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    SeccionLabel(text = "¿Cómo son tus ingresos mensuales aproximadamente?")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = listOf("Menos de 1 SMLV", "1 SMLV", "Más de 1 SMLV", "2 o más SMLV", "No aplica"),
                        seleccionado = datos.ingresosMensuales,
                        onSeleccionar = { onUpdate(datos.copy(ingresosMensuales = it)) }
                    )
                }

                SeparadorSeccion("Familia y relaciones")

                PreguntaCard {
                    CampoTexto(
                        label = "¿Con quiénes vives actualmente?", value = datos.composicionFamiliar,
                        onValueChange = { onUpdate(datos.copy(composicionFamiliar = it)) },
                        placeholder = "Ej. Mi pareja, dos hijos y mi mamá",
                        singleLine = false, maxLines = 3
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    SeccionLabel(text = "Estado civil")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = listOf("Unión libre", "Casada", "Soltera", "Divorciada", "Viuda"),
                        seleccionado = datos.estadoCivil,
                        onSeleccionar = { onUpdate(datos.copy(estadoCivil = it)) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    SeccionLabel(text = "¿Cómo describirías tu relación de pareja en este momento?")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = opcionesRelacionPareja,
                        seleccionado = if (datos.relacionPareja in opcionesRelacionPareja) datos.relacionPareja else "",
                        onSeleccionar = { onUpdate(datos.copy(relacionPareja = it, mostrarCampoRelacionPareja = false)) }
                    )
                    OpcionChip(
                        text = "Prefiero escribir", selected = datos.mostrarCampoRelacionPareja,
                        onClick = {
                            onUpdate(datos.copy(
                                mostrarCampoRelacionPareja = !datos.mostrarCampoRelacionPareja,
                                relacionPareja = if (datos.mostrarCampoRelacionPareja) "" else datos.relacionPareja
                            ))
                        }, modifier = Modifier.fillMaxWidth()
                    )
                    if (datos.mostrarCampoRelacionPareja) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CampoTexto(label = "", value = datos.relacionPareja,
                            onValueChange = { onUpdate(datos.copy(relacionPareja = it)) },
                            placeholder = "Escribe aquí...", singleLine = false, maxLines = 2)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    SeccionLabel(text = "¿Cómo son tus relaciones familiares?")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = opcionesRelFamiliar,
                        seleccionado = if (datos.relacionesFamiliares in opcionesRelFamiliar) datos.relacionesFamiliares else "",
                        onSeleccionar = { onUpdate(datos.copy(relacionesFamiliares = it, mostrarCampoRelacionesFamiliares = false)) }
                    )
                    OpcionChip(
                        text = "Prefiero escribir", selected = datos.mostrarCampoRelacionesFamiliares,
                        onClick = {
                            onUpdate(datos.copy(
                                mostrarCampoRelacionesFamiliares = !datos.mostrarCampoRelacionesFamiliares,
                                relacionesFamiliares = if (datos.mostrarCampoRelacionesFamiliares) "" else datos.relacionesFamiliares
                            ))
                        }, modifier = Modifier.fillMaxWidth()
                    )
                    if (datos.mostrarCampoRelacionesFamiliares) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CampoTexto(label = "", value = datos.relacionesFamiliares,
                            onValueChange = { onUpdate(datos.copy(relacionesFamiliares = it)) },
                            placeholder = "Escribe aquí...", singleLine = false, maxLines = 2)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // ── Red de apoyo: opciones + "Ninguna" + "Otro" (con texto libre) ──
                PreguntaCard {
                    SeccionLabel(text = "¿Con quién cuentas cuando las cosas se ponen difíciles?")
                    Spacer(modifier = Modifier.height(4.dp))
                    AyudaSeleccion(text = "(Puedes escoger más de uno)")
                    Spacer(modifier = Modifier.height(10.dp))

                    val ninguna = datos.redDeApoyo == "Ninguna"
                    val seleccionadas: List<String> =
                        if (ninguna) emptyList()
                        else datos.redDeApoyo.split(", ").filter { it in opcionesRedApoyo }
                    val otroActivo = datos.mostrarCampoRedApoyo && !ninguna

                    // Reconstruye el valor final: opciones marcadas + texto de "Otro"
                    fun actualizarRed(lista: List<String>, otroOn: Boolean, otroTexto: String) {
                        val partes = lista + if (otroOn && otroTexto.isNotBlank()) listOf(otroTexto.trim()) else emptyList()
                        onUpdate(
                            datos.copy(
                                redDeApoyo = partes.joinToString(", "),
                                mostrarCampoRedApoyo = otroOn,
                                redApoyoOtro = otroTexto
                            )
                        )
                    }

                    CheckboxMultiple(
                        opciones = opcionesRedApoyo,
                        seleccionados = seleccionadas,
                        onToggle = { item ->
                            val nueva = if (item in seleccionadas) seleccionadas - item else seleccionadas + item
                            actualizarRed(nueva, otroActivo, datos.redApoyoOtro)
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OpcionChip(
                            text = "Ninguna", selected = ninguna,
                            onClick = {
                                onUpdate(datos.copy(redDeApoyo = "Ninguna", mostrarCampoRedApoyo = false, redApoyoOtro = ""))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OpcionChip(
                            text = "Otro", selected = otroActivo,
                            onClick = {
                                if (otroActivo) actualizarRed(seleccionadas, false, "")
                                else actualizarRed(seleccionadas, true, datos.redApoyoOtro)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (otroActivo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CampoTexto(
                            label = "", value = datos.redApoyoOtro,
                            onValueChange = { actualizarRed(seleccionadas, true, it) },
                            placeholder = "¿Con quién más cuentas? Escribe aquí...",
                            singleLine = false, maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                BotonContinuar(texto = "Continuar", habilitado = habilitado, onClick = onContinue)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── PASO 3 ────────────────────────────────────────────────────────────────────

@Composable
fun CuestionarioPaso3(
    antecedentes: Antecedentes,
    onUpdate: (Antecedentes) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val a = antecedentes
    val opcionesViolencia = listOf("Sí", "No", "En el pasado", "Prefiero no decir")
    val opcionesEmbarazo  = listOf("Sí", "No", "Prefiero no responder")

    /**
     * Punto único de actualización: después de cada cambio limpia lo que dejó de
     * aplicar, para que no queden respuestas "fantasma" guardadas en Firestore.
     */
    fun actualizar(nuevo: Antecedentes) {
        var n = nuevo
        if (!n.requiereTratamiento) {
            n = n.copy(
                tratamientoPsicoterapia = "", sesiones = "", culmino = "",
                farmacoterapia = "", medicamentos = "", otroTratamiento = ""
            )
        }
        if (!n.sintomasAnimo && n.necesitaAyuda.isNotEmpty()) n = n.copy(necesitaAyuda = "")
        onUpdate(n)
    }

    // ── Validación ──
    val embarazoOk = a.embarazoPrevio.isNotBlank() &&
            (a.embarazoPrevio != "Sí" || a.embarazos.isNotBlank()) &&
            a.perdidaGestacional.isNotBlank()

    val violenciaOk = a.violenciaGeneral.isNotBlank() &&
            (a.violenciaGeneral != "Sí" ||
                    (a.victimaPsicologico.isNotBlank() && a.victimaFisico.isNotBlank() && a.victimaAbuso.isNotBlank()))

    val sustanciasOk = a.consumoSustancias.isNotBlank() &&
            (a.consumoSustancias != "Sí" ||
                    (a.consumoAlcohol.isNotBlank() && a.consumoPsicoactivas.isNotBlank() && a.consumoPareja.isNotBlank()))

    val diagnosticosOk = a.diagnosticoAnsiedad.isNotBlank() &&
            a.diagnosticoDepresion.isNotBlank() &&
            a.tristeza.isNotBlank() && a.pocoInteres.isNotBlank() &&
            (!a.sintomasAnimo || a.necesitaAyuda.isNotBlank())

    val tratamientoOk = !a.requiereTratamiento ||
            (a.tratamientoPsicoterapia.isNotBlank() && a.farmacoterapia.isNotBlank())

    val habilitado = embarazoOk && violenciaOk && sustanciasOk && diagnosticosOk && tratamientoOk

    PasoContainer(currentStep = 3, totalSteps = 5, onBack = onBack) {
        Spacer(modifier = Modifier.height(40.dp))
        Surface(
            shape = RoundedCornerShape(24.dp), color = Color.White,
            tonalElevation = 4.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                Text("Paso 3 de 5", fontSize = 12.sp, color = Color(0xFF6B6B8A), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Antecedentes", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D8C))
                Text("Información confidencial, solo visible para profesionales.",
                    fontSize = 13.sp, color = Color(0xFF6B6B8A), lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Mensaje previo obligatorio
                AvisoSensible()

                // ───────── Embarazos previos ─────────
                SeparadorSeccion("Lo que has vivido")

                PreguntaCard {
                    SeccionLabel(text = "Antes de este embarazo o parto, ¿habías estado embarazada alguna vez?")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = opcionesEmbarazo, seleccionado = a.embarazoPrevio,
                        onSeleccionar = {
                            actualizar(a.copy(embarazoPrevio = it, embarazos = if (it == "Sí") a.embarazos else ""))
                        }
                    )
                    if (a.embarazoPrevio == "Sí") {
                        Spacer(modifier = Modifier.height(6.dp))
                        CampoNumero(
                            label = "¿Cuántos embarazos habías tenido anteriormente?",
                            value = a.embarazos,
                            onValueChange = { actualizar(a.copy(embarazos = it.filter { c -> c.isDigit() }.take(2))) },
                            placeholder = "Ej. 2"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    SeccionLabel(text = "¿Has tenido alguna pérdida gestacional o perinatal?")
                    Spacer(modifier = Modifier.height(10.dp))
                    FilaOpciones(
                        opciones = opcionesEmbarazo, seleccionado = a.perdidaGestacional,
                        onSeleccionar = { actualizar(a.copy(perdidaGestacional = it)) }
                    )
                }

                // ───────── Violencia ─────────
                SeparadorSeccion("Violencia")

                PreguntaCard {
                    PreguntaSiNo(
                        label = "¿Has vivido o vives situaciones de violencia física, verbal, psicológica o emocional?",
                        valor = a.violenciaGeneral,
                        onSeleccionar = { r ->
                            // Si no abre el detalle, se guardan respuestas coherentes para el panel
                            val sub = when (r) { "No" -> "No"; else -> "" }
                            actualizar(
                                if (r == "Sí") a.copy(violenciaGeneral = r)
                                else a.copy(
                                    violenciaGeneral = r,
                                    victimaPsicologico = sub, victimaFisico = sub, victimaAbuso = sub
                                )
                            )
                        }
                    )
                }

                if (a.violenciaGeneral == "Sí") {
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        SeccionLabel(text = "Violencia psicológica: en el último año, ¿has vivido situaciones en las que tu pareja te haya menospreciado, faltado al respeto o intimidado?")
                        Spacer(modifier = Modifier.height(10.dp))
                        FilaOpciones(opciones = opcionesViolencia, seleccionado = a.victimaPsicologico,
                            onSeleccionar = { actualizar(a.copy(victimaPsicologico = it)) })
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        SeccionLabel(text = "Violencia física: en el último año, ¿has sufrido alguna agresión física por parte de tu pareja, como golpes, bofetadas o empujones?")
                        Spacer(modifier = Modifier.height(10.dp))
                        FilaOpciones(opciones = opcionesViolencia, seleccionado = a.victimaFisico,
                            onSeleccionar = { actualizar(a.copy(victimaFisico = it)) })
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        SeccionLabel(text = "Violencia sexual: en el último año, ¿te has visto en situaciones donde tu pareja te haya exigido o presionado a tener relaciones sexuales sin que tú quisieras?")
                        Spacer(modifier = Modifier.height(10.dp))
                        FilaOpciones(opciones = opcionesViolencia, seleccionado = a.victimaAbuso,
                            onSeleccionar = { actualizar(a.copy(victimaAbuso = it)) })
                    }
                    if (a.violenciaPositiva) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AvisoApoyo(
                            "Gracias por confiar en nosotras. Si consideras que necesitas ayuda, " +
                                    "puedes llamar a la Línea 155, la línea nacional de orientación para " +
                                    "situaciones de violencia. Es gratuita y confidencial."
                        )
                    }
                }

                // ───────── Sustancias ─────────
                SeparadorSeccion("Consumo de sustancias")

                PreguntaCard {
                    PreguntaSiNo(
                        label = "¿Tú o tu pareja han consumido alguna sustancia?",
                        valor = a.consumoSustancias,
                        onSeleccionar = { r ->
                            actualizar(
                                if (r == "Sí") a.copy(consumoSustancias = r)
                                else a.copy(
                                    consumoSustancias = r, consumoAlcohol = "", consumoPsicoactivas = "",
                                    consumoSustanciasDetalle = "", consumoPareja = ""
                                )
                            )
                        }
                    )
                }

                if (a.consumoSustancias == "Sí") {
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        PreguntaSiNo(
                            label = "En los últimos meses, ¿has consumido bebidas alcohólicas, incluso en pequeñas cantidades?",
                            valor = a.consumoAlcohol,
                            onSeleccionar = { actualizar(a.copy(consumoAlcohol = it)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        PreguntaSiNo(
                            label = "En los últimos meses, ¿has consumido alguna sustancia psicoactiva distinta del alcohol?",
                            valor = a.consumoPsicoactivas,
                            onSeleccionar = {
                                actualizar(
                                    a.copy(
                                        consumoPsicoactivas = it,
                                        consumoSustanciasDetalle = if (it == "Sí") a.consumoSustanciasDetalle else ""
                                    )
                                )
                            }
                        )
                        if (a.consumoPsicoactivas == "Sí") {
                            Spacer(modifier = Modifier.height(10.dp))
                            CampoTexto(
                                label = "¿Cuál o cuáles?", value = a.consumoSustanciasDetalle,
                                onValueChange = { actualizar(a.copy(consumoSustanciasDetalle = it)) },
                                placeholder = "Ej. Marihuana ocasional",
                                singleLine = false, maxLines = 2
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        PreguntaSiNo(
                            label = "Actualmente, ¿consideras que el consumo de alcohol o de otras sustancias por parte de tu pareja afecta tu bienestar, tu tranquilidad o el entorno familiar?",
                            valor = a.consumoPareja,
                            onSeleccionar = { actualizar(a.copy(consumoPareja = it)) }
                        )
                    }
                    if (a.consumoPositivo) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AvisoApoyo(
                            "Gracias por compartirlo. Tu bienestar y el de tu bebé son lo más importante. " +
                                    "Si lo consideras necesario, puedes comunicarte con la Línea 106 para " +
                                    "recibir orientación confidencial y profesional."
                        )
                    }
                }

                // ───────── Diagnósticos y ánimo ─────────
                SeparadorSeccion("Diagnósticos anteriores")

                PreguntaCard {
                    PreguntaSiNo(
                        label = "¿Te han diagnosticado ansiedad?",
                        valor = a.diagnosticoAnsiedad,
                        onSeleccionar = { actualizar(a.copy(diagnosticoAnsiedad = it)) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    PreguntaSiNo(
                        label = "¿Te han diagnosticado depresión?",
                        valor = a.diagnosticoDepresion,
                        onSeleccionar = { actualizar(a.copy(diagnosticoDepresion = it)) }
                    )
                }

                SeparadorSeccion("Cómo te has sentido")

                PreguntaCard {
                    PreguntaSiNo(
                        label = "Durante el mes pasado, ¿con frecuencia te has sentido triste, deprimida o sin esperanza?",
                        valor = a.tristeza,
                        onSeleccionar = { actualizar(a.copy(tristeza = it)) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PreguntaCard {
                    PreguntaSiNo(
                        label = "Durante el mes pasado, ¿has permanecido preocupada por tener poco interés o placer para hacer las cosas cotidianas?",
                        valor = a.pocoInteres,
                        onSeleccionar = { actualizar(a.copy(pocoInteres = it)) }
                    )
                }

                // D03: solo si D01 o D02 = Sí
                if (a.sintomasAnimo) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PreguntaCard {
                        PreguntaSiNo(
                            label = "Si respondiste “sí” a cualquiera de las preguntas anteriores, ¿sientes que necesitas ayuda?",
                            valor = a.necesitaAyuda,
                            onSeleccionar = { actualizar(a.copy(necesitaAyuda = it)) }
                        )
                    }
                    if (a.necesitaAyuda == "Sí") {
                        Spacer(modifier = Modifier.height(10.dp))
                        AvisoApoyo(
                            "Gracias por contarnos cómo te sientes. Si lo necesitas, puedes comunicarte " +
                                    "con la Línea 106 para recibir apoyo emocional confidencial y profesional."
                        )
                    }
                }

                // ───────── Tratamiento: solo si hay ansiedad o depresión ─────────
                if (a.requiereTratamiento) {
                    SeparadorSeccion("Tratamiento recibido")

                    PreguntaCard {
                        SeccionLabel(text = "Psicoterapia")
                        Spacer(modifier = Modifier.height(10.dp))
                        FilaOpciones(opciones = listOf("Sí", "No"), seleccionado = a.tratamientoPsicoterapia,
                            onSeleccionar = {
                                actualizar(
                                    a.copy(
                                        tratamientoPsicoterapia = it,
                                        sesiones = if (it == "Sí") a.sesiones else "",
                                        culmino = if (it == "Sí") a.culmino else ""
                                    )
                                )
                            })
                        if (a.tratamientoPsicoterapia == "Sí") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                CampoNumero(label = "¿Cuántas sesiones?", value = a.sesiones,
                                    onValueChange = { actualizar(a.copy(sesiones = it)) },
                                    placeholder = "Ej. 10", modifier = Modifier.weight(1f))
                                CampoTexto(label = "¿La culminaste?", value = a.culmino,
                                    onValueChange = { actualizar(a.copy(culmino = it)) },
                                    placeholder = "Sí / No / En proceso", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    PreguntaCard {
                        SeccionLabel(text = "Farmacoterapia")
                        Spacer(modifier = Modifier.height(10.dp))
                        FilaOpciones(opciones = listOf("Sí", "No"), seleccionado = a.farmacoterapia,
                            onSeleccionar = {
                                actualizar(a.copy(farmacoterapia = it, medicamentos = if (it == "Sí") a.medicamentos else ""))
                            })
                        if (a.farmacoterapia == "Sí") {
                            Spacer(modifier = Modifier.height(10.dp))
                            CampoTexto(label = "¿Qué medicamentos tomas o tomaste?", value = a.medicamentos,
                                onValueChange = { actualizar(a.copy(medicamentos = it)) },
                                placeholder = "Ej. Sertralina 50mg, Clonazepam 0.5mg",
                                singleLine = false, maxLines = 2)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    PreguntaCard {
                        CampoTexto(label = "¿Algún otro tratamiento que hayas recibido?",
                            value = a.otroTratamiento,
                            onValueChange = { actualizar(a.copy(otroTratamiento = it)) },
                            placeholder = "Ej. Acupuntura, grupos de apoyo, meditación",
                            singleLine = false, maxLines = 2)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                BotonContinuar(texto = "Continuar", habilitado = habilitado, onClick = onContinue)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── PASO 4 ────────────────────────────────────────────────────────────────────

@Composable
fun CuestionarioPaso4(
    infoAdicional: String,
    onUpdateInfo: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    PasoContainer(currentStep = 5, totalSteps = 5, onBack = onBack) {
        Spacer(modifier = Modifier.height(40.dp))
        Surface(
            shape = RoundedCornerShape(24.dp), color = Color.White,
            tonalElevation = 4.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                Text("Paso 5 de 5", fontSize = 12.sp, color = Color(0xFF6B6B8A), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Información adicional", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D8C))
                Spacer(modifier = Modifier.height(6.dp))
                Text("¿Qué otra información consideras importante que conozcamos para acompañarte mejor?",
                    fontSize = 13.sp, color = Color(0xFF6B6B8A), lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))

                PreguntaCard {
                    CampoTexto(label = "Cuéntanos aquí (opcional)", value = infoAdicional,
                        onValueChange = onUpdateInfo,
                        placeholder = "Ej. 'Tengo muchas dudas sobre los cambios en mi cuerpo'...",
                        singleLine = false, maxLines = 8)
                }

                Spacer(modifier = Modifier.height(20.dp))
                SeccionPrivacidad()
                Spacer(modifier = Modifier.height(24.dp))
                BotonContinuar(texto = "Continuar", habilitado = true, onClick = onContinue)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}