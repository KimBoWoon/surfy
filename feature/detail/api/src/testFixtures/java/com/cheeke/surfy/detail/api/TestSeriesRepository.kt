package com.cheeke.surfy.detail.api

import com.cheeke.surfy.detail.api.series.SeriesRepository
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Series
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.annotations.VisibleForTesting

class TestSeriesRepository : SeriesRepository {
    private val movieSeries =
        MutableSharedFlow<Series>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val imageList =
        MutableSharedFlow<ImageList>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

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