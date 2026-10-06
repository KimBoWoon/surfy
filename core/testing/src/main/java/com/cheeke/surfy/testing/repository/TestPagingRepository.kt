package com.cheeke.surfy.testing.repository

import android.annotation.SuppressLint
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.testing.asPagingSourceFactory
import com.cheeke.surfy.data.repository.PagingRepository
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.SearchKeyword
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.testing.model.movieSearchTestData
import com.cheeke.surfy.testing.model.peopleSearchTestData
import com.cheeke.surfy.testing.model.seriesSearchTestData
import com.cheeke.surfy.testing.model.testMovieReviews
import com.cheeke.surfy.testing.model.testRecommendedKeyword
import com.cheeke.surfy.testing.model.testTrendingMovie
import com.cheeke.surfy.testing.model.testTrendingPeople
import com.cheeke.surfy.testing.model.testTrendingTv
import com.cheeke.surfy.testing.model.testTvReviews
import com.cheeke.surfy.testing.model.tvSearchTestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TestPagingRepository : PagingRepository {
    @SuppressLint("VisibleForTests")
    private val testPagingSource = (0..100).map {
        SimilarMedia(
//            genres = listOf(Genre(id = it)),
            releaseDate = "releaseDate_$it",
            title = "title_$it",
            adult = true,
            id = it,
            posterPath = "/imagePath_$it.png"
        )
    }.asPagingSourceFactory().invoke()

    @SuppressLint("VisibleForTests")
    override fun getSearchPagingSource(
        type: SearchType,
        query: String,
        language: String,
        region: String,
        isAdult: Boolean
    ): PagingSource<Int, Media> {
        return (when (type) {
            SearchType.MOVIE -> movieSearchTestData.results
            SearchType.MULTI -> movieSearchTestData.results
            SearchType.TV -> tvSearchTestData.results
            SearchType.PEOPLE -> peopleSearchTestData.results
            SearchType.SERIES -> seriesSearchTestData.results
        }.orEmpty()).asPagingSourceFactory().invoke()
    }

    override fun getSimilarMoviePagingSource(
        id: Int,
        language: String,
        region: String
    ): PagingSource<Int, SimilarMedia> = testPagingSource

    @SuppressLint("VisibleForTests")
    override fun getRecommendKeywordPagingSource(query: String): PagingSource<Int, SearchKeyword> =
        testRecommendedKeyword.asPagingSourceFactory().invoke()

    @SuppressLint("VisibleForTests")
    override fun getMovieReviews(
        movieId: Int, language: String, region: String
    ): PagingSource<Int, Review> = testMovieReviews.asPagingSourceFactory().invoke()

    @SuppressLint("VisibleForTests")
    override fun getSimilarTvPagingSource(
        id: Int,
        language: String,
        region: String
    ): PagingSource<Int, SimilarMedia> = (0..100).map {
        SimilarMedia(
//            genres = listOf(Genre(id = it)),
            firstAirDate = "firstAirDate_$it",
            title = "title_$it",
            adult = true,
            id = it,
            posterPath = "/imagePath_$it.png"
        )
    }.asPagingSourceFactory().invoke()

    @SuppressLint("VisibleForTests")
    override fun getTvReviews(
        seriesId: Int,
        language: String,
        region: String
    ): PagingSource<Int, Review> = testTvReviews.asPagingSourceFactory().invoke()

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