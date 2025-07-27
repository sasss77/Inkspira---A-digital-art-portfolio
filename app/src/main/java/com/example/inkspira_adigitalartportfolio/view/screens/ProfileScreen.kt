package com.example.inkspira_adigitalartportfolio.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inkspira_adigitalartportfolio.view.ui.theme.*
import com.example.inkspira_adigitalartportfolio.viewmodel.AuthViewModel
import com.example.inkspira_adigitalartportfolio.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val currentUser = authViewModel.getCurrentUserEmail()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) } // ✅ NEW: Help dialog state

    // Load user profile data
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = InkspiraPrimary
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy
                )
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
            when {
                isLoading -> {
                    LoadingProfileContent()
                }

                userProfile != null -> {
                    ProfileContent(
                        userProfile = userProfile!!,
                        onHelpClick = { showHelpDialog = true }, // ✅ NEW: Pass help callback
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    EmptyProfileContent(
                        onRefresh = { profileViewModel.loadUserProfile() }
                    )
                }
            }

            // Error handling
            errorMessage?.let { error ->
                LaunchedEffect(error) {
                    // Show error snackbar
                    profileViewModel.clearErrorMessage()
                }
            }
        }

        // Edit Profile Dialog
        if (showEditDialog) {
            EditProfileDialog(
                currentProfile = userProfile,
                onDismiss = { showEditDialog = false },
                onSave = { updatedProfile ->
                    profileViewModel.updateUserProfile(updatedProfile)
                    showEditDialog = false
                }
            )
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                }
            )
        }

        // ✅ NEW: Help & Support Dialog
        if (showHelpDialog) {
            HelpSupportDialog(
                onDismiss = { showHelpDialog = false }
            )
        }
    }
}

@Composable
private fun ProfileContent(
    userProfile: UserProfileData,
    onHelpClick: () -> Unit, // ✅ NEW: Help callback parameter
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        ProfileHeader(userProfile = userProfile)

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Stats
        ProfileStats(userProfile = userProfile)

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Bio
        ProfileBio(userProfile = userProfile)

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ UPDATED: Profile Settings with help callback
        ProfileSettings(onHelpClick = onHelpClick)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileHeader(userProfile: UserProfileData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background gradient circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                InkspiraPrimary.copy(alpha = 0.3f),
                                InkspiraPrimary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Profile Image or Placeholder
            if (userProfile.profileImageUrl.isNotEmpty()) {
                // TODO: Load image from URL using Coil or similar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(InkspiraPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = InkspiraPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Default profile avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(InkspiraPrimary.copy(alpha = 0.2f))
                        .border(3.dp, InkspiraPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.displayName.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkspiraPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(
            text = userProfile.displayName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Username
        Text(
            text = "@${userProfile.username}",
            fontSize = 16.sp,
            color = InkspiraPrimary,
            fontWeight = FontWeight.Medium
        )

        // User Role Badge
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = InkspiraPrimary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = userProfile.role,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = InkspiraPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ProfileStats(userProfile: UserProfileData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                count = userProfile.artworkCount,
                label = "Artworks",
                icon = Icons.Default.Palette
            )

            StatDivider()

            StatItem(
                count = userProfile.followersCount,
                label = "Followers",
                icon = Icons.Default.Group
            )

            StatDivider()

            StatItem(
                count = userProfile.followingCount,
                label = "Following",
                icon = Icons.Default.PersonAdd
            )
        }
    }
}

@Composable
private fun StatItem(
    count: Int,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = InkspiraPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(50.dp)
            .background(TextMuted.copy(alpha = 0.3f))
    )
}

@Composable
private fun ProfileBio(userProfile: UserProfileData) {
    if (userProfile.bio.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DarkNavy.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Bio",
                        tint = InkspiraPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "About",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userProfile.bio,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                if (userProfile.websiteUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            // TODO: Open website URL
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Website",
                            tint = InkspiraPrimary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = userProfile.websiteUrl,
                            fontSize = 14.sp,
                            color = InkspiraPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ✅ UPDATED: Simplified ProfileSettings with only Help & Support
@Composable
private fun ProfileSettings(onHelpClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkNavy.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ ONLY: Help & Support item
            SettingItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                subtitle = "Get help, FAQs, and contact support",
                onClick = onHelpClick
            )
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = InkspiraPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LoadingProfileContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = InkspiraPrimary,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading profile...",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun EmptyProfileContent(onRefresh: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "No Profile",
                tint = TextMuted,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unable to load profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Please try again",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = InkspiraPrimary
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentProfile: UserProfileData?,
    onDismiss: () -> Unit,
    onSave: (UserProfileData) -> Unit
) {
    var displayName by remember { mutableStateOf(currentProfile?.displayName ?: "") }
    var bio by remember { mutableStateOf(currentProfile?.bio ?: "") }
    var websiteUrl by remember { mutableStateOf(currentProfile?.websiteUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Profile",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkspiraPrimary,
                        focusedLabelColor = InkspiraPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkspiraPrimary,
                        focusedLabelColor = InkspiraPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    label = { Text("Website URL") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkspiraPrimary,
                        focusedLabelColor = InkspiraPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    currentProfile?.let { profile ->
                        val updatedProfile = profile.copy(
                            displayName = displayName,
                            bio = bio,
                            websiteUrl = websiteUrl
                        )
                        onSave(updatedProfile)
                    }
                }
            ) {
                Text("Save", color = InkspiraPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkNavy
    )
}

@Composable
private fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Logout",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Are you sure you want to logout?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Logout", color = ErrorColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = InkspiraPrimary)
            }
        },
        containerColor = DarkNavy
    )
}

