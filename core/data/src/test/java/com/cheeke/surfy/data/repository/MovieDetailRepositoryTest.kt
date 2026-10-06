package com.cheeke.surfy.data.repository

import com.cheeke.surfy.core.datastore.InternalDataPreferences
import com.cheeke.surfy.datastore.InternalDataSource
import com.cheeke.surfy.datastore_test.InMemoryDataStore
import com.cheeke.surfy.testing.TestMovieRemoteDataSource
import com.cheeke.surfy.testing.TestSeriesRemoteDataSource
import com.cheeke.surfy.testing.model.favoriteMovieDetailTestData
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.testImageList
import com.cheeke.surfy.testing.model.watchProvidersTestData
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MovieDetailRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var movieApis: TestMovieRemoteDataSource
    private lateinit var seriesApis: TestSeriesRemoteDataSource
    private lateinit var datastore: InternalDataSource
    private lateinit var repository: MovieDetailRepositoryImpl
    private lateinit var userDataRepository: UserDataRepository

    @Before
    fun setup() {
        movieApis = TestMovieRemoteDataSource()
        seriesApis = TestSeriesRemoteDataSource()
        datastore = InternalDataSource(
            datastore = InMemoryDataStore(initialValue = InternalDataPreferences.getDefaultInstance())
        )
        userDataRepository = TestUserDataRepository()
        repository = MovieDetailRepositoryImpl(
            requestOptionsProvider = DetailRequestOptionsProvider(userdata = userDataRepository),
            movieApis = movieApis,
            seriesApis = seriesApis,
        )
    }

    @Test
    fun getMovieDetailTest() = runTest {
        val result = repository.getData(id = 0)

        assertEquals(expected = result.first(), actual = favoriteMovieDetailTestData)
    }

    @Test
    fun getMovieSeriesTest() = runTest {
        val result = repository.getMovieSeries(collectionId = 0)

        assertEquals(expected = result.first(), actual = movieSeriesTestData)
    }

    @Test
    fun getMovieSeriesImageListTest() = runTest {
        val result = repository.getMovieSeriesImageList(collectionId = 0)

        assertEquals(expected = result.first(), actual = testImageList)
    }

    @Test
    fun getMovieWatchProvidersTest() = runTest {
        val result = repository.getMovieWatchProviders(movieId = 0)

        assertEquals(expected = result.first(), actual = watchProvidersTestData)
    }
}