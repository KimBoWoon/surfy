package com.cheeke.surfy.home.impl

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.cheeke.surfy.datamanager.api.TestSurfyAppData
import com.cheeke.surfy.detail.api.TestMovieDao
import com.cheeke.surfy.detail.api.TestMovieRepository
import com.cheeke.surfy.detail.api.TestPeopleDao
import com.cheeke.surfy.detail.api.TestTvDao
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.network.api.TestNetworkMonitor
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import kotlinx.coroutines.flow.collect
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
    private lateinit var testDatabaseRepository: TestMovieRepository
    private lateinit var testNetworkMonitor: TestNetworkMonitor
    private lateinit var testUserRepository: TestUserDataRepository
    private lateinit var testMovieAppDataManager: TestSurfyAppData
    private lateinit var movieDao: TestMovieDao
    private lateinit var peopleDao: TestPeopleDao
    private lateinit var tvDao: TestTvDao

    @Before
    fun setup() {
        testDatabaseRepository = TestMovieRepository()
        testNetworkMonitor = TestNetworkMonitor()
        testUserRepository = TestUserDataRepository()
        testMovieAppDataManager = TestSurfyAppData()
        movieDao = TestMovieDao()
        peopleDao = TestPeopleDao()
        tvDao = TestTvDao()
        viewModel = HomeVM(
            movieDataBaseRepository = testDatabaseRepository,
            networkMonitor = testNetworkMonitor,
            dataManager = testMovieAppDataManager,
            homeRepository = TestHomeRepository()
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

        val nowPlayingMovieResult = movieDao.getNowPlayingMovie()
        val nowPlayingTestPager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getNowPlayingMovie()
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

        val upComingMovieResult = movieDao.getUpComingMovie()
        val upComingMovieTestPager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getUpComingMovie()
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