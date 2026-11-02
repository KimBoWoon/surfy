package com.cheeke.curfy.notification.impl

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.toBitmap
import coil3.transform.RoundedCornersTransformation
import com.cheeke.surfy.common.Dispatcher
import com.cheeke.surfy.common.Dispatchers
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.di.ApplicationScope
import com.cheeke.surfy.core.notifications.impl.R
import com.cheeke.surfy.model.Media
import com.cheeke.surfy.model.MediaVisitor
import com.cheeke.surfy.model.Movie
import com.cheeke.surfy.model.People
import com.cheeke.surfy.model.Series
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.notification.api.Notifier
import com.cheeke.surfy.userdata.api.UserDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

// 채널 id 값을 바꾸면 사용자가 조정한 채널 설정이 초기화되므로 값은 유지한다.
private const val COMING_SOON_CHANNEL_ID = "MOVIE_NOTIFICATION_CHANNEL"
private const val MOVIE_NOTIFICATION_CHANNEL_ID = "MOVIE_NOTIFICATION_CHANNEL"
private const val COMING_SOON_GROUP = "SURFY_NOTIFICATION"
private const val GENERAL_NOTIFICATION_TAG = "general"
private const val SUMMARY_NOTIFICATION_TAG = "summary"
private const val SUMMARY_NOTIFICATION_ID = 0
private const val IMAGE_LOAD_CONCURRENCY = 3
private const val IMAGE_CORNER_RADIUS = 20f
private const val TARGET_ACTIVITY_NAME = "com.cheeke.surfy.ui.activities.MainActivity"
private const val DEEP_LINK_SCHEME_AND_HOST = "https://www.cheeke.surfy.com"
private const val DEEP_LINK_PATH_SEGMENT = "surfy"
private const val DEEP_LINK_BASE_PATH = "$DEEP_LINK_SCHEME_AND_HOST/$DEEP_LINK_PATH_SEGMENT"
const val DEEP_LINK_URI_PATTERN = "$DEEP_LINK_BASE_PATH/{id}"

private val NOTIFICATION_SMALL_ICON = com.cheeke.surfy.designsystem.R.drawable.ic_launcher_round

/** 알림 대상 종류. tag는 영화와 TV의 id가 같아도 알림이 서로 덮어쓰지 않게 해준다. */
private enum class ComingSoonKind(val tag: String, @param:StringRes val titleRes: Int) {
    MOVIE(tag = "movie", titleRes = R.string.coming_soon_movie),
    TV(tag = "tv", titleRes = R.string.coming_soon_tv)
}

private object ComingSoonKindResolver : MediaVisitor<ComingSoonKind?> {
    override fun visitMovie(movie: Movie): ComingSoonKind = ComingSoonKind.MOVIE
    override fun visitTv(tv: Tv): ComingSoonKind = ComingSoonKind.TV
    override fun visitPeople(people: People): ComingSoonKind? = null
    override fun visitSeries(series: Series): ComingSoonKind? = null
}

private data class NotificationTarget(
    val kind: ComingSoonKind,
    val id: Int,
    val contentText: String?,
    val posterPath: String?,
    val deepLinkUri: Uri
)

