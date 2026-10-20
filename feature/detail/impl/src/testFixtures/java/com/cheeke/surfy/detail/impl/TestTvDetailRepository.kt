package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.annotations.VisibleForTesting

class TestTvDetailRepository : TvRepository {
    private val tv = MutableSharedFlow<Tv>(replay = 1, onBufferOverflow = DROP_OLDEST)
    private val tvSeasons = MutableSharedFlow<TvSeasons>(replay = 1, onBufferOverflow = DROP_OLDEST)
    private val tvEpisode = MutableSharedFlow<TvEpisode>(replay = 1, onBufferOverflow = DROP_OLDEST)

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
        this@TestTvDetailRepository.tv.tryEmit(value = tv)
    }

    @VisibleForTesting
    fun setTvSeason(tvSeasons: TvSeasons) {
        this@TestTvDetailRepository.tvSeasons.tryEmit(value = tvSeasons)
    }

    @VisibleForTesting
    fun setTvEpisode(tvEpisode: TvEpisode) {
        this@TestTvDetailRepository.tvEpisode.tryEmit(value = tvEpisode)
    }
}