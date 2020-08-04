package com.poovam.currencyconverter.core.network

import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofitModule = module {
    factory { provideOkHttpClient() }
    single { provideRetrofit(get()) }
}

const val API_URL = "http://api.currencylayer.com"

fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient().newBuilder().build()
}

fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder().baseUrl(API_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}


const val ACCESS_KEY = "f48af6036d4cd05fc0b9af63894f9616"