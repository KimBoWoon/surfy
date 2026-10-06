package com.cheeke.surfy.network

import com.cheeke.surfy.model.defaultLanguage
import com.cheeke.surfy.model.defaultLanguageRegion
import com.cheeke.surfy.model.defaultRegion
import com.cheeke.surfy.network.model.NetworkTMDBCertificationData
import com.cheeke.surfy.network.model.NetworkTMDBCombineCredits
import com.cheeke.surfy.network.model.NetworkTMDBConfiguration
import com.cheeke.surfy.network.model.NetworkTMDBExternalIds
import com.cheeke.surfy.network.model.NetworkTMDBImageList
import com.cheeke.surfy.network.model.NetworkTMDBLanguageItem
import com.cheeke.surfy.network.model.NetworkTMDBMovie
import com.cheeke.surfy.network.model.NetworkTMDBMovieGenres
import com.cheeke.surfy.network.model.NetworkTMDBMovieList
import com.cheeke.surfy.network.model.NetworkTMDBMovieReviews
import com.cheeke.surfy.network.model.NetworkTMDBMovieSeries
import com.cheeke.surfy.network.model.NetworkTMDBMovieWatchProvider
import com.cheeke.surfy.network.model.NetworkTMDBPeopleDetail
import com.cheeke.surfy.network.model.NetworkTMDBRegion
import com.cheeke.surfy.network.model.NetworkTMDBSearch
import com.cheeke.surfy.network.model.NetworkTMDBSearchKeywordData
import com.cheeke.surfy.network.model.NetworkTMDBSimilarMedia
import com.cheeke.surfy.network.model.NetworkTMDBTrendingMedia
import com.cheeke.surfy.network.model.NetworkTMDBTv
import com.cheeke.surfy.network.model.NetworkTMDBTvEpisode
import com.cheeke.surfy.network.model.NetworkTMDBTvReviews
import com.cheeke.surfy.network.model.NetworkTMDBTvSeasons
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SettingApis {
    @GET(value = "/3/configuration")
    suspend fun getConfiguration(): ApiResponse<NetworkTMDBConfiguration>

    @GET(value = "/3/certification/movie/list")
    suspend fun getCertification(): ApiResponse<NetworkTMDBCertificationData>

    @GET(value = "/3/configuration/languages")
    suspend fun getAvailableLanguage(): ApiResponse<List<NetworkTMDBLanguageItem>>

    @GET(value = "/3/watch/providers/regions")
    suspend fun getAvailableRegion(): ApiResponse<NetworkTMDBRegion>

    @GET(value = "/3/genre/movie/list")
    suspend fun getMovieGenres(
        @Query(value = "language") language: String = defaultLanguageRegion
    ): ApiResponse<NetworkTMDBMovieGenres>

    @GET(value = "/3/genre/tv/list")
    suspend fun getTvGenres(
        @Query(value = "language") language: String = defaultLanguageRegion
    ): ApiResponse<NetworkTMDBMovieGenres>
}

interface MovieApis {
    @GET(value = "/3/movie/{movie_id}")
    suspend fun getMovie(
        @Path(value = "movie_id") id: Int,
        @Query(value = "append_to_response") appendToResponse: String = "images,videos,credits,releases,alternative_titles",
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "include_image_language") includeImageLanguage: String = defaultLanguage,
        @Query(value = "region") region: String = defaultRegion
    ): ApiResponse<NetworkTMDBMovie>

    @GET(value = "/3/movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path(value = "movie_id") id: Int,
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "page") page: Int = 1,
    ): ApiResponse<NetworkTMDBSimilarMedia>

    @GET(value = "/3/movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProvider(
        @Path(value = "movie_id") movieId: Int
    ): ApiResponse<NetworkTMDBMovieWatchProvider>

    @GET(value = "/3/movie/{movie_id}/reviews")
    suspend fun getMovieReview(
        @Path(value = "movie_id") movieId: Int,
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "page") page: Int = 1
    ): ApiResponse<NetworkTMDBMovieReviews>
}

interface PeopleApis {
    @GET(value = "/3/person/{person_id}")
    suspend fun getPeopleDetail(
        @Path(value = "person_id") personId: Int,
        @Query(value = "append_to_response") appendToResponse: String = "images, combined_credits, external_ids",
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "include_image_language") includeImageLanguage: String = defaultLanguage
    ): ApiResponse<NetworkTMDBPeopleDetail>

    @GET(value = "/3/person/{person_id}/combined_credits")
    suspend fun getCombineCredits(
        @Path(value = "person_id") personId: Int,
        @Query(value = "language") language: String = defaultLanguageRegion
    ): ApiResponse<NetworkTMDBCombineCredits>

    @GET(value = "/3/person/{person_id}/external_ids")
    suspend fun getExternalIds(
        @Path(value = "person_id") personId: Int
    ): ApiResponse<NetworkTMDBExternalIds>
}

