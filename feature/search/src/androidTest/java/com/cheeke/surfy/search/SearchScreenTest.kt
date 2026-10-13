package com.cheeke.surfy.search

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.cheeke.surfy.analytics.AnalyticsEvent
import com.cheeke.surfy.analytics.AnalyticsHelper
import com.cheeke.surfy.model.Genre
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.model.SurfyAppData
import com.cheeke.surfy.testing.model.genreListTestData
import com.cheeke.surfy.testing.model.keywordList
import com.cheeke.surfy.testing.model.testRecommendedKeyword
import com.cheeke.surfy.testing.repository.TestKeywordDataBaseRepository
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.TestMovieAppDataManager
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var viewModel: SearchVM
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var testPagingRepository: TestPagingRepository
    private lateinit var testUserDataRepository: TestUserDataRepository
    private lateinit var testKeywordDataBaseRepository: TestKeywordDataBaseRepository
    private lateinit var movieAppDataRepository: TestMovieAppDataManager
    private val genres = genreListTestData.genres.orEmpty()

    @Before
    fun setup() {
        savedStateHandle = SavedStateHandle()
        testPagingRepository = TestPagingRepository()
        testUserDataRepository = TestUserDataRepository()
        movieAppDataRepository = TestMovieAppDataManager()
        testKeywordDataBaseRepository = TestKeywordDataBaseRepository()
        movieAppDataRepository.setMovieAppData(surfyAppData = SurfyAppData(movieGenres = genres))
        viewModel = SearchVM(
            initialQuery = "",
            initialSearchType = SearchType.MOVIE,
            savedStateHandle = savedStateHandle,
            dataManager = movieAppDataRepository,
            searchRepository = testPagingRepository,
            keywordDataBaseRepository = testKeywordDataBaseRepository,
            analyticsHelper = object : AnalyticsHelper {
                override fun logEvent(event: AnalyticsEvent) {
                    println("event: $event")
                }
            }
        )
    }

    @Test
    fun searchScreenInputKeywordHintDisplayTest() {
        composeTestRule.apply {
            setContent {
                val searchState by viewModel.searchResult.collectAsStateWithLifecycle()
                val searchType by viewModel.searchType.collectAsStateWithLifecycle()
                val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
                val movieAppData by movieAppDataRepository.surfyAppData.collectAsStateWithLifecycle()
                val query by viewModel.query.collectAsStateWithLifecycle()
                val recentlyKeyword = viewModel.recentlyKeywordPaging.collectAsLazyPagingItems()

                SearchScreen(
                    searchUiState = searchState,
                    recentlyKeyword = recentlyKeyword,
                    recommendKeyword = viewModel.recommendKeywordPaging.collectAsLazyPagingItems(),
                    query = query,
                    searchType = searchType,
                    surfyAppData = movieAppData.getMovieAppData(),
                    selectedGenre = selectedGenre,
                    goToMovie = {},
                    goToTv = {},
                    goToPeople = {},
                    goToSeries = {},
                    onSaveKeyword = viewModel::saveKeyword,
                    deleteKeyword = viewModel::deleteRecentlyKeyword,
                    deleteAllKeyword = viewModel::deleteAllRecentlyKeyword,
                    onSearchClick = viewModel::searchMovies,
                    updateKeyword = viewModel::updateQuery,
                    updateSearchType = viewModel::updateSearchType,
                    updateGenre = viewModel::updateGenre
                )
            }

            onNodeWithContentDescription(label = "searchBarIcon").assertExists().assertIsDisplayed()
            onNodeWithText(text = "검색어를 입력하세요.").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "searchKeywordClear").assertDoesNotExist()
            onNodeWithContentDescription(label = "searchMovies").assertDoesNotExist()
        }
    }

    @Test
    fun searchScreenInputKeywordTest() {
        composeTestRule.apply {
            setContent {
                val searchState by viewModel.searchResult.collectAsStateWithLifecycle()
                val searchType by viewModel.searchType.collectAsStateWithLifecycle()
                val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
                val query by viewModel.query.collectAsStateWithLifecycle()

                viewModel.updateQuery(value = TextFieldValue(text = "mission"))

                val movieAppData by movieAppDataRepository.surfyAppData.collectAsStateWithLifecycle()

                val recentlyKeyword = viewModel.recentlyKeywordPaging.collectAsLazyPagingItems()

                SearchScreen(
                    searchUiState = searchState,
                    recentlyKeyword = recentlyKeyword,
                    recommendKeyword = viewModel.recommendKeywordPaging.collectAsLazyPagingItems(),
                    query = query,
                    searchType = searchType,
                    surfyAppData = movieAppData.getMovieAppData(),
                    selectedGenre = selectedGenre,
                    goToMovie = {},
                    goToTv = {},
                    goToPeople = {},
                    goToSeries = {},
                    onSaveKeyword = viewModel::saveKeyword,
                    deleteKeyword = viewModel::deleteRecentlyKeyword,
                    deleteAllKeyword = viewModel::deleteAllRecentlyKeyword,
                    onSearchClick = viewModel::searchMovies,
                    updateKeyword = viewModel::updateQuery,
                    updateSearchType = viewModel::updateSearchType,
                    updateGenre = viewModel::updateGenre
                )
            }

            onNodeWithContentDescription(label = "searchBarIcon").assertExists().assertIsDisplayed()
            onNodeWithText(text = "mission").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "searchKeywordClear").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "searchMovies").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun recommendKeywordTest() = runTest(UnconfinedTestDispatcher()) {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.recommendKeywordPaging.collect { println("recommendKeywordPaging -> $it") } }
        composeTestRule.apply {
            setContent {
                val query by viewModel.query.collectAsStateWithLifecycle()

                RecommendKeywordComponent(
                    recentlyKeyword = flowOf(value = PagingData.from(data = keywordList)).collectAsLazyPagingItems(),
                    recommendKeyword = flowOf(value = PagingData.from(data = testRecommendedKeyword)).collectAsLazyPagingItems(),
                    query = query,
                    onSaveKeyword = viewModel::saveKeyword,
                    deleteKeyword = viewModel::deleteRecentlyKeyword,
                    deleteAllKeyword= viewModel::deleteAllRecentlyKeyword,
                    updateKeyword = viewModel::updateQuery,
                    onSearchClick = viewModel::searchMovies,
                    recommendKeywordVisible = {}
                )
            }

            onNodeWithText(text = "최근 검색어").assertExists().assertIsDisplayed()
            onNodeWithText(text = "검색 기록 삭제").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "recentlyKeywordList").assertExists().assertIsDisplayed()
            keywordList.forEach {
                onNodeWithContentDescription(label = "recentlyKeywordList").performScrollToNode(matcher = hasText(text = it.keyword)).assertExists().assertIsDisplayed()
            }
            onNodeWithText(text = "추천 검색어").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "recommendedKeywordClose").assertExists().assertIsDisplayed()
            (0..5).forEach {
                onNodeWithContentDescription(label = "recommendKeywordList").performScrollToNode(matcher = hasContentDescription(value = "mission$it")).assertExists().assertIsDisplayed()
            }

        }
    }

    @Test
    fun searchScreenMovieSearchTest() {
        composeTestRule.apply {
            setContent {
                val searchState by viewModel.searchResult.collectAsStateWithLifecycle()
                val searchType by viewModel.searchType.collectAsStateWithLifecycle()
                val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
                val movieAppData by movieAppDataRepository.surfyAppData.collectAsStateWithLifecycle()
                val query by viewModel.query.collectAsStateWithLifecycle()

                viewModel.updateQuery(value = TextFieldValue(text = "mission"))

                val recentlyKeyword = viewModel.recentlyKeywordPaging.collectAsLazyPagingItems()

                SearchScreen(
                    searchUiState = searchState,
                    recentlyKeyword = recentlyKeyword,
                    recommendKeyword = viewModel.recommendKeywordPaging.collectAsLazyPagingItems(),
                    query = query,
                    searchType = searchType,
                    surfyAppData = movieAppData.getMovieAppData(),
                    selectedGenre = selectedGenre,
                    goToMovie = {},
                    goToTv = {},
                    goToPeople = {},
                    goToSeries = {},
                    onSaveKeyword = viewModel::saveKeyword,
                    deleteKeyword = viewModel::deleteRecentlyKeyword,
                    deleteAllKeyword = viewModel::deleteAllRecentlyKeyword,
                    onSearchClick = viewModel::searchMovies,
                    updateKeyword = viewModel::updateQuery,
                    updateSearchType = viewModel::updateSearchType,
                    updateGenre = viewModel::updateGenre
                )
            }

            onNodeWithContentDescription(label = "searchBarIcon").assertExists().assertIsDisplayed()
            onNodeWithText(text = "mission").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "searchKeywordClear").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "searchMovies").assertExists().assertIsDisplayed().performClick()
            (1..100).forEach {
                onNodeWithContentDescription(label = "searchResultList").performScrollToNode(hasContentDescription(value = "${it}_title_$it")).assertExists().assertIsDisplayed()
            }
        }
    }

    @Test
    fun searchScreenPeopleSearchTest() {
        composeTestRule.apply {
            setContent {
                val searchState by viewModel.searchResult.collectAsStateWithLifecycle()
                val searchType by viewModel.searchType.collectAsStateWithLifecycle()
                val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
                val query by viewModel.query.collectAsStateWithLifecycle()

                viewModel.updateQuery(value = TextFieldValue(text = "name"))

                val movieAppData by movieAppDataRepository.surfyAppData.collectAsStateWithLifecycle()

                val recentlyKeyword = viewModel.recentlyKeywordPaging.collectAsLazyPagingItems()

                SearchScreen(
                    searchUiState = searchState,
                    recentlyKeyword = recentlyKeyword,
                    recommendKeyword = viewModel.recommendKeywordPaging.collectAsLazyPagingItems(),
                    query = query,
                    searchType = searchType,
                    surfyAppData = movieAppData.getMovieAppData(),
                    selectedGenre = selectedGenre,
                    goToMovie = {},
                    goToTv = {},
                    goToPeople = {},
                    goToSeries = {},
                    onSaveKeyword = viewModel::saveKeyword,
                    deleteKeyword = viewModel::deleteRecentlyKeyword,
                    deleteAllKeyword = viewModel::deleteAllRecentlyKeyword,
                    onSearchClick = viewModel::searchMovies,
                    updateKeyword = viewModel::updateQuery,
                    updateSearchType = viewModel::updateSearchType,
                    updateGenre = viewModel::updateGenre
                )
            }

            onNodeWithContentDescription(label = "searchBarIcon").assertExists().assertIsDisplayed()
            onNodeWithTag(testTag = "searchType").assertExists().assertIsDisplayed().performClick()
            onNodeWithText(text = SearchType.PEOPLE.label).assertExists().assertIsDisplayed().performClick()
            onNodeWithText(text = "name").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "searchKeywordClear").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "searchMovies").assertExists().assertIsDisplayed().performClick()
            (1..100).forEach {
                onNodeWithContentDescription(label = "searchResultList").performScrollToNode(hasContentDescription(value = "${it}_title_$it")).assertExists().assertIsDisplayed()
            }
        }
    }

