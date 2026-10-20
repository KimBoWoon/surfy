package com.cheeke.surfy.detail.api.movie

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.cheeke.surfy.detail.api.Repository
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.MovieWatchProvider
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SimilarMedia
import kotlinx.coroutines.flow.Flow

interface MovieRepository : Repository<Movie> {
    override fun getData(id: Int): Flow<Movie>
    fun getMovieSeries(collectionId: Int): Flow<Series>
    fun getMovieSeriesImageList(collectionId: Int): Flow<ImageList>
    fun getMovieWatchProviders(movieId: Int): Flow<MovieWatchProvider>
    fun getUpComingMovies(): Flow<PagingData<Movie>>
    fun getNowPlayingMovies(): Flow<PagingData<Movie>>
    fun getPopularMovies(): Flow<List<Movie>>
    suspend fun getNextWeekReleaseMovies(): List<Movie>
    fun getSimilarMoviePagingSource(
        id: Int,
        language: String,
        region: String
    ): PagingSource<Int, SimilarMedia>

    fun getMovieReviews(movieId: Int, language: String, region: String): PagingSource<Int, Review>
}