package com.cheeke.surfy.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheeke.surfy.data.repository.MovieRepository
import com.cheeke.surfy.data.repository.TvRepository
import com.cheeke.surfy.data.repository.UserDataRepository
import com.cheeke.surfy.data.util.DataManager
import com.cheeke.surfy.data.util.SurfyAppDataState
import com.cheeke.surfy.data.util.SyncManager
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.ui.image.imageUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
    dataManager: DataManager,
    syncManager: SyncManager,
    private val userDataRepository: UserDataRepository,
    private val movieRepository: MovieRepository,
    private val tvRepository: TvRepository
) : ViewModel() {
    init {
        viewModelScope.launch {
            supervisorScope {
                val isFirstInstall = userDataRepository.getFirstInstall()

                if (!isFirstInstall) {
                    syncManager.requestSync()
                    syncManager.syncMain()
                    userDataRepository.updateFirstInstall(value = true)
                }
            }

            supervisorScope {
                val lastUpdateMainDate = userDataRepository.getMainDate()

                if (lastUpdateMainDate.isEmpty()) {
                    syncManager.requestSync()
                } else {
                    if (LocalDate.parse(lastUpdateMainDate).plusDays(1) < LocalDate.now()) {
                        syncManager.requestSync()
                    }
                }
            }

            supervisorScope {
                _nextWeekReleaseMedias.emit(
                    value = (movieRepository.getNextWeekReleaseMovies() + tvRepository.getNextWeekReleaseTvs())
                        .sortedBy { it.releaseDate }
                )
            }
        }
    }

    private val _nextWeekReleaseMedias: MutableStateFlow<List<Media>> = MutableStateFlow(value = emptyList())
    val nextWeekReleaseMedias = _nextWeekReleaseMedias.asStateFlow()
    private val dismissedThisSession: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
    private val hiddenToday: StateFlow<Boolean> = flow {
        emit(value = userDataRepository.getShowNextReleaseMoviesDate())
    }.map { stored ->
        stored == LocalDate.now().toString()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )
    val shouldShowNextWeekReleaseDialog: StateFlow<Boolean> =
        combine(
            flow = _nextWeekReleaseMedias,
            flow2 = hiddenToday,
            flow3 = dismissedThisSession
        ) { snapshot: List<Media>?, hidden: Boolean, dismissed: Boolean ->
            !snapshot.isNullOrEmpty() && !hidden && !dismissed
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    val surfyAppData = dataManager.surfyAppData
        .onEach {
            val url = it.getMovieAppData().getImageUrl()

            if (url.startsWith(prefix = "http://") || url.startsWith(prefix = "https://")) {
                imageUrl = url
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SurfyAppDataState.Loading
        )

    fun dismissNextWeekReleaseDialog() {
        dismissedThisSession.value = true
    }

    fun dontShowNextWeekReleaseDialogToday() {
        dismissedThisSession.value = true
        viewModelScope.launch {
            userDataRepository.updateShowNextReleaseMoviesDate(value = LocalDate.now().toString())
        }
    }
}