package com.example.inkspira_adigitalartportfolio.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.inkspira_adigitalartportfolio.R
import kotlin.math.sin

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InkspiraDarkTheme {
                EnhancedSplashScreen()
            }
        }
    }
}

@Composable
fun InkspiraDarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF8B5CF6), // Purple
            secondary = Color(0xFFEC4899), // Pink
            tertiary = Color(0xFF06B6D4), // Cyan
            background = Color(0xFF0F0F23), // Deep Dark Blue
            surface = Color(0xFF1A1A2E), // Dark Navy
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

@Composable
fun EnhancedSplashScreen() {
    val context = LocalContext.current
    val activity = context as Activity

    val sharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE)
    val localEmail: String = sharedPreferences.getString("email", "").toString()

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
    var startAnimation by remember { mutableStateOf(false) }

    // Logo animations
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val logoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_rotation"
    )

    // Text animations
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )

    // Particle animations
    val particleOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle1"
    )

    val particleOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -80f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle2"
    )

    // Initial fade-in animation
    val initialAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "initial_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000)

        if (localEmail.isEmpty()) {
            // User not logged in, go to Login screen
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
            activity.finish()
        } else {
            // User already logged in, go directly to Navigation screen
            val intent = Intent(context, NavigationActivity::class.java)
            context.startActivity(intent)
            activity.finish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E), // Dark Navy center
                        Color(0xFF16213E), // Darker blue
                        Color(0xFF0F0F23)  // Deep dark blue edges
                    ),
                    radius = 1000f
                )
            )
    ) {
        // Animated background particles
        repeat(8) { index ->
            val offset = if (index % 2 == 0) particleOffset1 else particleOffset2
            val color = when (index % 3) {
                0 -> Color(0xFF8B5CF6).copy(alpha = 0.3f)
                1 -> Color(0xFFEC4899).copy(alpha = 0.3f)
                else -> Color(0xFF06B6D4).copy(alpha = 0.3f)
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .offset(
                        x = (100 + index * 50).dp,
                        y = (100 + index * 80 + offset).dp
                    )
                    .clip(CircleShape)
                    .background(color)
                    .alpha(initialAlpha)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(initialAlpha),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo container with glow effect
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        rotationZ = logoRotation * 0.1f // Subtle rotation
                    },
                contentAlignment = Alignment.Center
            ) {
                // Glow effect background
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.4f),
                                    Color(0xFFEC4899).copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                radius = 400f
                            )
                        )
                )

                // Logo image
                Card(
                    modifier = Modifier.size(220.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 20.dp
                    )
                ) {
                    Image(
                        painter = painterResource(R.drawable.inkspira),
                        contentDescription = "Inkspira Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // App name with gradient effect
            Text(
                text = "Inkspira",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Digital Art Portfolio",
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.alpha(textAlpha * 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Custom animated loading indicator
            CustomLoadingIndicator()

            Spacer(modifier = Modifier.height(20.dp))

            // Loading text
            Text(
                text = "Loading your creative space...",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.alpha(textAlpha),
                textAlign = TextAlign.Center
            )
        }

        // Bottom decorative elements
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(initialAlpha)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val delay = index * 200
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, delayMillis = delay, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_scale_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6))
                    )
                }
            }
        }
    }
}

@Composable
fun CustomLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_indicator")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation"
    )

    Box(
        modifier = Modifier.size(50.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899),
                            Color(0xFF06B6D4),
                            Color.Transparent
                        )
                    )
                )
                .rotate(rotation)
        )

        // Inner circle
        Box(
            modifier = Modifier
                .size(35.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F0F23))
        )
    }
}

