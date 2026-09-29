package com.cheeke.surfy.network.impl.retrofit

import com.cheeke.surfy.model.CertificationData
import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.Configuration
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.Genres
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Language
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.MovieResult
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
import com.cheeke.surfy.model.asExternalMovie
import com.cheeke.surfy.network.api.MovieRemoteDataSource
import com.cheeke.surfy.network.api.PeopleRemoteDataSource
import com.cheeke.surfy.network.api.SearchRemoteDataSource
import com.cheeke.surfy.network.api.SeriesRemoteDataSource
import com.cheeke.surfy.network.api.SettingRemoteDataSource
import com.cheeke.surfy.network.api.SurfyNetworkException
import com.cheeke.surfy.network.api.SyncRemoteDataSource
import com.cheeke.surfy.network.api.TrendingRemoteDataSource
import com.cheeke.surfy.network.api.TvRemoteDataSource
import com.cheeke.surfy.network.impl.ApiResponse
import com.cheeke.surfy.network.impl.MovieApis
import com.cheeke.surfy.network.impl.PeopleApis
import com.cheeke.surfy.network.impl.SearchApis
import com.cheeke.surfy.network.impl.SeriesApis
import com.cheeke.surfy.network.impl.SettingApis
import com.cheeke.surfy.network.impl.SyncApis
import com.cheeke.surfy.network.impl.TrendingApis
import com.cheeke.surfy.network.impl.TvApis
import com.cheeke.surfy.network.impl.model.asExternalModel
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingNetworkDataSourceImpl @Inject constructor(
    private val apis: SettingApis
) : SettingRemoteDataSource {
    override suspend fun getConfiguration(): Configuration =
        when (val response = apis.getConfiguration()) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }

    override suspend fun getCertification(): CertificationData =
        when (val response = apis.getCertification()) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }

    override suspend fun getAvailableLanguage(): List<Language> =
        when (val response = apis.getAvailableLanguage()) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }

    override suspend fun getAvailableRegion(): Regions =
        when (val response = apis.getAvailableRegion()) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }

    override suspend fun getMovieGenres(language: String): Genres =
        when (val response = apis.getMovieGenres(language = language)) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }

    override suspend fun getTvGenres(language: String): Genres =
        when (val response = apis.getTvGenres(language = language)) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }
}

@Singleton
class SearchRemoteDataSourceImpl @Inject constructor(
    private val apis: SearchApis
) : SearchRemoteDataSource {
    override suspend fun searchMulti(
        query: String,
        includeAdult: Boolean,
        language: String,
        page: Int
    ): SearchData = when (
        val response = apis.searchMulti(
            query = query,
            includeAdult = includeAdult,
            language = language,
            page = page
        )
    ) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel(mediaType = null)
    }

    override suspend fun searchMovies(
        query: String,
        includeAdult: Boolean,
        language: String,
        region: String,
        page: Int
    ): SearchData = when (
        val response = apis.searchMovies(
            query = query,
            includeAdult = includeAdult,
            language = language,
            region = region,
            page = page
        )
    ) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel(mediaType = MediaType.MOVIE)
    }

    override suspend fun searchTv(
        query: String,
        includeAdult: Boolean,
        language: String,
        region: String,
        page: Int
    ): SearchData = when (
        val response = apis.searchTv(
            query = query,
            includeAdult = includeAdult,
            language = language,
            region = region,
            page = page
        )
    ) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel(mediaType = MediaType.TV)
    }

    override suspend fun searchPeople(
        query: String,
        includeAdult: Boolean,
        language: String,
        region: String,
        page: Int
    ): SearchData = when (
        val response = apis.searchPeople(
            query = query,
            includeAdult = includeAdult,
            language = language,
            region = region,
            page = page
        )
    ) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel(mediaType = MediaType.PEOPLE)
    }

    override suspend fun searchSeries(
        query: String,
        includeAdult: Boolean,
        language: String,
        region: String,
        page: Int
    ): SearchData = when (
        val response = apis.searchMovieSeries(
            query = query,
            includeAdult = includeAdult,
            language = language,
            region = region,
            page = page
        )
    ) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel(mediaType = MediaType.SERIES)
    }

    override suspend fun getSearchKeyword(query: String, page: Int): SearchKeywordData =
        when (val response = apis.getSearchKeyword(query = query, page = page)) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }
}

@Singleton
class MovieRemoteDataSourceImpl @Inject constructor(
    private val apis: MovieApis
) : MovieRemoteDataSource {
    override suspend fun getMovie(
        id: Int,
        appendToResponse: String,
        language: String,
        includeImageLanguage: String,
        region: String
    ): Movie = when (
        val response = apis.getMovie(
            id = id,
            appendToResponse = appendToResponse,
            language = language,
            includeImageLanguage = includeImageLanguage
        )
    ) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }

    override suspend fun getSimilarMovies(
        id: Int,
        language: String,
        page: Int
    ): SimilarMedias = when (val response = apis.getSimilarMovies(id = id, language = language, page = page)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }

    override suspend fun getMovieReviews(
        movieId: Int,
        language: String,
        page: Int
    ): Reviews = when (val response = apis.getMovieReview(movieId = movieId, language = language, page = page)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }

    override suspend fun getMovieWatchProvider(movieId: Int): MovieWatchProvider = when (val response = apis.getMovieWatchProvider(movieId = movieId)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }
}

