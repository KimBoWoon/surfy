package com.cheeke.surfy.data.repository

import androidx.paging.testing.asSnapshot
import com.cheeke.surfy.model.MediaType
import com.cheeke.surfy.testing.TestTrendingRemoteDataSource
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class HomeRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var repository: HomeRepositoryImpl
    private lateinit var fakeTrendingRemoteDataSource: TestTrendingRemoteDataSource

    @Before
    fun setup() {
        fakeTrendingRemoteDataSource = TestTrendingRemoteDataSource()
        repository = HomeRepositoryImpl(
            trendingApis = fakeTrendingRemoteDataSource
        )
    }

    @Test
    fun `첫 로드는 전달한 파라미터와 page 1로 요청한다`() = runTest {
        repository.getTrending(
            mediaType = MediaType.MOVIE,
            timeWindow = "day",
            language = "ko-KR"
        ).asSnapshot()

        val firstRequest = fakeTrendingRemoteDataSource.requests.first()
        assertEquals(
            expected = TestTrendingRemoteDataSource.Request(
                mediaType = MediaType.MOVIE,
                timeWindow = "day",
                language = "ko-KR",
                page = 1
            ),
            actual = firstRequest
        )
    }

    @Test
    fun `스크롤하면 다음 page까지 이어서 로드한다`() = runTest {
        val loadedItems = repository.getTrending(
            mediaType = MediaType.MOVIE,
            timeWindow = "day",
            language = "ko-KR"
        ).asSnapshot {
            scrollTo(index = 20)
        }

        print(loadedItems.size)

        assertTrue(loadedItems.size >= 20)
        assertEquals(
            expected = listOf<Int>(1, 2),
            actual = fakeTrendingRemoteDataSource.requests.map { request -> request.page }.distinct().take(n = 2)
        )
    }

    @Test
    fun `마지막 page 이후에는 더 요청하지 않는다`() = runTest {
        fakeTrendingRemoteDataSource.totalPages = 1

        repository.getTrending(
            mediaType = MediaType.MOVIE,
            timeWindow = "day",
            language = "ko-KR"
        ).asSnapshot {
            scrollTo(index = 19)
        }

        assertTrue(fakeTrendingRemoteDataSource.requests.none { request -> request.page > 1 })
    }
}