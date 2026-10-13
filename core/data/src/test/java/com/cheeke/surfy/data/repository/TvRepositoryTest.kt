package com.cheeke.surfy.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.cheeke.surfy.core.datastore.InternalDataPreferences
import com.cheeke.surfy.data.testdouble.TestTvDao
import com.cheeke.surfy.database.model.TvEntity
import com.cheeke.surfy.datastore.InternalDataSource
import com.cheeke.surfy.datastore_test.InMemoryDataStore
import com.cheeke.surfy.testing.TestTvRemoteDataSource
import com.cheeke.surfy.testing.model.tvEpisodeTestData
import com.cheeke.surfy.testing.model.tvSeasonTestData
import com.cheeke.surfy.testing.model.tvTestData
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
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
            apis = tvApis,
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

//    @Test
//    fun similarTvPagingTest() = runTest {
//        val pagingSource = repository.getSimilarTvPagingSource(id = 0, language = "ko", region = "KR")
//
//        val actual = pagingSource.load(
//            params = PagingSource.LoadParams.Refresh(
//                key = null,
//                loadSize = 101,
//                placeholdersEnabled = false
//            )
//        )
//
//        assertTrue(actual is PagingSource.LoadResult.Page)
//
//        val page = actual as PagingSource.LoadResult.Page
//
//        assertEquals(
//            expected = similarTvTestData.results?.mapIndexed { index, tv ->
//                SimilarMedia(
////                    genres = listOf(Genre(id = index)),
//                    title = tv.name,
//                    id = tv.id,
//                    posterPath = tv.posterPath,
//                    firstAirDate = "firstAirDate_$index",
//                    adult = true
//                )
//            }?.firstOrNull() ?: emptyList<Tv>(),
//            actual = page.data.firstOrNull() ?: emptyList<Tv>()
//        )
//        assertEquals(expected = null, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
//
//    @Test
//    fun tvReviewPagingTest() = runTest {
//        val pagingSource = repository.getTvReviews(seriesId = 0, language = "ko", region = "KR")
//
//        val actual = pagingSource.load(
//            params = PagingSource.LoadParams.Refresh(
//                key = null,
//                loadSize = 101,
//                placeholdersEnabled = false
//            )
//        )
//
//        assertTrue(actual is PagingSource.LoadResult.Page)
//
//        val page = actual as PagingSource.LoadResult.Page
//
//        assertEquals(expected = testTvReviews, actual = page.data)
//        assertEquals(expected = null, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
}