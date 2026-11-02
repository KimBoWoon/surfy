package com.cheeke.surfy.detail.impl.movie

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.network.api.MovieRemoteDataSource
import kotlin.coroutines.cancellation.CancellationException

class MovieReviewPagingSource(
    private val apis: MovieRemoteDataSource,
    private val id: Int,
    private val language: String,
    private val region: String
) : PagingSource<Int, Review>() {
    override fun getRefreshKey(state: PagingState<Int, Review>): Int? = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Review> =
        try {
            val page = params.key ?: 1
            val response = apis.getMovieReviews(movieId = id, language = "$language-$region", page = page)

            LoadResult.Page(
                data = response.results.orEmpty(),
                prevKey = null,
                nextKey = if ((response.totalPages ?: 1) > page) page + 1 else null
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Log.printStackTrace(tr = throwable)
            LoadResult.Error(throwable)
        }
}