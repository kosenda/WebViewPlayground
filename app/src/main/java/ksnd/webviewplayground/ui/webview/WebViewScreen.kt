package ksnd.webviewplayground.ui.webview

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ksnd.webviewplayground.R
import timber.log.Timber
import java.net.URL

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

    val imageSavedMessage = stringResource(R.string.image_saved)
    val imageSaveFailedMessage = stringResource(R.string.image_save_failed)
    val viewActionLabel = stringResource(R.string.view)

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

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

    // 画像長押し保存可能な画像のURL
    var imageUrlToSave by remember { mutableStateOf<String?>(null) }

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

            // 長押し
            setOnLongClickListener {
                // 長押しした結果が画像もしくは画像リンクの場合だけ後続の処理を進め、それ以外の場合は何もしない
                if (hitTestResult.type != WebView.HitTestResult.IMAGE_TYPE && hitTestResult.type != WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    return@setOnLongClickListener false
                }

                hitTestResult.extra?.let { imageUrl ->
                    imageUrlToSave = imageUrl
                }
                true
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

    fun clearImageUrlToSave() {
        imageUrlToSave = null
    }

    suspend fun saveImage(url: String) {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                // 拡張子とMIMEタイプを特定し標準画像(PNG/JPG)かどうか判定
                val extension = MimeTypeMap.getFileExtensionFromUrl(url).lowercase()
                val isStandardImage = extension in listOf("png", "jpg", "jpeg")
                val mimeType = if (isStandardImage) {
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"
                } else {
                    "application/octet-stream" // SVG等は画像フォルダを避けるために汎用バイナリとして扱う
                }

                val fileName = "file_${System.currentTimeMillis()}.$extension"
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    /* Android 10 (API 29) 以降の対応:
                     *   Scoped Storageにより、ファイルを保存する相対パス（RELATIVE_PATH）を明示的に指定する必要がある。
                     *   標準画像（PNG/JPG）は「Pictures」フォルダ、それ以外（SVG/PDF等）は「Downloads」フォルダに振り分ける。*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val folder = if (isStandardImage) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_DOWNLOADS
                        put(MediaStore.MediaColumns.RELATIVE_PATH, folder)
                    }
                }

                // 保存先URIの決定
                val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (isStandardImage) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else {
                    // Android 9以前は汎用的なFilesを使用
                    MediaStore.Files.getContentUri("external")
                }
                val uri = resolver.insert(collectionUri, contentValues) ?: error("Failed to create MediaStore entry")

                // データの書き込み
                URL(url).openStream().use { input ->
                    resolver.openOutputStream(uri)?.use { output ->
                        input.copyTo(output)
                    } ?: error("Failed to open output stream")
                }
                Triple(uri, mimeType, isStandardImage)
            }.onFailure { Timber.w(it) }
        }

        if (result.isSuccess) {
            val (savedUri, mimeType, isStandardImage) = result.getOrThrow()

            snackBarHostState.showSnackbar(
                message = imageSavedMessage,
                actionLabel = viewActionLabel,
                withDismissAction = true,
            ).let { snackBarResult ->
                if (snackBarResult == SnackbarResult.ActionPerformed) {
                    val intent = if (isStandardImage) {
                        Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(savedUri, mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    } else {
                        // 標準画像以外の場合は、ダウンロードフォルダそのものを開く
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // Android 10以降: ダウンロードマネージャーの画面を開く
                            Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                        } else {
                            // Android 9以前: 汎用的なフォルダ閲覧（ファイルマネージャー）
                            Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "*/*"
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                        }
                    }

                    context.startActivity(intent)
                }
            }
        } else {
            snackBarHostState.showSnackbar(message = imageSaveFailedMessage)
        }
    }

    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(500)
            progress = null
        }
    }

    BackHandler(enabled = canGoBack, onBack = webView::goBack)

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        },
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

    imageUrlToSave?.let { url ->
        AlertDialog(
            onDismissRequest = ::clearImageUrlToSave,
            title = {
                Text(text = stringResource(R.string.save_image))
            },
            text = {
                Text(text = url)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            saveImage(url)
                        }
                        clearImageUrlToSave()
                    },
                ) {
                    Text(text = stringResource(R.string.save_image))
                }
            }, dismissButton = {
                TextButton(
                    onClick = ::clearImageUrlToSave,
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
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
