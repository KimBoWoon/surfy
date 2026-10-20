package com.cheeke.surfy.detail.api.series

import androidx.paging.PagingData
import com.cheeke.surfy.detail.api.Repository
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Series
import kotlinx.coroutines.flow.Flow

interface SeriesRepository : Repository<Series> {
    override fun getData(id: Int): Flow<Series>
    override fun getFavorite(): Flow<PagingData<Series>> { throw UnsupportedOperationException("not implemented") }
    override fun isFavorite(id: Int): Flow<Boolean> { throw UnsupportedOperationException("not implemented") }
    override suspend fun insert(media: Series): Long { throw UnsupportedOperationException("not implemented") }
    override suspend fun delete(media: Series) { throw UnsupportedOperationException("not implemented") }
    override suspend fun upsert(medias: List<Series>) { throw UnsupportedOperationException("not implemented") }
    fun getMovieSeriesImageList(collectionId: Int): Flow<ImageList>
}