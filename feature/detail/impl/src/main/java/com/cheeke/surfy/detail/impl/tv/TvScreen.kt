package com.cheeke.surfy.detail.impl.tv

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.cheeke.surfy.analytics.api.LocalAnalyticsHelper
import com.cheeke.surfy.analytics.api.TrackScreenViewEvent
import com.cheeke.surfy.analytics.api.logFavorite
import com.cheeke.surfy.analytics.api.logSelectEpisode
import com.cheeke.surfy.analytics.api.logSelectSeason
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.detail.impl.movie.AlternativeTitleComponent
import com.cheeke.surfy.feature.detail.impl.R
import com.cheeke.surfy.firebase.api.LocalFirebaseLogHelper
import com.cheeke.surfy.model.Cast
import com.cheeke.surfy.model.Credits
import com.cheeke.surfy.model.Image
import com.cheeke.surfy.model.Review
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeason
import com.cheeke.surfy.ui.components.CircularProgressComponent
import com.cheeke.surfy.ui.components.CreditsComponent
import com.cheeke.surfy.ui.components.ImageOverlay
import com.cheeke.surfy.ui.components.ImagesComponent
import com.cheeke.surfy.ui.components.MediaTitleComponent
import com.cheeke.surfy.ui.components.OverviewComponent
import com.cheeke.surfy.ui.components.ProductionComponent
import com.cheeke.surfy.ui.components.ReviewComponent
import com.cheeke.surfy.ui.components.SimilarComponent
import com.cheeke.surfy.ui.components.TitleComponent
import com.cheeke.surfy.ui.components.VideosComponent
import com.cheeke.surfy.ui.dialog.ConfirmDialog
import com.cheeke.surfy.ui.image.DynamicAsyncImageLoader
import com.cheeke.surfy.ui.utils.dp10
import com.cheeke.surfy.ui.utils.dp12
import com.cheeke.surfy.ui.utils.dp150
import com.cheeke.surfy.ui.utils.dp16
import com.cheeke.surfy.ui.utils.dp180
import com.cheeke.surfy.ui.utils.dp2
import com.cheeke.surfy.ui.utils.dp20
import com.cheeke.surfy.ui.utils.dp227
import com.cheeke.surfy.ui.utils.dp5
import com.cheeke.surfy.ui.utils.dp8
import com.cheeke.surfy.ui.utils.sp10
import com.cheeke.surfy.ui.utils.sp12
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun TvScreen(
    goToBack: () -> Unit,
    goToTv: (Int) -> Unit,
    goToPeople: (Int) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    viewModel: TvVM = hiltViewModel()
) {
    LocalFirebaseLogHelper.current.sendLog("DetailScreen", "detail screen start!")
    TrackScreenViewEvent(screenName = "TvScreen")

    val similarTvs = viewModel.similarTvs
    val tvReviews = viewModel.tvReviews.collectAsLazyPagingItems()
    val selectedEpisode by viewModel.selectedEpisode.collectAsStateWithLifecycle()
    val tvUiState by viewModel.uiState.collectAsStateWithLifecycle()

    TvScreen(
        tvUiState = tvUiState,
        similarTvs = similarTvs,
        tvReviews = tvReviews,
        selectedEpisode = selectedEpisode,
        goToTv = goToTv,
        goToPeople = goToPeople,
        goToBack = goToBack,
        showEpisodeDetail = viewModel::showEpisodeDetail,
        hideEpisodeDetail = viewModel::hideEpisodeDetail,
        onShowSnackbar = onShowSnackbar,
        insertFavoriteTv = viewModel::insertTv,
        deleteFavoriteTv = viewModel::deleteTv,
        restart = viewModel::restart,
        onSelectSeason = viewModel::onSelectSeason
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvScreen(
    tvUiState: TvState,
    similarTvs: Flow<PagingData<SimilarMedia>>,
    tvReviews: LazyPagingItems<Review>,
    selectedEpisode: TvEpisode?,
    goToTv: (Int) -> Unit,
    goToPeople: (Int) -> Unit,
    goToBack: () -> Unit,
    showEpisodeDetail: (TvEpisode) -> Unit,
    hideEpisodeDetail: () -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    insertFavoriteTv: (Tv) -> Unit,
    deleteFavoriteTv: (Tv) -> Unit,
    restart: () -> Unit,
    onSelectSeason: (TvSeason) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (tvUiState) {
            is TvState.Loading -> {
                Log.d("loading...")
                LocalFirebaseLogHelper.current.sendLog(name = "TvScreen", message = "loading...")

                CircularProgressComponent(
                    modifier = Modifier
                        .semantics { contentDescription = "tvDetailLoading" }
                        .align(Alignment.Center)
                )
            }
            is TvState.Success -> {
                Log.d("${tvUiState.tvUiState.tv}")
                LocalFirebaseLogHelper.current.sendLog(name = "TvScreen", message = "$tvUiState")

                var selectedImage by remember { mutableStateOf<Image?>(value = null) }
                val onSelect: (Image) -> Unit = { image ->
                    selectedImage = image
                }

                SharedTransitionLayout {
                    TvDetailComponent(
                        tv = tvUiState.tvUiState,
                        isAutoPlayTrailer = tvUiState.tvUiState.autoPlayTrailer,
                        similarTvs = similarTvs,
                        tvReviews = tvReviews,
                        goToTv = goToTv,
                        goToPeople = goToPeople,
                        goToBack = goToBack,
                        showEpisodeDetail = showEpisodeDetail,
                        onShowSnackbar = onShowSnackbar,
                        insertFavoriteTv = insertFavoriteTv,
                        deleteFavoriteTv = deleteFavoriteTv,
                        selectedImage = selectedImage,
                        onSelect = onSelect,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        onSelectSeason = onSelectSeason
                    )

                    ImageOverlay(
                        selectedImage = selectedImage,
                        onDismiss = { selectedImage = null }
                    )
                }

                if (selectedEpisode != null) {
                    EpisodeDetailBottomSheetDialog(
                        episode = selectedEpisode,
                        state = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        scope = rememberCoroutineScope(),
                        goToPeople = goToPeople,
                        onDismiss = { hideEpisodeDetail() }
                    )
                }
            }
            is TvState.Error -> {
                Log.e(tvUiState.throwable.message ?: "something wrong")
                LocalFirebaseLogHelper.current.sendLog(name = "TvScreen", message = tvUiState.throwable.message ?: "something wrong")

                val message = tvUiState.throwable.stringRes?.let { stringResource(id = it) } ?: stringResource(id = com.cheeke.surfy.core.network.api.R.string.something_wrong)

                ConfirmDialog(
                    title = stringResource(id = com.cheeke.surfy.core.network.api.R.string.network_failed),
                    message = message,
                    confirmPair = stringResource(id = com.cheeke.surfy.core.ui.R.string.retry_message) to { restart() },
                    dismissPair = stringResource(id = com.cheeke.surfy.core.ui.R.string.back_message) to goToBack
                )
            }
        }
    }
}

@Composable
fun TvDetailComponent(
    tv: TvUiState,
    isAutoPlayTrailer: Boolean,
    similarTvs: Flow<PagingData<SimilarMedia>>,
    tvReviews: LazyPagingItems<Review>,
    goToTv: (Int) -> Unit,
    goToPeople: (Int) -> Unit,
    goToBack: () -> Unit,
    showEpisodeDetail: (TvEpisode) -> Unit,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    insertFavoriteTv: (Tv) -> Unit,
    deleteFavoriteTv: (Tv) -> Unit,
    selectedImage: Image?,
    onSelect: (Image) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    onSelectSeason: (TvSeason) -> Unit
) {
    val favoriteMessage = if (tv.tv.isFavorite) stringResource(id = R.string.add_favorite_movie) else stringResource(id = R.string.remove_favorite_movie)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val analyticsHelper = LocalAnalyticsHelper.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
    ) {
        TitleComponent(
            isFavorite = tv.tv.isFavorite,
            goToBack = goToBack,
            onFavorite = {
                if (tv.tv.isFavorite) {
                    deleteFavoriteTv(tv.tv)
                    analyticsHelper.logFavorite(isFavorite = false, contentType = "tv", id = tv.tv.id ?: -1, title = tv.tv.title.orEmpty())
                } else {
                    insertFavoriteTv(tv.tv)
                    analyticsHelper.logFavorite(isFavorite = true, contentType = "tv", id = tv.tv.id ?: -1, title = tv.tv.title.orEmpty())
                }
                scope.launch {
                    onShowSnackbar(favoriteMessage, null)
                }
            }
        )
        tv.tv.videos?.results?.filter { it.site == "YouTube" }?.takeIf { it.isNotEmpty() }?.let { vods ->
            VideosComponent(scope = scope, vodList = vods, autoPlayTrailer = isAutoPlayTrailer)
        }
        MediaTitleComponent(media = tv.tv)
        tv.tv.alternativeTitles?.titles?.takeIf { it.isNotEmpty() }?.let { alternativeTitles ->
            AlternativeTitleComponent(alternativeTitles = alternativeTitles)
        }
        tv.tv.overview?.takeIf { it.trim().isNotEmpty() }?.let { overview ->
            OverviewComponent(overview = overview)
        }
        tv.tv.credits?.let { credits ->
            CreditsComponent(credits = credits, goToPeople = goToPeople)
        }
        tv.seasons.takeIf { it.isNotEmpty() }?.let { seasons ->
            SeasonComponent(
                tv = tv.tv,
                seasons = seasons,
                episodeState = tv.episodeState,
                episodesBySeason = tv.episodesBySeason,
                initialSeasonId = seasons.firstOrNull()?.id,
                onEpisodeClick = { episode ->
                    showEpisodeDetail(episode)
                    analyticsHelper.logSelectEpisode(
                        tvId = tv.tv.id.toString(),
                        tvTitle = tv.tv.title.toString(),
                        seasonName = seasons.find { it.seasonNumber == episode.seasonNumber }?.name.toString(),
                        seasonNumber = episode.seasonNumber.toString(),
                        episodeName = episode.name.toString(),
                        episodeNumber = episode.episodeNumber.toString()
                    )
                },
                onSelectSeason = onSelectSeason
            )
        }
        tv.tv.productionCompanies?.let { productionCompanies ->
            ProductionComponent(companies = productionCompanies)
        }
        tv.tv.images?.let {
            ImagesComponent(
                backdrops = it.backdrops.orEmpty(),
                posters = it.posters.orEmpty(),
                sharedTransitionScope = sharedTransitionScope,
                selectedImage = selectedImage,
                onSelect = onSelect
            )
        }
        if (!tv.tv.reviews?.results.isNullOrEmpty()) {
            ReviewComponent(
                items = tv.tv.reviews?.results.orEmpty(),
                reviews = tvReviews
            )
        }
        if (!tv.tv.similar?.results.isNullOrEmpty()) {
            SimilarComponent(
                similarMovies = similarTvs,
                items = tv.tv.similar?.results.orEmpty(),
                goToDestination = goToTv
            )
        }
        Spacer(modifier = Modifier.height(height = dp20))
    }
}

