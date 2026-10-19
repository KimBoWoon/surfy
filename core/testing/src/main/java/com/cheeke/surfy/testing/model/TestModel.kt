package com.cheeke.surfy.testing.model

import com.cheeke.surfy.database.model.KeywordEntity
import com.cheeke.surfy.database.model.NowPlayingMovieEntity
import com.cheeke.surfy.database.model.UpComingMovieEntity
import com.cheeke.surfy.model.AlternativeTitle
import com.cheeke.surfy.model.AlternativeTitles
import com.cheeke.surfy.model.BelongsToCollection
import com.cheeke.surfy.model.Cast
import com.cheeke.surfy.model.Certification
import com.cheeke.surfy.model.CertificationData
import com.cheeke.surfy.model.CertificationMap
import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.CombineCreditsCast
import com.cheeke.surfy.model.CombineCreditsCrew
import com.cheeke.surfy.model.Configuration
import com.cheeke.surfy.model.Country
import com.cheeke.surfy.model.Credits
import com.cheeke.surfy.model.Crew
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.Genre
import com.cheeke.surfy.model.Genres
import com.cheeke.surfy.model.Image
import com.cheeke.surfy.model.ImageInfo
import com.cheeke.surfy.model.ImageList
import com.cheeke.surfy.model.Images
import com.cheeke.surfy.model.Keyword
import com.cheeke.surfy.model.Keywords
import com.cheeke.surfy.model.Language
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.MovieWatchProvider
import com.cheeke.surfy.model.MovieWatchProviderResult
import com.cheeke.surfy.model.People
import com.cheeke.surfy.model.ProductionCompany
import com.cheeke.surfy.model.ProductionCountry
import com.cheeke.surfy.model.Region
import com.cheeke.surfy.model.Regions
import com.cheeke.surfy.model.Releases
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.ReviewAuthorDetails
import com.cheeke.surfy.model.Reviews
import com.cheeke.surfy.model.SearchData
import com.cheeke.surfy.model.SearchKeyword
import com.cheeke.surfy.model.SearchKeywordData
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.SeriesPart
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.SimilarMedias
import com.cheeke.surfy.model.SpokenLanguage
import com.cheeke.surfy.model.TrendingMedia
import com.cheeke.surfy.model.TrendingMediaResult
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.model.VideoInfo
import com.cheeke.surfy.model.Videos

val certificationTestData = CertificationData(
    certifications = CertificationMap(
        certifications = mapOf(
            "en" to listOf(
                Certification(certification = "1", meaning = "1", order = 0),
                Certification(certification = "2", meaning = "2", order = 1)
            ),
            "ko" to listOf(
                Certification(certification = "1", meaning = "1", order = 0),
                Certification(certification = "2", meaning = "2", order = 1)
            )
        )
    )
)

val languageListTestData = listOf(
    Language(englishName = "en", iso6391 = "en", name = "en", isSelected = false),
    Language(englishName = "ko", iso6391 = "ko", name = "ko", isSelected = true)
)

val regionTestData = Regions(
    results = listOf(
        Region(englishName = "en", iso31661 = "en", nativeName = "en", isSelected = false),
        Region(englishName = "ko", iso31661 = "ko", nativeName = "ko", isSelected = true)
    )
)

val genreListTestData = Genres(
    genres = listOf(
        Genre(id = 0, name = "name1"),
        Genre(id = 1, name = "name2"),
        Genre(id = 2, name = "Action"),
        Genre(id = 3, name = "name3"),
        Genre(id = 4, name = "name4"),
        Genre(id = 5, name = "name5")
    )
)

val configurationTestData = Configuration(
    changeKeys = listOf(),
    images = ImageInfo(
        baseUrl = "https://",
        secureBaseUrl = "https://",
        posterSizes = listOf("w92", "w182", "w342", "w540", "w720", "original")
    )
)

val testImageList = ImageList(
    backdrops = emptyList(),
    posters = emptyList(),
    id = 0
)

