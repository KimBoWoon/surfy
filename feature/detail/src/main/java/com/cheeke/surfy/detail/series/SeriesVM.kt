package com.cheeke.surfy.detail.series

import androidx.compose.ui.util.trace
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheeke.surfy.analytics.AnalyticsHelper
import com.cheeke.surfy.analytics.logSelectContent
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.Result
import com.cheeke.surfy.common.asResult
import com.cheeke.surfy.domain.GetSeriesDetailUseCase
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.network.model.SurfyNetworkException
import com.cheeke.surfy.network.model.toSurfyNetworkException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SeriesVM.Factory::class)
class SeriesVM @AssistedInject constructor(
    @Assisted val id: Int,
    private val getSeriesDetailUseCase: GetSeriesDetailUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {
    companion object {
        internal const val TAG = "SeriesVM"
    }

    @AssistedFactory
    interface Factory {
        fun create(id: Int): SeriesVM
    }

    private val reload = MutableSharedFlow<Unit>(replay = 1)
    @OptIn(ExperimentalCoroutinesApi::class)
    val series = reload
        .flatMapLatest {
            trace(sectionName = "GetSeriesDetail") {
                val start = System.nanoTime()
                try {
                    getSeriesDetailUseCase(id = id)
                } finally {
                    val elapsed = System.nanoTime() - start
                    Log.i(TAG, "GetSeriesDetail: ${elapsed / 1_000_000.0} ms")
                }
            }.asResult()
        }.map { result ->
            when (result) {
                is Result.Loading -> SeriesState.Loading
                is Result.Success -> {
                    analyticsHelper.logSelectContent(contentType = "series", media = result.data.series)
                    SeriesState.Success(series = result.data.series, imageList = result.data.imageList)
                }
                is Result.Error -> SeriesState.Error(throwable = result.throwable.toSurfyNetworkException())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = SeriesState.Loading
        )

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
}

sealed interface SeriesState {
    data object Loading : SeriesState
    data class Success(val series: Series, val imageList: ImageList) : SeriesState
    data class Error(val throwable: SurfyNetworkException) : SeriesState
}