package com.poovam.currencyconverter.viewmodel

import androidx.lifecycle.*
import com.poovam.currencyconverter.core.dateFormat
import com.poovam.currencyconverter.core.network.Resource
import com.poovam.currencyconverter.model.CurrencyRepository
import com.poovam.currencyconverter.model.db.Currency
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

val currencyViewModel = module {
    fun provideViewModel(repository: CurrencyRepository): CurrencyViewModel {
        return CurrencyViewModel(repository)
    }

    viewModel { provideViewModel(get()) }
}

class CurrencyViewModel(private val repository: CurrencyRepository) : ViewModel() {

    private var currencySource: LiveData<Resource<List<Currency>>>? = null

    private val currencies = MediatorLiveData<Resource<List<CurrencyDataViewModel>>>()

    private val searchSource = MediatorLiveData<Resource<List<CurrencyDataViewModel>>>()

    private val selectedCurrency = MediatorLiveData<Currency>()

    private val lastSyncedOn = MediatorLiveData<String>()

    private var resultSearchQuery: String? = null

    var sourceCurrencySearchQuery: String? = null
        private set

    private var amountEntered = 1.0

    init {
        loadSelectedCurrency()
        loadLastSyncedOn()
        loadCurrencySource(true)
    }

    private fun loadSelectedCurrency() {
        selectedCurrency.addSource(repository.getSelectedCurrency().asLiveData()) {
            selectedCurrency.value = it
            loadCurrencySource(false)
        }
    }

    private fun loadLastSyncedOn() {
        lastSyncedOn.addSource(repository.getLastSyncedTime().asLiveData()) {
            it?.let {
                val date = Date(it)
                val df2 = SimpleDateFormat(dateFormat)
                lastSyncedOn.value = df2.format(date)
            }
        }
    }

    fun getCurrencies(): LiveData<Resource<List<CurrencyDataViewModel>>> {
        return currencies
    }

    fun getSelectedCurrency(): LiveData<Currency> {
        return selectedCurrency
    }

    fun getLastSyncedOn(): LiveData<String> {
        return lastSyncedOn
    }

    fun getSearchCurrency(): LiveData<Resource<List<CurrencyDataViewModel>>> {
        return searchSource
    }

    fun setResultSearch(query: String?) {
        resultSearchQuery = query
        loadCurrencySource(false)
    }

    fun setSourceCurrencySearch(query: String?) {
        sourceCurrencySearchQuery = query
        loadCurrencySource(false)
    }

    fun setAmountEntered(value: Double) {
        amountEntered = value
        loadCurrencySource(false)
    }

    fun setSelectedCurrency(currencyCode: String) {
        viewModelScope.launch { repository.setSelectedCurrency(currencyCode) }
    }

    fun refreshCurrencySource(){
        loadCurrencySource(true)
    }

    private fun loadCurrencySource(refresh: Boolean) {
        currencySource?.let {
            currencies.removeSource(it)
            searchSource.removeSource(it)
        }
        if (refresh) {
            currencySource = repository.getCurrencies().asLiveData()
        }
        currencySource?.let { it ->
            currencies.addSource(it) { resource ->
                val data = processSource(resource.data, resultSearchQuery)
                currencies.value = Resource(resource.status, data, resource.message)
            }
            searchSource.addSource(it) { resource ->
                val data = processSource(resource.data, sourceCurrencySearchQuery)
                searchSource.value = Resource(resource.status, data, resource.message)
            }
        }
    }

    /**
     * This method will convert [Currency] to [CurrencyDataViewModel] and get the desired
     * conversion value for a searched currency
     */
    private fun processSource(
        currencies: List<Currency>?,
        searchQuery: String?
    ): List<CurrencyDataViewModel> {
        return currencies?.map { currency ->
                CurrencyDataViewModel(
                    currency.code,
                    currency.name,
                    currency.comparedToUsd,
                    convertCurrency(currency.comparedToUsd, selectedCurrency.value)
                )
            }?.filter { it -> search(it, searchQuery) } ?: ArrayList()
    }

    /**
     * This handles the conversion between currencies
     */
    private fun convertCurrency(comparedToUsd: Double, selectedCurrency: Currency?): Double {
        return if (selectedCurrency?.comparedToUsd == null)
            convertValue(1.0, comparedToUsd, amountEntered)
        else
            convertValue(selectedCurrency.comparedToUsd, comparedToUsd, amountEntered)
    }

    /**
     * We handle the currency conversion calculation here
     * We find the USD value for the selected currency by dividing [usdToSelectedCurrency]
     * Then multiply the result with [destinationCurrencyComparedToUsd] and [amount]
     * ex -
     * 1USD = 0.76GBP available in [usdToSelectedCurrency]
     * 1GBP = 1.31USD obtained by dividing 1 with [usdToSelectedCurrency]
     * result = 1.31 * [destinationCurrencyComparedToUsd] * [amount]
     */
    private fun convertValue(
        usdToSelectedCurrency: Double,
        destinationCurrencyComparedToUsd: Double,
        amount: Double
    ): Double {
        val selectedCurrencyToUsd: Double = 1 / usdToSelectedCurrency
        return selectedCurrencyToUsd * destinationCurrencyComparedToUsd * amount
    }

    /**
     * We search for [CurrencyDataViewModel.code] [CurrencyDataViewModel.name] and
     * [CurrencyDataViewModel.convertedValue]
     */
    private fun search(currency: CurrencyDataViewModel, query: String?): Boolean {
        return query?.let { q ->
            currency.code.contains(q, true) ||
                    currency.name.contains(q, true) ||
                    currency.convertedValue.toString().contains(q, true)
        } ?: true
    }
}

data class CurrencyDataViewModel(
    val code: String,
    val name: String,
    val comparedToUsd: Double,
    val convertedValue: Double
)