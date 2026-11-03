package com.cheeke.surfy.search.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.cheeke.surfy.analytics.api.TrackScreenViewEvent
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.POSTER_IMAGE_RATIO
import com.cheeke.surfy.database.impl.model.KeywordEntity
import com.cheeke.surfy.feature.search.impl.R
import com.cheeke.surfy.firebase.api.LocalFirebaseLogHelper
import com.cheeke.surfy.model.Genre
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.SearchKeyword
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.model.SurfyAppData
import com.cheeke.surfy.ui.components.CircularProgressComponent
import com.cheeke.surfy.ui.components.FilterChipComponent
import com.cheeke.surfy.ui.components.PagingAppendErrorComponent
import com.cheeke.surfy.ui.dialog.ConfirmDialog
import com.cheeke.surfy.ui.image.DynamicAsyncImageLoader
import com.cheeke.surfy.ui.utils.animateRotation
import com.cheeke.surfy.ui.utils.bounceClick
import com.cheeke.surfy.ui.utils.dp0
import com.cheeke.surfy.ui.utils.dp1
import com.cheeke.surfy.ui.utils.dp10
import com.cheeke.surfy.ui.utils.dp100
import com.cheeke.surfy.ui.utils.dp15
import com.cheeke.surfy.ui.utils.dp16
import com.cheeke.surfy.ui.utils.dp35
import com.cheeke.surfy.ui.utils.dp40
import com.cheeke.surfy.ui.utils.dp5
import com.cheeke.surfy.ui.utils.dp50
import com.cheeke.surfy.ui.utils.dp60
import com.cheeke.surfy.ui.utils.dp8
import com.cheeke.surfy.ui.utils.dp999
import com.cheeke.surfy.ui.utils.matchedColorString
import com.cheeke.surfy.ui.utils.roundedCornerClickable
import com.cheeke.surfy.ui.utils.sp12
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    goToMovie: (Int) -> Unit,
    goToTv: (Int) -> Unit,
    goToPeople: (Int) -> Unit,
    goToSeries: (Int) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    viewModel: SearchVM = hiltViewModel()
) {
    LocalFirebaseLogHelper.current.sendLog("SearchScreen", "search screen init")
    TrackScreenViewEvent(screenName = "SearchScreen")

    val searchUiState by viewModel.searchResult.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
    val searchType by viewModel.searchType.collectAsStateWithLifecycle()
    val recommendKeyword = viewModel.recommendKeywordPaging.collectAsLazyPagingItems()
    val inputKeyword = stringResource(id = R.string.input_keyword)
    val movieAppData by viewModel.surfyAppData.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val recentlyKeyword = viewModel.recentlyKeywordPaging.collectAsLazyPagingItems()
    val query by viewModel.query.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.showSnackbar
            .flowWithLifecycle(lifecycle = lifecycle, minActiveState = Lifecycle.State.STARTED)
            .collect { onShowSnackbar(inputKeyword, null) }
    }

    SearchScreen(
        searchUiState = searchUiState,
        recentlyKeyword = recentlyKeyword,
        recommendKeyword = recommendKeyword,
        query = query,
        searchType = searchType,
        surfyAppData = movieAppData,
        selectedGenre = selectedGenre,
        goToMovie = goToMovie,
        goToTv = goToTv,
        goToPeople = goToPeople,
        goToSeries = goToSeries,
        onSaveKeyword = viewModel::saveKeyword,
        deleteKeyword = viewModel::deleteRecentlyKeyword,
        deleteAllKeyword = viewModel::deleteAllRecentlyKeyword,
        onSearchClick = viewModel::searchMovies,
        updateKeyword = viewModel::updateQuery,
        updateSearchType = viewModel::updateSearchType,
        updateGenre = viewModel::updateGenre
    )
}

