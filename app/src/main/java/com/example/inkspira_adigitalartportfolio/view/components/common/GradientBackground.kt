package com.example.inkspira_adigitalartportfolio.view.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkerBlue
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DeepDarkBlue
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    gradientType: GradientType = GradientType.PRIMARY,
    content: @Composable BoxScope.() -> Unit
) {
    val gradient = when (gradientType) {
        GradientType.PRIMARY -> Brush.verticalGradient(
            colors = listOf(
                DeepDarkBlue,
                DarkNavy,
                InkspiraPrimary.copy(alpha = 0.1f)
            )
        )
        GradientType.SECONDARY -> Brush.radialGradient(
            colors = listOf(
                InkspiraSecondary.copy(alpha = 0.2f),
                DeepDarkBlue,
                DarkNavy
            )
        )
        GradientType.ARTISTIC -> Brush.linearGradient(
            colors = listOf(
                InkspiraPrimary.copy(alpha = 0.15f),
                InkspiraSecondary.copy(alpha = 0.1f),
                InkspiraTertiary.copy(alpha = 0.08f),
                DeepDarkBlue
            )
        )
        GradientType.CANVAS -> Brush.verticalGradient(
            colors = listOf(
                DarkerBlue,
                DeepDarkBlue
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        content()
    }
}

enum class GradientType {
    PRIMARY, SECONDARY, ARTISTIC, CANVAS
}
