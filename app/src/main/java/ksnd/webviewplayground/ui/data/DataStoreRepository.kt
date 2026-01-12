package ksnd.webviewplayground.ui.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ksnd.webviewplayground.ui.theme.Theme
import timber.log.Timber
import javax.inject.Inject

object PreferenceKeys {
    val THEME = stringPreferencesKey("theme")
}

interface DataStoreRepository {
    fun theme(): Flow<Theme>
    suspend fun updateTheme(theme: Theme)
}

class DataStoreRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : DataStoreRepository {
    override fun theme(): Flow<Theme> = dataStore.data
        .catch { Timber.e(it) }
        .map { preferences ->
            preferences[PreferenceKeys.THEME]?.let {
                runCatching { enumValueOf<Theme>(it) }.getOrNull()
            } ?: Theme.AUTO
        }

    override suspend fun updateTheme(theme: Theme) {
        dataStore.edit { it[PreferenceKeys.THEME] = theme.name }
    }
}
