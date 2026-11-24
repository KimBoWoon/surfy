package com.cheeke.surfy.search

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.rxjava3.cachedIn
import androidx.paging.rxjava3.observable
import com.cheeke.surfy.analytics.AnalyticsHelper
import com.cheeke.surfy.analytics.logSearch
import com.cheeke.surfy.data.repository.SearchRepository
import com.cheeke.surfy.data.util.DataManager
import com.cheeke.surfy.data.util.SurfyAppDataState
import com.cheeke.surfy.database.model.KeywordEntity
import com.cheeke.surfy.model.Genre
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.network.model.SurfyNetworkException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx3.asFlow
import java.util.concurrent.TimeUnit

@HiltViewModel(assistedFactory = SearchVM.Factory::class)
class SearchVM @AssistedInject constructor(
    @Assisted initialQuery: String,
    @Assisted initialSearchType: SearchType,
    dataManager: DataManager,
    private val savedStateHandle: SavedStateHandle,
    private val searchRepository: SearchRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {
    companion object {
        internal const val TAG = "SearchVM"
        private const val GENRE = "genre"
        private const val SEARCH_TYPE = "searchType"
    }

    @AssistedFactory
    interface Factory {
        fun create(
            initialQuery: String,
            initialSearchType: SearchType
        ): SearchVM
    }

    // Rx는 onNext(null)을 허용하지 않으므로, nullable 값은 항상 non-null 래퍼로 감싼다.
    data class GenreSelection(val genre: Genre?)

    private sealed interface SearchRequest {
        data object None : SearchRequest
        data class Requested(val searchType: SearchType, val query: String) : SearchRequest
    }

    private val compositeDisposable = CompositeDisposable()

    private val querySubject: BehaviorSubject<TextFieldValue> =
        BehaviorSubject.createDefault(TextFieldValue(text = initialQuery))
    val query: Observable<TextFieldValue> = querySubject.hide()

    private val genreSubject: BehaviorSubject<GenreSelection> =
        BehaviorSubject.createDefault(GenreSelection(genre = savedStateHandle.get<Genre?>(GENRE)))
    val selectedGenre: Observable<GenreSelection> = genreSubject.hide()

    private val searchTypeSubject: BehaviorSubject<SearchType> =
        BehaviorSubject.createDefault(savedStateHandle.get<SearchType>(SEARCH_TYPE) ?: initialSearchType)
    val searchType: Observable<SearchType> = searchTypeSubject.hide()

    val showSnackbar: PublishSubject<Unit> = PublishSubject.create()

    // language/region/isAdult를 검색 시점에 동기로 스냅샷 읽어야 해서 BehaviorSubject로 캐시.
    private val surfyAppDataSubject: BehaviorSubject<SurfyAppDataState> = BehaviorSubject.create()
    val surfyAppData: Observable<SurfyAppDataState> = surfyAppDataSubject.hide()

    val recentlyKeywordPaging: Observable<PagingData<KeywordEntity>> = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 5),
        initialKey = 1,
        pagingSourceFactory = { searchRepository.getKeywords() }
    ).observable.cachedIn(scope = viewModelScope)

    private val recommendKeywordSubject: BehaviorSubject<String> = BehaviorSubject.createDefault("")

    val recommendKeywordPaging = recommendKeywordSubject
        .debounce(300, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
        .switchMap { keyword: String ->
            if (keyword.trim().isEmpty()) {
                Observable.just(PagingData.empty())
            } else {
                Pager(
                    config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 5),
                    initialKey = 1,
                    pagingSourceFactory = { searchRepository.getRecommendKeywordPagingSource(query = keyword) }
                ).observable
            }
        }.cachedIn(scope = viewModelScope)

    private val searchRequestSubject: BehaviorSubject<SearchRequest> = BehaviorSubject.createDefault(
        if (initialQuery.trim().isNotEmpty()) {
            SearchRequest.Requested(searchType = initialSearchType, query = initialQuery.trim())
        } else {
            SearchRequest.None
        }
    )

    val searchResult: Observable<SearchUiState> = searchRequestSubject
        .switchMap { request: SearchRequest ->
            when (request) {
                is SearchRequest.None -> Observable.just(SearchUiState.SearchHint)
                is SearchRequest.Requested -> Observable.just(request)
                    .map<SearchUiState> { requested: SearchRequest.Requested ->
                        val appDataSnapshot: SurfyAppDataState? = surfyAppDataSubject.value

                        val pagingDataObservable: Observable<PagingData<Media>> = Observable.combineLatest(
                            Pager(
                                config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 5),
                                initialKey = 1,
                                pagingSourceFactory = {
                                    searchRepository.getSearchPagingSource(
                                        type = requested.searchType,
                                        query = requested.query,
                                        language = appDataSnapshot?.getMovieAppData()?.language?.find { it.isSelected }?.code.orEmpty(),
                                        region = appDataSnapshot?.getMovieAppData()?.region?.find { it.isSelected }?.code.orEmpty(),
                                        isAdult = appDataSnapshot?.getMovieAppData()?.isAdult ?: false
                                    )
                                }
                            ).observable.cachedIn(scope = viewModelScope),
                            genreSubject
                        ) { pagingData: PagingData<Media>, genreSelection: GenreSelection ->
                            val genre: Genre? = genreSelection.genre
                            if (genre != null) {
                                pagingData.filter { media: Media ->
                                    genre.id in (media.genres?.map { it.id }.orEmpty())
                                }
                            } else {
                                pagingData
                            }
                        }

                        val pagingDataFlow: Flow<PagingData<Media>> = pagingDataObservable.asFlow()

                        SearchUiState.Success(pagingData = pagingDataFlow)
                    }
                    .onErrorReturn { throwable: Throwable -> SearchUiState.Error(throwable = SurfyNetworkException(throwable = throwable)) }
            }
        }
        .replay(1)
        .autoConnect(1) { disposable -> compositeDisposable.add(disposable) }

    init {
        dataManager.surfyAppData
            .onBackpressureLatest()
            .map { surfyAppDataState: SurfyAppDataState -> surfyAppDataState.getMovieAppData() }
            .toObservable()
            .subscribeBy(
                onNext = {
                    surfyAppDataSubject.onNext(SurfyAppDataState.Success(data = it))
                },
                onError = { throwable: Throwable ->
                    /* 로깅 처리, subject는 살려둠 */
                    surfyAppDataSubject.onNext(SurfyAppDataState.Error(throwable = throwable))
                }
            )
            .addTo(compositeDisposable)
    }

    fun updateGenre(genre: Genre?) {
        val currentGenre: Genre? = genreSubject.value?.genre
        val newGenre: Genre? = if (genre == currentGenre) null else genre
        savedStateHandle[GENRE] = newGenre
        genreSubject.onNext(GenreSelection(genre = newGenre))
    }

    fun updateQuery(newTextFieldValue: TextFieldValue) {
        querySubject.onNext(newTextFieldValue)
        recommendKeywordSubject.onNext(newTextFieldValue.text)
    }

    fun updateSearchType(newSearchType: SearchType) {
        savedStateHandle[SEARCH_TYPE] = newSearchType
        searchTypeSubject.onNext(newSearchType)
        searchRequestSubject.onNext(SearchRequest.None)
    }

    fun searchMovies() {
        val currentQuery: String = querySubject.value?.text.orEmpty().trim()

        if (currentQuery.isEmpty()) {
            showSnackbar.onNext(Unit)
            return
        }

        val currentSearchType: SearchType = searchTypeSubject.value ?: return

        analyticsHelper.logSearch(searchType = currentSearchType.label, query = currentQuery)
        searchRequestSubject.onNext(
            SearchRequest.Requested(searchType = currentSearchType, query = currentQuery)
        )
    }

    fun saveKeyword(keyword: String) {
        searchRepository.insert(keyword = keyword)
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = { throwable: Throwable -> /* 로깅 처리 */ })
            .addTo(compositeDisposable)
    }

    fun deleteAllRecentlyKeyword() {
        searchRepository.deleteAll()
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = { throwable: Throwable -> /* 로깅 처리 */ })
            .addTo(compositeDisposable)
    }

    fun deleteRecentlyKeyword(entity: KeywordEntity) {
        searchRepository.delete(entity = entity)
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = { throwable: Throwable -> /* 로깅 처리 */ })
            .addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}

sealed interface SearchUiState {
    data object SearchHint : SearchUiState
    data class Success(val pagingData: Flow<PagingData<Media>>) : SearchUiState
    data class Error(val throwable: SurfyNetworkException) : SearchUiState
}