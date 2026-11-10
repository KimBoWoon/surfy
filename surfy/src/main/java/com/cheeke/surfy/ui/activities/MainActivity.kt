package com.cheeke.surfy.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.cheeke.surfy.R
import com.cheeke.surfy.analytics.api.AnalyticsHelper
import com.cheeke.surfy.analytics.api.LocalAnalyticsHelper
import com.cheeke.surfy.common.AppDoubleBackToExit
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.deeplink.DeepLinkManager
import com.cheeke.surfy.detail.api.movie.MovieNavKey
import com.cheeke.surfy.detail.api.tv.TvNavKey
import com.cheeke.surfy.firebase.api.LocalFirebaseLogHelper
import com.cheeke.surfy.firebase.api.LogHelper
import com.cheeke.surfy.ui.SurfyApp
import com.cheeke.surfy.ui.root.ReleaseMoviesDialog
import com.cheeke.surfy.ui.root.navigation.RootNavKey
import com.cheeke.surfy.ui.setting.SettingScreen
import com.cheeke.surfy.ui.setting.SettingVM
import com.cheeke.surfy.ui.setting.SettingsAction
import com.cheeke.surfy.ui.theme.SurfyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val LOG_TAG = "MainActivity"
private const val EXTRA_IS_DEEPLINK = "isDeeplink"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainVM by viewModels()
    private val settingVM: SettingVM by viewModels()
    @Inject
    lateinit var surfyFirebase: LogHelper
    @Inject
    lateinit var analyticsHelper: AnalyticsHelper
    @Inject
    lateinit var deepLinkManager: DeepLinkManager
    private val doubleBackToExitHandler = AppDoubleBackToExit()
    // onNewIntent 로 들어온 딥링크도 반영되도록 State 로 보관
    private var isDeeplink by mutableStateOf(value = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        surfyFirebase.sendLog(name = LOG_TAG, message = "create MainActivity")

        setUpSplashScreen(splashScreen = splashScreen)
        setUpDoubleBackToExit()
        handleIncomingIntent(
            incomingIntent = intent,
            // 화면 회전/프로세스 복원 시 같은 딥링크를 다시 처리하지 않도록 최초 생성에서만 처리
            shouldHandleDeepLink = savedInstanceState == null
        )

        setContent {
            CompositionLocalProvider(
                LocalFirebaseLogHelper provides surfyFirebase,
                LocalAnalyticsHelper provides analyticsHelper
            ) {
                MainContent(
                    mainViewModel = viewModel,
                    settingViewModel = settingVM,
                    deepLinkManager = deepLinkManager,
                    isDeeplink = isDeeplink,
                    onApplyEdgeToEdge = ::applyEdgeToEdge
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("onNewIntent")
        setIntent(intent)
        handleIncomingIntent(incomingIntent = intent, shouldHandleDeepLink = true)
    }

    private fun handleIncomingIntent(incomingIntent: Intent?, shouldHandleDeepLink: Boolean) {
        isDeeplink = incomingIntent?.getBooleanExtra(EXTRA_IS_DEEPLINK, false) ?: false
        if (shouldHandleDeepLink) {
            deepLinkManager.handleDeepLink(uri = incomingIntent?.data)
        }
    }

    /** true -> 스플래쉬 화면 노출, false -> 스플래쉬 화면 미노출 */
    private fun setUpSplashScreen(splashScreen: SplashScreen) {
        splashScreen.setKeepOnScreenCondition {
            viewModel.surfyAppData.value.shouldKeepSplashScreen()
        }
    }

    private fun setUpDoubleBackToExit() {
        onBackPressedDispatcher.addCallback(
            owner = this,
            onBackPressedCallback = object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    doubleBackToExitHandler.handleBackPressed(
                        onFirstPress = {
                            Toast.makeText(
                                this@MainActivity,
                                R.string.double_back_message,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onExit = { finish() }
                    )
                }
            }
        )
    }

    private fun applyEdgeToEdge(darkTheme: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { darkTheme }
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = lightScrim,
                darkScrim = darkScrim,
                detectDarkMode = { darkTheme }
            )
        )
    }
}

@Composable
private fun MainContent(
    mainViewModel: MainVM,
    settingViewModel: SettingVM,
    deepLinkManager: DeepLinkManager,
    isDeeplink: Boolean,
    onApplyEdgeToEdge: (darkTheme: Boolean) -> Unit,
) {
    val firebaseLogHelper = LocalFirebaseLogHelper.current
    LaunchedEffect(key1 = Unit) {
        firebaseLogHelper.sendLog(name = LOG_TAG, message = "compose start!")
    }

    val appData by mainViewModel.surfyAppData.collectAsStateWithLifecycle()
    val darkTheme = appData.shouldUseDarkTheme(isSystemDarkTheme = isSystemInDarkTheme())

    DisposableEffect(key1 = darkTheme) {
        onApplyEdgeToEdge(darkTheme)
        onDispose { }
    }

    SurfyTheme(darkTheme = darkTheme) {
        val backstack = rememberNavBackStack(RootNavKey)
        val snackbarHostState = remember { SnackbarHostState() }
        val navigateTo: (NavKey) -> Unit = { navKey -> backstack.add(element = navKey) }

        RootDeepLinkEffect(
            deepLinkManager = deepLinkManager,
            onNavigate = navigateTo
        )

        if (!isDeeplink) {
            NextWeekReleaseDialogHost(
                mainViewModel = mainViewModel,
                onNavigate = navigateTo
            )
        }

        SurfyApp(
            backstack = backstack,
            snackbarHostState = snackbarHostState,
            showSettingDialog = { settingViewModel.onAction(action = SettingsAction.OpenMain) }
        )

        SettingScreen(viewModel = settingViewModel)
    }
}

/** 루트 딥링크가 들어오면 백스택에 쌓고 소비 처리 */
@Composable
private fun RootDeepLinkEffect(
    deepLinkManager: DeepLinkManager,
    onNavigate: (NavKey) -> Unit,
) {
    val rootDeeplink by deepLinkManager.rootDeeplink
        .collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(key1 = rootDeeplink) {
        if (rootDeeplink.isEmpty()) return@LaunchedEffect
        rootDeeplink.forEach { deepLinkKey -> onNavigate(deepLinkKey) }
        deepLinkManager.consumeRootDeepLink()
    }
}

/** 다음 주 개봉작 다이얼로그 (노출 조건은 ViewModel 이 결정) */
@Composable
private fun NextWeekReleaseDialogHost(
    mainViewModel: MainVM,
    onNavigate: (NavKey) -> Unit,
) {
    val releaseMedias by mainViewModel.nextWeekReleaseMedias.collectAsStateWithLifecycle()
    val shouldShowDialog by mainViewModel.shouldShowNextWeekReleaseDialog.collectAsStateWithLifecycle()

    if (!shouldShowDialog) return

    ReleaseMoviesDialog(
        releaseMovies = releaseMedias,
        goToMovie = { movieId -> onNavigate(MovieNavKey(id = movieId)) },
        goToTv = { tvId -> onNavigate(TvNavKey(id = tvId)) },
        updateShowNextReleaseMoviesDate = mainViewModel::dontShowNextWeekReleaseDialogToday,
        dismissNextWeekReleaseDialog = mainViewModel::dismissNextWeekReleaseDialog
    )
}

/**
 * 안드로이드에서 기본적으로 제공되는 라이트 모드
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * 안드로이드에서 기본적으로 제공되는 다크 모드
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)