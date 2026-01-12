package ksnd.webviewplayground.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ksnd.webviewplayground.R
import ksnd.webviewplayground.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()

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
            theme = theme,
            updateTheme = viewModel::updateTheme,
        )
    }
}

@Composable
private fun SettingsScreenContent(
    innerPadding: PaddingValues,
    theme: Theme,
    updateTheme: (Theme) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(paddingValues = innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        Text(text = "テーマ")
        Spacer(modifier = Modifier.height(8.dp))

        Theme.entries.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = it == theme,
                        onClick = { updateTheme(it) },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = it == theme,
                    colors = RadioButtonDefaults.colors(),
                    onClick = { updateTheme(it) },
                )
                Text(
                    text = stringResource(
                        id = when (it) {
                            Theme.LIGHT -> R.string.theme_light
                            Theme.DARK -> R.string.theme_dark
                            Theme.AUTO -> R.string.theme_auto
                        }
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewSettingsScreenContent() {
    SettingsScreenContent(
        innerPadding = PaddingValues(),
        theme = Theme.AUTO,
        updateTheme = {},
    )
}
