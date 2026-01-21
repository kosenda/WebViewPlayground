package ksnd.webviewplayground.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.EaseInQuad
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ksnd.webviewplayground.ui.navigate.Navigator
import ksnd.webviewplayground.ui.navigate.Settings
import ksnd.webviewplayground.ui.navigate.Top
import ksnd.webviewplayground.ui.navigate.WebView
import ksnd.webviewplayground.ui.navigate.rememberNavigationState
import ksnd.webviewplayground.ui.navigate.toEntries
import ksnd.webviewplayground.ui.settings.SettingsScreen
import ksnd.webviewplayground.ui.top.TopScreen
import ksnd.webviewplayground.ui.webview.WebViewScreen

@Composable
fun MainScreen() {
    val navigationState = rememberNavigationState(
        startRoute = Top,
        topLevelRoutes = setOf(Top),
    )
    val navigator = remember { Navigator(state = navigationState) }

    val entryProvider = entryProvider {
        entry<Top> {
            TopScreen(
                navigate = navigator::navigate,
            )
        }
        entry<Settings>(metadata = getHorizontalTransitionMetadata()) {
            SettingsScreen(
                viewModel = hiltViewModel(),
                onBack = dropUnlessResumed(block = navigator::goBack),
            )
        }
        entry<WebView>(metadata = getVerticalTransitionMetadata()) {
            WebViewScreen(
                url = it.url,
                onBack = dropUnlessResumed(block = navigator::goBack),
            )
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = dropUnlessResumed(block = navigator::goBack),
    )
}

private fun getHorizontalTransitionMetadata() = NavDisplay.transitionSpec {
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(durationMillis = 500, easing = EaseInOutQuart)
    ) + fadeIn(animationSpec = tween(durationMillis = 500, easing = EaseInQuad)) togetherWith ExitTransition.KeepUntilTransitionsFinished
} + NavDisplay.popTransitionSpec {
    EnterTransition.None togetherWith slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(durationMillis = 400, easing = EaseInOutQuart)
    )
} + NavDisplay.predictivePopTransitionSpec {
    EnterTransition.None togetherWith slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(durationMillis = 400, easing = EaseInOutQuart)
    )
}

private fun getVerticalTransitionMetadata() = NavDisplay.transitionSpec {
    slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(durationMillis = 500, easing = EaseInOutQuart)
    ) + fadeIn(animationSpec = tween(durationMillis = 500, easing = EaseInQuad)) togetherWith ExitTransition.KeepUntilTransitionsFinished
} + NavDisplay.popTransitionSpec {
    EnterTransition.None togetherWith slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(durationMillis = 500, easing = EaseInOutQuart)
    )
} + NavDisplay.predictivePopTransitionSpec {
    EnterTransition.None togetherWith slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(durationMillis = 500, easing = EaseInOutQuart)
    )
}
