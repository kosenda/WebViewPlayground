package ksnd.webviewplayground.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ksnd.webviewplayground.ui.data.DataStoreRepository
import ksnd.webviewplayground.ui.theme.Theme
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
) : ViewModel() {
    val theme = dataStoreRepository.theme().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Theme.AUTO,
    )

    fun updateTheme(newTheme: Theme) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStoreRepository.updateTheme(newTheme)
        }
    }
}