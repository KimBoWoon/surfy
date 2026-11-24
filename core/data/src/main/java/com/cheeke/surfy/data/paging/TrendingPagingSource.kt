package com.cheeke.surfy.data.paging

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.network.TrendingRemoteDataSource
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class TrendingPagingSource(
    private val apis: TrendingRemoteDataSource,
    private val type: MediaType,
    private val timeWindow: String,
    private val language: String
) : RxPagingSource<Int, TrendingMediaResult>() {
    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, TrendingMediaResult>> {
        val page = params.key ?: 1

        return apis.getTrending(type = type, timeWindow = timeWindow, language = language, page = page)
            .subscribeOn(Schedulers.io())
            .map<LoadResult<Int, TrendingMediaResult>> { response ->
                LoadResult.Page(
                    data = response.results.orEmpty(),
                    prevKey = null,
                    nextKey = if ((response.totalPages ?: 1) > (params.key ?: 1)) (params.key ?: 1) + 1 else null
                )
            }.onErrorReturn { e ->
                Log.printStackTrace(e)
                LoadResult.Error(e)
            }
    }

    override fun getRefreshKey(state: PagingState<Int, TrendingMediaResult>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
}