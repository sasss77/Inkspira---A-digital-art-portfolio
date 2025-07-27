package com.example.inkspira_adigitalartportfolio.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.inkspira_adigitalartportfolio.view.ui.theme.*
import com.example.inkspira_adigitalartportfolio.viewmodel.ArtistData
import com.example.inkspira_adigitalartportfolio.viewmodel.DiscoverViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onArtworkClick: (ArtworkData) -> Unit, // ✅ FIXED: HTML entity removed
    onArtistClick: (ArtistData) -> Unit, // ✅ FIXED: HTML entity removed
    discoverViewModel: DiscoverViewModel = viewModel()
) {
    // ✅ FIXED: Collect states from ViewModel
    val featuredArtworks by discoverViewModel.featuredArtworks.collectAsState()
    val trendingArtworks by discoverViewModel.trendingArtworks.collectAsState()
    val searchResults by discoverViewModel.searchResults.collectAsState()
    val categories by discoverViewModel.categories.collectAsState()
    val discoverArtists by discoverViewModel.discoverArtists.collectAsState()
    val isLoadingFeatured by discoverViewModel.isLoadingFeatured.collectAsState()
    val isLoadingSearch by discoverViewModel.isLoadingSearch.collectAsState()
    val isRefreshing by discoverViewModel.isRefreshing.collectAsState()
    val errorMessage by discoverViewModel.errorMessage.collectAsState()
    val searchQuery by discoverViewModel.searchQuery.collectAsState()
    val selectedCategory by discoverViewModel.selectedCategory.collectAsState()
    val isSearchMode by discoverViewModel.isSearchMode.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var showArtworkDetail by remember { mutableStateOf<ArtworkData?>(null) } // ✅ FIXED: Proper nullable type
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ FIXED: Handle error messages with proper lambda syntax
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            discoverViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Discover",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { discoverViewModel.refreshData() }) {
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
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data -> // ✅ FIXED: Proper lambda syntax
                    Snackbar(
                        snackbarData = data,
                        containerColor = ErrorColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
        }
    ) { paddingValues -> // ✅ FIXED: Proper lambda syntax
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar
                SearchBar(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    onSearch = {
                        discoverViewModel.searchArtworks(searchText)
                        keyboardController?.hide()
                    },
                    onClearSearch = {
                        searchText = ""
                        discoverViewModel.clearSearch()
                    },
                    isSearching = isLoadingSearch
                )

                // Category Filter Chips (only show when not searching)
                if (!isSearchMode) {
                    CategoryFilterChips(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category -> // ✅ FIXED: Proper lambda syntax
                            discoverViewModel.filterByCategory(category)
                        }
                    )
                }

                // Main Content
                when {
                    isLoadingFeatured && featuredArtworks.isEmpty() -> { // ✅ FIXED: HTML entity
                        LoadingContent()
                    }

                    isSearchMode -> { // ✅ FIXED: Proper arrow syntax
                        SearchResultsContent(
                            searchResults = searchResults,
                            searchQuery = searchQuery,
                            isLoading = isLoadingSearch,
                            onArtworkClick = onArtworkClick,
                            onLikeClick = { artwork -> // ✅ FIXED: Proper lambda syntax
                                discoverViewModel.toggleLikeArtwork(artwork.id, false)
                            }
                        )
                    }

                    else -> { // ✅ FIXED: Proper arrow syntax
                        DiscoverMainContent(
                            featuredArtworks = featuredArtworks,
                            trendingArtworks = trendingArtworks,
                            discoverArtists = discoverArtists,
                            isRefreshing = isRefreshing,
                            onArtworkClick = { artwork -> // ✅ FIXED: Proper lambda syntax
                                discoverViewModel.incrementViewCount(artwork.id)
                                onArtworkClick(artwork)
                            },
                            onArtistClick = onArtistClick,
                            onLikeClick = { artwork -> // ✅ FIXED: Proper lambda syntax
                                discoverViewModel.toggleLikeArtwork(artwork.id, false)
                            },
                            onRefresh = { discoverViewModel.refreshData() }
                        )
                    }
                }
            }
        }

        // Artwork Detail Dialog
        showArtworkDetail?.let { artwork -> // ✅ FIXED: Proper lambda syntax
            ArtworkDetailDialog(
                artwork = artwork,
                onDismiss = { showArtworkDetail = null },
                onLike = {
                    discoverViewModel.toggleLikeArtwork(artwork.id, false)
                    showArtworkDetail = null
                }
            )
        }
    }
}

