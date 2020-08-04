package com.poovam.currencyconverter.model.network

import com.poovam.currencyconverter.core.network.ACCESS_KEY
import retrofit2.http.GET

interface CurrencyListApi {
    @GET("list?access_key=$ACCESS_KEY")
    suspend fun getCurrencyList(): CurrencyList

    @GET("live?access_key=$ACCESS_KEY")
    suspend fun getCurrencyValueComparedToUSD(): Quotes
}

data class CurrencyList (
    val currencies: Map<String, String>
)

data class Quotes (
    val quotes: Map<String, Double>
)