package com.cheeke.surfy.data.repository

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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import jakarta.inject.Inject
import java.time.Instant

interface TvRepository : DataBaseRepository<TvEntity>, DetailRepository<Tv> {
    override fun getData(id: Int): Single<Tv>
    fun getTvSeasons(seriesId: Int, seasonNumber: Int): Single<TvSeasons>
    fun getTvEpisode(seriesId: Int, seasonNumber: Int, episodeNumber: Int): Single<TvEpisode>
    fun getNextWeekReleaseTvs(): Single<List<Tv>>
    fun getSimilarTvPagingSource(id: Int, language: String, region: String): PagingSource<Int, SimilarMedia>
    fun getTvReviews(seriesId: Int, language: String, region: String): PagingSource<Int, Review>
}

class TvRepositoryImpl @Inject constructor(
    private val tvDao: TvDao,
    private val requestOptionsProvider: DetailRequestOptionsProvider,
    private val tvApis: TvRemoteDataSource
) : TvRepository {
    override fun getData(id: Int): Single<Tv> = requestOptionsProvider.current()
        .flatMap {
            tvApis.getTv(
                id = id,
                language = it.languageTag,
                includeImageLanguage = it.includeImageLanguage
            )
        }

    override fun getTvSeasons(
        seriesId: Int,
        seasonNumber: Int
    ): Single<TvSeasons> = requestOptionsProvider.current()
        .flatMap {
            tvApis.getTvSeasons(
                seriesId = seriesId,
                seasonNumber = seasonNumber,
                language = it.languageTag
            )
        }

    override fun getTvEpisode(
        seriesId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ): Single<TvEpisode> = requestOptionsProvider.current()
        .flatMap {
            tvApis.getTvEpisode(
                seriesId = seriesId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                language = it.languageTag
            )
        }

    override fun isFavorite(id: Int): Observable<Boolean> = tvDao.isFavoriteTv(id = id)

    override fun insert(media: Media): Single<Long> =
        tvDao.insertOrIgnoreTvs(
            tv = TvEntity(
                id = media.id ?: -1,
                posterPath = media.posterPath ?: "",
                name = media.title ?: "",
                firstAirDate = media.firstAirDate ?: "",
                lastAirDate = media.lastAirDate,
                timestamp = Instant.now().toEpochMilli()
            )
        ).subscribeOn(Schedulers.io())

    override fun delete(media: Media): Completable {
        return media.id?.let { id ->
            tvDao.deleteTv(id = id).subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun upsert(medias: List<Media>): Completable =
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
        ).subscribeOn(Schedulers.io())

    override fun getNextWeekReleaseTvs(): Single<List<Tv>> =
        tvDao.getNextWeekReleaseTvs().map { tvEntity ->
            tvEntity.map(transform = TvEntity::asExternalModel)
        }.subscribeOn(Schedulers.io())

    override fun getFavorite(): PagingSource<Int, TvEntity> =
        tvDao.getFavoriteTv()

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