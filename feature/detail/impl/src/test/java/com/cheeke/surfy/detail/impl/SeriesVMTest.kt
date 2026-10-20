package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.analytics.api.TestAnalyticsHelper
import com.cheeke.surfy.datamanager.api.TestSurfyAppData
import com.cheeke.surfy.detail.api.TestSeriesRepository
import com.cheeke.surfy.detail.impl.series.GetSeriesDetailUseCase
import com.cheeke.surfy.detail.impl.series.SeriesState
import com.cheeke.surfy.detail.impl.series.SeriesVM
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.testImageList
import com.cheeke.surfy.testing.utils.MainDispatcherRule
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
    private lateinit var testDetailRepository: TestSeriesRepository
    private lateinit var testMovieAppDataManager: TestSurfyAppData
    private lateinit var testAnalyticsHelper: TestAnalyticsHelper
    private lateinit var getSeriesDetailUseCase: GetSeriesDetailUseCase
    private lateinit var seriesVM: SeriesVM

    @Before
    fun setup() {
        testDetailRepository = TestSeriesRepository()
        testMovieAppDataManager = TestSurfyAppData()
        testAnalyticsHelper = TestAnalyticsHelper()
        getSeriesDetailUseCase = GetSeriesDetailUseCase(repository = testDetailRepository)

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

        testDetailRepository.setMovieSeries(movieSeriesTestData)
        testDetailRepository.setImageList(imageList = testImageList)

        assertEquals(
            SeriesState.Success(series = movieSeriesTestData, imageList = testImageList),
            seriesVM.series.value
        )
    }
}