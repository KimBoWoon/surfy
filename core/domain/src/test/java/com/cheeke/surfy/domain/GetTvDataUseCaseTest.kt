package com.cheeke.surfy.domain

import com.cheeke.surfy.model.Tv
import com.cheeke.surfy.model.TvEpisode
import com.cheeke.surfy.model.TvSeasons
import com.cheeke.surfy.testing.repository.TestTvDatabaseRepository
import com.cheeke.surfy.testing.repository.TestTvDetailRepository
import com.cheeke.surfy.testing.repository.TestUserDataRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GetTvDataUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var detailRepository: TestTvDetailRepository
    private lateinit var tvDataBaseRepository: TestTvDatabaseRepository
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
        detailRepository = TestTvDetailRepository()
        tvDataBaseRepository = TestTvDatabaseRepository()
        userDataRepository = TestUserDataRepository()
        getTvDetailUseCase = GetTvDetailUseCase(
            tvDataBaseRepository = tvDataBaseRepository,
            tvRepository = detailRepository
        )
    }

    @Test
    fun getTvDetailTest() = runTest {
        detailRepository.setTv(tv)
        detailRepository.setTvSeason(tvSeasons)
        detailRepository.setTvEpisode(tvEpisode)
        tvDataBaseRepository.insert(media = tv)

        val result = getTvDetailUseCase(id = 0).first()

        assertEquals(
            expected = result.tv,
            actual = tv.copy(isFavorite = true)
        )
    }

    @Test
    fun getFavoriteTvTest() = runTest {
        detailRepository.setTv(tv)
        detailRepository.setTvSeason(tvSeasons)
        detailRepository.setTvEpisode(tvEpisode)
        tvDataBaseRepository.insert(media = Tv(id = 123))

        assertEquals(
            expected = getTvDetailUseCase(id = 0).first().tv.isFavorite,
            actual = false
        )
        tvDataBaseRepository.insert(media = tv)
        assertEquals(
            expected = getTvDetailUseCase(id = 0).first().tv.isFavorite,
            actual = true
        )
    }
}