package com.example.inkspira_adigitalartportfolio.view.activities.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.inkspira_adigitalartportfolio.R

import com.example.inkspira_adigitalartportfolio.view.activities.main.DashboardActivity

import com.example.inkspira_adigitalartportfolio.view.activities.main.OnboardingActivity
import com.example.inkspira_adigitalartportfolio.view.ui.theme.*
import com.example.inkspira_adigitalartportfolio.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

class SplashActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            delay(3000) // Extended to 3 seconds for artistic effect

            when {
                authViewModel.isUserLoggedIn() -> navigateToMain()
                isFirstTimeUser() -> navigateToOnboarding()
                else -> navigateToLogin()
            }
        }

        setContent {
            InkspiraDarkTheme {
                ArtisticSplashScreen()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    private fun navigateToOnboarding() {
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun isFirstTimeUser(): Boolean {
        val sharedPrefs = getSharedPreferences("inkspira_prefs", MODE_PRIVATE)
        return !sharedPrefs.getBoolean("onboarding_completed", false)
    }
}

@Composable
private fun ArtisticSplashScreen() {
    // Animation values for artistic effects
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animations")

    // Rotating gradient animation
    val gradientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_rotation"
    )

    // Pulsing effect for logo
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    // Floating animation for logo
    val logoFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_float"
    )

    // Color morphing animation
    val colorMorph by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color_morph"
    )

    // Particle system animation
    val particleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic animated background with rotating gradients
        ArtisticBackground(
            gradientRotation = gradientRotation,
            colorMorph = colorMorph
        )

        // Floating art particles
        ArtisticParticleSystem(
            particleTime = particleTime,
            modifier = Modifier.fillMaxSize()
        )

        // Artistic geometric shapes
        GeometricArtShapes(
            rotation = gradientRotation / 2,
            modifier = Modifier.fillMaxSize()
        )

        // Main content with artistic layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Artistic logo container with glow effect
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .offset(y = logoFloat.dp)
                    .scale(logoScale)
            ) {
                // Glow rings around logo
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size((120 + index * 30).dp)
                            .alpha(0.3f - index * 0.1f)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        InkspiraPrimary.copy(alpha = 0.4f),
                                        Color.Transparent
                                    ),
                                    radius = 200f
                                ),
                                shape = CircleShape
                            )
                            .blur(radius = (5 + index * 5).dp)
                    )
                }

                // Logo with artistic border
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.inkspira),
                            contentDescription = "Inkspira Logo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            colorFilter = ColorFilter.tint(
                                Color.White.copy(alpha = 0.9f),
                                BlendMode.SrcAtop
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Artistic app name with gradient text effect
            ArtisticText(
                text = "Inkspira",
                fontSize = 42.sp,
                colorMorph = colorMorph,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Elegant tagline
            Text(
                text = "Digital Art Portfolio",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Decorative line
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                InkspiraPrimary.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Artistic loading indicator
            ArtisticLoadingIndicator(
                rotation = gradientRotation,
                colorMorph = colorMorph
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading text with fade animation
            Text(
                text = "Preparing your creative space...",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ArtisticBackground(
    gradientRotation: Float,
    colorMorph: Float
) {
    val density = LocalDensity.current

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Base dark gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0A0A2E),
                    Color(0xFF16213E),
                    Color(0xFF0F0F23)
                ),
                radius = size.maxDimension * 0.8f,
                center = center
            )
        )

        // Rotating gradient overlay
        rotate(gradientRotation, center) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        InkspiraPrimary.copy(alpha = 0.2f),
                        InkspiraSecondary.copy(alpha = 0.15f),
                        InkspiraTertiary.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
        }

        // Dynamic color overlay
        val morphColor = lerp(
            InkspiraPrimary.copy(alpha = 0.1f),
            InkspiraSecondary.copy(alpha = 0.1f),
            colorMorph
        )
        drawRect(color = morphColor)
    }
}

@Composable
private fun ArtisticParticleSystem(
    particleTime: Float,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(25) {
            ArtParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 1f,
                speed = Random.nextFloat() * 0.3f + 0.1f,
                color = listOf(InkspiraPrimary, InkspiraSecondary, InkspiraTertiary).random(),
                opacity = Random.nextFloat() * 0.6f + 0.2f
            )
        }
    }

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val currentY = (particle.y + particle.speed * particleTime / 1000f) % 1.2f
            val currentOpacity = particle.opacity * (1f - currentY * 0.5f)

            if (currentY <= 1f && currentOpacity > 0f) {
                drawCircle(
                    color = particle.color.copy(alpha = currentOpacity),
                    radius = particle.size,
                    center = Offset(
                        particle.x * size.width,
                        currentY * size.height
                    )
                )
            }
        }
    }
}

@Composable
private fun GeometricArtShapes(
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Rotating geometric patterns
        repeat(6) { index ->
            rotate(rotation + index * 60f, center) {
                val radius = size.minDimension * (0.15f + index * 0.05f)
                val strokeWidth = 2f
                val alpha = 0.1f - index * 0.015f

                drawCircle(
                    color = InkspiraPrimary.copy(alpha = alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        // Floating triangular shapes
        repeat(4) { index ->
            val angle = rotation * 0.5f + index * 90f
            val distance = size.minDimension * 0.3f
            val shapeCenter = Offset(
                center.x + cos(angle * PI / 180).toFloat() * distance,
                center.y + sin(angle * PI / 180).toFloat() * distance
            )

            drawCircle(
                color = InkspiraSecondary.copy(alpha = 0.08f),
                radius = 20f,
                center = shapeCenter
            )
        }
    }
}

@Composable
private fun ArtisticText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    colorMorph: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = InkspiraPrimary
    val secondaryColor = InkspiraSecondary
    val animatedColor = lerp(primaryColor, secondaryColor, colorMorph)

    Box(modifier = modifier) {
        // Glow effect
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = animatedColor.copy(alpha = 0.3f),
            textAlign = TextAlign.Center,
            modifier = Modifier.blur(radius = 8.dp)
        )

        // Main text
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ArtisticLoadingIndicator(
    rotation: Float,
    colorMorph: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(60.dp)
    ) {
        // Outer rotating ring
        Canvas(
            modifier = Modifier
                .size(60.dp)
                .rotate(rotation)
        ) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        InkspiraPrimary.copy(alpha = 0.8f),
                        InkspiraSecondary.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Inner pulsing circle
        val innerScale by animateFloatAsState(
            targetValue = 0.5f + (colorMorph * 0.3f),
            animationSpec = tween(500),
            label = "inner_scale"
        )

        Box(
            modifier = Modifier
                .size(20.dp)
                .scale(innerScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            InkspiraTertiary.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

// Data class for artistic particles
private data class ArtParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val color: Color,
    val opacity: Float
)

// Helper function to create smooth color transitions
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + fraction * (stop.red - start.red),
        green = start.green + fraction * (stop.green - start.green),
        blue = start.blue + fraction * (stop.blue - start.blue),
        alpha = start.alpha + fraction * (stop.alpha - start.alpha)
    )
}