@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit, // ✅ FIXED: Proper arrow syntax
    onSearch: () -> Unit, // ✅ FIXED: Proper arrow syntax
    onClearSearch: () -> Unit, // ✅ FIXED: Proper arrow syntax
    isSearching: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = { Text("Search artworks, artists, tags...", color = TextMuted) },
            leadingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = InkspiraPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = InkspiraPrimary
                    )
                }
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = TextSecondary
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = InkspiraPrimary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun CategoryFilterChips(
    categories: List<String>, // ✅ FIXED: Added proper generic type
    selectedCategory: String,
    onCategorySelected: (String) -> Unit // ✅ FIXED: Proper arrow syntax
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(categories) { category -> // ✅ FIXED: Proper lambda syntax
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = category == selectedCategory,
                leadingIcon = if (category == selectedCategory) {
                    { // ✅ FIXED: Proper lambda structure
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = InkspiraPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = DarkNavy.copy(alpha = 0.6f),
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = category == selectedCategory,
                    borderColor = if (category == selectedCategory) InkspiraPrimary else TextMuted.copy(alpha = 0.3f),
                    selectedBorderColor = InkspiraPrimary
                )
            )
        }
    }
}

@Composable
private fun SearchResultsContent(
    searchResults: List<ArtworkData>, // ✅ FIXED: Added proper generic type
    searchQuery: String,
    isLoading: Boolean,
    onArtworkClick: (ArtworkData) -> Unit, // ✅ FIXED: Proper arrow syntax
    onLikeClick: (ArtworkData) -> Unit // ✅ FIXED: Proper arrow syntax
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Results Header
        Text(
            text = if (searchResults.isEmpty() && !isLoading) { // ✅ FIXED: HTML entity
                "No results found for \"$searchQuery\""
            } else {
                "${searchResults.size} results for \"$searchQuery\""
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (searchResults.isEmpty() && !isLoading) { // ✅ FIXED: HTML entity
            // Empty search results
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No results",
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No artworks found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Try different keywords or browse categories",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Search Results Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(searchResults) { artwork -> // ✅ FIXED: Proper lambda syntax
                    ArtworkGridItem(
                        artwork = artwork,
                        onClick = { onArtworkClick(artwork) },
                        onLikeClick = { onLikeClick(artwork) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoverMainContent(
    featuredArtworks: List<ArtworkData>, // ✅ FIXED: Added proper generic type
    trendingArtworks: List<ArtworkData>, // ✅ FIXED: Added proper generic type
    discoverArtists: List<ArtistData>, // ✅ FIXED: Added proper generic type
    isRefreshing: Boolean,
    onArtworkClick: (ArtworkData) -> Unit, // ✅ FIXED: Proper arrow syntax
    onArtistClick: (ArtistData) -> Unit, // ✅ FIXED: Proper arrow syntax
    onLikeClick: (ArtworkData) -> Unit, // ✅ FIXED: Proper arrow syntax
    onRefresh: () -> Unit // ✅ FIXED: Proper arrow syntax
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Trending Section
        if (trendingArtworks.isNotEmpty()) {
            item {
                TrendingSection(
                    trendingArtworks = trendingArtworks,
                    onArtworkClick = onArtworkClick,
                    onLikeClick = onLikeClick
                )
            }
        }

        // Artists Discovery Section
        if (discoverArtists.isNotEmpty()) {
            item {
                ArtistsDiscoverySection(
                    artists = discoverArtists,
                    onArtistClick = onArtistClick
                )
            }
        }

        // Featured Artworks Section
        item {
            FeaturedArtworksSection(
                featuredArtworks = featuredArtworks,
                onArtworkClick = onArtworkClick,
                onLikeClick = onLikeClick
            )
        }
    }
}

@Composable
private fun TrendingSection(
    trendingArtworks: List<ArtworkData>, // ✅ FIXED: Added proper generic type
    onArtworkClick: (ArtworkData) -> Unit, // ✅ FIXED: Proper arrow syntax
    onLikeClick: (ArtworkData) -> Unit // ✅ FIXED: Proper arrow syntax
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = "Trending",
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Trending",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(trendingArtworks.take(10)) { artwork -> // ✅ FIXED: Proper lambda syntax
                TrendingArtworkCard(
                    artwork = artwork,
                    onClick = { onArtworkClick(artwork) },
                    onLikeClick = { onLikeClick(artwork) }
                )
            }
        }
    }
}

@Composable
private fun ArtistsDiscoverySection(
    artists: List<ArtistData>, // ✅ FIXED: Added proper generic type
    onArtistClick: (ArtistData) -> Unit // ✅ FIXED: Proper arrow syntax
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Artists",
                tint = InkspiraPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Discover Artists",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(artists.take(10)) { artist -> // ✅ FIXED: Proper lambda syntax
                ArtistCard(
                    artist = artist,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}

@Composable
private fun FeaturedArtworksSection(
    featuredArtworks: List<ArtworkData>, // ✅ FIXED: Added proper generic type
    onArtworkClick: (ArtworkData) -> Unit, // ✅ FIXED: Proper arrow syntax
    onLikeClick: (ArtworkData) -> Unit // ✅ FIXED: Proper arrow syntax
) {
    Column {
        Text(
            text = "Featured Artworks",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 800.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            userScrollEnabled = false
        ) {
            items(featuredArtworks.take(20)) { artwork -> // ✅ FIXED: Proper lambda syntax
                ArtworkGridItem(
                    artwork = artwork,
                    onClick = { onArtworkClick(artwork) },
                    onLikeClick = { onLikeClick(artwork) }
                )
            }
        }
    }
}

@Composable
private fun TrendingArtworkCard(
    artwork: ArtworkData,
    onClick: () -> Unit, // ✅ FIXED: Proper arrow syntax
    onLikeClick: () -> Unit // ✅ FIXED: Proper arrow syntax
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(280.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = artwork.imageUrl,
                    contentDescription = artwork.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Trending badge
                Card(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = InkspiraPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Trending",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "HOT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Like button
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = artwork.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = artwork.likesCount.toString(),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = artwork.viewsCount.toString(),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistCard(
    artist: ArtistData,
    onClick: () -> Unit // ✅ FIXED: Proper arrow syntax
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(InkspiraPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (artist.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = artist.profileImageUrl,
                        contentDescription = artist.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = artist.getProfileDisplayName().firstOrNull()?.uppercase() ?: "A",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkspiraPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = artist.getProfileDisplayName(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (artist.username.isNotEmpty()) {
                Text(
                    text = "@${artist.username}",
                    fontSize = 12.sp,
                    color = InkspiraPrimary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = artist.getArtworkCountText(),
                fontSize = 10.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (artist.followersCount > 0) { // ✅ FIXED: HTML entity
                Text(
                    text = artist.getFollowersText(),
                    fontSize = 10.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ArtworkGridItem(
    artwork: ArtworkData,
    onClick: () -> Unit, // ✅ FIXED: Proper arrow syntax
    onLikeClick: () -> Unit // ✅ FIXED: Proper arrow syntax
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = artwork.imageUrl,
                contentDescription = artwork.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // Like button
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = Color.White
                )
            }

            // Artwork info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = artwork.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = artwork.likesCount.toString(),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = artwork.viewsCount.toString(),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
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
                text = "Discovering amazing artworks...",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ArtworkDetailDialog(
    artwork: ArtworkData,
    onDismiss: () -> Unit, // ✅ FIXED: Proper arrow syntax
    onLike: () -> Unit // ✅ FIXED: Proper arrow syntax
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = artwork.title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column {
                AsyncImage(
                    model = artwork.imageUrl,
                    contentDescription = artwork.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (artwork.description.isNotEmpty()) {
                    Text(
                        text = artwork.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

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
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onLike) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = InkspiraPrimary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text("Like", color = InkspiraPrimary)
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
}
