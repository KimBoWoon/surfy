package com.cheeke.surfy.network.retrofit

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
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SimilarMedias
import com.cheeke.surfy.model.TrendingMedia
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.model.asExternalMovie
import com.cheeke.surfy.network.ApiResponse
import com.cheeke.surfy.network.MovieApis
import com.cheeke.surfy.network.MovieRemoteDataSource
import com.cheeke.surfy.network.PeopleApis
import com.cheeke.surfy.network.PeopleRemoteDataSource
import com.cheeke.surfy.network.SearchApis
import com.cheeke.surfy.network.SearchRemoteDataSource
import com.cheeke.surfy.network.SeriesApis
import com.cheeke.surfy.network.SeriesRemoteDataSource
import com.cheeke.surfy.network.SettingApis
import com.cheeke.surfy.network.SettingRemoteDataSource
import com.cheeke.surfy.network.SyncApis
import com.cheeke.surfy.network.SyncRemoteDataSource
import com.cheeke.surfy.network.TrendingApis
import com.cheeke.surfy.network.TrendingRemoteDataSource
import com.cheeke.surfy.network.TvApis
import com.cheeke.surfy.network.TvRemoteDataSource
import com.cheeke.surfy.network.model.SurfyNetworkException
import com.cheeke.surfy.network.model.asExternalModel
import io.reactivex.rxjava3.core.Single
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingNetworkDataSourceImpl @Inject constructor(
    private val apis: SettingApis
) : SettingRemoteDataSource {
    override fun getConfiguration(): Single<Configuration> =
        apis.getConfiguration()
            .map {
                when (it) {
                    is ApiResponse.Failure -> throw SurfyNetworkException(throwable = it.throwable, stringRes = it.stringRes)
                    is ApiResponse.Success -> it.data.asExternalModel()
                }
            }

    override fun getCertification(): Single<CertificationData> =
        apis.getCertification()
            .map {
                when (it) {
                    is ApiResponse.Failure -> throw SurfyNetworkException(throwable = it.throwable, stringRes = it.stringRes)
                    is ApiResponse.Success -> it.data.asExternalModel()
                }
            }

    override fun getAvailableLanguage(): Single<List<Language>> =
        apis.getAvailableLanguage()
            .map {
                when (it) {
                    is ApiResponse.Failure -> throw SurfyNetworkException(throwable = it.throwable, stringRes = it.stringRes)
                    is ApiResponse.Success -> it.data.asExternalModel()
                }
            }

    override fun getAvailableRegion(): Single<Regions> =
        apis.getAvailableRegion()
            .map {
                when (it) {
                    is ApiResponse.Failure -> throw SurfyNetworkException(throwable = it.throwable, stringRes = it.stringRes)
                    is ApiResponse.Success -> it.data.asExternalModel()
                }
            }

    override fun getMovieGenres(language: String): Single<Genres> =
        apis.getGenres(media = MediaType.MOVIE.label, language = language)
            .map {
                when (it) {
                    is ApiResponse.Failure -> throw SurfyNetworkException(throwable = it.throwable, stringRes = it.stringRes)
                    is ApiResponse.Success -> it.data.asExternalModel()
                }
            }

    override fun getTvGenres(language: String): Single<Genres> =
        apis.getGenres(media = MediaType.TV.label, language = language)
            .map {
                when (it) {
                    is ApiResponse.Failure -> throw SurfyNetworkException(throwable = it.throwable, stringRes = it.stringRes)
                    is ApiResponse.Success -> it.data.asExternalModel()
                }
            }
}

@Singleton
class SearchRemoteDataSourceImpl @Inject constructor(
    private val apis: SearchApis
) : SearchRemoteDataSource {
    override fun search(
        type: SearchType,
        query: String,
        includeAdult: Boolean,
        language: String,
        page: Int
    ): Single<SearchData> = apis.search(
        type = type.type,
        query = query,
        includeAdult = includeAdult,
        language = language,
        page = page
    ).map { response ->
        when (response) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel(
                mediaType = when (type) {
                    SearchType.MULTI -> null
                    SearchType.MOVIE -> MediaType.MOVIE
                    SearchType.TV -> MediaType.TV
                    SearchType.PEOPLE -> MediaType.PEOPLE
                    SearchType.SERIES -> MediaType.SERIES
                }
            )
        }
    }

    override fun getSearchKeyword(query: String, page: Int): Single<SearchKeywordData> =
        apis.getSearchKeyword(query = query, page = page).map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }
}

