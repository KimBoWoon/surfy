package com.cheeke.surfy.testing.repository

import com.cheeke.surfy.data.repository.SeriesRepository
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Series
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.annotations.VisibleForTesting

class TestSeriesRepository : SeriesRepository {
    private val movieSeries = MutableSharedFlow<Series>(replay = 1, onBufferOverflow = DROP_OLDEST)
    private val imageList = MutableSharedFlow<ImageList>(replay = 1, onBufferOverflow = DROP_OLDEST)

    override fun getData(id: Int): Flow<Series> = movieSeries

    override fun getMovieSeriesImageList(collectionId: Int): Flow<ImageList> = imageList

    @VisibleForTesting
    fun setMovieSeries(movieSeries: Series) {
        this@TestSeriesRepository.movieSeries.tryEmit(value = movieSeries)
    }

    @VisibleForTesting
    fun setImageList(imageList: ImageList) {
        this@TestSeriesRepository.imageList.tryEmit(value = imageList)
    }
}