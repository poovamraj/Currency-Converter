package com.poovam.currencyconverter

import android.app.Application
import com.poovam.currencyconverter.core.db.dbModule
import com.poovam.currencyconverter.core.db.keyvaluestore.keyValueStoreModule
import com.poovam.currencyconverter.core.network.cache.cache
import com.poovam.currencyconverter.core.network.retrofitModule
import com.poovam.currencyconverter.model.currencyRepositoryModule
import com.poovam.currencyconverter.viewmodel.currencyViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CurrencyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@CurrencyApplication)
            modules(
                dbModule,
                retrofitModule,
                cache,
                currencyRepositoryModule,
                currencyViewModel,
                keyValueStoreModule
            )
        }
    }
}