package com.example.inkspira_adigitalartportfolio.view.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.example.inkspira_adigitalartportfolio.ui.theme.*
import com.example.inkspira_adigitalartportfolio.view.ui.theme.ErrorColor
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraPrimary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraSecondary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.InkspiraTertiary
import com.example.inkspira_adigitalartportfolio.view.ui.theme.SuccessColor
import com.example.inkspira_adigitalartportfolio.view.ui.theme.WarningColor
import kotlinx.coroutines.launch

enum class SnackbarType {
    SUCCESS, ERROR, INFO, WARNING, CREATIVE
}

@Composable
fun InkspiraSnackbar(
    message: String,
    type: SnackbarType,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    val (backgroundColor, iconColor, icon) = when (type) {
        SnackbarType.SUCCESS -> Triple(
            Brush.horizontalGradient(listOf(SuccessColor, SuccessColor.copy(alpha = 0.8f))),
            Color.White,
            Icons.Default.CheckCircle
        )
        SnackbarType.ERROR -> Triple(
            Brush.horizontalGradient(listOf(ErrorColor, ErrorColor.copy(alpha = 0.8f))),
            Color.White,
            Icons.Default.Error
        )
        SnackbarType.INFO -> Triple(
            Brush.horizontalGradient(listOf(InkspiraTertiary, InkspiraTertiary.copy(alpha = 0.8f))),
            Color.White,
            Icons.Default.Info
        )
        SnackbarType.WARNING -> Triple(
            Brush.horizontalGradient(listOf(WarningColor, WarningColor.copy(alpha = 0.8f))),
            Color.White,
            Icons.Default.Warning
        )
        SnackbarType.CREATIVE -> Triple(
            Brush.horizontalGradient(listOf(InkspiraPrimary, InkspiraSecondary)),
            Color.White,
            Icons.Default.Palette
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Message
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = iconColor,
                modifier = Modifier.weight(1f)
            )

            // Action
            action?.let {
                Spacer(modifier = Modifier.width(8.dp))
                it()
            }

            // Dismiss Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Helper object for showing snackbars
object SnackbarUtils {
    fun showSuccess(
        snackbarHostState: SnackbarHostState,
        message: String,
        scope: kotlinx.coroutines.CoroutineScope
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun showError(
        snackbarHostState: SnackbarHostState,
        message: String,
        scope: kotlinx.coroutines.CoroutineScope
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }
}

@Composable
fun InkspiraSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbarType: SnackbarType = SnackbarType.INFO
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        InkspiraSnackbar(
            message = data.visuals.message,
            type = snackbarType,
            onDismiss = { data.dismiss() },
            action = data.visuals.actionLabel?.let {
                {
                    TextButton(onClick = { data.performAction() }) {
                        Text(
                            text = it,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }
}
