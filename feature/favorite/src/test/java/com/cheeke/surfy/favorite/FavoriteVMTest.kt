package com.cheeke.surfy.favorite

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.cheeke.surfy.data.repository.FavoriteKeys
import com.cheeke.surfy.data.repository.FavoriteMovieRepository
import com.cheeke.surfy.data.repository.FavoritePeopleRepository
import com.cheeke.surfy.data.repository.FavoriteTvRepository
import com.cheeke.surfy.testing.repository.TestMovieRepository
import com.cheeke.surfy.testing.repository.TestPeopleRepository
import com.cheeke.surfy.testing.repository.TestTvRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class FavoriteVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val movieRepository = TestMovieRepository()
    private val peopleRepository = TestPeopleRepository()
    private val tvRepository = TestTvRepository()
    private val movieTab = FavoriteMovieRepository(repository = movieRepository)
    private val peopleTab = FavoritePeopleRepository(repository = peopleRepository)
    private val tvTab = FavoriteTvRepository(repository = tvRepository)
    private lateinit var viewModel: FavoriteVM

    @Before
    fun setup() {
        viewModel = FavoriteVM(
            initialTabKey = FavoriteKeys.MOVIE,
            repositories = mapOf(
                FavoriteKeys.MOVIE to movieTab,
                FavoriteKeys.TV to tvTab,
                FavoriteKeys.PEOPLE to peopleTab
            )
        )
    }

    @Test
    fun changeTabIndexTest() = runTest {
        backgroundScope.launch(context = UnconfinedTestDispatcher()) { viewModel.currentTab.collect() }

        assertEquals(
            expected = viewModel.currentTab.value,
            actual = FavoriteKeys.MOVIE
        )

        viewModel.updateTabKey(key = FavoriteKeys.TV)

        assertEquals(
            expected = viewModel.currentTab.value,
            actual = FavoriteKeys.TV
        )

        viewModel.updateTabKey(key = FavoriteKeys.PEOPLE)

        assertEquals(
            expected = viewModel.currentTab.value,
            actual = FavoriteKeys.PEOPLE
        )
    }

    @Test
    fun favoriteMoviePagingTest() = runTest {
        val favoriteMovieSource = movieRepository.getFavorite()
        val favoriteMoviePager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieRepository.getFavorite()
        )

        assertEquals(
            expected = favoriteMoviePager.refresh(initialKey = 0),
            actual = favoriteMovieSource.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        )
    }

    @Test
    fun favoritePeoplePagingTest() = runTest {
        val favoritePeopleSource = movieRepository.getFavorite()
        val favoritePeoplePager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieRepository.getFavorite()
        )

        assertEquals(
            expected = favoritePeoplePager.refresh(initialKey = 0),
            actual = favoritePeopleSource.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        )
    }

    @Test
    fun favoriteTvPagingTest() = runTest {
        val favoriteTvSource = movieRepository.getFavorite()
        val favoriteTvPager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieRepository.getFavorite()
        )

        assertEquals(
            expected = favoriteTvPager.refresh(initialKey = 0),
            actual = favoriteTvSource.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        )
    }

//    @Test
//    fun deleteMovieTest() = runTest {
//        backgroundScope.launch(context = UnconfinedTestDispatcher()) { viewModel.favoriteMovies.collect() }
//
//        testDatabaseRepository.insertMovie(movie = movie1)
//        testDatabaseRepository.insertMovie(movie = movie2)
//        viewModel.deleteFavorite(favoriteTab = FavoriteTab.MOVIE, media = movie1)
//
//        assertEquals(
//            expected = viewModel.favoriteMovies.value,
//            actual = listOf(movie2)
//        )
//    }
//
//    @Test
//    fun deletePeopleTest() = runTest {
//        backgroundScope.launch(context = UnconfinedTestDispatcher()) { viewModel.favoritePeoples.collect() }
//
//        testDatabaseRepository.insertPeople(people = people1)
//        testDatabaseRepository.insertPeople(people = people2)
//        viewModel.deletePeople(people = people1)
//
//        assertEquals(
//            expected = viewModel.favoritePeoples.value,
//            actual = listOf(people2)
//        )
//    }
//
//    @Test
//    fun deleteTvTest() = runTest {
//        backgroundScope.launch(context = UnconfinedTestDispatcher()) { viewModel.favoriteTvs.collect() }
//
//        testDatabaseRepository.insertTv(tv = tv1)
//        testDatabaseRepository.insertTv(tv = tv2)
//        viewModel.deleteTv(tv = tv1)
//
//        assertEquals(
//            expected = viewModel.favoriteTvs.value,
//            actual = listOf(tv2)
//        )
//    }
}