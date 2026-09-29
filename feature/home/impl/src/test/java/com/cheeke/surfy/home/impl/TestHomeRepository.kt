package com.cheeke.surfy.home.impl

import androidx.paging.PagingData
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.testing.model.testTrendingMovie
import com.cheeke.surfy.testing.model.testTrendingPeople
import com.cheeke.surfy.testing.model.testTrendingTv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TestHomeRepository : HomeRepository {
    override fun getTrending(
        trending: MediaType,
        timeWindow: TimeWindow,
        language: String
    ): Flow<PagingData<TrendingMediaResult>> {
        val results: List<TrendingMediaResult> = when (trending) {
            MediaType.MOVIE -> testTrendingMovie.results.orEmpty()
            MediaType.TV -> testTrendingTv.results.orEmpty()
            MediaType.PEOPLE -> testTrendingPeople.results.orEmpty()
            MediaType.SERIES,
            MediaType.NONE -> throw IllegalArgumentException("잘못된 타입입니다.")
        }

        return flowOf(value = PagingData.from(data = results))
    }
}