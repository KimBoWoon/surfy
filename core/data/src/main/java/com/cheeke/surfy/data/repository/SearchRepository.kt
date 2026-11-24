package com.cheeke.surfy.data.repository

import androidx.paging.PagingSource
import com.cheeke.surfy.data.paging.RecommendKeywordPagingSource
import com.cheeke.surfy.data.paging.SearchPagingSource
import com.cheeke.surfy.database.dao.KeywordDao
import com.cheeke.surfy.database.model.KeywordEntity
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.SearchKeyword
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.network.SearchRemoteDataSource
import io.reactivex.rxjava3.core.Completable
import jakarta.inject.Inject
import kotlin.time.Clock

interface SearchRepository {
    fun getKeywords(): PagingSource<Int, KeywordEntity>
    fun insert(keyword: String): Completable
    fun deleteAll(): Completable
    fun delete(entity: KeywordEntity): Completable
    fun getSearchPagingSource(type: SearchType, query: String, language: String, region: String, isAdult: Boolean): PagingSource<Int, Media>
    fun getRecommendKeywordPagingSource(query: String): PagingSource<Int, SearchKeyword>
}

class SearchRepositoryImpl @Inject constructor(
    private val keywordDao: KeywordDao,
    private val searchApis: SearchRemoteDataSource
) : SearchRepository {
    override fun getKeywords(): PagingSource<Int, KeywordEntity> = keywordDao.getKeywords()
    override fun insert(keyword: String): Completable =
        keywordDao.insertOrReplaceKeyword(
            entity = KeywordEntity(
                keyword = keyword,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        )

    override fun deleteAll(): Completable = keywordDao.deleteAll()

    override fun delete(entity: KeywordEntity): Completable = keywordDao.delete(id = entity.id)

    override fun getSearchPagingSource(
        type: SearchType,
        query: String,
        language: String,
        region: String,
        isAdult: Boolean
    ): PagingSource<Int, Media> = SearchPagingSource(
        apis = searchApis,
        type = type,
        query = query,
        language = language,
        region = region,
        isAdult = isAdult,
    )

    override fun getRecommendKeywordPagingSource(
        query: String
    ): PagingSource<Int, SearchKeyword> = RecommendKeywordPagingSource(
        apis = searchApis,
        query = query
    )
}