package com.example.inkspira_adigitalartportfolio.view.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.inkspira_adigitalartportfolio.view.navigation.InkspiraNavigation
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InkspiraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    InkspiraApp()
                }
            }
        }
    }
}

@Composable
fun InkspiraApp() {
    val navController = rememberNavController()
    InkspiraNavigation(navController = navController)
}
