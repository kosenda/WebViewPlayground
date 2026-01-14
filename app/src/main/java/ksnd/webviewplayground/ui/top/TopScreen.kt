package ksnd.webviewplayground.ui.top

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import ksnd.webviewplayground.R
import ksnd.webviewplayground.ui.components.NavigationButton
import ksnd.webviewplayground.ui.navigate.Settings
import ksnd.webviewplayground.ui.navigate.SimplestWebView

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

        val googleUrl = stringResource(id = R.string.example_url)
        NavigationButton(
            text = R.string.simplest_webview,
            onClick = dropUnlessResumed { navigate(SimplestWebView(url = googleUrl)) },
        )
    }
}
