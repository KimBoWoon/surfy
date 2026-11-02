package com.cheeke.surfy.search.impl.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.model.SearchKeyword
import com.cheeke.surfy.network.api.SearchRemoteDataSource
import kotlin.coroutines.cancellation.CancellationException

class RecommendKeywordPagingSource(
    private val apis: SearchRemoteDataSource,
    private val query: String
): PagingSource<Int, SearchKeyword>() {
    override fun getRefreshKey(state: PagingState<Int, SearchKeyword>): Int? = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchKeyword> =
        try {
            if (query.isNotEmpty()) {
                val page = params.key ?: 1
                val response = apis.getSearchKeyword(query = query, page = page)

                LoadResult.Page(
                    data = response.results.orEmpty(),
                    prevKey = null,
                    nextKey = if (response.totalPages == page) null else page + 1
                )
            } else {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Log.printStackTrace(tr = throwable)
            LoadResult.Error(throwable)
        }
}