@Composable
fun SeasonComponent(
    tv: Tv,
    seasons: List<TvSeason>,
    episodesBySeason: Map<String, List<TvEpisode>>,
    initialSeasonId: Int? = seasons.firstOrNull()?.id,
    onEpisodeClick: (TvEpisode) -> Unit = {},
    onSelectSeason: (TvSeason) -> Unit,
    episodeState: TvSeasonLoadState
) {
    var expanded by remember { mutableStateOf(value = false) }
    var selectedSeasonId by rememberSaveable { mutableStateOf(value = initialSeasonId) }
    val selectedSeason = remember(key1 = selectedSeasonId, key2 = seasons) {
        seasons.firstOrNull { it.id == selectedSeasonId } ?: seasons.firstOrNull()
    }
    val episodes = remember(key1 = selectedSeason?.id, key2 = episodesBySeason) {
        selectedSeason?.name?.let { episodesBySeason[it].orEmpty() }.orEmpty()
    }
    val episodeListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val analyticsHelper = LocalAnalyticsHelper.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = dp227)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(start = dp16),
                text = "Episodes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                minLines = 1
            )

            SeasonSpinner(
                modifier = Modifier.padding(start = dp5, end = dp16),
                seasons = seasons,
                selected = selectedSeason,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onSelect = { season ->
                    onSelectSeason(season)
                    selectedSeasonId = season.id
                    expanded = false
                    scope.launch {
                        episodeListState.scrollToItem(index = 0)
                    }
                    analyticsHelper.logSelectSeason(
                        tvId = tv.id.toString(),
                        tvTitle = tv.title.orEmpty(),
                        seasonName = season.name.orEmpty(),
                        seasonNumber = season.seasonNumber.toString()
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(height = dp12))

        EpisodeContents(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f),
            episodeState = episodeState,
            episodes = episodes,
            episodeListState = episodeListState,
            onEpisodeClick = onEpisodeClick
        )
    }
}

