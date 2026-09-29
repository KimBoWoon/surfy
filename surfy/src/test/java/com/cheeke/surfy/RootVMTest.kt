package com.cheeke.surfy

import android.net.Uri
import androidx.navigation3.runtime.NavKey
import app.cash.turbine.test
import com.cheeke.surfy.datamanager.api.TestSurfyAppData
import com.cheeke.surfy.deeplink.DeepLinkManager
import com.cheeke.surfy.deeplink.parseDeeplink
import com.cheeke.surfy.detail.api.TestMovieDatabaseRepository
import com.cheeke.surfy.detail.api.TestTvDatabaseRepository
import com.cheeke.surfy.favorite.api.FavoriteNavKey
import com.cheeke.surfy.home.api.HomeNavKey
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.network.api.TestNetworkMonitor
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.ui.root.RootVM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RootVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var deepLinkManager: TestDeepLinkManager
    private lateinit var testMovieAppDataManager: TestSurfyAppData
    private lateinit var movieRepository: TestMovieDatabaseRepository
    private lateinit var tvRepository: TestTvDatabaseRepository
    private lateinit var networkMonitor: TestNetworkMonitor
    private lateinit var viewModel: RootVM
    private val media1 = Movie(
        id = 1,
        releaseDate = LocalDate.now().plusDays(3).toString()
    )
    private val media2 = Movie(
        id = 2,
        releaseDate = LocalDate.now().plusDays(4).toString()
    )
    private val media3 = Tv(
        id = 3,
        releaseDate = LocalDate.now().plusDays(1).toString()
    )

    @Before
    fun setUp() {
        deepLinkManager = TestDeepLinkManager()
        testMovieAppDataManager = TestSurfyAppData()
        movieRepository = TestMovieDatabaseRepository()
        tvRepository = TestTvDatabaseRepository()
        networkMonitor = TestNetworkMonitor()
        tvRepository.setTvs(listOf(media3))
        runBlocking {
            movieRepository.setMovies(listOf(media1, media2))
        }
        viewModel = RootVM(
            deepLinkManager = deepLinkManager,
            movieDataBaseRepository = movieRepository,
            tvDataBaseRepository = tvRepository,
            networkMonitor = networkMonitor,
            surfyDataManager = testMovieAppDataManager
        )
    }

    @Test
    fun `nextWeekReleaseMedias should combine and sort medias`() = runTest {
        backgroundScope.launch { viewModel.nextWeekReleaseMedias.collect() }

        assertEquals(
            expected = listOf(media3, media1, media2),
            actual = viewModel.nextWeekReleaseMedias.value
        )
    }

    @Test
    fun networkMonitorTest() = runTest {
        viewModel.isOffline.test {
            runCurrent()

            // initialValue
            assertFalse(actual = awaitItem())

            networkMonitor.setConnected(isConnected = false)
            assertTrue(actual = awaitItem())

            networkMonitor.setConnected(isConnected = true)
            assertFalse(actual = awaitItem())
        }
    }

    @Test
    fun `bottomDeeplink should expose deeplink from manager`() = runTest {
        deepLinkManager.emitBottomDeepLink(HomeNavKey, FavoriteNavKey())

        assertEquals(
            expected = viewModel.bottomDeeplink.first(),
            actual = deepLinkManager.bottomDeeplink.value
        )
    }

    @Test
    fun `consumeBottomDeepLink should call manager consume`() = runTest {
        viewModel.consumeBottomDeepLink()

        assertTrue(actual = deepLinkManager.consumeBottomDeepLinkCalled)
    }
}

class TestDeepLinkManager : DeepLinkManager {
    private val _rootDeeplink = MutableStateFlow<List<NavKey>>(emptyList())
    override val rootDeeplink = _rootDeeplink.asStateFlow()
    private val _bottomDeeplink = MutableStateFlow<List<NavKey>>(emptyList())
    override val bottomDeeplink = _bottomDeeplink.asStateFlow()

    var consumeRootDeepLinkCalled = false
        private set

    var consumeBottomDeepLinkCalled = false
        private set

    fun emitRootDeepLink(vararg navKeys: NavKey) {
        _rootDeeplink.value = navKeys.toList()
    }

    fun emitBottomDeepLink(vararg navKeys: NavKey) {
        _bottomDeeplink.value = navKeys.toList()
    }

    override fun handleDeepLink(uri: Uri?) {
        val stack = parseDeeplink(uri = uri)
        if (stack.isEmpty()) {
            return
        }

        val rootDeepLink = mutableListOf<NavKey>()
        val bottomDeepLink = mutableListOf<NavKey>()

        stack.forEach { route ->
            when(route) {
                is HomeNavKey, is FavoriteNavKey -> bottomDeepLink.add(element = route)
                else -> rootDeepLink.add(element = route)
//                bottomNavKeys.any { it.java.simpleName == route::class.java.simpleName } -> bottomDeepLink.add(element = route)
//                rootNavKeys.any { it.java.simpleName == route::class.java.simpleName } -> rootDeepLink.add(element = route)
//                else -> throw RuntimeException("잘못된 deeplink 입니다. : $route")
            }
        }

        _rootDeeplink.value = rootDeepLink
        _bottomDeeplink.value = bottomDeepLink
//        val root = mutableListOf<NavKey>()
//        val bottom = mutableListOf<NavKey>()
//
//        navKeys.forEach { key ->
//            when (key) {
//                is HomeNavKey,
//                is FavoriteNavKey -> bottom += key
//                else -> root += key
//            }
//        }
//
//        _rootDeeplink.value = root
//        _bottomDeeplink.value = bottom
    }

    override fun consumeRootDeepLink() {
        consumeRootDeepLinkCalled = true
        _rootDeeplink.value = emptyList()
    }

    override fun consumeBottomDeepLink() {
        consumeBottomDeepLinkCalled = true
        _bottomDeeplink.value = emptyList()
    }
}