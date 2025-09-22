package com.currand60.karooexttemplate.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.currand60.karooexttemplate.data.ConfigData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ConfigurationManager (
    private val context: Context,
){


    companion object {
        private val EXAMPLE_VALUE_KEY = booleanPreferencesKey("exampleValue")
    }

    suspend fun saveConfig(config: ConfigData) {
        Timber.d("Attempting to save configuration to DataStore: $config")
        context.dataStore.edit { preferences ->
            preferences[EXAMPLE_VALUE_KEY] = config.exampleValue
        }
        Timber.i("Configuration successfully saved to DataStore.")
    }

    suspend fun getConfig(): ConfigData {
        Timber.d("Attempting to retrieve configuration from DataStore.")
        return context.dataStore.data.map { preferences ->
            val config = ConfigData(
                exampleValue = preferences[EXAMPLE_VALUE_KEY] ?: ConfigData.DEFAULT.exampleValue,
            )
            Timber.d("Retrieved configuration: $config")
            config
        }.first()
    }

    fun getConfigFlow(): Flow<ConfigData> {
        return context.dataStore.data.map { preferences ->
            ConfigData(
                exampleValue = preferences[EXAMPLE_VALUE_KEY] ?: ConfigData.DEFAULT.exampleValue,
                )
        }.distinctUntilChanged()
    }
}