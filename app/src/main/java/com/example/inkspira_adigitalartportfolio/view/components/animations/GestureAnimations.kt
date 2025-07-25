package com.example.inkspira_adigitalartportfolio.view.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// ✅ Press Animation (Button Press Effect)
@Composable
fun PressAnimation(
    pressed: Boolean,
    content: @Composable (scale: Float) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "press_scale"
    )

    content(scale)
}

// ✅ Hover Animation (for cards and interactive elements)
@Composable
fun HoverAnimation(
    hovered: Boolean,
    content: @Composable (scale: Float, elevation: Float) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (hovered) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "hover_scale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (hovered) 12f else 6f,
        animationSpec = tween(200),
        label = "hover_elevation"
    )

    content(scale, elevation)
}

// ✅ FIXED: Swipe Gesture Animation
@Composable
fun SwipeGestureAnimation(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    threshold: Float = 100f,
    content: @Composable (offsetX: Float, offsetY: Float) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset_x"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset_y"
    )

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    when {
                        offsetX > threshold -> onSwipeRight()
                        offsetX < -threshold -> onSwipeLeft()
                        offsetY > threshold -> onSwipeDown()
                        offsetY < -threshold -> onSwipeUp()
                    }
                    offsetX = 0f
                    offsetY = 0f
                }
            ) { change, dragAmount ->
                // ✅ CRITICAL FIX: Use dragAmount instead of change.x/change.y
                offsetX += dragAmount.x
                offsetY += dragAmount.y
                change.consume()
            }
        }
    ) {
        content(animatedOffsetX, animatedOffsetY)
    }
}

// ✅ Pull to Refresh Animation
@Composable
fun PullToRefreshAnimation(
    pullProgress: Float,
    isRefreshing: Boolean,
    content: @Composable (rotation: Float, scale: Float) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else pullProgress * 180f,
        animationSpec = if (isRefreshing) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        },
        label = "pull_rotation"
    )

    val scale by animateFloatAsState(
        targetValue = (pullProgress * 0.5f + 0.5f).coerceIn(0.5f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pull_scale"
    )

    content(rotation, scale)
}

// ✅ Long Press Animation
@Composable
fun LongPressAnimation(
    onLongPress: () -> Unit,
    pressThreshold: Long = 500L,
    content: @Composable (progress: Float, isPressed: Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableStateOf(0L) }

    val progress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(pressThreshold.toInt()),
        label = "long_press_progress"
    )

    LaunchedEffect(progress) {
        if (progress >= 1f && isPressed) {
            onLongPress()
            isPressed = false
        }
    }

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    pressStartTime = System.currentTimeMillis()
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
    ) {
        content(progress, isPressed)
    }
}

// ✅ RECOMMENDED SOLUTION: Using animateFloatAsState approach
@Composable
fun ShakeAnimation(
    trigger: Boolean,
    content: @Composable (offsetX: Float) -> Unit
) {
    var shakeCount by remember { mutableStateOf(0) }
    var isShaking by remember { mutableStateOf(false) }

    val shakeOffsetX by animateFloatAsState(
        targetValue = when {
            !isShaking -> 0f
            shakeCount % 2 == 0 -> 10f
            else -> -10f
        },
        animationSpec = tween(
            durationMillis = 50,
            easing = LinearEasing
        ),
        label = "shake_offset",
        finishedListener = {
            if (isShaking && shakeCount < 6) {
                shakeCount++
            } else {
                isShaking = false
                shakeCount = 0
            }
        }
    )

    LaunchedEffect(trigger) {
        if (trigger && !isShaking) {
            isShaking = true
            shakeCount = 1
        }
    }

    content(shakeOffsetX)
}



// ✅ Pinch to Zoom Animation
@Composable
fun PinchToZoomAnimation(
    minScale: Float = 0.5f,
    maxScale: Float = 3f,
    content: @Composable (scale: Float, offsetX: Float, offsetY: Float) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "zoom_scale"
    )

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "zoom_offset_x"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "zoom_offset_y"
    )

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = (scale * zoom).coerceIn(minScale, maxScale)
                if (scale > 1f) {
                    offsetX += pan.x
                    offsetY += pan.y
                } else {
                    offsetX = 0f
                    offsetY = 0f
                }
            }
        }
    ) {
        content(animatedScale, animatedOffsetX, animatedOffsetY)
    }
}

// ✅ NEW: Tap Animation (for button feedback)
@Composable
fun TapAnimation(
    content: @Composable (scale: Float, alpha: Float) -> Unit
) {
    var isTapped by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isTapped) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "tap_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isTapped) 0.8f else 1f,
        animationSpec = tween(100),
        label = "tap_alpha"
    )

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isTapped = true
                    tryAwaitRelease()
                    isTapped = false
                }
            )
        }
    ) {
        content(scale, alpha)
    }
}

// ✅ NEW: Rotation Gesture Animation
@Composable
fun RotationGestureAnimation(
    onRotationChange: (Float) -> Unit = {},
    content: @Composable (rotation: Float) -> Unit
) {
    var rotation by remember { mutableStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation_animation"
    )

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectTransformGestures { _, _, _, rotationChange ->
                rotation += rotationChange
                onRotationChange(rotation)
            }
        }
    ) {
        content(animatedRotation)
    }
}
