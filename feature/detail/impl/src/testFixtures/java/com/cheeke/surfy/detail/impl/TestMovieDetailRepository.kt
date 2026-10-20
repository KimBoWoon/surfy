package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.MovieWatchProvider
import com.cheeke.surfy.model.Series
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.annotations.VisibleForTesting

class TestMovieDetailRepository : MovieDetailRepository {
    private val movie = MutableSharedFlow<Movie>(replay = 1, onBufferOverflow = DROP_OLDEST)
    private val movieSeries = MutableSharedFlow<Series>(replay = 1, onBufferOverflow = DROP_OLDEST)
    private val imageList = MutableSharedFlow<ImageList>(replay = 1, onBufferOverflow = DROP_OLDEST)
    private val watchProvider = MutableSharedFlow<MovieWatchProvider>(replay = 1, onBufferOverflow = DROP_OLDEST)

    override fun getData(id: Int): Flow<Movie> = movie

    override fun getMovieSeries(collectionId: Int): Flow<Series> = movieSeries

    override fun getMovieSeriesImageList(collectionId: Int): Flow<ImageList> = imageList

    override fun getMovieWatchProviders(movieId: Int): Flow<MovieWatchProvider> = watchProvider

    @VisibleForTesting
    fun setMovie(detail: Movie) {
        movie.tryEmit(value = detail)
    }

    @VisibleForTesting
    fun setMovieSeries(movieSeries: Series) {
        this@TestMovieDetailRepository.movieSeries.tryEmit(value = movieSeries)
    }

    @VisibleForTesting
    fun setImageList(imageList: ImageList) {
        this@TestMovieDetailRepository.imageList.tryEmit(value = imageList)
    }
}