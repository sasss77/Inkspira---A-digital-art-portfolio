package com.example.inkspira_adigitalartportfolio.view.activities.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inkspira_adigitalartportfolio.R
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.utils.ValidationUtils
import com.example.inkspira_adigitalartportfolio.view.activities.main.DashboardActivity

import com.example.inkspira_adigitalartportfolio.view.ui.theme.*
import com.example.inkspira_adigitalartportfolio.viewmodel.AuthViewModel

class LoginActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InkspiraDarkTheme {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    },
                    onNavigateToRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    // ✅ ADD: ForgetPassword navigation callback
                    onNavigateToForgetPassword = {
                        startActivity(Intent(this, ForgetPasswordActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgetPassword: () -> Unit // ✅ ADD: New parameter
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // ✅ FIXED: Use authState instead of loginState
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ FIXED: Handle auth result properly
    LaunchedEffect(authState) {
        when (authState) {
            is NetworkResult.Success -> {
                onLoginSuccess()
            }
            is NetworkResult.Error -> {
                snackbarHostState.showSnackbar(
                    authState.message ?: "Login failed. Please try again."
                )
            }
            else -> {}
        }
    }

    // Show error message from ViewModel
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            authViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = ErrorColor,
                        contentColor = Color.White
                    )
                }
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // App Logo and Title
                Image(
                    painter = painterResource(id = R.drawable.inkspira),
                    contentDescription = "Inkspira Logo",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome Back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Sign in to continue to Inkspira",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Login Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = null
                            },
                            label = { Text("Email") },
                            placeholder = { Text("Enter your email")
                                          },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = InkspiraPrimary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            isError = emailError != null,
                            supportingText = emailError?.let { { Text(it, color = ErrorColor) } },
                            modifier = Modifier.fillMaxWidth().testTag("email"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InkspiraPrimary,
                                focusedLabelColor = InkspiraPrimary
                            )
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = null
                            },
                            label = { Text("Password") },
                            placeholder = { Text("Enter your password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = InkspiraPrimary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password"
                                        else "Show password",
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = passwordError != null,
                            supportingText = passwordError?.let { { Text(it, color = ErrorColor) } },
                            modifier = Modifier.fillMaxWidth().testTag("password"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InkspiraPrimary,
                                focusedLabelColor = InkspiraPrimary
                            )
                        )

                        // ✅ FIXED: Forgot Password with Navigation
                        Text(
                            text = "Forgot Password?",
                            color = InkspiraPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable {
                                    // ✅ FIXED: Navigate to ForgetPasswordActivity
                                    onNavigateToForgetPassword()
                                }
                        )

                        // Login Button
                        Button(
                            onClick = {
                                // ✅ FIXED: Use correct validation methods
                                val isEmailValid = ValidationUtils.isValidEmail(email)
                                val isPasswordValid = ValidationUtils.isValidPassword(password)

                                emailError = if (isEmailValid) null else "Please enter a valid email address"
                                passwordError = if (isPasswordValid) null else "Password must be at least 6 characters"

                                if (isEmailValid && isPasswordValid) {
                                    // ✅ FIXED: Use correct ViewModel method
                                    authViewModel.loginUser(email, password)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp).testTag("submit"),
                            // ✅ FIXED: Use isLoading from ViewModel
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = InkspiraPrimary
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            // ✅ FIXED: Use isLoading state
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Signing In...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Register Link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Sign Up",
                        color = InkspiraPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
