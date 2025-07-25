package com.example.inkspira_adigitalartportfolio.view.components.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary

@Composable
fun ArtistBadge(
    role: UserRole,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val (badgeColor, textColor, icon, roleText) = when (role) {
        UserRole.ARTIST -> Tuple4(
            Brush.horizontalGradient(listOf(InkspiraPrimary, InkspiraSecondary)),
            Color.White,
            Icons.Default.Palette,
            "Artist"
        )
        UserRole.VIEWER -> Tuple4(
            Brush.horizontalGradient(listOf(InkspiraTertiary, InkspiraPrimary.copy(alpha = 0.7f))),
            Color.White,
            Icons.Default.Visibility,
            "Art Lover"
        )
        UserRole.BOTH -> Tuple4(
            Brush.horizontalGradient(listOf(
                InkspiraPrimary,
                InkspiraSecondary,
                InkspiraTertiary
            )),
            Color.White,
            Icons.Default.AutoAwesome,
            "Creator & Collector"
        )
    }

    Box(
        modifier = modifier
            .background(
                brush = badgeColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = roleText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// Helper data class for multiple return values
private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