@Singleton
class MovieRemoteDataSourceImpl @Inject constructor(
    private val apis: MovieApis
) : MovieRemoteDataSource {
    override fun getMovie(
        id: Int,
        appendToResponse: String,
        language: String,
        includeImageLanguage: String,
        region: String
    ): Single<Movie> = apis.getMovie(
        id = id,
        appendToResponse = appendToResponse,
        language = language,
        includeImageLanguage = includeImageLanguage
    ).map { response ->
        when (response) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }
    }

    override fun getSimilarMovies(
        id: Int,
        language: String,
        page: Int
    ): Single<SimilarMedias> = apis.getSimilarMovies(id = id, language = language, page = page)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }

    override fun getMovieReviews(
        movieId: Int,
        language: String,
        page: Int
    ): Single<Reviews> = apis.getMovieReview(movieId = movieId, language = language, page = page)
        .map { response ->
            when (response) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }
    }

    override fun getMovieWatchProvider(movieId: Int): Single<MovieWatchProvider> = apis.getMovieWatchProvider(movieId = movieId)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }
}

@Singleton
class PeopleRemoteDataSourceImpl @Inject constructor(
    private val apis: PeopleApis
) : PeopleRemoteDataSource {
    override fun getPeopleDetail(
        personId: Int,
        appendToResponse: String,
        language: String,
        includeImageLanguage: String
    ): Single<People> = apis.getPeopleDetail(
        personId = personId,
        appendToResponse = appendToResponse,
        language = language,
        includeImageLanguage = includeImageLanguage
    ).map { response ->
        when (response) {
            is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
            is ApiResponse.Success -> response.data.asExternalModel()
        }
    }

    override fun getCombineCredits(
        personId: Int,
        language: String
    ): Single<CombineCredits> = apis.getCombineCredits(personId = personId, language = language)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }

    override fun getExternalIds(personId: Int): Single<ExternalIds> = apis.getExternalIds(personId)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }
}

@Singleton
class TvRemoteDataSourceImpl @Inject constructor(
    private val apis: TvApis
) : TvRemoteDataSource {
    override fun getTv(
        id: Int,
        language: String,
        appendToResponse: String,
        includeImageLanguage: String
    ): Single<Tv> = apis.getTv(id = id, appendToResponse = appendToResponse, language = language, includeImageLanguage = includeImageLanguage)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }

    override fun getTvSeasons(
        seriesId: Int,
        seasonNumber: Int,
        appendToResponse: String,
        language: String
    ): Single<TvSeasons> = apis.getTvSeasons(seriesId = seriesId, seasonNumber = seasonNumber, appendToResponse = appendToResponse, language = language)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }

    override fun getTvEpisode(
        seriesId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        appendToResponse: String,
        language: String
    ): Single<TvEpisode> = apis.getTvEpisode(seriesId = seriesId, seasonNumber = seasonNumber, episodeNumber = episodeNumber, appendToResponse = appendToResponse, language = language)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }

    override fun getSimilarTv(
        id: Int,
        language: String,
        page: Int
    ): Single<SimilarMedias> = apis.getSimilarTv(id = id, language = language, page = page)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }

    override fun getTvReviews(
        seriesId: Int,
        language: String,
        page: Int
    ): Single<Reviews> = apis.getTvReview(seriesId = seriesId, language = language, page = page)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }
}

@Singleton
class SeriesRemoteDataSourceImpl @Inject constructor(
    private val apis: SeriesApis
) : SeriesRemoteDataSource {
    override fun getMovieSeries(collectionId: Int, language: String): Single<Series> =
        apis.getMovieSeries(collectionId = collectionId, language = language).map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }

    override fun getSeriesImages(
        collectionId: Int,
        includeImageLanguage: String,
        language: String
    ): Single<ImageList> = apis.getSeriesImages(collectionId = collectionId, includeImageLanguage = includeImageLanguage, language = language)
        .map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel()
            }
        }
}

