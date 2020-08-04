package com.poovam.currencyconverter.core.db.keyvaluestore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.poovam.currencyconverter.core.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.dsl.module


val keyValueStoreModule = module {
    fun provideKeyValueStoreDao(db: AppDatabase): KeyValueStoreDao {
        return db.keyValueStoreDao()
    }

    single { provideKeyValueStoreDao(get()) }
}

@Dao
interface KeyValueStoreDao {
    @Query("SELECT * FROM KEYVALUESTORE where `key`=:key LIMIT 1")
    fun getValueAsFlow(key: String): Flow<KeyValueStore?>

    @Query("SELECT * FROM KEYVALUESTORE where `key`=:key LIMIT 1")
    suspend fun getValue(key: String): KeyValueStore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putValue(keyValue: KeyValueStore)
}