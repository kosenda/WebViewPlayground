package ksnd.webviewplayground.ui.navigate

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Top : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data class WebView(
    val url: String,
) : NavKey
