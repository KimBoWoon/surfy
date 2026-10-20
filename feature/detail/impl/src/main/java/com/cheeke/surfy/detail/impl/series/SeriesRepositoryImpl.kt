package com.cheeke.surfy.detail.impl.series

import com.cheeke.surfy.detail.api.series.SeriesRepository
import com.cheeke.surfy.detail.impl.DetailRequestOptionsProvider
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.network.api.SeriesRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SeriesRepositoryImpl @Inject constructor(
    private val apis: SeriesRemoteDataSource,
    private val requestOptionsProvider: DetailRequestOptionsProvider
) : SeriesRepository {
    override fun getData(id: Int): Flow<Series> = flow {
        val internalData = requestOptionsProvider.current()

        emit(value = apis.getMovieSeries(collectionId = id, language = internalData.languageTag))
    }

    override fun getMovieSeriesImageList(collectionId: Int): Flow<ImageList> = flow {
        val internalData = requestOptionsProvider.current()

        emit(value = apis.getSeriesImages(collectionId = collectionId, includeImageLanguage = internalData.localizedImageLanguage, language = internalData.languageTag))
    }
}