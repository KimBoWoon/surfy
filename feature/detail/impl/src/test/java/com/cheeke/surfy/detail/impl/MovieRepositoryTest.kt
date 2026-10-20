package com.cheeke.surfy.detail.impl

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.cheeke.surfy.database.impl.model.MovieEntity
import com.cheeke.surfy.database.impl.model.NowPlayingMovieEntity
import com.cheeke.surfy.database.impl.model.asExternalModel
import com.cheeke.surfy.detail.api.TestMovieDao
import com.cheeke.surfy.detail.impl.movie.MovieRepositoryImpl
import com.cheeke.surfy.network.api.TestMovieRemoteDataSource
import com.cheeke.surfy.network.api.TestSeriesRemoteDataSource
import com.cheeke.surfy.testing.model.favoriteMovieDetailTestData
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.testImageList
import com.cheeke.surfy.testing.model.watchProvidersTestData
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class MovieRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val movieDao = TestMovieDao()
    private val movieApis = TestMovieRemoteDataSource()
    private val seriesApis = TestSeriesRemoteDataSource()
    private val userdataRepository = TestUserDataRepository()
    private val requestOptionsProvider = DetailRequestOptionsProvider(userdataRepository = userdataRepository)
    private val repository = MovieRepositoryImpl(movieDao = movieDao, movieApis = movieApis, seriesApis = seriesApis, requestOptionsProvider = requestOptionsProvider)
    private val movie = MovieEntity(id = 0, title = "title_0", posterPath = "posterPath_0", releaseDate = "releaseDate", timestamp = 0)

    @Test
    fun getFavoriteTest() = runTest {
        val favoriteMovieSource = movieDao.getFavoriteMovie()
        val favoriteMoviePager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getFavoriteMovie()
        )

        assertEquals(
            expected = favoriteMoviePager.refresh(initialKey = 0),
            actual = favoriteMovieSource.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        )
    }

    @Test
    fun isFavorite() = runTest {
        assertEquals(
            expected = movieDao.isFavoriteMovie(id = 0).first(),
            actual = false
        )

        movieDao.insertOrIgnoreMovies(movie = movie)

        assertEquals(
            expected = movieDao.isFavoriteMovie(id = 0).first(),
            actual = true
        )
    }

    @Test
    fun insert() = runTest {
        val favoritePagerBeforeInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getFavoriteMovie()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        movieDao.insertOrIgnoreMovies(movie = movie)

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getFavoriteMovie()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = movie, actual = result.data.first())
    }

    @Test
    fun delete() = runTest {
        val favoritePagerBeforeInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getFavoriteMovie()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        movieDao.insertOrIgnoreMovies(movie = movie)

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getFavoriteMovie()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = movie, actual = result.data.first())

        movieDao.deleteMovie(id = movie.id)

        val favoritePagerAfterDelete = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getFavoriteMovie()
        )

        result = favoritePagerAfterDelete.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)
    }

    @Test
    fun upsert() = runTest {
        val favoritePagerBeforeInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getFavoriteMovie()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        movieDao.upsertMovies(entities = listOf(movie))

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getFavoriteMovie()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = movie, actual = result.data.first())
    }

    @Test
    fun getUpComingMovies() = runTest {
        val favoriteMovieSource = movieDao.getUpComingMovie()
        val favoriteMoviePager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getUpComingMovie()
        )

        assertEquals(
            expected = favoriteMoviePager.refresh(initialKey = 0),
            actual = favoriteMovieSource.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        )
    }

    @Test
    fun getNowPlayingMovies() = runTest {
        val favoriteMovieSource = movieDao.getNowPlayingMovie()
        val favoriteMoviePager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = movieDao.getNowPlayingMovie()
        )

        assertEquals(
            expected = favoriteMoviePager.refresh(initialKey = 0),
            actual = favoriteMovieSource.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        )
    }

    @Test
    fun getPopularMoviesTest() = runTest {
        val movie = NowPlayingMovieEntity(
            id = 4,
            posterPath = "/Movie_4.png",
            title = "movie_4",
            releaseDate = LocalDate.now().toString(),
            voteAverage = 8.6f,
            voteCount = 673
        )

        assertEquals(
            expected = repository.getPopularMovies().first(),
            actual = emptyList()
        )

        movieDao.upsertNowPlayingMovie(entities = listOf(movie))

        assertEquals(
            expected = repository.getPopularMovies().first(),
            actual = listOf(movie.asExternalModel())
        )
    }

    @Test
    fun getNextWeekReleaseMovies() = runTest {
        val movie1 = MovieEntity(id = 0, releaseDate = LocalDate.now().plusDays(1).toString(), posterPath = "", timestamp = 0L, title = "")
        val movie2 = MovieEntity(id = 1, releaseDate = LocalDate.now().minusDays(1).toString(), posterPath = "", timestamp = 0L, title = "")
        val movie3 = MovieEntity(id = 2, releaseDate = LocalDate.now().plusDays(2).toString(), posterPath = "", timestamp = 0L, title = "")

        movieDao.insertOrIgnoreMovies(movie = movie1)
        movieDao.insertOrIgnoreMovies(movie = movie2)
        movieDao.insertOrIgnoreMovies(movie = movie3)

        assertEquals(
            expected = movieDao.getNextWeekReleaseMovies(),
            actual = listOf(movie1, movie3)
        )
    }

    @Test
    fun getMovieDetailTest() = runTest {
        val result = repository.getData(id = 0)

        assertEquals(expected = result.first(), actual = favoriteMovieDetailTestData)
    }

    @Test
    fun getMovieSeriesTest() = runTest {
        val result = repository.getMovieSeries(collectionId = 0)

        assertEquals(expected = result.first(), actual = movieSeriesTestData)
    }

    @Test
    fun getMovieSeriesImageListTest() = runTest {
        val result = repository.getMovieSeriesImageList(collectionId = 0)

        assertEquals(expected = result.first(), actual = testImageList)
    }

    @Test
    fun getMovieWatchProvidersTest() = runTest {
        val result = repository.getMovieWatchProviders(movieId = 0)

        assertEquals(expected = result.first(), actual = watchProvidersTestData)
    }
}