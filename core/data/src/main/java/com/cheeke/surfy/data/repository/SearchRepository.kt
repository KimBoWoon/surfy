package com.cheeke.surfy.data.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.cheeke.surfy.database.model.KeywordEntity
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.SearchKeyword
import com.cheeke.surfy.model.SearchType
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun getKeywords(): PagingSource<Int, KeywordEntity>
    suspend fun insert(keyword: String)
    suspend fun deleteAll()
    suspend fun delete(entity: KeywordEntity)
    fun getSearchPagingSource(type: SearchType, query: String, language: String, region: String, isAdult: Boolean): Flow<PagingData<Media>>
    fun getRecommendKeywordPagingSource(query: String): Flow<PagingData<SearchKeyword>>
}