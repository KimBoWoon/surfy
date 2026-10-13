package com.cheeke.surfy.detail

import com.cheeke.surfy.detail.tv.TvState
import com.cheeke.surfy.detail.tv.TvUiState
import com.cheeke.surfy.detail.tv.TvVM
import com.cheeke.surfy.domain.GetTvDetailUseCase
import com.cheeke.surfy.domain.TvSeasonLoadState
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeason
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.testing.repository.TestTvRepository
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.testing.utils.TestAnalyticsHelper
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class TvVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testTvRepository = TestTvRepository()
    private val testUserDataRepository = TestUserDataRepository()
    private val testAnalyticsHelper = TestAnalyticsHelper()
    private val getTvDetailUseCase = GetTvDetailUseCase(
        tvRepository = testTvRepository
    )
    private lateinit var viewModel: TvVM
    private val tv = Tv(id = 0)
    private val tvEpisode = TvEpisode(
        id = 0,
        episodeNumber = 0
    )
    private val tvSeason = TvSeason(
        id = 0,
        seasonNumber = 1,
        name = "Season1",
        posterPath = "/tvSeason.png"
    )
    private val tvSeasons = TvSeasons(
        id = 0,
        seasonNumber = 0,
        name = "Season1",
        episodes = listOf(tvEpisode)
    )

    @Before
    fun setup() {
        viewModel = TvVM(
            id = 0,
            tvRepository = testTvRepository,
            getTvDetailUseCase = getTvDetailUseCase,
            analyticsHelper = testAnalyticsHelper,
            userDataRepository = testUserDataRepository
        )
    }

    @Test
    fun loadingTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Loading
        )
    }

    @Test
    fun successTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Loading
        )
        testTvRepository.setTv(tv)
        testTvRepository.setTvSeason(tvSeasons)
        testTvRepository.setTvEpisode(tvEpisode)
        testTvRepository.insert(media = Tv(id = 124))
        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Success(
                TvUiState(
                    tv = tv.copy(isFavorite = false),
                    autoPlayTrailer = true,
                    seasons = tv.seasons.orEmpty(),
                    episodeState = TvSeasonLoadState.Idle,
                    episodesBySeason = emptyMap()
                )
            )
        )
    }

    @Test
    fun insertTvTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Loading
        )
        testTvRepository.setTv(tv)
        testTvRepository.setTvSeason(tvSeasons)
        testTvRepository.setTvEpisode(tvEpisode)
        testTvRepository.insert(media = Tv(id = 124))
        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Success(
                TvUiState(
                    tv = tv.copy(isFavorite = false),
                    autoPlayTrailer = true,
                    seasons = tv.seasons.orEmpty(),
                    episodeState = TvSeasonLoadState.Idle,
                    episodesBySeason = emptyMap()
                )
            )
        )
        viewModel.insertTv(tv)
        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Success(
                TvUiState(
                    tv = tv.copy(isFavorite = true),
                    autoPlayTrailer = true,
                    seasons = tv.seasons.orEmpty(),
                    episodeState = TvSeasonLoadState.Idle,
                    episodesBySeason = emptyMap()
                )
            )
        )
    }

    @Test
    fun deleteTvTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Loading
        )
        testTvRepository.setTv(tv)
        testTvRepository.setTvSeason(tvSeasons)
        testTvRepository.setTvEpisode(tvEpisode)
        testTvRepository.insert(media = tv)
        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Success(
                TvUiState(
                    tv = tv.copy(isFavorite = true),
                    autoPlayTrailer = true,
                    seasons = tv.seasons.orEmpty(),
                    episodeState = TvSeasonLoadState.Idle,
                    episodesBySeason = emptyMap()
                )
            )
        )
        viewModel.deleteTv(tv)
        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Success(
                TvUiState(
                    tv = tv.copy(isFavorite = false),
                    autoPlayTrailer = true,
                    seasons = tv.seasons.orEmpty(),
                    episodeState = TvSeasonLoadState.Idle,
                    episodesBySeason = emptyMap()
                )
            )
        )
    }

    @Test
    fun showEpisodeDetailTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.selectedEpisode.collect() }

        assertEquals(
            expected = viewModel.selectedEpisode.value,
            actual = null
        )
        viewModel.showEpisodeDetail(episode = tvEpisode)
        assertEquals(
            expected = viewModel.selectedEpisode.value,
            actual = tvEpisode
        )
    }

    @Test
    fun hideEpisodeDetailTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.selectedEpisode.collect() }

        assertEquals(
            expected = viewModel.selectedEpisode.value,
            actual = null
        )
        viewModel.showEpisodeDetail(episode = tvEpisode)
        assertEquals(
            expected = viewModel.selectedEpisode.value,
            actual = tvEpisode
        )
        viewModel.hideEpisodeDetail()
        assertEquals(
            expected = viewModel.selectedEpisode.value,
            actual = null
        )
    }

    @Test
    fun selectedSeasonTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Loading
        )
        testTvRepository.setTv(tv)
        testTvRepository.setTvSeason(tvSeasons)
        testTvRepository.setTvEpisode(tvEpisode)
        testTvRepository.insert(media = Tv(id = 124))
        viewModel.onSelectSeason(season = tvSeason)
        assertEquals(
            expected = viewModel.uiState.value,
            actual = TvState.Success(
                TvUiState(
                    tv = tv.copy(isFavorite = false),
                    autoPlayTrailer = true,
                    seasons = tv.seasons.orEmpty(),
                    episodeState = TvSeasonLoadState.Idle,
                    episodesBySeason = mapOf("Season1" to listOf(tvEpisode))
                )
            )
        )
    }
}