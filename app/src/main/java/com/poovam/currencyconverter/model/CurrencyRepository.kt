package com.poovam.currencyconverter.model

import com.poovam.currencyconverter.core.network.Resource
import com.poovam.currencyconverter.model.db.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun getCurrencies(): Flow<Resource<List<Currency>>>

    fun getLastSyncedTime(): Flow<Long?>

    fun getSelectedCurrency(): Flow<Currency?>

    suspend fun setSelectedCurrency(currencyCode: String)
}