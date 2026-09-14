package com.cheeke.surfy.detail.impl.tv

import androidx.compose.ui.util.trace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.cheeke.surfy.analytics.api.AnalyticsHelper
import com.cheeke.surfy.analytics.api.logSelectContent
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.Result
import com.cheeke.surfy.common.asResult
import com.cheeke.surfy.detail.api.tv.TvRepository
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeason
import com.cheeke.surfy.network.api.SurfyNetworkException
import com.cheeke.surfy.network.api.toSurfyNetworkException
import com.cheeke.surfy.userdata.api.UserDataRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TvVM.Factory::class)
class TvVM @AssistedInject constructor(
    @Assisted(value = "id") val id: Int,
    userDataRepository: UserDataRepository,
    private val getTvDetailUseCase: GetTvDetailUseCase,
    private val tvRepository: TvRepository,
    private val analyticsHelper: AnalyticsHelper
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

    private val reload = MutableSharedFlow<Unit>(replay = 1)
    @OptIn(ExperimentalCoroutinesApi::class)
    val similarTvs = userDataRepository.internalData
        .map { it.language to it.region }
        .flatMapLatest {
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
            ).flow
        }.cachedIn(scope = viewModelScope)
    private val _selectedEpisode = MutableStateFlow<TvEpisode?>(value = null)
    val selectedEpisode = _selectedEpisode.asStateFlow()
    private val selectedSeason = MutableStateFlow<TvSeason?>(value = null)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val tv = reload.flatMapLatest {
        trace(sectionName = "GetTvDetail") {
            val start = System.nanoTime()
            try {
                getTvDetailUseCase(id = id, selectedSeason = selectedSeason).asResult()
            } finally {
                val elapsed = System.nanoTime() - start
                Log.i(TAG, "GetTvDetail: ${elapsed / 1_000_000.0} ms")
            }
        }
    }
    val uiState = combine(
        tv,
        selectedSeason,
        userDataRepository.internalData
    ) { result, selectedSeason, internalData ->
        when (result) {
            is Result.Loading -> TvState.Loading
            is Result.Success -> {
                analyticsHelper.logSelectContent(contentType = "tv", id = id, title = result.data.tv.title.orEmpty())
                val tv = result.data.tv
                val seasons = tv.seasons
                val initialSeason = selectedSeason ?: seasons?.sortedBy { it.seasonNumber }?.firstOrNull()

                if (selectedSeason == null && initialSeason != null) {
                    this@TvVM.selectedSeason.value = initialSeason
                }

                TvState.Success(
                    tvUiState = TvUiState(
                        tv = result.data.tv,
                        seasons = seasons.orEmpty(),
                        episodeState = result.data.seasonLoadState,
                        episodesBySeason = result.data.episodesBySeason,
                        autoPlayTrailer = internalData.isAutoPlayTrailer
                    )
                )
            }
            is Result.Error -> TvState.Error(throwable = result.throwable.toSurfyNetworkException())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = TvState.Loading
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    val tvReviews = userDataRepository.internalData
        .map { it.language to it.region }
        .flatMapLatest {
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
            ).flow
        }.cachedIn(scope = viewModelScope)

    init {
        viewModelScope.launch {
            reload.emit(value = Unit)
        }
    }

    fun onSelectSeason(season: TvSeason) {
        viewModelScope.launch {
            selectedSeason.emit(value = season)
        }
    }

    fun restart() {
        viewModelScope.launch {
            reload.emit(value = Unit)
        }
    }

    fun insertTv(tv: Tv) {
        viewModelScope.launch {
            tvRepository.insert(media = tv)
        }
    }

    fun deleteTv(tv: Tv) {
        viewModelScope.launch {
            tvRepository.delete(media = tv)
        }
    }

    fun showEpisodeDetail(episode: TvEpisode) {
        viewModelScope.launch {
            _selectedEpisode.emit(value = episode)
        }
    }

    fun hideEpisodeDetail() {
        viewModelScope.launch {
            _selectedEpisode.emit(value = null)
        }
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