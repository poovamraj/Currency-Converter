package com.poovam.currencyconverter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.poovam.currencyconverter.core.network.Resource
import com.poovam.currencyconverter.model.CurrencyRepository
import com.poovam.currencyconverter.model.db.Currency
import com.poovam.currencyconverter.viewmodel.CurrencyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.setMain
import org.hamcrest.core.Is.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CurrencyViewModelUnitTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var mockCurrencyRepository: CurrencyRepository

    private lateinit var viewModel: CurrencyViewModel

    private val selectedCurrency = Currency("JPY", "Japanese Yen", 106.09)

    private val currencyList =
        listOf(
            Currency("USD", "United States Dollar", 1.0),
            selectedCurrency
        )

    @ExperimentalCoroutinesApi
    @Before
    fun before() {
        Dispatchers.setMain(Unconfined)
        `when`(mockCurrencyRepository.getCurrencies()).thenReturn(flow {
            emit(Resource.success(currencyList))
        })
        `when`(mockCurrencyRepository.getSelectedCurrency()).thenReturn(flow {
            emit(selectedCurrency)
        })
        `when`(mockCurrencyRepository.getLastSyncedTime()).thenReturn(flow { emit(0L) })
        viewModel = CurrencyViewModel(mockCurrencyRepository)
        viewModel.getSelectedCurrency().observeForever { }
        viewModel.getLastSyncedOn().observeForever { }
    }

    @Test
    fun checkIfCurrencyConversionWorks() {
        var checkUsdConversion: Double? = 0.0
        var checkJpyConversion: Double? = 0.0
        viewModel.getCurrencies().observeForever {
            checkUsdConversion = it?.data?.get(0)?.convertedValue
            checkJpyConversion = it?.data?.get(1)?.convertedValue
        }

        assertThat(checkUsdConversion, `is`(0.009425959091337543))
        assertThat(checkJpyConversion, `is`(1.0))

        viewModel.setAmountEntered(2.0)
        assertThat(checkUsdConversion, `is`(0.009425959091337543 * 2.0))
        assertThat(checkJpyConversion, `is`(1.0 * 2.0))
    }


    @Test
    fun checkIfResultSearchWorks() {
        var resultCurrencyCode: String? = null
        var searchResultLength: Int? = null
        viewModel.getCurrencies().observeForever {
            resultCurrencyCode = it.data?.getOrNull(0)?.code
            searchResultLength = it.data?.size
        }

        viewModel.setResultSearch("JPY")
        assertThat(resultCurrencyCode, `is`("JPY"))

        viewModel.setResultSearch("yen")
        assertThat(resultCurrencyCode, `is`("JPY"))

        viewModel.setResultSearch("009")
        assertThat(resultCurrencyCode, `is`("USD"))

        viewModel.setResultSearch("")
        assertThat(searchResultLength, `is`(2))

        viewModel.setResultSearch("EMPTY_RESULT")
        assertThat(searchResultLength, `is`(0))
    }

    @Test
    fun checkIfSourceCurrencySearchWorks() {
        var resultCurrencyCode: String? = null
        var searchResultLength: Int? = null
        viewModel.getSearchCurrency().observeForever {
            resultCurrencyCode = it.data?.getOrNull(0)?.code
            searchResultLength = it.data?.size
        }

        viewModel.setSourceCurrencySearch("JPY")
        assertThat(resultCurrencyCode, `is`("JPY"))

        viewModel.setSourceCurrencySearch("yen")
        assertThat(resultCurrencyCode, `is`("JPY"))

        viewModel.setSourceCurrencySearch("009")
        assertThat(resultCurrencyCode, `is`("USD"))

        viewModel.setSourceCurrencySearch("")
        assertThat(searchResultLength, `is`(2))

        viewModel.setSourceCurrencySearch("EMPTY_RESULT")
        assertThat(searchResultLength, `is`(0))
    }
}
