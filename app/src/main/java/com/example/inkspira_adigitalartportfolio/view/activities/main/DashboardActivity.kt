package com.example.inkspira_adigitalartportfolio.view.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.inkspira_adigitalartportfolio.view.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.activities.auth.LoginActivity
import com.example.inkspira_adigitalartportfolio.view.screens.DiscoverScreen
import com.example.inkspira_adigitalartportfolio.view.screens.GalleryScreen
import com.example.inkspira_adigitalartportfolio.view.screens.ProfileScreen
import com.example.inkspira_adigitalartportfolio.view.screens.UploadScreen
import com.example.inkspira_adigitalartportfolio.viewmodel.AuthViewModel
import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import kotlinx.coroutines.flow.first

class DashboardActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is still authenticated
        if (!authViewModel.isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        setContent {
            InkspiraDarkTheme {
                MainScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.logoutUser()
                        navigateToLogin()
                    }
                )
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

// ✅ ENHANCED: Bottom Navigation Items with role-based access
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val allowedRoles: List<UserRole> // ✅ NEW: Define which roles can access each item
) {
    object Gallery : BottomNavItem(
        route = "gallery",
        title = "Gallery",
        icon = Icons.Default.PhotoLibrary,
        selectedIcon = Icons.Filled.PhotoLibrary,
        allowedRoles = listOf(UserRole.ARTIST, UserRole.BOTH)
    )

    object Discover : BottomNavItem(
        route = "discover",
        title = "Discover",
        icon = Icons.Default.Explore,
        selectedIcon = Icons.Filled.Explore,
        allowedRoles = listOf(UserRole.VIEWER, UserRole.BOTH)
    )

    object Upload : BottomNavItem(
        route = "upload",
        title = "Upload",
        icon = Icons.Default.Add,
        allowedRoles = listOf(UserRole.ARTIST, UserRole.BOTH)
    )

    object Profile : BottomNavItem(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person,
        selectedIcon = Icons.Filled.Person,
        allowedRoles = listOf(UserRole.ARTIST, UserRole.VIEWER, UserRole.BOTH) // All users can access profile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // ✅ NEW: State for user role and loading
    var userRole by remember { mutableStateOf<UserRole?>(null) }
    var isLoadingRole by remember { mutableStateOf(true) }
    var roleError by remember { mutableStateOf<String?>(null) }

    // ✅ NEW: Fetch user role when component loads
    LaunchedEffect(Unit) {
        try {
            authViewModel.getCurrentUserData().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val userData = result.data as? Map<*, *>
                        val roleString = userData?.get("role") as? String
                        userRole = when (roleString) {
                            "ARTIST" -> UserRole.ARTIST
                            "VIEWER" -> UserRole.VIEWER
                            "BOTH" -> UserRole.BOTH
                            else -> UserRole.VIEWER // Default fallback
                        }
                        isLoadingRole = false
                    }
                    is NetworkResult.Error -> {
                        roleError = result.message
                        userRole = UserRole.VIEWER // Default fallback
                        isLoadingRole = false
                    }
                    is NetworkResult.Loading -> {
                        isLoadingRole = true
                    }
                }
            }
        } catch (e: Exception) {
            roleError = "Failed to load user data"
            userRole = UserRole.VIEWER // Default fallback
            isLoadingRole = false
        }
    }

    // ✅ NEW: Filter navigation items based on user role
    val allowedNavItems = remember(userRole) {
        if (userRole == null) {
            emptyList()
        } else {
            listOf(
                BottomNavItem.Gallery,
                BottomNavItem.Discover,
                BottomNavItem.Upload,
                BottomNavItem.Profile
            ).filter { item ->
                item.allowedRoles.contains(userRole)
            }
        }
    }

    // ✅ NEW: Determine start destination based on user role
    val startDestination = remember(userRole) {
        when (userRole) {
            UserRole.ARTIST, UserRole.BOTH -> BottomNavItem.Gallery.route
            UserRole.VIEWER -> BottomNavItem.Discover.route
            null -> BottomNavItem.Profile.route // Fallback
        }
    }

    // ✅ NEW: Show loading screen while fetching user role
    if (isLoadingRole) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = InkspiraPrimary,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Loading your dashboard...",
                    color = TextSecondary
                )
            }
        }
        return
    }

    // ✅ NEW: Show error if role loading failed
    if (roleError != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = ErrorColor,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Failed to load dashboard",
                    color = ErrorColor
                )
                Text(
                    text = roleError ?: "Unknown error",
                    color = TextSecondary
                )
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = InkspiraPrimary)
                ) {
                    Text("Back to Login")
                }
            }
        }
        return
    }

    Scaffold(
        bottomBar = {
            // Only show bottom navigation on main screens and if user has allowed items
            if (currentDestination?.route in allowedNavItems.map { it.route } && allowedNavItems.isNotEmpty()) {
                InkspiraBottomNavigation(
                    navController = navController,
                    items = allowedNavItems,
                    currentDestination = currentDestination,
                    userRole = userRole
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // ✅ ENHANCED: Conditional composables based on user role

            // Gallery Screen - Only for ARTIST and BOTH
            if (userRole == UserRole.ARTIST || userRole == UserRole.BOTH) {
                composable(BottomNavItem.Gallery.route) {
                    GalleryScreen(
                        onNavigateToUpload = {
                            if (userRole == UserRole.ARTIST || userRole == UserRole.BOTH) {
                                navController.navigate(BottomNavItem.Upload.route)
                            }
                        }
                    )
                }
            }

            // Discover Screen - Only for VIEWER and BOTH
            if (userRole == UserRole.VIEWER || userRole == UserRole.BOTH) {
                composable(BottomNavItem.Discover.route) {
                    DiscoverScreen(
                        onArtworkClick = { artwork ->
                            println("Artwork clicked: ${artwork.title}")
                        },
                        onArtistClick = { artist ->
                            println("Artist clicked: ${artist.displayName}")
                        }
                    )
                }
            }

            // Upload Screen - Only for ARTIST and BOTH
            if (userRole == UserRole.ARTIST || userRole == UserRole.BOTH) {
                composable(BottomNavItem.Upload.route) {
                    UploadScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onUploadSuccess = {
                            // Navigate back to gallery after successful upload
                            navController.navigate(BottomNavItem.Gallery.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // Profile Screen - Available for all users
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun InkspiraBottomNavigation(
    navController: androidx.navigation.NavController,
    items: List<BottomNavItem>,
    currentDestination: androidx.navigation.NavDestination?,
    userRole: UserRole?
) {
    NavigationBar(
        containerColor = DarkNavy,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.height(80.dp)
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == item.route
            } == true

            NavigationBarItem(
                icon = {
                    NavigationIcon(
                        item = item,
                        selected = selected
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (selected) InkspiraPrimary else TextMuted
                    )
                },
                selected = selected,
                onClick = {
                    // ✅ ENHANCED: Role-based navigation with validation
                    if (userRole != null && item.allowedRoles.contains(userRole)) {
                        if (item.route == BottomNavItem.Upload.route) {
                            // Special handling for upload - always navigate
                            navController.navigate(item.route)
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = InkspiraPrimary,
                    unselectedIconColor = TextMuted,
                    selectedTextColor = InkspiraPrimary,
                    unselectedTextColor = TextMuted,
                    indicatorColor = InkspiraPrimary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
private fun NavigationIcon(
    item: BottomNavItem,
    selected: Boolean
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        // Special styling for upload button
        if (item == BottomNavItem.Upload) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = if (selected) InkspiraPrimary else InkspiraPrimary.copy(alpha = 0.8f),
                shadowElevation = if (selected) 8.dp else 4.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // Regular navigation icons
            Icon(
                imageVector = if (selected) item.selectedIcon else item.icon,
                contentDescription = item.title,
                tint = if (selected) InkspiraPrimary else TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
