package com.cheeke.surfy.search.impl

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.cheeke.surfy.analytics.api.AnalyticsHelper
import com.cheeke.surfy.analytics.api.logSearch
import com.cheeke.surfy.database.impl.model.KeywordEntity
import com.cheeke.surfy.datamanager.api.DataManager
import com.cheeke.surfy.model.Genre
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.model.SurfyAppData
import com.cheeke.surfy.network.api.SurfyNetworkException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = SearchVM.Factory::class)
class SearchVM @AssistedInject constructor(
    @Assisted initialQuery: String,
    @Assisted initialSearchType: SearchType,
    dataManager: DataManager,
    private val savedStateHandle: SavedStateHandle,
    private val keywordDataBaseRepository: KeywordDataBaseRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val searchRepository: SearchRepository
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

    private val _query = MutableStateFlow(value = TextFieldValue(text = initialQuery))
    val query = _query.asStateFlow()
    val selectedGenre = savedStateHandle.getStateFlow<Genre?>(key = GENRE, initialValue = null)
    val searchType = savedStateHandle.getStateFlow<SearchType>(key = SEARCH_TYPE, initialValue = initialSearchType)
    val showSnackbar = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val surfyAppData = dataManager.surfyAppData
        .map { it.getMovieAppData() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = SurfyAppData()
        )
    val recentlyKeywordPaging = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 5),
        initialKey = 1,
        pagingSourceFactory = { keywordDataBaseRepository.getKeywords() }
    ).flow.cachedIn(scope = viewModelScope)
    val recommendKeywordPaging = query
        .map { textFieldValue: TextFieldValue -> textFieldValue.text }
        .debounce(timeoutMillis = 300)
        .distinctUntilChanged()
        .flatMapLatest { keyword: String ->
            if (keyword.trim().isEmpty()) {
                flowOf(value = PagingData.empty())
            } else {
                Pager(
                    config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 5),
                    initialKey = 1,
                    pagingSourceFactory = { searchRepository.getRecommendKeywordPagingSource(query = keyword) }
                ).flow
            }
        }.cachedIn(scope = viewModelScope)
    private val searchRequest = MutableStateFlow<SearchRequest?>(
        value = if (initialQuery.trim().isNotEmpty()) {
            SearchRequest(searchType = initialSearchType, query = initialQuery.trim())
        } else {
            null
        }
    )
    val searchResult: StateFlow<SearchUiState> = searchRequest
        .flatMapLatest { request: SearchRequest? ->
            if (request == null) {
                flowOf(value = SearchUiState.SearchHint)
            } else {
                flow<SearchUiState> {
                    emit(
                        value = SearchUiState.Success(
                            pagingData = combine(
                                Pager(
                                    config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 5),
                                    initialKey = 1,
                                    pagingSourceFactory = {
                                        searchRepository.getSearchPagingSource(
                                            type = request.searchType,
                                            query = request.query,
                                            language = surfyAppData.value.selectedLanguage,
                                            region = surfyAppData.value.selectedRegion,
                                            isAdult = surfyAppData.value.isAdult
                                        )
                                    }
                                ).flow.cachedIn(scope = viewModelScope),
                                selectedGenre
                            ) { pagingData: PagingData<Media>, genre: Genre? ->
                                if (genre != null) {
                                    pagingData.filter { media: Media ->
                                        genre.id in (media.genres?.map { it.id }.orEmpty())
                                    }
                                } else {
                                    pagingData
                                }
                            }
                        )
                    )
                }.catch { throwable: Throwable ->
                    val networkException = throwable as? SurfyNetworkException ?: throw throwable
                    emit(value = SearchUiState.Error(throwable = networkException))
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = SearchUiState.SearchHint
        )

    fun updateGenre(genre: Genre?) {
        savedStateHandle[GENRE] = if (genre == selectedGenre.value) null else genre
    }

    fun updateQuery(value: TextFieldValue) {
        _query.value = value
    }

    fun updateSearchType(newSearchType: SearchType) {
        savedStateHandle[SEARCH_TYPE] = newSearchType
        searchRequest.value = null // 타입 바뀌면 명시적으로 힌트 상태로 리셋
    }

    fun searchMovies() {
        val currentQuery: String = query.value.text.trim()

        if (currentQuery.isEmpty()) {
            viewModelScope.launch { showSnackbar.emit(value = Unit) }
            return
        }

        analyticsHelper.logSearch(searchType = searchType.value.label, query = currentQuery)
        searchRequest.value = SearchRequest(searchType = searchType.value, query = currentQuery)
    }

    fun saveKeyword(keyword: String) {
        viewModelScope.launch {
            keywordDataBaseRepository.insert(keyword = keyword)
        }
    }

    fun deleteAllRecentlyKeyword() {
        viewModelScope.launch {
            keywordDataBaseRepository.deleteAll()
        }
    }

    fun deleteRecentlyKeyword(entity: KeywordEntity) {
        viewModelScope.launch {
            keywordDataBaseRepository.delete(entity = entity)
        }
    }
}

private data class SearchRequest(
    val searchType: SearchType,
    val query: String
)

sealed interface SearchUiState {
    data object SearchHint : SearchUiState
    data class Success(val pagingData: Flow<PagingData<Media>>) : SearchUiState
    data class Error(val throwable: SurfyNetworkException) : SearchUiState
}