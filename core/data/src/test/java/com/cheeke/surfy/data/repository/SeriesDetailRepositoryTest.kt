package com.cheeke.surfy.data.repository

import com.cheeke.surfy.core.datastore.InternalDataPreferences
import com.cheeke.surfy.datastore.InternalDataSource
import com.cheeke.surfy.datastore_test.InMemoryDataStore
import com.cheeke.surfy.testing.TestSeriesRemoteDataSource
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.testImageList
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SeriesDetailRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var movieApis: TestSeriesRemoteDataSource
    private lateinit var datastore: InternalDataSource
    private lateinit var repository: SeriesDetailRepositoryImpl
    private lateinit var userDataRepository: UserDataRepository

    @Before
    fun setup() {
        movieApis = TestSeriesRemoteDataSource()
        datastore = InternalDataSource(
            datastore = InMemoryDataStore(initialValue = InternalDataPreferences.getDefaultInstance())
        )
        userDataRepository = TestUserDataRepository()
        repository = SeriesDetailRepositoryImpl(
            apis = movieApis,
            requestOptionsProvider = DetailRequestOptionsProvider(userdata = userDataRepository),
        )
    }

    @Test
    fun getTvDetailTest() = runTest {
        val result = repository.getData(id = 0)

        assertEquals(expected = result.first(), actual = movieSeriesTestData)
    }

    @Test
    fun getMovieSeriesImageListTest() = runTest {
        val result = repository.getMovieSeriesImageList(collectionId = 0)

        assertEquals(expected = result.first(), actual = testImageList)
    }
}