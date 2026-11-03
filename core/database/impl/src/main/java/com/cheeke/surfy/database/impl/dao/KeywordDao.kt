package com.cheeke.surfy.database.impl.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cheeke.surfy.database.impl.model.KeywordEntity

@Dao
interface KeywordDao {
    @Query(value = "SELECT * FROM recentlyKeyword ORDER BY timestamp DESC")
    fun getKeywords(): PagingSource<Int, KeywordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceKeyword(entity: KeywordEntity): Long

    @Query(value = "DELETE FROM recentlyKeyword")
    suspend fun deleteAll()

    @Query(value = "DELETE FROM recentlyKeyword WHERE keyword = :keyword")
    suspend fun delete(keyword: String)
}