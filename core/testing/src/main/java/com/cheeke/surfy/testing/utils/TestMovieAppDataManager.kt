package com.cheeke.surfy.testing.utils

import androidx.annotation.VisibleForTesting
import com.cheeke.surfy.data.util.DataManager
import com.cheeke.surfy.data.util.SurfyAppDataState
import com.cheeke.surfy.model.SurfyAppData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestMovieAppDataManager : DataManager {
    private val _movieAppData = MutableStateFlow<SurfyAppDataState>(value = SurfyAppDataState.Loading)
    override val surfyAppData: StateFlow<SurfyAppDataState> = _movieAppData.asStateFlow()

    @VisibleForTesting
    fun setMovieAppData(surfyAppData: SurfyAppData) {
        _movieAppData.value = SurfyAppDataState.Success(data = surfyAppData)
    }

    @VisibleForTesting
    fun setError(throwable: Throwable) {
        _movieAppData.value = SurfyAppDataState.Error(throwable = throwable)
    }
}