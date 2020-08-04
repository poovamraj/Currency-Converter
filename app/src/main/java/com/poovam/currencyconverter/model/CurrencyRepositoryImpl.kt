package com.poovam.currencyconverter.model

import com.poovam.currencyconverter.core.cacheTime
import com.poovam.currencyconverter.core.cacheTimeUnit
import com.poovam.currencyconverter.core.db.AppDatabase
import com.poovam.currencyconverter.core.db.keyvaluestore.KeyValueStore
import com.poovam.currencyconverter.core.db.keyvaluestore.KeyValueStoreDao
import com.poovam.currencyconverter.core.defaultSelectedCurrency
import com.poovam.currencyconverter.core.network.Resource
import com.poovam.currencyconverter.core.network.ResponseHandler
import com.poovam.currencyconverter.core.network.cache.Cache
import com.poovam.currencyconverter.model.db.Currency
import com.poovam.currencyconverter.model.db.CurrencyDao
import com.poovam.currencyconverter.model.network.CurrencyListApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.dsl.module
import retrofit2.Retrofit

val currencyRepositoryModule = module {
    fun provideCurrencyListApi(retrofit: Retrofit): CurrencyListApi {
        return retrofit.create(CurrencyListApi::class.java)
    }

    fun provideCurrencyDao(db: AppDatabase): CurrencyDao {
        return db.currencyDao()
    }

    fun provideCurrencyRepository(
        currencyListApi: CurrencyListApi,
        currencyDao: CurrencyDao,
        keyValueStoreDao: KeyValueStoreDao,
        cache: Cache
    ): CurrencyRepository {
        return CurrencyRepositoryImpl(currencyListApi, currencyDao, keyValueStoreDao, cache)
    }

    factory { provideCurrencyListApi(get()) }
    factory { provideCurrencyDao(get()) }
    factory { provideCurrencyRepository(get(), get(), get(), get()) }
}

class CurrencyRepositoryImpl(
    private val currencyListApi: CurrencyListApi,
    private val currencyDao: CurrencyDao,
    private val keyValueStoreDao: KeyValueStoreDao,
    private val cache: Cache
) : CurrencyRepository {

    private val cacheKey = "CURRENCY_LAST_SYNCED_TIME"

    private val selectedCurrencyKey = "SELECTED_CURRENCY"

    override fun getCurrencies(): Flow<Resource<List<Currency>>> {
        return flow {
            emit(Resource.loading(null))
            if (!cache.isCacheValid(cacheKey, cacheTime, cacheTimeUnit)) {
                emit(Resource.loading(currencyDao.getAllCurrencies()))
                emit(syncWithWebService())
            } else {
                emit(Resource.success(currencyDao.getAllCurrencies()))
            }
        }
    }

    private suspend fun syncWithWebService(): Resource<ArrayList<Currency>> {
        return try {
            val currencyList = currencyListApi.getCurrencyList()
            try {
                val result = ArrayList<Currency>()
                val quotes = currencyListApi.getCurrencyValueComparedToUSD()
                for (currency in currencyList.currencies) {
                    val value = quotes.quotes["USD" + currency.key]
                    value?.let { result.add(Currency(currency.key, currency.value, it)) }
                }
                currencyDao.saveAll(result)
                cache.storeLastSyncedTime(cacheKey)
                ResponseHandler.handleSuccess(result)
            } catch (e: Exception) {
                ResponseHandler.handleException(e)
            }
        } catch (e: Exception) {
            ResponseHandler.handleException(e)
        }
    }

    override fun getLastSyncedTime(): Flow<Long?> {
        return cache.getLastSyncedTimeAsFlow(cacheKey)
    }

    override fun getSelectedCurrency(): Flow<Currency?> {
        return keyValueStoreDao.getValueAsFlow(selectedCurrencyKey).map {
            if (it?.value != null)
                currencyDao.getCurrency(it.value)
            else
                currencyDao.getCurrency(defaultSelectedCurrency)
        }
    }

    override suspend fun setSelectedCurrency(currencyCode: String) {
        keyValueStoreDao.putValue(KeyValueStore(selectedCurrencyKey, currencyCode))
    }
}