@Singleton
class SyncRemoteDataSourceImpl @Inject constructor(
    private val apis: SyncApis
) : SyncRemoteDataSource {
    override fun getNowPlaying(
        language: String,
        region: String,
        page: Int
    ): Single<List<Movie>> = fetchAllPages(
        startPage = page,
        fetchPage = { currentPage ->
            apis.getNowPlaying(language = "$language-$region", region = region, page = currentPage)
        },
        extractPage = { data -> data.page },
        extractTotalPages = { data -> data.totalPages },
        extractMovies = { data -> data.asExternalModel().results.orEmpty().map(MovieResult::asExternalMovie) }
    ).map { movies: List<Movie> -> movies.distinctBy { movie -> movie.id } }

    override fun getUpcomingMovie(
        language: String,
        region: String,
        page: Int
    ): Single<List<Movie>> = fetchAllPages(
        startPage = page,
        fetchPage = { currentPage ->
            apis.getUpcomingMovie(language = "$language-$region", region = region, page = currentPage)
        },
        extractPage = { data -> data.page },
        extractTotalPages = { data -> data.totalPages },
        extractMovies = { data -> data.asExternalModel().results.orEmpty().map(MovieResult::asExternalMovie) }
    ).map { movies: List<Movie> ->
        movies
            .filter { movie: Movie -> movie.releaseDate.orEmpty() > LocalDate.now().toString() }
            .distinctBy { movie -> movie.id }
            .sortedBy { movie -> movie.releaseDate }
    }

    /**
     * totalPage에 도달할 때까지 다음 페이지를 재귀적으로 요청해 결과를 누적한다.
     * apis 응답 DTO 타입이 getNowPlaying/getUpcomingMovie마다 달라서, page/totalPages/movies를
     * 어떻게 꺼낼지는 호출부에서 람다로 주입받는다 (공통 상위 타입을 가정하지 않기 위함).
     */
    private fun <ResponseData> fetchAllPages(
        startPage: Int,
        fetchPage: (page: Int) -> Single<ApiResponse<ResponseData>>,
        extractPage: (data: ResponseData) -> Int?,
        extractTotalPages: (data: ResponseData) -> Int?,
        extractMovies: (data: ResponseData) -> List<Movie>
    ): Single<List<Movie>> {
        fun accumulate(
            response: ApiResponse<ResponseData>,
            accumulated: List<Movie>
        ): Single<List<Movie>> = when (response) {
            is ApiResponse.Failure -> Single.error(SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes))
            is ApiResponse.Success -> {
                val nextPage: Int = (extractPage(response.data) ?: 1) + 1
                val totalPage: Int = extractTotalPages(response.data) ?: Int.MAX_VALUE
                val accumulatedMovies: List<Movie> = accumulated + extractMovies(response.data)

                if (nextPage <= totalPage) {
                    fetchPage(nextPage).flatMap { nextResponse: ApiResponse<ResponseData> ->
                        accumulate(response = nextResponse, accumulated = accumulatedMovies)
                    }
                } else {
                    Single.just(accumulatedMovies)
                }
            }
        }

        return fetchPage(startPage).flatMap { firstResponse: ApiResponse<ResponseData> ->
            accumulate(response = firstResponse, accumulated = emptyList())
        }
    }
}

@Singleton
class TrendingRemoteDataSourceImpl @Inject constructor(
    private val apis: TrendingApis
) : TrendingRemoteDataSource {
    override fun getTrending(type: MediaType, timeWindow: String, language: String, page: Int): Single<TrendingMedia> =
        apis.getTrending(type = type.label, timeWindow = timeWindow, language = language, page = page).map { response ->
            when (response) {
                is ApiResponse.Failure -> throw SurfyNetworkException(throwable = response.throwable, stringRes = response.stringRes)
                is ApiResponse.Success -> response.data.asExternalModel(mediaType = MediaType.MOVIE)
            }
        }
}