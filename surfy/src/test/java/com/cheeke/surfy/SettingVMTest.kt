package com.cheeke.surfy

import com.cheeke.surfy.model.DarkThemeConfig
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.testing.utils.TestMovieAppDataManager
import com.cheeke.surfy.ui.setting.SettingVM
import com.cheeke.surfy.ui.setting.SettingsSheet
import com.cheeke.surfy.ui.setting.SettingsUiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SettingVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var viewModel: SettingVM
    private lateinit var testUserDataRepository: TestUserDataRepository
    private lateinit var testMovieAppDataManager: TestMovieAppDataManager
    private val settingUiState = SettingsUiState(
        sheet = SettingsSheet.Hidden,
        mainUpdateDate = "",
        theme = DarkThemeConfig.FOLLOW_SYSTEM,
        isAdult = true,
        isTrailerAutoplay = false,
        language = "",
        region = "",
        imageQuality = "original",
        imageQualityList = emptyList(),
        allLanguages = emptyList(),
        allRegions = emptyList(),
        selectedTheme = null,
        themeList = DarkThemeConfig.entries,
        selectedLanguage = "",
        selectedRegion = "",
        selectedImageQuality = null,
        isCheatActive = false
    )

    @Before
    fun setup() {
        testUserDataRepository = TestUserDataRepository()
        testMovieAppDataManager = TestMovieAppDataManager()
        viewModel = SettingVM(
            userDataRepository = testUserDataRepository,
            dataManager = testMovieAppDataManager
        )
    }

    @Test
    fun settingUiStateTest() = runTest {
        backgroundScope.launch(context = UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(expected = viewModel.uiState.value, actual = settingUiState)
    }

    @Test
    fun updateIsAdultTest() = runTest {
        assertEquals(
            expected = testUserDataRepository.internalData.first().isAdult,
            actual = true
        )
        testUserDataRepository.updateIsAdult(value = false)
        assertEquals(
            expected = testUserDataRepository.internalData.first().isAdult,
            actual = false
        )
    }

    @Test
    fun updateIsAutoPlayTrailerTest() = runTest {
        assertEquals(
            expected = testUserDataRepository.internalData.first().isAutoPlayTrailer,
            actual = true
        )
        testUserDataRepository.updateIsAutoPlayTrailer(value = false)
        assertEquals(
            expected = testUserDataRepository.internalData.first().isAutoPlayTrailer,
            actual = false
        )
    }

    @Test
    fun updateDarkModeSettingTest() = runTest {
        assertEquals(
            expected = testUserDataRepository.internalData.first().isDarkMode,
            actual = DarkThemeConfig.FOLLOW_SYSTEM
        )
        testUserDataRepository.updateDarkMode(darkThemeConfig = DarkThemeConfig.LIGHT)
        assertEquals(
            expected = testUserDataRepository.internalData.first().isDarkMode,
            actual = DarkThemeConfig.LIGHT
        )
    }

    @Test
    fun updateMainDateTest() = runTest {
        assertEquals(
            expected = testUserDataRepository.internalData.first().updateDate,
            actual = ""
        )
        testUserDataRepository.updateMainDate(value = "2025-12-13")
        assertEquals(
            expected = testUserDataRepository.internalData.first().updateDate,
            actual = "2025-12-13"
        )
    }

    @Test
    fun updateRegionTest() = runTest {
        assertEquals(
            expected = testUserDataRepository.internalData.first().region,
            actual = "KR"
        )
        testUserDataRepository.updateRegion(value = "US")
        assertEquals(
            expected = testUserDataRepository.internalData.first().region,
            actual = "US"
        )
    }

    @Test
    fun updateLanguageTest() = runTest {
        assertEquals(
            expected = testUserDataRepository.internalData.first().language,
            actual = "ko"
        )
        testUserDataRepository.updateLanguage(value = "ja")
        assertEquals(
            expected = testUserDataRepository.internalData.first().language,
            actual = "ja"
        )
    }

    @Test
    fun updateImageQualityTest() = runTest {
        assertEquals(
            expected = testUserDataRepository.internalData.first().imageQuality,
            actual = "original"
        )
        testUserDataRepository.updateImageQuality(value = "w342")
        assertEquals(
            expected = testUserDataRepository.internalData.first().imageQuality,
            actual = "w342"
        )
    }

    @Test
    fun updateShowNextReleaseMoviesDateTest() = runTest {
        assertEquals(
            expected = testUserDataRepository.internalData.first().showNextReleaseMoviesDate,
            actual = ""
        )
        testUserDataRepository.updateShowNextReleaseMoviesDate(value = "2025-12-13")
        assertEquals(
            expected = testUserDataRepository.internalData.first().showNextReleaseMoviesDate,
            actual = "2025-12-13"
        )
    }
}