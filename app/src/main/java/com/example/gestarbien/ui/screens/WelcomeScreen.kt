package com.example.gestarbien.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onAdminAccess: () -> Unit = {}
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "fadeIn"
    )

    // ── Acceso oculto: 3 toques a la flor, cada uno dentro de los 2s siguientes al anterior.
    var conteoToques by remember { mutableStateOf(0) }
    var ultimoToqueMs by remember { mutableStateOf(0L) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .alpha(alpha)
        ) {
            // Flor como IconButton interactivamente táctil
            IconButton(
                onClick = {
                    val ahora = System.currentTimeMillis()
                    if (ultimoToqueMs == 0L || (ahora - ultimoToqueMs <= 2000)) {
                        conteoToques++
                    } else {
                        conteoToques = 1
                    }
                    ultimoToqueMs = ahora

                    if (conteoToques >= 3) {
                        conteoToques = 0
                        ultimoToqueMs = 0L
                        Toast.makeText(context, "Modo Administrador", Toast.LENGTH_SHORT).show()
                        onAdminAccess()
                    }
                },
                modifier = Modifier.size(80.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flor),
                    contentDescription = "Flor decorativa",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¡Bienvenida!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A3080),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "¿Ya tienes una cuenta?",
                fontSize = 16.sp,
                color = Color(0xFF7A5BAF),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB1B4E1)
                )
            ) {
                Text(
                    text = "Sí, iniciar sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    Color(0xFFB1B4E1)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFB1B4E1),
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "No, quiero registrarme",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB1B4E1)
                )
            }
        }
    }
}