package com.example.inkspira_adigitalartportfolio.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.inkspira_adigitalartportfolio.view.ui.theme.*
import com.example.inkspira_adigitalartportfolio.viewmodel.ArtworkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArtworkScreen(
    artwork: ArtworkData,
    onNavigateBack: () -> Unit,
    onEditSuccess: () -> Unit,
    artworkViewModel: ArtworkViewModel = viewModel()
) {
    // State variables - Initialize with current artwork data
    var title by remember { mutableStateOf(artwork.title) }
    var description by remember { mutableStateOf(artwork.description) }
    var selectedCategory by remember { mutableStateOf(artwork.category) }
    var isPublic by remember { mutableStateOf(artwork.isPublic) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    // ViewModel states
    val uiState by artworkViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Check for unsaved changes
    LaunchedEffect(title, description, selectedCategory, isPublic) {
        hasUnsavedChanges = title != artwork.title ||
                description != artwork.description ||
                selectedCategory != artwork.category ||
                isPublic != artwork.isPublic
    }

    // Handle success message
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onEditSuccess()
            artworkViewModel.clearSuccessMessage()
        }
    }

    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            artworkViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Artwork",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            // Show confirmation dialog if there are unsaved changes
                            showSaveConfirmation = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                    }
                },
                actions = {
                    // Save button in app bar
                    TextButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                updateArtwork(
                                    artworkViewModel = artworkViewModel,
                                    artworkId = artwork.id,
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = selectedCategory,
                                    isPublic = isPublic
                                )
                            }
                        },
                        enabled = hasUnsavedChanges && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = InkspiraPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "SAVE",
                                color = if (hasUnsavedChanges) InkspiraPrimary else TextMuted,
                                fontWeight = FontWeight.Bold
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
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (data.visuals.message.contains("success", ignoreCase = true))
                            InkspiraPrimary else ErrorColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
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
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Current Artwork Preview Section
                CurrentArtworkPreview(artwork = artwork)

                Spacer(modifier = Modifier.height(24.dp))

                // Edit Form Section
                EditArtworkForm(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    selectedCategory = selectedCategory,
                    onCategoryClick = { showCategoryDialog = true },
                    isPublic = isPublic,
                    onPrivacyChange = { isPublic = it },
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Save Changes Button
                SaveChangesButton(
                    hasChanges = hasUnsavedChanges,
                    isLoading = uiState.isLoading,
                    onSave = {
                        updateArtwork(
                            artworkViewModel = artworkViewModel,
                            artworkId = artwork.id,
                            title = title.trim(),
                            description = description.trim(),
                            category = selectedCategory,
                            isPublic = isPublic
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Category Selection Dialog
        if (showCategoryDialog) {
            CategorySelectionDialog(
                currentCategory = selectedCategory,
                onDismiss = { showCategoryDialog = false },
                onCategorySelected = { category ->
                    selectedCategory = category
                    showCategoryDialog = false
                }
            )
        }

        // Unsaved Changes Confirmation Dialog
        if (showSaveConfirmation) {
            UnsavedChangesDialog(
                onSave = {
                    updateArtwork(
                        artworkViewModel = artworkViewModel,
                        artworkId = artwork.id,
                        title = title.trim(),
                        description = description.trim(),
                        category = selectedCategory,
                        isPublic = isPublic
                    )
                    showSaveConfirmation = false
                },
                onDiscard = {
                    showSaveConfirmation = false
                    onNavigateBack()
                },
                onCancel = {
                    showSaveConfirmation = false
                }
            )
        }
    }
}

@Composable
private fun CurrentArtworkPreview(artwork: ArtworkData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Artwork Image
            AsyncImage(
                model = artwork.imageUrl,
                contentDescription = artwork.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for text readability
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

            // Current artwork info overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Current Version",
                    fontSize = 12.sp,
                    color = InkspiraPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = artwork.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (artwork.description.isNotEmpty()) {
                    Text(
                        text = artwork.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
            }

            // Privacy indicator
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (artwork.isPublic) InkspiraPrimary.copy(alpha = 0.9f)
                    else Color.Gray.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (artwork.isPublic) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (artwork.isPublic) "Public" else "Private",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EditArtworkForm(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryClick: () -> Unit,
    isPublic: Boolean,
    onPrivacyChange: (Boolean) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Edit Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Artwork Title *") },
                placeholder = { Text("Enter a captivating title") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = null,
                        tint = InkspiraPrimary
                    )
                },
                enabled = !isLoading,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InkspiraPrimary,
                    focusedLabelColor = InkspiraPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                placeholder = { Text("Describe your artwork, inspiration, or technique") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = InkspiraPrimary
                    )
                },
                enabled = !isLoading,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InkspiraPrimary,
                    focusedLabelColor = InkspiraPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) { onCategoryClick() }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = { },
                    label = { Text("Category *") },
                    placeholder = { Text("Select artwork category") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = InkspiraPrimary
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Category",
                            tint = if (selectedCategory.isNotEmpty()) InkspiraPrimary else TextSecondary
                        )
                    },
                    enabled = false,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkspiraPrimary,
                        focusedLabelColor = InkspiraPrimary,
                        disabledBorderColor = if (selectedCategory.isNotEmpty()) InkspiraPrimary else TextMuted,
                        disabledLabelColor = if (selectedCategory.isNotEmpty()) InkspiraPrimary else TextMuted,
                        disabledLeadingIconColor = InkspiraPrimary,
                        disabledTrailingIconColor = if (selectedCategory.isNotEmpty()) InkspiraPrimary else TextSecondary,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Setting
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = DeepDarkBlue.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                            contentDescription = null,
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isPublic) "Public" else "Private",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = if (isPublic) "Visible to everyone in Discover" else "Only visible to you",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isPublic,
                        onCheckedChange = onPrivacyChange,
                        enabled = !isLoading,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = InkspiraPrimary,
                            checkedTrackColor = InkspiraPrimary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveChangesButton(
    hasChanges: Boolean,
    isLoading: Boolean,
    onSave: () -> Unit
) {
    Button(
        onClick = onSave,
        enabled = hasChanges && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = InkspiraPrimary,
            disabledContainerColor = InkspiraPrimary.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Saving Changes...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (hasChanges) "Save Changes" else "No Changes Made",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CategorySelectionDialog(
    currentCategory: String,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "3D Art",
        "Abstract",
        "Architecture",
        "Character Design",
        "Concept Art",
        "Digital Art",
        "Fan Art",
        "Illustration",
        "Landscape",
        "Mixed Media",
        "Photography",
        "Portrait",
        "Sculpture",
        "Traditional Art",
        "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Select Category",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(categories) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { onCategorySelected(category) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (category == currentCategory)
                                InkspiraPrimary.copy(alpha = 0.1f) else Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (category) {
                                    "Digital Art" -> Icons.Default.Computer
                                    "Traditional Art" -> Icons.Default.Brush
                                    "Photography" -> Icons.Default.CameraAlt
                                    "3D Art" -> Icons.Default.ViewInAr
                                    "Illustration" -> Icons.Default.Draw
                                    "Portrait" -> Icons.Default.Face
                                    "Landscape" -> Icons.Default.Landscape
                                    "Architecture" -> Icons.Default.Business
                                    else -> Icons.Default.Palette
                                },
                                contentDescription = null,
                                tint = if (category == currentCategory) InkspiraPrimary else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = category,
                                fontSize = 16.sp,
                                fontWeight = if (category == currentCategory) FontWeight.Bold else FontWeight.Medium,
                                color = if (category == currentCategory) InkspiraPrimary else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Current selection indicator
                            if (category == currentCategory) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = InkspiraPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = InkspiraPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = DarkNavy,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun UnsavedChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = InkspiraPrimary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Unsaved Changes",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "You have unsaved changes. Do you want to save them before leaving?",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDiscard) {
                    Text(
                        text = "Discard",
                        color = ErrorColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkspiraPrimary
                    )
                ) {
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = DarkNavy,
        shape = RoundedCornerShape(16.dp)
    )
}

// Helper function to update artwork
private fun updateArtwork(
    artworkViewModel: ArtworkViewModel,
    artworkId: String,
    title: String,
    description: String,
    category: String,
    isPublic: Boolean
) {
    artworkViewModel.updateArtwork(
        artworkId = artworkId,
        title = title,
        description = description,
        category = category,
        isPublic = isPublic
    )
}
