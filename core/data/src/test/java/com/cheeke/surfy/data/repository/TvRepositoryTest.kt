package com.cheeke.surfy.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import androidx.paging.testing.asSnapshot
import com.cheeke.surfy.core.datastore.InternalDataPreferences
import com.cheeke.surfy.data.testdouble.TestTvDao
import com.cheeke.surfy.database.model.TvEntity
import com.cheeke.surfy.datastore.InternalDataSource
import com.cheeke.surfy.datastore_test.InMemoryDataStore
import com.cheeke.surfy.model.Reviews
import com.cheeke.surfy.model.SimilarMedia
import com.cheeke.surfy.testing.TestTvRemoteDataSource
import com.cheeke.surfy.testing.model.similarTvTestData
import com.cheeke.surfy.testing.model.testTvReviewList
import com.cheeke.surfy.testing.model.tvEpisodeTestData
import com.cheeke.surfy.testing.model.tvSeasonTestData
import com.cheeke.surfy.testing.model.tvTestData
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class TvRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var tvApis: TestTvRemoteDataSource
    private lateinit var datastore: InternalDataSource
    private lateinit var userDataRepository: UserDataRepository
    private lateinit var tvDao: TestTvDao
    private lateinit var repository: TvRepositoryImpl
    private val tv = TvEntity(id = 0, posterPath = "posterPath_0", timestamp = 0L, name = "tv_0", firstAirDate = LocalDate.now().plusDays(1).toString(), lastAirDate = "lastAirDate_0")

    @Before
    fun setup() {
        tvDao = TestTvDao()
        tvApis = TestTvRemoteDataSource()
        datastore = InternalDataSource(
            datastore = InMemoryDataStore(initialValue = InternalDataPreferences.getDefaultInstance())
        )
        userDataRepository = TestUserDataRepository()
        repository = TvRepositoryImpl(
            requestOptionsProvider = DetailRequestOptionsProvider(userdata = userDataRepository),
            tvDao = tvDao,
            tvApis = tvApis
        )
    }

    @Test
    fun getFavoriteTest() = runTest {
        val favoriteMovieSource = repository.getFavorite()
        val favoriteMoviePager = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        assertEquals(
            expected = favoriteMoviePager.refresh(initialKey = 0),
            actual = favoriteMovieSource.load(
                params = PagingSource.LoadParams.Refresh(
                    key = null,
                    loadSize = 20,
                    placeholdersEnabled = false
                )
            )
        )
    }

    @Test
    fun isFavorite() = runTest {
        assertEquals(
            expected = tvDao.isFavoriteTv(id = 0).first(),
            actual = false
        )

        tvDao.insertOrIgnoreTvs(tv = tv)

        assertEquals(
            expected = tvDao.isFavoriteTv(id = 0).first(),
            actual = true
        )
    }

    @Test
    fun insert() = runTest {
        val favoritePagerBeforeInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        tvDao.insertOrIgnoreTvs(tv = tv)

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = tv, actual = result.data.first())
    }

    @Test
    fun delete() = runTest {
        val favoritePagerBeforeInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        tvDao.insertOrIgnoreTvs(tv = tv)

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = tv, actual = result.data.first())

        tvDao.deleteTv(id = tv.id)

        val favoritePagerAfterDelete = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterDelete.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)
    }

    @Test
    fun upsert() = runTest {
        val favoritePagerBeforeInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        tvDao.upsertTvs(entities = listOf(tv))

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = tv, actual = result.data.first())
    }

    @Test
    fun getNextWeekReleaseTvs() = runTest {
        val tv1 = TvEntity(id = 0, posterPath = "posterPath_0", timestamp = 0L, name = "tv_0", firstAirDate = LocalDate.now().plusDays(1).toString(), lastAirDate = "lastAirDate_0")
        val tv2 = TvEntity(id = 1, posterPath = "posterPath_1", timestamp = 0L, name = "tv_1", firstAirDate = LocalDate.now().minusDays(1).toString(), lastAirDate = "lastAirDate_1")
        val tv3 = TvEntity(id = 2, posterPath = "posterPath_2", timestamp = 0L, name = "tv_2", firstAirDate = LocalDate.now().plusDays(2).toString(), lastAirDate = "lastAirDate_2")

        tvDao.insertOrIgnoreTvs(tv = tv1)
        tvDao.insertOrIgnoreTvs(tv = tv2)
        tvDao.insertOrIgnoreTvs(tv = tv3)

        assertEquals(
            expected = tvDao.getNextWeekReleaseTvs(),
            actual = listOf(tv1, tv3)
        )
    }

    @Test
    fun getTvDetailTest() = runTest {
        val result = repository.getData(id = 0)

        assertEquals(expected = result.first(), actual = tvTestData)
    }

    @Test
    fun getTvSeasonTest() = runTest {
        val result = repository.getTvSeasons(seriesId = 0, seasonNumber = 1)

        assertEquals(expected = result.first(), actual = tvSeasonTestData)
    }

    @Test
    fun getTvEpisodeTest() = runTest {
        val result = repository.getTvEpisode(seriesId = 0, seasonNumber = 1, episodeNumber = 1)

        assertEquals(expected = result.first(), actual = tvEpisodeTestData)
    }

    @Test
    fun `유사 TV는 remote 결과를 SimilarMedia로 변환해 반환한다`() = runTest {
        val loadedItems = repository.getSimilarTvPagingSource(
            id = 0,
            language = "ko",
            region = "KR"
        ).asSnapshot()

        val expectedItems = similarTvTestData.results.orEmpty().mapIndexed { index, tv ->
            SimilarMedia(
                name = tv.name,
                id = tv.id,
                posterPath = tv.posterPath,
                firstAirDate = tv.firstAirDate,
                adult = true
            )
        }

        assertEquals(expected = expectedItems, actual = loadedItems)
    }

    @Test
    fun `유사 TV는 전달한 파라미터와 page 1로 요청한다`() = runTest {
        repository.getSimilarTvPagingSource(
            id = 7,
            language = "ko",
            region = "KR"
        ).asSnapshot()

        assertEquals(
            expected = TestTvRemoteDataSource.Request(
                seriesId = 7,
                language = "ko-KR",
                page = 1
            ),
            actual = tvApis.similarTvRequests.first()
        )
    }

    @Test
    fun `TV 리뷰는 remote 결과를 그대로 반환한다`() = runTest {
        val loadedItems = repository.getTvReviews(
            seriesId = 0,
            language = "ko",
            region = "KR"
        ).asSnapshot()

        assertEquals(expected = testTvReviewList, actual = loadedItems)
    }

    @Test
    fun `TV 리뷰는 전달한 파라미터와 page 1로 요청한다`() = runTest {
        repository.getTvReviews(
            seriesId = 7,
            language = "ko",
            region = "KR"
        ).asSnapshot()

        assertEquals(
            expected = TestTvRemoteDataSource.Request(
                seriesId = 7,
                language = "ko-KR",
                page = 1
            ),
            actual = tvApis.reviewRequests.first()
        )
    }

    @Test
    fun `TV 리뷰가 없으면 빈 리스트를 반환한다`() = runTest {
        tvApis.reviewsResponse = Reviews()

        val loadedItems = repository.getTvReviews(
            seriesId = 0,
            language = "ko",
            region = "KR"
        ).asSnapshot()

        assertTrue(loadedItems.isEmpty())
    }
}