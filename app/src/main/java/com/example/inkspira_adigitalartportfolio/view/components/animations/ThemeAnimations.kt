package com.example.inkspira_adigitalartportfolio.view.components.animations

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DeepDarkBlue
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextPrimary

// ✅ ULTRA SIMPLE: Color fade animation
@Composable
fun ColorTransitionAnimation(
    targetColor: Color,
    content: @Composable (animatedColor: Color) -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "color_transition"
    )

    content(animatedColor)
}

// ✅ ULTRA SIMPLE: Basic gradient that moves
@Composable
fun SimpleGradientAnimation(
    content: @Composable (brush: Brush) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val brush = Brush.linearGradient(
        colors = listOf(InkspiraPrimary, InkspiraSecondary, InkspiraTertiary),
        start = Offset(offset * 500, 0f),
        end = Offset((offset + 1) * 500, 100f)
    )

    content(brush)
}

// ✅ ULTRA SIMPLE: Theme color switching
@Composable
fun ThemeColorAnimation(
    isDark: Boolean,
    content: @Composable (bgColor: Color, textColor: Color) -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isDark) DeepDarkBlue else Color.White,
        animationSpec = tween(400),
        label = "bg_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isDark) TextPrimary else Color.Black,
        animationSpec = tween(400),
        label = "text_color"
    )

    content(bgColor, textColor)
}

// ✅ ULTRA SIMPLE: Scale pulse effect
@Composable
fun SimplePulse(
    pulsing: Boolean,
    content: @Composable (scale: Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (pulsing) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    content(scale)
}

// ✅ ULTRA SIMPLE: Moving shimmer effect
@Composable
fun SimpleShimmer(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val shimmerX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.3f),
            Color.Transparent
        ),
        start = Offset(shimmerX - 200, 0f),
        end = Offset(shimmerX, 0f)
    )

    Box(
        modifier = modifier.background(shimmerBrush)
    )
}

// ✅ ULTRA SIMPLE: Rotating colors
@Composable
fun ColorRotation(
    content: @Composable (color: Color) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "color_rotation")

    val colorIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "color_index"
    )

    val colors = listOf(InkspiraPrimary, InkspiraSecondary, InkspiraTertiary)
    val currentColor = colors[colorIndex.toInt() % colors.size]

    content(currentColor)
}

// ✅ ULTRA SIMPLE: Floating effect
@Composable
fun SimpleFloat(
    floating: Boolean,
    content: @Composable (offsetY: Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (floating) -10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    content(offsetY)
}

// ✅ ULTRA SIMPLE: Glow effect
@Composable
fun SimpleGlow(
    glowing: Boolean,
    content: @Composable (glowIntensity: Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val intensity by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (glowing) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_intensity"
    )

    content(intensity)
}

// ✅ ULTRA SIMPLE: Background waves (no complex drawing)
@Composable
fun SimpleWaveBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw 3 simple circles that move
        repeat(3) { index ->
            val x = (waveOffset + index * 150) % (width + 100)
            val y = height / 2 + (index * 50) - 100
            val color = when (index) {
                0 -> InkspiraPrimary.copy(alpha = 0.1f)
                1 -> InkspiraSecondary.copy(alpha = 0.1f)
                else -> InkspiraTertiary.copy(alpha = 0.1f)
            }

            drawCircle(
                color = color,
                radius = 80f + (index * 20),
                center = Offset(x, y)
            )
        }
    }
}

// ✅ ULTRA SIMPLE: Fade in/out animation
@Composable
fun SimpleFade(
    visible: Boolean,
    content: @Composable (alpha: Float) -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "fade_alpha"
    )

    content(alpha)
}