@Composable
fun SearchScreen(
    searchUiState: SearchUiState,
    recentlyKeyword: LazyPagingItems<KeywordEntity>,
    recommendKeyword: LazyPagingItems<SearchKeyword>,
    query: TextFieldValue,
    searchType: SearchType,
    surfyAppData: SurfyAppData,
    selectedGenre: Genre?,
    goToMovie: (Int) -> Unit,
    goToTv: (Int) -> Unit,
    goToPeople: (Int) -> Unit,
    goToSeries: (Int) -> Unit,
    onSaveKeyword: (String) -> Unit,
    deleteKeyword: (KeywordEntity) -> Unit,
    deleteAllKeyword: () -> Unit,
    onSearchClick: () -> Unit,
    updateKeyword: (TextFieldValue) -> Unit,
    updateSearchType: (SearchType) -> Unit,
    updateGenre: (Genre?) -> Unit
) {
    val scrollState = rememberLazyGridState()
    val focusManager = LocalFocusManager.current
    var isVisible by remember { mutableStateOf(value = false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SearchBarComponent(
            query = query,
            searchType = searchType,
            scrollState = scrollState,
            updateKeyword = updateKeyword,
            onSaveKeyword = onSaveKeyword,
            onSearchClick = onSearchClick,
            updateSearchType = updateSearchType,
            updateGenre = updateGenre,
            recommendKeywordVisible = { isVisible = it }
        )

        if (isVisible) {
            RecommendKeywordComponent(
                recentlyKeyword = recentlyKeyword,
                recommendKeyword = recommendKeyword,
                query = query,
                updateKeyword = updateKeyword,
                onSaveKeyword = onSaveKeyword,
                deleteKeyword = deleteKeyword,
                deleteAllKeyword = deleteAllKeyword,
                onSearchClick = onSearchClick,
                recommendKeywordVisible = { isVisible = it }
            )
        } else {
            focusManager.clearFocus()
            SearchResultComponent(
                searchUiState = searchUiState,
                scrollState = scrollState,
                searchType = searchType,
                goToMovie = goToMovie,
                goToTv = goToTv,
                goToPeople = goToPeople,
                goToSeries = goToSeries,
                surfyAppData = surfyAppData,
                selectedGenre = selectedGenre,
                updateGenre = updateGenre
            )
        }
    }
}

@Composable
fun SearchBarComponent(
    query: TextFieldValue,
    searchType: SearchType,
    scrollState: LazyGridState,
    onSearchClick: () -> Unit,
    onSaveKeyword: (String) -> Unit,
    updateKeyword: (TextFieldValue) -> Unit,
    updateSearchType: (SearchType) -> Unit,
    updateGenre: (Genre?) -> Unit,
    recommendKeywordVisible: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, keyboardType = KeyboardType.Text)
    val keyboardActions = KeyboardActions(
        onDone = {
            focusManager.clearFocus()
            recommendKeywordVisible(false)
        },
        onSearch = {
            scope.launch { scrollState.scrollToItem(index = 0) }
            updateGenre(null)
            onSaveKeyword(query.text)
            onSearchClick()
            focusManager.clearFocus()
            recommendKeywordVisible(false)
        }
    )

    BasicTextField(
        modifier = Modifier
            .padding(
                top = dp10,
                bottom = if (searchType == SearchType.MOVIE || searchType == SearchType.TV) dp0 else dp10,
                start = dp16,
                end = dp16
            )
            .fillMaxWidth()
            .height(height = dp40)
            .clip(shape = RoundedCornerShape(percent = 50))
            .background(color = MaterialTheme.colorScheme.inverseOnSurface),
        value = query,
        onValueChange = {
            Log.d(it.text)
            updateKeyword(it)
            if (it.text.isNotEmpty()) {
                recommendKeywordVisible(true)
            }
        },
        cursorBrush = SolidColor(
            value = MaterialTheme.colorScheme.onSurface
        ),
        textStyle = TextStyle(
            fontSize = sp12,
            color = MaterialTheme.colorScheme.onSurface
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(start = dp16, end = dp8),
                    imageVector = Icons.Filled.Search,
                    contentDescription = "searchBarIcon"
                )

                SearchTypeComponent(
                    searchType = searchType,
                    updateSearchType = updateSearchType,
                    recommendKeywordVisible = recommendKeywordVisible
                )

                Spacer(
                    modifier = Modifier
                        .padding(horizontal = dp5)
                        .width(width = dp1)
                        .height(height = dp10)
                        .background(color = MaterialTheme.colorScheme.onSurface)
                )

                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .align(Alignment.CenterVertically)
                ) {
                    innerTextField()
                    if (query.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.input_search_hint),
                            fontSize = sp12,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }

                AnimatedVisibility(
                    visible = query.text.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(start = dp8, end = dp8)
                                .clickable {
                                    updateKeyword(TextFieldValue(text = ""))
                                    recommendKeywordVisible(false)
                                },
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "searchKeywordClear"
                        )
                        Icon(
                            modifier = Modifier
                                .padding(end = dp16)
                                .clickable {
                                    scope.launch { scrollState.scrollToItem(index = 0) }
                                    updateGenre(null)
                                    onSaveKeyword(query.text)
                                    onSearchClick()
                                    focusManager.clearFocus()
                                    recommendKeywordVisible(false)
                                },
                            imageVector = Icons.Filled.Search,
                            contentDescription = "searchMovies"
                        )
                    }
                }
            }
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

