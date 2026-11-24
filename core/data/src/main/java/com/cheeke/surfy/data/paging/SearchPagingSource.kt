package com.cheeke.surfy.data.paging

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.network.SearchRemoteDataSource
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class SearchPagingSource(
    private val apis: SearchRemoteDataSource,
    private val type: SearchType,
    private val query: String,
    private val language: String,
    private val isAdult: Boolean
) : RxPagingSource<Int, Media>() {
    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Media>> =
        apis.search(type = type, query = query, includeAdult = isAdult, language = language, page = params.key ?: 1)
            .subscribeOn(Schedulers.io())
            .map<LoadResult<Int, Media>> { response ->
                LoadResult.Page(
                    data = response.results.orEmpty(),
                    prevKey = null,
                    nextKey = if ((response.totalPages ?: 1) > (params.key ?: 1)) (params.key ?: 1) + 1 else null
                )
            }.onErrorReturn { e ->
                Log.printStackTrace(e)
                LoadResult.Error(e)
            }

    override fun getRefreshKey(state: PagingState<Int, Media>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
}