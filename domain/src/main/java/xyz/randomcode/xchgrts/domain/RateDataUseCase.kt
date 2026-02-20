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

package xyz.randomcode.xchgrts.domain

import androidx.annotation.WorkerThread
import arrow.core.getOrElse
import arrow.core.toOption
import arrow.optics.Iso
import xyz.randomcode.xchgrts.domain.util.CurrencyInfoProvider
import xyz.randomcode.xchgrts.domain.util.DateProvider
import xyz.randomcode.xchgrts.entities.CurrencyCode
import xyz.randomcode.xchgrts.entities.CurrencyData
import xyz.randomcode.xchgrts.entities.CurrencyEntity
import xyz.randomcode.xchgrts.entities.CurrencyListItem
import xyz.randomcode.xchgrts.entities.CurrentDate
import xyz.randomcode.xchgrts.entities.ExchangeListItem

class RateDataUseCase(
    private val api: ExchangeRateApi,
    private val dao: ExchangeRateDao,
    private val currencyInfoProvider: CurrencyInfoProvider
) {

    private val currencyIso: Iso<CurrencyData, CurrencyEntity> = Iso(
        get = {
            CurrencyEntity(
                it.date,
                it.time,
                it.numberCode,
                it.letterCode,
                it.units,
                it.amount.toString()
            )
        },
        reverseGet = {
            CurrencyData(
                it.date,
                it.time,
                it.numberCode,
                it.letterCode,
                it.units,
                it.amount.toFloat()
            )
        }
    )

    private fun CurrencyEntity.toExchangeListItem(): ExchangeListItem =
        ExchangeListItem(
            letterCode,
            units,
            amount,
            currencyInfoProvider.getFlagRes(letterCode),
            date,
            currencyInfoProvider.getCurrencyName(letterCode)
        )

    private fun CurrencyEntity.toCurrencyCode(): CurrencyCode =
        CurrencyCode(numberCode, letterCode)

    @WorkerThread
    suspend fun getCurrencyList(): List<CurrencyListItem> =
        dao.getDistinctCurrencyCodes()
            .ifEmpty {
                api.getRates(DateProvider().currentDate.requestFormat)
                    .map(currencyIso.reverse()::set)
                    .takeIf { it.isNotEmpty() }
                    ?.also { dao.insertAll(it) }
                    ?.map { it.toCurrencyCode() }
                    ?: emptyList()

            }
            .map(currencyInfoProvider::getCurrencyListItem)

    @WorkerThread
    suspend fun getRatesForDate(date: CurrentDate): List<ExchangeListItem> =
        dao.getByDate(date.entityFormat)
            .ifEmpty {
                api.getRates(date.requestFormat)
                    .map(currencyIso.reverse()::set)
                    .takeIf { it.isNotEmpty() }
                    ?.also { entities -> dao.insertAll(entities) }
                    ?: emptyList()
            }
            .map { it.toExchangeListItem() }
            .sortedBy(ExchangeListItem::letterCode)

    @WorkerThread
    suspend fun getRateForDate(date: CurrentDate, letterCode: String): ExchangeListItem =
        dao.getExchangeRateForCurrency(date.entityFormat, letterCode)
            .toOption()
            .getOrElse {
                api.getRates(date.requestFormat)
                    .map(currencyIso.reverse()::set)
                    .also { entities -> dao.insertAll(entities) }
                    .single { it.letterCode == letterCode }
            }
            .toExchangeListItem()

    @WorkerThread
    suspend fun updateRates(date: CurrentDate) {
        dao.countForDate(date.entityFormat)
            .takeIf { it == 0 }
            ?.run {
                api.getRates(date.requestFormat)
                    .map(currencyIso.reverse()::set)
                    .let { dao.insertAll(it) }
            }
    }
}