@Composable
fun SearchTypeComponent(
    searchType: SearchType,
    updateSearchType: (SearchType) -> Unit,
    recommendKeywordVisible: (Boolean) -> Unit
) {
    var isExpand by remember { mutableStateOf(value = false) }
    val types = SearchType.entries

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .testTag(tag = "searchType")
                    .clickable { isExpand = !isExpand },
                text = searchType.label,
                fontSize = sp12,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                modifier = Modifier
                    .size(size = dp15)
                    .animateRotation(
                        expanded = isExpand,
                        startAngle = 0f,
                        endAngle = -180f,
                        animateMillis = 200
                    ),
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "searchTypeArrow"
            )
        }
        DropdownMenu(
            modifier = Modifier.wrapContentSize(),
            expanded = isExpand,
            onDismissRequest = { isExpand = false }
        ) {
            types.forEach { type ->
                DropdownMenuItem(
                    modifier = Modifier.testTag(tag = type.label),
                    onClick = {
                        Log.d(type.label)
                        updateSearchType(type)
                        recommendKeywordVisible(false)
                        isExpand = false
                    },
                    text = { Text(text = type.label) }
                )
            }
        }
    }
}

@Composable
fun SearchResultComponent(
    searchUiState: SearchUiState,
    scrollState: LazyGridState,
    searchType: SearchType,
    goToMovie: (Int) -> Unit,
    goToTv: (Int) -> Unit,
    goToPeople: (Int) -> Unit,
    goToSeries: (Int) -> Unit,
    surfyAppData: SurfyAppData,
    selectedGenre: Genre?,
    updateGenre: (Genre) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (searchUiState) {
            is SearchUiState.SearchHint -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(id = R.string.do_search),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            is SearchUiState.Success -> {
                val pagingData = searchUiState.pagingData.collectAsLazyPagingItems()

                if (pagingData.loadState.refresh is LoadState.Loading) {
                    CircularProgressComponent()
                } else if (pagingData.loadState.refresh is LoadState.Error) {
                    ConfirmDialog(
                        title = stringResource(id = com.cheeke.surfy.core.network.api.R.string.network_failed),
                        message = (pagingData.loadState.refresh as? LoadState.Error)?.error?.message ?: stringResource(id = com.cheeke.surfy.core.network.api.R.string.something_wrong),
                        confirmPair = stringResource(id = com.cheeke.surfy.core.ui.R.string.retry_message) to { pagingData.retry() },
                        dismissPair = stringResource(id = com.cheeke.surfy.core.ui.R.string.confirm_message) to {}
                    )
                } else if (pagingData.loadState.refresh is LoadState.NotLoading) {
                    if (pagingData.itemCount == 0) {
                        Text(
                            text = stringResource(id = R.string.search_result_empty),
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        SearchPagingComponent(
                            pagingData = pagingData,
                            scrollState = scrollState,
                            searchType = searchType,
                            surfyAppData = surfyAppData,
                            selectedGenre = selectedGenre,
                            updateGenre = updateGenre,
                            goToMovie = goToMovie,
                            goToTv = goToTv,
                            goToPeople = goToPeople,
                            goToSeries = goToSeries
                        )
                    }
                }
            }
            is SearchUiState.Error -> {
                LocalFirebaseLogHelper.current.sendLog("SearchResultPaging", searchUiState.throwable.message ?: stringResource(com.cheeke.surfy.core.network.api.R.string.something_wrong))

                val message = searchUiState.throwable.stringRes?.let { stringResource(id = it) } ?: stringResource(id = com.cheeke.surfy.core.network.api.R.string.something_wrong)

                ConfirmDialog(
                    title = stringResource(id = com.cheeke.surfy.core.network.api.R.string.network_failed),
                    message = message,
                    confirmPair = stringResource(id = com.cheeke.surfy.core.ui.R.string.confirm_message) to {}
                )
            }
        }
    }
}

