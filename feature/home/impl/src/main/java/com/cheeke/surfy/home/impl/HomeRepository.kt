package com.cheeke.surfy.home.impl

import androidx.paging.PagingSource
import com.cheeke.surfy.home.impl.paging.TrendingPagingSource
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.network.api.TrendingRemoteDataSource
import javax.inject.Inject

interface HomeRepository {
    fun getTrending(
        trending: Trending,
        timeWindow: String,
        language: String
    ): PagingSource<Int, TrendingMediaResult>

    enum class Trending {
        MOVIE, PEOPLE, TV
    }
}

class HomeRepositoryImpl @Inject constructor(
    private val trendingApis: TrendingRemoteDataSource
) : HomeRepository {
    override fun getTrending(
        trending: HomeRepository.Trending,
        timeWindow: String,
        language: String
    ): PagingSource<Int, TrendingMediaResult> =
        TrendingPagingSource(apis = trendingApis, trending = trending, timeWindow = timeWindow, language = language)
}