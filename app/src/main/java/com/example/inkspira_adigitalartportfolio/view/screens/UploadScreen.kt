package com.example.inkspira_adigitalartportfolio.view.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.inkspira_adigitalartportfolio.viewmodel.UploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onNavigateBack: () -> Unit,
    onUploadSuccess: () -> Unit,
    uploadViewModel: UploadViewModel = viewModel()
) {
    // State variables
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    // ViewModel states
    val isUploading by uploadViewModel.isUploading.collectAsState()
    val uploadProgress by uploadViewModel.uploadProgress.collectAsState()
    val errorMessage by uploadViewModel.errorMessage.collectAsState()
    val successMessage by uploadViewModel.successMessage.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Handle upload success
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            // Reset form on success
            selectedImageUri = null
            title = ""
            description = ""
            selectedCategory = ""
            isPublic = true
            onUploadSuccess()
            uploadViewModel.clearMessages()
        }
    }

    // Handle upload errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            uploadViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Upload Artwork",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
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
                // Image Selection Section
                ImageSelectionCard(
                    selectedImageUri = selectedImageUri,
                    onImageSelect = { imagePickerLauncher.launch("image/*") },
                    isUploading = isUploading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Artwork Details Form
                ArtworkDetailsForm(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    selectedCategory = selectedCategory,
                    onCategoryClick = { showCategoryDialog = true },
                    isPublic = isPublic,
                    onPrivacyChange = { isPublic = it },
                    isUploading = isUploading
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Upload Button
                UploadButton(
                    isEnabled = selectedImageUri != null && title.isNotBlank() && selectedCategory.isNotBlank(),
                    isUploading = isUploading,
                    uploadProgress = uploadProgress,
                    onUpload = {
                        selectedImageUri?.let { uri ->
                            uploadViewModel.uploadArtwork(
                                context = context,
                                imageUri = uri,
                                title = title.trim(),
                                description = description.trim(),
                                category = selectedCategory,
                                tags = emptyList(),
                                isPublic = isPublic
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Category Selection Dialog
        if (showCategoryDialog) {
            CategorySelectionDialog(
                onDismiss = { showCategoryDialog = false },
                onCategorySelected = { category ->
                    selectedCategory = category
                    showCategoryDialog = false
                }
            )
        }
    }
}

@Composable
private fun ImageSelectionCard(
    selectedImageUri: Uri?,
    onImageSelect: () -> Unit,
    isUploading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable(enabled = !isUploading) { onImageSelect() },
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                // Show selected image
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected artwork",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                // Overlay for uploading state
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = InkspiraPrimary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Processing...",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Change image button
                if (!isUploading) {
                    FloatingActionButton(
                        onClick = onImageSelect,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(48.dp),
                        containerColor = InkspiraPrimary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Image",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // Empty state - show upload prompt
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        InkspiraPrimary.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select Your Artwork",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap to choose an image from your gallery",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtworkDetailsForm(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryClick: () -> Unit,
    isPublic: Boolean,
    onPrivacyChange: (Boolean) -> Unit,
    isUploading: Boolean
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
            Text(
                text = "Artwork Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

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
                enabled = !isUploading,
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
                enabled = !isUploading,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InkspiraPrimary,
                    focusedLabelColor = InkspiraPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ FIXED: Category Selection - Proper clickable implementation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isUploading) { onCategoryClick() }
            ) {
                OutlinedTextField(
                    value = selectedCategory.ifEmpty { "" },
                    onValueChange = { }, // No direct editing
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
                    enabled = false, // ✅ FIXED: Disable to prevent keyboard
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
                        enabled = !isUploading,
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
private fun UploadButton(
    isEnabled: Boolean,
    isUploading: Boolean,
    uploadProgress: Float,
    onUpload: () -> Unit
) {
    Button(
        onClick = onUpload,
        enabled = isEnabled && !isUploading,
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
        if (isUploading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    progress = uploadProgress,
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Uploading... ${(uploadProgress * 100).toInt()}%",
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
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isEnabled) "Upload Artwork" else "Fill Required Fields",
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
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    // ✅ UPDATED: Complete category list with Landscape and Portrait properly ordered
    val categories = listOf(
        "3D Art",
        "Abstract",
        "Architecture",
        "Character Design",
        "Concept Art",
        "Digital Art",
        "Fan Art",
        "Illustration",
        "Landscape", // ✅ ADDED: Landscape category
        "Mixed Media",
        "Photography",
        "Portrait", // ✅ ADDED: Portrait category
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
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ✅ ENHANCED: Different icons for different categories
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
                                tint = InkspiraPrimary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = category,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {}, // No confirm button needed
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
