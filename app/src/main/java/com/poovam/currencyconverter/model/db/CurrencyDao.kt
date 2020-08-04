package com.poovam.currencyconverter.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(products: List<Currency>)

    @Query("SELECT * FROM CURRENCY")
    suspend fun getAllCurrencies(): List<Currency>

    @Query("SELECT * FROM CURRENCY WHERE code = :currencyCode")
    suspend fun getCurrency(currencyCode: String): Currency?
}