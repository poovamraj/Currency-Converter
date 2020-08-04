package com.poovam.currencyconverter

import android.os.AsyncTask
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.poovam.currencyconverter.core.db.keyvaluestore.KeyValueStoreDao
import com.poovam.currencyconverter.core.network.Status
import com.poovam.currencyconverter.core.network.cache.Cache
import com.poovam.currencyconverter.core.network.cache.cache
import com.poovam.currencyconverter.model.CurrencyRepositoryImpl
import com.poovam.currencyconverter.model.db.Currency
import com.poovam.currencyconverter.model.db.CurrencyDao
import com.poovam.currencyconverter.model.network.CurrencyList
import com.poovam.currencyconverter.model.network.CurrencyListApi
import com.poovam.currencyconverter.model.network.Quotes
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import junit.framework.Assert.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.runners.MockitoJUnitRunner
import retrofit2.HttpException
import retrofit2.Response
import java.lang.Exception
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class CurrencyRepositoryUnitTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    lateinit var currencyRepository: CurrencyRepositoryImpl

    private val mockCache = mockk<Cache>()

    private val mockCurrencyListApi = mockk<CurrencyListApi>()

    private val mockCurrencyDao = mockk<CurrencyDao>()

    private val mockKeyValueDao = mockk<KeyValueStoreDao>()

    private val currencyList =
        CurrencyList(mapOf(Pair("JPY", "Japanese Yen"), Pair("USD", "United States Dollar")))

    private val quotes =
        Quotes(mapOf(Pair("USDJPY", 106.09), Pair("USDUSD", 1.0)))

    private val selectedCurrency = Currency("JPY", "Japanese Yen", 106.09)

    private val listOfCurrencies =
        listOf(
            selectedCurrency,
            Currency("USD", "United States Dollar", 1.0)
        )

    @ExperimentalCoroutinesApi
    @Before
    fun before() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        currencyRepository =
            CurrencyRepositoryImpl(mockCurrencyListApi, mockCurrencyDao, mockKeyValueDao, mockCache)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun checkGetCurrenciesReturnsFromDb_ifCacheIsValid() = runBlockingTest {
        coEvery { mockCurrencyDao.getAllCurrencies() } returns listOfCurrencies
        coEvery { mockCache.isCacheValid(any(), any(), any()) } returns true
        val results = currencyRepository.getCurrencies().toList()
        assertThat(results[0].status, `is`(Status.LOADING))
        assertNull(results[0].data)
        assertThat(results[1].status, `is`(Status.SUCCESS))
        assertThat(results[1].data, `is`(listOfCurrencies))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun checkGetCurrenciesSyncsWithWebService_ifCacheIsInvalid() = runBlockingTest {
        coEvery { mockCurrencyDao.getAllCurrencies() } returns listOfCurrencies
        coEvery { mockCurrencyDao.saveAll(any()) } returns Unit
        coEvery { mockCache.storeLastSyncedTime(any()) } returns Unit
        coEvery {mockCurrencyListApi.getCurrencyList()} returns currencyList
        coEvery { mockCurrencyListApi.getCurrencyValueComparedToUSD() } returns quotes
        coEvery { mockCache.isCacheValid(any(), any(), any()) } returns false
        val results = currencyRepository.getCurrencies().toList()
        assertThat(results[0].status, `is`(Status.LOADING))
        assertNull(results[0].data)
        assertThat(results[1].status, `is`(Status.LOADING))
        assertThat(results[1].data, `is`(listOfCurrencies))
        assertThat(results[2].status, `is`(Status.SUCCESS))
        assertThat(results[2].data, `is`(listOfCurrencies))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun checkErrorStringThrown_ifRetrofitFails() = runBlockingTest {
        coEvery { mockCurrencyDao.getAllCurrencies() } returns listOfCurrencies
        coEvery { mockCurrencyDao.saveAll(any()) } returns Unit
        coEvery { mockCache.storeLastSyncedTime(any()) } returns Unit
        coEvery {mockCurrencyListApi.getCurrencyList()} throws HttpException(Response.error<String>(408, ResponseBody.create(
            MediaType.parse("text/plain"),"")))
        coEvery { mockCurrencyListApi.getCurrencyValueComparedToUSD() } returns quotes
        coEvery { mockCache.isCacheValid(any(), any(), any()) } returns false
        val results = currencyRepository.getCurrencies().toList()
        assertThat(results[0].status, `is`(Status.LOADING))
        assertNull(results[0].data)
        assertThat(results[1].status, `is`(Status.LOADING))
        assertThat(results[1].data, `is`(listOfCurrencies))
        assertThat(results[2].status, `is`(Status.ERROR))
        assertThat(results[2].message, `is`("Timeout"))
    }
}