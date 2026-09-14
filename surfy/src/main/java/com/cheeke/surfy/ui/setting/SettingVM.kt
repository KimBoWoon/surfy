package com.cheeke.surfy.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.data.repository.UserDataRepository
import com.cheeke.surfy.data.util.DataManager
import com.cheeke.surfy.model.DarkThemeConfig
import com.cheeke.surfy.model.LocaleOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingVM @Inject constructor(
    private val userDataRepository: UserDataRepository,
    dataManager: DataManager
) : ViewModel() {
    companion object {
        private const val TAG = "SettingVM"
    }

    private val _uiState = MutableStateFlow(value = SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = combine(
        flow = userDataRepository.internalData,
        flow2 = dataManager.surfyAppData,
        flow3 = _uiState
    ) { internalData, movieAppDataState, settingsUiState ->
        val movieAppData = movieAppDataState.getMovieAppData()
        val selectedLanguage = movieAppData.selectedLanguage
        val selectedRegion = movieAppData.selectedRegion

        SettingsUiState(
            sheet = settingsUiState.sheet,
            mainUpdateDate = internalData.updateDate,
            theme = internalData.isDarkMode,
            isAdult = internalData.isAdult,
            isTrailerAutoplay = settingsUiState.isTrailerAutoplay,
            language = selectedLanguage,
            region = selectedRegion,
            selectedLanguage = settingsUiState.selectedLanguage ?: selectedLanguage,
            selectedRegion = settingsUiState.selectedRegion ?: selectedRegion,
            imageQuality = internalData.imageQuality,
            imageQualityList = movieAppData.posterSize.map { it.size.orEmpty() },
            allLanguages = movieAppData.language.sortedBy { it.label },
            allRegions = movieAppData.region.sortedBy { it.label },
            selectedTheme = settingsUiState.selectedTheme,
            themeList = DarkThemeConfig.entries,
            selectedImageQuality = settingsUiState.selectedImageQuality,
            isCheatActive = settingsUiState.isCheatActive ?: internalData.isCheatActive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SettingsUiState()
    )
    var titleClickCount = 0
    val _isCheatActive = MutableStateFlow(value = false)
    val isCheatActive = _isCheatActive.asStateFlow()

    init {
        viewModelScope.launch {
            _isCheatActive.emit(value = userDataRepository.getIsCheatActive())
            _uiState.update { it.copy(isCheatActive = userDataRepository.getIsCheatActive()) }
        }
    }

    fun onAction(action: SettingsAction) {
        Log.d("onAction", "$action")
        when (action) {
            SettingsAction.OpenMain -> _uiState.update { it.copy(sheet = SettingsSheet.Main) }
            SettingsAction.CloseSheet -> _uiState.update { it.copy(sheet = SettingsSheet.Hidden) }
            is SettingsAction.SetAdult -> {
                _uiState.update { it.copy(isAdult = action.enabled) }
                viewModelScope.launch {
                    userDataRepository.updateIsAdult(value = action.enabled)
                }
            }
            is SettingsAction.SetTrailerAutoplay -> {
                _uiState.update { it.copy(isTrailerAutoplay = action.enabled) }
                viewModelScope.launch {
                    userDataRepository.updateIsAutoPlayTrailer(value = action.enabled)
                }
            }
            SettingsAction.OpenLanguageRegion -> {
                _uiState.update {
                    it.copy(
                        sheet = SettingsSheet.LanguageRegion,
                        language = it.language,
                        region = it.region,
                    )
                }
            }
            SettingsAction.OpenImageQuality -> {
                _uiState.update {
                    it.copy(
                        sheet = SettingsSheet.ImageQuality,
                        imageQuality = it.imageQuality,
                        imageQualityList = it.imageQualityList
                    )
                }
            }
            is SettingsAction.PickLanguage -> _uiState.update { it.copy(selectedLanguage = action.option) }
            is SettingsAction.PickRegion -> _uiState.update { it.copy(selectedRegion = action.option) }
            SettingsAction.ConfirmLanguageRegion -> {
                viewModelScope.launch {
                    _uiState.value.selectedLanguage?.let { selectedLanguage ->
                        if (selectedLanguage != _uiState.value.selectedLanguage) {
                            userDataRepository.updateLanguage(value = selectedLanguage)
                        }
                    }
                    _uiState.value.selectedRegion?.let { selectedRegion ->
                        if (selectedRegion != _uiState.value.selectedRegion) {
                            userDataRepository.updateRegion(value = selectedRegion)
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        language = _uiState.value.selectedLanguage,
                        region = _uiState.value.selectedRegion,
                        sheet = SettingsSheet.Main
                    )
                }
            }
            SettingsAction.BackToMainFromLanguageRegion -> {
                _uiState.update {
                    it.copy(
                        sheet = SettingsSheet.Main,
                        language = it.language,
                        region = it.region,
                        selectedLanguage = null,
                        selectedRegion = null
                    )
                }
            }
            is SettingsAction.PickImageQuality -> _uiState.update { it.copy(selectedImageQuality = action.option) }
            SettingsAction.ConfirmImageQuality -> {
                _uiState.update {
                    it.copy(
                        imageQuality = _uiState.value.selectedImageQuality ?: "original",
                        sheet = SettingsSheet.Main
                    )
                }
                viewModelScope.launch {
                    _uiState.value.selectedImageQuality?.let {
                        userDataRepository.updateImageQuality(value = it)
                    }
                }
            }
            SettingsAction.BackToMainFromImageQuality -> {
                _uiState.update {
                    it.copy(
                        sheet = SettingsSheet.Main,
                        imageQuality = it.imageQuality,
                        selectedImageQuality = null
                    )
                }
            }
            SettingsAction.BackToMainFromThemeSetting -> {
                _uiState.update {
                    it.copy(
                        sheet = SettingsSheet.Main,
                        theme = it.theme,
                        selectedTheme = null
                    )
                }
            }
            SettingsAction.ConfirmTheme -> {
                _uiState.update {
                    it.copy(
                        theme = _uiState.value.selectedTheme ?: DarkThemeConfig.FOLLOW_SYSTEM,
                        sheet = SettingsSheet.Main
                    )
                }
                viewModelScope.launch {
                    _uiState.value.selectedTheme?.let {
                        userDataRepository.updateDarkMode(darkThemeConfig = it)
                    }
                }
            }
            SettingsAction.OpenThemeSetting -> {
                _uiState.update {
                    it.copy(
                        sheet = SettingsSheet.ThemeSetting,
                        theme = it.theme,
                        themeList = DarkThemeConfig.entries
                    )
                }
            }
            is SettingsAction.PickTheme -> _uiState.update { it.copy(selectedTheme = action.option) }
            is SettingsAction.SetCheatActive -> {
                viewModelScope.launch {
                    _uiState.value.isCheatActive?.let { isCheatActive ->
                        userDataRepository.updateIsCheatActive(value = !isCheatActive)
                    }
                }
                _uiState.update { it.copy(isCheatActive = it.isCheatActive?.not()) }
            }
        }
    }

    fun onClickTitle() {
        viewModelScope.launch {
            titleClickCount++

            if (titleClickCount >= 10) {
                _isCheatActive.emit(value = !isCheatActive.value)
                titleClickCount = 0
            }
        }
    }
}

sealed interface SettingsSheet {
    data object Hidden : SettingsSheet
    data object Main : SettingsSheet
    data object ThemeSetting : SettingsSheet
    data object LanguageRegion : SettingsSheet
    data object ImageQuality : SettingsSheet
}

data class SettingsUiState(
    val sheet: SettingsSheet = SettingsSheet.Hidden,
    val mainUpdateDate: String = "",
    val theme: DarkThemeConfig = DarkThemeConfig.DARK,
    val selectedTheme: DarkThemeConfig? = null,
    val themeList: List<DarkThemeConfig> = emptyList(),
    val isAdult: Boolean = true,
    val isTrailerAutoplay: Boolean = false,
    val language: String? = null,
    val region: String? = null,
    val selectedLanguage: String? = null,
    val selectedRegion: String? = null,
    val imageQuality: String = "original",
    val imageQualityList: List<String> = emptyList(),
    val selectedImageQuality: String? = null,
    val allLanguages: List<LocaleOption> = emptyList(),
    val allRegions: List<LocaleOption> = emptyList(),
    val isCheatActive: Boolean? = null
)

sealed interface SettingsAction {
    data object OpenMain : SettingsAction
    data object CloseSheet : SettingsAction

    // Main toggles
    data class SetAdult(val enabled: Boolean) : SettingsAction
    data class SetTrailerAutoplay(val enabled: Boolean) : SettingsAction

    // Navigate to sub sheets
    data object OpenLanguageRegion : SettingsAction
    data object OpenImageQuality : SettingsAction
    data object OpenThemeSetting : SettingsAction

    // Language/Region sub sheet events
    data class PickLanguage(val option: String) : SettingsAction
    data class PickRegion(val option: String) : SettingsAction
    data object ConfirmLanguageRegion : SettingsAction
    data object BackToMainFromLanguageRegion : SettingsAction

    // ImageQuality sub sheet events
    data class PickImageQuality(val option: String) : SettingsAction
    data object ConfirmImageQuality : SettingsAction
    data object BackToMainFromImageQuality : SettingsAction

    // Theme sub sheet events
    data object BackToMainFromThemeSetting : SettingsAction
    data class PickTheme(val option: DarkThemeConfig) : SettingsAction
    data object ConfirmTheme : SettingsAction

    // Cheat sub sheet events
    data class SetCheatActive(val enabled: Boolean) : SettingsAction
}