package com.example.inkspira_adigitalartportfolio.view.components.artwork

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.view.components.common.EmptyGalleryComponent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtworkMasonryGrid(
    artworks: List<ArtworkModel>,
    favoritedArtworkIds: Set<String>,
    onArtworkClick: (ArtworkModel) -> Unit,
    onFavoriteToggle: (ArtworkModel) -> Unit,
    modifier: Modifier = Modifier,
    emptyTitle: String = "No Artworks Found",
    emptySubtitle: String = "Check back later for amazing artworks or start creating your own!",
    onEmptyActionClick: (() -> Unit)? = null,
    emptyActionText: String? = null
) {
    if (artworks.isEmpty()) {
        EmptyGalleryComponent(
            title = emptyTitle,
            subtitle = emptySubtitle,
            actionText = emptyActionText,
            onActionClick = onEmptyActionClick,
            modifier = modifier
        )
        return
    }

    val configuration = LocalConfiguration.current
    val columnCount = when {
        configuration.screenWidthDp > 800 -> 4
        configuration.screenWidthDp > 600 -> 3
        else -> 2
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columnCount),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = artworks,
            key = { artwork -> artwork.id }
        ) { artwork ->
            ArtworkCard(
                artwork = artwork,
                isFavorited = favoritedArtworkIds.contains(artwork.id),
                onClick = { onArtworkClick(artwork) },
                onFavoriteToggle = { onFavoriteToggle(artwork) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

@Composable
fun ArtworkPortfolioGrid(
    artworks: List<ArtworkModel>,
    favoritedArtworkIds: Set<String>,
    onArtworkClick: (ArtworkModel) -> Unit,
    onFavoriteToggle: (ArtworkModel) -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ArtworkMasonryGrid(
        artworks = artworks,
        favoritedArtworkIds = favoritedArtworkIds,
        onArtworkClick = onArtworkClick,
        onFavoriteToggle = onFavoriteToggle,
        emptyTitle = "Your Portfolio Awaits",
        emptySubtitle = "Start building your digital art portfolio by uploading your first masterpiece.",
        emptyActionText = "Upload Artwork",
        onEmptyActionClick = onUploadClick,
        modifier = modifier
    )
}
