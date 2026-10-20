package com.cheeke.surfy.search.impl

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingSource
import com.cheeke.surfy.analytics.api.TestAnalyticsHelper
import com.cheeke.surfy.datamanager.api.TestSurfyAppData
import com.cheeke.surfy.model.SearchKeyword
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.network.api.TestSearchRemoteDataSource
import com.cheeke.surfy.search.impl.paging.RecommendKeywordPagingSource
import com.cheeke.surfy.testing.model.movieSearchTestData
import com.cheeke.surfy.testing.model.peopleSearchTestData
import com.cheeke.surfy.testing.model.seriesSearchTestData
import com.cheeke.surfy.testing.model.testRecommendedKeyword
import com.cheeke.surfy.testing.model.tvSearchTestData
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SearchVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var viewModel: SearchVM
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var searchRepository: TestSearchRepository
    private lateinit var testUserDataRepository: TestUserDataRepository
    private lateinit var testMovieAppDataManager: TestSurfyAppData
    private lateinit var testAnalyticsHelper: TestAnalyticsHelper
    private lateinit var apis: TestSearchRemoteDataSource

    @Before
    fun setup() {
        savedStateHandle = SavedStateHandle()
        searchRepository = TestSearchRepository()
        testUserDataRepository = TestUserDataRepository()
        testMovieAppDataManager = TestSurfyAppData()
        testAnalyticsHelper = TestAnalyticsHelper()
        apis = TestSearchRemoteDataSource().apply {
            searchResponses = mapOf(
                SearchType.MOVIE to movieSearchTestData,
                SearchType.PEOPLE to peopleSearchTestData,
                SearchType.TV to tvSearchTestData,
                SearchType.SERIES to seriesSearchTestData,
                SearchType.MULTI to movieSearchTestData
            )
        }
        viewModel = SearchVM(
            initialQuery = "",
            initialSearchType = SearchType.MOVIE,
            savedStateHandle = savedStateHandle,
            dataManager = testMovieAppDataManager,
            searchRepository = searchRepository,
            analyticsHelper = testAnalyticsHelper
        )
    }

    @Test
    fun updateKeywordTest() {
        assertEquals(expected = viewModel.query.value, actual = TextFieldValue(text = ""))
        viewModel.updateQuery(value = TextFieldValue(text = "mission"))
        assertEquals(expected = viewModel.query.value,  actual = TextFieldValue(text = "mission"))
        viewModel.updateQuery(value = TextFieldValue(text = "미션"))
        assertEquals(expected = viewModel.query.value,  actual = TextFieldValue(text = "미션"))
    }

    @Test
    fun updateSearchTypeTest() {
        assertEquals(viewModel.searchType.value, SearchType.MOVIE)
        viewModel.updateSearchType(SearchType.PEOPLE)
        assertEquals(viewModel.searchType.value, SearchType.PEOPLE)
        viewModel.updateSearchType(SearchType.MOVIE)
        assertEquals(viewModel.searchType.value, SearchType.MOVIE)
    }

//    @Test
//    fun searchMovieStateTest() = runTest {
//        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.searchResult.collect() }
//        viewModel.updateQuery(value = TextFieldValue(text = "미션"))
//        viewModel.searchMovies()
//
////        assertEquals(viewModel.searchMovieState.value, PagingData.empty<Movie>())
//
//        val pagingSource = SearchPagingSource(
//            apis = TestSearchRemoteDataSource(),
//            type = SearchType.MOVIE,
//            query = "미션",
//            language = "ko-KR",
//            region = "KR",
//            isAdult = true
//        )
//
//        val a: PagingSource.LoadResult<Int, Media> = PagingSource.LoadResult.Page(
//            data = movieSearchTestData.results.orEmpty(),
//            prevKey = null,
//            nextKey = null
//        )
//        val b = pagingSource.load(
//            PagingSource.LoadParams.Refresh(
//                key = null,
//                loadSize = 2,
//                placeholdersEnabled = false
//            )
//        )
//
//        assertEquals(
//            expected = a,
//            actual = b,
//        )
//    }

    @Test
    fun recommendedKeywordTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.recommendKeywordPaging.collect() }

        viewModel.updateQuery(value = TextFieldValue(text = "mission"))

        val pagingSource = RecommendKeywordPagingSource(
            apis = TestSearchRemoteDataSource(),
            query = "미션"
        )
        val a: PagingSource.LoadResult<Int, SearchKeyword> = PagingSource.LoadResult.Page(
            data = testRecommendedKeyword,
            prevKey = null,
            nextKey = null
        )
        val b = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 6,
                placeholdersEnabled = false
            )
        )

        assertEquals(
            a,
            b
        )
    }
}