@Singleton
class PeopleRemoteDataSourceImpl @Inject constructor(
    private val apis: PeopleApis
) : PeopleRemoteDataSource {
    override suspend fun getPeopleDetail(
        personId: Int,
        appendToResponse: String,
        language: String,
        includeImageLanguage: String
    ): People = when (
        val response = apis.getPeopleDetail(
            personId = personId,
            appendToResponse = appendToResponse,
            language = language,
            includeImageLanguage = includeImageLanguage
        )
    ) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }

    override suspend fun getCombineCredits(
        personId: Int,
        language: String
    ): CombineCredits = when (val response = apis.getCombineCredits(personId = personId, language = language)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }

    override suspend fun getExternalIds(personId: Int): ExternalIds = when (val response = apis.getExternalIds(personId)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }
}

@Singleton
class TvRemoteDataSourceImpl @Inject constructor(
    private val apis: TvApis
) : TvRemoteDataSource {
    override suspend fun getTv(
        id: Int,
        language: String,
        appendToResponse: String,
        includeImageLanguage: String
    ): Tv = when (val response = apis.getTv(id = id, appendToResponse = appendToResponse, language = language, includeImageLanguage = includeImageLanguage)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }

    override suspend fun getTvSeasons(
        seriesId: Int,
        seasonNumber: Int,
        appendToResponse: String,
        language: String
    ): TvSeasons = when (val response = apis.getTvSeasons(seriesId = seriesId, seasonNumber = seasonNumber, appendToResponse = appendToResponse, language = language)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }

    override suspend fun getTvEpisode(
        seriesId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        appendToResponse: String,
        language: String
    ): TvEpisode = when (val response = apis.getTvEpisode(seriesId = seriesId, seasonNumber = seasonNumber, episodeNumber = episodeNumber, appendToResponse = appendToResponse, language = language)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }

    override suspend fun getSimilarTv(
        id: Int,
        language: String,
        page: Int
    ): SimilarMedias = when (val response = apis.getSimilarTv(id = id, language = language, page = page)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }

    override suspend fun getTvReviews(
        seriesId: Int,
        language: String,
        page: Int
    ): Reviews = when (val response = apis.getTvReview(seriesId = seriesId, language = language, page = page)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }
}

@Singleton
class SeriesRemoteDataSourceImpl @Inject constructor(
    private val apis: SeriesApis
) : SeriesRemoteDataSource {
    override suspend fun getMovieSeries(collectionId: Int, language: String): Series =
        when (val response = apis.getMovieSeries(collectionId = collectionId, language = language)) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }

    override suspend fun getSeriesImages(
        collectionId: Int,
        includeImageLanguage: String,
        language: String
    ): ImageList = when (val response = apis.getSeriesImages(collectionId = collectionId, includeImageLanguage = includeImageLanguage, language = language)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel()
    }
}

@Singleton
class SyncRemoteDataSourceImpl @Inject constructor(
    private val apis: SyncApis
) : SyncRemoteDataSource {
    override suspend fun getNowPlaying(
        language: String,
        region: String,
        page: Int
    ): List<Movie> {
        val result = mutableListOf<Movie>()
        var currentPage = page
        var totalPage: Int

        do {
            when (val response = apis.getNowPlaying(language = "$language-$region", region = region, page = currentPage)) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> {
                    currentPage = (response.data.page ?: 1) + 1
                    totalPage = response.data.totalPages ?: Int.MAX_VALUE
                    result.addAll(
                        response.data.asExternalModel().results?.map(MovieResult::asExternalMovie).orEmpty()
                    )
                }
            }
        } while (currentPage <= totalPage)

        return result.distinctBy { it.id }
    }

    override suspend fun getUpcomingMovie(
        language: String,
        region: String,
        page: Int
    ): List<Movie> {
        val result = mutableListOf<Movie>()
        var currentPage = 1
        var totalPage: Int

        do {
            when (val response = apis.getUpcomingMovie(language = "$language-$region", region = region, page = currentPage)) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> {
                    currentPage = (response.data.page ?: 1) + 1
                    totalPage = response.data.totalPages ?: Int.MAX_VALUE
                    result.addAll(
                        response.data.asExternalModel().results?.map(MovieResult::asExternalMovie).orEmpty()
                    )
                }
            }
        } while (currentPage <= totalPage)

        return result.filter { (it.releaseDate.orEmpty()) > LocalDate.now().toString() }.distinctBy { it.id }.sortedBy { it.releaseDate }
    }
}

@Singleton
class TrendingRemoteDataSourceImpl @Inject constructor(
    private val apis: TrendingApis
) : TrendingRemoteDataSource {
    override suspend fun getTrending(
        mediaType: MediaType,
        timeWindow: String,
        language: String,
        page: Int
    ): TrendingMedia = when (val response = apis.getTrending(mediaType = mediaType.label, timeWindow = timeWindow, language = language, page = page)) {
        is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
        is ApiResponse.Success -> response.data.asExternalModel(mediaType = mediaType)
    }
}