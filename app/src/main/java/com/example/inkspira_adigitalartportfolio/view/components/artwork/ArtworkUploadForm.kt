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
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.inkspira_adigitalartportfolio.model.data.ArtworkModel
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.utils.ValidationUtils
import com.example.inkspira_adigitalartportfolio.view.components.common.InlineLoadingIndicator
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.SuccessColor
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.WarningColor
import com.example.inkspira_adigitalartportfolio.viewmodel.ArtworkViewModel

@Composable
fun ArtworkUploadForm(
    artworkViewModel: ArtworkViewModel,
    snackbarHostState: SnackbarHostState,
    onImagePickerClick: () -> Unit,
    selectedImageUri: Uri?,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var isPublic by rememberSaveable { mutableStateOf(true) }

    val uploadState by artworkViewModel.uploadState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Image Selection Section
        ImageSelectionCard(
            selectedUri = selectedImageUri,
            onImagePickerClick = onImagePickerClick
        )

        // Title Field
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Artwork Title") },
            placeholder = { Text("Enter your artwork title") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Title,
                    contentDescription = null,
                    tint = InkspiraPrimary
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InkspiraPrimary,
                focusedLabelColor = InkspiraPrimary
            )
        )

        // Description Field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("Describe your artwork, inspiration, or technique") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = InkspiraPrimary
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InkspiraPrimary,
                focusedLabelColor = InkspiraPrimary
            )
        )

        // ✅ CRITICAL FIX: Corrected typo from "OutlinedTextFieldDefenses" to "OutlinedTextFieldDefaults"
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags") },
            placeholder = { Text("nature, digital, abstract (comma-separated)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Tag,
                    contentDescription = null,
                    tint = InkspiraPrimary
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = InkspiraPrimary,
                focusedLabelColor = InkspiraPrimary
            )
        )

        // Visibility Toggle
        VisibilityToggleCard(
            isPublic = isPublic,
            onToggle = { isPublic = it }
        )

        // Upload Button
        Button(
            onClick = {
                if (selectedImageUri == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Please select an image first")
                    }
                    return@Button
                }

                val tagsList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val validation = ValidationUtils.validateArtwork(title, description, tagsList)

                if (!validation.isValid) {
                    scope.launch {
                        snackbarHostState.showSnackbar(validation.errorMessage)
                    }
                    return@Button
                }

                // Upload artwork with Context parameter
                artworkViewModel.uploadArtwork(
                    title = title,
                    description = description,
                    tags = tagsList,
                    imageUri = selectedImageUri,
                    context = context,
                    isPublic = isPublic
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = uploadState !is NetworkResult.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = InkspiraPrimary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (uploadState is NetworkResult.Loading) {
                InlineLoadingIndicator(size = 24)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Uploading...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Upload Artwork",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // ✅ CRITICAL FIX: Properly handle nullable uploadState
    LaunchedEffect(uploadState) {
        uploadState?.let { state ->
            when (state) {
                is NetworkResult.Success -> {
                    snackbarHostState.showSnackbar("Artwork uploaded successfully! 🎨")
                    // Clear form after successful upload
                    title = ""
                    description = ""
                    tags = ""
                }
                is NetworkResult.Error -> {
                    // ✅ FIXED: Safe access to message property
                    snackbarHostState.showSnackbar(
                        state.message ?: "Upload failed. Please try again."
                    )
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
}

@Composable
private fun ImageSelectionCard(
    selectedUri: Uri?,
    onImagePickerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onImagePickerClick() },
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy
        ),
        shape = RoundedCornerShape(16.dp)
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap to change image",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
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
private fun VisibilityToggleCard(
    isPublic: Boolean,
    onToggle: (Boolean) -> Unit
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
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = InkspiraPrimary
                )
            )
        }
    }
}
