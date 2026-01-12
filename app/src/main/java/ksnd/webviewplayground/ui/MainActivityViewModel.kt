package ksnd.webviewplayground.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import ksnd.webviewplayground.ui.data.DataStoreRepository
import ksnd.webviewplayground.ui.theme.Theme
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    dataStoreRepository: DataStoreRepository,
) : ViewModel() {
    val theme = dataStoreRepository.theme().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Theme.AUTO,
    )
}
