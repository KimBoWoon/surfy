package com.cheeke.surfy.detail.impl.tv

import com.cheeke.surfy.common.Result
import com.cheeke.surfy.common.asResult
import com.cheeke.surfy.detail.api.tv.TvRepository
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeason
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class GetTvDetailUseCase @Inject constructor(
    private val repository: TvRepository
) {
    private val episodesCache = MutableStateFlow<Map<String, List<TvEpisode>>>(value = emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        id: Int,
        selectedSeason: Flow<TvSeason?> = flowOf(value = null)
    ): Flow<TvScreenData> {
        val seasonState = selectedSeason
            .flatMapLatest { season ->
                if (season == null) {
                    flowOf(value = TvSeasonLoadState.Idle)
                } else {
                    val cached = episodesCache.value[season.name]
                    if (cached != null) {
                        flowOf(value = TvSeasonLoadState.Idle)
                    } else {
                        getTvSeason(id = id, season = season)
                    }
                }
            }

        return combine(
            repository.getData(id = id),
            repository.isFavorite(id = id),
            episodesCache,
            seasonState
        ) { tv, isFavorite, episodesBySeason, currentSeasonState ->
            TvScreenData(
                tv = tv.copy(isFavorite = isFavorite),
                episodesBySeason = episodesBySeason,
                seasonLoadState = currentSeasonState
            )
        }
    }

    private fun getTvSeason(id: Int, season: TvSeason): Flow<TvSeasonLoadState> =
        repository.getTvSeasons(seriesId = id, seasonNumber = season.seasonNumber ?: -1)
            .asResult()
            .map { result ->
                when (result) {
                    is Result.Loading -> TvSeasonLoadState.Loading(message = season.name)
                    is Result.Success -> {
                        episodesCache.update {
                            it + ((result.data.name.orEmpty()) to (result.data.episodes.orEmpty()))
                        }
                        TvSeasonLoadState.Idle
                    }
                    is Result.Error -> TvSeasonLoadState.Error(message = season.name)
                }
            }
}

data class TvScreenData(
    val tv: Tv,
    val episodesBySeason: Map<String, List<TvEpisode>>,
    val seasonLoadState: TvSeasonLoadState
)

sealed interface TvSeasonLoadState {
    data object Idle : TvSeasonLoadState
    data class Loading(val message: String?) : TvSeasonLoadState
    data class Error(val message: String?) : TvSeasonLoadState
}