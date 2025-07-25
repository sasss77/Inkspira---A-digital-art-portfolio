package com.example.inkspira_adigitalartportfolio.view.components.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

// ✅ Inkspira Custom Screen Transitions
object InkspiraTransitions {

    val ArtisticSlideIn = slideInHorizontally(
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    ) { it } + fadeIn(
        animationSpec = tween(400, delayMillis = 200)
    )

    val ArtisticSlideOut = slideOutHorizontally(
        animationSpec = tween(400, easing = LinearOutSlowInEasing)
    ) { -it / 2 } + fadeOut(
        animationSpec = tween(300)
    )

    val GalleryEnter = slideInVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) { it / 3 } + fadeIn(tween(500)) + scaleIn(
        animationSpec = tween(500),
        initialScale = 0.95f
    )

    val GalleryExit = slideOutVertically(
        animationSpec = tween(300)
    ) { -it / 3 } + fadeOut(tween(250)) + scaleOut(
        animationSpec = tween(300),
        targetScale = 1.05f
    )
}

// ✅ Creative Page Transition
@Composable
fun CreativePageTransition(
    visible: Boolean,
    direction: SlideDirection = SlideDirection.LEFT_TO_RIGHT,
    duration: Int = 500,
    content: @Composable () -> Unit
) {
    val enterTransition = when (direction) {
        SlideDirection.LEFT_TO_RIGHT -> slideInHorizontally(
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) { -it } + fadeIn(tween(duration / 2, delayMillis = duration / 3))

        SlideDirection.RIGHT_TO_LEFT -> slideInHorizontally(
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) { it } + fadeIn(tween(duration / 2, delayMillis = duration / 3))

        SlideDirection.TOP_TO_BOTTOM -> slideInVertically(
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) { -it } + fadeIn(tween(duration / 2, delayMillis = duration / 3))

        SlideDirection.BOTTOM_TO_TOP -> slideInVertically(
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) { it } + fadeIn(tween(duration / 2, delayMillis = duration / 3))
    }

    val exitTransition = when (direction) {
        SlideDirection.LEFT_TO_RIGHT -> slideOutHorizontally(
            animationSpec = tween(duration / 2)
        ) { it } + fadeOut(tween(duration / 3))

        SlideDirection.RIGHT_TO_LEFT -> slideOutHorizontally(
            animationSpec = tween(duration / 2)
        ) { -it } + fadeOut(tween(duration / 3))

        SlideDirection.TOP_TO_BOTTOM -> slideOutVertically(
            animationSpec = tween(duration / 2)
        ) { it } + fadeOut(tween(duration / 3))

        SlideDirection.BOTTOM_TO_TOP -> slideOutVertically(
            animationSpec = tween(duration / 2)
        ) { -it } + fadeOut(tween(duration / 3))
    }

    AnimatedVisibility(
        visible = visible,
        enter = enterTransition,
        exit = exitTransition
    ) {
        content()
    }
}

// ✅ Bottom Navigation Animation
@Composable
fun BottomNavAnimation(
    selectedIndex: Int,
    totalTabs: Int,
    content: @Composable (animatedIndicatorOffset: Float) -> Unit
) {
    val indicatorOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_indicator"
    )

    content(indicatorOffset)
}

// ✅ COMPLETELY FIXED: Tab Switching Animation using togetherWith
@Composable
fun TabSwitchAnimation(
    currentTab: Int,
    content: @Composable (tabIndex: Int) -> Unit
) {
    AnimatedContent(
        targetState = currentTab,
        transitionSpec = {
            if (targetState > initialState) {
                // ✅ CRITICAL FIX: Use togetherWith instead of with
                (slideInHorizontally(
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) { it } + fadeIn(tween(300, delayMillis = 100))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(300)
                        ) { -it } + fadeOut(tween(200)))
            } else {
                (slideInHorizontally(
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) { -it } + fadeIn(tween(300, delayMillis = 100))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(300)
                        ) { it } + fadeOut(tween(200)))
            }.using(SizeTransform(clip = false))
        },
        label = "tab_switch"
    ) { tabIndex ->
        content(tabIndex)
    }
}

// ✅ Alternative Simple Version (Most Reliable)
@Composable
fun SimpleTabSwitchAnimation(
    currentTab: Int,
    content: @Composable (tabIndex: Int) -> Unit
) {
    AnimatedContent(
        targetState = currentTab,
        transitionSpec = {
            // ✅ SIMPLEST: Direct approach without complex transitions
            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
        },
        label = "simple_tab_switch"
    ) { tabIndex ->
        content(tabIndex)
    }
}

// ✅ Crossfade Alternative (Most Compatible)
@Composable
fun CrossfadeTabAnimation(
    currentTab: Int,
    content: @Composable (tabIndex: Int) -> Unit
) {
    Crossfade(
        targetState = currentTab,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "crossfade_tab"
    ) { tabIndex ->
        content(tabIndex)
    }
}

// ✅ Floating Action Button Animation
@Composable
fun FloatingActionButtonAnimation(
    expanded: Boolean,
    content: @Composable (scale: Float, alpha: Float) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "fab_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.7f,
        animationSpec = tween(200),
        label = "fab_alpha"
    )

    content(scale, alpha)
}

enum class SlideDirection {
    LEFT_TO_RIGHT, RIGHT_TO_LEFT, TOP_TO_BOTTOM, BOTTOM_TO_TOP
}
