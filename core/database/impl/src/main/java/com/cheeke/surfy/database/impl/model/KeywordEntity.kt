package com.cheeke.surfy.database.impl.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recentlyKeyword")
data class KeywordEntity(
    @PrimaryKey
    val keyword: String,
    val timestamp: Long
)