package com.cheeke.surfy.detail.api

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.testing.asPagingSourceFactory
import com.cheeke.surfy.detail.api.tv.TvRepository
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.testing.model.similarTvTestData
import com.cheeke.surfy.testing.model.testTvReviews
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.jetbrains.annotations.VisibleForTesting
import java.time.LocalDate

class TestTvRepository : TvRepository {
    val tvDatabase = MutableSharedFlow<List<Tv>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val currentTvDatabase get() = tvDatabase.replayCache.firstOrNull().orEmpty()
    private val tv = MutableSharedFlow<Tv>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val tvSeasons = MutableSharedFlow<TvSeasons>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val tvEpisode = MutableSharedFlow<TvEpisode>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun getFavorite(): Flow<PagingData<Tv>> =
        flow { emit(value = PagingData.from(data = currentTvDatabase)) }

    override fun isFavorite(id: Int): Flow<Boolean> =
        tvDatabase
            .map { tvs ->
                tvs.firstOrNull { it.id == id } != null
            }.distinctUntilChanged()

    override suspend fun insert(media: Tv): Long {
        tvDatabase.emit(value = currentTvDatabase + (media as Tv))
        return media.id?.toLong() ?: throw RuntimeException("room database insert failed...")
    }

    override suspend fun delete(media: Tv) {
        tvDatabase.emit(value = currentTvDatabase.filter { it.id != media.id })
    }

    override suspend fun upsert(medias: List<Tv>) {
        tvDatabase.emit(
            value = (currentTvDatabase + medias).map { tv ->
                medias.find { it.id == tv.id } ?: tv
            }
        )
    }

    override suspend fun getNextWeekReleaseTvs(): List<Tv> {
        val now = LocalDate.now()
        val nextWeekReleaseMovies = currentTvDatabase.filter { movie ->
            !movie.releaseDate?.trim().isNullOrEmpty() && LocalDate.parse(movie.releaseDate ?: "") in (now..now.plusDays(7))
        }
        tvDatabase.tryEmit(value = nextWeekReleaseMovies)
        return tvDatabase.first()
    }

    @VisibleForTesting
    fun setTvs(list: List<Tv>) {
        tvDatabase.tryEmit(value = list)
    }

    override fun getSimilarTvPagingSource(
        id: Int,
        language: String,
        region: String
    ): PagingSource<Int, SimilarMedia> = (similarTvTestData.results ?: emptyList()).asPagingSourceFactory().invoke()

    override fun getTvReviews(
        seriesId: Int,
        language: String,
        region: String
    ): PagingSource<Int, Review> = testTvReviews.asPagingSourceFactory().invoke()

    override fun getData(id: Int): Flow<Tv> = tv

    override fun getTvSeasons(
        seriesId: Int,
        seasonNumber: Int
    ): Flow<TvSeasons> = tvSeasons

    override fun getTvEpisode(
        seriesId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<TvEpisode> = tvEpisode

    @VisibleForTesting
    fun setTv(tv: Tv) {
        this@TestTvRepository.tv.tryEmit(value = tv)
    }

    @VisibleForTesting
    fun setTvSeason(tvSeasons: TvSeasons) {
        this@TestTvRepository.tvSeasons.tryEmit(value = tvSeasons)
    }

    @VisibleForTesting
    fun setTvEpisode(tvEpisode: TvEpisode) {
        this@TestTvRepository.tvEpisode.tryEmit(value = tvEpisode)
    }
}