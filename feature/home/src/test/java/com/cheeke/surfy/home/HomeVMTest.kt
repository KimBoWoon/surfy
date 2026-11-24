package com.cheeke.surfy.home

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.testing.repository.TestMovieDatabaseRepository
import com.cheeke.surfy.testing.repository.TestPagingRepository
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.testing.utils.TestMovieAppDataManager
import com.cheeke.surfy.testing.utils.TestNetworkMonitor
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class HomeVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var viewModel: HomeVM
    private lateinit var testDatabaseRepository: TestMovieDatabaseRepository
    private lateinit var testPagingRepository: TestPagingRepository
    private lateinit var testNetworkMonitor: TestNetworkMonitor
    private lateinit var testUserRepository: TestUserDataRepository
    private lateinit var testMovieAppDataManager: TestMovieAppDataManager

    @Before
    fun setup() {
        testDatabaseRepository = TestMovieDatabaseRepository()
        testPagingRepository = TestPagingRepository()
        testNetworkMonitor = TestNetworkMonitor()
        testUserRepository = TestUserDataRepository()
        testMovieAppDataManager = TestMovieAppDataManager()
        viewModel = HomeVM(
            movieRepository = testDatabaseRepository,
            homeRepository = testPagingRepository,
            networkMonitor = testNetworkMonitor,
            dataManager = testMovieAppDataManager
        )
    }

    @Test
    fun userDataLoadingTest() = runTest {
        assertEquals(expected = viewModel.homeUiState.value, actual = HomeState.Loading)
        backgroundScope.launch(context = UnconfinedTestDispatcher()) { viewModel.homeUiState.collect() }
    }

    @Test
    fun userDataSuccessTest() = runTest {
        assertEquals(expected = viewModel.homeUiState.value, actual = HomeState.Loading)
        backgroundScope.launch(context = UnconfinedTestDispatcher()) { viewModel.homeUiState.collect() }

        val nowPlayingMovieResult = testDatabaseRepository.getNowPlayingMovies()
        val nowPlayingTestPager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = testDatabaseRepository.getNowPlayingMovies()
        )

        assertEquals(
            expected = nowPlayingTestPager.refresh(initialKey = 0),
            actual = nowPlayingMovieResult.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        )

        val upComingMovieResult = testDatabaseRepository.getUpComingMovies()
        val upComingMovieTestPager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = testDatabaseRepository.getUpComingMovies()
        )

        assertEquals(
            expected = upComingMovieTestPager.refresh(initialKey = 0),
            actual = upComingMovieResult.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        )
    }

    @Test
    fun getHomeUiStateTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.homeUiState.collect() }

        val movie = Movie(voteCount = 687, voteAverage = 8.7f)

//        assertEquals(expected = viewModel.homeUiState.value, actual = HomeState.Loading)

        testDatabaseRepository.setMovies(listOf(movie))

        assertEquals(
            expected = viewModel.homeUiState.value,
            actual = HomeState.Success(
                homeUiState = HomeUiState(
                    popularMovies = listOf(movie),
                    isShowNextWeekReleaseMovieDialog = false,
                    nextWeekReleaseMovies = emptyList()
                )
            )
        )
    }
}