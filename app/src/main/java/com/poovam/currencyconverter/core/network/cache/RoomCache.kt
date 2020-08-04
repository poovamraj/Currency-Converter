package com.poovam.currencyconverter.core.network.cache

import com.poovam.currencyconverter.core.db.keyvaluestore.KeyValueStore
import com.poovam.currencyconverter.core.db.keyvaluestore.KeyValueStoreDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.dsl.module
import java.util.concurrent.TimeUnit


val cache = module {
    fun provideCache(keyValueStoreDao: KeyValueStoreDao): Cache {
        return RoomCache(
            keyValueStoreDao
        )
    }

    single { provideCache(get()) }
}

class RoomCache(private val keyValueStoreDao: KeyValueStoreDao) : Cache {

    override suspend fun getLastSyncedTime(key: String): Long? {
        return keyValueStoreDao.getValue(key)?.value?.toLong()
    }

    override fun getLastSyncedTimeAsFlow(key: String): Flow<Long?> {
        return keyValueStoreDao.getValueAsFlow(key).map { it?.value?.toLong() }
    }

    override suspend fun storeLastSyncedTime(key: String) {
        keyValueStoreDao.putValue(KeyValueStore(key, System.currentTimeMillis().toString()))
    }

    override suspend fun isCacheValid(key: String, duration: Long, time: TimeUnit): Boolean {
        val lastSyncedTime = getLastSyncedTime(key)
        return (lastSyncedTime ?: 0) + time.toMillis(duration) > System.currentTimeMillis()
    }
}