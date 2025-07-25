package com.example.inkspira_adigitalartportfolio.view.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InkspiraAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    showGradient: Boolean = true
) {
    val appBarBackground = if (showGradient) {
        Brush.horizontalGradient(
            colors = listOf(
                InkspiraPrimary.copy(alpha = 0.9f),
                InkspiraSecondary.copy(alpha = 0.8f),
                InkspiraTertiary.copy(alpha = 0.7f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(DarkNavy, DarkNavy)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(appBarBackground),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation Icon
            navigationIcon?.let { icon ->
                IconButton(
                    onClick = onNavigationClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Navigate Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            // Actions
            actions()
        }
    }
}

@Composable
fun InkspiraAppBarAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
