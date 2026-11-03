package com.cheeke.surfy.database.impl.model

import androidx.room.Entity
import androidx.room.PrimaryKey

//@Entity(
//    tableName = "recentlyKeyword",
//    indices = [
//        Index(
//            value = ["keyword"],
//            unique = true
//        )
//    ]
//)
//data class KeywordEntity(
//    @PrimaryKey(autoGenerate = true)
//    val id: Int = 0,
//    val keyword: String,
//    val timestamp: Long
//)

@Entity(tableName = "recentlyKeyword")
data class KeywordEntity(
    @PrimaryKey
    val keyword: String,
    val timestamp: Long
)