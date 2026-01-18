package ksnd.webviewplayground

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ksnd.webviewplayground.ui.LocalIsDark
import ksnd.webviewplayground.ui.MainActivityViewModel
import ksnd.webviewplayground.ui.MainScreen
import ksnd.webviewplayground.ui.theme.Theme
import ksnd.webviewplayground.ui.theme.WebViewPlaygroundTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        var isDarkTheme by mutableStateOf(false)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    mainViewModel.theme,
                    // ref: https://github.com/android/nowinandroid/blob/e308246131a44cd9a4caf700bee26e8351d06090/app/src/main/kotlin/com/google/samples/apps/nowinandroid/util/UiExtensions.kt
                    callbackFlow {
                        channel.trySend(resources.configuration.isSystemInDarkTheme)
                        val listener = Consumer<Configuration> {
                            channel.trySend(it.isSystemInDarkTheme)
                        }
                        addOnConfigurationChangedListener(listener)
                        awaitClose { removeOnConfigurationChangedListener(listener) }
                    }.distinctUntilChanged().conflate(),
                ) { theme, uiMode ->
                    when (theme) {
                        Theme.DARK -> true
                        Theme.LIGHT -> false
                        else -> uiMode
                    }
                }.collect {
                    isDarkTheme = it
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            lightScrim = Color.Transparent.toArgb(),
                            darkScrim = Color.Transparent.toArgb(),
                            detectDarkMode = { isDarkTheme },
                        ),
                        navigationBarStyle = SystemBarStyle.auto(
                            lightScrim = Color.Transparent.toArgb(),
                            darkScrim = Color.Transparent.toArgb(),
                            detectDarkMode = { isDarkTheme },
                        ),
                    )
                }
            }
        }

        super.onCreate(savedInstanceState)
        setContent {
            WebViewPlaygroundTheme(
                isDarkTheme = isDarkTheme
            ) {
                CompositionLocalProvider(LocalIsDark provides isDarkTheme) {
                    MainScreen()
                }
            }
        }
    }
}

/**
 * Convenience wrapper for dark mode checking
 * ref: https://github.com/android/nowinandroid/blob/e308246131a44cd9a4caf700bee26e8351d06090/app/src/main/kotlin/com/google/samples/apps/nowinandroid/util/UiExtensions.kt
 */
private val Configuration.isSystemInDarkTheme
    get() = (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
