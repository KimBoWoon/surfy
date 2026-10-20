package com.cheeke.surfy.search.impl.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.network.api.SearchRemoteDataSource
import kotlin.coroutines.cancellation.CancellationException

class SearchPagingSource(
    private val apis: SearchRemoteDataSource,
    private val type: SearchType,
    private val query: String,
    private val language: String,
    private val isAdult: Boolean
) : PagingSource<Int, Media>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Media> {
        val currentPage = params.key ?: FIRST_PAGE
        return try {
            val response = apis.search(type = type, query = query, isAdult = isAdult, language = language, page = currentPage)
            val totalPages = response.totalPages ?: FIRST_PAGE

            LoadResult.Page(
                data = response.results.orEmpty(),
                prevKey = null,
                nextKey = if (currentPage < totalPages) currentPage + 1 else null
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Log.printStackTrace(throwable)
            LoadResult.Error(throwable)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Media>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    private companion object {
        const val FIRST_PAGE = 1
    }
}