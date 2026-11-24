package com.cheeke.surfy.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.data.repository.MovieRepository
import com.cheeke.surfy.data.repository.TvRepository
import com.cheeke.surfy.data.repository.UserDataRepository
import com.cheeke.surfy.data.util.DataManager
import com.cheeke.surfy.data.util.SyncManager
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.ui.image.imageUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
    dataManager: DataManager,
    syncManager: SyncManager,
    movieRepository: MovieRepository,
    tvRepository: TvRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    val disposable = CompositeDisposable()

    init {
        userDataRepository.getFirstInstall()
            .subscribe(
                {
                    if (!it) {
                        syncManager.requestSync()
                        syncManager.syncMain()
                        userDataRepository.updateFirstInstall(value = true)
                    }
                },
                { Log.d(it.message.toString()) }
            ).addTo(disposable)
        userDataRepository.getMainDate()
            .subscribe(
                {
                    if (it.isEmpty()) {
                        syncManager.requestSync()
                    } else {
                        if (LocalDate.parse(it).plusDays(1) < LocalDate.now()) {
                            syncManager.requestSync()
                        }
                    }
                },
                { Log.d(it.message.toString()) }
            ).addTo(disposable)
        Single.zip(
            movieRepository.getNextWeekReleaseMovies(),
            tvRepository.getNextWeekReleaseTvs()
        ) { movies, tvs ->
            movies to tvs
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            {
                viewModelScope.launch {
                    _nextWeekReleaseMedias.onNext((it.first + it.second).sortedBy { media -> media.releaseDate })
                }
            },
            { Log.d(it.message.toString()) }
        ).addTo(disposable)
    }

    private val _nextWeekReleaseMedias = BehaviorProcessor.createDefault(emptyList<Media>())
    val nextWeekReleaseMedias = _nextWeekReleaseMedias.hide()
    private val dismissedThisSession = BehaviorProcessor.createDefault(false)
    private val hiddenToday = userDataRepository.getShowNextReleaseMoviesDate()
        .map { stored ->
            stored == LocalDate.now().toString()
        }
    val shouldShowNextWeekReleaseDialog =
        Flowable.combineLatest(
            _nextWeekReleaseMedias,
            hiddenToday,
            dismissedThisSession
        ) { snapshot, hidden, dismissed ->
            !snapshot.isNullOrEmpty() && !hidden && !dismissed
        }

    val surfyAppData = dataManager.surfyAppData
        .doOnEach {
            val url = it.value?.getMovieAppData()?.getImageUrl().orEmpty()

            if (url.startsWith(prefix = "http://") || url.startsWith(prefix = "https://")) {
                imageUrl = url
            }
        }

    fun dismissNextWeekReleaseDialog() {
        dismissedThisSession.onNext(true)
    }

    fun dontShowNextWeekReleaseDialogToday() {
        dismissedThisSession.onNext(true)
        viewModelScope.launch {
            userDataRepository.updateShowNextReleaseMoviesDate(value = LocalDate.now().toString())
        }
    }

    override fun onCleared() {
        super.onCleared()

        disposable.clear()
    }
}