package com.cheeke.surfy.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.model.TrendingMedia
import com.cheeke.surfy.model.TrendingMediaResult
import kotlinx.coroutines.CancellationException

class TrendingPagingSource(
    private val fetchPage: suspend (page: Int) -> TrendingMedia
) : PagingSource<Int, TrendingMediaResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TrendingMediaResult> {
        val page = params.key ?: 1
        return try {
            val response = fetchPage(page)
            val totalPages = response.totalPages ?: 1
            LoadResult.Page(
                data = response.results.orEmpty(),
                prevKey = null,
                nextKey = if (totalPages > page) page + 1 else null
            )
        } catch (e: CancellationException) {
            Log.printStackTrace(tr = e)
            throw e
        } catch (e: Exception) {
            Log.printStackTrace(tr = e)
            LoadResult.Error(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TrendingMediaResult>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.let { anchorPage ->
                anchorPage.prevKey?.plus(other = 1) ?: anchorPage.nextKey?.minus(other = 1)
            }
        }
}