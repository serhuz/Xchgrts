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
import com.squareup.moshi.JsonAdapter
import xyz.randomcode.xchgrts.entities.WidgetSettings
import javax.inject.Inject

class Prefs @Inject constructor(
    private val preferences: SharedPreferences,
    private val adapter: JsonAdapter<WidgetSettings>
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

    fun storeWidgetSettings(settings: WidgetSettings) {
        preferences.edit {
            putString(generatePrefId(settings.id), adapter.toJson(settings))
        }
    }

    fun loadWidgetSettings(id: Int): WidgetSettings? =
        preferences.getString(generatePrefId(id), null)?.let(adapter::fromJson)

    fun removeSettings(id: Int) {
        preferences.edit { remove(generatePrefId(id)) }
    }

    private fun generatePrefId(id: Int) = "${KEY_WIDGET_CODE}_${id}"

    companion object {
        private const val KEY_WIDGET_CODE = "widget_code"
        private const val KEY_FAV_CURRENCIES = "fav_currencies"
    }
}
