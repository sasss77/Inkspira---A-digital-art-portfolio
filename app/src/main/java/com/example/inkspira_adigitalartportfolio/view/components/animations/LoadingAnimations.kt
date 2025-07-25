package com.example.inkspira_adigitalartportfolio.view.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary
import kotlin.math.*

// ✅ Artistic Paint Brush Loading
@Composable
fun ArtisticLoadingBrush(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "brush_loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "brush_rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brush_scale"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.minDimension / 4 * scale

        drawArtisticBrush(center, radius)
    }
}

// ✅ Gradient Pulse Loading
@Composable
fun GradientPulseLoading(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_pulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.minDimension / 2 * scale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    InkspiraPrimary.copy(alpha = alpha),
                    InkspiraSecondary.copy(alpha = alpha * 0.7f),
                    InkspiraTertiary.copy(alpha = alpha * 0.4f)
                ),
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }
}

// ✅ Creative Dots Loading
@Composable
fun CreativeDotsLoading(
    modifier: Modifier = Modifier,
    dotCount: Int = 5
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_loading")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val animationDelay = index * 200

            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 800,
                        delayMillis = animationDelay,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$index"
            )

            val color = when (index % 3) {
                0 -> InkspiraPrimary
                1 -> InkspiraSecondary
                else -> InkspiraTertiary
            }

            Canvas(
                modifier = Modifier.size(12.dp)
            ) {
                drawCircle(
                    color = color,
                    radius = 6.dp.toPx() * scale,
                    center = Offset(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }
    }
}

// ✅ Spinning Palette Loading
@Composable
fun SpinningPaletteLoading(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "palette_loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "palette_rotation"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.minDimension / 3

        // Draw color palette segments
        val colors = listOf(InkspiraPrimary, InkspiraSecondary, InkspiraTertiary)
        val angleStep = 360f / colors.size

        colors.forEachIndexed { index, color ->
            val startAngle = index * angleStep
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = angleStep - 10f, // Small gap between segments
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }
    }
}


//WaveLoadingAnimation with proper amplitudes
@Composable
fun WaveLoadingAnimation(
    modifier: Modifier = Modifier,
    waveCount: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_loading")

    // Create amplitude animations properly
    val amplitudes = remember(waveCount) {
        List(waveCount) { index ->
            index * 400 // Animation delays
        }
    }.mapIndexed { index, animationDelay ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    delayMillis = animationDelay,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave_amplitude_$index" // Use index instead of indexOf
        )
    }

    Canvas(
        modifier = modifier.size(120.dp, 40.dp)
    ) {
        amplitudes.forEachIndexed { index, amplitude ->
            val color = when (index % 3) {
                0 -> InkspiraPrimary
                1 -> InkspiraSecondary
                else -> InkspiraTertiary
            }

            drawWave(
                color = color,
                amplitude = amplitude.value, // value to get current animated value
                frequency = 2f,
                phase = index * 60f,
                strokeWidth = 4.dp.toPx()
            )
        }
    }
}




// Helper function to draw artistic brush
private fun DrawScope.drawArtisticBrush(center: Offset, radius: Float) {
    val colors = listOf(InkspiraPrimary, InkspiraSecondary, InkspiraTertiary)

    repeat(8) { index ->
        val angle = index * 45f * PI / 180f
        val brushLength = radius * (0.7f + 0.3f * sin(angle * 3).toFloat())
        val endX = center.x + cos(angle).toFloat() * brushLength
        val endY = center.y + sin(angle).toFloat() * brushLength

        drawLine(
            color = colors[index % colors.size],
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// Helper function to draw wave
private fun DrawScope.drawWave(
    color: Color,
    amplitude: Float,
    frequency: Float,
    phase: Float,
    strokeWidth: Float
) {
    val path = Path()
    val width = size.width
    val centerY = size.height / 2

    path.moveTo(0f, centerY)

    for (x in 0..width.toInt() step 5) {
        val y = centerY + amplitude * sin((x * frequency + phase) * PI / 180).toFloat()
        path.lineTo(x.toFloat(), y)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}
