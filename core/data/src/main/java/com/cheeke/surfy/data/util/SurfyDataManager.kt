package com.cheeke.surfy.data.util

import com.cheeke.surfy.common.Dispatcher
import com.cheeke.surfy.common.Dispatchers
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.di.ApplicationScope
import com.cheeke.surfy.data.repository.UserDataRepository
import com.cheeke.surfy.model.Configuration
import com.cheeke.surfy.model.Genre
import com.cheeke.surfy.model.InternalData
import com.cheeke.surfy.model.Language
import com.cheeke.surfy.model.LocaleOption
import com.cheeke.surfy.model.PosterSize
import com.cheeke.surfy.model.Regions
import com.cheeke.surfy.model.SurfyAppData
import com.cheeke.surfy.network.SettingRemoteDataSource
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class SurfyDataManager @Inject constructor(
    @param:Dispatcher(dispatcher = Dispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val appScope: CoroutineScope,
    private val apis: SettingRemoteDataSource,
    private val userDataRepository: UserDataRepository,
    private val networkMonitor: NetworkMonitor
) : DataManager {
    private val userDataFlow = userDataRepository.internalData.distinctUntilChanged()
    val localeFlow =
        userDataFlow
            .map { Locale(it.language, it.region) }
            .distinctUntilChanged()
    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
        .apply { tryEmit(value = Unit) }
    @OptIn(ExperimentalCoroutinesApi::class)
    private val envDataFlow = retryableFlow(
        manualTrigger = retryTrigger,
        isOnline = networkMonitor.isOnline,
        loader = {
            coroutineScope {
                val configDeferred = async { apis.getConfiguration() }
                val languageDeferred = async { apis.getAvailableLanguage() }
                val regionDeferred = async { apis.getAvailableRegion() }
                EnvData(
                    configuration = configDeferred.await(),
                    language = languageDeferred.await(),
                    region = regionDeferred.await()
                )
            }
        }
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    private val genresFlow = localeFlow
        .flatMapLatest { locale ->
            retryableFlow(
                manualTrigger = retryTrigger,
                isOnline = networkMonitor.isOnline,
                loader = {
                    val language = "${locale.language}-${locale.region}"
                    coroutineScope {
                        val movieDeferred = async { apis.getMovieGenres(language = language) }
                        val tvDeferred = async { apis.getTvGenres(language = language) }
                        GenreData(
                            movie = movieDeferred.await().genres.orEmpty(),
                            tv = tvDeferred.await().genres.orEmpty()
                        )
                    }
                }
            )
        }
    @OptIn(ExperimentalCoroutinesApi::class)
    override val surfyAppData = combine(
        userDataFlow,
        flow2 = envDataFlow,
        flow3 = genresFlow
    ) { internalData, env, genres ->
        when {
            env == null -> SurfyAppDataState.Error(throwable = IllegalStateException(/*"환경 데이터를 불러오지 못했습니다"*/))
            genres == null -> SurfyAppDataState.Error(throwable = IllegalStateException(/*"장르 데이터를 불러오지 못했습니다"*/))
            else -> {
                userDataRepository.updateSecureBaseUrl(value = env.configuration.images?.secureBaseUrl.orEmpty())
                SurfyAppDataState.Success(
                    data = buildSurfyAppData(
                        internalData = internalData,
                        env = env,
                        genres = genres
                    )
                )
            }
        }
    }.distinctUntilChanged()
        .flowOn(context = ioDispatcher)
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = SurfyAppDataState.Loading
        )

    fun retry() {
        retryTrigger.tryEmit(value = Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> retryableFlow(
        manualTrigger: Flow<Unit>,
        isOnline: Flow<Boolean>,
        loader: suspend () -> T
    ): Flow<T?> {
        val failed = MutableStateFlow(value = false)
        val reconnectTrigger = isOnline
            .distinctUntilChanged()
            .filter { online -> online && failed.value }
            .map { }

        return merge(manualTrigger, reconnectTrigger)
            .flatMapLatest {
                flow {
                    emit(
                        value = runCatching { loader() }
                            .getOrElse { e ->
                                if (e is CancellationException) {
                                    throw e
                                }
                                Log.printStackTrace(tr = e)
                                null
                            }
                    )
                }
            }.onEach { failed.value = it == null }
    }

    private fun buildSurfyAppData(internalData: InternalData, env: EnvData, genres: GenreData): SurfyAppData =
        SurfyAppData(
            isAdult = internalData.isAdult,
            autoPlayTrailer = internalData.isAutoPlayTrailer,
            isDarkMode = internalData.isDarkMode,
            updateDate = internalData.updateDate,
            imageQuality = internalData.imageQuality,
            secureBaseUrl = env.configuration.images?.secureBaseUrl.orEmpty(),
            movieGenres = genres.movie,
            tvGenres = genres.tv,
            region = env.region.results?.map { region ->
                LocaleOption(code = region.iso31661 ?: "", label = region.nativeName ?: "")
            }.orEmpty(),
            language = env.language.map { language ->
                LocaleOption(code = language.iso6391 ?: "", label = language.englishName ?: "")
            },
            posterSize = env.configuration.images?.posterSizes?.map { quality ->
                PosterSize(size = quality)
            }.orEmpty(),
            selectedImageQuality = internalData.imageQuality,
            selectedRegion = internalData.region,
            selectedLanguage = internalData.language,
            selectedLanguageAndRegion = "${internalData.language}-${internalData.region}"
        )

    private data class GenreData(val movie: List<Genre>, val tv: List<Genre>)
    private data class EnvData(
        val configuration: Configuration,
        val language: List<Language>,
        val region: Regions
    )
}