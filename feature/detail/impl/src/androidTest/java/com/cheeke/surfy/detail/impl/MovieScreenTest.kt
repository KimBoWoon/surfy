package com.cheeke.surfy.detail.impl

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.cheeke.surfy.analytics.AnalyticsEvent
import com.cheeke.surfy.analytics.AnalyticsHelper
import com.cheeke.surfy.detail.impl.movie.GetMovieDetailUseCase
import com.cheeke.surfy.detail.movie.MovieScreen
import com.cheeke.surfy.detail.movie.MovieState
import com.cheeke.surfy.detail.movie.MovieVM
import com.cheeke.surfy.model.LocaleOption
import com.cheeke.surfy.model.PosterSize
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.SurfyAppData
import com.cheeke.surfy.network.model.SurfyNetworkException
import com.cheeke.surfy.testing.model.configurationTestData
import com.cheeke.surfy.testing.model.favoriteMovieDetailTestData
import com.cheeke.surfy.testing.model.genreListTestData
import com.cheeke.surfy.testing.model.languageListTestData
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.regionTestData
import com.cheeke.surfy.testing.model.unFavoriteMovieDetailTestData
import com.cheeke.surfy.testing.repository.TestMovieDatabaseRepository
import com.cheeke.surfy.testing.repository.TestPagingRepository
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.TestSurfyAppData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MovieScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var viewModel: MovieVM
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var testUserDataRepository: TestUserDataRepository
    private lateinit var movieDetailUseCase: GetMovieDetailUseCase
    private lateinit var testDatabaseRepository: TestMovieDatabaseRepository
    private lateinit var testDetailRepository: TestMovieDetailRepository
    private lateinit var testPagingRepository: TestPagingRepository
    private lateinit var testMovieAppDataManager: TestSurfyAppData
    private val surfyAppData = SurfyAppData(
        secureBaseUrl = configurationTestData.images?.secureBaseUrl.orEmpty(),
        movieGenres = genreListTestData.genres.orEmpty(),
        region = regionTestData.results?.map { region ->
            LocaleOption(code = region.iso31661 ?: "KR", label = region.nativeName ?: "KR", isSelected = region.iso31661 == "KR")
        }.orEmpty(),
        language = languageListTestData.map { language ->
            LocaleOption(code = language.iso6391 ?: "ko", label = language.englishName ?: "Korean", isSelected = language.iso6391 == "ko")
        },
        posterSize = configurationTestData.images?.posterSizes?.map {
            PosterSize(size = it, isSelected = it == "original")
        }.orEmpty()
    )

    @Before
    fun setup() {
//        savedStateHandle = SavedStateHandle(route = MovieNavKey(id = 0))
        savedStateHandle = SavedStateHandle().apply {
            set("id", 0)
        }
        testUserDataRepository = TestUserDataRepository()
        testDetailRepository = TestMovieDetailRepository()
        testDatabaseRepository = TestMovieDatabaseRepository()
        testPagingRepository = TestPagingRepository()
        testMovieAppDataManager = TestSurfyAppData()
        movieDetailUseCase = GetMovieDetailUseCase(
            userDataRepository = testUserDataRepository,
            repository = testDatabaseRepository,
            detailRepository = testDetailRepository
        )
        viewModel = MovieVM(
            id = 0,
            getMovieDetail = movieDetailUseCase,
            movieDataBaseRepository = testDatabaseRepository,
            pagingRepository = testPagingRepository,
            userDataRepository = testUserDataRepository,
            analyticsHelper = object : AnalyticsHelper {
                override fun logEvent(event: AnalyticsEvent) {
                    println("event: $event")
                }
            },
        )

        runBlocking {
            testMovieAppDataManager.setMovieAppData(surfyAppData)
        }
    }

    @Test
    fun detailLoadingTest() = runTest {
        composeTestRule.apply {
            setContent {
                val similarMovie = viewModel.similarMovies.collectAsLazyPagingItems()

                MovieScreen(
                    movieState = MovieState.Loading,
                    similarMovies = similarMovie,
                    goToMovie = {},
                    goToPeople = {},
                    goToBack = {},
                    goToSeries = {},
                    isCheatActive = false,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteMovie = viewModel::insertMovie,
                    deleteFavoriteMovie = viewModel::deleteMovie,
                    restart = viewModel::restart
                )
            }

            onNodeWithTag(testTag = "detailScreenLoading").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun detailErrorTest() = runTest {
        composeTestRule.apply {
            setContent {
                val similarMovie = viewModel.similarMovies.collectAsLazyPagingItems()

                MovieScreen(
                    movieState = MovieState.Error(throwable = SurfyNetworkException(throwable = Throwable("something wrong..."))),
                    similarMovies = similarMovie,
                    goToMovie = {},
                    goToPeople = {},
                    goToBack = {},
                    goToSeries = {},
                    isCheatActive = false,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteMovie = viewModel::insertMovie,
                    deleteFavoriteMovie = viewModel::deleteMovie,
                    restart = viewModel::restart
                )
            }

            onNodeWithText(text = "통신 실패").assertExists().assertIsDisplayed()
            onNodeWithText(text = "알 수 없는 문제가 있습니다.").assertExists().assertIsDisplayed()
            onNodeWithText(text = "돌아가기").assertExists().assertIsDisplayed()
            onNodeWithText(text = "재시도").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun detailSuccessTest() = runTest {
        composeTestRule.apply {
            setContent {
                val movie by viewModel.movie.collectAsStateWithLifecycle()
//                val similarMovie = viewModel.similarMovies.collectAsLazyPagingItems()
                val pagingData = PagingData.from(data = listOf(element = SimilarMedia(id = 3)))
                val flow = MutableStateFlow(value = pagingData)

                MovieScreen(
                    movieState = movie,
//                    similarMovies = similarMovie,
                    similarMovies = flow.collectAsLazyPagingItems(),
                    goToMovie = {},
                    goToPeople = {},
                    goToBack = {},
                    goToSeries = {},
                    isCheatActive = false,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteMovie = viewModel::insertMovie,
                    deleteFavoriteMovie = viewModel::deleteMovie,
                    restart = viewModel::restart
                )
            }

            onNodeWithTag(testTag = "detailScreenLoading").assertExists().assertIsDisplayed()

            testDatabaseRepository.insert(media = favoriteMovieDetailTestData)
            testDetailRepository.setMovie(detail = favoriteMovieDetailTestData)
            testDetailRepository.setMovieSeries(movieSeries = movieSeriesTestData)

            onNodeWithContentDescription(label = "favorite").assertExists().assertIsDisplayed()
            onNodeWithText(text = favoriteMovieDetailTestData.originalTitle.orEmpty()).assertExists().assertIsDisplayed()
            onNodeWithText(text = favoriteMovieDetailTestData.overview.orEmpty()).assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun seriesTest() = runTest {
        composeTestRule.apply {
            setContent {
                val movie by viewModel.movie.collectAsStateWithLifecycle()
//                val similarMovie = viewModel.similarMovies.collectAsLazyPagingItems()
                val pagingData = PagingData.from(data = listOf(element = SimilarMedia(id = 3)))
                val flow = MutableStateFlow(value = pagingData)

                MovieScreen(
                    movieState = movie,
//                    similarMovies = similarMovie,
                    similarMovies = flow.collectAsLazyPagingItems(),
                    goToMovie = {},
                    goToPeople = {},
                    goToBack = {},
                    goToSeries = {},
                    isCheatActive = false,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteMovie = viewModel::insertMovie,
                    deleteFavoriteMovie = viewModel::deleteMovie,
                    restart = viewModel::restart
                )
            }

            onNodeWithTag(testTag = "detailScreenLoading").assertExists().assertIsDisplayed()

            testDatabaseRepository.insert(media = favoriteMovieDetailTestData)
            testDetailRepository.setMovie(detail = favoriteMovieDetailTestData)
            testDetailRepository.setMovieSeries(movieSeries = movieSeriesTestData)

            onNodeWithText(text = "시리즈")
                .performScrollTo()
                .assertExists()
                .assertIsDisplayed()
                .performClick()

            onNodeWithContentDescription(label = movieSeriesTestData.posterPath!!).assertExists().assertIsDisplayed()
            onNodeWithText(text = movieSeriesTestData.title!!).assertExists().assertIsDisplayed()
            onNodeWithText(text = movieSeriesTestData.overview!!).assertExists().assertIsDisplayed()
            onNodeWithText(text = "${movieSeriesTestData.parts?.size} movies").assertExists().assertIsDisplayed()
            onNodeWithText(text = "Parts").performScrollTo().assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "seriesList").performScrollTo().assertExists().assertIsDisplayed()
            movieSeriesTestData.parts?.forEach { part ->
                onNodeWithContentDescription(label = "seriesList").performScrollToNode(matcher = hasContentDescription(value = part.posterPath!!)).assertExists().assertIsDisplayed()
                onNodeWithContentDescription(label = "seriesList").performScrollToNode(matcher = hasText(text = part.title!!)).assertExists().assertIsDisplayed()
                onNodeWithContentDescription(label = "seriesList").performScrollToNode(matcher = hasText(text = "★ ${"%.1f".format(part.voteAverage)}")).assertExists().assertIsDisplayed()
            }
        }
    }

    @Test
    fun castAndCrewTest() = runTest {
        composeTestRule.apply {
            setContent {
                val movie by viewModel.movie.collectAsStateWithLifecycle()
//                val similarMovie = viewModel.similarMovies.collectAsLazyPagingItems()
                val pagingData = PagingData.from(data = listOf(element = SimilarMedia(id = 3)))
                val flow = MutableStateFlow(value = pagingData)

                MovieScreen(
                    movieState = movie,
//                    similarMovies = similarMovie,
                    similarMovies = flow.collectAsLazyPagingItems(),
                    goToMovie = {},
                    goToPeople = {},
                    goToBack = {},
                    goToSeries = {},
                    isCheatActive = false,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteMovie = viewModel::insertMovie,
                    deleteFavoriteMovie = viewModel::deleteMovie,
                    restart = viewModel::restart
                )
            }

            onNodeWithTag(testTag = "detailScreenLoading").assertExists().assertIsDisplayed()

            testDatabaseRepository.insert(media = favoriteMovieDetailTestData)
            testDetailRepository.setMovie(detail = favoriteMovieDetailTestData)
            testDetailRepository.setMovieSeries(movieSeries = movieSeriesTestData)

            onNodeWithText(text = "배우").performScrollTo().assertExists().assertIsDisplayed()
            favoriteMovieDetailTestData.credits?.cast?.forEach { cast ->
                onNodeWithContentDescription(label = cast.profilePath.orEmpty()).assertExists().assertIsDisplayed()
                onNodeWithText(text = cast.name.orEmpty()).assertExists().assertIsDisplayed()
                onNodeWithText(text = cast.character.orEmpty()).assertExists().assertIsDisplayed()
            }
            onNodeWithText(text = "스태프").performScrollTo().assertExists().assertIsDisplayed()
            favoriteMovieDetailTestData.credits?.crew?.forEach { crew ->
                onNodeWithContentDescription(label = crew.profilePath.orEmpty()).assertExists().assertIsDisplayed()
                onNodeWithText(text = crew.name.orEmpty()).assertExists().assertIsDisplayed()
                onNodeWithText(text = crew.job.orEmpty()).assertExists().assertIsDisplayed()
                onNodeWithText(text = crew.department.orEmpty()).assertExists().assertIsDisplayed()
            }
        }
    }

    @Test
    fun movieImagesTest() = runTest {
        composeTestRule.apply {
            setContent {
                val movie by viewModel.movie.collectAsStateWithLifecycle()
//                val similarMovie = viewModel.similarMovies.collectAsLazyPagingItems()
                val pagingData = PagingData.from(data = listOf(element = SimilarMedia(id = 3)))
                val flow = MutableStateFlow(value = pagingData)

                MovieScreen(
                    movieState = movie,
//                    similarMovies = similarMovie,
                    similarMovies = flow.collectAsLazyPagingItems(),
                    goToMovie = {},
                    goToPeople = {},
                    goToBack = {},
                    goToSeries = {},
                    isCheatActive = false,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteMovie = viewModel::insertMovie,
                    deleteFavoriteMovie = viewModel::deleteMovie,
                    restart = viewModel::restart
                )
            }

            onNodeWithTag(testTag = "detailScreenLoading").assertExists().assertIsDisplayed()

            testDatabaseRepository.insert(media = favoriteMovieDetailTestData)
            testDetailRepository.setMovie(detail = favoriteMovieDetailTestData)
            testDetailRepository.setMovieSeries(movieSeries = movieSeriesTestData)

            onNodeWithText(text = "이미지").performScrollTo().assertExists().assertIsDisplayed()

            assertEquals(
                expected = favoriteMovieDetailTestData.images?.posters?.plus(element = favoriteMovieDetailTestData.images?.backdrops)?.size,
                actual = 4
            )
            onNodeWithText(text = "Backdrops").performScrollTo().assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "backdrops").performScrollTo().assertExists().assertIsDisplayed()
            (favoriteMovieDetailTestData.images?.backdrops.orEmpty()).forEach { backdrops ->
                onNodeWithContentDescription(label = backdrops.filePath.orEmpty()).assertExists().assertIsDisplayed()
            }
            onNodeWithText(text = "Posters").performScrollTo().assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "posters").performScrollTo().assertExists().assertIsDisplayed()
            (favoriteMovieDetailTestData.images?.posters.orEmpty()).forEach { posters ->
                onNodeWithContentDescription(label = posters.filePath.orEmpty()).assertExists().assertIsDisplayed()
            }
        }
    }

    @Test
    fun similarMovieTest() = runTest {
        val pagingData = PagingData.from(data = listOf(element = SimilarMedia(id = 3, posterPath = "/similarMedia.png")))
        val flow = MutableStateFlow(value = pagingData)

        composeTestRule.apply {
            setContent {
                val movie by viewModel.movie.collectAsStateWithLifecycle()
//                val similarMovie = viewModel.similarMovies.collectAsLazyPagingItems()

                MovieScreen(
                    movieState = movie,
//                    similarMovies = similarMovie,
                    similarMovies = flow.collectAsLazyPagingItems(),
                    goToMovie = {},
                    goToPeople = {},
                    goToBack = {},
                    goToSeries = {},
                    isCheatActive = false,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteMovie = viewModel::insertMovie,
                    deleteFavoriteMovie = viewModel::deleteMovie,
                    restart = viewModel::restart
                )
            }

            onNodeWithTag(testTag = "detailScreenLoading").assertExists().assertIsDisplayed()

            testDatabaseRepository.insert(media = favoriteMovieDetailTestData)
            testDetailRepository.setMovie(detail = favoriteMovieDetailTestData)
            testDetailRepository.setMovieSeries(movieSeries = movieSeriesTestData)

            onNodeWithText(text = "비슷한 작품").performScrollTo().assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "similarMovies").performScrollTo().assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "/similarMedia.png").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun addFavoriteTest() = runTest {
        composeTestRule.apply {
            setContent {
                val movie by viewModel.movie.collectAsStateWithLifecycle()
//                val similarMovie = viewModel.similarMovies.collectAsLazyPagingItems()
                val pagingData = PagingData.from(data = listOf(element = SimilarMedia(id = 3, posterPath = "/similarMedia.png")))
                val flow = MutableStateFlow(value = pagingData)

                MovieScreen(
                    movieState = movie,
//                    similarMovies = similarMovie,
                    similarMovies = flow.collectAsLazyPagingItems(),
                    goToMovie = {},
                    goToPeople = {},
                    goToBack = {},
                    goToSeries = {},
                    isCheatActive = false,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteMovie = viewModel::insertMovie,
                    deleteFavoriteMovie = viewModel::deleteMovie,
                    restart = viewModel::restart
                )
            }

            onNodeWithTag(testTag = "detailScreenLoading").assertExists().assertIsDisplayed()

            testDetailRepository.setMovie(detail = unFavoriteMovieDetailTestData)
            testDetailRepository.setMovieSeries(movieSeries = movieSeriesTestData)

            onNodeWithContentDescription(label = "unFavorite").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "favorite").assertIsNotDisplayed()
            onNodeWithText(text = favoriteMovieDetailTestData.originalTitle.orEmpty()).assertExists().assertIsDisplayed()
            onNodeWithText(text = favoriteMovieDetailTestData.overview.orEmpty()).assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "unFavorite").assertExists().assertIsDisplayed().performClick()
//            onNodeWithContentDescription(label = "unFavorite").assertIsNotDisplayed()
//            onNodeWithContentDescription(label = "favorite").assertIsDisplayed()
            assertEquals(
                expected = testDatabaseRepository.currentMovieDatabase.find { it.id == 324 }?.id,
                actual = unFavoriteMovieDetailTestData.id
            )
        }
    }

    @Test
    fun deleteFavoriteTest() = runTest {
        composeTestRule.apply {
            setContent {
                val movie by viewModel.movie.collectAsStateWithLifecycle()
//                val similarMovie = viewModel.similarMovies.collectAsLazyPagingItems()
                val pagingData = PagingData.from(data = listOf(element = SimilarMedia(id = 3, posterPath = "/similarMedia.png")))
                val flow = MutableStateFlow(value = pagingData)

                MovieScreen(
                    movieState = movie,
//                    similarMovies = similarMovie,
                    similarMovies = flow.collectAsLazyPagingItems(),
                    goToMovie = {},
                    goToPeople = {},
                    goToBack = {},
                    goToSeries = {},
                    isCheatActive = false,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteMovie = viewModel::insertMovie,
                    deleteFavoriteMovie = viewModel::deleteMovie,
                    restart = viewModel::restart
                )
            }

            onNodeWithTag(testTag = "detailScreenLoading").assertExists().assertIsDisplayed()

            testDatabaseRepository.insert(media = favoriteMovieDetailTestData)
            testDetailRepository.setMovie(detail = favoriteMovieDetailTestData)
            testDetailRepository.setMovieSeries(movieSeries = movieSeriesTestData)

            onNodeWithContentDescription(label = "favorite").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "unFavorite").assertIsNotDisplayed()
            onNodeWithText(text = favoriteMovieDetailTestData.originalTitle.orEmpty()).assertExists().assertIsDisplayed()
            onNodeWithText(text = favoriteMovieDetailTestData.overview.orEmpty()).assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "favorite").assertExists().assertIsDisplayed().performClick()
            onNodeWithContentDescription(label = "unFavorite").assertIsDisplayed()
            onNodeWithContentDescription(label = "favorite").assertIsNotDisplayed()
            assertEquals(
                expected = testDatabaseRepository.currentMovieDatabase.find { it.id == 0 },
                actual = null
            )
        }
    }
}