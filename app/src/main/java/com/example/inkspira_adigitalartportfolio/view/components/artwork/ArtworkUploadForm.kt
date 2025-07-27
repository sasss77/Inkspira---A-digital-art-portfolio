package com.example.inkspira_adigitalartportfolio.view.components.artwork

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.SuccessColor
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.WarningColor
import com.example.inkspira_adigitalartportfolio.view.ui.theme.ErrorColor
import com.example.inkspira_adigitalartportfolio.viewmodel.UploadViewModel

@OptIn(ExperimentalMaterial3Api::class) // Needed for ExposedDropdownMenuBox
@Composable
fun ArtworkUploadForm(
    uploadViewModel: UploadViewModel,
    snackbarHostState: SnackbarHostState,
    onImagePickerClick: () -> Unit,
    selectedImageUri: Uri?,
    onUploadSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Form state
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var isPublic by rememberSaveable { mutableStateOf(true) }

    // Category state
    val categories = listOf("Digital Art", "Traditional Art", "Photography", "Sculpture", "Illustration", "Other")
    var selectedCategory by rememberSaveable { mutableStateOf(categories[0]) } // Default to first category
    var expanded by remember { mutableStateOf(false) } // For dropdown menu

    // ViewModel states
    val isUploading by uploadViewModel.isUploading.collectAsState()
    val uploadProgress by uploadViewModel.uploadProgress.collectAsState()
    val errorMessage by uploadViewModel.errorMessage.collectAsState()
    val successMessage by uploadViewModel.successMessage.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Handle upload success/error messages
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            // Clear form after successful upload
            title = ""
            description = ""
            selectedCategory = categories[0] // Reset category
            uploadViewModel.clearMessages()
            onUploadSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            uploadViewModel.clearMessages()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Text(
            text = "Share Your Art",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Upload your artwork and share it with the community",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        // Image Selection Section
        ImageSelectionCard(
            selectedUri = selectedImageUri,
            onImagePickerClick = onImagePickerClick,
            isUploading = isUploading
        )

        // Upload Progress
        if (isUploading && uploadProgress > 0f) {
            UploadProgressCard(progress = uploadProgress)
        }

        // Title Field
        OutlinedTextField(
            value = title,
            onValueChange = {
                if (it.length <= 100) title = it
            },
            label = { Text("Artwork Title") },
            placeholder = { Text("Enter your artwork title") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Title,
                    contentDescription = null,
                    tint = InkspiraPrimary
                )
            },
            trailingIcon = {
                Text(
                    text = "${title.length}/100",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            singleLine = true,
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InkspiraPrimary,
                focusedLabelColor = InkspiraPrimary
            )
        )

        // Description Field
        OutlinedTextField(
            value = description,
            onValueChange = {
                if (it.length <= 500) description = it
            },
            label = { Text("Description") },
            placeholder = { Text("Describe your artwork, inspiration, or technique...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = InkspiraPrimary
                )
            },
            trailingIcon = {
                Text(
                    text = "${description.length}/500",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            enabled = !isUploading,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InkspiraPrimary,
                focusedLabelColor = InkspiraPrimary
            )
        )

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {}, // Not directly editable
                readOnly = true,
                label = { Text("Category") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = InkspiraPrimary
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor() // IMPORTANT: This makes the TextField the anchor for the dropdown
                    .fillMaxWidth(),
                enabled = !isUploading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InkspiraPrimary,
                    focusedLabelColor = InkspiraPrimary
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        // Visibility Toggle
        VisibilityToggleCard(
            isPublic = isPublic,
            onToggle = { if (!isUploading) isPublic = it },
            enabled = !isUploading
        )

        // Upload Button
        Button(
            onClick = {
                // Validation
                when {
                    selectedImageUri == null -> {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please select an image first")
                        }
                        return@Button
                    }
                    title.isBlank() -> {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a title for your artwork")
                        }
                        return@Button
                    }
                    title.length < 3 -> {
                        scope.launch {
                            snackbarHostState.showSnackbar("Title must be at least 3 characters long")
                        }
                        return@Button
                    }
                    description.isBlank() -> {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please add a description")
                        }
                        return@Button
                    }
                    description.length < 10 -> {
                        scope.launch {
                            snackbarHostState.showSnackbar("Description must be at least 10 characters long")
                        }
                        return@Button
                    }
                }

                // Upload artwork
                uploadViewModel.uploadArtwork(
                    context = context,
                    imageUri = selectedImageUri,
                    title = title.trim(),
                    description = description.trim(),
                    category = selectedCategory, // Use the selected category
                    tags = emptyList(), // Tags are fully removed, pass an empty list
                    isPublic = isPublic
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isUploading && selectedImageUri != null && title.isNotBlank() && description.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = InkspiraPrimary,
                disabledContainerColor = InkspiraPrimary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Upload Artwork",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Cancel Button (visible during upload)
        if (isUploading) {
            OutlinedButton(
                onClick = {
                    // Note: Cloudinary doesn't support upload cancellation easily
                    // This is a placeholder for UI feedback
                    scope.launch {
                        snackbarHostState.showSnackbar("Upload cannot be cancelled once started")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorColor
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cancel Upload",
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Form Guidelines
        if (!isUploading) {
            FormGuidelinesCard()
        }
    }
}

@Composable
private fun ImageSelectionCard(
    selectedUri: Uri?,
    onImagePickerClick: () -> Unit,
    isUploading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(enabled = !isUploading) { onImagePickerClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (selectedUri != null) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Selected artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )

                // Overlay for re-selection
                if (!isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to change image",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // Empty state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        InkspiraPrimary.copy(alpha = 0.2f),
                                        InkspiraPrimary.copy(alpha = 0.1f)
                                    )
                                ),
                                RoundedCornerShape(40.dp)
                            )
                            .border(
                                2.dp,
                                InkspiraPrimary.copy(alpha = 0.3f),
                                RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add image",
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select Artwork Image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Tap to choose from gallery",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadProgressCard(progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Uploading to Cloudinary...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkspiraPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = InkspiraPrimary,
                trackColor = InkspiraPrimary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun VisibilityToggleCard(
    isPublic: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isPublic) SuccessColor else WarningColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPublic) "Public Artwork" else "Private Artwork",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isPublic) "Visible to everyone in the gallery" else "Only visible to you",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Switch(
                checked = isPublic,
                onCheckedChange = onToggle,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = InkspiraPrimary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = TextSecondary
                )
            )
        }
    }
}

@Composable
private fun FormGuidelinesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upload Guidelines",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val guidelines = listOf(
                "• Image formats: JPG, PNG, WEBP",
                "• Maximum file size: 5MB",
                "• Recommended resolution: 1920x1080",
                "• Title: 3-100 characters",
                "• Description: 10-500 characters",
                "• Category: Select an appropriate category"
            )

            guidelines.forEach { guideline ->
                Text(
                    text = guideline,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}