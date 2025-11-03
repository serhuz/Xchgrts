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

package xyz.randomcode.xchgrts.widgets.config

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.singleOrNone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.domain.util.extractValue
import xyz.randomcode.xchgrts.entities.CurrencyListItem
import xyz.randomcode.xchgrts.entities.Failure
import xyz.randomcode.xchgrts.entities.Loading
import xyz.randomcode.xchgrts.entities.Resource
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.entities.WidgetSettings
import xyz.randomcode.xchgrts.entities.isSelected
import xyz.randomcode.xchgrts.util.Prefs
import xyz.randomcode.xchgrts.util.SingleLiveEvent
import xyz.randomcode.xchgrts.util.currentValue
import javax.inject.Inject

@HiltViewModel
class CurrencySelectionViewModel @Inject constructor(
    private val state: SavedStateHandle,
    val case: RateDataUseCase,
    val prefs: Prefs
) : ViewModel() {

    val currencies: MutableLiveData<Resource<List<CurrencyListItem>>> = MutableLiveData()
    val hasSelectedItem: MutableLiveData<Boolean> = MutableLiveData(false)
    val confirmSelection: SingleLiveEvent<Nothing?> = SingleLiveEvent()

    var widgetId: Int = Int.MIN_VALUE

    init {
        loadCurrencyList()
    }

    fun loadCurrencyList() {
        viewModelScope.launch {
            Either.catch {
                currencies.value = Loading
                state.get<List<CurrencyListItem>>(ITEMS).orEmpty()
                    .ifEmpty {
                        withContext(Dispatchers.IO) {
                            case.getCurrencyList()
                        }
                    }
                    .also { state[ITEMS] = it }
            }
                .mapLeft(::Failure)
                .map(::Success)
                .fold(currencies::setValue, currencies::setValue)
        }
    }

    fun updateItemSelection(letterCode: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                currencies.currentValue
                    .flatMap(Resource<List<CurrencyListItem>>::extractValue)
                    .getOrElse { error("List is empty") }
                    .map {
                        if (it.letterCode == letterCode) {
                            CurrencyListItem.isSelected.modify(it) { true }
                        } else {
                            CurrencyListItem.isSelected.modify(it) { false }
                        }
                    }
                    .also {
                        withContext(Dispatchers.Main) {
                            (it.count { it.isSelected } > 0).let(hasSelectedItem::setValue)
                        }
                    }
                    .let(::Success)
            }.let { withContext(Dispatchers.Main) { currencies.value = it } }
        }
    }

    fun confirmSelection() {
        currencies.currentValue
            .flatMap(Resource<List<CurrencyListItem>>::extractValue)
            .flatMap { it.singleOrNone(CurrencyListItem::isSelected) }
            .map(CurrencyListItem::letterCode)
            .fold(
                { error("No selected currency") },
                {
                    prefs.storeWidgetSettings(WidgetSettings(widgetId, it))
                    confirmSelection.call()
                }
            )
    }

    companion object {
        private const val ITEMS = "currencyCodes"
    }
}
