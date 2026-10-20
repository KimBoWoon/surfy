package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.detail.impl.series.SeriesRepositoryImpl
import com.cheeke.surfy.network.api.TestSeriesRemoteDataSource
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.testImageList
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SeriesRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var movieApis: TestSeriesRemoteDataSource
    private lateinit var userdata: TestUserDataRepository
    private lateinit var repository: SeriesRepositoryImpl

    @Before
    fun setup() {
        movieApis = TestSeriesRemoteDataSource()
        userdata = TestUserDataRepository()
        repository = SeriesRepositoryImpl(
            apis = movieApis,
            requestOptionsProvider = DetailRequestOptionsProvider(userdataRepository = userdata),
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