package com.cheeke.surfy.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.cheeke.surfy.core.datastore.InternalDataPreferences
import com.cheeke.surfy.data.testdouble.TestPeopleDao
import com.cheeke.surfy.database.model.PeopleEntity
import com.cheeke.surfy.datastore.InternalDataSource
import com.cheeke.surfy.datastore_test.InMemoryDataStore
import com.cheeke.surfy.testing.TestPeopleRemoteDataSource
import com.cheeke.surfy.testing.model.combineCreditsTestData
import com.cheeke.surfy.testing.model.externalIdsTestData
import com.cheeke.surfy.testing.model.peopleDetailTestData
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class PeopleRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var movieApis: TestPeopleRemoteDataSource
    private lateinit var datastore: InternalDataSource
    private lateinit var repository: PeopleRepositoryImpl
    private lateinit var userDataRepository: UserDataRepository
    private lateinit var peopleDao: TestPeopleDao
    private val people = PeopleEntity(id = 0, timestamp = 0L, name = "people_0", profilePath = "profilePath_0")

    @Before
    fun setup() {
        peopleDao = TestPeopleDao()
        movieApis = TestPeopleRemoteDataSource()
        datastore = InternalDataSource(
            datastore = InMemoryDataStore(initialValue = InternalDataPreferences.getDefaultInstance())
        )
        userDataRepository = TestUserDataRepository()
        repository = PeopleRepositoryImpl(
            apis = movieApis,
            requestOptionsProvider = DetailRequestOptionsProvider(userdata = userDataRepository),
            peopleDao = peopleDao
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
            expected = peopleDao.isFavoritePeople(id = 0).first(),
            actual = false
        )

        peopleDao.insertOrIgnorePeoples(people = people)

        assertEquals(
            expected = peopleDao.isFavoritePeople(id = 0).first(),
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

        peopleDao.insertOrIgnorePeoples(people = people)

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = people, actual = result.data.first())
    }

    @Test
    fun delete() = runTest {
        val favoritePagerBeforeInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        var result = favoritePagerBeforeInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = true)

        peopleDao.insertOrIgnorePeoples(people = people)

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = people, actual = result.data.first())

        peopleDao.deletePeople(id = people.id)

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

        peopleDao.upsertPeoples(entities = listOf(people))

        val favoritePagerAfterInsert = TestPager(
            config = PagingConfig(pageSize = 0, initialLoadSize = 20, prefetchDistance = 5),
            pagingSource = repository.getFavorite()
        )

        result = favoritePagerAfterInsert.refresh() as PagingSource.LoadResult.Page

        assertEquals(expected = result.data.isEmpty(), actual = false)
        assertEquals(expected = people, actual = result.data.first())
    }

    @Test
    fun getPeopleDetailTest() = runTest {
        val result = repository.getData(id = 0)

        assertEquals(expected = result.first(), actual = peopleDetailTestData)
    }

    @Test
    fun getCombineCreditsTest() = runTest {
        val result = repository.getCombineCredits(0)

        assertEquals(expected = result.first(), actual = combineCreditsTestData)
    }

    @Test
    fun getExternalIdsTest() = runTest {
        val result = repository.getExternalIds(0)

        assertEquals(expected = result.first(), actual = externalIdsTestData)
    }
}