package com.cheeke.surfy.detail.api.tv

import androidx.paging.PagingSource
import com.cheeke.surfy.detail.api.Repository
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import kotlinx.coroutines.flow.Flow

interface TvRepository : Repository<Tv> {
    override fun getData(id: Int): Flow<Tv>
    fun getTvSeasons(seriesId: Int, seasonNumber: Int): Flow<TvSeasons>
    fun getTvEpisode(seriesId: Int, seasonNumber: Int, episodeNumber: Int): Flow<TvEpisode>
    suspend fun getNextWeekReleaseTvs(): List<Tv>
    fun getSimilarTvPagingSource(
        id: Int,
        language: String,
        region: String
    ): PagingSource<Int, SimilarMedia>

    fun getTvReviews(seriesId: Int, language: String, region: String): PagingSource<Int, Review>
}