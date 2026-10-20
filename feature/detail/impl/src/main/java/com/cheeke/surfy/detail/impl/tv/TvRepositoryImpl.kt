package com.cheeke.surfy.detail.impl.tv

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import com.cheeke.surfy.database.impl.dao.TvDao
import com.cheeke.surfy.database.impl.model.TvEntity
import com.cheeke.surfy.database.impl.model.asExternalModel
import com.cheeke.surfy.detail.api.tv.TvRepository
import com.cheeke.surfy.detail.impl.DetailRequestOptionsProvider
import com.cheeke.surfy.detail.impl.paging.SimilarTvPagingSource
import com.cheeke.surfy.detail.impl.paging.TvReviewPagingSource
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.network.api.TvRemoteDataSource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class TvRepositoryImpl @Inject constructor(
    private val tvDao: TvDao,
    private val tvApis: TvRemoteDataSource,
    private val requestOptionsProvider: DetailRequestOptionsProvider
) : TvRepository {
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

    override suspend fun getNextWeekReleaseTvs(): List<Tv> =
        tvDao.getNextWeekReleaseTvs().map { it.asExternalModel() }

    override fun getFavorite(): Flow<PagingData<Tv>> =
        Pager(config = PagingConfig(pageSize = 20)) {
            tvDao.getFavoriteTv()
        }.flow.map { pagingData -> pagingData.map(transform = TvEntity::asExternalModel) }

    override fun isFavorite(id: Int): Flow<Boolean> =
        tvDao.isFavoriteTv(id = id)

    override suspend fun insert(media: Tv): Long =
        tvDao.insertOrIgnoreTvs(
            tv = TvEntity(
                id = media.id ?: -1,
                name = media.title,
                posterPath = media.posterPath ?: "",
                timestamp = Instant.now().toEpochMilli(),
                firstAirDate = media.firstAirDate,
                lastAirDate = media.lastAirDate
            )
        )

    override suspend fun delete(media: Tv) {
        media.id?.let { id ->
            tvDao.deleteTv(id = id)
        }
    }

    override suspend fun upsert(medias: List<Tv>) {
        tvDao.upsertTvs(entities = medias.map(transform = Tv::asExternalModel))
    }

    override fun getSimilarTvPagingSource(
        id: Int,
        language: String,
        region: String
    ): PagingSource<Int, SimilarMedia> = SimilarTvPagingSource(
        apis = tvApis,
        id = id,
        language = language,
        region = region
    )

    override fun getTvReviews(
        seriesId: Int,
        language: String,
        region: String
    ): PagingSource<Int, Review> = TvReviewPagingSource(
        apis = tvApis,
        id = seriesId,
        language = language,
        region = region
    )
}