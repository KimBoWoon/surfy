package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.detail.api.TestTvRepository
import com.cheeke.surfy.detail.impl.tv.GetTvDetailUseCase
import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GetTvDataUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var repository: TestTvRepository
    private lateinit var getTvDetailUseCase: GetTvDetailUseCase
    private lateinit var userDataRepository: TestUserDataRepository
    private val tv = Tv(id = 0)
    private val tvSeasons = TvSeasons(
        id = 0,
        seasonNumber = 0,
        episodes = emptyList()
    )
    private val tvEpisode = TvEpisode(
        id = 0,
        episodeNumber = 0
    )

    @Before
    fun setup() {
        repository = TestTvRepository()
        userDataRepository = TestUserDataRepository()
        getTvDetailUseCase = GetTvDetailUseCase(
            repository = repository
        )
    }

    @Test
    fun getTvDetailTest() = runTest {
        repository.setTv(tv)
        repository.setTvSeason(tvSeasons)
        repository.setTvEpisode(tvEpisode)
        repository.insert(media = tv)

        val result = getTvDetailUseCase(id = 0).first()

        assertEquals(
            expected = result.tv,
            actual = tv.copy(isFavorite = true)
        )
    }

    @Test
    fun getFavoriteTvTest() = runTest {
        repository.setTv(tv)
        repository.setTvSeason(tvSeasons)
        repository.setTvEpisode(tvEpisode)
        repository.insert(media = Tv(id = 123))

        assertEquals(
            expected = getTvDetailUseCase(id = 0).first().tv.isFavorite,
            actual = false
        )
        repository.insert(media = tv)
        assertEquals(
            expected = getTvDetailUseCase(id = 0).first().tv.isFavorite,
            actual = true
        )
    }
}