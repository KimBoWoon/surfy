package com.cheeke.surfy.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.cheeke.surfy.data.repository.SyncRepository
import com.cheeke.surfy.data.repository.UserDataRepository
import com.cheeke.surfy.notifications.Notifier
import com.cheeke.surfy.notifications.SystemTrayNotifier
import com.cheeke.surfy.sync.workers.MidnightSyncWorker
import com.cheeke.surfy.sync.workers.delegatedData
import com.cheeke.surfy.testing.TestSyncRemoteDataSource
import com.cheeke.surfy.testing.repository.TestSyncRepository
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MidnightSyncWorkerTest {
    private lateinit var context: Context
    private lateinit var apis: TestSyncRemoteDataSource
    private lateinit var syncRepository: TestSyncRepository
    private lateinit var userDataRepository: TestUserDataRepository
    private lateinit var databaseRepository: TestMovieDatabaseRepository
    private lateinit var notifier: Notifier

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        apis = TestSyncRemoteDataSource()
        syncRepository = TestSyncRepository()
        userDataRepository = TestUserDataRepository()
        databaseRepository = TestMovieDatabaseRepository()
        notifier = SystemTrayNotifier(
            context = context,
            ioDispatcher = UnconfinedTestDispatcher(),
            userDataRepository = userDataRepository
        )
    }

    @Test
    fun syncSuccessTest() = runBlocking {
        syncRepository.setDate(LocalDate.now().minusDays(5))

        val worker = TestListenableWorkerBuilder<MidnightSyncWorker>(context)
            .setWorkerFactory(
                TestWorkerFactory(
                    syncRepository = syncRepository,
                    userDataRepository = userDataRepository,
                    databaseRepository = databaseRepository,
                    notifier = notifier
                )
            ).setInputData(MidnightSyncWorker::class.delegatedData(isForce = false))
            .build()

        val result = worker.doWork()

        assertEquals(expected = ListenableWorker.Result.success(), actual = result)
    }

    @Test
    fun isForceTest() = runBlocking {
        syncRepository.setIsForce(value = true)

        val worker = TestListenableWorkerBuilder<MidnightSyncWorker>(context)
            .setWorkerFactory(
                TestWorkerFactory(
                    syncRepository = syncRepository,
                    userDataRepository = userDataRepository,
                    databaseRepository = databaseRepository,
                    notifier = notifier
                )
            ).setInputData(MidnightSyncWorker::class.delegatedData(isForce = true))
            .build()

        val result = worker.doWork()

        assertEquals(expected = ListenableWorker.Result.success(), actual = result)
    }

    @Test
    fun failureTest() = runBlocking {
        val worker = TestListenableWorkerBuilder<MidnightSyncWorker>(context)
            .setWorkerFactory(
                TestWorkerFactory(
                    syncRepository = syncRepository,
                    userDataRepository = userDataRepository,
                    databaseRepository = databaseRepository,
                    notifier = notifier
                )
            ).setRunAttemptCount(6).build()

        val result = worker.doWork()

        assertEquals(expected = ListenableWorker.Result.failure(), actual = result)
    }

    @Test
    fun retryTest() = runBlocking {
        val worker = TestListenableWorkerBuilder<MidnightSyncWorker>(context)
            .setWorkerFactory(
                TestWorkerFactory(
                    syncRepository = syncRepository,
                    userDataRepository = userDataRepository,
                    databaseRepository = databaseRepository,
                    notifier = notifier
                )
            ).setRunAttemptCount(2).build()

        val result = worker.doWork()

        assertEquals(expected = ListenableWorker.Result.retry(), actual = result)
    }
}

class TestWorkerFactory(
    private val syncRepository: SyncRepository,
    private val userDataRepository: UserDataRepository,
    private val databaseRepository: DatabaseRepository,
    private val notifier: Notifier
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = MidnightSyncWorker(
        appContext = appContext,
        workerParams = workerParameters,
        ioDispatcher = Dispatchers.Unconfined,
        userDateRepository = userDataRepository,
        syncRepository = syncRepository,
        databaseRepository = databaseRepository,
        notifier = notifier
    )
}