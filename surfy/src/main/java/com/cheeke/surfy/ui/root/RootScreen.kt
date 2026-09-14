package com.cheeke.surfy.ui.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.cheeke.surfy.R
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.POSTER_IMAGE_RATIO
import com.cheeke.surfy.common.ScrollTopEvent
import com.cheeke.surfy.common.scrollToTop
import com.cheeke.surfy.datamanager.api.SurfyAppDataState
import com.cheeke.surfy.favorite.api.FavoriteNavKey
import com.cheeke.surfy.favorite.impl.favoriteEntry
import com.cheeke.surfy.firebase.api.LocalFirebaseLogHelper
import com.cheeke.surfy.home.api.HomeNavKey
import com.cheeke.surfy.home.impl.homeEntry
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.model.SearchType
import com.cheeke.surfy.navigation.TOP_LEVEL_NAV_ITEMS
import com.cheeke.surfy.ui.BottomNavigationBarItem
import com.cheeke.surfy.ui.MovieNavigationDefaults
import com.cheeke.surfy.ui.dialog.Indexer
import com.cheeke.surfy.ui.image.DynamicAsyncImageLoader
import com.cheeke.surfy.ui.utils.Line
import com.cheeke.surfy.ui.utils.border
import com.cheeke.surfy.ui.utils.bounceClick
import com.cheeke.surfy.ui.utils.dp1
import com.cheeke.surfy.ui.utils.dp10
import com.cheeke.surfy.ui.utils.dp16
import com.cheeke.surfy.ui.utils.dp20
import com.cheeke.surfy.ui.utils.dp300
import com.cheeke.surfy.ui.utils.dp40
import com.cheeke.surfy.ui.utils.dp5
import com.cheeke.surfy.ui.utils.dp50
import com.cheeke.surfy.ui.utils.roundedCornerClickable
import com.cheeke.surfy.ui.utils.sp15
import com.cheeke.surfy.ui.utils.sp20
import com.cheeke.surfy.utils.VerticalRollingAnimation

@Composable
fun RootScreen(
    goToMovie: (Int) -> Unit,
    goToPeople: (Int) -> Unit,
    goToTv: (Int) -> Unit,
    goToSearch: (String, SearchType) -> Unit,
    showSettingDialog: () -> Unit,
    viewModel: RootVM = hiltViewModel<RootVM>()
) {
    val backstack = rememberNavBackStack(HomeNavKey)
    val snackbarHostState = remember { SnackbarHostState() }
    val nextWeekReleaseDialogItems by viewModel.nextWeekReleaseMedias.collectAsStateWithLifecycle()
    val bottomDeeplink by viewModel.bottomDeeplink.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(key1 = bottomDeeplink) {
        if (bottomDeeplink.isEmpty()) return@LaunchedEffect

        bottomDeeplink.forEach { route ->
            backstack.add(element = route)
        }

        viewModel.consumeBottomDeepLink()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(modifier = Modifier.semantics { contentDescription = "snackbar" }, hostState = snackbarHostState) },
        topBar = {
            MovieSearchTopBar(
                nextWeekReleaseMovies = nextWeekReleaseDialogItems,
                goToMovie = goToMovie,
                goToTv = goToTv,
                goToSearch = goToSearch,
                showSettingDialog = showSettingDialog
            )
        },
        bottomBar = { MovieBottomBar(backstack = backstack) }
    ) { paddingValues ->
        val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
        val notConnectedMessage = stringResource(id = R.string.not_connected)
        val firebaseLog = LocalFirebaseLogHelper.current
        val retryMessage = stringResource(id = R.string.network_retry)
        val surfyAppDataState by viewModel.surfyDataManager.surfyAppData.collectAsStateWithLifecycle()

        LaunchedEffect(key1 = surfyAppDataState) {
            val errorState = surfyAppDataState as? SurfyAppDataState.Error ?: return@LaunchedEffect

            firebaseLog.sendLog("MovieMainScreen", "error ${errorState.throwable}")

            val result = snackbarHostState.showSnackbar(
                message = notConnectedMessage,
                actionLabel = retryMessage,
                duration = SnackbarDuration.Indefinite
            )

            when (result) {
                SnackbarResult.ActionPerformed -> viewModel.surfyDataManager.retry()
                SnackbarResult.Dismissed -> {}
            }
        }

        val entryProvider = entryProvider {
            homeEntry(
                goToMovie = goToMovie,
                goToPeople = goToPeople,
                goToTv = goToTv
            )
            favoriteEntry(
                goToMovie = goToMovie,
                goToTv = goToTv,
                goToPeople = goToPeople,
                onShowSnackbar = { message, action ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = action,
                        duration = SnackbarDuration.Short,
                    ) == SnackbarResult.ActionPerformed
                }
            )
        }

        NavDisplay(
            modifier = Modifier.padding(paddingValues = paddingValues),
            backStack = backstack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(), // 백 스택의 항목 상태를 관리하는 객체
                rememberViewModelStoreNavEntryDecorator() // 각 컴포저블 화면마다 독립적인 뷰모델을 사용하는 객체
            ),
            entryProvider = entryProvider,
            onBack = {
                if (backstack.last() != HomeNavKey) {
                    backstack.clear()
                    backstack.add(element = HomeNavKey)
                }
            },
        )
    }
}