@Composable
fun SearchPagingComponent(
    pagingData: LazyPagingItems<Media>,
    scrollState: LazyGridState,
    searchType: SearchType,
    surfyAppData: SurfyAppData,
    selectedGenre: Genre?,
    updateGenre: (Genre) -> Unit,
    goToMovie: (Int) -> Unit,
    goToTv: (Int) -> Unit,
    goToPeople: (Int) -> Unit,
    goToSeries: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MovieFilterRowComponent(
            searchType = searchType,
            surfyAppData = surfyAppData,
            selectedGenre = selectedGenre,
            updateGenre = updateGenre
        )

        LazyVerticalGrid(
            modifier = Modifier
                .semantics { contentDescription = "searchResultList" }
                .fillMaxSize(),
            state = scrollState,
            columns = GridCells.Adaptive(minSize = dp100),
            contentPadding = PaddingValues(horizontal = dp10),
            horizontalArrangement = Arrangement.spacedBy(space = dp10),
            verticalArrangement = Arrangement.spacedBy(space = dp10)
        ) {
            items(
                count = pagingData.itemCount,
                key = { index -> "${pagingData.peek(index)?.id}_${index}_${pagingData.peek(index)?.title}" }
            ) { index ->
                pagingData[index]?.let { item ->
                    DynamicAsyncImageLoader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(POSTER_IMAGE_RATIO)
                            .roundedCornerClickable(
                                onClick = {
                                    when (item.mediaType) {
                                        MediaType.NONE -> {}
                                        MediaType.MOVIE -> goToMovie(item.id ?: -1)
                                        MediaType.TV -> goToTv(item.id ?: -1)
                                        MediaType.PEOPLE -> goToPeople(item.id ?: -1)
                                        MediaType.SERIES -> goToSeries(item.id ?: -1)
//                                        SearchType.MULTI -> {}
//                                        SearchType.MOVIE -> goToMovie(item.id ?: -1)
//                                        SearchType.TV -> goToTv(item.id ?: -1)
//                                        SearchType.PEOPLE -> goToPeople(item.id ?: -1)
//                                        SearchType.SERIES -> goToSeries(item.id ?: -1)
                                    }
                                }, cornerRadius = dp10
                            ),
                        source = item.posterPath.orEmpty(),
                        contentDescription = "${item.id}_${item.title}"
                    )
                }
            }
            if (pagingData.loadState.append is LoadState.Loading) {
                item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
                    CircularProgressComponent(modifier = Modifier.wrapContentSize())
                }
            }
            if (pagingData.loadState.append is LoadState.Error) {
                item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
                    PagingAppendErrorComponent(retry = { pagingData.retry() })
                }
            }
        }
    }
}

