package com.cheeke.surfy.data.repository

import androidx.paging.PagingSource
import com.cheeke.surfy.data.testdouble.TestKeywordDao
import com.cheeke.surfy.database.model.KeywordEntity
import com.cheeke.surfy.testing.TestSearchRemoteDataSource
import com.cheeke.surfy.testing.model.keywordList
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SearchRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val keywordDao = TestKeywordDao()
    private val repository = SearchRepositoryImpl(keywordDao = keywordDao, searchApis = TestSearchRemoteDataSource())
    private val keyword1 = KeywordEntity(id = 1, keyword = "title_1", timestamp = 1)
    private val keyword2 = KeywordEntity(id = 2, keyword = "title_2", timestamp = 2)

    @Test
    fun getKeywordsTest() = runTest {
        val getKeyword = repository.getKeywords().load(
            params = PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 20,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        assertEquals(
            expected = getKeyword.data,
            actual = keywordList
        )
    }

    @Test
    fun insertTest() = runTest {
        assertEquals(
            expected = keywordDao.entitiesStateFlow.value,
            actual = emptyList()
        )

//        repository.insert(keyword = keyword1.keyword)
        keywordDao.insertOrReplaceKeyword(entity = keyword1)

        assertEquals(
            expected = keywordDao.entitiesStateFlow.value,
            actual = listOf(keyword1)
        )

//        repository.insert(keyword = keyword2.keyword)
        keywordDao.insertOrReplaceKeyword(entity = keyword2)

        assertEquals(
            expected = keywordDao.entitiesStateFlow.value,
            actual = listOf(keyword1, keyword2)
        )
    }

    @Test
    fun deleteAll() = runTest {
//        repository.insert(keyword = keyword1.keyword)
//        repository.insert(keyword = keyword2.keyword)
        keywordDao.insertOrReplaceKeyword(entity = keyword1)
        keywordDao.insertOrReplaceKeyword(entity = keyword2)

        assertEquals(
            expected = keywordDao.entitiesStateFlow.value,
            actual = listOf(keyword1, keyword2)
        )

        keywordDao.deleteAll()

        assertEquals(
            expected = keywordDao.entitiesStateFlow.value,
            actual = emptyList()
        )
    }

    @Test
    fun delete() = runTest {
//        repository.insert(keyword = keyword1.keyword)
//        repository.insert(keyword = keyword2.keyword)
        keywordDao.insertOrReplaceKeyword(entity = keyword1)
        keywordDao.insertOrReplaceKeyword(entity = keyword2)

        assertEquals(
            expected = keywordDao.entitiesStateFlow.value,
            actual = listOf(keyword1, keyword2)
        )

        keywordDao.delete(id = keyword1.id)

        assertEquals(
            expected = keywordDao.entitiesStateFlow.value,
            actual = listOf(keyword2)
        )
    }

//    @Test
//    fun moviePagingTest() = runTest {
//        val pagingSource = repository.getSearchPagingSource(
//            type = SearchType.MOVIE,
//            query = "미션",
//            language = "ko-KR",
//            region = "KR",
//            isAdult = true
//        )
//
//        val actual = pagingSource.load(
//            params = PagingSource.LoadParams.Refresh(
//                key = null,
//                loadSize = 100,
//                placeholdersEnabled = false
//            )
//        )
//
//        assertTrue(actual is PagingSource.LoadResult.Page)
//
//        val page = actual as PagingSource.LoadResult.Page
//
//        assertEquals(expected = movieSearchTestData.results.orEmpty(), actual = page.data)
//        assertEquals(expected = null, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
//
//    @Test
//    fun peoplePagingTest() = runTest {
//        val pagingSource = repository.getSearchPagingSource(
//            type = SearchType.PEOPLE,
//            query = "톰 크루즈",
//            language = "ko-KR",
//            region = "KR",
//            isAdult = true
//        )
//
//        val actual = pagingSource.load(
//            params = PagingSource.LoadParams.Refresh(
//                key = null,
//                loadSize = 100,
//                placeholdersEnabled = false
//            )
//        )
//
//        assertTrue(actual is PagingSource.LoadResult.Page)
//
//        val page = actual as PagingSource.LoadResult.Page
//
//        assertEquals(expected = peopleSearchTestData.results.orEmpty(), actual = page.data)
//        assertEquals(expected = null, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
//
//    @Test
//    fun tvPagingTest() = runTest {
//        val pagingSource = repository.getSearchPagingSource(
//            type = SearchType.TV,
//            query = "톰 크루즈",
//            language = "ko-KR",
//            region = "KR",
//            isAdult = true
//        )
//
//        val actual = pagingSource.load(
//            params = PagingSource.LoadParams.Refresh(
//                key = null,
//                loadSize = 100,
//                placeholdersEnabled = false
//            )
//        )
//
//        assertTrue(actual is PagingSource.LoadResult.Page)
//
//        val page = actual as PagingSource.LoadResult.Page
//
//        assertEquals(expected = tvSearchTestData.results.orEmpty(), actual = page.data)
//        assertEquals(expected = null, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
//
//    @Test
//    fun seriesPagingTest() = runTest {
//        val pagingSource = repository.getSearchPagingSource(
//            type = SearchType.SERIES,
//            query = "톰 크루즈",
//            language = "ko-KR",
//            region = "KR",
//            isAdult = true
//        )
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
//        assertEquals(expected = seriesSearchTestData.results.orEmpty(), actual = page.data)
//        assertEquals(expected = null, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
//
//    @Test
//    fun multiPagingTest() = runTest {
//        val pagingSource = repository.getSearchPagingSource(
//            type = SearchType.MULTI,
//            query = "톰 크루즈",
//            language = "ko-KR",
//            region = "KR",
//            isAdult = true
//        )
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
//        assertEquals(expected = movieSearchTestData.results.orEmpty(), actual = page.data)
//        assertEquals(expected = null, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
//
//    @Test
//    fun recommendKeywordPagingTest() = runTest {
//        val pagingSource = repository.getRecommendKeywordPagingSource(query = "미션")
//
//        val actual = pagingSource.load(
//            params = PagingSource.LoadParams.Refresh(
//                key = null,
//                loadSize = 6,
//                placeholdersEnabled = false
//            )
//        )
//
//        assertTrue(actual is PagingSource.LoadResult.Page)
//
//        val page = actual as PagingSource.LoadResult.Page
//
//        assertEquals(expected = testRecommendedKeyword, actual = page.data)
//        assertEquals(expected = null, actual = page.prevKey)
//        assertEquals(expected = null, actual = page.nextKey)
//    }
}