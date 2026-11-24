package com.cheeke.surfy.data.repository

import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.network.SeriesRemoteDataSource
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

interface SeriesRepository : DetailRepository<Series> {
    override fun getData(id: Int): Single<Series>
    fun getMovieSeriesImageList(collectionId: Int): Single<ImageList>
}

class SeriesRepositoryImpl @Inject constructor(
    private val apis: SeriesRemoteDataSource,
    private val requestOptionsProvider: DetailRequestOptionsProvider
) : SeriesRepository {
    override fun getData(id: Int): Single<Series> = requestOptionsProvider.current()
        .flatMap {
            apis.getMovieSeries(
                collectionId = id,
                language = it.languageTag
            )
        }

    override fun getMovieSeriesImageList(collectionId: Int): Single<ImageList> = requestOptionsProvider.current()
        .flatMap {
            apis.getSeriesImages(
                collectionId = collectionId,
                includeImageLanguage = it.localizedImageLanguage,
                language = it.languageTag
            )
        }
}