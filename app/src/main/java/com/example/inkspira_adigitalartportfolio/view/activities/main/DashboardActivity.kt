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

// ✅ FIXED: Bottom Navigation Items (removed Community screen)
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    object Gallery : BottomNavItem(
        route = "gallery",
        title = "Gallery",
        icon = Icons.Default.PhotoLibrary,
        selectedIcon = Icons.Filled.PhotoLibrary
    )

    object Discover : BottomNavItem(
        route = "discover",
        title = "Discover",
        icon = Icons.Default.Explore,
        selectedIcon = Icons.Filled.Explore
    )

    object Upload : BottomNavItem(
        route = "upload",
        title = "Upload",
        icon = Icons.Default.Add
    )

    object Profile : BottomNavItem(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person,
        selectedIcon = Icons.Filled.Person
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // ✅ FIXED: Only 4 navigation items
    val bottomNavItems = listOf(
        BottomNavItem.Gallery,
        BottomNavItem.Discover,
        BottomNavItem.Upload,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            // Only show bottom navigation on main screens
            if (currentDestination?.route in bottomNavItems.map { it.route }) {
                InkspiraBottomNavigation(
                    navController = navController,
                    items = bottomNavItems,
                    currentDestination = currentDestination
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Gallery.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // ✅ FIXED: Gallery Screen with proper navigation
            composable(BottomNavItem.Gallery.route) {
                GalleryScreen(
                    onNavigateToUpload = {
                        navController.navigate(BottomNavItem.Upload.route)
                    }
                )
            }


            composable(BottomNavItem.Discover.route) {
                DiscoverScreen(
                    onArtworkClick = { artwork ->
                        // Handle artwork click - for now just log
                        println("Artwork clicked: ${artwork.title}")
                    },
                    onArtistClick = { artist ->
                        // Handle artist click - for now just log
                        println("Artist clicked: ${artist.displayName}")
                    }
                )
            }


            // ✅ FIXED: Upload Screen with proper navigation and context
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

            // ✅ FIXED: Profile Screen with logout callback
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
    currentDestination: androidx.navigation.NavDestination?
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
                    if (item.route == BottomNavItem.Upload.route) {
                        // Special handling for upload - always navigate
                        navController.navigate(item.route)
                    } else {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
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
