package com.example.gestarbien.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.BorderStroke

@Composable
fun OnboardingWelcomeScreen(onContinue: () -> Unit) {
    val userName = FirebaseAuth.getInstance().currentUser?.displayName
        ?.takeIf { it.isNotBlank() }
        ?: "Bienvenida"

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Mitad superior lavanda ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(Color(0xFFB1B4E1))  // ← igual que OnboardingFeaturesScreen
        )

        // ── Mitad inferior blanca ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.BottomCenter)
                .background(Color.White)
        )

        // ── Contenido centrado ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Surface(
                shape           = RoundedCornerShape(24.dp),
                color           = Color.White,
                tonalElevation  = 4.dp,
                shadowElevation = 8.dp,
                modifier        = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        painter            = painterResource(id = R.drawable.flor),
                        contentDescription = "Flor decorativa",
                        modifier           = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text      = "Tu espacio seguro",
                        fontSize  = 15.sp,
                        color     = Color(0xFF9E86C8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF3D2A6E), fontWeight = FontWeight.Bold)) {
                                append("¡Bienvenida,\n")
                            }
                            withStyle(
                                SpanStyle(
                                    color      = Color(0xFFB8A0E0),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append(userName)
                            }
                            withStyle(SpanStyle(color = Color(0xFF3D2A6E), fontWeight = FontWeight.Bold)) {
                                append("!")
                            }
                        },
                        fontSize   = 30.sp,
                        textAlign  = TextAlign.Center,
                        lineHeight = 38.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text       = "Necesitamos conocerte un poco\npara personalizar tu experiencia.",
                        fontSize   = 14.sp,
                        color      = Color(0xFF888888),
                        textAlign  = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Botón igual al de OnboardingFeaturesScreen ───────
                    Button(
                        onClick  = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor   = Color(0xFF5C5FA8)
                        ),
                        border    = BorderStroke(1.5.dp, Color(0xFFB1B4E1)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text       = "Comenzar",
                            fontSize   = 16.sp,
                            color      = Color(0xFF5C5FA8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Nota de privacidad ───────────────────────────────────────
            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter            = painterResource(id = R.drawable.candado),
                        contentDescription = "Candado de privacidad",
                        modifier           = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Tu información es ")
                            withStyle(
                                SpanStyle(color = Color(0xFF5A2D9E), fontWeight = FontWeight.Bold)
                            ) {
                                append("privada y confidencial")
                            }
                            append(". Solo se usa para personalizar tu acompañamiento.")
                        },
                        fontSize   = 12.sp,
                        color      = Color(0xFF555555),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingFeaturesScreen(onContinue: () -> Unit) {
    val features = listOf(
        "Chatbot de apoyo emocional",
        "Diario emocional",
        "Evaluaciones psicométricas",
        "Registro de estado emocional"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                .background(Color(0xFFB1B4E1))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.BottomCenter)
                .background(Color.White)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape           = RoundedCornerShape(24.dp),
                color           = Color.White,
                tonalElevation  = 4.dp,
                shadowElevation = 8.dp,
                modifier        = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = "Siempre tendrás acceso a:",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF4A2E8F),
                        textAlign  = TextAlign.Start,
                        modifier   = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    features.forEachIndexed { index, text ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Image(
                                painter            = painterResource(id = R.drawable.flor),
                                contentDescription = null,
                                modifier           = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text       = text,
                                fontSize   = 15.sp,
                                color      = Color(0xFF444444),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (index < features.size - 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(
                                color     = Color(0xFFF0EBF8),
                                thickness = 1.dp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Button(
                        onClick  = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor   = Color(0xFF5C5FA8)
                        ),
                        border    = BorderStroke(1.5.dp, Color(0xFFB1B4E1)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text       = "Continuar",
                            fontSize   = 16.sp,
                            color      = Color(0xFF5C5FA8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}