@Composable
fun MovieSearchTopBar(
    nextWeekReleaseMovies: List<Media>,
    goToMovie: (Int) -> Unit,
    goToTv: (Int) -> Unit,
    goToSearch: (String, SearchType) -> Unit,
    showSettingDialog: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(top = dp10, bottom = dp10, start = dp16)
                .weight(weight = 1f)
                .height(height = dp40)
                .clip(shape = RoundedCornerShape(percent = 50))
                .background(color = MaterialTheme.colorScheme.inverseOnSurface)
                .roundedCornerClickable(onClick = { goToSearch("", SearchType.MULTI) }),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = dp20)
                        .align(Alignment.CenterVertically)
                        .clickable { goToSearch("", SearchType.MULTI) },
                    imageVector = Icons.Default.Search,
                    contentDescription = "goToSearch",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                if (nextWeekReleaseMovies.isEmpty()) {
                    Text(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(start = dp10),
                        text = stringResource(id = R.string.go_to_search),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (nextWeekReleaseMovies.size == 1) {
                    val nextWeekReleaseMovie = nextWeekReleaseMovies.first()

                    Text(
                        modifier = Modifier
                            .wrapContentWidth()
                            .clickable {
                                when (nextWeekReleaseMovie.mediaType) {
                                    MediaType.MOVIE -> goToMovie(nextWeekReleaseMovie.id ?: -1)
                                    MediaType.TV -> goToTv(nextWeekReleaseMovie.id ?: -1)
                                    else -> throw RuntimeException("${nextWeekReleaseMovie.mediaType} not found")
                                }
                            },
                        text = stringResource(id = R.string.next_week_release_movie, nextWeekReleaseMovie.title.orEmpty()),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    VerticalRollingAnimation(
                        modifier = Modifier.padding(start = dp10, end = dp20),
                        nextWeekReleaseMovies = nextWeekReleaseMovies,
                        goToMovie = { id -> goToMovie(id) },
                        goToTv = { id -> goToTv(id) }
                    )
                }
            }
        }

        Icon(
            modifier = Modifier
                .size(size = 48.dp)
                .padding(start = dp5, end = dp16)
                .bounceClick(onClick = { showSettingDialog() }),
            imageVector = Icons.Rounded.Settings,
            contentDescription = "SettingIcon"
        )
    }
}