val movieSearchTestData = SearchData(
    page = 1,
    results = (1..100).map {
        Movie(
            genres = listOf(Genre(id = it)),
            releaseDate = "releaseDate_$it",
            title = "title_$it",
            adult = true,
            id = it,
            posterPath = "/imagePath_$it.png"
        ) as Media
    },
    totalPages = 1,
    totalResults = 100
)

val peopleSearchTestData = SearchData(
    page = 1,
    results = (1..100).map {
        Movie(
            genres = listOf(Genre(id = it)),
            releaseDate = "releaseDate_$it",
            title = "title_$it",
            adult = true,
            id = it,
            posterPath = "/imagePath_$it.png"
        ) as Media
    },
    totalPages = 1,
    totalResults = 100
)

val seriesSearchTestData = SearchData(
    page = 1,
    results = (0 .. 5).map {
        Series(
            id = it,
            title = "name_$it",
            posterPath = "/imagePath_$it.png"
        )
    },
    totalPages = 1,
    totalResults = 5
)

val tvSearchTestData = SearchData(
    page = 1,
    results = (0 .. 5).map {
        Tv(
            id = it,
            title = "name_$it",
            posterPath = "/imagePath_$it.png",
            adult = true
        )
    },
    totalPages = 1,
    totalResults = 5
)

val similarMoviesTestData = SimilarMedias(
    page = 1,
    results = (1..100).map {
        SimilarMedia(
//            genres = listOf(Genre(id = it)),
            releaseDate = "releaseDate_$it",
            title = "title_$it",
            adult = true,
            id = it,
            posterPath = "/imagePath_$it.png"
        )
    },
    totalPages = 1,
    totalResults = 100
)

val similarTvTestData = SimilarMedias(
    page = 1,
    results = (0..10).map {
        SimilarMedia(
//            genres = listOf(Genre(id = it)),
            firstAirDate = "firstAirDate_$it",
            name = "title_$it",
            adult = true,
            id = it,
            posterPath = "/imagePath_$it.png"
        )
    },
    totalPages = 1,
    totalResults = 101
)

val nowPlayingMoviesTestData =
    listOf(Movie(id = 0, title = "nowPlaying_1", posterPath = "/nowPlaying_1.png"))
val upcomingMoviesTestData =
    listOf(Movie(id = 0, title = "upcomingMovie_1", posterPath = "/upcomingMovie_1.png"))
val movieSeriesTestData = Series(
    backdropPath = "/backdropPath.png",
    id = 896,
    title = "movieSeries",
    overview = "movieSeriesOverview",
    parts = listOf(
        SeriesPart(id = 0, title = "movieSeries_0", releaseDate = "2024-09-23", overview = "movieSeries_0_overview", posterPath = "/movieSeriesPosterPath_1.png", voteAverage = 8.72f),
        SeriesPart(id = 1, title = "movieSeries_1", releaseDate = "2024-09-24", overview = "movieSeries_1_overview", posterPath = "/movieSeriesPosterPath_2.png", voteAverage = 7.8f),
        SeriesPart(adult = true, id = 2, title = "movieSeries_2", releaseDate = "2024-09-25", overview = "movieSeries_2_overview", posterPath = "/movieSeriesPosterPath_3.jpg", voteAverage = 6.54f),
        SeriesPart(adult = false, id = 3, title = "movieSeries_3", releaseDate = "2024-09-26", overview = "movieSeries_3_overview", posterPath = "/movieSeriesPosterPath_3.png", voteAverage = 7.59f)
    ),
    posterPath = "/movieSeriesPosterPath.png"
)
val testRecommendedKeyword = (0 .. 5).map {
    SearchKeyword(id = it, name = "mission$it")
}
val searchKeywordTest = SearchKeywordData(
    page = 1,
    results = listOf(
        SearchKeyword(id = 0, name = "mission0"),
        SearchKeyword(id = 1, name = "mission1"),
        SearchKeyword(id = 2, name = "mission2"),
        SearchKeyword(id = 3, name = "mission3"),
        SearchKeyword(id = 4, name = "mission4"),
        SearchKeyword(id = 5, name = "mission5")
    ),
    totalPages = 1,
    totalResults = 5
)

