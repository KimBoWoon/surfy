package com.cheeke.surfy.testing

import com.cheeke.surfy.model.CertificationData
import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.Configuration
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.Genres
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Language
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.MovieWatchProvider
import com.cheeke.surfy.model.People
import com.cheeke.surfy.model.Regions
import com.cheeke.surfy.model.Reviews
import com.cheeke.surfy.model.SearchData
import com.cheeke.surfy.model.SearchKeywordData
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SimilarMedias
import com.cheeke.surfy.model.TrendingMedia
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.network.MovieRemoteDataSource
import com.cheeke.surfy.network.PeopleRemoteDataSource
import com.cheeke.surfy.network.SearchRemoteDataSource
import com.cheeke.surfy.network.SeriesRemoteDataSource
import com.cheeke.surfy.network.SettingRemoteDataSource
import com.cheeke.surfy.network.SyncRemoteDataSource
import com.cheeke.surfy.network.TrendingRemoteDataSource
import com.cheeke.surfy.network.TvRemoteDataSource
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

class TestSettingRemoteDataSource : SettingRemoteDataSource {
    override suspend fun getConfiguration(): Configuration = configurationTestData
    override suspend fun getCertification(): CertificationData = certificationTestData
    override suspend fun getAvailableLanguage(): List<Language> = languageListTestData
    override suspend fun getAvailableRegion(): Regions = regionTestData
    override suspend fun getMovieGenres(language: String): Genres = genreListTestData
    override suspend fun getTvGenres(language: String): Genres = genreListTestData
}

class TestMovieRemoteDataSource : MovieRemoteDataSource {
    override suspend fun getMovie(
        id: Int,
        appendToResponse: String,
        language: String,
        includeImageLanguage: String,
        region: String
    ): Movie = favoriteMovieDetailTestData

    override suspend fun getSimilarMovies(id: Int, language: String, page: Int): SimilarMedias =
        similarMoviesTestData

    override suspend fun getMovieReviews(
        movieId: Int,
        language: String,
        page: Int
    ): Reviews = Reviews(
        id = 0,
        page = 1,
        results = testMovieReviews,
        totalPages = 1,
        totalResults = 0
    )

    override suspend fun getMovieWatchProvider(movieId: Int): MovieWatchProvider = watchProvidersTestData
}

class TestPeopleRemoteDataSource : PeopleRemoteDataSource {
    override suspend fun getPeopleDetail(
        personId: Int,
        appendToResponse: String,
        language: String,
        includeImageLanguage: String
    ): People = peopleDetailTestData

    override suspend fun getCombineCredits(personId: Int, language: String): CombineCredits =
        combineCreditsTestData

    override suspend fun getExternalIds(personId: Int): ExternalIds = externalIdsTestData
}

class TestTvRemoteDataSource : TvRemoteDataSource {
    override suspend fun getTv(
        id: Int,
        language: String,
        appendToResponse: String,
        includeImageLanguage: String
    ): Tv = tvTestData

    override suspend fun getTvSeasons(
        seriesId: Int,
        seasonNumber: Int,
        appendToResponse: String,
        language: String
    ): TvSeasons = tvSeasonTestData

    override suspend fun getTvEpisode(
        seriesId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        appendToResponse: String,
        language: String
    ): TvEpisode = tvEpisodeTestData

    override suspend fun getSimilarTv(
        id: Int,
        language: String,
        page: Int
    ): SimilarMedias = similarTvTestData

    override suspend fun getTvReviews(
        seriesId: Int,
        language: String,
        page: Int
    ): Reviews = testMovieReview
}

class TestSeriesRemoteDataSource : SeriesRemoteDataSource {
    override suspend fun getMovieSeries(collectionId: Int, language: String): Series =
        movieSeriesTestData

    override suspend fun getSeriesImages(
        collectionId: Int,
        includeImageLanguage: String,
        language: String
    ): ImageList = testImageList
}

class TestSyncRemoteDataSource : SyncRemoteDataSource {
    override suspend fun getNowPlaying(language: String, region: String, page: Int): List<Movie> =
        nowPlayingMoviesTestData

    override suspend fun getUpcomingMovie(
        language: String,
        region: String,
        page: Int
    ): List<Movie> = upcomingMoviesTestData
}

class TestTrendingRemoteDataSource : TrendingRemoteDataSource {
    override suspend fun getTrending(
        mediaType: MediaType,
        timeWindow: String,
        language: String,
        page: Int
    ): TrendingMedia = when (mediaType) {
        MediaType.MOVIE -> testTrendingMovie
        MediaType.TV -> testTrendingTv
        MediaType.PEOPLE -> testTrendingPeople
        else -> testTrendingMovie
    }
}

class TestSearchRemoteDataSource : SearchRemoteDataSource {
    override suspend fun searchMulti(
        query: String,
        includeAdult: Boolean,
        language: String,
        page: Int
    ): SearchData = movieSearchTestData

    override suspend fun searchMovies(
        query: String,
        includeAdult: Boolean,
        language: String,
        region: String,
        page: Int
    ): SearchData = movieSearchTestData

    override suspend fun searchPeople(
        query: String,
        includeAdult: Boolean,
        language: String,
        region: String,
        page: Int
    ): SearchData = peopleSearchTestData

    override suspend fun searchTv(
        query: String,
        includeAdult: Boolean,
        language: String,
        region: String,
        page: Int
    ): SearchData = tvSearchTestData

    override suspend fun searchSeries(
        query: String,
        includeAdult: Boolean,
        language: String,
        region: String,
        page: Int
    ): SearchData = seriesSearchTestData

    override suspend fun getSearchKeyword(query: String, page: Int): SearchKeywordData = searchKeywordTest
}