@Composable
fun RecommendKeywordComponent(
    recentlyKeyword: LazyPagingItems<KeywordEntity>,
    recommendKeyword: LazyPagingItems<SearchKeyword>,
    query: TextFieldValue,
    updateKeyword: (TextFieldValue) -> Unit,
    onSaveKeyword: (String) -> Unit,
    deleteKeyword: (KeywordEntity) -> Unit,
    deleteAllKeyword: () -> Unit,
    onSearchClick: () -> Unit,
    recommendKeywordVisible: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (recentlyKeyword.itemCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = dp50)
                    .padding(horizontal = dp10),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.padding(top = dp5, bottom = dp5),
                    text = stringResource(id = R.string.recently_keyword),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    modifier = Modifier.bounceClick(onClick = { deleteAllKeyword() }),
                    text = stringResource(id = R.string.delete_recently_keyword),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            LazyRow(
                modifier = Modifier
                    .semantics { contentDescription = "recentlyKeywordList" }
                    .fillMaxWidth()
                    .height(height = dp60),
                contentPadding = PaddingValues(all = dp10),
                horizontalArrangement = Arrangement.spacedBy(space = dp10)
            ) {
                items(
                    count = recentlyKeyword.itemCount,
                    key = { index -> recentlyKeyword.peek(index)?.keyword ?: -1 }
                ) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                shape = RoundedCornerShape(size = dp999)
                            )
                            .bounceClick(
                                onClick = {
                                    focusManager.clearFocus()
                                    updateKeyword(TextFieldValue(text = recentlyKeyword[index]?.keyword.orEmpty()))
                                    onSaveKeyword(recentlyKeyword[index]?.keyword.orEmpty())
                                    onSearchClick()
                                    recommendKeywordVisible(false)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        recentlyKeyword[index]?.let { keywordEntity ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.padding(start = dp10, top = dp10, bottom = dp10, end = dp5),
                                    text = keywordEntity.keyword,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Icon(
                                    modifier = Modifier.padding(top = dp10, bottom = dp10, end = dp10).bounceClick(onClick = { deleteKeyword(keywordEntity) }),
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "recentlyKeywordClose"
                                )
                            }
                        }
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .semantics { contentDescription = "recommendKeywordList" }
                .fillMaxSize()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = dp60),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(start = dp16),
                        text = stringResource(id = R.string.recommend_keyword),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Icon(
                        modifier = Modifier
                            .clickable { recommendKeywordVisible(false) }
                            .padding(end = dp16),
                        imageVector = Icons.Filled.Close,
                        contentDescription = "recommendedKeywordClose"
                    )
                }
            }
            items(
                count = recommendKeyword.itemCount,
                key = { index -> recommendKeyword.peek(index)?.id ?: -1 }
            ) { index ->
                recommendKeyword[index]?.let { recommendKeyword ->
                    val annotatedString = recommendKeyword.name.matchedColorString(keyword = query.text, color = MaterialTheme.colorScheme.primary)
                    Text(
                        modifier = Modifier
                            .semantics { contentDescription = annotatedString.toString() }
                            .padding(start = dp16, end = dp16, top = dp10)
                            .fillMaxWidth()
                            .height(height = dp35)
                            .bounceClick {
                                focusManager.clearFocus()
                                updateKeyword(TextFieldValue(text = recommendKeyword.name.orEmpty()))
                                onSaveKeyword(recommendKeyword.name.orEmpty())
                                onSearchClick()
                                recommendKeywordVisible(false)
                            },
                        text = annotatedString,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (recommendKeyword.loadState.append is LoadState.Loading) {
                item {
                    CircularProgressComponent(modifier = Modifier.wrapContentSize())
                }
            }
        }
    }
}

@Composable
fun MovieFilterRowComponent(
    searchType: SearchType,
    surfyAppData: SurfyAppData,
    selectedGenre: Genre?,
    updateGenre: (Genre) -> Unit
) {
    if (searchType == SearchType.MOVIE || searchType == SearchType.TV) {
        LazyRow(
            modifier = Modifier
                .testTag(tag = "FilterRow")
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = dp16, vertical = dp5),
            horizontalArrangement = Arrangement.spacedBy(space = dp10)
        ) {
            items(
                items = if (searchType == SearchType.MOVIE) surfyAppData.movieGenres else if (searchType == SearchType.TV) surfyAppData.tvGenres else emptyList(),
                key = { it.id ?: -1 }
            ) { genre ->
                genre.name?.let { name ->
                    FilterChipComponent(
                        title = name,
                        selectedFilter = selectedGenre?.id == genre.id,
                        updateFilter = { updateGenre(genre) }
                    )
                }
            }
        }
    }
}