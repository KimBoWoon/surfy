package com.cheeke.surfy.data.repository

import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Series
import kotlinx.coroutines.flow.Flow

interface SeriesRepository : DetailRepository<Series> {
    override fun getData(id: Int): Flow<Series>
    fun getMovieSeriesImageList(collectionId: Int): Flow<ImageList>
}