package com.cheeke.surfy.home.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.cheeke.surfy.common.Result
import com.cheeke.surfy.common.asResult
import com.cheeke.surfy.datamanager.api.DataManager
import com.cheeke.surfy.datamanager.api.SurfyAppDataState
import com.cheeke.surfy.detail.api.movie.MovieRepository
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.network.api.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeVM @Inject constructor(
    dataManager: DataManager,
    movieDataBaseRepository: MovieRepository,
    networkMonitor: NetworkMonitor,
    private val homeRepository: HomeRepository
) : ViewModel() {
    companion object {
        private const val TAG = "HomeVM"
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 5
    }

    private val pagingConfig = PagingConfig(
        pageSize = PAGE_SIZE,
        prefetchDistance = PREFETCH_DISTANCE
    )
    private val _trendingMovieTimeWindow = MutableStateFlow(value = TimeWindow.DAY)
    val trendingMovieTimeWindow = _trendingMovieTimeWindow.asStateFlow()
    private val _trendingPeopleTimeWindow = MutableStateFlow(value = TimeWindow.DAY)
    val trendingPeopleTimeWindow = _trendingPeopleTimeWindow.asStateFlow()
    private val _trendingTvTimeWindow = MutableStateFlow(value = TimeWindow.DAY)
    val trendingTvTimeWindow = _trendingTvTimeWindow.asStateFlow()
    private val onlineState = networkMonitor.isOnline
        .distinctUntilChanged()
        .filter { it }
    private val localeState = dataManager.surfyAppData
        .filterIsInstance<SurfyAppDataState.Success>()
        .map { surfyAppData -> surfyAppData.data.selectedLanguageAndRegion }
        .distinctUntilChanged()
    val nowPlayingMoviePaging = movieDataBaseRepository.getNowPlayingMovies().cachedIn(scope = viewModelScope)
    val upComingMoviePaging = movieDataBaseRepository.getUpComingMovies().cachedIn(scope = viewModelScope)
    val trendingMoviePaging =
        createTrendingPaging(
            timeWindowFlow = trendingMovieTimeWindow,
            pagingSourceFactory = { timeWindow, language ->
                homeRepository.getTrending(trending = HomeRepository.Trending.MOVIE, timeWindow = timeWindow, language = language)
            }
        )
    val trendingPeoplePaging =
        createTrendingPaging(
            timeWindowFlow = trendingPeopleTimeWindow,
            pagingSourceFactory = { timeWindow, language ->
                homeRepository.getTrending(trending = HomeRepository.Trending.PEOPLE, timeWindow = timeWindow, language = language)
            }
        )
    val trendingTvPaging =
        createTrendingPaging(
            timeWindowFlow = trendingTvTimeWindow,
            pagingSourceFactory = { timeWindow, language ->
                homeRepository.getTrending(trending = HomeRepository.Trending.TV, timeWindow = timeWindow, language = language)
            }
        )
    val homeUiState = movieDataBaseRepository.getPopularMovies()
        .map { popularMovies -> HomeUiState(popularMovies = popularMovies) }
        .asResult()
        .map { result ->
            when (result) {
                is Result.Loading -> HomeState.Loading
                is Result.Success -> HomeState.Success(homeUiState = result.data)
                is Result.Error -> HomeState.Error(result.throwable)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = HomeState.Loading
        )

    fun updateTrendingMovieTimeWindow(timeWindow: TimeWindow) {
        when (timeWindow) {
            TimeWindow.DAY -> _trendingMovieTimeWindow.value = TimeWindow.DAY
            TimeWindow.WEEK -> _trendingMovieTimeWindow.value = TimeWindow.WEEK
        }
    }

    fun updateTrendingPeopleTimeWindow(timeWindow: TimeWindow) {
        when (timeWindow) {
            TimeWindow.DAY -> _trendingPeopleTimeWindow.value = TimeWindow.DAY
            TimeWindow.WEEK -> _trendingPeopleTimeWindow.value = TimeWindow.WEEK
        }
    }

    fun updateTrendingTvTimeWindow(timeWindow: TimeWindow) {
        when (timeWindow) {
            TimeWindow.DAY -> _trendingTvTimeWindow.value = TimeWindow.DAY
            TimeWindow.WEEK -> _trendingTvTimeWindow.value = TimeWindow.WEEK
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T : Any> createTrendingPaging(
        timeWindowFlow: StateFlow<TimeWindow>,
        pagingSourceFactory: (timeWindow: String, language: String) -> PagingSource<Int, T>
    ): Flow<PagingData<T>> {
        return combine(
            timeWindowFlow,
            localeState,
            onlineState
        ) { timeWindow, language, _ ->
            TrendingRequest(timeWindow = timeWindow.label, language = language)
        }.distinctUntilChanged()
            .flatMapLatest { request ->
                Pager(
                    config = pagingConfig,
                    pagingSourceFactory = {
                        pagingSourceFactory(request.timeWindow, request.language)
                    }
                ).flow
            }.cachedIn(viewModelScope)
    }
}

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val homeUiState: HomeUiState) : HomeState
    data class Error(val throwable: Throwable) : HomeState
}

data class HomeUiState(
    val popularMovies: List<Movie> = emptyList(),
    val isShowNextWeekReleaseMovieDialog: Boolean = false,
    val nextWeekReleaseMovies: List<Media> = emptyList(),
)

enum class TimeWindow(val label: String) {
    DAY(label = "day"), WEEK(label = "week")
}

private data class TrendingRequest(
    val timeWindow: String,
    val language: String
)