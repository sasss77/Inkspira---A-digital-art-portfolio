package com.example.inkspira_adigitalartportfolio.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.inkspira_adigitalartportfolio.R
import com.example.inkspira_adigitalartportfolio.model.UserModel
import com.example.inkspira_adigitalartportfolio.repository.UserRepositoryImpl
import com.example.inkspira_adigitalartportfolio.viewmodel.UserViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InkspiraDarkThemeLogin {
                LoginScreen()
            }
        }
    }
}

@Composable
fun InkspiraDarkThemeLogin(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF8B5CF6), // Purple
            secondary = Color(0xFFEC4899), // Pink
            tertiary = Color(0xFF06B6D4), // Cyan
            background = Color(0xFF0F0F23), // Deep Dark Blue
            surface = Color(0xFF1A1A2E), // Dark Navy
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

@Composable
fun LoginScreen() {
    val repo = remember { UserRepositoryImpl() }
    val userViewModel = remember { UserViewModel(repo) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var startAnimation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as Activity
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "login_animations")

    val floatingAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    val glowAnimation by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val particleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_rotation"
    )

    val slideInAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 100f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "slide_in"
    )

    val fadeInAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "fade_in"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F23), // Deep dark blue
                        Color(0xFF1A1A2E), // Dark navy
                        Color(0xFF16213E), // Darker blue
                        Color(0xFF0F0F23)  // Deep dark blue
                    )
                )
            )
    ) {
        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E).copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.8f),
                                    Color(0xFFEC4899).copy(alpha = 0.8f),
                                    Color(0xFF06B6D4).copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        // Animated background particles
        repeat(12) { index ->
            val delay = index * 1000
            val particleAlpha by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_alpha_$index"
            )

            val particleSize by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = 12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_size_$index"
            )

            val color = when (index % 3) {
                0 -> Color(0xFF8B5CF6)
                1 -> Color(0xFFEC4899)
                else -> Color(0xFF06B6D4)
            }

            Box(
                modifier = Modifier
                    .size(particleSize.dp)
                    .offset(
                        x = (50 + (index * 30) % 300).dp,
                        y = (100 + (index * 70) % 600).dp
                    )
                    .rotate(particleRotation)
                    .clip(CircleShape)
                    .background(color.copy(alpha = particleAlpha))
                    .blur(1.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .graphicsLayer {
                    translationY = slideInAnimation
                    alpha = fadeInAnimation
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Section with enhanced effects
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(y = floatingAnimation.dp)
                    .shadow(
                        elevation = 30.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFF8B5CF6).copy(alpha = glowAnimation),
                        spotColor = Color(0xFFEC4899).copy(alpha = glowAnimation)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Multiple glow layers for enhanced effect
                repeat(3) { layer ->
                    Box(
                        modifier = Modifier
                            .size((180 - layer * 20).dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6).copy(alpha = glowAnimation / (layer + 1)),
                                        Color(0xFFEC4899).copy(alpha = glowAnimation / (layer + 2)),
                                        Color.Transparent
                                    ),
                                    radius = 300f
                                )
                            )
                    )
                }

                // Logo card
                Card(
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A2E).copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.loginimg),
                        contentDescription = "Inkspira Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Welcome text
            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(fadeInAnimation)
            )

            Text(
                text = "Sign in to your creative space",
                fontSize = 16.sp,
                color = Color(0xFF8B5CF6),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Enhanced Login Form Card with better visibility
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6).copy(alpha = 0.6f),
                                Color(0xFFEC4899).copy(alpha = 0.4f),
                                Color(0xFF06B6D4).copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .shadow(
                        elevation = 30.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color(0xFF8B5CF6).copy(alpha = 0.4f),
                        spotColor = Color(0xFFEC4899).copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A2E).copy(alpha = 0.98f)
                )
            ) {
                // Inner gradient background for more distinction
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A2E).copy(alpha = 0.8f),
                                    Color(0xFF16213E).copy(alpha = 0.9f),
                                    Color(0xFF1A1A2E).copy(alpha = 0.8f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            placeholder = {
                                Text(
                                    text = "Enter your email",
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color(0xFF8B5CF6)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF8B5CF6),
                                focusedContainerColor = Color(0xFF16213E).copy(alpha = 0.5f),
                                unfocusedContainerColor = Color(0xFF16213E).copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            ),
                            singleLine = true
                        )

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            placeholder = {
                                Text(
                                    text = "Enter your password",
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Password",
                                    tint = Color(0xFF8B5CF6)
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible }
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isPasswordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24
                                        ),
                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF8B5CF6),
                                focusedContainerColor = Color(0xFF16213E).copy(alpha = 0.5f),
                                unfocusedContainerColor = Color(0xFF16213E).copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            singleLine = true
                        )

                        // Forgot password with pointer cursor
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = Color(0xFFEC4899),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(
                                        bounded = false,
                                        color = Color(0xFFEC4899)
                                    )
                                ) {
                                    val intent = Intent(context, ForgetPasswordActivity::class.java)
                                    context.startActivity(intent)
                                }
                            )
                        }

                        // Enhanced Sign in button
                        Button(
                            onClick = {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    isLoading = true
                                    userViewModel.login(email, password) { success, message ->
                                        isLoading = false
                                        if (success) {
                                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "✅ $message",
                                                    duration = SnackbarDuration.Short
                                                )
                                                kotlinx.coroutines.delay(1000)
                                                val intent = Intent(context, NavigationActivity::class.java)
                                                context.startActivity(intent)
                                                activity.finish()
                                            }
                                        } else {
                                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "❌ $message",
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            message = "⚠️ Please fill all fields",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .shadow(
                                    elevation = 15.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    ambientColor = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                                    spotColor = Color(0xFFEC4899).copy(alpha = 0.5f)
                                ),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6)
                            ),
                            enabled = !isLoading
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF8B5CF6),
                                                Color(0xFFEC4899),
                                                Color(0xFF8B5CF6)
                                            )
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Sign In",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Register link
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New to Inkspira? ",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Register Now",
                                color = Color(0xFF06B6D4),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(
                                        bounded = false,
                                        color = Color(0xFF06B6D4)
                                    )
                                ) {
                                    val intent = Intent(context, RegistrationActivity::class.java)
                                    context.startActivity(intent)
                                    activity.finish()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer
            Text(
                text = "Secure • Creative • Inspiring",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}