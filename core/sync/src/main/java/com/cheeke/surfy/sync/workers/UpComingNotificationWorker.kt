package com.cheeke.surfy.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.cheeke.surfy.data.repository.MovieRepository
import com.cheeke.surfy.data.repository.TvRepository
import com.cheeke.surfy.notifications.Notifier
import com.cheeke.surfy.sync.utils.millisUntilNextMidnight
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class UpComingNotificationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val movieRepository: MovieRepository,
    private val tvRepository: TvRepository,
    private val notifier: Notifier
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val WORKER_NAME = "UP_COMING_NOTIFICATION_WORKER_NAME"
        const val WORKER_TAG = "UP_COMING_NOTIFICATION_WORKER"

        fun startUpSyncWork(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<UpComingNotificationWorker>()
                .addTag(tag = WORKER_TAG)
                .setInitialDelay(duration = millisUntilNextMidnight(), timeUnit = TimeUnit.MILLISECONDS)
                .build()
    }

    override suspend fun doWork(): Result {
        val nextReleaseMedias = movieRepository.getNextWeekReleaseMovies() + tvRepository.getNextWeekReleaseTvs()
        notifier.postMovieNotifications(movies = nextReleaseMedias.sortedBy { it.releaseDate })
        return Result.success()
    }
}