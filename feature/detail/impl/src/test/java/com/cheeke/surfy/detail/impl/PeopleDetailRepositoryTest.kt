package com.cheeke.surfy.detail.impl

import com.cheeke.surfy.network.api.TestPeopleRemoteDataSource
import com.cheeke.surfy.testing.model.combineCreditsTestData
import com.cheeke.surfy.testing.model.externalIdsTestData
import com.cheeke.surfy.testing.model.peopleDetailTestData
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.userdata.api.TestUserDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class PeopleDetailRepositoryTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var movieApis: TestPeopleRemoteDataSource
    private lateinit var userdata: TestUserDataRepository
    private lateinit var repository: PeopleDetailRepositoryImpl

    @Before
    fun setup() {
        movieApis = TestPeopleRemoteDataSource()
        userdata = TestUserDataRepository()
        repository = PeopleDetailRepositoryImpl(
            apis = movieApis,
            requestOptionsProvider = DetailRequestOptionsProvider(userdataRepository = userdata),
        )
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