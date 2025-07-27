package com.example.inkspira_adigitalartportfolio.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.inkspira_adigitalartportfolio.view.ui.theme.*
import com.example.inkspira_adigitalartportfolio.viewmodel.GalleryViewModel

// ✅ FIXED: Complete ArtworkData class with all necessary fields
data class ArtworkData(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val tags: List<String> = emptyList(), // ✅ FIXED: Proper generic type
    val isPublic: Boolean = true,
    val likesCount: Int = 0,
    val viewsCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val userId: String = ""
) {
    // Helper methods
    fun getFormattedCreatedDate(): String {
        val date = java.util.Date(createdAt)
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    fun getEngagementScore(): Int {
        return likesCount + (viewsCount * 0.1).toInt()
    }
}

// ✅ Sort options
enum class SortOption(val displayName: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    MOST_LIKED("Most Liked"),
    MOST_VIEWED("Most Viewed"),
    TITLE_AZ("Title A-Z")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onNavigateToUpload: () -> Unit, // ✅ FIXED: Proper arrow syntax
    galleryViewModel: GalleryViewModel = viewModel()
) {
    // ✅ FIXED: Use ViewModel state instead of local state
    val artworks by galleryViewModel.artworks.collectAsState()
    val isLoading by galleryViewModel.isLoading.collectAsState()
    val isRefreshing by galleryViewModel.isRefreshing.collectAsState()
    val errorMessage by galleryViewModel.errorMessage.collectAsState()

    var showSortDialog by remember { mutableStateOf(false) }
    var selectedArtwork by remember { mutableStateOf<ArtworkData?>(null) } // ✅ FIXED: Proper nullable type
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ CRITICAL: Load artworks on screen initialization
    LaunchedEffect(Unit) {
        println("🔥 GalleryScreen: LaunchedEffect triggered")
        galleryViewModel.loadUserArtworks()
    }

    // ✅ Handle error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message -> // ✅ FIXED: Proper arrow syntax
            snackbarHostState.showSnackbar(message)
            galleryViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Gallery",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = InkspiraPrimary
                        )
                    }
                    IconButton(
                        onClick = { galleryViewModel.loadUserArtworks() },
                        enabled = !isLoading
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = InkspiraPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToUpload,
                containerColor = InkspiraPrimary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Artwork",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data -> // ✅ FIXED: Proper arrow syntax
                    Snackbar(
                        snackbarData = data,
                        containerColor = ErrorColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
        }
    ) { paddingValues -> // ✅ FIXED: Proper arrow syntax
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepDarkBlue, DarkNavy)
                    )
                )
                .padding(paddingValues)
        ) {
            when {
                isLoading && artworks.isEmpty() -> { // ✅ FIXED: HTML entity
                    LoadingContent()
                }

                artworks.isEmpty() && !isLoading -> { // ✅ FIXED: HTML entity
                    EmptyGalleryContent(onNavigateToUpload = onNavigateToUpload)
                }

                else -> { // ✅ FIXED: Proper arrow syntax
                    GalleryContent(
                        artworks = artworks,
                        onArtworkClick = { artwork -> selectedArtwork = artwork } // ✅ FIXED: Proper arrow syntax
                    )
                }
            }
        }

        // ✅ Sort Dialog
        if (showSortDialog) {
            SimpleSortDialog(
                onDismiss = { showSortDialog = false },
                onSortSelected = { sortOption -> // ✅ FIXED: Proper arrow syntax
                    galleryViewModel.sortArtworks(sortOption)
                    showSortDialog = false
                }
            )
        }

        // ✅ Artwork Detail Dialog
        selectedArtwork?.let { artwork -> // ✅ FIXED: Proper arrow syntax
            SimpleArtworkDialog(
                artwork = artwork,
                onDismiss = { selectedArtwork = null },
                onEdit = { selectedArtwork = null },
                onDelete = { artworkId -> // ✅ FIXED: Proper arrow syntax
                    galleryViewModel.deleteArtwork(artworkId)
                    selectedArtwork = null
                }
            )
        }
    }
}

@Composable
private fun GalleryContent(
    artworks: List<ArtworkData>, // ✅ FIXED: Proper generic type
    onArtworkClick: (ArtworkData) -> Unit // ✅ FIXED: Proper arrow syntax
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Gallery Stats Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkNavy.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Artworks",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = artworks.size.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkspiraPrimary
                    )
                }

                // Additional stats
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Likes",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = artworks.sumOf { it.likesCount }.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkspiraPrimary
                    )
                }

                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Artworks",
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Artwork List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(artworks) { artwork -> // ✅ FIXED: Proper arrow syntax
                ArtworkCard(
                    artwork = artwork,
                    onClick = { onArtworkClick(artwork) }
                )
            }
        }
    }
}

