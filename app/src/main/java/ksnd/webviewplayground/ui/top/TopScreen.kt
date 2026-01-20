package ksnd.webviewplayground.ui.top

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import ksnd.webviewplayground.R
import ksnd.webviewplayground.ui.LocalIsDark
import ksnd.webviewplayground.ui.components.NavigationButton
import ksnd.webviewplayground.ui.navigate.Settings
import ksnd.webviewplayground.ui.navigate.WebView
import ksnd.webviewplayground.util.WebUtil
import ksnd.webviewplayground.util.WebUtil.createPartialCustomTabsIntent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopScreen(
    navigate: (route: NavKey) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.top))
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        TopScreenContent(
            innerPadding = innerPadding,
            navigate = navigate,
        )
    }
}

@Composable
private fun TopScreenContent(
    innerPadding: PaddingValues,
    navigate: (route: NavKey) -> Unit,
) {
    val context = LocalContext.current
    val isDark = LocalIsDark.current
    val androidDevelopersUrl = stringResource(id = R.string.android_developers_url)

    Column(
        modifier = Modifier
            .padding(paddingValues = innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NavigationButton(
            text = R.string.settings,
            onClick = dropUnlessResumed { navigate(Settings) },
        )
        NavigationButton(
            text = R.string.webview,
            onClick = dropUnlessResumed { navigate(WebView(url = androidDevelopersUrl)) },
        )

        NavigationButton(
            text = R.string.external_browser,
            onClick = dropUnlessResumed { WebUtil.openExternalBrowser(context = context, url = androidDevelopersUrl) },
        )

        NavigationButton(
            text = R.string.custom_tabs,
            onClick = dropUnlessResumed { WebUtil.openCustomTabs(context = context, url = androidDevelopersUrl, isDark = isDark) },
        )

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        val heightPx = LocalWindowInfo.current.containerSize.height / 2
        NavigationButton(
            text = R.string.partial_custom_tabs,
            onClick = dropUnlessResumed {
                val customTabsIntent = createPartialCustomTabsIntent(heightPx = heightPx, isDark = isDark).apply {
                    data = androidDevelopersUrl.toUri()
                }
                launcher.launch(customTabsIntent)
            },
        )
    }
}
