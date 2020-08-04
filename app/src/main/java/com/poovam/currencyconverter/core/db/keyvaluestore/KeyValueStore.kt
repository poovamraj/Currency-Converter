package com.poovam.currencyconverter.core.db.keyvaluestore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class KeyValueStore(
    @PrimaryKey val key: String,

    val value: String
)