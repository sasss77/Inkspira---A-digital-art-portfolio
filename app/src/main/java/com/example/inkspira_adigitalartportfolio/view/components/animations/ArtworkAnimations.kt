package com.example.inkspira_adigitalartportfolio.view.components.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun ArtworkCardRevealAnimation(
    visible: Boolean,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    // ✅ FIXED: Create separate animation specs for different types
    val slideAnimationSpec = remember {
        tween<IntOffset>(
            durationMillis = 600,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        )
    }

    val fadeAnimationSpec = remember {
        tween<Float>(
            durationMillis = 600,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        )
    }

    val scaleAnimationSpec = remember {
        tween<Float>(
            durationMillis = 600,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(slideAnimationSpec) { it / 2 } +
                fadeIn(fadeAnimationSpec) +
                scaleIn(
                    animationSpec = scaleAnimationSpec,
                    initialScale = 0.8f
                ),
        exit = slideOutVertically(slideAnimationSpec) { -it / 2 } +
                fadeOut(fadeAnimationSpec) +
                scaleOut(scaleAnimationSpec, targetScale = 0.8f)
    ) {
        content()
    }
}

// ✅ Alternative Simplified Approach (Recommended)
@Composable
fun ArtworkCardRevealAnimationSimple(
    visible: Boolean,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = 600,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            )
        ) { it / 2 } +
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 600,
                        delayMillis = delay,
                        easing = FastOutSlowInEasing
                    )
                ) +
                scaleIn(
                    animationSpec = tween(
                        durationMillis = 600,
                        delayMillis = delay,
                        easing = FastOutSlowInEasing
                    ),
                    initialScale = 0.8f
                ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        ) { -it / 2 } +
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                ) +
                scaleOut(
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    ),
                    targetScale = 0.8f
                )
    ) {
        content()
    }
}

// ✅ Masonry Grid Staggered Animation (Already Correct)
@Composable
fun MasonryGridStaggeredAnimation(
    itemIndex: Int,
    totalItems: Int,
    content: @Composable () -> Unit
) {
    val delay = (itemIndex * 100).coerceAtMost(800)
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        ),
        label = "alpha_animation"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}

// ✅ NEW: Artwork Detail Transition Animation
@Composable
fun ArtworkDetailTransition(
    targetImageUrl: String,
    onAnimationComplete: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var animationState by remember { mutableStateOf(AnimationState.INITIAL) }

    LaunchedEffect(targetImageUrl) {
        animationState = AnimationState.LOADING
        kotlinx.coroutines.delay(300)
        animationState = AnimationState.COMPLETE
        onAnimationComplete()
    }

    val scale by animateFloatAsState(
        targetValue = when (animationState) {
            AnimationState.INITIAL -> 0.9f
            AnimationState.LOADING -> 1.05f
            AnimationState.COMPLETE -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "detail_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = when (animationState) {
            AnimationState.INITIAL -> 0f
            AnimationState.LOADING -> 0.7f
            AnimationState.COMPLETE -> 1f
        },
        animationSpec = tween(300),
        label = "detail_alpha"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}

// ✅ NEW: Image Upload Progress Animation
@Composable
fun UploadProgressAnimation(
    progress: Float,
    isCompleted: Boolean,
    content: @Composable (animatedProgress: Float) -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "upload_progress"
    )

    val pulseScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.1f else 1f,
        animationSpec = if (isCompleted) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        } else {
            tween(200)
        },
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier.scale(pulseScale)
    ) {
        content(animatedProgress)
    }
}

// ✅ NEW: Floating Animation for Cards
@Composable
fun FloatingAnimation(
    floating: Boolean,
    content: @Composable (offsetY: Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (floating) -8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    content(offsetY)
}

// ✅ NEW: Entrance Animation for Lists
@Composable
fun ListItemEntranceAnimation(
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    val delay = (index * 50).coerceAtMost(500)

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            )
        ) { -it / 2 } + fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delay,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        content()
    }
}

private enum class AnimationState {
    INITIAL, LOADING, COMPLETE
}
