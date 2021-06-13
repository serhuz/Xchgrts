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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.randomcode.xchgrts.entities.CurrencyCode
import xyz.randomcode.xchgrts.entities.CurrencyEntity

@Dao
interface ExchangeRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CurrencyEntity>)

    @Query("SELECT * FROM rates WHERE date = :date ORDER BY letterCode")
    suspend fun getByDate(date: String): List<CurrencyEntity>

    @Query("SELECT COUNT(*) FROM rates WHERE date = :date")
    suspend fun countForDate(date: String): Int

    @Query("SELECT DISTINCT letterCode, numberCode FROM rates ORDER BY letterCode")
    suspend fun getDistinctCurrencyCodes(): List<CurrencyCode>

    @Query("DELETE FROM rates WHERE date != :date")
    suspend fun deleteNotEqualToDate(date: String): Int

    @Query("SELECT * FROM rates WHERE date = :date AND letterCode = :letterCode LIMIT 1")
    suspend fun getExchangeRateForCurrency(date: String, letterCode: String): CurrencyEntity?
}
