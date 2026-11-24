package com.cheeke.surfy.domain

import com.cheeke.surfy.data.repository.TvRepository
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeason
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class GetTvDetailUseCase @Inject constructor(
    private val tvRepository: TvRepository
) {
    // 시즌 이름 -> 에피소드 스트림. replay+autoConnect로 "한 번 구독되면 끝까지 캐시" 형태의 메모이제이션.
    private val seasonEpisodesCache = ConcurrentHashMap<String, Flowable<List<TvEpisode>>>()

    operator fun invoke(
        tvId: Int,
        selectedSeason: Flowable<SeasonSelection> = Flowable.just(SeasonSelection.None)
    ): Flowable<TvScreenData> {
        val detail =
            tvRepository
                .getData(tvId)
                .toFlowable()
                .replay(1)
                .refCount()

        val season =
            selectedSeason
                .switchMap { selection -> resolveInitialSelection(selection = selection, detail = detail) }
                .ofType(SeasonSelection.Selected::class.java)
                .distinctUntilChanged()
                .switchMap { selected -> fetchSeasonEvents(tvId = tvId, season = selected.season) }
                .replay(1)
                .refCount()

        val episodesBySeason =
            season
                .ofType(SeasonEvent.Success::class.java)
                .scan(emptyMap<String, List<TvEpisode>>()) { cache, event ->
                    cache + (event.seasonName to event.episodes)
                }
                .replay(1)
                .refCount()

        val loadState =
            season
                .map { event -> toLoadState(event = event) }
                .startWithItem(TvSeasonLoadState.Idle)

        return Flowable.combineLatest(
            detail,
            tvRepository.isFavorite(tvId).toFlowable(BackpressureStrategy.LATEST),
            episodesBySeason,
            loadState
        ) { tv, favorite, cache, state ->
            TvScreenData(
                tv = tv.copy(isFavorite = favorite),
                episodesBySeason = cache,
                seasonLoadState = state
            )
        }
    }

    // "선택 안 함" 상태를 detail 스트림에서 첫 시즌으로 치환.
    private fun resolveInitialSelection(
        selection: SeasonSelection,
        detail: Flowable<Tv>
    ): Flowable<SeasonSelection> =
        when (selection) {
            SeasonSelection.None ->
                detail
                    .take(1)
                    .map { tv ->
                        tv.seasons
                            ?.minByOrNull { season -> season.seasonNumber ?: Int.MAX_VALUE }
                            ?.let { firstSeason -> SeasonSelection.Selected(season = firstSeason) }
                            ?: SeasonSelection.None
                    }

            is SeasonSelection.Selected -> Flowable.just(selection)
        }

    // 시즌별 에피소드 조회. 같은 시즌은 캐시된 Flowable을 재사용(재구독 시 네트워크 재호출 없음).
    private fun fetchSeasonEvents(tvId: Int, season: TvSeason): Flowable<SeasonEvent> {
        val seasonName = season.name.orEmpty()

        val episodes = seasonEpisodesCache.getOrPut(seasonName) {
            tvRepository
                .getTvSeasons(tvId, season.seasonNumber ?: -1)
                .map { seasonDetail -> seasonDetail.episodes.orEmpty() }
                .toFlowable()
                .replay(1)
                .autoConnect()
        }

        return episodes
            .map<SeasonEvent> { episodeList -> SeasonEvent.Success(seasonName = seasonName, episodes = episodeList) }
            .startWithItem(SeasonEvent.Loading(seasonName = seasonName))
            .onErrorReturn { SeasonEvent.Error(seasonName = seasonName) }
    }

    private fun toLoadState(event: SeasonEvent): TvSeasonLoadState =
        when (event) {
            is SeasonEvent.Loading -> TvSeasonLoadState.Loading(message = event.seasonName)
            is SeasonEvent.Success -> TvSeasonLoadState.Idle
            is SeasonEvent.Error -> TvSeasonLoadState.Error(message = event.seasonName)
        }
}

private sealed interface SeasonEvent {
    data class Loading(
        val seasonName: String
    ) : SeasonEvent

    data class Success(
        val seasonName: String,
        val episodes: List<TvEpisode>
    ) : SeasonEvent

    data class Error(
        val seasonName: String
    ) : SeasonEvent
}

sealed interface SeasonSelection {
    data object None : SeasonSelection
    data class Selected(
        val season: TvSeason
    ) : SeasonSelection
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