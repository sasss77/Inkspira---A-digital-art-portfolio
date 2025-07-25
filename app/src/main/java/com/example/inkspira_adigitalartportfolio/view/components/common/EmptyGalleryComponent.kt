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
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary

@Composable
fun EmptyGalleryComponent(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Palette,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Artistic Icon with Gradient Background
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            InkspiraPrimary.copy(alpha = 0.2f),
                            InkspiraSecondary.copy(alpha = 0.15f),
                            InkspiraTertiary.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(60.dp),
                tint = InkspiraPrimary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )

        // Action Button
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onActionClick,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = InkspiraPrimary
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = actionText,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun EmptyFavoritesComponent(
    modifier: Modifier = Modifier,
    onBrowseArtClick: () -> Unit
) {
    EmptyGalleryComponent(
        title = "No Favorites Yet",
        subtitle = "Discover amazing artworks in the gallery and add them to your favorites collection.",
        icon = Icons.Default.FavoriteBorder,
        actionText = "Browse Gallery",
        onActionClick = onBrowseArtClick,
        modifier = modifier
    )
}

@Composable
fun EmptyPortfolioComponent(
    modifier: Modifier = Modifier,
    onUploadClick: () -> Unit
) {
    EmptyGalleryComponent(
        title = "Your Portfolio Awaits",
        subtitle = "Start building your digital art portfolio by uploading your first masterpiece.",
        icon = Icons.Default.Add,
        actionText = "Upload Artwork",
        onActionClick = onUploadClick,
        modifier = modifier
    )
}

@Composable
fun EmptySearchComponent(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    EmptyGalleryComponent(
        title = "No Results Found",
        subtitle = "We couldn't find any artworks matching \"$searchQuery\". Try different keywords or browse all artworks.",
        icon = Icons.Default.SearchOff,
        modifier = modifier
    )
}
