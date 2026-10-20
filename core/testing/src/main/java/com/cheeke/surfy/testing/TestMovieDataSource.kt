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
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SimilarMedias
import com.cheeke.surfy.model.TrendingMedia
import com.cheeke.surfy.model.TrendingMediaResult
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
import com.cheeke.surfy.testing.model.movieSeriesTestData
import com.cheeke.surfy.testing.model.nowPlayingMoviesTestData
import com.cheeke.surfy.testing.model.peopleDetailTestData
import com.cheeke.surfy.testing.model.regionTestData
import com.cheeke.surfy.testing.model.searchKeywordTest
import com.cheeke.surfy.testing.model.similarMoviesTestData
import com.cheeke.surfy.testing.model.similarTvTestData
import com.cheeke.surfy.testing.model.testImageList
import com.cheeke.surfy.testing.model.testMovieReview
import com.cheeke.surfy.testing.model.testTvReviews
import com.cheeke.surfy.testing.model.tvEpisodeTestData
import com.cheeke.surfy.testing.model.tvSeasonTestData
import com.cheeke.surfy.testing.model.tvTestData
import com.cheeke.surfy.testing.model.upcomingMoviesTestData
import com.cheeke.surfy.testing.model.watchProvidersTestData
import java.util.concurrent.CopyOnWriteArrayList

class TestSettingRemoteDataSource : SettingRemoteDataSource {
    override suspend fun getConfiguration(): Configuration = configurationTestData
    override suspend fun getCertification(): CertificationData = certificationTestData
    override suspend fun getAvailableLanguage(): List<Language> = languageListTestData
    override suspend fun getAvailableRegion(): Regions = regionTestData
    override suspend fun getMovieGenres(language: String): Genres = genreListTestData
    override suspend fun getTvGenres(language: String): Genres = genreListTestData
}

class TestMovieRemoteDataSource : MovieRemoteDataSource {
    data class Request(
        val movieId: Int,
        val language: String,
        val page: Int
    )

    val similarMovieRequests: MutableList<Request> = CopyOnWriteArrayList()
    val reviewRequests: MutableList<Request> = CopyOnWriteArrayList()
    var similarMoviesResponse: SimilarMedias = similarMoviesTestData
    var reviewsResponse: Reviews = testMovieReview

    override suspend fun getSimilarMovies(
        movieId: Int, language: String, page: Int
    ): SimilarMedias {
        similarMovieRequests += Request(movieId = movieId, language = language, page = page)
        return similarMoviesResponse
    }

    override suspend fun getMovieReviews(
        movieId: Int,
        language: String,
        page: Int
    ): Reviews {
        reviewRequests += Request(movieId = movieId, language = language, page = page)
        return reviewsResponse
    }

    override suspend fun getMovie(
        id: Int,
        appendToResponse: String,
        language: String,
        includeImageLanguage: String,
        region: String
    ): Movie = favoriteMovieDetailTestData

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
    data class Request(
        val seriesId: Int,
        val language: String,
        val page: Int
    )

    val similarTvRequests: MutableList<Request> = CopyOnWriteArrayList()
    val reviewRequests: MutableList<Request> = CopyOnWriteArrayList()
    var similarTvResponse: SimilarMedias = similarTvTestData
    var reviewsResponse: Reviews = testTvReviews

    override suspend fun getSimilarTv(
        id: Int,
        language: String,
        page: Int
    ): SimilarMedias {
        similarTvRequests += Request(seriesId = id, language = language, page = page)
        return similarTvResponse
    }

    override suspend fun getTvReviews(
        seriesId: Int,
        language: String,
        page: Int
    ): Reviews {
        reviewRequests += Request(seriesId = seriesId, language = language, page = page)
        return reviewsResponse
    }

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

//    override suspend fun getSimilarTv(
//        id: Int,
//        language: String,
//        page: Int
//    ): SimilarMedias = similarTvTestData
//
//    override suspend fun getTvReviews(
//        seriesId: Int,
//        language: String,
//        page: Int
//    ): Reviews = testMovieReview
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
    data class Request(
        val mediaType: MediaType,
        val timeWindow: String,
        val language: String,
        val page: Int
    )

