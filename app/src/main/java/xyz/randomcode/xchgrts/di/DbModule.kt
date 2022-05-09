package xyz.randomcode.xchgrts.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import xyz.randomcode.xchgrts.db.XchgrtsDb
import xyz.randomcode.xchgrts.domain.ExchangeRateDao

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides
    fun provideXchgrtsDb(@ApplicationContext context: Context): XchgrtsDb =
        Room.databaseBuilder(context, XchgrtsDb::class.java, DB_NAME).build()

    @Provides
    fun provideExchangeRatesDao(db: XchgrtsDb): ExchangeRateDao =
        db.exchangeRateDao()

    private const val DB_NAME = "xchgrts.db"
}
