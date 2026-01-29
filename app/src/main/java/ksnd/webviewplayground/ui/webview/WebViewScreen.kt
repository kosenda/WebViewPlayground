package ksnd.webviewplayground.ui.webview

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ksnd.webviewplayground.R

private sealed interface WebViewScreenLoadingState {
    data object Initial : WebViewScreenLoadingState
    data object Loading : WebViewScreenLoadingState
    data object Error : WebViewScreenLoadingState
    data object Success : WebViewScreenLoadingState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    onBack: () -> Unit,
    javaScriptEnabled: Boolean = false,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var loadingState by remember { mutableStateOf<WebViewScreenLoadingState>(WebViewScreenLoadingState.Initial) }
    var canGoBack by remember { mutableStateOf(false) }
    var pageTitle by remember { mutableStateOf("") }
    var currentUrl by remember { mutableStateOf("") }
    var receivedMessage by remember { mutableStateOf("") }

    // ページ内検索
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResultCount by remember { mutableIntStateOf(0) }
    var currentSearchIndex by remember { mutableIntStateOf(0) }

    // 進捗（0f~1f）
    var progress by remember { mutableStateOf<Float?>(null) }

    val webView = remember {
        WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    loadingState = WebViewScreenLoadingState.Loading
                    canGoBack = view?.canGoBack() ?: false
                    currentUrl = url ?: ""
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (loadingState != WebViewScreenLoadingState.Error) {
                        loadingState = WebViewScreenLoadingState.Success
                    }
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        loadingState = WebViewScreenLoadingState.Error
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    pageTitle = title ?: ""
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    progress = newProgress.toFloat() / 100f
                }
            }

            settings.javaScriptEnabled = javaScriptEnabled
            if (javaScriptEnabled) {
                @Suppress("unused")
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun postMessage(message: String) {
                            coroutineScope.launch {
                                receivedMessage = message
                            }
                        }
                    },
                    "AndroidBridge"
                )
            }

            // debuggable が true の場合にのみ WebView デバッグを有効にする
            if (0 != (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)) {
                setWebContentsDebuggingEnabled(true)
            }

            // ページ内検索
            setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                if (isDoneCounting) {
                    searchResultCount = numberOfMatches
                    currentSearchIndex = if (numberOfMatches > 0) activeMatchOrdinal + 1 else 0
                }
            }
        }
    }

    fun dismissMessageDialog() {
        // Dialogを閉じた時にWebページに通知を送る
        val jsCode = "javascript:updateFromAndroid('最後に送信したメッセージ: $receivedMessage')"
        webView.loadUrl(jsCode)

        receivedMessage = ""
    }

    fun search(query: String) {
        searchQuery = query
        if (query.isNotEmpty()) {
            webView.findAllAsync(query)
        } else {
            webView.clearMatches()
            searchResultCount = 0
            currentSearchIndex = 0
        }
    }

    fun clearSearch() {
        webView.clearMatches()
        searchQuery = ""
        searchResultCount = 0
        currentSearchIndex = 0
        isSearchVisible = false
    }

    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(500)
            progress = null
        }
    }

    BackHandler(enabled = canGoBack, onBack = webView::goBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = pageTitle.ifEmpty { currentUrl },
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
                        onClick = { isSearchVisible = !isSearchVisible },
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = stringResource(R.string.search),
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
                        )
                    }
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
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .fillMaxSize()
        ) {
            // 検索バー
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = slideInVertically() + expandVertically(),
                exit = slideOutVertically() + shrinkVertically(),
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = ::search,
                    resultCount = searchResultCount,
                    currentIndex = currentSearchIndex,
                    onPrevious = { webView.findNext(false) },
                    onNext = { webView.findNext(true) },
                    onClose = ::clearSearch,
                )
            }

            // WebView
            AndroidView(
                factory = { webView },
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxSize(),
                update = { it.loadUrl(url) }
            )
        }

        AnimatedVisibility(
            visible = progress != null,
            modifier = Modifier.padding(paddingValues = innerPadding),
            enter = slideInVertically(),
            exit = slideOutVertically(),
        ) {
            progress?.let {
                LinearProgressIndicator(
                    progress = { it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        AnimatedVisibility(
            visible = loadingState is WebViewScreenLoadingState.Error,
            modifier = Modifier.padding(paddingValues = innerPadding),
            enter = EnterTransition.None,
            exit = fadeOut(),
        ) {
            ErrorContent(
                onRetry = webView::reload,
            )
        }
    }

    if (receivedMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = ::dismissMessageDialog,
            title = {
                Text(text = stringResource(R.string.message_from_web))
            },
            confirmButton = {
                TextButton(
                    onClick = ::dismissMessageDialog,
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            text = {
                Text(text = receivedMessage)
            }
        )
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = stringResource(R.string.error_message),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            modifier = modifier,
            shape = RoundedCornerShape(size = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(id = R.string.retry),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    resultCount: Int,
    currentIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(text = stringResource(R.string.search_hint))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                }
            ),
        )

        // 検索結果表示
        if (query.isNotEmpty()) {
            Text(
                text = if (resultCount > 0) {
                    stringResource(R.string.search_result, currentIndex, resultCount)
                } else {
                    stringResource(R.string.search_no_result)
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(80.dp),
            )

            // 前へ
            IconButton(
                onClick = onPrevious,
                enabled = resultCount > 0,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_up),
                    contentDescription = stringResource(R.string.previous),
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(
                        color = if (resultCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                )
            }

            // 次へ
            IconButton(
                onClick = onNext,
                enabled = resultCount > 0,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = stringResource(R.string.next),
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(
                        color = if (resultCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                )
            }
        }

        IconButton(onClick = onClose) {
            Image(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(R.string.close),
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewErrorContent() {
    ErrorContent {}
}
