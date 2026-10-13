package com.cheeke.surfy.testing.repository

import android.annotation.SuppressLint
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.testing.asPagingSourceFactory
import com.cheeke.surfy.data.repository.SearchRepository
import com.cheeke.surfy.database.model.KeywordEntity
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.SearchKeyword
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.testing.model.keywordList
import com.cheeke.surfy.testing.model.movieSearchTestData
import com.cheeke.surfy.testing.model.peopleSearchTestData
import com.cheeke.surfy.testing.model.seriesSearchTestData
import com.cheeke.surfy.testing.model.testRecommendedKeyword
import com.cheeke.surfy.testing.model.tvSearchTestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class TestSearchRepository : SearchRepository {
    val keywordDatabase = MutableStateFlow<List<KeywordEntity>>(value = emptyList())

    override fun getKeywords(): PagingSource<Int, KeywordEntity> =
        keywordList.asPagingSourceFactory().invoke()

    override suspend fun insert(keyword: String) {
        keywordDatabase.emit(value = keywordDatabase.value + KeywordEntity(id = 0, keyword = keyword, timestamp = 0))
    }

    override suspend fun deleteAll() {
        keywordDatabase.emit(value = emptyList())
    }

    override suspend fun delete(entity: KeywordEntity) {
        keywordDatabase.emit(value = keywordDatabase.value.filterNot { it.id == entity.id })
    }

    @SuppressLint("VisibleForTests")
    override fun getSearchPagingSource(
        type: SearchType,
        query: String,
        language: String,
        region: String,
        isAdult: Boolean
    ): Flow<PagingData<Media>> {
        val result = PagingData.from(
            data = when (type) {
                SearchType.MOVIE -> movieSearchTestData.results
                SearchType.MULTI -> movieSearchTestData.results
                SearchType.TV -> tvSearchTestData.results
                SearchType.PEOPLE -> peopleSearchTestData.results
                SearchType.SERIES -> seriesSearchTestData.results
            }.orEmpty()
        )

        return flowOf(value = result)
    }

    @SuppressLint("VisibleForTests")
    override fun getRecommendKeywordPagingSource(query: String): Flow<PagingData<SearchKeyword>> =
        flowOf(value = PagingData.from(data = testRecommendedKeyword))
}