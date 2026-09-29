package com.cheeke.surfy.home.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cheeke.surfy.home.impl.paging.TrendingPagingSource
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.network.api.TrendingRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface HomeRepository {
    fun getTrending(
        trending: MediaType,
        timeWindow: TimeWindow,
        language: String
    ): Flow<PagingData<TrendingMediaResult>>
}

class HomeRepositoryImpl @Inject constructor(
    private val trendingApis: TrendingRemoteDataSource
) : HomeRepository {
    override fun getTrending(
        trending: MediaType,
        timeWindow: TimeWindow,
        language: String
    ): Flow<PagingData<TrendingMediaResult>> =
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                TrendingPagingSource(
                    fetchPage = { page ->
                        trendingApis.getTrending(
                            mediaType = trending,
                            timeWindow = timeWindow.label,
                            language = language,
                            page = page
                        )
                    }
                )
            }
        ).flow
}