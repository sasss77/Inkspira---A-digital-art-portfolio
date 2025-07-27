package com.example.inkspira_adigitalartportfolio.view.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inkspira_adigitalartportfolio.view.activities.auth.LoginActivity
import com.example.inkspira_adigitalartportfolio.view.activities.auth.RegisterActivity
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DarkNavy
import com.example.inkspira_adigitalartportfolio.view.ui.theme.DeepDarkBlue
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraDarkTheme
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextMuted
import com.example.inkspira_adigitalartportfolio.view.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InkspiraDarkTheme {
                OnboardingScreen(
                    onGetStarted = {
                        // Navigate to registration
                        startActivity(Intent(this, RegisterActivity::class.java))
                        finish()
                    },
                    onSkip = {
                        // Navigate to login
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onSkip: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Showcase Your Art",
            description = "Upload and display your digital artworks in a beautiful, professional portfolio",
            icon = Icons.Default.Palette,
            color = InkspiraPrimary
        ),
        OnboardingPage(
            title = "Connect with Artists",
            description = "Discover amazing digital art from creators around the world and get inspired",
            icon = Icons.Default.Group,
            color = InkspiraSecondary
        ),
        OnboardingPage(
            title = "Build Your Brand",
            description = "Grow your artistic presence with a personalized profile and seamless sharing",
            icon = Icons.Default.Star,
            color = InkspiraTertiary
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepDarkBlue, DarkNavy)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Bottom section with indicators and buttons
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == index) 32.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index)
                                        InkspiraPrimary else TextMuted.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                if (pagerState.currentPage == pages.size - 1) {
                    // Get Started button on last page
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InkspiraPrimary
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // Next button
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InkspiraPrimary
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with gradient background
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            page.color.copy(alpha = 0.2f),
                            page.color.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.color,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
