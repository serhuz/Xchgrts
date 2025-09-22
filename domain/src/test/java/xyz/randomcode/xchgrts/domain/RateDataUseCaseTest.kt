package xyz.randomcode.xchgrts.domain

import arrow.optics.Getter
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import xyz.randomcode.xchgrts.domain.util.CurrencyInfoProvider
import xyz.randomcode.xchgrts.entities.CurrencyData
import xyz.randomcode.xchgrts.entities.CurrencyEntity
import xyz.randomcode.xchgrts.entities.ExchangeListItem

@RunWith(MockitoJUnitRunner::class)
class RateDataUseCaseTest {

    @Mock
    lateinit var api: ExchangeRateApi

    @Mock
    lateinit var dao: ExchangeRateDao

    @Mock
    lateinit var provider: CurrencyInfoProvider

    private lateinit var case: RateDataUseCase

    @Before
    fun setUp() {
        provider = mock {
            on { this.flagRes } doReturn Getter { com.blongho.country_data.R.drawable.globe }
            on { this.currencyName } doReturn Getter { "" }
        }

        case = RateDataUseCase(api, dao, provider)
    }

    @After
    fun tearDown() {
        reset(api, dao, provider)
    }

    @Test
    fun returnItems() = runTest {
        whenever(dao.getByDate(any())).thenReturn(
            listOf(
                CurrencyEntity(
                    "",
                    "",
                    "",
                    "AAA",
                    1,
                    "10"
                )
            )
        )

        val actual = case.getRatesForDate("20200101")

        val expected =
            ExchangeListItem("AAA", 1, "10", com.blongho.country_data.R.drawable.globe, "")

        Assertions.assertThat(actual).hasSize(1).contains(expected, Assertions.atIndex(0))
    }

    @Test
    fun requestItemsWhenNoneAreCached() = runTest {
        whenever(dao.getByDate(any())).thenReturn(emptyList())
        whenever(api.getRates(any())).thenReturn(emptyList())

        case.getRatesForDate("20200101")

        verify(api, times(1)).getRates(eq("20200101"))
        verify(dao, never()).insertAll(any())
    }

    @Test
    fun storeReceivedItems() = runTest {
        whenever(dao.getByDate(any())).thenReturn(emptyList())
        whenever(api.getRates(any())).thenReturn(listOf(CurrencyData("", "", "AAA", "", 1, 10f)))

        case.getRatesForDate("20200101")

        val expected = CurrencyEntity("", "", "AAA", "", 1, "10.0")

        verify(api, times(1)).getRates(eq("20200101"))
        verify(dao, times(1)).insertAll(argThat { size == 1 && first() == expected })
    }
}
