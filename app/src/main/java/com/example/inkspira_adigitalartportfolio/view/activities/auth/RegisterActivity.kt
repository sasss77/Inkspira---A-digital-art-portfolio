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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inkspira_adigitalartportfolio.R
import com.example.inkspira_adigitalartportfolio.model.data.UserRole
import com.example.inkspira_adigitalartportfolio.utils.NetworkResult
import com.example.inkspira_adigitalartportfolio.utils.ValidationUtils

import com.example.inkspira_adigitalartportfolio.view.ui.theme.*
import com.example.inkspira_adigitalartportfolio.viewmodel.AuthViewModel

class RegisterActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InkspiraDarkTheme {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegistrationSuccess = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    onNavigateToLogin = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    onBackPressed = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackPressed: () -> Unit
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.ARTIST) }
    var acceptTerms by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Error states
    var displayNameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }

    // ✅ FIXED: Use correct state flows from AuthViewModel
    val authState by authViewModel.authState.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ FIXED: Handle registration result properly
    LaunchedEffect(authState) {
        when (authState) {
            is NetworkResult.Success -> {
                onRegistrationSuccess()
            }
            is NetworkResult.Error -> {
                snackbarHostState.showSnackbar(
                    authState.message ?: "Registration failed. Please try again."
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
                // Header with back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                    }

                    Text(
                        text = "Already have an account?",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // App Logo and Title
                Image(
                    painter = painterResource(id = R.drawable.inkspira),
                    contentDescription = "Inkspira Logo",
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Join Inkspira",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Create your digital art portfolio",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Registration Form Card
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Display Name Field
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                displayNameError = null
                            },
                            label = { Text("Display Name") },
                            placeholder = { Text("Your full name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = InkspiraPrimary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            ),
                            singleLine = true,
                            isError = displayNameError != null,
                            supportingText = displayNameError?.let { { Text(it, color = ErrorColor) } },
                            modifier = Modifier.fillMaxWidth().testTag("fullName"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InkspiraPrimary,
                                focusedLabelColor = InkspiraPrimary
                            )
                        )

                        // Username Field
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                usernameError = null
                            },
                            label = { Text("Username") },
                            placeholder = { Text("Choose a unique username") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AlternateEmail,
                                    contentDescription = null,
                                    tint = InkspiraPrimary
                                )
                            },
                            prefix = { Text("@", color = TextMuted) },
                            singleLine = true,
                            isError = usernameError != null,
                            supportingText = usernameError?.let { { Text(it, color = ErrorColor) } },
                            modifier = Modifier.fillMaxWidth().testTag("username"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InkspiraPrimary,
                                focusedLabelColor = InkspiraPrimary
                            )
                        )

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = null
                            },
                            label = { Text("Email") },
                            placeholder = { Text("Enter your email address") },
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
                            placeholder = { Text("Create a strong password") },
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

                        // Confirm Password Field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                confirmPasswordError = null
                            },
                            label = { Text("Confirm Password") },
                            placeholder = { Text("Re-enter your password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = InkspiraPrimary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Hide password"
                                        else "Show password",
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = confirmPasswordError != null,
                            supportingText = confirmPasswordError?.let { { Text(it, color = ErrorColor) } },
                            modifier = Modifier.fillMaxWidth().testTag("confirmPassword"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InkspiraPrimary,
                                focusedLabelColor = InkspiraPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Simple Role Selection (without RoleSwitcher to avoid component errors)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "I am here to...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Role Options
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(
                                UserRole.ARTIST to "Create & Share Art",
                                UserRole.VIEWER to "Discover Art",
                                UserRole.BOTH to "Create & Discover"
                            ).forEach { (role, title) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedRole = role }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedRole == role,
                                        onClick = { selectedRole = role },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = InkspiraPrimary,
                                            unselectedColor = TextMuted
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (selectedRole == role) InkspiraPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Terms and Conditions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = {
                            acceptTerms = it
                            termsError = null
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = InkspiraPrimary,
                            uncheckedColor = TextMuted
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "I agree to the ",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Terms of Service",
                        fontSize = 14.sp,
                        color = InkspiraPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { /* Show terms */ }
                    )
                    Text(
                        text = " and ",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Privacy Policy",
                        fontSize = 14.sp,
                        color = InkspiraPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { /* Show privacy policy */ }
                    )
                }

                // Terms error
                termsError?.let { error ->
                    Text(
                        text = error,
                        color = ErrorColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Register Button
                Button(
                    onClick = {
                        // ✅ FIXED: Use correct validation methods
                        val isDisplayNameValid = ValidationUtils.isValidDisplayName(displayName)
                        val isEmailValid = ValidationUtils.isValidEmail(email)
                        val isPasswordValid = ValidationUtils.isValidPassword(password)

                        displayNameError = if (isDisplayNameValid) null else "Display name must be 2-30 characters long"
                        usernameError = if (username.isNotBlank()) null else "Username is required"
                        emailError = if (isEmailValid) null else "Please enter a valid email address"
                        passwordError = if (isPasswordValid) null else "Password must be at least 6 characters"

                        // Validate password confirmation
                        confirmPasswordError = when {
                            confirmPassword.isEmpty() -> "Please confirm your password"
                            confirmPassword != password -> "Passwords do not match"
                            else -> null
                        }

                        // Validate terms acceptance
                        termsError = if (!acceptTerms) "Please accept the terms and conditions" else null

                        // If all validations pass, register user
                        if (isDisplayNameValid &&
                            username.isNotBlank() &&
                            isEmailValid &&
                            isPasswordValid &&
                            confirmPasswordError == null &&
                            acceptTerms) {

                            // ✅ FIXED: Use correct ViewModel method
                            authViewModel.registerUser(
                                email = email,
                                password = password,
                                displayName = displayName,
                                selectedRole = selectedRole
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp).testTag("register"),
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
                        Text("Creating Account...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Login Link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Sign In",
                        color = InkspiraPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
