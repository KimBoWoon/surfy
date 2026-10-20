package com.cheeke.surfy.detail.impl

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import androidx.paging.testing.asSnapshot
import com.cheeke.surfy.analytics.api.TestAnalyticsHelper
import com.cheeke.surfy.detail.api.TestMovieRepository
import com.cheeke.surfy.detail.impl.movie.GetMovieDetailUseCase
import com.cheeke.surfy.detail.impl.movie.MovieState
import com.cheeke.surfy.detail.impl.movie.MovieVM
import com.cheeke.surfy.detail.impl.paging.MovieReviewPagingSource
import com.cheeke.surfy.detail.impl.paging.SimilarMoviePagingSource
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.network.api.TestMovieRemoteDataSource
import com.cheeke.surfy.testing.model.favoriteMovieDetailTestData
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.similarMoviesTestData
import com.cheeke.surfy.testing.model.testMovieReviews
import com.cheeke.surfy.testing.model.unFavoriteMovieDetailTestData
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class MovieVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val repository = TestMovieRepository()
    private val testUserDataRepository = TestUserDataRepository()
    private val testAnalyticsHelper = TestAnalyticsHelper()
    private val getMovieDetailUseCase = GetMovieDetailUseCase(
        userDataRepository = testUserDataRepository,
        repository = repository
    )
    private lateinit var viewModel: MovieVM

    @Before
    fun setup() {
        viewModel = MovieVM(
            id = 0,
            movieRepository = repository,
            getMovieDetail = getMovieDetailUseCase,
            analyticsHelper = testAnalyticsHelper,
            userDataRepository = testUserDataRepository
        )
        runBlocking {
            repository.insert(media = Movie(id = 0, title = "movie_1", posterPath = "/movieImagePath.png"))
            repository.setMovieSeries(movieSeriesTestData)
        }
    }

    @Test
    fun getFavoriteMovieDetailTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.movie.collect() }

        val testPager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 7, prefetchDistance = 5),
            pagingSource = repository.getSimilarMoviePagingSource(id = 0, language = "ko", region = "KR")
        )

        assertEquals(viewModel.movie.value, MovieState.Loading)

        repository.setMovie(favoriteMovieDetailTestData)

        assertTrue(viewModel.movie.value is MovieState.Success)

        val similarMovies = viewModel.similarMovies

        assertEquals(
            expected = similarMovies.asSnapshot(),
            actual = (testPager.refresh(initialKey = 0) as PagingSource.LoadResult.Page).data
        )

        assertEquals(
            expected = viewModel.movie.value,
            actual = MovieState.Success(
                movie = favoriteMovieDetailTestData.copy(isFavorite = repository.isFavorite(id = 0).first()),
                isAutoPlayTrailer = testUserDataRepository.internalData.map { it.isAutoPlayTrailer }.first(),
            )
        )
    }

    @Test
    fun getUnFavoriteMovieDetailFlowTest() = runTest {
        viewModel = MovieVM(
            id = 324,
            getMovieDetail = getMovieDetailUseCase,
            movieRepository = repository,
            analyticsHelper = testAnalyticsHelper,
            userDataRepository = testUserDataRepository
        )
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.movie.collect() }

        val testPager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 7, prefetchDistance = 5),
            pagingSource = repository.getSimilarMoviePagingSource(id = 324, language = "ko", region = "KR")
        )

        assertEquals(viewModel.movie.value, MovieState.Loading)

        repository.setMovie(unFavoriteMovieDetailTestData)

        assertTrue(viewModel.movie.value is MovieState.Success)

        val similarMovies = viewModel.similarMovies

        assertEquals(
            similarMovies.asSnapshot(),
            (testPager.refresh(initialKey = 0) as PagingSource.LoadResult.Page).data
        )

        assertEquals(
            viewModel.movie.value,
            MovieState.Success(
                movie = unFavoriteMovieDetailTestData.copy(isFavorite = repository.isFavorite(id = 324).first()),
                isAutoPlayTrailer = testUserDataRepository.internalData.map { it.isAutoPlayTrailer }.first(),
            )
        )
    }

    @Test
    fun getSimilarMoviesTest() = runTest {
        val source = SimilarMoviePagingSource(
            apis = TestMovieRemoteDataSource(),
            id = 0,
            language = "ko",
            region = "KR"
        )

        assertEquals(
            expected = PagingSource.LoadResult.Page<Int, SimilarMedia>(
                data = similarMoviesTestData.results?.map {
                    SimilarMedia(
                        adult = it.adult,
                        releaseDate = it.releaseDate,
                        id = it.id,
                        title = it.title,
                        posterPath = it.posterPath
                    )
                }.orEmpty(),
                prevKey = null,
                nextKey = null
            ),
            actual = source.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 2,
                    placeholdersEnabled = false
                )
            ),
        )
    }

    @Test
    fun getMovieReviewsTest() = runTest {
        val source = MovieReviewPagingSource(
            apis = TestMovieRemoteDataSource(),
            id = 0,
            language = "ko",
            region = "KR"
        )

        assertEquals(
            expected = PagingSource.LoadResult.Page<Int, Review>(
                data = testMovieReviews,
                prevKey = null,
                nextKey = null
            ),
            actual = source.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 2,
                    placeholdersEnabled = false
                )
            ),
        )
    }

    @Test
    fun insertFavoriteTest() = runTest {
        viewModel = MovieVM(
            id = 23,
            getMovieDetail = getMovieDetailUseCase,
            movieRepository = repository,
            analyticsHelper = testAnalyticsHelper,
            userDataRepository = testUserDataRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.movie.collect() }
        val movie = Movie(id = 23, title = "movie_0", posterPath = "/imagePath.png")

        repository.setMovie(favoriteMovieDetailTestData.copy(id = 23))

        assertEquals(
            assertIs<MovieState.Success>(viewModel.movie.value).movie.isFavorite,
            false
        )
        viewModel.insertMovie(movie)
        assertEquals(
            assertIs<MovieState.Success>(viewModel.movie.value).movie.isFavorite,
            true
        )

//        assertEquals(
//            testDataBaseRepository.getMovies().first(),
//            emptyList()
//        )
//        viewModel.insertMovie(surfy)
//        assertEquals(
//            testDataBaseRepository.getMovies().first(),
//            listOf(surfy)
//        )
    }

    @Test
    fun deleteFavoriteTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.movie.collect { println(it) } }
        val movie = Movie(id = 0, title = "movie_1", posterPath = "/movieImagePath.png")

        repository.setMovie(favoriteMovieDetailTestData)

