package com.cheeke.surfy.data.repository

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.cheeke.surfy.database.model.MovieEntity
import com.cheeke.surfy.database.model.NowPlayingMovieEntity
import com.cheeke.surfy.database.model.UpComingMovieEntity
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.MovieWatchProvider
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SimilarMedia
import kotlinx.coroutines.flow.Flow

interface MovieRepository : DataBaseRepository<MovieEntity>, DetailRepository<Movie> {
    fun getUpComingMovies(): PagingSource<Int, UpComingMovieEntity>
    fun getNowPlayingMovies(): PagingSource<Int, NowPlayingMovieEntity>
    suspend fun getPopularMovies(): List<Movie>
    suspend fun getNextWeekReleaseMovies(): List<Movie>
    override fun getData(id: Int): Flow<Movie>
    fun getMovieSeries(collectionId: Int): Flow<Series>
    fun getMovieSeriesImageList(collectionId: Int): Flow<ImageList>
    fun getMovieWatchProviders(movieId: Int): Flow<MovieWatchProvider>
    fun getSimilarMoviePagingSource(id: Int, language: String, region: String): Flow<PagingData<SimilarMedia>>
    fun getMovieReviews(movieId: Int, language: String, region: String): Flow<PagingData<Review>>
}