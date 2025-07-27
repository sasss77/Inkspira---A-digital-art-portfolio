package com.example.inkspira_adigitalartportfolio.view.components.artwork

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.components.common.GradientBackground
import com.example.inkspira_adigitalartportfolio.view.components.common.GradientType
import com.example.inkspira_adigitalartportfolio.view.components.common.InkspiraAppBar
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.SuccessColor
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.WarningColor

@Composable
fun ArtworkDetailView(
    artwork: ArtworkModel,
    isFavorited: Boolean,
    canEdit: Boolean,
    onFavoriteToggle: () -> Unit,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GradientBackground(
        gradientType = GradientType.CANVAS,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Custom App Bar
            InkspiraAppBar(
                title = artwork.title,
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onBackClick,
                showGradient = false,
                actions = {
                    // Edit button (if user can edit)
                    if (canEdit) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit artwork",
                                tint = Color.White
                            )
                        }
                    }

                    // Favorite button
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorited) InkspiraSecondary else Color.White
                        )
                    }
                }
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Main Image with Zoom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp, max = 500.dp)
                        .padding(16.dp)
                ) {
                    ImageZoomComponent(
                        imageUrl = artwork.imageUrl,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                // Artwork Information Card
                ArtworkInfoCard(
                    artwork = artwork,
                    modifier = Modifier.padding(16.dp)
                )



                // Spacer for bottom navigation
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ArtworkInfoCard(
    artwork: ArtworkModel,
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
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Artist Info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "by @${artwork.artistUsername}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkspiraPrimary
                )
            }

            // Description
            if (artwork.description.isNotEmpty()) {
                Text(
                    text = artwork.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }

            // Upload Date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )

            }

            // Visibility Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (artwork.isPublic) Icons.Default.Public else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (artwork.isPublic) SuccessColor else WarningColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (artwork.isPublic) "Public artwork" else "Private artwork",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (artwork.isPublic) SuccessColor else WarningColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TagsSection(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Tags Flow Layout (simulated with wrapping)
        TagsFlowLayout(tags = tags)
    }
}

@Composable
private fun TagsFlowLayout(
    tags: List<String>
) {
    // Simple row-based layout for tags
    // In a real app, you might want to use a flow layout library
    val chunkedTags = tags.chunked(3) // Group tags into rows of 3

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedTags.forEach { rowTags ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTags.forEach { tag ->
                    TagChip(tag = tag)
                }
            }
        }
    }
}

@Composable
private fun TagChip(
    tag: String
) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        InkspiraPrimary.copy(alpha = 0.2f),
                        InkspiraSecondary.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "#$tag",
            style = MaterialTheme.typography.labelMedium,
            color = InkspiraPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