//        assertEquals(
//            assertIs<DetailState.Success>(viewModel.detail.value).detail?.isFavorite,
//            true
//        )
//        viewModel.deleteMovie(surfy)
//        assertEquals(
//            assertIs<DetailState.Success>(viewModel.detail.value).detail?.isFavorite,
//            false
//        )

        assertEquals(
            repository.movieDatabase.first(),
            listOf(movie)
        )
        viewModel.deleteMovie(movie)
        assertEquals(
            repository.movieDatabase.first(),
            emptyList()
        )
    }

//    @Test
//    fun restartFlowTest() = runTest {
//        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.movie.collect() }
//
//        val testPager = TestPager(
//            config = PagingConfig(pageSize = 0, initialLoadSize = 7, prefetchDistance = 5),
//            pagingSource = testPagingRepository.getSimilarMoviePagingSource(id = 0, language = "ko", region = "KR")
//        )
//
//        assertEquals(viewModel.movie.value, MovieState.Loading)
//
//        testDetailRepository.setMovie(favoriteMovieDetailTestData)
//
//        assertTrue(viewModel.movie.value is MovieState.Success)
//
//        val similarMovies = viewModel.similarMovies
//
//        assertEquals(
//            similarMovies.asSnapshot(),
//            (testPager.refresh(initialKey = 0) as PagingSource.LoadResult.Page).data
//        )
//
//        assertEquals(
//            viewModel.movie.value,
//            MovieState.Success(
//                MovieWithFavorite(
//                    movie = favoriteMovieDetailTestData,
//                    autoPlayTrailer = testUserDataRepository.internalData.map { it.isAutoPlayTrailer }.first(),
//                    isFavorite = testDataBaseRepository.isFavoriteMovie(id = 0).first()
//                )
//            )
//        )
//
//        viewModel.restart()
//
////        assertEquals(viewModel.movieInfo.value, MovieDetailState.Loading)
//
////        testDetailRepository.setMovieDetail(favoriteMovieDetailTestData)
////        testUserDataRepository.updateUserData(InternalData(), false)
////        testDataBaseRepository.insertMovie(Favorite(id = 0, title = "title_1"))
////        getMovieDetailUseCase(0)
//
////        assertEquals(viewModel.detail.value, DetailState.Success(favoriteMovieDetailTestData, movieSeriesTestData, emptyFlow()))
//    }
}