    val requests = mutableListOf<Request>()
    var totalPages: Int = 3

    override suspend fun getTrending(
        mediaType: MediaType,
        timeWindow: String,
        language: String,
        page: Int
    ): TrendingMedia {
        requests += Request(
            mediaType = mediaType,
            timeWindow = timeWindow,
            language = language,
            page = page
        )
        return createTrendingResponse(mediaType = mediaType, page = page, totalPages = totalPages)
    }

    private fun createTrendingResponse(mediaType: MediaType, page: Int, totalPages: Int): TrendingMedia =
        TrendingMedia(
            page = page,
            results = List(size = PAGE_SIZE) { index ->
                when (mediaType) {
                    MediaType.NONE -> {
                        TrendingMediaResult(
                            id = (page - 1) * PAGE_SIZE + index,
                            adult = true,
                            backdropPath = "backdropPath_$index",
                            genreIds = emptyList(),
                            originalLanguage = "originalLanguage_$index",
                            originalTitle = "originalTitle_$index",
                            title = "trendingMovieTitle_$index"
                        )
                    }
                    MediaType.MOVIE -> {
                        TrendingMediaResult(
                            id = (page - 1) * PAGE_SIZE + index,
                            adult = true,
                            backdropPath = "backdropPath_$index",
                            genreIds = emptyList(),
                            originalLanguage = "originalLanguage_$index",
                            originalTitle = "originalTitle_$index",
                            title = "trendingMovieTitle_$index"
                        )
                    }
                    MediaType.TV -> {
                        TrendingMediaResult(
                            id = (page - 1) * PAGE_SIZE + index,
                            adult = true,
                            backdropPath = "backdropPath_$index",
                            genreIds = emptyList(),
                            originalLanguage = "originalLanguage_$index",
                            originalTitle = "originalTitle_$index",
                            title = "trendingTvTitle_$index"
                        )
                    }
                    MediaType.PEOPLE -> {
                        TrendingMediaResult(
                            id = (page - 1) * PAGE_SIZE + index,
                            adult = true,
                            posterPath = "posterPath_$index",
                            originalTitle = "originalTitle_$index",
                            title = "trendingPeopleTitle_$index"
                        )
                    }
                    MediaType.SERIES -> {
                        TrendingMediaResult(
                            id = (page - 1) * PAGE_SIZE + index,
                            adult = true,
                            backdropPath = "backdropPath_$index",
                            genreIds = emptyList(),
                            originalLanguage = "originalLanguage_$index",
                            originalTitle = "originalTitle_$index",
                            title = "trendingMovieTitle_$index"
                        )
                    }
                }
            },
            totalPages = totalPages,
            totalResults = 0
        )

    private companion object {
        const val PAGE_SIZE = 21
    }
}

class TestSearchRemoteDataSource : SearchRemoteDataSource {
    data class SearchRequest(
        val type: SearchType,
        val query: String,
        val language: String,
        val isAdult: Boolean,
        val page: Int
    )

    data class KeywordRequest(
        val query: String,
        val page: Int
    )

    val searchRequests: MutableList<SearchRequest> = CopyOnWriteArrayList()
    val keywordRequests: MutableList<KeywordRequest> = CopyOnWriteArrayList()
    var searchResponses: Map<SearchType, SearchData> = emptyMap()
    var keywordResponse: SearchKeywordData = searchKeywordTest

    override suspend fun search(
        type: SearchType,
        query: String,
        language: String,
        isAdult: Boolean,
        page: Int
    ): SearchData {
        searchRequests += SearchRequest(
            type = type,
            query = query,
            language = language,
            isAdult = isAdult,
            page = page
        )
        return searchResponses.getValue(key = type)
    }

    override suspend fun getSearchKeyword(
        query: String,
        page: Int
    ): SearchKeywordData {
        keywordRequests += KeywordRequest(query = query, page = page)
        return keywordResponse
    }
}