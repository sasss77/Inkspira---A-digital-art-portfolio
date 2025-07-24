package com.example.inkspira_adigitalartportfolio.view.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun InkspiraNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.LOGIN
    ) {
        // Temporary placeholder screens (no errors)
        composable(NavigationRoutes.LOGIN) {
            PlaceholderScreen(title = "Login Screen")
        }

        composable(NavigationRoutes.REGISTER) {
            PlaceholderScreen(title = "Register Screen")
        }

        composable(NavigationRoutes.DASHBOARD) {
            PlaceholderScreen(title = "Dashboard Screen")
        }

        composable(NavigationRoutes.ARTWORK_UPLOAD) {
            PlaceholderScreen(title = "Upload Screen")
        }

        composable(NavigationRoutes.BROWSE_ARTWORK) {
            PlaceholderScreen(title = "Browse Screen")
        }

        composable(NavigationRoutes.FAVORITES) {
            PlaceholderScreen(title = "Favorites Screen")
        }
    }
}

// Temporary placeholder composable
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

object NavigationRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val ARTWORK_UPLOAD = "artwork_upload"
    const val BROWSE_ARTWORK = "browse_artwork"
    const val FAVORITES = "favorites"
}
