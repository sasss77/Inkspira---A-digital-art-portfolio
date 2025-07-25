package com.example.inkspira_adigitalartportfolio.view.components.user

import UserModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.SuccessColor
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextMuted

@Composable
fun UserAvatarComponent(
    user: UserModel,
    size: Dp = 56.dp,
    showOnlineStatus: Boolean = false,
    showBorder: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Avatar Container
        Box(
            modifier = Modifier
                .size(size)
                .let { mod ->
                    if (showBorder) {
                        mod.border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    InkspiraPrimary,
                                    InkspiraSecondary,
                                    InkspiraTertiary
                                )
                            ),
                            shape = CircleShape
                        )
                    } else mod
                }
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (user.profileImageUrl.isNotEmpty()) {
                // User's Profile Image
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "${user.displayName}'s avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                // Fallback Avatar with Initials
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    InkspiraPrimary.copy(alpha = 0.8f),
                                    InkspiraSecondary.copy(alpha = 0.6f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.displayName.isNotEmpty()) {
                        // Show initials
                        Text(
                            text = getInitials(user.displayName),
                            color = Color.White,
                            fontSize = (size.value * 0.4f).sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Show person icon
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default avatar",
                            tint = Color.White,
                            modifier = Modifier.size(size * 0.6f)
                        )
                    }
                }
            }
        }

        // Online Status Indicator
        if (showOnlineStatus) {
            Box(
                modifier = Modifier
                    .size(size * 0.25f)
                    .align(Alignment.BottomEnd)
                    .background(
                        color = SuccessColor,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = DarkNavy,
                        shape = CircleShape
                    )
            )
        }
    }
}

// Helper function to get initials from display name
private fun getInitials(displayName: String): String {
    return displayName
        .split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercaseChar() ?: "" }
        .joinToString("")
        .take(2)
}

@Composable
fun AvatarPlaceholder(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TextMuted.copy(alpha = 0.3f),
                        TextMuted.copy(alpha = 0.1f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Avatar placeholder",
            tint = TextMuted,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}