val testMovieReviewList = (0..5).map {
    Review(
        id = it.toString(),
        author = "author$it",
        authorDetails = null,
        content = "content$it",
        createdAt = "2018-07-05T13:22:41.754Z",
        updatedAt = "2021-06-23T15:58:10.199Z",
        url = "/url$it.jpg",
    )
}

val testMovieReview = Reviews(
    id = 0,
    page = 1,
    results = testMovieReviewList,
    totalPages = 1,
    totalResults = 0
)

val testTvReviewList = (0..10).map {
    Review(
        author = "author_$it",
        content = "content_$it",
        id = "id_$it",
        url = "url_$it",
        authorDetails = ReviewAuthorDetails(
            name = "name_$it",
            username = "username_$it",
            avatarPath = "avatarPath_$it",
            rating = it.toFloat()
        ),
        createdAt = "createdAt_$it",
        updatedAt = "updatedAt_$it",
    )
}

val testTvReviews = Reviews(
    id = 0,
    page = 1,
    results = testTvReviewList,
    totalPages = 1,
    totalResults = 0
)

val testTrendingMovie = TrendingMedia(
    page = 1,
    results = (0..20).map {
        TrendingMediaResult(
            adult = true,
            backdropPath = "backdropPath_$it",
            genreIds = emptyList(),
            id = it,
            originalLanguage = "originalLanguage_$it",
            originalTitle = "originalTitle_$it",
            title = "trendingMovieTitle_$it"
        )
    },
    totalPages = 3,
    totalResults = 0
)

val testTrendingPeople = TrendingMedia(
    page = 1,
    results = (0..20).map {
        TrendingMediaResult(
            adult = true,
            posterPath = "posterPath_$it",
            id = it,
            originalTitle = "originalTitle_$it",
            title = "trendingPeopleTitle_$it"
        )
    },
    totalPages = 3,
    totalResults = 0
)

val testTrendingTv = TrendingMedia(
    page = 1,
    results = (0..20).map {
        TrendingMediaResult(
            adult = true,
            backdropPath = "backdropPath_$it",
            genreIds = emptyList(),
            id = it,
            originalLanguage = "originalLanguage_$it",
            originalTitle = "originalTitle_$it",
            title = "trendingTvTitle_$it"
        )
    },
    totalPages = 3,
    totalResults = 0
)

val favoriteMovieDetailTestData = Movie(
    adult = true,
    alternativeTitles = AlternativeTitles(titles = listOf(AlternativeTitle(iso31661 = "KR", title = "title_KR", type = "type_kr"))),
    backdropPath = "backdropPath",
    belongsToCollection = BelongsToCollection(backdropPath = "/backdropPath.png", id = 896, name = "name", posterPath = "/posterPath.png"),
    budget = 30_000_000_000,
    credits = Credits(cast = listOf(Cast(castId = 0, name = "cast_1", profilePath = "/cast.png", character = "character")), crew = listOf(Crew(id = 0, name = "crew_1", profilePath = "/crew.png", department = "department", job = "job"))),
    genres = listOf(Genre(id = 0, name = "genre")),
    homepage = "homepage",
    id = 0,
    images = Images(backdrops = listOf(Image(filePath = "/backdrops_1.png")), logos = listOf(), posters = listOf(Image(filePath = "/poster_1.png"), Image(filePath = "/poster_2.png"), Image(filePath = "/poster_3.png"))),
    imdbId = "imdbId",
    keywords = Keywords(keywords = listOf(Keyword(id = 0, name = "name"))),
    originCountry = listOf("originCountry"),
    originalLanguage = "originalLanguage",
    originalTitle = "originalTitle",
    overview = "overview",
    popularity = 3.5,
    posterPath = "https://original/posterPath.png",
    productionCompanies = listOf(ProductionCompany(id = 0, logoPath = "https://original/logoPath.png", name = "name", originCountry = "originCountry")),
    productionCountries = listOf(ProductionCountry(iso31661 = "KR", name = "name")),
    releaseDate = "2025-12-25",
    releases = Releases(countries = listOf(Country(certification = "15", descriptors = listOf("descriptors"), iso31661 = "KR", primary = true, releaseDate = "2025-12-25"))),
    revenue = 30_000_000_000,
    runtime = 240,
    spokenLanguages = listOf(SpokenLanguage(englishName = "englishName", iso6391 = "ko", name = "name")),
    status = "Release",
    tagline = "tagline",
    title = "title",
    video = true,
    videos = Videos(listOf(VideoInfo(id = "", iso31661 = "", iso6391 = "", key = "", name = "", official = true, publishedAt = "", site = "", size = 19, type = ""))),
    voteAverage = 3.5f,
    voteCount = 203,
    certification = "15",
    series = movieSeriesTestData,
    isFavorite = true
)

