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

package xyz.randomcode.xchgrts.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.identity
import arrow.core.none
import arrow.core.some
import arrow.optics.Getter
import arrow.optics.Lens
import arrow.optics.Prism
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.domain.util.DateProvider
import xyz.randomcode.xchgrts.domain.util.extractValue
import xyz.randomcode.xchgrts.entities.ExchangeListItem
import xyz.randomcode.xchgrts.entities.Failure
import xyz.randomcode.xchgrts.entities.FavItem
import xyz.randomcode.xchgrts.entities.Loading
import xyz.randomcode.xchgrts.entities.Resource
import xyz.randomcode.xchgrts.entities.Success
import xyz.randomcode.xchgrts.util.ErrorLogger
import xyz.randomcode.xchgrts.util.Prefs
import xyz.randomcode.xchgrts.util.currentValue
import xyz.randomcode.xchgrts.util.modify
import javax.inject.Inject

@HiltViewModel
class ExchangeRatesViewModel @Inject constructor(
    private val prefs: Prefs,
    private val case: RateDataUseCase,
    private val logger: ErrorLogger
) : ViewModel() {

    val items: MutableLiveData<Resource<List<RateListItem>>> = MutableLiveData()

    val date: Flow<String> = items.asFlow()
        .filterIsInstance<Success<List<RateListItem>>>()
        .map { it.data }
        .map {
            it.fold(HashSet<String>()) { acc, item ->
                acc.apply { add(item.data.date) }
            }.single()
        }
        .catch { emit("") }


    private val favItem = Getter<ExchangeListItem, RateListItem> {
        FavItem(
            prefs.favCurrencies.contains(it.letterCode),
            it
        )
    }

    private var job: Job? = null

    init {
        loadRates()
    }

    fun loadRates() {
        job = viewModelScope.launch {
            items.value = Loading
            Either.catch {
                withContext(Dispatchers.IO) {
                    case.getRatesForDate(DateProvider().currentDate)
                }
            }
                .map(this@ExchangeRatesViewModel::mapFavorites)
                .map(this@ExchangeRatesViewModel::sortFavorites)
                .onLeft(logger::logError)
                .map(::Success)
                .mapLeft(::Failure)
                .fold(items::setValue, items::setValue)
        }
    }

    private fun mapFavorites(list: List<ExchangeListItem>) = list.map(favItem::get)

    private fun sortFavorites(list: List<RateListItem>) =
        list.sortedWith { o1, o2 ->
            if ((o1.isFavorite && o2.isFavorite) || (!o1.isFavorite && !o2.isFavorite)) {
                o1.data.letterCode.compareTo(o2.data.letterCode)
            } else if (o1.isFavorite) {
                Int.MIN_VALUE
            } else {
                Int.MAX_VALUE
            }
        }

    fun updateFavorites(letterCode: String) {
        items.currentValue
            .flatMap(Resource<List<RateListItem>>::extractValue)
            .fold(
                { error("List is empty") },
                { items ->
                    prefs.favCurrencies = if (prefs.favCurrencies.contains(letterCode)) {
                        prefs.favCurrencies - letterCode
                    } else {
                        prefs.favCurrencies + letterCode
                    }

                    items.modify(codeEquals(letterCode)) {
                        isFavorite<ExchangeListItem>().modify(
                            it,
                            Boolean::not
                        )
                    }
                        .let(this::sortFavorites)
                        .let(::Success)
                        .let(this.items::setValue)
                }
            )
    }

    private fun codeEquals(letterCode: String) = Prism<RateListItem, RateListItem>(
        getOption = { if (it.data.letterCode == letterCode) it.some() else none() },
        reverseGet = ::identity
    )

    private fun <T> isFavorite() = Lens(get = FavItem<T>::isFavorite, set = FavItem<T>::copy)

    override fun onCleared() {
        job?.cancel()
    }
}

typealias RateListItem = FavItem<ExchangeListItem>