interface TvApis {
    @GET(value = "/3/tv/{series_id}/similar")
    suspend fun getSimilarTv(
        @Path(value = "series_id") id: Int,
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "page") page: Int = 1,
    ): ApiResponse<NetworkTMDBSimilarMedia>

    @GET(value = "/3/tv/{series_id}")
    suspend fun getTv(
        @Path(value = "series_id") id: Int,
        @Query(value = "append_to_response") appendToResponse: String = "images,videos,credits,releases,alternative_titles",
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "include_image_language") includeImageLanguage: String = defaultLanguage
    ): ApiResponse<NetworkTMDBTv>

    @GET(value = "/3/tv/{series_id}/season/{season_number}")
    suspend fun getTvSeasons(
        @Path(value = "series_id") seriesId: Int,
        @Path(value = "season_number") seasonNumber: Int,
        @Query(value = "append_to_response") appendToResponse: String = "images,videos,credits,releases,alternative_titles",
        @Query(value = "language") language: String = defaultLanguageRegion
    ): ApiResponse<NetworkTMDBTvSeasons>

    @GET(value = "/3/tv/{series_id}/season/{season_number}/episode/{episode_number}")
    suspend fun getTvEpisode(
        @Path(value = "series_id") seriesId: Int,
        @Path(value = "season_number") seasonNumber: Int,
        @Path(value = "episode_number") episodeNumber: Int,
        @Query(value = "append_to_response") appendToResponse: String = "images,videos,credits,releases,alternative_titles",
        @Query(value = "language") language: String = defaultLanguageRegion
    ): ApiResponse<NetworkTMDBTvEpisode>

    @GET(value = "/3/tv/{series_id}/reviews")
    suspend fun getTvReview(
        @Path(value = "series_id") seriesId: Int,
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "page") page: Int = 1
    ): ApiResponse<NetworkTMDBTvReviews>
}

interface SeriesApis {
    @GET(value = "/3/collection/{collection_id}")
    suspend fun getMovieSeries(
        @Path(value = "collection_id") collectionId: Int,
        @Query(value = "language") language: String = defaultLanguageRegion,
    ): ApiResponse<NetworkTMDBMovieSeries>

    @GET(value = "/3/collection/{collection_id}/images")
    suspend fun getSeriesImages(
        @Path(value = "collection_id") collectionId: Int,
        @Query(value = "include_image_language") includeImageLanguage: String = "ko-KR,null",
        @Query(value = "language") language: String = defaultLanguageRegion
    ): ApiResponse<NetworkTMDBImageList>
}

interface SearchApis {
    @GET(value = "/3/search/multi")
    suspend fun searchMulti(
        @Query(value = "query") query: String,
        @Query(value = "include_adult") includeAdult: Boolean = true,
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "page") page: Int = 1
    ): ApiResponse<NetworkTMDBSearch>

    @GET(value = "/3/search/movie")
    suspend fun searchMovies(
        @Query(value = "query") query: String,
        @Query(value = "include_adult") includeAdult: Boolean = true,
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "region") region: String = defaultRegion,
        @Query(value = "page") page: Int = 1
    ): ApiResponse<NetworkTMDBSearch>

    @GET(value = "/3/search/tv")
    suspend fun searchTv(
        @Query(value = "query") query: String,
        @Query(value = "include_adult") includeAdult: Boolean = true,
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "region") region: String = defaultRegion,
        @Query(value = "page") page: Int = 1
    ): ApiResponse<NetworkTMDBSearch>

    @GET(value = "/3/search/person")
    suspend fun searchPeople(
        @Query(value = "query") query: String,
        @Query(value = "include_adult") includeAdult: Boolean = true,
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "region") region: String = defaultRegion,
        @Query(value = "page") page: Int = 1
    ): ApiResponse<NetworkTMDBSearch>

    @GET(value = "/3/search/collection")
    suspend fun searchMovieSeries(
        @Query(value = "query") query: String,
        @Query(value = "include_adult") includeAdult: Boolean = true,
        @Query(value = "page") page: Int = 1,
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "region") region: String = defaultRegion
    ): ApiResponse<NetworkTMDBSearch>

    @GET(value = "/3/search/keyword")
    suspend fun getSearchKeyword(
        @Query(value = "query") query: String,
        @Query(value = "page") page: Int
    ): ApiResponse<NetworkTMDBSearchKeywordData>
}

interface SyncApis {
    @GET(value = "/3/movie/now_playing")
    suspend fun getNowPlaying(
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "region") region: String = defaultRegion,
        @Query(value = "page") page: Int = 1
    ): ApiResponse<NetworkTMDBMovieList>

    @GET(value = "/3/movie/upcoming")
    suspend fun getUpcomingMovie(
        @Query(value = "language") language: String = defaultLanguageRegion,
        @Query(value = "region") region: String = defaultRegion,
        @Query(value = "page") page: Int = 1
    ): ApiResponse<NetworkTMDBMovieList>
}

interface TrendingApis {
    @GET("/3/trending/{mediaType}/{timeWindow}")
    suspend fun getTrending(
        @Path("mediaType") mediaType: String,
        @Path("timeWindow") timeWindow: String,
        @Query("language") language: String,
        @Query("page") page: Int = 1
    ): ApiResponse<NetworkTMDBTrendingMedia>
}