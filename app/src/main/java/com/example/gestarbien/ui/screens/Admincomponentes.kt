package com.example.gestarbien.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R
import com.google.firebase.auth.FirebaseAuth

// ─────────────────────────────────────────────────────────────
//  PALETA
//  Lila suave, más claro que el índigo anterior para que no
//  compita con el contenido ni canse la vista.
// ─────────────────────────────────────────────────────────────
val AdmOscuro     = Color(0xFF4A4487)
val AdmMedio      = Color(0xFF7B73C4)
val AdmSuave      = Color(0xFFB8B3E5)
val AdmFondoSuave = Color(0xFFF2F1FC)
val AdmFondoCard  = Color(0xFFF7F6FD)
val AdmGrisTxt    = Color(0xFF6B6B7B)
val AdmBordeSuave = Color(0xFFEAE8F7)
val AdmBlanco     = Color.White
val AdmRojo       = Color(0xFFE0655B)
val AdmRojoFondo  = Color(0xFFFDF0EF)
val AdmVerde      = Color(0xFF5FA678)
val AdmVerdeFondo = Color(0xFFEDF7F1)

/** Nombres propios de los administradores autorizados. */
private val NOMBRES_ADMIN = mapOf(
    "vabasto@ucundinamarca.edu.co"  to "Vanessa",
    "vcarreno@ucundinamarca.edu.co" to "Víctor"
)

/** Devuelve el nombre del admin según su correo. */
fun nombreDelAdmin(): String {
    val correo = FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase() ?: ""
    return NOMBRES_ADMIN[correo]
        ?: correo.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "Admin" }
}

/**
 * Encabezado de las pantallas internas.
 * Deja aire abajo para que el contenido no quede pegado al bloque lila.
 */
@Composable
fun AdminHeader(
    titulo: String,
    subtitulo: String = "",
    onBackClick: (() -> Unit)? = null,
    accion: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(AdmFondoSuave)
            .statusBarsPadding()
            .padding(start = 6.dp, end = 14.dp, top = 10.dp, bottom = 26.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = AdmOscuro
                    )
                }
            } else {
                Spacer(Modifier.width(18.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(
                    "MÓDULO ADMINISTRADORES",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = AdmMedio,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    titulo,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AdmOscuro
                )
                if (subtitulo.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(subtitulo, fontSize = 12.sp, color = AdmGrisTxt)
                }
            }

            accion?.invoke()
        }
    }
}

/** Tarjeta blanca estándar. */
@Composable
fun AdminTarjeta(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape           = RoundedCornerShape(18.dp),
        color           = AdmBlanco,
        shadowElevation = 2.dp,
        modifier        = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            content  = contenido
        )
    }
}

/** Avatar circular con una imagen del proyecto. */
@Composable
fun AdminAvatarImagen(
    recurso: Int,
    tamano: Int = 46,
    fondo: Color = AdmFondoSuave
) {
    Box(
        modifier = Modifier
            .size(tamano.dp)
            .clip(CircleShape)
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter            = painterResource(id = recurso),
            contentDescription = null,
            modifier           = Modifier.size(tamano.dp).clip(CircleShape),
            contentScale       = ContentScale.Crop
        )
    }
}

/** Etiqueta pequeña tipo píldora. */
@Composable
fun AdminEtiqueta(
    texto: String,
    fondo: Color = AdmFondoSuave,
    tinta: Color = AdmMedio
) {
    Surface(shape = RoundedCornerShape(20.dp), color = fondo) {
        Text(
            texto,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
            color      = tinta,
            modifier   = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

/** Estado vacío. */
@Composable
fun AdminVacio(mensaje: String) {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(vertical = 60.dp, horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            mensaje,
            fontSize   = 13.sp,
            color      = AdmGrisTxt,
            lineHeight = 19.sp,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.fillMaxWidth()
        )
    }
}

/** Buscador. */
@Composable
fun AdminBuscador(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value         = valor,
        onValueChange = onCambio,
        placeholder   = { Text(placeholder, fontSize = 13.sp, color = Color(0xFFA9A8BC)) },
        singleLine    = true,
        shape         = RoundedCornerShape(16.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = AdmSuave,
            unfocusedBorderColor    = AdmBordeSuave,
            cursorColor             = AdmMedio,
            focusedContainerColor   = AdmBlanco,
            unfocusedContainerColor = AdmBlanco
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Fotos rotativas para las usuarias, igual que en el panel clínico. */
val FOTOS_USUARIAS = listOf(
    R.drawable.mujer1,
    R.drawable.mujer2,
    R.drawable.mujer3,
    R.drawable.mujer4
)