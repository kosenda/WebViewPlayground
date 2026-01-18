package ksnd.webviewplayground.util

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import timber.log.Timber

object WebUtil {
    /**
     * アプリ外ブラウザで開く
     */
    fun openExternalBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.w(t = e)
        }
    }

    /**
     * Custom Tabs（アプリ内ブラウザ）で開く
     */
    fun openCustomTabs(context: Context, url: String, isDark: Boolean) {
        val colorScheme = if (isDark) CustomTabsIntent.COLOR_SCHEME_DARK else CustomTabsIntent.COLOR_SCHEME_LIGHT

        val customTabsIntent = CustomTabsIntent.Builder()
            .setColorScheme(colorScheme)
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(context, url.toUri())
    }
}
