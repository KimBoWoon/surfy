package com.cheeke.surfy.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import com.cheeke.surfy.data.repository.MovieRepository
import com.cheeke.surfy.data.repository.TvRepository
import com.cheeke.surfy.notifications.Notifier
import com.cheeke.surfy.sync.utils.millisUntilNextMidnight
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

@HiltWorker
class UpComingNotificationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val movieRepository: MovieRepository,
    private val tvRepository: TvRepository,
    private val notifier: Notifier
) : RxWorker(appContext, workerParams) {
    companion object {
        const val WORKER_TAG = "UP_COMING_NOTIFICATION_WORKER"
        const val PERIODIC_WORKER_TAG = "PERIODIC_WORKER_TAG"
        const val EXPEDITED_SYNC_WORK_NAME = "EXPEDITED_SYNC_WORK_NAME"

        fun startUpExpeditedSyncWork(isForce: Boolean = false): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<DelegatingWorker>()
                .addTag(tag = EXPEDITED_SYNC_WORK_NAME)
                .setExpedited(policy = OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        fun startUpSyncWork(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<DelegatingWorker>()
                .addTag(tag = WORKER_TAG)
                .setInitialDelay(duration = millisUntilNextMidnight(), timeUnit = TimeUnit.MILLISECONDS)
                .build()
    }

    override fun createWork(): Single<Result> =
        Single.zip(
            movieRepository.getNextWeekReleaseMovies(),
            tvRepository.getNextWeekReleaseTvs()
        ) { movies, tvs ->
            (movies + tvs).sortedBy { it.releaseDate }
        }.doOnSuccess { notifier.postMovieNotifications(movies = it) }
            .map { Result.success() }
}