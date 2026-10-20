package com.cheeke.surfy.detail.impl

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.cheeke.surfy.analytics.AnalyticsEvent
import com.cheeke.surfy.analytics.AnalyticsHelper
import com.cheeke.surfy.detail.impl.tv.GetTvDetailUseCase
import com.cheeke.surfy.detail.tv.TvScreen
import com.cheeke.surfy.detail.tv.TvState
import com.cheeke.surfy.detail.tv.TvVM
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.network.model.SurfyNetworkException
import com.cheeke.surfy.testing.model.tvTestData
import com.cheeke.surfy.testing.repository.TestPagingRepository
import com.cheeke.surfy.testing.repository.TestTvDatabaseRepository
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TvScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var viewModel: TvVM
    private lateinit var getTvDetailUseCase: GetTvDetailUseCase
    private lateinit var databaseRepository: TestTvDatabaseRepository
    private lateinit var detailRepository: TestTvDetailRepository
    private lateinit var pagingRepository: TestPagingRepository
    private lateinit var userDataRepository: TestUserDataRepository

    @Before
    fun setup() {
        databaseRepository = TestTvDatabaseRepository()
        detailRepository = TestTvDetailRepository()
        pagingRepository = TestPagingRepository()
        userDataRepository = TestUserDataRepository()
        getTvDetailUseCase = GetTvDetailUseCase(
            repository = databaseRepository,
            detailRepository = detailRepository
        )
        viewModel = TvVM(
            id = 0,
            getTvDetailUseCase = getTvDetailUseCase,
            tvDataBaseRepository = databaseRepository,
            pagingRepository = pagingRepository,
            userDataRepository = userDataRepository,
            analyticsHelper = object : AnalyticsHelper {
                override fun logEvent(event: AnalyticsEvent) {
                    println("event: $event")
                }
            }
        )
    }

    @Test
    fun tvDetailLoadingTest() {
        composeTestRule.apply {
            setContent {
                val tvState by viewModel.uiState.collectAsStateWithLifecycle()
                val similarTvs = viewModel.similarTvs.collectAsLazyPagingItems()
                val selectedEpisode by viewModel.selectedEpisode.collectAsStateWithLifecycle()

                TvScreen(
                    tvUiState = tvState,
                    similarTvs = similarTvs,
                    selectedEpisode = selectedEpisode,
                    goToTv = {},
                    goToPeople = {},
                    goToBack = {},
                    showEpisodeDetail = viewModel::showEpisodeDetail,
                    hideEpisodeDetail = viewModel::hideEpisodeDetail,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteTv = viewModel::insertTv,
                    deleteFavoriteTv = viewModel::deleteTv,
                    restart = viewModel::restart,
                    onSelectSeason = viewModel::onSelectSeason
                )
            }

            onNodeWithContentDescription(label = "tvDetailLoading").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun tvDetailSuccessTest() = runTest {
        composeTestRule.apply {
            setContent {
                val tvState by viewModel.uiState.collectAsStateWithLifecycle()
//                val similarTvs = viewModel.similarTvs.collectAsLazyPagingItems()
                val selectedEpisode by viewModel.selectedEpisode.collectAsStateWithLifecycle()
                val pagingData = PagingData.from(data = listOf(element = SimilarMedia(id = 0, title = "tv_1", posterPath = "/tvImagePath.png")))
                val flow = MutableStateFlow(value = pagingData)

                TvScreen(
                    tvUiState = tvState,
//                    similarTvs = similarTvs,
                    similarTvs = flow.collectAsLazyPagingItems(),
                    selectedEpisode = selectedEpisode,
                    goToTv = {},
                    goToPeople = {},
                    goToBack = {},
                    showEpisodeDetail = viewModel::showEpisodeDetail,
                    hideEpisodeDetail = viewModel::hideEpisodeDetail,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteTv = viewModel::insertTv,
                    deleteFavoriteTv = viewModel::deleteTv,
                    restart = viewModel::restart,
                    onSelectSeason = viewModel::onSelectSeason
                )
            }

            onNodeWithContentDescription(label = "tvDetailLoading").assertExists().assertIsDisplayed()

            databaseRepository.insert(media = Tv(id = 0, title = "tv_1", posterPath = "/tvImagePath.png"))
            detailRepository.setTvSeason(tvSeasons = TvSeasons())
            detailRepository.setTvEpisode(tvEpisode = TvEpisode())
            detailRepository.setTv(tv = tvTestData)

            onNodeWithContentDescription(label = "favorite").assertExists().assertIsDisplayed()
//            onNodeWithTag(testTag = "titleComponent").assertTextEquals(tvTestData.title.orEmpty()).assertIsDisplayed()
            onNodeWithText(text = tvTestData.originalTitle.orEmpty()).assertExists().assertIsDisplayed()
            onNodeWithText(text = tvTestData.overview.orEmpty()).assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun tvDetailErrorTest() = runTest {
        composeTestRule.apply {
            setContent {
                val similarTvs = viewModel.similarTvs.collectAsLazyPagingItems()
                val selectedEpisode by viewModel.selectedEpisode.collectAsStateWithLifecycle()

                TvScreen(
                    tvUiState = TvState.Error(throwable = SurfyNetworkException(throwable = Throwable("something wrong"))),
                    similarTvs = similarTvs,
                    selectedEpisode = selectedEpisode,
                    goToTv = {},
                    goToPeople = {},
                    goToBack = {},
                    showEpisodeDetail = viewModel::showEpisodeDetail,
                    hideEpisodeDetail = viewModel::hideEpisodeDetail,
                    onShowSnackbar = { _, _ -> true },
                    insertFavoriteTv = viewModel::insertTv,
                    deleteFavoriteTv = viewModel::deleteTv,
                    restart = viewModel::restart,
                    onSelectSeason = viewModel::onSelectSeason
                )
            }

            onNodeWithText(text = "알 수 없는 문제가 있습니다.").assertExists().assertIsDisplayed()
        }
    }
}