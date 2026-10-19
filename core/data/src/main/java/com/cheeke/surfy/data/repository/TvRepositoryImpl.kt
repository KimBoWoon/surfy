package com.cheeke.surfy.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.cheeke.surfy.data.paging.SimilarTvPagingSource
import com.cheeke.surfy.data.paging.TvReviewPagingSource
import com.cheeke.surfy.database.dao.TvDao
import com.cheeke.surfy.database.model.TvEntity
import com.cheeke.surfy.database.model.asExternalModel
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.network.TvRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

class TvRepositoryImpl @Inject constructor(
    private val tvDao: TvDao,
    private val tvApis: TvRemoteDataSource,
    private val requestOptionsProvider: DetailRequestOptionsProvider
) : TvRepository {
    private companion object {
        const val PAGE_SIZE = 20
        const val INITIAL_LOAD_SIZE = 20
        const val ENABLE_PLACE_HOLDER = false
        val config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE, enablePlaceholders = ENABLE_PLACE_HOLDER)
    }

    override fun isFavorite(id: Int): Flow<Boolean> = tvDao.isFavoriteTv(id = id)

    override suspend fun insert(media: Media): Long =
        tvDao.insertOrIgnoreTvs(
            tv = TvEntity(
                id = media.id ?: -1,
                posterPath = media.posterPath ?: "",
                name = media.title ?: "",
                firstAirDate = media.firstAirDate ?: "",
                lastAirDate = media.lastAirDate,
                timestamp = Instant.now().toEpochMilli()
            )
        )

    override suspend fun delete(media: Media) {
        media.id?.let { id ->
            tvDao.deleteTv(id = id)
        }
    }

    override suspend fun upsert(medias: List<Media>) {
        tvDao.upsertTvs(
            entities = medias.map { media ->
                TvEntity(
                    id = media.id ?: -1,
                    posterPath = media.posterPath ?: "",
                    name = media.title ?: "",
                    firstAirDate = media.firstAirDate ?: "",
                    lastAirDate = media.lastAirDate ?: "",
                    timestamp = Instant.now().toEpochMilli()
                )
            }
        )
    }

    override suspend fun getNextWeekReleaseTvs(): List<Tv> =
        tvDao.getNextWeekReleaseTvs().map { tvEntity ->
            tvEntity.asExternalModel()
        }

    override fun getFavorite(): PagingSource<Int, TvEntity> =
        tvDao.getFavoriteTv()

    override fun getData(id: Int): Flow<Tv> = flow {
        val internalData = requestOptionsProvider.current()
        val tv = tvApis.getTv(id = id, language = internalData.languageTag, includeImageLanguage = internalData.includeImageLanguage)
        emit(value = tv)
    }

    override fun getTvSeasons(
        seriesId: Int,
        seasonNumber: Int
    ): Flow<TvSeasons> = flow {
        val internalData = requestOptionsProvider.current()

        emit(value = tvApis.getTvSeasons(seriesId = seriesId, seasonNumber = seasonNumber, language = internalData.languageTag))
    }

    override fun getTvEpisode(
        seriesId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<TvEpisode> = flow {
        val internalData = requestOptionsProvider.current()

        emit(value = tvApis.getTvEpisode(seriesId = seriesId, seasonNumber = seasonNumber, episodeNumber = episodeNumber, language = internalData.languageTag))
    }

    override fun getSimilarTvPagingSource(
        id: Int,
        language: String,
        region: String
    ): Flow<PagingData<SimilarMedia>> = Pager(
        config = config,
        pagingSourceFactory = {
            SimilarTvPagingSource(
                apis = tvApis,
                id = id,
                language = language,
                region = region
            )
        }
    ).flow

    override fun getTvReviews(
        seriesId: Int,
        language: String,
        region: String
    ): Flow<PagingData<Review>> = Pager(
        config = config,
        pagingSourceFactory = {
            TvReviewPagingSource(
                apis = tvApis,
                id = seriesId,
                language = language,
                region = region
            )
        }
    ).flow
}