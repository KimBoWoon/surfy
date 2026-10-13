package com.cheeke.surfy.data.repository

import com.cheeke.surfy.testing.TestTrendingRemoteDataSource
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import org.junit.Before
import org.junit.Rule

class HomeRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var repository: HomeRepositoryImpl

    @Before
    fun setup() {
        repository = HomeRepositoryImpl(
            trendingApis = TestTrendingRemoteDataSource()
        )
    }

//    @Test
//    fun getTrendingMovieTest() = runTest {
//        val result = repository.getTrending(MediaType.MOVIE, timeWindow = "day", language = "ko-KR")
//
//        assertEquals(expected = result, actual = testTrendingMovie)
//    }
//
//    @Test
//    fun getTrendingPeopleTest() = runTest {
//        val result = repository.getTrending(MediaType.PEOPLE, timeWindow = "day", language = "ko-KR")
//
//        assertEquals(expected = result, actual = testTrendingPeople)
//    }
//
//    @Test
//    fun getTrendingTvTest() = runTest {
//        val result = repository.getTrending(MediaType.TV, timeWindow = "day", language = "ko-KR")
//
//        assertEquals(expected = result, actual = testTrendingTv)
//    }
}