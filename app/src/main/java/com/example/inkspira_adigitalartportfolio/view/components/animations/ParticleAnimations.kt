package com.example.inkspira_adigitalartportfolio.view.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary
import kotlin.math.*
import kotlin.random.Random

// ✅ ULTRA SIMPLE: Floating dots with basic movement
@Composable
fun FloatingParticlesAnimation(
    modifier: Modifier = Modifier,
    particleCount: Int = 20,
    colors: List<Color> = listOf(InkspiraPrimary, InkspiraSecondary, InkspiraTertiary)
) {
    val particles = remember {
        List(particleCount) {
            BasicParticle(
                color = colors.random(),
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 6f + 3f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                speedY = (Random.nextFloat() - 0.5f) * 0.3f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val newX = (particle.x + particle.speedX * time / 1000f) % 1f
            val newY = (particle.y + particle.speedY * time / 1000f) % 1f

            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(newX * size.width, newY * size.height)
            )
        }
    }
}

// ✅ ULTRA SIMPLE: Sparkles that just scale up and down
@Composable
fun SparkleAnimation(
    modifier: Modifier = Modifier,
    sparkleCount: Int = 15
) {
    val sparkles = remember {
        List(sparkleCount) {
            BasicSparkle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                baseSize = Random.nextFloat() * 4f + 2f,
                animationSpeed = Random.nextFloat() * 2f + 1f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sparkles")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        sparkles.forEach { sparkle ->
            val scale = 1f + 0.5f * sin(time * sparkle.animationSpeed * PI / 180).toFloat()
            val sparkleSize = sparkle.baseSize * scale

            drawCircle(
                color = InkspiraPrimary,
                radius = sparkleSize,
                center = Offset(sparkle.x * size.width, sparkle.y * size.height)
            )
        }
    }
}

// ✅ ULTRA SIMPLE: Paint dots that spread out
@Composable
fun PaintSplashAnimation(
    modifier: Modifier = Modifier,
    triggered: Boolean,
    onComplete: () -> Unit = {}
) {
    val splashes = remember {
        if (triggered) {
            List(8) {
                BasicSplash(
                    angle = it * 45f,
                    speed = Random.nextFloat() * 150f + 50f,
                    color = listOf(InkspiraPrimary, InkspiraSecondary, InkspiraTertiary).random(),
                    size = Random.nextFloat() * 8f + 4f
                )
            }
        } else emptyList()
    }

    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(triggered) {
        if (triggered) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(1000, easing = FastOutSlowInEasing)
            ) { value, _ ->
                progress = value
            }
            onComplete()
        } else {
            progress = 0f
        }
    }

    if (triggered) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            splashes.forEach { splash ->
                val distance = splash.speed * progress
                val x = centerX + cos(splash.angle * PI / 180).toFloat() * distance
                val y = centerY + sin(splash.angle * PI / 180).toFloat() * distance

                drawCircle(
                    color = splash.color,
                    radius = splash.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}

// ✅ ULTRA SIMPLE: Stars that just pulse
@Composable
fun ConstellationAnimation(
    modifier: Modifier = Modifier,
    starCount: Int = 25
) {
    val stars = remember {
        List(starCount) {
            BasicStar(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                baseSize = Random.nextFloat() * 3f + 1f,
                pulseSpeed = Random.nextFloat() * 3f + 1f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "constellation")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "constellation_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        stars.forEach { star ->
            val pulse = 0.5f + 0.5f * sin(time * star.pulseSpeed * PI / 180).toFloat()
            val starSize = star.baseSize * pulse

            drawCircle(
                color = InkspiraTertiary,
                radius = starSize,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }
    }
}

// ✅ BONUS: Simple falling particles
@Composable
fun FallingParticlesAnimation(
    modifier: Modifier = Modifier,
    particleCount: Int = 15
) {
    val particles = remember {
        List(particleCount) {
            FallingParticle(
                x = Random.nextFloat(),
                startY = -0.2f,
                size = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                color = listOf(InkspiraPrimary, InkspiraSecondary, InkspiraTertiary).random()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "falling")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val currentY = (particle.startY + particle.speed * time / 1000f) % 1.3f

            if (currentY <= 1f) {
                drawCircle(
                    color = particle.color,
                    radius = particle.size,
                    center = Offset(particle.x * size.width, currentY * size.height)
                )
            }
        }
    }
}

// ✅ SIMPLE DATA CLASSES - No complex properties
private data class BasicParticle(
    val color: Color,
    val x: Float,
    val y: Float,
    val size: Float,
    val speedX: Float,
    val speedY: Float
)

private data class BasicSparkle(
    val x: Float,
    val y: Float,
    val baseSize: Float,
    val animationSpeed: Float
)

private data class BasicSplash(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float
)

private data class BasicStar(
    val x: Float,
    val y: Float,
    val baseSize: Float,
    val pulseSpeed: Float
)

private data class FallingParticle(
    val x: Float,
    val startY: Float,
    val size: Float,
    val speed: Float,
    val color: Color
)
