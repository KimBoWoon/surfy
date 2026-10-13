package com.cheeke.surfy.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cheeke.surfy.data.paging.TrendingPagingSource
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.network.TrendingRemoteDataSource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class HomeRepositoryImpl @Inject constructor(
    private val trendingApis: TrendingRemoteDataSource
) : HomeRepository {
    private companion object {
        const val PAGE_SIZE = 20
        const val INITIAL_LOAD_SIZE = 20
        const val ENABLE_PLACE_HOLDER = false
        val config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE, enablePlaceholders = ENABLE_PLACE_HOLDER)
    }

    override fun getTrending(
        mediaType: MediaType,
        timeWindow: String,
        language: String
    ): Flow<PagingData<TrendingMediaResult>> = Pager(
        config = config,
        pagingSourceFactory = {
            TrendingPagingSource(
                fetchPage = { page ->
                    trendingApis.getTrending(
                        mediaType = mediaType,
                        timeWindow = timeWindow,
                        language = language,
                        page = page
                    )
                }
            )
        }
    ).flow
}