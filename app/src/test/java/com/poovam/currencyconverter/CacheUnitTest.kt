package com.poovam.currencyconverter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.poovam.currencyconverter.core.db.keyvaluestore.KeyValueStore
import com.poovam.currencyconverter.core.db.keyvaluestore.KeyValueStoreDao
import com.poovam.currencyconverter.core.network.cache.Cache
import com.poovam.currencyconverter.core.network.cache.RoomCache
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class CacheUnitTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mockKeyValueDao = mockk<KeyValueStoreDao>()

    lateinit var cache: Cache

    @Before
    @ExperimentalCoroutinesApi
    fun before() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        cache = RoomCache(mockKeyValueDao)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun checkCache_isValid() = runBlockingTest {
        val time = System.currentTimeMillis()
        val lastSyncedTime = time - TimeUnit.MINUTES.toMillis(15)
        coEvery { mockKeyValueDao.getValue(any()) } returns
                KeyValueStore("",lastSyncedTime.toString())
        assertThat(cache.isCacheValid("", 30, TimeUnit.MINUTES), `is`(true))
    }

    @Test
    @ExperimentalCoroutinesApi
    fun checkCache_isNotValid() = runBlockingTest {
        val time = System.currentTimeMillis()
        val lastSyncedTime = time - TimeUnit.MINUTES.toMillis(45)
        coEvery { mockKeyValueDao.getValue(any()) } returns
                KeyValueStore("",lastSyncedTime.toString())
        assertThat(cache.isCacheValid("", 30, TimeUnit.MINUTES), `is`(false))
    }
}