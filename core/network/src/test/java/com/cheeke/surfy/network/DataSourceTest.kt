package com.cheeke.surfy.network

import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.testing.TestMovieRemoteDataSource
import com.cheeke.surfy.testing.TestPeopleRemoteDataSource
import com.cheeke.surfy.testing.TestSearchRemoteDataSource
import com.cheeke.surfy.testing.TestSeriesRemoteDataSource
import com.cheeke.surfy.testing.TestSettingRemoteDataSource
import com.cheeke.surfy.testing.TestSyncRemoteDataSource
import com.cheeke.surfy.testing.TestTrendingRemoteDataSource
import com.cheeke.surfy.testing.TestTvRemoteDataSource
import com.cheeke.surfy.testing.model.certificationTestData
import com.cheeke.surfy.testing.model.combineCreditsTestData
import com.cheeke.surfy.testing.model.configurationTestData
import com.cheeke.surfy.testing.model.externalIdsTestData
import com.cheeke.surfy.testing.model.favoriteMovieDetailTestData
import com.cheeke.surfy.testing.model.genreListTestData
import com.cheeke.surfy.testing.model.languageListTestData
import com.cheeke.surfy.testing.model.movieSearchTestData
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.nowPlayingMoviesTestData
import com.cheeke.surfy.testing.model.peopleDetailTestData
import com.cheeke.surfy.testing.model.peopleSearchTestData
import com.cheeke.surfy.testing.model.regionTestData
import com.cheeke.surfy.testing.model.searchKeywordTest
import com.cheeke.surfy.testing.model.seriesSearchTestData
import com.cheeke.surfy.testing.model.similarMoviesTestData
import com.cheeke.surfy.testing.model.similarTvTestData
import com.cheeke.surfy.testing.model.testImageList
import com.cheeke.surfy.testing.model.testMovieReview
import com.cheeke.surfy.testing.model.testMovieReviews
import com.cheeke.surfy.testing.model.testTrendingMovie
import com.cheeke.surfy.testing.model.testTrendingPeople
import com.cheeke.surfy.testing.model.testTrendingTv
import com.cheeke.surfy.testing.model.tvEpisodeTestData
import com.cheeke.surfy.testing.model.tvSearchTestData
import com.cheeke.surfy.testing.model.tvSeasonTestData
import com.cheeke.surfy.testing.model.tvTestData
import com.cheeke.surfy.testing.model.upcomingMoviesTestData
import com.cheeke.surfy.testing.model.watchProvidersTestData
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SettingRemoteDataTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val datasource = TestSettingRemoteDataSource()

    @Test
    fun getConfigurationTest() = runTest {
        val result = datasource.getConfiguration()

        assertEquals(result, configurationTestData)
    }

    @Test
    fun getCertificationTest() = runTest {
        val result = datasource.getCertification()

        assertEquals(result, certificationTestData)
    }

    @Test
    fun getAvailableRegionTest() = runTest {
        val result = datasource.getAvailableRegion()

        assertEquals(result, regionTestData)
    }

    @Test
    fun getAvailableLanguageTest() = runTest {
        val result = datasource.getAvailableLanguage()

        assertEquals(result, languageListTestData)
    }

    @Test
    fun getMovieGenresTest() = runTest {
        val result = datasource.getMovieGenres()

        assertEquals(result, genreListTestData)
    }

    @Test
    fun getTvGenresTest() = runTest {
        val result = datasource.getTvGenres(language = "ko-KR")

        assertEquals(result, genreListTestData)
    }
}

class SyncRemoteDataTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val datasource = TestSyncRemoteDataSource()

    @Test
    fun getNowPlayingTest() = runTest {
        val result = datasource.getNowPlaying()

        assertEquals(result, nowPlayingMoviesTestData)
    }

    @Test
    fun getUpcomingMovieTest() = runTest {
        val result = datasource.getUpcomingMovie()

        assertEquals(result, upcomingMoviesTestData)
    }
}

class MovieRemoteDataTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val datasource = TestMovieRemoteDataSource()

    @Test
    fun getMovieDetailTest() = runTest {
        val result = datasource.getMovie(0)

        assertEquals(result, favoriteMovieDetailTestData)
    }

    @Test
    fun getSimilarMovieTest() = runTest {
        val result = datasource.getSimilarMovies(0)

        assertEquals(result, similarMoviesTestData)
    }

    @Test
    fun getMovieReviewTest() = runTest {
        val result = datasource.getMovieReviews(0)

        assertEquals(result.results, testMovieReviews)
    }

    @Test
    fun getWatchProviderTest() = runTest {
        val result = datasource.getMovieWatchProvider(0)

        assertEquals(result, watchProvidersTestData)
    }
}

class PeopleRemoteDataTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val datasource = TestPeopleRemoteDataSource()

    @Test
    fun getPeopleDetailTest() = runTest {
        val result = datasource.getPeopleDetail(0)

        assertEquals(result, peopleDetailTestData)
    }

    @Test
    fun getCombineCreditsTest() = runTest {
        val result = datasource.getCombineCredits(0)

        assertEquals(result, combineCreditsTestData)
    }

    @Test
    fun getExternalIdsTest() = runTest {
        val result = datasource.getExternalIds(0)

        assertEquals(result, externalIdsTestData)
    }
}

class TvRemoteDataTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val datasource = TestTvRemoteDataSource()

    @Test
    fun getTvTest() = runTest {
        val result = datasource.getTv(id = 0, language = "ko-KR")

        assertEquals(result, tvTestData)
    }

    @Test
    fun getTvSeasonTest() = runTest {
        val result = datasource.getTvSeasons(seriesId = 0, seasonNumber = 0, language = "ko-KR")

        assertEquals(result, tvSeasonTestData)
    }

    @Test
    fun getTvEpisodeTest() = runTest {
        val result = datasource.getTvEpisode(seriesId = 0, seasonNumber = 0, episodeNumber = 0)

        assertEquals(result, tvEpisodeTestData)
    }

    @Test
    fun getSimilarTvTest() = runTest {
        val result = datasource.getSimilarTv(id = 0, language = "ko-KR")

        assertEquals(result, similarTvTestData)
    }

    @Test
    fun getExternalIdsTest() = runTest {
        val result = datasource.getTvReviews(seriesId = 0, language = "ko-KR")

        assertEquals(result, testMovieReview)
    }
}

class SeriesRemoteDataTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val datasource = TestSeriesRemoteDataSource()

    @Test
    fun getSeriesTest() = runTest {
        val result = datasource.getMovieSeries(collectionId = 0, language = "ko-KR")

        assertEquals(result, movieSeriesTestData)
    }

    @Test
    fun getExternalIdsTest() = runTest {
        val result = datasource.getSeriesImages(collectionId = 0, includeImageLanguage = "ko,null", language = "ko-KR")

        assertEquals(result, testImageList)
    }
}

class TrendingRemoteDataTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val datasource = TestTrendingRemoteDataSource()

    @Test
    fun getTrendingMovieTest() = runTest {
        val result = datasource.getTrending(mediaType = MediaType.MOVIE, timeWindow = "day", language = "ko-KR", page = 1)

        assertEquals(result, testTrendingMovie)
    }

    @Test
    fun getTrendingPeopleTest() = runTest {
        val result = datasource.getTrending(mediaType = MediaType.PEOPLE, timeWindow = "day", language = "ko-KR", page = 1)

        assertEquals(result, testTrendingPeople)
    }

    @Test
    fun getTrendingTvTest() = runTest {
        val result = datasource.getTrending(mediaType = MediaType.TV, timeWindow = "day", language = "ko-KR", page = 1)

        assertEquals(result, testTrendingTv)
    }
}

class SearchRemoteDataTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val datasource = TestSearchRemoteDataSource()

    @Test
    fun searchMoviesTest() = runTest {
        val result = datasource.searchMovies("미션")

        assertEquals(result, movieSearchTestData)
    }

    @Test
    fun searchPeopleTest() = runTest {
        val result = datasource.searchPeople("톰 크루즈")

        assertEquals(result, peopleSearchTestData)
    }

    @Test
    fun searchTvTest() = runTest {
        val result = datasource.searchTv("톰 크루즈")

        assertEquals(result, tvSearchTestData)
    }

    @Test
    fun searchSeriesTest() = runTest {
        val result = datasource.searchSeries("톰 크루즈")

        assertEquals(result, seriesSearchTestData)
    }

    @Test
    fun searchMultiTest() = runTest {
        val result = datasource.searchMulti("톰 크루즈")

        assertEquals(result, movieSearchTestData)
    }

    @Test
    fun getSearchPeopleTest() = runTest {
        val result = datasource.getSearchKeyword("톰 크루즈", 1)

        assertEquals(result, searchKeywordTest)
    }
}