@Composable
fun MovieBottomBar(
    backstack: NavBackStack<NavKey>
) {
    LocalFirebaseLogHelper.current.sendLog("Navigation", "create navigation bar")

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = dp50)
            .border(
                line = Line.TOP,
                strokeWidth = dp1,
                color = MovieNavigationDefaults.navigationBorderColor()
            ),
        containerColor = MovieNavigationDefaults.navigationContainerColor(),
        contentColor = MovieNavigationDefaults.navigationContentColor()
    ) {
        TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
            BottomNavigationBarItem(
                selected = navKey.javaClass.simpleName == backstack.last().javaClass.simpleName,
                label = stringResource(id = navItem.titleTextId),
                selectedIcon = navItem.selectedIcon,
                unSelectedIcon = navItem.unselectedIcon,
                onClick = {
                    when (navKey) {
                        backstack.last() -> {
                            when (navKey::class.java.simpleName) {
                                HomeNavKey::class.java.simpleName -> scrollToTop(event = ScrollTopEvent.Home)
                                FavoriteNavKey::class.java.simpleName -> scrollToTop(event = ScrollTopEvent.Favorite)
                            }
                        }
                        HomeNavKey -> {
                            backstack.clear()
                            backstack.add(element = HomeNavKey)
                        }
                        else -> backstack.add(element = navKey)
                    }
                }
            )
        }
    }
}

@Composable
fun ReleaseMoviesDialog(
    updateShowNextReleaseMoviesDate: () -> Unit,
    releaseMovies: List<Media>,
    goToMovie: (Int) -> Unit,
    goToTv: (Int) -> Unit,
    dismissNextWeekReleaseDialog: () -> Unit
) {
    Dialog(
        onDismissRequest = { dismissNextWeekReleaseDialog() },
        properties = DialogProperties(
            windowTitle = "NextWeekReleaseMoviesNavKey",
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        content = {
            val pagerState = rememberPagerState(initialPage = 0) { releaseMovies.size }

            Column(
                modifier = Modifier
                    .width(width = dp300)
                    .background(color = Color.White, shape = RoundedCornerShape(size = dp10))
                    .verticalScroll(state = rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HorizontalPager(
                    modifier = Modifier.fillMaxWidth(),
                    state = pagerState,
                ) { index ->
                    Log.d("NextWeekReleaseMovies Index -> $index")
                    Box {
                        DynamicAsyncImageLoader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (releaseMovies[index].mediaType) {
                                        MediaType.MOVIE -> goToMovie(releaseMovies[index].id ?: -1)
                                        MediaType.TV -> goToTv(releaseMovies[index].id ?: -1)
                                        else -> Log.d("mediatype not found...")
                                    }
                                    dismissNextWeekReleaseDialog()
                                }
                                .aspectRatio(ratio = POSTER_IMAGE_RATIO)
                                .clip(shape = RoundedCornerShape(topStart = dp10, topEnd = dp10)),
                            source = "${releaseMovies[index].posterPath}",
                            contentDescription = "ReleaseMovieImage"
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(color = Color(color = 0x33000000)),
                            text = stringResource(id = com.cheeke.surfy.feature.home.impl.R.string.release_movie, releaseMovies[pagerState.currentPage].releaseDate.orEmpty()),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                        Indexer(
                            modifier = Modifier
                                .padding(top = dp10, end = dp10)
                                .wrapContentSize()
                                .background(
                                    color = Color(color = 0x33000000),
                                    shape = RoundedCornerShape(size = dp20)
                                )
                                .align(Alignment.TopEnd),
                            current = pagerState.currentPage + 1,
                            size = pagerState.pageCount
                        )
                    }
                }
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.coming_soon_movie),
                    color = Color.Black
                )
                Button(
                    modifier = Modifier.padding(horizontal = dp16, vertical = dp10),
                    onClick = { dismissNextWeekReleaseDialog() }
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(size = dp20)),
                        text = stringResource(id = R.string.close),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = sp20,
                        color = Color.White
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(bottom = dp10)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable { updateShowNextReleaseMoviesDate() },
                    text = stringResource(id = R.string.no_show_today),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = sp15,
                    color = Color.Black
                )
            }
        }
    )
}