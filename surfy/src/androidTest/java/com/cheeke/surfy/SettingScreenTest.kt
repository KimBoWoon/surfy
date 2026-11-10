package com.cheeke.surfy

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cheeke.surfy.model.LocaleOption
import com.cheeke.surfy.model.PosterSize
import com.cheeke.surfy.model.SurfyAppData
import com.cheeke.surfy.testing.model.configurationTestData
import com.cheeke.surfy.testing.model.languageListTestData
import com.cheeke.surfy.testing.model.regionTestData
import com.cheeke.surfy.testing.model.testGenreData
import com.cheeke.surfy.testing.utils.TestSurfyAppData
import com.cheeke.surfy.ui.setting.SettingScreen
import com.cheeke.surfy.ui.setting.SettingVM
import com.cheeke.surfy.ui.setting.SettingsAction
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var viewModel: SettingVM
    private lateinit var testUserDataRepository: TestUserDataRepository
    private lateinit var testMovieAppDataManager: TestSurfyAppData
    private val surfyAppData = SurfyAppData(
        secureBaseUrl = configurationTestData.images?.secureBaseUrl ?: "",
        movieGenres = testGenreData.genres.orEmpty(),
        region = regionTestData.results?.map {
            LocaleOption(
                code = it.iso31661 ?: "",
                label = it.englishName ?: "",
                isSelected = false
            )
        }.orEmpty(),
        language = languageListTestData.map {
            LocaleOption(
                code = it.iso6391 ?: "",
                label = it.englishName ?: "",
                isSelected = false
            )
        },
        posterSize = configurationTestData.images?.posterSizes?.map {
            PosterSize(size = it, isSelected = it == "original")
        }.orEmpty()
    )

    @Before
    fun setup() {
        testUserDataRepository = TestUserDataRepository()
        testMovieAppDataManager = TestSurfyAppData()
        viewModel = SettingVM(
            userDataRepository = testUserDataRepository,
            dataManager = testMovieAppDataManager
        )

        testMovieAppDataManager.setMovieAppData(surfyAppData)
        viewModel.onAction(action = SettingsAction.OpenMain)
    }

    @Test
    fun myScreenTest() {
        composeTestRule.apply {
            setContent {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                SettingScreen(
                    state = uiState,
                    onAction = {},
                    onClickTitle = {},
                    isCheatActive = false
                )
            }

            onNodeWithText(text = "메인 업데이트 날짜").assertExists().assertIsDisplayed()
            onNodeWithText(text = "2025-03-12").assertExists().assertIsDisplayed()
            onNodeWithText(text = "다크 모드 설정").assertExists().assertIsDisplayed()
            onNodeWithText(text = "성인").assertExists().assertIsDisplayed()
            onNodeWithText(text = "예고편 자동 재생").assertExists().assertIsDisplayed()
            onNodeWithText(text = "언어").assertExists().assertIsDisplayed()
            onNodeWithText(text = "ko-KR").assertExists().assertIsDisplayed()
            onNodeWithText(text = "이미지 퀄리티").assertExists().assertIsDisplayed()
            onNodeWithText(text = "original").assertExists().assertIsDisplayed()
            onNodeWithText(text = "버전 정보").assertExists().assertIsDisplayed()
//            onNodeWithText(text = getVersionName(context = composeTestRule.activity)).assertExists().assertIsDisplayed()
        }
    }
}