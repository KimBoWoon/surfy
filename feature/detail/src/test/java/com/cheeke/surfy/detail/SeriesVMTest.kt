package com.cheeke.surfy.detail

import com.cheeke.surfy.detail.series.SeriesState
import com.cheeke.surfy.detail.series.SeriesVM
import com.cheeke.surfy.domain.GetSeriesDetailUseCase
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.testImageList
import com.cheeke.surfy.testing.repository.TestSeriesRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.testing.utils.TestAnalyticsHelper
import com.cheeke.surfy.testing.utils.TestMovieAppDataManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SeriesVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var testSeriesRepository: TestSeriesRepository
    private lateinit var testMovieAppDataManager: TestMovieAppDataManager
    private lateinit var testAnalyticsHelper: TestAnalyticsHelper
    private lateinit var getSeriesDetailUseCase: GetSeriesDetailUseCase
    private lateinit var seriesVM: SeriesVM

    @Before
    fun setup() {
        testSeriesRepository = TestSeriesRepository()
        testMovieAppDataManager = TestMovieAppDataManager()
        testAnalyticsHelper = TestAnalyticsHelper()
        getSeriesDetailUseCase = GetSeriesDetailUseCase(seriesRepository = testSeriesRepository)

        seriesVM = SeriesVM(
            id = 0,
            getSeriesDetailUseCase = getSeriesDetailUseCase,
            analyticsHelper = testAnalyticsHelper
        )
    }

    @Test
    fun getSeriesTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { seriesVM.series.collect() }

        assertEquals(SeriesState.Loading, seriesVM.series.value)

        testSeriesRepository.setMovieSeries(movieSeriesTestData)
        testSeriesRepository.setImageList(imageList = testImageList)

        assertEquals(
            SeriesState.Success(series = movieSeriesTestData, imageList = testImageList),
            seriesVM.series.value
        )
    }
}