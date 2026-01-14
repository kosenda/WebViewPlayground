package ksnd.webviewplayground.ui.webview

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ksnd.webviewplayground.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplestWebViewScreen(
    url: String,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = url)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        SimplestWebViewScreenContent(
            url = url,
            innerPadding = innerPadding,
        )
    }
}

@Composable
private fun SimplestWebViewScreenContent(
    url: String,
    innerPadding: PaddingValues,
) {
    AndroidView(
        factory = ::WebView,
        modifier = Modifier
            .padding(paddingValues = innerPadding)
            .navigationBarsPadding()
            .fillMaxSize(),
    ) { webView ->
        with(webView) {
            loadUrl(url)
        }
    }
}
