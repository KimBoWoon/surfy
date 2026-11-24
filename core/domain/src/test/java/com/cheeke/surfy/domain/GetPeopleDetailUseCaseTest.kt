package com.cheeke.surfy.domain

import com.cheeke.surfy.model.People
import com.cheeke.surfy.testing.model.combineCreditsTestData
import com.cheeke.surfy.testing.model.externalIdsTestData
import com.cheeke.surfy.testing.model.peopleDetailTestData
import com.cheeke.surfy.testing.repository.TestPeopleDatabaseRepository
import com.cheeke.surfy.testing.repository.TestPeopleDetailRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.testing.utils.TestMovieAppDataManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GetPeopleDetailUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var detailRepository: TestPeopleDetailRepository
    private lateinit var peopleDataBaseRepository: TestPeopleDatabaseRepository
    private lateinit var movieAppDataRepository: TestMovieAppDataManager
    private lateinit var getPeopleDetailUseCase: GetPeopleDetailUseCase

    @Before
    fun setup() {
        detailRepository = TestPeopleDetailRepository()
        peopleDataBaseRepository = TestPeopleDatabaseRepository()
        movieAppDataRepository = TestMovieAppDataManager()
        getPeopleDetailUseCase = GetPeopleDetailUseCase(
            detailRepository = detailRepository,
            peopleRepository = peopleDataBaseRepository
        )

        runBlocking { peopleDataBaseRepository.insert(media = People(id = 489)) }
    }

    @Test
    fun getPeopleDetailTest() = runTest {
        detailRepository.setPeopleDetail(peopleDetailTestData)
        detailRepository.setCombineCredits(combineCreditsTestData)
        detailRepository.setExternalIds(externalIdsTestData)

        val result = getPeopleDetailUseCase(0)

        assertEquals(
            result.first(),
            peopleDetailTestData
        )

        assertEquals(
            result.first().isFavorite,
            peopleDataBaseRepository.isFavorite(id = 0).first()
        )
    }

    @Test
    fun getFavoritePeopleDetailTest() = runTest {
        detailRepository.setPeopleDetail(peopleDetailTestData)
        detailRepository.setCombineCredits(combineCreditsTestData)
        detailRepository.setExternalIds(externalIdsTestData)
        peopleDataBaseRepository.insert(media = People(id = 0))

        val result = getPeopleDetailUseCase(personId = 0)

        assertEquals(
            result.first(),
            peopleDetailTestData.copy(isFavorite = true)
        )
    }
}