val unFavoriteMovieDetailTestData = Movie(
    adult = true,
    alternativeTitles = AlternativeTitles(titles = listOf(AlternativeTitle(iso31661 = "KR", title = "title_KR", type = "type_kr"))),
    backdropPath = "backdropPath",
    belongsToCollection = BelongsToCollection(backdropPath = "https://original/backdropPath.png", id = 0, name = "name", posterPath = "https://original/posterPath.png"),
    budget = 30_000_000_000,
    credits = Credits(cast = listOf(), crew = listOf()),
    genres = listOf(Genre(id = 0, name = "genre")),
    homepage = "homepage",
    id = 324,
    images = Images(backdrops = listOf(), logos = listOf(), posters = listOf()),
    imdbId = "imdbId",
    keywords = Keywords(keywords = listOf(Keyword(id = 0, name = "name"))),
    originCountry = listOf("originCountry"),
    originalLanguage = "originalLanguage",
    originalTitle = "originalTitle",
    overview = "overview",
    popularity = 3.5,
    posterPath = "https://original/posterPath.png",
    productionCompanies = listOf(ProductionCompany(id = 0, logoPath = "https://original/logoPath.png", name = "name", originCountry = "originCountry")),
    productionCountries = listOf(ProductionCountry(iso31661 = "KR", name = "name")),
    releaseDate = "2025-12-25",
    releases = Releases(countries = listOf(Country(certification = "15", descriptors = listOf("descriptors"), iso31661 = "KR", primary = true, releaseDate = "2025-12-25"))),
    revenue = 30_000_000_000,
    runtime = 240,
    spokenLanguages = listOf(SpokenLanguage(englishName = "englishName", iso6391 = "ko", name = "name")),
    status = "Release",
    tagline = "tagline",
    title = "title",
    video = true,
    videos = Videos(listOf(VideoInfo(id = "", iso31661 = "", iso6391 = "", key = "", name = "", official = true, publishedAt = "", site = "", size = 19, type = ""))),
    voteAverage = 3.5f,
    voteCount = 203,
    certification = "15",
    series = movieSeriesTestData,
    isFavorite = false
)

