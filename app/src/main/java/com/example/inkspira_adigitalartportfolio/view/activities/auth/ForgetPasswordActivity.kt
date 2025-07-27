package com.example.inkspira_adigitalartportfolio.view.activities.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inkspira_adigitalartportfolio.view.ui.theme.*
import com.example.inkspira_adigitalartportfolio.utils.ValidationUtils
import com.example.inkspira_adigitalartportfolio.viewmodel.AuthViewModel

class ForgetPasswordActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InkspiraDarkTheme {
                ForgetPasswordScreen(
                    authViewModel = authViewModel,
                    onBackToLogin = {
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
private fun ForgetPasswordScreen(
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onBackPressed: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    // ✅ ENHANCED: Collect all states from updated AuthViewModel
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val successMessage by authViewModel.successMessage.collectAsState()
    val emailSent by authViewModel.emailSent.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ ENHANCED: Handle email sent status from AuthViewModel
    LaunchedEffect(emailSent) {
        if (emailSent) {
            showSuccess = true
        }
    }

    // ✅ ENHANCED: Handle success messages properly
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            if (message.contains("reset", ignoreCase = true) ||
                message.contains("sent", ignoreCase = true)) {
                showSuccess = true
            }
            authViewModel.clearSuccessMessage()
        }
    }

    // ✅ ENHANCED: Handle error messages with better filtering
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            // Only show snackbar for actual errors, not success messages
            if (!message.contains("reset", ignoreCase = true) &&
                !message.contains("sent", ignoreCase = true)) {
                snackbarHostState.showSnackbar(message)
            }
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
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
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
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Content based on state
                if (showSuccess) {
                    // Success State
                    SuccessContent(
                        email = email,
                        onBackToLogin = onBackToLogin,
                        onResendEmail = {
                            showSuccess = false
                            authViewModel.clearEmailSentStatus()
                            authViewModel.resendPasswordResetEmail(email)
                        }
                    )
                } else {
                    // Input State
                    InputContent(
                        email = email,
                        onEmailChange = {
                            email = it.trim()
                            emailError = null
                        },
                        emailError = emailError,
                        isLoading = isLoading,
                        onSendReset = {
                            // ✅ ENHANCED: Better validation with trimmed email
                            val trimmedEmail = email.trim()

                            when {
                                trimmedEmail.isEmpty() -> {
                                    emailError = "Email address is required"
                                }
                                !ValidationUtils.isValidEmail(trimmedEmail) -> {
                                    emailError = "Please enter a valid email address"
                                }
                                else -> {
                                    emailError = null
                                    println("Debug: Sending reset email for: $trimmedEmail")
                                    authViewModel.sendPasswordResetEmail(trimmedEmail)
                                }
                            }
                        },
                        onBackToLogin = onBackToLogin
                    )
                }
            }
        }
    }
}

@Composable
private fun InputContent(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    isLoading: Boolean,
    onSendReset: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ ENHANCED: Artistic lock icon with better styling
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            InkspiraPrimary.copy(alpha = 0.3f),
                            InkspiraPrimary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 150f
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                tint = InkspiraPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title and Description
        Text(
            text = "Forgot Password?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No worries! Enter your registered email address below and we'll send you a secure password reset link.",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ✅ ENHANCED: Email Input Card with better styling
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DarkNavy.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email Address") },
                    placeholder = { Text("Enter your registered email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = InkspiraPrimary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = emailError?.let {
                        { Text(it, color = ErrorColor) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkspiraPrimary,
                        focusedLabelColor = InkspiraPrimary,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.3f)
                    )
                )

                // ✅ ENHANCED: Send Reset Button with better styling
                Button(
                    onClick = onSendReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && email.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkspiraPrimary,
                        disabledContainerColor = InkspiraPrimary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Sending Reset Link...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Reset Link", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Back to Login Link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Remember your password? ",
                color = TextSecondary,
                fontSize = 16.sp
            )
            Text(
                text = "Sign In",
                color = InkspiraPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackToLogin() }
            )
        }
    }
}

@Composable
private fun SuccessContent(
    email: String,
    onBackToLogin: () -> Unit,
    onResendEmail: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ ENHANCED: Success Icon with better animation
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            InkspiraPrimary.copy(alpha = 0.3f),
                            InkspiraPrimary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 200f
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MarkEmailRead,
                contentDescription = null,
                tint = InkspiraPrimary,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Success Title
        Text(
            text = "Reset Link Sent!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Success Description
        Text(
            text = "We've sent a secure password reset link to:",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ ENHANCED: Email display with card styling
        Card(
            colors = CardDefaults.cardColors(
                containerColor = InkspiraPrimary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = email,
                fontSize = 16.sp,
                color = InkspiraPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please check your email inbox (and spam/junk folder) and click the reset link to create a new password.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Action Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // ✅ ENHANCED: Back to Login Button with better styling
            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = InkspiraPrimary
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // ✅ ENHANCED: Resend Email Button
            OutlinedButton(
                onClick = onResendEmail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = InkspiraPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    InkspiraPrimary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Resend Email", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ✅ ENHANCED: Help Text with better styling
        Card(
            colors = CardDefaults.cardColors(
                containerColor = DarkNavy.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = InkspiraPrimary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Email not received?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Check spam folder, verify email is registered, or try resending.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
