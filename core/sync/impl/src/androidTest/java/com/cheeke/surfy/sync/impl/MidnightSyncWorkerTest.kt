package com.cheeke.surfy.sync.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.cheeke.surfy.detail.api.TestMovieRepository
import com.cheeke.surfy.network.api.TestSyncRemoteDataSource
import com.cheeke.surfy.sync.api.SyncRepository
import com.cheeke.surfy.sync.api.TestSyncRepository
import com.cheeke.surfy.sync.impl.workers.MidnightSyncWorker
import com.cheeke.surfy.sync.impl.workers.delegatedData
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import com.cheeke.surfy.userdata.api.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
    private lateinit var databaseRepository: TestMovieRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        apis = TestSyncRemoteDataSource()
        syncRepository = TestSyncRepository()
        userDataRepository = TestUserDataRepository()
        databaseRepository = TestMovieRepository()
    }

    @Test
    fun syncSuccessTest() = runBlocking {
        syncRepository.setDate(LocalDate.now().minusDays(5))

        val worker = TestListenableWorkerBuilder<MidnightSyncWorker>(context)
            .setWorkerFactory(
                TestWorkerFactory(
                    syncRepository = syncRepository,
                    userDataRepository = userDataRepository,
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
                )
            ).setRunAttemptCount(2).build()

        val result = worker.doWork()

        assertEquals(expected = ListenableWorker.Result.retry(), actual = result)
    }
}

class TestWorkerFactory(
    private val syncRepository: SyncRepository,
    private val userDataRepository: UserDataRepository
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
    )
}