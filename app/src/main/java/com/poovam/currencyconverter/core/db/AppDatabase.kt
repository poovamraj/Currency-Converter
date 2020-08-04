package com.poovam.currencyconverter.core.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.poovam.currencyconverter.core.db.keyvaluestore.KeyValueStore
import com.poovam.currencyconverter.core.db.keyvaluestore.KeyValueStoreDao
import com.poovam.currencyconverter.model.db.Currency
import com.poovam.currencyconverter.model.db.CurrencyDao
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module


val dbModule = module {

    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "app-database")
            .build()
    }

    single { provideDatabase(androidApplication()) }
}

@Database(
    entities = [Currency::class, KeyValueStore::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun keyValueStoreDao(): KeyValueStoreDao
}