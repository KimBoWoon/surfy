package com.cheeke.surfy.detail.tv

import androidx.compose.ui.util.trace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.rxjava3.cachedIn
import androidx.paging.rxjava3.flowable
import com.cheeke.surfy.analytics.AnalyticsHelper
import com.cheeke.surfy.analytics.logSelectContent
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.Result
import com.cheeke.surfy.data.repository.TvRepository
import com.cheeke.surfy.data.repository.UserDataRepository
import com.cheeke.surfy.domain.GetTvDetailUseCase
import com.cheeke.surfy.domain.SeasonSelection
import com.cheeke.surfy.domain.TvScreenData
import com.cheeke.surfy.domain.TvSeasonLoadState
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeason
import com.cheeke.surfy.network.model.SurfyNetworkException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.processors.PublishProcessor
import kotlinx.coroutines.ExperimentalCoroutinesApi

@HiltViewModel(assistedFactory = TvVM.Factory::class)
class TvVM @AssistedInject constructor(
    @Assisted(value = "id") val id: Int,
    private val getTvDetailUseCase: GetTvDetailUseCase,
    private val tvRepository: TvRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    companion object {
        private const val TAG = "TvVM"
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted(value = "id") id: Int
        ): TvVM
    }

    private val disposables = CompositeDisposable()
    @OptIn(ExperimentalCoroutinesApi::class)
    val similarTvs = userDataRepository.internalData
        .map { it.language to it.region }
        .flatMap {
            Pager(
                config = PagingConfig(pageSize = 1, initialLoadSize = 1, prefetchDistance = 5),
                initialKey = 1,
                pagingSourceFactory = {
                    tvRepository.getSimilarTvPagingSource(
                        id = id,
                        language = it.first,
                        region = it.second
                    )
                }
            ).flowable
        }.cachedIn(scope = viewModelScope)

    private val reload = PublishProcessor.create<Unit>()
    private val _selectedEpisode = BehaviorProcessor.create<EpisodeDialog>()
    val selectedEpisode = BehaviorProcessor.create<EpisodeDialog>()
    private val _selectedSeason = BehaviorProcessor.createDefault<SeasonSelection>(SeasonSelection.None)
    val selectedSeason: Flowable<SeasonSelection> = _selectedSeason.hide()
    private val tv =
        reload
            .startWithItem(Unit)
            .switchMap {
                val start = System.nanoTime()
                trace("GetTvDetail") {
                    getTvDetailUseCase(
                        tvId = id,
                        selectedSeason = selectedSeason
                    )
                }.map<Result<TvScreenData>> { Result.Success(it) }
                    .startWithItem(Result.Loading)
                    .onErrorReturn { Result.Error(it) }
                    .doOnSubscribe {
                        val elapsed = System.nanoTime() - start
                        Log.i(TAG, "GetTvDetail: ${elapsed / 1_000_000.0} ms")
                    }
            }.replay(1)
            .refCount()
    val uiState =
        tv.switchMap { result ->
            when (result) {
                is Result.Loading -> Flowable.just(TvState.Loading)
                is Result.Success -> {
                    userDataRepository.internalData.map { internalData ->
                        analyticsHelper.logSelectContent(contentType = "tv", media = result.data.tv)

                        TvState.Success(
                            TvUiState(
                                tv = result.data.tv,
                                seasons = result.data.tv.seasons.orEmpty(),
                                episodeState = result.data.seasonLoadState,
                                episodesBySeason = result.data.episodesBySeason,
                                autoPlayTrailer = internalData.isAutoPlayTrailer
                            )
                        )
                    }
                }
                is Result.Error -> Flowable.just(TvState.Error(SurfyNetworkException(throwable = result.throwable)))
            }
        }.replay(1).refCount()
    @OptIn(ExperimentalCoroutinesApi::class)
    val tvReviews = userDataRepository.internalData
        .map { it.language to it.region }
        .flatMap {
            Pager(
                config = PagingConfig(pageSize = 1, initialLoadSize = 1, prefetchDistance = 5),
                initialKey = 1,
                pagingSourceFactory = {
                    tvRepository.getTvReviews(
                        seriesId = id,
                        language = it.first,
                        region = it.second
                    )
                }
            ).flowable
        }.cachedIn(scope = viewModelScope)

    init {
        reload.onNext(Unit)
    }

    fun onSelectSeason(season: TvSeason) {
        _selectedSeason.onNext(SeasonSelection.Selected(season))
    }

    fun restart() {
        reload.onNext(Unit)
    }

    fun insertTv(tv: Tv) {
        tvRepository.insert(media = tv).subscribe().addTo(disposables)
    }

    fun deleteTv(tv: Tv) {
        tvRepository.delete(media = tv).subscribe().addTo(disposables)
    }

    fun showEpisodeDetail(episode: TvEpisode) {
        _selectedEpisode.onNext(EpisodeDialog.Visible(episode))
    }

    fun hideEpisodeDetail() {
        _selectedEpisode.onNext(EpisodeDialog.Hidden)
    }

    override fun onCleared() {
        disposables.clear()
    }
}

sealed interface TvState {
    data object Loading : TvState
    data class Success(val tvUiState: TvUiState) : TvState
    data class Error(val throwable: SurfyNetworkException) : TvState
}

data class TvUiState(
    val tv: Tv,
    val seasons: List<TvSeason>,
    val episodeState: TvSeasonLoadState,
    val episodesBySeason: Map<String, List<TvEpisode>>,
    val autoPlayTrailer: Boolean
)

sealed interface EpisodeDialog {
    data object Hidden : EpisodeDialog
    data class Visible(val episode: TvEpisode) : EpisodeDialog
}