package com.cheeke.surfy.testing.repository

import android.annotation.SuppressLint
import androidx.paging.PagingData
import com.cheeke.surfy.data.repository.HomeRepository
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.testing.model.testTrendingMovie
import com.cheeke.surfy.testing.model.testTrendingPeople
import com.cheeke.surfy.testing.model.testTrendingTv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TestHomeRepository : HomeRepository {
    @SuppressLint("VisibleForTests")
    override fun getTrending(
        mediaType: MediaType,
        timeWindow: String,
        language: String
    ): Flow<PagingData<TrendingMediaResult>> {
        val results: List<TrendingMediaResult> = when (mediaType) {
            MediaType.MOVIE -> testTrendingMovie.results.orEmpty()
            MediaType.TV -> testTrendingTv.results.orEmpty()
            MediaType.PEOPLE -> testTrendingPeople.results.orEmpty()
            MediaType.SERIES,
            MediaType.NONE -> throw IllegalArgumentException("잘못된 타입입니다.")
        }

        return flowOf(value = PagingData.from(data = results))
    }
}