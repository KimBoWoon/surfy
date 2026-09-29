package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.datamanager.api.TestSurfyAppData
import com.cheeke.surfy.detail.api.TestMovieDatabaseRepository
import com.cheeke.surfy.detail.impl.movie.GetMovieDetailUseCase
import com.cheeke.surfy.model.LocaleOption
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.PosterSize
import com.cheeke.surfy.model.SurfyAppData
import com.cheeke.surfy.testing.model.configurationTestData
import com.cheeke.surfy.testing.model.favoriteMovieDetailTestData
import com.cheeke.surfy.testing.model.genreListTestData
import com.cheeke.surfy.testing.model.languageListTestData
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.regionTestData
import com.cheeke.surfy.testing.model.unFavoriteMovieDetailTestData
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GetMovieDetailUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var detailRepository: TestMovieDetailRepository
    private lateinit var movieDataBaseRepository: TestMovieDatabaseRepository
    private lateinit var userDataRepository: TestUserDataRepository
    private lateinit var getMovieDetailUseCase: GetMovieDetailUseCase
    private lateinit var movieAppDataRepository: TestSurfyAppData

    @Before
    fun setup() {
        detailRepository = TestMovieDetailRepository()
        movieDataBaseRepository = TestMovieDatabaseRepository()
        userDataRepository = TestUserDataRepository()
        movieAppDataRepository = TestSurfyAppData()
        getMovieDetailUseCase = GetMovieDetailUseCase(
            userDataRepository = userDataRepository,
            detailRepository = detailRepository,
            movieDataBaseRepository = movieDataBaseRepository
        )
        runBlocking {
            movieDataBaseRepository.insert(media = Movie(id = 23))
            movieAppDataRepository.setMovieAppData(
                SurfyAppData(
                    secureBaseUrl = configurationTestData.images?.secureBaseUrl.orEmpty(),
                    movieGenres = genreListTestData.genres.orEmpty(),
                    region = regionTestData.results?.map { LocaleOption(code = it.iso31661.orEmpty(), label = it.englishName.orEmpty()) }.orEmpty(),
                    language = languageListTestData.map { LocaleOption(code = it.iso6391.orEmpty(), label = it.englishName.orEmpty()) },
                    posterSize = configurationTestData.images?.posterSizes?.map {
                        PosterSize(size = it)
                    }.orEmpty()
                )
            )
        }
    }

    @Test
    fun getMovieDetailTest() = runTest {
        detailRepository.setMovie(favoriteMovieDetailTestData)
        detailRepository.setMovieSeries(movieSeriesTestData)

        val result = getMovieDetailUseCase(id = 0).first()

        assertEquals(
            expected = result,
            actual = favoriteMovieDetailTestData.copy(isFavorite = false)
        )

        assertEquals(
            expected = result.series,
            actual = movieSeriesTestData
        )

        assertEquals(
            expected = result.isFavorite,
            actual = movieDataBaseRepository.isFavorite(id = 0).first()
        )
    }

    @Test
    fun getFavoriteMovieDetailTest() = runTest {
        detailRepository.setMovie(unFavoriteMovieDetailTestData)
        detailRepository.setMovieSeries(movieSeriesTestData)
        movieDataBaseRepository.insert(
            media = Movie(
                id = unFavoriteMovieDetailTestData.id,
                title = unFavoriteMovieDetailTestData.title,
                posterPath = unFavoriteMovieDetailTestData.posterPath
            )
        )

        val result = getMovieDetailUseCase(id = 324).first()

        assertEquals(
            expected = result,
            actual = unFavoriteMovieDetailTestData.copy(isFavorite = true)
        )

        assertEquals(
            expected = result.series,
            actual = movieSeriesTestData
        )

        assertEquals(
            expected = result.isFavorite,
            actual = movieDataBaseRepository.isFavorite(id = 324).first()
        )
    }
}