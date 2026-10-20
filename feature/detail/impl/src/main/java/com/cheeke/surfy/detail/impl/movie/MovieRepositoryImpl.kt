package com.cheeke.surfy.detail.impl.movie

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import com.cheeke.surfy.database.impl.dao.MovieDao
import com.cheeke.surfy.database.impl.model.MovieEntity
import com.cheeke.surfy.database.impl.model.NowPlayingMovieEntity
import com.cheeke.surfy.database.impl.model.UpComingMovieEntity
import com.cheeke.surfy.database.impl.model.asExternalModel
import com.cheeke.surfy.detail.api.movie.MovieRepository
import com.cheeke.surfy.detail.impl.DetailRequestOptionsProvider
import com.cheeke.surfy.detail.impl.paging.MovieReviewPagingSource
import com.cheeke.surfy.detail.impl.paging.SimilarMoviePagingSource
import com.cheeke.surfy.feature.detail.impl.BuildConfig
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.MovieWatchProvider
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.network.api.MovieRemoteDataSource
import com.cheeke.surfy.network.api.SeriesRemoteDataSource
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant

class MovieRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val movieApis: MovieRemoteDataSource,
    private val seriesApis: SeriesRemoteDataSource,
    private val requestOptionsProvider: DetailRequestOptionsProvider
) : MovieRepository {
    init {
        if (BuildConfig.BENCHMARK) {
            CoroutineScope(context = Dispatchers.IO).launch {
                movieDao.deleteAllFavoriteMovies()
                movieDao.insertOrIgnoreMovies(
                    MovieEntity(
                        id = 575265,
                        posterPath = "/c5pDU8SW0ZbmO5jHfw7fX6keyyR.jpg",
                        title = "title",
                        releaseDate = "releaseDate",
                        timestamp = Instant.now().toEpochMilli()
                    )
                )
            }
        }
    }

    override fun getData(id: Int): Flow<Movie> = flow {
        val internalData = requestOptionsProvider.current()

        emit(value = movieApis.getMovie(id = id, language = internalData.languageTag, region = internalData.region, includeImageLanguage = internalData.includeImageLanguage))
    }

    override fun getMovieSeries(collectionId: Int): Flow<Series> = flow {
        val internalData = requestOptionsProvider.current()

        emit(value = seriesApis.getMovieSeries(collectionId = collectionId, language = internalData.languageTag))
    }

    override fun getMovieSeriesImageList(collectionId: Int): Flow<ImageList> = flow {
        val internalData = requestOptionsProvider.current()

        emit(value = seriesApis.getSeriesImages(collectionId = collectionId, includeImageLanguage = "${internalData.languageTag},null", language = internalData.languageTag))
    }

    override fun getMovieWatchProviders(movieId: Int): Flow<MovieWatchProvider> = flow {
        emit(value = movieApis.getMovieWatchProvider(movieId = movieId))
    }

    override fun getFavorite(): Flow<PagingData<Movie>> =
        Pager(config = PagingConfig(pageSize = 20)) {
            movieDao.getFavoriteMovie()
        }.flow.map { pagingData -> pagingData.map(transform = MovieEntity::asExternalModel) }

    override fun isFavorite(id: Int): Flow<Boolean> =
        movieDao.isFavoriteMovie(id = id)

    override suspend fun insert(media: Movie): Long =
        movieDao.insertOrIgnoreMovies(
            movie = MovieEntity(
                id = media.id ?: -1,
                posterPath = media.posterPath ?: "",
                title = media.title ?: "",
                releaseDate = media.releaseDate ?: "",
                timestamp = Instant.now().toEpochMilli()
            )
        )

    override suspend fun delete(media: Movie) {
        media.id?.let { id ->
            movieDao.deleteMovie(id = id)
        }
    }

    override suspend fun upsert(medias: List<Movie>) {
        movieDao.upsertMovies(entities = medias.map { it.asExternalModel() })
    }

    override fun getUpComingMovies(): Flow<PagingData<Movie>> =
        Pager(config = PagingConfig(pageSize = 20)) {
            movieDao.getUpComingMovie()
        }.flow.map { pagingData -> pagingData.map(transform = UpComingMovieEntity::asExternalModel) }

    override fun getPopularMovies(): Flow<List<Movie>> =
        movieDao.getPopularMovies().map { nowPlayingMovieEntities ->
            nowPlayingMovieEntities.map { nowPlayingMovieEntity -> nowPlayingMovieEntity.asExternalModel() }
        }

    override fun getNowPlayingMovies(): Flow<PagingData<Movie>> =
        Pager(config = PagingConfig(pageSize = 20)) {
            movieDao.getNowPlayingMovie()
        }.flow.map { pagingData -> pagingData.map(transform = NowPlayingMovieEntity::asExternalModel) }

    override suspend fun getNextWeekReleaseMovies(): List<Movie> =
        movieDao.getNextWeekReleaseMovies().map { it.asExternalModel() }

    override fun getSimilarMoviePagingSource(
        id: Int,
        language: String,
        region: String
    ): PagingSource<Int, SimilarMedia> = SimilarMoviePagingSource(
        apis = movieApis,
        id = id,
        language = language,
        region = region
    )

    override fun getMovieReviews(
        movieId: Int,
        language: String,
        region: String
    ): PagingSource<Int, Review> = MovieReviewPagingSource(
        apis = movieApis,
        id = movieId,
        language = language,
        region = region
    )
}