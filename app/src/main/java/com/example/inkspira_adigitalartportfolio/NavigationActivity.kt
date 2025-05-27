package com.example.inkspira_adigitalartportfolio

import android.adservices.topics.Topic
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.inkspira_adigitalartportfolio.ui.theme.InkspiraADigitalArtPortfolioTheme

class NavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
          navigationBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun navigationBody() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Ecommerce",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp)
                },
                actions = {
                    IconButton(onClick =  {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }



                    IconButton(onClick =  {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick =  {}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    )  {
        padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()

        ) {

        }
    }
}

@Preview
@Composable
fun Preview() {
    navigationBody()
}

