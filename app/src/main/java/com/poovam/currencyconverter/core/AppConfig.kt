package com.poovam.currencyconverter.core

import java.util.concurrent.TimeUnit

//Later this can be moved into settings and can be fetched dynamically based on user preference

const val dateFormat = "dd MMM hh:mm:ss aa"
const val defaultSelectedCurrency = "USD"
const val cacheTime = 30L
val cacheTimeUnit = TimeUnit.MINUTES
const val roundTo = 3