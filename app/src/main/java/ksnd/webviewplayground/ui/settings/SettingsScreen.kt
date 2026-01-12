package ksnd.webviewplayground.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ksnd.webviewplayground.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings))
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
    ) { innerPadding ->
        SettingsScreenContent(
            innerPadding = innerPadding,
        )
    }
}

@Composable
private fun SettingsScreenContent(
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .padding(paddingValues = innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        Text(text = "SETTINGS SCREEN CONTENT")
    }
}

@Preview
@Composable
private fun PreviewSettingsScreenContent() {
    SettingsScreenContent(innerPadding = PaddingValues())
}
