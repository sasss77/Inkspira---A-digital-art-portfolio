package com.example.inkspira_adigitalartportfolio.view.components.user

import UserModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextSecondary

@Composable
fun UserProfileCard(
    user: UserModel,
    isCurrentUser: Boolean = false,
    onEditClick: () -> Unit = {},
    onFollowClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header with Avatar
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Gradient Background Circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    InkspiraPrimary.copy(alpha = 0.3f),
                                    InkspiraSecondary.copy(alpha = 0.2f),
                                    InkspiraTertiary.copy(alpha = 0.1f)
                                ),
                                radius = 200f
                            ),
                            shape = CircleShape
                        )
                )

                // User Avatar
                UserAvatarComponent(
                    user = user,
                    size = 100.dp,
                    showOnlineStatus = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "@${user.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Role Badge
            ArtistBadge(
                role = user.role,
                modifier = Modifier
            )



            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isCurrentUser) {
                    // Edit Profile Button
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InkspiraPrimary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                } else {
                    // Follow Button
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InkspiraSecondary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Follow")
                    }

                    // Message Button
                    OutlinedButton(
                        onClick = { /* Handle message */ },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                listOf(InkspiraPrimary, InkspiraSecondary)
                            )
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = InkspiraPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Message",
                            color = InkspiraPrimary
                        )
                    }
                }
            }
        }
    }
}
