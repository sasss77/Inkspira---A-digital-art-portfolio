package com.example.inkspira_adigitalartportfolio.view.components.user

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DividerColor
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextMuted
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextSecondary

@Composable
fun RoleSwitcher(
    currentRole: UserRole,
    onRoleChange: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "I am here to...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role Options
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleOption(
                    role = UserRole.ARTIST,
                    title = "Create & Share Art",
                    description = "Upload and showcase my digital artworks",
                    icon = Icons.Default.Palette,
                    isSelected = currentRole == UserRole.ARTIST,
                    onSelect = { onRoleChange(UserRole.ARTIST) },
                    enabled = enabled
                )

                RoleOption(
                    role = UserRole.VIEWER,
                    title = "Discover Art",
                    description = "Explore and collect amazing digital artworks",
                    icon = Icons.Default.Visibility,
                    isSelected = currentRole == UserRole.VIEWER,
                    onSelect = { onRoleChange(UserRole.VIEWER) },
                    enabled = enabled
                )

                RoleOption(
                    role = UserRole.BOTH,
                    title = "Create & Discover",
                    description = "Both create my own art and explore others' work",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = currentRole == UserRole.BOTH,
                    onSelect = { onRoleChange(UserRole.BOTH) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun RoleOption(
    role: UserRole,
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) InkspiraPrimary.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(300),
        label = "background_color"
    )

    val borderColor = if (isSelected) InkspiraPrimary else DividerColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled) { onSelect() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with animated background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = if (isSelected) {
                            Brush.radialGradient(
                                colors = listOf(
                                    InkspiraPrimary.copy(alpha = 0.3f),
                                    InkspiraPrimary.copy(alpha = 0.1f)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    TextMuted.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        },
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) InkspiraPrimary else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) InkspiraPrimary else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }

            // Selection Indicator
            RadioButton(
                selected = isSelected,
                onClick = { onSelect() },
                enabled = enabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = InkspiraPrimary,
                    unselectedColor = TextMuted
                )
            )
        }
    }
}
