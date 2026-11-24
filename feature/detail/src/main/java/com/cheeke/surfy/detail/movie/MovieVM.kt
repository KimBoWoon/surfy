package com.cheeke.surfy.detail.movie

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
import com.cheeke.surfy.data.repository.MovieRepository
import com.cheeke.surfy.data.repository.UserDataRepository
import com.cheeke.surfy.domain.GetMovieDetailUseCase
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.network.model.SurfyNetworkException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.processors.BehaviorProcessor
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

    private val disposable = CompositeDisposable()
    private val reload = BehaviorProcessor.createDefault<Unit>(Unit)
    private val detail = reload
        .switchMap {
            val start = System.nanoTime()
            trace("GetMovieDetail") {
                getMovieDetail(id)
                    .map<Result<Movie>> { Result.Success(data = it) }
                    .startWithItem(Result.Loading)
                    .onErrorReturn { Result.Error(throwable = it) }
                    .doOnSubscribe {
                        val elapsed = System.nanoTime() - start
                        Log.i(TAG, "GetMovieDetail: ${elapsed / 1_000_000.0} ms")
                    }
            }
        }
    val movie = Flowable.combineLatest(
        detail,
        userDataRepository.internalData
    ) { result, internalData ->
        when (result) {
            is Result.Loading -> MovieState.Loading
            is Result.Success -> {
                analyticsHelper.logSelectContent(contentType = "movie", media = result.data)
                MovieState.Success(movie = result.data, isAutoPlayTrailer = internalData.isAutoPlayTrailer)
            }
            is Result.Error -> MovieState.Error(result.throwable as SurfyNetworkException)
        }
    }.replay(1)
        .refCount()
    @OptIn(ExperimentalCoroutinesApi::class)
    val similarMovies = userDataRepository.internalData
        .map { it.language to it.region }
        .flatMap {
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
            ).flowable
        }.cachedIn(scope = viewModelScope)
    val isCheatActive = userDataRepository.internalData
        .map { it.isCheatActive }
    @OptIn(ExperimentalCoroutinesApi::class)
    val movieReviews = userDataRepository.internalData
        .map { it.language to it.region }
        .flatMap {
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
            ).flowable
        }.cachedIn(scope = viewModelScope)

    init {
        reload.onNext(Unit)
    }

    fun restart() {
        reload.onNext(Unit)
    }

    fun insertMovie(movie: Movie) {
        movieRepository.insert(media = movie).subscribe().addTo(disposable)
    }

    fun deleteMovie(movie: Movie) {
        movieRepository.delete(media = movie).subscribe().addTo(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}

sealed interface MovieState {
    data object Loading : MovieState
    data class Success(val movie: Movie, val isAutoPlayTrailer: Boolean) : MovieState
    data class Error(val throwable: SurfyNetworkException) : MovieState
}