package xyz.randomcode.xchgrts.di

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.preference.PreferenceManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import xyz.randomcode.xchgrts.dataStore
import xyz.randomcode.xchgrts.domain.ExchangeRateApi
import xyz.randomcode.xchgrts.domain.ExchangeRateDao
import xyz.randomcode.xchgrts.domain.RateDataUseCase
import xyz.randomcode.xchgrts.domain.util.CurrencyInfoProvider
import xyz.randomcode.xchgrts.domain.util.FlagResourceProvider
import xyz.randomcode.xchgrts.entities.WidgetSettings
import java.util.Locale

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideLocale(@ApplicationContext context: Context): Locale =
        context.resources.configuration.locales.get(0) ?: Locale.getDefault()

    @Provides
    fun provideWidgetSettingsJsonAdapter(moshi: Moshi): JsonAdapter<WidgetSettings> =
        moshi.adapter(WidgetSettings::class.java)

    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    fun provideCurrencyInfoProvider(
        locale: Locale,
        provider: FlagResourceProvider
    ): CurrencyInfoProvider =
        CurrencyInfoProvider(locale, provider)

    @Provides
    fun provideRateDataUseCase(
        api: ExchangeRateApi,
        dao: ExchangeRateDao,
        provider: CurrencyInfoProvider
    ): RateDataUseCase =
        RateDataUseCase(api, dao, provider)
}
