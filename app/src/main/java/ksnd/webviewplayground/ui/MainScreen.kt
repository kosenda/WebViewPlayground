package ksnd.webviewplayground.ui

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
        entry<Settings> {
            SettingsScreen(
                viewModel = hiltViewModel(),
                onBack = dropUnlessResumed(block = navigator::goBack),
            )
        }
        entry<WebView> {
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
