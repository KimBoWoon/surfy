package com.cheeke.surfy.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.cheeke.surfy.data.paging.RecommendKeywordPagingSource
import com.cheeke.surfy.data.paging.SearchPagingSource
import com.cheeke.surfy.database.dao.KeywordDao
import com.cheeke.surfy.database.model.KeywordEntity
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.SearchKeyword
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.network.SearchRemoteDataSource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

class SearchRepositoryImpl @Inject constructor(
    private val searchApis: SearchRemoteDataSource,
    private val keywordDao: KeywordDao
) : SearchRepository {
    private companion object {
        const val PAGE_SIZE = 20
        const val INITIAL_LOAD_SIZE = 20
        const val ENABLE_PLACE_HOLDER = false
        val config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE, enablePlaceholders = ENABLE_PLACE_HOLDER)
    }

    override fun getRecentlyKeyword(): PagingSource<Int, KeywordEntity> = keywordDao.getKeywords()
    override suspend fun insert(keyword: String) {
        keywordDao.insertOrReplaceKeyword(
            entity = KeywordEntity(
                keyword = keyword,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun deleteAll() {
        keywordDao.deleteAll()
    }

    override suspend fun delete(entity: KeywordEntity) {
        keywordDao.delete(id = entity.id)
    }

    override fun search(
        type: SearchType,
        query: String,
        language: String,
        region: String,
        isAdult: Boolean
    ): Flow<PagingData<Media>> = Pager(
        config = config,
        pagingSourceFactory = {
            SearchPagingSource(
                apis = searchApis,
                type = type,
                query = query,
                language = language,
                region = region,
                isAdult = isAdult,
            )
        }
    ).flow

    override fun getRecommendKeyword(
        query: String
    ): Flow<PagingData<SearchKeyword>> = Pager(
        config = config,
        pagingSourceFactory = {
            RecommendKeywordPagingSource(
                apis = searchApis,
                query = query
            )
        }
    ).flow
}