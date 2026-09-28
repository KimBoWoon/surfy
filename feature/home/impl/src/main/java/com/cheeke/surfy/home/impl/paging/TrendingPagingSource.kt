package com.cheeke.surfy.home.impl.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.home.impl.HomeRepository
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.network.api.TrendingRemoteDataSource

class TrendingPagingSource(
    private val trending: HomeRepository.Trending,
    private val apis: TrendingRemoteDataSource,
    private val timeWindow: String,
    private val language: String
) : PagingSource<Int, TrendingMediaResult>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TrendingMediaResult> =
        runCatching {
            val page = params.key ?: 1
            val response = when (trending) {
                HomeRepository.Trending.MOVIE -> apis.getTrendingMovie(timeWindow = timeWindow, language = language, page = page)
                HomeRepository.Trending.PEOPLE -> apis.getTrendingPeople(timeWindow = timeWindow, language = language, page = page)
                HomeRepository.Trending.TV -> apis.getTrendingTv(timeWindow = timeWindow, language = language, page = page)
            }
            val totalPages = response.totalPages ?: 1

            LoadResult.Page(
                data = response.results.orEmpty(),
                prevKey = null,
                nextKey = if (totalPages > page) page + 1 else null
            )
        }.getOrElse { e ->
            Log.printStackTrace(tr = e)
            LoadResult.Error(throwable = e)
        }

    override fun getRefreshKey(state: PagingState<Int, TrendingMediaResult>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
}