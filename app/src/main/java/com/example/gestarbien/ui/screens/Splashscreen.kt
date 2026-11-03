package com.example.gestarbien.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestarbien.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
val PurpleLight = Color(0xFFE8E0F8)
val PurpleMid   = Color(0xFF9C7DD9)
val PurpleDark  = Color(0xFF6A4CAF)
val PinkLight   = Color(0xFFF5C6D8)
val PinkMid     = Color(0xFFD4607E)
val White       = Color(0xFFFFFFFF)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "alpha"
    )
// En SplashScreen o LoginScreen, al inicio:
    LaunchedEffect(Unit) {
        launch {
            try {
                val url = java.net.URL("https://azaratthh-color-detectorr.hf.space/ping")
                url.openConnection().apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                }.getInputStream().close()
            } catch (_: Exception) {}
        }
    }
    LaunchedEffect(Unit) {
        repeat(100) {
            delay(30)
            progress += 0.01f
        }
        delay(500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha)
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "Logo GestarBien",
                modifier = Modifier.size(280.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Loading...",
                fontSize = 13.sp,
                color = PurpleMid,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PurpleMid,
                trackColor = PurpleLight,
            )
        }
    }
}