// ✅ NEW: Complete Help & Support Dialog with detailed information
@Composable
private fun HelpSupportDialog(
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("FAQ", "Contact", "About")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = "Help",
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Help & Support",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 500.dp)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = InkspiraPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = InkspiraPrimary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                when (selectedTab) {
                    0 -> FAQContent()
                    1 -> ContactContent()
                    2 -> AboutContent()
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = InkspiraPrimary)
            }
        },
        containerColor = DarkNavy,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FAQContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        FAQItem(
            question = "How do I upload artwork?",
            answer = "Go to the Upload tab, select an image from your gallery, add a title and description, choose a category, and tap Upload Artwork."
        )

        FAQItem(
            question = "How do I make my artwork public?",
            answer = "When uploading, make sure the 'Public' toggle is enabled. Public artworks appear in the Discover section for other users to see."
        )

        FAQItem(
            question = "Can I edit my artwork after uploading?",
            answer = "Currently, you can delete artworks from your Gallery but editing uploaded artworks is not supported. You can re-upload with changes."
        )

        FAQItem(
            question = "How do I search for artworks?",
            answer = "Use the search bar in the Discover tab to find artworks by title, description, or artist. You can also filter by categories."
        )

        FAQItem(
            question = "What image formats are supported?",
            answer = "We support JPG, PNG, and other common image formats. Images are automatically optimized for best quality and performance."
        )
    }
}

@Composable
private fun ContactContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Need more help? Contact us:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        ContactItem(
            icon = Icons.Default.Email,
            title = "Email Support",
            description = "support@inkspira.com",
            subtitle = "Response within 24 hours"
        )

        ContactItem(
            icon = Icons.Default.Phone,
            title = "Phone Support",
            description = "+1 (555) 123-4567",
            subtitle = "Mon-Fri, 9 AM - 6 PM EST"
        )

        ContactItem(
            icon = Icons.Default.Chat,
            title = "Live Chat",
            description = "Available in app",
            subtitle = "Real-time assistance"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = InkspiraPrimary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "💡 Tip: Before contacting support, check the FAQ section above for quick answers to common questions.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun AboutContent() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Inkspira",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = InkspiraPrimary
        )

        Text(
            text = "Digital Art Portfolio",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Version 1.0.0",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Inkspira is a platform for digital artists to showcase their work, discover inspiring artworks from the community, and build their creative portfolio.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Divider(color = TextMuted.copy(alpha = 0.3f))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Features:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        FeatureItem("✨ Upload and showcase your digital artworks")
        FeatureItem("🔍 Discover amazing art from the community")
        FeatureItem("❤️ Like and interact with artworks")
        FeatureItem("📂 Organize your portfolio by categories")
        FeatureItem("👤 Customize your artist profile")
        FeatureItem("🔒 Privacy controls for your artworks")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "© 2024 Inkspira. All rights reserved.",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FAQItem(
    question: String,
    answer: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepDarkBlue.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = answer,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ContactItem(
    icon: ImageVector,
    title: String,
    description: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = InkspiraPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                fontSize = 14.sp,
                color = InkspiraPrimary,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = TextSecondary,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

// Data class for user profile (unchanged)
data class UserProfileData(
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val websiteUrl: String = "",
    val artworkCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val createdAt: Long = 0L,
    val isActive: Boolean = true
)
