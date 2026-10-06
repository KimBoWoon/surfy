package com.cheeke.surfy.network

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
import com.cheeke.surfy.model.defaultLanguage
import com.cheeke.surfy.model.defaultLanguageRegion
import com.cheeke.surfy.model.defaultRegion

interface SettingRemoteDataSource {
    suspend fun getConfiguration(): Configuration
    suspend fun getCertification(): CertificationData
    suspend fun getAvailableLanguage(): List<Language>
    suspend fun getAvailableRegion(): Regions
    suspend fun getMovieGenres(language: String = defaultLanguageRegion): Genres
    suspend fun getTvGenres(language: String): Genres
}

interface MovieRemoteDataSource {
    suspend fun getMovie(
        id: Int,
        appendToResponse: String = "images,videos,credits,releases,keywords,alternative_titles,similar,reviews",
        language: String = defaultLanguageRegion,
        includeImageLanguage: String = defaultLanguage,
        region: String = defaultRegion
    ): Movie

    suspend fun getSimilarMovies(
        id: Int,
        language: String = defaultLanguageRegion,
        page: Int = 1
    ): SimilarMedias

    suspend fun getMovieReviews(
        movieId: Int,
        language: String = defaultLanguageRegion,
        page: Int = 1
    ): Reviews

    suspend fun getMovieWatchProvider(
        movieId: Int
    ): MovieWatchProvider
}

interface PeopleRemoteDataSource {
    suspend fun getPeopleDetail(
        personId: Int,
        appendToResponse: String = "images, combined_credits, external_ids",
        language: String = defaultLanguageRegion,
        includeImageLanguage: String = defaultLanguage
    ): People

    suspend fun getCombineCredits(
        personId: Int,
        language: String = defaultLanguageRegion
    ): CombineCredits

    suspend fun getExternalIds(
        personId: Int
    ): ExternalIds
}

interface TvRemoteDataSource {
    suspend fun getTv(
        id: Int,
        language: String,
        appendToResponse: String = "images,videos,credits,releases,keywords,alternative_titles,similar,reviews",
        includeImageLanguage: String = defaultLanguage
    ): Tv

    suspend fun getTvSeasons(
        seriesId: Int,
        seasonNumber: Int,
        appendToResponse: String = "images,videos,credits,releases,keywords,alternative_titles",
        language: String = defaultLanguageRegion
    ): TvSeasons

    suspend fun getTvEpisode(
        seriesId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        appendToResponse: String = "images,videos,credits,releases,keywords,alternative_titles",
        language: String = defaultLanguageRegion
    ): TvEpisode

    suspend fun getSimilarTv(
        id: Int,
        language: String = defaultLanguageRegion,
        page: Int = 1
    ): SimilarMedias

    suspend fun getTvReviews(
        seriesId: Int,
        language: String = defaultLanguageRegion,
        page: Int = 1
    ): Reviews
}

interface SeriesRemoteDataSource {
    suspend fun getMovieSeries(
        collectionId: Int,
        language: String = defaultLanguageRegion
    ): Series

    suspend fun getSeriesImages(
        collectionId: Int,
        includeImageLanguage: String,
        language: String
    ): ImageList
}

interface SyncRemoteDataSource {
    suspend fun getNowPlaying(
        language: String = defaultLanguageRegion,
        region: String = defaultRegion,
        page: Int = 1
    ): List<Movie>

    suspend fun getUpcomingMovie(
        language: String = defaultLanguageRegion,
        region: String = defaultRegion,
        page: Int = 1
    ): List<Movie>
}

interface TrendingRemoteDataSource {
    suspend fun getTrending(mediaType: MediaType, timeWindow: String, language: String, page: Int): TrendingMedia
}

interface SearchRemoteDataSource {
    suspend fun searchMulti(
        query: String,
        includeAdult: Boolean = true,
        language: String = defaultLanguageRegion,
        page: Int = 1
    ): SearchData

    suspend fun searchMovies(
        query: String,
        includeAdult: Boolean = true,
        language: String = defaultLanguageRegion,
        region: String = defaultRegion,
        page: Int = 1
    ): SearchData

    suspend fun searchTv(
        query: String,
        includeAdult: Boolean = true,
        language: String = defaultLanguageRegion,
        region: String = defaultRegion,
        page: Int = 1
    ): SearchData

    suspend fun searchPeople(
        query: String,
        includeAdult: Boolean = true,
        language: String = defaultLanguageRegion,
        region: String = defaultRegion,
        page: Int = 1
    ): SearchData

    suspend fun searchSeries(
        query: String,
        includeAdult: Boolean = true,
        language: String = defaultLanguageRegion,
        region: String = defaultRegion,
        page: Int = 1
    ): SearchData

    suspend fun getSearchKeyword(
        query: String,
        page: Int
    ): SearchKeywordData
}