@Singleton
class SystemTrayNotifier @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    @param:Dispatcher(dispatcher = Dispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    private val userDataRepository: UserDataRepository
) : Notifier {
    private val notificationManager = NotificationManagerCompat.from(appContext)

    init {
        createNotificationChannel()
    }

    override fun postNotification(notificationId: Int, contentTitle: String, deepLinkUri: String) {
        if (!canPostNotifications()) return

        val notification = buildNotification {
            setContentTitle(contentTitle)
            setContentIntent(
                createDeepLinkPendingIntent(requestCode = notificationId, deepLinkUri = deepLinkUri.toUri())
            )
            setAutoCancel(true)
        }
        notificationManager.notify(GENERAL_NOTIFICATION_TAG, notificationId, notification)
    }

    override fun postMovieNotifications(movies: List<Media>) {
        if (!canPostNotifications()) return

        // 이미지를 받기 전에 대상부터 걸러서, 쓰지 않을 포스터를 내려받지 않는다. 개수 제한은 두지 않는다.
        val targets = movies.mapNotNull { media -> media.toNotificationTarget() }
        if (targets.isEmpty()) return

        applicationScope.launch(context = ioDispatcher) {
            try {
                postComingSoonNotifications(targets = targets)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                Log.printStackTrace(throwable)
            }
        }
    }

    private suspend fun postComingSoonNotifications(targets: List<NotificationTarget>) = coroutineScope {
        val baseImageUrl = userDataRepository.getSecureBaseUrl()
        val imageLoadPermits = Semaphore(permits = IMAGE_LOAD_CONCURRENCY)

        val posters = targets.map { target ->
            async {
                val posterPath = target.posterPath ?: return@async null
                imageLoadPermits.withPermit {
                    loadPosterBitmap(imageUrl = "$baseImageUrl$posterPath")
                }
            }
        }.awaitAll()

        targets.zip(other = posters).forEach { (target, poster) ->
            postTargetNotification(target = target, poster = poster)
        }
        postSummaryNotification()
    }

    private fun postTargetNotification(target: NotificationTarget, poster: Bitmap?) {
        val notification = buildNotification {
            setContentTitle(appContext.getString(target.kind.titleRes))
            setContentText(target.contentText)
            setContentIntent(
                createDeepLinkPendingIntent(requestCode = target.id, deepLinkUri = target.deepLinkUri)
            )
            setGroup(COMING_SOON_GROUP)
            setAutoCancel(true)
            setLargeIcon(poster)
        }
        notificationManager.notify(target.kind.tag, target.id, notification)
    }

    private fun postSummaryNotification() {
        val summary = buildNotification {
            setContentTitle(appContext.getString(R.string.coming_soon_movie))
            setGroup(COMING_SOON_GROUP)
            setGroupSummary(true)
            setAutoCancel(true)
        }
        notificationManager.notify(SUMMARY_NOTIFICATION_TAG, SUMMARY_NOTIFICATION_ID, summary)
    }

    private suspend fun loadPosterBitmap(imageUrl: String): Bitmap? {
        // 시스템이 권장하는 큰 아이콘 크기에 맞춘다.
        val sizePx = appContext.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
        return try {
            appContext.imageLoader.execute(
                request = ImageRequest.Builder(context = appContext)
                    .data(data = imageUrl)
                    .size(width = sizePx, height = sizePx)
                    .transformations(RoundedCornersTransformation(radius = IMAGE_CORNER_RADIUS))
                    .build()
            ).image?.toBitmap()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Log.printStackTrace(throwable)
            null
        }
    }

    private fun buildNotification(configure: NotificationCompat.Builder.() -> Unit): Notification =
        NotificationCompat.Builder(appContext, COMING_SOON_CHANNEL_ID)
            .setSmallIcon(NOTIFICATION_SMALL_ICON)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .apply(configure)
            .build()

    private fun createDeepLinkPendingIntent(requestCode: Int, deepLinkUri: Uri): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            component = ComponentName(appContext.packageName, TARGET_ACTIVITY_NAME)
            putExtra("isDeeplink", true)
        }
        return PendingIntent.getActivity(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(
            COMING_SOON_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        ).setName(appContext.getString(R.string.system_notification_channel_name))
            .setDescription(appContext.getString(R.string.coming_soon_movie))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    // 알림 전체 허용 여부를 확인한다. (Android 13+ 런타임 권한은 별도로 요청해야 한다.)
    private fun canPostNotifications(): Boolean = notificationManager.areNotificationsEnabled()
}

private fun Media.toNotificationTarget(): NotificationTarget? {
    val mediaId = id ?: return null
    val kind = accept(visitor = ComingSoonKindResolver) ?: return null
    return NotificationTarget(
        kind = kind,
        id = mediaId,
        contentText = title,
        posterPath = posterPath,
        deepLinkUri = toDeepLinkUri(mediaId = mediaId)
    )
}

private fun Media.toDeepLinkUri(mediaId: Int): Uri =
    "$DEEP_LINK_SCHEME_AND_HOST/$deepLinkPath?id=$mediaId".toUri()

fun Context.deepLinkPendingIntent(requestCode: Int, deepLinkUri: Uri): PendingIntent {
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = deepLinkUri
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        component = ComponentName(packageName, TARGET_ACTIVITY_NAME)
    }
    return PendingIntent.getActivity(
        this,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

fun Context.buildMovieNotification(block: (NotificationCompat.Builder) -> Unit): Notification {
    val builder = NotificationCompat.Builder(this, MOVIE_NOTIFICATION_CHANNEL_ID)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    block(builder)
    return builder.build()
}