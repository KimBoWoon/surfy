package com.cheeke.surfy.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.cheeke.surfy.testing.model.nowPlayingMovieTest
import com.cheeke.surfy.testing.model.popularMovieTest
import com.cheeke.surfy.testing.model.testTrendingMovie
import com.cheeke.surfy.testing.model.testTrendingPeople
import com.cheeke.surfy.testing.model.testTrendingTv
import com.cheeke.surfy.testing.model.upComingMovieTest
import com.cheeke.surfy.testing.utils.TestMovieAppDataManager
import com.cheeke.surfy.testing.utils.TestNetworkMonitor
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var viewModel: HomeVM
    private lateinit var dataManager: TestMovieAppDataManager
    private lateinit var testDatabaseRepository: TestMovieDatabaseRepository
    private lateinit var testPagingRepository: TestPagingRepository
    private lateinit var testNetworkMonitor: TestNetworkMonitor

    @Before
    fun setup() {
        testDatabaseRepository = TestMovieDatabaseRepository()
        dataManager = TestMovieAppDataManager()
        testPagingRepository = TestPagingRepository()
        testNetworkMonitor = TestNetworkMonitor()
        viewModel = HomeVM(
            dataManager = dataManager,
            movieRepository = testDatabaseRepository,
            homeRepository = testPagingRepository,
            networkMonitor = testNetworkMonitor
        )
    }

    @Test
    fun homeScreenLoadingTest() {
        composeTestRule.apply {
            setContent {
                HomeScreen(
                    homeUiState = HomeState.Loading,
                    nowPlayingMovies = viewModel.nowPlayingMoviePaging.collectAsLazyPagingItems(),
                    upComingMovies = viewModel.upComingMoviePaging.collectAsLazyPagingItems(),
                    trendingMovies = viewModel.trendingMoviePaging.collectAsLazyPagingItems(),
                    trendingPeoples = viewModel.trendingPeoplePaging.collectAsLazyPagingItems(),
                    trendingTvs = viewModel.trendingTvPaging.collectAsLazyPagingItems(),
                    trendingMovieTimeWindow = viewModel.trendingTvTimeWindow.collectAsStateWithLifecycle().value,
                    updateTrendingMovieTimeWindow = viewModel::updateTrendingMovieTimeWindow,
                    trendingPeopleTimeWindow = viewModel.trendingPeopleTimeWindow.collectAsStateWithLifecycle().value,
                    updateTrendingPeopleTimeWindow = viewModel::updateTrendingPeopleTimeWindow,
                    trendingTvTimeWindow = viewModel.trendingTvTimeWindow.collectAsStateWithLifecycle().value,
                    updateTrendingTvTimeWindow = viewModel::updateTrendingTvTimeWindow,
                    goToMovie = {},
                    goToPeople = {},
                    goToTv = {}
                )
            }

            onNodeWithContentDescription(label = "homeLoading").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun homeScreenSuccessTest() {
        composeTestRule.apply {
            setContent {
                HomeScreen(
                    homeUiState = HomeState.Success(homeUiState = HomeUiState(popularMovies = popularMovieTest, isShowNextWeekReleaseMovieDialog = false, nextWeekReleaseMovies = emptyList())),
                    nowPlayingMovies = viewModel.nowPlayingMoviePaging.collectAsLazyPagingItems(),
                    upComingMovies = viewModel.upComingMoviePaging.collectAsLazyPagingItems(),
                    trendingMovies = viewModel.trendingMoviePaging.collectAsLazyPagingItems(),
                    trendingPeoples = viewModel.trendingPeoplePaging.collectAsLazyPagingItems(),
                    trendingTvs = viewModel.trendingTvPaging.collectAsLazyPagingItems(),
                    trendingMovieTimeWindow = viewModel.trendingTvTimeWindow.collectAsStateWithLifecycle().value,
                    updateTrendingMovieTimeWindow = viewModel::updateTrendingMovieTimeWindow,
                    trendingPeopleTimeWindow = viewModel.trendingPeopleTimeWindow.collectAsStateWithLifecycle().value,
                    updateTrendingPeopleTimeWindow = viewModel::updateTrendingPeopleTimeWindow,
                    trendingTvTimeWindow = viewModel.trendingTvTimeWindow.collectAsStateWithLifecycle().value,
                    updateTrendingTvTimeWindow = viewModel::updateTrendingTvTimeWindow,
                    goToMovie = {},
                    goToPeople = {},
                    goToTv = {}
                )
            }

            onNodeWithText(text = "오늘의 추천 영화").assertExists().assertIsDisplayed()
            popularMovieTest.forEach { entity ->
                onNodeWithContentDescription(label = "todayRecommendMovies")
                    .assertExists()
                    .assertIsDisplayed()
                    .performScrollToNode(matcher = hasText(text = entity.title!!) and hasText(text = "추천수 ${entity.voteCount} · 평점 ${"%.2f".format(entity.voteAverage)}") and hasText(text = entity.releaseDate!!))
                    .assertExists()
                    .assertIsDisplayed()
            }
            onNodeWithText(text = "상영중인 영화").performScrollTo().assertExists().assertIsDisplayed()
            nowPlayingMovieTest.forEachIndexed { index, entity ->
                onNodeWithContentDescription(label = "nowPlayingMovies")
                    .performScrollTo()
                    .assertExists()
                    .assertIsDisplayed()
                    .performScrollToNode(matcher = hasText(text = "nowPlaying_$index"))
                    .assertExists()
                    .assertIsDisplayed()
            }
            onNodeWithText(text = "개봉예정 영화").performScrollTo().assertExists().assertIsDisplayed()
            upComingMovieTest.forEachIndexed { index, entity ->
                onNodeWithContentDescription(label = "upComingMovies")
                    .performScrollTo()
                    .assertExists()
                    .assertIsDisplayed()
                    .performScrollToNode(matcher = hasText(text = "upcomingMovie_$index") and hasText(text = "upcomingMovie_releaseDate_$index"))
                    .assertExists()
                    .assertIsDisplayed()
            }
            onNodeWithText(text = "인기 영화").performScrollTo().assertExists().assertIsDisplayed()
            testTrendingMovie.results?.forEachIndexed { index, entity ->
                onNodeWithContentDescription(label = activity.getString(com.cheeke.surfy.feature.home.R.string.trending_movie))
                    .performScrollTo()
                    .assertExists()
                    .assertIsDisplayed()
                    .performScrollToNode(matcher = hasText(text = entity.title!!) and hasText(text = entity.originalTitle!!))
                    .assertExists()
                    .assertIsDisplayed()
            }
            onNodeWithText(text = "인기 인물").performScrollTo().assertExists().assertIsDisplayed()
            testTrendingPeople.results?.forEachIndexed { index, entity ->
                onNodeWithContentDescription(label = activity.getString(com.cheeke.surfy.feature.home.R.string.trending_people))
                    .performScrollTo()
                    .assertExists()
                    .assertIsDisplayed()
                    .performScrollToNode(matcher = hasText(text = entity.title!!) and hasText(text = entity.originalTitle!!))
                    .assertExists()
                    .assertIsDisplayed()
            }
            onNodeWithText(text = "인기 드라마").performScrollTo().assertExists().assertIsDisplayed()
            testTrendingTv.results?.forEachIndexed { index, entity ->
                onNodeWithContentDescription(label = activity.getString(com.cheeke.surfy.feature.home.R.string.trending_tv))
                    .performScrollTo()
                    .assertExists()
                    .assertIsDisplayed()
                    .performScrollToNode(matcher = hasText(text = entity.title!!) and hasText(text = entity.originalTitle!!))
                    .assertExists()
                    .assertIsDisplayed()
            }
        }
    }

    @Test
    fun homeScreenErrorTest() {
        composeTestRule.apply {
            setContent {
                HomeScreen(
                    homeUiState = HomeState.Error(throwable = RuntimeException("something wrong...")),
                    nowPlayingMovies = viewModel.nowPlayingMoviePaging.collectAsLazyPagingItems(),
                    upComingMovies = viewModel.upComingMoviePaging.collectAsLazyPagingItems(),
                    trendingMovies = viewModel.trendingMoviePaging.collectAsLazyPagingItems(),
                    trendingPeoples = viewModel.trendingPeoplePaging.collectAsLazyPagingItems(),
                    trendingTvs = viewModel.trendingTvPaging.collectAsLazyPagingItems(),
                    trendingMovieTimeWindow = viewModel.trendingTvTimeWindow.collectAsStateWithLifecycle().value,
                    updateTrendingMovieTimeWindow = viewModel::updateTrendingMovieTimeWindow,
                    trendingPeopleTimeWindow = viewModel.trendingPeopleTimeWindow.collectAsStateWithLifecycle().value,
                    updateTrendingPeopleTimeWindow = viewModel::updateTrendingPeopleTimeWindow,
                    trendingTvTimeWindow = viewModel.trendingTvTimeWindow.collectAsStateWithLifecycle().value,
                    updateTrendingTvTimeWindow = viewModel::updateTrendingTvTimeWindow,
                    goToMovie = {},
                    goToPeople = {},
                    goToTv = {}
                )
            }

            onNodeWithText(text = "something wrong...").assertExists().assertIsDisplayed()
        }
    }

    // DAY 상태로 시작했을 때 DAY 텍스트가 표시되는지 확인
    @Test
    fun startDayTest() {
        composeTestRule.setContent {
            TimeWindowSwitch(
                timeWindow = TimeWindow.DAY,
                onChangeTimeWindow = {}
            )
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(com.cheeke.surfy.feature.home.R.string.time_window_day)) // R.string.time_window_day 값에 맞게 수정
            .assertIsDisplayed()
    }

    // WEEK 상태로 시작했을 때 WEEK 텍스트가 표시되는지 확인
    @Test
    fun startWeekTest() {
        composeTestRule.setContent {
            TimeWindowSwitch(
                timeWindow = TimeWindow.WEEK,
                onChangeTimeWindow = {}
            )
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(com.cheeke.surfy.feature.home.R.string.time_window_week)) // R.string.time_window_week 값에 맞게 수정
            .assertIsDisplayed()
    }

    // 클릭 시 onChangeTimeWindow가 반대 값으로 호출되는지 확인 (DAY → WEEK)
    @Test
    fun changeWeekTest() {
        var capturedTimeWindow: TimeWindow? = null

        composeTestRule.setContent {
            TimeWindowSwitch(
                timeWindow = TimeWindow.DAY,
                onChangeTimeWindow = { receivedTimeWindow ->
                    capturedTimeWindow = receivedTimeWindow
                }
            )
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(com.cheeke.surfy.feature.home.R.string.time_window_day))
            .performClick()

        assertEquals(TimeWindow.WEEK, capturedTimeWindow)
    }

    // 클릭 시 onChangeTimeWindow가 반대 값으로 호출되는지 확인 (WEEK → DAY)
    @Test
    fun chageDayTest() {
        var capturedTimeWindow: TimeWindow? = null

        composeTestRule.setContent {
            TimeWindowSwitch(
                timeWindow = TimeWindow.WEEK,
                onChangeTimeWindow = { receivedTimeWindow ->
                    capturedTimeWindow = receivedTimeWindow
                }
            )
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(com.cheeke.surfy.feature.home.R.string.time_window_week))
            .performClick()

        assertEquals(TimeWindow.DAY, capturedTimeWindow)
    }

    // 두 텍스트 모두 항상 화면에 존재하는지 확인
    @Test
    fun displayDayAndWeekTest() {
        composeTestRule.setContent {
            TimeWindowSwitch(
                timeWindow = TimeWindow.DAY,
                onChangeTimeWindow = {}
            )
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.cheeke.surfy.feature.home.R.string.time_window_day)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.cheeke.surfy.feature.home.R.string.time_window_week)).assertIsDisplayed()
    }

    // onChangeTimeWindow가 클릭당 정확히 1회만 호출되는지 확인
    @Test
    fun onChangeTimeWindowCallCountTest() {
        var callCount = 0

        composeTestRule.setContent {
            TimeWindowSwitch(
                timeWindow = TimeWindow.DAY,
                onChangeTimeWindow = { callCount++ }
            )
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(com.cheeke.surfy.feature.home.R.string.time_window_day))
            .performClick()

        assertEquals(1, callCount)
    }
}