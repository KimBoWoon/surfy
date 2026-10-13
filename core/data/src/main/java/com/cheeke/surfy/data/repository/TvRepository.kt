package com.cheeke.surfy.data.repository

import androidx.paging.PagingData
import com.cheeke.surfy.database.model.TvEntity
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import kotlinx.coroutines.flow.Flow

interface TvRepository  : DataBaseRepository<TvEntity>, DetailRepository<Tv> {
    suspend fun getNextWeekReleaseTvs(): List<Tv>
    override fun getData(id: Int): Flow<Tv>
    fun getTvSeasons(seriesId: Int, seasonNumber: Int): Flow<TvSeasons>
    fun getTvEpisode(seriesId: Int, seasonNumber: Int, episodeNumber: Int): Flow<TvEpisode>
    fun getSimilarTvPagingSource(id: Int, language: String, region: String): Flow<PagingData<SimilarMedia>>
    fun getTvReviews(seriesId: Int, language: String, region: String): Flow<PagingData<Review>>
}