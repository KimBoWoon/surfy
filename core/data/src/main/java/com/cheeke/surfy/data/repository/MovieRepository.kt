package com.cheeke.surfy.data.repository

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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

interface MovieRepository : DataBaseRepository<MovieEntity>, DetailRepository<Movie> {
    fun getUpComingMovies(): PagingSource<Int, UpComingMovieEntity>
    fun getNowPlayingMovies(): PagingSource<Int, NowPlayingMovieEntity>
    fun getPopularMovies(): Observable<List<Movie>>
    fun getNextWeekReleaseMovies(): Single<List<Movie>>
    override fun getData(id: Int): Single<Movie>
    fun getMovieSeries(collectionId: Int): Single<Series>
    fun getMovieSeriesImageList(collectionId: Int): Single<ImageList>
    fun getMovieWatchProviders(movieId: Int): Single<MovieWatchProvider>
    fun getSimilarMoviePagingSource(id: Int, language: String, region: String): PagingSource<Int, SimilarMedia>
    fun getMovieReviews(movieId: Int, language: String, region: String): PagingSource<Int, Review>
}

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

    override fun getData(id: Int): Single<Movie> = requestOptionsProvider.current()
        .flatMap {
            movieApis.getMovie(
                id = id,
                language = it.languageTag,
                region = it.region,
                includeImageLanguage = it.includeImageLanguage
            )
        }

    override fun getMovieSeries(collectionId: Int): Single<Series> = requestOptionsProvider.current()
        .flatMap {
            seriesApis.getMovieSeries(
                collectionId = collectionId,
                language = it.languageTag
            )
        }

    override fun getMovieSeriesImageList(collectionId: Int): Single<ImageList> = requestOptionsProvider.current()
        .flatMap {
            seriesApis.getSeriesImages(
                collectionId = collectionId,
                includeImageLanguage = it.localizedImageLanguage,
                language = it.languageTag
            )
        }

    override fun getMovieWatchProviders(movieId: Int): Single<MovieWatchProvider> =
        movieApis.getMovieWatchProvider(movieId = movieId)

    override fun isFavorite(id: Int): Observable<Boolean> = movieDao.isFavoriteMovie(id = id)

    override fun insert(media: Media): Single<Long> =
        movieDao.insertOrIgnoreMovies(
            MovieEntity(
                id = media.id ?: -1,
                posterPath = media.posterPath ?: "",
                title = media.title ?: "",
                releaseDate = media.releaseDate ?: "",
                timestamp = Instant.now().toEpochMilli()
            )
        ).subscribeOn(Schedulers.io())

    override fun delete(media: Media): Completable {
        return media.id?.let { id ->
            return movieDao.deleteMovie(id = id).subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun upsert(medias: List<Media>): Completable {
        return movieDao.upsertMovies(
            entities = medias.map { movie ->
                MovieEntity(
                    id = movie.id ?: -1,
                    posterPath = movie.posterPath ?: "",
                    title = movie.title ?: "",
                    releaseDate = movie.releaseDate ?: "",
                    timestamp = Instant.now().toEpochMilli()
                )
            }
        ).subscribeOn(Schedulers.io())
    }

    override fun getUpComingMovies(): PagingSource<Int, UpComingMovieEntity> =
        movieDao.getUpComingMovie()

    override fun getNowPlayingMovies(): PagingSource<Int, NowPlayingMovieEntity> =
        movieDao.getNowPlayingMovie()

    override fun getPopularMovies(): Observable<List<Movie>> =
        movieDao.getPopularMovies().map { nowPlayingMovieEntities ->
            nowPlayingMovieEntities.map(transform = NowPlayingMovieEntity::asExternalModel)
        }.subscribeOn(Schedulers.io())

    override fun getNextWeekReleaseMovies(): Single<List<Movie>> =
        movieDao.getNextWeekReleaseMovies().map { movieEntity ->
            movieEntity.map(transform = MovieEntity::asExternalModel)
        }.subscribeOn(Schedulers.io())

    override fun getFavorite(): PagingSource<Int, MovieEntity> =
        movieDao.getFavoriteMovie()

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