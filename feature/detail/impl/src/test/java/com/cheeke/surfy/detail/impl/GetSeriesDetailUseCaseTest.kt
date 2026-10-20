package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.detail.impl.series.GetSeriesDetailUseCase
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SeriesPart
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GetSeriesDetailUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var detailRepository: TestSeriesDetailRepository
    private lateinit var getSeriesDetailUseCase: GetSeriesDetailUseCase

    @Before
    fun setup() {
        detailRepository = TestSeriesDetailRepository()
        getSeriesDetailUseCase = GetSeriesDetailUseCase(repository = detailRepository)
    }

    @Test
    fun sortSeriesPartTest() = runTest {
        val series = Series(
            id = 1,
            parts = listOf(
                SeriesPart(id = 2, title = "Beta", releaseDate = "2024-01-01"),
                SeriesPart(id = 3, title = "Alpha", releaseDate = "2024-01-01"),
                SeriesPart(id = 4, title = "NoDate", releaseDate = null),
                SeriesPart(id = 1, title = "Oldest", releaseDate = "2023-12-31")
            )
        )
        val imageList = ImageList(id = 100)

        detailRepository.setMovieSeries(movieSeries = series)
        detailRepository.setImageList(imageList = imageList)

        val result = getSeriesDetailUseCase(id = 1).first()

        assertEquals(
            listOf(1, 3, 2, 4),
            result.series.parts?.map { it.id }
        )
    }

    @Test
    fun seriesAndImageListTest() = runTest {
        val series = Series(id = 10)
        val imageList = ImageList(id = 20)

        detailRepository.setMovieSeries(movieSeries = series)
        detailRepository.setImageList(imageList = imageList)

        val result = getSeriesDetailUseCase(id = 10).first()

        assertEquals(series.id, result.series.id)
        assertEquals(imageList, result.imageList)
    }
}