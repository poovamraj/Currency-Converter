package com.poovam.currencyconverter.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Currency(
    @PrimaryKey
    var code: String,

    var name: String,

    var comparedToUsd: Double
)