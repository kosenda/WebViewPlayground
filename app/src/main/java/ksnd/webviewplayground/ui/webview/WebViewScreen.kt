package ksnd.webviewplayground.ui.webview

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ksnd.webviewplayground.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    var canGoBack by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf("") }

    val webView = remember {
        WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    canGoBack = view?.canGoBack() ?: false
                    currentUrl = url ?: ""
                }
            }
        }
    }

    BackHandler(enabled = canGoBack, onBack = webView::goBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentUrl,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(
                            onClick = webView::goBack,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_close),
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
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .navigationBarsPadding()
                .fillMaxSize(),
            update = { it.loadUrl(url) }
        )
    }
}
