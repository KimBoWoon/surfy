package com.cheeke.surfy.detail.impl.movie

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
import com.cheeke.surfy.detail.api.movie.MovieRepository
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.network.api.SurfyNetworkException
import com.cheeke.surfy.network.api.toSurfyNetworkException
import com.cheeke.surfy.userdata.api.UserDataRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MovieVM.Factory::class)
class MovieVM @AssistedInject constructor(
    @Assisted(value = "id") val id: Int,
    userDataRepository: UserDataRepository,
    private val getMovieDetail: GetMovieDetailUseCase,
    private val movieRepository: MovieRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {
    companion object {
        private const val TAG = "MovieVM"
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted(value = "id") id: Int
        ): MovieVM
    }

    private val reload = MutableSharedFlow<Unit>(replay = 1)
    @OptIn(ExperimentalCoroutinesApi::class)
    val movie = combine(
        reload.flatMapLatest {
            trace(sectionName = "GetMovieDetail") {
                val start = System.nanoTime()
                try {
                    getMovieDetail(id = id)
                } finally {
                    val elapsed = System.nanoTime() - start
                    Log.i(TAG, "GetMovieDetail: ${elapsed / 1_000_000.0} ms")
                }
            }.asResult()
        },
        userDataRepository.internalData
    ) { result, internalData ->
        when (result) {
            is Result.Loading -> MovieState.Loading
            is Result.Success -> {
                analyticsHelper.logSelectContent(contentType = "movie", id = id, title = result.data.title.orEmpty())
                MovieState.Success(movie = result.data, isAutoPlayTrailer = internalData.isAutoPlayTrailer)
            }
            is Result.Error -> MovieState.Error(throwable = result.throwable.toSurfyNetworkException())
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = MovieState.Loading,
        started = SharingStarted.Lazily
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    val similarMovies = userDataRepository.internalData
        .map { it.language to it.region }
        .flatMapLatest {
            Pager(
                config = PagingConfig(pageSize = 1, initialLoadSize = 1, prefetchDistance = 5),
                initialKey = 1,
                pagingSourceFactory = {
                    movieRepository.getSimilarMoviePagingSource(
                        id = id,
                        language = it.first,
                        region = it.second
                    )
                }
            ).flow
        }.cachedIn(scope = viewModelScope)
    val isCheatActive = userDataRepository.internalData
        .map { it.isCheatActive }
        .stateIn(
            scope = viewModelScope,
            initialValue = false,
            started = SharingStarted.Lazily
        )
    @OptIn(ExperimentalCoroutinesApi::class)
    val movieReviews = userDataRepository.internalData
        .map { it.language to it.region }
        .flatMapLatest {
            Pager(
                config = PagingConfig(pageSize = 1, initialLoadSize = 1, prefetchDistance = 5),
                initialKey = 1,
                pagingSourceFactory = {
                    movieRepository.getMovieReviews(
                        movieId = id,
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

    fun restart() {
        viewModelScope.launch {
            reload.emit(value = Unit)
        }
    }

    fun insertMovie(movie: Movie) {
        viewModelScope.launch {
            movieRepository.insert(media = movie)
        }
    }

    fun deleteMovie(movie: Movie) {
        viewModelScope.launch {
            movieRepository.delete(media = movie)
        }
    }
}

sealed interface MovieState {
    data object Loading : MovieState
    data class Success(val movie: Movie, val isAutoPlayTrailer: Boolean) : MovieState
    data class Error(val throwable: SurfyNetworkException) : MovieState
}