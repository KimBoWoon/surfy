package com.cheeke.surfy.datamanager.api

import com.cheeke.surfy.model.DarkThemeConfig
import com.cheeke.surfy.model.SurfyAppData
import kotlinx.coroutines.flow.StateFlow

interface DataManager {
    val surfyAppData: StateFlow<SurfyAppDataState>
}

sealed interface SurfyAppDataState {
    data object Loading : SurfyAppDataState
    data class Success(val data: SurfyAppData) : SurfyAppDataState {
        override fun shouldUseDarkTheme(isSystemDarkTheme: Boolean) =
            when (data.isDarkMode) {
                DarkThemeConfig.FOLLOW_SYSTEM -> isSystemDarkTheme
                DarkThemeConfig.LIGHT -> false
                DarkThemeConfig.DARK -> true
            }

        override fun getMovieAppData(): SurfyAppData = this.data
    }
    data class Error(val throwable: Throwable) : SurfyAppDataState

    fun shouldKeepSplashScreen(): Boolean = this is Loading
    fun shouldUseDarkTheme(isSystemDarkTheme: Boolean): Boolean = isSystemDarkTheme
    fun getMovieAppData(): SurfyAppData = SurfyAppData()
}

data class Locale(val language: String, val region: String)