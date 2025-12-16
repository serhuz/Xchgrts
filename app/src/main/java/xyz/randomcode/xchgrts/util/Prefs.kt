/*
 * Copyright 2021 Sergei Munovarov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.randomcode.xchgrts.util

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.randomcode.xchgrts.entities.WidgetSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefs @Inject constructor(
    private val preferences: SharedPreferences,
    private val dataStore: DataStore<Preferences>,
    private val adapter: JsonAdapter<WidgetSettings>,
    private val logger: ErrorLogger
) {

    var favCurrencies: Set<String>
        get() = loadFavoriteCurrencies()
        set(value) {
            storeFavoriteCurrencies(value)
        }

    fun storeFavoriteCurrencies(favs: Set<String>) {
        preferences.edit {
            putStringSet(KEY_FAV_CURRENCIES, favs)
        }
    }

    fun loadFavoriteCurrencies(): Set<String> =
        preferences.getStringSet(KEY_FAV_CURRENCIES, emptySet()) ?: emptySet()

    suspend fun storeWidgetSettings(settings: WidgetSettings) {
        runCatching {
            dataStore.edit {
                it[generatePrefId(settings.id)] = adapter.toJson(settings)
            }
        }.onFailure(logger::logError)
    }

    fun loadWidgetSettings(id: Int): Flow<WidgetSettings?> =
        dataStore.data.map {
            it[generatePrefId(id)]?.let(adapter::fromJson)
        }

    suspend fun removeSettings(id: Int) {
        runCatching {
            dataStore.edit {
                it.remove(generatePrefId(id))
            }
        }.onFailure(logger::logError)
    }

    private fun generatePrefId(id: Int) = stringPreferencesKey("${KEY_WIDGET_CODE}_${id}")

    companion object {
        private const val KEY_WIDGET_CODE = "widget_code"
        private const val KEY_FAV_CURRENCIES = "fav_currencies"
    }
}