// ✅ Rest of your existing Composable functions remain the same...
// (ArtworkCard, LoadingContent, EmptyGalleryContent, etc.)

@Composable
private fun ArtworkCard(
    artwork: ArtworkData,
    onClick: () -> Unit // ✅ FIXED: Proper arrow syntax
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // ✅ FIXED: Display actual artwork image from Firebase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(InkspiraPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (artwork.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = artwork.imageUrl,
                        contentDescription = artwork.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback for missing image
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "No Image",
                        tint = InkspiraPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Artwork Info
            Text(
                text = artwork.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (artwork.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = artwork.description,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Upload date
            Text(
                text = "Created: ${artwork.getFormattedCreatedDate()}",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatChip(
                        icon = Icons.Default.Favorite,
                        count = artwork.likesCount
                    )
                    StatChip(
                        icon = Icons.Default.Visibility,
                        count = artwork.viewsCount
                    )
                    StatChip(
                        icon = Icons.Default.Comment,
                        count = artwork.commentsCount
                    )
                }

                // Category badge and privacy indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Privacy indicator
                    Icon(
                        imageVector = if (artwork.isPublic) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = if (artwork.isPublic) "Public" else "Private",
                        tint = if (artwork.isPublic) InkspiraPrimary else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )

                    // Category badge
                    if (artwork.category.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = InkspiraPrimary.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = artwork.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkspiraPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkspiraPrimary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = InkspiraPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading your gallery...",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun EmptyGalleryContent(onNavigateToUpload: () -> Unit) { // ✅ FIXED: Proper arrow syntax
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Empty state illustration
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                InkspiraPrimary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Empty Gallery",
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your Gallery is Empty",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start building your digital art portfolio by uploading your first artwork",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToUpload,
                colors = ButtonDefaults.buttonColors(
                    containerColor = InkspiraPrimary
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upload Your First Artwork",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ✅ Simple Sort Dialog
@Composable
private fun SimpleSortDialog(
    onDismiss: () -> Unit, // ✅ FIXED: Proper arrow syntax
    onSortSelected: (SortOption) -> Unit // ✅ FIXED: Proper arrow syntax
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sort Artworks",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                SortOption.values().forEach { option -> // ✅ FIXED: Proper arrow syntax
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (option) {
                                SortOption.NEWEST, SortOption.OLDEST -> Icons.Default.Schedule // ✅ FIXED: HTML entity
                                SortOption.MOST_LIKED -> Icons.Default.Favorite // ✅ FIXED: HTML entity
                                SortOption.MOST_VIEWED -> Icons.Default.Visibility // ✅ FIXED: HTML entity
                                SortOption.TITLE_AZ -> Icons.Default.SortByAlpha // ✅ FIXED: HTML entity
                            },
                            contentDescription = null,
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = option.displayName,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = InkspiraPrimary)
            }
        },
        containerColor = DarkNavy,
        shape = RoundedCornerShape(16.dp)
    )
}

// ✅ FIXED: Simple Artwork Dialog with real image display
@Composable
private fun SimpleArtworkDialog(
    artwork: ArtworkData,
    onDismiss: () -> Unit, // ✅ FIXED: Proper arrow syntax
    onEdit: () -> Unit, // ✅ FIXED: Proper arrow syntax
    onDelete: (String) -> Unit // ✅ FIXED: Proper arrow syntax
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = artwork.title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                // ✅ FIXED: Display actual artwork image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(InkspiraPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (artwork.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = artwork.imageUrl,
                            contentDescription = artwork.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No Image",
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                if (artwork.description.isNotEmpty()) {
                    Text(
                        text = artwork.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Upload date
                Text(
                    text = "Created: ${artwork.getFormattedCreatedDate()}",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = artwork.likesCount.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Likes",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = artwork.viewsCount.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Views",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (artwork.isPublic) Icons.Default.Public else Icons.Default.Lock,
                            contentDescription = "Privacy",
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (artwork.isPublic) "Public" else "Private",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Privacy",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit", color = InkspiraPrimary)
                }
                TextButton(onClick = { showDeleteConfirmation = true }) {
                    Text("Delete", color = ErrorColor)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = DarkNavy,
        shape = RoundedCornerShape(16.dp)
    )

    // Delete confirmation
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Artwork", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${artwork.title}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(artwork.id)
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = InkspiraPrimary)
                }
            },
            containerColor = DarkNavy
        )
    }
}