@Composable
fun EpisodeContents(
    modifier: Modifier,
    episodeState: TvSeasonLoadState,
    episodes: List<TvEpisode>,
    episodeListState: LazyListState,
    onEpisodeClick: (TvEpisode) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (episodeState) {
            is TvSeasonLoadState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressComponent()
                    Text(
                        text = stringResource(id = R.string.tv_season_loading, episodeState.message.orEmpty()),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            is TvSeasonLoadState.Idle -> {
                if (episodes.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.empty_episode),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    EpisodeList(
                        episodes = episodes,
                        episodeListState = episodeListState,
                        onEpisodeClick = onEpisodeClick
                    )
                }
            }
            is TvSeasonLoadState.Error -> {
                Text(
                    text = stringResource(id = R.string.tv_season_error, episodeState.message.orEmpty()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeasonSpinner(
    modifier: Modifier,
    seasons: List<TvSeason>,
    selected: TvSeason?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (TvSeason) -> Unit
) {
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selected?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable)
                .widthIn(min = dp180, max = dp180),
            label = { Text(text = "Season") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            seasons.forEach { season ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = season.name.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    },
                    onClick = { onSelect(season) }
                )
            }
        }
    }
}

@Composable
private fun EpisodeList(
    episodes: List<TvEpisode>,
    episodeListState: LazyListState,
    onEpisodeClick: (TvEpisode) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(space = dp10),
        contentPadding = PaddingValues(start = dp16, end = dp16, bottom = dp8),
        state = episodeListState
    ) {
        items(
            items = episodes,
            key = { it.id ?: -1 }
        ) { e ->
            EpisodeRow(
                episode = e,
                onClick = { onEpisodeClick(e) }
            )
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: TvEpisode,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(width = dp150)
            .clickable(onClick = onClick),
    ) {
        DynamicAsyncImageLoader(
            source = episode.stillPath.orEmpty(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 16f / 9f)
                .clip(shape = RoundedCornerShape(size = dp12)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "E${episode.episodeNumber}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(height = dp2))
        Text(
            text = episode.name.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailBottomSheetDialog(
    episode: TvEpisode,
    state: SheetState,
    scope: CoroutineScope,
    goToPeople: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                onDismiss()
                state.hide()
            }
        },
        sheetState = state,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        sheetGesturesEnabled = false
    ) {
        val columnScrollState = rememberScrollState()

        Column(
            modifier = Modifier.verticalScroll(state = columnScrollState)
        ) {
            DynamicAsyncImageLoader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dp10, end = dp10, bottom = dp10)
                    .aspectRatio(ratio = 16f / 9f)
                    .clip(shape = RoundedCornerShape(size = dp10)),
                source = episode.stillPath.orEmpty(),
                contentDescription = episode.stillPath
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dp10),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = episode.name.orEmpty() }
                        .weight(weight = 1f),
                    text = episode.name.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                episode.runtime?.let {
                    Text(
                        modifier = Modifier.padding(start = dp5),
                        text = "${it}분",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                }
            }
            Text(
                modifier = Modifier.padding(horizontal = dp10),
                text = episode.airDate.orEmpty(),
                fontSize = sp10,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                modifier = Modifier.padding(horizontal = dp10),
                text = episode.overview.orEmpty(),
                fontSize = sp12,
                style = MaterialTheme.typography.labelLarge,
            )
            CreditsComponent(
                credits = Credits(
                    cast = episode.guestStars?.map {
                        Cast(
                            adult = it.adult,
                            castId = it.id,
                            character = it.character,
                            creditId = it.creditId,
                            gender = it.gender,
                            id = it.id,
                            knownForDepartment = it.knownForDepartment,
                            name = it.name,
                            order = it.order,
                            originalName = it.originalName,
                            popularity = it.popularity,
                            profilePath = it.profilePath
                        )
                    },
                    crew = episode.crew.orEmpty()
                ),
                goToPeople = goToPeople
            )
        }
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(height = dp20))
    }
}