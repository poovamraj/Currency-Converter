package com.poovam.currencyconverter.core.network.cache

import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

interface Cache {
    suspend fun getLastSyncedTime(key: String): Long?

    fun getLastSyncedTimeAsFlow(key: String): Flow<Long?>

    suspend fun storeLastSyncedTime(key: String)

    suspend fun isCacheValid(key: String, duration: Long, time: TimeUnit): Boolean
}