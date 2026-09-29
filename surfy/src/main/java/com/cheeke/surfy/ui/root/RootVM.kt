package com.cheeke.surfy.ui.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheeke.surfy.datamanager.api.DataManager
import com.cheeke.surfy.deeplink.DeepLinkManager
import com.cheeke.surfy.detail.api.movie.MovieRepository
import com.cheeke.surfy.detail.api.tv.TvRepository
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.network.api.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class RootVM @Inject constructor(
    private val deepLinkManager: DeepLinkManager,
    private val movieDataBaseRepository: MovieRepository,
    private val tvDataBaseRepository: TvRepository,
    private val networkMonitor: NetworkMonitor,
    val surfyDataManager: DataManager
) : ViewModel() {
    private val _nextWeekReleaseMedias: MutableStateFlow<List<Media>> = MutableStateFlow(value = emptyList())
    val nextWeekReleaseMedias = _nextWeekReleaseMedias.asStateFlow()
    val isOffline = networkMonitor.isOnline
        .map(transform = Boolean::not)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = false
        )
    val bottomDeeplink = deepLinkManager.bottomDeeplink

    init {
        viewModelScope.launch {
            supervisorScope {
                _nextWeekReleaseMedias.emit(
                    value = (movieDataBaseRepository.getNextWeekReleaseMovies() + tvDataBaseRepository.getNextWeekReleaseTvs())
                        .sortedBy { it.releaseDate }
                )
            }
        }
    }

    fun consumeBottomDeepLink() {
        deepLinkManager.consumeBottomDeepLink()
    }
}