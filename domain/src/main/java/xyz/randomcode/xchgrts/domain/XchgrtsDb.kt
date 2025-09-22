package xyz.randomcode.xchgrts.domain

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.randomcode.xchgrts.entities.CurrencyEntity

@Database(entities = [CurrencyEntity::class], version = 1)
abstract class XchgrtsDb : RoomDatabase() {

    abstract fun exchangeRateDao(): ExchangeRateDao
}