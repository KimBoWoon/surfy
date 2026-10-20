package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.datamanager.api.TestSurfyAppData
import com.cheeke.surfy.detail.api.TestPeopleRepository
import com.cheeke.surfy.detail.impl.people.GetPeopleDetailUseCase
import com.cheeke.surfy.model.People
import com.cheeke.surfy.testing.model.combineCreditsTestData
import com.cheeke.surfy.testing.model.externalIdsTestData
import com.cheeke.surfy.testing.model.peopleDetailTestData
import com.cheeke.surfy.testing.utils.MainDispatcherRule
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
    private lateinit var repository: TestPeopleRepository
    private lateinit var movieAppDataRepository: TestSurfyAppData
    private lateinit var getPeopleDetailUseCase: GetPeopleDetailUseCase

    @Before
    fun setup() {
        repository = TestPeopleRepository()
        movieAppDataRepository = TestSurfyAppData()
        getPeopleDetailUseCase = GetPeopleDetailUseCase(
            repository = repository,
        )

        runBlocking { repository.insert(media = People(id = 489)) }
    }

    @Test
    fun getPeopleDetailTest() = runTest {
        repository.setPeopleDetail(peopleDetailTestData)
        repository.setCombineCredits(combineCreditsTestData)
        repository.setExternalIds(externalIdsTestData)

        val result = getPeopleDetailUseCase(0)

        assertEquals(
            result.first(),
            peopleDetailTestData
        )

        assertEquals(
            result.first().isFavorite,
            repository.isFavorite(id = 0).first()
        )
    }

    @Test
    fun getFavoritePeopleDetailTest() = runTest {
        repository.setPeopleDetail(peopleDetailTestData)
        repository.setCombineCredits(combineCreditsTestData)
        repository.setExternalIds(externalIdsTestData)
        repository.insert(media = People(id = 0))

        val result = getPeopleDetailUseCase(personId = 0)

        assertEquals(
            result.first(),
            peopleDetailTestData.copy(isFavorite = true)
        )
    }
}