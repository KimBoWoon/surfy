package com.cheeke.surfy.data.repository

import androidx.paging.PagingSource
import com.cheeke.surfy.data.paging.TrendingPagingSource
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.network.TrendingRemoteDataSource
import javax.inject.Inject

interface HomeRepository {
    fun getTrending(type: MediaType, timeWindow: String, language: String): PagingSource<Int, TrendingMediaResult>
}

class HomeRepositoryImpl @Inject constructor(
    private val trendingApis: TrendingRemoteDataSource
) : HomeRepository {
    override fun getTrending(
        type: MediaType,
        timeWindow: String,
        language: String
    ): PagingSource<Int, TrendingMediaResult> =
        TrendingPagingSource(
            apis = trendingApis,
            type = type,
            timeWindow = timeWindow,
            language = language
        )
}