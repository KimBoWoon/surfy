package com.cheeke.surfy.domain

import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.toEpochDayOrMax
import com.cheeke.surfy.data.repository.MovieRepository
import com.cheeke.surfy.data.repository.UserDataRepository
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SeriesPart
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate
import javax.inject.Inject

class GetMovieDetailUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val movieRepository: MovieRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(id: Int): Flowable<Movie> =
        Flowable.combineLatest(
            movieRepository.getData(id = id).toFlowable(),
            userDataRepository.internalData,
            movieRepository.isFavorite(id = id).toFlowable(BackpressureStrategy.LATEST)
        ) { movie, internalData, isFavorite ->
            val country = movie.releases?.countries?.filter { country ->
                country.iso31661.equals(other = internalData.region, ignoreCase = true)
            }?.maxByOrNull { LocalDate.parse(it.releaseDate) }

            movie.copy(
                releaseDate = country?.releaseDate ?: movie.releaseDate,
                certification = country?.certification ?: movie.certification,
                isFavorite = isFavorite
            )
        }.flatMap { movie ->
            movie.belongsToCollection?.id?.let { collectionId ->
                getCollection(collectionId = collectionId).map { series -> movie.copy(series = series) }.toFlowable()
            } ?: Flowable.just(movie)
        }

    private fun getCollection(collectionId: Int): Single<Series> =
        movieRepository.getMovieSeries(collectionId = collectionId)
            .map { series ->
                series.copy(
                    parts = series.parts?.sortedWith(
                        comparator = compareBy<SeriesPart> { it.releaseDate.toEpochDayOrMax() }
                            .thenBy { it.title.orEmpty() }
                    )
                )
            }.onErrorReturn { e ->
                Log.printStackTrace(tr = e)
                Series()
            }
}