package com.cheeke.surfy.detail.impl.movie

import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.toEpochDayOrMax
import com.cheeke.surfy.detail.api.movie.MovieRepository
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SeriesPart
import com.cheeke.surfy.userdata.api.UserDataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetMovieDetailUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val repository: MovieRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(id: Int): Flow<Movie> =
        combine(
            getMovieWithCollection(id = id),
            repository.isFavorite(id = id)
        ) { movie, isFavorite ->
            movie.copy(isFavorite = isFavorite)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getMovieWithCollection(id: Int): Flow<Movie> =
        combine(
            repository.getData(id = id),
            userDataRepository.internalData
        ) { movie, internalData ->
            val country = movie.releases?.countries?.filter { country ->
                country.iso31661.equals(other = internalData.region, ignoreCase = true)
            }?.maxByOrNull { LocalDate.parse(it.releaseDate) }

            movie.copy(
                releaseDate = country?.releaseDate ?: movie.releaseDate,
                certification = country?.certification ?: movie.certification
            )
        }.flatMapLatest { movie ->
            movie.belongsToCollection?.id?.let { collectionId ->
                getCollection(collectionId = collectionId).map { series -> movie.copy(series = series) }
            } ?: flowOf(value = movie)
        }

    private fun getCollection(collectionId: Int): Flow<Series> =
        repository.getMovieSeries(collectionId = collectionId)
            .map { series ->
                series.copy(
                    parts = series.parts?.sortedWith(
                        comparator = compareBy<SeriesPart> { it.releaseDate.toEpochDayOrMax() }
                            .thenBy { it.title.orEmpty() }
                    )
                )
            }.catch { e ->
                Log.printStackTrace(tr = e)
                emit(value = Series())
            }
}