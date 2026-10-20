package com.cheeke.surfy.detail.impl.series

import com.cheeke.surfy.common.toEpochDayOrMax
import com.cheeke.surfy.detail.api.series.SeriesRepository
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SeriesPart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSeriesDetailUseCase @Inject constructor(
    private val repository: SeriesRepository
) {
    operator fun invoke(id: Int): Flow<SeriesWithImages> = combine(
        repository.getData(id = id)
            .map { series ->
                series.copy(
                    parts = series.parts?.sortedWith(
                        comparator = compareBy<SeriesPart> { it.releaseDate.toEpochDayOrMax() }
                            .thenBy { it.title.orEmpty() }
                    )
                )
            },
        repository.getMovieSeriesImageList(collectionId = id)
    ) { series, imageList ->
        SeriesWithImages(series = series, imageList = imageList)
    }
}

data class SeriesWithImages(
    val series: Series,
    val imageList: ImageList
)