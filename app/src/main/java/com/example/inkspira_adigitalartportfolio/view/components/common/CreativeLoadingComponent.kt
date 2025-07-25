package com.example.inkspira_adigitalartportfolio.view.components.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary
import kotlin.math.*

@Composable
fun CreativeLoadingComponent(
    modifier: Modifier = Modifier,
    loadingText: String = "Creating magic...",
    showText: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Creative Paint Brush Loading Animation
        Canvas(
            modifier = Modifier.size(120.dp)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 4

            rotate(rotation, center) {
                drawPaintBrushLoading(center, radius, scale)
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = loadingText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = InkspiraPrimary
            )
        }
    }
}

private fun DrawScope.drawPaintBrushLoading(center: Offset, radius: Float, scale: Float) {
    val colors = listOf(
        InkspiraPrimary,
        InkspiraSecondary,
        InkspiraTertiary
    )

    repeat(3) { index ->
        val angle = (index * 120f) * PI / 180f
        val scaledRadius = radius * scale

        val startOffset = Offset(
            center.x + cos(angle).toFloat() * scaledRadius * 0.5f,
            center.y + sin(angle).toFloat() * scaledRadius * 0.5f
        )

        val endOffset = Offset(
            center.x + cos(angle).toFloat() * scaledRadius,
            center.y + sin(angle).toFloat() * scaledRadius
        )

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(colors[index].copy(alpha = 0.3f), colors[index]),
                start = startOffset,
                end = endOffset
            ),
            start = startOffset,
            end = endOffset,
            strokeWidth = 8.dp.toPx()
        )
    }

    // Center dot
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(InkspiraPrimary, InkspiraSecondary),
            radius = 20.dp.toPx()
        ),
        radius = 12.dp.toPx() * scale,
        center = center
    )
}

@Composable
fun InlineLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Int = 32
) {
    val infiniteTransition = rememberInfiniteTransition(label = "inline_loading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "inline_rotation"
    )

    Canvas(
        modifier = modifier.size(size.dp)
    ) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.minDimension / 6

        rotate(rotation, center) {
            repeat(3) { index ->
                val angle = (index * 120f) * PI / 180f
                val offset = Offset(
                    center.x + cos(angle).toFloat() * radius,
                    center.y + sin(angle).toFloat() * radius
                )

                drawCircle(
                    color = when(index) {
                        0 -> InkspiraPrimary
                        1 -> InkspiraSecondary
                        else -> InkspiraTertiary
                    },
                    radius = 4.dp.toPx(),
                    center = offset
                )
            }
        }
    }
}
