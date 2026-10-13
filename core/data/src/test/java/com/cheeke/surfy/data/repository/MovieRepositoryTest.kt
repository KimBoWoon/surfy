package com.cheeke.surfy.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.cheeke.surfy.core.datastore.InternalDataPreferences
import com.cheeke.surfy.data.testdouble.TestMovieDao
import com.cheeke.surfy.database.model.MovieEntity
import com.cheeke.surfy.database.model.NowPlayingMovieEntity
import com.cheeke.surfy.database.model.asExternalModel
import com.cheeke.surfy.datastore.InternalDataSource
import com.cheeke.surfy.datastore_test.InMemoryDataStore
import com.cheeke.surfy.testing.TestMovieRemoteDataSource
import com.cheeke.surfy.testing.TestSeriesRemoteDataSource
import com.cheeke.surfy.testing.model.favoriteMovieDetailTestData
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.testImageList
import com.cheeke.surfy.testing.model.watchProvidersTestData
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class MovieRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var movieApis: TestMovieRemoteDataSource
    private lateinit var seriesApis: TestSeriesRemoteDataSource
    private lateinit var datastore: InternalDataSource
    private lateinit var userDataRepository: UserDataRepository
    private lateinit var movieDao: TestMovieDao
    private lateinit var repository: MovieRepositoryImpl
    private val movie = MovieEntity(id = 0, title = "title_0", posterPath = "posterPath_0", releaseDate = "releaseDate", timestamp = 0)

    @Before
    fun setup() {
        movieDao = TestMovieDao()
        movieApis = TestMovieRemoteDataSource()
        seriesApis = TestSeriesRemoteDataSource()
        datastore = InternalDataSource(
            datastore = InMemoryDataStore(initialValue = InternalDataPreferences.getDefaultInstance())
        )
        userDataRepository = TestUserDataRepository()
        repository = MovieRepositoryImpl(
            requestOptionsProvider = DetailRequestOptionsProvider(userdata = userDataRepository),
            movieApis = movieApis,
            seriesApis = seriesApis,
            movieDao = movieDao
        )
    }

    @Test
    fun getFavoriteTest() = runTest {
        val favoriteMovieSource = repository.getFavorite()
        val favoriteMoviePager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
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
            pagingSource = repository.getFavorite()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        movieDao.insertOrIgnoreMovies(movie = movie)

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = movie, actual = result.data.first())
    }

    @Test
    fun delete() = runTest {
        val favoritePagerBeforeInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        movieDao.insertOrIgnoreMovies(movie = movie)

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = movie, actual = result.data.first())

        movieDao.deleteMovie(id = movie.id)

        val favoritePagerAfterDelete = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterDelete.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)
    }

    @Test
    fun upsert() = runTest {
        val favoritePagerBeforeInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        movieDao.upsertMovies(entities = listOf(movie))

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = movie, actual = result.data.first())
    }

    @Test
    fun getUpComingMovies() = runTest {
        val favoriteMovieSource = repository.getUpComingMovies()
        val favoriteMoviePager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getUpComingMovies()
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
        val favoriteMovieSource = repository.getNowPlayingMovies()
        val favoriteMoviePager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getNowPlayingMovies()
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
            expected = repository.getPopularMovies(),
            actual = emptyList()
        )

        movieDao.upsertNowPlayingMovie(entities = listOf(movie))

        assertEquals(
            expected = repository.getPopularMovies(),
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

//    @Test
//    fun similarMoviePagingTest() = runTest {
//        val pagingSource = repository.getSimilarMoviePagingSource(id = 0, language = "ko", region = "KR")
//
//        val actual = pagingSource.load(
//            params = PagingSource.LoadParams.Refresh(
//                key = 1,
//                loadSize = 100,
//                placeholdersEnabled = false
//            )
//        )
//
//        assertTrue(actual is PagingSource.LoadResult.Page)
//
//        val page = actual as PagingSource.LoadResult.Page
//
//        assertEquals(expected = similarMoviesTestData.results.orEmpty(), actual = page.data)
//        assertEquals(expected = 0, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
//
//    @Test
//    fun movieReviewPagingTest() = runTest {
//        val pagingSource = repository.getMovieReviews(movieId = 0, language = "ko", region = "KR")
//
//        val actual = pagingSource.load(
//            params = PagingSource.LoadParams.Refresh(
//                key = null,
//                loadSize = 6,
//                placeholdersEnabled = false
//            )
//        )
//
//        assertTrue(actual is PagingSource.LoadResult.Page)
//
//        val page = actual as PagingSource.LoadResult.Page
//
//        assertEquals(expected = testMovieReviews, actual = page.data)
//        assertEquals(expected = null, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
}