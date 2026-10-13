package com.cheeke.surfy.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.cheeke.surfy.core.data.BuildConfig
import com.cheeke.surfy.data.paging.MovieReviewPagingSource
import com.cheeke.surfy.data.paging.SimilarMoviePagingSource
import com.cheeke.surfy.database.dao.MovieDao
import com.cheeke.surfy.database.model.MovieEntity
import com.cheeke.surfy.database.model.NowPlayingMovieEntity
import com.cheeke.surfy.database.model.UpComingMovieEntity
import com.cheeke.surfy.database.model.asExternalModel
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.MovieWatchProvider
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.network.MovieRemoteDataSource
import com.cheeke.surfy.network.SeriesRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val movieApis: MovieRemoteDataSource,
    private val seriesApis: SeriesRemoteDataSource,
    private val requestOptionsProvider: DetailRequestOptionsProvider
) : MovieRepository {
    private companion object {
        const val PAGE_SIZE = 20
        const val INITIAL_LOAD_SIZE = 20
        const val ENABLE_PLACE_HOLDER = false
        val config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = INITIAL_LOAD_SIZE, enablePlaceholders = ENABLE_PLACE_HOLDER)
    }

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

    override fun isFavorite(id: Int): Flow<Boolean> = movieDao.isFavoriteMovie(id = id)

    override suspend fun insert(media: Media): Long =
        movieDao.insertOrIgnoreMovies(
            MovieEntity(
                id = media.id ?: -1,
                posterPath = media.posterPath ?: "",
                title = media.title ?: "",
                releaseDate = media.releaseDate ?: "",
                timestamp = Instant.now().toEpochMilli()
            )
        )

    override suspend fun delete(media: Media) {
        media.id?.let { id ->
            movieDao.deleteMovie(id = id)
        }
    }

    override suspend fun upsert(medias: List<Media>) {
        movieDao.upsertMovies(
            entities = medias.map { movie ->
                MovieEntity(
                    id = movie.id ?: -1,
                    posterPath = movie.posterPath ?: "",
                    title = movie.title ?: "",
                    releaseDate = movie.releaseDate ?: "",
                    timestamp = Instant.now().toEpochMilli()
                )
            }
        )
    }

    override fun getUpComingMovies(): PagingSource<Int, UpComingMovieEntity> =
        movieDao.getUpComingMovie()

    override fun getNowPlayingMovies(): PagingSource<Int, NowPlayingMovieEntity> =
        movieDao.getNowPlayingMovie()

    override suspend fun getPopularMovies(): List<Movie> =
        movieDao.getPopularMovies().map { nowPlayingMovieEntities ->
            nowPlayingMovieEntities.asExternalModel()
        }

    override suspend fun getNextWeekReleaseMovies(): List<Movie> =
        movieDao.getNextWeekReleaseMovies().map { movieEntity ->
            movieEntity.asExternalModel()
        }

    override fun getFavorite(): PagingSource<Int, MovieEntity> =
        movieDao.getFavoriteMovie()

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

    override fun getSimilarMoviePagingSource(
        id: Int,
        language: String,
        region: String
    ): Flow<PagingData<SimilarMedia>> = Pager(
        config = config,
        pagingSourceFactory = {
            SimilarMoviePagingSource(
                apis = movieApis,
                id = id,
                language = language,
                region = region
            )
        }
    ).flow

    override fun getMovieReviews(
        movieId: Int,
        language: String,
        region: String
    ): Flow<PagingData<Review>> = Pager(
        config = config,
        pagingSourceFactory = {
            MovieReviewPagingSource(
                apis = movieApis,
                id = movieId,
                language = language,
                region = region
            )
        }
    ).flow
}