val tvTestData = Tv(
    adult = true,
    alternativeTitles = null,
    backdropPath = "backdropPath",
    createdBy = listOf(),
    episodeRunTime = listOf(),
    firstAirDate = "2025-12-25",
    genres = listOf(Genre(id = 0, name = "genre")),
    homepage = "homepage",
    id = 0,
    images = Images(backdrops = listOf(), logos = listOf(), posters = listOf()),
    inProduction = true,
    languages = listOf(),
    lastAirDate = "2025-12-25",
    lastEpisodeToAir = null,
    nextEpisodeToAir = null,
    networks = listOf(),
    numberOfEpisodes = 0,
    numberOfSeasons = 0,
    originCountry = listOf(),
    originalLanguage = "originalLanguage",
    originalTitle = "originalName",
    overview = "overview",
    popularity = 3.5,
    posterPath = "https://original/posterPath.png",
    productionCompanies = listOf(ProductionCompany(id = 0, logoPath = "https://original/logoPath.png", name = "name", originCountry = "originCountry")),
    productionCountries = listOf(ProductionCountry(iso31661 = "KR", name = "name")),
    seasons = listOf(),
    spokenLanguages = listOf(SpokenLanguage(englishName = "englishName", iso6391 = "ko", name = "name")),
    status = "Release",
    tagline = "tagline",
    title = "title",
    type = "type",
    voteAverage = 3.5f,
    voteCount = 203,
    credits = Credits(cast = listOf(), crew = listOf()),
    videos = null,
    keywords = null,
    episode = null,
    seasonList = mapOf(
        "1" to TvSeasons(
            airDate = "airDate",
            credits = Credits(),
            episodes = listOf(TvEpisode()),
            _id = "_id",
            id = 0,
            images = null,
            name = "name",
            networks = listOf(),
            overview = "tvSeasonOverview",
            posterPath = "/tvSeasonPosterPath.png",
            seasonNumber = 1,
            videos = null,
            voteAverage = 3.8f
        )
    )
)

val tvSeasonTestData = TvSeasons(
    id = 0,
    name = "tvSeasonName",
    episodes = listOf()
)

val tvEpisodeTestData = TvEpisode(
    id = 0,
    name = "tvEpisode"
)

val watchProvidersTestData = MovieWatchProvider(
    id = 0,
    results = mapOf(
        "KR" to MovieWatchProviderResult(
            link = "link",
            flatrate = listOf(),
            buy = listOf(),
            rent = listOf()
        )
    )
)

val combineCreditsTestData = CombineCredits(
    cast = listOf(
        CombineCreditsCast(id = 1, posterPath = "/CombineCreditsCast.png", mediaType = MediaType.MOVIE)
    ),
    crew = listOf(
        CombineCreditsCrew(id = 2, posterPath = "/CombineCreditsCrew.png", mediaType = MediaType.MOVIE)
    )
)

val externalIdsTestData = ExternalIds(
    facebookId = "facebook",
    instagramId = "instagram",
    youtubeId = "youtubeId"
)

val peopleDetailTestData = People(
    mediaType = MediaType.PEOPLE,
    adult = true,
    alsoKnownAs = listOf("alsoKnownAs"),
    biography = "biography",
    birthday = "1992-06-24",
    combineCredits = combineCreditsTestData,
    deathday = null,
    externalIds = externalIdsTestData,
    gender = 1,
    homepage = "homepage",
    id = 0,
    images = listOf(Image()),
    imdbId = "imdbId",
    knownForDepartment = "knownForDepartment",
    title = "name",
    placeOfBirth = "placeOfBirth",
    popularity = 3.5,
    posterPath = "/profilePath.png"
)

val nowPlayingMovieTest = (0..20).map {
    NowPlayingMovieEntity(
        releaseDate = "nowPlaying_releaseDate_$it",
        title = "nowPlaying_$it",
        id = it,
        posterPath = "/imagePath_$it.png",
        voteAverage = 3.5f,
        voteCount = 395
    )
}

val upComingMovieTest = (0..20).map {
    UpComingMovieEntity(
        releaseDate = "upcomingMovie_releaseDate_$it",
        title = "upcomingMovie_$it",
        id = it,
        posterPath = "/imagePath_$it.png",
        voteAverage = 3.5f,
        voteCount = 395
    )
}

val popularMovieTest = (0 until 5).map {
    Movie(
        id = it,
        title = "popularMovie_$it",
        posterPath = "/imagePath_$it.png",
        releaseDate = "2026-05-29",
        voteAverage = 8.57f,
        voteCount = 36246
    )
}

val keywordList = (0 until 10).map {
    KeywordEntity(
        id = it,
        keyword = "keyword_$it",
        timestamp = it.toLong()
    )
}