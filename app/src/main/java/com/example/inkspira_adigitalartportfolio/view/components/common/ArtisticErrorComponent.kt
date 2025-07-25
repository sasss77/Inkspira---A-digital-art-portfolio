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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.ErrorColor
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary

@Composable
fun ArtisticErrorComponent(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Error,
    onRetryClick: (() -> Unit)? = null,
    retryText: String = "Try Again"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error Icon with Gradient Background
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ErrorColor.copy(alpha = 0.2f),
                                ErrorColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                    tint = ErrorColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Error Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            // Retry Button
            onRetryClick?.let { retry ->
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = retry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkspiraPrimary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = retryText,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkErrorComponent(
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit
) {
    ArtisticErrorComponent(
        title = "Connection Issue",
        message = "Unable to connect to Inkspira servers. Please check your internet connection and try again.",
        icon = Icons.Default.CloudOff,
        onRetryClick = onRetryClick,
        retryText = "Reconnect",
        modifier = modifier
    )
}

@Composable
fun NotFoundErrorComponent(
    title: String = "Content Not Found",
    message: String = "The content you're looking for doesn't exist or has been moved.",
    modifier: Modifier = Modifier,
    onGoBackClick: (() -> Unit)? = null
) {
    ArtisticErrorComponent(
        title = title,
        message = message,
        icon = Icons.Default.SearchOff,
        onRetryClick = onGoBackClick,
        retryText = "Go Back",
        modifier = modifier
    )
}