//    @Test
//    fun searchScreenSnackbarTest() = runTest {
//        composeTestRule.apply {
//            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.showSnackbar.collect { println(it) } }
//            setContent {
//                val searchState by viewModel.searchResult.collectAsStateWithLifecycle()
//                val searchType by viewModel.searchType.collectAsStateWithLifecycle()
//                val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
//                val movieAppData by movieAppDataRepository.movieAppData.collectAsStateWithLifecycle()
//
//                viewModel.updateKeyword(keyword = " ")
//                viewModel.showSnackbar.tryEmit(value = Unit)
//
//                SearchScreen(
//                    searchUiState = searchState,
//                    recommendKeyword = RecommendKeywordUiState.Loading,
//                    keyword = viewModel.searchQuery,
//                    searchType = searchType,
//                    movieAppData = movieAppData,
//                    selectedGenre = selectedGenre,
//                    goToMovie = {},
//                    goToPeople = {},
//                    goToSeries = {},
//                    onSearchClick = viewModel::searchMovies,
//                    updateKeyword = viewModel::updateKeyword,
//                    updateSearchType = viewModel::updateSearchType,
//                    updateGenre = viewModel::updateGenre
//                )
//            }
//
//            onNodeWithContentDescription(label = "searchBarIcon").assertExists().assertIsDisplayed()
//            onNodeWithText(text = " ").assertExists().assertIsDisplayed()
//            onNodeWithContentDescription(label = "searchKeywordClear").assertExists().assertIsDisplayed()
//            onNodeWithContentDescription(label = "searchMovies").assertExists().assertIsDisplayed().performClick()
//            onNodeWithText(text = "검색어를 입력하세요.").assertExists().assertIsDisplayed()
//        }
//    }

    @Test
    fun searchScreenFilterTest() {
        composeTestRule.apply {
            setContent {
                val searchState by viewModel.searchResult.collectAsStateWithLifecycle()
                val searchType by viewModel.searchType.collectAsStateWithLifecycle()
                val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
                val movieAppData by movieAppDataRepository.surfyAppData.collectAsStateWithLifecycle()
                val query by viewModel.query.collectAsStateWithLifecycle()

                viewModel.updateQuery(value = TextFieldValue(text = "mission"))

                val recentlyKeyword = viewModel.recentlyKeywordPaging.collectAsLazyPagingItems()

                SearchScreen(
                    searchUiState = searchState,
                    recentlyKeyword = recentlyKeyword,
                    recommendKeyword = viewModel.recommendKeywordPaging.collectAsLazyPagingItems(),
                    query = query,
                    searchType = searchType,
                    surfyAppData = movieAppData.getMovieAppData(),
                    selectedGenre = selectedGenre,
                    goToMovie = {},
                    goToTv = {},
                    goToPeople = {},
                    goToSeries = {},
                    onSaveKeyword = viewModel::saveKeyword,
                    deleteKeyword = viewModel::deleteRecentlyKeyword,
                    deleteAllKeyword = viewModel::deleteAllRecentlyKeyword,
                    onSearchClick = viewModel::searchMovies,
                    updateKeyword = viewModel::updateQuery,
                    updateSearchType = viewModel::updateSearchType,
                    updateGenre = viewModel::updateGenre
                )
            }

            onNodeWithContentDescription(label = "searchBarIcon").assertExists().assertIsDisplayed()
            onNodeWithText(text = "mission").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "searchKeywordClear").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "searchMovies").assertExists().assertIsDisplayed().performClick()
            genres.forEach {
                onNodeWithTag(testTag = "FilterRow").performScrollToNode(hasContentDescription(value = "${it.name}")).assertExists().assertIsDisplayed()
            }
            viewModel.updateGenre(Genre(id = 2, name = "Action"))
            onNodeWithContentDescription(label = "Action").assertExists().assertIsDisplayed().performClick()
            (1..100).forEach {
                onNodeWithContentDescription(label = "searchResultList").performScrollToNode(hasContentDescription(value = "${it}_title_$it")).assertExists().assertIsDisplayed()
            }
        }
    }
}